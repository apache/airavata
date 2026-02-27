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
package org.apache.airavata.gateway.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.model.PreferenceKeys;
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified configuration service for both gateway-level and system-level preferences.
 *
 * <p>Replaces the former {@code GatewayConfigService} and {@code SystemConfigService}.
 * Both configuration domains delegate to {@link PreferenceResolutionService} and differ
 * only in the {@link PreferenceResourceType} enum value used.
 */
@Service
@Transactional(readOnly = true)
public class DefaultConfigService implements ConfigService {

    private final PreferenceResolutionService preferenceService;

    public DefaultConfigService(PreferenceResolutionService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // ========================================================================
    // Gateway Configuration
    // ========================================================================

    @Override
    public Map<String, String> getEffectiveConfig(String gatewayId, String userId, List<String> groupIds) {
        return preferenceService.resolvePreferences(
                PreferenceResourceType.GATEWAY, gatewayId, gatewayId, userId, groupIds);
    }

    @Override
    public String getConfigValue(String gatewayId, String userId, List<String> groupIds, String key) {
        Map<String, String> prefs = getEffectiveConfig(gatewayId, userId, groupIds);
        return prefs.get(key);
    }

    private Boolean getBooleanGatewayConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getConfigValue(gatewayId, userId, groupIds, key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    @Override
    public boolean isFeatureEnabled(String gatewayId, String userId, List<String> groupIds, String featureName) {
        Map<String, String> prefs = getEffectiveConfig(gatewayId, userId, groupIds);
        String flagsJson = prefs.get(PreferenceKeys.Gateway.FEATURE_FLAGS);
        if (flagsJson != null) {
            Map<String, Boolean> flags = ConfigJsonParser.parseFeatureFlags(flagsJson);
            Boolean enabled = flags.get(featureName);
            if (enabled != null) {
                return enabled;
            }
        }
        return false;
    }

    @Override
    public Map<String, Boolean> getFeatureFlags(String gatewayId, String userId, List<String> groupIds) {
        String flagsJson = getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.FEATURE_FLAGS);
        return ConfigJsonParser.parseFeatureFlags(flagsJson);
    }

    @Override
    public boolean isMaintenanceMode(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanGatewayConfig(gatewayId, userId, groupIds, PreferenceKeys.Gateway.MAINTENANCE_MODE);
        return enabled != null ? enabled : false;
    }

    @Override
    public String getMaintenanceMessage(String gatewayId, String userId, List<String> groupIds) {
        return getConfigValue(gatewayId, userId, groupIds, PreferenceKeys.Gateway.MAINTENANCE_MESSAGE);
    }

    @Override
    @Transactional
    public void setGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId, String key, String value) {
        preferenceService.setPreference(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level, key, value);
    }

    @Override
    @Transactional
    public void setMaintenanceMode(String ownerId, PreferenceLevel level, String gatewayId, boolean enabled) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.MAINTENANCE_MODE, String.valueOf(enabled));
    }

    @Override
    @Transactional
    public void setMaintenanceMessage(String ownerId, PreferenceLevel level, String gatewayId, String message) {
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.MAINTENANCE_MESSAGE, message);
    }

    @Transactional
    private void setFeatureFlags(String ownerId, PreferenceLevel level, String gatewayId, Map<String, Boolean> flags) {
        String json = ConfigJsonParser.toJson(flags);
        setGatewayConfig(ownerId, level, gatewayId, PreferenceKeys.Gateway.FEATURE_FLAGS, json);
    }

    @Override
    @Transactional
    public void setFeatureFlag(
            String ownerId, PreferenceLevel level, String gatewayId, String featureName, boolean enabled) {
        Map<String, Boolean> flags = getFeatureFlagsAtLevel(ownerId, level, gatewayId);
        flags.put(featureName, enabled);
        setFeatureFlags(ownerId, level, gatewayId, flags);
    }

    @Override
    @Transactional
    public void deleteGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId, String key) {
        preferenceService.deletePreference(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level, key);
    }

    // ========================================================================
    // System Configuration
    // ========================================================================

    @Override
    public Map<String, String> getEffectiveSystemConfig(String gatewayId, String userId, List<String> groupIds) {
        Map<String, String> globalPrefs = preferenceService.resolvePreferences(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID, gatewayId, userId, groupIds);
        if (gatewayId != null && !gatewayId.equals(PreferenceResourceType.GLOBAL_RESOURCE_ID)) {
            Map<String, String> gatewayPrefs = preferenceService.resolvePreferences(
                    PreferenceResourceType.SYSTEM, gatewayId, gatewayId, userId, groupIds);
            globalPrefs.putAll(gatewayPrefs);
        }
        return globalPrefs;
    }

    @Override
    public String getSystemConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        Map<String, String> prefs = getEffectiveSystemConfig(gatewayId, userId, groupIds);
        return prefs.get(key);
    }

    @Override
    public String getGlobalDefault(String key) {
        Map<String, String> prefs = preferenceService.getPreferencesAtLevel(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID, PreferenceLevel.GATEWAY);
        return prefs.get(key);
    }

    @Override
    @Transactional
    public void setGlobalConfig(String key, String value) {
        preferenceService.setPreference(
                PreferenceResourceType.SYSTEM,
                PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceLevel.GATEWAY,
                key,
                value);
    }

    @Override
    @Transactional
    public void setGatewayOverride(String gatewayId, String key, String value) {
        preferenceService.setPreference(
                PreferenceResourceType.SYSTEM, gatewayId, gatewayId, PreferenceLevel.GATEWAY, key, value);
    }

    @Override
    @Transactional
    public void deleteGlobalConfig(String key) {
        preferenceService.deletePreference(
                PreferenceResourceType.SYSTEM,
                PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceLevel.GATEWAY,
                key);
    }

    @Override
    @Transactional
    public void deleteGatewayOverride(String gatewayId, String key) {
        preferenceService.deletePreference(
                PreferenceResourceType.SYSTEM, gatewayId, gatewayId, PreferenceLevel.GATEWAY, key);
    }

    // ========== Helper Methods ==========

    private Map<String, Boolean> getFeatureFlagsAtLevel(String ownerId, PreferenceLevel level, String gatewayId) {
        Map<String, String> prefs =
                preferenceService.getPreferencesAtLevel(PreferenceResourceType.GATEWAY, gatewayId, ownerId, level);
        String json = prefs.get(PreferenceKeys.Gateway.FEATURE_FLAGS);
        return ConfigJsonParser.parseFeatureFlags(json);
    }
}
