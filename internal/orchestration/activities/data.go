package activities

import (
	"context"
	"log/slog"
	"os"

	datamodel "github.com/apache/airavata/api/data/model"
)

type FileMetadata struct {
	Path string
	Size int64
	Mode os.FileMode
}

func (a *Activities) CopyData(ctx context.Context, processId string, dataStagingTaskId string) (int, error) {
	dataStagingTask, err := a.svcs.DataStagingTask.Get(ctx, processId, dataStagingTaskId)
	if err != nil {
		return 0, err
	}
	slog.Info("Copying data ...", "taskId", dataStagingTask.ID, "processId", dataStagingTask.ProcessID)

	if *dataStagingTask.SourceDataStorageType == datamodel.DataStorageTypeSCP {

	}

	if *dataStagingTask.SourceDataStorageType == datamodel.DataStorageTypeHPC {

	}

	if *dataStagingTask.DestinationDataStorageType == datamodel.DataStorageTypeSCP {

	}

	if *dataStagingTask.DestinationDataStorageType == datamodel.DataStorageTypeHPC {

	}

	return 0, nil
}
