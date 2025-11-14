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

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavata_apiConstants;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.appcatalog.accountprovisioning.SSHAccountProvisioner;
import org.apache.airavata.model.appcatalog.accountprovisioning.SSHAccountProvisionerConfigParam;
import org.apache.airavata.model.appcatalog.accountprovisioning.SSHAccountProvisionerConfigParamType;
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
import org.apache.airavata.model.commons.airavata_commonsConstants;
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
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private Publisher statusPublisher;
    private Publisher experimentPublisher;

    private ThriftClientPool<SharingRegistryService.Client> sharingClientPool;
    private ThriftClientPool<RegistryService.Client> registryClientPool;
    private ThriftClientPool<CredentialStoreService.Client> csClientPool;
    private org.apache.airavata.service.AiravataService airavataService =
            new org.apache.airavata.service.AiravataService();

    public AiravataServerHandler() {
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);

            sharingClientPool = new ThriftClientPool<>(
                    tProtocol -> new SharingRegistryService.Client(tProtocol),
                    this.<SharingRegistryService.Client>createGenericObjectPoolConfig(),
                    ServerSettings.getSharingRegistryHost(),
                    Integer.parseInt(ServerSettings.getSharingRegistryPort()));
            registryClientPool = new ThriftClientPool<>(
                    tProtocol -> new RegistryService.Client(tProtocol),
                    this.<RegistryService.Client>createGenericObjectPoolConfig(),
                    ServerSettings.getRegistryServerHost(),
                    Integer.parseInt(ServerSettings.getRegistryServerPort()));
            csClientPool = new ThriftClientPool<>(
                    tProtocol -> new CredentialStoreService.Client(tProtocol),
                    this.<CredentialStoreService.Client>createGenericObjectPoolConfig(),
                    ServerSettings.getCredentialStoreServerHost(),
                    Integer.parseInt(ServerSettings.getCredentialStoreServerPort()));

            initSharingRegistry();
            postInitDefaultGateway();
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (TException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
    }

    private <T> GenericObjectPoolConfig<T> createGenericObjectPoolConfig() {

        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<T>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMinutes(5));
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWait(Duration.ofSeconds(3));
        return poolConfig;
    }

    /**
     * This method creates a password token for the default gateway profile. Default gateway is originally initialized
     * at the registry server but we can not add the password token at that step as the credential store is not initialized
     * before registry server.
     */
    private void postInitDefaultGateway() {

        RegistryService.Client registryClient = registryClientPool.getResource();
        try {

            GatewayResourceProfile gatewayResourceProfile =
                    registryClient.getGatewayResourceProfile(ServerSettings.getDefaultUserGateway());
            if (gatewayResourceProfile != null && gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {

                logger.debug("Starting to add the password credential for default gateway : "
                        + ServerSettings.getDefaultUserGateway());

                PasswordCredential passwordCredential = new PasswordCredential();
                passwordCredential.setPortalUserName(ServerSettings.getDefaultUser());
                passwordCredential.setGatewayId(ServerSettings.getDefaultUserGateway());
                passwordCredential.setLoginUserName(ServerSettings.getDefaultUser());
                passwordCredential.setPassword(ServerSettings.getDefaultUserPassword());
                passwordCredential.setDescription("Credentials for default gateway");

                CredentialStoreService.Client csClient = csClientPool.getResource();
                String token = null;
                try {
                    logger.info("Creating password credential for default gateway");
                    token = csClient.addPasswordCredential(passwordCredential);
                    csClientPool.returnResource(csClient);
                } catch (Exception ex) {
                    logger.error(
                            "Failed to create the password credential for the default gateway : "
                                    + ServerSettings.getDefaultUserGateway(),
                            ex);
                    if (csClient != null) {
                        csClientPool.returnBrokenResource(csClient);
                    }
                }

                if (token != null) {
                    logger.debug("Adding password credential token " + token + " to the default gateway : "
                            + ServerSettings.getDefaultUserGateway());
                    gatewayResourceProfile.setIdentityServerPwdCredToken(token);
                    gatewayResourceProfile.setIdentityServerTenant(ServerSettings.getDefaultUserGateway());
                    registryClient.updateGatewayResourceProfile(
                            ServerSettings.getDefaultUserGateway(), gatewayResourceProfile);
                }
            }

            registryClientPool.returnResource(registryClient);
        } catch (Exception e) {
            logger.error("Failed to add the password credentials for the default gateway", e);

            if (registryClient != null) {
                registryClientPool.returnBrokenResource(registryClient);
            }
        }
    }

    private void initSharingRegistry() throws ApplicationSettingsException, TException {
        SharingRegistryService.Client client = sharingClientPool.getResource();
        try {
            if (!client.isDomainExists(ServerSettings.getDefaultUserGateway())) {
                Domain domain = new Domain();
                domain.setDomainId(ServerSettings.getDefaultUserGateway());
                domain.setName(ServerSettings.getDefaultUserGateway());
                domain.setDescription("Domain entry for " + domain.getName());
                client.createDomain(domain);

                User user = new User();
                user.setDomainId(domain.getDomainId());
                user.setUserId(ServerSettings.getDefaultUser() + "@" + ServerSettings.getDefaultUserGateway());
                user.setUserName(ServerSettings.getDefaultUser());
                client.createUser(user);

                // Creating Entity Types for each domain
                EntityType entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":PROJECT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":EXPERIMENT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":FILE");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("APPLICATION-DEPLOYMENT");
                entityType.setDescription("Application Deployment entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDescription("Group Resource Profile entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDescription("Credential Store Token entity type");
                client.createEntityType(entityType);

                // Creating Permission Types for each domain
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":READ");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                client.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":WRITE");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
                client.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId() + ":MANAGE_SHARING");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                client.createPermissionType(permissionType);
            }
            sharingClientPool.returnResource(client);
        } catch (Exception ex) {
            sharingClientPool.returnBrokenResource(client);
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
        try {
            boolean isExists = airavataService.isUserExists(gatewayId, userName);
            logger.debug("Checking if the user" + userName + "exists in the gateway" + gatewayId);
            return isExists;
        } catch (Exception e) {
            logger.error("Error while verifying user", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while verifying user. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            String gatewayId = airavataService.addGatewayWithSharing(registryClient, sharingClient, gateway);
            registryClientPool.returnResource(registryClient);
            sharingClientPool.returnResource(sharingClient);
            return gatewayId;
        } catch (Exception e) {
            logger.error("Error while adding gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding gateway. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
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
        try {
            return airavataService.getAllUsersInGateway(gatewayId);
        } catch (Exception e) {
            logger.error("Error while retrieving users", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving users. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.updateGateway(gatewayId, updatedGateway);
        } catch (Exception e) {
            logger.error("Error while updating the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            Gateway result = airavataService.getGateway(gatewayId);
            logger.debug("Airavata found the gateway with " + gatewayId);
            return result;
        } catch (Exception e) {
            logger.error("Error while getting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.deleteGateway(gatewayId);
        } catch (Exception e) {
            logger.error("Error while deleting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            logger.debug("Airavata searching for all gateways");
            return airavataService.getAllGateways();
        } catch (Exception e) {
            logger.error("Error while getting all the gateways", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            logger.debug("Airavata verifying if the gateway with " + gatewayId + "exits");
            return airavataService.isGatewayExist(gatewayId);
        } catch (Exception e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            return airavataService.createNotification(notification);
        } catch (Exception e) {
            logger.error("Error while creating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.updateNotification(notification);
        } catch (Exception e) {
            logger.error("Error while updating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.deleteNotification(gatewayId, notificationId);
        } catch (Exception e) {
            logger.error("Error while deleting notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.getNotification(gatewayId, notificationId);
        } catch (Exception e) {
            logger.error("Error while retrieving notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retreiving notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.getAllNotifications(gatewayId);
        } catch (Exception e) {
            logger.error("Error while getting all notifications", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all notifications. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String key = airavataService.generateAndRegisterSSHKeys(
                    csClient, sharingClient, gatewayId, userName, description);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            registryClientPool.returnResource(regClient);
            return key;
        } catch (Exception e) {
            logger.error("Error occurred while registering SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering SSH Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
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
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String key = airavataService.registerPwdCredential(
                    csClient, sharingClient, gatewayId, userName, loginUserName, password, description);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            return key;
        } catch (org.apache.airavata.credential.store.exception.CredentialStoreException e) {
            logger.error("Error occurred while registering PWD Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering PWD Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error occurred while registering PWD Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering PWD Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public CredentialSummary getCredentialSummary(AuthzToken authzToken, String tokenId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            CredentialSummary credentialSummary = airavataService.getCredentialSummaryWithAuth(
                    csClient, sharingClient, authzToken, tokenId, gatewayId);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            return credentialSummary;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have permission")) {
                String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                logger.info("User " + userName + " not allowed to access credential store token " + tokenId);
                csClientPool.returnResource(csClient);
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException("User does not have permission to access this resource");
            }
            String msg = "Error retrieving credential summary for token " + tokenId + ". GatewayId: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaries(AuthzToken authzToken, SummaryType type)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            List<CredentialSummary> credentialSummaries = airavataService.getAllCredentialSummariesWithAuth(
                    csClient, sharingClient, authzToken, type, gatewayId, userName);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            return credentialSummaries;
        } catch (org.apache.airavata.credential.store.exception.CredentialStoreException e) {
            String msg = "Error retrieving credential summaries of type " + type + ". GatewayId: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving credential summaries of type " + type + ". GatewayId: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            boolean result = airavataService.deleteSSHPubKey(
                    csClient, sharingClient, authzToken, airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have permission")) {
                String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                logger.info("User " + userName + " not allowed to delete (no WRITE permission) credential store token "
                        + airavataCredStoreToken);
                csClientPool.returnResource(csClient);
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.error("Error occurred while deleting SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting SSH credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            boolean result = airavataService.deletePWDCredentialWithAuth(
                    csClient, sharingClient, authzToken, airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have permission")) {
                String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                logger.info("User " + userName + " not allowed to delete (no WRITE permission) credential store token "
                        + airavataCredStoreToken);
                csClientPool.returnResource(csClient);
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException("User does not have permission to delete this resource.");
            }
            logger.error("Error occurred while deleting PWD credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting PWD credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        // TODO: verify that gatewayId and project.gatewayId match authzToken
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String projectId = airavataService.createProjectWithSharing(sharingClient, gatewayId, project);
            sharingClientPool.returnResource(sharingClient);
            return projectId;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            airavataService.updateProjectWithAuth(sharingClient, authzToken, projectId, updatedProject);
            sharingClientPool.returnResource(sharingClient);
        } catch (Exception e) {
            if (e.getMessage() != null
                    && (e.getMessage().contains("does not have permission")
                            || e.getMessage().contains("cannot be changed"))) {
                if (e.getMessage().contains("cannot be changed")) {
                    sharingClientPool.returnResource(sharingClient);
                    throw new InvalidRequestException(e.getMessage());
                } else {
                    sharingClientPool.returnResource(sharingClient);
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            logger.error("Error while updating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            boolean ret = airavataService.deleteProjectWithAuth(sharingClient, authzToken, projectId);
            sharingClientPool.returnResource(sharingClient);
            return ret;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have permission")) {
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException("User does not have permission to access this resource");
            }
            logger.error("Error while removing the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while removing the project. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    private boolean validateString(String name) {
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0) {
            valid = false;
        }
        return valid;
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project project = airavataService.getProjectWithAuth(sharingClient, authzToken, projectId);
            sharingClientPool.returnResource(sharingClient);
            return project;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have permission")) {
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException("User does not have permission to access this resource");
            }
            logger.error("Error while retrieving the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<Project> result = airavataService.getUserProjectsWithSharing(
                    sharingClient, authzToken, gatewayId, userName, limit, offset);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleProjIds = new ArrayList<>();

            List<Project> result;
            if (ServerSettings.isEnableSharing()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                sharingFilters.add(searchCriteria);
                sharingClient
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
                    result = airavataService.searchProjects(
                            gatewayId, userName, accessibleProjIds, filters, limit, offset);
                }
            } else {
                result = airavataService.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
            }
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<ExperimentSummaryModel> result = airavataService.searchExperimentsWithSharing(
                    sharingClient, authzToken, gatewayId, userName, filters, limit, offset);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        try {
            // FIXME: re-enable experiment statistics for non-admin users
            // Find accessible experiments in date range
            // List<String> accessibleExpIds = new ArrayList<>();
            // List<SearchCriteria> sharingFilters = new ArrayList<>();
            // SearchCriteria entityTypeCriteria = new SearchCriteria();
            // entityTypeCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            // entityTypeCriteria.setSearchCondition(SearchCondition.EQUAL);
            // entityTypeCriteria.setValue(gatewayId + ":EXPERIMENT");
            // sharingFilters.add(entityTypeCriteria);
            // SearchCriteria fromCreatedTimeCriteria = new SearchCriteria();
            // fromCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            // fromCreatedTimeCriteria.setSearchCondition(SearchCondition.GTE);
            // fromCreatedTimeCriteria.setValue(Long.toString(fromTime));
            // sharingFilters.add(fromCreatedTimeCriteria);
            // SearchCriteria toCreatedTimeCriteria = new SearchCriteria();
            // toCreatedTimeCriteria.setSearchField(EntitySearchField.CREATED_TIME);
            // toCreatedTimeCriteria.setSearchCondition(SearchCondition.LTE);
            // toCreatedTimeCriteria.setValue(Long.toString(toTime));
            // sharingFilters.add(toCreatedTimeCriteria);
            // String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            // sharingClient.searchEntities(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
            //         userId + "@" + gatewayId, sharingFilters, 0, Integer.MAX_VALUE).forEach(e ->
            // accessibleExpIds.add(e.getEntityId()));
            List<String> accessibleExpIds = null;

            ExperimentStatistics result = airavataService.getExperimentStatistics(
                    gatewayId,
                    fromTime,
                    toTime,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExpIds,
                    limit,
                    offset);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project project = airavataService.getProject(projectId);

            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()
                            && !authzToken
                                    .getClaimsMap()
                                    .get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    .equals(project.getOwner())
                    || !authzToken
                            .getClaimsMap()
                            .get(org.apache.airavata.common.utils.Constants.GATEWAY_ID)
                            .equals(project.getGatewayId())) {
                try {
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, projectId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            List<ExperimentModel> result = airavataService.getExperimentsInProject(gatewayId, projectId, limit, offset);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        try {
            return airavataService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            throw exception;
        }
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
        // TODO: verify that gatewayId and experiment.gatewayId match authzToken
        logger.info("Api server accepted experiment creation with name {}", experiment.getExperimentName());
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String experimentId = airavataService.createExperimentWithSharingAndPublish(
                    sharingClient, statusPublisher, gatewayId, experiment);
            sharingClientPool.returnResource(sharingClient);
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getExperimentName());
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            boolean result = airavataService.deleteExperimentWithAuth(sharingClient, authzToken, experimentId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the experiment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        ExperimentModel experimentModel = null;
        try {
            experimentModel = airavataService.getExperiment(airavataExperimentId);
            if (authzToken
                            .getClaimsMap()
                            .get(org.apache.airavata.common.utils.Constants.USER_NAME)
                            .equals(experimentModel.getUserName())
                    && authzToken
                            .getClaimsMap()
                            .get(org.apache.airavata.common.utils.Constants.GATEWAY_ID)
                            .equals(experimentModel.getGatewayId())) {
                sharingClientPool.returnResource(sharingClient);
                return experimentModel;
            } else if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, airavataExperimentId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                    sharingClientPool.returnResource(sharingClient);
                    return experimentModel;
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            } else {
                sharingClientPool.returnResource(sharingClient);
                return null;
            }
        } catch (Exception e) {
            if (e instanceof ExperimentNotFoundException) {
                logger.error(e.getMessage(), e);
                sharingClientPool.returnResource(sharingClient);
                throw (ExperimentNotFoundException) e;
            }
            logger.error("Error while getting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the experiment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            ExperimentModel experimentModel = airavataService.getExperiment(airavataExperimentId);
            if (gatewayId.equals(experimentModel.getGatewayId())) {
                return experimentModel;
            } else {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } catch (Exception e) {
            logger.error("Error while getting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the experiment. More info : " + e.getMessage());
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentModel result = airavataService.getDetailedExperimentTree(airavataExperimentId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentModel experimentModel = airavataService.getExperiment(airavataExperimentId);
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()
                            && !authzToken
                                    .getClaimsMap()
                                    .get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    .equals(experimentModel.getUserName())
                    || !authzToken
                            .getClaimsMap()
                            .get(org.apache.airavata.common.utils.Constants.GATEWAY_ID)
                            .equals(experimentModel.getGatewayId())) {
                try {
                    // Verify WRITE access
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, airavataExperimentId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            try {
                // Update name, description and parent on Entity
                // TODO: update the experiment via a DB event
                Entity entity = sharingClient.getEntity(gatewayId, airavataExperimentId);
                entity.setName(experiment.getExperimentName());
                entity.setDescription(experiment.getDescription());
                entity.setParentEntityId(experiment.getProjectId());
                sharingClient.updateEntity(entity);
            } catch (Exception e) {
                throw new Exception("Failed to update entity in sharing registry", e);
            }

            airavataService.updateExperiment(airavataExperimentId, experiment);
            sharingClientPool.returnResource(sharingClient);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(
            AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        try {
            airavataService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating user configuration", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating user configuration. " + "Update experiment is only valid for experiments "
                            + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                            + "experiment is in one of above statuses...  "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            AuthzToken authzToken, String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, TException {
        try {
            airavataService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating scheduling info", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating scheduling info. " + "Update experiment is only valid for experiments "
                            + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                            + "experiment is in one of above statuses...  "
                            + e.getMessage());
            throw exception;
        }
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
        // TODO - call validation module and validate experiment
        /*     	try {
                 ExperimentModel experimentModel = regClient.getExperiment(airavataExperimentId);
        	if (experimentModel == null) {
                     logger.error(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                     throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
                 }
             } catch (RegistryServiceException | ApplicationSettingsException e1) {
        	  logger.error(airavataExperimentId, "Error while retrieving projects", e1);
                   AiravataSystemException exception = new AiravataSystemException();
                   exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                   exception.setMessage("Error while retrieving projects. More info : " + e1.getMessage());
                   throw exception;
        }

             Client orchestratorClient = getOrchestratorClient();
             try{
             if (orchestratorClient.validateExperiment(airavataExperimentId)) {
                 logger.debug(airavataExperimentId, "Experiment validation succeed.");
                 return true;
             } else {
                 logger.debug(airavataExperimentId, "Experiment validation failed.");
                 return false;
             }}catch (TException e){
                 throw e;
             }finally {
                 orchestratorClient.getOutputProtocol().getTransport().close();
                 orchestratorClient.getInputProtocol().getTransport().close();
             }*/

        return true;
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
        try {
            ExperimentStatus result = airavataService.getExperimentStatus(airavataExperimentId);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage(e.getMessage());
            throw exception;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage(e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            List<OutputDataObjectType> result = airavataService.getExperimentOutputs(airavataExperimentId);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            airavataService.validateAndFetchIntermediateOutputs(
                    sharingClient, authzToken, airavataExperimentId, outputNames, experimentPublisher);
            sharingClientPool.returnResource(sharingClient);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have WRITE access")) {
                logger.error(e.getMessage(), e);
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException(e.getMessage());
            } else if (e.getMessage() != null
                    && (e.getMessage().contains("does not have currently ACTIVE job")
                            || e.getMessage().contains("already intermediate output fetching tasks"))) {
                logger.error(e.getMessage(), e);
                sharingClientPool.returnResource(sharingClient);
                throw new InvalidRequestException(e.getMessage());
            }
            logger.error(
                    "Error while processing request to fetch intermediate outputs for experiment: "
                            + airavataExperimentId,
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while processing request to fetch intermediate outputs for experiment. More info : "
                            + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ProcessStatus result = airavataService.getIntermediateOutputProcessStatusInternal(
                    sharingClient, authzToken, airavataExperimentId, outputNames);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have READ access")) {
                logger.debug(e.getMessage(), e);
                sharingClientPool.returnResource(sharingClient);
                throw new AuthorizationException(e.getMessage());
            } else if (e.getMessage() != null && e.getMessage().contains("No matching intermediate output")) {
                logger.debug(e.getMessage(), e);
                sharingClientPool.returnResource(sharingClient);
                throw new InvalidRequestException(e.getMessage());
            }
            logger.error(
                    "Error while processing request to get intermediate output process status for experiment: "
                            + airavataExperimentId,
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while processing request to get intermediate output process status for experiment. More info : "
                            + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            Map<String, JobStatus> result = airavataService.getJobStatuses(airavataExperimentId);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error(airavataExperimentId, "Error while retrieving the job statuses", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job statuses. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job statuses", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job statuses. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            List<JobModel> result = airavataService.getJobDetails(airavataExperimentId);
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error(airavataExperimentId, "Error while retrieving the job details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            throw exception;
        }
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
        // TODO: verify that gatewayId matches gatewayId in authzToken
        logger.info("Launching experiment {}", airavataExperimentId);
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            airavataService.launchExperimentWithValidation(
                    sharingClient, authzToken, gatewayId, airavataExperimentId, experimentPublisher);
            logger.info("Experiment with ExpId: " + airavataExperimentId + " was submitted in gateway with gatewayID: "
                    + gatewayId);
            sharingClientPool.returnResource(sharingClient);
        } catch (InvalidRequestException | ExperimentNotFoundException | AuthorizationException e) {
            logger.error(e.getMessage(), e);
            sharingClientPool.returnResource(sharingClient);
            throw e;
        } catch (Exception e1) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e1.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            // getExperiment will apply sharing permissions
            ExperimentModel existingExperiment = this.getExperiment(authzToken, existingExperimentID);
            String result = cloneExperimentInternal(
                    sharingClient,
                    authzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(
            AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException, TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            // get existing experiment by bypassing normal sharing permissions for the admin user
            ExperimentModel existingExperiment = this.getExperimentByAdmin(authzToken, existingExperimentID);
            String result = cloneExperimentInternal(
                    sharingClient,
                    authzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    private String cloneExperimentInternal(
            SharingRegistryService.Client sharingClient,
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, TException, AuthorizationException,
                    ApplicationSettingsException {
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
            Project project = this.getProject(authzToken, newExperimentProjectId);
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
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        if (!sharingClient.userHasAccess(
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
                List<OutputDataObjectType> applicationOutputs =
                        airavataService.getApplicationOutputs(existingExperiment.getExecutionId());
                existingExperiment.setExperimentOutputs(applicationOutputs);
            } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
                logger.warn("Error getting application outputs for experiment clone: " + e.getMessage());
            } catch (Exception e) {
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
            String compResourceId = existingExperiment
                    .getUserConfigurationData()
                    .getComputationalResourceScheduling()
                    .getResourceHostId();

            try {
                ComputeResourceDescription computeResourceDescription =
                        airavataService.getComputeResource(compResourceId);
                if (!computeResourceDescription.isEnabled()) {
                    existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                }
            } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
                logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
            } catch (Exception e) {
                logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
            }
        }
        logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
        existingExperiment.setUserName(userId);
        String expId;
        try {
            expId = airavataService.createExperiment(gatewayId, existingExperiment);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            throw new TException("Error creating cloned experiment: " + e.getMessage(), e);
        }

        if (ServerSettings.isEnableSharing()) {
            try {
                Entity entity = new Entity();
                entity.setEntityId(expId);
                final String domainId = existingExperiment.getGatewayId();
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
                entity.setOwnerId(existingExperiment.getUserName() + "@" + domainId);
                entity.setName(existingExperiment.getExperimentName());
                entity.setDescription(existingExperiment.getDescription());
                sharingClient.createEntity(entity);
                airavataService.shareEntityWithAdminGatewayGroups(sharingClient, entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("rolling back experiment creation Exp ID : " + expId);
                try {
                    airavataService.deleteExperiment(expId);
                } catch (org.apache.airavata.registry.cpi.RegistryException e) {
                    logger.error("Error deleting experiment during rollback: " + e.getMessage());
                }
            }
        }

        return expId;
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
        try {
            ExperimentModel existingExperiment = airavataService.getExperiment(airavataExperimentId);
            ExperimentStatus experimentLastStatus = airavataService.getExperimentStatus(airavataExperimentId);
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
                    airavataService.publishExperimentCancelEvent(experimentPublisher, gatewayId, airavataExperimentId);
                    logger.debug("Airavata cancelled experiment with experiment id : " + airavataExperimentId);
                    break;
            }
        } catch (AiravataException e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cancelling the experiment. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cancelling the experiment. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.registerApplicationModule(gatewayId, applicationModule);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while adding application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application module. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while adding application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application module. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            ApplicationModule result = airavataService.getApplicationModule(appModuleId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the adding application module. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the adding application module. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateApplicationModule(appModuleId, applicationModule);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<ApplicationModule> result = airavataService.getAllAppModules(gatewayId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<ApplicationModule> result =
                    airavataService.getAccessibleAppModulesWithSharing(sharingClient, authzToken, gatewayId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteApplicationModule(appModuleId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(appModuleId, "Error while deleting application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the application module. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while deleting application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the application module. More info : " + e.getMessage());
            throw exception;
        }
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
        // TODO: verify that gatewayId matches authzToken gatewayId
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String result = airavataService.registerApplicationDeployment(gatewayId, applicationDeployment);
            Entity entity = new Entity();
            entity.setEntityId(result);
            final String domainId = gatewayId;
            entity.setDomainId(domainId);
            entity.setEntityTypeId(domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            entity.setOwnerId(userName + "@" + domainId);
            entity.setName(result);
            entity.setDescription(applicationDeployment.getAppDeploymentDescription());
            sharingClient.createEntity(entity);
            airavataService.shareEntityWithAdminGatewayGroups(sharingClient, entity);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application deployment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = airavataService.userHasAccessInternal(
                        sharingClient, authzToken, appDeploymentId, ResourcePermissionType.READ);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have access to application deployment " + appDeploymentId);
                }
            }
            ApplicationDeploymentDescription result = airavataService.getApplicationDeployment(appDeploymentId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = airavataService.userHasAccessInternal(
                        sharingClient, authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            boolean result = airavataService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while updating application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application deployment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            final boolean hasAccess = airavataService.userHasAccessInternal(
                    sharingClient, authzToken, appDeploymentId, ResourcePermissionType.WRITE);
            if (!hasAccess) {
                throw new AuthorizationException(
                        "User does not have WRITE access to application deployment " + appDeploymentId);
            }
            final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            boolean result = airavataService.deleteApplicationDeployment(appDeploymentId);
            sharingClient.deleteEntity(domainId, appDeploymentId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while deleting application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        return getAccessibleApplicationDeployments(authzToken, gatewayId, ResourcePermissionType.READ);
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<ApplicationDeploymentDescription> result =
                    airavataService.getAccessibleApplicationDeploymentsWithSharing(
                            sharingClient, authzToken, gatewayId, permissionType);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        try {
            // TODO: restrict to only application deployments that are accessible to user
            List<String> result = airavataService.getAppModuleDeployedResources(appModuleId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
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
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            // Get list of compute resources for this Group Resource Profile
            if (!airavataService.userHasAccessInternal(
                    sharingClient, authzToken, groupResourceProfileId, ResourcePermissionType.READ)) {
                throw new AuthorizationException(
                        "User is not authorized to access Group Resource Profile " + groupResourceProfileId);
            }
            GroupResourceProfile groupResourceProfile = airavataService.getGroupResourceProfile(groupResourceProfileId);
            List<String> accessibleComputeResourceIds = groupResourceProfile.getComputePreferences().stream()
                    .map(compPref -> compPref.getComputeResourceId())
                    .collect(Collectors.toList());

            // Get list of accessible Application Deployments
            List<String> accessibleAppDeploymentIds = new ArrayList<>();
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
                    .searchEntities(gatewayId, userName + "@" + gatewayId, sharingFilters, 0, -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));

            List<ApplicationDeploymentDescription> result =
                    airavataService.getAccessibleApplicationDeploymentsForAppModule(
                            appModuleId, gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (AuthorizationException checkedException) {
            logger.error("Error while retrieving application deployments...", checkedException);
            sharingClientPool.returnResource(sharingClient);
            throw checkedException;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
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
        try {
            String result = airavataService.registerApplicationInterface(gatewayId, applicationInterface);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ApplicationInterfaceDescription existingInterface =
                    airavataService.getApplicationInterface(existingAppInterfaceID);
            if (existingInterface == null) {
                logger.error(
                        "Provided application interface does not exist.Please provide a valid application interface id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }

            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            String interfaceId = airavataService.registerApplicationInterface(gatewayId, existingInterface);
            logger.debug("Airavata cloned application interface : " + existingAppInterfaceID + " for gateway id : "
                    + gatewayId);
            return interfaceId;
        } catch (Exception e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            ApplicationInterfaceDescription result = airavataService.getApplicationInterface(appInterfaceId);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while retrieving application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interface. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateApplicationInterface(appInterfaceId, applicationInterface);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while updating application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application interface. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteApplicationInterface(appInterfaceId);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while deleting application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application interface. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            Map<String, String> result = airavataService.getAllApplicationInterfaceNames(gatewayId);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<ApplicationInterfaceDescription> result = airavataService.getAllApplicationInterfaces(gatewayId);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<InputDataObjectType> result = airavataService.getApplicationInputs(appInterfaceId);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while retrieving application inputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application inputs. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<OutputDataObjectType> result = airavataService.getApplicationOutputs(appInterfaceId);
            return result;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage(e.getMessage());
            throw exception;
        }
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
        try {
            Map<String, String> result = airavataService.getAvailableAppInterfaceComputeResources(appInterfaceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(appInterfaceId, "Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.registerComputeResource(computeResourceDescription);
            return result;
        } catch (Exception e) {
            logger.error("Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            ComputeResourceDescription result = airavataService.getComputeResource(computeResourceId);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            Map<String, String> result = airavataService.getAllComputeResourceNames();
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateComputeResource(computeResourceId, computeResourceDescription);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while updating compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updaing compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteComputeResource(computeResourceId);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while deleting compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting compute resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.registerStorageResource(storageResourceDescription);
            return result;
        } catch (Exception e) {
            logger.error("Error while saving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving storage resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            StorageResourceDescription result = airavataService.getStorageResource(storageResourceId);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while retrieving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            Map<String, String> result = airavataService.getAllStorageResourceNames();
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateStorageResource(storageResourceId, storageResourceDescription);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while updating storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updaing storage resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteStorageResource(storageResourceId);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while deleting storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting storage resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        RegistryService.Client regClient = registryClientPool.getResource();
        StorageInfoContext context;

        try {
            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
                ComputeResourceDescription computeResource = regClient.getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (TApplicationException e) {
                // TApplicationException with "unknown result" means resource not found (null return for non-nullable
                // type)
                logger.debug("Compute resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = regClient.getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (TApplicationException e) {
                    // TApplicationException with "unknown result" means resource not found (null return for
                    // non-nullable type)
                    logger.debug(
                            "Storage resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
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

            registryClientPool.returnResource(regClient);
            regClient = null;

            return context.adaptor.getStorageVolumeInfo(location);

        } catch (InvalidRequestException | AiravataClientException e) {
            if (regClient != null) {
                registryClientPool.returnResource(regClient);
            }
            logger.error("Error while retrieving storage resource.", e);
            throw e;

        } catch (Exception e) {
            logger.error("Error while retrieving storage volume info for resource {}", resourceId, e);
            registryClientPool.returnBrokenResource(regClient);

            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage volume info. More info: " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(AuthzToken authzToken, String resourceId, String location)
            throws TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        RegistryService.Client regClient = registryClientPool.getResource();
        StorageInfoContext context;

        try {
            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
                ComputeResourceDescription computeResource = regClient.getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (TApplicationException e) {
                // TApplicationException with "unknown result" means resource not found (null return for non-nullable
                // type)
                logger.debug("Compute resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = regClient.getStorageResource(resourceId);
                    if (storageResource != null) {
                        storageResourceOp = Optional.of(storageResource);
                    }
                } catch (TApplicationException e) {
                    // TApplicationException with "unknown result" means resource not found (null return for
                    // non-nullable type)
                    logger.debug(
                            "Storage resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
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

            registryClientPool.returnResource(regClient);
            regClient = null;

            return context.adaptor.getStorageDirectoryInfo(location);

        } catch (InvalidRequestException | AiravataClientException e) {
            if (regClient != null) {
                registryClientPool.returnResource(regClient);
            }
            logger.error("Error while retrieving storage resource.", e);
            throw e;

        } catch (Exception e) {
            logger.error("Error while retrieving storage volume info for resource {}", resourceId, e);
            registryClientPool.returnBrokenResource(regClient);

            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage volume info. More info: " + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            LOCALSubmission result = airavataService.getLocalJobSubmission(jobSubmissionId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            SSHJobSubmission result = airavataService.getSSHJobSubmission(jobSubmissionId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    computeResourceId,
                    "Error while adding job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            CloudJobSubmission result = airavataService.getCloudJobSubmission(jobSubmissionId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.addUNICOREJobSubmissionDetails(
                    computeResourceId, priorityOrder, unicoreJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            UnicoreJobSubmission result = airavataService.getUnicoreJobSubmission(jobSubmissionId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result =
                    airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result =
                    airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating job submission interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding data movement interface to resource. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while adding data movement interface to resource. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(dataMovementInterfaceId, "Error while updating local data movement interface..", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating local data movement interface. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating local data movement interface..", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating local data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            LOCALDataMovement result = airavataService.getLocalDataMovement(dataMovementId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result =
                    airavataService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    dataMovementInterfaceId,
                    "Error while updating data movement interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating data movement interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    dataMovementInterfaceId,
                    "Error while updating data movement interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating data movement interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            SCPDataMovement result = airavataService.getSCPDataMovement(dataMovementId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving SCP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.addUnicoreDataMovementDetails(
                    resourceId, dmType, priorityOrder, unicoreDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result =
                    airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            UnicoreDataMovement result = airavataService.getUnicoreDataMovement(dataMovementId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving UNICORE data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.addGridFTPDataMovementDetails(
                    computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : "
                    + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result =
                    airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    dataMovementInterfaceId,
                    "Error while updating GridFTP data movement interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating GridFTP data movement interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(
                    dataMovementInterfaceId,
                    "Error while updating GridFTP data movement interface to resource compute resource...",
                    e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating GridFTP data movement interface to resource compute resource. More info : "
                            + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            GridFTPDataMovement result = airavataService.getGridFTPDataMovement(dataMovementId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting job submission interface. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting job submission interface. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            String result = airavataService.registerResourceJobManager(resourceJobManager);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(
            AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ResourceJobManager result = airavataService.getResourceJobManager(resourceJobManagerId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.deleteResourceJobManager(resourceJobManagerId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.deleteBatchQueue(computeResourceId, queueName);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(computeResourceId, "Error while deleting batch queue...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting batch queue. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while deleting batch queue...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting batch queue. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.registerGatewayResourceProfile(gatewayResourceProfile);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while registering gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while registering gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            GatewayResourceProfile result = airavataService.getGatewayResourceProfile(gatewayID);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while retrieving gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while updating gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteGatewayResourceProfile(gatewayID);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while removing gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while removing gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.addGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result =
                    airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            ComputeResourcePreference result =
                    airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            StoragePreference result = airavataService.getGatewayStoragePreference(gatewayID, storageId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<ComputeResourcePreference> result = airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<StoragePreference> result = airavataService.getAllGatewayStoragePreferences(gatewayID);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<GatewayResourceProfile> result = airavataService.getAllGatewayResourceProfiles();
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, computeResourcePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while updating gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while updating gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result =
                    airavataService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while updating gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while updating gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while deleting gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while deleting gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while deleting gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while deleting gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while deleting gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while deleting gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<SSHAccountProvisioner> getSSHAccountProvisioners(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {

        List<SSHAccountProvisioner> sshAccountProvisioners = new ArrayList<>();
        List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders =
                SSHAccountProvisionerFactory.getSSHAccountProvisionerProviders();
        for (SSHAccountProvisionerProvider provider : sshAccountProvisionerProviders) {
            // TODO: Move this Thrift conversion to utility class
            SSHAccountProvisioner sshAccountProvisioner = new SSHAccountProvisioner();
            sshAccountProvisioner.setCanCreateAccount(provider.canCreateAccount());
            sshAccountProvisioner.setCanInstallSSHKey(provider.canInstallSSHKey());
            sshAccountProvisioner.setName(provider.getName());
            List<SSHAccountProvisionerConfigParam> sshAccountProvisionerConfigParams = new ArrayList<>();
            for (ConfigParam configParam : provider.getConfigParams()) {
                SSHAccountProvisionerConfigParam sshAccountProvisionerConfigParam =
                        new SSHAccountProvisionerConfigParam();
                sshAccountProvisionerConfigParam.setName(configParam.getName());
                sshAccountProvisionerConfigParam.setDescription(configParam.getDescription());
                sshAccountProvisionerConfigParam.setIsOptional(configParam.isOptional());
                switch (configParam.getType()) {
                    case STRING:
                        sshAccountProvisionerConfigParam.setType(SSHAccountProvisionerConfigParamType.STRING);
                        break;
                    case CRED_STORE_PASSWORD_TOKEN:
                        sshAccountProvisionerConfigParam.setType(
                                SSHAccountProvisionerConfigParamType.CRED_STORE_PASSWORD_TOKEN);
                        break;
                }
                sshAccountProvisionerConfigParams.add(sshAccountProvisionerConfigParam);
            }
            sshAccountProvisioner.setConfigParams(sshAccountProvisionerConfigParams);
            sshAccountProvisioners.add(sshAccountProvisioner);
        }
        return sshAccountProvisioners;
    }

    @Override
    @SecurityCheck
    public boolean doesUserHaveSSHAccount(AuthzToken authzToken, String computeResourceId, String userId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return SSHAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (Exception e) {
            String errorMessage = "Error occurred while checking if [" + userId + "] has an SSH Account on ["
                    + computeResourceId + "].";
            logger.error(errorMessage, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMessage + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SSHCredential sshCredential = null;
        try {
            sshCredential = csClient.getSSHCredential(airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }

        try {
            return SSHAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Exception e) {
            final String msg =
                    "Error occurred while checking if setup of SSH account is complete for user [" + userId + "].";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        CredentialStoreService.Client csClient = csClientPool.getResource();
        SSHCredential sshCredential = null;
        try {
            sshCredential = csClient.getSSHCredential(airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }

        try {
            UserComputeResourcePreference userComputeResourcePreference =
                    SSHAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
            return userComputeResourcePreference;
        } catch (Exception e) {
            logger.error("Error occurred while automatically setting up SSH account for user [" + userId + "]", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while automatically setting up SSH account for user [" + userId
                    + "]. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            String result = airavataService.registerUserResourceProfile(userResourceProfile);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while registering user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while registering user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.isUserResourceProfileExists(userId, gatewayID);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while checking existence of user resource profile for " + userId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while checking existence of user resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Error while checking existence of user resource profile for " + userId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while checking existence of user resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            UserResourceProfile result = airavataService.getUserResourceProfile(userId, gatewayID);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving user resource profile for " + userId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while updating user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.deleteUserResourceProfile(userId, gatewayID);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while removing user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while removing user resource profile. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.addUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering user resource profile preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while registering user resource profile preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.addUserStoragePreference(
                    userId, gatewayID, userStorageResourceId, dataStoragePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while registering user storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while registering user storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user storage preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            UserComputeResourcePreference result =
                    airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            UserStoragePreference result = airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while reading user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user data storage preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            List<UserComputeResourcePreference> result =
                    airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while reading User compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading User compute resource preferences. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while reading User compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading User compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<UserStoragePreference> result = airavataService.getAllUserStoragePreferences(userId, gatewayID);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while reading User data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading User data storage preferences. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while reading User data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading User data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<UserResourceProfile> result = airavataService.getAllUserResourceProfiles();
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading retrieving all user resource profiles. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading retrieving all user resource profiles. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while updating user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while updating user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while updating user compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result = airavataService.updateUserStoragePreference(
                    userId, gatewayID, userStorageId, dataStoragePreference);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while updating user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while updating user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            boolean result =
                    airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while deleting user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while deleting user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while deleting user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while deleting user compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            boolean result = airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while deleting user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting user data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error(userId, "Error while deleting user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting user data storage preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<QueueStatusModel> result = airavataService.getLatestQueueStatuses();
            return result;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
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
        try {
            return airavataService.registerDataProduct(dataProductModel);
        } catch (Exception e) {
            String msg = "Error in registering the data resource" + dataProductModel.getProductName() + ".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.getDataProduct(productUri);
        } catch (Exception e) {
            String msg = "Error in retreiving the data product " + productUri + ".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.registerReplicaLocation(replicaLocationModel);
        } catch (Exception e) {
            String msg = "Error in retreiving the replica " + replicaLocationModel.getReplicaName() + ".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.getParentDataProduct(productUri);
        } catch (Exception e) {
            String msg = "Error in retreiving the parent data product for " + productUri + ".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return airavataService.getChildDataProducts(productUri);
        } catch (Exception e) {
            String msg = "Error in retreiving the child products for " + productUri + ".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
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
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (!airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE",
                            true);
                else if (userPermission.getValue().equals(ResourcePermissionType.READ))
                    sharingClient.shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ",
                            true);
                else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing(sharingClient, gatewayId);
                        sharingClient.shareEntityWithUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "MANAGE_SHARING",
                                true);
                    } else
                        throw new AuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                } else {
                    logger.error("Invalid ResourcePermissionType : "
                            + userPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in sharing resource with users. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (!airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE",
                            true);
                else if (groupPermission.getValue().equals(ResourcePermissionType.READ))
                    sharingClient.shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ",
                            true);
                else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing(sharingClient, gatewayId);
                        sharingClient.shareEntityWithGroups(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "MANAGE_SHARING",
                                true);
                    } else
                        throw new AuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                } else {
                    logger.error("Invalid ResourcePermissionType : "
                            + groupPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in sharing resource with groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (!airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE");
                else if (userPermission.getValue().equals(ResourcePermissionType.READ))
                    sharingClient.revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ");
                else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing(sharingClient, gatewayId);
                        sharingClient.revokeEntitySharingFromUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "MANAGE_SHARING");
                    } else
                        throw new AuthorizationException(
                                "User is not allowed to change sharing permission because the user is not the resource owner.");
                } else {
                    logger.error("Invalid ResourcePermissionType : "
                            + userPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in revoking access to resource from users. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        final String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (!airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            // For certain resource types, restrict them from being unshared with admin groups
            ResourceType resourceType = airavataService.getResourceType(sharingClient, gatewayId, resourceId);
            Set<ResourceType> adminRestrictedResourceTypes = new HashSet<>(Arrays.asList(
                    ResourceType.EXPERIMENT, ResourceType.APPLICATION_DEPLOYMENT, ResourceType.GROUP_RESOURCE_PROFILE));
            if (adminRestrictedResourceTypes.contains(resourceType)) {
                // Prevent removing Admins WRITE/MANAGE_SHARING access and Read Only Admins READ access
                GatewayGroups gatewayGroups = airavataService.retrieveGatewayGroups(gatewayId);
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.WRITE)) {
                    throw new Exception("Not allowed to remove Admins group's WRITE access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getReadOnlyAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getReadOnlyAdminsGroupId())
                                .equals(ResourcePermissionType.READ)) {
                    throw new Exception("Not allowed to remove Read Only Admins group's READ access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.READ)) {
                    throw new Exception("Not allowed to remove Admins group's READ access.");
                }
                if (groupPermissionList.containsKey(gatewayGroups.getAdminsGroupId())
                        && groupPermissionList
                                .get(gatewayGroups.getAdminsGroupId())
                                .equals(ResourcePermissionType.MANAGE_SHARING)) {
                    throw new Exception("Not allowed to remove Admins group's MANAGE_SHARING access.");
                }
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "WRITE");
                else if (groupPermission.getValue().equals(ResourcePermissionType.READ))
                    sharingClient.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "READ");
                else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal(
                            sharingClient, authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing(sharingClient, gatewayId);
                        sharingClient.revokeEntitySharingFromUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                gatewayId + ":" + "MANAGE_SHARING");
                    } else
                        throw new AuthorizationException(
                                "User is not allowed to change sharing because the user is not the resource owner");
                } else {
                    logger.error("Invalid ResourcePermissionType : "
                            + groupPermission.getValue().toString());
                    throw new AiravataClientException(AiravataErrorType.UNSUPPORTED_OPERATION);
                }
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in revoking access to resource from groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleUsers = airavataService.getAllAccessibleUsersWithSharing(
                    sharingClient, authzToken, resourceId, permissionType, false);
            sharingClientPool.returnResource(sharingClient);
            return accessibleUsers;
        } catch (Exception e) {
            String msg = "Error in getting all accessible users for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleUsers = airavataService.getAllAccessibleUsersWithSharing(
                    sharingClient, authzToken, resourceId, permissionType, true);
            sharingClientPool.returnResource(sharingClient);
            return accessibleUsers;
        } catch (Exception e) {
            String msg = "Error in getting all directly accessible users for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleGroups = airavataService.getAllAccessibleGroupsWithSharing(
                    sharingClient, authzToken, resourceId, permissionType, false);
            sharingClientPool.returnResource(sharingClient);
            return accessibleGroups;
        } catch (Exception e) {
            String msg = "Error in getting all accessible groups for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleGroups = airavataService.getAllAccessibleGroupsWithSharing(
                    sharingClient, authzToken, resourceId, permissionType, true);
            sharingClientPool.returnResource(sharingClient);
            return accessibleGroups;
        } catch (Exception e) {
            String msg = "Error in getting all directly accessible groups for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        final String userId = authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + domainId;
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            final boolean hasAccess =
                    airavataService.userHasAccessInternal(sharingClient, authzToken, resourceId, permissionType);
            sharingClientPool.returnResource(sharingClient);
            return hasAccess;
        } catch (Exception e) {
            String msg = "Error in if user can access resource. User ID : " + userId + ", Resource ID : " + resourceId
                    + ", Resource Permission Type : " + permissionType.toString();
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // TODO: verify that gatewayId in groupResourceProfile matches authzToken gatewayId
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String groupResourceProfileId = airavataService.createGroupResourceProfileWithSharing(
                    sharingClient, authzToken, groupResourceProfile);
            sharingClientPool.returnResource(sharingClient);
            return groupResourceProfileId;
        } catch (AuthorizationException ae) {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName
                    + " not allowed access to resources referenced in this GroupResourceProfile. Reason: "
                    + ae.getMessage());
            sharingClientPool.returnResource(sharingClient);
            throw ae;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error creating group resource profile.";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error creating group resource profile.";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            airavataService.validateGroupResourceProfile(sharingClient, authzToken, groupResourceProfile);
            if (!airavataService.userHasAccessInternal(
                    sharingClient,
                    authzToken,
                    groupResourceProfile.getGroupResourceProfileId(),
                    ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to update group resource profile");
            }
            airavataService.updateGroupResourceProfile(groupResourceProfile);
            sharingClientPool.returnResource(sharingClient);
        } catch (AuthorizationException ae) {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName + " not allowed access to update GroupResourceProfile "
                    + groupResourceProfile.getGroupResourceProfileId() + ", reason: " + ae.getMessage());
            sharingClientPool.returnResource(sharingClient);
            throw ae;
        } catch (Exception e) {
            String msg = "Error updating group resource profile. groupResourceProfileId: "
                    + groupResourceProfile.getGroupResourceProfileId();
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            GroupResourceProfile groupResourceProfile = airavataService.getGroupResourceProfile(groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return groupResourceProfile;
        } catch (AuthorizationException checkedException) {
            logger.error(
                    "Error while retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId,
                    checkedException);
            sharingClientPool.returnResource(sharingClient);
            throw checkedException;
        } catch (Exception e) {
            String msg = "Error retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()) {
                try {
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to remove group resource profile");
                }
            }
            boolean result = airavataService.removeGroupResourceProfile(groupResourceProfileId);
            sharingClient.deleteEntity(gatewayId, groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<GroupResourceProfile> groupResourceProfileList =
                    airavataService.getGroupResourceListWithSharing(sharingClient, authzToken, gatewayId);
            sharingClientPool.returnResource(sharingClient);
            return groupResourceProfileList;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving list group resource profile list. GatewayId: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving list group resource profile list. GatewayId: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputePrefs(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group compute preferences");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute preferences");
                }
            }
            boolean result = airavataService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group compute resource preferences. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error removing group compute resource preferences. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy =
                            airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group compute resource policy");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute resource policy");
                }
            }
            boolean result = airavataService.removeGroupComputeResourcePolicy(resourcePolicyId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error removing group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy =
                            airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove batch queue resource policy");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove batch queue resource policy");
                }
            }
            boolean result = airavataService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error removing batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            GroupComputeResourcePreference groupComputeResourcePreference =
                    airavataService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return groupComputeResourcePreference;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute preference. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group compute preference. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy =
                            airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }

            ComputeResourcePolicy computeResourcePolicy =
                    airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
            sharingClientPool.returnResource(sharingClient);
            return computeResourcePolicy;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy =
                            airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            BatchQueueResourcePolicy batchQueueResourcePolicy =
                    airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
            sharingClientPool.returnResource(sharingClient);
            return batchQueueResourcePolicy;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            List<GroupComputeResourcePreference> groupComputeResourcePreferenceList =
                    airavataService.getGroupComputeResourcePrefList(groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return groupComputeResourcePreferenceList;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource preference. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group compute resource preference. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            List<BatchQueueResourcePolicy> batchQueueResourcePolicyList =
                    airavataService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return batchQueueResourcePolicyList;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group batch queue resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group batch queue resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            List<ComputeResourcePolicy> computeResourcePolicyList =
                    airavataService.getGroupComputeResourcePolicyList(groupResourceProfileId);
            sharingClientPool.returnResource(sharingClient);
            return computeResourcePolicyList;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving Group compute resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        try {
            GatewayGroups gatewayGroups = airavataService.retrieveGatewayGroups(gatewayId);
            return gatewayGroups;
        } catch (Exception e) {
            String msg = "Error retrieving GatewayGroups for gateway: " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Parser getParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            Parser parser = airavataService.getParser(parserId, gatewayId);
            return parser;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parser with id: " + parserId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving parser with id: " + parserId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String saveParser(AuthzToken authzToken, Parser parser)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            String parserId = airavataService.saveParser(parser);
            return parserId;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while saving the parser";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error while saving the parser";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Parser> listAllParsers(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<Parser> parsers = airavataService.listAllParsers(gatewayId);
            return parsers;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while listing the parsers for gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error while listing the parsers for gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            airavataService.removeParser(parserId, gatewayId);
            return true;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while removing the parser " + parserId + " in gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error while removing the parser " + parserId + " in gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ParsingTemplate parsingTemplate = airavataService.getParsingTemplate(templateId, gatewayId);
            return parsingTemplate;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parsing template with id: " + templateId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving parsing template with id: " + templateId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            AuthzToken authzToken, String experimentId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<ParsingTemplate> parsingTemplates =
                    airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
            return parsingTemplates;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parsing templates for experiment: " + experimentId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error retrieving parsing templates for experiment: " + experimentId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String saveParsingTemplate(AuthzToken authzToken, ParsingTemplate parsingTemplate)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            String templateId = airavataService.saveParsingTemplate(parsingTemplate);
            return templateId;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error saving the parsing template";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error saving the parsing template";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            airavataService.removeParsingTemplate(templateId, gatewayId);
            return true;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while removing the parsing template " + templateId + " in gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error while removing the parsing template " + templateId + " in gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> listAllParsingTemplates(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<ParsingTemplate> templates = airavataService.listAllParsingTemplates(gatewayId);
            return templates;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while listing the parsing templates for gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        } catch (Exception e) {
            String msg = "Error while listing the parsing templates for gateway " + gatewayId;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * To hold storage info context (login username, credential token, and adaptor)
     */
    private record StorageInfoContext(String loginUserName, String credentialToken, AgentAdaptor adaptor) {}

    /**
     * Check if a gateway resource profile exists
     */
    private boolean isGatewayResourceProfileExists(String gatewayId) throws TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            try {
                GatewayResourceProfile profile = regClient.getGatewayResourceProfile(gatewayId);
                registryClientPool.returnResource(regClient);
                return profile != null;
            } catch (org.apache.thrift.TApplicationException e) {
                logger.error("Gateway resource profile does not exist for gateway: {}", gatewayId, e);
                registryClientPool.returnResource(regClient);
                return false;
            }
        } catch (Exception e) {
            registryClientPool.returnBrokenResource(regClient);
            logger.error("Error while checking if gateway resource profile exists", e);
            throw e;
        }
    }

    private AiravataClientException clientException(AiravataErrorType errorType, String parameter) {
        AiravataClientException exception = new AiravataClientException();
        exception.setAiravataErrorType(errorType);
        exception.setParameter(parameter);
        return exception;
    }

    /**
     * Resolves compute resource storage info context (login username, credential token, and adaptor).
     * Handles user preference → group preference fallback for both login and credentials.
     */
    private StorageInfoContext resolveComputeStorageInfoContext(
            AuthzToken authzToken, String gatewayId, String userId, String resourceId)
            throws AgentException, TException {
        String loginUserName = null;
        boolean loginFromUserPref = false;
        GroupComputeResourcePreference groupComputePref = null;
        GroupResourceProfile groupResourceProfile = null;

        UserComputeResourcePreference userComputePref = null;
        if (isUserResourceProfileExists(authzToken, userId, gatewayId)) {
            userComputePref = getUserComputeResourcePreference(authzToken, userId, gatewayId, resourceId);
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
            List<GroupResourceProfile> groupResourceProfiles = getGroupResourceList(authzToken, gatewayId);
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
                UserResourceProfile userResourceProfile = getUserResourceProfile(authzToken, userId, gatewayId);
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
                UserResourceProfile userResourceProfile = getUserResourceProfile(authzToken, userId, gatewayId);
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
            throws AgentException, TException {
        UserStoragePreference userStoragePref = null;
        if (isUserResourceProfileExists(authzToken, userId, gatewayId)) {
            userStoragePref = getUserStoragePreference(authzToken, userId, gatewayId, resourceId);
        } else {
            logger.debug(
                    "User resource profile does not exist for user {} in gateway {}, will try gateway preferences",
                    userId,
                    gatewayId);
        }

        StoragePreference storagePref = null;
        if (isGatewayResourceProfileExists(gatewayId)) {
            storagePref = getGatewayStoragePreference(authzToken, gatewayId, resourceId);
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
                UserResourceProfile userResourceProfile = getUserResourceProfile(authzToken, userId, gatewayId);
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
                GatewayResourceProfile gatewayResourceProfile = getGatewayResourceProfile(authzToken, gatewayId);
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
}
