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

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.agent.UserContext;
import org.apache.airavata.agent.config.ClusterApplicationConfiguration;
import org.apache.airavata.agent.model.AgentLaunchRequest;
import org.apache.airavata.agent.model.AgentLaunchResponse;
import org.apache.airavata.agent.model.AgentTerminateResponse;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.apache.airavata.compute.resource.model.ComputationalResourceScheduling;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.model.UserConfigurationData;
import org.apache.airavata.research.experiment.service.ExperimentSearchService;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.research.project.model.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AgentManagementService.class);
    private static final long ONE_HOUR_MS = 60 * 60 * 1000;
    private final ExperimentService experimentService;
    private final ExperimentSearchService experimentSearchService;
    private final ClusterApplicationConfiguration clusterApplicationConfig;
    private final ResourceProfileAdapter resourceProfileAdapter;
    private final ResourceService resourceService;
    private final ApplicationAdapter applicationAdapter;
    private final String storageResourceId;
    private final String storagePath;

    // no longer configurable; keep local default (used for the "server_url" app input)
    private final String grpcHost = "localhost";

    public AgentManagementService(
            ExperimentService experimentService,
            ExperimentSearchService experimentSearchService,
            ClusterApplicationConfiguration clusterApplicationConfig,
            ResourceProfileAdapter resourceProfileAdapter,
            ResourceService resourceService,
            ApplicationAdapter applicationAdapter,
            @Value("${airavata.services.agent.storage.id}") String storageResourceId,
            @Value("${airavata.services.agent.storage.path}") String storagePath) {
        this.experimentService = experimentService;
        this.experimentSearchService = experimentSearchService;
        this.clusterApplicationConfig = clusterApplicationConfig;
        this.resourceProfileAdapter = resourceProfileAdapter;
        this.resourceService = resourceService;
        this.applicationAdapter = applicationAdapter;
        this.storageResourceId = storageResourceId;
        this.storagePath = storagePath;
    }

    public AgentTerminateResponse terminateExperiment(String experimentId) {
        try {
            var experiment = experimentService.getExperiment(UserContext.authzToken(), experimentId);
            experimentService.terminateExperiment(experiment.getExperimentId(), experiment.getGatewayId());
            return new AgentTerminateResponse(experimentId, true);
        } catch (Exception e) {
            logger.error("Error terminating experiment {}", experimentId, e);
            throw new IllegalStateException("Error terminating experiment with the id: " + experimentId, e);
        }
    }

    public Experiment getExperiment(String experimentId) {
        try {
            var experiment = experimentService.getExperiment(UserContext.authzToken(), experimentId);
            return experiment;
        } catch (Exception e) {
            logger.error("Error while extracting the experiment with the id: {}", experimentId);
            throw new IllegalStateException("Error while extracting the experiment with the id: " + experimentId, e);
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
                    IdGenerator.getUniqueTimestamp().toEpochMilli() - ONE_HOUR_MS,
                    IdGenerator.getUniqueTimestamp().toEpochMilli(),
                    null,
                    appInterfaceId,
                    null,
                    null,
                    100,
                    0);

            int runningExperimentCount = experimentStatistics.getRunningExperimentCount();
            int failedExperimentCount = experimentStatistics.getFailedExperimentCount();
            logger.info(
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
            logger.info("Creating an Airavata Experiment for {} with agent id {}", req.getExperimentName(), agentId);
            var experiment = generateExperiment(req, agentId, envName);

            var experimentId = experimentService.createExperiment(experiment.getGatewayId(), experiment);
            logger.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            experimentService.launchExperiment(UserContext.authzToken(), experiment.getGatewayId(), experimentId);
            return new AgentLaunchResponse(agentId, experimentId, envName);
        } catch (Exception e) {
            logger.error("Error while creating the experiment with the name: {}", req.getExperimentName(), e);
            throw new IllegalStateException(
                    "Error while creating the experiment with the name: " + req.getExperimentName(), e);
        }
    }

    public void terminateApplication(String gatewayId, String experimentId) {
        try {
            logger.info("Terminating the application with experiment Id: {}", experimentId);
            experimentService.terminateExperiment(experimentId, gatewayId);
        } catch (Exception e) {
            logger.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new IllegalStateException(
                    "Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    public ProcessModel getEnvProcessModel(String expId) {
        try {
            logger.info("Extracting the process model for experiment id: {}", expId);
            var expModel = experimentService.getExperiment(expId);
            if (expModel.getProcesses() != null && !expModel.getProcesses().isEmpty()) {
                return expModel.getProcesses().get(0);
            } else {
                logger.error("No process found for experiment id: {}", expId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while extracting the process model for experiment id: {}", expId, e);
            throw new IllegalStateException("Error while extracting the process model for experiment id: " + expId, e);
        }
    }

    private Experiment generateExperiment(AgentLaunchRequest req, String agentId, String envName)
            throws AiravataSystemException {
        var experimentName = req.getExperimentName();
        var projectName = req.getProjectName() != null ? req.getProjectName() : "Default Project";
        var projectDir = projectName.replace(" ", "_");
        var projectId = getProjectId(projectName);
        var userName = UserContext.username();
        var gatewayId = UserContext.gatewayId();
        var appInterfaceId = clusterApplicationConfig.getApplicationInterfaceId();
        var experimentModel = new Experiment();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(projectId);
        experimentModel.setUserName(userName);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setApplicationId(appInterfaceId);

        var computationalResourceSchedulingModel = new ComputationalResourceScheduling();
        var binding = resolveBindingForCluster(req.getGroup(), req.getRemoteCluster(), gatewayId);
        computationalResourceSchedulingModel.setQueueName(req.getQueue());
        computationalResourceSchedulingModel.setNodeCount(req.getNodeCount());
        computationalResourceSchedulingModel.setTotalCPUCount(req.getCpuCount());
        computationalResourceSchedulingModel.setWallTimeLimit(req.getWallTime());
        computationalResourceSchedulingModel.setTotalPhysicalMemory(req.getMemory());
        computationalResourceSchedulingModel.setResourceHostId(binding.getResourceId());
        computationalResourceSchedulingModel.setOverrideScratchLocation(
                ResourceProfileAdapter.getMetadataString(binding.getMetadata(), "scratchLocation"));
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber(extractSlurmAllocationProject(binding));
        computationalResourceSchedulingModel.setOverrideLoginUserName(binding.getLoginUsername());

        var userConfigurationDataModel = new UserConfigurationData();
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
        // groupResourceProfileId now stores the resource binding ID
        userConfigurationDataModel.setGroupResourceProfileId(binding.getBindingId());

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        var applicationInputs = applicationAdapter.getApplicationInputs(appInterfaceId);
        var experimentInputs = applicationInputs.stream()
                .map(appInput -> {
                    var ei = new org.apache.airavata.research.experiment.model.ExperimentInput();
                    ei.setName(appInput.getName());
                    ei.setType(
                            appInput.getType() != null
                                    ? appInput.getType()
                                    : org.apache.airavata.storage.resource.model.DataType.STRING);
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
                    eo.setType(
                            appOutput.getType() != null
                                    ? appOutput.getType()
                                    : org.apache.airavata.storage.resource.model.DataType.STRING);
                    eo.setValue(appOutput.getValue());
                    eo.setRequired(appOutput.getIsRequired());
                    eo.setDescription(appOutput.getApplicationArgument());
                    return eo;
                })
                .collect(Collectors.toList()));
        logger.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    /**
     * Resolve the SLURM allocation project number from the binding metadata.
     *
     * <p>Returns the {@code allocationProjectNumber} metadata value only when the
     * resource's compute capability is SLURM. Returns {@code null} for all other
     * resource types or when the metadata key is absent.
     *
     * @param binding the resource binding whose metadata and resource capabilities to inspect
     * @return the allocation project number string, or {@code null}
     */
    private String extractSlurmAllocationProject(ResourceBindingEntity binding) {
        Resource resource = resourceService.getResource(binding.getResourceId());
        if (resource != null
                && resource.getCapabilities() != null
                && resource.getCapabilities().getCompute() != null) {
            ComputeResourceType resourceType =
                    resource.getCapabilities().getCompute().getComputeResourceType();
            if (resourceType == ComputeResourceType.SLURM) {
                return ResourceProfileAdapter.getMetadataString(binding.getMetadata(), "allocationProjectNumber");
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
                userProjects = experimentService.getUserProjects(
                        UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
            } catch (Exception e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                logger.error(msg, e);
                throw new IllegalStateException(msg, e);
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

        throw new EntityNotFoundException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    /**
     * Resolve the resource binding for the given cluster identifier within the current gateway.
     *
     * <p>GroupResourceProfile and GroupComputeResourcePreference have been removed from the model.
     * Resources and their bindings are now looked up directly from the resource and binding
     * repositories. The {@code group} parameter previously referred to a named group resource
     * profile; it is now used as an optional secondary filter on the resource name. The
     * {@code remoteCluster} is matched against the start of both the resource name and the
     * resource hostname to identify the target compute resource.
     *
     * <p>Lookup order:
     * <ol>
     *   <li>List all resources for the gateway.</li>
     *   <li>Filter resources whose name or hostname starts with {@code remoteCluster} (case-insensitive).
     *       If {@code group} is also provided, further narrow to resources whose name contains {@code group}.</li>
     *   <li>For each candidate resource, attempt to find a binding via
     *       {@link ResourceProfileAdapter#getBinding(String, String)}.</li>
     *   <li>Return the first binding found, or throw if none match.</li>
     * </ol>
     *
     * @param group         optional group/profile name hint used as a secondary resource name filter
     * @param remoteCluster prefix matched against resource name or hostname
     * @param gatewayId     the gateway to scope the lookup to
     * @return the resolved binding entity
     * @throws EntityNotFoundException if no matching binding can be found
     */
    private ResourceBindingEntity resolveBindingForCluster(String group, String remoteCluster, String gatewayId) {
        List<Resource> gatewayResources = resourceService.getResources(gatewayId);

        if (gatewayResources == null || gatewayResources.isEmpty()) {
            throw new EntityNotFoundException("No compute resources registered for gateway: " + gatewayId
                    + ". Cannot resolve binding for cluster: " + remoteCluster);
        }

        String clusterLower = remoteCluster != null ? remoteCluster.toLowerCase() : "";
        String groupLower = StringUtils.isNotBlank(group) ? group.toLowerCase() : null;

        for (Resource resource : gatewayResources) {
            String nameLower = resource.getName() != null ? resource.getName().toLowerCase() : "";
            String hostLower =
                    resource.getHostName() != null ? resource.getHostName().toLowerCase() : "";

            boolean matchesCluster = nameLower.startsWith(clusterLower) || hostLower.startsWith(clusterLower);
            if (!matchesCluster) {
                continue;
            }

            // When a group hint is given, apply it as an additional filter on resource name.
            if (groupLower != null && !nameLower.contains(groupLower)) {
                continue;
            }

            ResourceBindingEntity binding = resourceProfileAdapter.getBinding(resource.getResourceId(), gatewayId);
            if (binding != null) {
                logger.debug(
                        "Resolved binding id={} for cluster={}, group={}, gatewayId={}",
                        binding.getBindingId(),
                        remoteCluster,
                        group,
                        gatewayId);
                return binding;
            }
        }

        throw new EntityNotFoundException("Could not find a resource binding for cluster='" + remoteCluster
                + "', group='" + group + "', gatewayId='" + gatewayId
                + "' for user: " + UserContext.username());
    }
}
