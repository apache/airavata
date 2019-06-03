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
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

@TaskDef(name = "Input Data Staging Task")
public class InputDataStagingTask extends DataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(InputDataStagingTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        logger.info("Starting Input Data Staging Task " + getTaskId());

        saveAndPublishProcessStatus(ProcessState.INPUT_DATA_STAGING);

        try {
            // Get and validate data staging task model
            DataStagingTaskModel dataStagingTaskModel = getDataStagingTaskModel();

            // Fetch and validate input data type from data staging task model
            InputDataObjectType processInput = dataStagingTaskModel.getProcessInput();
            if (processInput != null && processInput.getValue() == null) {
                String message = "expId: " + getExperimentId() + ", processId: " + getProcessId() + ", taskId: " + getTaskId() +
                        ":- Couldn't stage file " + processInput.getName() + " , file name shouldn't be null. ";
                logger.error(message);
                if (processInput.isIsRequired()) {
                    message += "File name is null, but this input's isRequired bit is not set";
                } else {
                    message += "File name is null";
                }
                logger.error(message);
                throw new TaskOnFailException(message, true, null);
            }

            try {

                String sourceUrls[];

                if (dataStagingTaskModel.getProcessInput().getType() == DataType.URI_COLLECTION) {
                    logger.info("Found a URI collection so splitting by comma for path " + dataStagingTaskModel.getSource());
                    sourceUrls = dataStagingTaskModel.getSource().split(",");
                } else {
                    sourceUrls = new String[]{dataStagingTaskModel.getSource()};
                }

                for (String url : sourceUrls) {
                    URI sourceURI = new URI(url);
                    URI destinationURI = new URI(dataStagingTaskModel.getDestination());

                    logger.info("Source file " + sourceURI.getPath() + ", destination uri " + destinationURI.getPath() + " for task " + getTaskId());
                    copySingleFile(sourceURI, destinationURI, taskHelper);
                }

            } catch (URISyntaxException e) {
                throw new TaskOnFailException("Failed to obtain source URI for input data staging task " + getTaskId(), true, e);
            }

            return onSuccess("Input data staging task " + getTaskId() + " successfully completed");

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return onFail(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing input data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing input data staging task " + getTaskId(), false,  e);
        }
    }

    private void copySingleFile(URI sourceURI, URI destinationURI, TaskHelper taskHelper) throws TaskOnFailException {

        String sourceFileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                    sourceURI.getPath().length());

        // Fetch and validate storage adaptor
        StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());

        // Fetch and validate compute resource adaptor
        AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

        String localSourceFilePath = getLocalDataPath(sourceFileName);
        // Downloading input file from the storage resource

        try {
            try {
                logger.info("Downloading input file " + sourceURI.getPath() + " to the local path " + localSourceFilePath);
                storageResourceAdaptor.downloadFile(sourceURI.getPath(), localSourceFilePath);
                logger.info("Input file downloaded to " + localSourceFilePath);
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed downloading input file " + sourceFileName + " to the local path " + localSourceFilePath, false, e);
            }

            File localFile = new File(localSourceFilePath);
            if (localFile.exists()) {
                if (localFile.length() == 0) {
                    logger.error("Local file " + localSourceFilePath +" size is 0 so ignoring the upload");
                    throw new TaskOnFailException("Input staging has failed as file " + localSourceFilePath + " size is 0", true, null);
                }
            } else {
                throw new TaskOnFailException("Local file does not exist at " + localSourceFilePath, false, null);
            }

            // Uploading input file to the compute resource
            try {
                logger.info("Uploading the input file to " + destinationURI.getPath() + " from local path " + localSourceFilePath);
                adaptor.copyFileTo(localSourceFilePath, destinationURI.getPath());
                logger.info("Input file uploaded to " + destinationURI.getPath());
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed uploading the input file to " + destinationURI.getPath() + " from local path " + localSourceFilePath, false, e);
            }

        } finally {
            logger.info("Deleting temporary file " + localSourceFilePath);
            deleteTempFile(localSourceFilePath);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
