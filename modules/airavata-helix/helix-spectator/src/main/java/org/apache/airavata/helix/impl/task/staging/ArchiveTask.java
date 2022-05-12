/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.staging;

import org.apache.airavata.agents.api.*;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

@TaskDef(name = "Archival Task")
public class ArchiveTask extends DataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(ArchiveTask.class);
    private final static long MAX_ARCHIVE_SIZE = 1024L * 1024L * 1024L * 20L; // 20GB
    private final static CountMonitor archiveTaskCounter = new CountMonitor("archive_task_counter");


    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        logger.info("Starting archival task " + getTaskId() + " in experiment " + getExperimentId());
        archiveTaskCounter.inc();
        saveAndPublishProcessStatus(ProcessState.OUTPUT_DATA_STAGING);

        try {

            // Get and validate data staging task model
            DataStagingTaskModel dataStagingTaskModel = getDataStagingTaskModel();

            // Fetch and validate source and destination URLS
            URI sourceURI;
            String tarDirPath;
            String tarCreationAbsPath;
            final String archiveFileName = "archive.tar";
            String destFilePath;
            try {
                sourceURI = new URI(dataStagingTaskModel.getSource());
                if (sourceURI.getPath().endsWith("/")) {
                    tarDirPath = sourceURI.getPath().substring(0, sourceURI.getPath().length() - 1);
                } else {
                    tarDirPath = sourceURI.getPath();
                }

                String inputPath = getTaskContext().getStorageFileSystemRootLocation();
                destFilePath = buildDestinationFilePath(inputPath, archiveFileName);

                tarCreationAbsPath = tarDirPath + File.separator + archiveFileName;
            } catch (URISyntaxException e) {
                throw new TaskOnFailException("Failed to obtain source URI for archival staging task " + getTaskId(), true, e);
            }

            // Fetch and validate storage adaptor
            StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());
            // Fetch and validate compute resource adaptor
            AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

            // Creating the tar file in the output path of the compute resource
            // Finds the list of files that do not include directories and symlinks
            String tarringCommand = "cd " + tarDirPath +
                    " && find ./ -not -type l -not -type d -print0 | tar --null --files-from - -cvf " + tarCreationAbsPath;

            logger.info("Running tar creation command " + tarringCommand);

            try {
                CommandOutput tarCommandOutput = adaptor.executeCommand(tarringCommand, null);
                if (tarCommandOutput.getExitCode() != 0) {
                    throw new TaskOnFailException("Failed while running the tar command " + tarringCommand + ". Sout : " +
                            tarCommandOutput.getStdOut() + ". Serr " + tarCommandOutput.getStdError(), false, null);
                }

            } catch (AgentException e) {
                throw new TaskOnFailException("Failed while running the tar command " + tarringCommand, true, null);
            }

            try {
                FileMetadata fileMetadata = adaptor.getFileMetadata(tarCreationAbsPath);
                long maxArchiveSize = Long.parseLong(ServerSettings.getSetting("max.archive.size", MAX_ARCHIVE_SIZE + ""));

                if (fileMetadata.getSize() < maxArchiveSize) {
                    boolean fileTransferred = transferFileToStorage(tarCreationAbsPath, destFilePath, archiveFileName, adaptor, storageResourceAdaptor);
                    if (!fileTransferred) {
                        logger.error("Failed to transfer created archive file " + tarCreationAbsPath);
                        throw new TaskOnFailException("Failed to transfer created archive file " + tarCreationAbsPath, false, null);
                    }

                    String destParent = destFilePath.substring(0, destFilePath.lastIndexOf("/"));
                    final String storageArchiveDir = "ARCHIVE";
                    String[] unarchiveCommands = {
                            "mkdir -p " + storageArchiveDir,
                            "tar -xvf " + archiveFileName + " -C " + storageArchiveDir,
                            "rm " + archiveFileName,
                            "chmod 755 -f -R " + storageArchiveDir + "/*"
                    };

                    try {
                        for (String command : unarchiveCommands) {
                            logger.info("Running command {} as a part of the un-archiving process", command);
                            CommandOutput unTarCommandOutput = storageResourceAdaptor.executeCommand(command, destParent);
                            if (unTarCommandOutput.getExitCode() != 0) {
                                throw new TaskOnFailException("Failed while running the un-archiving command " + command + ". Sout : " +
                                        unTarCommandOutput.getStdOut() + ". Serr : " + unTarCommandOutput.getStdError(), false, null);
                            }
                        }
                    } catch (AgentException e) {
                        throw new TaskOnFailException("Failed while running the untar command " + tarringCommand, false, null);
                    }

                    return onSuccess("Archival task successfully completed");
                } else {
                    logger.error("Archive size {} MB is larger than the maximum allowed size {} MB. So skipping the transfer.",
                            fileMetadata.getSize() / (1024L * 1024L), maxArchiveSize / (1024L * 1024L));
                    // This is not a recoverable issue. So mark it as critical
                    throw new TaskOnFailException("Archive task was skipped as size is " + fileMetadata.getSize() / (1024L * 1024L) + " MB", true, null);
                }

            } finally {
                String deleteTarCommand = "rm " + tarCreationAbsPath;
                logger.info("Running delete temporary tar command " + deleteTarCommand);
                try {
                    CommandOutput rmCommandOutput = adaptor.executeCommand(deleteTarCommand, null);
                    if (rmCommandOutput.getExitCode() != 0) {
                        logger.error("Failed while running the rm command " + deleteTarCommand + ". Sout : " +
                                rmCommandOutput.getStdOut() + ". Serr " + rmCommandOutput.getStdError());
                    }

                } catch (AgentException e) {
                    logger.error("Failed while running the rm command " + tarringCommand, e);
                }
            }

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            logger.error("Failed while un-archiving the data", e);
            return onFail(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing archiving staging task " + getTaskId(), e);
            return onFail("Unknown error while executing archiving staging task " + getTaskId(), false,  e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
