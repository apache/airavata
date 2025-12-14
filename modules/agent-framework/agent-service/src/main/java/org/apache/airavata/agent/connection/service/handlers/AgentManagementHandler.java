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
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.api.model.Airavata;
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
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementHandler.class);
    private final AiravataService airavataService;
    private final ClusterApplicationConfig clusterApplicationConfig;

    @Value("${airavata.storageResourceId}")
    private String storageResourceId;

    @Value("${airavata.storagePath}")
    private String storagePath;

    @Value("${grpc.server.host}")
    private String grpcHost;

    public AgentManagementHandler(AiravataService airavataService, ClusterApplicationConfig clusterApplicationConfig) {
        this.airavataService = airavataService;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataService.airavata();
            ExperimentModel experiment = airavata.getExperiment(UserContext.authzToken(), experimentId);
            airavata.terminateExperiment(
                    UserContext.authzToken(), experiment.getExperimentId(), experiment.getGatewayId());
            return new AgentTerminateResponse(experimentId, true);
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataService.airavata();
            ExperimentModel experiment = airavata.getExperiment(UserContext.authzToken(), experimentId);
            GroupResourceProfile groupResourceProfile = airavata.getGroupResourceProfile(
                    UserContext.authzToken(),
                    experiment.getUserConfigurationData().getGroupResourceProfileId());

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                List<GroupResourceProfile> groupResourceList =
                        airavata.getGroupResourceList(UserContext.authzToken(), experiment.getGatewayId());

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
            ExperimentStatistics experimentStatistics = airavataService
                    .airavata()
                    .getExperimentStatistics(
                            UserContext.authzToken(),
                            UserContext.gatewayId(),
                            System.currentTimeMillis() - 60 * 60 * 1000,
                            System.currentTimeMillis(),
                            null,
                            appInterfaceId,
                            null,
                            100,
                            0);

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

            String experimentId = airavataService
                    .airavata()
                    .createExperiment(UserContext.authzToken(), experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService
                    .airavata()
                    .launchExperiment(UserContext.authzToken(), experimentId, experiment.getGatewayId());
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
            airavataService.airavata().terminateExperiment(UserContext.authzToken(), experimentId, gatewayId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            LOGGER.info("Extracting the process model for experiment id: {}", expId);
            ExperimentModel expModel =
                    airavataService.airavata().getDetailedExperimentTree(UserContext.authzToken(), expId);
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
        Airavata.Client airavataClient = airavataService.airavata();

        String experimentName = req.getExperimentName();
        String projectName = req.getProjectName() != null ? req.getProjectName() : "Default Project";
        String projectDir = projectName.replace(" ", "_");
        String projectId = airavataService.getProjectId(airavataClient, projectName);
        AuthzToken authzToken = UserContext.authzToken();
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
        GroupComputeResourcePreference groupCompResourcePref = airavataService.extractGroupComputeResourcePreference(
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

        List<InputDataObjectType> applicationInputs = airavataClient.getApplicationInputs(authzToken, appInterfaceId);
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
        experimentModel.setExperimentOutputs(airavataClient.getApplicationOutputs(authzToken, appInterfaceId));
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ComputeResourceType.SLURM && pref.isSetSpecificPreferences()) {
            EnvironmentSpecificPreferences esp = pref.getSpecificPreferences();
            if (esp.isSetSlurm()) {
                return esp.getSlurm().getAllocationProjectNumber();
            }
        }
        return null;
    }
}
