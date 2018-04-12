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
package org.apache.airavata.registry.api.service.handler;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.DuplicateEntryException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.error.ProjectNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ProjectSearchFields;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.api.registry_apiConstants;
import org.apache.airavata.registry.core.app.catalog.resources.CloudSubmissionResource;
import org.apache.airavata.registry.core.app.catalog.resources.GridftpDataMovementResource;
import org.apache.airavata.registry.core.app.catalog.resources.LocalDataMovementResource;
import org.apache.airavata.registry.core.app.catalog.resources.LocalSubmissionResource;
import org.apache.airavata.registry.core.app.catalog.resources.ScpDataMovementResource;
import org.apache.airavata.registry.core.app.catalog.resources.SshJobSubmissionResource;
import org.apache.airavata.registry.core.app.catalog.resources.UnicoreDataMovementResource;
import org.apache.airavata.registry.core.app.catalog.resources.UnicoreJobSubmissionResource;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.core.experiment.catalog.resources.AbstractExpCatResource;
import org.apache.airavata.registry.core.repositories.appcatalog.ApplicationDeploymentRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.ApplicationInterfaceRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.GroupResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.GwyResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.StorageResourceRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.UserResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.core.repositories.replicacatalog.DataReplicaLocationRepository;
import org.apache.airavata.registry.core.repositories.workflowcatalog.WorkflowRepository;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExpCatParentDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryServerHandler implements RegistryService.Iface {
    private final static Logger logger = LoggerFactory.getLogger(RegistryServerHandler.class);

    private ExperimentCatalog experimentCatalog;
    private ApplicationDeploymentRepository applicationDeploymentRepository = new ApplicationDeploymentRepository();
    private ApplicationInterfaceRepository applicationInterfaceRepository = new ApplicationInterfaceRepository();
    private UserResourceProfileRepository userResourceProfileRepository = new UserResourceProfileRepository();
    private DataProductRepository dataProductRepository = new DataProductRepository();
    private DataReplicaLocationRepository dataReplicaLocationRepository = new DataReplicaLocationRepository();
    private WorkflowRepository workflowRepository = new WorkflowRepository();
    private StorageResourceRepository storageResourceRepository = new StorageResourceRepository();

    /**
     * Fetch Apache Registry API version
     */
    @Override
    public String getAPIVersion() throws RegistryServiceException, TException {
        return registry_apiConstants.REGISTRY_API_VERSION;
    }

    /**
     * Verify if User Exists within Airavata.
     *
     * @param gatewayId
     * @param userName
     * @return true/false
     */
    @Override
    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException, TException {
        try {
            return ExpCatResourceUtils.isUserExist(userName, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while verifying user", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while verifying user. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get all users in the gateway
     *
     * @param gatewayId The gateway data model.
     * @return users
     * list of usernames of the users in the gateway
     */
    @Override
    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException, TException {
        try {
            return ExpCatResourceUtils.getAllUsersInGateway(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving users", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving users. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Gateway details by providing gatewayId
     *
     * @param gatewayId The gateway Id of the Gateway.
     * @return gateway
     * Gateway obejct.
     */
    @Override
    public Gateway getGateway(String gatewayId) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Gateway
     *
     * @param gatewayId The gateway Id of the Gateway to be deleted.
     * @return boolean
     * Boolean identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteGateway(String gatewayId) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get All the Gateways Connected to Airavata.
     */
    @Override
    public List<Gateway> getAllGateways() throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Check for the Existance of a Gateway within Airavata
     *
     * @param gatewayId Provide the gatewayId of the gateway you want to check the existancy
     * @return gatewayId
     * return the gatewayId of the existing gateway.
     */
    @Override
    public boolean isGatewayExist(String gatewayId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            experimentCatalog.remove(ExperimentCatalogModelType.NOTIFICATION, notificationId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public Notification getNotification(String gatewayId, String notificationId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return (Notification)experimentCatalog.get(ExperimentCatalogModelType.NOTIFICATION, notificationId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retreiving notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<Notification> getAllNotifications(String gatewayId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            List<Object> objectList = experimentCatalog.get(ExperimentCatalogModelType.NOTIFICATION, null, gatewayId);
            List<Notification> notifications = new ArrayList<>();
            for(Object o : objectList)
                notifications.add((Notification) o);
            return notifications;
        } catch (RegistryException e) {
            logger.error("Error while getting all notifications", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting all notifications. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get a Project by ID
     * This method is to obtain a project by providing a projectId.
     *
     * @param projectId projectId of the project you require.
     * @return project
     * project data model will be returned.
     */
    @Override
    public Project getProject(String projectId) throws RegistryServiceException, TException {
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
            return project;
        } catch (RegistryException e) {
            logger.error("Error while retrieving the project", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Project
     * This method is used to delete an existing Project.
     *
     * @param projectId projectId of the project you want to delete.
     * @return boolean
     * Boolean identifier for the success or failure of the deletion operation.
     * <p>
     * NOTE: This method is not used within gateways connected with Airavata.
     */
    @Override
    public boolean deleteProject(String projectId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }

            experimentCatalog.remove(ExperimentCatalogModelType.PROJECT, projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId );
            return true;
        } catch (RegistryException e) {
            logger.error("Error while removing the project", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get All User Projects
     * Get all Project for the user with pagination. Results will be ordered based on creation time DESC.
     *
     * @param gatewayId The identifier for the requested gateway.
     * @param userName  The identifier of the user.
     * @param limit     The amount results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset) throws RegistryServiceException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        List<Project> projects = new ArrayList<Project>();
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return projects;
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Experiment Statistics
     * Get Experiment Statisitics for a given gateway for a specific time period. This feature is available only for admins of a particular gateway. Gateway admin access is managed by the user roles.
     *
     * @param gatewayId Unique identifier of the gateway making the request to fetch statistics.
     * @param fromTime  Starting date time.
     * @param toTime    Ending data time.
     */
    @Override
    public ExperimentStatistics getExperimentStatistics(String gatewayId, long fromTime, long toTime, String userName, String applicationName, String resourceHostName) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            Map<String, String> filters = new HashMap();
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, fromTime+"");
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, toTime+"");
            if (userName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            }
            if (applicationName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, applicationName);
            }
            if (resourceHostName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.RESOURCE_HOST_ID, resourceHostName);
            }

            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT_STATISTICS, filters);
            logger.debug("Airavata retrieved experiments for gateway id : " + gatewayId + " between : " + AiravataUtils.getTime(fromTime) + " and " + AiravataUtils.getTime(toTime));
            return (ExperimentStatistics) results.get(0);
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get All Experiments of the Project
     * Get Experiments within project with pagination. Results will be sorted based on creation time DESC.
     *
     * @param projectId Uniqie identifier of the project.
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<ExperimentModel> getExperimentsInProject(String projectId, int limit, int offset) throws RegistryServiceException, TException {
        if (!validateString(projectId)){
            logger.error("Project id cannot be empty. Please provide a valid project ID...");
            RegistryServiceException exception = new RegistryServiceException();
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get All Experiments of the User
     * Get experiments by user with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Identifier of the requesting gateway.
     * @param userName  Username of the requested end user.
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset) throws RegistryServiceException, TException {
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
        List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
        try {
            if (!ExpCatResourceUtils.isUserExist(userName, gatewayId)){
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return experiments;
            }
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete an Experiment
     * If the experiment is not already launched experiment can be deleted.
     *
     * @param experimentId@return boolean
     *                            Identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteExperiment(String experimentId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, experimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);

            if(!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)){
                logger.error("Error while deleting the experiment");
                throw new ExperimentCatalogException("Experiment is not in CREATED state. Hence cannot deleted. ID:"+ experimentId);
            }
            experimentCatalog.remove(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            logger.debug("Airavata removed experiment with experiment id : " + experimentId);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * *
     * * Get Experiment
     * * Fetch previously created experiment metadata.
     * *
     * * @param airavataExperimentId
     * *    The unique identifier of the requested experiment. This ID is returned during the create experiment step.
     * *
     * * @return ExperimentModel
     * *   This method will return the previously stored experiment metadata.
     * *
     * * @throws org.apache.airavata.model.error.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     * *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * *
     * * @throws org.apache.airavata.model.error.AiravataClientException
     * *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     * *
     * *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     * *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     * *         gateway registration steps and retry this request.
     * *
     * *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     * *         For now this is a place holder.
     * *
     * *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     * *         is implemented, the authorization will be more substantial.
     * *
     * * @throws org.apache.airavata.model.error.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param airavataExperimentId
     */
    @Override
    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryServiceException, TException {
        ExperimentModel experimentModel = getExperimentInternal(airavataExperimentId);
        return experimentModel;
    }

    /**
     * Get Complete Experiment Details
     * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
     * tasks -> jobs information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return ExperimentModel
     * This method will return the previously stored experiment metadata including application input parameters, computational resource scheduling
     * information, special input output handling and additional quality of service parameters.
     * @throws InvalidRequestException     For any incorrect forming of the request itself.
     * @throws ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                     <p>
     *                                     UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                     step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                     gateway registration steps and retry this request.
     *                                     <p>
     *                                     AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                     For now this is a place holder.
     *                                     <p>
     *                                     INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                     is implemented, the authorization will be more substantial.
     * @throws AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                     rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Experiment Status
     * <p>
     * Obtain the status of an experiment by providing the Experiment Id
     *
     * @param airavataExperimentId Experiment ID of the experimnet you require the status.
     * @return ExperimentStatus
     * ExperimentStatus model with the current status will be returned.
     */
    @Override
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryServiceException, TException {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : " + airavataExperimentId);
        return experimentStatus;
    }

    /**
     * Get Experiment Outputs
     * This method to be used when need to obtain final outputs of a certain Experiment
     *
     * @param airavataExperimentId Experiment ID of the experimnet you need the outputs.
     * @return list
     * List of experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
     */
    @Override
    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Intermediate Experiment Outputs
     * This method to be used when need to obtain intermediate outputs of a certain Experiment
     *
     * @param airavataExperimentId Experiment ID of the experimnet you need intermediate outputs.
     * @return list
     * List of intermediate experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
     */
    @Override
    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws RegistryServiceException, TException {
        return null;
    }

    /**
     * Get Job Statuses for an Experiment
     * This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup
     *
     * @param airavataExperimentId@return JobStatus
     *                                    Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map
     */
    @Override
    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryServiceException, TException {
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
                                    List<JobStatus> status = jobModel.getJobStatuses();
                                    if (status != null && status.size()>0){
                                        jobStatus.put(jobID, status.get(0));
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
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id) throws RegistryServiceException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (ExpCatChildDataType.PROCESS_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                experimentCatalog.add(ExpCatChildDataType.PROCESS_OUTPUT, outputs, id);
            }
            else if(ExpCatChildDataType.EXPERIMENT_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_OUTPUT, outputs, id);
            }
        } catch (Exception e) {
            logger.error(id, "Error while adding outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryServiceException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (ExpCatChildDataType.EXPERIMENT_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_ERROR, errorModel, id);
            }
            else if (ExpCatChildDataType.TASK_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentCatalog.add(ExpCatChildDataType.TASK_ERROR, errorModel, id);
            }
            else if (ExpCatChildDataType.PROCESS_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentCatalog.add(ExpCatChildDataType.PROCESS_ERROR, errorModel, id);
            }
        } catch (Exception e) {
            logger.error(id, "Error while adding error", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding error. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, taskStatus, taskId);
        } catch (Exception e) {
            logger.error(taskId, "Error while adding task status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding task status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.add(ExpCatChildDataType.PROCESS_STATUS, processStatus, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding process status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding process status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.update(ExperimentCatalogModelType.PROCESS_STATUS, processStatus, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while updating process status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating process status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT_STATUS, experimentStatus, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while updating experiment status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            CompositeIdentifier ids = new CompositeIdentifier(taskId, jobId);
            experimentCatalog.add(ExpCatChildDataType.JOB_STATUS, jobStatus, ids);
        } catch (Exception e) {
            logger.error(jobId, "Error while adding job status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.add(ExpCatChildDataType.JOB, jobModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding job ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (String)experimentCatalog.add(ExpCatChildDataType.PROCESS, processModel, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while adding process ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding process. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while updating process ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating process. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (String)experimentCatalog.add(ExpCatChildDataType.TASK, taskModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding task ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding task. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (UserConfigurationDataModel) experimentCatalog.get(ExperimentCatalogModelType.USER_CONFIGURATION_DATA, experimentId);
        }
        catch (Exception e) {
            logger.error(experimentId, "Error while getting user configuration ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding task. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while retrieving user configuration ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving user configuration. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException, TException {
        try {
            List<ProcessModel> processModels = new ArrayList<ProcessModel>();
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            List<Object> processList = experimentCatalog.get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);

            if (processList != null && !processList.isEmpty()) {
                for (Object processObject : processList) {
                    ProcessModel processModel = (ProcessModel)processObject;
                    processModels.add(processModel);
                }
            }
            return processModels;

        } catch (Exception e) {
            logger.error(experimentId, "Error while retrieving process list ", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving process list. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (ProcessStatus) experimentCatalog.get(ExperimentCatalogModelType.PROCESS_STATUS, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while retrieving process status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving process status. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     *
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public boolean isJobExist(String queryType, String id) throws RegistryServiceException, TException {
        try {
            JobModel jobModel = fetchJobModel(queryType, id);
            return jobModel != null;
        } catch (Exception e) {
            logger.error(id, "Error while retrieving job", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving job. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     *
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public JobModel getJob(String queryType, String id) throws RegistryServiceException, TException {
        try {
            JobModel jobModel = fetchJobModel(queryType, id);
            if (jobModel != null) return jobModel;
            throw new Exception("Job not found for queryType: " + queryType + ", id: " + id);
        } catch (Exception e) {
            logger.error(id, "Error while retrieving job", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving job. More info : " + e.getMessage());
            throw exception;
        }
    }

    private JobModel fetchJobModel(String queryType, String id) throws RegistryException {
        experimentCatalog = RegistryFactory.getDefaultExpCatalog();
        if (queryType.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
            List<Object> jobs = experimentCatalog.get(ExperimentCatalogModelType.JOB, Constants.FieldConstants.JobConstants.TASK_ID, id);
            JobModel jobModel = null;
            if (jobs != null) {
                for (Object object : jobs) {
                    jobModel = ((JobModel) object);
                    if (jobModel.getJobId() != null || !jobModel.equals("")) {
                        return jobModel;
                    }
                }
            }
        }
        else if (queryType.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
            List<Object> objects = experimentCatalog.get(ExperimentCatalogModelType.JOB,
                    Constants.FieldConstants.JobConstants.PROCESS_ID, id);
            JobModel jobModel = null;
            if (objects != null) {
                for (Object object : objects) {
                    jobModel = ((JobModel) object);
                    if (jobModel.getJobId() != null || !jobModel.equals("")) {
                        return jobModel;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return (List<OutputDataObjectType> )experimentCatalog.get(ExperimentCatalogModelType.PROCESS_OUTPUT, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while retrieving process outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving process outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            return experimentCatalog.getIds(ExperimentCatalogModelType.PROCESS, AbstractExpCatResource.ProcessConstants.EXPERIMENT_ID, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while retrieving process ids", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving process ids. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Job Details for all the jobs within an Experiment.
     * This method to be used when need to get the job details for one or many jobs of an Experiment.
     *
     * @param airavataExperimentId@return list of JobDetails
     *                                    Job details.
     */
    @Override
    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Application Module.
     *
     * @param appModuleId The unique identifier of the application module required
     * @return applicationModule
     * Returns an Application Module Object.
     */
    @Override
    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryServiceException, TException {
        try {
            ApplicationModule module = applicationInterfaceRepository.getApplicationModule(appModuleId);
            logger.debug("Airavata retrieved application module with module id : " + appModuleId);
            return module;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the adding application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @return list
     * Returns the list of all Application Module Objects.
     */
    @Override
    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationModule> moduleList = applicationInterfaceRepository.getAllApplicationModules(gatewayId);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppIds App IDs that are accessible to the user
     * @return list
     * Returns the list of all Application Module Objects that are accessible to the user.
     */
    @Override
    public List<ApplicationModule> getAccessibleAppModules(String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationModule> moduleList = applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete an Application Module.
     *
     * @param appModuleId The identifier of the Application Module to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationModule(String appModuleId) throws RegistryServiceException, TException {
        try {
            logger.debug("Airavata deleted application module with module id : " + appModuleId);
            return applicationInterfaceRepository.removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while deleting application module...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting the application module. More info : " + e.getMessage());
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
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws RegistryServiceException, TException {
        try {
            ApplicationDeploymentDescription deployement = applicationDeploymentRepository.getApplicationDeployement(appDeploymentId);
            logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
            return deployement;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete an Application Deployment.
     *
     * @param appDeploymentId The unique identifier of application deployment to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryServiceException, TException {
        try {
            applicationDeploymentRepository.removeAppDeployment(appDeploymentId);
            logger.debug("Airavata removed application deployment with deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while deleting application deployment...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @param gatewayId
     * @return list<applicationDeployment.
     * Returns the list of all application Deployment Objects.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationDeploymentDescription> deployements = applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppDeploymentIds App IDs that are accessible to the user
     * @return list<applicationDeployment.
     * Returns the list of all application Deployment Objects  that are accessible to the user.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationDeploymentDescription> deployements = applicationDeploymentRepository.getAccessibleApplicationDeployements(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            RegistryServiceException exception = new RegistryServiceException();
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
    public List<String> getAppModuleDeployedResources(String appModuleId) throws RegistryServiceException, TException {
        try {
            List<String> appDeployments = new ArrayList<>();
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments = applicationDeploymentRepository.getApplicationDeployements(filters);
            for (ApplicationDeploymentDescription description : applicationDeployments){
                appDeployments.add(description.getAppDeploymentId());
            }
            logger.debug("Airavata retrieved application deployments for module id : " + appModuleId);
            return appDeployments;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws RegistryServiceException, TException {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments = applicationDeploymentRepository.getApplicationDeployements(filters);
            return applicationDeployments;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch an Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface.
     * @return applicationInterface
     * Returns an application Interface Object.
     */
    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws RegistryServiceException, TException {
        try {
            ApplicationInterfaceDescription interfaceDescription = applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
            logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
            return interfaceDescription;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application interface...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete an Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryServiceException, TException {
        try {
            boolean removeApplicationInterface = applicationInterfaceRepository.removeApplicationInterface(appInterfaceId);
            logger.debug("Airavata removed application interface with interface id : " + appInterfaceId);
            return removeApplicationInterface;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while deleting application interface...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch name and ID of  Application Interface documents.
     *
     * @param gatewayId
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing ID's
     */
    @Override
    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationInterfaceDescription> allApplicationInterfaces = applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
            Map<String, String> allApplicationInterfacesMap = new HashMap<>();
            if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()){
                for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces){
                    allApplicationInterfacesMap.put(interfaceDescription.getApplicationInterfaceId(), interfaceDescription.getApplicationName());
                }
            }
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return allApplicationInterfacesMap;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application interfaces...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Interface documents.
     *
     * @param gatewayId
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces documents (Application Interface ID, name, description, Inputs and Outputs objects).
     */
    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationInterfaceDescription> interfaces = applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return interfaces;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application interfaces...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the list of Application Inputs.
     *
     * @param appInterfaceId The identifier of the application interface which need inputs to be fetched.
     * @return list<application_interface_model.InputDataObjectType>
     * Returns a list of application inputs.
     */
    @Override
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws RegistryServiceException, TException {
        try {
            List<InputDataObjectType> applicationInputs = applicationInterfaceRepository.getApplicationInputs(appInterfaceId);
            logger.debug("Airavata retrieved application inputs for application interface id : " + appInterfaceId);
            return applicationInputs;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application inputs...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving application inputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch list of Application Outputs.
     *
     * @param appInterfaceId The identifier of the application interface which need outputs to be fetched.
     * @return list<application_interface_model.OutputDataObjectType>
     * Returns a list of application outputs.
     */
    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryServiceException, TException {
        List<OutputDataObjectType> list = getApplicationOutputsInternal(appInterfaceId);
        logger.debug("Airavata retrieved application outputs for app interface id : " + appInterfaceId);
        return list;
    }

    /**
     * Fetch a list of all deployed Compute Hosts for a given application interfaces.
     *
     * @param appInterfaceId The identifier for the requested application interface.
     * @return map<computeResourceId, computeResourceName>
     * A map of registered compute resource id's and their corresponding hostnames.
     * Deployments of each modules listed within the interfaces will be listed.
     */
    @Override
    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) throws RegistryServiceException, TException {
        try {
            Map<String, String> allComputeResources = new ComputeResourceRepository().getAvailableComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface = applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules) {
                    filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, moduleId);
                    List<ApplicationDeploymentDescription> applicationDeployments =
                            applicationDeploymentRepository.getApplicationDeployements(filters);
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
            RegistryServiceException exception = new RegistryServiceException();
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
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryServiceException, TException {
        try {
            ComputeResourceDescription computeResource = new ComputeResourceRepository().getComputeResource(computeResourceId);
            logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
            return computeResource;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while retrieving compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
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
    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException, TException {
        try {
            Map<String, String> computeResourceIdList = new ComputeResourceRepository().getAllComputeResourceIdList();
            logger.debug("Airavata retrieved all the available compute resources...");
            return computeResourceIdList;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
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
    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().removeComputeResource(computeResourceId);
            logger.debug("Airavata deleted compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while deleting compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the given Storage Resource.
     *
     * @param storageResourceId The identifier for the requested storage resource
     * @return storageResourceDescription
     * Storage Resource Object created from the datamodel..
     */
    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId) throws RegistryServiceException, TException {
        try {
            StorageResourceDescription storageResource = storageResourceRepository.getStorageResource(storageResourceId);
            logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
            return storageResource;
        } catch (AppCatalogException e) {
            logger.error(storageResourceId, "Error while retrieving storage resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all registered Storage Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException, TException {
        try {
            Map<String, String> resourceIdList = storageResourceRepository.getAllStorageResourceIdList();
            logger.debug("Airavata retrieved storage resources list...");
            return resourceIdList;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving storage resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving storage resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Storage Resource.
     *
     * @param storageResourceId The identifier of the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException, TException {
        try {
            storageResourceRepository.removeStorageResource(storageResourceId);
            logger.debug("Airavata deleted storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(storageResourceId, "Error while deleting storage resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting storage resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns localJobSubmission object
     *
     * @param jobSubmissionId@return LOCALSubmission instance
     */
    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            LOCALSubmission localJobSubmission = new ComputeResourceRepository().getLocalJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
            return localJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns SSHJobSubmission object
     *
     * @param jobSubmissionId@return SSHJobSubmission instance
     */
    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            SSHJobSubmission sshJobSubmission = new ComputeResourceRepository().getSSHJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
            return sshJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * *
     * * This method returns UnicoreJobSubmission object
     * *
     * * @param jobSubmissionInterfaceId
     * *   The identifier of the JobSubmission Interface to be retrieved.
     * *  @return UnicoreJobSubmission instance
     * *
     * *
     *
     * @param jobSubmissionId
     */
    @Override
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            UnicoreJobSubmission unicoreJobSubmission = new ComputeResourceRepository().getUNICOREJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
            return unicoreJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * *
     * * This method returns cloudJobSubmission object
     * * @param jobSubmissionInterfaceI
     * *   The identifier of the JobSubmission Interface to be retrieved.
     * *  @return CloudJobSubmission instance
     * *
     *
     * @param jobSubmissionId
     */
    @Override
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            CloudJobSubmission cloudJobSubmission = new ComputeResourceRepository().getCloudJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
            return cloudJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns local datamovement object.
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return LOCALDataMovement instance
     */
    @Override
    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            LOCALDataMovement localDataMovement = new ComputeResourceRepository().getLocalDataMovement(dataMovementId);
            logger.debug("Airavata retrieved local data movement with data movement id: " + dataMovementId);
            return localDataMovement;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns SCP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return SCPDataMovement instance
     */
    @Override
    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            SCPDataMovement scpDataMovement = new ComputeResourceRepository().getSCPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved SCP data movement with data movement id: " + dataMovementId);
            return scpDataMovement;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving SCP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns UNICORE datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return UnicoreDataMovement instance
     */
    @Override
    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            UnicoreDataMovement unicoreDataMovement = new ComputeResourceRepository().getUNICOREDataMovement(dataMovementId);
            logger.debug("Airavata retrieved UNICORE data movement with data movement id: " + dataMovementId);
            return unicoreDataMovement;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving UNICORE data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * This method returns GridFTP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return GridFTPDataMovement instance
     */
    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            GridFTPDataMovement gridFTPDataMovement = new ComputeResourceRepository().getGridFTPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved GRIDFTP data movement with data movement id: " + dataMovementId);
            return gridFTPDataMovement;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            RegistryServiceException exception = new RegistryServiceException();
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
    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder) throws RegistryServiceException, TException {
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
    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder) throws RegistryServiceException, TException {
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
    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap) throws RegistryServiceException, TException {
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
    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap) throws RegistryServiceException, TException {
        return false;
    }

    /**
     * Delete a given job submisison interface
     *
     * @param computeResourceId
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            logger.debug("Airavata deleted job submission interface with interface id : " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting job submission interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws RegistryServiceException, TException {
        try {
            return new ComputeResourceRepository().getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Compute Resource Queue
     *
     * @param computeResourceId The identifier of the compute resource which has the queue to be deleted
     * @param queueName         Name of the queue need to be deleted. Name is the uniqueue identifier for the queue within a compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while deleting batch queue...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting batch queue. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource.
     * @return gatewayResourceProfile
     * Gateway Resource Profile Object.
     */
    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
            return gatewayResourceProfile;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while retrieving gateway resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving gateway resource profile. More info : " + e.getMessage());
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
    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            gwyResourceProfileRepository.delete(gatewayID);
            logger.debug("Airavata deleted gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while removing gateway resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing gateway resource profile. More info : " + e.getMessage());
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
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                RegistryServiceException exception = new RegistryServiceException();
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }
            if (!computeResourceRepository.isComputeResourceExists(computeResourceId)){
                logger.error(computeResourceId, "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                RegistryServiceException exception = new RegistryServiceException();
                exception.setMessage("Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw exception;
            }
            ComputeResourcePreference computeResourcePreference = gwyResourceProfileRepository.getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Airavata retrieved gateway compute resource preference with gateway id : " + gatewayID + " and for compute resoruce id : " + computeResourceId );
            return computeResourcePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Stprage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                RegistryServiceException exception = new RegistryServiceException();
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }

            StoragePreference storagePreference = gwyResourceProfileRepository.getStoragePreference(gatewayID, storageId);
            logger.debug("Airavata retrieved storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
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
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Storage Resource Preferences of a registered gateway profile.
     *
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getStoragePreferences();
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Gateway Profiles registered
     *
     * @return GatewayResourceProfile
     * Returns all the GatewayResourcePrifle list object.
     */
    @Override
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryServiceException, TException {
        try {
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getAllGatewayProfiles();
        } catch (Exception e) {
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
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
    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();

            return gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID The identifier of the gateway profile to be deleted.
     * @param storageId ID of the storage preference you want to delete.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }

            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.removeDataStoragePreferenceFromGateway(gatewayID, storageId);
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayId@return status
     *                         Returns a success/failure of the deletion.
     */
    @Override
    public List<String> getAllWorkflows(String gatewayId) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return workflowRepository.getAllWorkflows(gatewayId);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in retrieving all workflow template Ids.";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * API Methods Related for Work-Flow Submission Features.
     *
     * @param workflowTemplateId
     */
    @Override
    public WorkflowModel getWorkflow(String workflowTemplateId) throws RegistryServiceException, TException {
        try {
            return workflowRepository.getWorkflow(workflowTemplateId);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in retrieving the workflow "+workflowTemplateId+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void deleteWorkflow(String workflowTemplateId) throws RegistryServiceException, TException {
        try {
            workflowRepository.deleteWorkflow(workflowTemplateId);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in deleting the workflow "+workflowTemplateId+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String getWorkflowTemplateId(String workflowName) throws RegistryServiceException, TException {
        try {
            return workflowRepository.getWorkflowTemplateId(workflowName);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in retrieving the workflow template id for "+workflowName+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean isWorkflowExistWithName(String workflowName) throws RegistryServiceException, TException {
        try {
            return workflowRepository.isWorkflowExistWithName(workflowName);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException, TException {
        try {
            DataProductModel dataProductModel = dataProductRepository.getDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the data product "+productUri+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws RegistryServiceException, TException {
        try {
            DataProductModel dataProductModel = dataProductRepository.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the parent data product for "+ productUri+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryServiceException, TException {
        try {
            List<DataProductModel> dataProductModels = dataProductRepository.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the child products for "+productUri+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(String gatewayId, String userId, String productName, int limit, int offset) throws RegistryServiceException, TException {
        try {
            List<DataProductModel> dataProductModels = dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return dataProductModels;
        } catch (RegistryException e) {
            String msg = "Error in searching the data products for name " + productName + ".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(groupResourceProfile.getGatewayId())){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
            logger.debug("New Group Resource Profile Created: " + groupResourceProfileId);
            return groupResourceProfileId;
        } catch (Exception e) {
            logger.error("Error while creating group resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while creating group resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfile.getGroupResourceProfileId())) {
                logger.error("Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw exception;
            }
            String groupResourceProfileId = groupResourceProfileRepository.updateGroupResourceProfile(groupResourceProfile);
            logger.debug(" Group Resource Profile updated: " + groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while updating group resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating group resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("No group resource profile found with matching gatewayId and groupResourceProfileId");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw exception;
            }

            return groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving group resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving group resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw exception;
            }
            return groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while removing group resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing group resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
        } catch (Exception e) {
            logger.error("Error while retrieving group resource list ", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving group resource list. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            groupResourceProfileRepository.removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            logger.debug("Removed compute resource preferences with compute resource ID: "+ computeResourceId);
            return true;
        } catch (Exception e) {
            logger.error("Error while removing group compute preference", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing group compute preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            groupResourceProfileRepository.removeComputeResourcePolicy(resourcePolicyId);
            logger.debug("Removed compute resource policy with resource policy ID: "+ resourcePolicyId);
            return true;
        } catch (Exception e) {
            logger.error("Error while removing group compute resource policy", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing group compute resource policy. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            groupResourceProfileRepository.removeBatchQueueResourcePolicy(resourcePolicyId);
            logger.debug("Removed batch resource policy with resource policy ID: "+ resourcePolicyId);
            return true;
        } catch (Exception e) {
            logger.error("Error while removing group batch queue resource policy", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing group batch queue resource policy. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(String computeResourceId, String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            GroupComputeResourcePreference groupComputeResourcePreference = groupResourceProfileRepository.getGroupComputeResourcePreference(
                                                                        computeResourceId, groupResourceProfileId);
            if (!(groupComputeResourcePreference != null)) {
                logger.error("GroupComputeResourcePreference not found");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("GroupComputeResourcePreference not found ");
                throw exception;
            }
            return groupComputeResourcePreference;
        } catch (Exception e) {
            logger.error("Error while retrieving group compute resource preference", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving group compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            ComputeResourcePolicy computeResourcePolicy = groupResourceProfileRepository.getComputeResourcePolicy(resourcePolicyId);
            if (!(computeResourcePolicy != null)) {
                logger.error("Group Compute Resource policy not found");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Group Compute Resource policy not found ");
                throw exception;
            }
            return computeResourcePolicy;
        } catch (Exception e) {
            logger.error("Error while retrieving group compute resource policy", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving group compute resource policy. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            BatchQueueResourcePolicy batchQueueResourcePolicy = groupResourceProfileRepository.getBatchQueueResourcePolicy(resourcePolicyId);
            if(!(batchQueueResourcePolicy != null)) {
                logger.error("Group Batch Queue Resource policy not found");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Group Batch Queue Resource policy not found ");
                throw exception;
            }
            return batchQueueResourcePolicy;
        } catch (Exception e) {
            logger.error("Error while retrieving Batch Queue resource policy", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving Batch Queue resource policy. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupComputeResourcePreferences(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving retrieving Group Compute Resource Preference list", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving retrieving Group Compute Resource Preference list. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving retrieving Group Batch Queue Resource policy list", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving retrieving Group Batch Queue Resource policy list. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving retrieving Group Compute Resource policy list", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving retrieving Group Compute Resource policy list. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws RegistryServiceException, TException {
        try {
            String replicaId = dataReplicaLocationRepository.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the replica "+replicaLocationModel.getReplicaName()+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * API Methods related to replica catalog
     *
     * @param dataProductModel
     */
    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryServiceException, TException {
        try {
            String productUrl = dataProductRepository.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (RegistryException e) {
            String msg = "Error in registering the data resource"+dataProductModel.getProductName()+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateWorkflow(String workflowTemplateId, WorkflowModel workflow) throws RegistryServiceException, TException {
        try {
            workflowRepository.updateWorkflow(workflowTemplateId, workflow);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in updating the workflow "+workflow.getName()+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String registerWorkflow(String gatewayId, WorkflowModel workflow) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return workflowRepository.registerWorkflow(workflow, gatewayId);
        } catch (WorkflowCatalogException e) {
            String msg = "Error in registering the workflow "+workflow.getName()+".";
            logger.error(msg, e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier of the gateway profile to be updated.
     * @param storageId         The Storage resource identifier of the one that you want to update
     * @param storagePreference The storagePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateGatewayStoragePreference(String gatewayID, String storageId, StoragePreference storagePreference) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
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
            profile.getStoragePreferences().add(storagePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId );
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
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
    public boolean updateGatewayComputeResourcePreference(String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
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
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID         The identifier of the gateway profile to be added.
     * @param storageResourceId Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addGatewayStoragePreference(String gatewayID, String storageResourceId, StoragePreference dataStoragePreference) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }

            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!(gwyResourceProfileRepository.isExists(gatewayID))){
                throw new RegistryServiceException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);

            dataStoragePreference.setStorageResourceId(storageResourceId);
            profile.addToStoragePreferences(dataStoragePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageResourceId );
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
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
    public boolean addGatewayComputeResourcePreference(String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!(gwyResourceProfileRepository.isExists(gatewayID))){
                throw new RegistryServiceException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            profile.addToComputeResourcePreferences(computeResourcePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added gateway compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
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
    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile) throws RegistryServiceException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            gwyResourceProfileRepository.updateGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while updating gateway resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Gateway Resource Profile.
     *
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     *                               The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
     *                               resource profile.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) throws RegistryServiceException, TException {
        try {
            if (!validateString(gatewayResourceProfile.getGatewayID())){
                logger.error("Cannot create gateway profile with empty gateway id");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Cannot create gateway profile with empty gateway id");
                throw exception;
            }
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            String resourceProfile = gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata registered gateway profile with gateway id : " + gatewayResourceProfile.getGatewayID());
            return resourceProfile;
        } catch (Exception e) {
            logger.error("Error while registering gateway resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws RegistryServiceException, TException {
        try {
            return new ComputeResourceRepository().addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a given data movement interface
     *
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @param dmType
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType) throws RegistryServiceException, TException {
        try {
            switch (dmType){
                case COMPUTE_RESOURCE:
                    new ComputeResourceRepository().removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                case STORAGE_RESOURCE:
                    storageResourceRepository.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                default:
                    logger.error("Unsupported data movement type specifies.. Please provide the correct data movement type... ");
                    return false;
            }
        } catch (AppCatalogException e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param gridFTPDataMovement     The GridFTPDataMovement object to be updated.
     * @return boolean
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateGridFTPDataMovementDetails(String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws RegistryServiceException, TException {
        try {
            GridftpDataMovementResource movment = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated GRIDFTP data movement with data movement id: " + dataMovementInterfaceId );
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * productUri          The identifier of the compute resource to which dataMovement protocol to be added
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param gridFTPDataMovement The GridFTPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addGridFTPDataMovementDetails(String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String addDataMovementInterface = addDataMovementInterface(computeResourceRepository, computeResourceId, dmType,
                    computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement), DataMovementProtocol.GridFTP, priorityOrder);
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
     * Update a selected UNICORE data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param unicoreDataMovement
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUnicoreDataMovementDetails(String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws RegistryServiceException, TException {
        try {
            UnicoreDataMovementResource movment = AppCatalogThriftConversion.getUnicoreDMResource(unicoreDataMovement);
            movment.setDataMovementId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated UNICORE data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a UNICORE data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     *  productUri          The identifier of the compute resource to which data movement protocol to be added
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param unicoreDataMovement
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addUnicoreDataMovementDetails(String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(computeResourceRepository, resourceId, dmType,
                    computeResourceRepository.addUnicoreDataMovement(unicoreDataMovement), DataMovementProtocol.UNICORE_STORAGE_SERVICE, priorityOrder);
            logger.debug("Airavata registered UNICORE data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given scp data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param scpDataMovement         The SCPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement) throws RegistryServiceException, TException {
        try {
            ScpDataMovementResource movment = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated SCP data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a SCP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * productUri      The identifier of the compute resource to which JobSubmission protocol to be added
     * @param dmType
     * @param priorityOrder   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param scpDataMovement The SCPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addSCPDataMovementDetails(String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(computeResourceRepository, resourceId, dmType,
                    computeResourceRepository.addScpDataMovement(scpDataMovement), DataMovementProtocol.SCP, priorityOrder);
            logger.debug("Airavata registered SCP data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given Local data movement details
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param localDataMovement       The LOCALDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement) throws RegistryServiceException, TException {
        try {
            LocalDataMovementResource movment = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
            movment.setDataMovementInterfaceId(dataMovementInterfaceId);
            movment.save();
            logger.debug("Airavata updated local data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating local data movement interface..", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating local data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a Local data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * productUri        The identifier of the compute resource to which JobSubmission protocol to be added
     * @param dataMoveType
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localDataMovement The LOCALDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addLocalDataMovementDetails(String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(computeResourceRepository, resourceId, dataMoveType,
                    computeResourceRepository.addLocalDataMovement(localDataMovement), DataMovementProtocol.LOCAL, priorityOrder);
            logger.debug("Airavata registered local data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            logger.error(resourceId, "Error while adding data movement interface to resource resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding data movement interface to resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the UNIOCRE Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param unicoreJobSubmission
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUnicoreJobSubmissionDetails(String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws RegistryServiceException, TException {
        try {
            UnicoreJobSubmissionResource submission = AppCatalogThriftConversion.getUnicoreJobSubmission(unicoreJobSubmission);
            submission.setjobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated UNICORE job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the cloud Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission sshJobSubmission) throws RegistryServiceException, TException {
        try {
            CloudSubmissionResource submission = AppCatalogThriftConversion.getCloudJobSubmission(sshJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated Cloud job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission) throws RegistryServiceException, TException {
        try {
            SshJobSubmissionResource submission = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated SSH job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * *
     * * Add a Cloud Job Submission details to a compute resource
     * *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     * *
     * * @param computeResourceId
     * *   The identifier of the compute resource to which JobSubmission protocol to be added
     * *
     * * @param priorityOrder
     * *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * *
     * * @param sshJobSubmission
     * *   The SSHJobSubmission object to be added to the resource.
     * *
     * * @return status
     * *   Returns the unique job submission id.
     * *
     * *
     *
     * @param computeResourceId
     * @param priorityOrder
     * @param cloudSubmission
     */
    @Override
    public String addCloudJobSubmissionDetails(String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(computeResourceRepository, computeResourceId,
                    computeResourceRepository.addCloudJobSubmission(cloudSubmission), JobSubmissionProtocol.CLOUD, priorityOrder);
            logger.debug("Airavata registered Cloud job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a UNICORE Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId    The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder        Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param unicoreJobSubmission The UnicoreJobSubmission object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addUNICOREJobSubmissionDetails(String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(computeResourceRepository, computeResourceId,
                    computeResourceRepository.addUNICOREJobSubmission(unicoreJobSubmission), JobSubmissionProtocol.UNICORE, priorityOrder);
            logger.debug("Airavata registered UNICORE job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error("Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
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
     * Returns the unique job submission id.
     */
    @Override
    public String addSSHForkJobSubmissionDetails(String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionDetails = addJobSubmissionInterface(computeResourceRepository, computeResourceId,
                    computeResourceRepository.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH_FORK, priorityOrder);
            logger.debug("Airavata registered Fork job submission for compute resource id: " + computeResourceId);
            return submissionDetails;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
     * Returns the unique job submission id.
     */
    @Override
    public String addSSHJobSubmissionDetails(String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(computeResourceRepository, computeResourceId,
                    computeResourceRepository.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH, priorityOrder);
            logger.debug("Airavata registered SSH job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
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
    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission) throws RegistryServiceException, TException {
        try {
            LocalSubmissionResource submission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            logger.debug("Airavata updated local job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
     * Returns the unique job submission id.
     */
    @Override
    public String addLocalSubmissionDetails(String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws RegistryServiceException, TException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(computeResourceRepository, computeResourceId,
                    computeResourceRepository.addLocalJobSubmission(localSubmission), JobSubmissionProtocol.LOCAL, priorityOrder);
            logger.debug("Airavata added local job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Storage Resource.
     *
     * @param storageResourceId          The identifier for the requested compute resource to be updated.
     * @param storageResourceDescription Storage Resource Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateStorageResource(String storageResourceId, StorageResourceDescription storageResourceDescription) throws RegistryServiceException, TException {
        try {
            storageResourceRepository.updateStorageResource(storageResourceId, storageResourceDescription);
            logger.debug("Airavata updated storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(storageResourceId, "Error while updating storage resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updaing storage resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Storage Resource.
     *
     * @param storageResourceDescription Storge Resource Object created from the datamodel.
     * @return storageResourceId
     * Returns a server-side generated airavata storage resource globally unique identifier.
     */
    @Override
    public String registerStorageResource(StorageResourceDescription storageResourceDescription) throws RegistryServiceException, TException {
        try {
            String storageResource = storageResourceRepository.addStorageResource(storageResourceDescription);
            logger.debug("Airavata registered storage resource with storage resource Id : " + storageResource);
            return storageResource;
        } catch (AppCatalogException e) {
            logger.error("Error while saving storage resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while saving storage resource. More info : " + e.getMessage());
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
    public boolean updateComputeResource(String computeResourceId, ComputeResourceDescription computeResourceDescription) throws RegistryServiceException, TException {
        try {
            new ComputeResourceRepository().updateComputeResource(computeResourceId, computeResourceDescription);
            logger.debug("Airavata updated compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while updating compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updaing compute resource. More info : " + e.getMessage());
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
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription) throws RegistryServiceException, TException {
        try {
            String computeResource = new ComputeResourceRepository().addComputeResource(computeResourceDescription);
            logger.debug("Airavata registered compute resource with compute resource Id : " + computeResource);
            return computeResource;
        } catch (AppCatalogException e) {
            logger.error("Error while saving compute resource...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Application Interface.
     *
     * @param appInterfaceId       The identifier of the requested application deployment to be updated.
     * @param applicationInterface
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateApplicationInterface(String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws RegistryServiceException, TException {
        try {
            applicationInterfaceRepository.updateApplicationInterface(appInterfaceId, applicationInterface);
            logger.debug("Airavata updated application interface with interface id : " + appInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while updating application interface...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Application Interface.
     *
     * @param gatewayId
     * @param applicationInterface Application Module Object created from the datamodel.
     * @return appInterfaceId
     * Returns a server-side generated airavata application interface globally unique identifier.
     */
    @Override
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface) throws RegistryServiceException, TException {
            if (!isGatewayExistInternal(gatewayId)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            try {
                String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterface, gatewayId);
                logger.debug("Airavata registered application interface for gateway id : " + gatewayId);
                return interfaceId;
            } catch (AppCatalogException e) {
                logger.error("Error while adding application interface...", e);
                RegistryServiceException exception = new RegistryServiceException();
                exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
                throw exception;
            }
    }

    /**
     * Update an Application Deployment.
     *
     * @param appDeploymentId       The identifier of the requested application deployment to be updated.
     * @param applicationDeployment
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateApplicationDeployment(String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws RegistryServiceException, TException {
        try {
            applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            logger.debug("Airavata updated application deployment for deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while updating application deployment...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register an Application Deployment.
     *
     * @param gatewayId             ID of the gateway which is registering the new Application Deployment.
     * @param applicationDeployment Application Module Object created from the datamodel.
     * @return appDeploymentId
     * Returns a server-side generated airavata appDeployment globally unique identifier.
     */
    @Override
    public String registerApplicationDeployment(String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            String deployment = applicationDeploymentRepository.addApplicationDeployment(applicationDeployment, gatewayId);
            logger.debug("Airavata registered application deployment for gateway id : " + gatewayId);
            return deployment;
        } catch (AppCatalogException e) {
            logger.error("Error while adding application deployment...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding application deployment. More info : " + e.getMessage());
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
    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule) throws RegistryServiceException, TException {
        try {
            applicationInterfaceRepository.updateApplicationModule(appModuleId, applicationModule);
            logger.debug("Airavata updated application module with module id: " + appModuleId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Application Module.
     *
     * @param gatewayId
     * @param applicationModule Application Module Object created from the datamodel.
     * @return appModuleId
     * Returns the server-side generated airavata appModule globally unique identifier.
     * @gatewayId ID of the gateway which is registering the new Application Module.
     */
    @Override
    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws RegistryServiceException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            String module = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
            logger.debug("Airavata registered application module for gateway id : " + gatewayId);
            return module;
        } catch (AppCatalogException e) {
            logger.error("Error while adding application module...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateResourceScheduleing(String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating scheduling info. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration) throws RegistryServiceException, TException {
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating user configuration. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Previously Created Experiment
     * Configure the CREATED experiment with required inputs, scheduling and other quality of service parameters. This method only updates the experiment object within the registry.
     * The experiment has to be launched to make it actionable by the server.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experiment
     * @return This method call does not have a return value.
     * @throws InvalidRequestException     For any incorrect forming of the request itself.
     * @throws ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                     <p>
     *                                     UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                     step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                     gateway registration steps and retry this request.
     *                                     <p>
     *                                     AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                     For now this is a place holder.
     *                                     <p>
     *                                     INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                     is implemented, the authorization will be more substantial.
     * @throws AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                     rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Update request failed, Experiment {} doesn't exist.", airavataExperimentId);
                throw new RegistryServiceException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
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
                            ComputeResourceDescription computeResourceDescription = new ComputeResourceRepository()
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * *
     * * Create New Experiment
     * * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * *   but inferred from the sshKeyAuthentication header. This experiment is just a persistent place holder. The client
     * *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * *   registering the experiment in a persistent store.
     * *
     * * @param gatewayId
     * *    The unique ID of the gateway where the experiment is been created.
     * *
     * * @param ExperimentModel
     * *    The create experiment will require the basic experiment metadata like the name and description, intended user,
     * *      the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
     * *      the ExperimentMetadata is a required field.
     * *
     * * @return
     * *   The server-side generated.airavata.registry.core.experiment.globally unique identifier.
     * *
     * * @throws org.apache.airavata.model.error.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.model.error.AiravataClientException
     * *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     * *
     * *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     * *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     * *         gateway registration steps and retry this request.
     * *
     * *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     * *         For now this is a place holder.
     * *
     * *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     * *         is implemented, the authorization will be more substantial.
     * *
     * * @throws org.apache.airavata.model.error.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param gatewayId
     * @param experiment
     */
    @Override
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
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
                ComputeResourceDescription computeResourceDescription = new ComputeResourceRepository()
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
            logger.debug(experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getExperimentName());
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments.
     * Search Experiments by using multiple filter criteria with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Identifier of the requested gateway.
     * @param userName  Username of the user requesting the search function.
     * @param filters   Map of multiple filter criteria. Currenlt search filters includes Experiment Name, Description, Application, etc....
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     * @return ExperimentSummaryModel
     * List of experiments for the given search filter. Here only the Experiment summary will be returned.
     */
    @Override
    public List<ExperimentSummaryModel> searchExperiments(String gatewayId, String userName, List<String> accessibleExpIds,
                                                          Map<ExperimentSearchFields, String> filters, int limit, int offset) throws RegistryServiceException, TException {
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

            if(accessibleExpIds.size() == 0 && !ServerSettings.isEnableSharing()){
                if(!regFilters.containsKey(AbstractExpCatResource.ExperimentConstants.USER_NAME)){
                    regFilters.put(AbstractExpCatResource.ExperimentConstants.USER_NAME, userName);
                }
            }
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search User Projects
     * Search and get all Projects for user by project description or/and project name  with pagination.
     * Results will be ordered based on creation time DESC.
     *
     * @param gatewayId The unique identifier of the gateway making the request.
     * @param userName  The identifier of the user.
     * @param filters   Map of multiple filter criteria. Currenlt search filters includes Project Name and Project Description
     * @param limit     The amount results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<Project> searchProjects(String gatewayId, String userName, List<String> accessibleProjIds,
                                        Map<ProjectSearchFields, String> filters, int limit, int offset) throws RegistryServiceException, TException {
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

            if(accessibleProjIds.size() == 0 && !ServerSettings.isEnableSharing()){
                if(!regFilters.containsKey(AbstractExpCatResource.ProjectConstants.USERNAME)){
                    regFilters.put(AbstractExpCatResource.ProjectConstants.USERNAME, userName);
                }
            }

            List<Object> results = experimentCatalog.searchAllAccessible(ExperimentCatalogModelType.PROJECT, accessibleProjIds,
                    regFilters, limit, offset, Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                projects.add((Project)object);
            }
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
            return projects;
        }catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update an Existing Project
     *
     * @param projectId      The projectId of the project needed an update.
     * @param updatedProject
     * @return void
     * Currently this does not return any value.
     */
    @Override
    public void updateProject(String projectId, Project updatedProject) throws RegistryServiceException, TException {
        if (!validateString(projectId) || !validateString(projectId)){
            logger.error("Project id cannot be empty...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty...");
            throw exception;
        }
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(updatedProject.getGatewayId());
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }

            experimentCatalog.update(ExperimentCatalogModelType.PROJECT, updatedProject, projectId);
            logger.debug("Airavata updated project with project Id : " + projectId );
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Creates a Project with basic metadata.
     * A Project is a container of experiments.
     *
     * @param gatewayId The identifier for the requested gateway.
     * @param project
     */
    @Override
    public String createProject(String gatewayId, Project project) throws RegistryServiceException, TException {
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
            return projectId;
        } catch (Exception e) {
            logger.error("Error while creating the project", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean updateNotification(Notification notification) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(notification.getGatewayId());
            experimentCatalog.update(ExperimentCatalogModelType.NOTIFICATION, notification, notification.getGatewayId());
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * * API methods to retrieve notifications
     * *
     *
     * @param notification
     */
    @Override
    public String createNotification(Notification notification) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(notification.getGatewayId());
            return (String) experimentCatalog.add(ExpCatParentDataType.NOTIFICATION, notification, notification.getGatewayId());
        } catch (RegistryException e) {
            logger.error("Error while creating notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while creating notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update previously registered Gateway metadata.
     *
     * @param gatewayId      The gateway Id of the Gateway which require an update.
     * @param updatedGateway
     * @return gateway
     * Modified gateway obejct.
     * @throws AiravataClientException
     */
    @Override
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            experimentCatalog.update(ExperimentCatalogModelType.GATEWAY, updatedGateway, gatewayId);

            // check if gatewayprofile exists and check if the identity server password token equals the admin password token, if not update
            GatewayResourceProfile existingGwyResourceProfile = new GwyResourceProfileRepository().getGatewayProfile(gatewayId);
            if (existingGwyResourceProfile.getIdentityServerPwdCredToken() == null
                    || !existingGwyResourceProfile.getIdentityServerPwdCredToken().equals(updatedGateway.getIdentityServerPasswordToken())) {
                existingGwyResourceProfile.setIdentityServerPwdCredToken(updatedGateway.getIdentityServerPasswordToken());
                new GwyResourceProfileRepository().updateGatewayResourceProfile(gatewayId, existingGwyResourceProfile);
            }
            logger.debug("Airavata update gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating the gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating the gateway. More info : " + e.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error("Error while updating gateway profile", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Gateway with Airavata.
     *
     * @param gateway The gateway data model.
     * @return gatewayId
     * Th unique identifier of the  newly registered gateway.
     */
    @Override
    public String addGateway(Gateway gateway) throws RegistryServiceException, DuplicateEntryException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!validateString(gateway.getGatewayId())){
                logger.error("Gateway id cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            // check if gateway exists
            if (isGatewayExist(gateway.getGatewayId())) {
                throw new DuplicateEntryException("Gateway with gatewayId: " + gateway.getGatewayId() + ", already exists in ExperimentCatalog.");
            }
            // check if gatewayresourceprofile exists
            if (new GwyResourceProfileRepository().isGatewayResourceProfileExists(gateway.getGatewayId())) {
                throw new DuplicateEntryException("GatewayResourceProfile with gatewayId: " + gateway.getGatewayId() + ", already exists in AppCatalog.");
            }

            // add gateway in experimentCatalog
            String gatewayId = (String) experimentCatalog.add(ExpCatParentDataType.GATEWAY, gateway, gateway.getGatewayId());

            // add gatewayresourceprofile in appCatalog
            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(gatewayId);
            gatewayResourceProfile.setIdentityServerTenant(gatewayId);
            gatewayResourceProfile.setIdentityServerPwdCredToken(gateway.getIdentityServerPasswordToken());
            new GwyResourceProfileRepository().addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata added gateway with gateway id : " + gateway.getGatewayId());
            return gatewayId;
        } catch (RegistryException e) {
            logger.error("Error while adding gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding gateway. More info : " + e.getMessage());
            throw exception;
        } catch (AppCatalogException e) {
            logger.error("Error while adding gateway profile", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while adding gateway profile. More info : " + e.getMessage());
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
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
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


    /*This private method wraps the logic of getApplicationOutputs method as this method is called internally in the API.*/
    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, TException {
        try {
            List<OutputDataObjectType> applicationOutputs = applicationInterfaceRepository.getApplicationOutputs(appInterfaceId);
            logger.debug("Airavata retrieved application outputs for application interface id : " + appInterfaceId);
            return applicationOutputs;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application outputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    private String addJobSubmissionInterface(ComputeResourceRepository computeResourceRepository,
                                             String computeResourceId, String jobSubmissionInterfaceId,
                                             JobSubmissionProtocol protocolType, int priorityOrder)
            throws AppCatalogException {
        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        jobSubmissionInterface.setPriorityOrder(priorityOrder);
        jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
        return computeResourceRepository.addJobSubmissionProtocol(computeResourceId,jobSubmissionInterface);
    }

    private String addDataMovementInterface(ComputeResource computeResource,
                                            String computeResourceId, DMType dmType, String dataMovementInterfaceId,
                                            DataMovementProtocol protocolType, int priorityOrder)
            throws AppCatalogException {
        DataMovementInterface dataMovementInterface = new DataMovementInterface();
        dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
        dataMovementInterface.setPriorityOrder(priorityOrder);
        dataMovementInterface.setDataMovementProtocol(protocolType);
        if (dmType.equals(DMType.COMPUTE_RESOURCE)) {
            return computeResource.addDataMovementProtocol(computeResourceId, dmType, dataMovementInterface);
        }
        else if (dmType.equals(DMType.STORAGE_RESOURCE)) {
            dataMovementInterface.setStorageResourceId(computeResourceId);
            return storageResourceRepository.addDataMovementInterface(dataMovementInterface);
        }
        return null;
    }

    /**
     * Register a User Resource Profile.
     *
     * @param userResourceProfile User Resource Profile Object.
     *                               The GatewayID should be obtained from Airavata user profile data model and passed to register a corresponding
     *                               resource profile.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws RegistryServiceException, TException {
        try {
            if (!validateString(userResourceProfile.getUserId())){
                logger.error("Cannot create user resource profile with empty user id");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Cannot create user resource profile with empty gateway id");
                throw exception;
            }
            if (!validateString(userResourceProfile.getGatewayID())){
                logger.error("Cannot create user resource profile with empty gateway id");
                RegistryServiceException exception =  new RegistryServiceException();
                exception.setMessage("Cannot create user resource profile with empty gateway id");
                throw exception;
            }

            if (!ExpCatResourceUtils.isUserExist(userResourceProfile.getUserId(), userResourceProfile.getGatewayID())){
                logger.error("User does not exist.Please provide a valid user ID...");
                throw new RegistryServiceException("User does not exist.Please provide a valid user ID...");
            }
            String resourceProfile = userResourceProfileRepository.addUserResourceProfile(userResourceProfile);
            logger.debug("Airavata registered user resource profile with gateway id : " + userResourceProfile.getGatewayID() + "and user id : " + userResourceProfile.getUserId());
            return resourceProfile;
        } catch (AppCatalogException e) {
            logger.error("Error while registering user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering user resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error("Error while registering user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param userId The identifier for the requested user resource.
     * @return UserResourceProfile object
     *
     */
    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayId)){
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            UserResourceProfile userResourceProfile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayId);
            logger.debug("Airavata retrieved User resource profile with user id : " + userId);
            return userResourceProfile;
        } catch (AppCatalogException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a User Resource Profile.
     *
     * @param gatewayID              The identifier for the requested gateway resource to be updated.
     * @param userResourceProfile Gateway Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */

    @Override
    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("User does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while updating gateway resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating gateway resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete the given User Resource Profile.
     * @param userId identifier for user profile
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.removeUserResourceProfile(userId, gatewayID);
            logger.debug("Airavata deleted User profile with gateway id : " + gatewayID + " and user id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while removing User resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while removing User resource profile. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public String addUser(UserProfile userProfile) throws RegistryServiceException, DuplicateEntryException, TException {
        try {
            //FIXME: figure out a way to get password
            logger.info("Adding User in Registry: " + userProfile);
            if (isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                throw new DuplicateEntryException("User already exists, with userId: " +
                        userProfile.getUserId() + ", and gatewayId: " + userProfile.getGatewayId());
            }
            ExpCatResourceUtils.addUser(userProfile.getUserId(), null, userProfile.getGatewayId());
            return userProfile.getUserId();
        } catch (RegistryException ex) {
            logger.error("Error while adding user in registry: " + ex, ex);
            RegistryServiceException rse = new RegistryServiceException();
            rse.setMessage("Error while adding user in registry: " + ex.getMessage());
            throw rse;
        }
    }

    /**
     * Add a User Compute Resource Preference to a registered gateway profile.
     * @param userId
     * @param gatewayID                 The identifier for the gateway profile to be added.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param userComputeResourcePreference The UserComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference userComputeResourcePreference) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            if (! userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new RegistryServiceException("User resource profile with user id'"+userId+" &  gateway Id"+gatewayID+"' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            profile.addToUserComputeResourcePreferences(userComputeResourcePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added User compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering User resource profile preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering user resource profile preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID         The identifier of the gateway profile to be added.
     * @param storageResourceId Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserStoragePreference(String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            if (! userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)){
                throw new RegistryServiceException("User resource profile with user id'"+userId+" &  gateway Id"+gatewayID+"' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId,gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            profile.addToUserStoragePreferences(dataStoragePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering user resource profile preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while registering user resource profile preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Compute Resource Preference of a registered gateway profile.
     * @param userId
     * @param gatewayID         The identifier for the gateway profile to be requested
     * @param userComputeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(String userId, String gatewayID, String userComputeResourceId) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)){
                throw new RegistryServiceException("User resource profile with user id'"+userId+" &  gateway Id"+gatewayID+"' does not exist!!!");
            }
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!computeResourceRepository.isComputeResourceExists(userComputeResourceId)){
                logger.error(userComputeResourceId, "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                RegistryServiceException exception = new RegistryServiceException();
                exception.setMessage("Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw exception;
            }
            UserComputeResourcePreference userComputeResourcePreference = userResourceProfileRepository.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            logger.debug("Airavata retrieved user compute resource preference with gateway id : " + gatewayID + " and for compute resoruce id : " + userComputeResourceId );
            return userComputeResourcePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading user compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     * @param userId identifier for user data model
     * @param gatewayID         The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Storage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            if (! userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)){
                throw new RegistryServiceException("User resource profile with user id'"+userId+" &  gateway Id"+gatewayID+"' does not exist!!!");
            }

            UserStoragePreference storagePreference = userResourceProfileRepository.getUserStoragePreference(userId, gatewayID, storageId);
            logger.debug("Airavata retrieved user storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all User Resource Profiles registered
     *
     * @return UserResourceProfile
     * Returns all the UserResourceProfile list object.
     */
    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryServiceException, TException {
        try {
            return userResourceProfileRepository.getAllUserResourceProfiles();
        } catch (AppCatalogException e) {
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Compute Resource Preference to a registered user resource profile.
     * @param userId identifier for user data model
     * @param gatewayID                 The identifier for the gateway profile to be updated.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param userComputeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference userComputeResourcePreference) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId,gatewayID);
            List<UserComputeResourcePreference> userComputeResourcePreferences = profile.getUserComputeResourcePreferences();
            UserComputeResourcePreference preferenceToRemove = null;
            for (UserComputeResourcePreference preference : userComputeResourcePreferences) {
                if (preference.getComputeResourceId().equals(computeResourceId)){
                    preferenceToRemove=preference;
                    break;
                }
            }
            if (preferenceToRemove!=null) {
                profile.getUserComputeResourcePreferences().remove(
                        preferenceToRemove);
            }
            profile.getUserComputeResourcePreferences().add(userComputeResourcePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID + " and for compute resource id : " + computeResourceId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Storage Resource Preference of a registered user resource profile.
     * @param userId identifier for user data model
     * @param gatewayID         The identifier of the gateway profile to be updated.
     * @param storageId         The Storage resource identifier of the one that you want to update
     * @param userStoragePreference The storagePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserStoragePreference(String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId,gatewayID);
            List<UserStoragePreference> dataStoragePreferences = profile.getUserStoragePreferences();
            UserStoragePreference preferenceToRemove = null;
            for (UserStoragePreference preference : dataStoragePreferences) {
                if (preference.getStorageResourceId().equals(storageId)){
                    preferenceToRemove=preference;
                    break;
                }
            }
            if (preferenceToRemove!=null) {
                profile.getUserStoragePreferences().remove(
                        preferenceToRemove);
            }
            profile.getUserStoragePreferences().add(userStoragePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated user storage resource preference with gateway id : " + gatewayID + " and for storage resource id : " + storageId );
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading user data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete the Compute Resource Preference of a registered gateway profile.
     * @param userId The identifier for user data model
     * @param gatewayID         The identifier for the gateway profile to be deleted.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(userId, gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            logger.error(userId, "Error while reading user compute resource preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating user compute resource preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     * @param userId The identifier for user data model
     * @param gatewayID The identifier of the gateway profile to be deleted.
     * @param storageId ID of the storage preference you want to delete.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId) throws RegistryServiceException, TException {
        try {
            if (!ExpCatResourceUtils.isUserExist(userId, gatewayID)){
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryServiceException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileRepository.removeUserDataStoragePreferenceFromGateway(userId, gatewayID, storageId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading user data storage preference...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while updating user data storage preference. More info : " + e.getMessage());
            throw exception;
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving user resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * * Get queue statuses of all compute resources
     * *
     */
    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(ServerSettings.getDefaultUserGateway());
            List<Object> temp = experimentCatalog.get(ExperimentCatalogModelType.QUEUE_STATUS, null, null, -1, 0, null, null);
            List<QueueStatusModel> queueStatusModels = new ArrayList<>();
            temp.stream().forEach(t->{
                queueStatusModels.add((QueueStatusModel)t);
            });
            return queueStatusModels;
        } catch (RegistryException | ApplicationSettingsException e) {
            logger.error("Error while reading queue status models....", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading queue status models.... : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryServiceException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(ServerSettings.getDefaultUserGateway());
            experimentCatalog.add(ExpCatParentDataType.QUEUE_STATUS, queueStatuses, null);
        } catch (RegistryException | ApplicationSettingsException e) {
            logger.error("Error while storing queue status models....", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while storing queue status models.... : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all User Compute Resource Preferences of a registered User Resource Profile.
     *
     * @param userId
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isUserExists(gatewayID,userId)){
                logger.error("User Resource Profile does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("User Resource Profile does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileRepository.getUserResourceProfile(userId, gatewayID).getUserComputeResourcePreferences();
        } catch (AppCatalogException e) {
            logger.error(userId, "Error while reading User Resource Profile compute resource preferences...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading User Resource Profile compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Storage Resource Preferences of a registered User Resource Profile.
     *
     * @param userId
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID) throws RegistryServiceException, TException {
        try {
            if (!isUserExists(gatewayID,userId)){
                logger.error("User does not exist.Please provide a valid gateway id...");
                throw new RegistryServiceException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileRepository.getUserResourceProfile(userId, gatewayID).getUserStoragePreferences();
        } catch (AppCatalogException e) {
            logger.error(userId, "Error while reading user resource Profile data storage preferences...", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while reading user resource Profile data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }


}