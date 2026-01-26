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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for resource access management.
 * Provides endpoints for managing access grants at different levels (GATEWAY, GROUP, USER).
 */
@RestController
@RequestMapping("/api/v1/resource-access")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Resource Access", description = "Resource access management API")
public class ResourceAccessController {

    private final ResourceAccessRepository resourceAccessRepository;

    public ResourceAccessController(ResourceAccessRepository resourceAccessRepository) {
        this.resourceAccessRepository = resourceAccessRepository;
    }

    /**
     * Get all access grants for a resource.
     */
    @GetMapping
    @Operation(summary = "Get access grants for a resource")
    public ResponseEntity<?> getAccessGrants(
            @RequestParam PreferenceResourceType resourceType,
            @RequestParam String resourceId) {
        try {
            var grants = resourceAccessRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
            return ResponseEntity.ok(grants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get accessible resources for a user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get accessible resources for a user")
    public ResponseEntity<?> getAccessibleResources(
            @PathVariable String userId,
            @RequestParam String gatewayId,
            @RequestParam PreferenceResourceType resourceType,
            @RequestParam(required = false) String groupIds) {
        try {
            String airavataInternalUserId = userId + "@" + gatewayId;
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            var resourceIds = resourceAccessRepository.findAccessibleResourceIds(
                    resourceType, gatewayId, groupIdList, airavataInternalUserId);
            return ResponseEntity.ok(Map.of("resourceIds", resourceIds));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create or update an access grant.
     */
    @PostMapping
    @Operation(summary = "Create or update an access grant")
    public ResponseEntity<?> createAccessGrant(@RequestBody AccessGrantRequest request) {
        try {
            // Check if grant already exists
            var existing = resourceAccessRepository.findByResourceTypeAndResourceIdAndOwnerIdAndOwnerType(
                    request.resourceType(), request.resourceId(), request.ownerId(), request.ownerType());

            ResourceAccessEntity entity;
            if (existing.isPresent()) {
                entity = existing.get();
                if (request.credentialToken() != null) {
                    entity.setCredentialToken(request.credentialToken());
                }
                entity.setEnabled(request.enabled() != null ? request.enabled() : true);
            } else {
                entity = new ResourceAccessEntity();
                entity.setResourceType(request.resourceType());
                entity.setResourceId(request.resourceId());
                entity.setOwnerId(request.ownerId());
                entity.setOwnerType(request.ownerType());
                entity.setGatewayId(request.gatewayId());
                entity.setCredentialToken(request.credentialToken());
                entity.setEnabled(request.enabled() != null ? request.enabled() : true);
            }

            var saved = resourceAccessRepository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an existing access grant.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an access grant")
    public ResponseEntity<?> updateAccessGrant(
            @PathVariable Long id,
            @RequestBody AccessGrantUpdateRequest request) {
        try {
            var existing = resourceAccessRepository.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ResourceAccessEntity entity = existing.get();
            if (request.credentialToken() != null) {
                entity.setCredentialToken(request.credentialToken());
            }
            if (request.enabled() != null) {
                entity.setEnabled(request.enabled());
            }

            var saved = resourceAccessRepository.save(entity);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete an access grant.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an access grant")
    public ResponseEntity<?> deleteAccessGrant(@PathVariable Long id) {
        try {
            if (!resourceAccessRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            resourceAccessRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get access grants by owner.
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get access grants by owner")
    public ResponseEntity<?> getAccessGrantsByOwner(
            @PathVariable String ownerId,
            @RequestParam PreferenceLevel ownerType) {
        try {
            var grants = resourceAccessRepository.findByOwnerIdAndOwnerType(ownerId, ownerType);
            return ResponseEntity.ok(grants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get enabled access grants for a resource.
     */
    @GetMapping("/enabled")
    @Operation(summary = "Get enabled access grants for a resource")
    public ResponseEntity<?> getEnabledAccessGrants(
            @RequestParam PreferenceResourceType resourceType,
            @RequestParam String resourceId) {
        try {
            var grants = resourceAccessRepository.findByResourceTypeAndResourceIdAndEnabledTrue(
                    resourceType, resourceId);
            return ResponseEntity.ok(grants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Request records
    public record AccessGrantRequest(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel ownerType,
            String gatewayId,
            String credentialToken,
            Boolean enabled) {}

    public record AccessGrantUpdateRequest(
            String credentialToken,
            Boolean enabled) {}
}
