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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.research.session.entity.SessionEntity;
import org.apache.airavata.research.session.model.SessionStatus;
import org.apache.airavata.research.session.service.ResearchSessionService;
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
@RequestMapping("/api/v1/research-hub/sessions")
@Tag(name = "Sessions", description = "All operations related to sessions (created from projects")
public class ResearchSessionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchSessionController.class);

    private final ResearchSessionService sessionService;

    public ResearchSessionController(ResearchSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("")
    @Operation(summary = "Get all sessions by session status and userId")
    public ResponseEntity<List<SessionEntity>> getSessions(
            @RequestParam(value = "status", required = false) SessionStatus status) {
        LOGGER.info("Getting all sessions for user: {}, status filter: {}", UserContext.userId(), status);
        var userId = UserContext.userId();
        List<SessionEntity> sessions;
        if (status == null) {
            sessions = sessionService.findAllByUserId(userId);
        } else {
            sessions = sessionService.findAllByUserIdAndStatus(userId, status);
        }
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "Update a session's status")
    public ResponseEntity<SessionEntity> updateSessionStatus(
            @PathVariable(value = "sessionId") String sessionId, @RequestParam(value = "status") SessionStatus status) {
        LOGGER.info("Updating session status for session: {} to {}", sessionId, status);
        return ResponseEntity.ok(sessionService.updateSessionStatus(sessionId, status));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<Boolean> deleteSession(@PathVariable(value = "sessionId") String sessionId) {
        LOGGER.info("Deleting session: {}", sessionId);
        sessionService.updateSessionStatus(sessionId, SessionStatus.TERMINATED);
        sessionService.deleteSession(sessionId);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
