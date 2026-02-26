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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.SharingEntity;
import org.apache.airavata.iam.model.SharingResourceType;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Credentials")
public class CredentialController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CredentialController.class);

    private final CredentialStoreService credentialStoreService;
    private final AuthorizationService authorizationService;
    private final SharingService sharingService;

    public CredentialController(
            CredentialStoreService credentialStoreService,
            AuthorizationService authorizationService,
            SharingService sharingService) {
        this.credentialStoreService = credentialStoreService;
        this.authorizationService = authorizationService;
        this.sharingService = sharingService;
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
            HttpServletRequest request)
            throws CredentialStoreException {
        var authzToken = getAuthzToken(request);
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        // scope=owned: list credentials owned by user (same as previous /owned-by-user path; avoids /{token}
        // conflict)
        if ("owned".equalsIgnoreCase(scope != null ? scope.trim() : "")) {
            String ownerId;
            if (userId != null && !userId.isEmpty()) {
                ownerId = userId.endsWith("@" + scopedGatewayId) ? userId : (userId + "@" + scopedGatewayId);
            } else {
                ownerId = resolveOwnerIdFromToken(authzToken, scopedGatewayId);
                if (ownerId == null) {
                    throw new InvalidRequestException("userId is required when not available from token");
                }
            }
            List<CredentialSummary> owned =
                    credentialStoreService.getCredentialSummariesForUser(scopedGatewayId, ownerId);
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
    }

    /**
     * Get a specific credential summary
     */
    @GetMapping("/credential-summaries/{token}")
    public ResponseEntity<?> getCredentialSummary(
            @PathVariable String token, @RequestParam(required = false) String gatewayId, HttpServletRequest request)
            throws CredentialStoreException {
        // Avoid path conflict: list-owned is GET /credential-summaries?scope=owned; these are not tokens
        if ("owned".equals(token) || "owned-by-user".equals(token)) {
            throw new ResourceNotFoundException("CredentialSummary", token);
        }
        var authzToken = getAuthzToken(request);

        // Validate and scope gateway ID - extract from token if not provided
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        var summary = credentialStoreService.getCredentialSummary(token, scopedGatewayId);
        if (summary == null) {
            throw new ResourceNotFoundException("CredentialSummary", token);
        }

        // Verify user has access to the credential's gateway
        authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);

        return ResponseEntity.ok(summary);
    }

    /**
     * Add SSH credential.
     * If userId and gatewayId are present, ownerId is set to userId@gatewayId for access-control.
     * When body userId is missing, derives from auth token so owned listing and creation stay aligned.
     */
    @PostMapping("/credentials/ssh")
    public ResponseEntity<?> addSSHCredential(@RequestBody SSHCredential sshCredential, HttpServletRequest request)
            throws Exception {
        String gid = sshCredential.getGatewayId();
        if (gid == null || gid.isEmpty()) {
            throw new InvalidRequestException("gatewayId is required");
        }
        var authzToken = getAuthzToken(request);
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gid);
        String userName = (String) authzToken.getClaimsMap().get(Constants.USER_NAME);
        if (userName == null) {
            userName = (String) authzToken.getClaimsMap().get("userId");
        }
        String token = generateAndRegisterSSHKeys(scopedGatewayId, userName, sshCredential.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
    }

    /**
     * Add password credential.
     * If userId and gatewayId are present, ownerId is set to userId@gatewayId for access-control.
     * When body userId is missing, derives from auth token so owned listing and creation stay aligned.
     */
    @PostMapping("/credentials/password")
    public ResponseEntity<?> addPasswordCredential(
            @RequestBody PasswordCredential passwordCredential, HttpServletRequest request)
            throws CredentialStoreException {
        String gid = passwordCredential.getGatewayId();
        if (gid == null || gid.isEmpty()) {
            throw new InvalidRequestException("gatewayId is required");
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
    }

    /**
     * Get SSH credential
     */
    @GetMapping("/credentials/ssh/{token}")
    public ResponseEntity<?> getSSHCredential(
            @PathVariable String token, @RequestParam(required = false) String gatewayId, HttpServletRequest request)
            throws CredentialStoreException {
        var authzToken = getAuthzToken(request);

        // Validate and scope gateway ID - extract from token if not provided
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        var credential = credentialStoreService.getSSHCredential(token, scopedGatewayId);
        if (credential == null) {
            throw new ResourceNotFoundException("SSHCredential", token);
        }

        // Verify user has access to the credential's gateway
        authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);

        return ResponseEntity.ok(credential);
    }

    /**
     * Get password credential
     */
    @GetMapping("/credentials/password/{token}")
    public ResponseEntity<?> getPasswordCredential(
            @PathVariable String token, @RequestParam(required = false) String gatewayId, HttpServletRequest request)
            throws CredentialStoreException {
        var authzToken = getAuthzToken(request);

        // Validate and scope gateway ID - extract from token if not provided
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        var credential = credentialStoreService.getPasswordCredential(token, scopedGatewayId);
        if (credential == null) {
            throw new ResourceNotFoundException("PasswordCredential", token);
        }

        // Verify user has access to the credential's gateway
        authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);

        return ResponseEntity.ok(credential);
    }

    /**
     * Generates an SSH key pair, registers the credential, and creates a sharing entity for access control.
     * Rolls back the credential if sharing entity creation fails.
     */
    private String generateAndRegisterSSHKeys(String gatewayId, String userName, String description) throws Exception {
        try {
            var sshCredential = new SSHCredential();
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setUserId(userName + "@" + gatewayId);
            sshCredential.setDescription(description);
            var key = credentialStoreService.addSSHCredential(sshCredential);

            try {
                var entity = new SharingEntity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + SharingResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingService.createEntity(entity);
            } catch (SharingRegistryException | DuplicateEntryException ex) {
                String msg = "Error while creating SSH credential: " + userName + " " + description + " "
                        + ex.getMessage() + ". rolling back ssh key creation";
                logger.error(msg, ex);
                credentialStoreService.deleteSSHCredential(key, gatewayId);
                throw new RuntimeException(msg, ex);
            }
            logger.debug("Generated SSH keys for gateway : {} and for user : {}", gatewayId, userName);
            return key;
        } catch (RuntimeException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while registering SSH Credential: " + e.getMessage();
            logger.error(msg, e);
            throw e;
        }
    }

    /**
     * Delete credential (tries SSH first, then falls back to password)
     */
    @DeleteMapping("/credentials/{token}")
    public ResponseEntity<?> deleteCredential(
            @PathVariable String token, @RequestParam(required = false) String gatewayId, HttpServletRequest request)
            throws CredentialStoreException {
        var authzToken = getAuthzToken(request);

        // Validate and scope gateway ID - extract from token if not provided
        String scopedGatewayId = authorizationService.validateAndScopeGateway(authzToken, gatewayId);

        // Verify user has access to the credential's gateway
        authorizationService.requireGatewayAccess(authzToken, scopedGatewayId);

        // Try SSH first, then fall back to password
        try {
            boolean result = credentialStoreService.deleteSSHCredential(token, scopedGatewayId);
            return ResponseEntity.ok(Map.of("deleted", result));
        } catch (CredentialStoreException e) {
            // If SSH fails, try password
            boolean result = credentialStoreService.deletePWDCredential(token, scopedGatewayId);
            return ResponseEntity.ok(Map.of("deleted", result));
        }
    }
}
