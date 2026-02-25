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
package org.apache.airavata.storage.client.sftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.core.exception.TaskFailureException;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.protocol.FileMetadata;
import org.apache.airavata.protocol.StorageResourceAdapter;
import org.apache.airavata.protocol.streaming.TransferResult;
import org.apache.airavata.protocol.streaming.VirtualStreamProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnParticipant
public class DataStagingSupport {

    private static final Logger logger = LoggerFactory.getLogger(DataStagingSupport.class);
    private static final CounterMetric transferSizeTaskCounter = new CounterMetric("transfer_data_size_counter");

    private static final ExecutorService PASS_THROUGH_EXECUTOR =
            new ThreadPoolExecutor(10, 60, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    private final ServerProperties serverProperties;

    public DataStagingSupport(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public String getLocalDataPath(String fileName, String processId) throws TaskFailureException {
        String localDataPath = serverProperties.localDataLocation();
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator)
                + processId + File.separator + "temp_inputs" + File.separator;
        try {
            Files.createDirectories(new File(localDataPath).toPath());
        } catch (IOException e) {
            throw new TaskFailureException("Failed build directories " + localDataPath, true, e);
        }
        localDataPath = localDataPath + fileName;
        return localDataPath;
    }

    public String buildDestinationFilePath(String targetStorageRoot, String fileName, TaskContext taskContext) {
        String targetRoot = targetStorageRoot.trim();
        if (!targetRoot.endsWith(File.separator)) {
            targetRoot += File.separator;
        }

        String experimentDataDir = taskContext.getProcessModel().getExperimentDataDir();

        if (experimentDataDir == null || experimentDataDir.trim().isEmpty()) {
            return targetRoot + taskContext.getProcessId() + File.separator + fileName;
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

    public String escapeSpecialCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", "?", "&", "%"};

        for (String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }

    public void naiveTransfer(
            AgentAdapter srcAdapter, String sourceFile, AgentAdapter destAdapter, String destFile, String tempFile)
            throws TaskFailureException {

        sourceFile = escapeSpecialCharacters(sourceFile);
        destFile = escapeSpecialCharacters(destFile);

        logger.info("Using naive transfer to transfer " + sourceFile + " to " + destFile);
        try {
            try {
                logger.info("Downloading file " + sourceFile + " to local temp file " + tempFile);
                srcAdapter.downloadFile(sourceFile, tempFile);
            } catch (AgentException e) {
                throw new TaskFailureException(
                        "Failed downloading file " + sourceFile + " to the local path " + tempFile, false, e);
            }

            File localFile = new File(tempFile);
            if (!localFile.exists()) {
                throw new TaskFailureException("Local file does not exist at " + tempFile, false, null);
            }

            transferSizeTaskCounter.inc(localFile.length());

            try {
                logger.info("Uploading file form local temp file " + tempFile + " to " + destFile);
                destAdapter.uploadFile(tempFile, destFile);
            } catch (AgentException e) {
                throw new TaskFailureException(
                        "Failed uploading file to " + destFile + " from local path " + tempFile, false, e);
            }
        } finally {
            logger.info("Deleting temporary file " + tempFile);
            deleteTempFile(tempFile);
        }
    }

    public static void passThroughTransfer(
            AgentAdapter srcAdapter, String sourceFile, AgentAdapter destAdapter, String destFile)
            throws TaskFailureException {
        logger.info("Using pass through transfer to transfer " + sourceFile + " to " + destFile);

        FileMetadata tempMetadata;
        try {
            tempMetadata = srcAdapter.getFileMetadata(sourceFile);
        } catch (AgentException e) {
            throw new TaskFailureException("Failed to obtain metadata for file " + sourceFile, false, e);
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
                srcAdapter.downloadFile(sourceFile, os, fileMetadata);
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
                destAdapter.uploadFile(is, fileMetadata, destFile);
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
                    throw new TaskFailureException("Error occurred while monitoring transfers", false, e);
                }
            }

            if (failed > 0 && failedResult != null) {
                logger.error(
                        "Transfer from " + sourceFile + " to " + destFile + " failed. " + failedResult.getMessage(),
                        failedResult.getError());
                throw new TaskFailureException(
                        "Pass through file transfer failed from " + sourceFile + " to " + destFile,
                        false,
                        failedResult.getError());
            } else {
                logger.info("Transfer from " + sourceFile + " to " + destFile + " completed");
            }

        } finally {
            if (unResolvedFutures.size() > 0) {
                unResolvedFutures.forEach((id, future) -> {
                    try {
                        logger.warn("Cancelling transfer " + id);
                        future.cancel(true);
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                    }
                });
            }
        }
    }

    public void transferFileToComputeResource(
            String sourcePath, String destPath, AgentAdapter computeAdapter,
            StorageResourceAdapter storageAdapter, String processId)
            throws TaskFailureException {

        try {
            FileMetadata fileMetadata = storageAdapter.getFileMetadata(sourcePath);
            if (fileMetadata.getSize() == 0) {
                logger.error("File " + sourcePath + " size is 0 so ignoring the upload");
                throw new TaskFailureException(
                        "Input staging has failed as file " + sourcePath + " size is 0", false, null);
            }
        } catch (AgentException e) {
            logger.error("Failed to fetch metadata for file " + sourcePath, e);
            throw new TaskFailureException("Failed to fetch metadata for file " + sourcePath, false, e);
        }

        boolean streamingEnabled = serverProperties.streamingTransfer().enabled();
        if (streamingEnabled) {
            passThroughTransfer(storageAdapter, sourcePath, computeAdapter, destPath);
        } else {
            String sourceFileName =
                    sourcePath.substring(sourcePath.lastIndexOf(File.separator) + 1, sourcePath.length());
            String tempPath = getLocalDataPath(sourceFileName, processId);
            naiveTransfer(storageAdapter, sourcePath, computeAdapter, destPath, tempPath);
        }
    }

    public boolean transferFileToStorage(
            String sourcePath,
            String destPath,
            String fileName,
            AgentAdapter adapter,
            StorageResourceAdapter storageResourceAdapter,
            String processId)
            throws TaskFailureException {

        try {
            boolean fileExists = adapter.doesFileExist(sourcePath);

            if (!fileExists) {
                for (int i = 1; i <= 3; i++) {
                    logger.warn("File " + sourcePath + " was not found in path. Retrying in 10 seconds. Try " + i);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        logger.error("Unexpected error in waiting", e);
                    }
                    fileExists = adapter.doesFileExist(sourcePath);
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
            throw new TaskFailureException("Error while checking the file " + sourcePath + " existence", false, e);
        }

        String parentDir = destPath.substring(0, destPath.lastIndexOf(File.separator));
        try {
            logger.info("Checking whether the parent directory {} in storage exists", parentDir);
            Boolean exists = storageResourceAdapter.doesFileExist(parentDir);
            if (!exists) {
                logger.info("Parent directory {} on storage does not exist. So creating it recursively", parentDir);
                storageResourceAdapter.createDirectory(parentDir, true);
            }
        } catch (AgentException e) {
            logger.error("Failed in validating the parent directory {} in storage side", parentDir, e);
            throw new TaskFailureException(
                    "Failed in validating the parent directory " + parentDir + " in storage side", false, e);
        }

        boolean streamingEnabled = serverProperties.streamingTransfer().enabled();
        if (streamingEnabled) {
            passThroughTransfer(adapter, sourcePath, storageResourceAdapter, destPath);
        } else {
            String tempPath = getLocalDataPath(fileName, processId);
            naiveTransfer(adapter, sourcePath, storageResourceAdapter, destPath, tempPath);
        }
        return true;
    }

    public void deleteTempFile(String filePath) {
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
