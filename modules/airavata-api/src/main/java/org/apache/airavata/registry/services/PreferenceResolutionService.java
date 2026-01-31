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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.PreferenceKeys;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.ProfileOwnerType;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntityPK;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.repositories.ResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ResourceProfileRepository;
import org.apache.airavata.service.security.CredentialStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for resolving effective preferences using 3-level hierarchy (Zanzibar-like).
 *
 * <p>Resolution order (lower priority number = higher authority):
 * SYSTEM (0) > GATEWAY (1) > GROUP (2)
 *
 * <p>When multiple groups at GROUP level have the same preference key, the user
 * must explicitly select which group's value to use (conflict resolution).
 */
@Service
@Transactional(readOnly = true)
public class PreferenceResolutionService {

    private final ResourcePreferenceRepository preferenceRepository;
    private final UserGroupSelectionService selectionService;
    private final CredentialStoreService credentialStoreService;
    private final ResourceProfileRepository resourceProfileRepository;

    @Autowired
    public PreferenceResolutionService(
            ResourcePreferenceRepository preferenceRepository,
            UserGroupSelectionService selectionService,
            CredentialStoreService credentialStoreService,
            ResourceProfileRepository resourceProfileRepository) {
        this.preferenceRepository = preferenceRepository;
        this.selectionService = selectionService;
        this.credentialStoreService = credentialStoreService;
        this.resourceProfileRepository = resourceProfileRepository;
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
        ResolvedPreferencesResult result = resolvePreferencesWithConflicts(
                resourceType, resourceId, gatewayId, userId, groupIds);
        return result.getResolved();
    }

    /**
     * Resolve preferences with conflict detection. When multiple groups have the same key,
     * returns conflict info and uses explicit selection if set.
     */
    public ResolvedPreferencesResult resolvePreferencesWithConflicts(
            PreferenceResourceType resourceType,
            String resourceId,
            String gatewayId,
            String userId,
            List<String> groupIds) {

        String airavataInternalUserId = userId.contains("@") ? userId : userId + "@" + gatewayId;
        List<String> effectiveGroupIds = new ArrayList<>(groupIds != null ? groupIds : Collections.emptyList());
        String personalGroupId = airavataInternalUserId + "_personal";
        if (!effectiveGroupIds.contains(personalGroupId)) {
            effectiveGroupIds.add(personalGroupId);
        }

        List<ResourcePreferenceEntity> allPreferences = preferenceRepository.findAllForResolution(
                resourceType, resourceId, gatewayId, effectiveGroupIds, airavataInternalUserId);

        return resolveByPriorityWithConflicts(
                allPreferences, airavataInternalUserId, gatewayId, resourceType, resourceId, effectiveGroupIds);
    }

    /**
     * Resolve preferences by 3-level hierarchy with conflict detection.
     * Lower priority number = higher authority (SYSTEM=0, GATEWAY=1, GROUP=2).
     */
    private ResolvedPreferencesResult resolveByPriorityWithConflicts(
            List<ResourcePreferenceEntity> preferences,
            String userId,
            String domainId,
            PreferenceResourceType resourceType,
            String resourceId,
            List<String> userGroupIds) {

        Map<String, List<ResourcePreferenceEntity>> byKey =
                preferences.stream().collect(Collectors.groupingBy(ResourcePreferenceEntity::getKey));

        Map<String, String> resolved = new HashMap<>();
        Set<String> conflictKeys = new HashSet<>();
        Map<String, List<GroupPreferenceOption>> conflictOptions = new HashMap<>();

        for (Map.Entry<String, List<ResourcePreferenceEntity>> entry : byKey.entrySet()) {
            String key = entry.getKey();
            List<ResourcePreferenceEntity> keyPrefs = entry.getValue();

            ResourcePreferenceEntity enforcedPref = keyPrefs.stream()
                    .filter(ResourcePreferenceEntity::isEnforced)
                    .min((a, b) -> Integer.compare(a.getLevel().getPriority(), b.getLevel().getPriority()))
                    .orElse(null);

            if (enforcedPref != null && enforcedPref.getValue() != null) {
                resolved.put(key, enforcedPref.getValue());
                continue;
            }

            List<ResourcePreferenceEntity> groupLevelPrefs = keyPrefs.stream()
                    .filter(p -> p.getLevel().isGroupLevel())
                    .collect(Collectors.toList());

            if (groupLevelPrefs.isEmpty()) {
                // No group-level: most specific wins = highest priority number (GROUP/USER > GATEWAY > SYSTEM)
                ResourcePreferenceEntity winner = keyPrefs.stream()
                        .max((a, b) -> Integer.compare(a.getLevel().getPriority(), b.getLevel().getPriority()))
                        .orElse(null);
                if (winner != null && winner.getValue() != null) {
                    resolved.put(key, winner.getValue());
                }
                continue;
            }

            if (groupLevelPrefs.size() == 1) {
                resolved.put(key, groupLevelPrefs.get(0).getValue());
                continue;
            }

            // User's personal preference (USER level or GROUP for personal group) wins without requiring selection
            String personalGroupId = userId + "_personal";
            ResourcePreferenceEntity personalPref = groupLevelPrefs.stream()
                    .filter(p -> (p.getLevel() == PreferenceLevel.USER && userId.equals(p.getOwnerId()))
                            || (p.getLevel() == PreferenceLevel.GROUP && personalGroupId.equals(p.getOwnerId())))
                    .findFirst()
                    .orElse(null);
            if (personalPref != null && personalPref.getValue() != null) {
                resolved.put(key, personalPref.getValue());
                continue;
            }

            String selectedGroupId = selectionService.getSelectedGroupId(
                    userId, domainId, resourceType.name(), resourceId, key);

            if (selectedGroupId != null && userGroupIds.contains(selectedGroupId)) {
                String value = groupLevelPrefs.stream()
                        .filter(p -> selectedGroupId.equals(p.getOwnerId()))
                        .map(ResourcePreferenceEntity::getValue)
                        .findFirst()
                        .orElse(null);
                if (value != null) {
                    resolved.put(key, value);
                    continue;
                }
            }

            conflictKeys.add(key);
            conflictOptions.put(key, groupLevelPrefs.stream()
                    .map(p -> new GroupPreferenceOption(p.getOwnerId(), p.getValue()))
                    .collect(Collectors.toList()));
        }

        return new ResolvedPreferencesResult(resolved, conflictKeys, conflictOptions);
    }

    /**
     * Result of preference resolution including conflicts that require explicit selection.
     */
    public static class ResolvedPreferencesResult {
        private final Map<String, String> resolved;
        private final Set<String> conflictKeys;
        private final Map<String, List<GroupPreferenceOption>> conflictOptions;

        public ResolvedPreferencesResult(
                Map<String, String> resolved,
                Set<String> conflictKeys,
                Map<String, List<GroupPreferenceOption>> conflictOptions) {
            this.resolved = resolved != null ? resolved : new HashMap<>();
            this.conflictKeys = conflictKeys != null ? conflictKeys : new HashSet<>();
            this.conflictOptions = conflictOptions != null ? conflictOptions : new HashMap<>();
        }

        public Map<String, String> getResolved() {
            return resolved;
        }

        public Set<String> getConflictKeys() {
            return conflictKeys;
        }

        public Map<String, List<GroupPreferenceOption>> getConflictOptions() {
            return conflictOptions;
        }
    }

    /**
     * Option for a preference key when multiple groups have values (conflict).
     */
    public static class GroupPreferenceOption {
        private final String groupId;
        private final String value;

        public GroupPreferenceOption(String groupId, String value) {
            this.groupId = groupId;
            this.value = value;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getValue() {
            return value;
        }
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

        // Validate credential exists when saving resource-specific credential token
        if (PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN.equals(key) && value != null) {
            String gatewayId = resolveGatewayIdForPreference(ownerId, level);
            if (gatewayId == null || !credentialStoreService.credentialExists(value, gatewayId)) {
                throw new IllegalArgumentException("Credential does not exist for token: " + value);
            }
        }

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

    /**
     * Resolve gateway ID from preference owner/level for credential validation.
     * GATEWAY: ownerId is gatewayId; USER: ownerId is userId@gatewayId; GROUP: load profile.
     */
    private String resolveGatewayIdForPreference(String ownerId, PreferenceLevel level) {
        return switch (level) {
            case GATEWAY -> ownerId;
            case USER -> ownerId.contains("@") ? ownerId.substring(ownerId.indexOf('@') + 1) : null;
            case GROUP -> {
                ResourceProfileEntity profile = resourceProfileRepository
                        .findById(new ResourceProfileEntityPK(ownerId, ProfileOwnerType.GROUP))
                        .orElse(null);
                yield profile != null ? profile.getGatewayId() : null;
            }
            default -> null;
        };
    }
}
