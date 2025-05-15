package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.service.dto.CreateProjectRequest;
import org.apache.airavata.research.service.handlers.ProjectHandler;
import org.apache.airavata.research.service.model.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Project>> getProjectsByOwnerId(@PathVariable(value="ownerId") String ownerId) {
        return ResponseEntity.ok(projectHandler.getAllProjectsByOwnerId(ownerId));
    }

    @PostMapping("/")
    @Operation(summary = "Create a project")

    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        return ResponseEntity.ok(projectHandler.createProject(createProjectRequest));
    }
}
