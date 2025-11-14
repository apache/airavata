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
package org.apache.airavata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ProjectSearchFields;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.service.security.GatewayGroupsInitializer;
import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.EntityType;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataService {
    private static final Logger logger = LoggerFactory.getLogger(AiravataService.class);

    private org.apache.airavata.service.RegistryService registryService =
            new org.apache.airavata.service.RegistryService();

    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryException {
        return registryService.getAllUsersInGateway(gatewayId);
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway)
            throws RegistryException, AppCatalogException {
        return registryService.updateGateway(gatewayId, updatedGateway);
    }

    public Gateway getGateway(String gatewayId) throws RegistryException {
        return registryService.getGateway(gatewayId);
    }

    public boolean deleteGateway(String gatewayId) throws RegistryException {
        return registryService.deleteGateway(gatewayId);
    }

    public List<Gateway> getAllGateways() throws RegistryException {
        return registryService.getAllGateways();
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        return registryService.isGatewayExist(gatewayId);
    }

    public String createNotification(Notification notification) throws RegistryException {
        return registryService.createNotification(notification);
    }

    public boolean updateNotification(Notification notification) throws RegistryException {
        return registryService.updateNotification(notification);
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryException {
        return registryService.deleteNotification(gatewayId, notificationId);
    }

    public Notification getNotification(String gatewayId, String notificationId) throws RegistryException {
        return registryService.getNotification(gatewayId, notificationId);
    }

    public List<Notification> getAllNotifications(String gatewayId) throws RegistryException {
        return registryService.getAllNotifications(gatewayId);
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryException {
        return registryService.registerDataProduct(dataProductModel);
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryException {
        return registryService.getDataProduct(productUri);
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws RegistryException {
        return registryService.registerReplicaLocation(replicaLocationModel);
    }

    public DataProductModel getParentDataProduct(String productUri) throws RegistryException {
        return registryService.getParentDataProduct(productUri);
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryException {
        return registryService.getChildDataProducts(productUri);
    }

    public boolean isUserExists(String gatewayId, String userName) throws RegistryException {
        return registryService.isUserExists(gatewayId, userName);
    }

    public Project getProject(String projectId) throws RegistryException {
        return registryService.getProject(projectId);
    }

    public String createProject(String gatewayId, Project project) throws RegistryException {
        return registryService.createProject(gatewayId, project);
    }

    public void updateProject(String projectId, Project updatedProject) throws RegistryException {
        registryService.updateProject(projectId, updatedProject);
    }

    public boolean deleteProject(String projectId) throws RegistryException {
        return registryService.deleteProject(projectId);
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjectIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
        return registryService.searchProjects(gatewayId, userName, accessibleProjectIds, filters, limit, offset);
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
        return registryService.getUserExperiments(gatewayId, userName, limit, offset);
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryException {
        return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
    }

    public ExperimentStatistics getExperimentStatistics(
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> accessibleExpIds,
            int limit,
            int offset)
            throws RegistryException {
        return registryService.getExperimentStatistics(
                gatewayId,
                fromTime,
                toTime,
                userName,
                applicationName,
                resourceHostName,
                accessibleExpIds,
                limit,
                offset);
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryException {
        return registryService.getExperiment(airavataExperimentId);
    }

    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryException {
        return registryService.createExperiment(gatewayId, experiment);
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws RegistryException {
        registryService.updateExperiment(airavataExperimentId, experiment);
    }

    public boolean deleteExperiment(String experimentId) throws RegistryException {
        return registryService.deleteExperiment(experimentId);
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
        return registryService.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
    }

    /**
     * Search experiments with sharing registry integration - processes filters and builds search criteria
     */
    public List<ExperimentSummaryModel> searchExperimentsWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception {
        List<String> accessibleExpIds = new ArrayList<>();
        Map<ExperimentSearchFields, String> filtersCopy = new HashMap<>(filters);
        List<SearchCriteria> sharingFilters = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue(gatewayId + ":EXPERIMENT");
        sharingFilters.add(searchCriteria);

        // Apply as much of the filters in the sharing API as possible,
        // removing each filter that can be filtered via the sharing API
        if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
            String fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
            SearchCriteria fromCreatedTimeCriteria = new SearchCriteria();
            fromCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            fromCreatedTimeCriteria.setSearchCondition(SearchCondition.GTE);
            fromCreatedTimeCriteria.setValue(fromTime);
            sharingFilters.add(fromCreatedTimeCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
            String toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
            SearchCriteria toCreatedTimeCriteria = new SearchCriteria();
            toCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            toCreatedTimeCriteria.setSearchCondition(SearchCondition.LTE);
            toCreatedTimeCriteria.setValue(toTime);
            sharingFilters.add(toCreatedTimeCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
            String projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
            SearchCriteria projectParentEntityCriteria = new SearchCriteria();
            projectParentEntityCriteria.setSearchField(EntitySearchField.PARRENT_ENTITY_ID);
            projectParentEntityCriteria.setSearchCondition(SearchCondition.EQUAL);
            projectParentEntityCriteria.setValue(projectId);
            sharingFilters.add(projectParentEntityCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
            String username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
            SearchCriteria usernameOwnerCriteria = new SearchCriteria();
            usernameOwnerCriteria.setSearchField(EntitySearchField.OWNER_ID);
            usernameOwnerCriteria.setSearchCondition(SearchCondition.EQUAL);
            usernameOwnerCriteria.setValue(username + "@" + gatewayId);
            sharingFilters.add(usernameOwnerCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
            String experimentName = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
            SearchCriteria experimentNameCriteria = new SearchCriteria();
            experimentNameCriteria.setSearchField(EntitySearchField.NAME);
            experimentNameCriteria.setSearchCondition(SearchCondition.LIKE);
            experimentNameCriteria.setValue(experimentName);
            sharingFilters.add(experimentNameCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
            String experimentDescription = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
            SearchCriteria experimentDescriptionCriteria = new SearchCriteria();
            experimentDescriptionCriteria.setSearchField(EntitySearchField.DESCRIPTION);
            experimentDescriptionCriteria.setSearchCondition(SearchCondition.LIKE);
            experimentDescriptionCriteria.setValue(experimentDescription);
            sharingFilters.add(experimentDescriptionCriteria);
        }
        // Grab all of the matching experiments in the sharing registry
        // unless all of the filtering can be done through the sharing API
        int searchOffset = 0;
        int searchLimit = Integer.MAX_VALUE;
        boolean filteredInSharing = filtersCopy.isEmpty();
        if (filteredInSharing) {
            searchOffset = offset;
            searchLimit = limit;
        }
        sharingClient
                .searchEntities(
                        authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        userName + "@" + gatewayId,
                        sharingFilters,
                        searchOffset,
                        searchLimit)
                .forEach(e -> accessibleExpIds.add(e.getEntityId()));
        int finalOffset = offset;
        // If no more filtering to be done (either empty or all done through sharing API), set the offset to 0
        if (filteredInSharing) {
            finalOffset = 0;
        }
        return searchExperiments(gatewayId, userName, accessibleExpIds, filtersCopy, limit, finalOffset);
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryException {
        return registryService.getExperimentStatus(airavataExperimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws RegistryException {
        return registryService.getExperimentOutputs(airavataExperimentId);
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryException {
        return registryService.getDetailedExperimentTree(airavataExperimentId);
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AppCatalogException {
        return registryService.getApplicationOutputs(appInterfaceId);
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws AppCatalogException {
        return registryService.getComputeResource(computeResourceId);
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws AppCatalogException {
        return registryService.registerComputeResource(computeResourceDescription);
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws AppCatalogException {
        return registryService.updateComputeResource(computeResourceId, computeResourceDescription);
    }

    public boolean deleteComputeResource(String computeResourceId) throws AppCatalogException {
        return registryService.deleteComputeResource(computeResourceId);
    }

    public Map<String, String> getAllComputeResourceNames() throws AppCatalogException {
        return registryService.getAllComputeResourceNames();
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws AppCatalogException {
        return registryService.registerStorageResource(storageResourceDescription);
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws AppCatalogException {
        return registryService.getStorageResource(storageResourceId);
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws AppCatalogException {
        return registryService.updateStorageResource(storageResourceId, storageResourceDescription);
    }

    public boolean deleteStorageResource(String storageResourceId) throws AppCatalogException {
        return registryService.deleteStorageResource(storageResourceId);
    }

    public Map<String, String> getAllStorageResourceNames() throws AppCatalogException {
        return registryService.getAllStorageResourceNames();
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws AppCatalogException {
        return registryService.registerGatewayResourceProfile(gatewayResourceProfile);
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws AppCatalogException {
        return registryService.getGatewayResourceProfile(gatewayID);
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws AppCatalogException {
        return registryService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) throws AppCatalogException {
        return registryService.deleteGatewayResourceProfile(gatewayID);
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        return registryService.getUserResourceProfile(userId, gatewayId);
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws AppCatalogException {
        return registryService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws AppCatalogException {
        return registryService.deleteUserResourceProfile(userId, gatewayID);
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws AppCatalogException {
        return registryService.getGroupResourceProfile(groupResourceProfileId);
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AppCatalogException {
        registryService.updateGroupResourceProfile(groupResourceProfile);
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws AppCatalogException {
        return registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
    }

    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryException {
        return registryService.getGatewayGroups(gatewayId);
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryException {
        return registryService.isGatewayGroupsExists(gatewayId);
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryException {
        registryService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
    }

    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryException {
        registryService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AppCatalogException {
        return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws AppCatalogException {
        return registryService.getApplicationDeployment(appDeploymentId);
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws AppCatalogException {
        return registryService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws AppCatalogException {
        return registryService.deleteApplicationDeployment(appDeploymentId);
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AppCatalogException {
        return registryService.getApplicationInterface(appInterfaceId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws AppCatalogException {
        return registryService.getApplicationDeployments(appModuleId);
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws AppCatalogException {
        return registryService.registerApplicationInterface(gatewayId, applicationInterface);
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws AppCatalogException {
        return registryService.updateApplicationInterface(appInterfaceId, applicationInterface);
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws AppCatalogException {
        return registryService.deleteApplicationInterface(appInterfaceId);
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws AppCatalogException {
        return registryService.getAllApplicationInterfaceNames(gatewayId);
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AppCatalogException {
        return registryService.getAllApplicationInterfaces(gatewayId);
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws AppCatalogException {
        return registryService.getApplicationInputs(appInterfaceId);
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws AppCatalogException {
        return registryService.registerApplicationModule(gatewayId, applicationModule);
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws AppCatalogException {
        return registryService.getApplicationModule(appModuleId);
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws AppCatalogException {
        return registryService.updateApplicationModule(appModuleId, applicationModule);
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws AppCatalogException {
        return registryService.getAllAppModules(gatewayId);
    }

    public boolean deleteApplicationModule(String appModuleId) throws AppCatalogException {
        return registryService.deleteApplicationModule(appModuleId);
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible app modules with sharing registry integration
     */
    public List<ApplicationModule> getAccessibleAppModulesWithSharing(
            SharingRegistryService.Client sharingClient, AuthzToken authzToken, String gatewayId) throws Exception {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        List<String> accessibleAppDeploymentIds = new ArrayList<>();
        if (ServerSettings.isEnableSharing()) {
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
            sharingClient
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            sharingFilters,
                            0,
                            -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
        }
        List<String> accessibleComputeResourceIds = new ArrayList<>();
        List<GroupResourceProfile> groupResourceProfileList =
                getGroupResourceListWithSharing(sharingClient, authzToken, gatewayId);
        for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
            List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                    groupResourceProfile.getComputePreferences();
            for (GroupComputeResourcePreference groupComputeResourcePreference : groupComputeResourcePreferenceList) {
                accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
            }
        }
        return getAccessibleAppModules(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryException {
        return registryService.getJobStatuses(airavataExperimentId);
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryException {
        return registryService.getJobDetails(airavataExperimentId);
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws AppCatalogException {
        return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        return registryService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission)
            throws AppCatalogException {
        return registryService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws AppCatalogException {
        return registryService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws AppCatalogException {
        return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws AppCatalogException {
        return registryService.getLocalJobSubmission(jobSubmissionId);
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws AppCatalogException {
        return registryService.getSSHJobSubmission(jobSubmissionId);
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws AppCatalogException {
        return registryService.getCloudJobSubmission(jobSubmissionId);
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws AppCatalogException {
        return registryService.getUnicoreJobSubmission(jobSubmissionId);
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws AppCatalogException {
        return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        return registryService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws AppCatalogException {
        return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        return registryService.registerResourceJobManager(resourceJobManager);
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AppCatalogException {
        return registryService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        return registryService.getResourceJobManager(resourceJobManagerId);
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        return registryService.deleteResourceJobManager(resourceJobManagerId);
    }

    public String addPasswordCredential(CredentialStoreService.Client csClient, PasswordCredential passwordCredential)
            throws CredentialStoreException, TException {
        return csClient.addPasswordCredential(passwordCredential);
    }

    public void deletePWDCredential(CredentialStoreService.Client csClient, String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        csClient.deletePWDCredential(tokenId, gatewayId);
    }

    public boolean deleteSSHCredential(CredentialStoreService.Client csClient, String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        return csClient.deleteSSHCredential(tokenId, gatewayId);
    }

    public CredentialSummary getCredentialSummary(
            CredentialStoreService.Client csClient, String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        return csClient.getCredentialSummary(tokenId, gatewayId);
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            CredentialStoreService.Client csClient, SummaryType type, List<String> accessibleTokenIds, String gatewayId)
            throws CredentialStoreException, TException {
        return csClient.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws AppCatalogException {
        return registryService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws AppCatalogException {
        return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws AppCatalogException {
        return registryService.getLocalDataMovement(dataMovementId);
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws AppCatalogException {
        return registryService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws AppCatalogException {
        return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
        return registryService.getUserProjects(gatewayId, userName, limit, offset);
    }

    /**
     * Get user projects with sharing registry integration
     */
    public List<Project> getUserProjectsWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            int limit,
            int offset)
            throws Exception {
        if (ServerSettings.isEnableSharing()) {
            // user projects + user accessible projects
            List<String> accessibleProjectIds = new ArrayList<>();
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":PROJECT");
            filters.add(searchCriteria);
            sharingClient
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            filters,
                            0,
                            -1)
                    .stream()
                    .forEach(p -> accessibleProjectIds.add(p.getEntityId()));
            List<Project> result;
            if (accessibleProjectIds.isEmpty()) {
                result = Collections.emptyList();
            } else {
                result = searchProjects(gatewayId, userName, accessibleProjectIds, new HashMap<>(), limit, offset);
            }
            return result;
        } else {
            return getUserProjects(gatewayId, userName, limit, offset);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return registryService.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible application deployments with sharing registry integration
     */
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String gatewayId,
            ResourcePermissionType permissionType)
            throws Exception {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        List<String> accessibleAppDeploymentIds = new ArrayList<>();
        if (ServerSettings.isEnableSharing()) {
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
            sharingClient
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            sharingFilters,
                            0,
                            -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
        }
        List<String> accessibleComputeResourceIds = new ArrayList<>();
        List<GroupResourceProfile> groupResourceProfileList =
                getGroupResourceListWithSharing(sharingClient, authzToken, gatewayId);
        for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
            List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                    groupResourceProfile.getComputePreferences();
            for (GroupComputeResourcePreference groupComputeResourcePreference : groupComputeResourcePreferenceList) {
                accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
            }
        }
        return getAccessibleApplicationDeployments(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws AppCatalogException {
        return registryService.getAppModuleDeployedResources(appModuleId);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String appModuleId,
            String gatewayId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return registryService.getAccessibleApplicationDeploymentsForAppModule(
                gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws AppCatalogException {
        return registryService.getAvailableAppInterfaceComputeResources(appInterfaceId);
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws AppCatalogException {
        return registryService.getSCPDataMovement(dataMovementId);
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws AppCatalogException {
        return registryService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        return registryService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws AppCatalogException {
        return registryService.getUnicoreDataMovement(dataMovementId);
    }

    public String addGridFTPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws AppCatalogException {
        return registryService.addGridFTPDataMovementDetails(resourceId, dmType, priorityOrder, gridFTPDataMovement);
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        return registryService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws AppCatalogException {
        return registryService.getGridFTPDataMovement(dataMovementId);
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws AppCatalogException {
        return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        return registryService.deleteBatchQueue(computeResourceId, queueName);
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AppCatalogException {
        return registryService.addGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws AppCatalogException {
        return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AppCatalogException {
        return registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws AppCatalogException {
        return registryService.getGatewayStoragePreference(gatewayID, storageId);
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws AppCatalogException {
        return registryService.getAllGatewayComputeResourcePreferences(gatewayID);
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws AppCatalogException {
        return registryService.getAllGatewayStoragePreferences(gatewayID);
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws AppCatalogException {
        return registryService.getAllGatewayResourceProfiles();
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AppCatalogException {
        return registryService.updateGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference dataStoragePreference) throws AppCatalogException {
        return registryService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AppCatalogException {
        return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws AppCatalogException {
        return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
    }

    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        return registryService.registerUserResourceProfile(userResourceProfile);
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        return registryService.isUserResourceProfileExists(userId, gatewayId);
    }

    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AppCatalogException {
        return registryService.addUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, userComputeResourcePreference);
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String userStorageResourceId, UserStoragePreference dataStoragePreference)
            throws AppCatalogException {
        return registryService.addUserStoragePreference(
                userId, gatewayID, userStorageResourceId, dataStoragePreference);
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws AppCatalogException {
        return registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws AppCatalogException {
        return registryService.getUserStoragePreference(userId, gatewayID, userStorageId);
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws AppCatalogException {
        return registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws AppCatalogException {
        return registryService.getAllUserStoragePreferences(userId, gatewayID);
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        return registryService.getAllUserResourceProfiles();
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AppCatalogException {
        return registryService.updateUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, userComputeResourcePreference);
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String userStorageId, UserStoragePreference userStoragePreference)
            throws AppCatalogException {
        return registryService.updateUserStoragePreference(userId, gatewayID, userStorageId, userStoragePreference);
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String userComputeResourceId)
            throws AppCatalogException {
        return registryService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws AppCatalogException {
        return registryService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        return registryService.getLatestQueueStatuses();
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AppCatalogException {
        return registryService.createGroupResourceProfile(groupResourceProfile);
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws AppCatalogException {
        return registryService.removeGroupResourceProfile(groupResourceProfileId);
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws AppCatalogException {
        return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws AppCatalogException {
        return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        return registryService.getGroupComputeResourcePolicy(resourcePolicyId);
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        return registryService.getBatchQueueResourcePolicy(resourcePolicyId);
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws AppCatalogException {
        return registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws AppCatalogException {
        return registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws AppCatalogException {
        return registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
    }

    public Parser getParser(String parserId, String gatewayId) throws RegistryException {
        return registryService.getParser(parserId, gatewayId);
    }

    public String saveParser(Parser parser) throws RegistryException {
        return registryService.saveParser(parser);
    }

    public List<Parser> listAllParsers(String gatewayId) throws RegistryException {
        return registryService.listAllParsers(gatewayId);
    }

    public void removeParser(String parserId, String gatewayId) throws RegistryException {
        registryService.removeParser(parserId, gatewayId);
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        return registryService.getParsingTemplate(templateId, gatewayId);
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryException {
        return registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryException {
        return registryService.saveParsingTemplate(parsingTemplate);
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        registryService.removeParsingTemplate(templateId, gatewayId);
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryException {
        return registryService.listAllParsingTemplates(gatewayId);
    }

    // Helper methods for sharing registry and authorization
    public GatewayGroups retrieveGatewayGroups(String gatewayId) throws TException {
        try {
            if (isGatewayGroupsExists(gatewayId)) {
                return getGatewayGroups(gatewayId);
            } else {
                return GatewayGroupsInitializer.initializeGatewayGroups(gatewayId);
            }
        } catch (Exception e) {
            throw new TException("Error retrieving gateway groups: " + e.getMessage(), e);
        }
    }

    public void createManageSharingPermissionTypeIfMissing(SharingRegistryService.Client sharingClient, String domainId)
            throws TException {
        // AIRAVATA-3297 Some gateways were created without the MANAGE_SHARING permission, so add it if missing
        String permissionTypeId = domainId + ":MANAGE_SHARING";
        try {
            if (!sharingClient.isPermissionExists(domainId, permissionTypeId)) {
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(permissionTypeId);
                permissionType.setDomainId(domainId);
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Manage sharing permission type");
                sharingClient.createPermissionType(permissionType);
                logger.info("Created MANAGE_SHARING permission type for domain " + domainId);
            }
        } catch (TException e) {
            throw e;
        } catch (Exception e) {
            throw new TException("Error creating MANAGE_SHARING permission type", e);
        }
    }

    public void shareEntityWithAdminGatewayGroups(SharingRegistryService.Client sharingClient, Entity entity)
            throws TException {
        final String domainId = entity.getDomainId();
        GatewayGroups gatewayGroups = retrieveGatewayGroups(domainId);
        createManageSharingPermissionTypeIfMissing(sharingClient, domainId);
        sharingClient.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()),
                domainId + ":MANAGE_SHARING",
                true);
        sharingClient.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()),
                domainId + ":WRITE",
                true);
        sharingClient.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                domainId + ":READ",
                true);
    }

    public boolean userHasAccessInternal(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String entityId,
            ResourcePermissionType permissionType) {
        final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        final String userId = authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + domainId;
        try {
            final boolean hasOwnerAccess = sharingClient.userHasAccess(
                    domainId, userId, entityId, domainId + ":" + ResourcePermissionType.OWNER);
            boolean hasAccess = false;
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                hasAccess = hasOwnerAccess
                        || sharingClient.userHasAccess(
                                domainId, userId, entityId, domainId + ":" + ResourcePermissionType.WRITE);
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                hasAccess = hasOwnerAccess
                        || sharingClient.userHasAccess(
                                domainId, userId, entityId, domainId + ":" + ResourcePermissionType.READ);
            } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
                hasAccess = hasOwnerAccess
                        || sharingClient.userHasAccess(
                                domainId, userId, entityId, domainId + ":" + ResourcePermissionType.MANAGE_SHARING);
            } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
                hasAccess = hasOwnerAccess;
            }
            return hasAccess;
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }

    // Credential management methods
    public String generateAndRegisterSSHKeys(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            String gatewayId,
            String userName,
            String description)
            throws Exception {
        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setUsername(userName);
        sshCredential.setGatewayId(gatewayId);
        sshCredential.setDescription(description);
        String key = csClient.addSSHCredential(sshCredential);
        try {
            Entity entity = new Entity();
            entity.setEntityId(key);
            entity.setDomainId(gatewayId);
            entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
            entity.setOwnerId(userName + "@" + gatewayId);
            entity.setName(key);
            entity.setDescription(description);
            sharingClient.createEntity(entity);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error(
                    "Rolling back ssh key creation for user " + userName + " and description [" + description + "]");
            csClient.deleteSSHCredential(key, gatewayId);
            throw new Exception("Failed to create sharing registry record", ex);
        }
        logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
        return key;
    }

    public String registerPwdCredential(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            String gatewayId,
            String userName,
            String loginUserName,
            String password,
            String description)
            throws Exception {
        PasswordCredential pwdCredential = new PasswordCredential();
        pwdCredential.setPortalUserName(userName);
        pwdCredential.setLoginUserName(loginUserName);
        pwdCredential.setPassword(password);
        pwdCredential.setDescription(description);
        pwdCredential.setGatewayId(gatewayId);
        String key = addPasswordCredential(csClient, pwdCredential);
        try {
            Entity entity = new Entity();
            entity.setEntityId(key);
            entity.setDomainId(gatewayId);
            entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
            entity.setOwnerId(userName + "@" + gatewayId);
            entity.setName(key);
            entity.setDescription(description);
            sharingClient.createEntity(entity);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("Rolling back password registration for user " + userName + " and description [" + description
                    + "]");
            try {
                deletePWDCredential(csClient, key, gatewayId);
            } catch (Exception rollbackEx) {
                logger.error("Failed to rollback password credential deletion", rollbackEx);
            }
            throw new Exception("Failed to create sharing registry record", ex);
        }
        logger.debug(
                "Airavata generated PWD credential for gateway : " + gatewayId + " and for user : " + loginUserName);
        return key;
    }

    public CredentialSummary getCredentialSummaryWithAuth(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String tokenId,
            String gatewayId)
            throws Exception {
        if (!userHasAccessInternal(sharingClient, authzToken, tokenId, ResourcePermissionType.READ)) {
            throw new Exception("User does not have permission to access this resource");
        }
        CredentialSummary credentialSummary = getCredentialSummary(csClient, tokenId, gatewayId);
        logger.debug("Airavata retrived the credential summary for token " + tokenId + "GatewayId: " + gatewayId);
        return credentialSummary;
    }

    public List<CredentialSummary> getAllCredentialSummariesWithAuth(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            SummaryType type,
            String gatewayId,
            String userName)
            throws Exception {
        List<SearchCriteria> filters = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN.name());
        filters.add(searchCriteria);
        List<String> accessibleTokenIds =
                sharingClient.searchEntities(gatewayId, userName + "@" + gatewayId, filters, 0, -1).stream()
                        .map(p -> p.getEntityId())
                        .collect(Collectors.toList());
        List<CredentialSummary> credentialSummaries =
                getAllCredentialSummaries(csClient, type, accessibleTokenIds, gatewayId);
        logger.debug(
                "Airavata successfully retrived credential summaries of type " + type + " GatewayId: " + gatewayId);
        return credentialSummaries;
    }

    public boolean deleteSSHPubKey(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String airavataCredStoreToken,
            String gatewayId)
            throws Exception {
        if (!userHasAccessInternal(sharingClient, authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
            throw new Exception("User does not have permission to delete this resource.");
        }
        logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : "
                + airavataCredStoreToken);
        return deleteSSHCredential(csClient, airavataCredStoreToken, gatewayId);
    }

    public boolean deletePWDCredentialWithAuth(
            CredentialStoreService.Client csClient,
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String airavataCredStoreToken,
            String gatewayId)
            throws Exception {
        if (!userHasAccessInternal(sharingClient, authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
            throw new Exception("User does not have permission to delete this resource.");
        }
        logger.debug("Airavata deleted PWD credential for gateway Id : " + gatewayId + " and with token id : "
                + airavataCredStoreToken);
        deletePWDCredential(csClient, airavataCredStoreToken, gatewayId);
        return true;
    }

    // Project management methods with sharing registry integration
    public String createProjectWithSharing(
            SharingRegistryService.Client sharingClient, String gatewayId, Project project) throws Exception {
        String projectId = createProject(gatewayId, project);
        if (ServerSettings.isEnableSharing()) {
            try {
                Entity entity = new Entity();
                entity.setEntityId(projectId);
                final String domainId = project.getGatewayId();
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + "PROJECT");
                entity.setOwnerId(project.getOwner() + "@" + domainId);
                entity.setName(project.getName());
                entity.setDescription(project.getDescription());
                sharingClient.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back project creation Proj ID : " + projectId);
                try {
                    deleteProject(projectId);
                } catch (RegistryException rollbackEx) {
                    logger.error("Failed to rollback project deletion", rollbackEx);
                }
                throw new Exception("Failed to create entry for project in Sharing Registry", ex);
            }
        }
        logger.debug("Airavata created project with project Id : " + projectId + " for gateway Id : " + gatewayId);
        return projectId;
    }

    public void updateProjectWithAuth(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String projectId,
            Project updatedProject)
            throws Exception {
        Project existingProject = getProject(projectId);
        if (ServerSettings.isEnableSharing()
                        && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingProject.getOwner())
                || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
                throw new Exception("User does not have permission to access this resource");
            }
        }
        if (!updatedProject.getOwner().equals(existingProject.getOwner())) {
            throw new Exception("Owner of a project cannot be changed");
        }
        if (!updatedProject.getGatewayId().equals(existingProject.getGatewayId())) {
            throw new Exception("Gateway ID of a project cannot be changed");
        }
        updateProject(projectId, updatedProject);
        logger.debug("Airavata updated project with project Id : " + projectId);
    }

    public boolean deleteProjectWithAuth(
            SharingRegistryService.Client sharingClient, AuthzToken authzToken, String projectId) throws Exception {
        Project existingProject = getProject(projectId);
        if (ServerSettings.isEnableSharing()
                        && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingProject.getOwner())
                || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
                throw new Exception("User does not have permission to access this resource");
            }
        }
        boolean ret = deleteProject(projectId);
        logger.debug("Airavata deleted project with project Id : " + projectId);
        return ret;
    }

    public Project getProjectWithAuth(
            SharingRegistryService.Client sharingClient, AuthzToken authzToken, String projectId) throws Exception {
        Project project = getProject(projectId);
        if (authzToken.getClaimsMap().get(Constants.USER_NAME).equals(project.getOwner())
                && authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(project.getGatewayId())) {
            return project;
        } else if (ServerSettings.isEnableSharing()) {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                throw new Exception("User does not have permission to access this resource");
            }
            return project;
        } else {
            return null;
        }
    }

    // Experiment management methods with sharing registry integration
    public String createExperimentWithSharing(
            SharingRegistryService.Client sharingClient, String gatewayId, ExperimentModel experiment)
            throws Exception {
        String experimentId = createExperiment(gatewayId, experiment);

        if (ServerSettings.isEnableSharing()) {
            try {
                Entity entity = new Entity();
                entity.setEntityId(experimentId);
                final String domainId = experiment.getGatewayId();
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
                entity.setOwnerId(experiment.getUserName() + "@" + domainId);
                entity.setName(experiment.getExperimentName());
                entity.setDescription(experiment.getDescription());
                entity.setParentEntityId(experiment.getProjectId());

                sharingClient.createEntity(entity);
                shareEntityWithAdminGatewayGroups(sharingClient, entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back experiment creation Exp ID : " + experimentId);
                deleteExperiment(experimentId);
                throw new Exception("Failed to create sharing registry record", ex);
            }
        }

        logger.info(
                experimentId,
                "Created new experiment with experiment name {} and id ",
                experiment.getExperimentName(),
                experimentId);
        return experimentId;
    }

    public String createExperimentWithSharingAndPublish(
            SharingRegistryService.Client sharingClient,
            Publisher statusPublisher,
            String gatewayId,
            ExperimentModel experiment)
            throws Exception {
        String experimentId = createExperimentWithSharing(sharingClient, gatewayId, experiment);

        if (statusPublisher != null) {
            ExperimentStatusChangeEvent event =
                    new ExperimentStatusChangeEvent(ExperimentState.CREATED, experimentId, gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(messageContext);
        }

        return experimentId;
    }

    public void validateLaunchExperimentAccess(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String gatewayId,
            ExperimentModel experiment)
            throws Exception {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

        // For backwards compatibility, if there is no groupResourceProfileId, look up one that is shared with the user
        if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
            // This will be handled by the handler calling getGroupResourceList
            throw new Exception("Experiment doesn't have groupResourceProfileId");
        }

        // Verify user has READ access to groupResourceProfileId
        if (!sharingClient.userHasAccess(
                gatewayId,
                username + "@" + gatewayId,
                experiment.getUserConfigurationData().getGroupResourceProfileId(),
                gatewayId + ":READ")) {
            throw new Exception("User " + username + " in gateway " + gatewayId
                    + " doesn't have access to group resource profile "
                    + experiment.getUserConfigurationData().getGroupResourceProfileId());
        }

        // Verify user has READ access to Application Deployment
        final String appInterfaceId = experiment.getExecutionId();
        ApplicationInterfaceDescription applicationInterfaceDescription = getApplicationInterface(appInterfaceId);

        List<String> appModuleIds = applicationInterfaceDescription.getApplicationModules();
        // Assume that there is only one app module for this interface
        String appModuleId = appModuleIds.get(0);
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions =
                getApplicationDeployments(appModuleId);

        if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
            final String resourceHostId = experiment
                    .getUserConfigurationData()
                    .getComputationalResourceScheduling()
                    .getResourceHostId();

            Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                    applicationDeploymentDescriptions.stream()
                            .filter(dep -> dep.getComputeHostId().equals(resourceHostId))
                            .findFirst();
            if (applicationDeploymentDescription.isPresent()) {
                final String appDeploymentId =
                        applicationDeploymentDescription.get().getAppDeploymentId();
                if (!sharingClient.userHasAccess(
                        gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                    throw new Exception("User " + username + " in gateway " + gatewayId
                            + " doesn't have access to app deployment " + appDeploymentId);
                }
            } else {
                throw new Exception("Application deployment doesn't exist for application interface " + appInterfaceId
                        + " and host " + resourceHostId + " in gateway " + gatewayId);
            }
        } else if (experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList() != null
                && !experiment
                        .getUserConfigurationData()
                        .getAutoScheduledCompResourceSchedulingList()
                        .isEmpty()) {
            List<ComputationalResourceSchedulingModel> compResourceSchedulingList =
                    experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList();
            for (ComputationalResourceSchedulingModel crScheduling : compResourceSchedulingList) {
                Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                        applicationDeploymentDescriptions.stream()
                                .filter(dep -> dep.getComputeHostId().equals(crScheduling.getResourceHostId()))
                                .findFirst();
                if (applicationDeploymentDescription.isPresent()) {
                    final String appDeploymentId =
                            applicationDeploymentDescription.get().getAppDeploymentId();
                    if (!sharingClient.userHasAccess(
                            gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                        throw new Exception("User " + username + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                }
            }
        }
    }

    public boolean deleteExperimentWithAuth(
            SharingRegistryService.Client sharingClient, AuthzToken authzToken, String experimentId) throws Exception {
        ExperimentModel experimentModel = getExperiment(experimentId);

        if (ServerSettings.isEnableSharing()
                        && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(experimentModel.getUserName())
                || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())) {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId, experimentId, gatewayId + ":WRITE")) {
                throw new Exception("User does not have permission to access this resource");
            }
        }

        if (!(experimentModel.getExperimentStatus().get(0).getState()
                == org.apache.airavata.model.status.ExperimentState.CREATED)) {
            throw new Exception("Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
        }
        return deleteExperiment(experimentId);
    }

    public ResourceType getResourceType(SharingRegistryService.Client sharingClient, String domainId, String entityId)
            throws TException {
        Entity entity = sharingClient.getEntity(domainId, entityId);
        for (ResourceType resourceType : ResourceType.values()) {
            if (entity.getEntityTypeId().equals(domainId + ":" + resourceType.name())) {
                return resourceType;
            }
        }
        throw new RuntimeException("Unrecognized entity type id: " + entity.getEntityTypeId());
    }

    // Gateway management methods with sharing registry integration
    public String addGatewayWithSharing(
            RegistryService.Client registryClient, SharingRegistryService.Client sharingClient, Gateway gateway)
            throws Exception {
        String gatewayId = registryClient.addGateway(gateway);
        Domain domain = new Domain();
        domain.setDomainId(gateway.getGatewayId());
        domain.setName(gateway.getGatewayName());
        domain.setDescription("Domain entry for " + domain.getName());
        sharingClient.createDomain(domain);

        // Creating Entity Types for each domain
        EntityType entityType = new EntityType();
        entityType.setEntityTypeId(domain.getDomainId() + ":PROJECT");
        entityType.setDomainId(domain.getDomainId());
        entityType.setName("PROJECT");
        entityType.setDescription("Project entity type");
        sharingClient.createEntityType(entityType);

        entityType = new EntityType();
        entityType.setEntityTypeId(domain.getDomainId() + ":EXPERIMENT");
        entityType.setDomainId(domain.getDomainId());
        entityType.setName("EXPERIMENT");
        entityType.setDescription("Experiment entity type");
        sharingClient.createEntityType(entityType);

        entityType = new EntityType();
        entityType.setEntityTypeId(domain.getDomainId() + ":FILE");
        entityType.setDomainId(domain.getDomainId());
        entityType.setName("FILE");
        entityType.setDescription("File entity type");
        sharingClient.createEntityType(entityType);

        entityType = new EntityType();
        entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
        entityType.setDomainId(domain.getDomainId());
        entityType.setName("APPLICATION-DEPLOYMENT");
        entityType.setDescription("Application Deployment entity type");
        sharingClient.createEntityType(entityType);

        entityType = new EntityType();
        entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
        entityType.setDomainId(domain.getDomainId());
        entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
        entityType.setDescription("Group Resource Profile entity type");
        sharingClient.createEntityType(entityType);

        // Creating Permission Types for each domain
        PermissionType permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domain.getDomainId() + ":READ");
        permissionType.setDomainId(domain.getDomainId());
        permissionType.setName("READ");
        permissionType.setDescription("Read permission type");
        sharingClient.createPermissionType(permissionType);

        permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domain.getDomainId() + ":WRITE");
        permissionType.setDomainId(domain.getDomainId());
        permissionType.setName("WRITE");
        permissionType.setDescription("Write permission type");
        sharingClient.createPermissionType(permissionType);

        permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domain.getDomainId() + ":MANAGE_SHARING");
        permissionType.setDomainId(domain.getDomainId());
        permissionType.setName("MANAGE_SHARING");
        permissionType.setDescription("Sharing permission type");
        sharingClient.createPermissionType(permissionType);

        logger.debug("Airavata successfully created the gateway with " + gatewayId);
        return gatewayId;
    }

    // Event publishing methods
    public void publishExperimentSubmitEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws Exception {
        ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
        MessageContext messageContext = new MessageContext(
                event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    public void publishExperimentCancelEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws Exception {
        ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
        MessageContext messageContext = new MessageContext(
                event,
                MessageType.EXPERIMENT_CANCEL,
                "CANCEL.EXP-" + UUID.randomUUID().toString(),
                gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    public void publishExperimentIntermediateOutputsEvent(
            Publisher experimentPublisher, String gatewayId, String experimentId, List<String> outputNames)
            throws Exception {
        ExperimentIntermediateOutputsEvent event =
                new ExperimentIntermediateOutputsEvent(experimentId, gatewayId, outputNames);
        MessageContext messageContext = new MessageContext(
                event,
                MessageType.INTERMEDIATE_OUTPUTS,
                "INTERMEDIATE_OUTPUTS.EXP-" + UUID.randomUUID().toString(),
                gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    /**
     * Validate and fetch intermediate outputs - checks access, job state, and existing processes
     */
    public void validateAndFetchIntermediateOutputs(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames,
            Publisher experimentPublisher)
            throws Exception {
        // Verify that user has WRITE access to experiment
        final boolean hasAccess =
                userHasAccessInternal(sharingClient, authzToken, airavataExperimentId, ResourcePermissionType.WRITE);
        if (!hasAccess) {
            throw new Exception("User does not have WRITE access to this experiment");
        }

        // Verify that the experiment's job is currently ACTIVE
        ExperimentModel existingExperiment = getExperiment(airavataExperimentId);
        List<JobModel> jobs = getJobDetails(airavataExperimentId);
        boolean anyJobIsActive = jobs.stream().anyMatch(j -> {
            if (j.getJobStatusesSize() > 0) {
                return j.getJobStatuses().get(j.getJobStatusesSize() - 1).getJobState() == JobState.ACTIVE;
            } else {
                return false;
            }
        });
        if (!anyJobIsActive) {
            throw new Exception("Experiment does not have currently ACTIVE job");
        }

        // Figure out if there are any currently running intermediate output fetching processes for outputNames
        // First, find any existing intermediate output fetch processes for outputNames
        List<ProcessModel> intermediateOutputFetchProcesses = existingExperiment.getProcesses().stream()
                .filter(p -> {
                    // Filter out completed or failed processes
                    if (p.getProcessStatusesSize() > 0) {
                        ProcessStatus latestStatus = p.getProcessStatuses().get(p.getProcessStatusesSize() - 1);
                        if (latestStatus.getState() == ProcessState.COMPLETED
                                || latestStatus.getState() == ProcessState.FAILED) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(p -> p.getTasks().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                .filter(p -> p.getProcessOutputs().stream().anyMatch(o -> outputNames.contains(o.getName())))
                .collect(Collectors.toList());
        if (!intermediateOutputFetchProcesses.isEmpty()) {
            throw new Exception("There are already intermediate output fetching tasks running for those outputs.");
        }

        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        publishExperimentIntermediateOutputsEvent(experimentPublisher, gatewayId, airavataExperimentId, outputNames);
    }

    /**
     * Get intermediate output process status - finds the most recent matching process and returns its status
     */
    public ProcessStatus getIntermediateOutputProcessStatusInternal(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames)
            throws Exception {
        // Verify that user has READ access to experiment
        final boolean hasAccess =
                userHasAccessInternal(sharingClient, authzToken, airavataExperimentId, ResourcePermissionType.READ);
        if (!hasAccess) {
            throw new Exception("User does not have READ access to this experiment");
        }

        ExperimentModel existingExperiment = getExperiment(airavataExperimentId);

        // Find the most recent intermediate output fetching process for the outputNames
        // Assumption: only one of these output fetching processes runs at a
        // time so we only need to check the status of the most recent one
        Optional<ProcessModel> mostRecentOutputFetchProcess = existingExperiment.getProcesses().stream()
                .filter(p -> p.getTasks().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                .filter(p -> {
                    List<String> names =
                            p.getProcessOutputs().stream().map(o -> o.getName()).collect(Collectors.toList());
                    return new HashSet<>(names).equals(new HashSet<>(outputNames));
                })
                .sorted(Comparator.comparing(ProcessModel::getLastUpdateTime).reversed())
                .findFirst();

        if (!mostRecentOutputFetchProcess.isPresent()) {
            throw new Exception("No matching intermediate output fetching process found.");
        }

        ProcessStatus result;
        // Determine the most recent status for the most recent process
        ProcessModel process = mostRecentOutputFetchProcess.get();
        if (process.getProcessStatusesSize() > 0) {
            result = process.getProcessStatuses().get(process.getProcessStatusesSize() - 1);
        } else {
            // Process has no statuses so it must be created but not yet running
            result = new ProcessStatus(ProcessState.CREATED);
        }

        return result;
    }

    // Access control methods
    public List<String> getAllAccessibleUsers(
            SharingRegistryService.Client sharingClient,
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryService.Client, ResourcePermissionType, Collection<User>> userListFunction)
            throws Exception {
        HashSet<String> accessibleUsers = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            userListFunction.apply(sharingClient, ResourcePermissionType.WRITE).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingClient, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            userListFunction.apply(sharingClient, ResourcePermissionType.READ).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingClient, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
            userListFunction.apply(sharingClient, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            userListFunction.apply(sharingClient, ResourcePermissionType.MANAGE_SHARING).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingClient, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        }
        return new ArrayList<>(accessibleUsers);
    }

    public List<String> getAllAccessibleGroups(
            SharingRegistryService.Client sharingClient,
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryService.Client, ResourcePermissionType, Collection<UserGroup>> groupListFunction)
            throws Exception {
        HashSet<String> accessibleGroups = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            groupListFunction.apply(sharingClient, ResourcePermissionType.WRITE).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            groupListFunction.apply(sharingClient, ResourcePermissionType.READ).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            groupListFunction.apply(sharingClient, ResourcePermissionType.MANAGE_SHARING).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        }
        return new ArrayList<>(accessibleGroups);
    }

    /**
     * Get all accessible users for a resource (includes shared and directly shared)
     */
    public List<String> getAllAccessibleUsersWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            boolean directlySharedOnly)
            throws Exception {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        BiFunction<SharingRegistryService.Client, ResourcePermissionType, Collection<User>> userListFunction;
        if (directlySharedOnly) {
            userListFunction = (c, t) -> {
                try {
                    return c.getListOfDirectlySharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            userListFunction = (c, t) -> {
                try {
                    return c.getListOfSharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return getAllAccessibleUsers(sharingClient, gatewayId, resourceId, permissionType, userListFunction);
    }

    /**
     * Get all accessible groups for a resource (includes shared and directly shared)
     */
    public List<String> getAllAccessibleGroupsWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            boolean directlySharedOnly)
            throws Exception {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        BiFunction<SharingRegistryService.Client, ResourcePermissionType, Collection<UserGroup>> groupListFunction;
        if (directlySharedOnly) {
            groupListFunction = (c, t) -> {
                try {
                    return c.getListOfDirectlySharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            groupListFunction = (c, t) -> {
                try {
                    return c.getListOfSharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return getAllAccessibleGroups(sharingClient, gatewayId, resourceId, permissionType, groupListFunction);
    }

    // Group resource profile management with sharing registry integration
    public String createGroupResourceProfileWithSharing(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            GroupResourceProfile groupResourceProfile)
            throws Exception {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        validateGroupResourceProfile(sharingClient, authzToken, groupResourceProfile);
        String groupResourceProfileId = createGroupResourceProfile(groupResourceProfile);
        if (ServerSettings.isEnableSharing()) {
            try {
                Entity entity = new Entity();
                entity.setEntityId(groupResourceProfileId);
                entity.setDomainId(groupResourceProfile.getGatewayId());
                entity.setEntityTypeId(groupResourceProfile.getGatewayId() + ":" + "GROUP_RESOURCE_PROFILE");
                entity.setOwnerId(userName + "@" + groupResourceProfile.getGatewayId());
                entity.setName(groupResourceProfile.getGroupResourceProfileName());

                sharingClient.createEntity(entity);
                shareEntityWithAdminGatewayGroups(sharingClient, entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back group resource profile creation Group Resource Profile ID : "
                        + groupResourceProfileId);
                try {
                    removeGroupResourceProfile(groupResourceProfileId);
                } catch (AppCatalogException rollbackEx) {
                    logger.error("Failed to rollback group resource profile deletion", rollbackEx);
                }
                throw new Exception("Failed to create sharing registry record", ex);
            }
        }
        return groupResourceProfileId;
    }

    public void validateGroupResourceProfile(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            GroupResourceProfile groupResourceProfile)
            throws Exception {
        Set<String> tokenIds = new HashSet<>();
        if (groupResourceProfile.getComputePreferences() != null) {
            for (GroupComputeResourcePreference groupComputeResourcePreference :
                    groupResourceProfile.getComputePreferences()) {
                if (groupComputeResourcePreference.getResourceSpecificCredentialStoreToken() != null) {
                    tokenIds.add(groupComputeResourcePreference.getResourceSpecificCredentialStoreToken());
                }
            }
        }
        if (groupResourceProfile.getDefaultCredentialStoreToken() != null) {
            tokenIds.add(groupResourceProfile.getDefaultCredentialStoreToken());
        }
        for (String tokenId : tokenIds) {
            if (!userHasAccessInternal(sharingClient, authzToken, tokenId, ResourcePermissionType.READ)) {
                throw new Exception("User does not have READ permission to credential token " + tokenId + ".");
            }
        }
    }

    // Launch experiment business logic
    public void launchExperimentWithValidation(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String gatewayId,
            String airavataExperimentId,
            Publisher experimentPublisher)
            throws Exception {
        ExperimentModel experiment = getExperiment(airavataExperimentId);

        if (experiment == null) {
            throw new Exception("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
        }
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

        // For backwards compatibility, if there is no groupResourceProfileId, look up one that is shared with the user
        if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
            List<GroupResourceProfile> groupResourceProfiles =
                    getGroupResourceListWithSharing(sharingClient, authzToken, gatewayId);
            if (groupResourceProfiles != null && !groupResourceProfiles.isEmpty()) {
                // Just pick the first one
                final String groupResourceProfileId =
                        groupResourceProfiles.get(0).getGroupResourceProfileId();
                logger.warn(
                        "Experiment {} doesn't have groupResourceProfileId, picking first one user has access to: {}",
                        airavataExperimentId,
                        groupResourceProfileId);
                experiment.getUserConfigurationData().setGroupResourceProfileId(groupResourceProfileId);
                updateExperimentConfiguration(airavataExperimentId, experiment.getUserConfigurationData());
            } else {
                throw new Exception("User " + username + " in gateway " + gatewayId
                        + " doesn't have access to any group resource profiles.");
            }
        }

        // Validate access to group resource profile and application deployments
        validateLaunchExperimentAccess(sharingClient, authzToken, gatewayId, experiment);
        publishExperimentSubmitEvent(experimentPublisher, gatewayId, airavataExperimentId);
    }

    /**
     * Get group resource list with sharing registry integration
     */
    public List<GroupResourceProfile> getGroupResourceListWithSharing(
            SharingRegistryService.Client sharingClient, AuthzToken authzToken, String gatewayId) throws Exception {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        List<String> accessibleGroupResProfileIds = new ArrayList<>();
        if (ServerSettings.isEnableSharing()) {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
            filters.add(searchCriteria);
            sharingClient
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            filters,
                            0,
                            -1)
                    .stream()
                    .forEach(p -> accessibleGroupResProfileIds.add(p.getEntityId()));
        }
        return getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
    }
}
