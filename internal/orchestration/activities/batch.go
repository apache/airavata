package activities

import (
	"context"
	"fmt"
	"log/slog"

	dto "github.com/apache/airavata/api/process/dto"
)

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

func (a *Activities) SubmitBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jst, err := a.svcs.JobSubmissionTask.Get(ctx, processID, taskID)
	if err != nil {
		return 0, err
	}
	slog.Info("Submitting batch job ...", "processId", processID, "deploymentId", process.BatchProcess.DeploymentID, "JST Id", jst.ID)
	return 0, nil
}

func (a *Activities) CancelBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jst, err := a.svcs.JobSubmissionTask.Get(ctx, processID, taskID)
	if err != nil {
		return 0, err
	}
	slog.Info("Cancelling batch job ...", "processId", processID, "jobId", process.BatchProcess.JobID, "JST Id", jst.ID)
	return 0, nil
}

func (a *Activities) MonitorBatchJob(ctx context.Context, processID string, taskID string) (int, error) {
	process, err := a.process(ctx, processID)
	if err != nil {
		return 0, err
	}
	jmt, err := a.svcs.JobMonitoringTask.Get(ctx, processID, taskID)
	if err != nil {
		return 0, err
	}
	slog.Info("Monitoring batch job ...", "processId", processID, "jobId", process.BatchProcess.JobID, "JMT Id", jmt.ID)
	return 0, nil
}

func (a *Activities) process(ctx context.Context, processID string) (*dto.Response, error) {
	process, err := a.svcs.Process.Get(ctx, processID)
	if err != nil {
		return nil, err
	}
	if process.BatchProcess == nil {
		return nil, fmt.Errorf("process %s carries no batch process section", processID)
	}
	return process, nil
}
