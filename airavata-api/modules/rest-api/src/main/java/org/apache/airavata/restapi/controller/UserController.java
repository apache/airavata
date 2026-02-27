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
import java.util.stream.Collectors;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.exception.UserProfileServiceException;
import org.apache.airavata.iam.model.UserProfile;
import org.apache.airavata.iam.service.IamAdminService;
import org.apache.airavata.iam.service.UserService;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IamAdminService iamAdminService;
    private final AuthorizationService authorizationService;
    private final UserService userProfileService;

    public UserController(
            IamAdminService iamAdminService,
            AuthorizationService authorizationService,
            UserService userProfileService) {
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
            @RequestParam(required = false) String gatewayId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);

        // Resolve target gateway: from query param or token
        String targetGatewayId = gatewayId;
        if (targetGatewayId != null && !targetGatewayId.trim().isEmpty()) {
            targetGatewayId = targetGatewayId.trim();
        } else {
            targetGatewayId = (String) authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        }

        if (targetGatewayId == null || targetGatewayId.isEmpty()) {
            throw new InvalidRequestException("gatewayId is required");
        }

        // Ensure caller has access to this gateway
        authorizationService.requireGatewayAccess(authzToken, targetGatewayId);

        // Prefer gateway user profiles (DB) so admin page shows users registered in this gateway
        try {
            List<UserProfile> users =
                    userProfileService.getAllUserProfilesInGateway(authzToken, targetGatewayId, offset, limit);

            // Optional client-side search filter (DB query does not support search)
            if (search != null && !search.isBlank()) {
                String term = search.toLowerCase().trim();
                users = users.stream().filter(u -> matchesSearch(u, term)).collect(Collectors.toList());
            }

            // Ensure gatewayId is set on each profile
            for (var user : users) {
                if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                    user.setGatewayId(targetGatewayId);
                }
            }
            return ResponseEntity.ok(users);
        } catch (UserProfileServiceException e) {
            logger.debug(
                    "User profiles for gateway {} unavailable, falling back to IAM: {}",
                    targetGatewayId,
                    e.getMessage());
        }

        // Fallback: use IAM (Keycloak) list when DB has no profiles or throws
        if (!targetGatewayId.equals(authzToken.getClaimsMap().get(Constants.GATEWAY_ID))) {
            authzToken.getClaimsMap().put(Constants.GATEWAY_ID, targetGatewayId);
        }
        try {
            List<UserProfile> users = iamAdminService.getUsers(authzToken, offset, limit, search);
            for (var user : users) {
                if (user.getGatewayId() == null || user.getGatewayId().isEmpty()) {
                    user.setGatewayId(targetGatewayId);
                }
            }
            return ResponseEntity.ok(users);
        } catch (IamAdminServicesException e) {
            logger.warn("Error retrieving users from IAM: {}", e.getMessage());
            String warningMsg = e.getMessage() != null && e.getMessage().contains("Gateway ID")
                    ? "Gateway IAM configuration missing - returning empty user list. Please configure IAM settings for the gateway."
                    : "IAM service unavailable - returning empty user list";
            return ResponseEntity.ok().header("X-Warning", warningMsg).body(new java.util.ArrayList<>());
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
            HttpServletRequest request, @PathVariable String userId, @RequestParam(required = false) String gatewayId) {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);

        // Resolve target gateway: from query param or token
        String targetGatewayId = gatewayId;
        if (targetGatewayId != null && !targetGatewayId.trim().isEmpty()) {
            targetGatewayId = targetGatewayId.trim();
        } else {
            targetGatewayId = (String) authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        }

        if (targetGatewayId == null || targetGatewayId.isEmpty()) {
            throw new InvalidRequestException("gatewayId is required");
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
            logger.debug(
                    "User profile for userId={} in gateway={} not found in DB, trying IAM: {}",
                    userId,
                    targetGatewayId,
                    e.getMessage());
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

        throw new ResourceNotFoundException("User", userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            HttpServletRequest request, @PathVariable String userId, @RequestBody UserProfile userProfile)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        userProfile.setUserId(userId);
        iamAdminService.updateUserProfile(authzToken, userProfile);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/enable")
    public ResponseEntity<?> enableUser(HttpServletRequest request, @PathVariable String userId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        boolean result = iamAdminService.enableUser(authzToken, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/disable")
    public ResponseEntity<?> disableUser(HttpServletRequest request, @PathVariable String userId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        boolean result = iamAdminService.disableUser(authzToken, userId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, @PathVariable String userId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        boolean result = iamAdminService.deleteUser(authzToken, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<?> checkUserExists(HttpServletRequest request, @PathVariable String userId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        boolean exists = iamAdminService.isUserExist(authzToken, userId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{userId}/enabled")
    public ResponseEntity<?> checkUserEnabled(HttpServletRequest request, @PathVariable String userId)
            throws IamAdminServicesException {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        boolean enabled = iamAdminService.isUserEnabled(authzToken, userId);
        return ResponseEntity.ok(enabled);
    }
}
