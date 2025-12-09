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
package org.apache.airavata.credential.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.credential.CommunityUser;
import org.apache.airavata.credential.entities.CommunityUserEntity;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.repositories.CommunityUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing community user entities.
 */
@Service
@Transactional
public class CommunityUserEntityService {

    private static final Logger logger = LoggerFactory.getLogger(CommunityUserEntityService.class);

    private final CommunityUserRepository communityUserRepository;

    public CommunityUserEntityService(CommunityUserRepository communityUserRepository) {
        this.communityUserRepository = communityUserRepository;
    }

    /**
     * Add or update community user.
     */
    public void saveCommunityUser(CommunityUser user, String token) throws CredentialStoreException {
        try {
            // Delete existing user with same token if exists
            communityUserRepository.deleteByGatewayIdAndCommunityUserNameAndTokenId(
                    user.getGatewayName(), user.getUserName(), token);

            CommunityUserEntity entity = new CommunityUserEntity();
            entity.setGatewayId(user.getGatewayName());
            entity.setCommunityUserName(user.getUserName());
            entity.setTokenId(token);
            entity.setCommunityUserEmail(user.getUserEmail());

            communityUserRepository.save(entity);
        } catch (Exception e) {
            logger.error(
                    "Error saving community user for gateway: {}, user: {}, token: {}",
                    user.getGatewayName(),
                    user.getUserName(),
                    token,
                    e);
            throw new CredentialStoreException("Error saving community user", e);
        }
    }

    /**
     * Delete community user.
     */
    public void deleteCommunityUser(CommunityUser user) throws CredentialStoreException {
        try {
            communityUserRepository.deleteByGatewayIdAndCommunityUserName(user.getGatewayName(), user.getUserName());
        } catch (Exception e) {
            logger.error(
                    "Error deleting community user for gateway: {}, user: {}",
                    user.getGatewayName(),
                    user.getUserName(),
                    e);
            throw new CredentialStoreException("Error deleting community user", e);
        }
    }

    /**
     * Delete community user by token.
     */
    public void deleteCommunityUserByToken(CommunityUser user, String token) throws CredentialStoreException {
        try {
            communityUserRepository.deleteByGatewayIdAndCommunityUserNameAndTokenId(
                    user.getGatewayName(), user.getUserName(), token);
        } catch (Exception e) {
            logger.error(
                    "Error deleting community user by token for gateway: {}, user: {}, token: {}",
                    user.getGatewayName(),
                    user.getUserName(),
                    token,
                    e);
            throw new CredentialStoreException("Error deleting community user by token", e);
        }
    }

    /**
     * Get community user by gateway ID and community user name.
     */
    public CommunityUser getCommunityUser(String gatewayName, String communityUserName)
            throws CredentialStoreException {
        Optional<CommunityUserEntity> entityOpt =
                communityUserRepository.findByGatewayIdAndCommunityUserName(gatewayName, communityUserName);
        if (entityOpt.isEmpty()) {
            return null;
        }

        CommunityUserEntity entity = entityOpt.get();
        return new CommunityUser(entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail());
    }

    /**
     * Get community user by gateway ID and token ID.
     */
    public CommunityUser getCommunityUserByToken(String gatewayName, String tokenId) throws CredentialStoreException {
        Optional<CommunityUserEntity> entityOpt =
                communityUserRepository.findByGatewayIdAndTokenId(gatewayName, tokenId);
        if (entityOpt.isEmpty()) {
            return null;
        }

        CommunityUserEntity entity = entityOpt.get();
        return new CommunityUser(entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail());
    }

    /**
     * Get all community users for a gateway.
     */
    public List<CommunityUser> getCommunityUsers(String gatewayName) throws CredentialStoreException {
        List<CommunityUserEntity> entities = communityUserRepository.findByGatewayId(gatewayName);
        List<CommunityUser> users = new ArrayList<>();
        for (CommunityUserEntity entity : entities) {
            users.add(new CommunityUser(
                    entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail()));
        }
        return users;
    }
}
