/*
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
 *
 */

package org.apache.airavata.api.server.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavata_apiConstants;
import org.apache.airavata.api.server.security.interceptor.SecurityCheck;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.datamodel.PasswordCredential;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.grouper.GroupManagerCPI;
import org.apache.airavata.grouper.GroupManagerException;
import org.apache.airavata.grouper.GroupManagerFactory;
import org.apache.airavata.grouper.SubjectType;
import org.apache.airavata.grouper.group.Group;
import org.apache.airavata.grouper.permission.PermissionAction;
import org.apache.airavata.grouper.resource.Resource;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.airavata_commonsConstants;
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
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService.Client;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
    private Publisher publisher;
    private ReplicaCatalog dataCatalog;
	private WorkflowCatalog workflowCatalog;
    private CredentialStoreService.Client csClient;

    public AiravataServerHandler() {
        try {
            publisher = PublisherFactory.createActivityPublisher();
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
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
        try {
            return ExpCatResourceUtils.isUserExist(userName, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while verifying user", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while verifying user. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            appCatalog = RegistryFactory.getAppCatalog();
            if (!validateString(gateway.getGatewayId())){
                logger.error("Gateway id cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            String gatewayId = (String) experimentCatalog.add(ExpCatParentDataType.GATEWAY, gateway, gateway.getGatewayId());
            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(gatewayId);
            appCatalog.getGatewayProfile().addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata added gateway with gateway id : " + gateway.getGatewayId());
            return gatewayId;
        } catch (RegistryException e) {
            logger.error("Error while adding gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding gateway. More info : " + e.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error("Error while adding gateway profile", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding gateway profile. More info : " + e.getMessage());
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
        try {
            return ExpCatResourceUtils.getAllUsersInGateway(gatewayId);
        } catch (RegistryException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            experimentCatalog.update(ExperimentCatalogModelType.GATEWAY, updatedGateway, gatewayId);
            logger.debug("Airavata update gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            Gateway gateway = (Gateway) experimentCatalog.get(ExperimentCatalogModelType.GATEWAY, gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : " + gateway.getGatewayId());
            return gateway;
        } catch (RegistryException e) {
            logger.error("Error while getting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            experimentCatalog.remove(ExperimentCatalogModelType.GATEWAY, gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException {
        try {
            List<Gateway> gateways = new ArrayList<Gateway>();
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.GATEWAY, null, null);
            for (Object gateway : list){
                gateways.add((Gateway)gateway);
            }
            logger.debug("Airavata retrieved all available gateways...");
            return gateways;
        } catch (RegistryException e) {
            logger.error("Error while getting all the gateways", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
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
    public String createNotification(AuthzToken authzToken, Notification notification) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(notification.getGatewayId());
            return (String) experimentCatalog.add(ExpCatParentDataType.NOTIFICATION, notification, notification.getGatewayId());
        } catch (RegistryException e) {
            logger.error("Error while creating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(notification.getGatewayId());
            experimentCatalog.update(ExperimentCatalogModelType.NOTIFICATION, notification, notification.getGatewayId());
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            experimentCatalog.remove(ExperimentCatalogModelType.NOTIFICATION, notificationId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return (Notification)experimentCatalog.get(ExperimentCatalogModelType.NOTIFICATION, notificationId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving notification", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retreiving notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            List<Object> objectList = experimentCatalog.get(ExperimentCatalogModelType.NOTIFICATION, null, gatewayId);
            List<Notification> notifications = new ArrayList<>();
            for(Object o : objectList)
                notifications.add((Notification) o);
            return notifications;
        } catch (RegistryException e) {
            logger.error("Error while getting all notifications", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all notifications. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*Following method wraps the logic of isGatewayExist method and this is to be called by any other method of the API as needed.*/
    private boolean isGatewayExistInternal(String gatewayId) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException{
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String gatewayId, String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername(userName);
            sshCredential.setGatewayId(gatewayId);
            String key = csClient.addSSHCredential(sshCredential);
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            return key;
        }catch (Exception e){
            logger.error("Error occurred while registering SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering SSH Credential. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Generate and Register Username PWD Pair with Airavata Credential Store.
     *
     * @param authzToken
     * @param gatewayId  The identifier for the requested Gateway.
     * @param userName   The User for which the credential should be registered. For community accounts, this user is the name of the
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
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            PasswordCredential pwdCredential = new PasswordCredential();
            pwdCredential.setPortalUserName(portalUserName);
            pwdCredential.setLoginUserName(loginUserName);
            pwdCredential.setPassword(password);
            pwdCredential.setDescription(description);
            pwdCredential.setGatewayId(gatewayId);
            String key = csClient.addPasswordCredential(pwdCredential);
            logger.debug("Airavata generated PWD credential for gateway : " + gatewayId + " and for user : " + loginUserName);
            return key;
        }catch (Exception e){
            logger.error("Error occurred while registering PWD Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering PWD Credential. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String getSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            SSHCredential sshCredential = csClient.getSSHCredential(airavataCredStoreToken, gatewayId);
            logger.debug("Airavata retrieved SSH pub key for gateway id : " + gatewayId + " and for token : " + airavataCredStoreToken);
            return sshCredential.getPublicKey();
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH credential. More info : " + e.getMessage());
            throw exception;
        }
    }


    @Override
    @SecurityCheck
    public Map<String, String> getAllGatewaySSHPubKeys(AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            Map<String, String> allSSHKeysForGateway = csClient.getAllSSHKeysForGateway(gatewayId);
            logger.debug("Airavata retrieved all SSH pub keys for gateway Id : " + gatewayId);
            return allSSHKeysForGateway;
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH public keys for gateway : " + gatewayId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH public keys for gateway : " + gatewayId + ". More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllGatewayPWDCredentials(AuthzToken authzToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            Map<String, String> allPwdCredentials = csClient.getAllPWDCredentialsForGateway(gatewayId);
            logger.debug("Airavata retrieved all PWD Credentials for gateway Id : " + gatewayId);
            return allPwdCredentials;
        }catch (Exception e){
            logger.error("Error occurred while retrieving PWD Credentials for gateway : " + gatewayId , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving PWD Credentials for gateway : " + gatewayId + ". More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            return csClient.deleteSSHCredential(airavataCredStoreToken, gatewayId);
        }catch (Exception e){
            logger.error("Error occurred while deleting SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting SSH credential. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            logger.debug("Airavata deleted PWD credential for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            return csClient.deletePWDCredential(airavataCredStoreToken, gatewayId);
        }catch (Exception e){
            logger.error("Error occurred while deleting PWD credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while deleting PWD credential. More info : " + e.getMessage());
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

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!validateString(project.getName()) || !validateString(project.getOwner())){
                logger.error("Project name and owner cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            if (!validateString(gatewayId)){
                logger.error("Gateway ID cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            if (!isGatewayExistInternal(gatewayId)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            String projectId = (String) experimentCatalog.add(ExpCatParentDataType.PROJECT, project, gatewayId);
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            Resource projResource = new Resource(projectId, org.apache.airavata.grouper.resource.ResourceType.PROJECT);
            projResource.setOwnerId(project.getOwner() + "@" + project.getGatewayId());
            projResource.setName(project.getName());
            projResource.setDescription(project.getDescription());
            groupManager.createResource(projResource);
            logger.debug("Airavata created project with project Id : " + projectId + " for gateway Id : " + gatewayId);
            return projectId;
        } catch (Exception e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {

        if (!validateString(projectId) || !validateString(projectId)){
            logger.error("Project id cannot be empty...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty...");
            throw exception;
        }
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }

            Project existingProject = (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, projectId);
            if(!authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(existingProject.getGatewayId())){
                try {
                    if(!hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            existingProject.getProjectID(), ResourceType.PROJECT, ResourcePermissionType.WRITE)){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            experimentCatalog.update(ExperimentCatalogModelType.PROJECT, updatedProject, projectId);
            logger.debug("Airavata updated project with project Id : " + projectId );
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            throw exception;
        }

    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }

            Project existingProject = (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, projectId);
            if(!authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(existingProject.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(existingProject.getGatewayId())){
                try {
                    if(!hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            existingProject.getProjectID(), ResourceType.PROJECT, ResourcePermissionType.WRITE)){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            experimentCatalog.remove(ExperimentCatalogModelType.PROJECT, projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId );
            return true;
        } catch (RegistryException e) {
            logger.error("Error while removing the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while removing the project. More info : " + e.getMessage());
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
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            logger.debug("Airavata retrieved project with project Id : " + projectId );

            Project project = (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, projectId);
            if(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(project.getOwner())
                    && authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(project.getGatewayId())){
                return project;
            }else{
                try {
                    if(hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            project.getProjectID(), ResourceType.PROJECT, ResourcePermissionType.READ)){
                        return project;
                    }else {
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }
        } catch (RegistryException e) {
            logger.error("Error while retrieving the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
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
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        List<Project> projects = new ArrayList<Project>();
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            List<Object> list = experimentCatalog.search(ExperimentCatalogModelType.PROJECT, filters, limit, offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    projects.add((Project) o);
                }
            }
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId );
            return projects;
        } catch (RegistryException e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
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
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<Project> projects = new ArrayList<Project>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> regFilters = new HashMap<String, String>();
            regFilters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            for(Map.Entry<ProjectSearchFields, String> entry : filters.entrySet())
            {
                if(entry.getKey().equals(ProjectSearchFields.PROJECT_NAME)){
                    regFilters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, entry.getValue());
                }else if(entry.getKey().equals(ProjectSearchFields.PROJECT_DESCRIPTION)){
                    regFilters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, entry.getValue());
                }
            }
            List<String> accessibleProjIds  = new ArrayList<>();
            accessibleProjIds.addAll(getAllAccessibleResourcesForUser(userName+"@"+gatewayId, ResourceType.PROJECT, ResourcePermissionType.READ));

            if(accessibleProjIds.size() == 0)
                return new ArrayList<>();

            List<Object> results = experimentCatalog.searchAllAccessible(ExperimentCatalogModelType.PROJECT, accessibleProjIds,
                    regFilters, limit, offset, Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                projects.add((Project)object);
            }
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
            return projects;
        }catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
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
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> regFilters = new HashMap();
            regFilters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            for(Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet())
            {
                if(entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_NAME)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_DESC)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.DESCRIPTION, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.APPLICATION_ID)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.STATUS)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.FROM_DATE)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.TO_DATE)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.PROJECT_ID)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.PROJECT_ID, entry.getValue());
                }
            }
            List<String> accessibleExpIds = new ArrayList<>();
            accessibleExpIds.addAll(getAllAccessibleResourcesForUser(userName + "@" + gatewayId, ResourceType.EXPERIMENT, ResourcePermissionType.READ));

            if(accessibleExpIds.size() == 0)
                return new ArrayList<>();

            List<Object> results = experimentCatalog.searchAllAccessible(ExperimentCatalogModelType.EXPERIMENT,
                    accessibleExpIds, regFilters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            logger.debug("Airavata retrieved experiments for user : " + userName + " and gateway id : " + gatewayId );
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
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
    public ExperimentStatistics getExperimentStatistics(AuthzToken authzToken, String gatewayId, long fromTime, long toTime)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            Map<String, String> filters = new HashMap();
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, fromTime+"");
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, toTime+"");

            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT_STATISTICS, filters);
            logger.debug("Airavata retrieved experiments for gateway id : " + gatewayId + " between : " + AiravataUtils.getTime(fromTime) + " and " + AiravataUtils.getTime(toTime));
            return (ExperimentStatistics) results.get(0);
        }catch (Exception e) {
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
        if (!validateString(projectId)){
            logger.error("Project id cannot be empty. Please provide a valid project ID...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty. Please provide a valid project ID...");
            throw exception;
        }
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }

            Project project = (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, projectId);
            if(!authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(project.getOwner())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(project.getGatewayId())){
                try {
                    if(!hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            project.getProjectID(), ResourceType.PROJECT, ResourcePermissionType.READ)){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID, projectId, limit, offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()) {
                for (Object o : list) {
                    experiments.add((ExperimentModel) o);
                }
            }
            logger.debug("Airavata retrieved experiments for project : " + projectId);
            return experiments;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
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
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT,
                    Constants.FieldConstants.ExperimentConstants.USER_NAME, userName, limit, offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    experiments.add((ExperimentModel)o);
                }
            }
            logger.debug("Airavata retrieved experiments for user : " + userName);
            return experiments;
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
    public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            appCatalog = RegistryFactory.getAppCatalog();
            if (!validateString(experiment.getExperimentName())){
                logger.error("Cannot create experiments with empty experiment name");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Cannot create experiments with empty experiment name");
                throw exception;
            }
            if (!isGatewayExistInternal(gatewayId)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }

            if(experiment.getUserConfigurationData() != null && experiment.getUserConfigurationData()
                .getComputationalResourceScheduling() != null){

                String compResourceId = experiment.getUserConfigurationData()
                    .getComputationalResourceScheduling().getResourceHostId();
                ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                    .getComputeResource(compResourceId);
                if(!computeResourceDescription.isEnabled()){
                    logger.error("Compute Resource is not enabled by the Admin!");
                    AiravataSystemException exception = new AiravataSystemException();
                    exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                    exception.setMessage("Compute Resource is not enabled by the Admin!");
                    throw exception;
                }
            }

            String experimentId = (String) experimentCatalog.add(ExpCatParentDataType.EXPERIMENT, experiment, gatewayId);
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            Resource expResource = new Resource(experimentId, org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT);
            expResource.setOwnerId(experiment.getUserName()+"@"+experiment.getGatewayId());
            expResource.setParentResourceId(experiment.getProjectId());
            expResource.setName(experiment.getExperimentName());
            expResource.setDescription(experiment.getDescription());
            groupManager.createResource(expResource);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.CREATED,
                    experimentId,
                    gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            if(publisher!=null) {
                publisher.publish(messageContext);
            }
            logger.debug(experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getExperimentName());
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
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
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, experimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);

            if(!authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                    || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
                try {
                    if(! hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            experimentModel.getExperimentId(), ResourceType.EXPERIMENT, ResourcePermissionType.WRITE)){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            if(!(experimentModel.getExperimentStatus().getState() == ExperimentState.CREATED)){
                logger.error("Error while deleting the experiment");
                throw new ExperimentCatalogException("Experiment is not in CREATED state. Hence cannot deleted. ID:"+ experimentId);
            }
            experimentCatalog.remove(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            logger.debug("Airavata removed experiment with experiment id : " + experimentId);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the experiment. More info : " + e.getMessage());
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
        ExperimentModel experimentModel = getExperimentInternal(airavataExperimentId);
        if(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                && authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
            return experimentModel;
        }else{
            try {
                if(hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                        experimentModel.getExperimentId(), ResourceType.EXPERIMENT, ResourcePermissionType.READ)){
                    return experimentModel;
                }else {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            } catch (Exception e) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
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
        try {
            ExperimentModel experimentModel =  getExperimentInternal(airavataExperimentId);
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            List<Object> processObjects  = experimentCatalog.get(ExperimentCatalogModelType.PROCESS,
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
            List<ProcessModel> processList = new ArrayList<>();
            if(processObjects != null){
                processObjects.stream().forEach(p -> {
                    //Process already has the task object
                    ((ProcessModel)p).getTasks().stream().forEach(t->{
                        try {
                            List<Object> jobObjects = experimentCatalog.get(ExperimentCatalogModelType.JOB,
                                    Constants.FieldConstants.JobConstants.TASK_ID, ((TaskModel)t).getTaskId());
                            List<JobModel> jobList  = new ArrayList<JobModel>();
                            if(jobObjects != null){
                                jobObjects.stream().forEach(j -> jobList.add((JobModel)j));
                                Collections.sort(jobList, new Comparator<JobModel>() {
                                    @Override
                                    public int compare(JobModel o1, JobModel o2) {
                                        return (int) (o1.getCreationTime() - o2.getCreationTime());
                                    }
                                });
                                t.setJobs(jobList);
                            }
                        } catch (RegistryException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
                    processList.add((ProcessModel)p);
                });
                experimentModel.setProcesses(processList);
            }
            logger.debug("Airavata retrieved detailed experiment with experiment id : " + airavataExperimentId);
            return experimentModel;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*This private method wraps the logic of getExperiment method as this method is called internally in the API.*/
    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
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
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Update request failed, Experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }

            ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId);
            if(!authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME).equals(experimentModel.getUserName())
                || !authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID).equals(experimentModel.getGatewayId())){
                try {
                    if(! hasPermission(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                                    +"@"+authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID),
                            experimentModel.getExperimentId(), ResourceType.EXPERIMENT, ResourcePermissionType.WRITE)){
                        throw new AuthorizationException("User does not have permission to access this resource");
                    }
                } catch (Exception e) {
                    throw new AuthorizationException("User does not have permission to access this resource");
                }
            }

            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED:
                        if(experiment.getUserConfigurationData() != null && experiment.getUserConfigurationData()
                            .getComputationalResourceScheduling() != null){
                            String compResourceId = experiment.getUserConfigurationData()
                                .getComputationalResourceScheduling().getResourceHostId();
                            ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                                .getComputeResource(compResourceId);
                            if(!computeResourceDescription.isEnabled()){
                                logger.error("Compute Resource is not enabled by the Admin!");
                                AiravataSystemException exception = new AiravataSystemException();
                                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                                exception.setMessage("Compute Resource is not enabled by the Admin!");
                                throw exception;
                            }
                        }
                        experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment, airavataExperimentId);
                        logger.debug(airavataExperimentId, "Successfully updated experiment {} ", experiment.getExperimentName());
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (RegistryException e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Update experiment configuration failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED:
                        experimentCatalog.add(ExpCatChildDataType.USER_CONFIGURATION_DATA, userConfiguration, airavataExperimentId);
                        logger.debug(airavataExperimentId, "Successfully updated experiment configuration for experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating experiment {}. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ", airavataExperimentId);
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating user configuration", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user configuration. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(AuthzToken authzToken, String airavataExperimentId,
                                          ComputationalResourceSchedulingModel resourceScheduling) throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.debug(airavataExperimentId, "Update resource scheduling failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED:
                        experimentCatalog.add(ExpCatChildDataType.PROCESS_RESOURCE_SCHEDULE, resourceScheduling, airavataExperimentId);
                        logger.debug(airavataExperimentId, "Successfully updated resource scheduling for the experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating scheduling info. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating scheduling info", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating scheduling info. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
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
    public boolean validateExperiment(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
     	try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
 			if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
        } catch (RegistryException e1) {
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
        }


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
    public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : " + airavataExperimentId);
        return experimentStatus;
    }

    /*Private method wraps the logic of getExperimentStatus method since this method is called internally.*/
    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving experiment status, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId +
                        " does not exist in the system..");
            }
            return (ExperimentStatus) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT_STATUS, airavataExperimentId);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Get experiment outputs failed, experiment {} doesn't exit.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            logger.debug("Airavata retrieved experiment outputs for experiment id : " + airavataExperimentId);
            return (List<OutputDataObjectType>) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT_OUTPUT, airavataExperimentId);
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
    public List<OutputDataObjectType> getIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return null;
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving job details, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<Object> processModels = experimentCatalog.get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            Map<String, JobStatus> jobStatus = new HashMap<String, JobStatus>();
            if (processModels != null && !processModels.isEmpty()){
                for (Object process : processModels) {
                    ProcessModel processModel = (ProcessModel) process;
                    List<TaskModel> tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()){
                      for (TaskModel task : tasks){
                          String taskId =  task.getTaskId();
                          List<Object> jobs = experimentCatalog.get(ExperimentCatalogModelType.JOB, Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                          if (jobs != null && !jobs.isEmpty()){
                              for (Object jobObject : jobs) {
                                  JobModel jobModel = (JobModel) jobObject;
                                  String jobID = jobModel.getJobId();
                                  JobStatus status = jobModel.getJobStatus();
                                  if (status != null){
                                      jobStatus.put(jobID, status);
                                  }
                              }
                          }
                      }
                    }
                }
            }
            logger.debug("Airavata retrieved job statuses for experiment with experiment id : " + airavataExperimentId);
            return jobStatus;
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
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving job details, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<Object> processModels = experimentCatalog.get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            List<JobModel> jobList = new ArrayList<>();
            if (processModels != null && !processModels.isEmpty()){
                for (Object process : processModels) {
                    ProcessModel processModel = (ProcessModel) process;
                    List<TaskModel> tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()){
                        for (TaskModel taskModel : tasks){
                            String taskId =  taskModel.getTaskId();
                            List<Object> jobs = experimentCatalog.get(ExperimentCatalogModelType.JOB, Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            for (Object jobObject : jobs) {
                                jobList.add ((JobModel)jobObject);
                            }
                        }
                    }
                }
            }
            logger.debug("Airavata retrieved job models for experiment with experiment id : " + airavataExperimentId);
            return jobList;
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
            throws AuthorizationException, TException {
    	try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            appCatalog = RegistryFactory.getAppCatalog();
			if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Error while launching experiment, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)) {
                logger.error(airavataExperimentId, "Error while launching experiment, gatewayId {} doesn't exist.", gatewayId);
                throw new ExperimentNotFoundException("Requested gateway id " + gatewayId + " does not exist in the system..");
            }
            ExperimentModel experiment = getExperimentInternal(airavataExperimentId);
            String applicationID = experiment.getExecutionId();
            if (!appCatalog.getApplicationInterface().isApplicationInterfaceExists(applicationID)){
                logger.error(airavataExperimentId, "Error while launching experiment, application id {} for experiment {} doesn't exist.", applicationID, airavataExperimentId);
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Error while launching experiment, application id : " + applicationID  + " for experiment : " + airavataExperimentId +
                        " doesn't exist..");
                throw exception;
            }
            OrchestratorService.Client orchestratorClient = getOrchestratorClient();
            if (orchestratorClient.validateExperiment(airavataExperimentId)) {
                orchestratorClient.launchExperiment(airavataExperimentId, gatewayId);
                logger.debug("Airavata launched experiment with experiment id : " + airavataExperimentId);
            }else {
                logger.error(airavataExperimentId, "Couldn't identify experiment type, experiment {} is neither single application nor workflow.", airavataExperimentId);
                throw new InvalidRequestException("Experiment '" + airavataExperimentId + "' launch failed. Unable to figureout execution type for application " + applicationID);
            }
        } catch (RegistryException e1) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e1.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e.getMessage());
        }
    }


    private OrchestratorService.Client getOrchestratorClient() throws TException {
	    try {
		    final String serverHost = ServerSettings.getOrchestratorServerHost();
		    final int serverPort = ServerSettings.getOrchestratorServerPort();
		    return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
	    } catch (AiravataException e) {
		    throw new TException(e);
	    }
    }

    /**
     * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
     *   The client has to subsequently update this configuration if needed and launch the cloned experiment.
     *
     * @param existingExperimentID
     *    This is the experiment identifier that already exists in the system. Will use this experimentID to retrieve
     *    user configuration which is used with the clone experiment.
     *
     * @param newExperiementName
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
     * @param newExperiementName
     */
    @Override
    @SecurityCheck
    public String cloneExperiment(AuthzToken authzToken, String existingExperimentID, String newExperiementName)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID)){
                logger.error(existingExperimentID, "Error while cloning experiment {}, experiment doesn't exist.", existingExperimentID);
                throw new ExperimentNotFoundException("Requested experiment id " + existingExperimentID + " does not exist in the system..");
            }
            ExperimentModel existingExperiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID);
            String gatewayId = (String) experimentCatalog.getValue(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID, Constants.FieldConstants.ExperimentConstants.GATEWAY_ID);
            existingExperiment.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            if (existingExperiment.getExecutionId() != null){
                List<OutputDataObjectType> applicationOutputs = getApplicationOutputsInternal(existingExperiment.getExecutionId());
                existingExperiment.setExperimentOutputs(applicationOutputs);
            }
            if (validateString(newExperiementName)){
                existingExperiment.setExperimentName(newExperiementName);
            }
            if (existingExperiment.getErrors() != null ){
                existingExperiment.getErrors().clear();
            }
            if(existingExperiment.getUserConfigurationData() != null && existingExperiment.getUserConfigurationData()
                .getComputationalResourceScheduling() != null){
                String compResourceId = existingExperiment.getUserConfigurationData()
                    .getComputationalResourceScheduling().getResourceHostId();

                ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                    .getComputeResource(compResourceId);
                if(!computeResourceDescription.isEnabled()){
                    existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                }
            }
            logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
            existingExperiment.setUserName(authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME));
            String expId = (String) experimentCatalog.add(ExpCatParentDataType.EXPERIMENT, existingExperiment, gatewayId);

            String projectId = existingExperiment.getProjectId();
            if(!isResourceExistsInGrouper(projectId, ResourceType.PROJECT)){
                initializeResourceWithGrouper(projectId, ResourceType.PROJECT);
            }
            initializeResourceWithGrouper(expId, ResourceType.EXPERIMENT);
            return expId;
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            throw exception;
        }
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
    public void terminateExperiment(AuthzToken authzToken, String airavataExperimentId, String gatewayId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!(experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId))){
                logger.error("Experiment does not exist.Please provide a valid experiment id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            Client client = getOrchestratorClient();
            client.terminateExperiment(airavataExperimentId, gatewayId);
            logger.debug("Airavata cancelled experiment with experiment id : " + airavataExperimentId);
        } catch (RegistryException e) {
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
    public String registerApplicationModule(AuthzToken authzToken, String gatewayId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            String module = appCatalog.getApplicationInterface().addApplicationModule(applicationModule, gatewayId);
            logger.debug("Airavata registered application module for gateway id : " + gatewayId);
            return module;
        } catch (AppCatalogException e) {
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
    public ApplicationModule getApplicationModule(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ApplicationModule module = appCatalog.getApplicationInterface().getApplicationModule(appModuleId);
            logger.debug("Airavata retrieved application module with module id : " + appModuleId);
            return module;
        } catch (AppCatalogException e) {
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
    public boolean updateApplicationModule(AuthzToken authzToken, String appModuleId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationModule(appModuleId, applicationModule);
            logger.debug("Airavata updated application module with module id: " + appModuleId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<ApplicationModule> moduleList = appCatalog.getApplicationInterface().getAllApplicationModules(gatewayId);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            logger.debug("Airavata deleted application module with module id : " + appModuleId);
            return appCatalog.getApplicationInterface().removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
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
    public String registerApplicationDeployment(AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            String deployment = appCatalog.getApplicationDeployment().addApplicationDeployment(applicationDeployment, gatewayId);
            logger.debug("Airavata registered application deployment for gateway id : " + gatewayId);
            return deployment;
        } catch (AppCatalogException e) {
            logger.error("Error while adding application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application deployment. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ApplicationDeploymentDescription deployement = appCatalog.getApplicationDeployment().getApplicationDeployement(appDeploymentId);
            logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
            return deployement;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().updateApplicationDeployment(appDeploymentId, applicationDeployment);
            logger.debug("Airavata updated application deployment for deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while updating application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application deployment. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().removeAppDeployment(appDeploymentId);
            logger.debug("Airavata removed application deployment with deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while deleting application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
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
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<ApplicationDeploymentDescription> deployements = appCatalog.getApplicationDeployment().getAllApplicationDeployements(gatewayId);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
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
        try {
            List<String> appDeployments = new ArrayList<String>();
            appCatalog = RegistryFactory.getAppCatalog();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments = appCatalog.getApplicationDeployment().getApplicationDeployements(filters);
            for (ApplicationDeploymentDescription description : applicationDeployments){
                appDeployments.add(description.getAppDeploymentId());
            }
            logger.debug("Airavata retrieved application deployments for module id : " + appModuleId);
            return appDeployments;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
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
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            String interfaceId = appCatalog.getApplicationInterface().addApplicationInterface(applicationInterface, gatewayId);
            logger.debug("Airavata registered application interface for gateway id : " + gatewayId);
            return interfaceId;
        } catch (AppCatalogException e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            if (!appCatalog.getApplicationInterface().isApplicationInterfaceExists(existingAppInterfaceID)){
                logger.error("Provided application interface does not exist.Please provide a valid application interface id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            ApplicationInterfaceDescription existingInterface = appCatalog.getApplicationInterface().getApplicationInterface(existingAppInterfaceID);
            existingInterface.setApplicationName(newApplicationName);
            existingInterface.setApplicationInterfaceId(airavata_commonsConstants.DEFAULT_ID);
            String interfaceId = appCatalog.getApplicationInterface().addApplicationInterface(existingInterface, gatewayId);
            logger.debug("Airavata cloned application interface : " + existingAppInterfaceID + " for gateway id : " + gatewayId );
            return interfaceId;
        } catch (AppCatalogException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ApplicationInterfaceDescription interfaceDescription = appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
            logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
            return interfaceDescription;
        } catch (AppCatalogException e) {
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
    public boolean updateApplicationInterface(AuthzToken authzToken, String appInterfaceId,
                                              ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationInterface(appInterfaceId, applicationInterface);
            logger.debug("Airavata updated application interface with interface id : " + appInterfaceId);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean deleteApplicationInterface(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            boolean removeApplicationInterface = appCatalog.getApplicationInterface().removeApplicationInterface(appInterfaceId);
            logger.debug("Airavata removed application interface with interface id : " + appInterfaceId);
            return removeApplicationInterface;
        } catch (AppCatalogException e) {
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
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<ApplicationInterfaceDescription> allApplicationInterfaces = appCatalog.getApplicationInterface().getAllApplicationInterfaces(gatewayId);
            Map<String, String> allApplicationInterfacesMap = new HashMap<String, String>();
            if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()){
                for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces){
                    allApplicationInterfacesMap.put(interfaceDescription.getApplicationInterfaceId(), interfaceDescription.getApplicationName());
                }
            }
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return allApplicationInterfacesMap;
        } catch (AppCatalogException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<ApplicationInterfaceDescription> interfaces = appCatalog.getApplicationInterface().getAllApplicationInterfaces(gatewayId);
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return interfaces;
        } catch (AppCatalogException e) {
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
    public List<InputDataObjectType> getApplicationInputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<InputDataObjectType> applicationInputs = appCatalog.getApplicationInterface().getApplicationInputs(appInterfaceId);
            logger.debug("Airavata retrieved application inputs for application interface id : " + appInterfaceId);
            return applicationInputs;
        } catch (AppCatalogException e) {
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
    public List<OutputDataObjectType> getApplicationOutputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        List<OutputDataObjectType> list = getApplicationOutputsInternal(appInterfaceId);
        logger.debug("Airavata retrieved application outputs for app interface id : " + appInterfaceId);
        return list;
    }

    /*This private method wraps the logic of getApplicationOutputs method as this method is called internally in the API.*/
    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application outputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application outputs. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ApplicationDeployment applicationDeployment = appCatalog.getApplicationDeployment();
            Map<String, String> allComputeResources = appCatalog.getComputeResource().getAvailableComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface =
                    appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<String,String>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules) {
                    filters.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, moduleId);
                    List<ApplicationDeploymentDescription> applicationDeployments =
                            applicationDeployment.getApplicationDeployements(filters);
                    for (ApplicationDeploymentDescription deploymentDescription : applicationDeployments) {
                        if (allComputeResources.get(deploymentDescription.getComputeHostId()) != null){
                            availableComputeResources.put(deploymentDescription.getComputeHostId(),
                                    allComputeResources.get(deploymentDescription.getComputeHostId()));
                        }
                    }
                }
            }
            logger.debug("Airavata retrieved available compute resources for application interface id : " + appInterfaceId);
            return availableComputeResources;
        } catch (AppCatalogException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            String computeResource = appCatalog.getComputeResource().addComputeResource(computeResourceDescription);
            logger.debug("Airavata registered compute resource with compute resource Id : " + computeResource);
            return computeResource;
        } catch (AppCatalogException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResourceDescription computeResource = appCatalog.getComputeResource().getComputeResource(computeResourceId);
            logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
            return computeResource;
        } catch (AppCatalogException e) {
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
    public Map<String, String> getAllComputeResourceNames(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            Map<String, String> computeResourceIdList = appCatalog.getComputeResource().getAllComputeResourceIdList();
            logger.debug("Airavata retrieved all the available compute resources...");
            return computeResourceIdList;
        } catch (AppCatalogException e) {
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
    public boolean updateComputeResource(AuthzToken authzToken, String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().updateComputeResource(computeResourceId, computeResourceDescription);
            logger.debug("Airavata updated compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean deleteComputeResource(AuthzToken authzToken, String computeResourceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeComputeResource(computeResourceId);
            logger.debug("Airavata deleted compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
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
    public String registerStorageResource(AuthzToken authzToken, StorageResourceDescription storageResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            String storageResource = appCatalog.getStorageResource().addStorageResource(storageResourceDescription);
            logger.debug("Airavata registered storage resource with storage resource Id : " + storageResource);
            return storageResource;
        } catch (AppCatalogException e) {
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
    public StorageResourceDescription getStorageResource(AuthzToken authzToken, String storageResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            StorageResourceDescription storageResource = appCatalog.getStorageResource().getStorageResource(storageResourceId);
            logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
            return storageResource;
        } catch (AppCatalogException e) {
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
    public Map<String, String> getAllStorageResourceNames(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            Map<String, String> resourceIdList = appCatalog.getStorageResource().getAllStorageResourceIdList();
            logger.debug("Airavata retrieved storage resources list...");
            return resourceIdList;
        } catch (AppCatalogException e) {
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
    public boolean updateStorageResource(AuthzToken authzToken, String storageResourceId, StorageResourceDescription storageResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getStorageResource().updateStorageResource(storageResourceId, storageResourceDescription);
            logger.debug("Airavata updated storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean deleteStorageResource(AuthzToken authzToken, String storageResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getStorageResource().removeStorageResource(storageResourceId);
            logger.debug("Airavata deleted storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(storageResourceId, "Error while deleting storage resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting storage resource. More info : " + e.getMessage());
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
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String submissionInterface = addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addLocalJobSubmission(localSubmission), JobSubmissionProtocol.LOCAL, priorityOrder);
            logger.debug("Airavata added local job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    	try {
            LocalSubmissionResource submission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated local job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            LOCALSubmission localJobSubmission = appCatalog.getComputeResource().getLocalJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
            return localJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    private String addJobSubmissionInterface(ComputeResource computeResource,
			String computeResourceId, String jobSubmissionInterfaceId,
			JobSubmissionProtocol protocolType, int priorityOrder)
			throws AppCatalogException {
		JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
		jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
		jobSubmissionInterface.setPriorityOrder(priorityOrder);
		jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
		return computeResource.addJobSubmissionProtocol(computeResourceId,jobSubmissionInterface);
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
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String submissionInterface = addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH, priorityOrder);
            logger.debug("Airavata registered SSH job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String submissionDetails = addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH_FORK, priorityOrder);
            logger.debug("Airavata registered Fork job submission for compute resource id: " + computeResourceId);
            return submissionDetails;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            SSHJobSubmission sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
            return sshJobSubmission;
        } catch (AppCatalogException e) {
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
    public String addCloudJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                               CloudJobSubmission cloudJobSubmission) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String submissionInterface = addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addCloudJobSubmission(cloudJobSubmission), JobSubmissionProtocol.CLOUD, priorityOrder);
            logger.debug("Airavata registered Cloud job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            CloudJobSubmission cloudJobSubmission = appCatalog.getComputeResource().getCloudJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
            return cloudJobSubmission;
        } catch (AppCatalogException e) {
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
	public String addUNICOREJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                                 UnicoreJobSubmission unicoreJobSubmission)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
	        appCatalog = RegistryFactory.getAppCatalog();
	        ComputeResource computeResource = appCatalog.getComputeResource();
            String submissionInterface = addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addUNICOREJobSubmission(unicoreJobSubmission), JobSubmissionProtocol.UNICORE, priorityOrder);
            logger.debug("Airavata registered UNICORE job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
	    } catch (AppCatalogException e) {
	        logger.error("Error while adding job submission interface to resource compute resource...", e);
	        AiravataSystemException exception = new AiravataSystemException();
	        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
	        exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
	        throw exception;
	    }
	}

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            UnicoreJobSubmission unicoreJobSubmission = appCatalog.getComputeResource().getUNICOREJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
            return unicoreJobSubmission;
        } catch (AppCatalogException e) {
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
    public boolean updateSSHJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            SshJobSubmissionResource submission = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated SSH job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
        try {
            CloudSubmissionResource submission = AppCatalogThriftConversion.getCloudJobSubmission(cloudJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated Cloud job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            UnicoreJobSubmissionResource submission = AppCatalogThriftConversion.getUnicoreJobSubmission(unicoreJobSubmission);
            submission.setjobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated UNICORE job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String movementInterface = addDataMovementInterface(computeResource, resourceId, dmType,
                    computeResource.addLocalDataMovement(localDataMovement), DataMovementProtocol.LOCAL, priorityOrder);
            logger.debug("Airavata registered local data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource. More info : " + e.getMessage());
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
    	try {
            LocalDataMovementResource movment = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated local data movement with data movement id: " + dataMovementInterfaceId);
            return true;
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
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            LOCALDataMovement localDataMovement = appCatalog.getComputeResource().getLocalDataMovement(dataMovementId);
            logger.debug("Airavata retrieved local data movement with data movement id: " + dataMovementId);
            return localDataMovement;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    private String addDataMovementInterface(ComputeResource computeResource,
			String computeResourceId, DMType dmType, String dataMovementInterfaceId,
			DataMovementProtocol protocolType, int priorityOrder)
			throws AppCatalogException {
		DataMovementInterface dataMovementInterface = new DataMovementInterface();
		dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
		dataMovementInterface.setPriorityOrder(priorityOrder);
		dataMovementInterface.setDataMovementProtocol(protocolType);
		return computeResource.addDataMovementProtocol(computeResourceId, dmType, dataMovementInterface);
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
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String movementInterface = addDataMovementInterface(computeResource, resourceId, dmType,
                    computeResource.addScpDataMovement(scpDataMovement), DataMovementProtocol.SCP, priorityOrder);
            logger.debug("Airavata registered SCP data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
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
    	try {
            ScpDataMovementResource movment = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated SCP data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            SCPDataMovement scpDataMovement = appCatalog.getComputeResource().getSCPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved SCP data movement with data movement id: " + dataMovementId);
            return scpDataMovement;
        } catch (AppCatalogException e) {
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
    public String addUnicoreDataMovementDetails(AuthzToken authzToken, String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String movementInterface = addDataMovementInterface(computeResource, resourceId, dmType,
                    computeResource.addUnicoreDataMovement(unicoreDataMovement), DataMovementProtocol.UNICORE_STORAGE_SERVICE, priorityOrder);
            logger.debug("Airavata registered UNICORE data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            UnicoreDataMovementResource movment = AppCatalogThriftConversion.getUnicoreDMResource(unicoreDataMovement);
            movment.setDataMovementId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated UNICORE data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            UnicoreDataMovement unicoreDataMovement = appCatalog.getComputeResource().getUNICOREDataMovement(dataMovementId);
            logger.debug("Airavata retrieved UNICORE data movement with data movement id: " + dataMovementId);
            return unicoreDataMovement;
        } catch (AppCatalogException e) {
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
    public String addGridFTPDataMovementDetails(AuthzToken authzToken, String computeResourceId, DMType dmType, int priorityOrder,
                                                GridFTPDataMovement gridFTPDataMovement) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            String addDataMovementInterface = addDataMovementInterface(computeResource, computeResourceId, dmType,
                    computeResource.addGridFTPDataMovement(gridFTPDataMovement), DataMovementProtocol.GridFTP, priorityOrder);
            logger.debug("Airavata registered GridFTP data movement for resource Id: " + computeResourceId);
            return addDataMovementInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
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
    	try {
            GridftpDataMovementResource movment = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated GRIDFTP data movement with data movement id: " + dataMovementInterfaceId );
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            GridFTPDataMovement gridFTPDataMovement = appCatalog.getComputeResource().getGridFTPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved GRIDFTP data movement with data movement id: " + dataMovementId);
            return gridFTPDataMovement;
        } catch (AppCatalogException e) {
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
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            logger.debug("Airavata deleted job submission interface with interface id : " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean deleteDataMovementInterface(AuthzToken authzToken, String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            switch (dmType){
                case COMPUTE_RESOURCE:
                    appCatalog.getComputeResource().removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                case STORAGE_RESOURCE:
                    appCatalog.getStorageResource().removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                default:
                    logger.error("Unsupported data movement type specifies.. Please provide the correct data movement type... ");
                    return false;
            }
        } catch (AppCatalogException e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken,String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!validateString(gatewayResourceProfile.getGatewayID())){
                logger.error("Cannot create gateway profile with empty gateway id");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Cannot create gateway profile with empty gateway id");
                throw exception;
            }
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            String resourceProfile = gatewayProfile.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata registered gateway profile with gateway id : " + gatewayResourceProfile.getGatewayID());
            return resourceProfile;
        } catch (AppCatalogException e) {
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
    public GatewayResourceProfile getGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            GatewayResourceProfile gatewayResourceProfile = gatewayProfile.getGatewayProfile(gatewayID);
            logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
            return gatewayResourceProfile;
        } catch (AppCatalogException e) {
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
    public boolean updateGatewayResourceProfile(AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            logger.debug("Airavata deleted gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
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
    public boolean addGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                       ComputeResourcePreference computeResourcePreference) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
            	throw new AppCatalogException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            profile.addToComputeResourcePreferences(computeResourcePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            logger.debug("Airavata added gateway compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                throw new AppCatalogException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            profile.addToStoragePreferences(dataStoragePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
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
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            ComputeResource computeResource = appCatalog.getComputeResource();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }
            if (!computeResource.isComputeResourceExists(computeResourceId)){
                logger.error(computeResourceId, "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw exception;
            }
            ComputeResourcePreference computeResourcePreference = gatewayProfile.getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Airavata retrieved gateway compute resource preference with gateway id : " + gatewayID + " and for compute resoruce id : " + computeResourceId );
            return computeResourcePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }

            StoragePreference storagePreference = gatewayProfile.getStoragePreference(gatewayID, storageId);
            logger.debug("Airavata retrieved storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
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
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID).getStoragePreferences();
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getAllGatewayProfiles();
        } catch (AppCatalogException e) {
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
    public boolean updateGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                          ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
            List<ComputeResourcePreference> computeResourcePreferences = profile.getComputeResourcePreferences();
            ComputeResourcePreference preferenceToRemove = null;
            for (ComputeResourcePreference preference : computeResourcePreferences) {
				if (preference.getComputeResourceId().equals(computeResourceId)){
					preferenceToRemove=preference;
					break;
				}
			}
            if (preferenceToRemove!=null) {
				profile.getComputeResourcePreferences().remove(
						preferenceToRemove);
			}
            profile.getComputeResourcePreferences().add(computeResourcePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
            List<StoragePreference> dataStoragePreferences = profile.getStoragePreferences();
            StoragePreference preferenceToRemove = null;
            for (StoragePreference preference : dataStoragePreferences) {
                if (preference.getStorageResourceId().equals(storageId)){
                    preferenceToRemove=preference;
                    break;
                }
            }
            if (preferenceToRemove!=null) {
                profile.getStoragePreferences().remove(
                        preferenceToRemove);
            }
            profile.getStoragePreferences().add(dataStoragePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
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
    public boolean deleteGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.removeDataStoragePreferenceFromGateway(gatewayID, storageId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
	public List<String> getAllWorkflows(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
			return getWorkflowCatalog().getAllWorkflows(gatewayId);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in retrieving all workflow template Ids.";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public WorkflowModel getWorkflow(AuthzToken authzToken, String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().getWorkflow(workflowTemplateId);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in retrieving the workflow "+workflowTemplateId+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public void deleteWorkflow(AuthzToken authzToken, String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			getWorkflowCatalog().deleteWorkflow(workflowTemplateId);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in deleting the workflow "+workflowTemplateId+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public String registerWorkflow(AuthzToken authzToken, String gatewayId, WorkflowModel workflow)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
			return getWorkflowCatalog().registerWorkflow(workflow, gatewayId);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in registering the workflow "+workflow.getName()+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public void updateWorkflow(AuthzToken authzToken, String workflowTemplateId, WorkflowModel workflow)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			getWorkflowCatalog().updateWorkflow(workflowTemplateId, workflow);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in updating the workflow "+workflow.getName()+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public String getWorkflowTemplateId(AuthzToken authzToken, String workflowName)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			return getWorkflowCatalog().getWorkflowTemplateId(workflowName);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in retrieving the workflow template id for "+workflowName+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public boolean isWorkflowExistWithName(AuthzToken authzToken, String workflowName)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			return getWorkflowCatalog().isWorkflowExistWithName(workflowName);
		} catch (WorkflowCatalogException e) {
			String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

    private WorkflowCatalog getWorkflowCatalog() {
		if (workflowCatalog == null) {
			try {
				workflowCatalog = RegistryFactory.getAppCatalog().getWorkflowCatalog();
			} catch (Exception e) {
				logger.error("Unable to create Workflow Catalog", e);
			}
		}
		return workflowCatalog;
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
        try {
            dataCatalog = RegistryFactory.getReplicaCatalog();
            String productUrl = dataCatalog.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (RegistryException e) {
            String msg = "Error in registering the data resource"+dataProductModel.getProductName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            dataCatalog = RegistryFactory.getReplicaCatalog();
            DataProductModel dataProductModel = dataCatalog.getDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the data product "+productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            dataCatalog = RegistryFactory.getReplicaCatalog();
            String replicaId = dataCatalog.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the replica "+replicaLocationModel.getReplicaName()+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            dataCatalog = RegistryFactory.getReplicaCatalog();
            DataProductModel dataProductModel = dataCatalog.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the parent data product for "+ productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            dataCatalog = RegistryFactory.getReplicaCatalog();
            List<DataProductModel> dataProductModels = dataCatalog.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the child products for "+productUri+".";
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
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
        try {
            if(!isResourceExistsInGrouper(resourceId, resourceType)){
                initializeResourceWithGrouper(resourceId, resourceType);
            }
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            for(Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()){
                org.apache.airavata.grouper.resource.ResourceType gResouceType;
                if(resourceType.equals(ResourceType.EXPERIMENT)){
                    gResouceType = org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT;
                }else if(resourceType.equals(ResourceType.PROJECT)){
                    gResouceType = org.apache.airavata.grouper.resource.ResourceType.PROJECT;
                }else{
                    //Unsupported data type
                    continue;
                }

                if(entry.getValue().equals(ResourcePermissionType.READ)){
                    groupManager.grantPermission(entry.getKey(), SubjectType.PERSON, resourceId, gResouceType, PermissionAction.READ);
                }else if(entry.getValue().equals(ResourcePermissionType.WRITE)){
                    groupManager.grantPermission(entry.getKey(), SubjectType.PERSON, resourceId, gResouceType, PermissionAction.WRITE);
                }else{
                    //Unsupported permission type
                    continue;
                }
            }
            return true;
        } catch (Exception e) {
            String msg = "Error in sharing resource with users. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(AuthzToken authzToken, String resourceId, ResourceType resourceType,
                                                    Map<String, ResourcePermissionType> userPermissionList) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if(!isResourceExistsInGrouper(resourceId, resourceType)){
                initializeResourceWithGrouper(resourceId, resourceType);
            }
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            for(Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()){
                org.apache.airavata.grouper.resource.ResourceType gResouceType;
                if(resourceType.equals(ResourceType.EXPERIMENT)){
                    gResouceType = org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT;
                }else if(resourceType.equals(ResourceType.PROJECT)){
                    gResouceType = org.apache.airavata.grouper.resource.ResourceType.PROJECT;
                }else{
                    //Unsupported data type
                    continue;
                }

                if(entry.getValue().equals(ResourcePermissionType.READ)){
                    groupManager.revokePermission(entry.getKey(), SubjectType.PERSON, resourceId, gResouceType, PermissionAction.READ);
                }else if(entry.getValue().equals(ResourcePermissionType.WRITE)){
                    groupManager.revokePermission(entry.getKey(), SubjectType.PERSON, resourceId, gResouceType, PermissionAction.WRITE);
                }else{
                    //Unsupported permission type
                    continue;
                }
            }
            return true;
        } catch (Exception e) {
            String msg = "Error in revoking access to resouce from users. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(AuthzToken authzToken, String resourceId, ResourceType resourceType, ResourcePermissionType permissionType) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            org.apache.airavata.grouper.resource.ResourceType gResourceType;
            if(resourceType.equals(ResourceType.PROJECT)){
                gResourceType = org.apache.airavata.grouper.resource.ResourceType.PROJECT;
            }else if(resourceType.equals(ResourceType.EXPERIMENT)){
                gResourceType = org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT;
            }else{
                throw new GroupManagerException("Unsupported Resource Type");
            }

            org.apache.airavata.grouper.permission.PermissionAction gPermissionType;
            if(permissionType.equals(ResourcePermissionType.READ)){
                gPermissionType = PermissionAction.READ;
            } else if (permissionType.equals(ResourcePermissionType.WRITE)){
                gPermissionType = PermissionAction.WRITE;
            }else{
                throw new GroupManagerException("Unsupported Permission Type");
            }
            List<String> accessibleUsers = new ArrayList<>();
            accessibleUsers.addAll(groupManager.getAllAccessibleUsers(resourceId, gResourceType, gPermissionType));
            return accessibleUsers;
        } catch (Exception e) {
            String msg = "Error in getting all accessible users for resource. Resource ID : " + resourceId + " Resource Type : " + resourceType.toString() ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean createGroup(AuthzToken authzToken, GroupModel groupModel) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            Group group = new Group();
            group.setName(groupModel.getName());
            group.setDescription(groupModel.getDescription());
            group.setMembers(groupModel.getMembers());
            groupManager.createGroup(group);
        } catch (Exception e) {
            String msg = "Error Creating Group" ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
        return true;
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            Group group = new Group();
            group.setId(groupModel.getId());
            group.setName(groupModel.getName());
            group.setDescription(groupModel.getDescription());
            group.setMembers(groupModel.getMembers());
            groupManager.updateGroup(group);
        } catch (Exception e) {
            String msg = "Error Updating Group" ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
        return true;
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            groupManager.deleteGroup(groupId, ownerId + "@" + gatewayId);
        } catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
        return true;
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            Group group = groupManager.getGroup(groupId);
            GroupModel groupModel = new GroupModel();
            groupModel.setId(group.getId());
            groupModel.setName(group.getName());
            groupModel.setDescription(group.getDescription());
            groupModel.setMembers(group.getMembers());

            return  groupModel;
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
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
            List<Group> userGroups = groupManager.getAllGroupsUserBelongs(userName+"@"+gatewayId);
            List<GroupModel> groupModels = new ArrayList<>();
            userGroups.stream().forEach(group->{
                GroupModel groupModel = new GroupModel();
                groupModel.setId(group.getId());
                groupModel.setName(group.getName());
                groupModel.setDescription(group.getDescription());
                groupModel.setMembers(group.getMembers());

                groupModels.add(groupModel);
            });
            return groupModels;
        } catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName ;
            logger.error(msg, e);
            AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    private void initializeResourceWithGrouper(String resourceId, ResourceType resourceType) throws RegistryException, GroupManagerException {
        ExperimentCatalog experimentCatalog = RegistryFactory.getDefaultExpCatalog();
        GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
        if(resourceType.equals(ResourceType.PROJECT)){
            Project project = (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, resourceId);

            Resource projectResource = new Resource(project.getProjectID(), org.apache.airavata.grouper.resource.ResourceType.PROJECT);
            projectResource.setName(project.getName());
            projectResource.setDescription(project.getDescription());
            projectResource.setOwnerId(project.getOwner()+"@"+project.getGatewayId());
            groupManager.createResource(projectResource);

        }else if(resourceType.equals(ResourceType.EXPERIMENT)){
            ExperimentModel experiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, resourceId);
            if(!isResourceExistsInGrouper(experiment.getProjectId(), ResourceType.PROJECT)){
                initializeResourceWithGrouper(experiment.getProjectId(), ResourceType.PROJECT);
            }
            Resource experimentResource = new Resource(experiment.getExperimentId(), org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT);
            experimentResource.setName(experiment.getExperimentName());
            experimentResource.setDescription(experiment.getDescription());
            experimentResource.setParentResourceId(experiment.getProjectId());
            experimentResource.setOwnerId(experiment.getUserName()+"@"+experiment.getGatewayId());
            groupManager.createResource(experimentResource);
        }else{
            throw new GroupManagerException("Unsupported Resource Type");
        }

    }

    private boolean isResourceExistsInGrouper(String resourceId, ResourceType resourceType) throws GroupManagerException {
        GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
        if(resourceType.equals(ResourceType.PROJECT)){
            return groupManager.isResourceRegistered(resourceId, org.apache.airavata.grouper.resource.ResourceType.PROJECT);
        }else if(resourceType.equals(ResourceType.EXPERIMENT)){
            return groupManager.isResourceRegistered(resourceId, org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT);
        }else{
            throw new GroupManagerException("Unsupported Resource Type");
        }

    }

    private boolean hasPermission(String userId, String resourceId, ResourceType resourceType, ResourcePermissionType permissionType) throws GroupManagerException, RegistryException {
        if(!isResourceExistsInGrouper(resourceId, resourceType)){
            initializeResourceWithGrouper(resourceId, resourceType);
        }
        GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
        org.apache.airavata.grouper.resource.ResourceType gResourceType;
        if(resourceType.equals(ResourceType.PROJECT)){
            gResourceType = org.apache.airavata.grouper.resource.ResourceType.PROJECT;
        }else if(resourceType.equals(ResourceType.EXPERIMENT)){
            gResourceType = org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT;
        }else{
            throw new GroupManagerException("Unsupported Resource Type");
        }

        org.apache.airavata.grouper.permission.PermissionAction gPermissionType;
        if(permissionType.equals(ResourcePermissionType.READ)){
            gPermissionType = PermissionAction.READ;
        } else if (permissionType.equals(ResourcePermissionType.WRITE)){
            gPermissionType = PermissionAction.WRITE;
        }else{
            throw new GroupManagerException("Unsupported Permission Type");
        }
        Set<String> accessibleUsers = groupManager.getAllAccessibleUsers(resourceId, gResourceType, gPermissionType);
        return accessibleUsers.contains(userId);
    }

    private List<String> getAllAccessibleResourcesForUser(String userId, ResourceType resourceType, ResourcePermissionType permissionType) throws GroupManagerException {
        GroupManagerCPI groupManager = GroupManagerFactory.getGroupManager();
        org.apache.airavata.grouper.resource.ResourceType gResourceType;
        if(resourceType.equals(ResourceType.PROJECT)){
            gResourceType = org.apache.airavata.grouper.resource.ResourceType.PROJECT;
        }else if(resourceType.equals(ResourceType.EXPERIMENT)){
            gResourceType = org.apache.airavata.grouper.resource.ResourceType.EXPERIMENT;
        }else{
            throw new GroupManagerException("Unsupported Resource Type");
        }

        org.apache.airavata.grouper.permission.PermissionAction gPermissionType;
        if(permissionType.equals(ResourcePermissionType.READ)){
            gPermissionType = PermissionAction.READ;
        } else if (permissionType.equals(ResourcePermissionType.WRITE)){
            gPermissionType = PermissionAction.WRITE;
        }else{
            throw new GroupManagerException("Unsupported Permission Type");
        }

        List<String> allAccessibleResources = groupManager.getAccessibleResourcesForUser(userId, gResourceType, gPermissionType);
        return allAccessibleResources;
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
}
