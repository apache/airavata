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
package org.apache.airavata.iam.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.iam.entity.SharingPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SharingPermissionRepository extends JpaRepository<SharingPermissionEntity, String> {

    Optional<SharingPermissionEntity> findByDomainIdAndResourceTypeAndResourceIdAndGranteeTypeAndGranteeIdAndPermission(
            String domainId, String resourceType, String resourceId, String granteeType, String granteeId, String permission);

    List<SharingPermissionEntity> findByDomainIdAndResourceTypeAndResourceId(
            String domainId, String resourceType, String resourceId);

    List<SharingPermissionEntity> findByDomainIdAndResourceTypeAndGranteeTypeAndGranteeId(
            String domainId, String resourceType, String granteeType, String granteeId);

    @Query("""
            SELECT COUNT(e) > 0
            FROM SharingPermissionEntity e
            WHERE e.domainId = :domainId
              AND e.resourceType = :resourceType
              AND e.resourceId = :resourceId
              AND e.permission IN :permissions
              AND e.granteeId IN :granteeIds
            """)
    boolean hasAccess(
            @Param("domainId") String domainId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
            @Param("permissions") List<String> permissions,
            @Param("granteeIds") List<String> granteeIds);

    @Query("""
            SELECT CAST(COUNT(e) AS int)
            FROM SharingPermissionEntity e
            WHERE e.domainId = :domainId
              AND e.resourceType = :resourceType
              AND e.resourceId = :resourceId
              AND e.permission <> :excludePermission
            """)
    int countExcludingPermission(
            @Param("domainId") String domainId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
            @Param("excludePermission") String excludePermission);
}
