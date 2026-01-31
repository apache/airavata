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
import java.util.Optional;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing resource access grants.
 */
@Repository
public interface ResourceAccessRepository extends JpaRepository<ResourceAccessEntity, Long> {

    /**
     * Find all access grants for a specific resource.
     */
    List<ResourceAccessEntity> findByResourceTypeAndResourceId(
            PreferenceResourceType resourceType, String resourceId);

    /**
     * Find all access grants for a specific resource and owner type.
     */
    List<ResourceAccessEntity> findByResourceTypeAndResourceIdAndOwnerType(
            PreferenceResourceType resourceType, String resourceId, PreferenceLevel ownerType);

    /**
     * Find all access grants for a specific owner.
     */
    List<ResourceAccessEntity> findByOwnerIdAndOwnerType(String ownerId, PreferenceLevel ownerType);

    /**
     * Find access grant for a specific owner and resource.
     */
    Optional<ResourceAccessEntity> findByResourceTypeAndResourceIdAndOwnerIdAndOwnerType(
            PreferenceResourceType resourceType, 
            String resourceId, 
            String ownerId, 
            PreferenceLevel ownerType);

    /**
     * Find all enabled access grants for a resource.
     */
    List<ResourceAccessEntity> findByResourceTypeAndResourceIdAndEnabledTrue(
            PreferenceResourceType resourceType, String resourceId);

    /**
     * Find all access grants for a gateway.
     */
    List<ResourceAccessEntity> findByGatewayId(String gatewayId);

    /**
     * Find all access grants for a gateway and resource type.
     */
    List<ResourceAccessEntity> findByGatewayIdAndResourceType(
            String gatewayId, PreferenceResourceType resourceType);

    /**
     * Find all enabled access grants for an owner across all resources.
     */
    List<ResourceAccessEntity> findByOwnerIdAndOwnerTypeAndEnabledTrue(
            String ownerId, PreferenceLevel ownerType);

    /**
     * Find all access grants using a specific credential.
     */
    List<ResourceAccessEntity> findByCredentialToken(String credentialToken);

    /**
     * Delete all access grants for a specific owner.
     */
    void deleteByOwnerIdAndOwnerType(String ownerId, PreferenceLevel ownerType);

    /**
     * Delete access grant for a specific owner and resource.
     */
    void deleteByResourceTypeAndResourceIdAndOwnerIdAndOwnerType(
            PreferenceResourceType resourceType, 
            String resourceId, 
            String ownerId, 
            PreferenceLevel ownerType);

    /**
     * Check if an access grant exists for a specific owner and resource.
     */
    boolean existsByResourceTypeAndResourceIdAndOwnerIdAndOwnerType(
            PreferenceResourceType resourceType, 
            String resourceId, 
            String ownerId, 
            PreferenceLevel ownerType);

    /**
     * Find all access grants for resolution (gateway + groups + user) for a specific resource.
     * Results are ordered by owner type for hierarchical resolution.
     */
    @Query("SELECT ra FROM ResourceAccessEntity ra "
            + "WHERE ra.resourceType = :resourceType "
            + "AND ra.resourceId = :resourceId "
            + "AND ra.enabled = true "
            + "AND ((ra.ownerId = :gatewayId AND ra.ownerType = 'GATEWAY') "
            + "    OR (ra.ownerId IN :groupIds AND ra.ownerType = 'GROUP') "
            + "    OR (ra.ownerId = :userId AND ra.ownerType = 'USER')) "
            + "ORDER BY ra.ownerType ASC")
    List<ResourceAccessEntity> findAllForResolution(
            @Param("resourceType") PreferenceResourceType resourceType,
            @Param("resourceId") String resourceId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("userId") String userId);

    /**
     * Find all resources a user has access to (directly or through groups/gateway).
     */
    @Query("SELECT DISTINCT ra.resourceId FROM ResourceAccessEntity ra "
            + "WHERE ra.resourceType = :resourceType "
            + "AND ra.enabled = true "
            + "AND ((ra.ownerId = :gatewayId AND ra.ownerType = 'GATEWAY') "
            + "    OR (ra.ownerId IN :groupIds AND ra.ownerType = 'GROUP') "
            + "    OR (ra.ownerId = :userId AND ra.ownerType = 'USER'))")
    List<String> findAccessibleResourceIds(
            @Param("resourceType") PreferenceResourceType resourceType,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("userId") String userId);
}
