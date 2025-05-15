package org.apache.airavata.agent.connection.service.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import io.micrometer.common.util.StringUtils;

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

    public String getProjectId(Airavata.Client airavataClient, String projectName) throws TException {
      int limit = 10;
      int offset = 0;

      while (true) {
          List<Project> userProjects = airavataClient.getUserProjects(UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
          Optional<Project> defaultProject = userProjects.stream()
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

      throw new RuntimeException("Could not find project: " + projectName + " for the user: " + UserContext.username());
  }

    public GroupComputeResourcePreference extractGroupComputeResourcePreference(Airavata.Client airavataClient, String group, String remoteCluster) throws TException {
        List<GroupResourceProfile> groupResourceList = airavataClient.getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());
        String groupProfileName = StringUtils.isNotBlank(group) ? group : "Default";

        return groupResourceList.stream()
                .filter(profile -> groupProfileName.equalsIgnoreCase(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences()
                        .stream()
                        .filter(preference -> preference.getComputeResourceId().startsWith(remoteCluster)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a matching Compute Resource Preference in the " + groupProfileName + " group resource profile for the user: " + UserContext.username()));
    }

    public List<String> getUserExperimentIDs(Airavata.Client airavataClient) throws TException {
        int limit = 100;
        Map<ExperimentSearchFields, String> filters = Map.of(ExperimentSearchFields.PROJECT_ID, getProjectId(airavataClient, "Default Project"));

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
