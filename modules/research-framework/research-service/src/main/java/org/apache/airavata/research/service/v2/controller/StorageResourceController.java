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
import org.apache.airavata.research.service.dto.StorageResourceDTO;
import org.apache.airavata.research.service.handler.StorageResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/v2/rf/storage-resources")
@Tag(name = "Storage Resources V2", description = "V2 API for managing storage infrastructure resources")
public class StorageResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageResourceController.class);

    @Autowired
    private StorageResourceHandler storageResourceHandler;

    @Operation(summary = "Get all public storage resources")
    @GetMapping("/public")
    public ResponseEntity<List<StorageResourceDTO>> getStorageResources(
            @RequestParam(value = "nameSearch", required = false) String nameSearch) {
        
        LOGGER.info("Getting storage resources - search: {}", nameSearch);
        
        try {
            List<StorageResourceDTO> resources;
            
            if (nameSearch != null && !nameSearch.trim().isEmpty()) {
                resources = storageResourceHandler.searchStorageResources(nameSearch);
            } else {
                resources = storageResourceHandler.getAllStorageResources();
            }
            
            LOGGER.info("Found {} storage resources", resources.size());
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            LOGGER.error("Failed to get storage resources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get storage resource by ID")
    @GetMapping("/public/{id}")
    public ResponseEntity<StorageResourceDTO> getStorageResourceById(@PathVariable("id") String id) {
        LOGGER.info("Getting storage resource by ID: {}", id);
        
        try {
            StorageResourceDTO resource = storageResourceHandler.getStorageResource(id);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            LOGGER.error("Error getting storage resource {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Create new storage resource")
    @PostMapping("/")
    public ResponseEntity<?> createStorageResource(@Valid @RequestBody StorageResourceDTO storageResourceDTO, BindingResult bindingResult) {
        LOGGER.info("Creating new storage resource: {}", storageResourceDTO.getHostName());
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        try {
            // Set default values for fields that might be null
            if (storageResourceDTO.getCapacityTB() == null) {
                storageResourceDTO.setCapacityTB(1L); // Default to 1 TB
            }
            
            StorageResourceDTO savedResource = storageResourceHandler.createStorageResource(storageResourceDTO);
            LOGGER.info("Created storage resource with ID: {}", savedResource.getStorageResourceId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResource);
        } catch (Exception e) {
            LOGGER.error("Error creating storage resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating storage resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Update storage resource")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStorageResource(@PathVariable("id") String id, @Valid @RequestBody StorageResourceDTO storageResourceDTO, BindingResult bindingResult) {
        LOGGER.info("Updating storage resource with ID: {}", id);
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        try {
            StorageResourceDTO updatedResource = storageResourceHandler.updateStorageResource(id, storageResourceDTO);
            LOGGER.info("Successfully updated storage resource with ID: {}", id);
            
            return ResponseEntity.ok(updatedResource);
        } catch (Exception e) {
            LOGGER.error("Error updating storage resource with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating storage resource: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Delete storage resource")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStorageResource(@PathVariable("id") String id) {
        LOGGER.info("Deleting storage resource with ID: {}", id);
        
        try {
            storageResourceHandler.deleteStorageResource(id);
            LOGGER.info("Successfully deleted storage resource with ID: {}", id);
            return ResponseEntity.ok().body("Storage resource deleted successfully");
        } catch (Exception e) {
            LOGGER.error("Error deleting storage resource with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting storage resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Search storage resources by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<StorageResourceDTO>> searchStorageResources(
            @RequestParam(value = "keyword") String keyword) {
        
        LOGGER.info("Searching storage resources with keyword: {}", keyword);
        
        try {
            List<StorageResourceDTO> resources = storageResourceHandler.searchStorageResources(keyword);
            LOGGER.info("Found {} storage resources matching keyword: {}", resources.size(), keyword);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            LOGGER.error("Error searching storage resources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get storage resources by type")
    @GetMapping("/type/{storageType}")
    public ResponseEntity<List<StorageResourceDTO>> getStorageResourcesByType(
            @PathVariable("storageType") String storageType) {
        
        LOGGER.info("Getting storage resources by type: {}", storageType);
        
        try {
            List<StorageResourceDTO> resources = storageResourceHandler.getStorageResourcesByType(storageType);
            LOGGER.info("Found {} storage resources of type: {}", resources.size(), storageType);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            LOGGER.error("Error filtering storage resources by type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Star/unstar a storage resource")
    @PostMapping("/{id}/star")
    public ResponseEntity<Boolean> starStorageResource(@PathVariable("id") String id) {
        LOGGER.info("Toggling star for storage resource with ID: {}", id);
        
        try {
            if (storageResourceHandler.existsStorageResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                // For now, return simple toggle response
                LOGGER.info("Star toggle requested for storage resource: {} (simplified implementation)", id);
                return ResponseEntity.ok(true);
            } else {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error toggling storage resource star: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Check if user starred a storage resource")
    @GetMapping("/{id}/star")
    public ResponseEntity<Boolean> checkStorageResourceStarred(@PathVariable("id") String id) {
        LOGGER.info("Checking if storage resource is starred: {}", id);
        
        try {
            if (storageResourceHandler.existsStorageResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                LOGGER.info("Star status check for storage resource: {} (simplified implementation)", id);
                return ResponseEntity.ok(false);
            } else {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error checking storage resource star status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get storage resource star count")
    @GetMapping("/{id}/stars/count")
    public ResponseEntity<Integer> getStorageResourceStarCount(@PathVariable("id") String id) {
        LOGGER.info("Getting star count for storage resource: {}", id);
        
        try {
            if (storageResourceHandler.existsStorageResource(id)) {
                // TODO: Implement proper v1 ResourceStar system integration
                return ResponseEntity.ok(0);
            } else {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error getting star count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all starred storage resources")
    @GetMapping("/starred")
    public ResponseEntity<List<StorageResourceDTO>> getStarredStorageResources() {
        LOGGER.info("Fetching starred storage resources");
        
        try {
            // TODO: Implement proper v1 ResourceStar system integration
            // For now, return empty list
            List<StorageResourceDTO> starredResources = List.of();
            LOGGER.info("Found {} starred storage resources", starredResources.size());
            return ResponseEntity.ok(starredResources);
        } catch (Exception e) {
            LOGGER.error("Error fetching starred storage resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}