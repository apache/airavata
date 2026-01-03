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

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.services.ProjectService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@RestController
@RequestMapping("/api/v1/projects")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable String projectId) {
        try {
            Project project = projectService.getProject(projectId);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(project);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestParam String gatewayId, @RequestBody Project project) {
        try {
            String projectId = projectService.addProject(project, gatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("projectId", projectId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody Project project) {
        try {
            project.setProjectID(projectId);
            projectService.updateProject(project, projectId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId) {
        try {
            projectService.removeProject(projectId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> searchProjects(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderType) {
        try {
            Map<String, String> filters = gatewayId != null ? Map.of("GATEWAY_ID", gatewayId) : null;
            ResultOrderType resultOrderType =
                    orderType != null ? ResultOrderType.valueOf(orderType.toUpperCase()) : null;
            List<Project> projects = projectService.searchProjects(filters, limit, offset, orderBy, resultOrderType);
            return ResponseEntity.ok(projects);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
