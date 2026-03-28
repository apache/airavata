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
package org.apache.airavata.research.service.handlers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.security.UserContext;
import org.apache.airavata.research.service.config.ResearchProperties;
import org.apache.airavata.research.service.enums.SessionStatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.Session;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ResearchHubHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHubHandler.class);
    private static final String RH_SPAWN_URL = "%s/hub/spawn/%s/%s?git=%s";

    private static final String RH_SESSION_URL = "%s/hub/spawn/%s/%s";

    private static final String SERVERS_API_URL = "%s/hub/api/users/%s/servers/%s";

    private final ProjectHandler projectHandler;
    private final SessionHandler sessionHandler;
    private final ProjectRepository projectRepository;
    private final ResearchProperties researchProperties;

    public ResearchHubHandler(
            ProjectHandler projectHandler,
            SessionHandler sessionHandler,
            ProjectRepository projectRepository,
            ResearchProperties researchProperties) {
        this.projectHandler = projectHandler;
        this.sessionHandler = sessionHandler;
        this.projectRepository = projectRepository;
        this.researchProperties = researchProperties;
    }

    public boolean stopSession(String sessionId) {
        String userId = UserContext.userId();
        String url = String.format(SERVERS_API_URL, researchProperties.getHubUrl(), userId, sessionId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + researchProperties.getAdminApiKey());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.DELETE, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Successfully stopped/deleted RHub session {} for user {}", sessionId, userId);
            return true;
        } else {
            throw new RuntimeException("Failed to delete RHub session " + sessionId + " for user " + userId);
        }
    }

    public boolean deleteSession(String sessionId) {
        String userId = UserContext.userId();
        String url = String.format(SERVERS_API_URL, researchProperties.getHubUrl(), userId, sessionId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + researchProperties.getAdminApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("remove", true);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.DELETE, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Successfully stopped/deleted RHub session {} for user {}", sessionId, userId);
            return true;
        } else {
            throw new RuntimeException("Failed to delete RHub session " + sessionId + " for user " + userId);
        }
    }

    public String spinRHubSession(String projectId, String sessionName) {
        String userId = UserContext.userId();
        int alreadyCreated = sessionHandler.countSessionsByUserIdAndStatus(userId, SessionStatusEnum.CREATED);
        if (alreadyCreated >= researchProperties.getLimit()) {
            throw new RuntimeException(
                    "Max number of active sessions (10) has already been reached. Please terminate or delete a session to continue.");
        }

        Project project = projectHandler.findProject(projectId);
        ArrayList<DatasetResource> datasetResourceArrayList =
                new ArrayList<DatasetResource>(project.getDatasetResources());

        Session session = sessionHandler.createSession(sessionName, project);

        String baseSpawnUrl = String.format(
                RH_SPAWN_URL,
                researchProperties.getHubUrl(),
                UserContext.userId(),
                session.getId(),
                project.getRepositoryResource().getRepositoryUrl());

        StringBuilder spawnUrlBuilder = new StringBuilder(baseSpawnUrl);
        for (DatasetResource datasetResource : datasetResourceArrayList) {
            spawnUrlBuilder
                    .append("&dataPath=")
                    .append(URLEncoder.encode(datasetResource.getDatasetUrl(), StandardCharsets.UTF_8));
        }

        String spawnUrl = spawnUrlBuilder.toString();

        LOGGER.debug(
                "Generated the spawn url: {} for the user: {} against the project: {}",
                spawnUrl,
                UserContext.userId(),
                projectId);
        return spawnUrl;
    }

    public String resolveRHubExistingSession(String sessionId) {
        LOGGER.debug("Resolving RH session id {} for user: {}", sessionId, UserContext.userId());
        // TODO restrict this execution for owner
        Session session = sessionHandler.findSession(sessionId);

        String sessionUrl =
                String.format(RH_SESSION_URL, researchProperties.getHubUrl(), UserContext.userId(), session.getId());
        LOGGER.debug("Generated the session url: {} for the user: {}", sessionUrl, UserContext.userId());
        return sessionUrl;
    }
}
