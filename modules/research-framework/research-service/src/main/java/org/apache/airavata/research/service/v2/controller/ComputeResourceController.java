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
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.apache.airavata.research.service.v2.entity.ComputeResource;
import org.apache.airavata.research.service.v2.enums.PrivacyEnumV2;
import org.apache.airavata.research.service.v2.enums.StateEnumV2;
import org.apache.airavata.research.service.v2.repository.ComputeResourceRepository;
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
@RequestMapping("/api/v2/rf/compute-resources")
@Tag(name = "Compute Resources V2", description = "V2 API for managing compute infrastructure resources")
public class ComputeResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResourceController.class);
    private static final PrivacyEnumV2 PUBLIC_PRIVACY = PrivacyEnumV2.PUBLIC;
    private static final StateEnumV2 ACTIVE_STATE = StateEnumV2.ACTIVE;

    private final ComputeResourceRepository computeResourceRepository;

    public ComputeResourceController(ComputeResourceRepository computeResourceRepository) {
        this.computeResourceRepository = computeResourceRepository;
    }

    @Operation(summary = "Get all public compute resources with pagination")
    @GetMapping("/public")
    public ResponseEntity<Page<ComputeResource>> getComputeResources(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "nameSearch", required = false) String nameSearch,
            @RequestParam(value = "tag", required = false) String[] tags) {
        
        LOGGER.info("Getting compute resources - page: {}, size: {}, search: {}", pageNumber, pageSize, nameSearch);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<ComputeResource> resources;
        
        if (nameSearch != null && !nameSearch.trim().isEmpty()) {
            resources = computeResourceRepository.findByNameSearchAndPrivacyAndState(nameSearch, PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        } else {
            resources = computeResourceRepository.findByPrivacyAndState(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        }
        
        LOGGER.info("Found {} compute resources", resources.getTotalElements());
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get compute resource by ID")
    @GetMapping("/public/{id}")
    public ResponseEntity<ComputeResource> getComputeResourceById(@PathVariable("id") String id) {
        LOGGER.info("Getting compute resource by ID: {}", id);
        
        Optional<ComputeResource> resource = computeResourceRepository.findById(id);
        if (resource.isPresent()) {
            return ResponseEntity.ok(resource.get());
        } else {
            LOGGER.warn("Compute resource not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new compute resource")
    @PostMapping("/")
    public ResponseEntity<?> createComputeResource(@Valid @RequestBody ComputeResource computeResource, BindingResult bindingResult) {
        LOGGER.info("Creating new compute resource: {}", computeResource.getName());
        
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
            if (computeResource.getCpuCores() == null) {
                computeResource.setCpuCores(1); // Default to 1 core
            }
            if (computeResource.getMemoryGB() == null) {
                computeResource.setMemoryGB(1); // Default to 1 GB
            }
            if (computeResource.getPrivacy() == null) {
                computeResource.setPrivacy(PUBLIC_PRIVACY);
            }
            if (computeResource.getState() == null) {
                computeResource.setState(ACTIVE_STATE);
            }
            
            ComputeResource savedResource = computeResourceRepository.save(computeResource);
            LOGGER.info("Created compute resource with ID: {}", savedResource.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResource);
        } catch (Exception e) {
            LOGGER.error("Error creating compute resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating compute resource: " + e.getMessage());
        }
    }

    @Operation(summary = "Update compute resource")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComputeResource(@PathVariable("id") String id, @Valid @RequestBody ComputeResource computeResource, BindingResult bindingResult) {
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
        
        try {
            Optional<ComputeResource> existingResource = computeResourceRepository.findById(id);
            if (!existingResource.isPresent()) {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Set the ID to ensure we update the correct resource
            computeResource.setId(id);
            
            // Preserve creation timestamp
            computeResource.setCreatedAt(existingResource.get().getCreatedAt());
            
            ComputeResource updatedResource = computeResourceRepository.save(computeResource);
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
    public ResponseEntity<?> deleteComputeResource(@PathVariable("id") String id) {
        LOGGER.info("Deleting compute resource with ID: {}", id);
        
        try {
            Optional<ComputeResource> existingResource = computeResourceRepository.findById(id);
            if (!existingResource.isPresent()) {
                LOGGER.warn("Compute resource not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            computeResourceRepository.deleteById(id);
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
    public ResponseEntity<List<ComputeResource>> searchComputeResources(
            @RequestParam(value = "keyword") String keyword) {
        
        LOGGER.info("Searching compute resources with keyword: {}", keyword);
        
        List<ComputeResource> resources = computeResourceRepository
                .findByNameContainingIgnoreCaseAndPrivacyAndState(keyword, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} compute resources matching keyword: {}", resources.size(), keyword);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get compute resources by type")
    @GetMapping("/type/{computeType}")
    public ResponseEntity<List<ComputeResource>> getComputeResourcesByType(
            @PathVariable("computeType") String computeType) {
        
        LOGGER.info("Getting compute resources by type: {}", computeType);
        
        List<ComputeResource> resources = computeResourceRepository
                .findByComputeTypeAndPrivacyAndState(computeType, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} compute resources of type: {}", resources.size(), computeType);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Star/unstar a compute resource")
    @PostMapping("/{id}/star")
    public ResponseEntity<Boolean> starComputeResource(@PathVariable("id") String id) {
        LOGGER.info("Starring compute resource with ID: {}", id);
        // For now, just return true - starring functionality can be implemented later
        return ResponseEntity.ok(true);
    }

    @Operation(summary = "Check if user starred a compute resource")
    @GetMapping("/{id}/star")
    public ResponseEntity<Boolean> checkComputeResourceStarred(@PathVariable("id") String id) {
        LOGGER.info("Checking if compute resource is starred: {}", id);
        // For now, just return false - starring functionality can be implemented later
        return ResponseEntity.ok(false);
    }

    @Operation(summary = "Get compute resource star count")
    @GetMapping("/{id}/stars/count")
    public ResponseEntity<Long> getComputeResourceStarCount(@PathVariable("id") String id) {
        LOGGER.info("Getting star count for compute resource: {}", id);
        // For now, just return 0 - starring functionality can be implemented later
        return ResponseEntity.ok(0L);
    }
}