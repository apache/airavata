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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.registry.services.GatewayConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for gateway-level configuration management.
 * Provides endpoints for managing gateway configuration as key-value preferences.
 */
@RestController
@RequestMapping("/api/v1/gateway-config")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Gateway Configuration", description = "Gateway configuration management API")
public class GatewayConfigController {

    private final GatewayConfigService gatewayConfigService;

    public GatewayConfigController(GatewayConfigService gatewayConfigService) {
        this.gatewayConfigService = gatewayConfigService;
    }

    /**
     * Get effective configuration for a gateway/user.
     */
    @GetMapping("/{gatewayId}")
    @Operation(summary = "Get effective gateway configuration")
    public ResponseEntity<?> getGatewayConfig(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            Map<String, String> config = gatewayConfigService.getEffectiveConfig(
                    gatewayId, userId, groupIdList);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a specific configuration value.
     */
    @GetMapping("/{gatewayId}/{key}")
    @Operation(summary = "Get a specific configuration value")
    public ResponseEntity<?> getConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            String value = gatewayConfigService.getConfigValue(gatewayId, userId, groupIdList, key);
            if (value == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set a configuration value.
     */
    @PostMapping("/{gatewayId}")
    @Operation(summary = "Set a configuration value")
    public ResponseEntity<?> setConfigValue(
            @PathVariable String gatewayId,
            @RequestBody ConfigRequest request) {
        try {
            gatewayConfigService.setGatewayConfig(
                    request.ownerId() != null ? request.ownerId() : gatewayId,
                    request.level() != null ? request.level() : PreferenceLevel.GATEWAY,
                    gatewayId,
                    request.key(),
                    request.value());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a configuration value.
     */
    @DeleteMapping("/{gatewayId}/{key}")
    @Operation(summary = "Delete a configuration value")
    public ResponseEntity<?> deleteConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) PreferenceLevel level) {
        try {
            gatewayConfigService.deleteGatewayConfig(
                    ownerId != null ? ownerId : gatewayId,
                    level != null ? level : PreferenceLevel.GATEWAY,
                    gatewayId,
                    key);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get feature flags for a gateway/user.
     */
    @GetMapping("/{gatewayId}/features")
    @Operation(summary = "Get feature flags for a gateway")
    public ResponseEntity<?> getFeatureFlags(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            var features = gatewayConfigService.getFeatureFlags(gatewayId, userId, groupIdList);
            return ResponseEntity.ok(features);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if a feature is enabled.
     */
    @GetMapping("/{gatewayId}/features/{feature}")
    @Operation(summary = "Check if a feature is enabled")
    public ResponseEntity<?> isFeatureEnabled(
            @PathVariable String gatewayId,
            @PathVariable String feature,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            boolean enabled = gatewayConfigService.isFeatureEnabled(gatewayId, userId, groupIdList, feature);
            return ResponseEntity.ok(Map.of("feature", feature, "enabled", enabled));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Enable or disable a feature.
     */
    @PutMapping("/{gatewayId}/features/{feature}")
    @Operation(summary = "Enable or disable a feature")
    public ResponseEntity<?> setFeatureEnabled(
            @PathVariable String gatewayId,
            @PathVariable String feature,
            @RequestParam boolean enabled,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) PreferenceLevel level) {
        try {
            gatewayConfigService.setFeatureFlag(
                    ownerId != null ? ownerId : gatewayId,
                    level != null ? level : PreferenceLevel.GATEWAY,
                    gatewayId,
                    feature,
                    enabled);
            return ResponseEntity.ok(Map.of("feature", feature, "enabled", enabled));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set maintenance mode for a gateway.
     */
    @PutMapping("/{gatewayId}/maintenance")
    @Operation(summary = "Set maintenance mode for a gateway")
    public ResponseEntity<?> setMaintenanceMode(
            @PathVariable String gatewayId,
            @RequestParam boolean enabled,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) PreferenceLevel level) {
        try {
            String effectiveOwnerId = ownerId != null ? ownerId : gatewayId;
            PreferenceLevel effectiveLevel = level != null ? level : PreferenceLevel.GATEWAY;

            gatewayConfigService.setMaintenanceMode(effectiveOwnerId, effectiveLevel, gatewayId, enabled);
            if (message != null) {
                gatewayConfigService.setMaintenanceMessage(effectiveOwnerId, effectiveLevel, gatewayId, message);
            }
            return ResponseEntity.ok(Map.of("maintenanceMode", enabled));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if gateway is in maintenance mode.
     */
    @GetMapping("/{gatewayId}/maintenance")
    @Operation(summary = "Check if gateway is in maintenance mode")
    public ResponseEntity<?> isInMaintenanceMode(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            boolean inMaintenance = gatewayConfigService.isMaintenanceMode(gatewayId, userId, groupIdList);
            String message = gatewayConfigService.getMaintenanceMessage(gatewayId, userId, groupIdList);
            return ResponseEntity.ok(Map.of(
                    "maintenanceMode", inMaintenance,
                    "message", message != null ? message : ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Request record for configuration
    public record ConfigRequest(
            String key,
            String value,
            String ownerId,
            PreferenceLevel level) {}
}
