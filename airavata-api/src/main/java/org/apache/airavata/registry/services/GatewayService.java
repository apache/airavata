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
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.entities.expcatalog.GatewayEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.GatewayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GatewayService {
    private final GatewayRepository gatewayRepository;
    private final Mapper mapper;

    public GatewayService(GatewayRepository gatewayRepository, Mapper mapper) {
        this.gatewayRepository = gatewayRepository;
        this.mapper = mapper;
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        return gatewayRepository.existsById(gatewayId);
    }

    public Gateway getGateway(String gatewayId) throws RegistryException {
        GatewayEntity entity = gatewayRepository.findById(gatewayId).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, Gateway.class);
    }

    public List<Gateway> getAllGateways() throws RegistryException {
        List<GatewayEntity> entities = gatewayRepository.findAll();
        return entities.stream().map(e -> mapper.map(e, Gateway.class)).collect(Collectors.toList());
    }

    public void removeGateway(String gatewayId) throws RegistryException {
        gatewayRepository.deleteById(gatewayId);
    }

    public String addGateway(Gateway gateway) throws RegistryException {
        GatewayEntity entity = mapper.map(gateway, GatewayEntity.class);
        GatewayEntity saved = gatewayRepository.save(entity);
        return saved.getGatewayId();
    }

    public void updateGateway(String gatewayId, Gateway gateway) throws RegistryException {
        GatewayEntity entity = mapper.map(gateway, GatewayEntity.class);
        entity.setGatewayId(gatewayId);
        gatewayRepository.save(entity);
    }
}
