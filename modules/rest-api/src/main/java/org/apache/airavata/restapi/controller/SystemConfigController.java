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
import org.apache.airavata.gateway.service.ConfigService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for system-wide configuration management.
 * Provides endpoints for managing global system configuration.
 */
@RestController
@RequestMapping("/api/v1/system-config")
@Tag(name = "System Configuration", description = "System-wide configuration management API")
public class SystemConfigController {

    private final ConfigService configService;

    public SystemConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Get a specific global configuration value.
     */
    @GetMapping("/global/{key}")
    @Operation(summary = "Get a specific global configuration value")
    public Map<String, String> getGlobalConfigValue(@PathVariable String key) throws Exception {
        String value = configService.getGlobalDefault(key);
        if (value == null) {
            throw new ResourceNotFoundException("GlobalConfig", key);
        }
        return Map.of("key", key, "value", value);
    }

    /**
     * Set a global configuration value.
     */
    @PostMapping("/global")
    @Operation(summary = "Set a global configuration value")
    public Map<String, Boolean> setGlobalConfigValue(@RequestBody ConfigRequest request) throws Exception {
        configService.setGlobalConfig(request.key(), request.value());
        return Map.of("success", true);
    }

    /**
     * Delete a global configuration value.
     */
    @DeleteMapping("/global/{key}")
    @Operation(summary = "Delete a global configuration value")
    public Map<String, Boolean> deleteGlobalConfigValue(@PathVariable String key) throws Exception {
        configService.deleteGlobalConfig(key);
        return Map.of("success", true);
    }

    /**
     * Get effective configuration for a gateway/user.
     */
    @GetMapping("/{gatewayId}/effective")
    @Operation(summary = "Get effective configuration for a gateway/user")
    public Map<String, String> getEffectiveConfig(
            @PathVariable String gatewayId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        return configService.getEffectiveSystemConfig(gatewayId, userId, groupIdList);
    }

    /**
     * Set a gateway-specific override for a system configuration.
     */
    @PostMapping("/{gatewayId}/override")
    @Operation(summary = "Set a gateway-specific configuration override")
    public Map<String, Boolean> setGatewayOverride(@PathVariable String gatewayId, @RequestBody ConfigRequest request)
            throws Exception {
        configService.setGatewayOverride(gatewayId, request.key(), request.value());
        return Map.of("success", true);
    }

    /**
     * Delete a gateway-specific override.
     */
    @DeleteMapping("/{gatewayId}/override/{key}")
    @Operation(summary = "Delete a gateway-specific configuration override")
    public Map<String, Boolean> deleteGatewayOverride(@PathVariable String gatewayId, @PathVariable String key)
            throws Exception {
        configService.deleteGatewayOverride(gatewayId, key);
        return Map.of("success", true);
    }

    /**
     * Get a specific configuration value for a user.
     */
    @GetMapping("/{gatewayId}/{key}")
    @Operation(summary = "Get a specific configuration value for a user")
    public Map<String, String> getConfigValue(
            @PathVariable String gatewayId,
            @PathVariable String key,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupIds)
            throws Exception {
        List<String> groupIdList =
                groupIds != null && !groupIds.isEmpty() ? List.of(groupIds.split(",")) : Collections.emptyList();

        String value = configService.getSystemConfig(gatewayId, userId, groupIdList, key);
        if (value == null) {
            throw new ResourceNotFoundException("SystemConfig", key);
        }
        return Map.of("key", key, "value", value);
    }

    // Request record for configuration
    public record ConfigRequest(String key, String value) {}
}
