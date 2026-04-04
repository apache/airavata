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
package org.apache.airavata.research.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.ResourceProfileRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.group.proto.ResourceType;
import org.apache.airavata.sharing.registry.models.proto.EntitySearchField;
import org.apache.airavata.sharing.registry.models.proto.SearchCondition;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCatalogService.class);

    private final AppCatalogRegistry appCatalogRegistry;
    private final ResourceProfileRegistry resourceProfileRegistry;
    private final RegistryProvider registryProvider;
    private final SharingFacade sharingHandler;
    private final CredentialProvider credentialHandler;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;

    public ApplicationCatalogService(
            AppCatalogRegistry appCatalogRegistry,
            ResourceProfileRegistry resourceProfileRegistry,
            RegistryProvider registryProvider,
            SharingFacade sharingHandler,
            CredentialProvider credentialHandler,
            GatewayGroupsInitializer gatewayGroupsInitializer) {
        this.appCatalogRegistry = appCatalogRegistry;
        this.resourceProfileRegistry = resourceProfileRegistry;
        this.registryProvider = registryProvider;
        this.sharingHandler = sharingHandler;
        this.credentialHandler = credentialHandler;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
    }

    // -------------------------------------------------------------------------
    // Application Modules
    // -------------------------------------------------------------------------

    public String registerApplicationModule(RequestContext ctx, String gatewayId, ApplicationModule applicationModule)
            throws ServiceException {
        try {
            return appCatalogRegistry.registerApplicationModule(gatewayId, applicationModule);
        } catch (Exception e) {
            throw new ServiceException("Error while adding application module: " + e.getMessage(), e);
        }
    }

    public ApplicationModule getApplicationModule(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return appCatalogRegistry.getApplicationModule(appModuleId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application module: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationModule(RequestContext ctx, String appModuleId, ApplicationModule applicationModule)
            throws ServiceException {
        try {
            return appCatalogRegistry.updateApplicationModule(appModuleId, applicationModule);
        } catch (Exception e) {
            throw new ServiceException("Error while updating application module: " + e.getMessage(), e);
        }
    }

    public List<ApplicationModule> getAllAppModules(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return appCatalogRegistry.getAllAppModules(gatewayId);
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
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name())
                        .build());
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.PERMISSION_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":" + ResourcePermissionType.READ)
                        .build());
                accessibleAppDeploymentIds.addAll(sharingHandler.searchEntityIds(
                        ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, sharingFilters, 0, -1));
            }
            List<String> accessibleComputeResourceIds = getAccessibleComputeResourceIds(ctx, gatewayId);
            return appCatalogRegistry.getAccessibleAppModules(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving accessible application modules: " + e.getMessage(), e);
        }
    }

    public boolean deleteApplicationModule(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return appCatalogRegistry.deleteApplicationModule(appModuleId);
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
            String result = appCatalogRegistry.registerApplicationDeployment(gatewayId, applicationDeployment);
            sharingHandler.createEntity(
                    result,
                    gatewayId,
                    gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name(),
                    ctx.getUserId() + "@" + gatewayId,
                    result,
                    applicationDeployment.getAppDeploymentDescription(),
                    null);
            SharingHelper.shareEntityWithAdminGatewayGroups(
                    sharingHandler, registryProvider, gatewayGroupsInitializer, gatewayId, result);
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
            return appCatalogRegistry.getApplicationDeployment(appDeploymentId);
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
            return appCatalogRegistry.updateApplicationDeployment(appDeploymentId, applicationDeployment);
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
            boolean result = appCatalogRegistry.deleteApplicationDeployment(appDeploymentId);
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
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name())
                        .build());
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.PERMISSION_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":" + permissionType.name())
                        .build());
                accessibleAppDeploymentIds.addAll(sharingHandler.searchEntityIds(
                        ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, sharingFilters, 0, -1));
            }
            List<String> accessibleComputeResourceIds = getAccessibleComputeResourceIds(ctx, gatewayId);
            return appCatalogRegistry.getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application deployments: " + e.getMessage(), e);
        }
    }

    public List<String> getAppModuleDeployedResources(RequestContext ctx, String appModuleId) throws ServiceException {
        try {
            return appCatalogRegistry.getAppModuleDeployedResources(appModuleId);
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
            GroupResourceProfile groupResourceProfile =
                    resourceProfileRegistry.getGroupResourceProfile(groupResourceProfileId);
            List<String> accessibleComputeResourceIds = new ArrayList<>();
            for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferencesList()) {
                accessibleComputeResourceIds.add(pref.getComputeResourceId());
            }

            List<String> accessibleAppDeploymentIds = new ArrayList<>();
            List<SearchCriteria> sharingFilters = new ArrayList<>();
            sharingFilters.add(SearchCriteria.newBuilder()
                    .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                    .setSearchCondition(SearchCondition.EQUAL)
                    .setValue(ctx.getGatewayId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name())
                    .build());
            sharingFilters.add(SearchCriteria.newBuilder()
                    .setSearchField(EntitySearchField.PERMISSION_TYPE_ID)
                    .setSearchCondition(SearchCondition.EQUAL)
                    .setValue(ctx.getGatewayId() + ":" + ResourcePermissionType.READ)
                    .build());
            accessibleAppDeploymentIds.addAll(sharingHandler.searchEntityIds(
                    ctx.getGatewayId(), ctx.getUserId() + "@" + ctx.getGatewayId(), sharingFilters, 0, -1));

            return appCatalogRegistry.getAccessibleApplicationDeploymentsForAppModule(
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
            return appCatalogRegistry.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (Exception e) {
            throw new ServiceException("Error while adding application interface: " + e.getMessage(), e);
        }
    }

    public String cloneApplicationInterface(
            RequestContext ctx, String existingAppInterfaceId, String newApplicationName, String gatewayId)
            throws ServiceException {
        try {
            ApplicationInterfaceDescription existingInterface =
                    appCatalogRegistry.getApplicationInterface(existingAppInterfaceId);
            if (existingInterface == null) {
                throw new ServiceException("Provided application interface does not exist: " + existingAppInterfaceId);
            }
            existingInterface = existingInterface.toBuilder()
                    .setApplicationName(newApplicationName)
                    .setApplicationInterfaceId("DO_NOT_SET_AT_CLIENTS")
                    .build();
            String interfaceId = appCatalogRegistry.registerApplicationInterface(gatewayId, existingInterface);
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
            return appCatalogRegistry.getApplicationInterface(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interface: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationInterface(
            RequestContext ctx, String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws ServiceException {
        try {
            return appCatalogRegistry.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (Exception e) {
            throw new ServiceException("Error while updating application interface: " + e.getMessage(), e);
        }
    }

    public boolean deleteApplicationInterface(RequestContext ctx, String appInterfaceId) throws ServiceException {
        try {
            return appCatalogRegistry.deleteApplicationInterface(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting application interface: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return appCatalogRegistry.getAllApplicationInterfaceNames(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interface names: " + e.getMessage(), e);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return appCatalogRegistry.getAllApplicationInterfaces(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application interfaces: " + e.getMessage(), e);
        }
    }

    public List<InputDataObjectType> getApplicationInputs(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return appCatalogRegistry.getApplicationInputs(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application inputs: " + e.getMessage(), e);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return appCatalogRegistry.getApplicationOutputs(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving application outputs: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(RequestContext ctx, String appInterfaceId)
            throws ServiceException {
        try {
            return appCatalogRegistry.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while retrieving available compute resources for interface: " + e.getMessage(), e);
        }
    }

    private List<String> getAccessibleComputeResourceIds(RequestContext ctx, String gatewayId) throws Exception {
        List<String> accessibleComputeResourceIds = new ArrayList<>();
        if (SharingHelper.isSharingEnabled()) {
            List<SearchCriteria> filters = new ArrayList<>();
            filters.add(SearchCriteria.newBuilder()
                    .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                    .setSearchCondition(SearchCondition.EQUAL)
                    .setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name())
                    .build());
            List<String> accessibleGroupResProfileIds = new ArrayList<>();
            accessibleGroupResProfileIds.addAll(sharingHandler.searchEntityIds(
                    ctx.getGatewayId(), ctx.getUserId() + "@" + gatewayId, filters, 0, -1));
            List<GroupResourceProfile> groupResourceProfiles =
                    resourceProfileRegistry.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
            for (GroupResourceProfile groupResourceProfile : groupResourceProfiles) {
                List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                        groupResourceProfile.getComputePreferencesList();
                for (GroupComputeResourcePreference pref : groupComputeResourcePreferenceList) {
                    accessibleComputeResourceIds.add(pref.getComputeResourceId());
                }
            }
        }
        return accessibleComputeResourceIds;
    }
}
