/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.registry.core.entities.expcatalog.GatewayUsageReportingCommandEntity;
import org.apache.airavata.registry.core.entities.expcatalog.GatewayUsageReportingPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayUsageReportingCommandRepository extends
                    ExpCatAbstractRepository<GatewayUsageReportingCommand, GatewayUsageReportingCommandEntity, GatewayUsageReportingPK> {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRepository.class);

    public GatewayUsageReportingCommandRepository() { super(GatewayUsageReportingCommand.class, GatewayUsageReportingCommandEntity.class); }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryException {
        String gatewayId = command.getGatewayId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayUsageReportingCommandEntity reportingEntity = mapper.map(command, GatewayUsageReportingCommandEntity.class);
        execute(entityManager -> entityManager.merge(reportingEntity));
        logger.info("Added gateway usage reporting command for gateway {} to the database", command.getGatewayId());
    }

    public GatewayUsageReportingCommand getGatewayUsageReportingCommand(String gatewayId, String computeResourceId) {
        GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
        pk.setGatewayId(gatewayId);
        pk.setComputeResourceId(computeResourceId);
        return get(pk);
    }

    public boolean isGatewayUsageReportingCommandExists(String gatewayId, String computeResourceId) throws RegistryException {
        GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
        pk.setGatewayId(gatewayId);
        pk.setComputeResourceId(computeResourceId);
        return isExists(pk);
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId) throws RegistryException {
        if (isGatewayUsageReportingCommandExists(gatewayId, computeResourceId)) {
            GatewayUsageReportingPK pk = new GatewayUsageReportingPK();
            pk.setGatewayId(gatewayId);
            pk.setComputeResourceId(computeResourceId);
            delete(pk);
            logger.info("Deleted gateway usage reporting command for gateway {}", gatewayId);
        }
    }
}
