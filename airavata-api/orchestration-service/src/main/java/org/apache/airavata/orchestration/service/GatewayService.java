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
package org.apache.airavata.orchestration.service;

import java.util.List;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.group.proto.ResourceType;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayService.class);

    private final RegistryHandler registryHandler;
    private final SharingFacade sharingHandler;

    public GatewayService(RegistryHandler registryHandler, SharingFacade sharingHandler) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
    }

    public String addGateway(RequestContext ctx, Gateway gateway) throws ServiceException {
        try {
            String gatewayId = registryHandler.addGateway(gateway);

            if (SharingHelper.isSharingEnabled()) {
                String domainId = gateway.getGatewayId();
                sharingHandler.createDomain(
                        domainId, gateway.getGatewayName(), "Domain entry for " + gateway.getGatewayName());

                // Creating Entity Types for each domain
                sharingHandler.createEntityType(domainId + ":PROJECT", domainId, "PROJECT", "Project entity type");
                sharingHandler.createEntityType(
                        domainId + ":EXPERIMENT", domainId, "EXPERIMENT", "Experiment entity type");
                sharingHandler.createEntityType(domainId + ":FILE", domainId, "FILE", "File entity type");
                sharingHandler.createEntityType(
                        domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name(),
                        domainId,
                        "APPLICATION-DEPLOYMENT",
                        "Application Deployment entity type");
                sharingHandler.createEntityType(
                        domainId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name(),
                        domainId,
                        ResourceType.GROUP_RESOURCE_PROFILE.name(),
                        "Group Resource Profile entity type");

                // Creating Permission Types for each domain
                sharingHandler.createPermissionType(domainId + ":READ", domainId, "READ", "Read permission type");
                sharingHandler.createPermissionType(domainId + ":WRITE", domainId, "WRITE", "Write permission type");
                sharingHandler.createPermissionType(
                        domainId + ":MANAGE_SHARING", domainId, "MANAGE_SHARING", "Sharing permission type");
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
