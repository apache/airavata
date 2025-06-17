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
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.research.service.dto.CreateResourceRequest;
import org.apache.airavata.research.service.dto.ModifyResourceRequest;
import org.apache.airavata.research.service.dto.ResourceResponse;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.handlers.ProjectHandler;
import org.apache.airavata.research.service.handlers.ResourceHandler;
import org.apache.airavata.research.service.model.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rf/resources")
@Tag(name = "Resources", description = "Datasets, notebooks, repositories, models")
public class ResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);

    @org.springframework.beans.factory.annotation.Autowired
    private ResourceHandler resourceHandler;

    @Autowired
    private ProjectHandler projectHandler;

    @PostMapping("/dataset")
    public ResponseEntity<ResourceResponse> createDatasetResource(@RequestBody DatasetResource datasetResource) {
        ResourceResponse response = resourceHandler.createResource(datasetResource, ResourceTypeEnum.DATASET);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notebook")
    public ResponseEntity<ResourceResponse> createNotebookResource(@RequestBody NotebookResource notebookResource) {
        ResourceResponse response = resourceHandler.createResource(notebookResource, ResourceTypeEnum.NOTEBOOK);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/repository")
    public ResponseEntity<ResourceResponse> createRepositoryResource(
            @RequestBody CreateResourceRequest resourceRequest,
            @RequestParam(value = "githubUrl") String repositoryUrl) {
        ResourceResponse response = resourceHandler.createRepositoryResource(resourceRequest, repositoryUrl);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/repository")
    public ResponseEntity<Resource> modifyRepositoryResource(@RequestBody ModifyResourceRequest resourceRequest) {
        Resource response = resourceHandler.modifyResource(resourceRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/model")
    public ResponseEntity<ResourceResponse> createModelResource(@RequestBody ModelResource modelResource) {
        ResourceResponse response = resourceHandler.createResource(modelResource, ResourceTypeEnum.MODEL);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all tags")
    @GetMapping(value = "/tags/all")
    public ResponseEntity<List<org.apache.airavata.research.service.model.entity.Tag>> getTags() {
        return ResponseEntity.ok(resourceHandler.getAllTagsByPopularity());
    }

    @Operation(summary = "Get dataset, notebook, repository, or model")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(resourceHandler.getResourceById(id));
    }

    @Operation(summary = "Delete dataset, notebook, repository, or model")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Boolean> deleteResource(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(resourceHandler.deleteResourceById(id));
    }

    @Operation(summary = "Get all resources")
    @GetMapping("/")
    public ResponseEntity<Page<Resource>> getAllResources(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "nameSearch") String nameSearch,
            @RequestParam(value = "type") ResourceTypeEnum[] types,
            @RequestParam(value = "tag", required = false) String[] tags) {
        List<Class<? extends Resource>> typeList = new ArrayList<>();
        for (ResourceTypeEnum resourceType : types) {
            if (resourceType == ResourceTypeEnum.REPOSITORY) {
                typeList.add(RepositoryResource.class);
            } else if (resourceType == ResourceTypeEnum.NOTEBOOK) {
                typeList.add(NotebookResource.class);
            } else if (resourceType == ResourceTypeEnum.MODEL) {
                typeList.add(ModelResource.class);
            } else if (resourceType == ResourceTypeEnum.DATASET) {
                typeList.add(DatasetResource.class);
            }
        }
        Page<Resource> response = resourceHandler.getAllResources(pageNumber, pageSize, typeList, tags, nameSearch);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get resource by name")
    @GetMapping("/search")
    public ResponseEntity<List<Resource>> searchResource(
            @RequestParam(value = "type") ResourceTypeEnum type,
            @RequestParam(value = "name", required = false) String name) {

        List<Resource> resources = resourceHandler.getAllResourcesByTypeAndName(getResourceType(type), name);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get projects associated with a resource")
    @GetMapping(value = "/{id}/projects")
    public ResponseEntity<List<Project>> getProjectsFromResourceId(@PathVariable(value = "id") String id) {
        Resource resouce = resourceHandler.getResourceById(id);
        List<Project> projects;
        if (resouce.getClass() == RepositoryResource.class) {
            projects = projectHandler.findProjectsWithRepository((RepositoryResource) resouce);
        } else if (resouce.getClass() == DatasetResource.class) {
            projects = projectHandler.findProjectsContainingDataset((DatasetResource) resouce);
        } else {
            throw new RuntimeException(
                    "Projects are only associated with repositories and datasets, and id: " + id + " is not either.");
        }

        return ResponseEntity.ok(projects);
    }

    private Class<? extends Resource> getResourceType(ResourceTypeEnum resourceTypeEnum) {
        return switch (resourceTypeEnum) {
            case REPOSITORY -> RepositoryResource.class;
            case NOTEBOOK -> NotebookResource.class;
            case MODEL -> ModelResource.class;
            case DATASET -> DatasetResource.class;
            default -> null;
        };
    }
}
