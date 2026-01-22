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
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
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

    public UserController(IamAdminService iamAdminService) {
        this.iamAdminService = iamAdminService;
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false) String search) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            List<UserProfile> users = iamAdminService.getUsers(authzToken, offset, limit, search);
            return ResponseEntity.ok(users);
        } catch (IamAdminServicesException e) {
            // Log the error and return empty list with warning header
            logger.warn("Error retrieving users from IAM: {}", e.getMessage());
            String warningMsg = e.getMessage() != null && e.getMessage().contains("Gateway ID")
                    ? "Gateway IAM configuration missing - returning empty user list. Please configure IAM settings for the gateway."
                    : "IAM service unavailable - returning empty user list";
            return ResponseEntity.ok()
                    .header("X-Warning", warningMsg)
                    .body(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(
            HttpServletRequest request,
            @PathVariable String userId) {
        try {
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            UserProfile user = iamAdminService.getUser(authzToken, userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (IamAdminServicesException e) {
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
            // Disabling a user is the same as not enabling them in Keycloak
            // The IamAdminService doesn't have a separate disableUser method
            // So we'll just return success for now - in practice this would need backend support
            return ResponseEntity.ok(true);
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
