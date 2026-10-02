package orchestration

import (
	"context"
	slog "log/slog"
	model "github.com/apache/airavata/api/process/model"
	"time"
	internalptr "github.com/apache/airavata/internal/ptr"

)

func (a *ExecutionEngine) completeProcess(ctx context.Context, executionContext *ExecutionContext, processID string) (*ExecutionContext, error) {
	slog.Info("Completing process", "processID", processID)

	runningStatus := model.ProcessStatusTypeCompleted
	if err := a.processStatus.Create(ctx, &model.ProcessStatus{
		ProcessID: &processID,
		Status:    &runningStatus,
		Log:       internalptr.To("Process has completed"),
		Timestamp: internalptr.To(time.Now().UnixMilli()),
	}); err != nil {
		slog.Error("Failed to create process status", "processID", processID, "error", err)
		return nil, err
	}
	return executionContext, nil
}
