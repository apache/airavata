/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package orchestration

import (
	"context"
	"log/slog"
	"os"

	"github.com/google/uuid"
	"path/filepath"

	datamodel "github.com/apache/airavata/api/data/model"
	ssh "github.com/apache/airavata/internal/ssh"
)

func (a *ExecutionEngine) copyData(ctx context.Context, executionContext *ExecutionContext, processId string, dataStagingTaskId string) (*ExecutionContext, error) {

	process, err := a.process(ctx, processId)
	if err != nil {
		slog.Error("Failed to retrieve process for submitting batch job", "taskId", dataStagingTaskId, "processId", processId, "error", err)
		return nil, err
	}

	dataStagingTask, err := a.dataStagingTasks.FindByIDAndProcessID(ctx, dataStagingTaskId, processId)
	if err != nil {
		return nil, err
	}
	slog.Info("Starting data staging task", "taskId", dataStagingTaskId, "processId", processId)

	localTempDataPath := "/tmp/" + dataStagingTask.ID + "-" + uuid.New().String()
	defer os.RemoveAll(localTempDataPath) // Make sure to clean up the temporary data path after the function completes

	if *dataStagingTask.SourceDataStorageType == datamodel.DataStorageTypeSCP {

		s, err := a.scpStorages.FindByID(ctx, *dataStagingTask.SourceDataStorageID)
		if err != nil {
			return nil, err
		}

		dialConfig := ssh.NewSSHDialConfig(
			*s.HostName,
			*s.Port,
			*s.LoginUser,
			*s.SSHCredential,
			*process.OwnerID,
		)

		err = ssh.DownloadFileFromSCP(ctx, dialConfig, a.sshContext, *dataStagingTask.SourcePath, localTempDataPath)
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

		dialConfig := ssh.NewSSHDialConfig(
			s.SlurmCluster.HeadnodeHost,
			s.SlurmCluster.HeadnodePort,
			s.LoginUser,
			*s.SSHCredential,
			*process.OwnerID,
		)

		err = ssh.DownloadFileFromSCP(ctx, dialConfig, a.sshContext, *dataStagingTask.SourcePath, localTempDataPath)
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

		dialConfig := ssh.NewSSHDialConfig(
			*s.HostName,
			*s.Port,
			*s.LoginUser,
			*s.SSHCredential,
			*process.OwnerID,
		)

		// Ensure the destination directory exists before attempting the SCP upload.
		if err := ssh.CreateDirectorySSH(ctx, dialConfig, a.sshContext, filepath.Dir(*dataStagingTask.DestinationPath)); err != nil {
			slog.Error("Failed to create destination directory through SSH", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"destinationDirectory", filepath.Dir(*dataStagingTask.DestinationPath), "error", err)
			return nil, err
		}

		err = ssh.UploadFileToSCP(ctx, dialConfig, a.sshContext, localTempDataPath, *dataStagingTask.DestinationPath)
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

		// Ensure the destination directory exists before attempting the SCP upload.
		dialConfig := ssh.NewSSHDialConfig(
			s.SlurmCluster.HeadnodeHost,
			s.SlurmCluster.HeadnodePort,
			s.LoginUser,
			*s.SSHCredential,
			*process.OwnerID,
		)
		if err := ssh.CreateDirectorySSH(ctx, dialConfig, a.sshContext, filepath.Dir(*dataStagingTask.DestinationPath)); err != nil {
			slog.Error("Failed to create destination directory on HPC through SSH", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"destinationDirectory", filepath.Dir(*dataStagingTask.DestinationPath), "error", err)
			return nil, err
		}

		err = ssh.UploadFileToSCP(ctx, dialConfig, a.sshContext, localTempDataPath, *dataStagingTask.DestinationPath)
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
