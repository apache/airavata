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
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public abstract class DataStagingTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(DataStagingTask.class);

    @SuppressWarnings("WeakerAccess")
    protected DataStagingTaskModel getDataStagingTaskModel() throws TaskOnFailException {
        try {
            Object subTaskModel = getTaskContext().getSubTaskModel();
            if (subTaskModel != null) {
                return DataStagingTaskModel.class.cast(subTaskModel);
            } else {
                throw new TaskOnFailException("Data staging task model can not be null for task " + getTaskId(), true, null);
            }
        } catch (Exception e) {
            throw new TaskOnFailException("Failed while obtaining data staging task model for task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected StorageResourceDescription getStorageResource() throws TaskOnFailException {
        StorageResourceDescription storageResource = getTaskContext().getStorageResourceDescription();
        if (storageResource == null) {
            throw new TaskOnFailException("Storage resource can not be null for task " + getTaskId(), true, null);
        }
        return storageResource;
    }

    @SuppressWarnings("WeakerAccess")
    protected StorageResourceAdaptor getStorageAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        try {
            StorageResourceAdaptor storageResourceAdaptor = adaptorSupport.fetchStorageAdaptor(
                    getGatewayId(),
                    getTaskContext().getStorageResourceId(),
                    getTaskContext().getDataMovementProtocol(),
                    getTaskContext().getStorageResourceCredentialToken(),
                    getTaskContext().getStorageResourceLoginUserName());

            if (storageResourceAdaptor == null) {
                throw new TaskOnFailException("Storage resource adaptor for " + getTaskContext().getStorageResourceId() + " can not be null", true, null);
            }
            return storageResourceAdaptor;
        } catch (AgentException e) {
            throw new TaskOnFailException("Failed to obtain adaptor for storage resource " + getTaskContext().getStorageResourceId() +
                    " in task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected AgentAdaptor getComputeResourceAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        try {
            return adaptorSupport.fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    getTaskContext().getComputeResourceId(),
                    getTaskContext().getJobSubmissionProtocol(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());
        } catch (Exception e) {
            throw new TaskOnFailException("Failed to obtain adaptor for compute resource " + getTaskContext().getComputeResourceId() +
                    " in task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected String getLocalDataPath(String fileName) throws TaskOnFailException {
        String localDataPath = ServerSettings.getLocalDataLocation();
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator);
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator) +
                getProcessId() + File.separator + "temp_inputs" + File.separator;
        try {
            FileUtils.forceMkdir(new File(localDataPath));
        } catch (IOException e) {
            throw new TaskOnFailException("Failed build directories " + localDataPath, true, e);
        }
        localDataPath = localDataPath + fileName;
        return localDataPath;
    }

    protected String buildDestinationFilePath(String inputPath, String fileName) {

        inputPath = (inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator);
        String experimentDataDir = getProcessModel().getExperimentDataDir();
        String filePath;
        if(experimentDataDir != null && !experimentDataDir.isEmpty()) {
            if(!experimentDataDir.endsWith(File.separator)){
                experimentDataDir += File.separator;
            }
            if (experimentDataDir.startsWith(File.separator)) {
                filePath = experimentDataDir + fileName;
            } else {
                filePath = inputPath + experimentDataDir + fileName;
            }
        } else {
            filePath = inputPath + getProcessId() + File.separator + fileName;
        }
        return filePath;
    }

    protected boolean transferFileToStorage(String sourcePath, String destPath, String fileName, AgentAdaptor adaptor,
                              StorageResourceAdaptor storageResourceAdaptor) throws TaskOnFailException {

        try {
            boolean fileExists = adaptor.doesFileExist(sourcePath);

            if (!fileExists) {
                for (int i = 1; i <= 3; i++) {
                    logger.warn("File " + sourcePath + " was not found in path. Retrying in 10 seconds. Try " + i);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        logger.error("Unexpected error in waiting", e);
                    }
                    fileExists = adaptor.doesFileExist(sourcePath);
                    if (fileExists) {
                        break;
                    }
                }
            }

            if (!fileExists) {
                logger.warn("Ignoring the file " + sourcePath + " transfer as it is not available");
                return false;
            }
        } catch (AgentException e) {
            logger.error("Error while checking the file " + sourcePath + " existence");
            throw new TaskOnFailException("Error while checking the file " + sourcePath + " existence", false, e);
        }

        String localSourceFilePath = getLocalDataPath(fileName);

        try {
            try {
                logger.info("Downloading output file " + sourcePath + " to the local path " + localSourceFilePath);
                adaptor.copyFileFrom(sourcePath, localSourceFilePath);
                logger.info("Output file downloaded to " + localSourceFilePath);
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed downloading output file " + sourcePath + " to the local path " +
                        localSourceFilePath, false, e);
            }

            File localFile = new File(localSourceFilePath);
            if (localFile.exists()) {
                /*if (localFile.length() == 0) {
                    logger.warn("Local file " + localSourceFilePath +" size is 0 so ignoring the upload");
                    return false;
                }*/
            } else {
                throw new TaskOnFailException("Local file does not exist at " + localSourceFilePath, false, null);
            }
            // Uploading output file to the storage resource
            try {
                logger.info("Uploading the output file to " + destPath + " from local path " + localSourceFilePath);
                storageResourceAdaptor.uploadFile(localSourceFilePath, destPath);
                logger.info("Output file uploaded to " + destPath);
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed uploading the output file to " + destPath + " from local path " +
                        localSourceFilePath, false, e);
            }

            return true;
        } finally {
            logger.info("Deleting temporary file " + localSourceFilePath);
            deleteTempFile(localSourceFilePath);
        }
    }

    protected void deleteTempFile(String filePath) {
        try {
            File tobeDeleted = new File(filePath);
            if (tobeDeleted.exists()) {
                tobeDeleted.delete();
            }
        } catch (Exception e) {
            logger.warn("Failed to delete temporary file " + filePath);
        }
    }
}
