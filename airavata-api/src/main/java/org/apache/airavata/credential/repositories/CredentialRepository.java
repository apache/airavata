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
package org.apache.airavata.credential.repositories;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.credential.entities.CredentialEntity;
import org.apache.airavata.credential.entities.CredentialEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for CredentialEntity.
 */
@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, CredentialEntityPK> {

    /**
     * Find credential by gateway ID and token ID.
     */
    Optional<CredentialEntity> findByGatewayIdAndTokenId(String gatewayId, String tokenId);

    /**
     * Find all credentials for a gateway.
     */
    List<CredentialEntity> findByGatewayId(String gatewayId);

    /**
     * Find credentials for a gateway with specific token IDs.
     */
    @Query("SELECT c FROM CredentialEntity c WHERE c.gatewayId = :gatewayId AND c.tokenId IN :tokenIds")
    List<CredentialEntity> findByGatewayIdAndTokenIdIn(
            @Param("gatewayId") String gatewayId, @Param("tokenIds") List<String> tokenIds);

    /**
     * Find gateway ID by token ID.
     */
    @Query("SELECT c.gatewayId FROM CredentialEntity c WHERE c.tokenId = :tokenId")
    Optional<String> findGatewayIdByTokenId(@Param("tokenId") String tokenId);

    /**
     * Delete credential by gateway ID and token ID.
     */
    void deleteByGatewayIdAndTokenId(String gatewayId, String tokenId);
}
