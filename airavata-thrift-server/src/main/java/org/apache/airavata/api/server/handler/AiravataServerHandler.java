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
package org.apache.airavata.api.server.handler;

import java.util.*;
import java.util.function.BiFunction;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavata_apiConstants;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.config.ServerSettings;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.appcatalog.accountprovisioning.SSHAccountProvisioner;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
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
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.appcatalog.ApplicationCatalogService;
import org.apache.airavata.credential.service.CredentialService;
import org.apache.airavata.service.dataproduct.DataProductService;
import org.apache.airavata.service.experiment.ExperimentService;
import org.apache.airavata.service.gateway.GatewayService;
import org.apache.airavata.service.groupprofile.GroupResourceProfileService;
import org.apache.airavata.messaging.service.EventPublisher;
import org.apache.airavata.service.notification.NotificationService;
import org.apache.airavata.service.parser.ParserService;
import org.apache.airavata.service.project.ProjectService;
import org.apache.airavata.service.resource.ResourceService;
import org.apache.airavata.service.resourceprofile.GatewayResourceProfileService;
import org.apache.airavata.service.resourceprofile.UserResourceProfileService;
import org.apache.airavata.security.service.interceptor.SecurityCheck;
import org.apache.airavata.sharing.service.ResourceSharingService;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private Publisher statusPublisher;
    private Publisher experimentPublisher;

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;
    private final CredentialStoreServerHandler credentialHandler;
    private final ApplicationCatalogService applicationCatalogService;
    private final CredentialService credentialService;
    private final ExperimentService experimentService;
    private final GatewayService gatewayService;
    private final GatewayResourceProfileService gatewayResourceProfileService;
    private final NotificationService notificationService;
    private final ProjectService projectService;
    private final ResourceService resourceService;
    private final UserResourceProfileService userResourceProfileService;
    private final DataProductService dataProductService;
    private final GroupResourceProfileService groupResourceProfileService;
    private final ParserService parserService;
    private final ResourceSharingService resourceSharingService;
    private final org.apache.airavata.credential.service.SSHAccountService sshAccountService;

    public AiravataServerHandler(
            RegistryServerHandler registryHandler,
            SharingRegistryServerHandler sharingHandler,
            CredentialStoreServerHandler credentialHandler) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
        this.credentialHandler = credentialHandler;
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
        EventPublisher eventPub = new EventPublisher(statusPublisher, experimentPublisher);
        this.applicationCatalogService =
                new ApplicationCatalogService(registryHandler, sharingHandler, credentialHandler);
        this.credentialService = new CredentialService(credentialHandler, sharingHandler);
        this.experimentService = new ExperimentService(registryHandler, sharingHandler, eventPub);
        this.gatewayService = new GatewayService(registryHandler, sharingHandler);
        this.gatewayResourceProfileService = new GatewayResourceProfileService(registryHandler);
        this.notificationService = new NotificationService(registryHandler);
        this.projectService = new ProjectService(registryHandler, sharingHandler);
        this.userResourceProfileService = new UserResourceProfileService(registryHandler);
        this.dataProductService = new DataProductService(registryHandler);
        this.groupResourceProfileService = new GroupResourceProfileService(registryHandler, sharingHandler);
        this.experimentService.setGroupResourceProfileService(this.groupResourceProfileService);
        this.resourceService = new ResourceService(registryHandler, this.groupResourceProfileService);
        this.parserService = new ParserService(registryHandler);
        this.resourceSharingService = new ResourceSharingService(sharingHandler, registryHandler);
        this.sshAccountService = new org.apache.airavata.credential.service.SSHAccountService(credentialHandler);
    }

    public AiravataServerHandler() throws Exception {
        this(new RegistryServerHandler(), new SharingRegistryServerHandler(), new CredentialStoreServerHandler());
    }

    public void initialize() {
        try {
            initSharingRegistry();
            postInitDefaultGateway();
        } catch (Exception e) {
            logger.error("Error during server initialization", e);
        }
    }

    /**
     * This method creates a password token for the default gateway profile. Default gateway is originally initialized
     * at the registry server but we can not add the password token at that step as the credential store is not initialized
     * before registry server.
     */
    private void postInitDefaultGateway() {

        try {

            GatewayResourceProfile gatewayResourceProfile =
                    registryHandler.getGatewayResourceProfile(ServerSettings.getDefaultUserGateway());
            if (gatewayResourceProfile != null && gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {

                logger.debug("Starting to add the password credential for default gateway : "
                        + ServerSettings.getDefaultUserGateway());

                PasswordCredential passwordCredential = new PasswordCredential();
                passwordCredential.setPortalUserName(ServerSettings.getDefaultUser());
                passwordCredential.setGatewayId(ServerSettings.getDefaultUserGateway());
                passwordCredential.setLoginUserName(ServerSettings.getDefaultUser());
                passwordCredential.setPassword(ServerSettings.getDefaultUserPassword());
                passwordCredential.setDescription("Credentials for default gateway");

                String token = null;
                try {
                    logger.info("Creating password credential for default gateway");
                    token = credentialHandler.addPasswordCredential(passwordCredential);
                } catch (Exception ex) {
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
                    registryHandler.updateGatewayResourceProfile(
                            ServerSettings.getDefaultUserGateway(), gatewayResourceProfile);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to add the password credentials for the default gateway", e);
        }
    }

    private void initSharingRegistry() throws ApplicationSettingsException, TException {
        try {
            if (!sharingHandler.isDomainExists(ServerSettings.getDefaultUserGateway())) {
                Domain domain = new Domain();
                domain.setDomainId(ServerSettings.getDefaultUserGateway());
                domain.setName(ServerSettings.getDefaultUserGateway());
                domain.setDescription("Domain entry for " + domain.getName());
                sharingHandler.createDomain(domain);

                User user = new User();
                user.setDomainId(domain.getDomainId());
                user.setUserId(ServerSettings.getDefaultUser() + "@" + ServerSettings.getDefaultUserGateway());
                user.setUserName(ServerSettings.getDefaultUser());
                sharingHandler.createUser(user);

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

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDescription("Credential Store Token entity type");
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
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String getAPIVersion() throws TException {
        return airavata_apiConstants.AIRAVATA_API_VERSION;
    }

    /**
     * Verify if User Exists within Airavata.
     *
     * @param authzToken
     * @param gatewayId
     * @param userName
     * @return true/false
     */
    @Override
    @SecurityCheck
    public boolean isUserExists(AuthzToken authzToken, String gatewayId, String userName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> gatewayService.isUserExists(ctx, gatewayId, userName));
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.addGateway(ctx, gateway));
    }

    /**
     * Get all users in the gateway
     *
     * @param authzToken
     * @param gatewayId  The gateway data model.
     * @return users
     * list of usernames of the users in the gateway
     */
    @Override
    @SecurityCheck
    public List<String> getAllUsersInGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.getAllUsersInGateway(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayService.updateGateway(ctx, gatewayId, updatedGateway));
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.getGateway(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.deleteGateway(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.getAllGateways(ctx));
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> gatewayService.isGatewayExist(ctx, gatewayId));
    }

    /**
     * * API methods to retrieve notifications
     * *
     *
     * @param authzToken
     * @param notification
     */
    @Override
    @SecurityCheck
    public String createNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> notificationService.createNotification(ctx, notification));
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> notificationService.updateNotification(ctx, notification));
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> notificationService.deleteNotification(ctx, gatewayId, notificationId));
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> notificationService.getNotification(ctx, gatewayId, notificationId));
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> notificationService.getAllNotifications(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> credentialService.generateAndRegisterSSHKeys(ctx, description));
    }

    /**
     * Register Username PWD Pair with Airavata Credential Store.
     *
     * @param authzToken
     * @param loginUserName
     * @param password
     * @param description
     * @return airavataCredStoreToken
     */
    @Override
    @SecurityCheck
    public String registerPwdCredential(
            AuthzToken authzToken, String loginUserName, String password, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> credentialService.registerPwdCredential(ctx, loginUserName, password, description));
    }

    @Override
    @SecurityCheck
    public CredentialSummary getCredentialSummary(AuthzToken authzToken, String tokenId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> credentialService.getCredentialSummary(ctx, tokenId));
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaries(AuthzToken authzToken, SummaryType type)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> credentialService.getAllCredentialSummaries(ctx, type));
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> credentialService.deleteSSHPubKey(ctx, airavataCredStoreToken));
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> credentialService.deletePWDCredential(ctx, airavataCredStoreToken));
    }

    /**
     * Create a Project
     *
     * @param project
     */
    @Override
    @SecurityCheck
    public String createProject(AuthzToken authzToken, String gatewayId, Project project)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> projectService.createProject(ctx, gatewayId, project));
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        ThriftAdapter.execute(authzToken, null, ctx -> {
            projectService.updateProject(ctx, projectId, updatedProject);
            return null;
        });
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> projectService.deleteProject(ctx, projectId));
    }

    /**
     * Get a Project by ID
     *
     * @param projectId
     */
    @Override
    @SecurityCheck
    public Project getProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> projectService.getProject(ctx, projectId));
    }

    /**
     * Get all Project by user with pagination. Results will be ordered based
     * on creation time DESC
     *
     * @param gatewayId
     *    The identifier for the requested gateway.
     * @param userName
     *    The identifier of the user
     * @param limit
     *    The amount results to be fetched
     * @param offset
     *    The starting point of the results to be fetched
     **/
    @Override
    @SecurityCheck
    public List<Project> getUserProjects(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> projectService.getUserProjects(ctx, gatewayId, userName, limit, offset));
    }

    /**
     *
     *  Search User Projects
     *  Search and get all Projects for user by project description or/and project name  with pagination.
     *  Results will be ordered based on creation time DESC.
     *
     *  @param gatewayId
     *     The unique identifier of the gateway making the request.
     *
     *  @param userName
     *     The identifier of the user.
     *
     *  @param filters
     *     Map of multiple filter criteria. Currenlt search filters includes Project Name and Project Description
     *
     *  @param limit
     *     The amount results to be fetched.
     *
     *  @param offset
     *     The starting point of the results to be fetched.
     *
     */
    @Override
    @SecurityCheck
    public List<Project> searchProjects(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> projectService.searchProjects(ctx, gatewayId, userName, filters, limit, offset));
    }

    /**
     * Search Experiments by using multiple filter criteria with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param filters
     *       map of multiple filter criteria.
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperiments(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> experimentService.searchExperiments(ctx, gatewayId, userName, filters, limit, offset));
    }

    /**
     * Get Experiment execution statisitics by sending the gateway id and the time period interested in.
     * This method will retrun an ExperimentStatistics object which contains the number of successfully
     * completed experiments, failed experiments etc.
     * @param gatewayId
     * @param fromTime
     * @param toTime
     * @return
     * @throws InvalidRequestException
     * @throws AiravataClientException
     * @throws AiravataSystemException
     * @throws TException
     */
    @Override
    @SecurityCheck
    public ExperimentStatistics getExperimentStatistics(
            AuthzToken authzToken,
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            int limit,
            int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> experimentService.getExperimentStatistics(
                        ctx, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName, limit, offset));
    }

    /**
     * Get Experiments within project with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param projectId
     *       Identifier of the project
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentModel> getExperimentsInProject(AuthzToken authzToken, String projectId, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getExperimentsInProject(ctx, projectId, limit, offset));
    }

    /**
     * Get Experiments by user pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requesting gateway
     * @param userName
     *       Username of the requested user
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentModel> getUserExperiments(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> experimentService.getUserExperiments(ctx, gatewayId, userName, limit, offset));
    }

    /**
     * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * but inferred from the authentication header. This experiment is just a persistent place holder. The client
     * has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * registering the experiment in a persistent store.
     *
     * @param experiment@return The server-side generated.airavata.registry.core.experiment.globally unique identifier.
     * @throws org.apache.airavata.model.error.InvalidRequestException For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.AiravataClientException The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                               <p/>
     *                                                               UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                               step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                               gateway registration steps and retry this request.
     *                                                               <p/>
     *                                                               AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                               For now this is a place holder.
     *                                                               <p/>
     *                                                               INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                               is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                               rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, gatewayId, ctx -> experimentService.createExperiment(ctx, experiment));
    }

    /**
     * If the experiment is not already launched experiment can be deleted.
     * @param authzToken
     * @param experimentId
     * @return
     * @throws InvalidRequestException
     * @throws AiravataClientException
     * @throws AiravataSystemException
     * @throws AuthorizationException
     * @throws TException
     */
    @Override
    @SecurityCheck
    public boolean deleteExperiment(AuthzToken authzToken, String experimentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> experimentService.deleteExperiment(ctx, experimentId));
    }

    /**
     * Fetch previously created experiment metadata.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getExperiment(ctx, airavataExperimentId));
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getExperimentByAdmin(ctx, airavataExperimentId));
    }

    /**
     * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
     * tasks -> jobs information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentModel getDetailedExperimentTree(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getDetailedExperimentTree(ctx, airavataExperimentId));
    }

    /**
     * Configure a previously created experiment with required inputs, scheduling and other quality of service
     * parameters. This method only updates the experiment object within the registry. The experiment has to be launched
     * to make it actionable by the server.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experiment
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void updateExperiment(AuthzToken authzToken, String airavataExperimentId, ExperimentModel experiment)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        ThriftAdapter.executeVoid(
                authzToken, null, ctx -> experimentService.updateExperiment(ctx, airavataExperimentId, experiment));
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(
            AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        ThriftAdapter.executeVoid(
                authzToken,
                null,
                ctx -> experimentService.updateExperimentConfiguration(ctx, airavataExperimentId, userConfiguration));
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            AuthzToken authzToken, String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, TException {
        ThriftAdapter.executeVoid(
                authzToken,
                null,
                ctx -> experimentService.updateResourceScheduleing(ctx, airavataExperimentId, resourceScheduling));
    }

    /**
     * *
     * * Validate experiment configuration. A true in general indicates, the experiment is ready to be launched.
     * *
     * * @param experimentID
     * * @return sucess/failure
     * *
     * *
     *
     * @param airavataExperimentId
     */
    @Override
    @SecurityCheck
    public boolean validateExperiment(AuthzToken authzToken, String airavataExperimentId) throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.validateExperiment(ctx, airavataExperimentId));
    }

    /**
     * Fetch the previously configured experiment configuration information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method returns the previously configured experiment configuration data.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *<p/>
     *UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *gateway registration steps and retry this request.
     *<p/>
     *AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *For now this is a place holder.
     *<p/>
     *INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any
     *          Airavata Server side issues and if the problem cannot be corrected by the client
     *         rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId) throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getExperimentStatus(ctx, airavataExperimentId));
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getExperimentOutputs(ctx, airavataExperimentId));
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return null;
    }

    @Override
    @SecurityCheck
    public void fetchIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        ThriftAdapter.executeVoid(
                authzToken,
                null,
                ctx -> experimentService.fetchIntermediateOutputs(ctx, airavataExperimentId, outputNames));
    }

    @Override
    @SecurityCheck
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> experimentService.getIntermediateOutputProcessStatus(ctx, airavataExperimentId, outputNames));
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getJobStatuses(ctx, airavataExperimentId));
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> experimentService.getJobDetails(ctx, airavataExperimentId));
    }

    /**
     * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
     * notifications and intermediate and output data will be subsequently available for this experiment.
     *
     *
     * @param airavataExperimentId   The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.model.error.InvalidRequestException
     *          For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     *          If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException
     *          The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *          <p/>
     *          UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *          step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *          gateway registration steps and retry this request.
     *          <p/>
     *          AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *          For now this is a place holder.
     *          <p/>
     *          INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *          is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException
     *          This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *          rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void launchExperiment(AuthzToken authzToken, final String airavataExperimentId, String gatewayId)
            throws AuthorizationException, AiravataSystemException, TException {
        logger.info("Launching experiment {}", airavataExperimentId);
        ThriftAdapter.executeVoid(
                authzToken, gatewayId, ctx -> experimentService.launchExperiment(ctx, airavataExperimentId, gatewayId));
    }

    //    private OrchestratorService.Client getOrchestratorClient() throws TException {
    //	    try {
    //		    final String serverHost = ServerSettings.getOrchestratorServerHost();
    //		    final int serverPort = ServerSettings.getOrchestratorServerPort();
    //		    return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
    //	    } catch (AiravataException e) {
    //		    throw new TException(e);
    //	    }
    //    }

    /**
     * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
     *   The client has to subsequently update this configuration if needed and launch the cloned experiment.
     *
     * @param existingExperimentID
     *    This is the experiment identifier that already exists in the system. Will use this experimentID to retrieve
     *    user configuration which is used with the clone experiment.
     *
     * @param newExperimentName
     *   experiment name that should be used in the cloned experiment
     *
     * @return
     *   The server-side generated.airavata.registry.core.experiment.globally unique identifier for the newly cloned experiment.
     *
     * @throws org.apache.airavata.model.error.InvalidRequestException
     *    For any incorrect forming of the request itself.
     *
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     *
     * @throws org.apache.airavata.model.error.AiravataClientException
     *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *
     *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *         gateway registration steps and retry this request.
     *
     *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *         For now this is a place holder.
     *
     *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *         is implemented, the authorization will be more substantial.
     *
     * @throws org.apache.airavata.model.error.AiravataSystemException
     *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *       rather an Airavata Administrator will be notified to take corrective action.
     *
     *
     * @param existingExperimentID
     * @param newExperimentName
     */
    @Override
    @SecurityCheck
    public String cloneExperiment(
            AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException, TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> experimentService.cloneExperiment(
                        ctx, existingExperimentID, newExperimentName, newExperimentProjectId, false));
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(
            AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException, TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> experimentService.cloneExperiment(
                        ctx, existingExperimentID, newExperimentName, newExperimentProjectId, true));
    }

    /**
     * Terminate a running experiment.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void terminateExperiment(AuthzToken authzToken, String airavataExperimentId, String gatewayId)
            throws TException {
        ThriftAdapter.executeVoid(
                authzToken, gatewayId, ctx -> experimentService.terminateExperiment(ctx, airavataExperimentId));
    }

    /**
     * Register a Application Module.
     *
     * @param applicationModule Application Module Object created from the datamodel.
     * @return appModuleId
     * Returns a server-side generated airavata appModule globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationModule(
            AuthzToken authzToken, String gatewayId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.registerApplicationModule(ctx, gatewayId, applicationModule));
    }

    /**
     * Fetch a Application Module.
     *
     * @param appModuleId The identifier for the requested application module
     * @return applicationModule
     * Returns a application Module Object.
     */
    @Override
    @SecurityCheck
    public ApplicationModule getApplicationModule(AuthzToken authzToken, String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, appModuleId, ctx -> applicationCatalogService.getApplicationModule(ctx, appModuleId));
    }

    /**
     * Update a Application Module.
     *
     * @param appModuleId       The identifier for the requested application module to be updated.
     * @param applicationModule Application Module Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateApplicationModule(
            AuthzToken authzToken, String appModuleId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appModuleId,
                ctx -> applicationCatalogService.updateApplicationModule(ctx, appModuleId, applicationModule));
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @return list applicationModule.
     * Returns the list of all Application Module Objects.
     */
    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> applicationCatalogService.getAllAppModules(ctx, gatewayId));
    }

    /**
     * Fetch all accessible Application Module Descriptions.
     *
     * @return list applicationModule.
     * Returns the list of Application Module Objects that are accessible to the user.
     */
    @Override
    @SecurityCheck
    public List<ApplicationModule> getAccessibleAppModules(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> applicationCatalogService.getAccessibleAppModules(ctx, gatewayId));
    }

    /**
     * Delete a Application Module.
     *
     * @param appModuleId The identifier for the requested application module to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationModule(AuthzToken authzToken, String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, appModuleId, ctx -> applicationCatalogService.deleteApplicationModule(ctx, appModuleId));
    }

    /**
     * Register a Application Deployment.
     *
     * @param applicationDeployment@return appModuleId
     *                                     Returns a server-side generated airavata appModule globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationDeployment(
            AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.registerApplicationDeployment(ctx, gatewayId, applicationDeployment));
    }

    /**
     * Fetch a Application Deployment.
     *
     * @param appDeploymentId The identifier for the requested application module
     * @return applicationDeployment
     * Returns a application Deployment Object.
     */
    @Override
    @SecurityCheck
    public ApplicationDeploymentDescription getApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appDeploymentId,
                ctx -> applicationCatalogService.getApplicationDeployment(ctx, appDeploymentId));
    }

    /**
     * Update a Application Deployment.
     *
     * @param appDeploymentId       The identifier for the requested application deployment to be updated.
     * @param applicationDeployment
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateApplicationDeployment(
            AuthzToken authzToken, String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appDeploymentId,
                ctx -> applicationCatalogService.updateApplicationDeployment(
                        ctx, appDeploymentId, applicationDeployment));
    }

    /**
     * Delete a Application deployment.
     *
     * @param appDeploymentId The identifier for the requested application deployment to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appDeploymentId,
                ctx -> applicationCatalogService.deleteApplicationDeployment(ctx, appDeploymentId));
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @return list applicationDeployment.
     * Returns the list of all Application Deployment Objects.
     */
    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> applicationCatalogService.getAllApplicationDeployments(ctx, gatewayId));
    }

    /**
     * Fetch all accessible Application Deployment Descriptions.
     *
     * @return list applicationDeployment.
     * Returns the list of Application Deployment Objects that are accessible to the user.
     */
    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            AuthzToken authzToken, String gatewayId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.getAccessibleApplicationDeployments(ctx, gatewayId, permissionType));
    }

    /**
     * Fetch a list of Deployed Compute Hosts.
     *
     * @param appModuleId The identifier for the requested application module
     * @return list<string>
     * Returns a list of Deployed Resources.
     */
    @Override
    @SecurityCheck
    @Deprecated
    public List<String> getAppModuleDeployedResources(AuthzToken authzToken, String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appModuleId,
                ctx -> applicationCatalogService.getAppModuleDeployedResources(ctx, appModuleId));
    }

    /**
     * Fetch a list of Application Deployments that this user can use for executing the given Application Module using the given Group Resource Profile.
     * The user must have at least READ access to the Group Resource Profile.
     *
     * @param appModuleId
     *    The identifier for the Application Module
     *
     * @param groupResourceProfileId
     *    The identifier for the Group Resource Profile
     *
     * @return list<ApplicationDeploymentDescription>
     *    Returns a list of Application Deployments
     */
    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
            AuthzToken authzToken, String appModuleId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appModuleId,
                ctx -> applicationCatalogService.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                        ctx, appModuleId, groupResourceProfileId));
    }

    /**
     * Register a Application Interface.
     *
     * @param applicationInterface@return appInterfaceId
     *                                    Returns a server-side generated airavata application interface globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationInterface(
            AuthzToken authzToken, String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.registerApplicationInterface(ctx, gatewayId, applicationInterface));
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.cloneApplicationInterface(
                        ctx, existingAppInterfaceID, newApplicationName, gatewayId));
    }

    /**
     * Fetch a Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application module
     * @return applicationInterface
     * Returns a application Interface Object.
     */
    @Override
    @SecurityCheck
    public ApplicationInterfaceDescription getApplicationInterface(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appInterfaceId,
                ctx -> applicationCatalogService.getApplicationInterface(ctx, appInterfaceId));
    }

    /**
     * Update a Application Interface.
     *
     * @param appInterfaceId       The identifier for the requested application deployment to be updated.
     * @param applicationInterface
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateApplicationInterface(
            AuthzToken authzToken, String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appInterfaceId,
                ctx -> applicationCatalogService.updateApplicationInterface(ctx, appInterfaceId, applicationInterface));
    }

    /**
     * Delete a Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationInterface(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appInterfaceId,
                ctx -> applicationCatalogService.deleteApplicationInterface(ctx, appInterfaceId));
    }

    /**
     * Fetch name and id of  Application Interface documents.
     *
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing id's
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> applicationCatalogService.getAllApplicationInterfaceNames(ctx, gatewayId));
    }

    /**
     * Fetch all Application Interface documents.
     *
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces documents
     */
    @Override
    @SecurityCheck
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> applicationCatalogService.getAllApplicationInterfaces(ctx, gatewayId));
    }

    /**
     * Fetch the list of Application Inputs.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return list<applicationInterfaceModel.InputDataObjectType>
     * Returns a list of application inputs.
     */
    @Override
    @SecurityCheck
    public List<InputDataObjectType> getApplicationInputs(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, appInterfaceId, ctx -> applicationCatalogService.getApplicationInputs(ctx, appInterfaceId));
    }

    /**
     * Fetch the list of Application Outputs.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return list<applicationInterfaceModel.OutputDataObjectType>
     * Returns a list of application outputs.
     */
    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getApplicationOutputs(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appInterfaceId,
                ctx -> applicationCatalogService.getApplicationOutputs(ctx, appInterfaceId));
    }

    /**
     * Fetch a list of all deployed Compute Hosts for a given application interfaces.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return map<computeResourceId, computeResourceName>
     * A map of registered compute resource id's and their corresponding hostnames.
     * Deployments of each modules listed within the interfaces will be listed.
     */
    @Override
    @SecurityCheck
    @Deprecated
    public Map<String, String> getAvailableAppInterfaceComputeResources(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                appInterfaceId,
                ctx -> applicationCatalogService.getAvailableAppInterfaceComputeResources(ctx, appInterfaceId));
    }

    /**
     * Register a Compute Resource.
     *
     * @param computeResourceDescription Compute Resource Object created from the datamodel.
     * @return computeResourceId
     * Returns a server-side generated airavata compute resource globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerComputeResource(AuthzToken authzToken, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.registerComputeResource(computeResourceDescription));
    }

    /**
     * Fetch the given Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource
     * @return computeResourceDescription
     * Compute Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public ComputeResourceDescription getComputeResource(AuthzToken authzToken, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getComputeResource(computeResourceId));
    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllComputeResourceNames(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getAllComputeResourceNames());
    }

    /**
     * Update a Compute Resource.
     *
     * @param computeResourceId          The identifier for the requested compute resource to be updated.
     * @param computeResourceDescription Compute Resource Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateComputeResource(
            AuthzToken authzToken, String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateComputeResource(computeResourceId, computeResourceDescription));
    }

    /**
     * Delete a Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteComputeResource(AuthzToken authzToken, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.deleteComputeResource(computeResourceId));
    }

    /**
     * Register a Storage Resource.
     *
     * @param authzToken
     * @param storageResourceDescription Storge Resource Object created from the datamodel.
     * @return storageResourceId
     * Returns a server-side generated airavata storage resource globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerStorageResource(AuthzToken authzToken, StorageResourceDescription storageResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.registerStorageResource(storageResourceDescription));
    }

    /**
     * Fetch the given Storage Resource.
     *
     * @param authzToken
     * @param storageResourceId The identifier for the requested storage resource
     * @return storageResourceDescription
     * Storage Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public StorageResourceDescription getStorageResource(AuthzToken authzToken, String storageResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getStorageResource(storageResourceId));
    }

    /**
     * Fetch all registered Storage Resources.
     *
     * @param authzToken
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllStorageResourceNames(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getAllStorageResourceNames());
    }

    /**
     * Update a Compute Resource.
     *
     * @param authzToken
     * @param storageResourceId          The identifier for the requested compute resource to be updated.
     * @param storageResourceDescription Storage Resource Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateStorageResource(
            AuthzToken authzToken, String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateStorageResource(storageResourceId, storageResourceDescription));
    }

    /**
     * Delete a Storage Resource.
     *
     * @param authzToken
     * @param storageResourceId The identifier for the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteStorageResource(AuthzToken authzToken, String storageResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.deleteStorageResource(storageResourceId));
    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.getResourceStorageInfo(ctx, resourceId, location));
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(AuthzToken authzToken, String resourceId, String location)
            throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.getStorageDirectoryInfo(ctx, resourceId, location));
    }

    /**
     * Add a Local Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localSubmission   The LOCALSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addLocalSubmissionDetails(
            AuthzToken authzToken, String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission));
    }

    /**
     * Update the given Local Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param localSubmission          The LOCALSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean updateLocalSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission));
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getLocalJobSubmission(jobSubmissionId));
    }

    /**
     * Add a SSH Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param sshJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSSHJobSubmissionDetails(
            AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission));
    }

    /**
     * Add a SSH_FORK Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param sshJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSSHForkJobSubmissionDetails(
            AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addSSHForkJobSubmissionDetails(
                        computeResourceId, priorityOrder, sshJobSubmission));
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getSSHJobSubmission(jobSubmissionId));
    }

    /**
     * Add a Cloud Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param cloudJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addCloudJobSubmissionDetails(
            AuthzToken authzToken, String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addCloudJobSubmissionDetails(
                        computeResourceId, priorityOrder, cloudJobSubmission));
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getCloudJobSubmission(jobSubmissionId));
    }

    @Override
    @SecurityCheck
    public String addUNICOREJobSubmissionDetails(
            AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addUNICOREJobSubmissionDetails(
                        computeResourceId, priorityOrder, unicoreJobSubmission));
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getUnicoreJobSubmission(jobSubmissionId));
    }

    /**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean updateSSHJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission));
    }

    /**
     * Update the given cloud Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param cloudJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean updateCloudJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission));
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateUnicoreJobSubmissionDetails(
                        jobSubmissionInterfaceId, unicoreJobSubmission));
    }

    /**
     * Add a Local data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param resourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localDataMovement The LOCALDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addLocalDataMovementDetails(
            AuthzToken authzToken,
            String resourceId,
            DMType dmType,
            int priorityOrder,
            LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addLocalDataMovementDetails(
                        resourceId, dmType, priorityOrder, localDataMovement));
    }

    /**
     * Update the given Local data movement details
     *
     * @param dataMovementInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param localDataMovement        The LOCALDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateLocalDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement));
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getLocalDataMovement(dataMovementId));
    }

    /**
     * Add a SCP data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param resourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param scpDataMovement   The SCPDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSCPDataMovementDetails(
            AuthzToken authzToken, String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement));
    }

    /**
     * Update the given scp data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param scpDataMovement          The SCPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateSCPDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement));
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getSCPDataMovement(dataMovementId));
    }

    @Override
    @SecurityCheck
    public String addUnicoreDataMovementDetails(
            AuthzToken authzToken,
            String resourceId,
            DMType dmType,
            int priorityOrder,
            UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addUnicoreDataMovementDetails(
                        resourceId, dmType, priorityOrder, unicoreDataMovement));
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement));
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getUnicoreDataMovement(dataMovementId));
    }

    /**
     * Add a GridFTP data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param computeResourceId   The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param gridFTPDataMovement The GridFTPDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addGridFTPDataMovementDetails(
            AuthzToken authzToken,
            String computeResourceId,
            DMType dmType,
            int priorityOrder,
            GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.addGridFTPDataMovementDetails(
                        computeResourceId, dmType, priorityOrder, gridFTPDataMovement));
    }

    /**
     * Update the given GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param gridFTPDataMovement      The GridFTPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    @SecurityCheck
    public boolean updateGridFTPDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement));
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> resourceService.getGridFTPDataMovement(dataMovementId));
    }

    /**
     * Change the priority of a given job submisison interface
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @param newPriorityOrder
     * @return status
     * Returns a success/failure of the change.
     */
    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriority(
            AuthzToken authzToken, String jobSubmissionInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    /**
     * Change the priority of a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @param newPriorityOrder
     * @return status
     * Returns a success/failure of the change.
     */
    @Override
    @SecurityCheck
    public boolean changeDataMovementPriority(
            AuthzToken authzToken, String dataMovementInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    /**
     * Change the priorities of a given set of job submission interfaces
     *
     * @param jobSubmissionPriorityMap A Map of identifiers of the JobSubmission Interfaces and thier associated priorities to be set.
     * @return status
     * Returns a success/failure of the changes.
     */
    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriorities(AuthzToken authzToken, Map<String, Integer> jobSubmissionPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    /**
     * Change the priorities of a given set of data movement interfaces
     *
     * @param dataMovementPriorityMap A Map of identifiers of the DataMovement Interfaces and thier associated priorities to be set.
     * @return status
     * Returns a success/failure of the changes.
     */
    @Override
    @SecurityCheck
    public boolean changeDataMovementPriorities(AuthzToken authzToken, Map<String, Integer> dataMovementPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    /**
     * Delete a given job submisison interface
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteJobSubmissionInterface(
            AuthzToken authzToken, String computeResourceId, String jobSubmissionInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId));
    }

    /**
     * Delete a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteDataMovementInterface(
            AuthzToken authzToken, String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType));
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.registerResourceJobManager(resourceJobManager));
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(
            AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager));
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.getResourceJobManager(resourceJobManagerId));
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.deleteResourceJobManager(resourceJobManagerId));
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceService.deleteBatchQueue(computeResourceId, queueName));
    }

    /**
     * Register a Gateway Resource Profile.
     *
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     *   The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
     *      resource profile.
     * @return status.
     * Returns a success/failure of the registration.
     */
    @Override
    @SecurityCheck
    public String registerGatewayResourceProfile(AuthzToken authzToken, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.registerGatewayResourceProfile(ctx, gatewayResourceProfile));
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource
     * @return gatewayResourceProfile
     * Gateway Resource Profile Object.
     */
    @Override
    @SecurityCheck
    public GatewayResourceProfile getGatewayResourceProfile(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayResourceProfileService.getGatewayResourceProfile(ctx, gatewayID));
    }

    /**
     * Update a Gateway Resource Profile.
     *
     * @param gatewayID              The identifier for the requested gateway resource to be updated.
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateGatewayResourceProfile(
            AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile) throws TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.updateGatewayResourceProfile(
                        ctx, gatewayID, gatewayResourceProfile));
    }

    /**
     * Delete the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayResourceProfileService.deleteGatewayResourceProfile(ctx, gatewayID));
    }

    /**
     * Add a Compute Resource Preference to a registered gateway profile.
     *
     * @param gatewayID                 The identifier for the gateway profile to be added.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param computeResourcePreference The ComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    @SecurityCheck
    public boolean addGatewayComputeResourcePreference(
            AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.addGatewayComputeResourcePreference(
                        ctx, gatewayID, computeResourceId, computeResourcePreference));
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.addGatewayStoragePreference(
                        ctx, gatewayID, storageResourceId, dataStoragePreference));
    }

    /**
     * Fetch a Compute Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier for the gateway profile to be requested
     * @param computeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    @SecurityCheck
    public ComputeResourcePreference getGatewayComputeResourcePreference(
            AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.getGatewayComputeResourcePreference(
                        ctx, gatewayID, computeResourceId));
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.getGatewayStoragePreference(ctx, gatewayID, storageId));
    }

    /**
     * Fetch all Compute Resource Preferences of a registered gateway profile.
     *
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    @SecurityCheck
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(
            AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.getAllGatewayComputeResourcePreferences(ctx, gatewayID));
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayResourceProfileService.getAllGatewayStoragePreferences(ctx, gatewayID));
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayResourceProfileService.getAllGatewayResourceProfiles(ctx));
    }

    /**
     * Update a Compute Resource Preference to a registered gateway profile.
     *
     * @param gatewayID                 The identifier for the gateway profile to be updated.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param computeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    @SecurityCheck
    public boolean updateGatewayComputeResourcePreference(
            AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.updateGatewayComputeResourcePreference(
                        ctx, gatewayID, computeResourceId, computeResourcePreference));
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.updateGatewayStoragePreference(
                        ctx, gatewayID, storageId, dataStoragePreference));
    }

    /**
     * Delete the Compute Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier for the gateway profile to be deleted.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteGatewayComputeResourcePreference(
            AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.deleteGatewayComputeResourcePreference(
                        ctx, gatewayID, computeResourceId));
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> gatewayResourceProfileService.deleteGatewayStoragePreference(ctx, gatewayID, storageId));
    }

    @Override
    @SecurityCheck
    public List<SSHAccountProvisioner> getSSHAccountProvisioners(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> gatewayResourceProfileService.getSSHAccountProvisioners(ctx));
    }

    @Override
    @SecurityCheck
    public boolean doesUserHaveSSHAccount(AuthzToken authzToken, String computeResourceId, String userId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> sshAccountService.doesUserHaveSSHAccount(ctx, computeResourceId, userId));
    }

    @Override
    @SecurityCheck
    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(
                        ctx, computeResourceId, airavataCredStoreToken));
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> sshAccountService.setupUserComputeResourcePreferencesForSSH(
                        ctx, computeResourceId, userId, airavataCredStoreToken));
    }

    /**
     * Register a User Resource Profile.
     *
     * @param userResourceProfile User Resource Profile Object.
     *   The userId should be obtained from Airavata user profile registration and passed to register a corresponding
     *      resource profile.
     * @return status.
     * Returns a success/failure of the registration.
     */
    @Override
    @SecurityCheck
    public String registerUserResourceProfile(AuthzToken authzToken, UserResourceProfile userResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.registerUserResourceProfile(ctx, userResourceProfile));
    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.isUserResourceProfileExists(ctx, userId, gatewayID));
    }

    /**
     * Fetch the given User Resource Profile.
     *
     * @param userId The identifier for the requested User resource
     *
     * @param gatewayID The identifier to link a gateway for the requested User resource
     *
     * @return userResourceProfile
     * User Resource Profile Object.
     */
    @Override
    @SecurityCheck
    public UserResourceProfile getUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> userResourceProfileService.getUserResourceProfile(ctx, userId, gatewayID));
    }

    /**
     * Update a User Resource Profile.
     *
     * @param userId : The identifier for the requested user resource profile to be updated.
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @param userResourceProfile User Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateUserResourceProfile(
            AuthzToken authzToken, String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.updateUserResourceProfile(
                        ctx, userId, gatewayID, userResourceProfile));
    }

    /**
     * Delete the given User Resource Profile.
     *
     * @param userId  : The identifier for the requested userId resource to be deleted.
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID) throws TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> userResourceProfileService.deleteUserResourceProfile(ctx, userId, gatewayID));
    }

    /**
     * Add a Compute Resource Preference to a registered User Resource profile.
     *
     * @param userId                 The identifier for the User Resource profile to be added.
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @param userComputeResourceId         Preferences related to a particular compute resource
     * @param userComputeResourcePreference The ComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    @SecurityCheck
    public boolean addUserComputeResourcePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.addUserComputeResourcePreference(
                        ctx, userId, gatewayID, userComputeResourceId, userComputeResourcePreference));
    }

    @Override
    @SecurityCheck
    public boolean addUserStoragePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageResourceId,
            UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.addUserStoragePreference(
                        ctx, userId, gatewayID, userStorageResourceId, dataStoragePreference));
    }

    /**
     * Fetch a Compute Resource Preference of a registered User Resource profile.
     *
     * @param userId : The identifier for the User Resource profile to be requested
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @param userComputeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    @SecurityCheck
    public UserComputeResourcePreference getUserComputeResourcePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.getUserComputeResourcePreference(
                        ctx, userId, gatewayID, userComputeResourceId));
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.getUserStoragePreference(ctx, userId, gatewayID, userStorageId));
    }

    /**
     * Fetch all User Compute Resource Preferences of a registered gateway profile.
     *
     * @param userId
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    @SecurityCheck
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.getAllUserComputeResourcePreferences(ctx, userId, gatewayID));
    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.getAllUserStoragePreferences(ctx, userId, gatewayID));
    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> userResourceProfileService.getAllUserResourceProfiles(ctx));
    }

    /**
     * Update a Compute Resource Preference to a registered User Resource profile.
     *
     * @param userId : The identifier for the User Resource profile to be updated.
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @param userComputeResourceId         Preferences related to a particular compute resource
     * @param userComputeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    @SecurityCheck
    public boolean updateUserComputeResourcePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.updateUserComputeResourcePreference(
                        ctx, userId, gatewayID, userComputeResourceId, userComputeResourcePreference));
    }

    @Override
    @SecurityCheck
    public boolean updateUserStoragePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId,
            UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.updateUserStoragePreference(
                        ctx, userId, gatewayID, userStorageId, dataStoragePreference));
    }

    /**
     * Delete the Compute Resource Preference of a registered User Resource profile.
     *
     * @param userId         The identifier for the User profile to be deleted.
     * @param gatewayID The identifier to link a gateway for the requested User resource
     * @param userComputeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteUserComputeResourcePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.deleteUserComputeResourcePreference(
                        ctx, userId, gatewayID, userComputeResourceId));
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> userResourceProfileService.deleteUserStoragePreference(ctx, userId, gatewayID, userStorageId));
    }

    @Override
    @SecurityCheck
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> userResourceProfileService.getLatestQueueStatuses(ctx));
    }

    /**
     * ReplicaCatalog Related Methods
     * @return
     * @throws TException
     * @throws ApplicationSettingsException
     */
    @Override
    @SecurityCheck
    public String registerDataProduct(AuthzToken authzToken, DataProductModel dataProductModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> dataProductService.registerDataProduct(ctx, dataProductModel));
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> dataProductService.getDataProduct(ctx, productUri));
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> dataProductService.registerReplicaLocation(ctx, replicaLocationModel));
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> dataProductService.getParentDataProduct(ctx, productUri));
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> dataProductService.getChildDataProducts(ctx, productUri));
    }

    /**
     * Group Manager and Data Sharing Related API methods
     *
     * @param authzToken
     * @param resourceId
     * @param userPermissionList
     */
    @Override
    @SecurityCheck
    public boolean shareResourceWithUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.shareResourceWithUsers(ctx, resourceId, userPermissionList));
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.shareResourceWithGroups(ctx, resourceId, groupPermissionList));
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.revokeSharingOfResourceFromUsers(ctx, resourceId, userPermissionList));
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.revokeSharingOfResourceFromGroups(ctx, resourceId, groupPermissionList));
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceSharingService.getAllAccessibleUsers(ctx, resourceId, permissionType));
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.getAllDirectlyAccessibleUsers(ctx, resourceId, permissionType));
    }

    private List<String> getAllAccessibleUsersInternal(
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryServerHandler, ResourcePermissionType, Collection<User>> userListFunction)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            HashSet<String> accessibleUsers = new HashSet<>();
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                userListFunction.apply(sharingHandler, ResourcePermissionType.WRITE).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
                userListFunction.apply(sharingHandler, ResourcePermissionType.OWNER).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                userListFunction.apply(sharingHandler, ResourcePermissionType.READ).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
                userListFunction.apply(sharingHandler, ResourcePermissionType.OWNER).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
            } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
                userListFunction.apply(sharingHandler, ResourcePermissionType.OWNER).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
            } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
                userListFunction.apply(sharingHandler, ResourcePermissionType.MANAGE_SHARING).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
                userListFunction.apply(sharingHandler, ResourcePermissionType.OWNER).stream()
                        .forEach(u -> accessibleUsers.add(u.getUserId()));
            }
            return new ArrayList<>(accessibleUsers);
        } catch (Exception e) {
            String msg = "Error in getting all accessible users for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.getAllAccessibleGroups(ctx, resourceId, permissionType));
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> resourceSharingService.getAllDirectlyAccessibleGroups(ctx, resourceId, permissionType));
    }

    private List<String> getAllAccessibleGroupsInternal(
            AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingRegistryServerHandler, ResourcePermissionType, Collection<UserGroup>> groupListFunction)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            HashSet<String> accessibleGroups = new HashSet<>();
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                groupListFunction.apply(sharingHandler, ResourcePermissionType.WRITE).stream()
                        .forEach(g -> accessibleGroups.add(g.getGroupId()));
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                groupListFunction.apply(sharingHandler, ResourcePermissionType.READ).stream()
                        .forEach(g -> accessibleGroups.add(g.getGroupId()));
            } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
                groupListFunction.apply(sharingHandler, ResourcePermissionType.MANAGE_SHARING).stream()
                        .forEach(g -> accessibleGroups.add(g.getGroupId()));
            }
            return new ArrayList<>(accessibleGroups);
        } catch (Exception e) {
            String msg = "Error in getting all accessible groups for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, null, ctx -> resourceSharingService.userHasAccess(ctx, resourceId, permissionType));
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.createGroupResourceProfile(ctx, groupResourceProfile));
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        ThriftAdapter.executeVoid(
                authzToken,
                null,
                ctx -> groupResourceProfileService.updateGroupResourceProfile(ctx, groupResourceProfile));
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupResourceProfile(ctx, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.removeGroupResourceProfile(ctx, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> groupResourceProfileService.getGroupResourceList(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputePrefs(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.removeGroupComputePrefs(
                        ctx, computeResourceId, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.removeGroupComputeResourcePolicy(ctx, resourcePolicyId));
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.removeGroupBatchQueueResourcePolicy(ctx, resourcePolicyId));
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupComputeResourcePreference(
                        ctx, computeResourceId, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupComputeResourcePolicy(ctx, resourcePolicyId));
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getBatchQueueResourcePolicy(ctx, resourcePolicyId));
    }

    @Override
    @SecurityCheck
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupComputeResourcePrefList(ctx, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupBatchQueueResourcePolicyList(ctx, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                null,
                ctx -> groupResourceProfileService.getGroupComputeResourcePolicyList(ctx, groupResourceProfileId));
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> groupResourceProfileService.getGatewayGroups(ctx));
    }

    @Override
    @SecurityCheck
    public Parser getParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, gatewayId, ctx -> parserService.getParser(ctx, parserId, gatewayId));
    }

    @Override
    @SecurityCheck
    public String saveParser(AuthzToken authzToken, Parser parser)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> parserService.saveParser(ctx, parser));
    }

    @Override
    @SecurityCheck
    public List<Parser> listAllParsers(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, gatewayId, ctx -> parserService.listAllParsers(ctx, gatewayId));
    }

    @Override
    @SecurityCheck
    public boolean removeParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> parserService.removeParser(ctx, parserId, gatewayId));
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> parserService.getParsingTemplate(ctx, templateId, gatewayId));
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            AuthzToken authzToken, String experimentId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken,
                gatewayId,
                ctx -> parserService.getParsingTemplatesForExperiment(ctx, experimentId, gatewayId));
    }

    @Override
    @SecurityCheck
    public String saveParsingTemplate(AuthzToken authzToken, ParsingTemplate parsingTemplate)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(authzToken, null, ctx -> parserService.saveParsingTemplate(ctx, parsingTemplate));
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> parserService.removeParsingTemplate(ctx, templateId, gatewayId));
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> listAllParsingTemplates(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return ThriftAdapter.execute(
                authzToken, gatewayId, ctx -> parserService.listAllParsingTemplates(ctx, gatewayId));
    }
}
