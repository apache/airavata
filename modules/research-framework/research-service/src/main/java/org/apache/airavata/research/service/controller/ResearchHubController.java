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
import org.apache.airavata.research.service.ResponseTypes.RedirectResponse;
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

    public ResearchHubController(ResearchHubHandler rHubHandler) {
        this.rHubHandler = rHubHandler;
    }

    @GetMapping("/start/project/{projectId}")
    @Operation(summary = "Spawn new project session")
    public ResponseEntity<RedirectResponse> resolveRHubUrl(@PathVariable("projectId") String projectId, @RequestParam("sessionName") String sessionName) {
        LOGGER.info("Starting new RHub session ({}) for project: {}", sessionName, projectId);

        String spawnUrl = rHubHandler.spinRHubSession(projectId, sessionName);

        LOGGER.info("Session spawned: {}", spawnUrl);

        RedirectResponse response = new RedirectResponse(spawnUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/resume/session/{sessionId}")
    public ResponseEntity<RedirectResponse> resolveRHubExistingSession(@PathVariable("sessionId") String sessionId) {
        LOGGER.info("Resuming session: {}", sessionId);
        String spawnUrl = rHubHandler.resolveRHubExistingSession(sessionId);
        LOGGER.info("Resume success: {}", spawnUrl);

        RedirectResponse response = new RedirectResponse(spawnUrl);

        return ResponseEntity.ok(response);
    }
}

