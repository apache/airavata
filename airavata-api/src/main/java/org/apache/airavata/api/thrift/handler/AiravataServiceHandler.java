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
package org.apache.airavata.api.thrift.handler;

import java.util.*;
import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
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
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.group.ResourcePermissionType;
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
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.springframework.stereotype.Component;

@Component
public class AiravataServiceHandler implements org.apache.airavata.api.Airavata.Iface {

    private final AiravataService airavataService;

    public AiravataServiceHandler(AiravataService airavataService) throws AiravataException {
        this.airavataService = airavataService;
        this.airavataService.init();
    }

    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return org.apache.airavata.api.airavata_apiConstants.AIRAVATA_API_VERSION;
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.isUserExists(gatewayId, userName);
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addGateway(gateway);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllUsersInGateway(gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateGateway(gatewayId, updatedGateway);
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGateway(gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteGateway(gatewayId);
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllGateways();
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.isGatewayExist(gatewayId);
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
        return airavataService.createNotification(notification);
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateNotification(notification);
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteNotification(gatewayId, notificationId);
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getNotification(gatewayId, notificationId);
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllNotifications(gatewayId);
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        return airavataService.generateAndRegisterSSHKeys(gatewayId, userName, description);
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
        return airavataService.registerPwdCredential(gatewayId, userName, loginUserName, password, description);
    }

    @Override
    @SecurityCheck
    public CredentialSummary getCredentialSummary(AuthzToken authzToken, String tokenId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        return airavataService.getCredentialSummary(authzToken, tokenId, gatewayId);
    }

    @Override
    @SecurityCheck
    public List<CredentialSummary> getAllCredentialSummaries(AuthzToken authzToken, SummaryType type)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        return airavataService.getAllCredentialSummaries(authzToken, type, gatewayId, userName);
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        return airavataService.deleteSSHPubKey(authzToken, airavataCredStoreToken, gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        return airavataService.deletePWDCredential(authzToken, airavataCredStoreToken, gatewayId);
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
        return airavataService.createProject(gatewayId, project);
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException {
        airavataService.updateProject(authzToken, projectId, updatedProject);
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException {
        return airavataService.deleteProject(authzToken, projectId);
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
        return airavataService.getProject(authzToken, projectId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUserProjects(authzToken, gatewayId, userName, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.searchProjects(authzToken, gatewayId, userName, filters, limit, offset);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error searching projects: " + e.getMessage());
            exception.initCause(e);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.searchExperiments(authzToken, gatewayId, userName, filters, limit, offset);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        List<String> accessibleExpIds = null;
        return airavataService.getExperimentStatistics(
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
                    AuthorizationException {
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
        return airavataService.getUserExperiments(gatewayId, userName, limit, offset);
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
        try {
            return airavataService.createExperiment(gatewayId, experiment);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error creating experiment: " + e.getMessage());
            exception.initCause(e);
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
     */
    @Override
    @SecurityCheck
    public boolean deleteExperiment(AuthzToken authzToken, String experimentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, ProjectNotFoundException {
        return airavataService.deleteExperimentWithAuth(authzToken, experimentId);
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
                    AiravataSystemException, AuthorizationException {
        return airavataService.getExperiment(authzToken, airavataExperimentId);
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
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
        return airavataService.getDetailedExperimentTree(airavataExperimentId);
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
                    AiravataSystemException, AuthorizationException {
        try {
            airavataService.updateExperiment(authzToken, airavataExperimentId, experiment);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error updating experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(
            AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, AiravataSystemException {
        airavataService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            AuthzToken authzToken, String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, AiravataSystemException {
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
            throws AiravataClientException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, InvalidRequestException {
        // TODO - call validation module and validate experiment
        /*     	try {
            ExperimentModel existingExperiment = airavataService.getExperiment(airavataExperimentId);
        	if (experimentModel == null) {
                     logger.error(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                     throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
                 }
             } catch (RegistryServiceException e1) {
        	  logger.error(airavataExperimentId, "Error while retrieving projects", e1);
                   // Commented out code - not used
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
    public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId)
            throws AiravataSystemException {
        return airavataService.getExperimentStatus(airavataExperimentId);
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, AiravataSystemException {
        return airavataService.getExperimentOutputs(airavataExperimentId);
    }

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
        airavataService.fetchIntermediateOutputs(authzToken, airavataExperimentId, outputNames);
    }

    @Override
    @SecurityCheck
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        return airavataService.getIntermediateOutputProcessStatus(authzToken, airavataExperimentId, outputNames);
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, AiravataSystemException {
        return airavataService.getJobStatuses(airavataExperimentId);
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException {
        return airavataService.getJobDetails(airavataExperimentId);
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
            throws AiravataClientException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, InvalidRequestException, ProjectNotFoundException {
        try {
            airavataService.launchExperiment(authzToken, gatewayId, airavataExperimentId);
        } catch (SharingRegistryException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
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
        // getExperiment will apply sharing permissions
        ExperimentModel existingExperiment = airavataService.getExperiment(authzToken, existingExperimentID);
        return airavataService.cloneExperiment(
                authzToken, existingExperimentID, newExperimentName, newExperimentProjectId, existingExperiment);
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(
            AuthzToken authzToken, String existingExperimentID, String newExperimentName, String newExperimentProjectId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, ProjectNotFoundException {
        // get existing experiment by bypassing normal sharing permissions for the admin user
        ExperimentModel existingExperiment = airavataService.getExperimentByAdmin(authzToken, existingExperimentID);
        return airavataService.cloneExperiment(
                authzToken, existingExperimentID, newExperimentName, newExperimentProjectId, existingExperiment);
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
        airavataService.terminateExperiment(airavataExperimentId, gatewayId);
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
        return airavataService.registerApplicationModule(gatewayId, applicationModule);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getApplicationModule(appModuleId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateApplicationModule(appModuleId, applicationModule);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllAppModules(gatewayId);
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
        return airavataService.getAccessibleAppModules(authzToken, gatewayId);
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
        return airavataService.deleteApplicationModule(appModuleId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        try {
            return airavataService.registerApplicationDeployment(authzToken, gatewayId, applicationDeployment);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error registering application deployment: " + e.getMessage());
            exception.initCause(e);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getApplicationDeployment(authzToken, appDeploymentId);
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
        return airavataService.updateApplicationDeployment(authzToken, appDeploymentId, applicationDeployment);
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
        return airavataService.deleteApplicationDeployment(authzToken, appDeploymentId);
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
        return airavataService.getAccessibleApplicationDeployments(authzToken, gatewayId, permissionType);
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
        return airavataService.getAppModuleDeployedResources(appModuleId);
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
        return airavataService.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                authzToken, appModuleId, groupResourceProfileId);
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
        return airavataService.registerApplicationInterface(gatewayId, applicationInterface);
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.cloneApplicationInterface(existingAppInterfaceID, newApplicationName, gatewayId);
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
        return airavataService.getApplicationInterface(appInterfaceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateApplicationInterface(appInterfaceId, applicationInterface);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteApplicationInterface(appInterfaceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllApplicationInterfaceNames(gatewayId);
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
        return airavataService.getAllApplicationInterfaces(gatewayId);
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
        return airavataService.getApplicationInputs(appInterfaceId);
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
        return airavataService.getApplicationOutputs(appInterfaceId);
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
        return airavataService.getAvailableAppInterfaceComputeResources(appInterfaceId);
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
        return airavataService.registerComputeResource(computeResourceDescription);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getComputeResource(computeResourceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllComputeResourceNames();
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateComputeResource(computeResourceId, computeResourceDescription);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteComputeResource(computeResourceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.registerStorageResource(storageResourceDescription);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getStorageResource(storageResourceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllStorageResourceNames();
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateStorageResource(storageResourceId, storageResourceDescription);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteStorageResource(storageResourceId);
    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getResourceStorageInfo(authzToken, resourceId, location);
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getStorageDirectoryInfo(authzToken, resourceId, location);
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
            throws AiravataSystemException {
        return airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getLocalJobSubmission(jobSubmissionId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getSSHJobSubmission(jobSubmissionId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getCloudJobSubmission(jobSubmissionId);
    }

    @Override
    @SecurityCheck
    public String addUNICOREJobSubmissionDetails(
            AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUnicoreJobSubmission(jobSubmissionId);
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
        return airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
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
        return airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(
            AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
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
        return airavataService.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
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
        return airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getSCPDataMovement(dataMovementId);
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
        return airavataService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getLocalDataMovement(dataMovementId);
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUnicoreDataMovement(dataMovementId);
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
        return airavataService.addGridFTPDataMovementDetails(
                computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
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
        return airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGridFTPDataMovement(dataMovementId);
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
        return airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.registerResourceJobManager(resourceJobManager);
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(
            AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getResourceJobManager(resourceJobManagerId);
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteResourceJobManager(resourceJobManagerId);
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteBatchQueue(computeResourceId, queueName);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.registerGatewayResourceProfile(gatewayResourceProfile);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGatewayResourceProfile(gatewayID);
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
            AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
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
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        return airavataService.deleteGatewayResourceProfile(gatewayID);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGatewayStoragePreference(gatewayID, storageId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllGatewayStoragePreferences(gatewayID);
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllGatewayResourceProfiles();
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(
            AuthzToken authzToken, String gatewayID, String storageId, StoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateGatewayStoragePreference(gatewayID, storageId, dataStoragePreference);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
    }

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
        return airavataService.doesUserHaveSSHAccount(authzToken, computeResourceId, userId);
    }

    @Override
    @SecurityCheck
    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.isSSHAccountSetupComplete(authzToken, computeResourceId, airavataCredStoreToken);
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.setupSSHAccount(authzToken, computeResourceId, userId, airavataCredStoreToken);
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
        return airavataService.registerUserResourceProfile(userResourceProfile);
    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.isUserResourceProfileExists(userId, gatewayID);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUserResourceProfile(userId, gatewayID);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
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
    public boolean deleteUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID)
            throws AiravataClientException, AiravataSystemException, AuthorizationException, InvalidRequestException {
        return airavataService.deleteUserResourceProfile(userId, gatewayID);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.addUserComputeResourcePreference(
                userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
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
        return airavataService.addUserStoragePreference(
                userId, gatewayID, userStorageResourceId, dataStoragePreference);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllUserStoragePreferences(userId, gatewayID);
    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getAllUserResourceProfiles();
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
        return airavataService.updateUserComputeResourcePreference(
                userId, gatewayID, userComputeResourceId, userComputeResourcePreference);
    }

    @Override
    @SecurityCheck
    public boolean updateUserStoragePreference(
            AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId,
            UserStoragePreference dataStoragePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.updateUserStoragePreference(userId, gatewayID, userStorageId, dataStoragePreference);
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
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
    }

    @Override
    @SecurityCheck
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getLatestQueueStatuses();
    }

    /**
     * ReplicaCatalog Related Methods
     * @return
     * @throws ApplicationSettingsException
     */
    @Override
    @SecurityCheck
    public String registerDataProduct(AuthzToken authzToken, DataProductModel dataProductModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.registerDataProduct(dataProductModel);
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getDataProduct(productUri);
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.registerReplicaLocation(replicaLocationModel);
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getParentDataProduct(productUri);
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getChildDataProducts(productUri);
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
        return airavataService.shareResourceWithUsers(authzToken, resourceId, userPermissionList);
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.shareResourceWithGroups(authzToken, resourceId, groupPermissionList);
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.revokeSharingOfResourceFromUsers(authzToken, resourceId, userPermissionList);
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.revokeSharingOfResourceFromGroups(authzToken, resourceId, groupPermissionList);
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    SharingRegistryException {
        return airavataService.getAllAccessibleUsers(authzToken, resourceId, permissionType, false);
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    SharingRegistryException {
        return airavataService.getAllAccessibleUsers(authzToken, resourceId, permissionType, true);
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    SharingRegistryException {
        return airavataService.getAllAccessibleGroups(authzToken, resourceId, permissionType, false);
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    SharingRegistryException {
        return airavataService.getAllAccessibleGroups(authzToken, resourceId, permissionType, true);
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.userHasAccess(authzToken, resourceId, permissionType);
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.createGroupResourceProfile(authzToken, groupResourceProfile);
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        airavataService.updateGroupResourceProfile(authzToken, groupResourceProfile);
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupResourceProfile(authzToken, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.removeGroupResourceProfile(authzToken, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupResourceList(authzToken, gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputePrefs(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.removeGroupComputePrefs(authzToken, computeResourceId, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.removeGroupComputeResourcePolicy(authzToken, resourcePolicyId);
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.removeGroupBatchQueueResourcePolicy(authzToken, resourcePolicyId);
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupComputeResourcePreference(authzToken, computeResourceId, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupComputeResourcePolicy(authzToken, resourcePolicyId);
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getBatchQueueResourcePolicy(authzToken, resourcePolicyId);
    }

    @Override
    @SecurityCheck
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupComputeResourcePrefList(authzToken, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupBatchQueueResourcePolicyList(authzToken, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getGroupComputeResourcePolicyList(authzToken, groupResourceProfileId);
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        return airavataService.retrieveGatewayGroups(gatewayId);
    }

    @Override
    @SecurityCheck
    public Parser getParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getParser(parserId, gatewayId);
    }

    @Override
    @SecurityCheck
    public String saveParser(AuthzToken authzToken, Parser parser)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.saveParser(parser);
    }

    @Override
    @SecurityCheck
    public List<Parser> listAllParsers(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.listAllParsers(gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean removeParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws AiravataSystemException {
        airavataService.removeParser(parserId, gatewayId);
        return true;
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getParsingTemplate(templateId, gatewayId);
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            AuthzToken authzToken, String experimentId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
    }

    @Override
    @SecurityCheck
    public String saveParsingTemplate(AuthzToken authzToken, ParsingTemplate parsingTemplate)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.saveParsingTemplate(parsingTemplate);
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        airavataService.removeParsingTemplate(templateId, gatewayId);
        return true;
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> listAllParsingTemplates(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException {
        return airavataService.listAllParsingTemplates(gatewayId);
    }

    /**
     * To hold storage info context (login username, credential token, and adaptor)
     */
}
