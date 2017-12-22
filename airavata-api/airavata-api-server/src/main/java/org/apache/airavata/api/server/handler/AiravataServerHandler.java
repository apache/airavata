/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.api.server.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavata_apiConstants;
import org.apache.airavata.api.server.util.ThriftClientPool;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private Publisher statusPublisher;
    private Publisher experimentPublisher;

    private ThriftClientPool<SharingRegistryService.Client> sharingClientPool;
    private ThriftClientPool<RegistryService.Client> registryClientPool;
    private ThriftClientPool<CredentialStoreService.Client> csClientPool;

    public AiravataServerHandler() {
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);

            GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
            poolConfig.maxActive = 100;
            poolConfig.minIdle = 5;
            poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
            poolConfig.testOnBorrow = true;
            poolConfig.testWhileIdle = true;
            poolConfig.numTestsPerEvictionRun = 10;
            poolConfig.maxWait = 3000;

            sharingClientPool = new ThriftClientPool<>(
                    tProtocol -> new SharingRegistryService.Client(tProtocol), poolConfig, ServerSettings.getSharingRegistryHost(),
                    Integer.parseInt(ServerSettings.getSharingRegistryPort()));
            registryClientPool = new ThriftClientPool<>(
                    tProtocol -> new RegistryService.Client(tProtocol), poolConfig, ServerSettings.getRegistryServerHost(),
                    Integer.parseInt(ServerSettings.getRegistryServerPort()));
            csClientPool = new ThriftClientPool<>(
                    tProtocol -> new CredentialStoreService.Client(tProtocol), poolConfig, ServerSettings.getCredentialStoreServerHost(),
                    Integer.parseInt(ServerSettings.getCredentialStoreServerPort()));

            initSharingRegistry();
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (TException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
    }

    private void initSharingRegistry() throws ApplicationSettingsException, TException {
        SharingRegistryService.Client client = sharingClientPool.getResource();
        try {
            if (!client.isDomainExists(ServerSettings.getDefaultUserGateway())) {
                Domain domain = new Domain();
                domain.setDomainId(ServerSettings.getDefaultUserGateway());
                domain.setName(ServerSettings.getDefaultUserGateway());
                domain.setDescription("Domain entry for " + domain.name);
                client.createDomain(domain);

                User user = new User();
                user.setDomainId(domain.domainId);
                user.setUserId(ServerSettings.getDefaultUser() + "@" + ServerSettings.getDefaultUserGateway());
                user.setUserName(ServerSettings.getDefaultUser());
                client.createUser(user);

                //Creating Entity Types for each domain
                EntityType entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId + ":PROJECT");
                entityType.setDomainId(domain.domainId);
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId + ":EXPERIMENT");
                entityType.setDomainId(domain.domainId);
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                client.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId + ":FILE");
                entityType.setDomainId(domain.domainId);
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                client.createEntityType(entityType);

                //Creating Permission Types for each domain
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.domainId + ":READ");
                permissionType.setDomainId(domain.domainId);
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                client.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.domainId + ":WRITE");
                permissionType.setDomainId(domain.domainId);
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
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
    @SecurityCheck
    public String getAPIVersion(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException {

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
    public boolean isUserExists(AuthzToken authzToken, String gatewayId, String userName) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client client = registryClientPool.getResource();
        try {
            boolean isExists = client.isUserExists(gatewayId, userName);
            registryClientPool.returnResource(client);
            return isExists;
        } catch (Exception e) {
            logger.error("Error while verifying user", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while verifying user. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(client);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            String gatewayId = registryClient.addGateway(gateway);
            Domain domain = new Domain();
            domain.setDomainId(gateway.getGatewayId());
            domain.setName(gateway.getGatewayName());
            domain.setDescription("Domain entry for " + domain.name);
            sharingClient.createDomain(domain);

            //Creating Entity Types for each domain
            EntityType entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":PROJECT");
            entityType.setDomainId(domain.domainId);
            entityType.setName("PROJECT");
            entityType.setDescription("Project entity type");
            sharingClient.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":EXPERIMENT");
            entityType.setDomainId(domain.domainId);
            entityType.setName("EXPERIMENT");
            entityType.setDescription("Experiment entity type");
            sharingClient.createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":FILE");
            entityType.setDomainId(domain.domainId);
            entityType.setName("FILE");
            entityType.setDescription("File entity type");
            sharingClient.createEntityType(entityType);

            //Creating Permission Types for each domain
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.domainId+":READ");
            permissionType.setDomainId(domain.domainId);
            permissionType.setName("READ");
            permissionType.setDescription("Read permission type");
            sharingClient.createPermissionType(permissionType);

            permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.domainId+":WRITE");
            permissionType.setDomainId(domain.domainId);
            permissionType.setName("WRITE");
            permissionType.setDescription("Write permission type");
            sharingClient.createPermissionType(permissionType);

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
    public List<String> getAllUsersInGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<String> result = regClient.getAllUsersInGateway(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving users", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving users. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateGateway(gatewayId, updatedGateway);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while updating the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the gateway. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Gateway result = regClient.getGateway(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while getting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteGateway(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while deleting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<Gateway> result = regClient.getAllGateways();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while getting all the gateways", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.isGatewayExist(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String createNotification(AuthzToken authzToken, Notification notification) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.createNotification(notification);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while creating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating notification. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateNotification(notification);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while updating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteNotification(gatewayId, notificationId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while deleting notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting notification. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Notification result = regClient.getNotification(gatewayId, notificationId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retreiving notification. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<Notification> result = regClient.getAllNotifications(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while getting all notifications", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all notifications. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String gatewayId, String userName, String description, CredentialOwnerType credentialOwnerType) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername(userName);
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setDescription(description);
            if (credentialOwnerType != null) {
                sshCredential.setCredentialOwnerType(credentialOwnerType);
            }
            String key = csClient.addSSHCredential(sshCredential);
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            csClientPool.returnResource(csClient);
            return key;
        }catch (Exception e){
            logger.error("Error occurred while registering SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering SSH Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    /**
     * Generate and Register Username PWD Pair with Airavata Credential Store.
     *
     * @param authzToken
     * @param gatewayId  The identifier for the requested Gateway.
     * @param portalUserName The User for which the credential should be registered. For community accounts, this user is the name of the
     *                   community user name. For computational resources, this user name need not be the same user name on resoruces.
     * @param password
     * @return airavataCredStoreToken
     * An SSH Key pair is generated and stored in the credential store and associated with users or community account
     * belonging to a Gateway.
     */
    @Override
    @SecurityCheck
    public String registerPwdCredential(AuthzToken authzToken, String gatewayId, String portalUserName,
                                        String loginUserName, String password, String description) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            PasswordCredential pwdCredential = new PasswordCredential();
            pwdCredential.setPortalUserName(portalUserName);
            pwdCredential.setLoginUserName(loginUserName);
            pwdCredential.setPassword(password);
            pwdCredential.setDescription(description);
            pwdCredential.setGatewayId(gatewayId);
            String key = csClient.addPasswordCredential(pwdCredential);
            logger.debug("Airavata generated PWD credential for gateway : " + gatewayId + " and for user : " + loginUserName);
            csClientPool.returnResource(csClient);
            return key;
        }catch (Exception e){
            logger.error("Error occurred while registering PWD Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering PWD Credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String getSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            SSHCredential sshCredential = csClient.getSSHCredential(airavataCredStoreToken, gatewayId);
            logger.debug("Airavata retrieved SSH pub key for gateway id : " + gatewayId + " and for token : " + airavataCredStoreToken);
            csClientPool.returnResource(csClient);
            return sshCredential.getPublicKey();
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }


    @Override
    @SecurityCheck
    public Map<String, String> getAllGatewaySSHPubKeys(AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            Map<String, String> allSSHKeysForGateway = csClient.getAllSSHKeysForGateway(gatewayId);
            logger.debug("Airavata retrieved all SSH pub keys for gateway Id : " + gatewayId);
            csClientPool.returnResource(csClient);
            return allSSHKeysForGateway;
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH public keys for gateway : " + gatewayId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH public keys for gateway : " + gatewayId + ". More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaryForGateway(AuthzToken authzToken, SummaryType type, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            if(type.equals(SummaryType.SSH)){
                logger.debug("Airavata will retrieve all SSH pub keys summaries for gateway Id : " + gatewayId);
                List<CredentialSummary> result = csClient.getAllCredentialSummaryForGateway(type, gatewayId);
                csClientPool.returnResource(csClient);
                return result;
            } else {
                logger.info("Summay Type"+ type.toString() + " not supported by Airavata");
                AiravataSystemException ex = new AiravataSystemException();
                ex.setAiravataErrorType(AiravataErrorType.UNSUPPORTED_OPERATION);
                ex.setMessage("Summay Type"+ type.toString() + " not supported by Airavata");
                csClientPool.returnResource(csClient);
                throw ex;
            }
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH public keys summaries for gateway : " + gatewayId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH public keys summaries for gateway : " + gatewayId + ". More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaryForUsersInGateway(AuthzToken authzToken,SummaryType type, String gatewayId, String userId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            if(type.equals(SummaryType.SSH)){
                logger.debug("Airavata will retrieve all SSH pub keys summaries for gateway Id : " + gatewayId);
                List<CredentialSummary> result = csClient.getAllCredentialSummaryForUserInGateway(type, gatewayId, userId);
                csClientPool.returnResource(csClient);
                return result;
            } else {
                logger.info("Summay Type"+ type.toString() + " not supported by Airavata");
                AiravataSystemException ex = new AiravataSystemException();
                ex.setAiravataErrorType(AiravataErrorType.UNSUPPORTED_OPERATION);
                ex.setMessage("Summay Type"+ type.toString() + " not supported by Airavata");
                csClientPool.returnResource(csClient);
                throw ex;
            }
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH public keys summaries for user : " + userId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH public keys summaries for user : " + userId + ". More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllGatewayPWDCredentials(AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            Map<String, String> allPwdCredentials = csClient.getAllPWDCredentialsForGateway(gatewayId);
            logger.debug("Airavata retrieved all PWD Credentials for gateway Id : " + gatewayId);
            csClientPool.returnResource(csClient);
            return allPwdCredentials;
        }catch (Exception e){
            logger.error("Error occurred while retrieving PWD Credentials for gateway : " + gatewayId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving PWD Credentials for gateway : " + gatewayId + ". More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            boolean result = csClient.deleteSSHCredential(airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
            return result;
        }catch (Exception e){
            logger.error("Error occurred while deleting SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting SSH credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        CredentialStoreService.Client csClient = csClientPool.getResource();
        try {
            logger.debug("Airavata deleted PWD credential for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            boolean result = csClient.deletePWDCredential(airavataCredStoreToken, gatewayId);
            csClientPool.returnResource(csClient);
            return result;
        }catch (Exception e){
            logger.error("Error occurred while deleting PWD credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting PWD credential. More info : " + e.getMessage());
            csClientPool.returnBrokenResource(csClient);
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
    public String createProject(AuthzToken authzToken, String gatewayId, Project project) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String projectId = regClient.createProject(gatewayId, project);
            if(ServerSettings.isEnableSharing()){
                try {
                    Entity entity = new Entity();
                    entity.setEntityId(projectId);
                    entity.setDomainId(project.getGatewayId());
                    entity.setEntityTypeId(project.getGatewayId() + ":" + "PROJECT");
                    entity.setOwnerId(project.getOwner() + "@" + project.getGatewayId());
                    entity.setName(project.getName());
                    entity.setDescription(project.getDescription());
                    sharingClient.createEntity(entity);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    logger.error("Rolling back project creation Proj ID : " + projectId);
                    regClient.deleteProject(projectId);
                    AiravataSystemException ase = new AiravataSystemException();
                    ase.setMessage("Failed to create entry for project in Sharing Registry");
                    throw ase;
                }
            }
            logger.debug("Airavata created project with project Id : " + projectId + " for gateway Id : " + gatewayId);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return projectId;
        } catch (Exception e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project existingProject = regClient.getProject(projectId);
            if(ServerSettings.isEnableSharing() && !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(existingProject.getGatewayId())){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            projectId, gatewayId + ":WRITE")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            if(!updatedProject.getOwner().equals(existingProject.getOwner())){
                throw new InvalidRequestException("Owner of a project cannot be changed");
            }
            if(!updatedProject.getGatewayId().equals(existingProject.getGatewayId())){
                throw new InvalidRequestException("Gateway ID of a project cannot be changed");
            }
            regClient.updateProject(projectId, updatedProject);
            logger.debug("Airavata updated project with project Id : " + projectId );
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
        } catch (Exception e) {
            logger.error("Error while updating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project existingProject = regClient.getProject(projectId);
            if(ServerSettings.isEnableSharing() && !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(existingProject.getGatewayId())){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            projectId, gatewayId + ":WRITE")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            boolean ret = regClient.deleteProject(projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId );
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return ret;
        } catch (Exception e) {
            logger.error("Error while removing the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while removing the project. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    private boolean validateString(String name){
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0){
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
    public Project getProject(AuthzToken authzToken, String projectId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project project = regClient.getProject(projectId);
            if(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(project.getOwner())
                    && authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(project.getGatewayId())){
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return project;
            }else if (ServerSettings.isEnableSharing()){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            projectId, gatewayId + ":READ")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                    registryClientPool.returnResource(regClient);
                    sharingClientPool.returnResource(sharingClient);
                    return project;
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            } else {
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<Project> getUserProjects(AuthzToken authzToken, String gatewayId, String userName,
                                         int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            if (ServerSettings.isEnableSharing()){
                // user projects + user accessible projects
                List<String> accessibleProjectIds = new ArrayList<>();
                List<SearchCriteria> filters = new ArrayList<>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                filters.add(searchCriteria);
                sharingClient.searchEntities(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        userName + "@" + gatewayId, filters, offset, limit).stream().forEach(p -> accessibleProjectIds.add(p.entityId));
                List<Project> result = regClient.searchProjects(gatewayId, userName, accessibleProjectIds, new HashMap<>(), limit, offset);
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return result;
            }else{
                List<Project> result = regClient.getUserProjects(gatewayId, userName, limit, offset);
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return result;
            }

        } catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<Project> searchProjects(AuthzToken authzToken, String gatewayId, String userName, Map<ProjectSearchFields,
            String> filters, int limit, int offset) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleProjIds  = new ArrayList<>();

            if (ServerSettings.isEnableSharing()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                sharingFilters.add(searchCriteria);
                sharingClient.searchEntities(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        userName + "@" + gatewayId, sharingFilters, 0, -1).stream().forEach(e -> accessibleProjIds.add(e.entityId));
            }
            List<Project> result = regClient.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        }catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<ExperimentSummaryModel> searchExperiments(AuthzToken authzToken, String gatewayId, String userName, Map<ExperimentSearchFields,
            String> filters, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<String> accessibleExpIds = new ArrayList<>();
            if (ServerSettings.isEnableSharing()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":EXPERIMENT");
                sharingFilters.add(searchCriteria);
                sharingClient.searchEntities(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        userName + "@" + gatewayId, sharingFilters, 0, -1).forEach(e -> accessibleExpIds.add(e.entityId));
            }
            List<ExperimentSummaryModel> result = regClient.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public ExperimentStatistics getExperimentStatistics(AuthzToken authzToken, String gatewayId, long fromTime, long toTime,
                                                        String userName, String applicationName, String resourceHostName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentStatistics result = regClient.getExperimentStatistics(gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
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
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            Project project = regClient.getProject(projectId);

            if(ServerSettings.isEnableSharing() && !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(project.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(project.getGatewayId())){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            projectId, gatewayId + ":READ")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
            List<ExperimentModel> result = regClient.getExperimentsInProject(projectId, limit, offset);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<ExperimentModel> getUserExperiments(AuthzToken authzToken, String gatewayId, String userName, int limit,
                                                    int offset) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            List<ExperimentModel> result = regClient.getUserExperiments(gatewayId, userName, limit, offset);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
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
    public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            String experimentId = regClient.createExperiment(gatewayId, experiment);

            if(ServerSettings.isEnableSharing()) {
                try {
                    Entity entity = new Entity();
                    entity.setEntityId(experimentId);
                    entity.setDomainId(experiment.getGatewayId());
                    entity.setEntityTypeId(experiment.getGatewayId() + ":" + "EXPERIMENT");
                    entity.setOwnerId(experiment.getUserName() + "@" + experiment.getGatewayId());
                    entity.setName(experiment.getExperimentName());
                    entity.setDescription(experiment.getDescription());
                    entity.setParentEntityId(experiment.getProjectId());

                    sharingClient.createEntity(entity);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    logger.error("Rolling back experiment creation Exp ID : " + experimentId);
                    regClient.deleteExperiment(experimentId);
                    AiravataSystemException ase = new AiravataSystemException();
                    ase.setMessage("Failed to create sharing registry record");
                    throw ase;
                }
            }

            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.CREATED,
                    experimentId,
                    gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            if(statusPublisher !=null) {
                statusPublisher.publish(messageContext);
            }
            logger.debug(experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getExperimentName());
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteExperiment(AuthzToken authzToken, String experimentId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentModel experimentModel = regClient.getExperiment(experimentId);

            if(ServerSettings.isEnableSharing() && !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            experimentId, gatewayId + ":WRITE")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            if(!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)){
                logger.error("Error while deleting the experiment");
                throw new RegistryServiceException("Experiment is not in CREATED state. Hence cannot deleted. ID:"+ experimentId);
            }
            boolean result = regClient.deleteExperiment(experimentId);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        ExperimentModel experimentModel = null;
        try {
            experimentModel = regClient.getExperiment(airavataExperimentId);
            if(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                    && authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return experimentModel;
            }else if(ServerSettings.isEnableSharing()){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            airavataExperimentId, gatewayId + ":READ")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                    registryClientPool.returnResource(regClient);
                    sharingClientPool.returnResource(sharingClient);
                    return experimentModel;
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }else{
                registryClientPool.returnResource(regClient);
                sharingClientPool.returnResource(sharingClient);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while getting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        ExperimentModel experimentModel = null;
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            experimentModel = regClient.getExperiment(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            if(gatewayId.equals(experimentModel.getGatewayId())){
                return experimentModel;
            } else {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } catch (Exception e) {
            logger.error("Error while getting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public ExperimentModel getDetailedExperimentTree(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentModel result = regClient.getDetailedExperimentTree(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            ExperimentModel experimentModel = regClient.getExperiment(airavataExperimentId);
            if(ServerSettings.isEnableSharing() && !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                            airavataExperimentId, gatewayId + ":WRITE")){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            regClient.updateExperiment(airavataExperimentId, experiment);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            regClient.send_updateExperimentConfiguration(airavataExperimentId, userConfiguration);
            registryClientPool.returnResource(regClient);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating user configuration", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user configuration. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(AuthzToken authzToken, String airavataExperimentId,
                                          ComputationalResourceSchedulingModel resourceScheduling) throws AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            regClient.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
            registryClientPool.returnResource(regClient);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating scheduling info", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating scheduling info. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ExperimentStatus result = regClient.getExperimentStatus(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage(e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }


    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<OutputDataObjectType> result = regClient.getExperimentOutputs(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return null;
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Map<String, JobStatus> result = regClient.getJobStatuses(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job statuses", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job statuses. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<JobModel> result = regClient.getJobDetails(airavataExperimentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ExperimentModel experiment = regClient.getExperiment(airavataExperimentId);
            if (experiment == null) {
                logger.error(airavataExperimentId, "Error while launching experiment, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            submitExperiment(gatewayId, airavataExperimentId);
            registryClientPool.returnResource(regClient);
        } catch (Exception e1) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e1.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    public void executePostProcessing(AuthzToken authzToken, String airavataExperimentId, String gatewayId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ExperimentModel experiment = regClient.getExperiment(airavataExperimentId);
            if (experiment == null) {
                logger.error(airavataExperimentId, "Error while starting the post processing of experiment, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            startPostProcessingOfExperiment(gatewayId, airavataExperimentId);
            registryClientPool.returnResource(regClient);
        } catch (Exception e1) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e1.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String cloneExperiment(AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, ProjectNotFoundException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            // getExperiment will apply sharing permissions
            ExperimentModel existingExperiment = this.getExperiment(authzToken, existingExperimentID);
            String result = cloneExperimentInternal(regClient, sharingClient, authzToken, existingExperimentID, newExperimentName, newExperimentProjectId, existingExperiment);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, ProjectNotFoundException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            // get existing experiment by bypassing normal sharing permissions for the admin user
            ExperimentModel existingExperiment = this.getExperimentByAdmin(authzToken, existingExperimentID);
            String result = cloneExperimentInternal(regClient, sharingClient, authzToken, existingExperimentID, newExperimentName, newExperimentProjectId, existingExperiment);
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return result;
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            sharingClientPool.returnBrokenResource(sharingClient);
            throw exception;
        }
    }

    private String cloneExperimentInternal(RegistryService.Client regClient, SharingRegistryService.Client sharingClient,
                                           AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId, ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, TException, AuthorizationException, ApplicationSettingsException {
        if (existingExperiment == null){
            logger.error(existingExperimentID, "Error while cloning experiment {}, experiment doesn't exist.", existingExperimentID);
            throw new ExperimentNotFoundException("Requested experiment id " + existingExperimentID + " does not exist in the system..");
        }
        if (newExperimentProjectId != null) {

            // getProject will apply sharing permissions
            Project project = this.getProject(authzToken, newExperimentProjectId);
            if (project == null){
                logger.error("Error while cloning experiment {}, project {} doesn't exist.", existingExperimentID, newExperimentProjectId);
                throw new ProjectNotFoundException("Requested project id " + newExperimentProjectId + " does not exist in the system..");
            }
            existingExperiment.setProjectId(project.getProjectID());
        }

        // make sure user has write access to the project
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        if (!sharingClient.userHasAccess(gatewayId, userId + "@" + gatewayId,
                existingExperiment.getProjectId(), gatewayId + ":WRITE")){
            logger.error("Error while cloning experiment {}, user doesn't have write access to project {}", existingExperimentID, existingExperiment.getProjectId());
            throw new AuthorizationException("User does not have permission to clone an experiment in this project");
        }

        existingExperiment.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        if (existingExperiment.getExecutionId() != null){
            List<OutputDataObjectType> applicationOutputs = regClient.getApplicationOutputs(existingExperiment.getExecutionId());
            existingExperiment.setExperimentOutputs(applicationOutputs);
        }
        if (validateString(newExperimentName)){
            existingExperiment.setExperimentName(newExperimentName);
        }
        if (existingExperiment.getErrors() != null ){
            existingExperiment.getErrors().clear();
        }
        if(existingExperiment.getUserConfigurationData() != null && existingExperiment.getUserConfigurationData()
                .getComputationalResourceScheduling() != null){
            String compResourceId = existingExperiment.getUserConfigurationData()
                    .getComputationalResourceScheduling().getResourceHostId();

            ComputeResourceDescription computeResourceDescription = regClient.getComputeResource(compResourceId);
            if(!computeResourceDescription.isEnabled()){
                existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
            }
        }
        logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
        existingExperiment.setUserName(userId);
        String expId = regClient.createExperiment(gatewayId, existingExperiment);

        if(ServerSettings.isEnableSharing()){
            try {
                Entity entity = new Entity();
                entity.setEntityId(expId);
                entity.setDomainId(existingExperiment.getGatewayId());
                entity.setEntityTypeId(existingExperiment.getGatewayId() + ":" + "EXPERIMENT");
                entity.setOwnerId(existingExperiment.getUserName() + "@" + existingExperiment.getGatewayId());
                entity.setName(existingExperiment.getExperimentName());
                entity.setDescription(existingExperiment.getDescription());
                sharingClient.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("rolling back experiment creation Exp ID : " + expId);
                regClient.deleteExperiment(expId);
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
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ExperimentModel existingExperiment = regClient.getExperiment(airavataExperimentId);
            if (existingExperiment == null){
                logger.error(airavataExperimentId, "Error while cancelling experiment {}, experiment doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            switch (existingExperiment.getExperimentStatus().get(0).getState()) {
                case COMPLETED: case CANCELED: case FAILED: case CANCELING:
                    logger.warn("Can't terminate already {} experiment", existingExperiment.getExperimentStatus().get(0).getState().name());
                    break;
                case CREATED:
                    logger.warn("Experiment termination is only allowed for launched experiments.");
                    break;
                default:
                    submitCancelExperiment(gatewayId, airavataExperimentId);
                    logger.debug("Airavata cancelled experiment with experiment id : " + airavataExperimentId);
                    break;
            }
            registryClientPool.returnResource(regClient);
        } catch (RegistryServiceException | AiravataException e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cancelling the experiment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String registerApplicationModule(AuthzToken authzToken, String gatewayId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerApplicationModule(gatewayId, applicationModule);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application module. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public ApplicationModule getApplicationModule(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ApplicationModule result = regClient.getApplicationModule(appModuleId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the adding application module. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateApplicationModule(AuthzToken authzToken, String appModuleId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateApplicationModule(appModuleId, applicationModule);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<ApplicationModule> result = regClient.getAllAppModules(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteApplicationModule(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteApplicationModule(appModuleId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while deleting application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the application module. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String registerApplicationDeployment(AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerApplicationDeployment(gatewayId, applicationDeployment);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application deployment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ApplicationDeploymentDescription result = regClient.getApplicationDeployment(appDeploymentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateApplicationDeployment(AuthzToken authzToken, String appDeploymentId,
                                               ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while updating application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application deployment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteApplicationDeployment(AuthzToken authzToken, String appDeploymentId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteApplicationDeployment(appDeploymentId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appDeploymentId, "Error while deleting application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @return list applicationDeployment.
     * Returns the list of all application Deployment Objects.
     */
    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<ApplicationDeploymentDescription> result = regClient.getAllApplicationDeployments(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<String> getAppModuleDeployedResources(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<String> result = regClient.getAppModuleDeployedResources(appModuleId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String registerApplicationInterface(AuthzToken authzToken, String gatewayId,
                                               ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerApplicationInterface(gatewayId, applicationInterface);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ApplicationInterfaceDescription existingInterface = regClient.getApplicationInterface(existingAppInterfaceID);
            if (existingInterface == null){
                logger.error("Provided application interface does not exist.Please provide a valid application interface id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }

            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            String interfaceId = regClient.registerApplicationInterface(gatewayId, existingInterface);
            logger.debug("Airavata cloned application interface : " + existingAppInterfaceID + " for gateway id : " + gatewayId );
            registryClientPool.returnResource(regClient);
            return interfaceId;
        } catch (Exception e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ApplicationInterfaceDescription result = regClient.getApplicationInterface(appInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while retrieving application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateApplicationInterface(AuthzToken authzToken, String appInterfaceId,
                                              ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateApplicationInterface(appInterfaceId, applicationInterface);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while updating application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteApplicationInterface(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteApplicationInterface(appInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while deleting application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Map<String, String> result = regClient.getAllApplicationInterfaceNames(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<ApplicationInterfaceDescription> result = regClient.getAllApplicationInterfaces(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<InputDataObjectType> getApplicationInputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<InputDataObjectType> result = regClient.getApplicationInputs(appInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while retrieving application inputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application inputs. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<OutputDataObjectType> getApplicationOutputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<OutputDataObjectType> result = regClient.getApplicationOutputs(appInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage(e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public Map<String, String> getAvailableAppInterfaceComputeResources(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Map<String, String> result = regClient.getAvailableAppInterfaceComputeResources(appInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(appInterfaceId, "Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerComputeResource(computeResourceDescription);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ComputeResourceDescription result = regClient.getComputeResource(computeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public Map<String, String> getAllComputeResourceNames(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Map<String, String> result = regClient.getAllComputeResourceNames();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateComputeResource(AuthzToken authzToken, String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateComputeResource(computeResourceId, computeResourceDescription);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while updating compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updaing compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteComputeResource(AuthzToken authzToken, String computeResourceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteComputeResource(computeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while deleting compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerStorageResource(storageResourceDescription);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while saving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving storage resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public StorageResourceDescription getStorageResource(AuthzToken authzToken, String storageResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            StorageResourceDescription result = regClient.getStorageResource(storageResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while retrieving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public Map<String, String> getAllStorageResourceNames(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            Map<String, String> result = regClient.getAllStorageResourceNames();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateStorageResource(AuthzToken authzToken, String storageResourceId, StorageResourceDescription storageResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateStorageResource(storageResourceId, storageResourceDescription);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while updating storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updaing storage resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteStorageResource(AuthzToken authzToken, String storageResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteStorageResource(storageResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(storageResourceId, "Error while deleting storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting storage resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addLocalSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateLocalSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            LOCALSubmission result = regClient.getLocalJobSubmission(jobSubmissionId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addSSHJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addSSHForkJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            SSHJobSubmission result = regClient.getSSHJobSubmission(jobSubmissionId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addCloudJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                               CloudJobSubmission cloudJobSubmission) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            CloudJobSubmission result = regClient.getCloudJobSubmission(jobSubmissionId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addUNICOREJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                                 UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            UnicoreJobSubmission result = regClient.getUnicoreJobSubmission(jobSubmissionId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateSSHJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateCloudJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addLocalDataMovementDetails(AuthzToken authzToken, String resourceId, DMType dmType, int priorityOrder,
                                              LOCALDataMovement localDataMovement) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateLocalDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating local data movement interface..", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating local data movement interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            LOCALDataMovement result = regClient.getLocalDataMovement(dataMovementId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addSCPDataMovementDetails(AuthzToken authzToken, String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateSCPDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            SCPDataMovement result = regClient.getSCPDataMovement(dataMovementId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addUnicoreDataMovementDetails(AuthzToken authzToken, String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            UnicoreDataMovement result = regClient.getUnicoreDataMovement(dataMovementId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String addGridFTPDataMovementDetails(AuthzToken authzToken, String computeResourceId, DMType dmType, int priorityOrder,
                                                GridFTPDataMovement gridFTPDataMovement) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.addGridFTPDataMovementDetails(computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateGridFTPDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            GridFTPDataMovement result = regClient.getGridFTPDataMovement(dataMovementId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean changeJobSubmissionPriority(AuthzToken authzToken, String jobSubmissionInterfaceId, int newPriorityOrder) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
    public boolean changeDataMovementPriority(AuthzToken authzToken, String dataMovementInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
    public boolean deleteJobSubmissionInterface(AuthzToken authzToken, String computeResourceId, String jobSubmissionInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting job submission interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteDataMovementInterface(AuthzToken authzToken, String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerResourceJobManager(resourceJobManager);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken,String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ResourceJobManager result = regClient.getResourceJobManager(resourceJobManagerId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteResourceJobManager(resourceJobManagerId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteBatchQueue(computeResourceId, queueName);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(computeResourceId, "Error while deleting batch queue...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting batch queue. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerGatewayResourceProfile(gatewayResourceProfile);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while registering gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public GatewayResourceProfile getGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            GatewayResourceProfile result = regClient.getGatewayResourceProfile(gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while retrieving gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving gateway resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateGatewayResourceProfile(AuthzToken authzToken,
                                                String gatewayID,
                                                GatewayResourceProfile gatewayResourceProfile) throws TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while updating gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteGatewayResourceProfile(gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while removing gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while removing gateway resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean addGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                       ComputeResourcePreference computeResourcePreference) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.addGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public ComputeResourcePreference getGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            ComputeResourcePreference result = regClient.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            StoragePreference result = regClient.getGatewayStoragePreference(gatewayID, storageId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<ComputeResourcePreference> result = regClient.getAllGatewayComputeResourcePreferences(gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<StoragePreference> result = regClient.getAllGatewayStoragePreferences(gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<GatewayResourceProfile> result = regClient.getAllGatewayResourceProfiles();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                          ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteGatewayStoragePreference(gatewayID, storageId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerUserResourceProfile(userResourceProfile);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error("Error while registering user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public UserResourceProfile getUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            UserResourceProfile result = regClient.getUserResourceProfile(userId, gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateUserResourceProfile(AuthzToken authzToken,
                                             String userId,
                                             String gatewayID,
                                             UserResourceProfile userResourceProfile) throws TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while updating user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteUserResourceProfile(userId, gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while removing user resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while removing user resource profile. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean addUserComputeResourcePreference(AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId,
                                                    UserComputeResourcePreference userComputeResourcePreference) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.addUserComputeResourcePreference(userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user resource profile preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean addUserStoragePreference(AuthzToken authzToken, String userId, String gatewayID, String userStorageResourceId, UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.addUserStoragePreference(userId, gatewayID, userStorageResourceId, dataStoragePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while registering user storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering user storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public UserComputeResourcePreference getUserComputeResourcePreference(AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            UserComputeResourcePreference result = regClient.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(AuthzToken authzToken, String userId, String gatewayID, String userStorageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            UserStoragePreference result = regClient.getUserStoragePreference(userId, gatewayID, userStorageId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading user data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<UserComputeResourcePreference> result = regClient.getAllUserComputeResourcePreferences(userId, gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading User compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading User compute resource preferences. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(AuthzToken authzToken, String userId, String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<UserStoragePreference> result = regClient.getAllUserStoragePreferences(userId, gatewayID);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading User data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading User data storage preferences. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<UserResourceProfile> result = regClient.getAllUserResourceProfiles();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading retrieving all user resource profiles. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean updateUserComputeResourcePreference(AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId,
                                                       UserComputeResourcePreference userComputeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateUserComputeResourcePreference(userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserStoragePreference(AuthzToken authzToken, String userId, String gatewayID, String userStorageId, UserStoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.updateUserStoragePreference(userId, gatewayID, userStorageId, dataStoragePreference);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public boolean deleteUserComputeResourcePreference(AuthzToken authzToken, String userId,String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user compute resource preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(AuthzToken authzToken, String userId, String gatewayID, String userStorageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.deleteUserStoragePreference(userId, gatewayID, userStorageId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            logger.error(userId, "Error while reading user data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }



    @Override
    @SecurityCheck
    public List<String> getAllWorkflows(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<String> result = regClient.getAllWorkflows(gatewayId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retrieving all workflow template Ids.";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<QueueStatusModel> result = regClient.getLatestQueueStatuses();
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public WorkflowModel getWorkflow(AuthzToken authzToken, String workflowTemplateId)
            throws InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            WorkflowModel result = regClient.getWorkflow(workflowTemplateId);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retrieving the workflow "+workflowTemplateId+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void deleteWorkflow(AuthzToken authzToken, String workflowTemplateId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            regClient.deleteWorkflow(workflowTemplateId);
            registryClientPool.returnResource(regClient);
            return;
        } catch (Exception e) {
            String msg = "Error in deleting the workflow "+workflowTemplateId+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerWorkflow(AuthzToken authzToken, String gatewayId, WorkflowModel workflow)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerWorkflow(gatewayId, workflow);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in registering the workflow "+workflow.getName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateWorkflow(AuthzToken authzToken, String workflowTemplateId, WorkflowModel workflow)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            regClient.updateWorkflow(workflowTemplateId, workflow);
            registryClientPool.returnResource(regClient);
            return;
        } catch (Exception e) {
            String msg = "Error in updating the workflow "+workflow.getName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String getWorkflowTemplateId(AuthzToken authzToken, String workflowName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.getWorkflowTemplateId(workflowName);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retrieving the workflow template id for "+workflowName+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isWorkflowExistWithName(AuthzToken authzToken, String workflowName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            boolean result = regClient.isWorkflowExistWithName(workflowName);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
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
    public String registerDataProduct(AuthzToken authzToken, DataProductModel dataProductModel) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerDataProduct(dataProductModel);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in registering the data resource"+dataProductModel.getProductName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            DataProductModel result = regClient.getDataProduct(productUri);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retreiving the data product "+productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            String result = regClient.registerReplicaLocation(replicaLocationModel);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retreiving the replica "+replicaLocationModel.getReplicaName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            DataProductModel result = regClient.getParentDataProduct(productUri);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retreiving the parent data product for "+ productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            List<DataProductModel> result = regClient.getChildDataProducts(productUri);
            registryClientPool.returnResource(regClient);
            return result;
        } catch (Exception e) {
            String msg = "Error in retreiving the child products for "+productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            registryClientPool.returnBrokenResource(regClient);
            throw exception;
        }
    }

    /**
     * Group Manager and Data Sharing Related API methods
     *
     * @param authzToken
     * @param resourceId
     * @param resourceType
     * @param userPermissionList
     */
    @Override
    @SecurityCheck
    public boolean shareResourceWithUsers(AuthzToken authzToken, String resourceId, ResourceType resourceType,
                                          Map<String, ResourcePermissionType> userPermissionList) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            for(Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()){
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if(userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.shareEntityWithUsers(gatewayId, resourceId,
                            Arrays.asList(userPermission.getKey()), authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE", true);
                else
                    sharingClient.shareEntityWithUsers(gatewayId, resourceId,
                            Arrays.asList(userPermission.getKey()), authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ", true);
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in sharing resource with users. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
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
    public boolean revokeSharingOfResourceFromUsers(AuthzToken authzToken, String resourceId, ResourceType resourceType,
                                                    Map<String, ResourcePermissionType> userPermissionList) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            for(Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()){
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if(userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    sharingClient.revokeEntitySharingFromUsers(gatewayId, resourceId,
                            Arrays.asList(userPermission.getKey()), authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE");
                else
                    sharingClient.revokeEntitySharingFromUsers(gatewayId, resourceId,
                            Arrays.asList(userPermission.getKey()), authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ");
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return true;
        } catch (Exception e) {
            String msg = "Error in revoking access to resouce from users. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
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
    public List<String> getAllAccessibleUsers(AuthzToken authzToken, String resourceId, ResourceType resourceType, ResourcePermissionType permissionType) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        RegistryService.Client regClient = registryClientPool.getResource();
        SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
        try {
            HashSet<String> accessibleUsers = new HashSet<>();
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                sharingClient.getListOfSharedUsers(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        resourceId, authzToken.getClaimsMap().get(Constants.GATEWAY_ID)
                                + ":WRITE").stream().forEach(u -> accessibleUsers.add(u.userId));
                sharingClient.getListOfSharedUsers(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        resourceId, authzToken.getClaimsMap().get(Constants.GATEWAY_ID)
                                + ":OWNER").stream().forEach(u -> accessibleUsers.add(u.userId));
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                sharingClient.getListOfSharedUsers(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        resourceId, authzToken.getClaimsMap().get(Constants.GATEWAY_ID)
                                + ":READ").stream().forEach(u -> accessibleUsers.add(u.userId));
                sharingClient.getListOfSharedUsers(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        resourceId, authzToken.getClaimsMap().get(Constants.GATEWAY_ID)
                                + ":OWNER").stream().forEach(u -> accessibleUsers.add(u.userId));
            } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
                sharingClient.getListOfSharedUsers(authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                        resourceId, authzToken.getClaimsMap().get(Constants.GATEWAY_ID)
                                + ":OWNER").stream().forEach(u -> accessibleUsers.add(u.userId));
            }
            registryClientPool.returnResource(regClient);
            sharingClientPool.returnResource(sharingClient);
            return new ArrayList<>(accessibleUsers);
        } catch (Exception e) {
            String msg = "Error in getting all accessible users for resource. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
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
    public String createGroup(AuthzToken authzToken, GroupModel groupModel) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = sharingClientPool.getResource();

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(UUID.randomUUID().toString());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setDomainId(authzToken.getClaimsMap().get(Constants.GATEWAY_ID));

            String groupId = sharingClient.createGroup(sharingUserGroup);
            sharingClient.addUsersToGroup(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupModel.getMembers(), groupId);
            return groupId;
        } catch (Exception e) {
            String msg = "Error Creating Group" ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = sharingClientPool.getResource();

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(groupModel.getId());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setDomainId(authzToken.getClaimsMap().get(Constants.GATEWAY_ID));

            //adding and removal of users should be handle separately
            sharingClient.updateGroup(sharingUserGroup);
            return true;
        } catch (Exception e) {
            String msg = "Error Updating Group" ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId) throws
            InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = sharingClientPool.getResource();

            sharingClient.deleteGroup(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId);
            return true;
        } catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = sharingClientPool.getResource();
            UserGroup userGroup = sharingClient.getGroup(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId);

            GroupModel groupModel = new GroupModel();
            groupModel.setId(userGroup.getGroupId());
            groupModel.setName(userGroup.getName());
            groupModel.setDescription(userGroup.getDescription());
            groupModel.setOwnerId(userGroup.getOwnerId());

            sharingClient.getGroupMembersOfTypeUser(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, 0, -1).stream().forEach(user->
                    groupModel.addToMembers(user.getUserId())
            );

            return groupModel;
        } catch (Exception e) {
            String msg = "Error Retreiving Group. Group ID: " + groupId ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            throw new UnsupportedOperationException("Method not supported yet");
        } catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    private void startPostProcessingOfExperiment(String gatewayId, String experimentId) throws AiravataException {
        ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
        MessageContext messageContext = new MessageContext(event, MessageType.POSTPROCESSING_START, "POSTPROCESS.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    private void submitExperiment(String gatewayId,String experimentId) throws AiravataException {
        ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
        MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    private void submitCancelExperiment(String gatewayId, String experimentId) throws AiravataException {
        ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
        MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT_CANCEL, "CANCEL.EXP-" + UUID.randomUUID().toString(), gatewayId);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        experimentPublisher.publish(messageContext);
    }

    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }

    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }

    private SharingRegistryService.Client getSharingRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getSharingRegistryPort());
        final String serverHost = ServerSettings.getSharingRegistryHost();
        try {
            return SharingRegistryServiceClientFactory.createSharingRegistryClient(serverHost, serverPort);
        } catch (SharingRegistryException e) {
            throw new TException("Unable to create sharing registry client...", e);
        }
    }
}
