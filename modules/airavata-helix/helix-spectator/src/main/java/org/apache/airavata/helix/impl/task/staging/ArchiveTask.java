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

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

@TaskDef(name = "Archival Task")
public class ArchiveTask extends DataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(ArchiveTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        logger.info("Starting archival task " + getTaskId() + " in experiment " + getExperimentId());
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
            String tarringCommand = "cd " + tarDirPath + " && tar -cvf " + tarCreationAbsPath + " ./* ";
            logger.info("Running tar creation command " + tarringCommand);

            try {
                CommandOutput tarCommandOutput = adaptor.executeCommand(tarringCommand, null);
                if (tarCommandOutput.getExitCode() != 0) {
                    throw new TaskOnFailException("Failed while running the tar command " + tarringCommand + ". Sout : " +
                            tarCommandOutput.getStdOut() + ". Serr " + tarCommandOutput.getStdError(), true, null);
                }

            } catch (AgentException e) {
                throw new TaskOnFailException("Failed while running the tar command " + tarringCommand, true, null);
            }

            boolean fileTransferred = transferFileToStorage(tarCreationAbsPath, destFilePath, archiveFileName, adaptor, storageResourceAdaptor);

            if (!fileTransferred) {
                logger.error("Failed to transfer created archive file " + tarCreationAbsPath);
                throw new TaskOnFailException("Failed to transfer created archive file " + tarCreationAbsPath, true, null);
            }

            String deleteTarCommand = "rm " + tarCreationAbsPath;
            logger.info("Running delete temporary tar command " + deleteTarCommand);

            try {
                CommandOutput rmCommandOutput = adaptor.executeCommand(deleteTarCommand, null);
                if (rmCommandOutput.getExitCode() != 0) {
                    throw new TaskOnFailException("Failed while running the rm command " + deleteTarCommand + ". Sout : " +
                            rmCommandOutput.getStdOut() + ". Serr " + rmCommandOutput.getStdError(), true, null);
                }

            } catch (AgentException e) {
                throw new TaskOnFailException("Failed while running the rm command " + tarringCommand, true, null);
            }

            String destParent = destFilePath.substring(0, destFilePath.lastIndexOf("/"));
            final String storageArchiveDir = "ARCHIVE";
            String unArchiveTarCommand = "mkdir " + storageArchiveDir + " && tar -xvf " + archiveFileName + " -C "
                    + storageArchiveDir + " && rm " + archiveFileName + " && chmod 755 -R " + storageArchiveDir + "/*";
            logger.info("Running Un archiving command on storage resource " + unArchiveTarCommand);

            try {
                CommandOutput unTarCommandOutput = storageResourceAdaptor.executeCommand(unArchiveTarCommand, destParent);
                if (unTarCommandOutput.getExitCode() != 0) {
                    throw new TaskOnFailException("Failed while running the untar command " + deleteTarCommand + ". Sout : " +
                            unTarCommandOutput.getStdOut() + ". Serr " + unTarCommandOutput.getStdError(), true, null);
                }
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed while running the untar command " + tarringCommand, true, null);
            }

            return onSuccess("Archival task successfully completed");

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return onFail(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing output data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing output data staging task " + getTaskId(), false,  e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
