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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.services.PreferenceResolutionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for multi-level preference management.
 * Provides endpoints for resolving, setting, and deleting preferences
 * across GATEWAY, GROUP, and USER levels.
 */
@RestController
@RequestMapping("/api/v1/preferences")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Preferences", description = "Multi-level preference management API")
public class PreferenceController {

    private final PreferenceResolutionService preferenceResolutionService;

    public PreferenceController(PreferenceResolutionService preferenceResolutionService) {
        this.preferenceResolutionService = preferenceResolutionService;
    }

    /**
     * Resolve effective preferences for a resource, applying the precedence rules:
     * USER > GROUP > GATEWAY
     */
    @GetMapping("/resolve")
    @Operation(summary = "Resolve effective preferences applying USER > GROUP > GATEWAY precedence")
    public ResponseEntity<?> resolvePreferences(
            @RequestParam PreferenceResourceType resourceType,
            @RequestParam String resourceId,
            @RequestParam String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? Arrays.asList(groupIds.split(","))
                    : List.of();

            Map<String, String> resolved;
            if (resourceType == PreferenceResourceType.COMPUTE) {
                resolved = preferenceResolutionService.resolveComputePreferences(
                        gatewayId, userId, groupIdList, resourceId);
            } else if (resourceType == PreferenceResourceType.STORAGE) {
                resolved = preferenceResolutionService.resolveStoragePreferences(
                        gatewayId, userId, groupIdList, resourceId);
            } else {
                resolved = preferenceResolutionService.resolvePreferences(
                        resourceType, resourceId, gatewayId, userId, groupIdList);
            }

            return ResponseEntity.ok(resolved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get preferences at a specific level (unresolved).
     */
    @GetMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Get preferences at a specific level")
    public ResponseEntity<?> getPreferencesAtLevel(
            @PathVariable PreferenceResourceType resourceType,
            @PathVariable String resourceId,
            @RequestParam PreferenceLevel level,
            @RequestParam String ownerId,
            @RequestParam(required = false, defaultValue = "false") boolean detailed) {
        try {
            if (detailed) {
                var prefs = preferenceResolutionService.getPreferencesAtLevelDetailed(
                        resourceType, resourceId, ownerId, level);
                return ResponseEntity.ok(prefs);
            } else {
                Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                        resourceType, resourceId, ownerId, level);
                return ResponseEntity.ok(prefs);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set a preference at a specific level.
     * If enforced is true, this preference cannot be overridden by lower-level preferences.
     */
    @PostMapping
    @Operation(summary = "Set a preference at a specific level with optional enforcement")
    public ResponseEntity<?> setPreference(@RequestBody SetPreferenceRequest request) {
        try {
            preferenceResolutionService.setPreference(
                    request.resourceType(),
                    request.resourceId(),
                    request.ownerId(),
                    request.level(),
                    request.key(),
                    request.value(),
                    request.enforced() != null ? request.enforced() : false);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a preference at a specific level.
     */
    @DeleteMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Delete a preference at a specific level")
    public ResponseEntity<?> deletePreference(
            @PathVariable PreferenceResourceType resourceType,
            @PathVariable String resourceId,
            @RequestParam PreferenceLevel level,
            @RequestParam String ownerId,
            @RequestParam String key) {
        try {
            preferenceResolutionService.deletePreference(resourceType, resourceId, ownerId, level, key);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete all preferences for a resource at a specific level.
     */
    @DeleteMapping("/{resourceType}/{resourceId}/all")
    @Operation(summary = "Delete all preferences for a resource at a specific level")
    public ResponseEntity<?> deleteAllPreferences(
            @PathVariable PreferenceResourceType resourceType,
            @PathVariable String resourceId,
            @RequestParam PreferenceLevel level,
            @RequestParam String ownerId) {
        try {
            preferenceResolutionService.deleteAllPreferences(resourceType, resourceId, ownerId, level);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Request record for setting preferences
    public record SetPreferenceRequest(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key,
            String value,
            Boolean enforced) {}
}
