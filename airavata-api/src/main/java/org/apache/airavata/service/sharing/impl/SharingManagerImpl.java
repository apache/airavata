/**
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
package org.apache.airavata.service.sharing.impl;

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.GatewayGroupsInitializer;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.sharing.SharingManager;
import org.apache.airavata.sharing.models.DuplicateEntryException;
import org.apache.airavata.sharing.models.Entity;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of SharingManager.
 */
@Service
public class SharingManagerImpl implements SharingManager {
    private static final Logger logger = LoggerFactory.getLogger(SharingManagerImpl.class);
    
    private final AiravataServerProperties properties;
    private final SharingRegistryService sharingRegistryService;
    private final RegistryService registryService;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;
    
    public SharingManagerImpl(
            AiravataServerProperties properties,
            SharingRegistryService sharingRegistryService,
            RegistryService registryService,
            GatewayGroupsInitializer gatewayGroupsInitializer) {
        this.properties = properties;
        this.sharingRegistryService = sharingRegistryService;
        this.registryService = registryService;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
    }
    
    private AiravataSystemException airavataSystemException(AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(errorType, message, cause);
    }
    
    private void shareEntityWithAdminGatewayGroups(Entity entity) throws SharingRegistryException, InvalidRequestException, AuthorizationException {
        final String domainId = entity.getDomainId();
        try {
            GatewayGroups gatewayGroups = retrieveGatewayGroups(domainId);
            createManageSharingPermissionTypeIfMissing(domainId);
            sharingRegistryService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    java.util.Arrays.asList(gatewayGroups.getAdminsGroupId()),
                    domainId + ":MANAGE_SHARING",
                    true);
            sharingRegistryService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    java.util.Arrays.asList(gatewayGroups.getAdminsGroupId()),
                    domainId + ":WRITE",
                    true);
            sharingRegistryService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    java.util.Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                    domainId + ":READ",
                    true);
        } catch (SharingRegistryException | RegistryServiceException e) {
            logger.error("Error sharing entity with admin gateway groups: " + e.getMessage(), e);
            throw new SharingRegistryException("Error sharing entity with admin gateway groups: " + e.getMessage());
        }
    }
    
    private GatewayGroups retrieveGatewayGroups(String gatewayId) throws RegistryServiceException, SharingRegistryException {
        try {
            if (isGatewayGroupsExists(gatewayId)) {
                return registryService.getGatewayGroups(gatewayId);
            } else {
                return gatewayGroupsInitializer.initialize(gatewayId);
            }
        } catch (Exception e) {
            String msg = "Error while initializing gateway groups: " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw new RegistryServiceException(msg);
        }
    }
    
    private boolean isGatewayGroupsExists(String gatewayId) {
        try {
            return registryService.getGatewayGroups(gatewayId) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void createManageSharingPermissionTypeIfMissing(String domainId) {
        try {
            var permissionTypeId = domainId + ":MANAGE_SHARING";
            try {
                sharingRegistryService.getPermissionType(domainId, permissionTypeId);
            } catch (SharingRegistryException e) {
                // Permission type doesn't exist, create it
                var permissionType = new org.apache.airavata.sharing.models.PermissionType();
                permissionType.setPermissionTypeId(permissionTypeId);
                permissionType.setDomainId(domainId);
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                sharingRegistryService.createPermissionType(permissionType);
            }
        } catch (SharingRegistryException | org.apache.airavata.sharing.models.DuplicateEntryException e) {
            logger.warn("Error creating/managing MANAGE_SHARING permission type: " + e.getMessage());
        }
    }
    
    @Override
    public String createExperimentEntity(String experimentId, ExperimentModel experiment) throws AiravataSystemException {
        if (!properties.services.sharing.enabled) {
            return experimentId;
        }
        
        try {
            var entity = new Entity();
            entity.setEntityId(experimentId);
            final String domainId = experiment.getGatewayId();
            entity.setDomainId(domainId);
            entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
            entity.setOwnerId(experiment.getUserName() + "@" + domainId);
            entity.setName(experiment.getExperimentName());
            entity.setDescription(experiment.getDescription());
            entity.setParentEntityId(experiment.getProjectId());
            
            sharingRegistryService.createEntity(entity);
            shareEntityWithAdminGatewayGroups(entity);
            return experimentId;
        } catch (SharingRegistryException
                | DuplicateEntryException
                | InvalidRequestException
                | AuthorizationException ex) {
            logger.error(ex.getMessage(), ex);
            throw airavataSystemException(
                    AiravataErrorType.INTERNAL_ERROR,
                    "Failed to create sharing registry record. " + ex.getMessage(),
                    ex);
        }
    }
    
    @Override
    public String createProjectEntity(String projectId, Project project) throws AiravataSystemException {
        if (!properties.services.sharing.enabled) {
            return projectId;
        }
        
        try {
            var entity = new Entity();
            entity.setEntityId(projectId);
            final String domainId = project.getGatewayId();
            entity.setDomainId(domainId);
            entity.setEntityTypeId(domainId + ":" + "PROJECT");
            entity.setOwnerId(project.getOwner() + "@" + domainId);
            entity.setName(project.getName());
            entity.setDescription(project.getDescription());
            
            sharingRegistryService.createEntity(entity);
            return projectId;
        } catch (SharingRegistryException | DuplicateEntryException ex) {
            logger.error(ex.getMessage(), ex);
            throw airavataSystemException(
                    AiravataErrorType.INTERNAL_ERROR,
                    "Failed to create entry for project in Sharing Registry. More info : " + ex.getMessage(),
                    ex);
        }
    }
    
    @Override
    public void updateExperimentEntity(String experimentId, ExperimentModel experiment) throws AiravataSystemException {
        if (!properties.services.sharing.enabled) {
            return;
        }
        
        try {
            var entity = sharingRegistryService.getEntity(experiment.getGatewayId(), experimentId);
            entity.setName(experiment.getExperimentName());
            entity.setDescription(experiment.getDescription());
            entity.setParentEntityId(experiment.getProjectId());
            sharingRegistryService.updateEntity(entity);
        } catch (SharingRegistryException e) {
            String msg = "Error while updating experiment entity: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public void deleteEntity(String entityId) throws AiravataSystemException {
        if (!properties.services.sharing.enabled) {
            return;
        }
        
        try {
            // Note: We may need the gatewayId to delete, but for now this is a placeholder
            // The actual implementation may need to look up the entity first
            logger.debug("Deleting sharing entity: " + entityId);
        } catch (Exception e) {
            logger.warn("Error deleting sharing entity (non-critical): " + e.getMessage());
        }
    }
}
