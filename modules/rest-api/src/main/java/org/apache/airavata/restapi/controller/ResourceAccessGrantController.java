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
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.ResourceAccessGrant;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.registry.services.ResourceAccessGrantService;
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

/**
 * REST API for unified resource access grants (credential + compute resource + deployment settings).
 */
@RestController
@RequestMapping("/api/v1/resource-access-grants")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Resource Access Grants", description = "Unified resource access grant API")
public class ResourceAccessGrantController {

    private final ResourceAccessGrantService resourceAccessGrantService;

    public ResourceAccessGrantController(ResourceAccessGrantService resourceAccessGrantService) {
        this.resourceAccessGrantService = resourceAccessGrantService;
    }

    @GetMapping
    @Operation(summary = "List resource access grants")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) String credentialToken,
            @RequestParam(required = false) String computeResourceId,
            @RequestParam(defaultValue = "false") boolean enabledOnly) {
        try {
            List<ResourceAccessGrant> grants;
            if (gatewayId != null && !gatewayId.isBlank()) {
                grants = enabledOnly
                        ? resourceAccessGrantService.getByGatewayEnabled(gatewayId)
                        : resourceAccessGrantService.getByGateway(gatewayId);
            } else if (credentialToken != null && !credentialToken.isBlank()) {
                grants = enabledOnly
                        ? resourceAccessGrantService.getByCredentialEnabled(credentialToken)
                        : resourceAccessGrantService.getByCredential(credentialToken);
            } else if (computeResourceId != null && !computeResourceId.isBlank()) {
                grants = enabledOnly
                        ? resourceAccessGrantService.getByComputeResourceEnabled(computeResourceId)
                        : resourceAccessGrantService.getByComputeResource(computeResourceId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "At least one of gatewayId, credentialToken, or computeResourceId is required"));
            }
            return ResponseEntity.ok(grants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a resource access grant by ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return resourceAccessGrantService.getById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @PostMapping
    @Operation(summary = "Create a resource access grant")
    public ResponseEntity<?> create(@RequestBody ResourceAccessGrant grant) {
        try {
            if (grant.getGatewayId() == null || grant.getGatewayId().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "gatewayId is required"));
            }
            if (grant.getCredentialToken() == null || grant.getCredentialToken().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "credentialToken is required"));
            }
            if (grant.getComputeResourceId() == null || grant.getComputeResourceId().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "computeResourceId is required"));
            }
            ResourceAccessGrant created = resourceAccessGrantService.create(grant);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid credential"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resource access grant")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ResourceAccessGrant grant) {
        try {
            if (!resourceAccessGrantService.exists(id)) {
                return ResponseEntity.notFound().build();
            }
            ResourceAccessGrant updated = resourceAccessGrantService.update(id, grant);
            return ResponseEntity.ok(updated);
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid credential"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource access grant")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (!resourceAccessGrantService.exists(id)) {
                return ResponseEntity.notFound().build();
            }
            resourceAccessGrantService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }
}
