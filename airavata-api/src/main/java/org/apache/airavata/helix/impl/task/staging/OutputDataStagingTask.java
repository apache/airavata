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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Output Data Staging Task")
public class OutputDataStagingTask extends DataStagingTask {

    private static final Logger logger = LoggerFactory.getLogger(OutputDataStagingTask.class);
    private static final CountMonitor outputDSTaskCounter = new CountMonitor("output_ds_task_counter");

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        logger.info("Starting output data staging task " + getTaskId() + " in experiment " + getExperimentId());
        outputDSTaskCounter.inc();
        saveAndPublishProcessStatus(ProcessState.OUTPUT_DATA_STAGING);

        try {
            // Get and validate data staging task model
            DataStagingTaskModel dataStagingTaskModel = getDataStagingTaskModel();

            // Fetch and validate input data type from data staging task model
            OutputDataObjectType processOutput = dataStagingTaskModel.getProcessOutput();
            if (processOutput != null && processOutput.getValue() == null) {
                String message = "expId: " + getExperimentId() + ", processId: " + getProcessId() + ", taskId: "
                        + getTaskId() + ":- Couldn't stage file " + processOutput.getName()
                        + " , file name shouldn't be null. ";
                logger.error(message);
                if (processOutput.isIsRequired()) {
                    message += "File name is null, but this output's isRequired bit is not set";
                } else {
                    message += "File name is null";
                }
                throw new TaskOnFailException(message, true, null);
            }

            // Use output storage resource if specified, otherwise fall back to default
            StorageResourceDescription storageResource = getTaskContext().getOutputStorageResourceDescription();

            // Fetch and validate source and destination URLS
            URI sourceURI;
            URI destinationURI;
            String sourceFileName;
            try {
                sourceURI = new URI(dataStagingTaskModel.getSource());
                sourceFileName = sourceURI
                        .getPath()
                        .substring(
                                sourceURI.getPath().lastIndexOf(File.separator) + 1,
                                sourceURI.getPath().length());

                if (dataStagingTaskModel.getDestination().startsWith("dummy")) {
                    StoragePreference outputStoragePref = getTaskContext().getOutputGatewayStorageResourcePreference();
                    String outputStorageRoot = outputStoragePref.getFileSystemRootLocation();
                    String destFilePath = buildDestinationFilePath(outputStorageRoot, sourceFileName);
                    logger.info("Output storage path for task id {} is {}", getTaskId(), destFilePath);
                    destinationURI = new URI(
                            "file",
                            outputStoragePref.getLoginUserName(),
                            storageResource.getHostName(),
                            22,
                            destFilePath,
                            null,
                            null);

                } else {
                    destinationURI = new URI(dataStagingTaskModel.getDestination());
                    logger.info(
                            "Output data staging destination for task id {} is {}",
                            getTaskId(),
                            destinationURI.getPath());
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Source file " + sourceURI.getPath() + ", destination uri " + destinationURI.getPath()
                            + " for task " + getTaskId());
                }
            } catch (URISyntaxException e) {
                throw new TaskOnFailException(
                        "Failed to obtain source URI for output data staging task " + getTaskId(), true, e);
            }

            // Fetch and validate storage adaptor
            StorageResourceAdaptor storageResourceAdaptor = getOutputStorageAdaptor(taskHelper.getAdaptorSupport());

            // Fetch and validate compute resource adaptor
            AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

            List<URI> destinationURIs = new ArrayList<>();
            List<String> successfullyTransferredSourcePaths = new ArrayList<>();

            if (sourceFileName.contains("*")) {
                // if file is declared as a wild card
                logger.info("Handling output files with " + sourceFileName + " extension for task " + getTaskId());

                String destParentPath =
                        (new File(destinationURI.getPath())).getParentFile().getPath();
                String sourceParentPath =
                        (new File(sourceURI.getPath())).getParentFile().getPath();

                logger.debug("Destination parent path " + destParentPath + ", source parent path " + sourceParentPath);
                List<String> filePaths;
                try {
                    filePaths = adaptor.getFileNameFromExtension(sourceFileName, sourceParentPath);

                    if (logger.isTraceEnabled()) {
                        filePaths.forEach(fileName -> logger.trace("File found : " + fileName));
                    }

                } catch (AgentException e) {
                    throw new TaskOnFailException(
                            "Failed to fetch the file list from extension " + sourceFileName, false, e);
                }

                for (String subFilePath : filePaths) {
                    if (subFilePath == null || "".equals(subFilePath)) {
                        logger.warn("Ignoring file transfer as filename is empty or null");
                        continue;
                    }
                    sourceFileName = subFilePath;
                    if (destParentPath.endsWith(File.separator)) {
                        destinationURI = new URI(destParentPath + subFilePath);
                    } else {
                        destinationURI = new URI(destParentPath + File.separator + subFilePath);
                    }

                    URI newSourceURI = new URI((sourceParentPath.endsWith(File.separator)
                                    ? sourceParentPath
                                    : sourceParentPath + File.separator)
                            + sourceFileName);

                    // Wildcard support is only enabled for output data staging
                    assert processOutput != null;
                    logger.info("Transferring file " + sourceFileName);
                    boolean transferred = transferFileToStorage(
                            newSourceURI.getPath(),
                            destinationURI.getPath(),
                            sourceFileName,
                            adaptor,
                            storageResourceAdaptor);
                    if (transferred) {
                        destinationURIs.add(destinationURI);
                        successfullyTransferredSourcePaths.add(newSourceURI.getPath());
                    } else {
                        logger.warn("File {} did not transfer", sourceFileName);
                    }

                    if (processOutput.getType() == DataType.URI) {
                        if (filePaths.size() > 1) {
                            logger.warn(
                                    "More than one file matched wildcard, but output type is URI. Skipping remaining matches: {}",
                                    filePaths.subList(1, filePaths.size()));
                        }
                        break;
                    }
                }
                if (!destinationURIs.isEmpty()) {
                    if (processOutput.getType() == DataType.URI) {
                        saveExperimentOutput(
                                processOutput.getName(),
                                escapeSpecialCharacters(destinationURIs.get(0).toString()));
                    } else if (processOutput.getType() == DataType.URI_COLLECTION) {
                        saveExperimentOutputCollection(
                                processOutput.getName(),
                                destinationURIs.stream()
                                        .map(URI::toString)
                                        .map(this::escapeSpecialCharacters)
                                        .collect(Collectors.toList()));
                    }

                    try {
                        ApplicationInterfaceDescription appInterface =
                                getTaskContext().getApplicationInterfaceDescription();
                        if (appInterface != null && appInterface.isCleanAfterStaged()) {
                            logger.info(
                                    "cleanAfterStaged is enabled, deleting source files after successful staging for task with the Id: {}",
                                    getTaskId());
                            // Delete only successfully transferred source files
                            boolean allDeleted = deleteSourceFiles(successfullyTransferredSourcePaths, adaptor);
                            if (!allDeleted) {
                                logger.warn(
                                        "Some source files could not be deleted after staging for task {}.",
                                        getTaskId());
                            } else if (!successfullyTransferredSourcePaths.isEmpty()) {
                                logger.info(
                                        "Successfully deleted all {} source file(s) after staging for task {}",
                                        successfullyTransferredSourcePaths.size(),
                                        getTaskId());
                            }
                            deleteEmptyDirectoryIfNeeded(sourceParentPath, adaptor);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to clean up source files after staging for task {}", getTaskId(), e);
                    }
                }
                return onSuccess("Output data staging task " + getTaskId() + " successfully completed");

            } else {
                // Uploading output file to the storage resource
                assert processOutput != null;
                boolean transferred = transferFileToStorage(
                        sourceURI.getPath(), destinationURI.getPath(), sourceFileName, adaptor, storageResourceAdaptor);
                if (transferred) {
                    saveExperimentOutput(processOutput.getName(), escapeSpecialCharacters(destinationURI.toString()));

                    try {
                        ApplicationInterfaceDescription appInterface =
                                getTaskContext().getApplicationInterfaceDescription();
                        if (appInterface != null && appInterface.isCleanAfterStaged()) {
                            logger.info(
                                    "cleanAfterStaged is enabled, deleting source file after successful staging for task with the Id: {}",
                                    getTaskId());
                            boolean deleted = deleteSourceFiles(List.of(sourceURI.getPath()), adaptor);
                            if (!deleted) {
                                logger.warn("Source file could not be deleted after staging for task {}.", getTaskId());
                            } else {
                                logger.info("Successfully deleted source file after staging for task {}", getTaskId());
                            }
                            String sourceParentPath = (new File(sourceURI.getPath()))
                                    .getParentFile()
                                    .getPath();
                            deleteEmptyDirectoryIfNeeded(sourceParentPath, adaptor);
                        }
                    } catch (Exception e) {
                        logger.warn(
                                "Failed to clean up source file after staging for task {}. Staging completed successfully.",
                                getTaskId(),
                                e);
                    }
                } else {
                    logger.warn("File {} did not transfer", sourceFileName);
                }
                return onSuccess("Output data staging task " + getTaskId() + " successfully completed");
            }

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return onFail(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing output data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing output data staging task " + getTaskId(), false, e);
        }
    }

    private boolean deleteSourceFiles(List<String> filePaths, AgentAdaptor adaptor) {
        boolean allSucceeded = true;
        for (String filePath : filePaths) {
            if (filePath == null || filePath.trim().isEmpty()) {
                continue;
            }
            try {
                String escapedPath = filePath.replace("'", "'\"'\"'");
                String deleteCommand = "rm -f '" + escapedPath + "'";

                logger.debug("Deleting source file: {}", filePath);
                CommandOutput deleteOutput = adaptor.executeCommand(deleteCommand, null);

                if (deleteOutput.getExitCode() != 0) {
                    logger.warn(
                            "Failed to delete source file {} (exit code: {}). Stdout: {}, Stderr: {}",
                            filePath,
                            deleteOutput.getExitCode(),
                            deleteOutput.getStdOut(),
                            deleteOutput.getStdError());
                    allSucceeded = false;
                } else {
                    logger.debug("Successfully deleted source file: {}", filePath);
                }
            } catch (AgentException e) {
                logger.warn("Exception while deleting source file {}: {}", filePath, e.getMessage(), e);
                allSucceeded = false;
            } catch (Exception e) {
                logger.warn("Unexpected error while deleting source file {}: {}", filePath, e.getMessage(), e);
                allSucceeded = false;
            }
        }
        return allSucceeded;
    }

    private void deleteEmptyDirectoryIfNeeded(String directoryPath, AgentAdaptor adaptor) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }

        try {
            List<String> directoryContents = adaptor.listDirectory(directoryPath);
            if (directoryContents == null || directoryContents.isEmpty()) {
                String escapedPath = directoryPath.replace("'", "'\"'\"'");
                String rmdirCommand = "rmdir '" + escapedPath + "'";

                logger.debug("Removing empty directory: {}", directoryPath);
                CommandOutput rmdirOutput = adaptor.executeCommand(rmdirCommand, null);

                if (rmdirOutput.getExitCode() != 0) {
                    logger.debug(
                            "Could not remove directory {} (may not be empty or may have been removed already). Exit code: {}, Stderr: {}",
                            directoryPath,
                            rmdirOutput.getExitCode(),
                            rmdirOutput.getStdError());
                } else {
                    logger.debug("Successfully removed empty directory: {}", directoryPath);
                }
            } else {
                logger.debug(
                        "Directory {} is not empty (contains {} items), skipping removal",
                        directoryPath,
                        directoryContents.size());
            }
        } catch (AgentException e) {
            logger.debug("Could not check or remove directory {}: {}", directoryPath, e.getMessage());

        } catch (Exception e) {
            logger.debug("Unexpected error while checking directory {}: {}", directoryPath, e.getMessage());
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
