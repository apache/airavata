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
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@TaskDef(name = "Input Data Staging Task")
public class InputDataStagingTask extends DataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(InputDataStagingTask.class);
    private final static CountMonitor inputDSTaskCounter = new CountMonitor("input_ds_task_counter");

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        logger.info("Starting Input Data Staging Task " + getTaskId());

        inputDSTaskCounter.inc();

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

                String[] sourceUrls;

                if (dataStagingTaskModel.getProcessInput().getType() == DataType.URI_COLLECTION) {
                    logger.info("Found a URI collection so splitting by comma for path " + dataStagingTaskModel.getSource());
                    sourceUrls = dataStagingTaskModel.getSource().split(",");
                } else {
                    sourceUrls = new String[]{dataStagingTaskModel.getSource()};
                }

                // Fetch and validate storage adaptor
                StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());
                // Fetch and validate compute resource adaptor
                AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

                for (String url : sourceUrls) {
                    URI sourceURI = new URI(url);
                    URI destinationURI = new URI(dataStagingTaskModel.getDestination());

                    logger.info("Source file " + sourceURI.getPath() + ", destination uri " + destinationURI.getPath() + " for task " + getTaskId());
                    transferFileToComputeResource(sourceURI.getPath(), destinationURI.getPath(), adaptor, storageResourceAdaptor);
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

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
