package orchestration

import (
	"context"
	"log/slog"
	"os"

	"github.com/google/uuid"

	datamodel "github.com/apache/airavata/api/data/model"
)

type FileMetadata struct {
	Path string
	Size int64
	Mode os.FileMode
}

func (a *ExecutionEngine) copyData(ctx context.Context, executionContext *ExecutionContext, processId string, dataStagingTaskId string) (*ExecutionContext, error) {
	dataStagingTask, err := a.dataStagingTasks.FindByIDAndProcessID(ctx, dataStagingTaskId, processId)
	if err != nil {
		return nil, err
	}
	slog.Info("Starting data staging task", "taskId", dataStagingTask.ID, "processId", dataStagingTask.ProcessID)

	localTempDataPath := "/tmp/" + dataStagingTask.ID + "-" + uuid.New().String()
	defer os.RemoveAll(localTempDataPath) // Make sure to clean up the temporary data path after the function completes

	if *dataStagingTask.SourceDataStorageType == datamodel.DataStorageTypeSCP {

		s, err := a.scpStorages.FindByID(ctx, *dataStagingTask.SourceDataStorageID)
		if err != nil {
			return nil, err
		}

		err = DownloadFileFromSCP(ctx, *s.HostName, *s.Port, *s.LoginUser, *s.SSHKey,
			*dataStagingTask.SourcePath, localTempDataPath)
		if err != nil {
			slog.Error("Failed to download file through SCP", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"sourcePath", *dataStagingTask.SourcePath, "destinationPath", localTempDataPath, "error", err)
			return nil, err
		}
		slog.Info("Successfully downloaded file through SCP", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
			"sourcePath", *dataStagingTask.SourcePath, "destinationPath", localTempDataPath)
	}

	if *dataStagingTask.SourceDataStorageType == datamodel.DataStorageTypeHPC {
		s, err := a.slurmClusterConfigs.FindByID(ctx, *dataStagingTask.SourceDataStorageID)
		if err != nil {
			return nil, err
		}

		err = DownloadFileFromSCP(ctx, s.SlurmCluster.HeadnodeHost, s.SlurmCluster.HeadnodePort, s.LoginUser,
			*s.SSHKey, *dataStagingTask.SourcePath, localTempDataPath)
		if err != nil {
			slog.Error("Failed to download file from HPC", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"sourcePath", *dataStagingTask.SourcePath, "destinationPath", localTempDataPath, "error", err)
			return nil, err
		}
		slog.Info("Successfully downloaded file from HPC", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
			"sourcePath", *dataStagingTask.SourcePath, "destinationPath", localTempDataPath)
	}

	if *dataStagingTask.DestinationDataStorageType == datamodel.DataStorageTypeSCP {
		s, err := a.scpStorages.FindByID(ctx, *dataStagingTask.DestinationDataStorageID)
		if err != nil {
			return nil, err
		}

		err = UploadFileToSCP(ctx, *s.HostName, *s.Port, *s.LoginUser, *s.SSHKey, localTempDataPath, *dataStagingTask.DestinationPath)
		if err != nil {
			slog.Error("Failed to upload file through SCP", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"sourcePath", localTempDataPath, "destinationPath", *dataStagingTask.DestinationPath, "error", err)
			return nil, err
		}
		slog.Info("Successfully uploaded file through SCP", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
			"sourcePath", localTempDataPath, "destinationPath", *dataStagingTask.DestinationPath)

	}

	if *dataStagingTask.DestinationDataStorageType == datamodel.DataStorageTypeHPC {
		s, err := a.slurmClusterConfigs.FindByID(ctx, *dataStagingTask.DestinationDataStorageID)
		if err != nil {
			return nil, err
		}

		err = UploadFileToSCP(ctx, s.SlurmCluster.HeadnodeHost, s.SlurmCluster.HeadnodePort, s.LoginUser,
			*s.SSHKey, localTempDataPath, *dataStagingTask.DestinationPath)
		if err != nil {
			slog.Error("Failed to upload file to HPC", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"sourcePath", localTempDataPath, "destinationPath", *dataStagingTask.DestinationPath, "error", err)
			return nil, err
		}
		slog.Info("Successfully uploaded file to HPC", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
			"sourcePath", localTempDataPath, "destinationPath", *dataStagingTask.DestinationPath)
	}

	return executionContext, nil
}
