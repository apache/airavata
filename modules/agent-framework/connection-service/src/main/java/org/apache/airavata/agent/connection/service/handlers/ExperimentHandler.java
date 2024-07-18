package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public String createAndLaunchExperiment(String gatewayId, ExperimentModel experiment) {
        try {
            LOGGER.info("Creating an Airavata Experiment for {}", experiment.getExperimentName());
            String experimentId = airavataService.airavata().createExperiment(UserContext.authzToken(), gatewayId, experiment);
            LOGGER.info("Launching the application, Id: {}, Name: {}", experimentId, experiment.getExperimentName());
            airavataService.airavata().launchExperiment(UserContext.authzToken(), experimentId, gatewayId);
            return experimentId;
        } catch (TException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", experiment.getExperimentName());
            throw new RuntimeException("Error while creating the experiment with the name: " + experiment.getExperimentName(), e);
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
}
