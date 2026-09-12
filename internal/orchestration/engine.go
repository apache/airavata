package orchestration

import (
	"context"

	"github.com/cschleiden/go-workflows/backend/sqlite"
	"github.com/cschleiden/go-workflows/client"
	"github.com/cschleiden/go-workflows/worker"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/google/uuid"

	"log/slog"

	computerepo "github.com/apache/airavata/api/compute/repository"
	datastorerepo "github.com/apache/airavata/api/data/repository"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
)

type ExecutionEngine struct {
	dataStagingTasks    *processrepo.DataStagingTaskRepository
	jobSubmissionTasks  *processrepo.JobSubmissionTaskRepository
	jobMonitoringTasks  *processrepo.JobMonitoringTaskRepository
	scpStorages         *datastorerepo.SCPDataStorageRepository
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository
	processes           *processrepo.ProcessRepository
	orchestrator        *worker.WorkflowOrchestrator
}

type ExecutionContext struct {
	data map[string]interface{}
}

// NewExecutionEngine returns the workflow set scheduling acts.
func NewExecutionEngine(dataStagingTasks *processrepo.DataStagingTaskRepository,
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository,
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository,
	scpStorages *datastorerepo.SCPDataStorageRepository,
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository,
	processes *processrepo.ProcessRepository,
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
		orchestrator:        orchestrator,
	}
}

func (w *ExecutionEngine) StartEngine() {

	slog.Info("Starting execution engine...........")
	ctx := context.Background()
	w.orchestrator.RegisterWorkflow(w.submitProcessExecution)
	w.orchestrator.RegisterActivity(w.copyData)
	w.orchestrator.RegisterActivity(w.submitBatchJob)
	w.orchestrator.RegisterActivity(w.monitorBatchJob)

	if err := w.orchestrator.Start(ctx); err != nil {
		slog.Error("Failed to start execution engine", "error", err)
		panic("Could not start execution engine")
	}
}

func (w *ExecutionEngine) LaunchProcessExecution(ctx context.Context, processID string) (string, error) {
	workflowId := uuid.NewString()

	_, err := w.orchestrator.CreateWorkflowInstance(ctx, client.WorkflowInstanceOptions{
		InstanceID: workflowId,
	}, w.submitProcessExecution, processID)
	if err != nil {
		return "", err
	}
	return workflowId, nil
}

func (w *ExecutionEngine) submitProcessExecution(ctx workflow.Context, processID string) error {

	ctxInt := context.Background()
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

	jmts, err := w.jobMonitoringTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job monitoring tasks", "processId", processID, "error", err)
		return err
	}

	currentOrder := 0
	isPending := true
	plannedTasks := []string{}

	// This is a temp hook to initialize the execution context for the workflow
	executionContext := &ExecutionContext{
		data: make(map[string]interface{}),
	}

	slog.Info("Starting task planning for process", "processId", processID)
	for isPending {
		isPending = false
		for _, dst := range dsts {
			if dst.TaskOrder != nil && *dst.TaskOrder == currentOrder {
				plannedTasks = append(plannedTasks, dst.ID)
				executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, dst.RetryCount)}, w.copyData, executionContext, processID, dst.ID).Get(ctx)
				if err != nil {
					return err
				}
			} else if dst.TaskOrder != nil && *dst.TaskOrder > currentOrder {
				isPending = true
			}
		}

		for _, jst := range jsts {
			if jst.TaskOrder != nil && *jst.TaskOrder == currentOrder {
				plannedTasks = append(plannedTasks, jst.ID)
				executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jst.OnFailure, jst.RetryCount)}, w.submitBatchJob, executionContext, processID, jst.ID).Get(ctx)
				if err != nil {
					return err
				}
			} else if jst.TaskOrder != nil && *jst.TaskOrder > currentOrder {
				isPending = true
			}
		}

		for _, jmt := range jmts {
			if jmt.TaskOrder != nil && *jmt.TaskOrder == currentOrder {
				plannedTasks = append(plannedTasks, jmt.ID)
				executionContext, err = workflow.ExecuteActivity[*ExecutionContext](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jmt.OnFailure, jmt.RetryCount)}, w.monitorBatchJob, executionContext, processID, jmt.ID).Get(ctx)
				if err != nil {
					return err
				}
			} else if jmt.TaskOrder != nil && *jmt.TaskOrder > currentOrder {
				isPending = true
			}
		}

		currentOrder += 1

		if !isPending {
			slog.Info("All task planning completed for process", "processId", processID)
			slog.Info("Planned tasks for process", "processId", processID, "plannedTasks", plannedTasks)
		}
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
