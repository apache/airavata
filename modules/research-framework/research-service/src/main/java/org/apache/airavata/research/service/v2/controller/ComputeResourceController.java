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
package org.apache.airavata.research.service.v2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.research.service.dto.ComputeResourceDTO;
import org.apache.airavata.research.service.handler.ComputeResourceHandler;
import org.apache.airavata.research.service.service.UserContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
@RequestMapping("/api/v2/rf/compute-resources")
@Tag(name = "Compute Resources V2", description = "V2 API for managing compute infrastructure resources")
public class ComputeResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResourceController.class);

    @Autowired
    private ComputeResourceHandler computeResourceHandler;

    @Autowired
    private UserContextService userContextService;

    @Operation(summary = "Get all compute resources")
    @GetMapping("/")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<List<ComputeResourceDTO>> getComputeResources(
            @RequestParam(value = "nameSearch", required = false) String nameSearch) {
        
        LOGGER.info("Getting compute resources - search: {}", nameSearch);
        
        try {
            List<ComputeResourceDTO> resources;
            
            if (nameSearch != null && !nameSearch.trim().isEmpty()) {
                resources = computeResourceHandler.searchComputeResources(nameSearch);
            } else {
                resources = computeResourceHandler.getAllComputeResources();
            }
            
            LOGGER.info("Found {} compute resources", resources.size());
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            LOGGER.error("Failed to get compute resources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get compute resource by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<ComputeResourceDTO> getComputeResourceById(@PathVariable("id") String id) {
        LOGGER.info("Getting compute resource by ID: {}", id);
        
        try {
            ComputeResourceDTO resource = computeResourceHandler.getComputeResource(id);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            } else {
                LOGGER.error("Error getting compute resource {}: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @Operation(summary = "Create new compute resource")
    @PostMapping("/")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<?> createComputeResource(@Valid @RequestBody ComputeResourceDTO computeResourceDTO, BindingResult bindingResult) {
        LOGGER.info("Creating new compute resource: {}", computeResourceDTO.getHostName());
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        // TODO: Remove setDefaultValues() as part of migration - rely on DTO validation instead
        
        try {
            
            // Set creator from authenticated user
            String currentUser = userContextService.getCurrentUserId();
            // Note: ComputeResourceDTO would need a createdBy field to store this
            
            ComputeResourceDTO savedResource = computeResourceHandler.createComputeResource(computeResourceDTO);
            LOGGER.info("Created compute resource with ID: {} by user: {}", savedResource.getComputeResourceId(), currentUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResource);
        } catch (Exception e) {
            LOGGER.error("Error creating compute resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating compute resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Update compute resource")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<?> updateComputeResource(@PathVariable("id") String id, @Valid @RequestBody ComputeResourceDTO computeResourceDTO, BindingResult bindingResult) {
        LOGGER.info("Updating compute resource with ID: {}", id);
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        // TODO: Remove setDefaultValues() as part of migration - rely on DTO validation instead
        
        try {
            ComputeResourceDTO updatedResource = computeResourceHandler.updateComputeResource(id, computeResourceDTO);
            LOGGER.info("Successfully updated compute resource with ID: {}", id);
            
            return ResponseEntity.ok(updatedResource);
        } catch (Exception e) {
            LOGGER.error("Error updating compute resource with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating compute resource: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Delete compute resource")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<?> deleteComputeResource(@PathVariable("id") String id) {
        LOGGER.info("Deleting compute resource with ID: {}", id);
        
        try {
            computeResourceHandler.deleteComputeResource(id);
            LOGGER.info("Successfully deleted compute resource with ID: {}", id);
            return ResponseEntity.ok().body("Compute resource deleted successfully");
        } catch (Exception e) {
            LOGGER.error("Error deleting compute resource with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting compute resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Search compute resources by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<ComputeResourceDTO>> searchComputeResources(
            @RequestParam(value = "keyword") String keyword) {
        
        LOGGER.info("Searching compute resources with keyword: {}", keyword);
        
        try {
            List<ComputeResourceDTO> resources = computeResourceHandler.searchComputeResources(keyword);
            LOGGER.info("Found {} compute resources matching keyword: {}", resources.size(), keyword);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            LOGGER.error("Error searching compute resources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Star/unstar a compute resource")
    @PostMapping("/{id}/star")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> starComputeResource(@PathVariable("id") String id) {
        LOGGER.info("Toggling star for compute resource with ID: {}", id);
        
        try {
            String userId = userContextService.getCurrentUserId();
            if (computeResourceHandler.existsComputeResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                // For now, return simple toggle response
                LOGGER.info("Star toggle requested for compute resource: {} by user: {} (simplified implementation)", id, userId);
                return ResponseEntity.ok(true);
            } else {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error toggling compute resource star: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Check if user starred a compute resource")
    @GetMapping("/{id}/star")
    public ResponseEntity<Boolean> checkComputeResourceStarred(@PathVariable("id") String id) {
        LOGGER.info("Checking if compute resource is starred: {}", id);
        
        try {
            if (computeResourceHandler.existsComputeResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                LOGGER.info("Star status check for compute resource: {} (simplified implementation)", id);
                return ResponseEntity.ok(false);
            } else {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error checking compute resource star status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get compute resource star count")
    @GetMapping("/{id}/stars/count")
    public ResponseEntity<Integer> getComputeResourceStarCount(@PathVariable("id") String id) {
        LOGGER.info("Getting star count for compute resource: {}", id);
        
        try {
            if (computeResourceHandler.existsComputeResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                return ResponseEntity.ok(0);
            } else {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error getting star count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all starred compute resources")
    @GetMapping("/starred")
    public ResponseEntity<List<ComputeResourceDTO>> getStarredComputeResources() {
        LOGGER.info("Fetching starred compute resources");
        
        try {
            // TODO: Implement proper v1 ResourceStar system integration
            // For now, return empty list
            List<ComputeResourceDTO> starredResources = List.of();
            LOGGER.info("Found {} starred compute resources", starredResources.size());
            return ResponseEntity.ok(starredResources);
        } catch (Exception e) {
            LOGGER.error("Error fetching starred compute resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Set intelligent defaults for backend-only fields not provided by UI
     * UI provides core fields (hostName, name, description) - backend fills in infrastructure defaults
     */
    private void setDefaultValues(ComputeResourceDTO dto) {
        // Backend fills infrastructure defaults - UI provides name and resourceDescription
        
        // Set default compute type
        if (dto.getComputeType() == null || dto.getComputeType().trim().isEmpty()) {
            dto.setComputeType("HPC");
        }
        
        // Set default CPU cores
        if (dto.getCpuCores() == null) {
            dto.setCpuCores(1);
        }
        
        // Set default memory
        if (dto.getMemoryGB() == null) {
            dto.setMemoryGB(1);
        }
        
        // Set default operating system
        if (dto.getOperatingSystem() == null || dto.getOperatingSystem().trim().isEmpty()) {
            dto.setOperatingSystem("Linux");
        }
        
        // Set default queue system
        if (dto.getQueueSystem() == null || dto.getQueueSystem().trim().isEmpty()) {
            dto.setQueueSystem("SLURM");
        }
        
        // Set default resource manager
        if (dto.getResourceManager() == null || dto.getResourceManager().trim().isEmpty()) {
            dto.setResourceManager("Default Resource Manager");
        }
        
        // Set default SSH configuration (using alternative hostname field)
        if (dto.getAlternativeSSHHostName() == null || dto.getAlternativeSSHHostName().trim().isEmpty()) {
            dto.setAlternativeSSHHostName(dto.getHostName()); // Default to main hostname
        }
        
        if (dto.getSshPort() == null) {
            dto.setSshPort(22);
        }
        
        if (dto.getSecurityProtocol() == null || dto.getSecurityProtocol().trim().isEmpty()) {
            dto.setSecurityProtocol("SSH_KEYS");
        }
        
        // Working directory is no longer a direct field - handled by related entities
        
        // Set default resource job manager type
        if (dto.getResourceJobManagerType() == null || dto.getResourceJobManagerType().trim().isEmpty()) {
            dto.setResourceJobManagerType("SLURM");
        }
        
        // Set default data movement protocol
        if (dto.getDataMovementProtocol() == null || dto.getDataMovementProtocol().trim().isEmpty()) {
            dto.setDataMovementProtocol("SCP");
        }
        
        LOGGER.debug("Set default values for compute resource: name={}, type={}, cores={}", 
                    dto.getName(), dto.getComputeType(), dto.getCpuCores());
    }
}