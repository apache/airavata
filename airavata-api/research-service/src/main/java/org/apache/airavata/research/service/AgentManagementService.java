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
package org.apache.airavata.research.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentStatistics;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.research.config.ClusterApplicationConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementService.class);
    private final AgentExperimentService agentExperimentService;
    private final ClusterApplicationConfig clusterApplicationConfig;

    // Default values matching the old AgentLaunchRequest POJO defaults
    private static final String DEFAULT_QUEUE = "shared";
    private static final int DEFAULT_WALL_TIME = 30;
    private static final int DEFAULT_CPU_COUNT = 2;
    private static final int DEFAULT_NODE_COUNT = 1;
    private static final int DEFAULT_MEMORY = 2048;

    @Value("${grpc.server.host:localhost}")
    private String grpcHost;

    @Value("${airavata.agent.storage-resource-id:}")
    private String storageResourceId;

    @Value("${airavata.agent.storage-path:}")
    private String storagePath;

    public AgentManagementService(
            AgentExperimentService agentExperimentService, ClusterApplicationConfig clusterApplicationConfig) {
        this.agentExperimentService = agentExperimentService;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            RequestContext ctx = agentExperimentService.requestContext();
            agentExperimentService.experimentService().terminateExperiment(ctx, experimentId);
            return AgentTerminateResponse.newBuilder()
                    .setExperimentId(experimentId)
                    .setTerminated(true)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            RequestContext ctx = agentExperimentService.requestContext();
            ExperimentModel experiment =
                    agentExperimentService.experimentService().getExperiment(ctx, experimentId);
            GroupResourceProfile groupResourceProfile = agentExperimentService
                    .groupResourceProfileService()
                    .getGroupResourceProfile(
                            ctx, experiment.getUserConfigurationData().getGroupResourceProfileId());

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                List<GroupResourceProfile> groupResourceList = agentExperimentService
                        .groupResourceProfileService()
                        .getGroupResourceList(ctx, experiment.getGatewayId());

                final ExperimentModel exp = experiment;
                java.util.Optional<GroupResourceProfile> defaultProfile = groupResourceList.stream()
                        .filter(profile -> "Default".equalsIgnoreCase(profile.getGroupResourceProfileName()))
                        .findFirst();

                if (defaultProfile.isPresent()) {
                    UserConfigurationDataModel updatedConfig = experiment.getUserConfigurationData().toBuilder()
                            .setGroupResourceProfileId(defaultProfile.get().getGroupResourceProfileId())
                            .build();
                    experiment = experiment.toBuilder()
                            .setUserConfigurationData(updatedConfig)
                            .build();
                }
            }

            return experiment;

        } catch (ServiceException e) {
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
            RequestContext ctx = agentExperimentService.requestContext();
            String appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
            ExperimentStatistics experimentStatistics = agentExperimentService
                    .experimentService()
                    .getExperimentStatistics(
                            ctx,
                            ctx.getGatewayId(),
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

    /**
     * Derives the application interface name from the remote cluster and group,
     * matching the old AgentLaunchRequest.getApplicationInterfaceName() behavior.
     */
    private static String getApplicationInterfaceName(AgentLaunchRequest req) {
        return req.getRemoteCluster() + (StringUtils.isNotBlank(req.getGroup()) ? ("_" + req.getGroup()) : "");
    }

    public AgentLaunchResponse createAndLaunchExperiment(AgentLaunchRequest req) {
        try {
            String agentId = "agent_" + UUID.randomUUID().toString();
            String envName = generateEnvName(req.getLibrariesList(), req.getPipList());
            LOGGER.info("Creating an Airavata Experiment for {} with agent id {}", req.getExperimentName(), agentId);
            ExperimentModel experiment = generateExperiment(req, agentId, envName);

            RequestContext ctx = agentExperimentService.requestContext();
            String experimentId = agentExperimentService.experimentService().createExperiment(ctx, experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            agentExperimentService.experimentService().launchExperiment(ctx, experimentId, experiment.getGatewayId());
            return AgentLaunchResponse.newBuilder()
                    .setAgentId(agentId)
                    .setExperimentId(experimentId)
                    .setEnvName(envName)
                    .build();
        } catch (ServiceException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", req.getExperimentName(), e);
            throw new RuntimeException(
                    "Error while creating the experiment with the name: " + req.getExperimentName(), e);
        }
    }

    public void terminateApplication(String gatewayId, String experimentId) {
        try {
            LOGGER.info("Terminating the application with experiment Id: {}", experimentId);
            RequestContext ctx = agentExperimentService.requestContext();
            agentExperimentService.experimentService().terminateExperiment(ctx, experimentId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            LOGGER.info("Extracting the process model for experiment id: {}", expId);
            RequestContext ctx = agentExperimentService.requestContext();
            ExperimentModel expModel =
                    agentExperimentService.experimentService().getDetailedExperimentTree(ctx, expId);
            if (expModel.getProcessesList() != null
                    && !expModel.getProcessesList().isEmpty()) {
                return expModel.getProcessesList().get(0);
            } else {
                LOGGER.error("No process found for experiment id: {}", expId);
                return null;
            }
        } catch (ServiceException e) {
            LOGGER.error("Error while extracting the process model for experiment id: {}", expId, e);
            throw new RuntimeException(e);
        }
    }

    private ExperimentModel generateExperiment(AgentLaunchRequest req, String agentId, String envName)
            throws ServiceException {
        RequestContext ctx = agentExperimentService.requestContext();

        String experimentName = req.getExperimentName();
        String projectName = StringUtils.isNotBlank(req.getProjectName()) ? req.getProjectName() : "Default Project";
        String projectDir = projectName.replace(" ", "_");
        String projectId = agentExperimentService.getProjectId(projectName);
        String userName = UserContext.userId();
        String gatewayId = UserContext.gatewayId();
        String appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();

        GroupComputeResourcePreference groupCompResourcePref =
                agentExperimentService.extractGroupComputeResourcePreference(req.getGroup(), req.getRemoteCluster());

        // Apply defaults for proto fields that default to 0/"" when not set
        String queue = StringUtils.isNotBlank(req.getQueue()) ? req.getQueue() : DEFAULT_QUEUE;
        int wallTime = req.getWallTime() != 0 ? req.getWallTime() : DEFAULT_WALL_TIME;
        int cpuCount = req.getCpuCount() != 0 ? req.getCpuCount() : DEFAULT_CPU_COUNT;
        int nodeCount = req.getNodeCount() != 0 ? req.getNodeCount() : DEFAULT_NODE_COUNT;
        int memory = req.getMemory() != 0 ? req.getMemory() : DEFAULT_MEMORY;

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                ComputationalResourceSchedulingModel.newBuilder()
                        .setQueueName(queue)
                        .setNodeCount(nodeCount)
                        .setTotalCpuCount(cpuCount)
                        .setWallTimeLimit(wallTime)
                        .setTotalPhysicalMemory(memory)
                        .setResourceHostId(groupCompResourcePref.getComputeResourceId())
                        .setOverrideScratchLocation(groupCompResourcePref.getScratchLocation())
                        .setOverrideAllocationProjectNumber(extractSlurmAllocationProject(groupCompResourcePref))
                        .setOverrideLoginUserName(groupCompResourcePref.getLoginUserName())
                        .build();

        String experimentDataDir = Paths.get(storagePath, gatewayId, userName, projectDir, experimentName)
                .toString();

        UserConfigurationDataModel userConfigurationDataModel = UserConfigurationDataModel.newBuilder()
                .setComputationalResourceScheduling(computationalResourceSchedulingModel)
                .setAiravataAutoSchedule(false)
                .setOverrideManualScheduledParams(false)
                .setInputStorageResourceId(
                        StringUtils.isNotBlank(req.getInputStorageId()) ? req.getInputStorageId() : storageResourceId)
                .setOutputStorageResourceId(
                        StringUtils.isNotBlank(req.getOutputStorageId()) ? req.getInputStorageId() : storageResourceId)
                .setExperimentDataDir(experimentDataDir)
                .setGroupResourceProfileId(groupCompResourcePref.getGroupResourceProfileId())
                .build();

        List<InputDataObjectType> applicationInputs =
                agentExperimentService.applicationCatalogService().getApplicationInputs(ctx, appInterfaceId);
        List<InputDataObjectType> experimentInputs = applicationInputs.stream()
                .map(input -> {
                    if (input != null && !input.getName().isEmpty()) {
                        return switch (input.getName()) {
                            case "agent_id" ->
                                input.toBuilder().setValue(agentId).build();
                            case "env_name" ->
                                input.toBuilder().setValue(envName).build();
                            case "server_url" ->
                                input.toBuilder().setValue(grpcHost).build();
                            case "libraries" ->
                                input.toBuilder()
                                        .setValue(
                                                !req.getLibrariesList().isEmpty()
                                                        ? String.join(",", req.getLibrariesList())
                                                        : "")
                                        .build();
                            case "pip" ->
                                input.toBuilder()
                                        .setValue(!req.getPipList().isEmpty() ? String.join(",", req.getPipList()) : "")
                                        .build();
                            case "mounts" ->
                                input.toBuilder()
                                        .setValue(
                                                !req.getMountsList().isEmpty()
                                                        ? String.join(",", req.getMountsList())
                                                        : "")
                                        .build();
                            default -> input;
                        };
                    }
                    return input;
                })
                .collect(Collectors.toList());

        List<OutputDataObjectType> applicationOutputs =
                agentExperimentService.applicationCatalogService().getApplicationOutputs(ctx, appInterfaceId);

        ExperimentModel experimentModel = ExperimentModel.newBuilder()
                .setExperimentName(experimentName)
                .setProjectId(projectId)
                .setUserName(userName)
                .setGatewayId(gatewayId)
                .setExecutionId(appInterfaceId)
                .setUserConfigurationData(userConfigurationDataModel)
                .addAllExperimentInputs(experimentInputs)
                .addAllExperimentOutputs(applicationOutputs)
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();

        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());
        return experimentModel;
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ResourceType.SLURM && pref.hasSpecificPreferences()) {
            EnvironmentSpecificPreferences esp = pref.getSpecificPreferences();
            if (esp.hasSlurm()) {
                return esp.getSlurm().getAllocationProjectNumber();
            }
        }
        return "";
    }
}
