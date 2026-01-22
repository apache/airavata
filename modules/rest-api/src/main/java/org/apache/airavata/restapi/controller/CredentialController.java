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
     * Get all credential summaries for a gateway.
     * Performance optimized: uses single database call when no type filter is specified.
     */
    @GetMapping("/credential-summaries")
    public ResponseEntity<?> getCredentialSummaries(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) SummaryType type,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway ID - extract from token if not provided
            String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);
            
            logger.debug("Getting credential summaries for gateway: {}, type: {}", scopedGatewayId, type);
            
            List<CredentialSummary> allSummaries;
            
            // If type is specified, only get that type; otherwise get all types in a single DB call
            if (type != null) {
                allSummaries = credentialStoreService.getAllCredentialSummaries(type, null, scopedGatewayId);
            } else {
                // Optimized: single database call to get all credential types
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
     * Add SSH credential
     */
    @PostMapping("/credentials/ssh")
    public ResponseEntity<?> addSSHCredential(@RequestBody SSHCredential sshCredential) {
        try {
            String token = credentialStoreService.addSSHCredential(sshCredential);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Add password credential
     */
    @PostMapping("/credentials/password")
    public ResponseEntity<?> addPasswordCredential(@RequestBody PasswordCredential passwordCredential) {
        try {
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
