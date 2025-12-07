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

import com.github.dozermapper.core.Mapper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.entities.expcatalog.UserEntity;
import org.apache.airavata.registry.entities.expcatalog.UserPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.UserRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UserEntity entity = mapper.map(userProfile, UserEntity.class);
        UserEntity saved = userRepository.save(entity);
        return mapper.map(saved, UserProfile.class);
    }
}
