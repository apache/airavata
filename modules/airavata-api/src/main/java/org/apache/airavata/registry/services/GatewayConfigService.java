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
package org.apache.airavata.registry.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceKeys;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing gateway-level configuration.
 *
 * <p>This service provides multi-level (GATEWAY > GROUP > USER) preference resolution
 * for gateway settings including feature toggles, UI customization, and behavior.
 *
 * <h3>Resource ID Format:</h3>
 * The resource ID for gateway preferences is: {@code gatewayId}
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Set gateway-wide feature flags and settings</li>
 *   <li>Allow groups to have different feature access</li>
 *   <li>Give specific users personalized gateway experience</li>
 *   <li>Enable/disable features at different levels</li>
 * </ul>
 *
 * @see PreferenceKeys.Gateway
 * @see PreferenceResourceType#GATEWAY
 */
@Service
@Transactional(readOnly = true)
public class GatewayConfigService {

    private final PreferenceResolutionService preferenceService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GatewayConfigService(PreferenceResolutionService preferenceService) {
        this.preferenceService = preferenceService;
        this.objectMapper = new ObjectMapper();
    }

    // ========== Resolution Methods ==========

    /**
     * Resolve effective gateway configuration for a user.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @return map of preference key to resolved value
     */
    public Map<String, String> getEffectiveConfig(String gatewayId, String userId, List<String> groupIds) {
        return preferenceService.resolvePreferences(
                PreferenceResourceType.GATEWAY, gatewayId, gatewayId, userId, groupIds);
    }

    /**
     * Get a specific gateway configuration value.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs
     * @param key the preference key
     * @return the resolved value, or null if not set
     */
    public String getConfigValue(String gatewayId, String userId, List<String> groupIds, String key) {
        Map<String, String> prefs = getEffectiveConfig(gatewayId, userId, groupIds);
        return prefs.get(key);
    }

    /**
     * Get a boolean configuration value.
     */
    public Boolean getBooleanConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getConfigValue(gatewayId, userId, groupIds, key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Get an integer configuration value.
     */
    public Integer getIntegerConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getConfigValue(gatewayId, userId, groupIds, key);
        return value != null ? Integer.parseInt(value) : null;
    }

    // ========== Feature Flag Methods ==========

    /**
     * Check if a specific feature is enabled for a user.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @param featureName the feature name to check
     * @return true if the feature is enabled, false if disabled or not set
     */
    public boolean isFeatureEnabled(String gatewayId, String userId, List<String> groupIds, String featureName) {
        Map<String, String> prefs = getEffectiveConfig(gatewayId, userId, groupIds);

        // Check feature flags JSON
        String flagsJson = prefs.get(PreferenceKeys.Gateway.FEATURE_FLAGS);
        if (flagsJson != null) {
            Map<String, Boolean> flags = parseFeatureFlags(flagsJson);
            Boolean enabled = flags.get(featureName);
            if (enabled != null) {
                return enabled;
            }
        }

        // Default to false if not set
        return false;
    }

    /**
     * Get all feature flags for a user.
     */
    public Map<String, Boolean> getFeatureFlags(String gatewayId, String userId, List<String> groupIds) {
        String flagsJson = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.FEATURE_FLAGS);
        return parseFeatureFlags(flagsJson);
    }

    /**
     * Check if experiment launching is enabled.
     */
    public boolean isExperimentLaunchEnabled(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLE_EXPERIMENT_LAUNCH);
        return enabled != null ? enabled : true; // Default to enabled
    }

    /**
     * Check if data transfer is enabled.
     */
    public boolean isDataTransferEnabled(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLE_DATA_TRANSFER);
        return enabled != null ? enabled : true; // Default to enabled
    }

    /**
     * Check if workflows are enabled.
     */
    public boolean isWorkflowsEnabled(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLE_WORKFLOWS);
        return enabled != null ? enabled : false; // Default to disabled
    }

    /**
     * Check if gateway is in maintenance mode.
     */
    public boolean isMaintenanceMode(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.MAINTENANCE_MODE);
        return enabled != null ? enabled : false;
    }

    /**
     * Get the maintenance message.
     */
    public String getMaintenanceMessage(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.MAINTENANCE_MESSAGE);
    }

    // ========== Default Resource Methods ==========

    /**
     * Get the default storage resource for the gateway.
     */
    public String getDefaultStorageResource(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.DEFAULT_STORAGE_RESOURCE);
    }

    /**
     * Get the default compute resource for the gateway.
     */
    public String getDefaultComputeResource(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.DEFAULT_COMPUTE_RESOURCE);
    }

    /**
     * Get the maximum concurrent experiments per user.
     */
    public Integer getMaxConcurrentExperiments(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.MAX_CONCURRENT_EXPERIMENTS);
    }

    /**
     * Get the enabled applications for the gateway.
     */
    public List<String> getEnabledApplications(String gatewayId, String userId, List<String> groupIds) {
        String json = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLED_APPLICATIONS);
        return parseJsonList(json);
    }

    /**
     * Get the enabled compute resources for the gateway.
     */
    public List<String> getEnabledComputeResources(String gatewayId, String userId, List<String> groupIds) {
        String json = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLED_COMPUTE_RESOURCES);
        return parseJsonList(json);
    }

    /**
     * Get the enabled storage resources for the gateway.
     */
    public List<String> getEnabledStorageResources(String gatewayId, String userId, List<String> groupIds) {
        String json = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ENABLED_STORAGE_RESOURCES);
        return parseJsonList(json);
    }

    // ========== UI Configuration Methods ==========

    /**
     * Get the UI theme.
     */
    public String getUiTheme(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.UI_THEME);
    }

    /**
     * Get the dashboard layout configuration.
     */
    public Map<String, Object> getDashboardLayout(String gatewayId, String userId, List<String> groupIds) {
        String json = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.DASHBOARD_LAYOUT);
        return parseJsonObject(json);
    }

    /**
     * Get the current announcement/banner.
     */
    public String getAnnouncement(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.ANNOUNCEMENT);
    }

    // ========== Setter Methods ==========

    /**
     * Set a gateway configuration at a specific level.
     *
     * @param ownerId the owner ID (gatewayId, groupId, or userId@gatewayId)
     * @param level the preference level
     * @param gatewayId the gateway ID (resource ID)
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setGatewayConfig(
            String ownerId, PreferenceLevel level, String gatewayId, String key, String value) {
        preferenceService.setPreference(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level, key, value);
    }

    /**
     * Enable or disable experiment launching.
     */
    @Transactional
    public void setExperimentLaunchEnabled(String ownerId, PreferenceLevel level, String gatewayId, boolean enabled) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.ENABLE_EXPERIMENT_LAUNCH,
                String.valueOf(enabled));
    }

    /**
     * Enable or disable data transfer.
     */
    @Transactional
    public void setDataTransferEnabled(String ownerId, PreferenceLevel level, String gatewayId, boolean enabled) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.ENABLE_DATA_TRANSFER,
                String.valueOf(enabled));
    }

    /**
     * Enable or disable maintenance mode.
     */
    @Transactional
    public void setMaintenanceMode(String ownerId, PreferenceLevel level, String gatewayId, boolean enabled) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.MAINTENANCE_MODE, String.valueOf(enabled));
    }

    /**
     * Set the maintenance message.
     */
    @Transactional
    public void setMaintenanceMessage(String ownerId, PreferenceLevel level, String gatewayId, String message) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.MAINTENANCE_MESSAGE, message);
    }

    /**
     * Set feature flags.
     */
    @Transactional
    public void setFeatureFlags(String ownerId, PreferenceLevel level, String gatewayId, Map<String, Boolean> flags) {
        try {
            String json = objectMapper.writeValueAsString(flags);
            setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.FEATURE_FLAGS, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize feature flags", e);
        }
    }

    /**
     * Set a single feature flag.
     */
    @Transactional
    public void setFeatureFlag(
            String ownerId, PreferenceLevel level, String gatewayId, String featureName, boolean enabled) {
        // Get existing flags and update
        Map<String, Boolean> flags = getFeatureFlagsAtLevel(ownerId, level, gatewayId);
        flags.put(featureName, enabled);
        setFeatureFlags(ownerId, level, gatewayId, flags);
    }

    /**
     * Set the default storage resource.
     */
    @Transactional
    public void setDefaultStorageResource(
            String ownerId, PreferenceLevel level, String gatewayId, String storageResourceId) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.DEFAULT_STORAGE_RESOURCE, storageResourceId);
    }

    /**
     * Set the default compute resource.
     */
    @Transactional
    public void setDefaultComputeResource(
            String ownerId, PreferenceLevel level, String gatewayId, String computeResourceId) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.DEFAULT_COMPUTE_RESOURCE, computeResourceId);
    }

    /**
     * Set the maximum concurrent experiments.
     */
    @Transactional
    public void setMaxConcurrentExperiments(String ownerId, PreferenceLevel level, String gatewayId, int max) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.MAX_CONCURRENT_EXPERIMENTS,
                String.valueOf(max));
    }

    /**
     * Set the UI theme.
     */
    @Transactional
    public void setUiTheme(String ownerId, PreferenceLevel level, String gatewayId, String theme) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.UI_THEME, theme);
    }

    /**
     * Set the announcement.
     */
    @Transactional
    public void setAnnouncement(String ownerId, PreferenceLevel level, String gatewayId, String announcement) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.ANNOUNCEMENT, announcement);
    }

    /**
     * Delete a gateway configuration at a specific level.
     */
    @Transactional
    public void deleteGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId, String key) {
        preferenceService.deletePreference(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level, key);
    }

    /**
     * Delete all gateway configuration at a specific level.
     */
    @Transactional
    public void deleteAllGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId) {
        preferenceService.deleteAllPreferences(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level);
    }

    // ========== Helper Methods ==========

    private Map<String, Boolean> getFeatureFlagsAtLevel(String ownerId, PreferenceLevel level, String gatewayId) {
        Map<String, String> prefs =
                preferenceService.getPreferencesAtLevel(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level);
        String json = prefs.get(PreferenceKeys.Gateway.FEATURE_FLAGS);
        return parseFeatureFlags(json);
    }

    private Map<String, Boolean> parseFeatureFlags(String json) {
        if (json == null || json.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Boolean>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
