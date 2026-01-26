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
 * Service for managing batch queue policies and limits.
 *
 * <p>This service provides multi-level (GATEWAY > GROUP > USER) preference resolution
 * for batch queue settings including resource limits, defaults, and access control.
 *
 * <h3>Resource ID Format:</h3>
 * The resource ID for batch queue preferences is: {@code computeResourceId:queueName}
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Set gateway-wide queue limits (max nodes, max walltime)</li>
 *   <li>Allow groups to have higher limits than the gateway default</li>
 *   <li>Give specific users custom limits</li>
 *   <li>Control which users/groups can access which queues</li>
 * </ul>
 *
 * @see PreferenceKeys.BatchQueue
 * @see PreferenceResourceType#BATCH_QUEUE
 */
@Service
@Transactional(readOnly = true)
public class BatchQueuePreferenceService {

    private final PreferenceResolutionService preferenceService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BatchQueuePreferenceService(PreferenceResolutionService preferenceService) {
        this.preferenceService = preferenceService;
        this.objectMapper = new ObjectMapper();
    }

    // ========== Resolution Methods ==========

    /**
     * Resolve effective queue limits for a user.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @param computeResourceId the compute resource ID
     * @param queueName the queue name
     * @return map of preference key to resolved value
     */
    public Map<String, String> getEffectiveQueueLimits(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {

        String resourceId = PreferenceResourceType.batchQueueResourceId(computeResourceId, queueName);
        return preferenceService.resolvePreferences(
                PreferenceResourceType.BATCH_QUEUE, resourceId, gatewayId, userId, groupIds);
    }

    /**
     * Get a specific queue limit value.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs
     * @param computeResourceId the compute resource ID
     * @param queueName the queue name
     * @param key the preference key
     * @return the resolved value, or null if not set
     */
    public String getQueueLimit(
            String gatewayId,
            String userId,
            List<String> groupIds,
            String computeResourceId,
            String queueName,
            String key) {

        Map<String, String> prefs = getEffectiveQueueLimits(gatewayId, userId, groupIds, computeResourceId, queueName);
        return prefs.get(key);
    }

    /**
     * Get the effective maximum nodes for a user on a queue.
     */
    public Integer getEffectiveMaxNodes(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_NODES);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the effective maximum CPUs for a user on a queue.
     */
    public Integer getEffectiveMaxCpus(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_CPUS);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the effective maximum walltime (in minutes) for a user on a queue.
     */
    public Integer getEffectiveMaxWalltime(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_WALLTIME);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default nodes for a user on a queue.
     */
    public Integer getDefaultNodes(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.DEFAULT_NODES);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default CPUs for a user on a queue.
     */
    public Integer getDefaultCpus(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.DEFAULT_CPUS);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the default walltime (in minutes) for a user on a queue.
     */
    public Integer getDefaultWalltime(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {
        String value = getQueueLimit(
                gatewayId, userId, groupIds, computeResourceId, queueName, PreferenceKeys.BatchQueue.DEFAULT_WALLTIME);
        return value != null ? Integer.parseInt(value) : null;
    }

    // ========== Access Control Methods ==========

    /**
     * Check if a user is allowed to use a specific queue.
     *
     * <p>Access is determined by:
     * <ol>
     *   <li>If queue is disabled (queueEnabled=false), deny</li>
     *   <li>If user is in blockedUsers, deny</li>
     *   <li>If user's groups are in blockedGroups, deny</li>
     *   <li>If allowedUsers is set and user is not in it, deny</li>
     *   <li>If allowedGroups is set and user's groups don't intersect, deny</li>
     *   <li>Otherwise, allow</li>
     * </ol>
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs the user belongs to
     * @param computeResourceId the compute resource ID
     * @param queueName the queue name
     * @return true if the user can use the queue
     */
    public boolean isUserAllowedOnQueue(
            String gatewayId, String userId, List<String> groupIds, String computeResourceId, String queueName) {

        Map<String, String> prefs = getEffectiveQueueLimits(gatewayId, userId, groupIds, computeResourceId, queueName);

        // Check if queue is enabled
        String enabledStr = prefs.get(PreferenceKeys.BatchQueue.QUEUE_ENABLED);
        if (enabledStr != null && !Boolean.parseBoolean(enabledStr)) {
            return false;
        }

        // Check blocked users
        List<String> blockedUsers = parseJsonList(prefs.get(PreferenceKeys.BatchQueue.BLOCKED_USERS));
        if (blockedUsers.contains(userId)) {
            return false;
        }

        // Check blocked groups
        List<String> blockedGroups = parseJsonList(prefs.get(PreferenceKeys.BatchQueue.BLOCKED_GROUPS));
        if (groupIds != null && !Collections.disjoint(groupIds, blockedGroups)) {
            return false;
        }

        // Check allowed users (if set, user must be in it)
        List<String> allowedUsers = parseJsonList(prefs.get(PreferenceKeys.BatchQueue.ALLOWED_USERS));
        if (!allowedUsers.isEmpty() && !allowedUsers.contains(userId)) {
            // Check allowed groups as fallback
            List<String> allowedGroups = parseJsonList(prefs.get(PreferenceKeys.BatchQueue.ALLOWED_GROUPS));
            if (allowedGroups.isEmpty() || groupIds == null || Collections.disjoint(groupIds, allowedGroups)) {
                return false;
            }
        }

        return true;
    }

    // ========== Setter Methods ==========

    /**
     * Set a queue policy at a specific level.
     *
     * @param ownerId the owner ID (gatewayId, groupId, or userId@gatewayId)
     * @param level the preference level
     * @param computeResourceId the compute resource ID
     * @param queueName the queue name
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setQueuePolicy(
            String ownerId,
            PreferenceLevel level,
            String computeResourceId,
            String queueName,
            String key,
            String value) {

        String resourceId = PreferenceResourceType.batchQueueResourceId(computeResourceId, queueName);
        preferenceService.setPreference(PreferenceResourceType.BATCH_QUEUE, resourceId, ownerId, level, key, value);
    }

    /**
     * Set the maximum nodes at a specific level.
     */
    @Transactional
    public void setMaxNodes(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName, int maxNodes) {
        setQueuePolicy(
                ownerId, level, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_NODES,
                String.valueOf(maxNodes));
    }

    /**
     * Set the maximum CPUs at a specific level.
     */
    @Transactional
    public void setMaxCpus(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName, int maxCpus) {
        setQueuePolicy(
                ownerId, level, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_CPUS,
                String.valueOf(maxCpus));
    }

    /**
     * Set the maximum walltime at a specific level.
     */
    @Transactional
    public void setMaxWalltime(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName, int maxWalltime) {
        setQueuePolicy(
                ownerId, level, computeResourceId, queueName, PreferenceKeys.BatchQueue.MAX_WALLTIME,
                String.valueOf(maxWalltime));
    }

    /**
     * Enable or disable a queue at a specific level.
     */
    @Transactional
    public void setQueueEnabled(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName, boolean enabled) {
        setQueuePolicy(
                ownerId, level, computeResourceId, queueName, PreferenceKeys.BatchQueue.QUEUE_ENABLED,
                String.valueOf(enabled));
    }

    /**
     * Delete a queue policy at a specific level.
     */
    @Transactional
    public void deleteQueuePolicy(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName, String key) {

        String resourceId = PreferenceResourceType.batchQueueResourceId(computeResourceId, queueName);
        preferenceService.deletePreference(PreferenceResourceType.BATCH_QUEUE, resourceId, ownerId, level, key);
    }

    /**
     * Delete all queue policies at a specific level.
     */
    @Transactional
    public void deleteAllQueuePolicies(
            String ownerId, PreferenceLevel level, String computeResourceId, String queueName) {

        String resourceId = PreferenceResourceType.batchQueueResourceId(computeResourceId, queueName);
        preferenceService.deleteAllPreferences(PreferenceResourceType.BATCH_QUEUE, resourceId, ownerId, level);
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
