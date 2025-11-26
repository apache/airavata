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
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
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
import org.apache.airavata.model.appcatalog.storageresource.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.StorageVolumeInfo;
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
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.error.ProjectNotFoundException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
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
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataService {
    private static final Logger logger = LoggerFactory.getLogger(AiravataService.class);

    private record StorageInfoContext(String loginUserName, String credentialToken, AgentAdaptor adaptor) {}

    private boolean validateString(String name) {
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0) {
            valid = false;
        }
        return valid;
    }

    private AiravataClientException clientException(AiravataErrorType errorType, String parameter) {
        var exception = new AiravataClientException();
        exception.setAiravataErrorType(errorType);
        exception.setParameter(parameter);
        return exception;
    }

    private boolean safeIsUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayId) {
        try {
            return isUserResourceProfileExists(userId, gatewayId);
        } catch (Throwable e) {
            logger.error("Error checking if user resource profile exists", e);
            return false;
        }
    }

    private boolean isGatewayResourceProfileExists(String gatewayId) {
        try {
            var profile = getGatewayResourceProfile(gatewayId);
            return profile != null;
        } catch (Throwable e) {
            logger.error("Error while checking if gateway resource profile exists", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    private RuntimeException convertException(Throwable e, String msg) {
        if (e instanceof InvalidRequestException ||
            e instanceof AiravataClientException ||
            e instanceof AiravataSystemException ||
            e instanceof AuthorizationException ||
            e instanceof ExperimentNotFoundException ||
            e instanceof ProjectNotFoundException) {
            throw sneakyThrow(e);
        }
        if (e instanceof RegistryException || e instanceof AppCatalogException ||
            e instanceof CredentialStoreException ||
            e instanceof org.apache.airavata.sharing.registry.models.SharingRegistryException) {
            logger.error(msg, e);
            var exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + ". More info : " + e.getMessage());
            exception.initCause(e);
            throw sneakyThrow(exception);
        }
        logger.error(msg, e);
        var exception = new AiravataSystemException();
        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
        exception.setMessage(msg + ". More info : " + e.getMessage());
        exception.initCause(e);
        throw sneakyThrow(exception);
    }

    private RegistryService registryService =
            new RegistryService();

    private SharingRegistryService sharingRegistryService =
            new SharingRegistryService();

    private CredentialStoreService credentialStoreService;

    public AiravataService() {
        try {
            credentialStoreService = new CredentialStoreService();
        } catch (Exception e) {
            logger.error("Failed to initialize CredentialStoreService", e);
            throw new RuntimeException("Failed to initialize CredentialStoreService", e);
        }
    }

    public void init() {
        try {
            initSharingRegistry();
            postInitDefaultGateway();
        } catch (Exception e) {
            logger.error("Error occurred while initializing Airavata Service", e);
            throw new RuntimeException("Error occurred while initializing Airavata Service", e);
        }
    }


    private void postInitDefaultGateway() {
        try {
            var gatewayResourceProfile = getGatewayResourceProfile(ServerSettings.getDefaultUserGateway());
            if (gatewayResourceProfile != null && gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {

                logger.debug("Starting to add the password credential for default gateway : "
                        + ServerSettings.getDefaultUserGateway());

                var passwordCredential = new PasswordCredential();
                passwordCredential.setPortalUserName(ServerSettings.getDefaultUser());
                passwordCredential.setGatewayId(ServerSettings.getDefaultUserGateway());
                passwordCredential.setLoginUserName(ServerSettings.getDefaultUser());
                passwordCredential.setPassword(ServerSettings.getDefaultUserPassword());
                passwordCredential.setDescription("Credentials for default gateway");
                String token = null;
                try {
                    logger.info("Creating password credential for default gateway");
                    token = addPasswordCredential(passwordCredential);
                } catch (Throwable ex) {
                    logger.error(
                            "Failed to create the password credential for the default gateway : "
                                    + ServerSettings.getDefaultUserGateway(),
                            ex);
                }

                if (token != null) {
                    logger.debug("Adding password credential token " + token + " to the default gateway : "
                            + ServerSettings.getDefaultUserGateway());
                    gatewayResourceProfile.setIdentityServerPwdCredToken(token);
                    gatewayResourceProfile.setIdentityServerTenant(ServerSettings.getDefaultUserGateway());
                    updateGatewayResourceProfile(
                            ServerSettings.getDefaultUserGateway(), gatewayResourceProfile);
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to add the password credentials for the default gateway", e);
        }
    }

    private void initSharingRegistry() throws Exception {
        try {
            if (!isDomainExists(ServerSettings.getDefaultUserGateway())) {
                var domain = new Domain();
                domain.setDomainId(ServerSettings.getDefaultUserGateway());
                domain.setName(ServerSettings.getDefaultUserGateway());
                domain.setDescription("Domain entry for " + domain.getName());
                createDomain(domain);

                var user = new User();
                user.setDomainId(domain.getDomainId());
                user.setUserId(ServerSettings.getDefaultUser() + "@" + ServerSettings.getDefaultUserGateway());
                user.setUserName(ServerSettings.getDefaultUser());
                createUser(user);

                // Creating Entity Types for each domain
                var entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":PROJECT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":EXPERIMENT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":FILE");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("APPLICATION-DEPLOYMENT");
                entityType.setDescription("Application Deployment entity type");
                createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDescription("Group Resource Profile entity type");
                createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDescription("Credential Store Token entity type");
                createEntityType(entityType);

                // Creating Permission Types for each domain
                var permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":READ");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":WRITE");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
                createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":MANAGE_SHARING");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                createPermissionType(permissionType);
            }
        } catch (Throwable ex) {
            throw ex;
        }
    }

    public List<String> getAllUsersInGateway(String gatewayId) {
        try {
            return registryService.getAllUsersInGateway(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving users");
        }
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway) {
        try {
            return registryService.updateGateway(gatewayId, updatedGateway);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating gateway");
        }
    }

    public Gateway getGateway(String gatewayId) {
        try {
            var result = registryService.getGateway(gatewayId);
            logger.debug("Airavata found the gateway with " + gatewayId);
            return result;
        } catch (Throwable e) {
            throw convertException(e, "Error while getting the gateway");
        }
    }

    public boolean deleteGateway(String gatewayId) {
        try {
            return registryService.deleteGateway(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting the gateway");
        }
    }

    public List<Gateway> getAllGateways() {
        try {
            logger.debug("Airavata searching for all gateways");
            return registryService.getAllGateways();
        } catch (Throwable e) {
            throw convertException(e, "Error while getting all the gateways");
        }
    }

    public boolean isGatewayExist(String gatewayId) {
        try {
            logger.debug("Airavata verifying if the gateway with " + gatewayId + "exits");
            return registryService.isGatewayExist(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while getting gateway");
        }
    }

    public String createNotification(Notification notification) {
        try {
            return registryService.createNotification(notification);
        } catch (Throwable e) {
            throw convertException(e, "Error while creating notification");
        }
    }

    public boolean updateNotification(Notification notification) {
        try {
            return registryService.updateNotification(notification);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating notification");
        }
    }

    public boolean deleteNotification(String gatewayId, String notificationId) {
        try {
            return registryService.deleteNotification(gatewayId, notificationId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting notification");
        }
    }

    public Notification getNotification(String gatewayId, String notificationId) {
        try {
            return registryService.getNotification(gatewayId, notificationId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving notification");
        }
    }

    public List<Notification> getAllNotifications(String gatewayId) {
        try {
            return registryService.getAllNotifications(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while getting all notifications");
        }
    }

    public String registerDataProduct(DataProductModel dataProductModel) {
        try {
            return registryService.registerDataProduct(dataProductModel);
        } catch (Throwable e) {
            var msg = "Error in registering the data resource" + dataProductModel.getProductName() + ".";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public DataProductModel getDataProduct(String productUri) {
        try {
            return registryService.getDataProduct(productUri);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving data product");
        }
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) {
        try {
            return registryService.registerReplicaLocation(replicaLocationModel);
        } catch (Throwable e) {
            var msg = "Error in retreiving the replica " + replicaLocationModel.getReplicaName() + ".";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public DataProductModel getParentDataProduct(String productUri) {
        try {
            return registryService.getParentDataProduct(productUri);
        } catch (Throwable e) {
            var msg = "Error in retreiving the parent data product for " + productUri + ".";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public List<DataProductModel> getChildDataProducts(String productUri) {
        try {
            return registryService.getChildDataProducts(productUri);
        } catch (Throwable e) {
            var msg = "Error in retreiving the child products for " + productUri + ".";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean isUserExists(String gatewayId, String userName) {
        try {
            logger.debug("Checking if the user" + userName + "exists in the gateway" + gatewayId);
            return registryService.isUserExists(gatewayId, userName);
        } catch (Throwable e) {
            throw convertException(e, "Error while verifying user");
        }
    }

    public Project getProject(String projectId) {
        try {
            return registryService.getProject(projectId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving the project");
        }
    }

    public String createProject(String gatewayId, Project project) {
        try {
            return registryService.createProject(gatewayId, project);
        } catch (Throwable e) {
            throw convertException(e, "Error while creating project");
        }
    }

    public void updateProject(String projectId, Project updatedProject) {
        try {
            registryService.updateProject(projectId, updatedProject);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating project");
        }
    }

    public boolean deleteProject(String projectId) {
        try {
            return registryService.deleteProject(projectId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing the project");
        }
    }

    public List<Project> searchProjectsWithSharing(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
         try {
            var accessibleProjIds = new ArrayList<String>();
            List<Project> result;
            if (ServerSettings.isEnableSharing()) {
                var sharingFilters = new ArrayList<SearchCriteria>();
                var searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                sharingFilters.add(searchCriteria);
                sharingRegistryService.searchEntities(
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                                userName + "@" + gatewayId,
                                sharingFilters,
                                0,
                                Integer.MAX_VALUE)
                        .stream()
                        .forEach(e -> accessibleProjIds.add(e.getEntityId()));
                if (accessibleProjIds.isEmpty()) {
                    result = Collections.emptyList();
                } else {
                    result = registryService.searchProjects(
                            gatewayId, userName, accessibleProjIds, filters, limit, offset);
                }
            } else {
                result = registryService.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
            }
            return result;
         } catch (Throwable e) {
             throw convertException(e, "Error while retrieving projects");
         }
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjectIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset) throws RegistryServiceException {
        return registryService.searchProjects(gatewayId, userName, accessibleProjectIds, filters, limit, offset);
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting user experiments");
        }
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting experiments in project");
        }
    }

    public List<ExperimentModel> getExperimentsInProject(
            AuthzToken authzToken, String projectId, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            var project = getProject(projectId);
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()
                            && (!authzToken.getClaimsMap().get(Constants.USER_NAME).equals(project.getOwner())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(project.getGatewayId()))) {
                    var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!userHasAccess(
                            gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
            }
            return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving the experiments");
        }
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
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
        } catch (Throwable e) {
            throw convertException(e, "Error while getting experiment statistics");
        }
    }

    public ExperimentModel getExperiment(String airavataExperimentId) {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving experiment");
        }
    }

    public String createExperiment(String gatewayId, ExperimentModel experiment) {
        try {
            return registryService.createExperiment(gatewayId, experiment);
        } catch (Throwable e) {
            throw convertException(e, "Error while creating experiment");
        }
    }

    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId) throws AuthorizationException {
        try {
            var existingExperiment = getExperiment(airavataExperimentId);
            if (authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingExperiment.getUserName())
                    && authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingExperiment.getGatewayId())) {
                return existingExperiment;
            } else if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId, userId + "@" + gatewayId, airavataExperimentId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
                return existingExperiment;
            } else {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while getting the experiment");
        }
    }

    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId) throws AuthorizationException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var existingExperiment = getExperiment(airavataExperimentId);
            if (gatewayId.equals(existingExperiment.getGatewayId())) {
                return existingExperiment;
            } else {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while getting experiment by admin");
        }
    }

    public void updateExperiment(AuthzToken authzToken, String airavataExperimentId, ExperimentModel experiment) throws AuthorizationException {
         try {
            var existingExperiment = getExperiment(airavataExperimentId);
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()
                            && (!authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingExperiment.getUserName())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingExperiment.getGatewayId()))) {
                    var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!userHasAccess(
                            gatewayId, userId + "@" + gatewayId, airavataExperimentId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
            }

            try {
                // Update name, description and parent on Entity
                // TODO: update the experiment via a DB event
                var entity = getEntity(gatewayId, airavataExperimentId);
                entity.setName(experiment.getExperimentName());
                entity.setDescription(experiment.getDescription());
                entity.setParentEntityId(experiment.getProjectId());
                updateEntity(entity);
            } catch (Throwable e) {
                throw new Exception("Failed to update entity in sharing registry", e);
            }

            updateExperiment(airavataExperimentId, experiment);
         } catch (Throwable e) {
             throw convertException(e, "Error while updating experiment");
         }
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) {
        try {
            registryService.updateExperiment(airavataExperimentId, experiment);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating experiment");
        }
    }

    public boolean deleteExperiment(String experimentId) {
        try {
            return registryService.deleteExperiment(experimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting experiment");
        }
    }

    public String cloneExperimentInternal(
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, AuthorizationException {
        try {
            if (existingExperiment == null) {
                logger.error(
                        existingExperimentID,
                        "Error while cloning experiment {}, experiment doesn't exist.",
                        existingExperimentID);
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + existingExperimentID + " does not exist in the system..");
            }
            if (newExperimentProjectId != null) {
                // getProject will apply sharing permissions
                var project = getProjectWithAuth(authzToken, newExperimentProjectId);
                if (project == null) {
                    logger.error(
                            "Error while cloning experiment {}, project {} doesn't exist.",
                            existingExperimentID,
                            newExperimentProjectId);
                    throw new ProjectNotFoundException(
                            "Requested project id " + newExperimentProjectId + " does not exist in the system..");
                }
                existingExperiment.setProjectId(project.getProjectID());
            }

            // make sure user has write access to the project
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!userHasAccess(
                    gatewayId, userId + "@" + gatewayId, existingExperiment.getProjectId(), gatewayId + ":WRITE")) {
                logger.error(
                        "Error while cloning experiment {}, user doesn't have write access to project {}",
                        existingExperimentID,
                        existingExperiment.getProjectId());
                throw new AuthorizationException("User does not have permission to clone an experiment in this project");
            }

            existingExperiment.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            if (existingExperiment.getExecutionId() != null) {
                try {
                    var applicationOutputs = getApplicationOutputs(existingExperiment.getExecutionId());
                    existingExperiment.setExperimentOutputs(applicationOutputs);
                } catch (Throwable e) {
                    logger.warn("Error getting application outputs for experiment clone: " + e.getMessage());
                }
            }
            if (validateString(newExperimentName)) {
                existingExperiment.setExperimentName(newExperimentName);
            }
            existingExperiment.unsetErrors();
            existingExperiment.unsetProcesses();
            existingExperiment.unsetExperimentStatus();
            if (existingExperiment.getUserConfigurationData() != null
                    && existingExperiment.getUserConfigurationData().getComputationalResourceScheduling() != null
                    && existingExperiment
                                    .getUserConfigurationData()
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId()
                            != null) {
                var compResourceId = existingExperiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();

                try {
                    var computeResource = getComputeResource(compResourceId);
                    if (!computeResource.isEnabled()) {
                        existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                    }
                } catch (Throwable e) {
                    logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
                }
            }
            logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
            existingExperiment.setUserName(userId);
            
            var expId = createExperiment(gatewayId, existingExperiment);
            if (ServerSettings.isEnableSharing()) {
                try {
                    var entity = new Entity();
                    entity.setEntityId(expId);
                    final String domainId = existingExperiment.getGatewayId();
                    entity.setDomainId(domainId);
                    entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
                    entity.setOwnerId(existingExperiment.getUserName() + "@" + domainId);
                    entity.setName(existingExperiment.getExperimentName());
                    entity.setDescription(existingExperiment.getDescription());
                    createEntity(entity);
                    shareEntityWithAdminGatewayGroups(entity);
                } catch (Throwable ex) {
                    logger.error(ex.getMessage(), ex);
                    logger.error("rolling back experiment creation Exp ID : " + expId);
                    try {
                        deleteExperiment(expId);
                    } catch (Throwable e) {
                        logger.error("Error deleting experiment during rollback: " + e.getMessage());
                    }
                    throw convertException(ex, "Error while creating entity for cloned experiment");
                }
            }

            return expId;
        } catch (ExperimentNotFoundException | ProjectNotFoundException | AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while cloning experiment");
        }
    }

    public void terminateExperiment(Publisher experimentPublisher, String airavataExperimentId, String gatewayId)
            throws ExperimentNotFoundException {
        try {
            var existingExperiment = getExperiment(airavataExperimentId);
            var experimentLastStatus = getExperimentStatus(airavataExperimentId);
            if (existingExperiment == null) {
                logger.error(
                        airavataExperimentId,
                        "Error while cancelling experiment {}, experiment doesn't exist.",
                        airavataExperimentId);
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            switch (experimentLastStatus.getState()) {
                case COMPLETED:
                case CANCELED:
                case FAILED:
                case CANCELING:
                    logger.warn(
                            "Can't terminate already {} experiment",
                            existingExperiment
                                    .getExperimentStatus()
                                    .get(0)
                                    .getState()
                                    .name());
                    break;
                case CREATED:
                    logger.warn("Experiment termination is only allowed for launched experiments.");
                    break;
                default:
                    publishExperimentCancelEvent(experimentPublisher, gatewayId, airavataExperimentId);
                    logger.debug("Airavata cancelled experiment with experiment id : " + airavataExperimentId);
                    break;
            }
        } catch (ExperimentNotFoundException e) {
            throw e;
        } catch (Throwable e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            throw convertException(e, "Error occurred");
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset) throws RegistryServiceException {
        return registryService.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
    }

    /**
     * Search experiments with sharing registry integration - processes filters and builds search criteria
     */
    public List<ExperimentSummaryModel> searchExperimentsWithSharing(
            
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
        var accessibleExpIds = new ArrayList<String>();
        var filtersCopy = new HashMap<ExperimentSearchFields, String>(filters);
        var sharingFilters = new ArrayList<SearchCriteria>();
        var searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue(gatewayId + ":EXPERIMENT");
        sharingFilters.add(searchCriteria);

        // Apply as much of the filters in the sharing API as possible,
        // removing each filter that can be filtered via the sharing API
        if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
            var fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
            var fromCreatedTimeCriteria = new SearchCriteria();
            fromCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            fromCreatedTimeCriteria.setSearchCondition(SearchCondition.GTE);
            fromCreatedTimeCriteria.setValue(fromTime);
            sharingFilters.add(fromCreatedTimeCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
            var toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
            var toCreatedTimeCriteria = new SearchCriteria();
            toCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            toCreatedTimeCriteria.setSearchCondition(SearchCondition.LTE);
            toCreatedTimeCriteria.setValue(toTime);
            sharingFilters.add(toCreatedTimeCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
            var projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
            var projectParentEntityCriteria = new SearchCriteria();
            projectParentEntityCriteria.setSearchField(EntitySearchField.PARRENT_ENTITY_ID);
            projectParentEntityCriteria.setSearchCondition(SearchCondition.EQUAL);
            projectParentEntityCriteria.setValue(projectId);
            sharingFilters.add(projectParentEntityCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
            var username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
            var usernameOwnerCriteria = new SearchCriteria();
            usernameOwnerCriteria.setSearchField(EntitySearchField.OWNER_ID);
            usernameOwnerCriteria.setSearchCondition(SearchCondition.EQUAL);
            usernameOwnerCriteria.setValue(username + "@" + gatewayId);
            sharingFilters.add(usernameOwnerCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
            var experimentName = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
            var experimentNameCriteria = new SearchCriteria();
            experimentNameCriteria.setSearchField(EntitySearchField.NAME);
            experimentNameCriteria.setSearchCondition(SearchCondition.LIKE);
            experimentNameCriteria.setValue(experimentName);
            sharingFilters.add(experimentNameCriteria);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
            var experimentDescription = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
            var experimentDescriptionCriteria = new SearchCriteria();
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
        sharingRegistryService
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
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving experiments");
        }
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) {
        try {
            return registryService.getExperimentStatus(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving experiment status");
        }
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) {
        try {
            return registryService.getExperimentOutputs(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving experiment outputs");
        }
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) {
        try {
            return registryService.getDetailedExperimentTree(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving detailed experiment tree");
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) {
        try {
            return registryService.getApplicationOutputs(appInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application outputs");
        }
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) {
        try {
            return registryService.getComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving compute resource");
        }
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription) {
        try {
            return registryService.registerComputeResource(computeResourceDescription);
        } catch (Throwable e) {
            throw convertException(e, "Error while saving compute resource");
        }
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription) {
        try {
            return registryService.updateComputeResource(computeResourceId, computeResourceDescription);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating compute resource");
        }
    }

    public boolean deleteComputeResource(String computeResourceId) {
        try {
            return registryService.deleteComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting compute resource");
        }
    }

    public Map<String, String> getAllComputeResourceNames() {
        try {
            return registryService.getAllComputeResourceNames();
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving compute resource names");
        }
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription) {
        try {
            return registryService.registerStorageResource(storageResourceDescription);
        } catch (Throwable e) {
            throw convertException(e, "Error while saving storage resource");
        }
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) {
        try {
            return registryService.getStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving storage resource");
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) {
        try {
            return registryService.updateStorageResource(storageResourceId, storageResourceDescription);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating storage resource");
        }
    }

    public boolean deleteStorageResource(String storageResourceId) {
        try {
            return registryService.deleteStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting storage resource");
        }
    }

    public Map<String, String> getAllStorageResourceNames() {
        try {
            return registryService.getAllStorageResourceNames();
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving storage resource names");
        }
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering gateway resource profile");
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) {
        try {
            return registryService.getGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving gateway resource profile");
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile) {
        try {
            return registryService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating gateway resource profile");
        }
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) {
        try {
            return registryService.deleteGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing gateway resource profile");
        }
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) {
        try {
            return registryService.getUserResourceProfile(userId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving user resource profile");
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile) {
        try {
            return registryService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating user resource profile");
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) {
        try {
            return registryService.deleteUserResourceProfile(userId, gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing user resource profile");
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        try {
            return registryService.getGroupResourceProfile(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group resource profile");
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        try {
            registryService.updateGroupResourceProfile(groupResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating group resource profile");
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds) {
        try {
            return registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group resource list");
        }
    }

    public GatewayGroups getGatewayGroups(String gatewayId) {
        try {
            return registryService.getGatewayGroups(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving gateway groups");
        }
    }

    public boolean isGatewayGroupsExists(String gatewayId) {
        try {
            return registryService.isGatewayGroupsExists(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while checking if gateway groups exist");
        }
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration) {
        try {
            registryService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating experiment configuration");
        }
    }

    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling) {
        try {
            registryService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating resource scheduling");
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) {
        try {
            return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering application deployment");
        }
    }

    public String registerApplicationDeploymentWithSharing(
            AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment) {
        try {
            String result = registerApplicationDeployment(gatewayId, applicationDeployment);
            if (ServerSettings.isEnableSharing()) {
                    var entity = new Entity();
                entity.setEntityId(result);
                final String domainId = gatewayId;
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                entity.setOwnerId(userName + "@" + domainId);
                entity.setName(result);
                entity.setDescription(applicationDeployment.getAppDeploymentDescription());
                createEntity(entity);
                shareEntityWithAdminGatewayGroups(entity);
            }
            return result;
        } catch (Throwable e) {
            throw convertException(e, "Error while registering application deployment");
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) {
        try {
            return registryService.getApplicationDeployment(appDeploymentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application deployment");
        }
    }

    public ApplicationDeploymentDescription getApplicationDeploymentWithAuth(AuthzToken authzToken, String appDeploymentId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = userHasAccessInternal(authzToken, appDeploymentId, ResourcePermissionType.READ);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have access to application deployment " + appDeploymentId);
                }
            }
            return getApplicationDeployment(appDeploymentId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application deployment");
        }
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) {
        try {
            return registryService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating application deployment");
        }
    }

    public boolean updateApplicationDeploymentWithAuth(
            AuthzToken authzToken, String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = userHasAccessInternal(authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while updating application deployment");
        }
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) {
        try {
            return registryService.deleteApplicationDeployment(appDeploymentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting application deployment");
        }
    }

    public boolean deleteApplicationDeploymentWithAuth(AuthzToken authzToken, String appDeploymentId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = userHasAccessInternal(authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return deleteApplicationDeployment(appDeploymentId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting application deployment");
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) {
        try {
            return registryService.getApplicationInterface(appInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application interface");
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) {
        try {
            return registryService.getApplicationDeployments(appModuleId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application deployments");
        }
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface) {
        try {
            return registryService.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding application interface");
        }
    }

    public String cloneApplicationInterface(String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws AiravataSystemException {
        try {
            var existingInterface = getApplicationInterface(existingAppInterfaceID);
            if (existingInterface == null) {
                logger.error(
                        "Provided application interface does not exist.Please provide a valid application interface id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }

            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            var interfaceId = registerApplicationInterface(gatewayId, existingInterface);
            logger.debug("Airavata cloned application interface : " + existingAppInterfaceID + " for gateway id : "
                    + gatewayId);
            return interfaceId;
        } catch (AiravataSystemException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Provided application interface does not exist.Please provide a valid application interface id...");
        }
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) {
        try {
            return registryService.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating application interface");
        }
    }

    public boolean deleteApplicationInterface(String appInterfaceId) {
        try {
            return registryService.deleteApplicationInterface(appInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting application interface");
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) {
        try {
            return registryService.getAllApplicationInterfaceNames(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application interfaces");
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) {
        try {
            return registryService.getAllApplicationInterfaces(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application interfaces");
        }
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getApplicationInputs(appInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application inputs");
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.registerApplicationModule(gatewayId, applicationModule);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding application module");
        }
    }

    public ApplicationModule getApplicationModule(String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application module");
        }
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateApplicationModule(appModuleId, applicationModule);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating application module");
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAllAppModules(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving all application modules");
        }
    }

    public boolean deleteApplicationModule(String appModuleId) {
        try {
            return registryService.deleteApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting application module");
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds) throws RegistryServiceException {
        return registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible app modules with sharing registry integration
     */
    public List<ApplicationModule> getAccessibleAppModulesWithSharing(
             AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var accessibleAppDeploymentIds = new ArrayList<String>();
            if (ServerSettings.isEnableSharing()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                var entityTypeFilter = new SearchCriteria();
                entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                entityTypeFilter.setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                sharingFilters.add(entityTypeFilter);
                var permissionTypeFilter = new SearchCriteria();
                permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
                permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
                permissionTypeFilter.setValue(gatewayId + ":" + ResourcePermissionType.READ);
                sharingFilters.add(permissionTypeFilter);
                sharingRegistryService
                        .searchEntities(
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                                userName + "@" + gatewayId,
                                sharingFilters,
                                0,
                                -1)
                        .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
            }
            var accessibleComputeResourceIds = new ArrayList<String>();
            List<GroupResourceProfile> groupResourceProfileList =
                    getGroupResourceListWithSharing( authzToken, gatewayId);
            for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
                List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                        groupResourceProfile.getComputePreferences();
                for (GroupComputeResourcePreference groupComputeResourcePreference : groupComputeResourcePreferenceList) {
                    accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
                }
            }
            return getAccessibleAppModules(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting accessible app modules");
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) {
        try {
            return registryService.getJobStatuses(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving job statuses");
        }
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) {
        try {
            return registryService.getJobDetails(airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving job details");
        }
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) {
        try {
            return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding local job submission interface");
        }
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) {
        try {
            return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding SSH job submission interface");
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) {
        try {
            return registryService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding SSH fork job submission interface");
        }
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission) {
        try {
            return registryService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding cloud job submission interface");
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission) {
        try {
            return registryService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding UNICORE job submission interface");
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission) {
        try {
            return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating local job submission interface");
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getLocalJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving local job submission interface");
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) {
        try {
            return registryService.getSSHJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving SSH job submission");
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) {
        try {
            return registryService.getCloudJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving cloud job submission");
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) {
        try {
            return registryService.getUnicoreJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving UNICORE job submission");
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating job submission interface");
        }
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating job submission interface");
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating job submission interface");
        }
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting job submission interface");
        }
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.registerResourceJobManager(resourceJobManager);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding resource job manager");
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating resource job manager");
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving resource job manager");
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting resource job manager");
        }
    }

    public String addPasswordCredential( PasswordCredential passwordCredential)
            throws CredentialStoreException, TException {
        return credentialStoreService.addPasswordCredential(passwordCredential);
    }

    public void deletePWDCredential( String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        credentialStoreService.deletePWDCredential(tokenId, gatewayId);
    }

    public boolean deleteSSHCredential( String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
    }

    public CredentialSummary getCredentialSummary(
             String tokenId, String gatewayId)
            throws CredentialStoreException, TException {
        return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
    }

    public List<CredentialSummary> getAllCredentialSummaries(
             SummaryType type, List<String> accessibleTokenIds, String gatewayId)
            throws CredentialStoreException, TException {
        return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding data movement interface to resource");
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating local data movement interface");
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getLocalDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving local data movement interface");
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding SCP data movement interface");
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating SCP data movement interface");
        }
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset) throws RegistryServiceException {
        return registryService.getUserProjects(gatewayId, userName, limit, offset);
    }

    /**
     * Get user projects with sharing registry integration
     */
    public List<Project> getUserProjectsWithSharing(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            int limit,
            int offset)
            throws TException {
        try {
            if (ServerSettings.isEnableSharing()) {
                // user projects + user accessible projects
                var accessibleProjectIds = new ArrayList<String>();
                var filters = new ArrayList<SearchCriteria>();
                var searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                filters.add(searchCriteria);
                sharingRegistryService
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
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving user projects");
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds) throws RegistryServiceException {
        return registryService.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible application deployments with sharing registry integration
     */
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsWithSharing(
            
            AuthzToken authzToken,
            String gatewayId,
            ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        List<String> accessibleAppDeploymentIds = new ArrayList<>();
        if (ServerSettings.isEnableSharing()) {
            List<SearchCriteria> sharingFilters = new ArrayList<>();
            var entityTypeFilter = new SearchCriteria();
            entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            entityTypeFilter.setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            sharingFilters.add(entityTypeFilter);
            var permissionTypeFilter = new SearchCriteria();
            permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            permissionTypeFilter.setValue(gatewayId + ":" + permissionType.name());
            sharingFilters.add(permissionTypeFilter);
            sharingRegistryService
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            sharingFilters,
                            0,
                            -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
        }
        var accessibleComputeResourceIds = new ArrayList<String>();
        List<GroupResourceProfile> groupResourceProfileList =
                getGroupResourceListWithSharing( authzToken, gatewayId);
        for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
            List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                    groupResourceProfile.getComputePreferences();
            for (GroupComputeResourcePreference groupComputeResourcePreference : groupComputeResourcePreferenceList) {
                accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
            }
        }
        return getAccessibleApplicationDeployments(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting accessible application deployments");
        }
    }

    public List<String> getAppModuleDeployedResources(String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAppModuleDeployedResources(appModuleId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application deployment");
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String appModuleId,
            String gatewayId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            return registryService.getAccessibleApplicationDeploymentsForAppModule(
                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving accessible application deployments");
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
            AuthzToken authzToken, String appModuleId, String groupResourceProfileId)
            throws AuthorizationException {
        try {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            
            // Get list of compute resources for this Group Resource Profile
            if (!userHasAccessInternal(authzToken, groupResourceProfileId, ResourcePermissionType.READ)) {
                throw new AuthorizationException(
                        "User is not authorized to access Group Resource Profile " + groupResourceProfileId);
            }
            var groupResourceProfile = getGroupResourceProfile(groupResourceProfileId);
            var accessibleComputeResourceIds = groupResourceProfile.getComputePreferences().stream()
                    .map(compPref -> compPref.getComputeResourceId())
                    .collect(Collectors.toList());

            // Get list of accessible Application Deployments
            var accessibleAppDeploymentIds = new ArrayList<String>();
            var sharingFilters = new ArrayList<SearchCriteria>();
            var entityTypeFilter = new SearchCriteria();
            entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            entityTypeFilter.setValue(gatewayId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            sharingFilters.add(entityTypeFilter);
            var permissionTypeFilter = new SearchCriteria();
            permissionTypeFilter.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            permissionTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            permissionTypeFilter.setValue(gatewayId + ":" + ResourcePermissionType.READ);
            sharingFilters.add(permissionTypeFilter);
            searchEntities(gatewayId, userName + "@" + gatewayId, sharingFilters, 0, -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));

            return getAccessibleApplicationDeploymentsForAppModule(
                    appModuleId, gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving application deployments");
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) {
        try {
            return registryService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving available compute resources");
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getSCPDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving SCP data movement interface");
        }
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding UNICORE data movement interface");
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating unicore data movement interface");
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getUnicoreDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving UNICORE data movement interface");
        }
    }

    public String addGridFTPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addGridFTPDataMovementDetails(resourceId, dmType, priorityOrder, gridFTPDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while adding GridFTP data movement interface");
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating GridFTP data movement interface");
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getGridFTPDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving GridFTP data movement interface");
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting data movement interface");
        }
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteBatchQueue(computeResourceId, queueName);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting batch queue");
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering gateway resource profile preference");
        }
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering gateway storage preference");
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving gateway compute resource preference");
        }
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving gateway storage preference");
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAllGatewayComputeResourcePreferences(gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving all gateway compute resource preferences");
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) {
        try {
            return registryService.getAllGatewayStoragePreferences(gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving all gateway storage preferences");
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() {
        try {
            return registryService.getAllGatewayResourceProfiles();
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving all gateway resource profiles");
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating gateway compute resource preference");
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating gateway data storage preference");
        }
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (Throwable e) {
            logger.error(gatewayID, "Error while deleting gateway compute resource preference...", e);
            throw convertException(e, "Error while deleting gateway compute resource preference");
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting gateway data storage preference");
        }
    }

    public String registerUserResourceProfile(UserResourceProfile userResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.registerUserResourceProfile(userResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering user resource profile");
        }
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.isUserResourceProfileExists(userId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while checking existence of user resource profile");
        }
    }

    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (Throwable e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            throw convertException(e, "Error while registering user resource profile preference");
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String userStorageResourceId, UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.addUserStoragePreference(
                    userId, gatewayID, userStorageResourceId, dataStoragePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while registering user storage preference");
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (Throwable e) {
            throw convertException(e, "Error while reading user compute resource preference");
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (Throwable e) {
            throw convertException(e, "Error while reading user data storage preference");
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while reading User compute resource preferences");
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAllUserStoragePreferences(userId, gatewayID);
        } catch (Throwable e) {
            throw convertException(e, "Error while reading User data storage preferences");
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles()
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.getAllUserResourceProfiles();
        } catch (Throwable e) {
            throw convertException(e, "Error while reading retrieving all user resource profiles");
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating user compute resource preference");
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String userStorageId, UserStoragePreference userStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.updateUserStoragePreference(userId, gatewayID, userStorageId, userStoragePreference);
        } catch (Throwable e) {
            throw convertException(e, "Error while updating user data storage preference");
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (Throwable e) {
            logger.error(userId, "Error while deleting user compute resource preference...", e);
            throw convertException(e, "Error while deleting user compute resource preference");
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return registryService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting user data storage preference");
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses() {
        try {
            return registryService.getLatestQueueStatuses();
        } catch (Throwable e) {
            var msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        try {
            return registryService.createGroupResourceProfile(groupResourceProfile);
        } catch (Throwable e) {
            throw convertException(e, "Error while creating group resource profile");
        }
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) {
        try {
            return registryService.removeGroupResourceProfile(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing group resource profile");
        }
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) {
        try {
            return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing group compute preferences");
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        try {
            return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group compute resource preference");
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) {
        try {
            return registryService.getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group compute resource policy");
        }
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) {
        try {
            return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing group compute resource policy");
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        try {
            return registryService.getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving batch queue resource policy");
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) {
        try {
            return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing group batch queue resource policy");
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId) {
        try {
            return registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group compute resource preference list");
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId) {
        try {
            return registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group batch queue resource policy list");
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId) {
        try {
            return registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving group compute resource policy list");
        }
    }

    public Parser getParser(String parserId, String gatewayId) {
        try {
            return registryService.getParser(parserId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving parser");
        }
    }

    public String saveParser(Parser parser) {
        try {
            return registryService.saveParser(parser);
        } catch (Throwable e) {
            throw convertException(e, "Error while saving parser");
        }
    }

    public List<Parser> listAllParsers(String gatewayId) {
        try {
            return registryService.listAllParsers(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while listing all parsers");
        }
    }

    public void removeParser(String parserId, String gatewayId) {
        try {
            registryService.removeParser(parserId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing parser");
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) {
        try {
            return registryService.getParsingTemplate(templateId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving parsing template");
        }
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId) {
        try {
            return registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving parsing templates for experiment");
        }
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) {
        try {
            return registryService.saveParsingTemplate(parsingTemplate);
        } catch (Throwable e) {
            throw convertException(e, "Error while saving parsing template");
        }
    }

    public void removeParsingTemplate(String templateId, String gatewayId) {
        try {
            registryService.removeParsingTemplate(templateId, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while removing parsing template");
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) {
        try {
            return registryService.listAllParsingTemplates(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while listing all parsing templates");
        }
    }

    // Helper methods for sharing registry and authorization
    public GatewayGroups retrieveGatewayGroups(String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (isGatewayGroupsExists(gatewayId)) {
                return getGatewayGroups(gatewayId);
            } else {
                return GatewayGroupsInitializer.initializeGatewayGroups(gatewayId);
            }
        } catch (Throwable e) {
            throw convertException(e, "Error retrieving gateway groups");
        }
    }

    public void createManageSharingPermissionTypeIfMissing( String domainId)
            throws TException {
        // AIRAVATA-3297 Some gateways were created without the MANAGE_SHARING permission, so add it if missing
        var permissionTypeId = domainId + ":MANAGE_SHARING";
        try {
            if (!sharingRegistryService.isPermissionExists(domainId, permissionTypeId)) {
                var permissionType = new PermissionType();
                permissionType.setPermissionTypeId(permissionTypeId);
                permissionType.setDomainId(domainId);
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Manage sharing permission type");
                sharingRegistryService.createPermissionType(permissionType);
                logger.info("Created MANAGE_SHARING permission type for domain " + domainId);
            }
        } catch (Exception e) {
            throw new TException("Error creating MANAGE_SHARING permission type", e);
        }
    }

    public void shareEntityWithAdminGatewayGroups( Entity entity)
            throws TException {
        final String domainId = entity.getDomainId();
        GatewayGroups gatewayGroups = retrieveGatewayGroups(domainId);
        createManageSharingPermissionTypeIfMissing( domainId);
        sharingRegistryService.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()),
                domainId + ":MANAGE_SHARING",
                true);
        sharingRegistryService.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()),
                domainId + ":WRITE",
                true);
        sharingRegistryService.shareEntityWithGroups(
                domainId,
                entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                domainId + ":READ",
                true);
    }

    public boolean userHasAccessInternal(
            
            AuthzToken authzToken,
            String entityId,
            ResourcePermissionType permissionType) {
        final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        final String userId = authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + domainId;
        try {
            final boolean hasOwnerAccess = sharingRegistryService.userHasAccess(
                    domainId, userId, entityId, domainId + ":" + ResourcePermissionType.OWNER);
            boolean hasAccess = false;
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                hasAccess = hasOwnerAccess
                        || sharingRegistryService.userHasAccess(
                                domainId, userId, entityId, domainId + ":" + ResourcePermissionType.WRITE);
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                hasAccess = hasOwnerAccess
                        || sharingRegistryService.userHasAccess(
                                domainId, userId, entityId, domainId + ":" + ResourcePermissionType.READ);
            } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
                hasAccess = hasOwnerAccess
                        || sharingRegistryService.userHasAccess(
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
            String gatewayId,
            String userName,
            String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        try {
            var sshCredential = new SSHCredential();
            sshCredential.setUsername(userName);
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setDescription(description);
            var key = credentialStoreService.addSSHCredential(sshCredential);

            try {
                    var entity = new Entity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingRegistryService.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error(
                        "Rolling back ssh key creation for user " + userName + " and description [" + description + "]");
                credentialStoreService.deleteSSHCredential(key, gatewayId);
                throw convertException(ex, "Failed to create sharing registry record");
            }
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            return key;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while registering SSH Credential");
        }
    }

    public String registerPwdCredential(
            
            
            String gatewayId,
            String userName,
            String loginUserName,
            String password,
            String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        try {
            var pwdCredential = new PasswordCredential();
            pwdCredential.setPortalUserName(userName);
            pwdCredential.setLoginUserName(loginUserName);
            pwdCredential.setPassword(password);
            pwdCredential.setDescription(description);
            pwdCredential.setGatewayId(gatewayId);
            var key = addPasswordCredential( pwdCredential);
            try {
                        var entity = new Entity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingRegistryService.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back password registration for user " + userName + " and description [" + description
                        + "]");
                try {
                    deletePWDCredential( key, gatewayId);
                } catch (Exception rollbackEx) {
                    logger.error("Failed to rollback password credential deletion", rollbackEx);
                }
                throw convertException(ex, "Failed to create sharing registry record");
            }
            logger.debug(
                    "Airavata generated PWD credential for gateway : " + gatewayId + " and for user : " + loginUserName);
            return key;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while registering password credential");
        }
    }

    public CredentialSummary getCredentialSummaryWithAuth(
            
            
            AuthzToken authzToken,
            String tokenId,
            String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccessInternal( authzToken, tokenId, ResourcePermissionType.READ)) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
            var credentialSummary = getCredentialSummary( tokenId, gatewayId);
            logger.debug("Airavata retrived the credential summary for token " + tokenId + "GatewayId: " + gatewayId);
            return credentialSummary;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting credential summary");
        }
    }

    public List<CredentialSummary> getAllCredentialSummariesWithAuth(
            
            
            AuthzToken authzToken,
            SummaryType type,
            String gatewayId,
            String userName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        try {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN.name());
            filters.add(searchCriteria);
            List<String> accessibleTokenIds =
                    sharingRegistryService.searchEntities(gatewayId, userName + "@" + gatewayId, filters, 0, -1).stream()
                            .map(p -> p.getEntityId())
                            .collect(Collectors.toList());
            List<CredentialSummary> credentialSummaries =
                    getAllCredentialSummaries( type, accessibleTokenIds, gatewayId);
            logger.debug(
                    "Airavata successfully retrived credential summaries of type " + type + " GatewayId: " + gatewayId);
            return credentialSummaries;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting all credential summaries");
        }
    }

    public boolean deleteSSHPubKey(
            
            
            AuthzToken authzToken,
            String airavataCredStoreToken,
            String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccessInternal( authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : "
                    + airavataCredStoreToken);
            return deleteSSHCredential( airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while deleting SSH pub key");
        }
    }

    public boolean deletePWDCredentialWithAuth(
            
            
            AuthzToken authzToken,
            String airavataCredStoreToken,
            String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccessInternal( authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Airavata deleted PWD credential for gateway Id : " + gatewayId + " and with token id : "
                    + airavataCredStoreToken);
            deletePWDCredential( airavataCredStoreToken, gatewayId);
            return true;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while deleting PWD credential");
        }
    }

    // Project management methods with sharing registry integration
    public String createProjectWithSharing(
             String gatewayId, Project project) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            var projectId = createProject(gatewayId, project);
            // TODO: verify that gatewayId and project.gatewayId match authzToken
            if (ServerSettings.isEnableSharing()) {
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
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    logger.error("Rolling back project creation Proj ID : " + projectId);
                    deleteProject(projectId);
                    throw convertException(ex, "Failed to create entry for project in Sharing Registry");
                }
            }
            logger.debug("Airavata created project with project Id : " + projectId + " for gateway Id : " + gatewayId);
            return projectId;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while creating project");
        }
    }

    public void updateProjectWithAuth(
            
            AuthzToken authzToken,
            String projectId,
            Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var existingProject = getProject(projectId);
            if (ServerSettings.isEnableSharing()
                            && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            if (!updatedProject.getOwner().equals(existingProject.getOwner())) {
                throw new InvalidRequestException("Owner of a project cannot be changed");
            }
            if (!updatedProject.getGatewayId().equals(existingProject.getGatewayId())) {
                throw new InvalidRequestException("Gateway ID of a project cannot be changed");
            }
            updateProject(projectId, updatedProject);
            logger.debug("Airavata updated project with project Id : " + projectId);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while updating project");
        }
    }

    public boolean deleteProjectWithAuth(
             AuthzToken authzToken, String projectId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var existingProject = getProject(projectId);
            if (ServerSettings.isEnableSharing()
                            && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            boolean ret = deleteProject(projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId);
            return ret;
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while deleting project");
        }
    }

    public Project getProjectWithAuth(
             AuthzToken authzToken, String projectId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var project = getProject(projectId);
            if (authzToken.getClaimsMap().get(Constants.USER_NAME).equals(project.getOwner())
                    && authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(project.getGatewayId())) {
                return project;
            } else if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
                return project;
            } else {
                return null;
            }
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting project");
        }
    }

    // Experiment management methods with sharing registry integration
    public String createExperimentWithSharing(
             String gatewayId, ExperimentModel experiment)
            throws Exception {
        var experimentId = createExperiment(gatewayId, experiment);

        if (ServerSettings.isEnableSharing()) {
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
                shareEntityWithAdminGatewayGroups( entity);
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
            
            Publisher statusPublisher,
            String gatewayId,
            ExperimentModel experiment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            logger.info("Api server accepted experiment creation with name {}", experiment.getExperimentName());
            var experimentId = createExperimentWithSharing( gatewayId, experiment);

            if (statusPublisher != null) {
                var event =
                        new ExperimentStatusChangeEvent(ExperimentState.CREATED, experimentId, gatewayId);
                var messageId = AiravataUtils.getId("EXPERIMENT");
                var messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
                messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                statusPublisher.publish(messageContext);
            }

            return experimentId;
        } catch (Throwable e) {
            throw convertException(e, "Error while creating the experiment");
        }
    }

    public void validateLaunchExperimentAccess(
            
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
        if (!sharingRegistryService.userHasAccess(
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
        var applicationInterfaceDescription = getApplicationInterface(appInterfaceId);

        List<String> appModuleIds = applicationInterfaceDescription.getApplicationModules();
        // Assume that there is only one app module for this interface
        var appModuleId = appModuleIds.get(0);
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
                if (!sharingRegistryService.userHasAccess(
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
                    if (!sharingRegistryService.userHasAccess(
                            gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                        throw new Exception("User " + username + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                }
            }
        }
    }

    public boolean deleteExperimentWithAuth(
             AuthzToken authzToken, String experimentId) throws AuthorizationException, InvalidRequestException {
        try {
            var experimentModel = getExperiment(experimentId);

            if (ServerSettings.isEnableSharing()
                            && !authzToken.getClaimsMap().get(Constants.USER_NAME).equals(experimentModel.getUserName())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(gatewayId, userId + "@" + gatewayId, experimentId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            if (!(experimentModel.getExperimentStatus().get(0).getState()
                    == org.apache.airavata.model.status.ExperimentState.CREATED)) {
                throw new InvalidRequestException("Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
            }
            return deleteExperiment(experimentId);
        } catch (AuthorizationException | InvalidRequestException e) {
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting experiment");
        }
    }

    public ResourceType getResourceType( String domainId, String entityId) {
        try {
            var entity = sharingRegistryService.getEntity(domainId, entityId);
            for (ResourceType resourceType : ResourceType.values()) {
                if (entity.getEntityTypeId().equals(domainId + ":" + resourceType.name())) {
                    return resourceType;
                }
            }
            throw new RuntimeException("Unrecognized entity type id: " + entity.getEntityTypeId());
        } catch (Throwable e) {
            throw convertException(e, "Error while getting resource type");
        }
    }

    // Gateway management methods with sharing registry integration
    public String addGatewayWithSharing(Gateway gateway) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            var gatewayId = registryService.addGateway(gateway);
            Domain domain = new Domain();
            domain.setDomainId(gateway.getGatewayId());
            domain.setName(gateway.getGatewayName());
            domain.setDescription("Domain entry for " + domain.getName());
            sharingRegistryService.createDomain(domain);

            // Creating Entity Types for each domain
            EntityType entityType = new EntityType();
            entityType.setEntityTypeId(domain.getDomainId() + ":PROJECT");
            entityType.setDomainId(domain.getDomainId());
            entityType.setName("PROJECT");
            entityType.setDescription("Project entity type");
            sharingRegistryService.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.getDomainId() + ":EXPERIMENT");
            entityType.setDomainId(domain.getDomainId());
            entityType.setName("EXPERIMENT");
            entityType.setDescription("Experiment entity type");
            sharingRegistryService.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.getDomainId() + ":FILE");
            entityType.setDomainId(domain.getDomainId());
            entityType.setName("FILE");
            entityType.setDescription("File entity type");
            sharingRegistryService.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            entityType.setDomainId(domain.getDomainId());
            entityType.setName("APPLICATION-DEPLOYMENT");
            entityType.setDescription("Application Deployment entity type");
            sharingRegistryService.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
            entityType.setDomainId(domain.getDomainId());
            entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
            entityType.setDescription("Group Resource Profile entity type");
            sharingRegistryService.createEntityType(entityType);

            // Creating Permission Types for each domain
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":READ");
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName("READ");
            permissionType.setDescription("Read permission type");
            sharingRegistryService.createPermissionType(permissionType);

            permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":WRITE");
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName("WRITE");
            permissionType.setDescription("Write permission type");
            sharingRegistryService.createPermissionType(permissionType);

            permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":MANAGE_SHARING");
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName("MANAGE_SHARING");
            permissionType.setDescription("Sharing permission type");
            sharingRegistryService.createPermissionType(permissionType);

            logger.debug("Airavata successfully created the gateway with " + gatewayId);
            return gatewayId;
        } catch (Throwable e) {
            throw convertException(e, "Error while adding gateway");
        }
    }

    // Event publishing methods
    public void publishExperimentSubmitEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws Exception {
        var event = new ExperimentSubmitEvent(experimentId, gatewayId);
        var messageContext = new MessageContext(
                event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    public void publishExperimentCancelEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws Exception {
        var event = new ExperimentSubmitEvent(experimentId, gatewayId);
        var messageContext = new MessageContext(
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
        var event =
                new ExperimentIntermediateOutputsEvent(experimentId, gatewayId, outputNames);
        var messageContext = new MessageContext(
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
            
            AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames,
            Publisher experimentPublisher)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            // Verify that user has WRITE access to experiment
            final boolean hasAccess =
                    userHasAccessInternal( authzToken, airavataExperimentId, ResourcePermissionType.WRITE);
            if (!hasAccess) {
                var msg = "User does not have WRITE access to this experiment";
                logger.error(msg);
                throw new AuthorizationException(msg);
            }

            // Verify that the experiment's job is currently ACTIVE
            ExperimentModel existingExperiment = getExperiment(airavataExperimentId);
            var jobs = getJobDetails(airavataExperimentId);
            boolean anyJobIsActive = jobs.stream().anyMatch(j -> {
                if (j.getJobStatusesSize() > 0) {
                    return j.getJobStatuses().get(j.getJobStatusesSize() - 1).getJobState() == JobState.ACTIVE;
                } else {
                    return false;
                }
            });
            if (!anyJobIsActive) {
                var msg = "Experiment does not have currently ACTIVE job";
                logger.error(msg);
                throw new InvalidRequestException(msg);
            }

            // Figure out if there are any currently running intermediate output fetching processes for outputNames
            // First, find any existing intermediate output fetch processes for outputNames
            var intermediateOutputFetchProcesses = existingExperiment.getProcesses().stream()
                    .filter(p -> {
                        // Filter out completed or failed processes
                        if (p.getProcessStatusesSize() > 0) {
                            var latestStatus = p.getProcessStatuses().get(p.getProcessStatusesSize() - 1);
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
                var msg = "There are already intermediate output fetching tasks running for those outputs.";
                logger.error(msg);
                throw new InvalidRequestException(msg);
            }

            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            publishExperimentIntermediateOutputsEvent(experimentPublisher, gatewayId, airavataExperimentId, outputNames);
        } catch (AuthorizationException | InvalidRequestException e) {
            throw e;
        } catch (Throwable e) {
            logger.error(
                    "Error while processing request to fetch intermediate outputs for experiment: "
                            + airavataExperimentId,
                    e);
            throw convertException(e, "Error occurred");
        }
    }

    /**
     * Get intermediate output process status - finds the most recent matching process and returns its status
     */
    public ProcessStatus getIntermediateOutputProcessStatusInternal(
            
            AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            // Verify that user has READ access to experiment
            final boolean hasAccess =
                    userHasAccessInternal( authzToken, airavataExperimentId, ResourcePermissionType.READ);
            if (!hasAccess) {
                var msg = "User does not have READ access to this experiment";
                logger.debug(msg);
                throw new AuthorizationException(msg);
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
                var msg = "No matching intermediate output fetching process found.";
                logger.debug(msg);
                throw new InvalidRequestException(msg);
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
        } catch (AuthorizationException | InvalidRequestException e) {
            throw e;
        } catch (Throwable e) {
            logger.error(
                    "Error while processing request to get intermediate output process status for experiment: "
                            + airavataExperimentId,
                    e);
            throw convertException(e, "Error occurred");
        }
    }

    // Access control methods
    public List<String> getAllAccessibleUsers(
            
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryService, ResourcePermissionType, Collection<User>> userListFunction)
            throws Exception {
        HashSet<String> accessibleUsers = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.WRITE).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.READ).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.MANAGE_SHARING).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction.apply(sharingRegistryService, ResourcePermissionType.OWNER).stream()
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        }
        return new ArrayList<>(accessibleUsers);
    }

    public List<String> getAllAccessibleGroups(
            
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryService, ResourcePermissionType, Collection<UserGroup>> groupListFunction)
            throws Exception {
        HashSet<String> accessibleGroups = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            groupListFunction.apply(sharingRegistryService, ResourcePermissionType.WRITE).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            groupListFunction.apply(sharingRegistryService, ResourcePermissionType.READ).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            groupListFunction.apply(sharingRegistryService, ResourcePermissionType.MANAGE_SHARING).stream()
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        }
        return new ArrayList<>(accessibleGroups);
    }

    /**
     * Get all accessible users for a resource (includes shared and directly shared)
     */
    public List<String> getAllAccessibleUsersWithSharing(
            
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            boolean directlySharedOnly)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        BiFunction<SharingRegistryService, ResourcePermissionType, Collection<User>> userListFunction;
        if (directlySharedOnly) {
            userListFunction = (c, t) -> {
                try {
                    return c.getListOfDirectlySharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            userListFunction = (c, t) -> {
                try {
                    return c.getListOfSharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return getAllAccessibleUsers( gatewayId, resourceId, permissionType, userListFunction);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting all accessible users");
        }
    }

    /**
     * Get all accessible groups for a resource (includes shared and directly shared)
     */
    public List<String> getAllAccessibleGroupsWithSharing(
            
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            boolean directlySharedOnly)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        BiFunction<SharingRegistryService, ResourcePermissionType, Collection<UserGroup>> groupListFunction;
        if (directlySharedOnly) {
            groupListFunction = (c, t) -> {
                try {
                    return c.getListOfDirectlySharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            groupListFunction = (c, t) -> {
                try {
                    return c.getListOfSharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return getAllAccessibleGroups( gatewayId, resourceId, permissionType, groupListFunction);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting all accessible groups");
        }
    }

    // Group resource profile management with sharing registry integration
    public String createGroupResourceProfileWithSharing(
            
            AuthzToken authzToken,
            GroupResourceProfile groupResourceProfile)
            throws Exception {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        validateGroupResourceProfile( authzToken, groupResourceProfile);
        String groupResourceProfileId = createGroupResourceProfile(groupResourceProfile);
        if (ServerSettings.isEnableSharing()) {
            try {
                    var entity = new Entity();
                entity.setEntityId(groupResourceProfileId);
                entity.setDomainId(groupResourceProfile.getGatewayId());
                entity.setEntityTypeId(groupResourceProfile.getGatewayId() + ":" + "GROUP_RESOURCE_PROFILE");
                entity.setOwnerId(userName + "@" + groupResourceProfile.getGatewayId());
                entity.setName(groupResourceProfile.getGroupResourceProfileName());

                sharingRegistryService.createEntity(entity);
                shareEntityWithAdminGatewayGroups( entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back group resource profile creation Group Resource Profile ID : "
                        + groupResourceProfileId);
                try {
                    removeGroupResourceProfile(groupResourceProfileId);
                } catch (Throwable rollbackEx) {
                    logger.error("Failed to rollback group resource profile deletion", rollbackEx);
                }
                throw new Exception("Failed to create sharing registry record", ex);
            }
        }
        return groupResourceProfileId;
    }

    public void validateGroupResourceProfile(
            
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
            if (!userHasAccessInternal( authzToken, tokenId, ResourcePermissionType.READ)) {
                throw new Exception("User does not have READ permission to credential token " + tokenId + ".");
            }
        }
    }

    // Launch experiment business logic
    public void launchExperimentWithValidation(
            AuthzToken authzToken,
            String gatewayId,
            String airavataExperimentId,
            Publisher experimentPublisher)
            throws TException {
        try {
            logger.info("Launching experiment {}", airavataExperimentId);
            ExperimentModel experiment = getExperiment(airavataExperimentId);

            if (experiment == null) {
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

            // For backwards compatibility, if there is no groupResourceProfileId, look up one that is shared with the user
            if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
                List<GroupResourceProfile> groupResourceProfiles =
                        getGroupResourceListWithSharing( authzToken, gatewayId);
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
            validateLaunchExperimentAccess( authzToken, gatewayId, experiment);
            publishExperimentSubmitEvent(experimentPublisher, gatewayId, airavataExperimentId);
        } catch (Throwable e) {
            throw convertException(e, "Error launching experiment");
        }
    }

    /**
     * Get group resource list with sharing registry integration
     */
    public List<GroupResourceProfile> getGroupResourceListWithSharing(
             AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var accessibleGroupResProfileIds = new ArrayList<String>();
            if (ServerSettings.isEnableSharing()) {
                var filters = new ArrayList<SearchCriteria>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                filters.add(searchCriteria);
                sharingRegistryService
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
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while getting group resource list");
        }
    }

    // Sharing Registry Delegation Methods for ServerHandler
    public boolean isDomainExists(String domainId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.isDomainExists(domainId);
    }

    public String createDomain(Domain domain) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        return sharingRegistryService.createDomain(domain);
    }

    public String createUser(User user) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        return sharingRegistryService.createUser(user);
    }

    public String createGroup(UserGroup group) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        return sharingRegistryService.createGroup(group);
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.addUsersToGroup(domainId, userIds, groupId);
    }

    public String createEntityType(org.apache.airavata.sharing.registry.models.EntityType entityType) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        return sharingRegistryService.createEntityType(entityType);
    }

    public String createPermissionType(org.apache.airavata.sharing.registry.models.PermissionType permissionType) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        return sharingRegistryService.createPermissionType(permissionType);
    }

    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.userHasAccess(domainId, userId, entityId, permissionTypeId);
    }

    public org.apache.airavata.sharing.registry.models.Entity getEntity(String domainId, String entityId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.getEntity(domainId, entityId);
    }

    public void updateEntity(org.apache.airavata.sharing.registry.models.Entity entity) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        sharingRegistryService.updateEntity(entity);
    }

    public boolean shareEntityWithUsers(String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascade) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.shareEntityWithUsers(domainId, entityId, userList, permissionTypeId, cascade);
    }

    public SSHCredential getSSHCredential(String token, String gatewayId) {
        try {
            return credentialStoreService.getSSHCredential(token, gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving SSH credential");
        }
    }

    public void createEntity(org.apache.airavata.sharing.registry.models.Entity entity) throws org.apache.airavata.sharing.registry.models.SharingRegistryException, org.apache.airavata.sharing.registry.models.DuplicateEntryException {
        sharingRegistryService.createEntity(entity);
    }

    public java.util.List<org.apache.airavata.sharing.registry.models.Entity> searchEntities(String domainId, String userId, java.util.List<org.apache.airavata.sharing.registry.models.SearchCriteria> filters, int offset, int limit) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.searchEntities(domainId, userId, filters, offset, limit);
    }

    public boolean shareEntityWithGroups(String domainId, String entityId, List<String> groupList, String permissionTypeId, boolean cascade) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.shareEntityWithGroups(domainId, entityId, groupList, permissionTypeId, cascade);
    }

    public boolean revokeEntitySharingFromUsers(String domainId, String entityId, List<String> userList, String permissionTypeId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.revokeEntitySharingFromUsers(domainId, entityId, userList, permissionTypeId);
    }

    public boolean revokeEntitySharingFromGroups(String domainId, String entityId, List<String> groupList, String permissionTypeId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.revokeEntitySharingFromGroups(domainId, entityId, groupList, permissionTypeId);
    }

    public boolean deleteEntity(String domainId, String entityId) throws org.apache.airavata.sharing.registry.models.SharingRegistryException {
        return sharingRegistryService.deleteEntity(domainId, entityId);
    }

    /**
     * Resolves compute resource storage info context (login username, credential token, and adaptor).
     * Handles user preference → group preference fallback for both login and credentials.
     */
    private StorageInfoContext resolveComputeStorageInfoContext(
            AuthzToken authzToken, String gatewayId, String userId, String resourceId)
            throws Exception {
        String loginUserName = null;
        boolean loginFromUserPref = false;
        GroupComputeResourcePreference groupComputePref = null;
        GroupResourceProfile groupResourceProfile = null;

        UserComputeResourcePreference userComputePref = null;
        if (safeIsUserResourceProfileExists(authzToken, userId, gatewayId)) {
            userComputePref = getUserComputeResourcePreference(userId, gatewayId, resourceId);
        } else {
            logger.debug(
                    "User resource profile does not exist for user {} in gateway {}, will try group preferences",
                    userId,
                    gatewayId);
        }

        if (userComputePref != null
                && userComputePref.getLoginUserName() != null
                && !userComputePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = userComputePref.getLoginUserName();
            loginFromUserPref = true;
            logger.debug("Using user preference login username: {}", loginUserName);

        } else {
            // Fallback to GroupComputeResourcePreference
            var groupResourceProfiles = getGroupResourceListWithSharing(authzToken, gatewayId);
            for (GroupResourceProfile groupProfile : groupResourceProfiles) {
                List<GroupComputeResourcePreference> groupComputePrefs = groupProfile.getComputePreferences();

                if (groupComputePrefs != null && !groupComputePrefs.isEmpty()) {
                    for (GroupComputeResourcePreference groupPref : groupComputePrefs) {
                        if (resourceId.equals(groupPref.getComputeResourceId())
                                && groupPref.getLoginUserName() != null
                                && !groupPref.getLoginUserName().trim().isEmpty()) {
                            loginUserName = groupPref.getLoginUserName();
                            groupComputePref = groupPref;
                            groupResourceProfile = groupProfile;
                            logger.debug(
                                    "Using login username from group compute resource preference for resource {}",
                                    resourceId);
                            break;
                        }
                    }
                }
                if (loginUserName != null) {
                    break;
                }
            }
            if (loginUserName == null) {
                logger.debug("No login username found for compute resource {}", resourceId);
                throw new InvalidRequestException("No login username found for compute resource " + resourceId);
            }
        }

        // Resolve credential token based on where login came from
        String credentialToken;
        if (loginFromUserPref) {
            // Login username came from user preference. Use user preference token → user profile token
            if (userComputePref != null
                    && userComputePref.getResourceSpecificCredentialStoreToken() != null
                    && !userComputePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = userComputePref.getResourceSpecificCredentialStoreToken();
            } else {
                UserResourceProfile userResourceProfile = getUserResourceProfile(userId, gatewayId);
                if (userResourceProfile == null
                        || userResourceProfile.getCredentialStoreToken() == null
                        || userResourceProfile.getCredentialStoreToken().trim().isEmpty()) {
                    logger.error("No credential store token found for user {} in gateway {}", userId, gatewayId);
                    throw clientException(
                            AiravataErrorType.AUTHENTICATION_FAILURE,
                            "No credential store token found for user " + userId + " in gateway " + gatewayId);
                }
                credentialToken = userResourceProfile.getCredentialStoreToken();
            }
        } else {
            // Login username came from group preference. Use group preference token → group profile default token →
            // user profile token (fallback)
            if (groupComputePref != null
                    && groupComputePref.getResourceSpecificCredentialStoreToken() != null
                    && !groupComputePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = groupComputePref.getResourceSpecificCredentialStoreToken();

            } else if (groupResourceProfile != null
                    && groupResourceProfile.getDefaultCredentialStoreToken() != null
                    && !groupResourceProfile
                            .getDefaultCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = groupResourceProfile.getDefaultCredentialStoreToken();

            } else {
                UserResourceProfile userResourceProfile = getUserResourceProfile(userId, gatewayId);
                if (userResourceProfile == null
                        || userResourceProfile.getCredentialStoreToken() == null
                        || userResourceProfile.getCredentialStoreToken().trim().isEmpty()) {
                    logger.error("No credential store token found for user {} in gateway {}", userId, gatewayId);
                    throw clientException(
                            AiravataErrorType.AUTHENTICATION_FAILURE,
                            "No credential store token found for compute resource " + resourceId);
                }
                credentialToken = userResourceProfile.getCredentialStoreToken();
            }
        }

        AgentAdaptor adaptor = AdaptorSupportImpl.getInstance()
                .fetchComputeSSHAdaptor(gatewayId, resourceId, credentialToken, userId, loginUserName);
        logger.info("Resolved resource {} as compute resource to fetch storage details", resourceId);

        return new StorageInfoContext(loginUserName, credentialToken, adaptor);
    }

    /**
     * Resolves storage resource storage info context (login username, credential token, and adaptor).
     * Handles user preference → gateway preference fallback for both login and credentials.
     */
    private StorageInfoContext resolveStorageStorageInfoContext(
            AuthzToken authzToken, String gatewayId, String userId, String resourceId)
            throws Exception {
        UserStoragePreference userStoragePref = null;
        if (safeIsUserResourceProfileExists(authzToken, userId, gatewayId)) {
            userStoragePref = getUserStoragePreference(userId, gatewayId, resourceId);
        } else {
            logger.debug(
                    "User resource profile does not exist for user {} in gateway {}, will try gateway preferences",
                    userId,
                    gatewayId);
        }

        StoragePreference storagePref = null;
        if (isGatewayResourceProfileExists(gatewayId)) {
            storagePref = getGatewayStoragePreference(gatewayId, resourceId);
        } else {
            logger.debug(
                    "Gateway resource profile does not exist for gateway {}, will check if user preference exists",
                    gatewayId);
        }

        String loginUserName;
        boolean loginFromUserPref;

        if (userStoragePref != null
                && userStoragePref.getLoginUserName() != null
                && !userStoragePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = userStoragePref.getLoginUserName();
            loginFromUserPref = true;
            logger.debug("Using login username from user storage preference for resource {}", resourceId);

        } else if (storagePref != null
                && storagePref.getLoginUserName() != null
                && !storagePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = storagePref.getLoginUserName();
            loginFromUserPref = false;
            logger.debug("Using login username from gateway storage preference for resource {}", resourceId);

        } else {
            logger.error("No login username found for storage resource {}", resourceId);
            throw new InvalidRequestException("No login username found for storage resource " + resourceId);
        }

        // Resolve credential token based on where login came from
        String credentialToken;
        if (loginFromUserPref) {
            // Login came from user preference. Use user preference token or user profile token
            if (userStoragePref != null
                    && userStoragePref.getResourceSpecificCredentialStoreToken() != null
                    && !userStoragePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = userStoragePref.getResourceSpecificCredentialStoreToken();
                logger.debug("Using login username from user preference for resource {}", resourceId);

            } else {
                UserResourceProfile userResourceProfile = getUserResourceProfile(userId, gatewayId);
                if (userResourceProfile == null
                        || userResourceProfile.getCredentialStoreToken() == null
                        || userResourceProfile.getCredentialStoreToken().trim().isEmpty()) {
                    logger.error("No credential store token found for user {} in gateway {}", userId, gatewayId);
                    throw clientException(
                            AiravataErrorType.AUTHENTICATION_FAILURE,
                            "No credential store token found for user " + userId + " in gateway " + gatewayId);
                }
                credentialToken = userResourceProfile.getCredentialStoreToken();
            }
        } else {
            // Login came from gateway preference. Use gateway preference token or gateway profile token
            if (storagePref != null
                    && storagePref.getResourceSpecificCredentialStoreToken() != null
                    && !storagePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = storagePref.getResourceSpecificCredentialStoreToken();

            } else {
                GatewayResourceProfile gatewayResourceProfile = getGatewayResourceProfile(gatewayId);
                if (gatewayResourceProfile == null
                        || gatewayResourceProfile.getCredentialStoreToken() == null
                        || gatewayResourceProfile
                                .getCredentialStoreToken()
                                .trim()
                                .isEmpty()) {
                    logger.error("No credential store token found for gateway {}", gatewayId);
                    throw clientException(
                            AiravataErrorType.AUTHENTICATION_FAILURE,
                            "No credential store token found for gateway " + gatewayId);
                }
                credentialToken = gatewayResourceProfile.getCredentialStoreToken();
            }
        }

        AgentAdaptor adaptor = AdaptorSupportImpl.getInstance()
                .fetchStorageSSHAdaptor(gatewayId, resourceId, credentialToken, userId, loginUserName);
        logger.info("Resolved resource {} as storage resource to fetch storage details", resourceId);

        return new StorageInfoContext(loginUserName, credentialToken, adaptor);
    }

    public StorageVolumeInfo getResourceStorageInfo(
            AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            StorageInfoContext context;

            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
                ComputeResourceDescription computeResource = getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (Throwable e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (Throwable e) {
                    logger.debug("Storage resource {} not found: {}", resourceId, e.getMessage());
                }
            }

            if (computeResourceOp.isEmpty() && storageResourceOp.isEmpty()) {
                logger.error(
                        "Resource with ID {} not found as either compute resource or storage resource", resourceId);
                throw new InvalidRequestException("Resource with ID '" + resourceId
                        + "' not found as either compute resource or storage resource");
            }

            if (computeResourceOp.isPresent()) {
                logger.debug("Found compute resource with ID {}. Resolving login username and credentials", resourceId);
                context = resolveComputeStorageInfoContext(authzToken, gatewayId, userId, resourceId);
            } else {
                logger.debug("Found storage resource with ID {}. Resolving login username and credentials", resourceId);
                context = resolveStorageStorageInfoContext(authzToken, gatewayId, userId, resourceId);
            }

            return context.adaptor().getStorageVolumeInfo(location);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving storage resource.", e);
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving storage resource");
        }
    }

    public StorageDirectoryInfo getStorageDirectoryInfo(
            AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            StorageInfoContext context;

            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
                ComputeResourceDescription computeResource = getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (Throwable e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (Throwable e) {
                    logger.debug("Storage resource {} not found: {}", resourceId, e.getMessage());
                }
            }

            if (computeResourceOp.isEmpty() && storageResourceOp.isEmpty()) {
                logger.error(
                        "Resource with ID {} not found as either compute resource or storage resource", resourceId);
                throw new InvalidRequestException("Resource with ID '" + resourceId
                        + "' not found as either compute resource or storage resource");
            }

            if (computeResourceOp.isPresent()) {
                logger.debug("Found compute resource with ID {}. Resolving login username and credentials", resourceId);
                context = resolveComputeStorageInfoContext(authzToken, gatewayId, userId, resourceId);
            } else {
                logger.debug("Found storage resource with ID {}. Resolving login username and credentials", resourceId);
                context = resolveStorageStorageInfoContext(authzToken, gatewayId, userId, resourceId);
            }

            return context.adaptor().getStorageDirectoryInfo(location);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving storage resource.", e);
            throw e;
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving storage directory info");
        }
    }

    public boolean doesUserHaveSSHAccount(AuthzToken authzToken, String computeResourceId, String userId) {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            logger.debug("Checking if user {} has SSH account on compute resource {} in gateway {}", userId, computeResourceId, gatewayId);
            return org.apache.airavata.accountprovisioning.SSHAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while checking if user has an SSH Account");
        }
    }

    public boolean isSSHAccountSetupComplete(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken) {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.debug("Checking if SSH account setup is complete for user {} on compute resource {} in gateway {}", userId, computeResourceId, gatewayId);
            
            SSHCredential sshCredential = getSSHCredential(airavataCredStoreToken, gatewayId);
            return org.apache.airavata.accountprovisioning.SSHAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while checking if setup of SSH account is complete");
        }
    }

    public UserComputeResourcePreference setupSSHAccount(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken) {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            logger.debug("Setting up SSH account for user {} on compute resource {} in gateway {}", userId, computeResourceId, gatewayId);
            
            SSHCredential sshCredential = getSSHCredential(airavataCredStoreToken, gatewayId);
            return org.apache.airavata.accountprovisioning.SSHAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Throwable e) {
            throw convertException(e, "Error occurred while automatically setting up SSH account for user");
        }
    }

    public boolean shareResourceWithUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws AuthorizationException, AiravataClientException {
        try {
            if (!userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            gatewayId + ":" + "WRITE",
                            true);
                } else if (userPermission.getValue().equals(ResourcePermissionType.READ)) {
                    shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            gatewayId + ":" + "READ",
                            true);
                } else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        shareEntityWithUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                gatewayId + ":" + "MANAGE_SHARING",
                                true);
                    } else {
                        throw new AuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                    }
                } else {
                    logger.error("Invalid ResourcePermissionType : {}", userPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException | AiravataClientException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error in sharing resource with users. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws AuthorizationException, AiravataClientException {
        try {
            if (!userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            gatewayId + ":" + "WRITE",
                            true);
                } else if (groupPermission.getValue().equals(ResourcePermissionType.READ)) {
                    shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            gatewayId + ":" + "READ",
                            true);
                } else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        shareEntityWithGroups(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                gatewayId + ":" + "MANAGE_SHARING",
                                true);
                    } else {
                        throw new AuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                    }
                } else {
                    logger.error("Invalid ResourcePermissionType : {}", groupPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException | AiravataClientException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error in sharing resource with groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws AuthorizationException, AiravataClientException {
        try {
            if (!userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            gatewayId + ":" + "WRITE");
                } else if (userPermission.getValue().equals(ResourcePermissionType.READ)) {
                    revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            gatewayId + ":" + "READ");
                } else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        revokeEntitySharingFromUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                gatewayId + ":" + "MANAGE_SHARING");
                    } else {
                        throw new AuthorizationException(
                                "User is not allowed to change sharing permission because the user is not the resource owner.");
                    }
                } else {
                    logger.error("Invalid ResourcePermissionType : {}", userPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException | AiravataClientException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error in revoking access to resource from users. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws AuthorizationException, AiravataClientException, InvalidRequestException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (!userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            // For certain resource types, restrict them from being unshared with admin groups
            ResourceType resourceType = getResourceType(gatewayId, resourceId);
            Set<ResourceType> adminRestrictedResourceTypes = new HashSet<>(Arrays.asList(
                    ResourceType.EXPERIMENT, ResourceType.APPLICATION_DEPLOYMENT, ResourceType.GROUP_RESOURCE_PROFILE));
            if (adminRestrictedResourceTypes.contains(resourceType)) {
                // Prevent removing Admins WRITE/MANAGE_SHARING access and Read Only Admins READ access
                GatewayGroups gatewayGroups = retrieveGatewayGroups(gatewayId);
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.WRITE)) {
                    throw new InvalidRequestException("Not allowed to remove Admins group's WRITE access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getReadOnlyAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getReadOnlyAdminsGroupId())
                                .equals(ResourcePermissionType.READ)) {
                    throw new InvalidRequestException("Not allowed to remove Read Only Admins group's READ access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.READ)) {
                    throw new InvalidRequestException("Not allowed to remove Admins group's READ access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.MANAGE_SHARING)) {
                    throw new InvalidRequestException("Not allowed to remove Admins group's MANAGE_SHARING access.");
                }
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    revokeEntitySharingFromGroups(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "WRITE");
                } else if (groupPermission.getValue().equals(ResourcePermissionType.READ)) {
                    revokeEntitySharingFromGroups(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "READ");
                } else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccessInternal(authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        revokeEntitySharingFromGroups(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                gatewayId + ":" + "MANAGE_SHARING");
                    } else {
                        throw new AuthorizationException(
                                "User is not allowed to change sharing because the user is not the resource owner");
                    }
                } else {
                    logger.error("Invalid ResourcePermissionType : {}", groupPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException | AiravataClientException | InvalidRequestException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error in revoking access to resource from groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public GroupResourceProfile getGroupResourceProfileWithAuth(AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupResourceProfile(groupResourceProfileId);
        } catch (AuthorizationException e) {
            logger.error(
                    "Error while retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId,
                    e);
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean removeGroupResourceProfileWithAuth(AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()) {
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group resource profile");
                }
            }
            boolean result = removeGroupResourceProfile(groupResourceProfileId);
            if (result) {
                deleteEntity(gatewayId, groupResourceProfileId);
            }
            return result;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean removeGroupComputePrefsWithAuth(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute preferences");
                }
            }
            return removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error removing group compute preferences. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean removeGroupComputeResourcePolicyWithAuth(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                ComputeResourcePolicy computeResourcePolicy = getGroupComputeResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        computeResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":WRITE")) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute resource policy");
                }
            }
            return removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error removing group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public boolean removeGroupBatchQueueResourcePolicyWithAuth(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                BatchQueueResourcePolicy batchQueueResourcePolicy = getBatchQueueResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        batchQueueResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":WRITE")) {
                    throw new AuthorizationException(
                            "User does not have permission to remove batch queue resource policy");
                }
            }
            return removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error removing batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreferenceWithAuth(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving Group compute preference. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicyWithAuth(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                ComputeResourcePolicy computeResourcePolicy = getGroupComputeResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        computeResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving Group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicyWithAuth(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                BatchQueueResourcePolicy batchQueueResourcePolicy = getBatchQueueResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        batchQueueResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public void updateGroupResourceProfileWithAuth(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException {
        try {
            validateGroupResourceProfile(authzToken, groupResourceProfile);
            if (!userHasAccessInternal(
                    authzToken,
                    groupResourceProfile.getGroupResourceProfileId(),
                    ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to update group resource profile");
            }
            updateGroupResourceProfile(groupResourceProfile);
        } catch (AuthorizationException e) {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName + " not allowed access to update GroupResourceProfile "
                    + groupResourceProfile.getGroupResourceProfileId() + ", reason: " + e.getMessage());
            throw e;
        } catch (Throwable e) {
            var msg = "Error updating group resource profile. groupResourceProfileId: "
                    + groupResourceProfile.getGroupResourceProfileId();
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefListWithAuth(
            AuthzToken authzToken, String groupResourceProfileId) throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving Group compute resource preference. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyListWithAuth(
            AuthzToken authzToken, String groupResourceProfileId) throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving Group batch queue resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyListWithAuth(
            AuthzToken authzToken, String groupResourceProfileId) throws AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Throwable e) {
            var msg = "Error retrieving Group compute resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }

    public String createGroupResourceProfileWithAuth(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException {
        try {
            var result = createGroupResourceProfileWithSharing(authzToken, groupResourceProfile);
            return result;
        } catch (AuthorizationException e) {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName
                    + " not allowed access to resources referenced in this GroupResourceProfile. Reason: "
                    + e.getMessage());
            throw e;
        } catch (Throwable e) {
            var msg = "Error creating group resource profile.";
            logger.error(msg, e);
            throw convertException(e, msg);
        }
    }
}
