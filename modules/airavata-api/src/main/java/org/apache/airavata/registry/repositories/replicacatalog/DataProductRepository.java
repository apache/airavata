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
package org.apache.airavata.registry.repositories.replicacatalog;

import java.util.List;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity.Privacy;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity.ResourceScope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataProductRepository extends JpaRepository<DataProductEntity, String> {

    @Query("SELECT dp FROM DataProductEntity dp WHERE dp.parentProductUri = :parentProductUri")
    List<DataProductEntity> findByParentProductUri(@Param("parentProductUri") String parentProductUri);

    @Query("SELECT dp FROM DataProductEntity dp WHERE dp.gatewayId = :gatewayId AND dp.ownerName = :ownerName "
            + "AND dp.productName LIKE :productName")
    List<DataProductEntity> findByGatewayIdAndOwnerNameAndProductNameLike(
            @Param("gatewayId") String gatewayId,
            @Param("ownerName") String ownerName,
            @Param("productName") String productName);

    List<DataProductEntity> findByPrivacy(Privacy privacy);

    @Query(value = "SELECT * FROM DATA_PRODUCT dp WHERE dp.PRIVACY = :privacy "
            + "AND (:nameSearch IS NULL OR LOWER(CAST(dp.PRODUCT_NAME AS VARCHAR(512))) LIKE LOWER(CONCAT('%', :nameSearch, '%'))) "
            + "ORDER BY dp.CREATION_TIME DESC", nativeQuery = true)
    List<DataProductEntity> findPublicWithFilters(
            @Param("privacy") String privacy,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    List<DataProductEntity> findByGatewayId(String gatewayId);

    List<DataProductEntity> findByOwnerId(String ownerId);

    @Query("SELECT dp FROM DataProductEntity dp WHERE dp.primaryStorageResourceId = :storageResourceId")
    List<DataProductEntity> findByPrimaryStorageResourceId(@Param("storageResourceId") String storageResourceId);

    @Query(value = "SELECT DISTINCT dpt.TAG FROM DATA_PRODUCT dp "
            + "INNER JOIN DATA_PRODUCT_TAG dpt ON dp.PRODUCT_URI = dpt.PRODUCT_URI "
            + "WHERE dp.PRIVACY = 'PUBLIC'", nativeQuery = true)
    List<String> findAllPublicTags();

    @Query("SELECT dp FROM DataProductEntity dp WHERE "
            + "(dp.resourceScope = :scopeUser AND dp.ownerId = :userId) OR "
            + "(dp.resourceScope = :scopeGateway AND dp.gatewayId = :gatewayId) OR "
            + "(dp.groupResourceProfileId IN :groupIds)")
    List<DataProductEntity> findAccessibleResources(
            @Param("userId") String userId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("scopeUser") ResourceScope scopeUser,
            @Param("scopeGateway") ResourceScope scopeGateway,
            Pageable pageable);

    @Query(value = "SELECT * FROM DATA_PRODUCT dp WHERE "
            + "((dp.RESOURCE_SCOPE = :scopeUser AND dp.OWNER_ID = :userId) OR "
            + "(dp.RESOURCE_SCOPE = :scopeGateway AND dp.GATEWAY_ID = :gatewayId) OR "
            + "(dp.GROUP_RESOURCE_PROFILE_ID IN :groupIds)) "
            + "AND (:nameSearch IS NULL OR LOWER(CAST(dp.PRODUCT_NAME AS VARCHAR(512))) LIKE LOWER(CONCAT('%', :nameSearch, '%'))) "
            + "ORDER BY dp.CREATION_TIME DESC", nativeQuery = true)
    List<DataProductEntity> findAccessibleResourcesWithFilters(
            @Param("userId") String userId,
            @Param("gatewayId") String gatewayId,
            @Param("groupIds") List<String> groupIds,
            @Param("scopeUser") String scopeUser,
            @Param("scopeGateway") String scopeGateway,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);
}
