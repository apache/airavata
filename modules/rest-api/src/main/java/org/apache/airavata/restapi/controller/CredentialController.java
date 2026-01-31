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

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.security.CredentialStoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class CredentialController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CredentialController.class);
    
    private final CredentialStoreService credentialStoreService;
    private final AuthorizationService authorizationService;

    public CredentialController(CredentialStoreService credentialStoreService, AuthorizationService authorizationService) {
        this.credentialStoreService = credentialStoreService;
        this.authorizationService = authorizationService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    /**
     * Resolve effective owner ID (userId@gatewayId) from auth token when request does not provide userId.
     * Uses claims "userId" then "userName" to align with JWT extraction.
     */
    private String resolveOwnerIdFromToken(AuthzToken authzToken, String scopedGatewayId) {
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
        return uid.endsWith("@" + scopedGatewayId) ? uid : (uid + "@" + scopedGatewayId);
    }

    /**
     * Get credential summaries for a gateway.
     * Use scope=owned to return only credentials owned by the current user (avoids path conflict with /credential-summaries/{token}).
     * Performance optimized: single database call when no type filter is specified.
     */
    @GetMapping("/credential-summaries")
    public ResponseEntity<?> getCredentialSummaries(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) SummaryType type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String userId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

            // scope=owned: list credentials owned by user (same as previous /owned-by-user path; avoids /{token} conflict)
            if ("owned".equalsIgnoreCase(scope != null ? scope.trim() : "")) {
                String ownerId;
                if (userId != null && !userId.isEmpty()) {
                    ownerId = userId.endsWith("@" + scopedGatewayId) ? userId : (userId + "@" + scopedGatewayId);
                } else {
                    ownerId = resolveOwnerIdFromToken(authzToken, scopedGatewayId);
                    if (ownerId == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "userId is required when not available from token"));
                    }
                }
                List<CredentialSummary> owned = credentialStoreService.getCredentialSummariesForUser(scopedGatewayId, ownerId);
                return ResponseEntity.ok(owned);
            }

            logger.debug("Getting credential summaries for gateway: {}, type: {}", scopedGatewayId, type);
            List<CredentialSummary> allSummaries;
            if (type != null) {
                allSummaries = credentialStoreService.getAllCredentialSummaries(type, null, scopedGatewayId);
            } else {
                allSummaries = credentialStoreService.getAllCredentialSummariesCombined(null, scopedGatewayId);
            }
            logger.debug("Returning {} total credential summaries for gateway {}", allSummaries.size(), scopedGatewayId);
            return ResponseEntity.ok(allSummaries);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (CredentialStoreException e) {
            logger.error("Error getting credential summaries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error getting credential summaries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get a specific credential summary
     */
    @GetMapping("/credential-summaries/{token}")
    public ResponseEntity<?> getCredentialSummary(
            @PathVariable String token,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            // Avoid path conflict: list-owned is GET /credential-summaries?scope=owned; these are not tokens
            if ("owned".equals(token) || "owned-by-user".equals(token)) {
                return ResponseEntity.notFound().build();
            }
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID - extract from token if not provided
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            var summary = credentialStoreService.getCredentialSummary(token, scopedGatewayId);
            if (summary == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the credential's gateway
            authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);
            
            return ResponseEntity.ok(summary);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Add SSH credential.
     * If userId and gatewayId are present, ownerId is set to userId@gatewayId for access-control.
     * When body userId is missing, derives from auth token so owned listing and creation stay aligned.
     */
    @PostMapping("/credentials/ssh")
    public ResponseEntity<?> addSSHCredential(@RequestBody SSHCredential sshCredential,
            HttpServletRequest request) {
        try {
            String gid = sshCredential.getGatewayId();
            if (gid == null || gid.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "gatewayId is required"));
            }
            var authzToken = getAuthzToken(request);
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gid);
            String uid = sshCredential.getUserId();
            if (uid == null || uid.isEmpty()) {
                uid = resolveOwnerIdFromToken(authzToken, scopedGatewayId);
                if (uid != null) {
                    sshCredential.setUserId(uid);
                }
            } else {
                String ownerId = uid.endsWith("@" + scopedGatewayId) ? uid : (uid + "@" + scopedGatewayId);
                sshCredential.setUserId(ownerId);
            }
            String token = credentialStoreService.addSSHCredential(sshCredential);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Add password credential.
     * If userId and gatewayId are present, ownerId is set to userId@gatewayId for access-control.
     * When body userId is missing, derives from auth token so owned listing and creation stay aligned.
     */
    @PostMapping("/credentials/password")
    public ResponseEntity<?> addPasswordCredential(@RequestBody PasswordCredential passwordCredential,
            HttpServletRequest request) {
        try {
            String gid = passwordCredential.getGatewayId();
            if (gid == null || gid.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "gatewayId is required"));
            }
            var authzToken = getAuthzToken(request);
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gid);
            String uid = passwordCredential.getUserId();
            if (uid == null || uid.isEmpty()) {
                uid = resolveOwnerIdFromToken(authzToken, scopedGatewayId);
                if (uid != null) {
                    passwordCredential.setUserId(uid);
                }
            } else {
                String ownerId = uid.endsWith("@" + scopedGatewayId) ? uid : (uid + "@" + scopedGatewayId);
                passwordCredential.setUserId(ownerId);
            }
            String token = credentialStoreService.addPasswordCredential(passwordCredential);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get SSH credential
     */
    @GetMapping("/credentials/ssh/{token}")
    public ResponseEntity<?> getSSHCredential(
            @PathVariable String token,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID - extract from token if not provided
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            var credential = credentialStoreService.getSSHCredential(token, scopedGatewayId);
            if (credential == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the credential's gateway
            authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);
            
            return ResponseEntity.ok(credential);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get password credential
     */
    @GetMapping("/credentials/password/{token}")
    public ResponseEntity<?> getPasswordCredential(
            @PathVariable String token,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID - extract from token if not provided
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            var credential = credentialStoreService.getPasswordCredential(token, scopedGatewayId);
            if (credential == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify user has access to the credential's gateway
            authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);
            
            return ResponseEntity.ok(credential);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Delete SSH credential
     */
    @DeleteMapping("/credentials/{token}")
    public ResponseEntity<?> deleteCredential(
            @PathVariable String token,
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID - extract from token if not provided
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            // Verify user has access to the credential's gateway
            authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);
            
            // Try SSH first
            try {
                boolean result = credentialStoreService.deleteSSHCredential(token, scopedGatewayId);
                return ResponseEntity.ok(Map.of("deleted", result));
            } catch (CredentialStoreException e) {
                // If SSH fails, try password
                boolean result = credentialStoreService.deletePWDCredential(token, scopedGatewayId);
                return ResponseEntity.ok(Map.of("deleted", result));
            }
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
