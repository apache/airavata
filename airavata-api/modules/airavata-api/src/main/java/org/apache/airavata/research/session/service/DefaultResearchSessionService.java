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
package org.apache.airavata.research.session.service;

import jakarta.persistence.EntityNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.repository.ResearchProjectRepository;
import org.apache.airavata.research.session.entity.SessionEntity;
import org.apache.airavata.research.session.mapper.SessionMapper;
import org.apache.airavata.research.session.model.Session;
import org.apache.airavata.research.session.model.SessionStatus;
import org.apache.airavata.research.session.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class DefaultResearchSessionService implements ResearchSessionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResearchSessionService.class);
    private static final String SPAWN_URL = "%s/hub/spawn/%s/%s?git=%s";
    private static final String SESSION_URL = "%s/hub/spawn/%s/%s";
    private static final String SERVERS_API_URL = "%s/hub/api/users/%s/servers/%s";

    private final SessionRepository sessionRepository;
    private final ResearchProjectRepository projectRepository;
    private final SessionMapper mapper;
    private final String hubUrl;
    private final String adminApiKey;
    private final int maxSessions;

    public DefaultResearchSessionService(
            SessionRepository sessionRepository,
            @Qualifier("researchProjectRepository") ResearchProjectRepository projectRepository,
            SessionMapper mapper,
            @Value("${airavata.services.research.hub.url}") String hubUrl,
            @Value("${airavata.services.research.hub.adminApiKey}") String adminApiKey,
            @Value("${airavata.services.research.hub.limit}") int maxSessions) {
        this.sessionRepository = sessionRepository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
        this.hubUrl = hubUrl;
        this.adminApiKey = adminApiKey;
        this.maxSessions = maxSessions;
    }

    // --- Session CRUD ---

    @Override
    public Session findSession(String sessionId) {
        var entity = findSessionEntity(sessionId);
        return mapper.toModel(entity);
    }

    @Override
    public Session createSession(String sessionName, String projectId) {
        var projectEntity = findProjectEntityById(projectId);
        sessionName = StringUtils.isNotBlank(sessionName)
                ? sessionName
                : UUID.randomUUID().toString().substring(0, 6);
        var session = new SessionEntity(sessionName, UserContext.userId(), projectEntity);
        session.setStatus(SessionStatus.CREATED);
        session = sessionRepository.save(session);
        logger.debug("Created session with Id: {}, Name: {}", session.getId(), sessionName);
        return mapper.toModel(session);
    }

    @Override
    public List<Session> findAllByUserId(String userId) {
        return mapper.toModelList(sessionRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public List<Session> findAllByUserIdAndStatus(String userId, SessionStatus status) {
        return mapper.toModelList(sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status));
    }

    @Override
    public Session updateSessionStatus(String sessionId, SessionStatus status) {
        var session = findSessionEntity(sessionId);

        var userId = UserContext.userId();
        if (!session.getUserId().equals(userId)) {
            logger.error("User {} is not authorized to update session {}", userId, session.getId());
            throw new IllegalArgumentException(
                    "User " + userId + " is not authorized to update session " + session.getId());
        }

        if (status == SessionStatus.TERMINATED) {
            try {
                stopRemoteSession(sessionId);
            } catch (Exception e) {
                logger.error("Unable to stop session {} for user {}", sessionId, userId, e);
            }
        }

        session.setStatus(status);
        session = sessionRepository.save(session);
        logger.debug("Updated session with Id: {}, Status: {}", session.getId(), status);
        return mapper.toModel(session);
    }

    @Override
    public int countSessionsByUserIdAndStatus(String userId, SessionStatus status) {
        return sessionRepository.countSessionsByUserIdAndStatus(userId, status);
    }

    @Override
    public boolean deleteSession(String sessionId) {
        var session = findSessionEntity(sessionId);
        if (!session.getUserId().equals(UserContext.userId())) {
            logger.error("Invalid session id {} for user {}", sessionId, session.getUserId());
            throw new IllegalArgumentException("Invalid session ID");
        }

        try {
            deleteRemoteSession(sessionId);
        } catch (Exception e) {
            logger.error("Unable to delete remote session, user already deleted it", e);
        }

        sessionRepository.delete(session);
        return true;
    }

    // --- Session spawning ---

    @Override
    public String spawnSession(String projectId, String sessionName) {
        var userId = UserContext.userId();
        var alreadyCreated = countSessionsByUserIdAndStatus(userId, SessionStatus.CREATED);
        if (alreadyCreated >= maxSessions) {
            throw new IllegalStateException("Max number of active sessions (" + maxSessions + ") has been reached. "
                    + "Please terminate or delete a session to continue.");
        }

        var project = findProjectEntityById(projectId);
        var datasetArtifacts = new ArrayList<DatasetArtifactEntity>(project.getDatasetArtifacts());

        var session = createSession(sessionName, projectId);

        var baseSpawnUrl = String.format(
                SPAWN_URL,
                hubUrl,
                userId,
                session.getId(),
                project.getRepositoryArtifact().getRepositoryUrl());

        var spawnUrlBuilder = new StringBuilder(baseSpawnUrl);
        for (DatasetArtifactEntity datasetArtifact : datasetArtifacts) {
            spawnUrlBuilder
                    .append("&dataPath=")
                    .append(URLEncoder.encode(datasetArtifact.getDatasetUrl(), StandardCharsets.UTF_8));
        }

        var spawnUrl = spawnUrlBuilder.toString();
        logger.debug("Generated spawn url: {} for user: {} against project: {}", spawnUrl, userId, projectId);
        return spawnUrl;
    }

    @Override
    public String resumeSession(String sessionId) {
        logger.debug("Resolving session id {} for user: {}", sessionId, UserContext.userId());
        var session = findSessionEntity(sessionId);
        var sessionUrl = String.format(SESSION_URL, hubUrl, UserContext.userId(), session.getId());
        logger.debug("Generated session url: {} for user: {}", sessionUrl, UserContext.userId());
        return sessionUrl;
    }

    // --- Private helpers ---

    private SessionEntity findSessionEntity(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> {
            logger.error("Unable to find session with id: {}", sessionId);
            return new EntityNotFoundException("Unable to find session with id: " + sessionId);
        });
    }

    private ResearchProjectEntity findProjectEntityById(String projectId) {
        return projectRepository
                .findByIdAndState(projectId, ArtifactState.ACTIVE)
                .orElseThrow(() -> {
                    logger.error("Unable to find a Project with id: {}", projectId);
                    return new EntityNotFoundException("Unable to find a Project with id: " + projectId);
                });
    }

    private void stopRemoteSession(String sessionId) {
        var userId = UserContext.userId();
        var url = String.format(SERVERS_API_URL, hubUrl, userId, sessionId);

        var headers = new HttpHeaders();
        headers.set("Authorization", "token " + adminApiKey);
        var request = new HttpEntity<Void>(headers);

        var response = new RestTemplate().exchange(url, HttpMethod.DELETE, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully stopped session {} for user {}", sessionId, userId);
        } else {
            throw new IllegalStateException("Failed to stop session " + sessionId + " for user " + userId);
        }
    }

    private void deleteRemoteSession(String sessionId) {
        var userId = UserContext.userId();
        var url = String.format(SERVERS_API_URL, hubUrl, userId, sessionId);

        var headers = new HttpHeaders();
        headers.set("Authorization", "token " + adminApiKey);

        var body = new HashMap<String, Object>();
        body.put("remove", true);

        var request = new HttpEntity<>(body, headers);

        var response = new RestTemplate().exchange(url, HttpMethod.DELETE, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully deleted session {} for user {}", sessionId, userId);
        } else {
            throw new IllegalStateException("Failed to delete session " + sessionId + " for user " + userId);
        }
    }
}
