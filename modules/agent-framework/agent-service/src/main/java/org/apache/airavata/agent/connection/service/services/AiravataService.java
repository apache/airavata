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
package org.apache.airavata.agent.connection.service.services;

import io.micrometer.common.util.StringUtils;
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

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

    @Value("${airavata.server.url:airavata.host}")
    private String serverUrl;

    @Value("${airavata.server.port:8930}")
    private int port;

    @Value("${airavata.server.secure:false}")
    private boolean secure;

    public Airavata.Client airavata() {
        try {
            return AiravataClientFactory.createAiravataClient(serverUrl, port, secure);
        } catch (AiravataClientException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }

    public String getProjectId(Airavata.Client airavataClient, String projectName) throws TException {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects = airavataClient.getUserProjects(
                    UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
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

        throw new RuntimeException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    public GroupComputeResourcePreference extractGroupComputeResourcePreference(
            Airavata.Client airavataClient, String group, String remoteCluster) throws TException {
        List<GroupResourceProfile> groupResourceList =
                airavataClient.getGroupResourceList(UserContext.authzToken(), UserContext.gatewayId());
        String groupProfileName = StringUtils.isNotBlank(group) ? group : "Default";

        return groupResourceList.stream()
                .filter(profile -> groupProfileName.equalsIgnoreCase(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences().stream()
                        .filter(preference -> preference.getComputeResourceId().startsWith(remoteCluster)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a matching Compute Resource Preference in the "
                        + groupProfileName + " group resource profile for the user: " + UserContext.username()));
    }

    public List<String> getUserExperimentIDs(Airavata.Client airavataClient) throws TException {
        int limit = 100;
        Map<ExperimentSearchFields, String> filters =
                Map.of(ExperimentSearchFields.PROJECT_ID, getProjectId(airavataClient, "Default Project"));

        return Stream.iterate(0, offset -> offset + limit)
                .map(offset -> {
                    try {
                        return airavataClient.searchExperiments(
                                UserContext.authzToken(),
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset);
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
}
