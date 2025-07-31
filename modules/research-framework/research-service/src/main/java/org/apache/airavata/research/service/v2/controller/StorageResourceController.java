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
import java.util.Optional;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.v2.entity.StorageResource;
import org.apache.airavata.research.service.v2.repository.StorageResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private static final PrivacyEnum PUBLIC_PRIVACY = PrivacyEnum.PUBLIC;
    private static final StateEnum ACTIVE_STATE = StateEnum.ACTIVE;

    private final StorageResourceRepository storageResourceRepository;

    public StorageResourceController(StorageResourceRepository storageResourceRepository) {
        this.storageResourceRepository = storageResourceRepository;
    }

    @Operation(summary = "Get all public storage resources with pagination")
    @GetMapping("/public")
    public ResponseEntity<Page<StorageResource>> getStorageResources(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "nameSearch", required = false) String nameSearch,
            @RequestParam(value = "tag", required = false) String[] tags) {
        
        LOGGER.info("Getting storage resources - page: {}, size: {}, search: {}", pageNumber, pageSize, nameSearch);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<StorageResource> resources;
        
        if (nameSearch != null && !nameSearch.trim().isEmpty()) {
            resources = storageResourceRepository.findByNameSearchAndPrivacyAndState(nameSearch, PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        } else {
            resources = storageResourceRepository.findByPrivacyAndState(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        }
        
        LOGGER.info("Found {} storage resources", resources.getTotalElements());
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get storage resource by ID")
    @GetMapping("/public/{id}")
    public ResponseEntity<StorageResource> getStorageResourceById(@PathVariable("id") String id) {
        LOGGER.info("Getting storage resource by ID: {}", id);
        
        Optional<StorageResource> resource = storageResourceRepository.findById(id);
        if (resource.isPresent()) {
            return ResponseEntity.ok(resource.get());
        } else {
            LOGGER.warn("Storage resource not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new storage resource")
    @PostMapping("/")
    public ResponseEntity<?> createStorageResource(@Valid @RequestBody StorageResource storageResource, BindingResult bindingResult) {
        LOGGER.info("Creating new storage resource: {}", storageResource.getName());
        
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
            if (storageResource.getCapacityTB() == null) {
                storageResource.setCapacityTB(1L); // Default to 1 TB
            }
            if (storageResource.getSupportsEncryption() == null) {
                storageResource.setSupportsEncryption(false);
            }
            if (storageResource.getSupportsVersioning() == null) {
                storageResource.setSupportsVersioning(false);
            }
            if (storageResource.getPrivacy() == null) {
                storageResource.setPrivacy(PUBLIC_PRIVACY);
            }
            if (storageResource.getState() == null) {
                storageResource.setState(ACTIVE_STATE);
            }
            // Note: starCount functionality handled separately in v1 star system
            
            StorageResource savedResource = storageResourceRepository.save(storageResource);
            LOGGER.info("Created storage resource with ID: {}", savedResource.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResource);
        } catch (Exception e) {
            LOGGER.error("Error creating storage resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating storage resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Update storage resource")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStorageResource(@PathVariable("id") String id, @Valid @RequestBody StorageResource storageResource, BindingResult bindingResult) {
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
            Optional<StorageResource> existingResource = storageResourceRepository.findById(id);
            if (!existingResource.isPresent()) {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Set the ID to ensure we update the correct resource
            storageResource.setId(id);
            
            // Preserve creation timestamp
            storageResource.setCreatedAt(existingResource.get().getCreatedAt());
            
            StorageResource updatedResource = storageResourceRepository.save(storageResource);
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
            Optional<StorageResource> existingResource = storageResourceRepository.findById(id);
            if (!existingResource.isPresent()) {
                LOGGER.warn("Storage resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            storageResourceRepository.deleteById(id);
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
    public ResponseEntity<List<StorageResource>> searchStorageResources(
            @RequestParam(value = "keyword") String keyword) {
        
        LOGGER.info("Searching storage resources with keyword: {}", keyword);
        
        List<StorageResource> resources = storageResourceRepository
                .findByNameContainingIgnoreCaseAndPrivacyAndState(keyword, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} storage resources matching keyword: {}", resources.size(), keyword);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get storage resources by type")
    @GetMapping("/type/{storageType}")
    public ResponseEntity<List<StorageResource>> getStorageResourcesByType(
            @PathVariable("storageType") String storageType) {
        
        LOGGER.info("Getting storage resources by type: {}", storageType);
        
        List<StorageResource> resources = storageResourceRepository
                .findByStorageTypeAndPrivacyAndState(storageType, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} storage resources of type: {}", resources.size(), storageType);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Star/unstar a storage resource")
    @PostMapping("/{id}/star")
    public ResponseEntity<Boolean> starStorageResource(@PathVariable("id") String id) {
        LOGGER.info("Toggling star for storage resource with ID: {}", id);
        
        try {
            Optional<StorageResource> resourceOpt = storageResourceRepository.findById(id);
            if (resourceOpt.isPresent()) {
                StorageResource resource = resourceOpt.get();
                
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
            Optional<StorageResource> resourceOpt = storageResourceRepository.findById(id);
            if (resourceOpt.isPresent()) {
                StorageResource resource = resourceOpt.get();
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
            Optional<StorageResource> resourceOpt = storageResourceRepository.findById(id);
            if (resourceOpt.isPresent()) {
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
    public ResponseEntity<Page<StorageResource>> getStarredStorageResources(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        LOGGER.info("Fetching starred storage resources - page: {}, size: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            // TODO: Implement proper v1 ResourceStar system integration
            // For now, return empty page
            Page<StorageResource> starredResources = storageResourceRepository.findByPrivacyAndState(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
            // Filter to empty for now until proper star system is implemented
            starredResources = Page.empty();
            LOGGER.info("Found {} starred storage resources", starredResources.getTotalElements());
            return ResponseEntity.ok(starredResources);
        } catch (Exception e) {
            LOGGER.error("Error fetching starred storage resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}