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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationDeploymentRepository extends JpaRepository<ApplicationDeploymentEntity, String> {

    @Query("SELECT ad FROM ApplicationDeploymentEntity ad WHERE ad.gatewayId LIKE :gatewayId")
    List<ApplicationDeploymentEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    @Query("SELECT ad FROM ApplicationDeploymentEntity ad WHERE ad.appModuleId LIKE :appModuleId")
    List<ApplicationDeploymentEntity> findByAppModuleId(@Param("appModuleId") String appModuleId);

    @Query("SELECT ad FROM ApplicationDeploymentEntity ad WHERE ad.computeHostId LIKE :computeHostId")
    List<ApplicationDeploymentEntity> findByComputeHostId(@Param("computeHostId") String computeHostId);

    @Query("SELECT ad FROM ApplicationDeploymentEntity ad WHERE ad.gatewayId LIKE :gatewayId "
            + "AND ad.appDeploymentId IN :accessibleAppIds AND ad.computeHostId IN :accessibleCompHostIds")
    List<ApplicationDeploymentEntity> findAccessibleApplicationDeployments(
            @Param("gatewayId") String gatewayId,
            @Param("accessibleAppIds") List<String> accessibleAppIds,
            @Param("accessibleCompHostIds") List<String> accessibleCompHostIds);

    @Query("SELECT ad FROM ApplicationDeploymentEntity ad WHERE ad.gatewayId LIKE :gatewayId "
            + "AND ad.appModuleId = :appModuleId AND ad.appDeploymentId IN :accessibleAppIds "
            + "AND ad.computeHostId IN :accessibleCompHostIds")
    List<ApplicationDeploymentEntity> findAccessibleApplicationDeploymentsForAppModule(
            @Param("gatewayId") String gatewayId,
            @Param("appModuleId") String appModuleId,
            @Param("accessibleAppIds") List<String> accessibleAppIds,
            @Param("accessibleCompHostIds") List<String> accessibleCompHostIds);
}
