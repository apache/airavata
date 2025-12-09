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
import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.registry.entities.expcatalog.GatewayUsageReportingCommandEntity;
import org.apache.airavata.registry.entities.expcatalog.GatewayUsageReportingPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.GatewayUsageReportingCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GatewayUsageReportingCommandService {
    private final GatewayUsageReportingCommandRepository gatewayUsageReportingCommandRepository;
    private final Mapper mapper;

    public GatewayUsageReportingCommandService(
            GatewayUsageReportingCommandRepository gatewayUsageReportingCommandRepository, Mapper mapper) {
        this.gatewayUsageReportingCommandRepository = gatewayUsageReportingCommandRepository;
        this.mapper = mapper;
    }

    public boolean isGatewayUsageReportingCommandExists(String gatewayId, String computeResourceId)
            throws RegistryException {
        GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
        pk.setGatewayId(gatewayId);
        pk.setComputeResourceId(computeResourceId);
        return gatewayUsageReportingCommandRepository.existsById(pk);
    }

    public GatewayUsageReportingCommand getGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
        pk.setGatewayId(gatewayId);
        pk.setComputeResourceId(computeResourceId);
        GatewayUsageReportingCommandEntity entity =
                gatewayUsageReportingCommandRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, GatewayUsageReportingCommand.class);
    }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryException {
        GatewayUsageReportingCommandEntity entity = mapper.map(command, GatewayUsageReportingCommandEntity.class);
        gatewayUsageReportingCommandRepository.save(entity);
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
        pk.setGatewayId(gatewayId);
        pk.setComputeResourceId(computeResourceId);
        gatewayUsageReportingCommandRepository.deleteById(pk);
    }
}
