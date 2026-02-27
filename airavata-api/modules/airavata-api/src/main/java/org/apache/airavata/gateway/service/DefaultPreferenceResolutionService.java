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
import org.apache.airavata.compute.resource.entity.ResourcePreferenceEntity;
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;
import org.apache.airavata.compute.resource.model.PreferenceValueType;
import org.apache.airavata.compute.resource.repository.ResourcePreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link PreferenceResolutionService}.
 *
 * <p>Preferences are resolved in precedence order: USER > GROUP > GATEWAY.
 * This service is used by {@link ConfigService}
 * to read and write structured preferences from the RESOURCE_PREFERENCE table.
 */
@Service
@Transactional(readOnly = true)
public class DefaultPreferenceResolutionService implements PreferenceResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPreferenceResolutionService.class);

    private final ResourcePreferenceRepository preferenceRepository;

    public DefaultPreferenceResolutionService(ResourcePreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
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

        // GROUP level for user's personal group (highest precedence among regular principals).
        // New writes use GROUP with ownerId = user's personal group ID.
        // Legacy USER-level records in the DB are no longer written but may still exist
        // until a migration updates preference_level = 'USER' rows to 'GROUP'.
        if (userId != null) {
            List<ResourcePreferenceEntity> userPrefs =
                    preferenceRepository.findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            resourceType, resourceId, userId, PreferenceLevel.GROUP);
            for (ResourcePreferenceEntity pref : userPrefs) {
                result.put(pref.getKey(), pref.getValue());
            }
        }

        return result;
    }

    @Override
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

    @Override
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

    @Override
    @Transactional
    public void deletePreference(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level, String key) {
        preferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                resourceType, resourceId, ownerId, level, key);
    }

    @Override
    @Transactional
    public void deleteAllPreferences(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level) {
        preferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                resourceType, resourceId, ownerId, level);
    }
}
