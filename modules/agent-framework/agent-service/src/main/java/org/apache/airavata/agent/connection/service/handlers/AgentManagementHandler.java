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
package org.apache.airavata.agent.connection.service.handlers;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.config.ClusterApplicationConfig;
import org.apache.airavata.agent.connection.service.models.AgentLaunchRequest;
import org.apache.airavata.agent.connection.service.models.AgentLaunchResponse;
import org.apache.airavata.agent.connection.service.models.AgentTerminateResponse;
import org.apache.airavata.agent.connection.service.services.AiravataThriftClient;
import org.apache.airavata.common.exception.AiravataClientException;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.exception.AuthorizationException;
import org.apache.airavata.common.exception.InvalidRequestException;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.EnvironmentSpecificPreferences;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentStatistics;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.thriftapi.service.Airavata;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementHandler.class);
    private final AiravataThriftClient airavataThriftClient;
    private final ClusterApplicationConfig clusterApplicationConfig;

    @Value("${services.agent.storage.id}")
    private String storageResourceId;

    @Value("${services.agent.storage.path}")
    private String storagePath;

    // no longer configurable; keep local default (used for the "server_url" app input)
    private final String grpcHost = "localhost";

    public AgentManagementHandler(AiravataThriftClient airavataThriftClient, ClusterApplicationConfig clusterApplicationConfig) {
        this.airavataThriftClient = airavataThriftClient;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    private org.apache.airavata.thriftapi.security.model.AuthzToken convertAuthzToken(
            org.apache.airavata.security.model.AuthzToken domainToken) {
        org.apache.airavata.thriftapi.security.model.AuthzToken thriftToken =
                new org.apache.airavata.thriftapi.security.model.AuthzToken();
        thriftToken.setAccessToken(domainToken.getAccessToken());
        thriftToken.setClaimsMap(domainToken.getClaimsMap());
        return thriftToken;
    }

    private ExperimentModel convertExperimentModel(org.apache.airavata.thriftapi.model.ExperimentModel thriftModel) {
        if (thriftModel == null) return null;
        ExperimentModel domainModel = new ExperimentModel();
        domainModel.setExperimentId(thriftModel.getExperimentId());
        domainModel.setExperimentName(thriftModel.getExperimentName());
        domainModel.setProjectId(thriftModel.getProjectId());
        domainModel.setUserName(thriftModel.getUserName());
        domainModel.setGatewayId(thriftModel.getGatewayId());
        domainModel.setExecutionId(thriftModel.getExecutionId());
        domainModel.setExperimentType(
                ExperimentType.valueOf(thriftModel.getExperimentType().name()));
        if (thriftModel.getUserConfigurationData() != null) {
            UserConfigurationDataModel userConfig = new UserConfigurationDataModel();
            userConfig.setGroupResourceProfileId(
                    thriftModel.getUserConfigurationData().getGroupResourceProfileId());
            userConfig.setExperimentDataDir(
                    thriftModel.getUserConfigurationData().getExperimentDataDir());
            userConfig.setInputStorageResourceId(
                    thriftModel.getUserConfigurationData().getInputStorageResourceId());
            userConfig.setOutputStorageResourceId(
                    thriftModel.getUserConfigurationData().getOutputStorageResourceId());
            if (thriftModel.getUserConfigurationData().getComputationalResourceScheduling() != null) {
                ComputationalResourceSchedulingModel scheduling = new ComputationalResourceSchedulingModel();
                scheduling.setQueueName(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getQueueName());
                scheduling.setNodeCount(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getNodeCount());
                scheduling.setTotalCPUCount(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getTotalCPUCount());
                scheduling.setWallTimeLimit(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getWallTimeLimit());
                scheduling.setTotalPhysicalMemory(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getTotalPhysicalMemory());
                scheduling.setResourceHostId(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId());
                scheduling.setOverrideScratchLocation(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideScratchLocation());
                scheduling.setOverrideAllocationProjectNumber(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideAllocationProjectNumber());
                scheduling.setOverrideLoginUserName(thriftModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideLoginUserName());
                userConfig.setComputationalResourceScheduling(scheduling);
            }
            domainModel.setUserConfigurationData(userConfig);
        }
        if (thriftModel.getExperimentInputs() != null) {
            domainModel.setExperimentInputs(thriftModel.getExperimentInputs().stream()
                    .map(this::convertInputDataObjectType)
                    .collect(Collectors.toList()));
        }
        if (thriftModel.getExperimentOutputs() != null) {
            domainModel.setExperimentOutputs(thriftModel.getExperimentOutputs().stream()
                    .map(this::convertOutputDataObjectType)
                    .collect(Collectors.toList()));
        }
        if (thriftModel.getProcesses() != null) {
            domainModel.setProcesses(thriftModel.getProcesses().stream()
                    .map(this::convertProcessModel)
                    .collect(Collectors.toList()));
        }
        return domainModel;
    }

    private org.apache.airavata.thriftapi.model.ExperimentModel convertExperimentModelToThrift(
            ExperimentModel domainModel) {
        if (domainModel == null) return null;
        org.apache.airavata.thriftapi.model.ExperimentModel thriftModel =
                new org.apache.airavata.thriftapi.model.ExperimentModel();
        thriftModel.setExperimentId(domainModel.getExperimentId());
        thriftModel.setExperimentName(domainModel.getExperimentName());
        thriftModel.setProjectId(domainModel.getProjectId());
        thriftModel.setUserName(domainModel.getUserName());
        thriftModel.setGatewayId(domainModel.getGatewayId());
        thriftModel.setExecutionId(domainModel.getExecutionId());
        if (domainModel.getExperimentType() != null) {
            thriftModel.setExperimentType(org.apache.airavata.thriftapi.model.ExperimentType.valueOf(
                    domainModel.getExperimentType().name()));
        }
        if (domainModel.getUserConfigurationData() != null) {
            org.apache.airavata.thriftapi.model.UserConfigurationDataModel userConfig =
                    new org.apache.airavata.thriftapi.model.UserConfigurationDataModel();
            userConfig.setGroupResourceProfileId(
                    domainModel.getUserConfigurationData().getGroupResourceProfileId());
            userConfig.setExperimentDataDir(
                    domainModel.getUserConfigurationData().getExperimentDataDir());
            userConfig.setInputStorageResourceId(
                    domainModel.getUserConfigurationData().getInputStorageResourceId());
            userConfig.setOutputStorageResourceId(
                    domainModel.getUserConfigurationData().getOutputStorageResourceId());
            if (domainModel.getUserConfigurationData().getComputationalResourceScheduling() != null) {
                org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel scheduling =
                        new org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel();
                scheduling.setQueueName(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getQueueName());
                scheduling.setNodeCount(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getNodeCount());
                scheduling.setTotalCPUCount(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getTotalCPUCount());
                scheduling.setWallTimeLimit(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getWallTimeLimit());
                scheduling.setTotalPhysicalMemory(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getTotalPhysicalMemory());
                scheduling.setResourceHostId(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId());
                scheduling.setOverrideScratchLocation(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideScratchLocation());
                scheduling.setOverrideAllocationProjectNumber(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideAllocationProjectNumber());
                scheduling.setOverrideLoginUserName(domainModel
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getOverrideLoginUserName());
                userConfig.setComputationalResourceScheduling(scheduling);
            }
            thriftModel.setUserConfigurationData(userConfig);
        }
        if (domainModel.getExperimentInputs() != null) {
            thriftModel.setExperimentInputs(domainModel.getExperimentInputs().stream()
                    .map(this::convertInputDataObjectTypeToThrift)
                    .collect(Collectors.toList()));
        }
        if (domainModel.getExperimentOutputs() != null) {
            thriftModel.setExperimentOutputs(domainModel.getExperimentOutputs().stream()
                    .map(this::convertOutputDataObjectTypeToThrift)
                    .collect(Collectors.toList()));
        }
        return thriftModel;
    }

    private GroupResourceProfile convertGroupResourceProfile(
            org.apache.airavata.thriftapi.model.GroupResourceProfile thriftModel) {
        if (thriftModel == null) return null;
        GroupResourceProfile domainModel = new GroupResourceProfile();
        domainModel.setGroupResourceProfileId(thriftModel.getGroupResourceProfileId());
        domainModel.setGroupResourceProfileName(thriftModel.getGroupResourceProfileName());
        if (thriftModel.getComputePreferences() != null) {
            domainModel.setComputePreferences(thriftModel.getComputePreferences().stream()
                    .map(this::convertGroupComputeResourcePreference)
                    .collect(Collectors.toList()));
        }
        return domainModel;
    }

    private ExperimentStatistics convertExperimentStatistics(
            org.apache.airavata.thriftapi.model.ExperimentStatistics thriftModel) {
        if (thriftModel == null) return null;
        ExperimentStatistics domainModel = new ExperimentStatistics();
        domainModel.setRunningExperimentCount(thriftModel.getRunningExperimentCount());
        domainModel.setFailedExperimentCount(thriftModel.getFailedExperimentCount());
        return domainModel;
    }

    private InputDataObjectType convertInputDataObjectType(
            org.apache.airavata.thriftapi.model.InputDataObjectType thriftModel) {
        if (thriftModel == null) return null;
        InputDataObjectType domainModel = new InputDataObjectType();
        domainModel.setName(thriftModel.getName());
        domainModel.setValue(thriftModel.getValue());
        if (thriftModel.getType() != null) {
            domainModel.setType(org.apache.airavata.common.model.DataType.valueOf(
                    thriftModel.getType().name()));
        }
        domainModel.setApplicationArgument(thriftModel.getApplicationArgument());
        if (thriftModel.isSetIsRequired()) {
            domainModel.setIsRequired(thriftModel.isIsRequired());
        }
        if (thriftModel.isSetRequiredToAddedToCommandLine()) {
            domainModel.setRequiredToAddedToCommandLine(thriftModel.isRequiredToAddedToCommandLine());
        }
        // Note: DataMovement, Location, and SearchQuery fields may not exist in domain model
        return domainModel;
    }

    private org.apache.airavata.thriftapi.model.InputDataObjectType convertInputDataObjectTypeToThrift(
            InputDataObjectType domainModel) {
        if (domainModel == null) return null;
        org.apache.airavata.thriftapi.model.InputDataObjectType thriftModel =
                new org.apache.airavata.thriftapi.model.InputDataObjectType();
        thriftModel.setName(domainModel.getName());
        thriftModel.setValue(domainModel.getValue());
        if (domainModel.getType() != null) {
            thriftModel.setType(org.apache.airavata.thriftapi.model.DataType.valueOf(
                    domainModel.getType().name()));
        }
        thriftModel.setApplicationArgument(domainModel.getApplicationArgument());
        // Note: isRequired and requiredToAddedToCommandLine may need to be set differently
        // These fields are typically boolean, so we'll set them if they exist
        thriftModel.setIsRequired(domainModel.getIsRequired());
        thriftModel.setRequiredToAddedToCommandLine(domainModel.getRequiredToAddedToCommandLine());
        // Note: DataMovement, Location, and SearchQuery fields may not exist in domain model
        return thriftModel;
    }

    private org.apache.airavata.common.model.OutputDataObjectType convertOutputDataObjectType(
            org.apache.airavata.thriftapi.model.OutputDataObjectType thriftModel) {
        if (thriftModel == null) return null;
        org.apache.airavata.common.model.OutputDataObjectType domainModel =
                new org.apache.airavata.common.model.OutputDataObjectType();
        domainModel.setName(thriftModel.getName());
        domainModel.setValue(thriftModel.getValue());
        if (thriftModel.getType() != null) {
            domainModel.setType(org.apache.airavata.common.model.DataType.valueOf(
                    thriftModel.getType().name()));
        }
        return domainModel;
    }

    private org.apache.airavata.thriftapi.model.OutputDataObjectType convertOutputDataObjectTypeToThrift(
            org.apache.airavata.common.model.OutputDataObjectType domainModel) {
        if (domainModel == null) return null;
        org.apache.airavata.thriftapi.model.OutputDataObjectType thriftModel =
                new org.apache.airavata.thriftapi.model.OutputDataObjectType();
        thriftModel.setName(domainModel.getName());
        thriftModel.setValue(domainModel.getValue());
        if (domainModel.getType() != null) {
            thriftModel.setType(org.apache.airavata.thriftapi.model.DataType.valueOf(
                    domainModel.getType().name()));
        }
        return thriftModel;
    }

    private ProcessModel convertProcessModel(org.apache.airavata.thriftapi.model.ProcessModel thriftModel) {
        if (thriftModel == null) return null;
        ProcessModel domainModel = new ProcessModel();
        domainModel.setProcessId(thriftModel.getProcessId());
        // Note: ProcessStatus may need different handling
        if (thriftModel.isSetProcessStatuses()) {
            var state = thriftModel.getProcessStatuses().get(0).getState();
            var processStatus = new ProcessStatus();
            processStatus.setState(ProcessState.valueOf(state.name()));
            domainModel.setProcessStatuses(List.of(processStatus));
        }
        return domainModel;
    }

    private GroupComputeResourcePreference convertGroupComputeResourcePreference(
            org.apache.airavata.thriftapi.model.GroupComputeResourcePreference thriftModel) {
        if (thriftModel == null) return null;
        GroupComputeResourcePreference domainModel = new GroupComputeResourcePreference();
        domainModel.setComputeResourceId(thriftModel.getComputeResourceId());
        domainModel.setGroupResourceProfileId(thriftModel.getGroupResourceProfileId());
        domainModel.setResourceType(
                ComputeResourceType.valueOf(thriftModel.getResourceType().name()));
        domainModel.setScratchLocation(thriftModel.getScratchLocation());
        domainModel.setLoginUserName(thriftModel.getLoginUserName());
        return domainModel;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataThriftClient.airavata();
            org.apache.airavata.thriftapi.model.ExperimentModel thriftExperiment =
                    airavata.getExperiment(convertAuthzToken(UserContext.authzToken()), experimentId);
            ExperimentModel experiment = convertExperimentModel(thriftExperiment);
            airavata.terminateExperiment(
                    convertAuthzToken(UserContext.authzToken()),
                    experiment.getExperimentId(),
                    experiment.getGatewayId());
            return new AgentTerminateResponse(experimentId, true);
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataThriftClient.airavata();
            org.apache.airavata.thriftapi.model.ExperimentModel thriftExperiment =
                    airavata.getExperiment(convertAuthzToken(UserContext.authzToken()), experimentId);
            ExperimentModel experiment = convertExperimentModel(thriftExperiment);
            org.apache.airavata.thriftapi.model.GroupResourceProfile thriftGroupResourceProfile =
                    airavata.getGroupResourceProfile(
                            convertAuthzToken(UserContext.authzToken()),
                            experiment.getUserConfigurationData().getGroupResourceProfileId());
            GroupResourceProfile groupResourceProfile = convertGroupResourceProfile(thriftGroupResourceProfile);

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                List<org.apache.airavata.thriftapi.model.GroupResourceProfile> thriftGroupResourceList =
                        airavata.getGroupResourceList(
                                convertAuthzToken(UserContext.authzToken()), experiment.getGatewayId());
                List<GroupResourceProfile> groupResourceList = thriftGroupResourceList.stream()
                        .map(this::convertGroupResourceProfile)
                        .collect(Collectors.toList());

                groupResourceList.stream()
                        .filter(profile -> "Default".equalsIgnoreCase(profile.getGroupResourceProfileName()))
                        .findFirst()
                        .ifPresent(profile -> experiment
                                .getUserConfigurationData()
                                .setGroupResourceProfileId(profile.getGroupResourceProfileId()));
            }

            return experiment;

        } catch (Exception e) {
            LOGGER.error("Error while extracting the experiment with the id: {}", experimentId);
            throw new RuntimeException("Error while extracting the experiment with the id: " + experimentId, e);
        }
    }

    /**
     * Meta-scheduling logic
     * @param launchRequests
     * @return
     * @throws Exception
     */
    public AgentLaunchRequest filterOptimumLaunchRequest(List<AgentLaunchRequest> launchRequests) throws Exception {
        int leastRunningExpCount = Integer.MAX_VALUE;
        AgentLaunchRequest sortedLaunchRequest = launchRequests.get(0);

        for (AgentLaunchRequest req : launchRequests) {
            String appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
            org.apache.airavata.thriftapi.model.ExperimentStatistics thriftExperimentStatistics = airavataThriftClient
                    .airavata()
                    .getExperimentStatistics(
                            convertAuthzToken(UserContext.authzToken()),
                            UserContext.gatewayId(),
                            System.currentTimeMillis() - 60 * 60 * 1000,
                            System.currentTimeMillis(),
                            null,
                            appInterfaceId,
                            null,
                            100,
                            0);
            ExperimentStatistics experimentStatistics = convertExperimentStatistics(thriftExperimentStatistics);

            int runningExperimentCount = experimentStatistics.getRunningExperimentCount();
            int failedExperimentCount = experimentStatistics.getFailedExperimentCount();
            LOGGER.info(
                    "Running count {} failed count {} for appInterfaceId {}",
                    runningExperimentCount,
                    failedExperimentCount,
                    appInterfaceId);
            if (runningExperimentCount + failedExperimentCount < leastRunningExpCount) {
                leastRunningExpCount = runningExperimentCount + failedExperimentCount;
                sortedLaunchRequest = req;
            }
        }
        return sortedLaunchRequest;
    }

    private String generateEnvName(List<String> libraries, List<String> pip) {
        String key = String.join(",", libraries) + "|" + String.join(",", pip);
        return Integer.toHexString(key.hashCode());
    }

    public AgentLaunchResponse createAndLaunchExperiment(AgentLaunchRequest req) {
        try {
            String agentId = "agent_" + UUID.randomUUID().toString();
            String envName = generateEnvName(req.getLibraries(), req.getPip());
            LOGGER.info("Creating an Airavata Experiment for {} with agent id {}", req.getExperimentName(), agentId);
            ExperimentModel experiment = generateExperiment(req, agentId, envName);

            String experimentId = airavataThriftClient
                    .airavata()
                    .createExperiment(
                            convertAuthzToken(UserContext.authzToken()),
                            experiment.getGatewayId(),
                            convertExperimentModelToThrift(experiment));
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataThriftClient
                    .airavata()
                    .launchExperiment(
                            convertAuthzToken(UserContext.authzToken()), experimentId, experiment.getGatewayId());
            return new AgentLaunchResponse(agentId, experimentId, envName);
        } catch (Exception e) {
            LOGGER.error("Error while creating the experiment with the name: {}", req.getExperimentName(), e);
            throw new RuntimeException(
                    "Error while creating the experiment with the name: " + req.getExperimentName(), e);
        }
    }

    public void terminateApplication(String gatewayId, String experimentId) {
        try {
            LOGGER.info("Terminating the application with experiment Id: {}", experimentId);
            airavataThriftClient
                    .airavata()
                    .terminateExperiment(convertAuthzToken(UserContext.authzToken()), experimentId, gatewayId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            LOGGER.info("Extracting the process model for experiment id: {}", expId);
            org.apache.airavata.thriftapi.model.ExperimentModel thriftExpModel = airavataThriftClient
                    .airavata()
                    .getDetailedExperimentTree(convertAuthzToken(UserContext.authzToken()), expId);
            ExperimentModel expModel = convertExperimentModel(thriftExpModel);
            if (expModel.getProcesses() != null && !expModel.getProcesses().isEmpty()) {
                return expModel.getProcesses().get(0);
            } else {
                LOGGER.error("No process found for experiment id: {}", expId);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while extracting the process model for experiment id: {}", expId, e);
            throw new RuntimeException(e);
        }
    }

    private ExperimentModel generateExperiment(AgentLaunchRequest req, String agentId, String envName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        Airavata.Client airavataClient = airavataThriftClient.airavata();

        String experimentName = req.getExperimentName();
        String projectName = req.getProjectName() != null ? req.getProjectName() : "Default Project";
        String projectDir = projectName.replace(" ", "_");
        String projectId = airavataThriftClient.getProjectId(airavataClient, projectName);
        org.apache.airavata.thriftapi.security.model.AuthzToken thriftAuthzToken =
                convertAuthzToken(UserContext.authzToken());
        String userName = UserContext.username();
        String gatewayId = UserContext.gatewayId();
        String appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(projectId);
        experimentModel.setUserName(userName);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExecutionId(appInterfaceId);

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                new ComputationalResourceSchedulingModel();
        GroupComputeResourcePreference groupCompResourcePref = airavataThriftClient.extractGroupComputeResourcePreference(
                airavataClient, req.getGroup(), req.getRemoteCluster());
        computationalResourceSchedulingModel.setQueueName(req.getQueue());
        computationalResourceSchedulingModel.setNodeCount(req.getNodeCount());
        computationalResourceSchedulingModel.setTotalCPUCount(req.getCpuCount());
        computationalResourceSchedulingModel.setWallTimeLimit(req.getWallTime());
        computationalResourceSchedulingModel.setTotalPhysicalMemory(req.getMemory());
        computationalResourceSchedulingModel.setResourceHostId(groupCompResourcePref.getComputeResourceId());
        // TODO - Support for both HPC & Cloud services --> Need to change the ComputationalResourceSchedulingModel
        computationalResourceSchedulingModel.setOverrideScratchLocation(groupCompResourcePref.getScratchLocation());
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber(
                extractSlurmAllocationProject(groupCompResourcePref));
        computationalResourceSchedulingModel.setOverrideLoginUserName(groupCompResourcePref.getLoginUserName());

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        userConfigurationDataModel.setAiravataAutoSchedule(false);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        userConfigurationDataModel.setInputStorageResourceId(
                StringUtils.isNotBlank(req.getInputStorageId()) ? req.getInputStorageId() : storageResourceId);
        userConfigurationDataModel.setOutputStorageResourceId(
                StringUtils.isNotBlank(req.getOutputStorageId()) ? req.getInputStorageId() : storageResourceId);
        String experimentDataDir = Paths.get(storagePath, gatewayId, userName, projectDir, experimentName)
                .toString();
        userConfigurationDataModel.setExperimentDataDir(experimentDataDir);
        userConfigurationDataModel.setGroupResourceProfileId(groupCompResourcePref.getGroupResourceProfileId());

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        List<org.apache.airavata.thriftapi.model.InputDataObjectType> thriftApplicationInputs =
                airavataClient.getApplicationInputs(thriftAuthzToken, appInterfaceId);
        List<InputDataObjectType> applicationInputs = thriftApplicationInputs.stream()
                .map(this::convertInputDataObjectType)
                .collect(Collectors.toList());
        List<InputDataObjectType> experimentInputs = applicationInputs.stream()
                .peek(input -> {
                    if (input != null && input.getName() != null) {
                        switch (input.getName()) {
                            case "agent_id" -> input.setValue(agentId);
                            case "env_name" -> input.setValue(envName);
                            case "server_url" -> input.setValue(grpcHost);
                            case "libraries" ->
                                input.setValue(req.getLibraries() != null ? String.join(",", req.getLibraries()) : "");
                            case "pip" -> input.setValue(req.getPip() != null ? String.join(",", req.getPip()) : "");
                            case "mounts" ->
                                input.setValue(req.getMounts() != null ? String.join(",", req.getMounts()) : "");
                            default -> {}
                        }
                    }
                })
                .collect(Collectors.toList());

        experimentModel.setExperimentInputs(experimentInputs);
        List<org.apache.airavata.thriftapi.model.OutputDataObjectType> thriftApplicationOutputs =
                airavataClient.getApplicationOutputs(thriftAuthzToken, appInterfaceId);
        List<org.apache.airavata.common.model.OutputDataObjectType> applicationOutputs =
                thriftApplicationOutputs.stream()
                        .map(this::convertOutputDataObjectType)
                        .collect(Collectors.toList());
        experimentModel.setExperimentOutputs(applicationOutputs);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ComputeResourceType.SLURM && pref.getSpecificPreferences() != null) {
            EnvironmentSpecificPreferences esp = pref.getSpecificPreferences();
            if (esp.isSlurm()) {
                return esp.getSlurm().getAllocationProjectNumber();
            }
        }
        return null;
    }
}
