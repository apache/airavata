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
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.model.CreateArtifactRequest;
import org.apache.airavata.research.artifact.model.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.model.ResearchArtifact;
import org.apache.airavata.research.artifact.service.ArtifactService;
import org.apache.airavata.research.project.model.ResearchProject;
import org.apache.airavata.research.project.service.ResearchProjectService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/research/artifacts")
@Tag(name = "Research Artifacts", description = "Datasets, notebooks, repositories, models")
public class ResearchArtifactController {

    private final ArtifactService artifactService;
    private final ResearchProjectService projectService;

    public ResearchArtifactController(ArtifactService artifactService, ResearchProjectService projectService) {
        this.artifactService = artifactService;
        this.projectService = projectService;
    }

    @Operation(summary = "Create a dataset artifact")
    @PostMapping("/dataset")
    public ResponseEntity<ResearchArtifact> createDatasetArtifact(@RequestBody CreateArtifactRequest request) {
        var artifact = artifactService.createArtifact(request, ArtifactType.DATASET);
        return ResponseEntity.ok(artifact);
    }

    @Operation(summary = "Create a notebook artifact")
    @PostMapping("/notebook")
    public ResponseEntity<ResearchArtifact> createNotebookArtifact(@RequestBody CreateArtifactRequest request) {
        var artifact = artifactService.createArtifact(request, ArtifactType.REPOSITORY);
        return ResponseEntity.ok(artifact);
    }

    @Operation(summary = "Create a repository artifact")
    @PostMapping("/repository")
    public ResponseEntity<ResearchArtifact> createRepositoryArtifact(
            @RequestBody CreateArtifactRequest request, @RequestParam(value = "githubUrl") String repositoryUrl) {
        request.setRepositoryUrl(repositoryUrl);
        var artifact = artifactService.createArtifact(request, ArtifactType.REPOSITORY);
        return ResponseEntity.ok(artifact);
    }

    @Operation(summary = "Modify a repository artifact")
    @PatchMapping("/repository")
    public ResponseEntity<ResearchArtifact> modifyRepositoryArtifact(@RequestBody ModifyArtifactRequest request) {
        var artifact = artifactService.modifyArtifact(request);
        return ResponseEntity.ok(artifact);
    }

    @Operation(summary = "Create a model artifact")
    @PostMapping("/model")
    public ResponseEntity<ResearchArtifact> createModelArtifact(@RequestBody CreateArtifactRequest request) {
        var artifact = artifactService.createArtifact(request, ArtifactType.REPOSITORY);
        return ResponseEntity.ok(artifact);
    }

    @Operation(summary = "Get all tags")
    @GetMapping(value = "/public/tags/all")
    public ResponseEntity<List<org.apache.airavata.research.artifact.model.Tag>> getTags() {
        return ResponseEntity.ok(artifactService.getAllTagsByAlphabeticalOrder());
    }

    @Operation(summary = "Get dataset, notebook, repository, or model")
    @GetMapping(value = "/public/{id}")
    public ResponseEntity<ResearchArtifact> getArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.getArtifactById(id));
    }

    @Operation(summary = "Delete dataset, notebook, repository, or model")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Boolean> deleteArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.deleteArtifactById(id));
    }

    @Operation(summary = "Get all artifacts")
    @GetMapping("/public")
    public ResponseEntity<Page<ResearchArtifact>> getAllArtifacts(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "nameSearch") String nameSearch,
            @RequestParam(value = "type") ArtifactType[] types,
            @RequestParam(value = "tag", required = false) String[] tags) {
        var response = artifactService.getAllArtifacts(pageNumber, pageSize, Arrays.asList(types), tags, nameSearch);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get artifact by name")
    @GetMapping("/search")
    public ResponseEntity<List<ResearchArtifact>> searchArtifact(
            @RequestParam(value = "type") ArtifactType type,
            @RequestParam(value = "name", required = false) String name) {
        var artifacts = artifactService.getAllArtifactsByTypeAndName(type, name);
        return ResponseEntity.ok(artifacts);
    }

    @Operation(summary = "Get projects associated with an artifact")
    @GetMapping(value = "/public/{id}/projects")
    public ResponseEntity<List<ResearchProject>> getProjectsFromArtifactId(@PathVariable(value = "id") String id) {
        var artifact = artifactService.getArtifactById(id);
        List<ResearchProject> projects;
        if (artifact.getType() == ArtifactType.REPOSITORY) {
            projects = projectService.findProjectsWithRepository(id);
        } else if (artifact.getType() == ArtifactType.DATASET) {
            projects = projectService.findProjectsContainingDataset(id);
        } else {
            throw new IllegalArgumentException(
                    "Projects are only associated with repositories and datasets, and id: " + id + " is not either.");
        }
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Star/unstar an artifact")
    @PostMapping(value = "/{id}/star")
    public ResponseEntity<Boolean> starOrUnstarArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.starOrUnstarArtifact(id));
    }

    @Operation(summary = "Check whether a user starred an artifact")
    @GetMapping(value = "/{id}/star")
    public ResponseEntity<Boolean> checkWhetherUserStarredArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.checkWhetherUserStarredArtifact(id));
    }

    @Operation(summary = "Get artifact star count")
    @GetMapping(value = "/public/{id}/star/count")
    public ResponseEntity<Long> getArtifactStarCount(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.getArtifactStarCount(id));
    }

    @Operation(summary = "Get all starred artifacts of a user")
    @GetMapping(value = "/{userId}/stars")
    public ResponseEntity<List<ResearchArtifact>> getAllStarredArtifacts(
            @PathVariable(value = "userId") String userId) {
        return ResponseEntity.ok(artifactService.getAllStarredArtifacts(userId));
    }
}
