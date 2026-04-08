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
package org.apache.airavata.research.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.research.model.SessionEntity;
import org.apache.airavata.research.model.SessionStatusEnum;
import org.apache.airavata.research.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;

    private final ResearchHubService researchHubService;

    public SessionService(SessionRepository sessionRepository, @Lazy ResearchHubService researchHubService) {
        this.sessionRepository = sessionRepository;
        this.researchHubService = researchHubService;
    }

    public SessionEntity findSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> {
            LOGGER.error("Unable to find session with id: " + sessionId);
            return new EntityNotFoundException("Unable to find session with id: " + sessionId);
        });
    }

    public SessionEntity createSession(String sessionName, ResearchProjectEntity project) {
        sessionName = StringUtils.isNotBlank(sessionName)
                ? sessionName
                : UUID.randomUUID().toString().substring(0, 6);
        SessionEntity session = new SessionEntity(sessionName, UserContext.userId(), project);
        session.setStatus(SessionStatusEnum.CREATED);
        session = sessionRepository.save(session);
        LOGGER.debug("Created session with Id: {}, Name: {}", session.getId(), sessionName);
        return session;
    }

    public List<SessionEntity> findAllByUserId(String userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<SessionEntity> findAllByUserIdAndStatus(String userId, SessionStatusEnum status) {
        return sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public SessionEntity updateSessionStatus(String sessionId, SessionStatusEnum status) {
        SessionEntity session = findSession(sessionId);

        String userId = UserContext.userId();
        if (!session.getUserId().equals(userId)) {
            LOGGER.error("User " + userId + " is not authorized to update session " + session.getId());
            throw new RuntimeException("User " + userId + " is not authorized to update session " + session.getId());
        }

        if (status == SessionStatusEnum.TERMINATED) {
            try {
                researchHubService.stopSession(sessionId);
            } catch (Exception e) {
                LOGGER.error("Unable to stop session {} for user {}", sessionId, userId, e);
            }
        }

        session.setStatus(status);
        session = sessionRepository.save(session);
        LOGGER.debug("Updated session with Id: {}, Status: {}", session.getId(), status);
        return session;
    }

    public int countSessionsByUserIdAndStatus(String userId, SessionStatusEnum status) {
        return sessionRepository.countSessionsByUserIdAndStatus(userId, status);
    }

    public boolean deleteSession(String sessionId) {
        SessionEntity session = findSession(sessionId);
        if (!session.getUserId().equals(UserContext.userId())) {
            LOGGER.error("Invalid session id {} for user {}", sessionId, session.getUserId());
            throw new RuntimeException("Invalid session ID");
        }

        try {
            researchHubService.deleteSession(sessionId);
        } catch (Exception e) {
            LOGGER.error("Unable to delete session, user already deleted it", e);
        }

        sessionRepository.delete(session);
        return true;
    }
}
