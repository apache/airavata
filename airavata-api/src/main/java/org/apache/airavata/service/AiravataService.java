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
import java.util.stream.Collectors;
import org.apache.airavata.accountprovisioning.InvalidSetupException;
import org.apache.airavata.accountprovisioning.InvalidUsernameException;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
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
import org.apache.airavata.model.commons.airavata_commonsConstants;
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
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.error.ProjectNotFoundException;
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
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.GatewayGroupsInitializer;
import org.apache.airavata.sharing.models.Domain;
import org.apache.airavata.sharing.models.DuplicateEntryException;
import org.apache.airavata.sharing.models.Entity;
import org.apache.airavata.sharing.models.EntitySearchField;
import org.apache.airavata.sharing.models.EntityType;
import org.apache.airavata.sharing.models.PermissionType;
import org.apache.airavata.sharing.models.SearchCondition;
import org.apache.airavata.sharing.models.SearchCriteria;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.User;
import org.apache.airavata.sharing.models.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@DependsOn("messagingFactory")
@ConditionalOnProperty(
        name = "services.airavata.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class AiravataService {
    private static final Logger logger = LoggerFactory.getLogger(AiravataService.class);

    private final AiravataServerProperties properties;

    private record StorageInfoContext(String loginUserName, String credentialToken, AgentAdaptor adaptor) {}

    // Record aliases for sharing registry models to avoid name clashes
    private record SharingEntity(Entity delegate) {}

    private boolean validateString(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private AiravataSystemException airavataSystemException(
            AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(
                errorType, message, cause);
    }

    /**
     * Functional interface for operations that may throw RegistryServiceException.
     */
    @FunctionalInterface
    private interface RegistryOperation<T> {
        T execute() throws RegistryServiceException;
    }

    /**
     * Helper method to execute registry operations with standardized error handling.
     * Reduces boilerplate code for try-catch blocks.
     *
     * @param operation The operation name for logging
     * @param registryOp The operation to execute
     * @param <T> The return type
     * @return The result of the operation
     * @throws AiravataSystemException if the operation fails
     */
    private <T> T executeRegistryOperation(String operation, RegistryOperation<T> registryOp)
            throws AiravataSystemException {
        try {
            return registryOp.execute();
        } catch (RegistryServiceException e) {
            String msg = String.format("Error while %s: %s", operation, e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    private boolean safeIsUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayId)
            throws AiravataSystemException {
        return isUserResourceProfileExists(userId, gatewayId);
    }

    private boolean isGatewayResourceProfileExists(String gatewayId) throws AiravataSystemException {
        var profile = getGatewayResourceProfile(gatewayId);
        return profile != null;
    }

    private final RegistryService registryService;
    private final SharingRegistryService sharingRegistryService;
    private final CredentialStoreService credentialStoreService;
    private final SSHAccountManager sshAccountManager;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;
    
    // Domain services
    private final org.apache.airavata.service.domain.ExperimentService experimentService;
    private final org.apache.airavata.service.domain.ProjectService projectService;
    private final org.apache.airavata.service.domain.NotificationService notificationService;
    private final org.apache.airavata.service.domain.DataProductService dataProductService;
    private final org.apache.airavata.service.domain.ApplicationService applicationService;
    private final org.apache.airavata.service.security.AuthorizationService authorizationService;
    private final org.apache.airavata.service.sharing.SharingManager sharingManager;
    
    private Publisher statusPublisher;
    private Publisher experimentPublisher;

    public AiravataService(
            AiravataServerProperties properties,
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            CredentialStoreService credentialStoreService,
            SSHAccountManager sshAccountManager,
            GatewayGroupsInitializer gatewayGroupsInitializer,
            org.apache.airavata.service.domain.ExperimentService experimentService,
            org.apache.airavata.service.domain.ProjectService projectService,
            org.apache.airavata.service.domain.NotificationService notificationService,
            org.apache.airavata.service.domain.DataProductService dataProductService,
            org.apache.airavata.service.domain.ApplicationService applicationService,
            org.apache.airavata.service.security.AuthorizationService authorizationService,
            org.apache.airavata.service.sharing.SharingManager sharingManager) {
        this.properties = properties;
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.credentialStoreService = credentialStoreService;
        this.sshAccountManager = sshAccountManager;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
        this.experimentService = experimentService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.dataProductService = dataProductService;
        this.applicationService = applicationService;
        this.authorizationService = authorizationService;
        this.sharingManager = sharingManager;

        logger.info("Initialized RegistryService");
        logger.info("Initialized SharingRegistryService");
        logger.info("Initialized CredentialStoreService");
        logger.info("Initialized domain services");
    }

    @jakarta.annotation.PostConstruct
    public void initializePublishers() throws AiravataException {
        logger.info("[BEAN-INIT] AiravataService.initializePublishers() called");
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
            logger.info("[BEAN-INIT] Initialized StatusPublisher");
        } catch (AiravataException e) {
            String msg = String.format(
                    "Error while getting StatusPublisher: %s. Publisher will be unavailable.", e.getMessage());
            logger.warn(msg, e);
            // Don't throw - allow server to start without RabbitMQ
        }

        try {
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);
            logger.info("[BEAN-INIT] Initialized ExperimentPublisher");
        } catch (AiravataException e) {
            String msg = String.format(
                    "Error while getting ExperimentPublisher: %s. Publisher will be unavailable.", e.getMessage());
            logger.warn(msg, e);
            // Don't throw - allow server to start without RabbitMQ
        }
    }

    @jakarta.annotation.PostConstruct
    public void init() throws AiravataException {
        logger.info("[BEAN-INIT] AiravataService.init() called");
        try {
            initSharingRegistry();
        } catch (SharingRegistryException | DuplicateEntryException e) {
            String msg = String.format("Error while initializing sharing registry: %s", e.getMessage());
            logger.error(msg, e);
            var exception = new AiravataException(msg);
            exception.initCause(e);
            throw exception;
        }

        try {
            postInitDefaultGateway();
        } catch (RegistryServiceException | CredentialStoreException | AiravataSystemException e) {
            String msg = String.format(
                    "Error while post-initializing default gateway: %s. Gateway initialization will be skipped.",
                    e.getMessage());
            logger.warn(msg, e);
            // Don't throw - allow server to start without default gateway initialization
        }
    }

    private void postInitDefaultGateway()
            throws RegistryServiceException, CredentialStoreException, AiravataSystemException {
        String defaultGateway = properties.services.default_.gateway;
        if (defaultGateway == null || defaultGateway.isEmpty()) {
            logger.debug("No default gateway configured. Skipping gateway initialization.");
            return;
        }
        var gatewayResourceProfile = getGatewayResourceProfile(defaultGateway);
        if (gatewayResourceProfile == null) {
            logger.debug(
                    "Default gateway '{}' does not exist in database. Skipping gateway initialization.",
                    defaultGateway);
            return;
        }
        if (gatewayResourceProfile != null && gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {

            logger.debug("Starting to add password credential to default gateway={}", defaultGateway);
            var passwordCredential = new PasswordCredential();
            passwordCredential.setPortalUserName(properties.services.default_.user);
            passwordCredential.setGatewayId(defaultGateway);
            passwordCredential.setLoginUserName(properties.services.default_.user);
            passwordCredential.setPassword(properties.services.default_.password);
            passwordCredential.setDescription("Credentials for default gateway=" + defaultGateway);
            String token = null;
            try {
                logger.info("Creating password credential for default gateway={}", defaultGateway);
                token = addPasswordCredential(passwordCredential);
            } catch (CredentialStoreException ex) {
                String msg = String.format(
                        "Failed to create password credential for default gateway=%s: %s",
                        defaultGateway, ex.getMessage());
                logger.error(msg, ex);
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, ex);
            }

            if (token != null) {
                logger.debug(
                        "Adding password credential token " + token + " to the default gateway : " + defaultGateway);
                gatewayResourceProfile.setIdentityServerPwdCredToken(token);
                gatewayResourceProfile.setIdentityServerTenant(defaultGateway);
                updateGatewayResourceProfile(defaultGateway, gatewayResourceProfile);
            }
        }
    }

    private void initSharingRegistry() throws SharingRegistryException, DuplicateEntryException {
        String defaultGateway = properties.services.default_.gateway;
        if (!isDomainExists(defaultGateway)) {
            var domain = new Domain();
            domain.setDomainId(defaultGateway);
            domain.setName(defaultGateway);
            domain.setDescription("Domain entry for " + domain.getName());
            createDomain(domain);

            var user = new User();
            user.setDomainId(domain.getDomainId());
            user.setUserId(properties.services.default_.user + "@" + defaultGateway);
            user.setUserName(properties.services.default_.user);
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
    }

    public List<String> getAllUsersInGateway(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllUsersInGateway(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving users: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws AiravataSystemException {
        try {
            return registryService.updateGateway(gatewayId, updatedGateway);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating gateway: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Gateway getGateway(String gatewayId) throws AiravataSystemException {
        try {
            var result = registryService.getGateway(gatewayId);
            logger.debug("Airavata found the gateway with " + gatewayId);
            return result;
        } catch (RegistryServiceException e) {
            String msg = "Error while getting the gateway: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteGateway(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.deleteGateway(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting the gateway: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<Gateway> getAllGateways() throws AiravataSystemException {
        try {
            logger.debug("Airavata searching for all gateways");
            return registryService.getAllGateways();
        } catch (RegistryServiceException e) {
            String msg = "Error while getting all the gateways: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean isGatewayExist(String gatewayId) throws AiravataSystemException {
        try {
            logger.debug("Airavata verifying if the gateway with " + gatewayId + "exits");
            return registryService.isGatewayExist(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while getting gateway: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String createNotification(Notification notification) throws AiravataSystemException {
        return notificationService.createNotification(notification);
    }

    public boolean updateNotification(Notification notification) throws AiravataSystemException {
        return notificationService.updateNotification(notification);
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws AiravataSystemException {
        return notificationService.deleteNotification(gatewayId, notificationId);
    }

    public Notification getNotification(String gatewayId, String notificationId) throws AiravataSystemException {
        return notificationService.getNotification(gatewayId, notificationId);
    }

    public List<Notification> getAllNotifications(String gatewayId) throws AiravataSystemException {
        return notificationService.getAllNotifications(gatewayId);
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws AiravataSystemException {
        return dataProductService.registerDataProduct(dataProductModel);
    }

    public DataProductModel getDataProduct(String productUri) throws AiravataSystemException {
        return dataProductService.getDataProduct(productUri);
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws AiravataSystemException {
        return dataProductService.registerReplicaLocation(replicaLocationModel);
    }

    public DataProductModel getParentDataProduct(String productUri) throws AiravataSystemException {
        return dataProductService.getParentDataProduct(productUri);
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws AiravataSystemException {
        return dataProductService.getChildDataProducts(productUri);
    }

    public boolean isUserExists(String gatewayId, String userName) throws AiravataSystemException {
        try {
            logger.debug("Checking if the user" + userName + "exists in the gateway" + gatewayId);
            return registryService.isUserExists(gatewayId, userName);
        } catch (RegistryServiceException e) {
            String msg = "Error while verifying user: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Project getProject(String projectId) throws AiravataSystemException, ProjectNotFoundException {
        return projectService.getProject(projectId);
    }

    public void updateProject(String projectId, Project updatedProject) throws AiravataSystemException {
        projectService.updateProject(projectId, updatedProject);
    }

    public boolean deleteProject(String projectId) throws AiravataSystemException, ProjectNotFoundException {
        return projectService.deleteProject(projectId);
    }

    public List<Project> searchProjects(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws AiravataSystemException {
        try {
            var accessibleProjIds = new ArrayList<String>();
            List<Project> result;
            if (properties.services.sharing.enabled) {
                var sharingFilters = new ArrayList<SearchCriteria>();
                var searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                sharingFilters.add(searchCriteria);
                sharingRegistryService
                        .searchEntities(
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
        } catch (RegistryServiceException | SharingRegistryException e) {
            String msg = "Error while retrieving projects: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjectIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException {
        return registryService.searchProjects(gatewayId, userName, accessibleProjectIds, filters, limit, offset);
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException {
        return experimentService.getUserExperiments(gatewayId, userName, limit, offset);
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws AiravataSystemException {
        return experimentService.getExperimentsInProject(gatewayId, projectId, limit, offset);
    }

    public List<ExperimentModel> getExperimentsInProject(AuthzToken authzToken, String projectId, int limit, int offset)
            throws AuthorizationException, AiravataSystemException, ProjectNotFoundException {
        try {
            var project = getProject(projectId);
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (properties.services.sharing.enabled
                    && (!authzToken.getClaimsMap().get(Constants.USER_NAME).equals(project.getOwner())
                            || !authzToken
                                    .getClaimsMap()
                                    .get(Constants.GATEWAY_ID)
                                    .equals(project.getGatewayId()))) {
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            return experimentService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (AuthorizationException | ProjectNotFoundException | AiravataSystemException e) {
            throw e;
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
            throws AiravataSystemException {
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
        } catch (RegistryServiceException e) {
            String msg = "Error while getting experiment statistics: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException {
        return experimentService.getExperiment(airavataExperimentId);
    }

    private String createExperimentInternal(String gatewayId, ExperimentModel experiment)
            throws AiravataSystemException {
        return experimentService.createExperiment(gatewayId, experiment);
    }

    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException {
        var existingExperiment = getExperiment(airavataExperimentId);
        authorizationService.validateExperimentReadAccess(
                authzToken, airavataExperimentId, existingExperiment.getUserName(), existingExperiment.getGatewayId());
        return existingExperiment;
    }

    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, AiravataSystemException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var existingExperiment = getExperiment(airavataExperimentId);
            if (gatewayId.equals(existingExperiment.getGatewayId())) {
                return existingExperiment;
            } else {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        }
    }

    public void updateExperiment(AuthzToken authzToken, String airavataExperimentId, ExperimentModel experiment)
            throws AuthorizationException, AiravataSystemException {
        var existingExperiment = getExperiment(airavataExperimentId);
        authorizationService.validateExperimentWriteAccess(
                authzToken, airavataExperimentId, existingExperiment.getUserName(), existingExperiment.getGatewayId());
        
        // Update sharing entity if enabled
        if (properties.services.sharing.enabled) {
            sharingManager.updateExperimentEntity(airavataExperimentId, experiment);
        }
        
        experimentService.updateExperiment(airavataExperimentId, experiment);
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws AiravataSystemException {
        experimentService.updateExperiment(airavataExperimentId, experiment);
    }

    public boolean deleteExperiment(String experimentId) throws AiravataSystemException {
        return experimentService.deleteExperiment(experimentId);
    }

    public String cloneExperiment(
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, AuthorizationException,
                    AiravataSystemException, InvalidRequestException {
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
                var project = getProject(authzToken, newExperimentProjectId);
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
                throw new AuthorizationException(
                        "User does not have permission to clone an experiment in this project");
            }

            existingExperiment.setCreationTime(
                    AiravataUtils.getCurrentTimestamp().getTime());
            if (existingExperiment.getExecutionId() != null) {
                try {
                    var applicationOutputs = getApplicationOutputs(existingExperiment.getExecutionId());
                    existingExperiment.setExperimentOutputs(applicationOutputs);
                } catch (AiravataSystemException e) {
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
                } catch (AiravataSystemException e) {
                    logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
                }
            }
            logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
            existingExperiment.setUserName(userId);

            var expId = createExperimentInternal(gatewayId, existingExperiment);
            try {
                sharingManager.createExperimentEntity(expId, existingExperiment);
            } catch (AiravataSystemException ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("rolling back experiment creation Exp ID : " + expId);
                try {
                    experimentService.deleteExperiment(expId);
                } catch (AiravataSystemException e) {
                    logger.error("Error deleting experiment during rollback: " + e.getMessage());
                }
                throw ex;
            }

            return expId;
        } catch (ExperimentNotFoundException
                | ProjectNotFoundException
                | AuthorizationException
                | AiravataSystemException e) {
            throw e;
        }
    }

    public void terminateExperiment(String airavataExperimentId, String gatewayId)
            throws ExperimentNotFoundException, AiravataSystemException {
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
        } catch (AiravataSystemException e) {
            String msg = "Error occurred while cancelling the experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException {
        return registryService.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
    }

    /**
     * Search experiments with sharing registry integration - processes filters and
     * builds search criteria
     */
    public List<ExperimentSummaryModel> searchExperiments(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws AiravataSystemException {
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
            // If no more filtering to be done (either empty or all done through sharing
            // API), set the offset to 0
            if (filteredInSharing) {
                finalOffset = 0;
            }
            return searchExperiments(gatewayId, userName, accessibleExpIds, filtersCopy, limit, finalOffset);
        } catch (RegistryServiceException | SharingRegistryException e) {
            String msg = "Error while retrieving experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getExperimentStatus(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving experiment status: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getExperimentOutputs(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving experiment outputs: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getDetailedExperimentTree(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving detailed experiment tree: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AiravataSystemException {
        try {
            return registryService.getApplicationOutputs(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application outputs: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws AiravataSystemException {
        return executeRegistryOperation("retrieving compute resource",
                () -> registryService.getComputeResource(computeResourceId));
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws AiravataSystemException {
        return executeRegistryOperation("saving compute resource",
                () -> registryService.registerComputeResource(computeResourceDescription));
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws AiravataSystemException {
        return executeRegistryOperation("updating compute resource",
                () -> registryService.updateComputeResource(computeResourceId, computeResourceDescription));
    }

    public boolean deleteComputeResource(String computeResourceId) throws AiravataSystemException {
        return executeRegistryOperation("deleting compute resource",
                () -> registryService.deleteComputeResource(computeResourceId));
    }

    public Map<String, String> getAllComputeResourceNames() throws AiravataSystemException {
        return executeRegistryOperation("retrieving compute resource names",
                () -> registryService.getAllComputeResourceNames());
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws AiravataSystemException {
        return executeRegistryOperation("saving storage resource",
                () -> registryService.registerStorageResource(storageResourceDescription));
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws AiravataSystemException {
        return executeRegistryOperation("retrieving storage resource",
                () -> registryService.getStorageResource(storageResourceId));
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws AiravataSystemException {
        return executeRegistryOperation("updating storage resource",
                () -> registryService.updateStorageResource(storageResourceId, storageResourceDescription));
    }

    public boolean deleteStorageResource(String storageResourceId) throws AiravataSystemException {
        return executeRegistryOperation("deleting storage resource",
                () -> registryService.deleteStorageResource(storageResourceId));
    }

    public Map<String, String> getAllStorageResourceNames() throws AiravataSystemException {
        return executeRegistryOperation("retrieving storage resource names",
                () -> registryService.getAllStorageResourceNames());
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws AiravataSystemException {
        try {
            return registryService.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering gateway resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws AiravataSystemException {
        try {
            return registryService.getGatewayResourceProfile(gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving gateway resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws AiravataSystemException {
        try {
            return registryService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating gateway resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) throws AiravataSystemException {
        try {
            return registryService.deleteGatewayResourceProfile(gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while removing gateway resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getUserResourceProfile(userId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving user resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws AiravataSystemException {
        try {
            return registryService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating user resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws AiravataSystemException {
        try {
            return registryService.deleteUserResourceProfile(userId, gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while removing user resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws AiravataSystemException {
        try {
            return registryService.getGroupResourceProfile(groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AiravataSystemException {
        try {
            registryService.updateGroupResourceProfile(groupResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating group resource profile: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws AiravataSystemException {
        try {
            return registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group resource list: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GatewayGroups getGatewayGroups(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getGatewayGroups(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving gateway groups: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.isGatewayGroupsExists(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while checking if gateway groups exist: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AiravataSystemException {
        try {
            registryService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating experiment configuration: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AiravataSystemException {
        try {
            registryService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating resource scheduling: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AiravataSystemException {
        try {
            return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerApplicationDeployment(
            AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws AiravataSystemException, InvalidRequestException, AuthorizationException,
                    ApplicationSettingsException {
        try {
            String result = registerApplicationDeployment(gatewayId, applicationDeployment);
            if (properties.services.sharing.enabled) {
                var entity = new Entity();
                entity.setEntityId(result);
                final String domainId = gatewayId;
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                entity.setOwnerId(userName + "@" + domainId);
                entity.setName(result);
                entity.setDescription(applicationDeployment.getAppDeploymentDescription());
                createEntity(new SharingEntity(entity));
                shareEntityWithAdminGatewayGroups(entity);
            }
            return result;
        } catch (InvalidRequestException | AuthorizationException e) {
            throw e;
        } catch (SharingRegistryException | DuplicateEntryException e) {
            String msg = "Error while registering application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws AiravataSystemException {
        try {
            return registryService.getApplicationDeployment(appDeploymentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws AuthorizationException, AiravataSystemException, InvalidRequestException {
        try {
            if (properties.services.sharing.enabled) {
                final boolean hasAccess = userHasAccess(authzToken, appDeploymentId, ResourcePermissionType.READ);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have access to application deployment " + appDeploymentId);
                }
            }
            return getApplicationDeployment(appDeploymentId);
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error occurred while getting application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws AiravataSystemException {
        try {
            return registryService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateApplicationDeployment(
            AuthzToken authzToken, String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                final boolean hasAccess = userHasAccess(authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error occurred while updating application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws AiravataSystemException {
        try {
            return registryService.deleteApplicationDeployment(appDeploymentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws AuthorizationException, AiravataSystemException, InvalidRequestException {
        try {
            if (properties.services.sharing.enabled) {
                final boolean hasAccess = userHasAccess(authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return deleteApplicationDeployment(appDeploymentId);
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error occurred while deleting application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws AiravataSystemException {
        return applicationService.getApplicationInterface(appInterfaceId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws AiravataSystemException {
        return applicationService.getApplicationDeployments(appModuleId);
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws AiravataSystemException {
        return applicationService.registerApplicationInterface(gatewayId, applicationInterface);
    }

    public String cloneApplicationInterface(String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws AiravataSystemException, InvalidRequestException, AuthorizationException {
        try {
            var existingInterface = getApplicationInterface(existingAppInterfaceID);
            if (existingInterface == null) {
                String msg =
                        "Provided application interface does not exist.Please provide a valid application interface id...";
                logger.error(msg);
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, null);
            }

            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            var interfaceId = registerApplicationInterface(gatewayId, existingInterface);
            logger.debug("Airavata cloned application interface : " + existingAppInterfaceID + " for gateway id : "
                    + gatewayId);
            return interfaceId;
        } catch (AiravataSystemException e) {
            throw e;
        }
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws AiravataSystemException {
        try {
            return registryService.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws AiravataSystemException {
        try {
            return registryService.deleteApplicationInterface(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllApplicationInterfaceNames(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application interfaces: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AiravataSystemException {
        try {
            return registryService.getAllApplicationInterfaces(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application interfaces: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws AiravataSystemException {
        try {
            return registryService.getApplicationInputs(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application inputs: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws AiravataSystemException {
        try {
            return registryService.registerApplicationModule(gatewayId, applicationModule);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.getApplicationModule(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws AiravataSystemException {
        try {
            return registryService.updateApplicationModule(appModuleId, applicationModule);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllAppModules(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all application modules: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteApplicationModule(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.deleteApplicationModule(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        return registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible app modules with sharing registry integration
     */
    public List<ApplicationModule> getAccessibleAppModules(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var accessibleAppDeploymentIds = new ArrayList<String>();
            if (properties.services.sharing.enabled) {
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
            List<GroupResourceProfile> groupResourceProfileList = getGroupResourceList(authzToken, gatewayId);
            for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
                List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                        groupResourceProfile.getComputePreferences();
                for (GroupComputeResourcePreference groupComputeResourcePreference :
                        groupComputeResourcePreferenceList) {
                    accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
                }
            }
            return getAccessibleAppModules(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AiravataSystemException e) {
            throw e;
        } catch (RegistryServiceException | SharingRegistryException e) {
            String msg = "Error occurred while getting accessible app modules: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getJobStatuses(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving job statuses: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getJobDetails(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving job details: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws AiravataSystemException {
        try {
            return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding local job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws AiravataSystemException {
        try {
            return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding SSH job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws AiravataSystemException {
        try {
            return registryService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding SSH fork job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission)
            throws AiravataSystemException {
        try {
            return registryService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding cloud job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws AiravataSystemException {
        try {
            return registryService.addUNICOREJobSubmissionDetails(
                    computeResourceId, priorityOrder, unicoreJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding UNICORE job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws AiravataSystemException {
        try {
            return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating local job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws AiravataSystemException {
        try {
            return registryService.getLocalJobSubmission(jobSubmissionId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving local job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws AiravataSystemException {
        try {
            return registryService.getSSHJobSubmission(jobSubmissionId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving SSH job submission: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws AiravataSystemException {
        try {
            return registryService.getCloudJobSubmission(jobSubmissionId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving cloud job submission: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws AiravataSystemException {
        try {
            return registryService.getUnicoreJobSubmission(jobSubmissionId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving UNICORE job submission: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws AiravataSystemException {
        try {
            return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws AiravataSystemException {
        try {
            return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws AiravataSystemException {
        try {
            return registryService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws AiravataSystemException {
        try {
            return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting job submission interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws AiravataSystemException {
        try {
            return registryService.registerResourceJobManager(resourceJobManager);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding resource job manager: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AiravataSystemException {
        try {
            return registryService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating resource job manager: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AiravataSystemException {
        try {
            return registryService.getResourceJobManager(resourceJobManagerId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving resource job manager: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws AiravataSystemException {
        try {
            return registryService.deleteResourceJobManager(resourceJobManagerId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting resource job manager: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        return credentialStoreService.addPasswordCredential(passwordCredential);
    }

    public void deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        credentialStoreService.deletePWDCredential(tokenId, gatewayId);
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding data movement interface to resource: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating local data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws AiravataSystemException {
        try {
            return registryService.getLocalDataMovement(dataMovementId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving local data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding SCP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating SCP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException {
        return registryService.getUserProjects(gatewayId, userName, limit, offset);
    }

    /**
     * Get user projects with sharing registry integration
     */
    public List<Project> getUserProjects(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
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
        } catch (RegistryServiceException | SharingRegistryException e) {
            String msg = "Error while retrieving user projects: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        return registryService.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    /**
     * Get accessible application deployments with sharing registry integration
     */
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            AuthzToken authzToken, String gatewayId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            List<String> accessibleAppDeploymentIds = new ArrayList<>();
            if (properties.services.sharing.enabled) {
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
            List<GroupResourceProfile> groupResourceProfileList = getGroupResourceList(authzToken, gatewayId);
            for (GroupResourceProfile groupResourceProfile : groupResourceProfileList) {
                List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                        groupResourceProfile.getComputePreferences();
                for (GroupComputeResourcePreference groupComputeResourcePreference :
                        groupComputeResourcePreferenceList) {
                    accessibleComputeResourceIds.add(groupComputeResourcePreference.getComputeResourceId());
                }
            }
            return getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (RegistryServiceException | SharingRegistryException e) {
            String msg = "Error occurred while getting accessible application deployments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.getAppModuleDeployedResources(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String appModuleId,
            String gatewayId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws AiravataSystemException {
        try {
            return registryService.getAccessibleApplicationDeploymentsForAppModule(
                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving accessible application deployments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
            AuthzToken authzToken, String appModuleId, String groupResourceProfileId)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException {
        try {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

            // Get list of compute resources for this Group Resource Profile
            if (!userHasAccess(authzToken, groupResourceProfileId, ResourcePermissionType.READ)) {
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
            try {
                sharingRegistryService.searchEntities(gatewayId, userName + "@" + gatewayId, sharingFilters, 0, -1)
                        .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));
            } catch (SharingRegistryException e) {
                String msg = "Error while searching entities: " + e.getMessage();
                logger.error(msg, e);
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
            }

            return getAccessibleApplicationDeploymentsForAppModule(
                    appModuleId, gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AiravataSystemException e) {
            throw e;
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws AiravataSystemException {
        try {
            return registryService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving available compute resources: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws AiravataSystemException {
        try {
            return registryService.getSCPDataMovement(dataMovementId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving SCP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.addUnicoreDataMovementDetails(
                    resourceId, dmType, priorityOrder, unicoreDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding UNICORE data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws AiravataSystemException {
        try {
            return registryService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating unicore data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws AiravataSystemException {
        try {
            return registryService.getUnicoreDataMovement(dataMovementId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving UNICORE data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String addGridFTPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws AiravataSystemException {
        try {
            return registryService.addGridFTPDataMovementDetails(
                    resourceId, dmType, priorityOrder, gridFTPDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding GridFTP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws AiravataSystemException {
        try {
            return registryService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating GridFTP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws AiravataSystemException {
        try {
            return registryService.getGridFTPDataMovement(dataMovementId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving GridFTP data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws AiravataSystemException {
        try {
            return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting data movement interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws AiravataSystemException {
        try {
            return registryService.deleteBatchQueue(computeResourceId, queueName);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting batch queue: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AiravataSystemException {
        try {
            return registryService.addGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering gateway resource profile preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws AiravataSystemException {
        try {
            return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering gateway storage preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AiravataSystemException {
        try {
            return registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving gateway compute resource preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws AiravataSystemException {
        try {
            return registryService.getGatewayStoragePreference(gatewayID, storageId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving gateway storage preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws AiravataSystemException {
        try {
            return registryService.getAllGatewayComputeResourcePreferences(gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all gateway compute resource preferences: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws AiravataSystemException {
        try {
            return registryService.getAllGatewayStoragePreferences(gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all gateway storage preferences: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws AiravataSystemException {
        try {
            return registryService.getAllGatewayResourceProfiles();
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all gateway resource profiles: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AiravataSystemException {
        try {
            return registryService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating gateway compute resource preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws AiravataSystemException {
        try {
            return registryService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating gateway data storage preference: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AiravataSystemException {
        try {
            return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting gateway compute resource preference: " + gatewayID + " "
                    + computeResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws AiravataSystemException {
        try {
            return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting gateway data storage preference: " + gatewayID + " " + storageId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws AiravataSystemException {
        try {
            return registryService.registerUserResourceProfile(userResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering user resource profile: " + userResourceProfile.getUserId() + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AiravataSystemException {
        try {
            return registryService.isUserResourceProfileExists(userId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while checking existence of user resource profile: " + userId + " " + gatewayId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AiravataSystemException {
        try {
            return registryService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering user resource profile preference: " + userId + " " + gatewayID + " "
                    + computeResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String userStorageResourceId, UserStoragePreference dataStoragePreference)
            throws AiravataSystemException {
        try {
            return registryService.addUserStoragePreference(
                    userId, gatewayID, userStorageResourceId, dataStoragePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while registering user storage preference: " + userId + " " + gatewayID + " "
                    + userStorageResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws AiravataSystemException {
        try {
            return registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while reading user compute resource preference: " + userId + " " + gatewayID + " "
                    + userComputeResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws AiravataSystemException {
        try {
            return registryService.getUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (RegistryServiceException e) {
            String msg = "Error while reading user data storage preference: " + userId + " " + gatewayID + " "
                    + userStorageId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws AiravataSystemException {
        try {
            return registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while reading User compute resource preferences: " + userId + " " + gatewayID + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws AiravataSystemException {
        try {
            return registryService.getAllUserStoragePreferences(userId, gatewayID);
        } catch (RegistryServiceException e) {
            String msg = "Error while reading User data storage preferences: " + userId + " " + gatewayID + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws AiravataSystemException {
        try {
            return registryService.getAllUserResourceProfiles();
        } catch (RegistryServiceException e) {
            String msg = "Error while reading retrieving all user resource profiles: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AiravataSystemException {
        try {
            return registryService.updateUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating user compute resource preference: " + userId + " " + gatewayID + " "
                    + computeResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String userStorageId, UserStoragePreference userStoragePreference)
            throws AiravataSystemException {
        try {
            return registryService.updateUserStoragePreference(userId, gatewayID, userStorageId, userStoragePreference);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating user data storage preference: " + userId + " " + gatewayID + " "
                    + userStorageId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String userComputeResourceId)
            throws AiravataSystemException {
        try {
            return registryService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting user compute resource preference: " + userId + " " + gatewayID + " "
                    + userComputeResourceId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String userStorageId)
            throws AiravataSystemException {
        try {
            return registryService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting user data storage preference: " + userId + " " + gatewayID + " "
                    + userStorageId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws AiravataSystemException {
        try {
            return registryService.getLatestQueueStatuses();
        } catch (RegistryServiceException e) {
            String msg = "Error in retrieving queue statuses: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AiravataSystemException {
        try {
            return registryService.createGroupResourceProfile(groupResourceProfile);
        } catch (RegistryServiceException e) {
            String msg = "Error while creating group resource profile: "
                    + groupResourceProfile.getGroupResourceProfileId() + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws AiravataSystemException {
        try {
            return registryService.removeGroupResourceProfile(groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while removing group resource profile: " + groupResourceProfileId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws AiravataSystemException {
        try {
            return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while removing group compute preferences: " + computeResourceId + " "
                    + groupResourceProfileId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws AiravataSystemException {
        try {
            return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group compute resource preference: " + computeResourceId + " "
                    + groupResourceProfileId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws AiravataSystemException {
        try {
            return registryService.getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while retrieving group compute resource policy: " + resourcePolicyId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws AiravataSystemException {
        try {
            return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while removing group compute resource policy: " + resourcePolicyId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId)
            throws AiravataSystemException {
        try {
            return registryService.getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while retrieving batch queue resource policy: " + resourcePolicyId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws AiravataSystemException {
        try {
            return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (RegistryServiceException e) {
            String msg = "Error while removing group batch queue resource policy: " + resourcePolicyId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws AiravataSystemException {
        try {
            return registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group compute resource preference list: " + groupResourceProfileId
                    + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws AiravataSystemException {
        try {
            return registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group batch queue resource policy list: " + groupResourceProfileId
                    + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws AiravataSystemException {
        try {
            return registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving group compute resource policy list: " + groupResourceProfileId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Parser getParser(String parserId, String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getParser(parserId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving parser: " + parserId + " " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String saveParser(Parser parser) throws AiravataSystemException {
        try {
            return registryService.saveParser(parser);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while saving parser: " + parser.getId() + " " + parser.getGatewayId() + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<Parser> listAllParsers(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.listAllParsers(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while listing all parsers: " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void removeParser(String parserId, String gatewayId) throws AiravataSystemException {
        try {
            registryService.removeParser(parserId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while removing parser: " + parserId + " " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getParsingTemplate(templateId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while retrieving parsing template: " + templateId + " " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws AiravataSystemException {
        try {
            return registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving parsing templates for experiment: " + experimentId + " " + gatewayId
                    + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws AiravataSystemException {
        try {
            return registryService.saveParsingTemplate(parsingTemplate);
        } catch (RegistryServiceException e) {
            String msg = "Error while saving parsing template: " + parsingTemplate.getId() + " "
                    + parsingTemplate.getGatewayId() + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws AiravataSystemException {
        try {
            registryService.removeParsingTemplate(templateId, gatewayId);
        } catch (RegistryServiceException e) {
            String msg =
                    "Error while removing parsing template: " + templateId + " " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.listAllParsingTemplates(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while listing all parsing templates: " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Helper methods for sharing registry and authorization
    public GatewayGroups retrieveGatewayGroups(String gatewayId) throws AiravataSystemException {
        try {
            if (isGatewayGroupsExists(gatewayId)) {
                return getGatewayGroups(gatewayId);
            } else {
                return gatewayGroupsInitializer.initialize(gatewayId);
            }
        } catch (AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error while initializing gateway groups: " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void createManageSharingPermissionTypeIfMissing(String domainId) throws AiravataSystemException {
        // AIRAVATA-3297 Some gateways were created without the MANAGE_SHARING
        // permission, so add it if missing
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
        } catch (SharingRegistryException | DuplicateEntryException e) {
            String msg = "Error creating MANAGE_SHARING permission type: " + domainId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void shareEntityWithAdminGatewayGroups(Entity entity)
            throws AiravataSystemException, InvalidRequestException, AuthorizationException, SharingRegistryException {
        final String domainId = entity.getDomainId();
        GatewayGroups gatewayGroups = retrieveGatewayGroups(domainId);
        createManageSharingPermissionTypeIfMissing(domainId);
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

    public boolean userHasAccess(AuthzToken authzToken, String entityId, ResourcePermissionType permissionType)
            throws AiravataSystemException {
        final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        final String userId = authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + domainId;
        return userHasAccess(domainId, userId, entityId, domainId + ":" + permissionType);
    }
    
    private boolean userHasAccess(String gatewayId, String userId, String entityId, String permissionTypeId) {
        try {
            final boolean hasOwnerAccess = sharingRegistryService.userHasAccess(
                    gatewayId, userId, entityId, gatewayId + ":OWNER");
            if (hasOwnerAccess) {
                return true;
            }
            return sharingRegistryService.userHasAccess(gatewayId, userId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            logger.warn("Error while checking if user has access: " + entityId + " " + permissionTypeId + " "
                    + e.getMessage());
            return false;
        }
    }

    // Credential management methods
    public String generateAndRegisterSSHKeys(String gatewayId, String userName, String description)
            throws InvalidRequestException, AiravataSystemException {
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
            } catch (SharingRegistryException | DuplicateEntryException ex) {
                String msg = "Error while creating SSH credential: " + userName + " " + description + " "
                        + ex.getMessage() + ". rolling back ssh key creation";
                logger.error(msg, ex);
                credentialStoreService.deleteSSHCredential(key, gatewayId);
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, ex);
            }
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            return key;
        } catch (AiravataSystemException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while registering SSH Credential: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerPwdCredential(
            String gatewayId, String userName, String loginUserName, String password, String description)
            throws InvalidRequestException, AiravataSystemException {
        try {
            var pwdCredential = new PasswordCredential();
            pwdCredential.setPortalUserName(userName);
            pwdCredential.setLoginUserName(loginUserName);
            pwdCredential.setPassword(password);
            pwdCredential.setDescription(description);
            pwdCredential.setGatewayId(gatewayId);
            var key = addPasswordCredential(pwdCredential);
            try {
                var entity = new Entity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingRegistryService.createEntity(entity);
            } catch (SharingRegistryException | DuplicateEntryException ex) {
                String msg = "Error while registering password credential: " + userName + " " + description + " "
                        + ex.getMessage() + ". rolling back password registration";
                logger.error(msg, ex);
                try {
                    deletePWDCredential(key, gatewayId);
                } catch (CredentialStoreException rollbackEx) {
                    logger.error("Failed to rollback password credential deletion", rollbackEx);
                }
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, ex);
            }
            logger.debug("Generated PWD credential for gateway: " + gatewayId + ", user: " + loginUserName);
            return key;
        } catch (AiravataSystemException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while registering password credential: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public CredentialSummary getCredentialSummary(AuthzToken authzToken, String tokenId, String gatewayId)
            throws AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccess(authzToken, tokenId, ResourcePermissionType.READ)) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
            var credentialSummary = getCredentialSummary(tokenId, gatewayId);
            logger.debug("Retrieved the credential summary for token: " + tokenId + ", GatewayId: " + gatewayId);
            return credentialSummary;
        } catch (AuthorizationException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while getting credential summary: " + tokenId + " " + gatewayId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            AuthzToken authzToken, SummaryType type, String gatewayId, String userName)
            throws AiravataSystemException, InvalidRequestException {
        try {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN.name());
            filters.add(searchCriteria);
            List<String> accessibleTokenIds =
                    sharingRegistryService
                            .searchEntities(gatewayId, userName + "@" + gatewayId, filters, 0, -1)
                            .stream()
                            .map(p -> p.getEntityId())
                            .collect(Collectors.toList());
            List<CredentialSummary> credentialSummaries =
                    getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
            logger.debug("Successfully retrieved credential summaries of type: " + type + ", GatewayId: " + gatewayId);
            return credentialSummaries;
        } catch (CredentialStoreException | SharingRegistryException e) {
            String msg = "Error occurred while getting all credential summaries: " + type + " " + gatewayId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId)
            throws AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccess(authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : "
                    + airavataCredStoreToken);
            return deleteSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (AuthorizationException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while deleting SSH pub key: " + airavataCredStoreToken + " " + gatewayId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId)
            throws AiravataSystemException, AuthorizationException {
        try {
            if (!userHasAccess(authzToken, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Deleted PWD credential for gateway Id : " + gatewayId + " and with token id : "
                    + airavataCredStoreToken);
            deletePWDCredential(airavataCredStoreToken, gatewayId);
            return true;
        } catch (AuthorizationException e) {
            throw e;
        } catch (CredentialStoreException e) {
            String msg = "Error occurred while deleting PWD credential: " + airavataCredStoreToken + " " + gatewayId
                    + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Project management methods with sharing registry integration
    public String createProject(String gatewayId, Project project) throws AiravataSystemException {
        try {
            var projectId = projectService.createProject(gatewayId, project);
            // TODO: verify that gatewayId and project.gatewayId match authzToken
            try {
                sharingManager.createProjectEntity(projectId, project);
            } catch (AiravataSystemException ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back project creation Proj ID : " + projectId);
                try {
                    projectService.deleteProject(projectId);
                } catch (ProjectNotFoundException e) {
                    // Ignore - project may not exist if creation failed
                }
                throw ex;
            }
            logger.debug("Airavata created project with project Id : " + projectId + " for gateway Id : " + gatewayId);
            return projectId;
        } catch (AiravataSystemException e) {
            throw e;
        }
    }

    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var existingProject = getProject(projectId);
            if (properties.services.sharing.enabled
                            && !authzToken
                                    .getClaimsMap()
                                    .get(Constants.USER_NAME)
                                    .equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(
                        gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
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
            logger.debug("Updated project with project Id : " + projectId);
        } catch (ProjectNotFoundException
                | AuthorizationException
                | InvalidRequestException
                | AiravataSystemException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while updating project: " + projectId + " " + updatedProject.getName() + " "
                    + updatedProject.getDescription() + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var existingProject = getProject(projectId);
            if (properties.services.sharing.enabled
                            && !authzToken
                                    .getClaimsMap()
                                    .get(Constants.USER_NAME)
                                    .equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(existingProject.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(
                        gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            boolean ret = deleteProject(projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId);
            return ret;
        } catch (ProjectNotFoundException | AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while deleting project: " + projectId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Project getProject(AuthzToken authzToken, String projectId)
            throws AiravataSystemException, ProjectNotFoundException, AuthorizationException {
        try {
            var project = getProject(projectId);
            if (authzToken.getClaimsMap().get(Constants.USER_NAME).equals(project.getOwner())
                    && authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(project.getGatewayId())) {
                return project;
            } else if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(
                        gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
                return project;
            } else {
                return null;
            }
        } catch (ProjectNotFoundException | AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while getting project: " + projectId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Experiment management methods with sharing registry integration
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException {
        try {
            var experimentId = createExperimentInternal(gatewayId, experiment);
            try {
                sharingManager.createExperimentEntity(experimentId, experiment);
            } catch (AiravataSystemException ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back experiment creation Exp ID : " + experimentId);
                experimentService.deleteExperiment(experimentId);
                throw ex;
            }

            logger.info(
                    experimentId,
                    "Created new experiment with experiment name {} and id ",
                    experiment.getExperimentName(),
                    experimentId);
            return experimentId;
        } catch (AiravataSystemException e) {
            throw e;
        }
    }

    public void validateLaunchExperimentAccess(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
            throws InvalidRequestException, AuthorizationException, AiravataSystemException, SharingRegistryException {
        authorizationService.validateLaunchExperimentAccess(authzToken, gatewayId, experiment);
    }

    public boolean deleteExperimentWithAuth(AuthzToken authzToken, String experimentId)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException,
                    ExperimentNotFoundException, ProjectNotFoundException {
        try {
            var experimentModel = getExperiment(experimentId);

            if (properties.services.sharing.enabled
                            && !authzToken
                                    .getClaimsMap()
                                    .get(Constants.USER_NAME)
                                    .equals(experimentModel.getUserName())
                    || !authzToken.getClaimsMap().get(Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!sharingRegistryService.userHasAccess(
                        gatewayId, userId + "@" + gatewayId, experimentId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            if (!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)) {
                throw new InvalidRequestException(
                        "Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
            }
            return deleteExperiment(experimentId);
        } catch (AuthorizationException | InvalidRequestException | AiravataSystemException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while deleting experiment: " + experimentId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ResourceType getResourceType(String domainId, String entityId) throws AiravataSystemException {
        try {
            var entity = sharingRegistryService.getEntity(domainId, entityId);
            for (ResourceType resourceType : ResourceType.values()) {
                if (entity.getEntityTypeId().equals(domainId + ":" + resourceType.name())) {
                    return resourceType;
                }
            }
            String msg = String.format(
                    "Unrecognized entity type id: domainId=%s, entityId=%s, entityTypeId=%s",
                    domainId, entityId, entity.getEntityTypeId());
            logger.error(msg);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, null);
        } catch (SharingRegistryException e) {
            String msg = String.format(
                    "Error while getting resource type: domainId=%s, entityId=%s. Reason: %s",
                    domainId, entityId, e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Gateway management methods with sharing registry integration
    public String addGateway(Gateway gateway) throws AiravataSystemException {
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

            logger.debug("Successfully created the gateway with " + gatewayId);
            return gatewayId;
        } catch (RegistryServiceException | SharingRegistryException | DuplicateEntryException e) {
            String msg = "Error while adding gateway: " + gateway.getGatewayId() + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Event publishing methods
    public void publishExperimentSubmitEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws AiravataSystemException {
        var event = new ExperimentSubmitEvent(experimentId, gatewayId);
        var messageContext = new MessageContext(
                event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        try {
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            String msg = "Error while publishing experiment submit event: " + experimentId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void publishExperimentCancelEvent(Publisher experimentPublisher, String gatewayId, String experimentId)
            throws AiravataSystemException {
        var event = new ExperimentSubmitEvent(experimentId, gatewayId);
        var messageContext = new MessageContext(
                event,
                MessageType.EXPERIMENT_CANCEL,
                "CANCEL.EXP-" + UUID.randomUUID().toString(),
                gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        try {
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            String msg = "Error while publishing experiment cancel event: " + experimentId + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void publishExperimentIntermediateOutputsEvent(
            Publisher experimentPublisher, String gatewayId, String experimentId, List<String> outputNames)
            throws AiravataSystemException {
        var event = new ExperimentIntermediateOutputsEvent(experimentId, gatewayId, outputNames);
        var messageContext = new MessageContext(
                event,
                MessageType.INTERMEDIATE_OUTPUTS,
                "INTERMEDIATE_OUTPUTS.EXP-" + UUID.randomUUID().toString(),
                gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        try {
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            String msg = "Error while publishing experiment intermediate outputs event: " + experimentId + " "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    /**
     * Validate and fetch intermediate outputs - checks access, job state, and
     * existing processes
     */
    public void fetchIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            // Verify that user has WRITE access to experiment
            final boolean hasAccess = userHasAccess(authzToken, airavataExperimentId, ResourcePermissionType.WRITE);
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

            // Figure out if there are any currently running intermediate output fetching
            // processes for outputNames
            // First, find any existing intermediate output fetch processes for outputNames
            List<ProcessModel> intermediateOutputFetchProcesses = existingExperiment.getProcesses().stream()
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
                    .filter(p -> {
                        return p.getTasks().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING);
                    })
                    .filter(p -> {
                        return p.getProcessOutputs().stream().anyMatch(o -> outputNames.contains(o.getName()));
                    })
                    .collect(Collectors.toList());
            if (!intermediateOutputFetchProcesses.isEmpty()) {
                var msg = "There are already intermediate output fetching tasks running for those outputs.";
                logger.error(msg);
                throw new InvalidRequestException(msg);
            }

            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            publishExperimentIntermediateOutputsEvent(
                    experimentPublisher, gatewayId, airavataExperimentId, outputNames);
        } catch (AuthorizationException | InvalidRequestException e) {
            throw e;
        }
    }

    /**
     * Get intermediate output process status - finds the most recent matching
     * process and returns its status
     */
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            // Verify that user has READ access to experiment
            final boolean hasAccess = userHasAccess(authzToken, airavataExperimentId, ResourcePermissionType.READ);
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
                        List<String> names = p.getProcessOutputs().stream()
                                .map(o -> o.getName())
                                .collect(Collectors.toList());
                        return new HashSet<>(names).equals(new HashSet<>(outputNames));
                    })
                    .sorted(Comparator.comparing(ProcessModel::getLastUpdateTime)
                            .reversed())
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
        }
    }

    // Access control methods
    public List<String> getAllAccessibleUsers(
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            ThrowingBiFunction<
                            SharingRegistryService, ResourcePermissionType, Collection<User>, AiravataSystemException>
                    userListFunction)
            throws AiravataSystemException {
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
            ThrowingBiFunction<
                            SharingRegistryService,
                            ResourcePermissionType,
                            Collection<UserGroup>,
                            AiravataSystemException>
                    groupListFunction)
            throws AiravataSystemException {
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
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType, boolean directlySharedOnly)
            throws AiravataSystemException, SharingRegistryException {
        String gatewayId;
        try {
            gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        } catch (NullPointerException e) {
            String msg = String.format(
                    "Error getting gatewayId from authzToken: resourceId=%s, permissionType=%s, directlySharedOnly=%s. Reason: %s",
                    resourceId, permissionType, directlySharedOnly, e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }

        ThrowingBiFunction<SharingRegistryService, ResourcePermissionType, Collection<User>, AiravataSystemException>
                userListFunction;
        if (directlySharedOnly) {
            userListFunction = (c, t) -> getDirectlySharedUsersInternal(gatewayId, resourceId, t);
        } else {
            userListFunction = (c, t) -> getSharedUsersInternal(gatewayId, resourceId, t);
        }

        return getAllAccessibleUsers(gatewayId, resourceId, permissionType, userListFunction);
    }

    private Collection<User> getDirectlySharedUsersInternal(
            String gatewayId, String resourceId, ResourcePermissionType permissionType) throws AiravataSystemException {
        try {
            return sharingRegistryService.getListOfDirectlySharedUsers(
                    gatewayId, resourceId, gatewayId + ":" + permissionType.name());
        } catch (SharingRegistryException e) {
            String msg = String.format(
                    "Error getting directly shared users: gatewayId=%s, resourceId=%s, permissionType=%s. Reason: %s",
                    gatewayId, resourceId, permissionType.name(), e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    private Collection<User> getSharedUsersInternal(
            String gatewayId, String resourceId, ResourcePermissionType permissionType) throws AiravataSystemException {
        try {
            return sharingRegistryService.getListOfSharedUsers(
                    gatewayId, resourceId, gatewayId + ":" + permissionType.name());
        } catch (SharingRegistryException e) {
            String msg = String.format(
                    "Error getting shared users: gatewayId=%s, resourceId=%s, permissionType=%s. Reason: %s",
                    gatewayId, resourceId, permissionType.name(), e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    /**
     * Get all accessible groups for a resource (includes shared and directly
     * shared)
     */
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType, boolean directlySharedOnly)
            throws AiravataSystemException, SharingRegistryException {
        String gatewayId;
        try {
            gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        } catch (NullPointerException e) {
            String msg = String.format(
                    "Error getting gatewayId from authzToken: resourceId=%s, permissionType=%s, directlySharedOnly=%s. Reason: %s",
                    resourceId, permissionType, directlySharedOnly, e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }

        ThrowingBiFunction<
                        SharingRegistryService, ResourcePermissionType, Collection<UserGroup>, AiravataSystemException>
                groupListFunction;
        if (directlySharedOnly) {
            groupListFunction = (c, t) -> getDirectlySharedGroupsInternal(gatewayId, resourceId, t);
        } else {
            groupListFunction = (c, t) -> getSharedGroupsInternal(gatewayId, resourceId, t);
        }

        return getAllAccessibleGroups(gatewayId, resourceId, permissionType, groupListFunction);
    }

    private Collection<UserGroup> getDirectlySharedGroupsInternal(
            String gatewayId, String resourceId, ResourcePermissionType permissionType) throws AiravataSystemException {
        try {
            return sharingRegistryService.getListOfDirectlySharedGroups(
                    gatewayId, resourceId, gatewayId + ":" + permissionType.name());
        } catch (SharingRegistryException e) {
            String msg = String.format(
                    "Error getting directly shared groups: gatewayId=%s, resourceId=%s, permissionType=%s. Reason: %s",
                    gatewayId, resourceId, permissionType.name(), e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    private Collection<UserGroup> getSharedGroupsInternal(
            String gatewayId, String resourceId, ResourcePermissionType permissionType) throws AiravataSystemException {
        try {
            return sharingRegistryService.getListOfSharedGroups(
                    gatewayId, resourceId, gatewayId + ":" + permissionType.name());
        } catch (SharingRegistryException e) {
            String msg = String.format(
                    "Error getting shared groups: gatewayId=%s, resourceId=%s, permissionType=%s. Reason: %s",
                    gatewayId, resourceId, permissionType.name(), e.getMessage());
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Group resource profile management with sharing registry integration
    public String createGroupResourceProfileWithSharing(
            AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException, AiravataSystemException, SharingRegistryException, DuplicateEntryException,
                    InvalidRequestException {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        validateGroupResourceProfile(authzToken, groupResourceProfile);
        String groupResourceProfileId = createGroupResourceProfile(groupResourceProfile);
        if (properties.services.sharing.enabled) {
            try {
                var entity = new Entity();
                entity.setEntityId(groupResourceProfileId);
                entity.setDomainId(groupResourceProfile.getGatewayId());
                entity.setEntityTypeId(groupResourceProfile.getGatewayId() + ":" + "GROUP_RESOURCE_PROFILE");
                entity.setOwnerId(userName + "@" + groupResourceProfile.getGatewayId());
                entity.setName(groupResourceProfile.getGroupResourceProfileName());

                sharingRegistryService.createEntity(entity);
                shareEntityWithAdminGatewayGroups(entity);
            } catch (SharingRegistryException
                    | DuplicateEntryException
                    | AiravataSystemException
                    | InvalidRequestException
                    | AuthorizationException ex) {
                String msg = "Error while creating group resource profile: " + groupResourceProfileId + " "
                        + ex.getMessage() + ". Rolling back group resource profile creation.";
                logger.error(msg, ex);
                try {
                    removeGroupResourceProfile(groupResourceProfileId);
                } catch (AiravataSystemException rollbackEx) {
                    String rollbackMsg = "Failed to rollback group resource profile creation: " + groupResourceProfileId
                            + " " + rollbackEx.getMessage();
                    logger.error(rollbackMsg, rollbackEx);
                }
                throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, ex);
            }
        }
        return groupResourceProfileId;
    }

    public void validateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException, AiravataSystemException {
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
            if (!userHasAccess(authzToken, tokenId, ResourcePermissionType.READ)) {
                String msg = "User does not have READ permission to credential token " + tokenId + ".";
                throw new AuthorizationException(msg);
            }
        }
    }

    // Launch experiment business logic
    public void launchExperiment(AuthzToken authzToken, String gatewayId, String airavataExperimentId)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, ProjectNotFoundException, SharingRegistryException {
        try {
            logger.info("Launching experiment {}", airavataExperimentId);
            ExperimentModel experiment = getExperiment(airavataExperimentId);

            if (experiment == null) {
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

            // For backwards compatibility, if there is no groupResourceProfileId, look up
            // one that is shared with the
            // user
            if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
                List<GroupResourceProfile> groupResourceProfiles = getGroupResourceList(authzToken, gatewayId);
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
                    String msg = "User " + username + " in gateway " + gatewayId
                            + " doesn't have access to any group resource profiles.";
                    logger.error(msg);
                    throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, null);
                }
            }

            // Validate access to group resource profile and application deployments
            validateLaunchExperimentAccess(authzToken, gatewayId, experiment);
            publishExperimentSubmitEvent(experimentPublisher, gatewayId, airavataExperimentId);
        } catch (InvalidRequestException
                | AiravataSystemException
                | AuthorizationException
                | ExperimentNotFoundException e) {
            throw e;
        }
    }

    /**
     * Get group resource list with sharing registry integration
     */
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws AiravataSystemException {
        try {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            var accessibleGroupResProfileIds = new ArrayList<String>();
            if (properties.services.sharing.enabled) {
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
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while getting group resource list: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    // Sharing Registry Delegation Methods for ServerHandler
    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        return sharingRegistryService.isDomainExists(domainId);
    }

    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException {
        return sharingRegistryService.createDomain(domain);
    }

    public String createUser(User user) throws SharingRegistryException, DuplicateEntryException {
        return sharingRegistryService.createUser(user);
    }

    public String createGroup(UserGroup group) throws SharingRegistryException, DuplicateEntryException {
        return sharingRegistryService.createGroup(group);
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        return sharingRegistryService.addUsersToGroup(domainId, userIds, groupId);
    }

    public String createEntityType(EntityType entityType) throws SharingRegistryException, DuplicateEntryException {
        return sharingRegistryService.createEntityType(entityType);
    }

    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException {
        return sharingRegistryService.createPermissionType(permissionType);
    }


    public SharingEntity getEntity(String domainId, String entityId) throws SharingRegistryException {
        return new SharingEntity(sharingRegistryService.getEntity(domainId, entityId));
    }

    public void updateEntity(SharingEntity entity) throws SharingRegistryException {
        sharingRegistryService.updateEntity(entity.delegate());
    }

    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascade)
            throws SharingRegistryException {
        return sharingRegistryService.shareEntityWithUsers(domainId, entityId, userList, permissionTypeId, cascade);
    }

    public SSHCredential getSSHCredential(String token, String gatewayId)
            throws AiravataSystemException, CredentialStoreException {
        return credentialStoreService.getSSHCredential(token, gatewayId);
    }

    public void createEntity(SharingEntity entity) throws SharingRegistryException, DuplicateEntryException {
        sharingRegistryService.createEntity(entity.delegate());
    }

    public List<SharingEntity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        return sharingRegistryService.searchEntities(domainId, userId, filters, offset, limit).stream()
                .map(SharingEntity::new)
                .collect(Collectors.toList());
    }

    public boolean shareEntityWithGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId, boolean cascade)
            throws SharingRegistryException {
        return sharingRegistryService.shareEntityWithGroups(domainId, entityId, groupList, permissionTypeId, cascade);
    }

    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException {
        return sharingRegistryService.revokeEntitySharingFromUsers(domainId, entityId, userList, permissionTypeId);
    }

    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException {
        return sharingRegistryService.revokeEntitySharingFromGroups(domainId, entityId, groupList, permissionTypeId);
    }

    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException {
        return sharingRegistryService.deleteEntity(domainId, entityId);
    }

    /**
     * Resolves compute resource storage info context (login username, credential
     * token, and adaptor).
     * Handles user preference → group preference fallback for both login and
     * credentials.
     */
    private StorageInfoContext resolveComputeStorageInfoContext(
            AuthzToken authzToken, String gatewayId, String userId, String resourceId)
            throws InvalidRequestException, AiravataSystemException, AgentException {
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
            var groupResourceProfiles = getGroupResourceList(authzToken, gatewayId);
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
            // Login username came from user preference. Use user preference token → user
            // profile token
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
                    String msg = "No credential store token found for user " + userId + " in gateway " + gatewayId;
                    logger.error(msg);
                    throw airavataSystemException(AiravataErrorType.AUTHENTICATION_FAILURE, msg, null);
                }
                credentialToken = userResourceProfile.getCredentialStoreToken();
            }
        } else {
            // Login username came from group preference. Use group preference token → group
            // profile default token →
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
                    String msg = "No credential store token found for user " + userId + " in gateway " + gatewayId;
                    logger.error(msg);
                    throw airavataSystemException(AiravataErrorType.AUTHENTICATION_FAILURE, msg, null);
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
     * Resolves storage resource storage info context (login username, credential
     * token, and adaptor).
     * Handles user preference → gateway preference fallback for both login and
     * credentials.
     */
    private StorageInfoContext resolveStorageStorageInfoContext(
            AuthzToken authzToken, String gatewayId, String userId, String resourceId)
            throws InvalidRequestException, AgentException, AiravataSystemException, AuthorizationException {
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
            // Login came from user preference. Use user preference token or user profile
            // token
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
                    String msg = "No credential store token found for user " + userId + " in gateway " + gatewayId;
                    logger.error(msg);
                    throw airavataSystemException(AiravataErrorType.AUTHENTICATION_FAILURE, msg, null);
                }
                credentialToken = userResourceProfile.getCredentialStoreToken();
            }
        } else {
            // Login came from gateway preference. Use gateway preference token or gateway
            // profile token
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
                    String msg = "No credential store token found for gateway " + gatewayId;
                    logger.error(msg);
                    throw airavataSystemException(AiravataErrorType.AUTHENTICATION_FAILURE, msg, null);
                }
                credentialToken = gatewayResourceProfile.getCredentialStoreToken();
            }
        }

        AgentAdaptor adaptor = AdaptorSupportImpl.getInstance()
                .fetchStorageSSHAdaptor(gatewayId, resourceId, credentialToken, userId, loginUserName);
        logger.info("Resolved resource {} as storage resource to fetch storage details", resourceId);

        return new StorageInfoContext(loginUserName, credentialToken, adaptor);
    }

    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
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
            } catch (AiravataSystemException e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (AiravataSystemException e) {
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
        } catch (InvalidRequestException | AiravataSystemException | AuthorizationException e) {
            throw e;
        } catch (AgentException e) {
            String msg = "Error occurred while getting resource storage info: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public StorageDirectoryInfo getStorageDirectoryInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
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
            } catch (AiravataSystemException e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (AiravataSystemException e) {
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
        } catch (InvalidRequestException | AiravataSystemException | AuthorizationException e) {
            throw e;
        } catch (AgentException e) {
            String msg = "Error occurred while getting storage directory info: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean doesUserHaveSSHAccount(AuthzToken authzToken, String computeResourceId, String userId)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            logger.debug(
                    "Checking if user {} has SSH account on compute resource {} in gateway {}",
                    userId,
                    computeResourceId,
                    gatewayId);
            return sshAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (InvalidSetupException | InvalidUsernameException e) {
            String msg = "Error occurred while checking if user has an SSH Account: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean isSSHAccountSetupComplete(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws AiravataSystemException, AuthorizationException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.debug(
                    "Checking if SSH account setup is complete for user {} on compute resource {} in gateway {}",
                    userId,
                    computeResourceId,
                    gatewayId);

            SSHCredential sshCredential = getSSHCredential(airavataCredStoreToken, gatewayId);
            return sshAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (InvalidSetupException
                | InvalidUsernameException
                | CredentialStoreException
                | AiravataSystemException e) {
            String msg = "Error occurred while checking if setup of SSH account is complete: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public UserComputeResourcePreference setupSSHAccount(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            logger.debug(
                    "Setting up SSH account for user {} on compute resource {} in gateway {}",
                    userId,
                    computeResourceId,
                    gatewayId);

            SSHCredential sshCredential = getSSHCredential(airavataCredStoreToken, gatewayId);
            return sshAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
        } catch (AiravataSystemException e) {
            throw e;
        } catch (CredentialStoreException | InvalidSetupException | InvalidUsernameException e) {
            String msg = "Error occurred while automatically setting up SSH account for user: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean shareResourceWithUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (!userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
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
                    if (userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)) {
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
                    logger.error(
                            "Invalid ResourcePermissionType : {}",
                            userPermission.getValue().toString());
                    throw new AiravataSystemException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error in sharing resource with users. Resource ID : " + resourceId + ": " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (!userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
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
                    if (userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)) {
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
                    logger.error(
                            "Invalid ResourcePermissionType : {}",
                            groupPermission.getValue().toString());
                    throw new AiravataSystemException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException e) {
            throw e;
        } catch (SharingRegistryException e) {
            var msg = "Error in sharing resource with groups. Resource ID : " + resourceId + ". More info : "
                    + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws AuthorizationException, AiravataSystemException, InvalidRequestException {
        try {
            if (!userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":" + "WRITE");
                } else if (userPermission.getValue().equals(ResourcePermissionType.READ)) {
                    revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":" + "READ");
                } else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)) {
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
                    logger.error(
                            "Invalid ResourcePermissionType : {}",
                            userPermission.getValue().toString());
                    throw new AiravataSystemException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException e) {
            throw e;
        } catch (SharingRegistryException e) {
            var msg = "Error in revoking access to resource from users. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (!userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            // For certain resource types, restrict them from being unshared with admin
            // groups
            ResourceType resourceType = getResourceType(gatewayId, resourceId);
            Set<ResourceType> adminRestrictedResourceTypes = new HashSet<>(Arrays.asList(
                    ResourceType.EXPERIMENT, ResourceType.APPLICATION_DEPLOYMENT, ResourceType.GROUP_RESOURCE_PROFILE));
            if (adminRestrictedResourceTypes.contains(resourceType)) {
                // Prevent removing Admins WRITE/MANAGE_SHARING access and Read Only Admins READ
                // access
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
                    if (userHasAccess(authzToken, resourceId, ResourcePermissionType.OWNER)) {
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
                    logger.error(
                            "Invalid ResourcePermissionType : {}",
                            groupPermission.getValue().toString());
                    throw new AiravataSystemException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            return true;
        } catch (AuthorizationException | InvalidRequestException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String msg = "Error in revoking access to resource from groups. Resource ID : " + resourceId
                    + ". More info : " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupResourceProfile(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (properties.services.sharing.enabled) {
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                    throw new AuthorizationException("User does not have permission to remove group resource profile");
                }
            }
            boolean result = removeGroupResourceProfile(groupResourceProfileId);
            if (result) {
                deleteEntity(gatewayId, groupResourceProfileId);
            }
            return result;
        } catch (AuthorizationException e) {
            throw e;
        } catch (SharingRegistryException e) {
            var msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean removeGroupComputePrefs(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
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
        }
    }

    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
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
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
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
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException, InvalidRequestException {
        try {
            if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                ComputeResourcePolicy computeResourcePolicy = getGroupComputeResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        computeResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                BatchQueueResourcePolicy batchQueueResourcePolicy = getBatchQueueResourcePolicy(resourcePolicyId);
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(
                        gatewayId,
                        userId + "@" + gatewayId,
                        batchQueueResourcePolicy.getGroupResourceProfileId(),
                        gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException, AiravataSystemException {
        try {
            validateGroupResourceProfile(authzToken, groupResourceProfile);
            if (!userHasAccess(
                    authzToken, groupResourceProfile.getGroupResourceProfileId(), ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to update group resource profile");
            }
            updateGroupResourceProfile(groupResourceProfile);
        } catch (AuthorizationException e) {
            var userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName + " not allowed access to update GroupResourceProfile "
                    + groupResourceProfile.getGroupResourceProfileId() + ", reason: " + e.getMessage());
            throw e;
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws AuthorizationException, AiravataSystemException {
        try {
            if (properties.services.sharing.enabled) {
                var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                if (!userHasAccess(gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw e;
        }
    }

    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws AuthorizationException, AiravataSystemException, InvalidRequestException {
        try {
            var result = createGroupResourceProfileWithSharing(authzToken, groupResourceProfile);
            return result;
        } catch (AuthorizationException | AiravataSystemException | InvalidRequestException e) {
            throw e;
        } catch (SharingRegistryException | DuplicateEntryException e) {
            String msg = "Error creating group resource profile. More info : " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
