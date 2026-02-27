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
package org.apache.airavata.credential.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.credential.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for CredentialEntity.
 *
 * <p>The PK is now {@code credentialId} (the token string), so {@code findById()} can be
 * used directly for single-credential lookups.
 */
@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, String> {

    /**
     * Find credential by gateway id and credential ID.
     * Provides a gateway-scoped lookup for access-control purposes.
     */
    Optional<CredentialEntity> findByGatewayIdAndCredentialId(String gatewayId, String credentialId);

    /**
     * Find all credentials for a gateway.
     */
    List<CredentialEntity> findByGatewayId(String gatewayId);

    /**
     * Find all credentials for a gateway owned by a specific user.
     */
    List<CredentialEntity> findByGatewayIdAndUserId(String gatewayId, String userId);

    /**
     * Find credentials for a gateway with specific credential IDs.
     */
    @Query("SELECT c FROM CredentialEntity c WHERE c.gatewayId = :gatewayId AND c.credentialId IN :credentialIds")
    List<CredentialEntity> findByGatewayIdAndCredentialIdIn(
            @Param("gatewayId") String gatewayId, @Param("credentialIds") List<String> credentialIds);

    /**
     * Find gateway id by credential ID.
     */
    @Query("SELECT c.gatewayId FROM CredentialEntity c WHERE c.credentialId = :credentialId")
    Optional<String> findGatewayIdByCredentialId(@Param("credentialId") String credentialId);

    /**
     * Delete credential by gateway id and credential ID.
     */
    void deleteByGatewayIdAndCredentialId(String gatewayId, String credentialId);

    /**
     * Count references to this credential in RESOURCE_BINDING and CREDENTIAL_ALLOCATION_PROJECT.
     */
    @Query(
            nativeQuery = true,
            value =
                    "SELECT (SELECT COUNT(*) FROM resource_binding rb WHERE rb.credential_id = :credentialId)"
                            + " + (SELECT COUNT(*) FROM credential_allocation_project cap WHERE cap.credential_id = :credentialId)")
    int countReferences(@Param("credentialId") String credentialId);
}
