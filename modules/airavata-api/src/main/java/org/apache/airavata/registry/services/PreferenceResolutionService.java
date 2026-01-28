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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.repositories.ResourcePreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for resolving effective preferences using level-based precedence.
 *
 * <p>Resolution follows "most specific wins" strategy:
 * USER (priority 2) > GROUP (priority 1) > GATEWAY (priority 0)
 *
 * <p>For each preference key, the service checks levels in order of priority
 * and returns the first non-null value found.
 */
@Service
@Transactional(readOnly = true)
public class PreferenceResolutionService {

    private final ResourcePreferenceRepository preferenceRepository;

    @Autowired
    public PreferenceResolutionService(ResourcePreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * Resolve effective preferences for a compute resource.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID (will be combined with gatewayId as userId@gatewayId)
     * @param userGroupIds list of group IDs the user belongs to
     * @param computeResourceId the compute resource ID
     * @return map of preference key to resolved value
     */
    public Map<String, String> resolveComputePreferences(
            String gatewayId, String userId, List<String> userGroupIds, String computeResourceId) {
        return resolvePreferences(
                PreferenceResourceType.COMPUTE, computeResourceId, gatewayId, userId, userGroupIds);
    }

    /**
     * Resolve effective preferences for a storage resource.
     *
     * @param gatewayId the gateway ID
     * @param userId the user ID (will be combined with gatewayId as userId@gatewayId)
     * @param userGroupIds list of group IDs the user belongs to
     * @param storageResourceId the storage resource ID
     * @return map of preference key to resolved value
     */
    public Map<String, String> resolveStoragePreferences(
            String gatewayId, String userId, List<String> userGroupIds, String storageResourceId) {
        return resolvePreferences(
                PreferenceResourceType.STORAGE, storageResourceId, gatewayId, userId, userGroupIds);
    }

    /**
     * Resolve effective preferences for any resource type.
     *
     * <p>Resolution algorithm:
     * <ol>
     *   <li>Fetch all preferences at all levels in one query</li>
     *   <li>Group by preference key</li>
     *   <li>For each key, pick the value from the highest priority level</li>
     * </ol>
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param gatewayId the gateway ID (for GATEWAY level)
     * @param userId the user ID (for USER level, combined as userId@gatewayId)
     * @param groupIds list of group IDs (for GROUP level)
     * @return map of preference key to resolved value
     */
    public Map<String, String> resolvePreferences(
            PreferenceResourceType resourceType,
            String resourceId,
            String gatewayId,
            String userId,
            List<String> groupIds) {

        String airavataInternalUserId = userId + "@" + gatewayId;
        List<String> effectiveGroupIds = groupIds != null ? groupIds : Collections.emptyList();

        // Fetch all preferences at all levels in one query
        List<ResourcePreferenceEntity> allPreferences = preferenceRepository.findAllForResolution(
                resourceType, resourceId, gatewayId, effectiveGroupIds, airavataInternalUserId);

        // Group by key and resolve
        return resolveByPriority(allPreferences);
    }

    /**
     * Resolve preferences by priority, respecting the enforced flag.
     *
     * <p>Resolution algorithm:
     * <ol>
     *   <li>For each key, first check for any enforced preference at a higher level</li>
     *   <li>If an enforced preference exists, use it (ignoring lower-level overrides)</li>
     *   <li>Otherwise, use the value from the highest priority level (most specific wins)</li>
     * </ol>
     *
     * <p>Enforced preferences enable top-down control: a GATEWAY admin can set a preference
     * with enforced=true to prevent GROUP or USER levels from overriding it.
     *
     * @param preferences list of preferences at various levels
     * @return map of key to resolved value
     */
    private Map<String, String> resolveByPriority(List<ResourcePreferenceEntity> preferences) {
        // Group preferences by key
        Map<String, List<ResourcePreferenceEntity>> byKey =
                preferences.stream().collect(Collectors.groupingBy(ResourcePreferenceEntity::getKey));

        Map<String, String> resolved = new HashMap<>();

        for (Map.Entry<String, List<ResourcePreferenceEntity>> entry : byKey.entrySet()) {
            String key = entry.getKey();
            List<ResourcePreferenceEntity> keyPrefs = entry.getValue();

            // First, check for any enforced preference (lower priority level = higher in hierarchy)
            // GATEWAY (priority 0) > GROUP (priority 1) > USER (priority 2) when enforced
            ResourcePreferenceEntity enforcedPref = keyPrefs.stream()
                    .filter(ResourcePreferenceEntity::isEnforced)
                    .min((a, b) -> Integer.compare(a.getLevel().getPriority(), b.getLevel().getPriority()))
                    .orElse(null);

            if (enforcedPref != null && enforcedPref.getValue() != null) {
                // Use the enforced preference (highest in hierarchy that's enforced)
                resolved.put(key, enforcedPref.getValue());
            } else {
                // No enforced preference, use most specific wins (highest priority level)
                ResourcePreferenceEntity winner = keyPrefs.stream()
                        .max((a, b) -> Integer.compare(a.getLevel().getPriority(), b.getLevel().getPriority()))
                        .orElse(null);

                if (winner != null && winner.getValue() != null) {
                    resolved.put(key, winner.getValue());
                }
            }
        }

        return resolved;
    }

    /**
     * Get a specific preference value, resolved across all levels.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param gatewayId the gateway ID
     * @param userId the user ID
     * @param groupIds list of group IDs
     * @param key the preference key
     * @return the resolved value, or null if not found at any level
     */
    public String resolvePreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String gatewayId,
            String userId,
            List<String> groupIds,
            String key) {
        Map<String, String> allPrefs = resolvePreferences(resourceType, resourceId, gatewayId, userId, groupIds);
        return allPrefs.get(key);
    }

    /**
     * Get all preferences at a specific level (unresolved) as simple key-value pairs.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param ownerId the owner ID (gatewayId, groupId, or userId@gatewayId)
     * @param level the preference level
     * @return map of key to value at that level
     */
    public Map<String, String> getPreferencesAtLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {

        List<ResourcePreferenceEntity> prefs =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        resourceType, resourceId, ownerId, level);

        return prefs.stream()
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toMap(ResourcePreferenceEntity::getKey, ResourcePreferenceEntity::getValue));
    }

    /**
     * Detailed preference information including enforcement status.
     */
    public static class PreferenceDetail {
        private String key;
        private String value;
        private boolean enforced;

        public PreferenceDetail(String key, String value, boolean enforced) {
            this.key = key;
            this.value = value;
            this.enforced = enforced;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public boolean isEnforced() {
            return enforced;
        }
    }

    /**
     * Get all preferences at a specific level with detailed information including enforcement status.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param ownerId the owner ID (gatewayId, groupId, or userId@gatewayId)
     * @param level the preference level
     * @return list of preference details at that level
     */
    public List<PreferenceDetail> getPreferencesAtLevelDetailed(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {

        List<ResourcePreferenceEntity> prefs =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        resourceType, resourceId, ownerId, level);

        return prefs.stream()
                .filter(p -> p.getValue() != null)
                .map(p -> new PreferenceDetail(p.getKey(), p.getValue(), p.isEnforced()))
                .collect(Collectors.toList());
    }

    /**
     * Set a preference at a specific level.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param ownerId the owner ID
     * @param level the preference level
     * @param key the preference key
     * @param value the preference value
     */
    @Transactional
    public void setPreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key,
            String value) {
        setPreference(resourceType, resourceId, ownerId, level, key, value, false);
    }

    /**
     * Set a preference at a specific level with optional enforcement.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param ownerId the owner ID
     * @param level the preference level
     * @param key the preference key
     * @param value the preference value
     * @param enforced if true, this preference cannot be overridden by lower-level preferences
     */
    @Transactional
    public void setPreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key,
            String value,
            boolean enforced) {

        ResourcePreferenceEntity existing =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                        resourceType, resourceId, ownerId, level, key);

        if (existing != null) {
            existing.setValue(value);
            existing.setEnforced(enforced);
            preferenceRepository.save(existing);
        } else {
            ResourcePreferenceEntity entity = new ResourcePreferenceEntity();
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setOwnerId(ownerId);
            entity.setLevel(level);
            entity.setKey(key);
            entity.setTypedValue(value);
            entity.setEnforced(enforced);
            preferenceRepository.save(entity);
        }
    }

    /**
     * Delete a preference at a specific level.
     *
     * @param resourceType COMPUTE or STORAGE
     * @param resourceId the resource ID
     * @param ownerId the owner ID
     * @param level the preference level
     * @param key the preference key
     */
    @Transactional
    public void deletePreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key) {

        ResourcePreferenceEntity existing =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                        resourceType, resourceId, ownerId, level, key);

        if (existing != null) {
            preferenceRepository.delete(existing);
        }
    }

    /**
     * Delete all preferences for a resource at a specific level.
     */
    @Transactional
    public void deleteAllPreferences(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {

        preferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                resourceType, resourceId, ownerId, level);
    }
}
