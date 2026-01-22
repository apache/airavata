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
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.service.AiravataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementHandler.class);
    private final AiravataService airavataService;
    private final ClusterApplicationConfig clusterApplicationConfig;

    @Value("${airavata.services.agent.storage.id}")
    private String storageResourceId;

    @Value("${airavata.services.agent.storage.path}")
    private String storagePath;

    // no longer configurable; keep local default (used for the "server_url" app input)
    private final String grpcHost = "localhost";

    public AgentManagementHandler(AiravataService airavataService, ClusterApplicationConfig clusterApplicationConfig) {
        this.airavataService = airavataService;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            var experiment = airavataService.getExperiment(UserContext.authzToken(), experimentId);
            airavataService.terminateExperiment(experiment.getExperimentId(), experiment.getGatewayId());
            return new AgentTerminateResponse(experimentId, true);
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            var experiment = airavataService.getExperiment(UserContext.authzToken(), experimentId);
            var groupResourceProfile = airavataService.getGroupResourceProfile(
                    UserContext.authzToken(),
                    experiment.getUserConfigurationData().getGroupResourceProfileId());

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                var groupResourceList =
                        airavataService.getGroupResourceList(UserContext.authzToken(), experiment.getGatewayId());

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
        var sortedLaunchRequest = launchRequests.get(0);

        for (var req : launchRequests) {
            var appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
            var experimentStatistics = airavataService.getExperimentStatistics(
                    UserContext.gatewayId(),
                    AiravataUtils.getUniqueTimestamp().getTime() - 60 * 60 * 1000,
                    AiravataUtils.getUniqueTimestamp().getTime(),
                    null,
                    appInterfaceId,
                    null,
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
        var key = String.join(",", libraries) + "|" + String.join(",", pip);
        return Integer.toHexString(key.hashCode());
    }

    public AgentLaunchResponse createAndLaunchExperiment(AgentLaunchRequest req) {
        try {
            var agentId = "agent_" + UUID.randomUUID().toString();
            var envName = generateEnvName(req.getLibraries(), req.getPip());
            LOGGER.info("Creating an Airavata Experiment for {} with agent id {}", req.getExperimentName(), agentId);
            var experiment = generateExperiment(req, agentId, envName);

            var experimentId = airavataService.createExperiment(experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService.launchExperiment(UserContext.authzToken(), experiment.getGatewayId(), experimentId);
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
            airavataService.terminateExperiment(experimentId, gatewayId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            LOGGER.info("Extracting the process model for experiment id: {}", expId);
            var expModel = airavataService.getDetailedExperimentTree(expId);
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
            throws AiravataSystemException {
        var experimentName = req.getExperimentName();
        var projectName = req.getProjectName() != null ? req.getProjectName() : "Default Project";
        var projectDir = projectName.replace(" ", "_");
        var projectId = getProjectId(projectName);
        var userName = UserContext.username();
        var gatewayId = UserContext.gatewayId();
        var appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
        var experimentModel = new ExperimentModel();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(projectId);
        experimentModel.setUserName(userName);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExecutionId(appInterfaceId);

        var computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        var groupCompResourcePref = extractGroupComputeResourcePreference(req.getGroup(), req.getRemoteCluster());
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

        var userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        userConfigurationDataModel.setAiravataAutoSchedule(false);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        userConfigurationDataModel.setInputStorageResourceId(
                StringUtils.isNotBlank(req.getInputStorageId()) ? req.getInputStorageId() : storageResourceId);
        userConfigurationDataModel.setOutputStorageResourceId(
                StringUtils.isNotBlank(req.getOutputStorageId()) ? req.getInputStorageId() : storageResourceId);
        var experimentDataDir = Paths.get(storagePath, gatewayId, userName, projectDir, experimentName)
                .toString();
        userConfigurationDataModel.setExperimentDataDir(experimentDataDir);
        userConfigurationDataModel.setGroupResourceProfileId(groupCompResourcePref.getGroupResourceProfileId());

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        var applicationInputs = airavataService.getApplicationInputs(appInterfaceId);
        var experimentInputs = applicationInputs.stream()
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
        var applicationOutputs = airavataService.getApplicationOutputs(appInterfaceId);
        experimentModel.setExperimentOutputs(applicationOutputs);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ComputeResourceType.SLURM && pref.getSpecificPreferences() != null) {
            var esp = pref.getSpecificPreferences();
            if (esp.isSlurm()) {
                return esp.getSlurm().getAllocationProjectNumber();
            }
        }
        return null;
    }

    private String getProjectId(String projectName) {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects;
            try {
                userProjects = airavataService.getUserProjects(
                        UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
            } catch (Exception e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            var defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectID();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    private GroupComputeResourcePreference extractGroupComputeResourcePreference(String group, String remoteCluster) {
        List<GroupResourceProfile> groupResourceList;
        try {
            groupResourceList = airavataService.getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());
        } catch (Exception e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        var groupProfileName = StringUtils.isNotBlank(group) ? group : "Default";

        return groupResourceList.stream()
                .filter(profile -> groupProfileName.equalsIgnoreCase(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences().stream()
                        .filter(preference -> preference.getComputeResourceId().startsWith(remoteCluster)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a matching Compute Resource Preference in the "
                        + groupProfileName + " group resource profile for the user: " + UserContext.username()));
    }
}
