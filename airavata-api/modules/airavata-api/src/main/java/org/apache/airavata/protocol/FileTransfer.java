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
package org.apache.airavata.protocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.exception.TaskFailureException;
import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.protocol.AgentAdapter.AgentException;
import org.apache.airavata.protocol.AgentAdapter.FileMetadata;
import org.apache.airavata.protocol.ssh.SSHUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Protocol-agnostic file transfer between two {@link AgentAdapter} instances.
 *
 * <p>Supports two transfer strategies: naive (download-to-local-then-upload)
 * and pass-through (streaming directly between adapters). The strategy is
 * selected based on {@link ServerProperties#streamingTransfer()}.
 */
@Component
@ConditionalOnParticipant
public class FileTransfer {

    private static final Logger logger = LoggerFactory.getLogger(FileTransfer.class);
    private static final CounterMetric transferSizeTaskCounter = new CounterMetric("transfer_data_size_counter");

    private static final ExecutorService PASS_THROUGH_EXECUTOR =
            new ThreadPoolExecutor(10, 60, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    private final ServerProperties serverProperties;

    public FileTransfer(ServerProperties serverProperties) {
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

    public void transferFileToComputeResource(
            String sourcePath,
            String destPath,
            AgentAdapter computeAdapter,
            AgentAdapter storageAdapter,
            String processId)
            throws TaskFailureException {

        try {
            FileMetadata fileMetadata = storageAdapter.getFileMetadata(sourcePath);
            if (fileMetadata.getSize() == 0) {
                logger.error("File {} size is 0 so ignoring the upload", sourcePath);
                throw new TaskFailureException(
                        "Input staging has failed as file " + sourcePath + " size is 0", false, null);
            }
        } catch (AgentException e) {
            logger.error("Failed to fetch metadata for file {}", sourcePath, e);
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
            AgentAdapter storageResourceAdapter,
            String processId)
            throws TaskFailureException {

        try {
            boolean fileExists = adapter.doesFileExist(sourcePath);

            if (!fileExists) {
                logger.warn("File {} not found at source path. Will be retried by Temporal.", sourcePath);
                throw new TaskFailureException(
                        "Source file not found: " + sourcePath, false, new java.io.FileNotFoundException(sourcePath));
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

    // -------------------------------------------------------------------------
    // Transfer strategies
    // -------------------------------------------------------------------------

    void naiveTransfer(
            AgentAdapter srcAdapter, String sourceFile, AgentAdapter destAdapter, String destFile, String tempFile)
            throws TaskFailureException {

        sourceFile = SSHUtil.escapeSpecialCharacters(sourceFile);
        destFile = SSHUtil.escapeSpecialCharacters(destFile);

        logger.info("Using naive transfer to transfer {} to {}", sourceFile, destFile);
        try {
            try {
                logger.info("Downloading file {} to local temp file {}", sourceFile, tempFile);
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
                logger.info("Uploading file from local temp file {} to {}", tempFile, destFile);
                destAdapter.uploadFile(tempFile, destFile);
            } catch (AgentException e) {
                throw new TaskFailureException(
                        "Failed uploading file to " + destFile + " from local path " + tempFile, false, e);
            }
        } finally {
            logger.info("Deleting temporary file {}", tempFile);
            deleteTempFile(tempFile);
        }
    }

    static void passThroughTransfer(
            AgentAdapter srcAdapter, String sourceFile, AgentAdapter destAdapter, String destFile)
            throws TaskFailureException {
        logger.info("Using pass through transfer to transfer {} to {}", sourceFile, destFile);

        FileMetadata tempMetadata;
        try {
            tempMetadata = srcAdapter.getFileMetadata(sourceFile);
        } catch (AgentException e) {
            throw new TaskFailureException("Failed to obtain metadata for file " + sourceFile, false, e);
        }

        final FileMetadata fileMetadata = tempMetadata;

        var queue = new LinkedBlockingQueue<Integer>(1024);
        OutputStream os = new VirtualOutputStream(queue, fileMetadata.getSize());
        InputStream is = new VirtualInputStream(queue, fileMetadata.getSize());

        Callable<TransferResult> inCallable = () -> {
            try {
                logger.info("Executing in-bound transfer for file {}", sourceFile);
                srcAdapter.downloadFile(sourceFile, os, fileMetadata);
                logger.info("Completed in-bound transfer for file {}", sourceFile);
                return new TransferResult(
                        "In", TransferResult.TransferStatus.COMPLETED, "Successfully completed the transfer", null);
            } catch (Exception e) {
                return new TransferResult(
                        "In",
                        TransferResult.TransferStatus.FAILED,
                        "In-bound transfer failed for file " + sourceFile + ". Reason : " + e.getMessage(),
                        e);
            }
        };

        Callable<TransferResult> outCallable = () -> {
            try {
                logger.info("Executing out-bound transfer for file {}", destFile);
                destAdapter.uploadFile(is, fileMetadata, destFile);
                logger.info("Completed out-bound transfer for file {}", destFile);
                return new TransferResult(
                        "Out", TransferResult.TransferStatus.COMPLETED, "Successfully completed the transfer", null);
            } catch (Exception e) {
                return new TransferResult(
                        "Out",
                        TransferResult.TransferStatus.FAILED,
                        "Out-bound transfer failed for file " + destFile + ". Reason : " + e.getMessage(),
                        e);
            }
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
                        logger.debug("Transfer {} completed", res.get().getTransferId());
                    } else {
                        failed++;
                        failedResult = res.get();
                        logger.warn("Transfer {} failed", res.get().getTransferId(), failedResult.getError());
                    }
                    unResolvedFutures.remove(res.get().getTransferId());

                } catch (Exception e) {
                    logger.error("Error occurred while monitoring transfers", e);
                    throw new TaskFailureException("Error occurred while monitoring transfers", false, e);
                }
            }

            if (failed > 0 && failedResult != null) {
                logger.error(
                        "Transfer from {} to {} failed. {}",
                        sourceFile,
                        destFile,
                        failedResult.getMessage(),
                        failedResult.getError());
                throw new TaskFailureException(
                        "Pass through file transfer failed from " + sourceFile + " to " + destFile,
                        false,
                        failedResult.getError());
            } else {
                logger.info("Transfer from {} to {} completed", sourceFile, destFile);
            }

        } finally {
            if (unResolvedFutures.size() > 0) {
                unResolvedFutures.forEach((id, future) -> {
                    try {
                        logger.warn("Cancelling transfer {}", id);
                        future.cancel(true);
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                    }
                });
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    public void deleteTempFile(String filePath) {
        try {
            File tobeDeleted = new File(filePath);
            if (tobeDeleted.exists()) {
                tobeDeleted.delete();
            }
        } catch (Exception e) {
            logger.warn("Failed to delete temporary file {}", filePath);
        }
    }

    // -------------------------------------------------------------------------
    // Stream bridging for pass-through transfers
    // -------------------------------------------------------------------------

    private static class VirtualInputStream extends InputStream {

        private final BlockingQueue<Integer> queue;
        private long byteCount;
        private final long streamLength;

        VirtualInputStream(BlockingQueue<Integer> queue, long streamLength) {
            this.queue = queue;
            this.streamLength = streamLength;
        }

        public int read(byte b[], int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            if (byteCount == streamLength) {
                return -1;
            }

            int c = read();
            b[off] = (byte) c;

            int i = 1;
            try {
                for (; i < len; i++) {
                    if (byteCount == streamLength) {
                        break;
                    }
                    c = read();
                    b[off + i] = (byte) c;
                }
            } catch (IOException ee) {
            }
            return i;
        }

        @Override
        public int read() throws IOException {
            try {
                Integer cont = queue.poll(10, TimeUnit.SECONDS);
                if (cont == null) {
                    throw new IOException("Timed out reading from the queue");
                }
                byteCount++;
                return cont;
            } catch (InterruptedException e) {
                throw new IOException("Read was interrupted", e);
            }
        }
    }

    private static class VirtualOutputStream extends OutputStream {
        private final BlockingQueue<Integer> queue;
        private long byteCount;
        private final long streamLength;

        VirtualOutputStream(BlockingQueue<Integer> queue, long streamLength) {
            this.queue = queue;
            this.streamLength = streamLength;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                if (byteCount == streamLength) {
                    throw new IOException("Can not write more than the stream length " + streamLength);
                }
                boolean status = this.queue.offer(b, 10, TimeUnit.SECONDS);
                if (!status) {
                    throw new IOException("Timed out writing into the queue");
                }
                byteCount++;
            } catch (InterruptedException e) {
                throw new IOException("Write was interrupted", e);
            }
        }
    }

    record TransferResult(String transferId, TransferStatus transferStatus, String message, Throwable error) {

        enum TransferStatus {
            COMPLETED,
            FAILED
        }

        String getTransferId() {
            return transferId;
        }

        TransferStatus getTransferStatus() {
            return transferStatus;
        }

        String getMessage() {
            return message;
        }

        Throwable getError() {
            return error;
        }
    }
}
