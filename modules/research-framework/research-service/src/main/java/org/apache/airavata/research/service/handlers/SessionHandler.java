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

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.research.service.enums.SessionStatusEnum;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.Session;
import org.apache.airavata.research.service.model.repo.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);

    private final SessionRepository sessionRepository;

    private final ResearchHubHandler researchHubHandler;

    public SessionHandler(SessionRepository sessionRepository, @Lazy ResearchHubHandler researchHubHandler) {
        this.sessionRepository = sessionRepository;
        this.researchHubHandler = researchHubHandler;
    }

    public Session findSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> {
            LOGGER.error("Unable to find session with id: " + sessionId);
            return new EntityNotFoundException("Unable to find session with id: " + sessionId);
        });
    }

    public Session createSession(String sessionName, Project project) {
        sessionName = StringUtils.isNotBlank(sessionName) ? sessionName : UUID.randomUUID().toString().substring(0, 6);
        Session session = new Session(sessionName, UserContext.userId(), project);
        session.setStatus(SessionStatusEnum.CREATED);
        session = sessionRepository.save(session);
        LOGGER.debug("Created session with Id: {}, Name: {}", session.getId(), sessionName);
        return session;
    }

    public List<Session> findAllByUserId(String userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Session> findAllByUserIdAndStatus(String userId, SessionStatusEnum status) {
        return sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public Session updateSessionStatus(String sessionId, SessionStatusEnum status) {
        Session session = findSession(sessionId);

        String userId = UserContext.userId();
        if (!session.getUserId().equals(userId)) {
            LOGGER.error("User " + userId + " is not authorized to update session " + session.getId());
            throw new RuntimeException("User " + userId + " is not authorized to update session " + session.getId());
        }

        if (status == SessionStatusEnum.TERMINATED) {
            try {
                researchHubHandler.stopSession(sessionId);
            } catch(Exception e) {
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
        Session session = findSession(sessionId);
        if (!session.getUserId().equals(UserContext.userId())) {
            LOGGER.error("Invalid session id {} for user {}", sessionId, session.getUserId());
            throw new RuntimeException("Invalid session ID");
        }

        try {
            researchHubHandler.deleteSession(sessionId);
        } catch(Exception e) {
            LOGGER.error("Unable to delete session, user already deleted it", e);
        }

        sessionRepository.delete(session);
        return true;
    }
}
