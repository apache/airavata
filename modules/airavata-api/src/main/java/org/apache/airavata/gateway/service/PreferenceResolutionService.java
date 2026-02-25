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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;
import org.apache.airavata.compute.resource.model.PreferenceValueType;
import org.apache.airavata.compute.resource.entity.ResourcePreferenceEntity;
import org.apache.airavata.compute.resource.repository.ResourcePreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service providing multi-level preference resolution.
 *
 * <p>Preferences are resolved in precedence order: USER > GROUP > GATEWAY.
 * This service is used by {@link ConfigService}
 * to read and write structured preferences from the RESOURCE_PREFERENCE table.
 */
@Service
@Transactional(readOnly = true)
public class PreferenceResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceResolutionService.class);

    private final ResourcePreferenceRepository preferenceRepository;

    public PreferenceResolutionService(ResourcePreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * Resolve effective preferences for a resource by merging all levels (GATEWAY, GROUP, USER).
     * USER preferences take highest precedence, then GROUP, then GATEWAY.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      gateway / group / user identifier used for GATEWAY level
     * @param userId       the user identifier for USER-level preferences
     * @param groupIds     list of group IDs for GROUP-level preferences
     * @return merged map of preference key to value
     */
    public Map<String, String> resolvePreferences(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            String userId,
            List<String> groupIds) {
        Map<String, String> result = new HashMap<>();

        // GATEWAY level (lowest precedence)
        List<ResourcePreferenceEntity> gatewayPrefs =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        resourceType, resourceId, ownerId, PreferenceLevel.GATEWAY);
        for (ResourcePreferenceEntity pref : gatewayPrefs) {
            result.put(pref.getKey(), pref.getValue());
        }

        // GROUP level (medium precedence) - last group wins if multiple groups set same key
        if (groupIds != null) {
            for (String groupId : groupIds) {
                List<ResourcePreferenceEntity> groupPrefs =
                        preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                                resourceType, resourceId, groupId, PreferenceLevel.GROUP);
                for (ResourcePreferenceEntity pref : groupPrefs) {
                    result.put(pref.getKey(), pref.getValue());
                }
            }
        }

        // USER level (highest precedence)
        if (userId != null) {
            List<ResourcePreferenceEntity> userPrefs =
                    preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            resourceType, resourceId, userId, PreferenceLevel.USER);
            for (ResourcePreferenceEntity pref : userPrefs) {
                result.put(pref.getKey(), pref.getValue());
            }
        }

        return result;
    }

    /**
     * Get preferences stored at a specific level for a specific owner.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     * @return map of preference key to value at that level
     */
    public Map<String, String> getPreferencesAtLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {
        List<ResourcePreferenceEntity> prefs = preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                resourceType, resourceId, ownerId, level);
        Map<String, String> result = new HashMap<>();
        for (ResourcePreferenceEntity pref : prefs) {
            result.put(pref.getKey(), pref.getValue());
        }
        return result;
    }

    /**
     * Set a single preference value.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     * @param key          the preference key
     * @param value        the preference value
     */
    @Transactional
    public void setPreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key,
            String value) {
        ResourcePreferenceEntity existing =
                preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                        resourceType, resourceId, ownerId, level, key);
        if (existing != null) {
            existing.setValue(value);
            preferenceRepository.save(existing);
        } else {
            ResourcePreferenceEntity entity = new ResourcePreferenceEntity();
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setOwnerId(ownerId);
            entity.setLevel(level);
            entity.setKey(key);
            entity.setValue(value);
            entity.setValueType(PreferenceValueType.STRING);
            entity.setEnforced(false);
            preferenceRepository.save(entity);
        }
    }

    /**
     * Delete a single preference.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     * @param key          the preference key to delete
     */
    @Transactional
    public void deletePreference(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level, String key) {
        preferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                resourceType, resourceId, ownerId, level, key);
    }

    /**
     * Delete all preferences for a resource at a specific owner/level.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     */
    @Transactional
    public void deleteAllPreferences(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {
        preferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                resourceType, resourceId, ownerId, level);
    }
}
