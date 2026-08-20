package activities

import (
	"context"
	model "github.com/apache/airavata/api/process/model"
	"log/slog"
)

func CopyData(ctx context.Context, dataStagingTask model.DataStagingTask) (int, error) {

	slog.Info("Copying data ...")

	return 0, nil
}
