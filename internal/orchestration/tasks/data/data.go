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

// Package data holds the task that stages a file between two storages, either of which
// may be a cluster reached over SSH.
package data

import (
	"context"
	"log/slog"
	"os"

	"github.com/google/uuid"
	"path/filepath"

	computerepo "github.com/apache/airavata/api/compute/repository"
	datamodel "github.com/apache/airavata/api/data/model"
	datastorerepo "github.com/apache/airavata/api/data/repository"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/orchestration/execution"
	ssh "github.com/apache/airavata/internal/ssh"
)

// Tasks carries out the data staging tasks of a process. Its methods are workflow
// activities.
type Tasks struct {
	processes           *processrepo.ProcessRepository
	dataStagingTasks    *processrepo.DataStagingTaskRepository
	scpStorages         *datastorerepo.SCPDataStorageRepository
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository
	sshContext          *ssh.GlobalSSHContext
}

// New returns the data staging tasks over the given repositories.
func New(processes *processrepo.ProcessRepository,
	dataStagingTasks *processrepo.DataStagingTaskRepository,
	scpStorages *datastorerepo.SCPDataStorageRepository,
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository,
	sshContext *ssh.GlobalSSHContext,
) *Tasks {
	return &Tasks{
		processes:           processes,
		dataStagingTasks:    dataStagingTasks,
		scpStorages:         scpStorages,
		slurmClusterConfigs: slurmClusterConfigs,
		sshContext:          sshContext,
	}
}

func (a *Tasks) CopyData(ctx context.Context, executionContext *execution.Context, processId string, dataStagingTaskId string) (*execution.Context, error) {

	process, err := execution.FindBatchProcess(ctx, a.processes, processId)
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
