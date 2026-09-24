package orchestration

import (
	"context"
	"fmt"
	"github.com/cschleiden/go-workflows/backend/sqlite"
	"github.com/cschleiden/go-workflows/client"
	"github.com/cschleiden/go-workflows/worker"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/google/uuid"

	"log/slog"

	applicationrepo "github.com/apache/airavata/api/application/repository"
	computerepo "github.com/apache/airavata/api/compute/repository"
	datastorerepo "github.com/apache/airavata/api/data/repository"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/config"
)

type ExecutionEngine struct {
	dataStagingTasks    *processrepo.DataStagingTaskRepository
	jobSubmissionTasks  *processrepo.JobSubmissionTaskRepository
	jobMonitoringTasks  *processrepo.JobMonitoringTaskRepository
	scpStorages         *datastorerepo.SCPDataStorageRepository
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository
	processes           *processrepo.ProcessRepository
	batchDeployments    *applicationrepo.BatchDeploymentRepository
	templates           *applicationrepo.TemplateRepository
	orchestrator        *worker.WorkflowOrchestrator
	batchStatus         *processrepo.BatchJobStatusRepository
}

type GlobalJobConfigs struct {
	MailUser string
}

type ExecutionContext struct {
	GlobalJobConfigs *GlobalJobConfigs      `json:"globalJobConfigs,omitempty"`
	Data             map[string]interface{} `json:"data,omitempty"`
	SomeData         string                 `json:"someData,omitempty"`
}

// set records a value under key for the rest of the run to read.
//
// The map is created on demand rather than assumed: an activity is handed a context
// rebuilt from what crossed the workflow backend, so one that writes cannot count on
// finding the map the workflow started with.
func (c *ExecutionContext) set(key string, value interface{}) {
	if c.Data == nil {
		c.Data = make(map[string]interface{})
	}
	c.Data[key] = value
}

// NewExecutionEngine returns the workflow set scheduling acts.
func NewExecutionEngine(dataStagingTasks *processrepo.DataStagingTaskRepository,
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository,
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository,
	scpStorages *datastorerepo.SCPDataStorageRepository,
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository,
	processes *processrepo.ProcessRepository,
	batchDeployments *applicationrepo.BatchDeploymentRepository,
	templates *applicationrepo.TemplateRepository,
	batchStatus *processrepo.BatchJobStatusRepository,
) *ExecutionEngine {

	backend := sqlite.NewSqliteBackend("/tmp/airavataorchestrator.sqlite")
	orchestrator := worker.NewWorkflowOrchestrator(backend, nil)

	return &ExecutionEngine{
		dataStagingTasks:    dataStagingTasks,
		jobSubmissionTasks:  jobSubmissionTasks,
		jobMonitoringTasks:  jobMonitoringTasks,
		scpStorages:         scpStorages,
		slurmClusterConfigs: slurmClusterConfigs,
		processes:           processes,
		batchDeployments:    batchDeployments,
		templates:           templates,
		orchestrator:        orchestrator,
		batchStatus:         batchStatus,
	}
}

func (w *ExecutionEngine) StartEngine() {

	slog.Info("Starting execution engine...........")
	ctx := context.Background()
	w.orchestrator.RegisterWorkflow(w.handleBatchJobSubmission)
	w.orchestrator.RegisterWorkflow(w.handleBatchJobCompletion)
	w.orchestrator.RegisterActivity(w.copyData)
	w.orchestrator.RegisterActivity(w.submitBatchJob)
	w.orchestrator.RegisterActivity(w.monitorBatchJob)

	if err := w.orchestrator.Start(ctx); err != nil {
		slog.Error("Failed to start execution engine", "error", err)
		panic("Could not start execution engine")
	}
}

func (w *ExecutionEngine) HandleBatchJobEmailResponse(ctx context.Context, email Email) error {
	slog.Info("Handling batch job email response", "from", email.From, "subject", email.Subject)
	return nil
}

func (w *ExecutionEngine) LaunchBatchJobSubmission(ctx context.Context, processID string) (string, error) {
	workflowId := uuid.NewString()

	_, err := w.orchestrator.CreateWorkflowInstance(ctx, client.WorkflowInstanceOptions{
		InstanceID: workflowId,
	}, w.handleBatchJobSubmission, processID)
	if err != nil {
		return "", err
	}
	return workflowId, nil
}

func (w *ExecutionEngine) LaunchBatchJobCompletion(ctx context.Context, processID string) (string, error) {
	workflowId := uuid.NewString()

	_, err := w.orchestrator.CreateWorkflowInstance(ctx, client.WorkflowInstanceOptions{
		InstanceID: workflowId,
	}, w.handleBatchJobCompletion, processID)
	if err != nil {
		return "", err
	}
	return workflowId, nil
}

func (w *ExecutionEngine) handleBatchJobCompletion(ctx workflow.Context, processID string) error {
	// Implement the logic for batch job completion workflow here

	ctxInt := context.Background()
	dsts, err := w.dataStagingTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list data staging tasks", "processId", processID, "error", err)
		return err
	}

	jmts, err := w.jobMonitoringTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job monitoring tasks", "processId", processID, "error", err)
		return err
	}

	if len(jmts) == 0 {
		slog.Warn("No job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("No job monitoring tasks found for process %s", processID)
	}

	globalJobConfigs, err := getGlobalJobConfig()
	if err != nil {
		slog.Error("Failed to get global job config", "processId", processID, "error", err)
		return err
	}

	executionContext := &ExecutionContext{
		GlobalJobConfigs: globalJobConfigs,
		Data:             make(map[string]interface{}),
	}

	if len(jmts) == 0 {
		slog.Warn("No job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("No job monitoring tasks found for process %s", processID)
	}

	if len(jmts) > 1 {
		slog.Warn("Multiple job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("Multiple job monitoring tasks found for process %s", processID)
	}

	jmt := jmts[0]

	executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
		ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jmt.OnFailure, jmt.RetryCount)},
		w.submitBatchJob, executionContext, processID, jmt.ID).Get(ctx)
	if err != nil {
		slog.Error("Failed processing job monitoring task", "processId", processID, "taskId", jmt.ID, "error", err)
		return err
	}

	for _, dst := range dsts { // Attach output staging tasks
		if dst.TaskOrder != nil && *dst.TaskOrder > *jmt.TaskOrder {
			executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
				ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, dst.RetryCount)},
				w.copyData, executionContext, processID, dst.ID).Get(ctx)
			if err != nil {
				slog.Error("Failed processing data staging task", "processId", processID, "taskId", dst.ID, "error", err)
				return err
			}
		}
	}

	return nil
}

func (w *ExecutionEngine) handleBatchJobSubmission(ctx workflow.Context, processID string) error {

	ctxInt := context.Background()

	// dsts are ordered by their task order
	dsts, err := w.dataStagingTasks.FindByProcessID(ctxInt, processID)

	if err != nil {
		slog.Error("Failed to list data staging tasks", "processId", processID, "error", err)
		return err
	}

	jsts, err := w.jobSubmissionTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job submission tasks", "processId", processID, "error", err)
		return err
	}

	if len(jsts) == 0 {
		slog.Error("No job submission tasks found for process", "processId", processID)
		return fmt.Errorf("No job submission tasks found for process %s", processID)
	}

	if len(jsts) > 1 {
		slog.Error("Multiple job submission tasks found for process", "processId", processID)
		return fmt.Errorf("Multiple job submission tasks found for process %s", processID)
	}
	// At this point, we are guaranteed to have exactly one job submission task.

	jst := jsts[0]

	globalJobConfigs, err := getGlobalJobConfig()
	if err != nil {
		slog.Error("Failed to get global job config", "processId", processID, "error", err)
		return err
	}

	executionContext := &ExecutionContext{
		Data:             make(map[string]interface{}),
		GlobalJobConfigs: globalJobConfigs,
		SomeData:         "Fooooo",
	}

	for _, dst := range dsts { // tasks are already sorted by their task order. Attach input staging tasks
		if dst.TaskOrder != nil && *dst.TaskOrder < *jst.TaskOrder {
			executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
				ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, dst.RetryCount)},
				w.copyData, executionContext, processID, dst.ID).Get(ctx)
			if err != nil {
				slog.Error("Failed processing data staging task", "processId", processID, "taskId", dst.ID, "error", err)
				return err
			}
		}
	}

	executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
		ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jst.OnFailure, jst.RetryCount)},
		w.submitBatchJob, executionContext, processID, jst.ID).Get(ctx)
	if err != nil {
		slog.Error("Failed processing job submission task", "processId", processID, "taskId", jst.ID, "error", err)
		return err
	}

	return nil
}

func retryOptions(onFailure model.OnFailureAction, retryCount *int) workflow.RetryOptions {
	opts := workflow.RetryOptions{MaxAttempts: 1}
	if onFailure != model.OnFailureActionRetry {
		return opts
	}
	// retryCount counts retries, so the first run is one attempt on top of it.
	opts.MaxAttempts = 1 + *retryCount
	opts.BackoffCoefficient = workflow.DefaultRetryOptions.BackoffCoefficient
	return opts
}

func getGlobalJobConfig() (*GlobalJobConfigs, error) {
	cfg, err := config.FetchSystemConfigs()
	if err != nil {
		return nil, err
	}
	return &GlobalJobConfigs{
		MailUser: cfg.EmailMonitorAddress,
	}, nil
}
