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

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.apache.airavata.registry.services.StorageResourceService;
import org.apache.airavata.security.model.AuthzToken;
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
@RequestMapping("/api/v1/storage-resources")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class StorageResourceController {
    private final StorageResourceService storageResourceService;
    private final ResourceAccessRepository resourceAccessRepository;

    public StorageResourceController(
            StorageResourceService storageResourceService,
            ResourceAccessRepository resourceAccessRepository) {
        this.storageResourceService = storageResourceService;
        this.resourceAccessRepository = resourceAccessRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllStorageResources(
            @RequestParam(required = false) String credentialToken,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var allStorageResources = storageResourceService.getAllStorageResourceIdList();
            
            // If credentialToken is provided, filter by accessible resources for that credential
            if (credentialToken != null && !credentialToken.trim().isEmpty()) {
                var accessGrants = resourceAccessRepository.findByCredentialToken(credentialToken.trim());
                var accessibleResourceIds = accessGrants.stream()
                        .filter(ra -> ra.getResourceType() == PreferenceResourceType.STORAGE && ra.isEnabled())
                        .map(ResourceAccessEntity::getResourceId)
                        .collect(Collectors.toSet());
                
                // Filter the list/map to only include accessible resources
                if (allStorageResources instanceof Map) {
                    Map<String, String> filteredResources = new HashMap<>();
                    @SuppressWarnings("unchecked")
                    Map<String, String> resourcesMap = (Map<String, String>) allStorageResources;
                    for (Map.Entry<String, String> entry : resourcesMap.entrySet()) {
                        if (accessibleResourceIds.contains(entry.getKey())) {
                            filteredResources.put(entry.getKey(), entry.getValue());
                        }
                    }
                    return ResponseEntity.ok(filteredResources);
                } else if (allStorageResources instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> resourcesList = (List<Map<String, Object>>) allStorageResources;
                    List<Map<String, Object>> filteredResources = resourcesList.stream()
                            .filter(resource -> {
                                String resourceId = (String) resource.get("storageResourceId");
                                return resourceId != null && accessibleResourceIds.contains(resourceId);
                            })
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(filteredResources);
                }
            }
            
            // If no credential token, try to filter by user's accessible resources
            // Only filter if there are explicit resource access grants configured for this gateway
            try {
                var authzToken = (AuthzToken) request.getAttribute("authzToken");
                if (authzToken != null && gatewayId != null) {
                    // Check if any resource access grants are configured for this gateway
                    var gatewayAccessGrants = resourceAccessRepository.findByGatewayIdAndResourceType(
                            gatewayId, PreferenceResourceType.STORAGE);
                    
                    // Only apply filtering if there are explicit access grants configured
                    if (!gatewayAccessGrants.isEmpty()) {
                        String userId = authzToken.getClaimsMap() != null ? authzToken.getClaimsMap().get("userName") : null;
                        if (userId != null) {
                            String airavataInternalUserId = userId + "@" + gatewayId;
                            // Get user's accessible resources
                            var accessibleResourceIds = resourceAccessRepository.findAccessibleResourceIds(
                                    PreferenceResourceType.STORAGE,
                                    gatewayId,
                                    Collections.emptyList(), // Get user's group IDs from sharing registry if enabled
                                    airavataInternalUserId
                            );
                            
                            // Filter the list/map to only include accessible resources
                            if (allStorageResources instanceof Map) {
                                Map<String, String> filteredResources = new HashMap<>();
                                @SuppressWarnings("unchecked")
                                Map<String, String> resourcesMap = (Map<String, String>) allStorageResources;
                                for (Map.Entry<String, String> entry : resourcesMap.entrySet()) {
                                    if (accessibleResourceIds.contains(entry.getKey())) {
                                        filteredResources.put(entry.getKey(), entry.getValue());
                                    }
                                }
                                return ResponseEntity.ok(filteredResources);
                            } else if (allStorageResources instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> resourcesList = (List<Map<String, Object>>) allStorageResources;
                                List<Map<String, Object>> filteredResources = resourcesList.stream()
                                        .filter(resource -> {
                                            String resourceId = (String) resource.get("storageResourceId");
                                            return resourceId != null && accessibleResourceIds.contains(resourceId);
                                        })
                                        .collect(Collectors.toList());
                                return ResponseEntity.ok(filteredResources);
                            }
                        }
                    }
                    // No access grants configured - return all resources
                }
            } catch (Exception e) {
                // If filtering fails, return all resources
            }
            
            return ResponseEntity.ok(allStorageResources);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{storageResourceId}")
    public ResponseEntity<?> getStorageResource(@PathVariable String storageResourceId) {
        try {
            var storageResource = storageResourceService.getStorageResource(storageResourceId);
            if (storageResource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(storageResource);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createStorageResource(@RequestBody StorageResourceDescription storageResource) {
        try {
            var storageResourceId = storageResourceService.addStorageResource(storageResource);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("storageResourceId", storageResourceId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{storageResourceId}")
    public ResponseEntity<?> updateStorageResource(
            @PathVariable String storageResourceId, @RequestBody StorageResourceDescription storageResource) {
        try {
            storageResource.setStorageResourceId(storageResourceId);
            storageResourceService.updateStorageResource(storageResourceId, storageResource);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{storageResourceId}")
    public ResponseEntity<?> deleteStorageResource(@PathVariable String storageResourceId) {
        try {
            storageResourceService.removeStorageResource(storageResourceId);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Check if a storage resource exists at higher levels (GATEWAY or GROUP)
     * and return information about where it's defined.
     */
    @GetMapping("/{storageResourceId}/hierarchy")
    public ResponseEntity<?> getResourceHierarchy(
            @PathVariable String storageResourceId,
            @RequestParam String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = (AuthzToken) request.getAttribute("authzToken");
            if (authzToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check if resource exists globally
            var resource = storageResourceService.getStorageResource(storageResourceId);
            if (resource == null) {
                return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "canCreate", true,
                    "level", "NONE"
                ));
            }

            // Check if there are access grants at GATEWAY level for this gateway
            var gatewayAccessGrants = resourceAccessRepository.findByResourceTypeAndResourceId(
                PreferenceResourceType.STORAGE,
                storageResourceId
            ).stream()
            .filter(ra -> ra.getOwnerType() == org.apache.airavata.common.model.PreferenceLevel.GATEWAY 
                      && ra.getOwnerId().equals(gatewayId))
            .collect(Collectors.toList());
            boolean existsAtGateway = !gatewayAccessGrants.isEmpty();

            // Check if there are access grants at GROUP level
            var groupAccessGrants = resourceAccessRepository.findByResourceTypeAndResourceId(
                PreferenceResourceType.STORAGE,
                storageResourceId
            ).stream()
            .filter(ra -> ra.getOwnerType() == org.apache.airavata.common.model.PreferenceLevel.GROUP)
            .collect(Collectors.toList());
            boolean existsAtGroup = !groupAccessGrants.isEmpty();

            String highestLevel = existsAtGateway ? "GATEWAY" : (existsAtGroup ? "GROUP" : "NONE");
            boolean canCreate = highestLevel.equals("NONE");

            return ResponseEntity.ok(Map.of(
                "exists", true,
                "canCreate", canCreate,
                "canOverride", !canCreate,
                "level", highestLevel,
                "resourceId", storageResourceId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
