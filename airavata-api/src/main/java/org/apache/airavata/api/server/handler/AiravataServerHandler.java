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
import java.util.stream.Collectors;
import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;
import org.apache.airavata.agents.api.AgentAdaptor;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavata_apiConstants;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;

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

import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.airavata.sharing.registry.models.*;

import org.apache.thrift.TException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private Publisher statusPublisher;
    private Publisher experimentPublisher;

    private org.apache.airavata.service.AiravataService airavataService =
            new org.apache.airavata.service.AiravataService();

    public AiravataServerHandler() {
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);
            airavataService.init();
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (Throwable e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
    }


    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String getAPIVersion() {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        logger.debug("Checking if the user" + userName + "exists in the gateway" + gatewayId);
        return airavataService.isUserExists(gatewayId, userName);
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return airavataService.addGatewayWithSharing(gateway);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return airavataService.getAllUsersInGateway(gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateGateway(gatewayId, updatedGateway);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating the gateway");
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            Gateway result = airavataService.getGateway(gatewayId);
            logger.debug("Airavata found the gateway with " + gatewayId);
            return result;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving gateway");
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteGateway(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting the gateway");
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            logger.debug("Airavata searching for all gateways");
            return airavataService.getAllGateways();
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving all gateways");
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            logger.debug("Airavata verifying if the gateway with " + gatewayId + "exits");
            return airavataService.isGatewayExist(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while checking if gateway exists");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.createNotification(notification);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while creating notification");
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateNotification(notification);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating notification");
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteNotification(gatewayId, notificationId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting notification");
        }
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getNotification(gatewayId, notificationId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving notification");
        }
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllNotifications(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while getting all notifications");
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            return airavataService.generateAndRegisterSSHKeys( gatewayId, userName, description);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while registering SSH Credential");
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
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            return airavataService.registerPwdCredential( gatewayId, userName, loginUserName, password, description);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while registering PWD Credential");
        }
    }

    @Override
    @SecurityCheck
    public CredentialSummary getCredentialSummary(AuthzToken authzToken, String tokenId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return airavataService.getCredentialSummaryWithAuth( authzToken, tokenId, gatewayId);
        } catch (Throwable e) {
            String msg = "Error retrieving credential summary for token " + tokenId + ". GatewayId: " + gatewayId;
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaries(AuthzToken authzToken, SummaryType type)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            return airavataService.getAllCredentialSummariesWithAuth( authzToken, type, gatewayId, userName);
        }  catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error retrieving credential summaries of type " + type + ". GatewayId: " + gatewayId);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return airavataService.deleteSSHPubKey( authzToken, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while deleting SSH credential");
        }
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return airavataService.deletePWDCredentialWithAuth( authzToken, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while deleting PWD credential");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.createProjectWithSharing( gatewayId, project);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while creating the project");
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException {
        try {
            airavataService.updateProjectWithAuth( authzToken, projectId, updatedProject);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating the project");
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException {
        try {
            return airavataService.deleteProjectWithAuth( authzToken, projectId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while removing the project");
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
                    AuthorizationException {
        try {
            return airavataService.getProjectWithAuth( authzToken, projectId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the project");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return airavataService.getUserProjectsWithSharing( authzToken, gatewayId, userName, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return airavataService.searchProjectsWithSharing(authzToken, gatewayId, userName, filters, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return airavataService.searchExperimentsWithSharing( authzToken, gatewayId, userName, filters, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
            List<String> accessibleExpIds = null;
            return airavataService.getExperimentStatistics(
                gatewayId, fromTime, toTime, userName, applicationName, resourceHostName, accessibleExpIds, limit, offset);
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
        return airavataService.getExperimentsInProject(authzToken, projectId, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the experiments. More info :");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        // TODO: verify that gatewayId and experiment.gatewayId match authzToken
        logger.info("Api server accepted experiment creation with name {}", experiment.getExperimentName());
        try {
            return airavataService.createExperimentWithSharingAndPublish( statusPublisher, gatewayId, experiment);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while creating the experiment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteExperimentWithAuth( authzToken, experimentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting the experiment");
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
        return airavataService.getExperiment(authzToken, airavataExperimentId);
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        return airavataService.getExperimentByAdmin(authzToken, airavataExperimentId);
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
                    AiravataSystemException, AuthorizationException {
        try {
            ExperimentModel result = airavataService.getDetailedExperimentTree(airavataExperimentId);
            return result;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the experiment");
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
        airavataService.updateExperiment(authzToken, airavataExperimentId, experiment);
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(
            AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        airavataService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            AuthzToken authzToken, String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, TException {
        airavataService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
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
    public boolean validateExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, ExperimentNotFoundException, InvalidRequestException {
        // TODO - call validation module and validate experiment
        /*     	try {
            ExperimentModel existingExperiment = airavataService.getExperiment(airavataExperimentId);
        	if (experimentModel == null) {
                     logger.error(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                     throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
                 }
             } catch (RegistryServiceException | ApplicationSettingsException e1) {
        	  logger.error(airavataExperimentId, "Error while retrieving projects", e1);
                   throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }

             Client orchestratorClient = getOrchestratorClient();
             try{
             if (orchestratorClient.validateExperiment(airavataExperimentId)) {
                 logger.debug(airavataExperimentId, "Experiment validation succeed.");
             } else {
                 logger.debug(airavataExperimentId, "Experiment validation failed.");
                 return false;
             }}finally {
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
    public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId) {
        try {
            return airavataService.getExperimentStatus(airavataExperimentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error retrieving experiment status");
        }    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException {
        try {
            return airavataService.getExperimentOutputs(airavataExperimentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the experiment outputs");
        }    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        return null;
    }

    @Override
    @SecurityCheck
    public void fetchIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        try {
            airavataService.validateAndFetchIntermediateOutputs( authzToken, airavataExperimentId, outputNames, experimentPublisher);
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have WRITE access")) {
                logger.error(e.getMessage(), e);
                throw new AuthorizationException(e.getMessage());
            } else if (e.getMessage() != null
                    && (e.getMessage().contains("does not have currently ACTIVE job")
                            || e.getMessage().contains("already intermediate output fetching tasks"))) {
                logger.error(e.getMessage(), e);
                throw new InvalidRequestException(e.getMessage());
            }
            logger.error(
                    "Error while processing request to fetch intermediate outputs for experiment: "
                            + airavataExperimentId,
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getIntermediateOutputProcessStatusInternal( authzToken, airavataExperimentId, outputNames);
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("does not have READ access")) {
                logger.debug(e.getMessage(), e);
                throw new AuthorizationException(e.getMessage());
            } else if (e.getMessage() != null && e.getMessage().contains("No matching intermediate output")) {
                logger.debug(e.getMessage(), e);
                throw new InvalidRequestException(e.getMessage());
            }
            logger.error(
                    "Error while processing request to get intermediate output process status for experiment: "
                            + airavataExperimentId,
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException {
        try {
            return airavataService.getJobStatuses(airavataExperimentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the job statuses");
        }    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getJobDetails(airavataExperimentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving the job details");
        }    }

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
            throws AiravataClientException, AiravataSystemException, AuthorizationException, ExperimentNotFoundException, InvalidRequestException, TException {
        // TODO: verify that gatewayId matches gatewayId in authzToken
        logger.info("Launching experiment {}", airavataExperimentId);
        airavataService.launchExperimentWithValidation( authzToken, gatewayId, airavataExperimentId, experimentPublisher);
        logger.info("Experiment with ExpId: " + airavataExperimentId + " was submitted in gateway with gatewayID: "
                + gatewayId);
    }

    //    private OrchestratorService.Client getOrchestratorClient() {
    //	    try {
    //		    final String serverHost = ServerSettings.getOrchestratorServerHost();
    //		    final int serverPort = ServerSettings.getOrchestratorServerPort();
    //		    return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
    //	    } catch (Throwable e) {
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
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException {
        try {
            // getExperiment will apply sharing permissions
            ExperimentModel existingExperiment = this.getExperiment(authzToken, existingExperimentID);
            return cloneExperimentInternal(
                    authzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (Throwable e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(
            AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException {
        try {
            // get existing experiment by bypassing normal sharing permissions for the admin user
            ExperimentModel existingExperiment = this.getExperimentByAdmin(authzToken, existingExperimentID);
            return cloneExperimentInternal(
                    authzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (Throwable e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    private String cloneExperimentInternal(
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException , AuthorizationException,
                    ApplicationSettingsException, InvalidRequestException, AiravataSystemException, AiravataClientException, org.apache.airavata.sharing.registry.models.SharingRegistryException {
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
        if (!airavataService.userHasAccess(
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
                List<OutputDataObjectType> applicationOutputs = airavataService.getApplicationOutputs(existingExperiment.getExecutionId());
                existingExperiment.setExperimentOutputs(applicationOutputs);
            } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
                logger.warn("Error getting application outputs for experiment clone: " + e.getMessage());
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
            String compResourceId = existingExperiment
                    .getUserConfigurationData()
                    .getComputationalResourceScheduling()
                    .getResourceHostId();

            try {
                ComputeResourceDescription computeResource = airavataService.getComputeResource(compResourceId);
                if (!computeResource.isEnabled()) {
                    existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                }
            } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
                logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
            } catch (Throwable e) {
                logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
            }
        }
        logger.debug("Airavata cloned experiment with experiment id : " + existingExperimentID);
        existingExperiment.setUserName(userId);
        
        String expId;
        try {
            expId = airavataService.createExperiment(gatewayId, existingExperiment);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error creating cloned experiment");
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
                airavataService.createEntity(entity);
                airavataService.shareEntityWithAdminGatewayGroups( entity);
            } catch (Throwable ex) {
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
     * @This method call does not have a value.
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException {
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
        } catch (Throwable e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerApplicationModule(gatewayId, applicationModule);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding application module");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application module");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateApplicationModule(appModuleId, applicationModule);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating application module");
        }    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @return list applicationModule.
     * Returns the list of all Application Module Objects.
     */
    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllAppModules(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving all application modules");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAccessibleAppModulesWithSharing( authzToken, gatewayId);
        }  catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving all application modules");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting application module");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        // TODO: verify that gatewayId matches authzToken gatewayId
        try {
            String result = airavataService.registerApplicationDeployment(gatewayId, applicationDeployment);
            if (ServerSettings.isEnableSharing()) {
                Entity entity = new Entity();
                entity.setEntityId(result);
                final String domainId = gatewayId;
                entity.setDomainId(domainId);
                entity.setEntityTypeId(domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                entity.setOwnerId(userName + "@" + domainId);
                entity.setName(result);
                entity.setDescription(applicationDeployment.getAppDeploymentDescription());
                airavataService.createEntity(entity);
                airavataService.shareEntityWithAdminGatewayGroups( entity);
            }
            return result;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding application deployment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = airavataService.userHasAccessInternal( authzToken, appDeploymentId, ResourcePermissionType.READ);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have access to application deployment " + appDeploymentId);
                }
            }
            ApplicationDeploymentDescription result = airavataService.getApplicationDeployment(appDeploymentId);
            return result;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application deployment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                final boolean hasAccess = airavataService.userHasAccessInternal( authzToken, appDeploymentId, ResourcePermissionType.WRITE);
                if (!hasAccess) {
                    throw new AuthorizationException(
                            "User does not have WRITE access to application deployment " + appDeploymentId);
                }
            }
            return airavataService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating application deployment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            final boolean hasAccess = airavataService.userHasAccessInternal( authzToken, appDeploymentId, ResourcePermissionType.WRITE);
            if (!hasAccess) {
                throw new AuthorizationException(
                        "User does not have WRITE access to application deployment " + appDeploymentId);
            }
            return airavataService.deleteApplicationDeployment(appDeploymentId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting application deployment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAccessibleApplicationDeploymentsWithSharing( authzToken, gatewayId, permissionType);
        }  catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application deployments");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAppModuleDeployedResources(appModuleId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application deployment");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            // Get list of compute resources for this Group Resource Profile
            if (!airavataService.userHasAccessInternal( authzToken, groupResourceProfileId, ResourcePermissionType.READ)) {
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
            airavataService.searchEntities(gatewayId, userName + "@" + gatewayId, sharingFilters, 0, -1)
                    .forEach(a -> accessibleAppDeploymentIds.add(a.getEntityId()));

            return airavataService.getAccessibleApplicationDeploymentsForAppModule(
                            appModuleId, gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application deployments");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding application interface");
        }    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            ApplicationInterfaceDescription existingInterface = airavataService.getApplicationInterface(existingAppInterfaceID);
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
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Provided application interface does not exist.Please provide a valid application interface id...");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            ApplicationInterfaceDescription existingInterface = airavataService.getApplicationInterface(appInterfaceId);
            return existingInterface;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating application interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteApplicationInterface(appInterfaceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting application interface");
        }    }

    /**
     * Fetch name and id of  Application Interface documents.
     *
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing id's
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllApplicationInterfaceNames(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application interfaces");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllApplicationInterfaces(gatewayId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application interfaces");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getApplicationInputs(appInterfaceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving application inputs");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            List<OutputDataObjectType> applicationOutputs = airavataService.getApplicationOutputs(appInterfaceId);
            return applicationOutputs;
        } catch (Throwable e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving available compute resources");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerComputeResource(computeResourceDescription);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while saving compute resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving compute resource");
        }    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllComputeResourceNames(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllComputeResourceNames();
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving compute resource names");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateComputeResource(computeResourceId, computeResourceDescription);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating compute resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting compute resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerStorageResource(storageResourceDescription);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while saving storage resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving storage resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllStorageResourceNames();
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving storage resource names");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateStorageResource(storageResourceId, storageResourceDescription);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating storage resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting storage resource");
        }    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        StorageInfoContext context;

        try {
            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
            ComputeResourceDescription computeResource = airavataService.getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (Throwable e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = airavataService.getStorageResource(resourceId);
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

            return context.adaptor.getStorageVolumeInfo(location);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving storage resource.", e);
            throw e;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving storage resource");
        }
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        StorageInfoContext context;

        try {
            Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
            try {
            ComputeResourceDescription computeResource = airavataService.getComputeResource(resourceId);
                if (computeResource != null) {
                    computeResourceOp = Optional.of(computeResource);
                }
            } catch (Throwable e) {
                logger.debug("Compute resource {} not found: {}", resourceId, e.getMessage());
            }

            Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
            if (computeResourceOp.isEmpty()) {
                try {
                    StorageResourceDescription storageResource = airavataService.getStorageResource(resourceId);
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

            return context.adaptor.getStorageDirectoryInfo(location);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving storage resource.", e);
            throw e;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving storage directory info");
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
            AuthzToken authzToken, String computeResourceId, int priorityOrder, LOCALSubmission localSubmission){
        try {
            return airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding local job submission interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating local job submission interface");
        }    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getLocalJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving local job submission interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding SSH job submission interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding SSH fork job submission interface");
        }    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getSSHJobSubmission(jobSubmissionId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving SSH job submission interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding cloud job submission interface");
        }    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getCloudJobSubmission(jobSubmissionId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public String addUNICOREJobSubmissionDetails(
            AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addUNICOREJobSubmissionDetails(
                    computeResourceId, priorityOrder, unicoreJobSubmission);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUnicoreJobSubmission(jobSubmissionId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(
                    jobSubmissionInterfaceId,
                    "Error while updating job submission interface to resource compute resource...",
                    e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding data movement interface to resource");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating local data movement interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding SCP data movement interface");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating SCP data movement interface");
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getSCPDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving SCP data movement interface");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding UNICORE data movement interface");
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating unicore data movement interface");
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getLocalDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving local data movement interface");
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUnicoreDataMovement(dataMovementId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving UNICORE data movement interface");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addGridFTPDataMovementDetails(computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding GridFTP data movement interface");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating GridFTP data movement interface");
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getGridFTPDataMovement(dataMovementId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting job submission interface");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting data movement interface");
        }    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerResourceJobManager(resourceJobManager);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while adding resource job manager");
        }    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(
            AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating resource job manager");
        }    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving resource job manager");
        }    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting resource job manager");
        }    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteBatchQueue(computeResourceId, queueName);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting batch queue");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while registering gateway resource profile");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving gateway resource profile");
        }    }

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
            AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating gateway resource profile");
        }    }

    /**
     * Delete the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        try {
            return airavataService.deleteGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while removing gateway resource profile");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while registering gateway resource profile preference");
        }    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while registering gateway storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading gateway compute resource preference");
        }    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading gateway data storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading gateway compute resource preferences");
        }    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllGatewayStoragePreferences(gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading gateway data storage preferences");
        }    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllGatewayResourceProfiles();
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving all gateway profiles");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating gateway compute resource preference");
        }    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating gateway data storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(gatewayID, "Error while deleting gateway compute resource preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(gatewayID, "Error while deleting gateway compute resource preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting gateway data storage preference");
        }    }

    @Override
    @SecurityCheck
    public List<SSHAccountProvisioner> getSSHAccountProvisioners(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return SSHAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while checking if user has an SSH Account");
        }
    }

    @Override
    @SecurityCheck
    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
        SSHCredential sshCredential = null;
        try {
            sshCredential = airavataService.getSSHCredential(airavataCredStoreToken, gatewayId);;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while retrieving SSH Credential");
        }

        try {
            return SSHAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while checking if setup of SSH account is complete");
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        SSHCredential sshCredential = null;
        try {
            try {
            sshCredential = airavataService.getSSHCredential(airavataCredStoreToken, gatewayId);;
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error occurred while retrieving SSH Credential");
        }
            
            return SSHAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
        }  catch (Throwable e) {
            
            throw ThriftExceptionHandler.convertException(e, "Error occurred while automatically setting up SSH account for user");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerUserResourceProfile(userResourceProfile);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while registering user resource profile");
        }    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.isUserResourceProfileExists(userId, gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while checking existence of user resource profile");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUserResourceProfile(userId, gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while retrieving user resource profile");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating user resource profile");
        }    }

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
    public boolean deleteUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        try {
            return airavataService.deleteUserResourceProfile(userId, gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while removing user resource profile");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(userId, "Error while registering user resource profile preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.addUserStoragePreference(userId, gatewayID, userStorageResourceId, dataStoragePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while registering user storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading user compute resource preference");
        }    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading user data storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading User compute resource preferences");
        }    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllUserStoragePreferences(userId, gatewayID);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while reading User data storage preferences");
        }    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllUserResourceProfiles();
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(
                    "Error while reading retrieving all user resource profiles. More info : " + e.getMessage());
            throw exception;
        } catch (Throwable e) {
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateUserComputeResourcePreference(userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating user compute resource preference");
        }    }

    @Override
    @SecurityCheck
    public boolean updateUserStoragePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId,
            UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.updateUserStoragePreference(userId, gatewayID, userStorageId, dataStoragePreference);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while updating user data storage preference");
        }    }

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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error(userId, "Error while deleting user compute resource preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        } catch (Throwable e) {
            logger.error(userId, "Error while deleting user compute resource preference...", e);
            throw ThriftExceptionHandler.convertException(e, "Error occurred");
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error while deleting user data storage preference");
        }    }

    @Override
    @SecurityCheck
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getLatestQueueStatuses();
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error in retrieving queue statuses";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerDataProduct(dataProductModel);
        } catch (Throwable e) {
            String msg = "Error in registering the data resource" + dataProductModel.getProductName() + ".";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getDataProduct(productUri);
        } catch (Throwable e) {
            throw ThriftExceptionHandler.convertException(e, "Error retrieving data product");
        }    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerReplicaLocation(replicaLocationModel);
        } catch (Throwable e) {
            String msg = "Error in retreiving the replica " + replicaLocationModel.getReplicaName() + ".";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getParentDataProduct(productUri);
        } catch (Throwable e) {
            String msg = "Error in retreiving the parent data product for " + productUri + ".";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getChildDataProducts(productUri);
        } catch (Throwable e) {
            String msg = "Error in retreiving the child products for " + productUri + ".";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    airavataService.shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE",
                            true);
                else if (userPermission.getValue().equals(ResourcePermissionType.READ))
                    airavataService.shareEntityWithUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ",
                            true);
                else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing( gatewayId);
                        airavataService.shareEntityWithUsers(
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
            return true;
        } catch (Throwable e) {
            String msg = "Error in sharing resource with users. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE))
                    airavataService.shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE",
                            true);
                else if (groupPermission.getValue().equals(ResourcePermissionType.READ))
                    airavataService.shareEntityWithGroups(
                            gatewayId,
                            resourceId,
                            Arrays.asList(groupPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ",
                            true);
                else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing( gatewayId);
                        airavataService.shareEntityWithGroups(
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
            return true;
        } catch (Throwable e) {
            String msg = "Error in sharing resource with groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (!airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE))
                    airavataService.revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "WRITE");
                else if (userPermission.getValue().equals(ResourcePermissionType.READ))
                    airavataService.revokeEntitySharingFromUsers(
                            gatewayId,
                            resourceId,
                            Arrays.asList(userPermission.getKey()),
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID) + ":" + "READ");
                else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing( gatewayId);
                        airavataService.revokeEntitySharingFromUsers(
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
            return true;
        } catch (Throwable e) {
            String msg = "Error in revoking access to resource from users. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        final String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (!airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)
                    && !airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new AuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            // For certain resource types, restrict them from being unshared with admin groups
            ResourceType resourceType = airavataService.getResourceType( gatewayId, resourceId);
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
                    airavataService.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "WRITE");
                else if (groupPermission.getValue().equals(ResourcePermissionType.READ))
                    airavataService.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":" + "READ");
                else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (airavataService.userHasAccessInternal( authzToken, resourceId, ResourcePermissionType.OWNER)) {
                        airavataService.createManageSharingPermissionTypeIfMissing( gatewayId);
                        airavataService.revokeEntitySharingFromUsers(
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
            return true;
        } catch (Throwable e) {
            String msg = "Error in revoking access to resource from groups. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllAccessibleUsersWithSharing( authzToken, resourceId, permissionType, false);
        } catch (Throwable e) {
            String msg = "Error in getting all accessible users for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllAccessibleUsersWithSharing( authzToken, resourceId, permissionType, true);
        } catch (Throwable e) {
            String msg = "Error in getting all directly accessible users for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllAccessibleGroupsWithSharing( authzToken, resourceId, permissionType, false);
        } catch (Throwable e) {
            String msg = "Error in getting all accessible groups for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getAllAccessibleGroupsWithSharing( authzToken, resourceId, permissionType, true);
        } catch (Throwable e) {
            String msg = "Error in getting all directly accessible groups for resource. Resource ID : " + resourceId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        final String domainId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        final String userId = authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + domainId;
        try {
            return airavataService.userHasAccessInternal( authzToken, resourceId, permissionType);
        } catch (Throwable e) {
            String msg = "Error in if user can access resource. User ID : " + userId + ", Resource ID : " + resourceId
                    + ", Resource Permission Type : " + permissionType.toString();
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        // TODO: verify that gatewayId in groupResourceProfile matches authzToken gatewayId
        try {
            return airavataService.createGroupResourceProfileWithSharing( authzToken, groupResourceProfile);
        } catch (AuthorizationException ae) {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName
                    + " not allowed access to resources referenced in this GroupResourceProfile. Reason: "
                    + ae.getMessage());
            throw ae;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error creating group resource profile.";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error creating group resource profile.";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            airavataService.validateGroupResourceProfile( authzToken, groupResourceProfile);
            if (!airavataService.userHasAccessInternal(
                    authzToken,
                    groupResourceProfile.getGroupResourceProfileId(),
                    ResourcePermissionType.WRITE)) {
                throw new AuthorizationException("User does not have permission to update group resource profile");
            }
            airavataService.updateGroupResourceProfile(groupResourceProfile);
        } catch (AuthorizationException ae) {
            String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
            logger.info("User " + userName + " not allowed access to update GroupResourceProfile "
                    + groupResourceProfile.getGroupResourceProfileId() + ", reason: " + ae.getMessage());
            throw ae;
        } catch (Throwable e) {
            String msg = "Error updating group resource profile. groupResourceProfileId: "
                    + groupResourceProfile.getGroupResourceProfileId();
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            GroupResourceProfile groupResourceProfile = airavataService.getGroupResourceProfile(groupResourceProfileId);
            return groupResourceProfile;
        } catch (AuthorizationException checkedException) {
            logger.error(
                    "Error while retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId,
                    checkedException);
            throw checkedException;
        } catch (Throwable e) {
            String msg = "Error retrieving group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (ServerSettings.isEnableSharing()) {
                try {
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to remove group resource profile");
                }
            }
            boolean result = airavataService.removeGroupResourceProfile(groupResourceProfileId);
            if (result) {
                airavataService.deleteEntity(gatewayId, groupResourceProfileId);
            }
            return result;
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error removing group resource profile. groupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getGroupResourceListWithSharing( authzToken, gatewayId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving list group resource profile list. GatewayId: " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving list group resource profile list. GatewayId: " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputePrefs(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group compute preferences");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute preferences");
                }
            }
            return airavataService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group compute resource preferences. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error removing group compute resource preferences. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy = airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove group compute resource policy");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove group compute resource policy");
                }
            }
            return airavataService.removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error removing group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy = airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new AuthorizationException(
                                "User does not have permission to remove batch queue resource policy");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException(
                            "User does not have permission to remove batch queue resource policy");
                }
            }
            return airavataService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error removing batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error removing batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return airavataService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute preference. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group compute preference. GroupResourceProfileId: " + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy = airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }

            return airavataService.getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group compute resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy = airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return airavataService.getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group batch queue resource policy. ResourcePolicyId: " + resourcePolicyId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return airavataService.getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource preference. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group compute resource preference. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return airavataService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group batch queue resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group batch queue resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            if (ServerSettings.isEnableSharing()) {
                try {
                    String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
                    String userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
                    if (!airavataService.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new AuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Throwable e) {
                    throw new AuthorizationException("User does not have permission to access group resource profile");
                }
            }
            return airavataService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            String msg = "Error retrieving Group compute resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving Group compute resource policy list. GroupResourceProfileId: "
                    + groupResourceProfileId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        try {
            return airavataService.retrieveGatewayGroups(gatewayId);
        } catch (Throwable e) {
            String msg = "Error retrieving GatewayGroups for gateway: " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public Parser getParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getParser(parserId, gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parser with id: " + parserId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving parser with id: " + parserId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public String saveParser(AuthzToken authzToken, Parser parser)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.saveParser(parser);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while saving the parser";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error while saving the parser";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<Parser> listAllParsers(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.listAllParsers(gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while listing the parsers for gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error while listing the parsers for gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParser(AuthzToken authzToken, String parserId, String gatewayId){
        try {
            airavataService.removeParser(parserId, gatewayId);
            return true;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while removing the parser " + parserId + " in gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error while removing the parser " + parserId + " in gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getParsingTemplate(templateId, gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parsing template with id: " + templateId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving parsing template with id: " + templateId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            AuthzToken authzToken, String experimentId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error retrieving parsing templates for experiment: " + experimentId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error retrieving parsing templates for experiment: " + experimentId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public String saveParsingTemplate(AuthzToken authzToken, ParsingTemplate parsingTemplate)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.saveParsingTemplate(parsingTemplate);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error saving the parsing template";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error saving the parsing template";
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            airavataService.removeParsingTemplate(templateId, gatewayId);
            return true;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while removing the parsing template " + templateId + " in gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error while removing the parsing template " + templateId + " in gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> listAllParsingTemplates(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.listAllParsingTemplates(gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            String msg = "Error while listing the parsing templates for gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        } catch (Throwable e) {
            String msg = "Error while listing the parsing templates for gateway " + gatewayId;
            logger.error(msg, e);
            throw ThriftExceptionHandler.convertException(e, msg);
        }
    }

    /**
     * To hold storage info context (login username, credential token, and adaptor)
     */
    private record StorageInfoContext(String loginUserName, String credentialToken, AgentAdaptor adaptor) {}

    /**
     * Check if a gateway resource profile exists
     */
        private boolean isGatewayResourceProfileExists(String gatewayId) {
        try {
            GatewayResourceProfile profile = airavataService.getGatewayResourceProfile(gatewayId);
            return profile != null;
        } catch (Throwable e) {
            logger.error("Error while checking if gateway resource profile exists", e);
            return false;
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
            throws Exception {
        String loginUserName = null;
        boolean loginFromUserPref = false;
        GroupComputeResourcePreference groupComputePref = null;
        GroupResourceProfile groupResourceProfile = null;

        UserComputeResourcePreference userComputePref = null;
        if (safeIsUserResourceProfileExists(authzToken, userId, gatewayId)) {
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
            throws Exception {
        UserStoragePreference userStoragePref = null;
        if (safeIsUserResourceProfileExists(authzToken, userId, gatewayId)) {
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

    private boolean safeIsUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayId) {
        try {
            return isUserResourceProfileExists(authzToken, userId, gatewayId);
        } catch (Throwable e) {
            logger.error("Error checking if user resource profile exists", e);
            return false;
        }
    }

}