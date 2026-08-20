package workflow

import (
	"context"
	model "github.com/apache/airavata/api/process/model"
	service "github.com/apache/airavata/api/process/service"
	"github.com/apache/airavata/internal/orchestration/activities"
	backend "github.com/cschleiden/go-workflows/backend"
	worker "github.com/cschleiden/go-workflows/worker"
	workflow "github.com/cschleiden/go-workflows/workflow"
)

func SubmitBatchJobWorkflow(ctx workflow.Context, batchJobProcess model.BatchJobProcess, dataStagingTaskService service.DataStagingTaskService) error {

	serviceCtx := context.Background()

	dataStagingTasks, err := dataStagingTaskService.ListForProcess(serviceCtx, batchJobProcess.ID)

	if err != nil {
		return err
	}

	for _, dataStagingTask := range dataStagingTasks {
		workflow.ExecuteActivity[int](ctx, workflow.ActivityOptions{}, activities.CopyData, dataStagingTask)
	}

	workflow.ExecuteActivity[int](ctx, workflow.ActivityOptions{}, activities.CopyData, batchJobProcess)
	_, err = workflow.ExecuteActivity[int](ctx, workflow.ActivityOptions{}, activities.SubmitBatchJob, batchJobProcess).Get(ctx)
	return err
}

func RunWorker(ctx context.Context, mb backend.Backend) error {
	w := worker.New(mb, nil)

	w.RegisterWorkflow(SubmitBatchJobWorkflow)
	w.RegisterActivity(activities.CopyData)
	w.RegisterActivity(activities.SubmitBatchJob)
	w.RegisterActivity(activities.CancelBatchJob)
	w.RegisterActivity(activities.MonitorBatchJob)

	if err := w.Start(ctx); err != nil {
		return err
	}

	return nil
}
