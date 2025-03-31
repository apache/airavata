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

import org.apache.airavata.research.service.ResponseTypes.ResourceResponse;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import org.apache.airavata.research.service.model.entity.NotebookResource;
import org.apache.airavata.research.service.handlers.ProjectHandler;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/rf/project-management")
@Tag(name = "Project", description = "The Project API")
public class ProjectController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);


    @org.springframework.beans.factory.annotation.Autowired
    private ProjectHandler projectHandler;


    // Endpoint for adding a DatasetResource
    @PostMapping("/dataset")
    public ResponseEntity<ResourceResponse> createDatasetResource(@RequestBody DatasetResource datasetResource) {
        ResourceResponse response = projectHandler.createResource(datasetResource, ResourceTypeEnum.DATASET);
        return ResponseEntity.ok(response);
    }

    // Endpoint for adding a NotebookResource
    @PostMapping("/notebook")
    public ResponseEntity<ResourceResponse> createNotebookResource(@RequestBody NotebookResource notebookResource) {
        ResourceResponse response = projectHandler.createResource(notebookResource, ResourceTypeEnum.NOTEBOOK);
        return ResponseEntity.ok(response);
    }

    // Endpoint for adding a RepositoryResource
    @PostMapping("/repository")
    public ResponseEntity<ResourceResponse> createRepositoryResource(@RequestBody RepositoryResource repositoryResource) {
        ResourceResponse response = projectHandler.createResource(repositoryResource, ResourceTypeEnum.REPOSITORY);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get dataset, notebook, or repository"
    )
    @GetMapping(value = "/resources/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable(value="id") String id) {
        return ResponseEntity.ok(projectHandler.getResourceById(id));
    }
}
