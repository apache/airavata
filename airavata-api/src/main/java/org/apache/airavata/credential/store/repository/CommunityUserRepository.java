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

import org.apache.airavata.credential.store.store.impl.db.CommunityUserEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Repository for CommunityUserEntity providing CRUD operations
 * and custom query methods for community user management.
 */
public class CommunityUserRepository extends AbstractCredentialStoreRepository<CommunityUserEntity, CommunityUserEntity, CommunityUserEntity.CommunityUserPK> {

    public CommunityUserRepository() {
        super(CommunityUserEntity.class, CommunityUserEntity.class);
    }

    @Override
    protected CommunityUserEntity mapToEntity(CommunityUserEntity entity) {
        return entity;
    }

    @Override
    protected CommunityUserEntity mapFromEntity(CommunityUserEntity entity) {
        return entity;
    }

    /**
     * Find all community users by gateway ID
     * @param gatewayId the gateway ID
     * @return list of community users for the gateway
     */
    public List<CommunityUserEntity> findByGatewayId(String gatewayId) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        return select(queryString, parameters);
    }

    /**
     * Find all community users by community user name
     * @param communityUserName the community user name
     * @return list of community users with the given name
     */
    public List<CommunityUserEntity> findByCommunityUserName(String communityUserName) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.communityUserName = :communityUserName";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("communityUserName", communityUserName);
        return select(queryString, parameters);
    }

    /**
     * Find all community users by token ID
     * @param tokenId the token ID
     * @return list of community users with the given token ID
     */
    public List<CommunityUserEntity> findByTokenId(String tokenId) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.tokenId = :tokenId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tokenId", tokenId);
        return select(queryString, parameters);
    }

    /**
     * Find community users by gateway ID and community user name
     * @param gatewayId the gateway ID
     * @param communityUserName the community user name
     * @return list of community users matching the criteria
     */
    public List<CommunityUserEntity> findByGatewayIdAndCommunityUserName(String gatewayId, String communityUserName) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId AND c.communityUserName = :communityUserName";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("communityUserName", communityUserName);
        return select(queryString, parameters);
    }

    /**
     * Find community users by gateway ID and token ID
     * @param gatewayId the gateway ID
     * @param tokenId the token ID
     * @return list of community users matching the criteria
     */
    public List<CommunityUserEntity> findByGatewayIdAndTokenId(String gatewayId, String tokenId) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId AND c.tokenId = :tokenId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("tokenId", tokenId);
        return select(queryString, parameters);
    }

    /**
     * Find community users by community user name and token ID
     * @param communityUserName the community user name
     * @param tokenId the token ID
     * @return list of community users matching the criteria
     */
    public List<CommunityUserEntity> findByCommunityUserNameAndTokenId(String communityUserName, String tokenId) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.communityUserName = :communityUserName AND c.tokenId = :tokenId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("communityUserName", communityUserName);
        parameters.put("tokenId", tokenId);
        return select(queryString, parameters);
    }

    /**
     * Find community users by email address
     * @param communityUserEmail the community user email
     * @return list of community users with the given email
     */
    public List<CommunityUserEntity> findByCommunityUserEmail(String communityUserEmail) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.communityUserEmail = :communityUserEmail";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("communityUserEmail", communityUserEmail);
        return select(queryString, parameters);
    }

    /**
     * Find community users by gateway ID and email address
     * @param gatewayId the gateway ID
     * @param communityUserEmail the community user email
     * @return list of community users matching the criteria
     */
    public List<CommunityUserEntity> findByGatewayIdAndCommunityUserEmail(String gatewayId, String communityUserEmail) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId AND c.communityUserEmail = :communityUserEmail";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        parameters.put("communityUserEmail", communityUserEmail);
        return select(queryString, parameters);
    }

    /**
     * Check if a community user exists by gateway ID, community user name, and token ID
     * @param gatewayId the gateway ID
     * @param communityUserName the community user name
     * @param tokenId the token ID
     * @return true if the community user exists, false otherwise
     */
    public boolean existsByGatewayIdAndCommunityUserNameAndTokenId(String gatewayId, String communityUserName, String tokenId) {
        CommunityUserEntity.CommunityUserPK pk = new CommunityUserEntity.CommunityUserPK(gatewayId, communityUserName, tokenId);
        return exists(pk);
    }

    /**
     * Delete community users by gateway ID
     * @param gatewayId the gateway ID
     */
    public void deleteByGatewayId(String gatewayId) {
        List<CommunityUserEntity> users = findByGatewayId(gatewayId);
        for (CommunityUserEntity user : users) {
            CommunityUserEntity.CommunityUserPK pk = new CommunityUserEntity.CommunityUserPK(
                user.getGatewayId(), user.getCommunityUserName(), user.getTokenId());
            delete(pk);
        }
    }

    /**
     * Delete community users by token ID
     * @param tokenId the token ID
     */
    public void deleteByTokenId(String tokenId) {
        List<CommunityUserEntity> users = findByTokenId(tokenId);
        for (CommunityUserEntity user : users) {
            CommunityUserEntity.CommunityUserPK pk = new CommunityUserEntity.CommunityUserPK(
                user.getGatewayId(), user.getCommunityUserName(), user.getTokenId());
            delete(pk);
        }
    }

    /**
     * Delete community users by gateway ID and community user name
     * @param gatewayId the gateway ID
     * @param communityUserName the community user name
     */
    public void deleteByGatewayIdAndCommunityUserName(String gatewayId, String communityUserName) {
        List<CommunityUserEntity> users = findByGatewayIdAndCommunityUserName(gatewayId, communityUserName);
        for (CommunityUserEntity user : users) {
            CommunityUserEntity.CommunityUserPK pk = new CommunityUserEntity.CommunityUserPK(
                user.getGatewayId(), user.getCommunityUserName(), user.getTokenId());
            delete(pk);
        }
    }

    /**
     * Find community users by partial community user name
     * @param partialName partial community user name
     * @return list of community users with names containing the partial name
     */
    public List<CommunityUserEntity> findByCommunityUserNameContaining(String partialName) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.communityUserName LIKE :partialName";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("partialName", "%" + partialName + "%");
        return select(queryString, parameters);
    }

    /**
     * Find community users by partial email address
     * @param partialEmail partial email address
     * @return list of community users with emails containing the partial email
     */
    public List<CommunityUserEntity> findByCommunityUserEmailContaining(String partialEmail) {
        String queryString = "SELECT c FROM CommunityUserEntity c WHERE c.communityUserEmail LIKE :partialEmail";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("partialEmail", "%" + partialEmail + "%");
        return select(queryString, parameters);
    }

    /**
     * Count community users by gateway ID
     * @param gatewayId the gateway ID
     * @return count of community users for the gateway
     */
    public long countByGatewayId(String gatewayId) {
        String queryString = "SELECT COUNT(c) FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        List<Long> result = execute(entityManager -> {
            jakarta.persistence.Query query = entityManager.createQuery(queryString);
            query.setParameter("gatewayId", gatewayId);
            return query.getResultList();
        });
        return result.isEmpty() ? 0 : result.get(0);
    }

    /**
     * Find distinct community user names by gateway ID
     * @param gatewayId the gateway ID
     * @return list of distinct community user names for the gateway
     */
    public List<String> findDistinctCommunityUserNamesByGatewayId(String gatewayId) {
        String queryString = "SELECT DISTINCT c.communityUserName FROM CommunityUserEntity c WHERE c.gatewayId = :gatewayId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gatewayId", gatewayId);
        return execute(entityManager -> {
            jakarta.persistence.Query query = entityManager.createQuery(queryString);
            query.setParameter("gatewayId", gatewayId);
            return query.getResultList();
        });
    }
}
