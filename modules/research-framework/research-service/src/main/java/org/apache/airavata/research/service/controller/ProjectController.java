/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.apache.airavata.research.service.dto.CreateProjectRequest;
import org.apache.airavata.research.service.handlers.ProjectHandler;
import org.apache.airavata.research.service.model.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rf/projects")
@Tag(name = "Projects", description = "Projects are comprised of dataset and repository resources")
public class ProjectController {

    @Autowired
    private ProjectHandler projectHandler;

    @GetMapping("/")
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectHandler.getAllProjects());
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<Project>> getProjectsByOwnerId(@PathVariable(value = "ownerId") String ownerId) {
        return ResponseEntity.ok(projectHandler.getAllProjectsByOwnerId(ownerId));
    }

    @PostMapping("/")
    @Operation(summary = "Create a project")
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        return ResponseEntity.ok(projectHandler.createProject(createProjectRequest));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project by id")
    public ResponseEntity<Boolean> deleteProjectById(@PathVariable(value = "projectId") String projectId) {
        return ResponseEntity.ok(projectHandler.deleteProject(projectId));
    }

}
