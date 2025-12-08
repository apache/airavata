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
package org.apache.airavata.sharing.repositories;

import java.util.List;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.entities.SharingPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SharingRepository extends JpaRepository<SharingEntity, SharingPK> {

    @Query("SELECT DISTINCT s FROM SharingEntity s WHERE s.domainId = :domainId AND s.inheritedParentId = :parentId "
            + "AND s.sharingType = :sharingType AND s.permissionTypeId = :permissionTypeId")
    List<SharingEntity> findIndirectSharedChildren(
            @Param("domainId") String domainId,
            @Param("parentId") String parentId,
            @Param("sharingType") String sharingType,
            @Param("permissionTypeId") String permissionTypeId);

    @Query("SELECT DISTINCT s FROM SharingEntity s WHERE s.domainId = :domainId AND s.entityId = :entityId "
            + "AND s.sharingType IN :sharingTypes ORDER BY s.createdTime DESC")
    List<SharingEntity> findCascadingPermissionsForEntity(
            @Param("domainId") String domainId,
            @Param("entityId") String entityId,
            @Param("sharingTypes") List<String> sharingTypes);

    @Query(
            "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SharingEntity s WHERE s.domainId = :domainId AND s.entityId = :entityId "
                    + "AND s.permissionTypeId IN :permissionTypeIds AND s.groupId IN :groupIds")
    boolean hasAccess(
            @Param("domainId") String domainId,
            @Param("entityId") String entityId,
            @Param("permissionTypeIds") List<String> permissionTypeIds,
            @Param("groupIds") List<String> groupIds);

    @Query("SELECT COUNT(s) FROM SharingEntity s WHERE s.domainId = :domainId AND s.entityId = :entityId "
            + "AND s.permissionTypeId <> :permissionTypeId AND s.sharingType <> :sharingType")
    int getSharedCount(
            @Param("domainId") String domainId,
            @Param("entityId") String entityId,
            @Param("permissionTypeId") String permissionTypeId,
            @Param("sharingType") String sharingType);

    @Modifying
    @Query("DELETE FROM SharingEntity s WHERE s.domainId = :domainId AND s.entityId = :entityId "
            + "AND s.sharingType = :sharingType")
    void removeAllIndirectCascadingPermissionsForEntity(
            @Param("domainId") String domainId,
            @Param("entityId") String entityId,
            @Param("sharingType") String sharingType);
}
