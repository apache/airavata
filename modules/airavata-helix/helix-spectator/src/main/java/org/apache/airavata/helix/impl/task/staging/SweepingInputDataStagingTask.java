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
import org.apache.airavata.helix.impl.SpecUtils;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TaskDef(name = "Sweeping Input Data Staging Task")
public class SweepingInputDataStagingTask extends DataStagingTask {
    private final static Logger logger = LoggerFactory.getLogger(SweepingInputDataStagingTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        try {
            saveAndPublishProcessStatus(ProcessState.INPUT_DATA_STAGING);

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

                String workingDir = taskContext.getWorkingDir();
                for (String url : sourceUrls) {
                    String sourcePath = new URI(url).getPath();
                    String sourceFileName = new File(sourcePath).getName();

                    // TODO: Put a flag in input model to detect sweeping zip files instead of looking at the extensions
                    /*
                    *  Zip file should contain sub directories which are named in sequential order upto sweep count - 1
                    *   Example: data.zip
                    *   Unzipped Content: data/
                    *                         /0/input.txt
                    *                         /1/input.txt
                    *                         .
                    *                         .
                    *                         /<sweepCount -1>/input.txt
                     */

                    List<Integer> rangeInts = taskContext.getSweepRange();

                    if (sourceFileName.endsWith(".zip")) {
                        String tempZipDir = Paths.get(workingDir, UUID.randomUUID().toString()).toString();
                        logger.info("Copying sweep input zip {} to temp directory {}", sourceFileName, tempZipDir);
                        adaptor.createDirectory(tempZipDir, true);

                        transferFileToComputeResource(sourcePath, Paths.get(tempZipDir, sourceFileName).toString(), adaptor, storageResourceAdaptor);
                        logger.info("Unzipping sweep input zip {} inside temp directory {}", sourceFileName, tempZipDir);
                        adaptor.executeCommand("unzip " + sourceFileName, tempZipDir);
                        String tempDataPath = Paths.get(tempZipDir, sourceFileName.substring(0, sourceFileName.length() - ".zip".length())).toString();

                        List<String> cpCmds = new ArrayList<>();

                        for (int i : rangeInts) {
                            String sweepSourceDir = Paths.get(tempDataPath, i +"").toString();
                            List<String> sweepFiles = adaptor.listDirectory(sweepSourceDir);
                            for (String sweepFile: sweepFiles) {
                                String localSourceFile = Paths.get(sweepSourceDir, sweepFile).toString();

                                String overrideFileName = dataStagingTaskModel.getProcessInput().getOverrideFilename();
                                String destFileName = (overrideFileName != null && !"".equals(overrideFileName)) ? overrideFileName : sweepFile;
                                String destPath = Paths.get(workingDir, i + "", destFileName).toString();

                                logger.info("Transferring zipped sweeping input file {} to destination path {} locally", localSourceFile, destPath);
                                cpCmds.add("cd " + sweepSourceDir + "; cp " + localSourceFile + " " + destPath);
                            }
                        }

                        String copyCommands = String.join("; ", cpCmds);
                        logger.info("Running input placement commands : {}", copyCommands);
                        adaptor.executeCommand(copyCommands, null);

                    } else {
                        // TODO: Optimize here to copy locally
                        for (int i : rangeInts) {
                            String overrideFileName = dataStagingTaskModel.getProcessInput().getOverrideFilename();
                            String destFileName = (overrideFileName != null && !"".equals(overrideFileName)) ? overrideFileName : sourceFileName;
                            String destPath = Paths.get(workingDir, i + "", destFileName).toString();
                            logger.info("Transferring sweeping input file {} to destination path {}", sourcePath, destPath);
                            transferFileToComputeResource(sourcePath, destPath, adaptor, storageResourceAdaptor);
                        }
                    }
                }

            } catch (URISyntaxException e) {
                throw new TaskOnFailException("Failed to obtain source URI for input data staging task " + getTaskId(), true, e);
            }

            return onSuccess("Input data staging task " + getTaskId() + " successfully completed");


        } catch (Exception e) {
            logger.error("Unknown error while executing sweeping input data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing sweeping input data staging task " + getTaskId(), false,  e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }


}
