package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.config.ClusterApplicationConfig;
import org.apache.airavata.agent.connection.service.models.LaunchAgentRequest;
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExperimentHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExperimentHandler.class);
    private final AiravataService airavataService;
    private final ClusterApplicationConfig clusterApplicationConfig;

    @Value("${airavata.storageResourceId}")
    private String storageResourceId;

    public ExperimentHandler(AiravataService airavataService, ClusterApplicationConfig clusterApplicationConfig) {
        this.airavataService = airavataService;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataService.airavata();
            ExperimentModel experiment = airavata.getExperiment(UserContext.authzToken(), experimentId);
            GroupResourceProfile groupResourceProfile = airavata.getGroupResourceProfile(UserContext.authzToken(), experiment.getUserConfigurationData().getGroupResourceProfileId());

            // Always get the Default allocation
            if (!"Default".equalsIgnoreCase(groupResourceProfile.getGroupResourceProfileName())) {
                List<GroupResourceProfile> groupResourceList = airavata.getGroupResourceList(UserContext.authzToken(), experiment.getGatewayId());

                groupResourceList.stream().filter(profile -> "Default".equalsIgnoreCase(profile.getGroupResourceProfileName())).findFirst().ifPresent(profile -> experiment.getUserConfigurationData().setGroupResourceProfileId(profile.getGroupResourceProfileId()));
            }

            return experiment;

        } catch (TException e) {
            LOGGER.error("Error while extracting the experiment with the id: {}", experimentId);
            throw new RuntimeException("Error while extracting the experiment with the id: " + experimentId, e);
        }
    }

    public String createAndLaunchExperiment(LaunchAgentRequest req) {
        try {
            LOGGER.info("Creating an Airavata Experiment for {}", req.getExperimentName());
            ExperimentModel experiment = generateExperiment(req);

            String experimentId = airavataService.airavata().createExperiment(UserContext.authzToken(), experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService.airavata().launchExperiment(UserContext.authzToken(), experimentId, experiment.getGatewayId());
            return experimentId;
        } catch (TException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", req.getExperimentName());
            throw new RuntimeException("Error while creating the experiment with the name: " + req.getExperimentName(), e);
        }
    }

    public void terminateApplication(String gatewayId, String experimentId) {
        try {
            LOGGER.info("Terminating the application with experiment Id: {}", experimentId);
            airavataService.airavata().terminateExperiment(UserContext.authzToken(), experimentId, gatewayId);
        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the experiment Id: {}", experimentId);
            throw new RuntimeException("Error while terminating the application with the experiment Id: " + experimentId, e);
        }
    }

    private ExperimentModel generateExperiment(LaunchAgentRequest req) throws TException {
        Airavata.Client airavataClient = airavataService.airavata();

        String experimentName = req.getExperimentName();
        String projectId = extractDefaultProjectId(airavataClient);
        String appInterfaceId = clusterApplicationConfig.getApplicationInterfaceIdByCluster(req.getRemoteCluster());

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(projectId);
        experimentModel.setUserName(UserContext.username());
        experimentModel.setGatewayId(UserContext.gatewayId());
        experimentModel.setExecutionId(appInterfaceId);

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setQueueName(req.getQueue());
        computationalResourceSchedulingModel.setNodeCount(req.getNodeCount());
        computationalResourceSchedulingModel.setTotalCPUCount(req.getCpuCount());
        computationalResourceSchedulingModel.setWallTimeLimit(req.getWallTime());
        computationalResourceSchedulingModel.setTotalPhysicalMemory(req.getMemory());
        computationalResourceSchedulingModel.setResourceHostId(extractComputeResourceId(airavataClient, req.getRemoteCluster()));

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        userConfigurationDataModel.setAiravataAutoSchedule(false);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        userConfigurationDataModel.setStorageId(storageResourceId);
        userConfigurationDataModel.setExperimentDataDir(UserContext.username()
                .concat(File.separator)
                .concat(projectId)
                .concat(File.separator)
                .concat(experimentName));

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        List<InputDataObjectType> applicationInputs = airavataClient.getApplicationInputs(UserContext.authzToken(), appInterfaceId);
        List<InputDataObjectType> experimentInputs = applicationInputs.stream()
                .peek(input -> {
                    if ("agent_id".equals(input.getName())) {
                        input.setValue("agent_" + UUID.randomUUID());

                    } else if ("server_url".equals(input.getName())) {
                        input.setValue(airavataService.getServerUrl());
                    }
                })
                .collect(Collectors.toList());

        experimentModel.setExperimentInputs(experimentInputs);
        experimentModel.setExperimentOutputs(airavataClient.getApplicationOutputs(UserContext.authzToken(), appInterfaceId));
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }

    private String extractDefaultProjectId(Airavata.Client airavataClient) throws TException {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects = airavataClient.getUserProjects(UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);

            // Check for the "Default Project"
            Optional<Project> defaultProject = userProjects.stream()
                    .filter(project -> "Default Project".equals(project.getName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectID();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException("Could not find a Default project for the user: " + UserContext.username());
    }

    private String extractComputeResourceId(Airavata.Client airavataClient, String remoteCluster) throws TException {
        List<GroupResourceProfile> groupResourceList = airavataClient.getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());

        return groupResourceList.stream()
                .filter(profile -> "Default".equals(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences().stream())
                .map(GroupComputeResourcePreference::getComputeResourceId)
                .filter(computeResourceId -> computeResourceId.startsWith(remoteCluster))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a Compute Resource in the Default group resource profile for the user: " + UserContext.username()));
    }

}
