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
import org.apache.airavata.registry.services.SystemConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for system-wide configuration management.
 * Provides endpoints for managing global system configuration.
 */
@RestController
@RequestMapping("/api/v1/system-config")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "System Configuration", description = "System-wide configuration management API")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * Get a specific global configuration value.
     */
    @GetMapping("/global/{key}")
    @Operation(summary = "Get a specific global configuration value")
    public ResponseEntity<?> getGlobalConfigValue(@PathVariable String key) {
        try {
            String value = systemConfigService.getGlobalDefault(key);
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
     * Set a global configuration value.
     */
    @PostMapping("/global")
    @Operation(summary = "Set a global configuration value")
    public ResponseEntity<?> setGlobalConfigValue(@RequestBody ConfigRequest request) {
        try {
            systemConfigService.setGlobalConfig(request.key(), request.value());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a global configuration value.
     */
    @DeleteMapping("/global/{key}")
    @Operation(summary = "Delete a global configuration value")
    public ResponseEntity<?> deleteGlobalConfigValue(@PathVariable String key) {
        try {
            systemConfigService.deleteGlobalConfig(key);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get effective configuration for a gateway/user.
     */
    @GetMapping("/{gatewayId}/effective")
    @Operation(summary = "Get effective configuration for a gateway/user")
    public ResponseEntity<?> getEffectiveConfig(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            Map<String, String> config = systemConfigService.getEffectiveSystemConfig(
                    gatewayId, userId, groupIdList);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set a gateway-specific override for a system configuration.
     */
    @PostMapping("/{gatewayId}/override")
    @Operation(summary = "Set a gateway-specific configuration override")
    public ResponseEntity<?> setGatewayOverride(
            @PathVariable String gatewayId,
            @RequestBody ConfigRequest request) {
        try {
            systemConfigService.setGatewayOverride(gatewayId, request.key(), request.value());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a gateway-specific override.
     */
    @DeleteMapping("/{gatewayId}/override/{key}")
    @Operation(summary = "Delete a gateway-specific configuration override")
    public ResponseEntity<?> deleteGatewayOverride(
            @PathVariable String gatewayId,
            @PathVariable String key) {
        try {
            systemConfigService.deleteGatewayOverride(gatewayId, key);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a specific configuration value for a user.
     */
    @GetMapping("/{gatewayId}/{key}")
    @Operation(summary = "Get a specific configuration value for a user")
    public ResponseEntity<?> getConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds) {
        try {
            List<String> groupIdList = groupIds != null && !groupIds.isEmpty()
                    ? List.of(groupIds.split(","))
                    : Collections.emptyList();

            String value = systemConfigService.getSystemConfig(gatewayId, userId, groupIdList, key);
            if (value == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Request record for configuration
    public record ConfigRequest(String key, String value) {}
}
