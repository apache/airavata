package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.tools.load.Configuration;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExperimentHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExperimentHandler.class);
    private final AiravataService airavataService;

    public ExperimentHandler(AiravataService airavataService) {
        this.airavataService = airavataService;
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

    public String createAndLaunchExperiment(Configuration config) {
        try {
            LOGGER.info("Creating an Airavata Experiment for {}", config.getExperimentBaseName());
            ExperimentModel experiment = generateExperiment(config);

            String experimentId = airavataService.airavata().createExperiment(UserContext.authzToken(), experiment.getGatewayId(), experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService.airavata().launchExperiment(UserContext.authzToken(), experimentId, experiment.getGatewayId());
            return experimentId;
        } catch (TException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", config.getExperimentBaseName());
            throw new RuntimeException("Error while creating the experiment with the name: " + config.getExperimentBaseName(), e);
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

    private ExperimentModel generateExperiment(Configuration config) throws TException {
        Airavata.Client airavataClient = airavataService.airavata();

        String experimentName = config.getExperimentBaseName();

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(config.getProjectId());
        experimentModel.setUserName(config.getUserId());
        experimentModel.setGatewayId(config.getGatewayId());
        experimentModel.setExecutionId(config.getApplicationInterfaceId());

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setQueueName(config.getQueue());
        computationalResourceSchedulingModel.setNodeCount(config.getNodeCount());
        computationalResourceSchedulingModel.setTotalCPUCount(config.getCpuCount());
        computationalResourceSchedulingModel.setWallTimeLimit(config.getWallTime());
        computationalResourceSchedulingModel.setTotalPhysicalMemory(config.getPhysicalMemory());
        computationalResourceSchedulingModel.setResourceHostId(config.getComputeResourceId());

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        userConfigurationDataModel.setAiravataAutoSchedule(false);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        userConfigurationDataModel.setStorageId(config.getStorageResourceId());
        userConfigurationDataModel.setExperimentDataDir(config.getUserId()
                .concat(File.separator)
                .concat(config.getProjectId())
                .concat(File.separator)
                .concat(experimentName));

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        List<InputDataObjectType> applicationInputs = airavataClient.getApplicationInputs(UserContext.authzToken(),
                config.getApplicationInterfaceId());
        List<InputDataObjectType> experimentInputs = new ArrayList<>();

        for (InputDataObjectType inputDataObjectType : applicationInputs) {

            Optional<Configuration.Input> input = config.getInputs().stream().filter(inp -> inp.getName().equals(inputDataObjectType.getName())).findFirst();
            input.ifPresent(value -> inputDataObjectType.setValue(value.getValue()));
            experimentInputs.add(inputDataObjectType);
        }

        experimentModel.setExperimentInputs(experimentInputs);
        experimentModel.setExperimentOutputs(airavataClient.getApplicationOutputs(UserContext.authzToken(), config.getApplicationInterfaceId()));
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        LOGGER.info("Generated the experiment: {}", experimentModel.getExperimentId());

        return experimentModel;
    }
}
