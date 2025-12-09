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
package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.research.service.enums.SessionStatusEnum;
import org.apache.airavata.research.service.handlers.SessionHandler;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rf/sessions")
@Tag(name = "Sessions", description = "All operations related to sessions (created from projects")
public class SessionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionController.class);

    private final SessionHandler sessionHandler;

    public SessionController(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    @GetMapping("/")
    @Operation(summary = "Get all sessions by session status and userId")
    public ResponseEntity<List<Session>> getSessions(
            @RequestParam(value = "status", required = false) SessionStatusEnum status) {
        LOGGER.info("Getting all sessions for user: {}, status filter: {}", UserContext.userId(), status);
        String userId = UserContext.userId();
        List<Session> sessions;
        if (status == null) {
            sessions = sessionHandler.findAllByUserId(userId);
        } else {
            sessions = sessionHandler.findAllByUserIdAndStatus(userId, status);
        }
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "Update a session's status")
    public ResponseEntity<Session> updateSessionStatus(
            @PathVariable(value = "sessionId") String sessionId,
            @RequestParam(value = "status") SessionStatusEnum status) {
        LOGGER.info("Updating session status for session: {} to {}", sessionId, status);
        return ResponseEntity.ok(sessionHandler.updateSessionStatus(sessionId, status));
    }

    @DeleteMapping("/delete/{sessionIds}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<Boolean> deleteSessions(@PathVariable(value = "sessionIds") List<String> sessionIds) {
        for (String id : sessionIds) {
            sessionHandler.updateSessionStatus(id, SessionStatusEnum.TERMINATED);
            sessionHandler.deleteSession(id);
        }
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<Boolean> deleteSession(@PathVariable(value = "sessionId") String sessionId) {
        LOGGER.info("Deleting session session: {}", sessionId);
        sessionHandler.updateSessionStatus(sessionId, SessionStatusEnum.TERMINATED);
        sessionHandler.deleteSession(sessionId);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
