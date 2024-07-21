package org.apache.airavata.agent.connection.service.services;

import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);
    private static final int TIMEOUT = 100000;

    @Value("${airavata.server.url:scigap02.sciencegateways.iu.edu}")
    private String serverUrl;

    @Value("${airavata.server.port:9930}")
    private int port;

    @Value("${airavata.server.truststore.path}")
    private String trustStorePath;

    public Airavata.Client airavata() {
        try {
            LOGGER.debug("Creating Airavata client with the TrustStore URL - " + trustStorePath);
            return AiravataClientFactory.createAiravataSecureClient(serverUrl, port, trustStorePath, "airavata", TIMEOUT);

        } catch (AiravataClientException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }

    public String extractDefaultProjectId(Airavata.Client airavataClient) throws TException {
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

    public String extractComputeResourceId(Airavata.Client airavataClient, String remoteCluster) throws TException {
        List<GroupResourceProfile> groupResourceList = airavataClient.getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());

        return groupResourceList.stream()
                .filter(profile -> "Default".equals(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences().stream())
                .map(GroupComputeResourcePreference::getComputeResourceId)
                .filter(computeResourceId -> computeResourceId.startsWith(remoteCluster))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a Compute Resource in the Default group resource profile for the user: " + UserContext.username()));
    }

    public List<String> getUserExperimentIDs(Airavata.Client airavataClient) throws TException {
        int limit = 100;
        Map<ExperimentSearchFields, String> filters = Map.of(ExperimentSearchFields.PROJECT_ID, extractDefaultProjectId(airavataClient));

        return Stream.iterate(0, offset -> offset + limit)
                .map(offset -> {
                    try {
                        return airavataClient.searchExperiments(UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), filters, limit, offset);
                    } catch (TException e) {
                        // Handle exception gracefully
                        throw new RuntimeException(e);
                    }
                })
                .takeWhile(list -> !list.isEmpty())
                .flatMap(List::stream)
                .map(ExperimentSummaryModel::getExperimentId)
                .collect(Collectors.toList());
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
