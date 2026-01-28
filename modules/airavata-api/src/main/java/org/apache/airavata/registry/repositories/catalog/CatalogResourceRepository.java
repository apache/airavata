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
package org.apache.airavata.registry.repositories.catalog;

import java.util.List;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.Privacy;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.ResourceScope;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.ResourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Research Catalog Resources.
 * 
 * <p>Resource types: DATASET, REPOSITORY</p>
 * <p>Resource scopes stored in DB: USER, GATEWAY (DELEGATED is inferred at service layer)</p>
 */
@Repository
public interface CatalogResourceRepository extends JpaRepository<CatalogResourceEntity, String> {

    /**
     * Find all public resources by privacy level
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE r.privacy = :privacy")
    List<CatalogResourceEntity> findByPrivacy(@Param("privacy") Privacy privacy);

    /**
     * Find public resources with optional type filter (DATASET or REPOSITORY)
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE r.privacy = :privacy AND (:type IS NULL OR r.type = :type)")
    List<CatalogResourceEntity> findByPrivacyAndType(
            @Param("privacy") Privacy privacy, @Param("type") ResourceType type, Pageable pageable);

    /**
     * Find resources by gateway ID
     */
    List<CatalogResourceEntity> findByGatewayId(String gatewayId);

    /**
     * Find resources by owner ID
     */
    List<CatalogResourceEntity> findByOwnerId(String ownerId);

    /**
     * Search resources by name (case-insensitive)
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE r.privacy = :privacy AND LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CatalogResourceEntity> searchByName(@Param("privacy") Privacy privacy, @Param("query") String query);

    /**
     * Search public resources with type and name filters
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE r.privacy = 'PUBLIC' "
            + "AND (:type IS NULL OR r.type = :type) "
            + "AND (:nameSearch IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    List<CatalogResourceEntity> findPublicWithFilters(
            @Param("type") ResourceType type, @Param("nameSearch") String nameSearch, Pageable pageable);

    /**
     * Get all distinct tags from public resources.
     * Uses native query to access the CATALOG_RESOURCE_TAG collection table directly.
     */
    @Query(value = "SELECT DISTINCT crt.TAG FROM CATALOG_RESOURCE cr " +
           "INNER JOIN CATALOG_RESOURCE_TAG crt ON cr.RESOURCE_ID = crt.RESOURCE_ID " +
           "WHERE cr.PRIVACY = 'PUBLIC'", nativeQuery = true)
    List<String> findAllPublicTags();

    /**
     * Find all accessible resources for a user.
     * 
     * <p>Returns resources where:</p>
     * <ul>
     *   <li>scope = USER and ownerId = userId (directly owned by user)</li>
     *   <li>scope = GATEWAY and gatewayId = gatewayId (directly owned by gateway)</li>
     *   <li>groupResourceProfileId IN groupIds (accessible via group delegation)</li>
     * </ul>
     * 
     * <p>Note: The service layer will infer DELEGATED scope for resources accessible via groups
     * but not directly owned. Only USER and GATEWAY are stored in the database.</p>
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE " +
           "(r.scope = 'USER' AND r.ownerId = :userId) OR " +
           "(r.scope = 'GATEWAY' AND r.gatewayId = :gatewayId) OR " +
           "(r.groupResourceProfileId IN :groupIds)")
    List<CatalogResourceEntity> findAccessibleResources(
            @Param("userId") String userId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            Pageable pageable);

    /**
     * Find accessible resources with type filter (DATASET or REPOSITORY)
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE " +
           "((r.scope = 'USER' AND r.ownerId = :userId) OR " +
           "(r.scope = 'GATEWAY' AND r.gatewayId = :gatewayId) OR " +
           "(r.groupResourceProfileId IN :groupIds)) " +
           "AND (:type IS NULL OR r.type = :type)")
    List<CatalogResourceEntity> findAccessibleResourcesByType(
            @Param("userId") String userId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("type") ResourceType type,
            Pageable pageable);

    /**
     * Find accessible resources with type and name search filters
     */
    @Query("SELECT r FROM CatalogResourceEntity r WHERE " +
           "((r.scope = 'USER' AND r.ownerId = :userId) OR " +
           "(r.scope = 'GATEWAY' AND r.gatewayId = :gatewayId) OR " +
           "(r.groupResourceProfileId IN :groupIds)) " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:nameSearch IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    List<CatalogResourceEntity> findAccessibleResourcesWithFilters(
            @Param("userId") String userId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("type") ResourceType type,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);
}
