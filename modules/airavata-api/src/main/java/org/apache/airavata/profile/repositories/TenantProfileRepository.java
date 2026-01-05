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
package org.apache.airavata.profile.repositories;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.profile.entities.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by goshenoy on 3/8/17.
 */
@Repository
public interface TenantProfileRepository extends JpaRepository<GatewayEntity, String> {

    @Query("SELECT g FROM GatewayEntity g WHERE g.airavataInternalGatewayId = :airavataInternalGatewayId")
    Optional<GatewayEntity> findByAiravataInternalGatewayId(
            @Param("airavataInternalGatewayId") String airavataInternalGatewayId);

    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayId = :gatewayId")
    Optional<GatewayEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    @Query("SELECT g FROM GatewayEntity g")
    List<GatewayEntity> findAllGateways();

    @Query("SELECT g FROM GatewayEntity g WHERE g.requesterUsername = :requesterUsername")
    List<GatewayEntity> findByRequesterUsername(@Param("requesterUsername") String requesterUsername);

    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayApprovalStatus IN :statuses "
            + "AND (g.gatewayId = :gatewayId OR g.gatewayName = :gatewayName OR g.gatewayUrl = :gatewayUrl)")
    List<GatewayEntity> findDuplicateGateways(
            @Param("statuses") List<String> statuses,
            @Param("gatewayId") String gatewayId,
            @Param("gatewayName") String gatewayName,
            @Param("gatewayUrl") String gatewayUrl);
}
