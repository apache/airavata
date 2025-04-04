package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.service.handlers.ProjectHandler;
import org.apache.airavata.research.service.model.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
