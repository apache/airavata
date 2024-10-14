package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.config.ClusterApplicationConfig;
import org.apache.airavata.agent.connection.service.models.LaunchAgentRequest;
import org.apache.airavata.agent.connection.service.models.LaunchAgentResponse;
import org.apache.airavata.agent.connection.service.models.TerminateAgentResponse;
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentManagementHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(AgentManagementHandler.class);
    private final AiravataService airavataService;
    private final ClusterApplicationConfig clusterApplicationConfig;

    @Value("${airavata.storageResourceId}")
    private String storageResourceId;

    public AgentManagementHandler(AiravataService airavataService, ClusterApplicationConfig clusterApplicationConfig) {
        this.airavataService = airavataService;
        this.clusterApplicationConfig = clusterApplicationConfig;
    }

    public TerminateAgentResponse terminateExperiment(String experimentId) {
        try {
            Airavata.Client airavata = airavataService.airavata();
            ExperimentModel experiment = airavata.getExperiment(UserContext.authzToken(), experimentId);
            airavata.terminateExperiment(UserContext.authzToken(), experiment.getExperimentId(), experiment.getGatewayId());
            return new TerminateAgentResponse(experimentId, true);
        } catch (Exception e) {
            LOGGER.error("Error terminating experiment {}", experimentId, e);
            throw new RuntimeException("Error terminating experiment with the id: " + experimentId, e);
        }
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

    public LaunchAgentResponse createAndLaunchExperiment(LaunchAgentRequest req) {
        try {
            String agentId = "agent_" + UUID.randomUUID().toString();
            LOGGER.info("Creating an Airavata Experiment for {} with agent id {}", req.getExperimentName(), agentId);
            ExperimentModel experiment = generateExperiment(req, agentId);

            String experimentId = airavataService.airavata().createExperiment(UserContext.authzToken(), experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService.airavata().launchExperiment(UserContext.authzToken(), experimentId, experiment.getGatewayId());
            return new LaunchAgentResponse(agentId, experimentId);
        } catch (TException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", req.getExperimentName(), e);
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

    private ExperimentModel generateExperiment(LaunchAgentRequest req, String agentId) throws TException {
        Airavata.Client airavataClient = airavataService.airavata();

        String experimentName = req.getExperimentName();
        String projectId = airavataService.extractDefaultProjectId(airavataClient);
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
        computationalResourceSchedulingModel.setResourceHostId(airavataService.extractComputeResourceId(airavataClient, req.getRemoteCluster()));

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
                        input.setValue(agentId);

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

}
