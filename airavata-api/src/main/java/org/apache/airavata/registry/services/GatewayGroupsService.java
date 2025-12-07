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
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.registry.entities.appcatalog.GatewayGroupsEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.GatewayGroupsRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GatewayGroupsService {
    @Autowired
    private GatewayGroupsRepository gatewayGroupsRepository;

    public boolean isExists(String gatewayId) throws RegistryException {
        return gatewayGroupsRepository.existsById(gatewayId);
    }

    public GatewayGroups get(String gatewayId) throws RegistryException {
        GatewayGroupsEntity entity = gatewayGroupsRepository.findById(gatewayId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, GatewayGroups.class);
    }

    public GatewayGroups create(GatewayGroups gatewayGroups) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayGroupsEntity entity = mapper.map(gatewayGroups, GatewayGroupsEntity.class);
        GatewayGroupsEntity saved = gatewayGroupsRepository.save(entity);
        return mapper.map(saved, GatewayGroups.class);
    }

    public GatewayGroups update(GatewayGroups gatewayGroups) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayGroupsEntity entity = mapper.map(gatewayGroups, GatewayGroupsEntity.class);
        GatewayGroupsEntity saved = gatewayGroupsRepository.save(entity);
        return mapper.map(saved, GatewayGroups.class);
    }

    public void delete(String gatewayId) throws RegistryException {
        gatewayGroupsRepository.deleteById(gatewayId);
    }
}
