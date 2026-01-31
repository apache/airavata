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
import java.util.stream.Collectors;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.exception.UserProfileServiceException;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.security.IamAdminService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class UserController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    private final IamAdminService iamAdminService;
    private final AuthorizationService authorizationService;
    private final UserProfileService userProfileService;

    public UserController(
            IamAdminService iamAdminService,
            AuthorizationService authorizationService,
            UserProfileService userProfileService) {
        this.iamAdminService = iamAdminService;
        this.authorizationService = authorizationService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gatewayId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);

            // Resolve target gateway: from query param or token
            String targetGatewayId = gatewayId;
            if (targetGatewayId != null && !targetGatewayId.trim().isEmpty()) {
                targetGatewayId = targetGatewayId.trim();
            } else {
                targetGatewayId = (String) authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            }

            if (targetGatewayId == null || targetGatewayId.isEmpty()) {
                return ResponseEntity.badRequest().body("gatewayId is required");
            }

            // Ensure caller has access to this gateway
            authorizationService.requireGatewayAccess(authzToken, targetGatewayId);

            // Prefer gateway user profiles (DB) so admin page shows users registered in this gateway
            try {
                List<UserProfile> users = userProfileService.getAllUserProfilesInGateway(
                        authzToken, targetGatewayId, offset, limit);

                // Optional client-side search filter (DB query does not support search)
                if (search != null && !search.isBlank()) {
                    String term = search.toLowerCase().trim();
                    users = users.stream()
                            .filter(u -> matchesSearch(u, term))
                            .collect(Collectors.toList());
                }

                // Ensure gatewayId is set on each profile
                for (var user : users) {
                    if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                        user.setGatewayId(targetGatewayId);
                    }
                }
                return ResponseEntity.ok(users);
            } catch (UserProfileServiceException e) {
                logger.debug("User profiles for gateway {} unavailable, falling back to IAM: {}",
                        targetGatewayId, e.getMessage());
            }

            // Fallback: use IAM (Keycloak) list when DB has no profiles or throws
            if (!targetGatewayId.equals(authzToken.getClaimsMap().get(Constants.GATEWAY_ID))) {
                authzToken.getClaimsMap().put(Constants.GATEWAY_ID, targetGatewayId);
            }
            List<UserProfile> users = iamAdminService.getUsers(authzToken, offset, limit, search);
            for (var user : users) {
                if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                    user.setGatewayId(targetGatewayId);
                }
            }
            return ResponseEntity.ok(users);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (IamAdminServicesException e) {
            logger.warn("Error retrieving users from IAM: {}", e.getMessage());
            String warningMsg = e.getMessage() != null && e.getMessage().contains("Gateway ID")
                    ? "Gateway IAM configuration missing - returning empty user list. Please configure IAM settings for the gateway."
                    : "IAM service unavailable - returning empty user list";
            return ResponseEntity.ok()
                    .header("X-Warning", warningMsg)
                    .body(new java.util.ArrayList<>());
        }
    }

    private static boolean matchesSearch(UserProfile u, String term) {
        if (u.getUserId() != null && u.getUserId().toLowerCase().contains(term)) return true;
        if (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(term)) return true;
        if (u.getLastName() != null && u.getLastName().toLowerCase().contains(term)) return true;
        if (u.getEmails() != null) {
            for (String e : u.getEmails()) {
                if (e != null && e.toLowerCase().contains(term)) return true;
            }
        }
        return false;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(
            HttpServletRequest request,
            @PathVariable String userId,
            @RequestParam(required = false) String gatewayId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);

            // Resolve target gateway: from query param or token
            String targetGatewayId = gatewayId;
            if (targetGatewayId != null && !targetGatewayId.trim().isEmpty()) {
                targetGatewayId = targetGatewayId.trim();
            } else {
                targetGatewayId = (String) authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            }

            if (targetGatewayId == null || targetGatewayId.isEmpty()) {
                return ResponseEntity.badRequest().body("gatewayId is required");
            }

            // Ensure caller has access to this gateway
            authorizationService.requireGatewayAccess(authzToken, targetGatewayId);

            // First try to get user from database (preferred)
            try {
                UserProfile user = userProfileService.getUserProfileById(authzToken, userId, targetGatewayId);
                if (user != null) {
                    if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                        user.setGatewayId(targetGatewayId);
                    }
                    return ResponseEntity.ok(user);
                }
            } catch (UserProfileServiceException e) {
                logger.debug("User profile for userId={} in gateway={} not found in DB, trying IAM: {}",
                        userId, targetGatewayId, e.getMessage());
            }

            // Fallback: try IAM (Keycloak)
            try {
                UserProfile user = iamAdminService.getUser(authzToken, userId);
                if (user != null) {
                    if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                        user.setGatewayId(targetGatewayId);
                    }
                    return ResponseEntity.ok(user);
                }
            } catch (IamAdminServicesException e) {
                logger.debug("User profile for userId={} not found in IAM: {}", userId, e.getMessage());
            }

            return ResponseEntity.notFound().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            HttpServletRequest request,
            @PathVariable String userId,
            @RequestBody UserProfile userProfile) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            userProfile.setUserId(userId);
            iamAdminService.updateUserProfile(authzToken, userProfile);
            return ResponseEntity.ok().build();
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/enable")
    public ResponseEntity<?> enableUser(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            boolean result = iamAdminService.enableUser(authzToken, userId);
            return ResponseEntity.ok(result);
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/disable")
    public ResponseEntity<?> disableUser(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            boolean result = iamAdminService.disableUser(authzToken, userId);
            return ResponseEntity.ok(result);
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            boolean result = iamAdminService.deleteUser(authzToken, userId);
            return ResponseEntity.ok(result);
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<?> checkUserExists(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            boolean exists = iamAdminService.isUserExist(authzToken, userId);
            return ResponseEntity.ok(exists);
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/enabled")
    public ResponseEntity<?> checkUserEnabled(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            boolean enabled = iamAdminService.isUserEnabled(authzToken, userId);
            return ResponseEntity.ok(enabled);
        } catch (IamAdminServicesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
