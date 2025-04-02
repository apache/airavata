/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.handlers;

import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.Session;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResearchHubHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHubHandler.class);
    private static final String RH_SPAWN_URL = "%s/hub/spawn/%s/%s?git=%s&dataPath=%s";
    private static final String RH_SESSION_URL = "%s/hub/spawn/%s/%s";

    private final ProjectHandler projectHandler;
    private final SessionHandler sessionHandler;
    private final ProjectRepository projectRepository;

    @Value("${airavata.research-hub.url}")
    private String csHubUrl;

    public ResearchHubHandler(ProjectHandler projectHandler, SessionHandler sessionHandler, ProjectRepository projectRepository) {
        this.projectHandler = projectHandler;
        this.sessionHandler = sessionHandler;
        this.projectRepository = projectRepository;
    }

    public String spinRHubSession(String projectId, String sessionName) {
        Project project = projectHandler.findProject(projectId);
        // TODO should support multiple data sets for RHub
        DatasetResource dataset = project.getDatasetResources().stream().findFirst().get();
        Session session = sessionHandler.createSession(sessionName, project);

        String spawnUrl = String.format(RH_SPAWN_URL, csHubUrl, UserContext.userId(), session.getId(), project.getRepositoryResource().getRepositoryUrl(), dataset.getDatasetUrl());
        LOGGER.debug("Generated the spawn url: {} for the user: {} against the project: {}", spawnUrl, UserContext.userId(), projectId);
        return spawnUrl;
    }

    public String resolveRHubExistingSession(String sessionId) {
        LOGGER.debug("Resolving RH session id {} for user: {}", sessionId, UserContext.userId());
        // TODO restrict this execution for owner
        Session session = sessionHandler.findSession(sessionId);

        String sessionUrl = String.format(RH_SESSION_URL, csHubUrl, UserContext.userId(), session.getId());
        LOGGER.debug("Generated the session url: {} for the user: {}", sessionUrl, UserContext.userId());
        return sessionUrl;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}
