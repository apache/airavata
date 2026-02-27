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
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;

/**
 * Service providing multi-level preference resolution.
 *
 * <p>Preferences are resolved in precedence order: USER > GROUP > GATEWAY.
 * This service is used by {@link ConfigService}
 * to read and write structured preferences from the RESOURCE_PREFERENCE table.
 */
public interface PreferenceResolutionService {

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
    Map<String, String> resolvePreferences(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            String userId,
            List<String> groupIds);

    /**
     * Get preferences stored at a specific level for a specific owner.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     * @return map of preference key to value at that level
     */
    Map<String, String> getPreferencesAtLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);

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
    void setPreference(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key,
            String value);

    /**
     * Delete a single preference.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     * @param key          the preference key to delete
     */
    void deletePreference(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level, String key);

    /**
     * Delete all preferences for a resource at a specific owner/level.
     *
     * @param resourceType type of the resource
     * @param resourceId   identifier of the resource
     * @param ownerId      the owner identifier
     * @param level        the preference level
     */
    void deleteAllPreferences(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);
}
