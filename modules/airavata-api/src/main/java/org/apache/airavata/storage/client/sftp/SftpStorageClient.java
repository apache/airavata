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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.core.exception.TaskFailureException;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.protocol.CommandOutput;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.storage.client.StorageClient;
import org.apache.airavata.storage.resource.model.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SFTP-based storage client implementing all data movement lifecycle operations.
 *
 * <p>Consolidates input staging, output staging, and archival into a single class.
 * Uses {@link DataStagingSupport} for transfer utilities and {@link SftpClient}
 * for adapter resolution. All data movement is resolved directly from the enriched
 * {@link TaskContext}.
 */
@Component
@ConditionalOnParticipant
public class SftpStorageClient implements StorageClient {

    private static final Logger logger = LoggerFactory.getLogger(SftpStorageClient.class);

    private final DataStagingSupport dataStagingSupport;
    private final SftpClient sftpClient;
    private final AdapterSupport adapterSupport;
    private final ExperimentRepository experimentRepository;
    private final ServerProperties serverProperties;

    public SftpStorageClient(
            DataStagingSupport dataStagingSupport,
            SftpClient sftpClient,
            AdapterSupport adapterSupport,
            ExperimentRepository experimentRepository,
            ServerProperties serverProperties) {
        this.dataStagingSupport = dataStagingSupport;
        this.sftpClient = sftpClient;
        this.adapterSupport = adapterSupport;
        this.experimentRepository = experimentRepository;
        this.serverProperties = serverProperties;
    }

    // -------------------------------------------------------------------------
    // Input staging
    // -------------------------------------------------------------------------

    /**
     * Stages all URI and URI_COLLECTION inputs from storage into the compute resource working
     * directory. Iterates over every process input and skips non-file types. Optional inputs with
     * null/empty values are silently skipped; required inputs with null values fail immediately.
     */
    @Override
    public DagTaskResult stageIn(TaskContext context) {
        logger.info("Starting input data staging for process {}", context.getProcessId());

        try {
            var processInputs = context.getProcessModel().getProcessInputs();
            if (processInputs == null || processInputs.isEmpty()) {
                logger.info("No process inputs to stage for process {}", context.getProcessId());
                return new DagTaskResult.Success("No input files to stage");
            }

            var storageResourceAdapter = sftpClient.resolveStorageAdapter(
                    context.getProcessModel().getInputStorageResourceId(),
                    "input", adapterSupport, context, context.getTaskId());
            var adapter = sftpClient.getComputeResourceAdapter(
                    adapterSupport, context, context.getTaskId());

            String workingDir = context.getWorkingDir();

            for (ApplicationInput input : processInputs) {
                // Only stage URI and URI_COLLECTION inputs
                if (input.getType() != DataType.URI && input.getType() != DataType.URI_COLLECTION) {
                    continue;
                }

                // Skip optional inputs with no value
                if ((input.getValue() == null || input.getValue().isEmpty()) && !input.getIsRequired()) {
                    logger.debug("Skipping optional input '{}' with null/empty value for process {}",
                            input.getName(), context.getProcessId());
                    continue;
                }

                // Required inputs with null values are a hard failure
                if (input.getValue() == null) {
                    String message = "expId: " + context.getExperimentId()
                            + ", processId: " + context.getProcessId()
                            + ", taskId: " + context.getTaskId()
                            + ":- Couldn't stage file " + input.getName()
                            + " , file name shouldn't be null. File name is null, but this input's isRequired bit is set";
                    logger.error(message);
                    return new DagTaskResult.Failure(message, true);
                }

                // Build the destination path — working dir + optional override filename
                String destPath = workingDir.endsWith("/") ? workingDir : workingDir + "/";
                if (input.getOverrideFilename() != null && !input.getOverrideFilename().isBlank()) {
                    destPath += input.getOverrideFilename();
                }

                // Split URI_COLLECTION by comma; treat URI as a single-element array
                String[] sourceUrls;
                if (input.getType() == DataType.URI_COLLECTION) {
                    logger.info("Found a URI collection so splitting by comma for input '{}' in process {}",
                            input.getName(), context.getProcessId());
                    sourceUrls = input.getValue().split(",");
                } else {
                    sourceUrls = new String[]{input.getValue()};
                }

                for (String url : sourceUrls) {
                    try {
                        URI sourceURI = new URI(url.trim());
                        String sourcePath = sourceURI.getPath();

                        // If no override filename was specified, append the source filename to the dest dir
                        String resolvedDestPath = destPath;
                        if (input.getOverrideFilename() == null || input.getOverrideFilename().isBlank()) {
                            String sourceFileName = sourcePath.substring(
                                    sourcePath.lastIndexOf('/') + 1);
                            resolvedDestPath = (workingDir.endsWith("/") ? workingDir : workingDir + "/")
                                    + sourceFileName;
                        }

                        logger.info("Staging input '{}': {} -> {} for process {}",
                                input.getName(), sourcePath, resolvedDestPath, context.getProcessId());
                        dataStagingSupport.transferFileToComputeResource(
                                sourcePath, resolvedDestPath, adapter, storageResourceAdapter,
                                context.getProcessId());

                    } catch (URISyntaxException e) {
                        return new DagTaskResult.Failure(
                                "Failed to parse source URI '" + url + "' for input '" + input.getName()
                                        + "' in task " + context.getTaskId(),
                                true, e);
                    }
                }
            }

            return new DagTaskResult.Success("Input data staging completed for process " + context.getProcessId());

        } catch (TaskFailureException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return new DagTaskResult.Failure(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing input data staging for process " + context.getProcessId(), e);
            return new DagTaskResult.Failure(
                    "Unknown error while executing input data staging for process " + context.getProcessId(),
                    false, e);
        }
    }

    // -------------------------------------------------------------------------
    // Output staging
    // -------------------------------------------------------------------------

    /**
     * Stages all URI, URI_COLLECTION, STDOUT, and STDERR outputs from the compute resource
     * working directory back to storage. Iterates over every process output. Outputs with null
     * values are skipped. Wildcard outputs are expanded on the compute resource before transfer.
     */
    @Override
    public DagTaskResult stageOut(TaskContext context) {
        logger.info("Starting output data staging for process {} in experiment {}",
                context.getProcessId(), context.getExperimentId());

        try {
            var processOutputs = context.getProcessModel().getProcessOutputs();
            if (processOutputs == null || processOutputs.isEmpty()) {
                logger.info("No process outputs to stage for process {}", context.getProcessId());
                return new DagTaskResult.Success("No output files to stage");
            }

            var storageResourceAdapter = sftpClient.resolveStorageAdapter(
                    context.getProcessModel().getOutputStorageResourceId(),
                    "output", adapterSupport, context, context.getTaskId());
            var adapter = sftpClient.getComputeResourceAdapter(
                    adapterSupport, context, context.getTaskId());

            String workingDir = context.getWorkingDir();

            for (ApplicationOutput output : processOutputs) {
                if (output.getValue() == null) {
                    logger.debug("Skipping output '{}' with null value for process {}",
                            output.getName(), context.getProcessId());
                    continue;
                }

                DataType type = output.getType();
                if (type != DataType.URI && type != DataType.URI_COLLECTION
                        && type != DataType.STDOUT && type != DataType.STDERR) {
                    continue;
                }

                // The output value is the file name (or pattern) relative to the working dir
                String sourceFile = output.getValue();
                String sourceDir = workingDir.endsWith("/") ? workingDir : workingDir + "/";
                String sourcePath = sourceDir + sourceFile;

                // Compute destination path in storage using the experiment data dir
                String outputStorageRoot = workingDir;
                String destFilePath = dataStagingSupport.buildDestinationFilePath(
                        outputStorageRoot, sourceFile, context);

                if (logger.isDebugEnabled()) {
                    logger.debug("Output '{}': source={}, destination={} for process {}",
                            output.getName(), sourcePath, destFilePath, context.getProcessId());
                }

                if (sourceFile.contains("*")) {
                    // Wildcard output — expand on compute resource then transfer each match
                    DagTaskResult wildcardResult = stageWildcardOutput(
                            context, output, sourcePath, destFilePath, adapter, storageResourceAdapter);
                    // A wildcard failure is non-critical (other outputs can still be staged)
                    if (wildcardResult instanceof DagTaskResult.Failure f && f.fatal()) {
                        return wildcardResult;
                    }
                } else {
                    boolean transferred = dataStagingSupport.transferFileToStorage(
                            sourcePath, destFilePath, sourceFile, adapter, storageResourceAdapter,
                            context.getProcessId());
                    if (transferred) {
                        saveExperimentOutput(
                                context.getExperimentId(),
                                output.getName(),
                                dataStagingSupport.escapeSpecialCharacters("file://" + destFilePath));
                    } else {
                        logger.warn("Output file '{}' did not transfer for process {}",
                                sourceFile, context.getProcessId());
                    }
                }
            }

            return new DagTaskResult.Success(
                    "Output data staging completed for process " + context.getProcessId());

        } catch (TaskFailureException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return new DagTaskResult.Failure(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing output data staging for process " + context.getProcessId(), e);
            return new DagTaskResult.Failure(
                    "Unknown error while executing output data staging for process " + context.getProcessId(),
                    false, e);
        }
    }

    /**
     * Handles wildcard output staging: expands the glob pattern on the compute resource,
     * then transfers each matched file to storage, collecting destination URIs for
     * persisting back to the experiment output record.
     */
    private DagTaskResult stageWildcardOutput(
            TaskContext context,
            ApplicationOutput output,
            String sourceGlobPath,
            String destFilePath,
            org.apache.airavata.protocol.AgentAdapter adapter,
            org.apache.airavata.protocol.StorageResourceAdapter storageResourceAdapter) {

        logger.info("Handling wildcard output '{}' with pattern {} for process {}",
                output.getName(), sourceGlobPath, context.getProcessId());

        String sourceFileName = new File(sourceGlobPath).getName();
        String sourceParentPath = new File(sourceGlobPath).getParent();
        String destParentPath = new File(destFilePath).getParent();

        List<String> filePaths;
        try {
            filePaths = adapter.getFileNameFromExtension(sourceFileName, sourceParentPath);
            if (logger.isTraceEnabled()) {
                filePaths.forEach(f -> logger.trace("Wildcard match found: {}", f));
            }
        } catch (AgentException e) {
            return new DagTaskResult.Failure(
                    "Failed to fetch the file list for wildcard pattern '" + sourceFileName
                            + "' from directory " + sourceParentPath,
                    false, e);
        }

        List<URI> destinationURIs = new ArrayList<>();

        for (String subFilePath : filePaths) {
            if (subFilePath == null || subFilePath.isEmpty()) {
                logger.warn("Ignoring wildcard match with empty filename");
                continue;
            }

            String currentSourcePath = (sourceParentPath.endsWith(File.separator)
                    ? sourceParentPath : sourceParentPath + File.separator) + subFilePath;

            String currentDestPath = (destParentPath.endsWith(File.separator)
                    ? destParentPath : destParentPath + File.separator) + subFilePath;

            logger.info("Transferring wildcard output file '{}'", subFilePath);
            boolean transferred;
            try {
                transferred = dataStagingSupport.transferFileToStorage(
                        currentSourcePath, currentDestPath, subFilePath,
                        adapter, storageResourceAdapter, context.getProcessId());
            } catch (TaskFailureException e) {
                return new DagTaskResult.Failure(
                        "Failed to transfer wildcard output file '" + subFilePath + "'", false, e.getError());
            }

            if (transferred) {
                try {
                    destinationURIs.add(new URI(currentDestPath));
                } catch (URISyntaxException e) {
                    logger.warn("Could not convert destination path '{}' to URI", currentDestPath, e);
                }
            } else {
                logger.warn("Wildcard file '{}' did not transfer", subFilePath);
            }

            // URI type only wants the first match
            if (output.getType() == DataType.URI) {
                if (filePaths.size() > 1) {
                    logger.warn(
                            "More than one file matched wildcard but output type is URI. "
                                    + "Skipping remaining {} matches.",
                            filePaths.size() - 1);
                }
                break;
            }
        }

        if (!destinationURIs.isEmpty()) {
            if (output.getType() == DataType.URI) {
                saveExperimentOutput(
                        context.getExperimentId(),
                        output.getName(),
                        dataStagingSupport.escapeSpecialCharacters(destinationURIs.get(0).toString()));
            } else if (output.getType() == DataType.URI_COLLECTION) {
                saveExperimentOutputCollection(
                        context.getExperimentId(),
                        output.getName(),
                        destinationURIs.stream()
                                .map(URI::toString)
                                .map(dataStagingSupport::escapeSpecialCharacters)
                                .collect(Collectors.toList()));
            }
        }

        return new DagTaskResult.Success(
                "Wildcard output staging completed for output '" + output.getName() + "'");
    }

    // -------------------------------------------------------------------------
    // Archival
    // -------------------------------------------------------------------------

    /**
     * Archives the process working directory on the compute resource into a tar file and
     * transfers it to storage. The working directory path is resolved directly from the
     * {@link TaskContext} without any pre-built model.
     */
    @Override
    public DagTaskResult archive(TaskContext context) {
        logger.info("Starting archival task {} in experiment {}", context.getTaskId(), context.getExperimentId());

        try {
            String workingDir = context.getWorkingDir();
            if (!workingDir.endsWith("/")) {
                workingDir += "/";
            }
            // tarDirPath is the working dir without trailing slash
            String tarDirPath = workingDir.substring(0, workingDir.length() - 1);

            final String archiveFileName = "archive.tar";
            String tarCreationAbsPath = tarDirPath + File.separator + archiveFileName;

            String outputStorageRoot = context.getWorkingDir();
            String destFilePath = dataStagingSupport.buildDestinationFilePath(
                    outputStorageRoot, archiveFileName, context);

            var storageResourceAdapter = sftpClient.resolveStorageAdapter(
                    context.getProcessModel().getOutputStorageResourceId(),
                    "output", adapterSupport, context, context.getTaskId());
            var adapter = sftpClient.getComputeResourceAdapter(
                    adapterSupport, context, context.getTaskId());

            String tarringCommand = "cd " + tarDirPath
                    + " && find ./ -not -type l -not -type d -print0 | tar --null --files-from - -cvf "
                    + tarCreationAbsPath;

            logger.info("Running tar creation command {}", tarringCommand);

            try {
                CommandOutput tarCommandOutput = adapter.executeCommand(tarringCommand, null);
                if (tarCommandOutput.getExitCode() != 0) {
                    return new DagTaskResult.Failure(
                            "Failed while running the tar command " + tarringCommand + ". Sout : "
                                    + tarCommandOutput.getStdOut() + ". Serr " + tarCommandOutput.getStdError(),
                            false);
                }
            } catch (AgentException e) {
                return new DagTaskResult.Failure(
                        "Failed while running the tar command " + tarringCommand, true, null);
            }

            try {
                var fileMetadata = adapter.getFileMetadata(tarCreationAbsPath);
                long maxArchiveSize = serverProperties.maxArchiveSize();

                if (fileMetadata.getSize() < maxArchiveSize) {
                    boolean fileTransferred = dataStagingSupport.transferFileToStorage(
                            tarCreationAbsPath, destFilePath, archiveFileName, adapter, storageResourceAdapter,
                            context.getProcessId());
                    if (!fileTransferred) {
                        logger.error("Failed to transfer created archive file {}", tarCreationAbsPath);
                        return new DagTaskResult.Failure(
                                "Failed to transfer created archive file " + tarCreationAbsPath, false);
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
                            CommandOutput unTarCommandOutput =
                                    storageResourceAdapter.executeCommand(command, destParent);
                            if (unTarCommandOutput.getExitCode() != 0) {
                                return new DagTaskResult.Failure(
                                        "Failed while running the un-archiving command " + command + ". Sout : "
                                                + unTarCommandOutput.getStdOut() + ". Serr : "
                                                + unTarCommandOutput.getStdError(),
                                        false);
                            }
                        }
                    } catch (AgentException e) {
                        return new DagTaskResult.Failure(
                                "Failed while running the untar command " + tarringCommand, false, null);
                    }

                    return new DagTaskResult.Success("Archival task successfully completed");

                } else {
                    logger.error(
                            "Archive size {} MB is larger than the maximum allowed size {} MB. Skipping transfer.",
                            fileMetadata.getSize() / (1024L * 1024L),
                            maxArchiveSize / (1024L * 1024L));
                    return new DagTaskResult.Failure(
                            "Archive task was skipped as size is " + fileMetadata.getSize() / (1024L * 1024L) + " MB",
                            true);
                }

            } finally {
                String deleteTarCommand = "rm " + tarCreationAbsPath;
                logger.info("Running delete temporary tar command {}", deleteTarCommand);
                try {
                    CommandOutput rmCommandOutput = adapter.executeCommand(deleteTarCommand, null);
                    if (rmCommandOutput.getExitCode() != 0) {
                        logger.error("Failed while running the rm command {}. Sout : {} Serr {}",
                                deleteTarCommand, rmCommandOutput.getStdOut(), rmCommandOutput.getStdError());
                    }
                } catch (AgentException e) {
                    logger.error("Failed while running the rm command {}", tarCreationAbsPath, e);
                }
            }

        } catch (TaskFailureException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            logger.error("Failed while un-archiving the data", e);
            return new DagTaskResult.Failure(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing archiving staging task " + context.getTaskId(), e);
            return new DagTaskResult.Failure(
                    "Unknown error while executing archiving staging task " + context.getTaskId(), false, e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void saveExperimentOutput(String experimentId, String outputName, String outputVal) {
        ExperimentEntity entity = experimentRepository.findById(experimentId).orElse(null);
        if (entity == null) {
            return;
        }
        List<ExperimentOutputEntity> outputs = entity.getOutputs();
        if (outputs == null) {
            outputs = new ArrayList<>();
            entity.setOutputs(outputs);
        }
        boolean found = false;
        for (ExperimentOutputEntity output : outputs) {
            if (outputName.equals(output.getName())) {
                output.setValue(outputVal);
                found = true;
                break;
            }
        }
        if (!found) {
            var newOutput = new ExperimentOutputEntity();
            newOutput.setOutputId(java.util.UUID.randomUUID().toString());
            newOutput.setName(outputName);
            newOutput.setValue(outputVal);
            newOutput.setType("STRING");
            newOutput.setExperiment(entity);
            outputs.add(newOutput);
        }
        experimentRepository.save(entity);
    }

    private void saveExperimentOutputCollection(String experimentId, String outputName, List<String> outputVals) {
        String collectionValue = String.join(",", outputVals);
        saveExperimentOutput(experimentId, outputName, collectionValue);
    }
}
