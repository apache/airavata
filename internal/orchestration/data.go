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

		dialConfig := ssh.SSHDialConfig{
			Host:     *s.HostName,
			Port:     *s.Port,
			Username: *s.LoginUser,
			Cred:     *s.SSHCredential,
		}

		err = ssh.DownloadFileFromSCP(ctx, dialConfig,
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

		dialConfig := ssh.SSHDialConfig{
			Host:     s.SlurmCluster.HeadnodeHost,
			Port:     s.SlurmCluster.HeadnodePort,
			Username: s.LoginUser,
			Cred:     *s.SSHCredential,
		}

		err = ssh.DownloadFileFromSCP(ctx, dialConfig, *dataStagingTask.SourcePath, localTempDataPath)
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

		dialConfig := ssh.SSHDialConfig{
			Host:     *s.HostName,
			Port:     *s.Port,
			Username: *s.LoginUser,
			Cred:     *s.SSHCredential,
		}

		// Ensure the destination directory exists before attempting the SCP upload.
		if err := ssh.CreateDirectorySSH(ctx, dialConfig, filepath.Dir(*dataStagingTask.DestinationPath)); err != nil {
			slog.Error("Failed to create destination directory through SSH", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"destinationDirectory", filepath.Dir(*dataStagingTask.DestinationPath), "error", err)
			return nil, err
		}

		err = ssh.UploadFileToSCP(ctx, dialConfig, localTempDataPath, *dataStagingTask.DestinationPath)
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
		dialConfig := ssh.SSHDialConfig{
			Host:     s.SlurmCluster.HeadnodeHost,
			Port:     s.SlurmCluster.HeadnodePort,
			Username: s.LoginUser,
			Cred:     *s.SSHCredential,
		}
		if err := ssh.CreateDirectorySSH(ctx, dialConfig, filepath.Dir(*dataStagingTask.DestinationPath)); err != nil {
			slog.Error("Failed to create destination directory on HPC through SSH", "taskId", dataStagingTask.ID, "processId", *dataStagingTask.ProcessID,
				"destinationDirectory", filepath.Dir(*dataStagingTask.DestinationPath), "error", err)
			return nil, err
		}

		err = ssh.UploadFileToSCP(ctx, dialConfig, localTempDataPath, *dataStagingTask.DestinationPath)
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
