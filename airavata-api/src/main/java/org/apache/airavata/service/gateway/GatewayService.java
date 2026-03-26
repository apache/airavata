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
package org.apache.airavata.service.gateway;

import java.util.List;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.sharing.SharingHelper;
import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.EntityType;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayService.class);

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;

    public GatewayService(RegistryServerHandler registryHandler, SharingRegistryServerHandler sharingHandler) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
    }

    public String addGateway(RequestContext ctx, Gateway gateway) throws ServiceException {
        try {
            String gatewayId = registryHandler.addGateway(gateway);

            if (SharingHelper.isSharingEnabled()) {
                Domain domain = new Domain();
                domain.setDomainId(gateway.getGatewayId());
                domain.setName(gateway.getGatewayName());
                domain.setDescription("Domain entry for " + domain.getName());
                sharingHandler.createDomain(domain);

                // Creating Entity Types for each domain
                EntityType entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":PROJECT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                sharingHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":EXPERIMENT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                sharingHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":FILE");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                sharingHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("APPLICATION-DEPLOYMENT");
                entityType.setDescription("Application Deployment entity type");
                sharingHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDescription("Group Resource Profile entity type");
                sharingHandler.createEntityType(entityType);

                // Creating Permission Types for each domain
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":READ");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                sharingHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":WRITE");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
                sharingHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":MANAGE_SHARING");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                sharingHandler.createPermissionType(permissionType);
            }

            logger.debug("Airavata successfully created the gateway with {}", gatewayId);
            return gatewayId;
        } catch (Exception e) {
            throw new ServiceException("Error while adding gateway: " + e.getMessage(), e);
        }
    }

    public List<String> getAllUsersInGateway(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.getAllUsersInGateway(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving users: " + e.getMessage(), e);
        }
    }

    public boolean updateGateway(RequestContext ctx, String gatewayId, Gateway updatedGateway) throws ServiceException {
        try {
            return registryHandler.updateGateway(gatewayId, updatedGateway);
        } catch (Exception e) {
            throw new ServiceException("Error while updating the gateway: " + e.getMessage(), e);
        }
    }

    public Gateway getGateway(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            Gateway result = registryHandler.getGateway(gatewayId);
            logger.debug("Airavata found the gateway with {}", gatewayId);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error while getting the gateway: " + e.getMessage(), e);
        }
    }

    public boolean deleteGateway(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.deleteGateway(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting the gateway: " + e.getMessage(), e);
        }
    }

    public List<Gateway> getAllGateways(RequestContext ctx) throws ServiceException {
        try {
            logger.debug("Airavata searching for all gateways");
            return registryHandler.getAllGateways();
        } catch (Exception e) {
            throw new ServiceException("Error while getting all the gateways: " + e.getMessage(), e);
        }
    }

    public boolean isGatewayExist(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            logger.debug("Airavata verifying if the gateway with {} exists", gatewayId);
            return registryHandler.isGatewayExist(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting gateway: " + e.getMessage(), e);
        }
    }

    public boolean isUserExists(RequestContext ctx, String gatewayId, String userName) throws ServiceException {
        try {
            logger.debug("Checking if the user {} exists in the gateway {}", userName, gatewayId);
            return registryHandler.isUserExists(gatewayId, userName);
        } catch (Exception e) {
            throw new ServiceException("Error while verifying user: " + e.getMessage(), e);
        }
    }
}
