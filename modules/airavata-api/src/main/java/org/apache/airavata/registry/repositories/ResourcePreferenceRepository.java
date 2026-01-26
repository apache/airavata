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
package org.apache.airavata.registry.repositories;

import java.util.List;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing resource preferences stored as key-value pairs.
 */
@Repository
public interface ResourcePreferenceRepository
        extends JpaRepository<ResourcePreferenceEntity, ResourcePreferenceEntityPK> {

    /**
     * Find all preferences for a specific resource, owner, and level.
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);

    /**
     * Find all preferences for a specific resource and level (across all owners).
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceIdAndLevel(
            PreferenceResourceType resourceType, String resourceId, PreferenceLevel level);

    /**
     * Find all preferences for a resource at a specific level, for multiple owners (e.g., user's groups).
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceIdAndOwnerIdInAndLevel(
            PreferenceResourceType resourceType,
            String resourceId,
            List<String> ownerIds,
            PreferenceLevel level);

    /**
     * Find all preferences for a specific resource type, owner, and level.
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndOwnerIdAndLevel(
            PreferenceResourceType resourceType, String ownerId, PreferenceLevel level);

    /**
     * Find all preferences for a specific resource ID, owner, and level.
     */
    List<ResourcePreferenceEntity> findByResourceIdAndOwnerIdAndLevel(
            String resourceId, String ownerId, PreferenceLevel level);

    /**
     * Delete all preferences for a specific owner.
     */
    void deleteByOwnerId(String ownerId);

    /**
     * Find a specific preference key for a resource, owner, and level.
     */
    ResourcePreferenceEntity findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key);

    /**
     * Find all preferences for a specific resource and owner (at all levels).
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndResourceIdAndOwnerId(
            PreferenceResourceType resourceType, String resourceId, String ownerId);

    /**
     * Find all preferences for a specific owner and level.
     */
    List<ResourcePreferenceEntity> findByOwnerIdAndLevel(String ownerId, PreferenceLevel level);

    /**
     * Find all preferences of a specific resource type for an owner.
     */
    List<ResourcePreferenceEntity> findByResourceTypeAndOwnerId(
            PreferenceResourceType resourceType, String ownerId);

    /**
     * Delete all preferences for a specific resource, owner, and level.
     */
    void deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
            PreferenceResourceType resourceType, String resourceId, String ownerId, PreferenceLevel level);

    /**
     * Get all distinct resource IDs that have preferences for a given owner.
     */
    @Query(
            "SELECT DISTINCT r.resourceId FROM ResourcePreferenceEntity r "
                    + "WHERE r.resourceType = :resourceType AND r.ownerId = :ownerId")
    List<String> findDistinctResourceIdsByResourceTypeAndOwnerId(
            @Param("resourceType") PreferenceResourceType resourceType, @Param("ownerId") String ownerId);

    /**
     * Get all preferences for resolution: finds all preferences for a resource
     * at all specified owner/level combinations. Useful for bulk resolution.
     */
    @Query(
            "SELECT r FROM ResourcePreferenceEntity r "
                    + "WHERE r.resourceType = :resourceType "
                    + "AND r.resourceId = :resourceId "
                    + "AND ((r.ownerId = :gatewayId AND r.level = 'GATEWAY') "
                    + "    OR (r.ownerId IN :groupIds AND r.level = 'GROUP') "
                    + "    OR (r.ownerId = :userId AND r.level = 'USER')) "
                    + "ORDER BY r.level ASC")
    List<ResourcePreferenceEntity> findAllForResolution(
            @Param("resourceType") PreferenceResourceType resourceType,
            @Param("resourceId") String resourceId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("userId") String userId);
}
