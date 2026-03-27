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
package org.apache.airavata.compute.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceAuthorizationException;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.sharing.service.SharingHelper;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCatalogService.class);

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;
    private final CredentialStoreServerHandler credentialHandler;

    public ApplicationCatalogService(
            RegistryServerHandler registryHandler,
            SharingRegistryServerHandler sharingHandler,
            CredentialStoreServerHandler credentialHandler) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
        this.credentialHandler = credentialHandler;
    }

    // -------------------------------------------------------------------------
    // Application Modules
    // -------------------------------------------------------------------------

    public String registerApplicationModule(RequestContext ctx, String gatewayId, ApplicationModule applicationModule)
            throws ServiceException {
        try {
            return registryHandler.registerApplicationModule(gatewayId, applicationModule);
        } catch (Exception e) {
            throw new ServiceException("Error while adding application module: " + e.getMessage(), e);
        }
    }

    public ApplicationModule getApplicationModule(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return registryHandler.getApplicationModule(appModuleId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application module: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationModule(RequestContext ctx, String appModuleId, ApplicationModule applicationModule)
            throws ServiceException {
        try {
            return registryHandler.updateApplicationModule(appModuleId, applicationModule);
        } catch (Exception e) {
            throw new ServiceException("Error while updating application module: " + e.getMessage(), e);
        }
    }

    public List<ApplicationModule> getAllAppModules(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.getAllAppModules(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving all application modules: " + e.getMessage(), e);
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            List<String> accessibleAppDeploymentIds = new ArrayList<>();
            if (SharingHelper.isSharingEnabled()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                SearchCriteria entityTypeFilter = new SearchCriteria();
                entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                entityTypeFilter.setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                sharingFilters.add(entityTypeFilter);
                SearchCriteria permissionTypeFilter = new SearchCriteria();
                permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
                permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                permissionTypeFilter.setValue(gatewayId + ":" + ResourcePermissionType.READ);
                sharingFilters.add(permissionTypeFilter);
                sharingHandler
                        .searchEntities(ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, sharingFilters, 0, -1)
                        .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
            }
            List<String> accessibleComputeResourceIds = getAccessibleComputeResourceIds(ctx, gatewayId);
            return registryHandler.getAccessibleAppModules(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving accessible application modules: " + e.getMessage(), e);
        }
    }

    public boolean deleteApplicationModule(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return registryHandler.deleteApplicationModule(appModuleId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting application module: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Application Deployments
    // -------------------------------------------------------------------------

    public String registerApplicationDeployment(
            RequestContext ctx, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws ServiceException {
        try {
            String result = registryHandler.registerApplicationDeployment(gatewayId, applicationDeployment);
            Entity entity = new Entity();
            entity.setEntityId(result);
            entity.setDomainId(gatewayId);
            entity.setEntityTypeId(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            entity.setOwnerId(ctx.getUserId() + "@" + gatewayId);
            entity.setName(result);
            entity.setDescription(applicationDeployment.getAppDeploymentDescription());
            sharingHandler.createEntity(entity);
            SharingHelper.shareEntityWithAdminGatewayGroups(sharingHandler, registryHandler, entity);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error while adding application deployment: " + e.getMessage(), e);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(RequestContext ctx, String appDeploymentId)
            throws ServiceException {
        try {
            if (SharingHelper.isSharingEnabled()) {
                if (!SharingHelper.userHasAccess(
                        sharingHandler,
                        ctx.getGatewayId(),
                        ctx.getUserId(),
                        appDeploymentId,
                        ResourcePermissionType.READ)) {
                    throw new ServiceAuthorizationException(
                            "User does not have access to application deployment " + appDeploymentId);
                }
            }
            return registryHandler.getApplicationDeployment(appDeploymentId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application deployment: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationDeployment(
            RequestContext ctx, String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws ServiceException {
        try {
            if (SharingHelper.isSharingEnabled()) {
                if (!SharingHelper.userHasAccess(
                        sharingHandler,
                        ctx.getGatewayId(),
                        ctx.getUserId(),
                        appDeploymentId,
                        ResourcePermissionType.WRITE)) {
                    throw new ServiceAuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return registryHandler.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while updating application deployment: " + e.getMessage(), e);
        }
    }

    public boolean deleteApplicationDeployment(RequestContext ctx, String appDeploymentId) throws ServiceException {
        try {
            if (!SharingHelper.userHasAccess(
                    sharingHandler,
                    ctx.getGatewayId(),
                    ctx.getUserId(),
                    appDeploymentId,
                    ResourcePermissionType.WRITE)) {
                throw new ServiceAuthorizationException(
                        "User does not have WRITE access to application deployment " + appDeploymentId);
            }
            boolean result = registryHandler.deleteApplicationDeployment(appDeploymentId);
            sharingHandler.deleteEntity(ctx.getGatewayId(), appDeploymentId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while deleting application deployment: " + e.getMessage(), e);
        }
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(RequestContext ctx, String gatewayId)
            throws ServiceException {
        return getAccessibleApplicationDeployments(ctx, gatewayId, ResourcePermissionType.READ);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            RequestContext ctx, String gatewayId, ResourcePermissionType permissionType) throws ServiceException {
        try {
            List<String> accessibleAppDeploymentIds = new ArrayList<>();
            if (SharingHelper.isSharingEnabled()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                SearchCriteria entityTypeFilter = new SearchCriteria();
                entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                entityTypeFilter.setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                sharingFilters.add(entityTypeFilter);
                SearchCriteria permissionTypeFilter = new SearchCriteria();
                permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
                permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                permissionTypeFilter.setValue(gatewayId + ":" + permissionType.name());
                sharingFilters.add(permissionTypeFilter);
                sharingHandler
                        .searchEntities(ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, sharingFilters, 0, -1)
                        .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
            }
            List<String> accessibleComputeResourceIds = getAccessibleComputeResourceIds(ctx, gatewayId);
            return registryHandler.getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application deployments: " + e.getMessage(), e);
        }
    }

    public List<String> getAppModuleDeployedResources(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return registryHandler.getAppModuleDeployedResources(appModuleId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application deployments: " + e.getMessage(), e);
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
            RequestContext ctx, String appModuleId, String groupResourceProfileId) throws ServiceException {
        try {
            if (!SharingHelper.userHasAccess(
                    sharingHandler,
                    ctx.getGatewayId(),
                    ctx.getUserId(),
                    groupResourceProfileId,
                    ResourcePermissionType.READ)) {
                throw new ServiceAuthorizationException(
                        "User is not authorized to access Group Resource Profile " + groupResourceProfileId);
            }
            GroupResourceProfile groupResourceProfile = registryHandler.getGroupResourceProfile(groupResourceProfileId);
            List<String> accessibleComputeResourceIds = new ArrayList<>();
            for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferences()) {
                accessibleComputeResourceIds.add(pref.getComputeResourceId());
            }

            List<String> accessibleAppDeploymentIds = new ArrayList<>();
            List<SearchCriteria> sharingFilters = new ArrayList<>();
            SearchCriteria entityTypeFilter = new SearchCriteria();
            entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            entityTypeFilter.setValue(ctx.getGatewayId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            sharingFilters.add(entityTypeFilter);
            SearchCriteria permissionTypeFilter = new SearchCriteria();
            permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            permissionTypeFilter.setValue(ctx.getGatewayId() + ":" + ResourcePermissionType.READ);
            sharingFilters.add(permissionTypeFilter);
            sharingHandler
                    .searchEntities(
                            ctx.getGatewayId(), ctx.getUserId() + "@" + ctx.getGatewayId(), sharingFilters, 0, -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));

            return registryHandler.getAccessibleApplicationDeploymentsForAppModule(
                    ctx.getGatewayId(), appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while retrieving application deployments for module and group resource profile: "
                            + e.getMessage(),
                    e);
        }
    }

    // -------------------------------------------------------------------------
    // Application Interfaces
    // -------------------------------------------------------------------------

    public String registerApplicationInterface(
            RequestContext ctx, String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws ServiceException {
        try {
            return registryHandler.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (Exception e) {
            throw new ServiceException("Error while adding application interface: " + e.getMessage(), e);
        }
    }

    public String cloneApplicationInterface(
            RequestContext ctx, String existingAppInterfaceId, String newApplicationName, String gatewayId)
            throws ServiceException {
        try {
            ApplicationInterfaceDescription existingInterface =
                    registryHandler.getApplicationInterface(existingAppInterfaceId);
            if (existingInterface == null) {
                throw new ServiceException("Provided application interface does not exist: " + existingAppInterfaceId);
            }
            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            String interfaceId = registryHandler.registerApplicationInterface(gatewayId, existingInterface);
            logger.debug("Cloned application interface {} for gateway {}", existingAppInterfaceId, gatewayId);
            return interfaceId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while cloning application interface: " + e.getMessage(), e);
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.getApplicationInterface(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interface: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationInterface(
            RequestContext ctx, String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws ServiceException {
        try {
            return registryHandler.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (Exception e) {
            throw new ServiceException("Error while updating application interface: " + e.getMessage(), e);
        }
    }

    public boolean deleteApplicationInterface(RequestContext ctx, String appInterfaceId) throws ServiceException {
        try {
            return registryHandler.deleteApplicationInterface(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting application interface: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getAllApplicationInterfaceNames(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interface names: " + e.getMessage(), e);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getAllApplicationInterfaces(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interfaces: " + e.getMessage(), e);
        }
    }

    public List<InputDataObjectType> getApplicationInputs(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.getApplicationInputs(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application inputs: " + e.getMessage(), e);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.getApplicationOutputs(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application outputs: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while retrieving available compute resources for interface: " + e.getMessage(), e);
        }
    }

    private List<String> getAccessibleComputeResourceIds(RequestContext ctx, String gatewayId) throws Exception {
        List<String> accessibleComputeResourceIds = new ArrayList<>();
        if (SharingHelper.isSharingEnabled()) {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
            filters.add(searchCriteria);
            List<String> accessibleGroupResProfileIds = new ArrayList<>();
            sharingHandler
                    .searchEntities(ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, filters, 0, -1)
                    .forEach(p -> accessibleGroupResProfileIds.add(p.getEntityId()));
            List<GroupResourceProfile> groupResourceProfiles =
                    registryHandler.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
            for (GroupResourceProfile groupResourceProfile : groupResourceProfiles) {
                List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                        groupResourceProfile.getComputePreferences();
                for (GroupComputeResourcePreference pref : groupComputeResourcePreferenceList) {
                    accessibleComputeResourceIds.add(pref.getComputeResourceId());
                }
            }
        }
        return accessibleComputeResourceIds;
    }
}
