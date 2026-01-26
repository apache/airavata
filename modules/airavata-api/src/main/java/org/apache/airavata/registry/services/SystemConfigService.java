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
 * Service for managing system-wide configuration with optional gateway overrides.
 *
 * <p>This service provides multi-level (GATEWAY > GROUP > USER) preference resolution
 * for system-level settings. Unlike other preference services, system preferences
 * use "GLOBAL" as the default resource ID, but can be overridden per-gateway.
 *
 * <h3>Resource ID Format:</h3>
 * <ul>
 *   <li>"GLOBAL" - System-wide defaults that apply to all gateways</li>
 *   <li>{@code gatewayId} - Gateway-specific overrides of system settings</li>
 * </ul>
 *
 * <h3>Resolution Order:</h3>
 * <ol>
 *   <li>User-level override (if exists)</li>
 *   <li>Group-level override (if exists)</li>
 *   <li>Gateway-level override (if exists)</li>
 *   <li>GLOBAL default</li>
 * </ol>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Set system-wide rate limits and quotas</li>
 *   <li>Override limits for specific gateways</li>
 *   <li>Configure authentication providers</li>
 *   <li>Set system maintenance mode</li>
 * </ul>
 *
 * @see PreferenceKeys.System
 * @see PreferenceResourceType#SYSTEM
 */
@Service
@Transactional(readOnly = true)
public class SystemConfigService {

    private final PreferenceResolutionService preferenceService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SystemConfigService(PreferenceResolutionService preferenceService) {
        this.preferenceService = preferenceService;
        this.objectMapper = new ObjectMapper();
    }

    // ========== Resolution Methods ==========

    /**
     * Resolve effective system configuration for a user in a specific gateway.
     *
     * <p>This method first resolves GLOBAL preferences, then overlays gateway-specific
     * preferences if they exist.
     *
     * @param gatewayId the gateway ID (for gateway-level overrides)
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @return map of preference key to resolved value
     */
    public Map<String, String> getEffectiveSystemConfig(String gatewayId, String userId, List<String> groupIds) {
        // First get global defaults
        Map<String, String> globalPrefs = preferenceService.resolvePreferences(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID, gatewayId, userId, groupIds);

        // Then overlay gateway-specific overrides if the resource ID differs
        if (gatewayId != null && !gatewayId.equals(PreferenceResourceType.GLOBAL_RESOURCE_ID)) {
            Map<String, String> gatewayPrefs =
                    preferenceService.resolvePreferences(PreferenceResourceType.SYSTEM, gatewayId, gatewayId, userId, groupIds);
            // Gateway preferences override global
            globalPrefs.putAll(gatewayPrefs);
        }

        return globalPrefs;
    }

    /**
     * Get a specific system configuration value.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs
     * @param key the preference key
     * @return the resolved value, or null if not set
     */
    public String getSystemConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        Map<String, String> prefs = getEffectiveSystemConfig(gatewayId, userId, groupIds);
        return prefs.get(key);
    }

    /**
     * Get a global default value (ignoring any overrides).
     *
     * @param key the preference key
     * @return the global value, or null if not set
     */
    public String getGlobalDefault(String key) {
        Map<String, String> prefs = preferenceService.getPreferencesAtLevel(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID, PreferenceLevel.GATEWAY);
        return prefs.get(key);
    }

    /**
     * Get a boolean configuration value.
     */
    public Boolean getBooleanConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getSystemConfig(gatewayId, userId, groupIds, key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Get an integer configuration value.
     */
    public Integer getIntegerConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getSystemConfig(gatewayId, userId, groupIds, key);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get a long configuration value.
     */
    public Long getLongConfig(String gatewayId, String userId, List<String> groupIds, String key) {
        String value = getSystemConfig(gatewayId, userId, groupIds, key);
        return value != null ? Long.parseLong(value) : null;
    }

    // ========== Quota and Limit Methods ==========

    /**
     * Get the maximum experiments per user.
     */
    public Integer getMaxExperimentsPerUser(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.MAX_EXPERIMENTS_PER_USER);
    }

    /**
     * Get the maximum storage per user in bytes.
     */
    public Long getMaxStoragePerUser(String gatewayId, String userId, List<String> groupIds) {
        return getLongConfig(gatewayId, userId, groupIds, PreferenceKeys.System.MAX_STORAGE_PER_USER);
    }

    /**
     * Get the maximum upload size in bytes.
     */
    public Long getMaxUploadSize(String gatewayId, String userId, List<String> groupIds) {
        return getLongConfig(gatewayId, userId, groupIds, PreferenceKeys.System.MAX_UPLOAD_SIZE);
    }

    /**
     * Get the API rate limit (requests per minute per user).
     */
    public Integer getApiRateLimit(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.API_RATE_LIMIT);
    }

    // ========== Session and Credential Methods ==========

    /**
     * Get the default credential lifetime in seconds.
     */
    public Integer getDefaultCredentialLifetime(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.DEFAULT_CREDENTIAL_LIFETIME);
    }

    /**
     * Get the session timeout in seconds.
     */
    public Integer getSessionTimeout(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.SESSION_TIMEOUT);
    }

    /**
     * Get the enabled authentication providers.
     */
    public List<String> getEnabledAuthProviders(String gatewayId, String userId, List<String> groupIds) {
        String json = getSystemConfig(gatewayId, userId, groupIds, PreferenceKeys.System.ENABLED_AUTH_PROVIDERS);
        return parseJsonList(json);
    }

    // ========== Retention and Cleanup Methods ==========

    /**
     * Get the audit log retention period in days.
     */
    public Integer getAuditLogRetention(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.AUDIT_LOG_RETENTION);
    }

    /**
     * Get the experiment data retention period in days.
     */
    public Integer getExperimentDataRetention(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.EXPERIMENT_DATA_RETENTION);
    }

    // ========== Registration Settings ==========

    /**
     * Check if new gateway registration is allowed.
     */
    public boolean isGatewayRegistrationAllowed(String gatewayId, String userId, List<String> groupIds) {
        Boolean allowed = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.System.ALLOW_GATEWAY_REGISTRATION);
        return allowed != null ? allowed : true; // Default to allowed
    }

    /**
     * Check if user self-registration is allowed.
     */
    public boolean isUserRegistrationAllowed(String gatewayId, String userId, List<String> groupIds) {
        Boolean allowed = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.System.ALLOW_USER_REGISTRATION);
        return allowed != null ? allowed : true; // Default to allowed
    }

    // ========== System Maintenance ==========

    /**
     * Check if system is in maintenance mode.
     */
    public boolean isSystemMaintenanceMode(String gatewayId, String userId, List<String> groupIds) {
        Boolean enabled = getBooleanConfig(gatewayId, userId, groupIds, PreferenceKeys.System.SYSTEM_MAINTENANCE_MODE);
        return enabled != null ? enabled : false;
    }

    /**
     * Get the system maintenance message.
     */
    public String getSystemMaintenanceMessage(String gatewayId, String userId, List<String> groupIds) {
        return getSystemConfig(gatewayId, userId, groupIds, PreferenceKeys.System.SYSTEM_MAINTENANCE_MESSAGE);
    }

    // ========== Email Configuration ==========

    /**
     * Get the email sender address.
     */
    public String getEmailSender(String gatewayId, String userId, List<String> groupIds) {
        return getSystemConfig(gatewayId, userId, groupIds, PreferenceKeys.System.EMAIL_SENDER);
    }

    /**
     * Get the SMTP host.
     */
    public String getSmtpHost(String gatewayId, String userId, List<String> groupIds) {
        return getSystemConfig(gatewayId, userId, groupIds, PreferenceKeys.System.SMTP_HOST);
    }

    /**
     * Get the SMTP port.
     */
    public Integer getSmtpPort(String gatewayId, String userId, List<String> groupIds) {
        return getIntegerConfig(gatewayId, userId, groupIds, PreferenceKeys.System.SMTP_PORT);
    }

    // ========== Setter Methods ==========

    /**
     * Set a global system configuration.
     *
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setGlobalConfig(String key, String value) {
        preferenceService.setPreference(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID, PreferenceLevel.GATEWAY, key, value);
    }

    /**
     * Set a system configuration at a specific level.
     *
     * @param resourceId "GLOBAL" for system-wide, or gatewayId for gateway-specific
     * @param ownerId the owner ID
     * @param level the preference level
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setSystemConfig(String resourceId, String ownerId, PreferenceLevel level, String key, String value) {
        preferenceService.setPreference(PreferenceResourceType.SYSTEM, resourceId, ownerId, level, key, value);
    }

    /**
     * Set a gateway-specific override of a system setting.
     */
    @Transactional
    public void setGatewayOverride(String gatewayId, String key, String value) {
        preferenceService.setPreference(
                PreferenceResourceType.SYSTEM, gatewayId, gatewayId, PreferenceLevel.GATEWAY, key, value);
    }

    /**
     * Set the maximum experiments per user (globally).
     */
    @Transactional
    public void setMaxExperimentsPerUser(int max) {
        setGlobalConfig(PreferenceKeys.System.MAX_EXPERIMENTS_PER_USER, String.valueOf(max));
    }

    /**
     * Set the maximum storage per user in bytes (globally).
     */
    @Transactional
    public void setMaxStoragePerUser(long maxBytes) {
        setGlobalConfig(PreferenceKeys.System.MAX_STORAGE_PER_USER, String.valueOf(maxBytes));
    }

    /**
     * Set the default credential lifetime in seconds (globally).
     */
    @Transactional
    public void setDefaultCredentialLifetime(int seconds) {
        setGlobalConfig(PreferenceKeys.System.DEFAULT_CREDENTIAL_LIFETIME, String.valueOf(seconds));
    }

    /**
     * Set the session timeout in seconds (globally).
     */
    @Transactional
    public void setSessionTimeout(int seconds) {
        setGlobalConfig(PreferenceKeys.System.SESSION_TIMEOUT, String.valueOf(seconds));
    }

    /**
     * Set the API rate limit (globally).
     */
    @Transactional
    public void setApiRateLimit(int requestsPerMinute) {
        setGlobalConfig(PreferenceKeys.System.API_RATE_LIMIT, String.valueOf(requestsPerMinute));
    }

    /**
     * Set the enabled authentication providers (globally).
     */
    @Transactional
    public void setEnabledAuthProviders(List<String> providers) {
        try {
            String json = objectMapper.writeValueAsString(providers);
            setGlobalConfig(PreferenceKeys.System.ENABLED_AUTH_PROVIDERS, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize auth providers", e);
        }
    }

    /**
     * Enable or disable system maintenance mode (globally).
     */
    @Transactional
    public void setSystemMaintenanceMode(boolean enabled) {
        setGlobalConfig(PreferenceKeys.System.SYSTEM_MAINTENANCE_MODE, String.valueOf(enabled));
    }

    /**
     * Set the system maintenance message (globally).
     */
    @Transactional
    public void setSystemMaintenanceMessage(String message) {
        setGlobalConfig(PreferenceKeys.System.SYSTEM_MAINTENANCE_MESSAGE, message);
    }

    /**
     * Delete a global system configuration.
     */
    @Transactional
    public void deleteGlobalConfig(String key) {
        preferenceService.deletePreference(
                PreferenceResourceType.SYSTEM, PreferenceResourceType.GLOBAL_RESOURCE_ID,
                PreferenceResourceType.GLOBAL_RESOURCE_ID, PreferenceLevel.GATEWAY, key);
    }

    /**
     * Delete a gateway-specific override.
     */
    @Transactional
    public void deleteGatewayOverride(String gatewayId, String key) {
        preferenceService.deletePreference(
                PreferenceResourceType.SYSTEM, gatewayId, gatewayId, PreferenceLevel.GATEWAY, key);
    }

    /**
     * Delete a system configuration at a specific level.
     */
    @Transactional
    public void deleteSystemConfig(String resourceId, String ownerId, PreferenceLevel level, String key) {
        preferenceService.deletePreference(PreferenceResourceType.SYSTEM, resourceId, ownerId, level, key);
    }

    // ========== Helper Methods ==========

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
}
