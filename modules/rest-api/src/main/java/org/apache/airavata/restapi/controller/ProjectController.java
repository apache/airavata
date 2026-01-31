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

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.ProjectResourceAccount;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectResourceAccountService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.security.model.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final ProjectResourceAccountService projectResourceAccountService;
    private final AuthorizationService authorizationService;
    private final GatewayService gatewayService;

    public ProjectController(ProjectService projectService,
            ProjectResourceAccountService projectResourceAccountService,
            AuthorizationService authorizationService,
            GatewayService gatewayService) {
        this.projectService = projectService;
        this.projectResourceAccountService = projectResourceAccountService;
        this.authorizationService = authorizationService;
        this.gatewayService = gatewayService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable String projectId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            var project = projectService.getProject(projectId);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the project's gateway
            authorizationService.requireGatewayAccess(authzToken, project.getGatewayId());
            
            return ResponseEntity.ok(project);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestParam(required = false) String gatewayId, 
            @RequestBody Project project,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Log incoming parameters for debugging
            logger.debug("Creating project - query param gatewayId: {}, body gatewayId: {}", 
                    gatewayId, project.getGatewayId());
            
            // Ignore gatewayId from request body - use only query parameter or token
            // This prevents conflicts if frontend sends gatewayId in both places
            if (project.getGatewayId() != null) {
                logger.debug("Ignoring gatewayId from request body: {}", project.getGatewayId());
                project.setGatewayId(null);
            }
            
            // Validate and scope gateway ID
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            logger.debug("Scoped gateway ID: {}", scopedGatewayId);
            
            // Ensure gateway exists before creating project (foreign key constraint)
            try {
                var gateway = gatewayService.getGateway(scopedGatewayId);
                if (gateway == null) {
                    logger.error("Gateway does not exist: {}", scopedGatewayId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Gateway does not exist: " + scopedGatewayId));
                }
            } catch (Exception e) {
                logger.error("Error checking gateway existence: gatewayId={}, error={}", scopedGatewayId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Gateway does not exist: " + scopedGatewayId));
            }
            
            logger.debug("Creating project: name={}, gatewayId={}", project.getName(), scopedGatewayId);
            var projectId = projectService.addProject(project, scopedGatewayId);
            logger.info("Successfully created project: projectId={}, gatewayId={}", projectId, scopedGatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("projectId", projectId));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            logger.error("RegistryException while creating project: gatewayId={}, error={}", gatewayId, e.getMessage(), e);
            // Check if it's a foreign key constraint error
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Gateway does not exist. Please ensure the gateway is properly configured."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while creating project: gatewayId={}, error={}", gatewayId, e.getMessage(), e);
            // Check if it's a foreign key constraint error
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("foreign key constraint") || 
                    errorMessage.contains("Cannot add or update a child row"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Gateway does not exist. Please ensure the gateway is properly configured."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create project: " + errorMessage));
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody Project project, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Get existing project to verify gateway access
            var existingProject = projectService.getProject(projectId);
            if (existingProject == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the project's gateway
            authorizationService.requireGatewayAccess(authzToken, existingProject.getGatewayId());
            
            project.setProjectID(projectId);
            projectService.updateProject(project, projectId);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Get existing project to verify gateway access
            var existingProject = projectService.getProject(projectId);
            if (existingProject == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the project's gateway
            authorizationService.requireGatewayAccess(authzToken, existingProject.getGatewayId());
            
            projectService.removeProject(projectId);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> searchProjects(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderType,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            var filters = scopedGatewayId != null ? Map.of("GATEWAY_ID", scopedGatewayId) : null;
            var resultOrderType = orderType != null ? ResultOrderType.valueOf(orderType.toUpperCase()) : null;
            var projects = projectService.searchProjects(filters, limit, offset, orderBy, resultOrderType);
            return ResponseEntity.ok(projects);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // --- Project resource accounts (one account per compute resource per project) ---

    @GetMapping("/{projectId}/resource-accounts")
    public ResponseEntity<?> getProjectResourceAccounts(@PathVariable String projectId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            var project = projectService.getProject(projectId);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            authorizationService.requireGatewayAccess(authzToken, project.getGatewayId());
            var bindings = projectResourceAccountService.getBindings(projectId);
            return ResponseEntity.ok(bindings);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{projectId}/resource-accounts")
    public ResponseEntity<?> addOrUpdateProjectResourceAccount(
            @PathVariable String projectId,
            @RequestBody ProjectResourceAccount binding,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            var project = projectService.getProject(projectId);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            authorizationService.requireGatewayAccess(authzToken, project.getGatewayId());
            binding.setProjectId(projectId);
            if (binding.getGatewayId() == null || binding.getGatewayId().isBlank()) {
                binding.setGatewayId(project.getGatewayId());
            }
            projectResourceAccountService.addOrUpdateBinding(binding);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{projectId}/resource-accounts/{computeResourceId}")
    public ResponseEntity<?> removeProjectResourceAccount(
            @PathVariable String projectId,
            @PathVariable String computeResourceId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            var project = projectService.getProject(projectId);
            if (project == null) {
                return ResponseEntity.notFound().build();
            }
            authorizationService.requireGatewayAccess(authzToken, project.getGatewayId());
            boolean removed = projectResourceAccountService.removeBinding(projectId, computeResourceId);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
