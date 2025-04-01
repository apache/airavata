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
package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.service.enums.SessionStatusEnum;
import org.apache.airavata.research.service.handlers.ResearchHubHandler;
import org.apache.airavata.research.service.handlers.SessionHandler;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rf/hub")
@Tag(name = "Research Hub", description = "Research Hub Operations")
public class ResearchHubController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHubController.class);

    private final ResearchHubHandler rHubHandler;

    @Autowired
    private SessionHandler sessionHandler;

    public ResearchHubController(ResearchHubHandler rHubHandler) {
        this.rHubHandler = rHubHandler;
    }

    @GetMapping("/projects")
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(rHubHandler.getAllProjects());
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Spawn new project session")
    public ResponseEntity<?> resolveRHubUrl(@PathVariable("projectId") String projectId, @RequestParam("sessionName") String sessionName) {
        String spawnUrl = rHubHandler.spinRHubSession(projectId, sessionName);

        LOGGER.info("Redirecting user to spawn URL: {}", spawnUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(spawnUrl)).build();
    }

    @GetMapping("/project/{projectId}/exists")
    @Operation(summary = "Check if session with project already exists")
    public ResponseEntity<Boolean> getProjectSessionExists(@PathVariable("projectId") String projectId) {

        return ResponseEntity.ok(sessionHandler.checkIfSessionExists(projectId, UserContext.user().getId()));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get all sessions for a given user")
    public ResponseEntity<List<Session>> getSessions() {
        String userId = UserContext.user().getId();
        List<Session> sessions = sessionHandler.findAllByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/sessions/{sessionId}")
    @Operation(summary = "Update a session's status")
    public ResponseEntity<Session> updateSessionStatus(@PathVariable(value="sessionId") String sessionId, @Param(value="status") SessionStatusEnum status) {
        return ResponseEntity.ok(sessionHandler.updateSessionStatus(sessionId, status));
    }

    @GetMapping("/sessions/{sessionId}/resolve")
    public ResponseEntity<?> resolveRHubExistingSession(@PathVariable("sessionId") String sessionId) {
        String spawnUrl = rHubHandler.resolveRHubExistingSession(sessionId);

        LOGGER.info("Redirecting to existing session spawn URL: {}", spawnUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(spawnUrl)).build();
    }
}

