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
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.gateway.service.ConfigService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for gateway-level configuration management.
 * Provides endpoints for managing gateway configuration as key-value preferences.
 */
@RestController
@RequestMapping("/api/v1/gateway-config")
@Tag(name = "Gateway Configuration", description = "Gateway configuration management API")
public class GatewayConfigController {

    private final ConfigService configService;

    public GatewayConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Get effective configuration for a gateway/user.
     */
    @GetMapping("/{gatewayId}")
    @Operation(summary = "Get effective gateway configuration")
    public Map<String, String> getGatewayConfig(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        return configService.getEffectiveConfig(gatewayId, userId, groupIdList);
    }

    /**
     * Get a specific configuration value.
     */
    @GetMapping("/{gatewayId}/{key}")
    @Operation(summary = "Get a specific configuration value")
    public Map<String, String> getConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        String value = configService.getConfigValue(gatewayId, userId, groupIdList, key);
        if (value == null) {
            throw new ResourceNotFoundException("ConfigValue", key);
        }
        return Map.of("key", key, "value", value);
    }

    /**
     * Set a configuration value.
     */
    @PostMapping("/{gatewayId}")
    @Operation(summary = "Set a configuration value")
    public Map<String, Boolean> setConfigValue(@PathVariable String gatewayId, @RequestBody ConfigRequest request)
            throws Exception {
        configService.setGatewayConfig(
                request.ownerId() != null ? request.ownerId() : gatewayId,
                request.level() != null ? request.level() : PreferenceLevel.GATEWAY,
                gatewayId,
                request.key(),
                request.value());
        return Map.of("success", true);
    }

    /**
     * Delete a configuration value.
     */
    @DeleteMapping("/{gatewayId}/{key}")
    @Operation(summary = "Delete a configuration value")
    public Map<String, Boolean> deleteConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) PreferenceLevel level)
            throws Exception {
        configService.deleteGatewayConfig(
                ownerId != null ? ownerId : gatewayId, level != null ? level : PreferenceLevel.GATEWAY, gatewayId, key);
        return Map.of("success", true);
    }

    /**
     * Get feature flags for a gateway/user.
     */
    @GetMapping("/{gatewayId}/features")
    @Operation(summary = "Get feature flags for a gateway")
    public Map<String, Boolean> getFeatureFlags(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        return configService.getFeatureFlags(gatewayId, userId, groupIdList);
    }

    /**
     * Check if a feature is enabled.
     */
    @GetMapping("/{gatewayId}/features/{feature}")
    @Operation(summary = "Check if a feature is enabled")
    public Map<String, Object> isFeatureEnabled(
            @PathVariable String gatewayId,
            @PathVariable String feature,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        boolean enabled = configService.isFeatureEnabled(gatewayId, userId, groupIdList, feature);
        return Map.of("feature", feature, "enabled", enabled);
    }

    /**
     * Enable or disable a feature.
     */
    @PutMapping("/{gatewayId}/features/{feature}")
    @Operation(summary = "Enable or disable a feature")
    public Map<String, Object> setFeatureEnabled(
            @PathVariable String gatewayId, @PathVariable String feature, @RequestBody FeatureRequest request)
            throws Exception {
        configService.setFeatureFlag(
                request.ownerId() != null ? request.ownerId() : gatewayId,
                request.level() != null ? request.level() : PreferenceLevel.GATEWAY,
                gatewayId,
                feature,
                request.enabled());
        return Map.of("feature", feature, "enabled", request.enabled());
    }

    /**
     * Set maintenance mode for a gateway.
     */
    @PutMapping("/{gatewayId}/maintenance")
    @Operation(summary = "Set maintenance mode for a gateway")
    public Map<String, Object> setMaintenanceMode(
            @PathVariable String gatewayId, @RequestBody MaintenanceRequest request) throws Exception {
        String effectiveOwnerId = request.ownerId() != null ? request.ownerId() : gatewayId;
        PreferenceLevel effectiveLevel = request.level() != null ? request.level() : PreferenceLevel.GATEWAY;

        configService.setMaintenanceMode(effectiveOwnerId, effectiveLevel, gatewayId, request.enabled());
        if (request.message() != null) {
            configService.setMaintenanceMessage(effectiveOwnerId, effectiveLevel, gatewayId, request.message());
        }
        return Map.of("maintenanceMode", request.enabled());
    }

    /**
     * Check if gateway is in maintenance mode.
     */
    @GetMapping("/{gatewayId}/maintenance")
    @Operation(summary = "Check if gateway is in maintenance mode")
    public Map<String, Object> isInMaintenanceMode(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        boolean inMaintenance = configService.isMaintenanceMode(gatewayId, userId, groupIdList);
        String message = configService.getMaintenanceMessage(gatewayId, userId, groupIdList);
        return Map.of("maintenanceMode", inMaintenance, "message", message != null ? message : "");
    }

    // Request record for configuration
    public record ConfigRequest(String key, String value, String ownerId, PreferenceLevel level) {}

    // Request record for feature flag updates
    public record FeatureRequest(boolean enabled, String ownerId, PreferenceLevel level) {}

    // Request record for maintenance mode updates
    public record MaintenanceRequest(boolean enabled, String message, String ownerId, PreferenceLevel level) {}
}
