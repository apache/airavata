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
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.ResourceAccessGrantService;
import org.apache.airavata.registry.services.StorageResourceService;
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
@RequestMapping("/api/v1/compute-resources")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ComputeResourceController {
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceController.class);

    private final ComputeResourceService computeResourceService;
    private final StorageResourceService storageResourceService;
    private final ResourceAccessRepository resourceAccessRepository;
    private final ResourceAccessGrantService resourceAccessGrantService;

    public ComputeResourceController(
            ComputeResourceService computeResourceService,
            StorageResourceService storageResourceService,
            ResourceAccessRepository resourceAccessRepository,
            ResourceAccessGrantService resourceAccessGrantService) {
        this.computeResourceService = computeResourceService;
        this.storageResourceService = storageResourceService;
        this.resourceAccessRepository = resourceAccessRepository;
        this.resourceAccessGrantService = resourceAccessGrantService;
    }

    @GetMapping
    public ResponseEntity<?> getAllComputeResources(
            @RequestParam(required = false) String credentialToken,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var allComputeResources = computeResourceService.getAllComputeResourceIdList();
            
            // If credentialToken is provided, filter by merged resource access and grants
            if (credentialToken != null && !credentialToken.trim().isEmpty()) {
                var accessibleResourceIds = resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken.trim());
                Map<String, String> filteredResources = new HashMap<>();
                for (Map.Entry<String, String> entry : allComputeResources.entrySet()) {
                    if (accessibleResourceIds.contains(entry.getKey())) {
                        filteredResources.put(entry.getKey(), entry.getValue());
                    }
                }
                return ResponseEntity.ok(filteredResources);
            }
            
            // If no credential token, try to filter by user's accessible resources
            // Only filter if there are explicit resource access grants configured for this gateway
            try {
                var authzToken = (AuthzToken) request.getAttribute("authzToken");
                if (authzToken != null && gatewayId != null) {
                    // Check if any resource access grants are configured for this gateway
                    var gatewayAccessGrants = resourceAccessRepository.findByGatewayIdAndResourceType(
                            gatewayId, PreferenceResourceType.COMPUTE);
                    
                    // Only apply filtering if there are explicit access grants configured
                    if (!gatewayAccessGrants.isEmpty()) {
                        String userId = authzToken.getClaimsMap() != null ? authzToken.getClaimsMap().get("userName") : null;
                        if (userId != null) {
                            String airavataInternalUserId = userId + "@" + gatewayId;
                            // Get user's accessible resources
                            var accessibleResourceIds = resourceAccessRepository.findAccessibleResourceIds(
                                    PreferenceResourceType.COMPUTE,
                                    gatewayId,
                                    Collections.emptyList(), // Get user's group IDs from sharing registry if enabled
                                    airavataInternalUserId
                            );
                            
                            // Filter the map to only include accessible resources
                            Map<String, String> filteredResources = new HashMap<>();
                            for (Map.Entry<String, String> entry : allComputeResources.entrySet()) {
                                if (accessibleResourceIds.contains(entry.getKey())) {
                                    filteredResources.put(entry.getKey(), entry.getValue());
                                }
                            }
                            return ResponseEntity.ok(filteredResources);
                        }
                    }
                    // No access grants configured - return all resources
                }
            } catch (Exception e) {
                // If filtering fails, return all resources
            }
            
            return ResponseEntity.ok(allComputeResources);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{computeResourceId}")
    public ResponseEntity<?> getComputeResource(@PathVariable String computeResourceId) {
        try {
            var computeResource = computeResourceService.getComputeResource(computeResourceId);
            if (computeResource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(computeResource);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createComputeResource(@RequestBody ComputeResourceDescription computeResource) {
        try {
            String linkedStorageResourceId = null;

            // If storageProtocol is provided, create a linked storage resource
            String storageProtocol = computeResource.getStorageProtocol();
            if (storageProtocol != null && !storageProtocol.trim().isEmpty()) {
                var storageResource = new StorageResourceDescription();
                storageResource.setHostName(computeResource.getHostName());
                storageResource.setStorageResourceDescription(
                        "Linked storage for compute resource: " + computeResource.getHostName());
                storageResource.setEnabled(true);

                linkedStorageResourceId = storageResourceService.addStorageResource(storageResource);
                logger.info("Created linked storage resource {} for compute resource {}",
                        linkedStorageResourceId, computeResource.getHostName());

                // Set the linked storage resource ID on the compute resource
                computeResource.setLinkedStorageResourceId(linkedStorageResourceId);
            }

            var computeResourceId = computeResourceService.addComputeResource(computeResource);

            var response = new HashMap<String, String>();
            response.put("computeResourceId", computeResourceId);
            if (linkedStorageResourceId != null) {
                response.put("linkedStorageResourceId", linkedStorageResourceId);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{computeResourceId}")
    public ResponseEntity<?> updateComputeResource(
            @PathVariable String computeResourceId, @RequestBody ComputeResourceDescription computeResource) {
        try {
            computeResource.setComputeResourceId(computeResourceId);
            computeResourceService.updateComputeResource(computeResourceId, computeResource);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{computeResourceId}")
    public ResponseEntity<?> deleteComputeResource(@PathVariable String computeResourceId) {
        try {
            computeResourceService.removeComputeResource(computeResourceId);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Check if a compute resource exists at higher levels (GATEWAY or GROUP)
     * and return information about where it's defined.
     */
    @GetMapping("/{computeResourceId}/hierarchy")
    public ResponseEntity<?> getResourceHierarchy(
            @PathVariable String computeResourceId,
            @RequestParam String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = (AuthzToken) request.getAttribute("authzToken");
            if (authzToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check if resource exists globally
            var resource = computeResourceService.getComputeResource(computeResourceId);
            if (resource == null) {
                return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "canCreate", true,
                    "level", "NONE"
                ));
            }

            // Check if there are access grants at GATEWAY level for this gateway
            var gatewayAccessGrants = resourceAccessRepository.findByResourceTypeAndResourceId(
                PreferenceResourceType.COMPUTE,
                computeResourceId
            ).stream()
            .filter(ra -> ra.getOwnerType() == org.apache.airavata.common.model.PreferenceLevel.GATEWAY 
                      && ra.getOwnerId().equals(gatewayId))
            .collect(Collectors.toList());
            boolean existsAtGateway = !gatewayAccessGrants.isEmpty();

            // Check if there are access grants at GROUP level
            var groupAccessGrants = resourceAccessRepository.findByResourceTypeAndResourceId(
                PreferenceResourceType.COMPUTE,
                computeResourceId
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
                "resourceId", computeResourceId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
