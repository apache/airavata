package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.research.project.dto.CreateProjectRequest;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.service.ResearchProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("researchProjectController")
@RequestMapping("/api/v1/research/artifacts/projects")
@Tag(name = "Projects", description = "Projects are comprised of dataset and repository artifacts")
public class ResearchProjectController {

    private final ResearchProjectService projectService;

    public ResearchProjectController(ResearchProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("")
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ResearchProjectEntity>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ResearchProjectEntity>> getProjectsByOwnerId(
            @PathVariable(value = "ownerId") String ownerId) {
        return ResponseEntity.ok(projectService.getAllProjectsByOwnerId(ownerId));
    }

    @PostMapping("")
    @Operation(summary = "Create a project")
    public ResponseEntity<ResearchProjectEntity> createProject(
            @RequestBody CreateProjectRequest createProjectRequest) {
        return ResponseEntity.ok(projectService.createProject(createProjectRequest));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project by id")
    public ResponseEntity<Boolean> deleteProjectById(@PathVariable(value = "projectId") String projectId) {
        return ResponseEntity.ok(projectService.deleteProject(projectId));
    }
}
