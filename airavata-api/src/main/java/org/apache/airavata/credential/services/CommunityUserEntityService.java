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
import org.apache.airavata.credential.entities.CommunityUserEntity;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CommunityUser;
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
        var gatewayName = user.getGatewayName();
        var userName = user.getUsername();
        var userEmail = user.getUserEmail();
        try {
            // Delete existing user with same token if exists
            communityUserRepository.deleteByGatewayIdAndCommunityUserNameAndTokenId(gatewayName, userName, token);
            var entity = new CommunityUserEntity();
            entity.setGatewayId(gatewayName);
            entity.setCommunityUserName(userName);
            entity.setTokenId(token);
            entity.setCommunityUserEmail(userEmail);
            communityUserRepository.save(entity);
        } catch (Exception e) {
            String msg = String.format("Error saving community user for gateway: %s, user: %s, token: %s", gatewayName, userName, token);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Delete community user.
     */
    public void deleteCommunityUser(CommunityUser user) throws CredentialStoreException {
        var gatewayName = user.getGatewayName();
        var userName = user.getUsername();
        try {
            communityUserRepository.deleteByGatewayIdAndCommunityUserName(gatewayName, userName);
        } catch (Exception e) {
            String msg = String.format("Error deleting community user for gateway: %s, user: %s", gatewayName, userName);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Delete community user by token.
     */
    public void deleteCommunityUserByToken(CommunityUser user, String token) throws CredentialStoreException {
        var gatewayName = user.getGatewayName();
        var userName = user.getUsername();
        try {
            communityUserRepository.deleteByGatewayIdAndCommunityUserNameAndTokenId(gatewayName, userName, token);
        } catch (Exception e) {
            String msg = String.format("Error deleting community user by token for gateway: %s, user: %s, token: %s", gatewayName, userName, token);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Get community user by gateway ID and community user name.
     */
    public CommunityUser getCommunityUser(String gatewayName, String communityUserName) throws CredentialStoreException {
        var entityOpt = communityUserRepository.findByGatewayIdAndCommunityUserName(gatewayName, communityUserName);
        if (entityOpt.isEmpty()) {
            var msg = String.format("Community user not found for gateway: %s, user: %s", gatewayName, communityUserName);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
        var entity = entityOpt.get();
        return new CommunityUser(entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail());
    }

    /**
     * Get community user by gateway ID and token ID.
     */
    public CommunityUser getCommunityUserByToken(String gatewayName, String tokenId) throws CredentialStoreException {
        var entityOpt = communityUserRepository.findByGatewayIdAndTokenId(gatewayName, tokenId);
        if (entityOpt.isEmpty()) {
            var msg = String.format("Community user not found for gateway: %s, token: %s", gatewayName, tokenId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
        var entity = entityOpt.get();
        return new CommunityUser(entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail());
    }

    /**
     * Get all community users for a gateway.
     */
    public List<CommunityUser> getCommunityUsers(String gatewayName) throws CredentialStoreException {
        var entities = communityUserRepository.findByGatewayId(gatewayName);
        var users = new ArrayList<CommunityUser>();
        for (var entity : entities) {
            users.add(new CommunityUser(entity.getGatewayId(), entity.getCommunityUserName(), entity.getCommunityUserEmail()));
        }
        return users;
    }
}
