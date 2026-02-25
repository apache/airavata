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
package org.apache.airavata.gateway.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified Gateway Repository. PK = gatewayId (UUID), UK = gatewayName (slug for URLs).
 *
 * <p>Lookup by slug: {@link #findByGatewayName(String)}. Lookup by PK: {@link #findById}.
 */
@Repository
public interface GatewayRepository extends JpaRepository<GatewayEntity, String> {

    /**
     * Find a gateway by its name (slug). Primary lookup for gateway-scoped operations and URLs.
     *
     * @param gatewayName the gateway name (slug)
     * @return Optional containing the gateway if found
     */
    @Query("SELECT g FROM GatewayEntity g WHERE g.gatewayName = :gatewayName")
    Optional<GatewayEntity> findByGatewayName(@Param("gatewayName") String gatewayName);

    /**
     * Find all gateways.
     *
     * @return List of all gateway entities
     */
    @Query("SELECT g FROM GatewayEntity g")
    List<GatewayEntity> findAllGateways();

    /**
     * Check if a gateway exists by its name (slug).
     *
     * @param gatewayName the gateway name (slug)
     * @return true if the gateway exists
     */
    @Query(
            "SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GatewayEntity g WHERE g.gatewayName = :gatewayName")
    boolean existsByGatewayName(@Param("gatewayName") String gatewayName);

    /**
     * Lookup by gateway name (slug) or by primary key (UUID).
     * Tries slug first, then PK. Use when caller may pass either.
     */
    default Optional<GatewayEntity> findByGatewayNameOrId(String nameOrId) {
        return findByGatewayName(nameOrId).or(() -> findById(nameOrId));
    }
}
