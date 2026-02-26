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
package org.apache.airavata.compute.resource.repository;

import java.util.List;
import org.apache.airavata.compute.resource.entity.ResourcePreferenceEntity;
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ResourcePreferenceEntity}.
 *
 * <p>Supports multi-level preference lookup (GATEWAY, GROUP, USER) across different resource types.
 */
@Repository
public interface ResourcePreferenceRepository extends JpaRepository<ResourcePreferenceEntity, Long> {

    /**
     * Find a single preference by its full unique key tuple.
     */
    ResourcePreferenceEntity findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level, String key);

    /**
     * Find all preferences for a given resource and owner at a specific level.
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);

    /**
     * Find all preferences for a given resource type and resource ID across all owners and levels.
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceId(
            PreferenceResourceType resourceType, String resourceId);

    /**
     * Delete a preference by its full unique key tuple.
     */
    void deleteByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level, String key);

    /**
     * Delete all preferences for a resource at a specific owner/level.
     */
    void deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);
}
