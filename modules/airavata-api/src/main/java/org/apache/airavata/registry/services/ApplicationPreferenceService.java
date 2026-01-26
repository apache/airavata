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
 * Service for managing application preferences and defaults.
 *
 * <p>This service provides multi-level (GATEWAY > GROUP > USER) preference resolution
 * for application settings including resource defaults, access control, and behavior.
 *
 * <h3>Resource ID Format:</h3>
 * The resource ID for application preferences is: {@code applicationInterfaceId}
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Set gateway-wide application defaults (compute resource, queue, etc.)</li>
 *   <li>Allow groups to have different defaults than the gateway</li>
 *   <li>Give specific users personalized application settings</li>
 *   <li>Control which users/groups can access which applications</li>
 * </ul>
 *
 * @see PreferenceKeys.Application
 * @see PreferenceResourceType#APPLICATION
 */
@Service
@Transactional(readOnly = true)
public class ApplicationPreferenceService {

    private final PreferenceResolutionService preferenceService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApplicationPreferenceService(PreferenceResolutionService preferenceService) {
        this.preferenceService = preferenceService;
        this.objectMapper = new ObjectMapper();
    }

    // ========== Resolution Methods ==========

    /**
     * Resolve effective application defaults for a user.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @param appInterfaceId the application interface ID
     * @return map of preference key to resolved value
     */
    public Map<String, String> getEffectiveAppDefaults(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {

        return preferenceService.resolvePreferences(
                PreferenceResourceType.APPLICATION, appInterfaceId, gatewayId, userId, groupIds);
    }

    /**
     * Get a specific application preference value.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs
     * @param appInterfaceId the application interface ID
     * @param key the preference key
     * @return the resolved value, or null if not set
     */
    public String getAppPreference(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId, String key) {

        Map<String, String> prefs = getEffectiveAppDefaults(gatewayId, userId, groupIds, appInterfaceId);
        return prefs.get(key);
    }

    /**
     * Get the default compute resource for an application.
     */
    public String getDefaultComputeResource(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        return getAppPreference(
                gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_COMPUTE_RESOURCE);
    }

    /**
     * Get the default queue for an application.
     */
    public String getDefaultQueue(String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        return getAppPreference(gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_QUEUE);
    }

    /**
     * Get the default walltime (in minutes) for an application.
     */
    public Integer getDefaultWalltime(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String value =
                getAppPreference(gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_WALLTIME);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default node count for an application.
     */
    public Integer getDefaultNodeCount(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String value =
                getAppPreference(gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_NODE_COUNT);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default CPU count for an application.
     */
    public Integer getDefaultCpuCount(String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String value =
                getAppPreference(gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_CPU_COUNT);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default memory (in MB) for an application.
     */
    public Integer getDefaultMemory(String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String value =
                getAppPreference(gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_MEMORY);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default input values as a map.
     */
    public Map<String, String> getDefaultInputValues(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String json = getAppPreference(
                gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.DEFAULT_INPUT_VALUES);
        return parseJsonMap(json);
    }

    // ========== Access Control Methods ==========

    /**
     * Check if an application is enabled for a user.
     *
     * <p>Access is determined by:
     * <ol>
     *   <li>If app is disabled (enabled=false), deny</li>
     *   <li>If user is in blockedUsers, deny</li>
     *   <li>If allowedUsers is set and user is not in it, deny</li>
     *   <li>If allowedGroups is set and user's groups don't intersect, deny</li>
     *   <li>Otherwise, allow</li>
     * </ol>
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @param appInterfaceId the application interface ID
     * @return true if the user can use the application
     */
    public boolean isAppEnabledForUser(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {

        Map<String, String> prefs = getEffectiveAppDefaults(gatewayId, userId, groupIds, appInterfaceId);

        // Check if app is enabled
        String enabledStr = prefs.get(PreferenceKeys.Application.ENABLED);
        if (enabledStr != null && !Boolean.parseBoolean(enabledStr)) {
            return false;
        }

        // Check blocked users
        List<String> blockedUsers = parseJsonList(prefs.get(PreferenceKeys.Application.BLOCKED_USERS));
        if (blockedUsers.contains(userId)) {
            return false;
        }

        // Check allowed users (if set, user must be in it)
        List<String> allowedUsers = parseJsonList(prefs.get(PreferenceKeys.Application.ALLOWED_USERS));
        if (!allowedUsers.isEmpty() && !allowedUsers.contains(userId)) {
            // Check allowed groups as fallback
            List<String> allowedGroups = parseJsonList(prefs.get(PreferenceKeys.Application.ALLOWED_GROUPS));
            if (allowedGroups.isEmpty() || groupIds == null || Collections.disjoint(groupIds, allowedGroups)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the maximum concurrent instances per user for an application.
     */
    public Integer getMaxConcurrentPerUser(
            String gatewayId, String userId, List<String> groupIds, String appInterfaceId) {
        String value = getAppPreference(
                gatewayId, userId, groupIds, appInterfaceId, PreferenceKeys.Application.MAX_CONCURRENT_PER_USER);
        return value != null ? Integer.parseInt(value) : null;
    }

    // ========== Setter Methods ==========

    /**
     * Set an application default at a specific level.
     *
     * @param ownerId the owner ID (gatewayId, groupId, or userId@gatewayId)
     * @param level the preference level
     * @param appInterfaceId the application interface ID
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setAppDefault(String ownerId, PreferenceLevel level, String appInterfaceId, String key, String value) {
        preferenceService.setPreference(PreferenceResourceType.APPLICATION, appInterfaceId, ownerId, level, key, value);
    }

    /**
     * Set the default compute resource for an application.
     */
    @Transactional
    public void setDefaultComputeResource(
            String ownerId, PreferenceLevel level, String appInterfaceId, String computeResourceId) {
        setAppDefault(ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_COMPUTE_RESOURCE,
                computeResourceId);
    }

    /**
     * Set the default queue for an application.
     */
    @Transactional
    public void setDefaultQueue(String ownerId, PreferenceLevel level, String appInterfaceId, String queueName) {
        setAppDefault(ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_QUEUE, queueName);
    }

    /**
     * Set the default walltime for an application.
     */
    @Transactional
    public void setDefaultWalltime(String ownerId, PreferenceLevel level, String appInterfaceId, int walltime) {
        setAppDefault(
                ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_WALLTIME, String.valueOf(walltime));
    }

    /**
     * Set the default node count for an application.
     */
    @Transactional
    public void setDefaultNodeCount(String ownerId, PreferenceLevel level, String appInterfaceId, int nodeCount) {
        setAppDefault(
                ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_NODE_COUNT,
                String.valueOf(nodeCount));
    }

    /**
     * Set the default CPU count for an application.
     */
    @Transactional
    public void setDefaultCpuCount(String ownerId, PreferenceLevel level, String appInterfaceId, int cpuCount) {
        setAppDefault(
                ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_CPU_COUNT, String.valueOf(cpuCount));
    }

    /**
     * Enable or disable an application at a specific level.
     */
    @Transactional
    public void setAppEnabled(String ownerId, PreferenceLevel level, String appInterfaceId, boolean enabled) {
        setAppDefault(ownerId, level, appInterfaceId, PreferenceKeys.Application.ENABLED, String.valueOf(enabled));
    }

    /**
     * Set default input values for an application.
     */
    @Transactional
    public void setDefaultInputValues(
            String ownerId, PreferenceLevel level, String appInterfaceId, Map<String, String> inputValues) {
        try {
            String json = objectMapper.writeValueAsString(inputValues);
            setAppDefault(ownerId, level, appInterfaceId, PreferenceKeys.Application.DEFAULT_INPUT_VALUES, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize input values", e);
        }
    }

    /**
     * Delete an application default at a specific level.
     */
    @Transactional
    public void deleteAppDefault(String ownerId, PreferenceLevel level, String appInterfaceId, String key) {
        preferenceService.deletePreference(PreferenceResourceType.APPLICATION, appInterfaceId, ownerId, level, key);
    }

    /**
     * Delete all application defaults at a specific level.
     */
    @Transactional
    public void deleteAllAppDefaults(String ownerId, PreferenceLevel level, String appInterfaceId) {
        preferenceService.deleteAllPreferences(PreferenceResourceType.APPLICATION, appInterfaceId, ownerId, level);
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

    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
