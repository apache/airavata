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
import java.util.stream.Collectors;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.registry.entities.expcatalog.UserEntity;
import org.apache.airavata.registry.entities.expcatalog.UserPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.UserMapper;
import org.apache.airavata.registry.repositories.expcatalog.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public boolean isUserExists(String gatewayId, String userName) throws RegistryException {
        UserPK pk = new UserPK();
        pk.setGatewayId(gatewayId);
        pk.setUserId(userName);
        return userRepository.existsById(pk);
    }

    public List<String> getAllUsernamesInGateway(String gatewayId) throws RegistryException {
        List<UserEntity> entities = userRepository.findByGatewayId(gatewayId);
        return entities.stream().map(UserEntity::getUserId).collect(Collectors.toList());
    }

    public UserProfile addUser(UserProfile userProfile) throws RegistryException {
        UserEntity entity = userMapper.toEntity(userProfile);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    public UserProfile get(UserPK userPK) throws RegistryException {
        UserEntity entity = userRepository.findById(userPK).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    public void delete(UserPK userPK) throws RegistryException {
        userRepository.deleteById(userPK);
    }
}
