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
package org.apache.airavata.credential.store.repository;

import org.apache.airavata.credential.store.store.impl.db.CredentialsEntity;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Timestamp;

/**
 * Repository for CredentialsEntity providing CRUD operations
 * and custom query methods for credential management.
 */
public class CredentialsRepository extends AbstractCredentialStoreRepository<CredentialsEntity, CredentialsEntity, CredentialsEntity.CredentialsPK> {

    public CredentialsRepository() {
        super(CredentialsEntity.class, CredentialsEntity.class);
    }

    @Override
    protected CredentialsEntity mapToEntity(CredentialsEntity entity) {
        return entity;
    }

    @Override
    protected CredentialsEntity mapFromEntity(CredentialsEntity entity) {
        return entity;
    }

    /**
     * Find all credentials by gateway ID
     * @param gatewayId the gateway ID
     * @return list of credentials for the gateway
     */
    public List<CredentialsEntity> findByGatewayId(String gatewayId) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        return select(queryString, parameters);
    }

    /**
     * Find all credentials by token ID
     * @param tokenId the token ID
     * @return list of credentials with the given token ID
     */
    public List<CredentialsEntity> findByTokenId(String tokenId) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.tokenId = :tokenId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tokenId", tokenId);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by gateway ID and token ID
     * @param gatewayId the gateway ID
     * @param tokenId the token ID
     * @return list of credentials matching the criteria
     */
    public List<CredentialsEntity> findByGatewayIdAndTokenId(String gatewayId, String tokenId) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId AND c.tokenId = :tokenId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("tokenId", tokenId);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by portal user ID
     * @param portalUserId the portal user ID
     * @return list of credentials for the portal user
     */
    public List<CredentialsEntity> findByPortalUserId(String portalUserId) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.portalUserId = :portalUserId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("portalUserId", portalUserId);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by gateway ID and portal user ID
     * @param gatewayId the gateway ID
     * @param portalUserId the portal user ID
     * @return list of credentials matching the criteria
     */
    public List<CredentialsEntity> findByGatewayIdAndPortalUserId(String gatewayId, String portalUserId) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId AND c.portalUserId = :portalUserId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("portalUserId", portalUserId);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by credential owner type
     * @param credentialOwnerType the credential owner type
     * @return list of credentials with the given owner type
     */
    public List<CredentialsEntity> findByCredentialOwnerType(CredentialOwnerType credentialOwnerType) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.credentialOwnerType = :credentialOwnerType";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("credentialOwnerType", credentialOwnerType);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by gateway ID and credential owner type
     * @param gatewayId the gateway ID
     * @param credentialOwnerType the credential owner type
     * @return list of credentials matching the criteria
     */
    public List<CredentialsEntity> findByGatewayIdAndCredentialOwnerType(String gatewayId, CredentialOwnerType credentialOwnerType) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId AND c.credentialOwnerType = :credentialOwnerType";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("credentialOwnerType", credentialOwnerType);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by description
     * @param description the description
     * @return list of credentials with the given description
     */
    public List<CredentialsEntity> findByDescription(String description) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.description = :description";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("description", description);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by partial description
     * @param partialDescription partial description
     * @return list of credentials with descriptions containing the partial description
     */
    public List<CredentialsEntity> findByDescriptionContaining(String partialDescription) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.description LIKE :partialDescription";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("partialDescription", "%" + partialDescription + "%");
        return select(queryString, parameters);
    }

    /**
     * Find credentials by time persisted range
     * @param startTime the start time
     * @param endTime the end time
     * @return list of credentials persisted within the time range
     */
    public List<CredentialsEntity> findByTimePersistedBetween(Timestamp startTime, Timestamp endTime) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.timePersisted BETWEEN :startTime AND :endTime";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startTime", startTime);
        parameters.put("endTime", endTime);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by time persisted after
     * @param time the time threshold
     * @return list of credentials persisted after the given time
     */
    public List<CredentialsEntity> findByTimePersistedAfter(Timestamp time) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.timePersisted > :time";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("time", time);
        return select(queryString, parameters);
    }

    /**
     * Find credentials by time persisted before
     * @param time the time threshold
     * @return list of credentials persisted before the given time
     */
    public List<CredentialsEntity> findByTimePersistedBefore(Timestamp time) {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.timePersisted < :time";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("time", time);
        return select(queryString, parameters);
    }

    /**
     * Check if a credential exists by gateway ID and token ID
     * @param gatewayId the gateway ID
     * @param tokenId the token ID
     * @return true if the credential exists, false otherwise
     */
    public boolean existsByGatewayIdAndTokenId(String gatewayId, String tokenId) {
        CredentialsEntity.CredentialsPK pk = new CredentialsEntity.CredentialsPK(gatewayId, tokenId);
        return exists(pk);
    }

    /**
     * Delete credentials by gateway ID
     * @param gatewayId the gateway ID
     */
    public void deleteByGatewayId(String gatewayId) {
        List<CredentialsEntity> credentials = findByGatewayId(gatewayId);
        for (CredentialsEntity credential : credentials) {
            CredentialsEntity.CredentialsPK pk = new CredentialsEntity.CredentialsPK(
                credential.getGatewayId(), credential.getTokenId());
            delete(pk);
        }
    }

    /**
     * Delete credentials by token ID
     * @param tokenId the token ID
     */
    public void deleteByTokenId(String tokenId) {
        List<CredentialsEntity> credentials = findByTokenId(tokenId);
        for (CredentialsEntity credential : credentials) {
            CredentialsEntity.CredentialsPK pk = new CredentialsEntity.CredentialsPK(
                credential.getGatewayId(), credential.getTokenId());
            delete(pk);
        }
    }

    /**
     * Delete credentials by portal user ID
     * @param portalUserId the portal user ID
     */
    public void deleteByPortalUserId(String portalUserId) {
        List<CredentialsEntity> credentials = findByPortalUserId(portalUserId);
        for (CredentialsEntity credential : credentials) {
            CredentialsEntity.CredentialsPK pk = new CredentialsEntity.CredentialsPK(
                credential.getGatewayId(), credential.getTokenId());
            delete(pk);
        }
    }

    /**
     * Count credentials by gateway ID
     * @param gatewayId the gateway ID
     * @return count of credentials for the gateway
     */
    public long countByGatewayId(String gatewayId) {
        String queryString = "SELECT COUNT(c) FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId";
        List<Long> result = execute(entityManager -> {
            jakarta.persistence.Query query = entityManager.createQuery(queryString);
            query.setParameter("gatewayId", gatewayId);
            return query.getResultList();
        });
        return result.isEmpty() ? 0 : result.get(0);
    }

    /**
     * Count credentials by portal user ID
     * @param portalUserId the portal user ID
     * @return count of credentials for the portal user
     */
    public long countByPortalUserId(String portalUserId) {
        String queryString = "SELECT COUNT(c) FROM CredentialsEntity c WHERE c.portalUserId = :portalUserId";
        List<Long> result = execute(entityManager -> {
            jakarta.persistence.Query query = entityManager.createQuery(queryString);
            query.setParameter("portalUserId", portalUserId);
            return query.getResultList();
        });
        return result.isEmpty() ? 0 : result.get(0);
    }

    /**
     * Find distinct portal user IDs by gateway ID
     * @param gatewayId the gateway ID
     * @return list of distinct portal user IDs for the gateway
     */
    public List<String> findDistinctPortalUserIdsByGatewayId(String gatewayId) {
        String queryString = "SELECT DISTINCT c.portalUserId FROM CredentialsEntity c WHERE c.gatewayId = :gatewayId";
        return execute(entityManager -> {
            jakarta.persistence.Query query = entityManager.createQuery(queryString);
            query.setParameter("gatewayId", gatewayId);
            return query.getResultList();
        });
    }

    /**
     * Find credentials with null descriptions
     * @return list of credentials with null descriptions
     */
    public List<CredentialsEntity> findByDescriptionIsNull() {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.description IS NULL";
        return select(queryString);
    }

    /**
     * Find credentials with non-null descriptions
     * @return list of credentials with non-null descriptions
     */
    public List<CredentialsEntity> findByDescriptionIsNotNull() {
        String queryString = "SELECT c FROM CredentialsEntity c WHERE c.description IS NOT NULL";
        return select(queryString);
    }
}
