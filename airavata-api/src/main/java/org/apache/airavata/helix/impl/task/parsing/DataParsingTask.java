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
package org.apache.airavata.helix.impl.task.parsing;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInputs;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutputs;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.data.replica.ReplicaPersistentType;
import org.apache.airavata.monitor.platform.CountMonitor;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the data parsing task.
 *
 * @since 1.0.0-SNAPSHOT
 */
@TaskDef(name = "Data Parsing Task")
public class DataParsingTask extends AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(DataParsingTask.class);
    private static final CountMonitor parsingTaskCounter = new CountMonitor("parsing_task_counter");

    private final RegistryService registryService;
    private final AiravataServerProperties properties;

    public DataParsingTask(
            RegistryService registryService, AiravataServerProperties properties) {
        this.registryService = registryService;
        this.properties = properties;
    }

    @TaskParam(name = "Parser Id")
    private String parserId;

    @TaskParam(name = "Parser inputs")
    private ParsingTaskInputs parsingTaskInputs;

    @TaskParam(name = "Parser outputs")
    private ParsingTaskOutputs parsingTaskOutputs;

    @TaskParam(name = "Gateway ID")
    private String gatewayId;

    @TaskParam(name = "Group Resource Profile Id")
    private String groupResourceProfileId;

    @TaskParam(name = "Local data dir")
    private String localDataDir;

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Starting data parsing task " + getTaskId());
        parsingTaskCounter.inc();
        try {

            Parser parser = getRegistryServiceClient().getParser(parserId, gatewayId);
            String containerId = getTaskId() + "_PARSER_" + parser.getId();
            containerId = containerId.replace(" ", "-");

            String localInputDir = createLocalInputDir(containerId);
            String localOutDir = createLocalOutputDir(containerId);
            logger.info("Created local input and ouput directories : " + localInputDir + ", " + localOutDir);

            logger.info("Downloading input files to local input directory");

            Map<String, String> properties = new HashMap<>();
            for (ParserInput parserInput : parser.getInputFiles()) {
                Optional<ParsingTaskInput> filteredInputOptional = parsingTaskInputs.getInputs().stream()
                        .filter(inp -> parserInput.getId().equals(inp.getId()))
                        .findFirst();

                if (filteredInputOptional.isPresent()) {

                    ParsingTaskInput parsingTaskInput = filteredInputOptional.get();

                    String inputVal = parsingTaskInput.getValue() != null
                            ? parsingTaskInput.getValue()
                            : getContextVariable(parsingTaskInput.getContextVariableName());

                    if ("PROPERTY".equals(parsingTaskInput.getType())) {
                        properties.put(parsingTaskInput.getName(), inputVal);
                    } else if ("FILE".equals(parsingTaskInput.getType())) {

                        String inputDataProductUri = inputVal;

                        if (inputDataProductUri == null || inputDataProductUri.isEmpty()) {
                            logger.error("Data product uri could not be null or empty for input "
                                    + parsingTaskInput.getId() + " with name " + parserInput.getName());
                            throw new TaskOnFailException(
                                    "Data product uri could not be null or empty for input " + parsingTaskInput.getId()
                                            + " with name " + parserInput.getName(),
                                    true,
                                    null);
                        }
                        DataProductModel inputDataProduct =
                                getRegistryServiceClient().getDataProduct(inputDataProductUri);
                        List<DataReplicaLocationModel> replicaLocations = inputDataProduct.getReplicaLocations();

                        boolean downloadPassed = false;

                        for (DataReplicaLocationModel replicaLocationModel : replicaLocations) {
                            String storageResourceId = replicaLocationModel.getStorageResourceId();
                            String remoteFilePath = new URI(replicaLocationModel.getFilePath()).getPath();
                            String localFilePath = localInputDir
                                    + (localInputDir.endsWith(File.separator) ? "" : File.separator)
                                    + parserInput.getName();

                            downloadPassed = downloadFileFromStorageResource(
                                    storageResourceId, remoteFilePath, localFilePath, helper.getAdaptorSupport());

                            if (downloadPassed) {
                                break;
                            }
                        }

                        if (!downloadPassed) {
                            logger.error("Failed to download input file with id " + parserInput.getId()
                                    + " from data product uri " + inputDataProductUri);
                            throw new TaskOnFailException(
                                    "Failed to download input file with id " + parserInput.getId()
                                            + " from data product uri " + inputDataProductUri,
                                    true,
                                    null);
                        }
                    }
                } else {
                    if (parserInput.isRequiredInput()) {
                        logger.error("Parser input with id " + parserInput.getId() + " and name "
                                + parserInput.getName() + " is not available");
                        throw new TaskOnFailException(
                                "Parser input with id " + parserInput.getId() + " and name " + parserInput.getName()
                                        + " is not available",
                                true,
                                null);
                    } else {
                        logger.warn("Parser input with id with id " + parserInput.getId() + " and name "
                                + parserInput.getName() + " is not available. But it is not required");
                    }
                }
            }

            logger.info("Running container with id " + containerId + " local input dir " + localInputDir
                    + " local output dir " + localOutDir);
            runContainer(parser, containerId, localInputDir, localOutDir, properties);

            for (ParserOutput parserOutput : parser.getOutputFiles()) {

                Optional<ParsingTaskOutput> filteredOutputOptional = parsingTaskOutputs.getOutputs().stream()
                        .filter(out -> parserOutput.getId().equals(out.getId()))
                        .findFirst();

                if (filteredOutputOptional.isPresent()) {

                    ParsingTaskOutput parsingTaskOutput = filteredOutputOptional.get();
                    String localFilePath = localOutDir
                            + (localOutDir.endsWith(File.separator) ? "" : File.separator)
                            + parserOutput.getName();
                    String remoteFilePath = "parsers" + File.separator + getTaskId() + File.separator + "outputs"
                            + File.separator + parserOutput.getName();

                    if (new File(localFilePath).exists()) {
                        uploadFileToStorageResource(
                                parsingTaskOutput, remoteFilePath, localFilePath, helper.getAdaptorSupport());
                    } else if (parserOutput.isRequiredOutput()) {
                        logger.error("Expected output file " + localFilePath + " can not be found");
                        throw new TaskOnFailException(
                                "Expected output file " + localFilePath + " can not be found", false, null);
                    } else {
                        logger.error("Expected output file " + localFilePath
                                + " can not be found but skipping as it is not mandatory");
                    }
                } else {
                    if (parserOutput.isRequiredOutput()) {
                        logger.error("File upload info with id " + parserOutput.getId() + " and name "
                                + parserOutput.getName() + " is not available");
                        throw new TaskOnFailException(
                                "File upload info with id " + parserOutput.getId() + " and name "
                                        + parserOutput.getName() + " is not available",
                                true,
                                null);
                    } else {
                        logger.warn("File upload info with id " + parserOutput.getId() + " and name "
                                + parserOutput.getName() + " is not available. But it is not required");
                    }
                }
            }

            return onSuccess("Successfully completed data parsing task " + getTaskId());
        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }

            return onFail(e.getReason(), e.isCritical());

        } catch (Exception e) {
            logger.error("Unknown error occurred in " + getTaskId(), e);
            return onFail("Unknown error occurred in " + getTaskId(), true);
        }
    }

    /**
     * Called when the task is cancelled.
     * No cleanup needed for data parsing tasks.
     */
    @Override
    public void onCancel() {}

    private void runContainer(
            Parser parser,
            String containerId,
            String localInputDir,
            String localOutputDir,
            Map<String, String> properties)
            throws ApplicationSettingsException {
        DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        logger.info("Pulling image " + parser.getImageName());
        dockerClient
                .pullImageCmd(parser.getImageName().split(":")[0])
                .withTag(parser.getImageName().split(":")[1])
                .exec(new PullImageResultCallback())
                .awaitSuccess();

        logger.info("Successfully pulled image " + parser.getImageName());

        String commands[] = parser.getExecutionCommand().split(" ");
        CreateContainerResponse containerResponse = dockerClient
                .createContainerCmd(parser.getImageName())
                .withCmd(commands)
                .withName(containerId)
                .withBinds(
                        Bind.parse(localInputDir + ":" + parser.getInputDirPath()),
                        Bind.parse(localOutputDir + ":" + parser.getOutputDirPath()))
                .withTty(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withEnv(properties.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.toList()))
                .exec();

        logger.info("Created the container with id " + containerResponse.getId());

        final StringBuilder dockerLogs = new StringBuilder();

        if (containerResponse.getWarnings() != null && containerResponse.getWarnings().length > 0) {
            StringBuilder warningStr = new StringBuilder();
            for (String w : containerResponse.getWarnings()) {
                warningStr.append(w).append(",");
            }
            logger.warn("Container " + containerResponse.getId() + " warnings : " + warningStr);
        } else {
            logger.info("Starting container with id " + containerResponse.getId());
            dockerClient.startContainerCmd(containerResponse.getId()).exec();
            LogContainerCmd logContainerCmd = dockerClient
                    .logContainerCmd(containerResponse.getId())
                    .withStdOut(true)
                    .withStdErr(true);

            try {
                logContainerCmd
                        .exec(new LogContainerResultCallback() {
                            @Override
                            public void onNext(Frame item) {
                                dockerLogs.append(item.toString());
                                dockerLogs.append("\n");
                            }
                        })
                        .awaitCompletion();
            } catch (InterruptedException e) {
                logger.error("Interrupted while reading container log" + e.getMessage());
            }

            logger.info("Waiting for the container to stop");
            Integer statusCode = dockerClient
                    .waitContainerCmd(containerResponse.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode();
            logger.info("Container " + containerResponse.getId() + " exited with status code " + statusCode);
            logger.info("Container logs " + dockerLogs.toString());
        }

        boolean deleteContainer = this.properties != null && this.properties.services != null 
                && this.properties.services.parser != null && this.properties.services.parser.deleteContainer;
        if (deleteContainer) {
            dockerClient.removeContainerCmd(containerResponse.getId()).exec();
            logger.info("Successfully removed container with id " + containerResponse.getId());
        }
    }

    private StorageResourceAdaptor getStorageResourceAdaptor(String storageResourceId, AdaptorSupport adaptorSupport)
            throws TaskOnFailException, AgentException, RegistryServiceException {
        if (registryService == null) {
            throw new TaskOnFailException("RegistryService not available.", false, null);
        }
        var gatewayStoragePreference = registryService.getGatewayStoragePreference(gatewayId, storageResourceId);
        var gatewayResourceProfile = registryService.getGatewayResourceProfile(gatewayId);
        var token = gatewayStoragePreference.getResourceSpecificCredentialStoreToken();
        if (token == null || token.isEmpty()) {
            token = gatewayResourceProfile.getCredentialStoreToken();
        }
        logger.info("Fetching adaptor for storage resource {} with token {}", storageResourceId, token);
        return adaptorSupport.fetchStorageAdaptor(
                gatewayId,
                storageResourceId,
                DataMovementProtocol.SCP,
                token,
                gatewayStoragePreference.getLoginUserName());
    }

    private boolean downloadFileFromStorageResource(
            String storageResourceId, String remoteFilePath, String localFilePath, AdaptorSupport adaptorSupport) {

        logger.info(
                "Downloading from storage resource {} from path {} to local path {}",
                storageResourceId,
                remoteFilePath,
                localFilePath);
        try {
            var storageResourceAdaptor = getStorageResourceAdaptor(storageResourceId, adaptorSupport);
            storageResourceAdaptor.downloadFile(remoteFilePath, localFilePath);
            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to download file from storage {} in path {} to local path {}",
                    storageResourceId,
                    remoteFilePath,
                    localFilePath,
                    e);
            return false;
        }
    }

    private void uploadFileToStorageResource(
            ParsingTaskOutput parsingTaskOutput,
            String remoteFilePath,
            String localFilePath,
            AdaptorSupport adaptorSupport)
            throws TaskOnFailException {
        logger.info(
                "Uploading from local path {} to remote path {} of storage resource {}",
                localFilePath,
                remoteFilePath,
                parsingTaskOutput.getStorageResourceId());
        try {
            var gatewayStoragePreference = getRegistryServiceClient()
                    .getGatewayStoragePreference(gatewayId, parsingTaskOutput.getStorageResourceId());
            var storageResource =
                    getRegistryServiceClient().getStorageResource(parsingTaskOutput.getStorageResourceId());
            var remoteFileRoot = gatewayStoragePreference.getFileSystemRootLocation();
            remoteFilePath =
                    remoteFileRoot + (remoteFileRoot.endsWith(File.separator) ? "" : File.separator) + remoteFilePath;
            var storageResourceAdaptor =
                    getStorageResourceAdaptor(parsingTaskOutput.getStorageResourceId(), adaptorSupport);
            storageResourceAdaptor.createDirectory(new File(remoteFilePath).getParent(), true);
            storageResourceAdaptor.uploadFile(localFilePath, remoteFilePath);

            logger.info("Uploading completed. Registering data product for path " + remoteFilePath);

            var dataProductModel = new DataProductModel();
            dataProductModel.setGatewayId(getGatewayId());
            dataProductModel.setOwnerName("ParserTask");
            dataProductModel.setProductName(parsingTaskOutput.getId());
            dataProductModel.setDataProductType(DataProductType.FILE);

            var replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setStorageResourceId(parsingTaskOutput.getStorageResourceId());
            replicaLocationModel.setReplicaName("Parsing task output " + parsingTaskOutput.getId());
            replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
            replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);

            var destinationURI = new URI(
                    "file",
                    gatewayStoragePreference.getLoginUserName(),
                    storageResource.getHostName(),
                    22,
                    remoteFilePath,
                    null,
                    null);

            replicaLocationModel.setFilePath(destinationURI.toString());
            dataProductModel.addToReplicaLocations(replicaLocationModel);

            var productUri = getRegistryServiceClient().registerDataProduct(dataProductModel);

            logger.info("Data product is {} for path {}", productUri, remoteFilePath);

            setContextVariable(parsingTaskOutput.getContextVariableName(), productUri);
        } catch (TaskOnFailException e) {
            throw e;
        } catch (RegistryServiceException e) {
            throw new TaskOnFailException("Failed to access registry service", false, e);
        } catch (AgentException e) {
            throw new TaskOnFailException("Agent error", false, e);
        } catch (Exception e) {
            String msg = String.format(
                    "Failed to upload from local path %s to remote path %s of storage resource %s",
                    localFilePath, remoteFilePath, parsingTaskOutput.getStorageResourceId());
            logger.error(msg, e);
            throw new TaskOnFailException(msg, false, e);
        }
    }

    private String createLocalInputDir(String containerName) throws TaskOnFailException {
        String localInpDir = (localDataDir.endsWith(File.separator) ? localDataDir : localDataDir + File.separator)
                + "parsers" + File.separator + containerName + File.separator + "data" + File.separator + "input"
                + File.separator;
        try {
            FileUtils.forceMkdir(new File(localInpDir));
            return localInpDir;

        } catch (IOException e) {
            throw new TaskOnFailException("Failed to build input directories " + localInpDir, true, e);
        }
    }

    private String createLocalOutputDir(String containerName) throws TaskOnFailException {
        String localOutDir = (localDataDir.endsWith(File.separator) ? localDataDir : localDataDir + File.separator)
                + "parsers" + File.separator + containerName + File.separator + "data" + File.separator + "output"
                + File.separator;
        try {
            FileUtils.forceMkdir(new File(localOutDir));
            return localOutDir;

        } catch (IOException e) {
            throw new TaskOnFailException("Failed to build output directories " + localOutDir, true, e);
        }
    }

    private RegistryService getRegistryServiceClient() throws TaskOnFailException {
        // Use injected service, fallback to ApplicationContext if not injected
        if (registryService == null) {
            throw new TaskOnFailException("RegistryService not available.", false, null);
        }
        return registryService;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public ParsingTaskInputs getParsingTaskInputs() {
        return parsingTaskInputs;
    }

    public void setParsingTaskInputs(ParsingTaskInputs parsingTaskInputs) {
        this.parsingTaskInputs = parsingTaskInputs;
    }

    public ParsingTaskOutputs getParsingTaskOutputs() {
        return parsingTaskOutputs;
    }

    public void setParsingTaskOutputs(ParsingTaskOutputs parsingTaskOutputs) {
        this.parsingTaskOutputs = parsingTaskOutputs;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getLocalDataDir() {
        return localDataDir;
    }

    public void setLocalDataDir(String localDataDir) {
        this.localDataDir = localDataDir;
    }
}
