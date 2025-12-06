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
package org.apache.airavata.helix.impl.task.staging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.FileMetadata;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.agents.streaming.TransferResult;
import org.apache.airavata.agents.streaming.VirtualStreamProducer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.monitor.platform.CountMonitor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public abstract class DataStagingTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(DataStagingTask.class);
    private static final CountMonitor transferSizeTaskCounter = new CountMonitor("transfer_data_size_counter");

    private static final ExecutorService PASS_THROUGH_EXECUTOR =
            new ThreadPoolExecutor(10, 60, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @SuppressWarnings("WeakerAccess")
    protected DataStagingTaskModel getDataStagingTaskModel() throws TaskOnFailException {
        try {
            Object subTaskModel = getTaskContext().getSubTaskModel();
            if (subTaskModel != null) {
                return DataStagingTaskModel.class.cast(subTaskModel);
            } else {
                throw new TaskOnFailException(
                        "Data staging task model can not be null for task " + getTaskId(), false, null);
            }
        } catch (Exception e) {
            throw new TaskOnFailException(
                    "Failed while obtaining data staging task model for task " + getTaskId(), false, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected StorageResourceDescription getStorageResource() throws Exception {
        StorageResourceDescription storageResource = getTaskContext().getStorageResourceDescription();
        if (storageResource == null) {
            throw new TaskOnFailException("Storage resource can not be null for task " + getTaskId(), false, null);
        }
        return storageResource;
    }

    /**
     * Gets the default storage adaptor configured for the gateway.
     * This is the fallback storage used when input/output storage resources are not specifically configured.
     */
    protected StorageResourceAdaptor getStorageAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        String storageId = null;
        try {
            storageId = getTaskContext().getStorageResourceId();
            StoragePreference gatewayStoragePref = getTaskContext().getGatewayStorageResourcePreference();
            return createStorageAdaptorFromPreference(adaptorSupport, storageId, gatewayStoragePref, "Default");

        } catch (Exception e) {
            logger.error(
                    "Failed to obtain adaptor for default storage resource {} in task {}", storageId, getTaskId(), e);
            throw new TaskOnFailException(
                    "Failed to obtain adaptor for default storage resource " + storageId + " in task " + getTaskId(),
                    false,
                    e);
        }
    }

    /**
     * Gets the input storage adaptor.
     * Use input storage resource if configured. Otherwise, falls back to default gateway storage.
     */
    protected StorageResourceAdaptor getInputStorageAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        String storageId = null;
        try {
            storageId = getTaskContext().getInputStorageResourceId();
            logger.info("Fetching input storage adaptor for input storage resource {}", storageId);

            if (getTaskContext().getProcessModel().getInputStorageResourceId() != null
                    && !getTaskContext()
                            .getProcessModel()
                            .getInputStorageResourceId()
                            .trim()
                            .isEmpty()) {

                StoragePreference inputStoragePref = getTaskContext().getInputGatewayStorageResourcePreference();
                return createStorageAdaptorFromPreference(adaptorSupport, storageId, inputStoragePref, "Input");
            } else {
                // Fall back to default storage resource configured
                return getStorageAdaptor(adaptorSupport);
            }
        } catch (Exception e) {
            logger.error(
                    "Failed to obtain adaptor for input storage resource {} in task {}", storageId, getTaskId(), e);
            throw new TaskOnFailException(
                    "Failed to obtain adaptor for input storage resource " + storageId + " in task " + getTaskId(),
                    false,
                    e);
        }
    }

    /**
     * Gets the output storage adaptor.
     * Use output storage resource if configured. Otherwise, falls back to default gateway storage.
     */
    protected StorageResourceAdaptor getOutputStorageAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        String storageId = null;
        try {
            storageId = getTaskContext().getOutputStorageResourceId();
            logger.info("Fetching output storage adaptor for input storage resource {}", storageId);

            if (getTaskContext().getProcessModel().getOutputStorageResourceId() != null
                    && !getTaskContext()
                            .getProcessModel()
                            .getOutputStorageResourceId()
                            .trim()
                            .isEmpty()) {

                StoragePreference outputStoragePref = getTaskContext().getOutputGatewayStorageResourcePreference();
                return createStorageAdaptorFromPreference(adaptorSupport, storageId, outputStoragePref, "Output");
            } else {
                // Fall back to default storage resource configured
                return getStorageAdaptor(adaptorSupport);
            }
        } catch (Exception e) {
            logger.error(
                    "Failed to obtain adaptor for output storage resource {} in task {}", storageId, getTaskId(), e);
            throw new TaskOnFailException(
                    "Failed to obtain adaptor for output storage resource " + storageId + " in task " + getTaskId(),
                    false,
                    e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected AgentAdaptor getComputeResourceAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        String computeId = null;
        try {
            computeId = getTaskContext().getComputeResourceId();
            return adaptorSupport.fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    computeId,
                    getTaskContext().getJobSubmissionProtocol(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());
        } catch (Exception e) {
            throw new TaskOnFailException(
                    "Failed to obtain adaptor for compute resource " + computeId + " in task " + getTaskId(), false, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected String getLocalDataPath(String fileName) throws TaskOnFailException {
        String localDataPath = ServerSettings.getLocalDataLocation();
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator);
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator)
                + getProcessId() + File.separator + "temp_inputs" + File.separator;
        try {
            FileUtils.forceMkdir(new File(localDataPath));
        } catch (IOException e) {
            throw new TaskOnFailException("Failed build directories " + localDataPath, true, e);
        }
        localDataPath = localDataPath + fileName;
        return localDataPath;
    }

    /**
     * Builds the destination file path for data staging tasks.
     * The method constructs the path as: targetStorageRoot + experimentDataDir + fileName
     * If experimentDataDir is given as an absolute path (starts with '/'), the leading
     * separator is stripped to make it relative to targetStorageRoot.
     * If experimentDataDir is null or empty falls back to processId-based path.
     *
     * @param targetStorageRoot The file system root location of the target storage resource
     * @param fileName          The name of the file to be staged
     * @return The complete destination file path
     */
    protected String buildDestinationFilePath(String targetStorageRoot, String fileName) {
        String targetRoot = targetStorageRoot.trim();
        if (!targetRoot.endsWith(File.separator)) {
            targetRoot += File.separator;
        }

        String experimentDataDir = getProcessModel().getExperimentDataDir();

        if (experimentDataDir == null || experimentDataDir.trim().isEmpty()) {
            return targetRoot + getProcessId() + File.separator + fileName;
        }

        String normalizedDir = experimentDataDir.trim();
        if (normalizedDir.startsWith(File.separator)) {
            normalizedDir = normalizedDir.substring(1);
            logger.debug(
                    "Stripped the leading separator from experimentDataDir to make it relative: {}", normalizedDir);
        }

        if (!normalizedDir.endsWith(File.separator)) {
            normalizedDir += File.separator;
        }

        return targetRoot + normalizedDir + fileName;
    }

    protected String escapeSpecialCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", "?", "&", "%"};

        for (String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }

    public void naiveTransfer(
            AgentAdaptor srcAdaptor, String sourceFile, AgentAdaptor destAdaptor, String destFile, String tempFile)
            throws TaskOnFailException {

        sourceFile = escapeSpecialCharacters(sourceFile);
        destFile = escapeSpecialCharacters(destFile);

        logger.info("Using naive transfer to transfer " + sourceFile + " to " + destFile);
        try {
            try {
                logger.info("Downloading file " + sourceFile + " to local temp file " + tempFile);
                srcAdaptor.downloadFile(sourceFile, tempFile);
            } catch (AgentException e) {
                throw new TaskOnFailException(
                        "Failed downloading file " + sourceFile + " to the local path " + tempFile, false, e);
            }

            File localFile = new File(tempFile);
            if (!localFile.exists()) {
                throw new TaskOnFailException("Local file does not exist at " + tempFile, false, null);
            }

            transferSizeTaskCounter.inc(localFile.length());

            try {
                logger.info("Uploading file form local temp file " + tempFile + " to " + destFile);
                destAdaptor.uploadFile(tempFile, destFile);
            } catch (AgentException e) {
                throw new TaskOnFailException(
                        "Failed uploading file to " + destFile + " from local path " + tempFile, false, e);
            }
        } finally {
            logger.info("Deleting temporary file " + tempFile);
            deleteTempFile(tempFile);
        }
    }

    public static void passThroughTransfer(
            AgentAdaptor srcAdaptor, String sourceFile, AgentAdaptor destAdaptor, String destFile)
            throws TaskOnFailException {
        logger.info("Using pass through transfer to transfer " + sourceFile + " to " + destFile);

        FileMetadata tempMetadata;
        try {
            tempMetadata = srcAdaptor.getFileMetadata(sourceFile);
        } catch (AgentException e) {
            throw new TaskOnFailException("Failed to obtain metadata for file " + sourceFile, false, e);
        }

        final FileMetadata fileMetadata = tempMetadata;

        VirtualStreamProducer streamProducer = new VirtualStreamProducer(1024, fileMetadata.getSize());

        OutputStream os = streamProducer.getOutputStream();
        InputStream is = streamProducer.getInputStream();

        Callable<TransferResult> inCallable = () -> {
            TransferResult result = new TransferResult();
            result.setTransferId("In");

            try {
                logger.info("Executing in-bound transfer for file " + sourceFile);
                srcAdaptor.downloadFile(sourceFile, os, fileMetadata);
                logger.info("Completed in-bound transfer for file " + sourceFile);
                result.setTransferStatus(TransferResult.TransferStatus.COMPLETED);
                result.setMessage("Successfully completed the transfer");

            } catch (Exception e) {
                result.setMessage("In-bound transfer failed for file " + sourceFile + ". Reason : " + e.getMessage());
                result.setTransferStatus(TransferResult.TransferStatus.FAILED);
                result.setError(e);
            }
            return result;
        };

        Callable<TransferResult> outCallable = () -> {
            TransferResult result = new TransferResult();
            result.setTransferId("Out");

            try {
                logger.info("Executing out-bound transfer for file " + destFile);
                destAdaptor.uploadFile(is, fileMetadata, destFile);
                logger.info("Completed out-bound transfer for file " + destFile);
                result.setTransferStatus(TransferResult.TransferStatus.COMPLETED);
                result.setMessage("Successfully completed the transfer");

            } catch (Exception e) {
                result.setMessage("Out-bound transfer failed for file " + destFile + ". Reason : " + e.getMessage());
                result.setTransferStatus(TransferResult.TransferStatus.FAILED);
                result.setError(e);
            }

            return result;
        };

        CompletionService<TransferResult> completionService =
                new ExecutorCompletionService<TransferResult>(PASS_THROUGH_EXECUTOR);

        Map<String, Future<TransferResult>> unResolvedFutures = new HashMap<>();

        unResolvedFutures.put("In", completionService.submit(inCallable));
        unResolvedFutures.put("Out", completionService.submit(outCallable));

        int completed = 0;
        int failed = 0;
        TransferResult failedResult = null;

        try {
            while (completed < 2 && failed == 0) {
                try {
                    Future<TransferResult> res = completionService.take();
                    if (res.get().getTransferStatus() == TransferResult.TransferStatus.COMPLETED) {
                        completed++;
                        logger.debug("Transfer " + res.get().getTransferId() + " completed");
                    } else {
                        failed++;
                        failedResult = res.get();
                        logger.warn("Transfer " + res.get().getTransferId() + " failed", failedResult.getError());
                    }
                    unResolvedFutures.remove(res.get().getTransferId());

                } catch (Exception e) {
                    logger.error("Error occurred while monitoring transfers", e);
                    throw new TaskOnFailException("Error occurred while monitoring transfers", false, e);
                }
            }

            if (failed > 0) {
                logger.error(
                        "Transfer from " + sourceFile + " to " + destFile + " failed. " + failedResult.getMessage(),
                        failedResult.getError());
                throw new TaskOnFailException(
                        "Pass through file transfer failed from " + sourceFile + " to " + destFile,
                        false,
                        failedResult.getError());
            } else {
                logger.info("Transfer from " + sourceFile + " to " + destFile + " completed");
            }

        } finally {
            // Cleaning up unresolved transfers
            if (unResolvedFutures.size() > 0) {
                unResolvedFutures.forEach((id, future) -> {
                    try {
                        logger.warn("Cancelling transfer " + id);
                        future.cancel(true);
                    } catch (Exception e) {
                        // Ignore
                        logger.warn(e.getMessage());
                    }
                });
            }
        }
    }

    protected void transferFileToComputeResource(
            String sourcePath, String destPath, AgentAdaptor computeAdaptor, StorageResourceAdaptor storageAdaptor)
            throws TaskOnFailException {

        try {
            FileMetadata fileMetadata = storageAdaptor.getFileMetadata(sourcePath);
            if (fileMetadata.getSize() == 0) {
                logger.error("File " + sourcePath + " size is 0 so ignoring the upload");
                throw new TaskOnFailException(
                        "Input staging has failed as file " + sourcePath + " size is 0", false, null);
            }
        } catch (AgentException e) {
            logger.error("Failed to fetch metadata for file " + sourcePath, e);
            throw new TaskOnFailException("Failed to fetch metadata for file " + sourcePath, false, e);
        }

        if (ServerSettings.isSteamingEnabled()) {
            passThroughTransfer(storageAdaptor, sourcePath, computeAdaptor, destPath);
        } else {
            String sourceFileName =
                    sourcePath.substring(sourcePath.lastIndexOf(File.separator) + 1, sourcePath.length());
            String tempPath = getLocalDataPath(sourceFileName);
            naiveTransfer(storageAdaptor, sourcePath, computeAdaptor, destPath, tempPath);
        }
    }

    protected boolean transferFileToStorage(
            String sourcePath,
            String destPath,
            String fileName,
            AgentAdaptor adaptor,
            StorageResourceAdaptor storageResourceAdaptor)
            throws TaskOnFailException {

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
                logger.warn("Ignoring the file {} transfer as it is not available", sourcePath);
                return false;
            }
        } catch (AgentException e) {
            logger.error("Error while checking the file {} existence", sourcePath, e);
            throw new TaskOnFailException("Error while checking the file " + sourcePath + " existence", false, e);
        }

        String parentDir = destPath.substring(0, destPath.lastIndexOf(File.separator));
        try {
            logger.info("Checking whether the parent directory {} in storage exists", parentDir);
            Boolean exists = storageResourceAdaptor.doesFileExist(parentDir);
            if (!exists) {
                logger.info("Parent directory {} on storage does not exist. So creating it recursively", parentDir);
                storageResourceAdaptor.createDirectory(parentDir, true);
            }
        } catch (AgentException e) {
            logger.error("Failed in validating the parent directory {} in storage side", parentDir, e);
            throw new TaskOnFailException(
                    "Failed in validating the parent directory " + parentDir + " in storage side", false, e);
        }

        if (ServerSettings.isSteamingEnabled()) {
            passThroughTransfer(adaptor, sourcePath, storageResourceAdaptor, destPath);
        } else {
            String tempPath = getLocalDataPath(fileName);
            naiveTransfer(adaptor, sourcePath, storageResourceAdaptor, destPath, tempPath);
        }
        return true;
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

    /**
     * Common method to create StorageResourceAdaptor from a StoragePreference.
     */
    private StorageResourceAdaptor createStorageAdaptorFromPreference(
            AdaptorSupport adaptorSupport, String storageId, StoragePreference storagePreference, String adaptorType)
            throws TaskOnFailException {
        try {
            String credentialToken = storagePreference.getResourceSpecificCredentialStoreToken() != null
                    ? storagePreference.getResourceSpecificCredentialStoreToken()
                    : getTaskContext().getGatewayResourceProfile().getCredentialStoreToken();

            StorageResourceAdaptor storageResourceAdaptor = adaptorSupport.fetchStorageAdaptor(
                    getGatewayId(),
                    storageId,
                    getTaskContext().getDataMovementProtocol(),
                    credentialToken,
                    storagePreference.getLoginUserName());

            if (storageResourceAdaptor == null) {
                throw new TaskOnFailException(
                        adaptorType + " storage resource adaptor for " + storageId + " can not be null", true, null);
            }
            return storageResourceAdaptor;

        } catch (Exception e) {
            throw new TaskOnFailException(
                    "Failed to obtain adaptor for " + adaptorType.toLowerCase() + " storage resource " + storageId
                            + " in task " + getTaskId(),
                    false,
                    e);
        }
    }
}
