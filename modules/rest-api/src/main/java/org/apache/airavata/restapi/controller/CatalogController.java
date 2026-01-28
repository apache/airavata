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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.CatalogResource;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.CatalogResourceService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
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

/**
 * REST Controller for Research Catalog (Research Framework) resources.
 * 
 * <h2>Resource Scope Model</h2>
 * <p>Resources support a two-level scope model with inferred delegation:</p>
 * <ul>
 *   <li><b>USER</b>: Resources owned by a specific user</li>
 *   <li><b>GATEWAY</b>: Resources owned at gateway level</li>
 *   <li><b>DELEGATED</b>: Resources accessible via group credentials (inferred, not stored)</li>
 * </ul>
 * 
 * <p>When creating resources, only USER or GATEWAY scope can be specified.
 * DELEGATED scope is automatically inferred when returning resources that are
 * accessible via group credentials but not directly owned.</p>
 * 
 * <h2>Resource Types</h2>
 * <p>Only two resource types are supported:</p>
 * <ul>
 *   <li><b>DATASET</b>: Data files and datasets</li>
 *   <li><b>REPOSITORY</b>: Code repositories, notebooks, models, or any version-controlled resource</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rf/resources")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class CatalogController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CatalogController.class);
    
    private final CatalogResourceService catalogResourceService;
    private final GroupResourceProfileService groupResourceProfileService;

    public CatalogController(CatalogResourceService catalogResourceService,
                             GroupResourceProfileService groupResourceProfileService) {
        this.catalogResourceService = catalogResourceService;
        this.groupResourceProfileService = groupResourceProfileService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    private List<String> getAccessibleGroupIds(AuthzToken authzToken, String gatewayId) {
        try {
            // Get accessible group resource profiles for the user
            var groups = groupResourceProfileService.getAllGroupResourceProfiles(
                    gatewayId, 
                    Collections.emptyList() // TODO: Get from sharing registry if enabled
            );
            return groups.stream()
                    .map(g -> g.getGroupResourceProfileId())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.warn("Error getting accessible group IDs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get all public resources with optional filtering
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicResources(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String nameSearch,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            logger.debug("Getting public resources: type={}, nameSearch={}, pageNumber={}, pageSize={}", 
                type, nameSearch, pageNumber, pageSize);
            var resources = catalogResourceService.getPublicResources(type, nameSearch, pageNumber, pageSize);
            logger.debug("Found {} public resources", resources != null ? resources.size() : 0);
            return ResponseEntity.ok(resources != null ? resources : java.util.Collections.emptyList());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid resource type: {}", type, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid resource type: " + e.getMessage()));
        } catch (RegistryException e) {
            logger.error("Registry error getting public resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        } catch (Exception e) {
            logger.error("Unexpected error getting public resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    /**
     * Get a single public resource by ID
     */
    @GetMapping("/public/{resourceId}")
    public ResponseEntity<?> getPublicResource(@PathVariable String resourceId) {
        try {
            var resource = catalogResourceService.getPublicResource(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(resource);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get all public tags
     */
    @GetMapping("/public/tags/all")
    public ResponseEntity<?> getAllPublicTags() {
        try {
            logger.debug("Getting all public tags");
            var tags = catalogResourceService.getAllPublicTags();
            logger.debug("Found {} public tags", tags != null ? tags.size() : 0);
            return ResponseEntity.ok(tags != null ? tags : java.util.Collections.emptyList());
        } catch (RegistryException e) {
            logger.error("Registry error getting public tags: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        } catch (Exception e) {
            logger.error("Unexpected error getting public tags: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    /**
     * Search public resources
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchResources(
            @RequestParam String query, @RequestParam(required = false) String type) {
        try {
            var resources = catalogResourceService.searchResources(query, type);
            return ResponseEntity.ok(resources);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get all accessible resources for the authenticated user.
     * 
     * <p>Returns resources with the following scopes:</p>
     * <ul>
     *   <li>USER: Resources owned by the user (scope=USER, ownerId=userId)</li>
     *   <li>GATEWAY: Resources owned at gateway level (scope=GATEWAY, gatewayId matches)</li>
     *   <li>DELEGATED: Resources accessible via group credentials (inferred automatically)</li>
     * </ul>
     * 
     * @param request HTTP request (for authentication context)
     * @param type Optional resource type filter (DATASET or REPOSITORY)
     * @param nameSearch Optional name search filter
     * @param pageNumber Page number (0-indexed)
     * @param pageSize Page size
     * @return List of accessible resources with inferred scope
     */
    @GetMapping
    public ResponseEntity<?> getAllResources(
            HttpServletRequest request,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String nameSearch,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            var authzToken = getAuthzToken(request);
            String userId = authzToken.getClaimsMap().get("userId");
            String gatewayId = authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID);
            List<String> groupIds = getAccessibleGroupIds(authzToken, gatewayId);
            
            var resources = catalogResourceService.getAccessibleResources(
                    userId, gatewayId, groupIds, type, nameSearch, pageNumber, pageSize);
            return ResponseEntity.ok(resources);
        } catch (RegistryException e) {
            logger.error("Error getting accessible resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting accessible resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Get a single resource by ID (authenticated)
     */
    @GetMapping("/{resourceId}")
    public ResponseEntity<?> getResource(@PathVariable String resourceId) {
        try {
            var resource = catalogResourceService.getResource(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(resource);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Create a new resource.
     * 
     * <p>Scope must be either USER or GATEWAY. DELEGATED cannot be set directly
     * as it is inferred automatically for resources accessible via groups.</p>
     * 
     * <p>For USER scope:</p>
     * <ul>
     *   <li>ownerId is automatically set to the authenticated user's ID</li>
     *   <li>groupResourceProfileId can be optionally set for delegation tracking</li>
     * </ul>
     * 
     * <p>For GATEWAY scope:</p>
     * <ul>
     *   <li>ownerId is set to null</li>
     *   <li>Only gateway admins can create gateway-level resources</li>
     *   <li>groupResourceProfileId can be optionally set for delegation tracking</li>
     * </ul>
     * 
     * @param request HTTP request (for authentication context)
     * @param resource Resource to create (scope must be USER or GATEWAY)
     * @return Created resource ID
     */
    @PostMapping
    public ResponseEntity<?> createResource(
            HttpServletRequest request,
            @RequestBody CatalogResource resource) {
        try {
            var authzToken = getAuthzToken(request);
            String userId = authzToken.getClaimsMap().get("userId");
            String gatewayId = authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID);
            
            // Set scope and ownership based on request
            // Only USER and GATEWAY are valid scopes (DELEGATED is inferred)
            if (resource.getScope() == null || resource.getScope().isEmpty()) {
                resource.setScope("USER"); // Default to USER scope
            }
            
            // Validate scope
            if (!"USER".equals(resource.getScope()) && !"GATEWAY".equals(resource.getScope())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Scope must be USER or GATEWAY. DELEGATED is inferred automatically."));
            }
            
            // Set gateway and owner based on scope
            resource.setGatewayId(gatewayId);
            if ("USER".equals(resource.getScope())) {
                resource.setOwnerId(userId);
                // groupResourceProfileId can be set for delegation tracking, but scope remains USER
            } else if ("GATEWAY".equals(resource.getScope())) {
                // Gateway admins can create gateway-level resources
                resource.setOwnerId(null);
                // groupResourceProfileId can be set for delegation tracking, but scope remains GATEWAY
            }
            
            String resourceId = catalogResourceService.createResource(resource);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", resourceId));
        } catch (RegistryException e) {
            logger.error("Error creating resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating resource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Update a resource
     */
    @PutMapping("/{resourceId}")
    public ResponseEntity<?> updateResource(@PathVariable String resourceId, @RequestBody CatalogResource resource) {
        try {
            catalogResourceService.updateResource(resourceId, resource);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Delete a resource
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable String resourceId) {
        try {
            catalogResourceService.deleteResource(resourceId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Star/unstar a resource (placeholder - would need user context)
     */
    @PostMapping("/{resourceId}/star")
    public ResponseEntity<?> starResource(@PathVariable String resourceId) {
        // This would need user context to track starred resources
        // For now, return success
        return ResponseEntity.ok().build();
    }

    /**
     * Check if resource is starred (placeholder)
     */
    @GetMapping("/{resourceId}/star")
    public ResponseEntity<?> checkStarred(@PathVariable String resourceId) {
        // This would need user context to check starred resources
        return ResponseEntity.ok(Map.of("starred", false));
    }

    /**
     * Get user's starred resources (placeholder)
     */
    @GetMapping("/{userEmail}/stars")
    public ResponseEntity<?> getStarredResources(@PathVariable String userEmail) {
        // This would need implementation of user-resource star mapping
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}
