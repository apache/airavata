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
package org.apache.airavata.agent.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.agent.UserContext;
import org.apache.airavata.agent.config.ClusterApplicationConfiguration;
import org.apache.airavata.agent.model.AgentLaunchRequest;
import org.apache.airavata.agent.model.AgentLaunchResponse;
import org.apache.airavata.agent.model.AgentTerminateResponse;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.compute.resource.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.GroupComputeResourcePreference;
import org.apache.airavata.compute.resource.model.GroupResourceProfile;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.project.model.Project;
import org.apache.airavata.research.experiment.service.ExperimentSearchService;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.core.exception.ValidationExceptions.ExceptionHandlerUtil;
import org.apache.airavata.core.model.EntitySearchField;
import org.apache.airavata.core.model.SearchCondition;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.SharingResourceType;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.research.experiment.model.UserConfigurationDataModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementService.class);
    private final ExperimentService experimentService;
    private final ExperimentSearchService experimentSearchService;
    private final ClusterApplicationConfiguration clusterApplicationConfig;
    private final ResourceProfileAdapter resourceProfileAdapter;
    private final SharingService sharingService;
    private final ApplicationAdapter applicationAdapter;

    @Value("${airavata.services.agent.storage.id}")
    private String storageResourceId;

    @Value("${airavata.services.agent.storage.path}")
    private String storagePath;

    // no longer configurable; keep local default (used for the "server_url" app input)
    private final String grpcHost = "localhost";

    public AgentManagementService(
            ExperimentService experimentService,
            ExperimentSearchService experimentSearchService,
            ClusterApplicationConfiguration clusterApplicationConfig,
            ResourceProfileAdapter resourceProfileAdapter,
            SharingService sharingService,
            ApplicationAdapter applicationAdapter) {
        this.experimentService = experimentService;
        this.experimentSearchService = experimentSearchService;
        this.clusterApplicationConfig = clusterApplicationConfig;
        this.resourceProfileAdapter = resourceProfileAdapter;
        this.sharingService = sharingService;
        this.applicationAdapter = applicationAdapter;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            var experiment = experimentService.getExperiment(UserContext.authzToken(), experimentId);
            experimentService.terminateExperiment(experiment.getExperimentId(), experiment.getGatewayId());
            return new AgentTerminateResponse(experimentId, true);
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            var experiment = experimentService.getExperiment(UserContext.authzToken(), experimentId);
            var groupResourceProfile = resourceProfileAdapter.getGroupResourceProfile(
                    experiment.getUserConfigurationData().getGroupResourceProfileId());

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                var groupResourceList = getGroupResourceList(UserContext.authzToken(), experiment.getGatewayId());

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
     */
    public AgentLaunchRequest filterOptimumLaunchRequest(List<AgentLaunchRequest> launchRequests) throws Exception {
        int leastRunningExpCount = Integer.MAX_VALUE;
        var sortedLaunchRequest = launchRequests.get(0);

        for (var req : launchRequests) {
            var appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
            var experimentStatistics = experimentSearchService.getExperimentStatistics(
                    UserContext.gatewayId(),
                    IdGenerator.getUniqueTimestamp().getTime() - 60 * 60 * 1000,
                    IdGenerator.getUniqueTimestamp().getTime(),
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

            var experimentId = experimentService.createExperiment(experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            experimentService.launchExperiment(
                    UserContext.authzToken(), experiment.getGatewayId(), experimentId);
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
            experimentService.terminateExperiment(experimentId, gatewayId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            LOGGER.info("Extracting the process model for experiment id: {}", expId);
            var expModel = experimentService.getExperiment(expId);
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

        var applicationInputs = applicationAdapter.getApplicationInputs(appInterfaceId);
        var experimentInputs = applicationInputs.stream()
                .map(appInput -> {
                    var ei = new org.apache.airavata.research.experiment.model.ExperimentInput();
                    ei.setName(appInput.getName());
                    ei.setType(appInput.getType() != null ? appInput.getType().name() : "STRING");
                    ei.setValue(appInput.getValue());
                    ei.setRequired(appInput.getIsRequired());
                    ei.setAddToCommandLine(appInput.getRequiredToAddedToCommandLine());
                    ei.setOrderIndex(appInput.getInputOrder());
                    ei.setDescription(appInput.getApplicationArgument());
                    if (appInput.getName() != null) {
                        switch (appInput.getName()) {
                            case "agent_id" -> ei.setValue(agentId);
                            case "env_name" -> ei.setValue(envName);
                            case "server_url" -> ei.setValue(grpcHost);
                            case "libraries" ->
                                ei.setValue(req.getLibraries() != null ? String.join(",", req.getLibraries()) : "");
                            case "pip" -> ei.setValue(req.getPip() != null ? String.join(",", req.getPip()) : "");
                            case "mounts" ->
                                ei.setValue(req.getMounts() != null ? String.join(",", req.getMounts()) : "");
                            default -> {}
                        }
                    }
                    return ei;
                })
                .collect(Collectors.toList());

        experimentModel.setInputs(experimentInputs);
        var applicationOutputs = applicationAdapter.getApplicationOutputs(appInterfaceId);
        experimentModel.setOutputs(applicationOutputs.stream()
                .map(appOutput -> {
                    var eo = new org.apache.airavata.research.experiment.model.ExperimentOutput();
                    eo.setName(appOutput.getName());
                    eo.setType(appOutput.getType() != null ? appOutput.getType().name() : "STRING");
                    eo.setValue(appOutput.getValue());
                    eo.setRequired(appOutput.getIsRequired());
                    eo.setDescription(appOutput.getApplicationArgument());
                    return eo;
                })
                .collect(Collectors.toList()));
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ComputeResourceType.SLURM) {
            return pref.getAllocationProjectNumber();
        }
        return null;
    }

    private String getProjectId(String projectName) {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects;
            try {
                userProjects = experimentService.getUserProjects(
                        UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
            } catch (Exception e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            var defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getProjectName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectId();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    private List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws AiravataSystemException {
        try {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var accessibleGroupResProfileIds = new java.util.ArrayList<String>();
            var filters = new java.util.ArrayList<SearchCriteria>();
            var searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + SharingResourceType.GROUP_RESOURCE_PROFILE.name());
            filters.add(searchCriteria);
            sharingService
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            filters,
                            0,
                            -1)
                    .forEach(p -> accessibleGroupResProfileIds.add(p.getEntityId()));
            return resourceProfileAdapter.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while getting group resource list: " + e.getMessage();
            LOGGER.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        } catch (Exception e) {
            String msg = "Error while retrieving group resource list: " + e.getMessage();
            LOGGER.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    private GroupComputeResourcePreference extractGroupComputeResourcePreference(String group, String remoteCluster) {
        List<GroupResourceProfile> groupResourceList;
        try {
            groupResourceList = getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());
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
