package orchestration

import (
	"context"
	"fmt"
	"log/slog"

	model "github.com/apache/airavata/api/process/model"
)

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

func (a *ExecutionEngine) submitBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to submit batch job", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job submission task for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed submitting batch job", "taskId", taskID, "processId", processID, "deploymentId", process.BatchProcess.DeploymentID, "JST Id", jst.ID)
	return executionContext, nil
}

func (a *ExecutionEngine) CancelBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to cancel batch job", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for cancelling batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job submission task for cancelling batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed cancelling batch job", "taskId", taskID, "processId", processID, "jobId", process.BatchProcess.JobID, "JST Id", jst.ID)
	return executionContext, nil
}

func (a *ExecutionEngine) monitorBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to monitor batch job ", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jmt, err := a.jobMonitoringTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job monitoring task for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed monitoring batch job", "taskId", taskID, "processId", processID, "jobId", process.BatchProcess.JobID, "JMT Id", jmt.ID)
	return executionContext, nil
}

func (a *ExecutionEngine) process(ctx context.Context, processID string) (*model.Process, error) {
	process, err := a.processes.FindByID(ctx, processID)
	if err != nil {
		return nil, err
	}
	if process.BatchProcess == nil {
		return nil, fmt.Errorf("process %s carries no batch process section", processID)
	}
	return process, nil
}
