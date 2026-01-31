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
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.ResourceAccessGrant;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.apache.airavata.registry.services.ResourceAccessGrantService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.security.model.AuthzToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

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
    private final CredentialStoreService credentialStoreService;
    private final ResourceAccessGrantService resourceAccessGrantService;

    public ResourceAccessController(
            ResourceAccessRepository resourceAccessRepository,
            CredentialStoreService credentialStoreService,
            ResourceAccessGrantService resourceAccessGrantService) {
        this.resourceAccessRepository = resourceAccessRepository;
        this.credentialStoreService = credentialStoreService;
        this.resourceAccessGrantService = resourceAccessGrantService;
    }

    /**
     * Resolve effective user from auth token when request does not provide userId.
     * Returns a two-element array: [effectiveUserId (for display), airavataInternalUserId (for DB lookup)].
     */
    private String[] resolveEffectiveUserFromToken(HttpServletRequest request, String gatewayId) {
        AuthzToken authzToken = (AuthzToken) request.getAttribute("authzToken");
        if (authzToken == null || authzToken.getClaimsMap() == null) {
            return null;
        }
        String uid = (String) authzToken.getClaimsMap().get("userId");
        if (uid == null || uid.isEmpty()) {
            uid = (String) authzToken.getClaimsMap().get("userName");
        }
        if (uid == null || uid.isEmpty()) {
            return null;
        }
        String airavataInternalUserId = uid.endsWith("@" + gatewayId) ? uid : (uid + "@" + gatewayId);
        return new String[] { uid, airavataInternalUserId };
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
            // Validate required fields
            if (request.resourceType() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "resourceType is required"));
            }
            if (request.resourceId() == null || request.resourceId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "resourceId is required"));
            }
            if (request.ownerId() == null || request.ownerId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ownerId is required"));
            }
            if (request.ownerType() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ownerType is required"));
            }
            if (request.gatewayId() == null || request.gatewayId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "gatewayId is required"));
            }
            // Credential token is required (NOT NULL in database)
            if (request.credentialToken() == null || request.credentialToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "credentialToken is required. Please select a credential."));
            }

            if (!credentialStoreService.credentialExists(request.credentialToken().trim(), request.gatewayId().trim())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Credential does not exist for the given gateway and token."));
            }

            // Check if grant already exists
            var existing = resourceAccessRepository.findByResourceTypeAndResourceIdAndOwnerIdAndOwnerType(
                    request.resourceType(), request.resourceId(), request.ownerId(), request.ownerType());

            ResourceAccessEntity entity;
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setCredentialToken(request.credentialToken().trim());
                entity.setEnabled(request.enabled() != null ? request.enabled() : true);
                if (request.loginUsername() != null) {
                    entity.setLoginUsername(request.loginUsername().trim().isEmpty() ? null : request.loginUsername().trim());
                }
            } else {
                entity = new ResourceAccessEntity();
                entity.setResourceType(request.resourceType());
                entity.setResourceId(request.resourceId().trim());
                entity.setOwnerId(request.ownerId().trim());
                entity.setOwnerType(request.ownerType());
                entity.setGatewayId(request.gatewayId().trim());
                entity.setCredentialToken(request.credentialToken().trim());
                entity.setEnabled(request.enabled() != null ? request.enabled() : true);
                entity.setLoginUsername(request.loginUsername() != null && !request.loginUsername().trim().isEmpty()
                        ? request.loginUsername().trim() : null);
            }

            var saved = resourceAccessRepository.save(entity);
            if (request.resourceType() == PreferenceResourceType.COMPUTE && request.resourceId() != null && !request.resourceId().trim().isEmpty()) {
                var existingRag = resourceAccessGrantService.getByGatewayCredentialAndResource(
                        request.gatewayId().trim(), request.credentialToken().trim(), request.resourceId().trim());
                if (existingRag.isPresent()) {
                    var rag = existingRag.get();
                    rag.setLoginUsername(request.loginUsername() != null && !request.loginUsername().trim().isEmpty()
                            ? request.loginUsername().trim() : null);
                    rag.setEnabled(request.enabled() != null ? request.enabled() : true);
                    resourceAccessGrantService.update(rag.getId(), rag);
                } else {
                    var grant = new ResourceAccessGrant();
                    grant.setGatewayId(request.gatewayId().trim());
                    grant.setCredentialToken(request.credentialToken().trim());
                    grant.setComputeResourceId(request.resourceId().trim());
                    grant.setLoginUsername(request.loginUsername() != null && !request.loginUsername().trim().isEmpty()
                            ? request.loginUsername().trim() : null);
                    grant.setEnabled(request.enabled() != null ? request.enabled() : true);
                    resourceAccessGrantService.create(grant);
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid credential"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String errorMsg = "Database constraint violation: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("uk_resource_access")) {
                errorMsg = "An access grant already exists for this resource, owner, and owner type combination.";
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", errorMsg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    /**
     * Update an existing access grant.
     * For COMPUTE grants, keeps ResourceAccessGrant in sync.
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
            String oldCredentialToken = entity.getCredentialToken();
            if (request.credentialToken() != null) {
                if (!credentialStoreService.credentialExists(request.credentialToken(), entity.getGatewayId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Credential does not exist for the given gateway and token."));
                }
                entity.setCredentialToken(request.credentialToken());
            }
            if (request.enabled() != null) {
                entity.setEnabled(request.enabled());
            }
            if (request.loginUsername() != null) {
                entity.setLoginUsername(request.loginUsername().trim().isEmpty() ? null : request.loginUsername().trim());
            }

            var saved = resourceAccessRepository.save(entity);

            if (saved.getResourceType() == PreferenceResourceType.COMPUTE && saved.getResourceId() != null && !saved.getResourceId().trim().isEmpty()) {
                String gatewayId = saved.getGatewayId();
                String resourceId = saved.getResourceId();
                String newToken = saved.getCredentialToken();
                var existingRag = resourceAccessGrantService.getByGatewayCredentialAndResource(gatewayId, newToken, resourceId);
                if (existingRag.isPresent()) {
                    var rag = existingRag.get();
                    rag.setLoginUsername(saved.getLoginUsername());
                    rag.setEnabled(saved.isEnabled());
                    resourceAccessGrantService.update(rag.getId(), rag);
                } else {
                    var grant = new ResourceAccessGrant();
                    grant.setGatewayId(gatewayId);
                    grant.setCredentialToken(newToken);
                    grant.setComputeResourceId(resourceId);
                    grant.setLoginUsername(saved.getLoginUsername());
                    grant.setEnabled(saved.isEnabled());
                    resourceAccessGrantService.create(grant);
                }
                if (oldCredentialToken != null && !oldCredentialToken.equals(newToken)) {
                    resourceAccessGrantService.getByGatewayCredentialAndResource(gatewayId, oldCredentialToken, resourceId)
                            .ifPresent(rag -> resourceAccessGrantService.delete(rag.getId()));
                }
            }
            return ResponseEntity.ok(saved);
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid credential"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete an access grant.
     * For COMPUTE grants, also removes the corresponding ResourceAccessGrant.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an access grant")
    public ResponseEntity<?> deleteAccessGrant(@PathVariable Long id) {
        try {
            var existing = resourceAccessRepository.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            ResourceAccessEntity entity = existing.get();
            if (entity.getResourceType() == PreferenceResourceType.COMPUTE && entity.getResourceId() != null && !entity.getResourceId().trim().isEmpty()) {
                resourceAccessGrantService.getByGatewayCredentialAndResource(
                        entity.getGatewayId(), entity.getCredentialToken(), entity.getResourceId())
                        .ifPresent(rag -> resourceAccessGrantService.delete(rag.getId()));
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

    /**
     * Get all access grants for a gateway and resource type.
     */
    @GetMapping("/by-type")
    @Operation(summary = "Get all access grants for a resource type")
    public ResponseEntity<?> getAccessGrantsByType(
            @RequestParam String gatewayId,
            @RequestParam PreferenceResourceType resourceType) {
        try {
            var grants = resourceAccessRepository.findByGatewayIdAndResourceType(gatewayId, resourceType);
            return ResponseEntity.ok(grants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Debug endpoint for access-control: returns gatewayId, userId, airavataInternalUserId,
     * ownedTokenIds, ownedCount. Useful to verify DB storage and API output.
     */
    @GetMapping("/access-control/debug")
    @Operation(summary = "Debug access-control: owned tokens and counts")
    public ResponseEntity<?> getAccessControlDebug(
            @RequestParam String gatewayId,
            @RequestParam String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            String airavataInternalUserId = userId + "@" + gatewayId;
            List<String> ownedTokenIds = List.of();
            try {
                var owned = credentialStoreService.getCredentialSummariesForUser(gatewayId, airavataInternalUserId);
                ownedTokenIds = owned.stream().map(CredentialSummary::getToken).toList();
            } catch (CredentialStoreException e) {
                return ResponseEntity.ok(Map.of(
                        "gatewayId", gatewayId,
                        "userId", userId,
                        "airavataInternalUserId", airavataInternalUserId,
                        "ownedTokenIds", List.<String>of(),
                        "ownedCount", 0,
                        "ownedError", e.getMessage()));
            }
            return ResponseEntity.ok(Map.of(
                    "gatewayId", gatewayId,
                    "userId", userId,
                    "airavataInternalUserId", airavataInternalUserId,
                    "ownedTokenIds", ownedTokenIds,
                    "ownedCount", ownedTokenIds.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Unified Access Control endpoint - returns credentials with their associated resources.
     * Shows user-owned credentials and gateway-level inherited credentials.
     */
    @GetMapping("/access-control")
    @Operation(summary = "Get unified access control view with credentials and resources")
    public ResponseEntity<?> getAccessControl(
            @RequestParam String gatewayId,
            @RequestParam(required = false) String userId,
            HttpServletRequest request) {
        try {
            // Get user ID - from parameter or derive from auth token when missing
            String effectiveUserId = userId;
            String airavataInternalUserId;
            if (effectiveUserId != null && !effectiveUserId.isEmpty()) {
                airavataInternalUserId = effectiveUserId.endsWith("@" + gatewayId)
                        ? effectiveUserId
                        : (effectiveUserId + "@" + gatewayId);
            } else {
                String[] resolved = resolveEffectiveUserFromToken(request, gatewayId);
                if (resolved == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "userId is required when not available from token"));
                }
                effectiveUserId = resolved[0];
                airavataInternalUserId = resolved[1];
            }

            // Get user-level resource access grants
            List<ResourceAccessEntity> userAccessGrants = resourceAccessRepository
                    .findByOwnerIdAndOwnerType(airavataInternalUserId, PreferenceLevel.USER);

            // Get gateway-level access grants
            List<ResourceAccessEntity> gatewayAccessGrants = resourceAccessRepository
                    .findByOwnerIdAndOwnerType(gatewayId, PreferenceLevel.GATEWAY);

            // Collect all unique credential tokens from access grants
            Set<String> allCredentialTokens = new HashSet<>();
            userAccessGrants.forEach(grant -> {
                if (grant.getCredentialToken() != null && !grant.getCredentialToken().isEmpty()) {
                    allCredentialTokens.add(grant.getCredentialToken());
                }
            });
            gatewayAccessGrants.forEach(grant -> {
                if (grant.getCredentialToken() != null && !grant.getCredentialToken().isEmpty()) {
                    allCredentialTokens.add(grant.getCredentialToken());
                }
            });
            
            // Get credential summaries for all tokens from grants
            List<CredentialSummary> allCredentials = new ArrayList<>();
            for (String token : allCredentialTokens) {
                try {
                    CredentialSummary cred = credentialStoreService.getCredentialSummary(token, gatewayId);
                    if (cred != null) {
                        allCredentials.add(cred);
                    }
                } catch (Exception e) {
                    // Skip if credential not accessible
                }
            }

            // Also include owned credentials with no grants (e.g. newly created)
            try {
                List<CredentialSummary> ownedOnly =
                        credentialStoreService.getCredentialSummariesForUser(gatewayId, airavataInternalUserId);
                for (CredentialSummary cred : ownedOnly) {
                    String token = cred.getToken();
                    if (!allCredentialTokens.contains(token)) {
                        allCredentialTokens.add(token);
                        allCredentials.add(cred);
                    }
                }
            } catch (CredentialStoreException e) {
                // Skip owned-only fetch on error
            }

            // Combine all access grants
            List<ResourceAccessEntity> allAccessGrants = new ArrayList<>();
            allAccessGrants.addAll(userAccessGrants);
            allAccessGrants.addAll(gatewayAccessGrants);

            // Group access grants by credential token
            Map<String, List<ResourceAccessEntity>> grantsByCredential = allAccessGrants.stream()
                    .filter(ra -> ra.getCredentialToken() != null && !ra.getCredentialToken().isEmpty())
                    .filter(ResourceAccessEntity::isEnabled)
                    .collect(Collectors.groupingBy(ResourceAccessEntity::getCredentialToken));

            // Build credential list with resources
            List<Map<String, Object>> credentialList = new ArrayList<>();
            
            for (CredentialSummary cred : allCredentials) {
                String token = cred.getToken();
                List<ResourceAccessEntity> grants = grantsByCredential.getOrDefault(token, Collections.emptyList());
                
                // Determine ownership and source
                boolean hasUserGrant = userAccessGrants.stream()
                        .anyMatch(grant -> token.equals(grant.getCredentialToken()));
                boolean hasGatewayGrant = gatewayAccessGrants.stream()
                        .anyMatch(grant -> token.equals(grant.getCredentialToken()));
                
                String ownership;
                String source;
                String sourceId;
                
                if (hasUserGrant) {
                    ownership = "OWNED";
                    source = "USER";
                    sourceId = effectiveUserId;
                } else if (hasGatewayGrant) {
                    ownership = "INHERITED";
                    source = "GATEWAY";
                    sourceId = gatewayId;
                } else {
                    ownership = "OWNED";
                    source = "USER";
                    sourceId = effectiveUserId;
                }
                
                Map<String, Object> credentialData = buildCredentialData(
                        cred, grants, ownership, source, sourceId);
                credentialList.add(credentialData);
            }

            return ResponseEntity.ok(Map.of("credentials", credentialList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> buildCredentialData(
            CredentialSummary cred,
            List<ResourceAccessEntity> grants,
            String ownership,
            String source,
            String sourceId) {
        
        List<Map<String, String>> computeResources = new ArrayList<>();
        List<Map<String, String>> storageResources = new ArrayList<>();
        
        for (ResourceAccessEntity grant : grants) {
            Map<String, String> resourceInfo = Map.of(
                    "resourceId", grant.getResourceId(),
                    "loginUsername", grant.getLoginUsername() != null ? grant.getLoginUsername() : ""
            );
            
            if (grant.getResourceType() == PreferenceResourceType.COMPUTE) {
                computeResources.add(resourceInfo);
            } else if (grant.getResourceType() == PreferenceResourceType.STORAGE) {
                storageResources.add(resourceInfo);
            }
        }
        
        String typeStr = cred.getType() != null ? cred.getType().name() : "UNKNOWN";
        String usernameStr = cred.getUsername() != null ? cred.getUsername() : null;
        String nameStr = cred.getName() != null ? cred.getName() : "";
        Map<String, Object> out = new HashMap<>();
        out.put("token", cred.getToken());
        out.put("name", nameStr);
        out.put("username", usernameStr != null ? usernameStr : "");
        out.put("type", typeStr);
        out.put("description", cred.getDescription() != null ? cred.getDescription() : "");
        out.put("persistedTime", cred.getPersistedTime() != null ? cred.getPersistedTime() : 0L);
        out.put("ownership", ownership);
        out.put("source", source);
        out.put("sourceId", sourceId);
        out.put("computeResources", computeResources);
        out.put("storageResources", storageResources);
        return out;
    }

    // Request records
    public record AccessGrantRequest(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel ownerType,
            String gatewayId,
            String credentialToken,
            String loginUsername,
            Boolean enabled) {}

    public record AccessGrantUpdateRequest(
            String credentialToken,
            String loginUsername,
            Boolean enabled) {}
}
