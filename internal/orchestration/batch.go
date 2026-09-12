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

func (a *ExecutionEngine) submitBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jst, err := a.jobMonitoringTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		return 0, err
	}
	slog.Info("Submitting batch job ...", "processId", processID, "deploymentId", process.BatchProcess.DeploymentID, "JST Id", jst.ID)
	return 0, nil
}

func (a *ExecutionEngine) CancelBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		return 0, err
	}
	slog.Info("Cancelling batch job ...", "processId", processID, "jobId", process.BatchProcess.JobID, "JST Id", jst.ID)
	return 0, nil
}

func (a *ExecutionEngine) monitorBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jmt, err := a.jobMonitoringTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		return 0, err
	}
	slog.Info("Monitoring batch job ...", "processId", processID, "jobId", process.BatchProcess.JobID, "JMT Id", jmt.ID)
	return 0, nil
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
