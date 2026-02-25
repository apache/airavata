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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.apache.airavata.research.experiment.model.Project;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.research.experiment.service.ProjectService;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.iam.model.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("restProjectController")
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final AuthorizationService authorizationService;
    private final GatewayService gatewayService;
    private final ExperimentService experimentService;

    public ProjectController(
            ProjectService projectService,
            AuthorizationService authorizationService,
            GatewayService gatewayService,
            ExperimentService experimentService) {
        this.projectService = projectService;
        this.authorizationService = authorizationService;
        this.gatewayService = gatewayService;
        this.experimentService = experimentService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable String projectId, HttpServletRequest request) throws Exception {
        var authzToken = getAuthzToken(request);
        var project = projectService.getProject(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        authorizationService.requireGatewayAccess(authzToken, project.getGatewayId());
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestParam(required = false) String gatewayId,
            @Valid @RequestBody Project project,
            HttpServletRequest request)
            throws Exception {
        var authzToken = getAuthzToken(request);

        if (project.getGatewayId() != null) {
            project.setGatewayId(null);
        }

        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        var gateway = gatewayService.getGateway(scopedGatewayId);
        if (gateway == null) {
            throw new InvalidRequestException("Gateway does not exist: " + scopedGatewayId);
        }

        var projectId = experimentService.createProject(scopedGatewayId, project);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("projectId", projectId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable String projectId, @Valid @RequestBody Project project, HttpServletRequest request)
            throws Exception {
        var authzToken = getAuthzToken(request);

        var existingProject = projectService.getProject(projectId);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        authorizationService.requireGatewayAccess(authzToken, existingProject.getGatewayId());

        project.setProjectId(projectId);
        projectService.updateProject(projectId, project);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, HttpServletRequest request)
            throws Exception {
        var authzToken = getAuthzToken(request);

        var existingProject = projectService.getProject(projectId);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        authorizationService.requireGatewayAccess(authzToken, existingProject.getGatewayId());

        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> searchProjects(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request)
            throws Exception {
        var authzToken = getAuthzToken(request);
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
        var projects = projectService.searchProjects(scopedGatewayId, null, null, limit, offset);
        return ResponseEntity.ok(projects);
    }
}
