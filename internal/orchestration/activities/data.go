package activities

import (
	"context"
	"log/slog"
)

func (a *Activities) CopyData(ctx context.Context, processId string, dataStagingTaskId string) (int, error) {
	dataStagingTask, err := a.svcs.DataStagingTask.Get(ctx, processId, dataStagingTaskId)
	if err != nil {
		return 0, err
	}
	slog.Info("Copying data ...", "taskId", dataStagingTask.ID, "processId", dataStagingTask.ProcessID)

	return 0, nil
}
