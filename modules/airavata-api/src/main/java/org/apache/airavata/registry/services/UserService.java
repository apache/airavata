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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.registry.entities.UserEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.mappers.UserMapper;
import org.apache.airavata.registry.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users in the registry.
 * Uses the unified OIDC-based UserEntity from registry.entities.
 */
@Service("registryUserService")
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Check if a user exists by gatewayId and userName (sub).
     *
     * @param gatewayId the gateway identifier
     * @param userName the user name (maps to sub)
     * @return true if user exists
     */
    public boolean isUserExists(String gatewayId, String userName) throws RegistryException {
        return userRepository.existsByUserIdAndGatewayId(userName, gatewayId);
    }

    /**
     * Get all usernames (sub values) in a gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of usernames (sub values)
     */
    public List<String> getAllUsernamesInGateway(String gatewayId) throws RegistryException {
        List<UserEntity> entities = userRepository.findByGatewayId(gatewayId);
        return entities.stream().map(UserEntity::getSub).toList();
    }

    /**
     * Add a new user.
     *
     * @param userProfile the user profile to add
     * @return the created user profile
     */
    public UserProfile addUser(UserProfile userProfile) throws RegistryException {
        UserEntity entity = userMapper.toEntity(userProfile);
        // Set the airavataInternalUserId if not set
        if (entity.getAiravataInternalUserId() == null && entity.getSub() != null && entity.getGatewayId() != null) {
            entity.setAiravataInternalUserId(UserEntity.createInternalUserId(entity.getSub(), entity.getGatewayId()));
        }
        UserEntity saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    /**
     * Get a user by userId (sub) and gatewayId.
     *
     * @param userId the user identifier (sub)
     * @param gatewayId the gateway identifier
     * @return the user profile, or null if not found
     */
    public UserProfile get(String userId, String gatewayId) throws RegistryException {
        UserEntity entity = userRepository.findByUserIdAndGatewayId(userId, gatewayId).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    /**
     * Get a user by airavataInternalUserId.
     *
     * @param airavataInternalUserId the internal user ID (sub@gatewayId format)
     * @return the user profile, or null if not found
     */
    public UserProfile getByInternalUserId(String airavataInternalUserId) throws RegistryException {
        UserEntity entity = userRepository.findById(airavataInternalUserId).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    /**
     * Delete a user by userId (sub) and gatewayId.
     *
     * @param userId the user identifier (sub)
     * @param gatewayId the gateway identifier
     */
    public void delete(String userId, String gatewayId) throws RegistryException {
        String airavataInternalUserId = UserEntity.createInternalUserId(userId, gatewayId);
        userRepository.deleteById(airavataInternalUserId);
    }

    /**
     * Delete a user by airavataInternalUserId.
     *
     * @param airavataInternalUserId the internal user ID
     */
    public void deleteByInternalUserId(String airavataInternalUserId) throws RegistryException {
        userRepository.deleteById(airavataInternalUserId);
    }
}
