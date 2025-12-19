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
package org.apache.airavata.restproxy.controller;

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.common.model.UserStoragePreference;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.UserResourceProfileService;
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
@RequestMapping("/api/v1/user-resource-profiles")
public class UserResourceProfileController {
    private final UserResourceProfileService userResourceProfileService;

    public UserResourceProfileController(UserResourceProfileService userResourceProfileService) {
        this.userResourceProfileService = userResourceProfileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserResourceProfile(
            @PathVariable String userId, @RequestParam String gatewayId) {
        try {
            UserResourceProfile profile = userResourceProfileService.getUserResourceProfile(userId, gatewayId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createUserResourceProfile(@RequestBody UserResourceProfile profile) {
        try {
            String userId = userResourceProfileService.addUserResourceProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", userId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserResourceProfile(
            @PathVariable String userId, @RequestParam String gatewayId, @RequestBody UserResourceProfile profile) {
        try {
            profile.setUserId(userId);
            profile.setGatewayID(gatewayId);
            userResourceProfileService.updateUserResourceProfile(userId, gatewayId, profile);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserResourceProfile(
            @PathVariable String userId, @RequestParam String gatewayId) {
        try {
            boolean deleted = userResourceProfileService.removeUserResourceProfile(userId, gatewayId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUserResourceProfiles() {
        try {
            List<UserResourceProfile> profiles = userResourceProfileService.getAllUserResourceProfiles();
            return ResponseEntity.ok(profiles);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/compute-preferences")
    public ResponseEntity<?> getUserComputeResourcePreferences(
            @PathVariable String userId, @RequestParam String gatewayId) {
        try {
            List<UserComputeResourcePreference> preferences =
                    userResourceProfileService.getAllUserComputeResourcePreferences(userId, gatewayId);
            return ResponseEntity.ok(preferences);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/storage-preferences")
    public ResponseEntity<?> getUserStoragePreferences(
            @PathVariable String userId, @RequestParam String gatewayId) {
        try {
            List<UserStoragePreference> preferences =
                    userResourceProfileService.getAllUserStoragePreferences(userId, gatewayId);
            return ResponseEntity.ok(preferences);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}


