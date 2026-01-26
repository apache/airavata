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
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified Gateway Repository that combines functionality from TenantProfileRepository
 * and expcatalog GatewayRepository.
 *
 * <p>This repository provides access to gateways via multiple identifiers:
 * <ul>
 *   <li>airavataInternalGatewayId - the primary key (UUID)</li>
 *   <li>gatewayId - the human-readable gateway identifier</li>
 * </ul>
 *
 * <p>Supports all query methods from both original repositories:
 * <ul>
 *   <li>From TenantProfileRepository: findByAiravataInternalGatewayId, findByGatewayId,
 *       findAllGateways, findByRequesterUsername, findDuplicateGateways</li>
 *   <li>From expcatalog GatewayRepository: findByGatewayName</li>
 * </ul>
 */
@Repository
public interface GatewayRepository extends JpaRepository<GatewayEntity, String> {

    /**
     * Find a gateway by its internal Airavata ID (the primary key).
     *
     * @param airavataInternalGatewayId the internal gateway ID (UUID)
     * @return Optional containing the gateway if found
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.airavataInternalGatewayId = :airavataInternalGatewayId")
    Optional<GatewayEntity> findByAiravataInternalGatewayId(
            @Param("airavataInternalGatewayId") String airavataInternalGatewayId);

    /**
     * Find a gateway by its human-readable gateway ID.
     *
     * @param gatewayId the gateway ID
     * @return Optional containing the gateway if found
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayId = :gatewayId")
    Optional<GatewayEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    /**
     * Find all gateways.
     *
     * @return List of all gateway entities
     */
    @Query("SELECT g FROM GatewayEntity g")
    List<GatewayEntity> findAllGateways();

    /**
     * Find gateways by gateway name (supports partial matching with LIKE).
     *
     * @param gatewayName the gateway name pattern (e.g., "%science%" for partial match)
     * @return List of matching gateways
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayName LIKE :gatewayName")
    List<GatewayEntity> findByGatewayName(@Param("gatewayName") String gatewayName);

    /**
     * Find all gateways requested by a specific user.
     *
     * @param requesterUsername the username of the requester
     * @return List of gateways requested by the user
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.requesterUsername = :requesterUsername")
    List<GatewayEntity> findByRequesterUsername(@Param("requesterUsername") String requesterUsername);

    /**
     * Find potential duplicate gateways based on gatewayId, name, or URL.
     * Used to prevent creation of gateways with conflicting identifiers.
     *
     * @param statuses list of approval statuses to check (e.g., APPROVED, CREATED, DEPLOYED)
     * @param gatewayId the gateway ID to check for duplicates
     * @param gatewayName the gateway name to check for duplicates
     * @param gatewayUrl the gateway URL to check for duplicates
     * @return List of gateways that match any of the criteria
     */
    @Query(
            "SELECT g FROM GatewayEntity g WHERE g.gatewayApprovalStatus IN :statuses "
                    + "AND (g.gatewayId = :gatewayId OR g.gatewayName = :gatewayName OR g.gatewayUrl = :gatewayUrl)")
    List<GatewayEntity> findDuplicateGateways(
            @Param("statuses") List<GatewayApprovalStatus> statuses,
            @Param("gatewayId") String gatewayId,
            @Param("gatewayName") String gatewayName,
            @Param("gatewayUrl") String gatewayUrl);

    /**
     * Check if a gateway exists by its human-readable gateway ID.
     *
     * @param gatewayId the gateway ID
     * @return true if the gateway exists
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GatewayEntity g WHERE g.gatewayId = :gatewayId")
    boolean existsByGatewayId(@Param("gatewayId") String gatewayId);

    /**
     * Find gateways by approval status.
     *
     * @param status the approval status
     * @return List of gateways with the specified status
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayApprovalStatus = :status")
    List<GatewayEntity> findByGatewayApprovalStatus(@Param("status") GatewayApprovalStatus status);
}
