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
package org.apache.airavata.thriftapi.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.thriftapi.exception.AiravataClientException;
import org.apache.airavata.thriftapi.exception.AiravataSystemException;
import org.apache.airavata.thriftapi.exception.DuplicateEntryException;
import org.apache.airavata.thriftapi.exception.ExperimentNotFoundException;
import org.apache.airavata.thriftapi.exception.InvalidRequestException;
import org.apache.airavata.thriftapi.exception.ProjectNotFoundException;
import org.apache.airavata.thriftapi.mapper.ApplicationDeploymentDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ApplicationInterfaceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ApplicationModuleMapper;
import org.apache.airavata.thriftapi.mapper.BatchQueueResourcePolicyMapper;
import org.apache.airavata.thriftapi.mapper.CloudJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.ComputationalResourceSchedulingModelMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourcePolicyMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.DataProductModelMapper;
import org.apache.airavata.thriftapi.mapper.DataReplicaLocationModelMapper;
import org.apache.airavata.thriftapi.mapper.ErrorModelMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentModelMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentStatisticsMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentStatusMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentSummaryModelMapper;
import org.apache.airavata.thriftapi.mapper.GatewayGroupsMapper;
import org.apache.airavata.thriftapi.mapper.GatewayMapper;
import org.apache.airavata.thriftapi.mapper.GatewayResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.GatewayUsageReportingCommandMapper;
import org.apache.airavata.thriftapi.mapper.GridFTPDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.GroupComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.GroupResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.InputDataObjectTypeMapper;
import org.apache.airavata.thriftapi.mapper.JobModelMapper;
import org.apache.airavata.thriftapi.mapper.LOCALDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.LOCALSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.NotificationMapper;
import org.apache.airavata.thriftapi.mapper.OutputDataObjectTypeMapper;
import org.apache.airavata.thriftapi.mapper.ParserInputMapper;
import org.apache.airavata.thriftapi.mapper.ParserMapper;
import org.apache.airavata.thriftapi.mapper.ParserOutputMapper;
import org.apache.airavata.thriftapi.mapper.ParsingTemplateMapper;
import org.apache.airavata.thriftapi.mapper.ProcessModelMapper;
import org.apache.airavata.thriftapi.mapper.ProcessStatusMapper;
import org.apache.airavata.thriftapi.mapper.ProcessWorkflowMapper;
import org.apache.airavata.thriftapi.mapper.ProjectMapper;
import org.apache.airavata.thriftapi.mapper.QueueStatusModelMapper;
import org.apache.airavata.thriftapi.mapper.ResourceJobManagerMapper;
import org.apache.airavata.thriftapi.mapper.SCPDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.SSHJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.StoragePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.StorageResourceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.TaskModelMapper;
import org.apache.airavata.thriftapi.mapper.TaskStatusMapper;
import org.apache.airavata.thriftapi.mapper.UnicoreDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.UnicoreJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.UserComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.UserConfigurationDataModelMapper;
import org.apache.airavata.thriftapi.mapper.UserProfileMapper;
import org.apache.airavata.thriftapi.mapper.UserResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.UserStoragePreferenceMapper;
import org.apache.airavata.thriftapi.model.ApplicationDeploymentDescription;
import org.apache.airavata.thriftapi.model.ApplicationInterfaceDescription;
import org.apache.airavata.thriftapi.model.ApplicationModule;
import org.apache.airavata.thriftapi.model.BatchQueueResourcePolicy;
import org.apache.airavata.thriftapi.model.CloudJobSubmission;
import org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.thriftapi.model.ComputeResourceDescription;
import org.apache.airavata.thriftapi.model.ComputeResourcePolicy;
import org.apache.airavata.thriftapi.model.ComputeResourcePreference;
import org.apache.airavata.thriftapi.model.DMType;
import org.apache.airavata.thriftapi.model.DataProductModel;
import org.apache.airavata.thriftapi.model.DataReplicaLocationModel;
import org.apache.airavata.thriftapi.model.ErrorModel;
import org.apache.airavata.thriftapi.model.ExperimentModel;
import org.apache.airavata.thriftapi.model.ExperimentSearchFields;
import org.apache.airavata.thriftapi.model.ExperimentStatistics;
import org.apache.airavata.thriftapi.model.ExperimentStatus;
import org.apache.airavata.thriftapi.model.ExperimentSummaryModel;
import org.apache.airavata.thriftapi.model.Gateway;
import org.apache.airavata.thriftapi.model.GatewayGroups;
import org.apache.airavata.thriftapi.model.GatewayResourceProfile;
import org.apache.airavata.thriftapi.model.GatewayUsageReportingCommand;
import org.apache.airavata.thriftapi.model.GridFTPDataMovement;
import org.apache.airavata.thriftapi.model.GroupComputeResourcePreference;
import org.apache.airavata.thriftapi.model.GroupResourceProfile;
import org.apache.airavata.thriftapi.model.InputDataObjectType;
import org.apache.airavata.thriftapi.model.JobModel;
import org.apache.airavata.thriftapi.model.LOCALDataMovement;
import org.apache.airavata.thriftapi.model.LOCALSubmission;
import org.apache.airavata.thriftapi.model.Notification;
import org.apache.airavata.thriftapi.model.OutputDataObjectType;
import org.apache.airavata.thriftapi.model.Parser;
import org.apache.airavata.thriftapi.model.ParserInput;
import org.apache.airavata.thriftapi.model.ParserOutput;
import org.apache.airavata.thriftapi.model.ParsingTemplate;
import org.apache.airavata.thriftapi.model.ProcessModel;
import org.apache.airavata.thriftapi.model.ProcessState;
import org.apache.airavata.thriftapi.model.ProcessStatus;
import org.apache.airavata.thriftapi.model.ProcessWorkflow;
import org.apache.airavata.thriftapi.model.Project;
import org.apache.airavata.thriftapi.model.ProjectSearchFields;
import org.apache.airavata.thriftapi.model.QueueStatusModel;
import org.apache.airavata.thriftapi.model.ResourceJobManager;
import org.apache.airavata.thriftapi.model.SCPDataMovement;
import org.apache.airavata.thriftapi.model.SSHJobSubmission;
import org.apache.airavata.thriftapi.model.StoragePreference;
import org.apache.airavata.thriftapi.model.StorageResourceDescription;
import org.apache.airavata.thriftapi.model.TaskModel;
import org.apache.airavata.thriftapi.model.TaskStatus;
import org.apache.airavata.thriftapi.model.UnicoreDataMovement;
import org.apache.airavata.thriftapi.model.UnicoreJobSubmission;
import org.apache.airavata.thriftapi.model.UserComputeResourcePreference;
import org.apache.airavata.thriftapi.model.UserConfigurationDataModel;
import org.apache.airavata.thriftapi.model.UserProfile;
import org.apache.airavata.thriftapi.model.UserResourceProfile;
import org.apache.airavata.thriftapi.model.UserStoragePreference;
import org.apache.airavata.thriftapi.registry.exception.RegistryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegistryServiceHandler implements org.apache.airavata.thriftapi.registry.model.RegistryService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceHandler.class);

    private final RegistryService registryService;
    private final GatewayMapper gatewayMapper = GatewayMapper.INSTANCE;
    private final ParserMapper parserMapper = ParserMapper.INSTANCE;
    private final ParsingTemplateMapper parsingTemplateMapper = ParsingTemplateMapper.INSTANCE;
    private final ParserInputMapper parserInputMapper = ParserInputMapper.INSTANCE;
    private final ParserOutputMapper parserOutputMapper = ParserOutputMapper.INSTANCE;
    private final GatewayGroupsMapper gatewayGroupsMapper = GatewayGroupsMapper.INSTANCE;
    private final GroupResourceProfileMapper groupResourceProfileMapper = GroupResourceProfileMapper.INSTANCE;
    private final GroupComputeResourcePreferenceMapper groupComputeResourcePreferenceMapper =
            GroupComputeResourcePreferenceMapper.INSTANCE;
    private final ComputeResourcePolicyMapper computeResourcePolicyMapper = ComputeResourcePolicyMapper.INSTANCE;
    private final BatchQueueResourcePolicyMapper batchQueueResourcePolicyMapper =
            BatchQueueResourcePolicyMapper.INSTANCE;
    private final GatewayUsageReportingCommandMapper gatewayUsageReportingCommandMapper =
            GatewayUsageReportingCommandMapper.INSTANCE;
    private final QueueStatusModelMapper queueStatusModelMapper = QueueStatusModelMapper.INSTANCE;
    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;
    private final ProjectMapper projectMapper = ProjectMapper.INSTANCE;
    private final ExperimentModelMapper experimentModelMapper = ExperimentModelMapper.INSTANCE;
    private final ExperimentSummaryModelMapper experimentSummaryModelMapper = ExperimentSummaryModelMapper.INSTANCE;
    private final ExperimentStatisticsMapper experimentStatisticsMapper = ExperimentStatisticsMapper.INSTANCE;
    private final ExperimentStatusMapper experimentStatusMapper = ExperimentStatusMapper.INSTANCE;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper = OutputDataObjectTypeMapper.INSTANCE;
    private final ErrorModelMapper errorModelMapper = ErrorModelMapper.INSTANCE;
    private final TaskStatusMapper taskStatusMapper = TaskStatusMapper.INSTANCE;
    private final ProcessStatusMapper processStatusMapper = ProcessStatusMapper.INSTANCE;
    private final JobModelMapper jobModelMapper = JobModelMapper.INSTANCE;
    private final ProcessModelMapper processModelMapper = ProcessModelMapper.INSTANCE;
    private final TaskModelMapper taskModelMapper = TaskModelMapper.INSTANCE;
    private final ProcessWorkflowMapper processWorkflowMapper = ProcessWorkflowMapper.INSTANCE;
    private final UserConfigurationDataModelMapper userConfigurationDataModelMapper =
            UserConfigurationDataModelMapper.INSTANCE;
    private final ApplicationModuleMapper applicationModuleMapper = ApplicationModuleMapper.INSTANCE;
    private final ApplicationDeploymentDescriptionMapper applicationDeploymentDescriptionMapper =
            ApplicationDeploymentDescriptionMapper.INSTANCE;
    private final ApplicationInterfaceDescriptionMapper applicationInterfaceDescriptionMapper =
            ApplicationInterfaceDescriptionMapper.INSTANCE;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper = InputDataObjectTypeMapper.INSTANCE;
    private final ComputeResourceDescriptionMapper computeResourceDescriptionMapper =
            ComputeResourceDescriptionMapper.INSTANCE;
    private final StorageResourceDescriptionMapper storageResourceDescriptionMapper =
            StorageResourceDescriptionMapper.INSTANCE;
    private final LOCALSubmissionMapper localSubmissionMapper = LOCALSubmissionMapper.INSTANCE;
    private final SSHJobSubmissionMapper sshJobSubmissionMapper = SSHJobSubmissionMapper.INSTANCE;
    private final UnicoreJobSubmissionMapper unicoreJobSubmissionMapper = UnicoreJobSubmissionMapper.INSTANCE;
    private final CloudJobSubmissionMapper cloudJobSubmissionMapper = CloudJobSubmissionMapper.INSTANCE;
    private final LOCALDataMovementMapper localDataMovementMapper = LOCALDataMovementMapper.INSTANCE;
    private final SCPDataMovementMapper scpDataMovementMapper = SCPDataMovementMapper.INSTANCE;
    private final UnicoreDataMovementMapper unicoreDataMovementMapper = UnicoreDataMovementMapper.INSTANCE;
    private final GridFTPDataMovementMapper gridFTPDataMovementMapper = GridFTPDataMovementMapper.INSTANCE;
    private final ResourceJobManagerMapper resourceJobManagerMapper = ResourceJobManagerMapper.INSTANCE;
    private final GatewayResourceProfileMapper gatewayResourceProfileMapper = GatewayResourceProfileMapper.INSTANCE;
    private final ComputeResourcePreferenceMapper computeResourcePreferenceMapper =
            ComputeResourcePreferenceMapper.INSTANCE;
    private final StoragePreferenceMapper storagePreferenceMapper = StoragePreferenceMapper.INSTANCE;
    private final DataProductModelMapper dataProductModelMapper = DataProductModelMapper.INSTANCE;
    private final DataReplicaLocationModelMapper dataReplicaLocationModelMapper =
            DataReplicaLocationModelMapper.INSTANCE;
    private final ComputationalResourceSchedulingModelMapper computationalResourceSchedulingModelMapper =
            ComputationalResourceSchedulingModelMapper.INSTANCE;
    private final UserResourceProfileMapper userResourceProfileMapper = UserResourceProfileMapper.INSTANCE;
    private final UserProfileMapper userProfileMapper = UserProfileMapper.INSTANCE;
    private final UserComputeResourcePreferenceMapper userComputeResourcePreferenceMapper =
            UserComputeResourcePreferenceMapper.INSTANCE;
    private final UserStoragePreferenceMapper userStoragePreferenceMapper = UserStoragePreferenceMapper.INSTANCE;

    public RegistryServiceHandler(RegistryService registryService) {
        this.registryService = registryService;
    }

    // Helper method to convert domain exceptions to Thrift exceptions
    private RegistryServiceException convertToRegistryServiceException(Throwable e, String context) {
        logger.error(context, e);
        RegistryServiceException exception = new RegistryServiceException();
        exception.setMessage(context + ". More info : " + e.getMessage());
        return exception;
    }

    private ProjectNotFoundException convertToThriftProjectNotFoundException(
            org.apache.airavata.common.exception.CatalogExceptions.ProjectNotFoundException e) {
        ProjectNotFoundException exception = new ProjectNotFoundException();
        exception.setMessage(e.getMessage());
        return exception;
    }

    /**
     * Fetch Apache Registry API version
     */
    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return org.apache.airavata.thriftapi.registry.model.registry_apiConstants.REGISTRY_API_VERSION;
    }

    /**
     * Verify if User Exists within Airavata.
     *
     * @param gatewayId
     * @param userName
     * @return true/false
     */
    @Override
    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException {
        try {
            return registryService.isUserExists(gatewayId, userName);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if user exists");
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
    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException {
        try {
            return registryService.getAllUsersInGateway(gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all users in gateway");
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
    public Gateway getGateway(String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Gateway domainGateway = registryService.getGateway(gatewayId);
            return gatewayMapper.toThrift(domainGateway);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway");
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
    public boolean deleteGateway(String gatewayId) throws RegistryServiceException {
        try {
            return registryService.deleteGateway(gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete gateway");
        }
    }

    /**
     * Get All the Gateways Connected to Airavata.
     */
    @Override
    public List<Gateway> getAllGateways() throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.Gateway> domainGateways = registryService.getAllGateways();
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all gateways");
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
    public boolean isGatewayExist(String gatewayId) throws RegistryServiceException {
        try {
            return registryService.isGatewayExist(gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if gateway exists");
        }
    }

    @Override
    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        try {
            return registryService.deleteNotification(gatewayId, notificationId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete notification");
        }
    }

    @Override
    public Notification getNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Notification domainNotification =
                    registryService.getNotification(gatewayId, notificationId);
            return notificationMapper.toThrift(domainNotification);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get notification");
        }
    }

    @Override
    public List<Notification> getAllNotifications(String gatewayId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.Notification> domainNotifications =
                    registryService.getAllNotifications(gatewayId);
            return domainNotifications.stream()
                    .map(notificationMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all notifications");
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
    public Project getProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        try {
            org.apache.airavata.common.model.Project domainProject = registryService.getProject(projectId);
            return projectMapper.toThrift(domainProject);
        } catch (org.apache.airavata.common.exception.CatalogExceptions.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get project");
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
    public boolean deleteProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        try {
            return registryService.deleteProject(projectId);
        } catch (org.apache.airavata.common.exception.CatalogExceptions.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete project");
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
    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.Project> domainProjects =
                    registryService.getUserProjects(gatewayId, userName, limit, offset);
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user projects");
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
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentStatistics domainExperimentStatistics =
                    registryService.getExperimentStatistics(
                            gatewayId,
                            fromTime,
                            toTime,
                            userName,
                            applicationName,
                            resourceHostName,
                            accessibleExpIds,
                            limit,
                            offset);
            return experimentStatisticsMapper.toThrift(domainExperimentStatistics);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get experiment statistics");
        }
    }

    /**
     * Get All Experiments of the Project
     * Get Experiments within project with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Unique identifier of the gateway.
     * @param projectId Unique identifier of the project.
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ExperimentModel> domainExperiments =
                    registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get experiments in project");
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
    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ExperimentModel> domainExperiments =
                    registryService.getUserExperiments(gatewayId, userName, limit, offset);
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user experiments");
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
    public boolean deleteExperiment(String experimentId) throws RegistryServiceException {
        try {
            return registryService.deleteExperiment(experimentId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete experiment");
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
     * * @throws org.apache.airavata.common.exception.CoreExceptions.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.common.exception.CatalogExceptions.ExperimentNotFoundException
     * *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * *
     * * @throws org.apache.airavata.common.exception.CoreExceptions.AiravataClientException
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
     * * @throws org.apache.airavata.common.exception.CoreExceptions.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param airavataExperimentId
     */
    @Override
    public ExperimentModel getExperiment(String airavataExperimentId)
            throws RegistryServiceException, ExperimentNotFoundException {
        org.apache.airavata.common.model.ExperimentModel domainExperimentModel =
                getExperimentInternal(airavataExperimentId);
        return experimentModelMapper.toThrift(domainExperimentModel);
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
    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentModel domainExperimentModel =
                    registryService.getDetailedExperimentTree(airavataExperimentId);
            return experimentModelMapper.toThrift(domainExperimentModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get detailed experiment tree");
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
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentStatus domainExperimentStatus =
                    registryService.getExperimentStatus(airavataExperimentId);
            return experimentStatusMapper.toThrift(domainExperimentStatus);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get experiment status");
        }
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
    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    registryService.getExperimentOutputs(airavataExperimentId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get experiment outputs");
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
    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    registryService.getIntermediateOutputs(airavataExperimentId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get intermediate outputs");
        }
    }

    /**
     * Get Job Statuses for an Experiment
     * This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup
     *
     * @param airavataExperimentId@return JobStatus
     *                                    Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map
     */
    @Override
    public Map<String, org.apache.airavata.thriftapi.model.JobStatus> getJobStatuses(String airavataExperimentId)
            throws RegistryServiceException {
        try {
            var domainJobStatuses = registryService.getJobStatuses(airavataExperimentId);
            var thriftStatuses = new HashMap<String, org.apache.airavata.thriftapi.model.JobStatus>();
            for (var entry : domainJobStatuses.entrySet()) {
                var thriftStatus = new org.apache.airavata.thriftapi.model.JobStatus();
                thriftStatus.setJobState(org.apache.airavata.thriftapi.model.JobState.valueOf(
                        entry.getValue().getJobState().name()));
                thriftStatus.setTimeOfStateChange(entry.getValue().getTimeOfStateChange());
                thriftStatus.setReason(entry.getValue().getReason());
                thriftStatus.setStatusId(entry.getValue().getStatusId());
                thriftStatuses.put(entry.getKey(), thriftStatus);
            }
            return thriftStatuses;
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get job statuses");
        }
    }

    @Override
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    outputs.stream().map(outputDataObjectTypeMapper::toDomain).collect(Collectors.toList());
            registryService.addExperimentProcessOutputs(outputType, domainOutputs, id);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add experiment process outputs");
        }
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ErrorModel domainErrorModel = errorModelMapper.toDomain(errorModel);
            registryService.addErrors(errorType, domainErrorModel, id);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add errors");
        }
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.TaskStatus domainTaskStatus = taskStatusMapper.toDomain(taskStatus);
            registryService.addTaskStatus(domainTaskStatus, taskId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add task status");
        }
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessStatus domainProcessStatus =
                    processStatusMapper.toDomain(processStatus);
            registryService.addProcessStatus(domainProcessStatus, processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add process status");
        }
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessStatus domainProcessStatus =
                    processStatusMapper.toDomain(processStatus);
            registryService.updateProcessStatus(domainProcessStatus, processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update process status");
        }
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentStatus domainExperimentStatus =
                    experimentStatusMapper.toDomain(experimentStatus);
            registryService.updateExperimentStatus(domainExperimentStatus, experimentId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update experiment status");
        }
    }

    @Override
    public void addJobStatus(org.apache.airavata.thriftapi.model.JobStatus jobStatus, String taskId, String jobId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.JobStatus domainJobStatus =
                    new org.apache.airavata.common.model.JobStatus();
            domainJobStatus.setJobState(org.apache.airavata.common.model.JobState.valueOf(
                    jobStatus.getJobState().name()));
            domainJobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            domainJobStatus.setReason(jobStatus.getReason());
            domainJobStatus.setStatusId(jobStatus.getStatusId());
            registryService.addJobStatus(domainJobStatus, taskId, jobId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add job status");
        }
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.JobModel domainJobModel = jobModelMapper.toDomain(jobModel);
            registryService.addJob(domainJobModel, processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add job");
        }
    }

    @Override
    public void deleteJobs(String processId) throws RegistryServiceException {
        try {
            registryService.deleteJobs(processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete jobs");
        }
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessModel domainProcessModel =
                    processModelMapper.toDomain(processModel);
            return registryService.addProcess(domainProcessModel, experimentId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add process");
        }
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessModel domainProcessModel =
                    processModelMapper.toDomain(processModel);
            registryService.updateProcess(domainProcessModel, processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update process");
        }
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.TaskModel domainTaskModel = taskModelMapper.toDomain(taskModel);
            return registryService.addTask(domainTaskModel, processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add task");
        }
    }

    @Override
    public void deleteTasks(String processId) throws RegistryServiceException {
        try {
            registryService.deleteTasks(processId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete tasks");
        }
    }

    @Override
    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserConfigurationDataModel domainUserConfigurationDataModel =
                    registryService.getUserConfigurationData(experimentId);
            return userConfigurationDataModelMapper.toThrift(domainUserConfigurationDataModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user configuration data");
        }
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessModel domainProcessModel = registryService.getProcess(processId);
            return processModelMapper.toThrift(domainProcessModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process");
        }
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ProcessModel> domainProcessModels =
                    registryService.getProcessList(experimentId);
            return domainProcessModels.stream()
                    .map(processModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process list");
        }
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessStatus domainProcessStatus =
                    registryService.getProcessStatus(processId);
            return processStatusMapper.toThrift(domainProcessStatus);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process status");
        }
    }

    @Override
    public List<ProcessModel> getProcessListInState(ProcessState processState) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessState domainProcessState =
                    org.apache.airavata.common.model.ProcessState.valueOf(processState.name());
            List<org.apache.airavata.common.model.ProcessModel> domainProcessModels =
                    registryService.getProcessListInState(domainProcessState);
            return domainProcessModels.stream()
                    .map(processModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process list in state");
        }
    }

    @Override
    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ProcessStatus> domainProcessStatuses =
                    registryService.getProcessStatusList(processId);
            return domainProcessStatuses.stream()
                    .map(processStatusMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process status list");
        }
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public boolean isJobExist(String queryType, String id) throws RegistryServiceException {
        try {
            return registryService.isJobExist(queryType, id);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if job exists");
        }
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public JobModel getJob(String queryType, String id) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.JobModel domainJobModel = registryService.getJob(queryType, id);
            return jobModelMapper.toThrift(domainJobModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get job");
        }
    }

    @Override
    public List<JobModel> getJobs(String queryType, String id) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.JobModel> domainJobModels = registryService.getJobs(queryType, id);
            return domainJobModels.stream().map(jobModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get jobs");
        }
    }

    @Override
    public int getJobCount(
            org.apache.airavata.thriftapi.model.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.JobStatus domainJobStatus =
                    new org.apache.airavata.common.model.JobStatus();
            domainJobStatus.setJobState(org.apache.airavata.common.model.JobState.valueOf(
                    jobStatus.getJobState().name()));
            domainJobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            domainJobStatus.setReason(jobStatus.getReason());
            domainJobStatus.setStatusId(jobStatus.getStatusId());
            return registryService.getJobCount(domainJobStatus, gatewayId, searchBackTimeInMinutes);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get job count");
        }
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        try {
            return registryService.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get average time distribution");
        }
    }

    @Override
    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    registryService.getProcessOutputs(processId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process outputs");
        }
    }

    @Override
    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ProcessWorkflow> domainProcessWorkflows =
                    registryService.getProcessWorkflows(processId);
            return domainProcessWorkflows.stream()
                    .map(processWorkflowMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process workflows");
        }
    }

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ProcessWorkflow domainProcessWorkflow =
                    processWorkflowMapper.toDomain(processWorkflow);
            registryService.addProcessWorkflow(domainProcessWorkflow);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add process workflow");
        }
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws RegistryServiceException {
        try {
            return registryService.getProcessIds(experimentId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get process ids");
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
    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.JobModel> domainJobModels =
                    registryService.getJobDetails(airavataExperimentId);
            return domainJobModels.stream().map(jobModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get job details");
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
    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationModule domainApplicationModule =
                    registryService.getApplicationModule(appModuleId);
            return applicationModuleMapper.toThrift(domainApplicationModule);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application module");
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
    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationModule> domainApplicationModules =
                    registryService.getAllAppModules(gatewayId);
            return domainApplicationModules.stream()
                    .map(applicationModuleMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all app modules");
        }
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId        ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppIds App IDs that are accessible to the user
     * @return list
     * Returns the list of all Application Module Objects that are accessible to the user.
     */
    @Override
    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationModule> domainApplicationModules =
                    registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
            return domainApplicationModules.stream()
                    .map(applicationModuleMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get accessible app modules");
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
    public boolean deleteApplicationModule(String appModuleId) throws RegistryServiceException {
        try {
            return registryService.deleteApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete application module");
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
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainApplicationDeploymentDescription =
                    registryService.getApplicationDeployment(appDeploymentId);
            return applicationDeploymentDescriptionMapper.toThrift(domainApplicationDeploymentDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application deployment");
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
    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryServiceException {
        try {
            return registryService.deleteApplicationDeployment(appDeploymentId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete application deployment");
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
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationDeploymentDescription>
                    domainApplicationDeploymentDescriptions = registryService.getAllApplicationDeployments(gatewayId);
            return domainApplicationDeploymentDescriptions.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all application deployments");
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @param gatewayId                  ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppDeploymentIds App IDs that are accessible to the user
     * @return list<applicationDeployment.
     * Returns the list of all application Deployment Objects that are accessible to the user.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationDeploymentDescription>
                    domainApplicationDeploymentDescriptions = registryService.getAccessibleApplicationDeployments(
                            gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            return domainApplicationDeploymentDescriptions.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get accessible application deployments");
        }
    }

    /**
     * Fetch all accessible Application Deployment Descriptions for the given Application Module.
     *
     * @param gatewayId                    ID of the gateway which need to list all available application deployment documentation.
     * @param appModuleId                  The given Application Module ID.
     * @param accessibleAppDeploymentIds   Application Deployment IDs which are accessible to the current user.
     * @param accessibleComputeResourceIds Compute Resource IDs which are accessible to the current user.
     * @return list<applicationDeployment>
     * Returns the list of all application Deployment Objects.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationDeploymentDescription>
                    domainApplicationDeploymentDescriptions =
                            registryService.getAccessibleApplicationDeploymentsForAppModule(
                                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            return domainApplicationDeploymentDescriptions.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Failed to get accessible application deployments for app module");
        }
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationDeploymentDescription>
                    domainApplicationDeploymentDescriptions = registryService.getApplicationDeployments(appModuleId);
            return domainApplicationDeploymentDescriptions.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application deployments");
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
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainApplicationInterfaceDescription =
                    registryService.getApplicationInterface(appInterfaceId);
            return applicationInterfaceDescriptionMapper.toThrift(domainApplicationInterfaceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application interface");
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
    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryServiceException {
        try {
            return registryService.deleteApplicationInterface(appInterfaceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete application interface");
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
    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws RegistryServiceException {
        try {
            return registryService.getAllApplicationInterfaceNames(gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all application interface names");
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
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ApplicationInterfaceDescription>
                    domainApplicationInterfaceDescriptions = registryService.getAllApplicationInterfaces(gatewayId);
            return domainApplicationInterfaceDescriptions.stream()
                    .map(applicationInterfaceDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all application interfaces");
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
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.InputDataObjectType> domainInputs =
                    registryService.getApplicationInputs(appInterfaceId);
            return domainInputs.stream()
                    .map(inputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application inputs");
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
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    registryService.getApplicationOutputs(appInterfaceId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get application outputs");
        }
    }

    /**
     * Fetch a list of all deployed Compute Hosts for a given application interfaces.
     *
     * @param appInterfaceId The identifier for the requested application interface.
     *
     * /**
     * Fetch the given Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource
     * @return computeResourceDescription
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResourceDescription =
                    registryService.getComputeResource(computeResourceId);
            return computeResourceDescriptionMapper.toThrift(domainComputeResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get compute resource");
        }
    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException {
        try {
            return registryService.getAllComputeResourceNames();
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all compute resource names");
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
    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException {
        try {
            return registryService.deleteComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete compute resource");
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
    public StorageResourceDescription getStorageResource(String storageResourceId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StorageResourceDescription domainStorageResourceDescription =
                    registryService.getStorageResource(storageResourceId);
            return storageResourceDescriptionMapper.toThrift(domainStorageResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get storage resource");
        }
    }

    /**
     * Fetch all registered Storage Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException {
        try {
            return registryService.getAllStorageResourceNames();
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all storage resource names");
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
    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException {
        try {
            return registryService.deleteStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete storage resource");
        }
    }

    /**
     * This method returns localJobSubmission object
     *
     * @param jobSubmissionId@return LOCALSubmission instance
     */
    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.LOCALSubmission domainLocalSubmission =
                    registryService.getLocalJobSubmission(jobSubmissionId);
            return localSubmissionMapper.toThrift(domainLocalSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get local job submission");
        }
    }

    /**
     * This method returns SSHJobSubmission object
     *
     * @param jobSubmissionId@return SSHJobSubmission instance
     */
    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SSHJobSubmission domainSSHJobSubmission =
                    registryService.getSSHJobSubmission(jobSubmissionId);
            return sshJobSubmissionMapper.toThrift(domainSSHJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get SSH job submission");
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
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicoreJobSubmission =
                    registryService.getUnicoreJobSubmission(jobSubmissionId);
            return unicoreJobSubmissionMapper.toThrift(domainUnicoreJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get Unicore job submission");
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
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.CloudJobSubmission domainCloudJobSubmission =
                    registryService.getCloudJobSubmission(jobSubmissionId);
            return cloudJobSubmissionMapper.toThrift(domainCloudJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get cloud job submission");
        }
    }

    /**
     * This method returns local datamovement object.
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return LOCALDataMovement instance
     */
    @Override
    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.LOCALDataMovement domainLocalDataMovement =
                    registryService.getLocalDataMovement(dataMovementId);
            return localDataMovementMapper.toThrift(domainLocalDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get local data movement");
        }
    }

    /**
     * This method returns SCP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return SCPDataMovement instance
     */
    @Override
    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SCPDataMovement domainSCPDataMovement =
                    registryService.getSCPDataMovement(dataMovementId);
            return scpDataMovementMapper.toThrift(domainSCPDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get SCP data movement");
        }
    }

    /**
     * This method returns UNICORE datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return UnicoreDataMovement instance
     */
    @Override
    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicoreDataMovement =
                    registryService.getUnicoreDataMovement(dataMovementId);
            return unicoreDataMovementMapper.toThrift(domainUnicoreDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get Unicore data movement");
        }
    }

    /**
     * This method returns GridFTP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return GridFTPDataMovement instance
     */
    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTPDataMovement =
                    registryService.getGridFTPDataMovement(dataMovementId);
            return gridFTPDataMovementMapper.toThrift(domainGridFTPDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get GridFTP data movement");
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
    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder)
            throws RegistryServiceException {
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
    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder)
            throws RegistryServiceException {
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
    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap)
            throws RegistryServiceException {
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
    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap)
            throws RegistryServiceException {
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
    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws RegistryServiceException {
        try {
            return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete job submission interface");
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainResourceJobManager =
                    registryService.getResourceJobManager(resourceJobManagerId);
            return resourceJobManagerMapper.toThrift(domainResourceJobManager);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get resource job manager");
        }
    }

    @Override
    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        try {
            return registryService.deleteResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete resource job manager");
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
    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws RegistryServiceException {
        try {
            return registryService.deleteBatchQueue(computeResourceId, queueName);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete batch queue");
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
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayResourceProfile domainGatewayResourceProfile =
                    registryService.getGatewayResourceProfile(gatewayID);
            return gatewayResourceProfileMapper.toThrift(domainGatewayResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway resource profile");
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
    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        try {
            return registryService.deleteGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete gateway resource profile");
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
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourcePreference domainComputeResourcePreference =
                    registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return computeResourcePreferenceMapper.toThrift(domainComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway compute resource preference");
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Stprage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StoragePreference domainStoragePreference =
                    registryService.getGatewayStoragePreference(gatewayID, storageId);
            return storagePreferenceMapper.toThrift(domainStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway storage preference");
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
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ComputeResourcePreference> domainComputeResourcePreferences =
                    registryService.getAllGatewayComputeResourcePreferences(gatewayID);
            return domainComputeResourcePreferences.stream()
                    .map(computeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all gateway compute resource preferences");
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
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.StoragePreference> domainStoragePreferences =
                    registryService.getAllGatewayStoragePreferences(gatewayID);
            return domainStoragePreferences.stream()
                    .map(storagePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all gateway storage preferences");
        }
    }

    /**
     * Fetch all Gateway Profiles registered
     *
     * @return GatewayResourceProfile
     * Returns all the GatewayResourcePrifle list object.
     */
    @Override
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.GatewayResourceProfile> domainGatewayResourceProfiles =
                    registryService.getAllGatewayResourceProfiles();
            return domainGatewayResourceProfiles.stream()
                    .map(gatewayResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all gateway resource profiles");
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
    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete gateway compute resource preference");
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
    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws RegistryServiceException {
        try {
            return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete gateway storage preference");
        }
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DataProductModel domainDataProductModel =
                    registryService.getDataProduct(productUri);
            return dataProductModelMapper.toThrift(domainDataProductModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get data product");
        }
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DataProductModel domainDataProductModel =
                    registryService.getParentDataProduct(productUri);
            return dataProductModelMapper.toThrift(domainDataProductModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parent data product");
        }
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.DataProductModel> domainDataProductModels =
                    registryService.getChildDataProducts(productUri);
            return domainDataProductModels.stream()
                    .map(dataProductModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Failed to get child data products for productUri=" + productUri);
        }
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.DataProductModel> domainDataProductModels =
                    registryService.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return domainDataProductModels.stream()
                    .map(dataProductModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to search data products by name");
        }
    }

    @Override
    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GroupResourceProfile domainGroupResourceProfile =
                    groupResourceProfileMapper.toDomain(groupResourceProfile);
            return registryService.createGroupResourceProfile(domainGroupResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to create group resource profile");
        }
    }

    @Override
    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GroupResourceProfile domainGroupResourceProfile =
                    groupResourceProfileMapper.toDomain(groupResourceProfile);
            registryService.updateGroupResourceProfile(domainGroupResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update group resource profile");
        }
    }

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GroupResourceProfile domainGroupResourceProfile =
                    registryService.getGroupResourceProfile(groupResourceProfileId);
            return groupResourceProfileMapper.toThrift(domainGroupResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group resource profile");
        }
    }

    @Override
    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws RegistryServiceException {
        try {
            return registryService.isGroupResourceProfileExists(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if group resource profile exists");
        }
    }

    @Override
    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        try {
            return registryService.removeGroupResourceProfile(groupResourceProfileId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove group resource profile");
        }
    }

    @Override
    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.GroupResourceProfile> domainGroupResourceProfiles =
                    registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
            return domainGroupResourceProfiles.stream()
                    .map(groupResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group resource list");
        }
    }

    @Override
    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        try {
            return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove group compute prefs");
        }
    }

    @Override
    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        try {
            return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove group compute resource policy");
        }
    }

    @Override
    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        try {
            return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove group batch queue resource policy");
        }
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GroupComputeResourcePreference domainGroupComputeResourcePreference =
                    registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            return groupComputeResourcePreferenceMapper.toThrift(domainGroupComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group compute resource preference");
        }
    }

    @Override
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        try {
            return registryService.isGroupComputeResourcePreferenceExists(computeResourceId, groupResourceProfileId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if group compute resource preference exists");
        }
    }

    @Override
    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourcePolicy domainComputeResourcePolicy =
                    registryService.getGroupComputeResourcePolicy(resourcePolicyId);
            return computeResourcePolicyMapper.toThrift(domainComputeResourcePolicy);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group compute resource policy");
        }
    }

    @Override
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.BatchQueueResourcePolicy domainBatchQueueResourcePolicy =
                    registryService.getBatchQueueResourcePolicy(resourcePolicyId);
            return batchQueueResourcePolicyMapper.toThrift(domainBatchQueueResourcePolicy);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get batch queue resource policy");
        }
    }

    @Override
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.GroupComputeResourcePreference>
                    domainGroupComputeResourcePreferences =
                            registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
            return domainGroupComputeResourcePreferences.stream()
                    .map(groupComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group compute resource pref list");
        }
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.BatchQueueResourcePolicy> domainBatchQueueResourcePolicies =
                    registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
            return domainBatchQueueResourcePolicies.stream()
                    .map(batchQueueResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group batch queue resource policy list");
        }
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ComputeResourcePolicy> domainComputeResourcePolicies =
                    registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
            return domainComputeResourcePolicies.stream()
                    .map(computeResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get group compute resource policy list");
        }
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DataReplicaLocationModel domainReplicaLocationModel =
                    dataReplicaLocationModelMapper.toDomain(replicaLocationModel);
            return registryService.registerReplicaLocation(domainReplicaLocationModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Error in retreiving the replica " + replicaLocationModel.getReplicaName());
        }
    }

    /**
     * API Methods related to replica catalog
     *
     * @param dataProductModel
     */
    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DataProductModel domainDataProductModel =
                    dataProductModelMapper.toDomain(dataProductModel);
            return registryService.registerDataProduct(domainDataProductModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Error in registering the data resource" + dataProductModel.getProductName());
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
    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StoragePreference domainStoragePreference =
                    storagePreferenceMapper.toDomain(storagePreference);
            return registryService.updateGatewayStoragePreference(gatewayID, storageId, domainStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update gateway storage preference");
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
    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourcePreference domainComputeResourcePreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            return registryService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, domainComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update gateway compute resource preference");
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID             The identifier of the gateway profile to be added.
     * @param storageResourceId     Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StoragePreference domainStoragePreference =
                    storagePreferenceMapper.toDomain(dataStoragePreference);
            return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, domainStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add gateway storage preference");
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
    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourcePreference domainComputeResourcePreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            return registryService.addGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, domainComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add gateway compute resource preference");
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
    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayResourceProfile domainGatewayResourceProfile =
                    gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            return registryService.updateGatewayResourceProfile(gatewayID, domainGatewayResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update gateway resource profile");
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
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayResourceProfile domainGatewayResourceProfile =
                    gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            return registryService.registerGatewayResourceProfile(domainGatewayResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register gateway resource profile");
        }
    }

    @Override
    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainResourceJobManager =
                    resourceJobManagerMapper.toDomain(updatedResourceJobManager);
            return registryService.updateResourceJobManager(resourceJobManagerId, domainResourceJobManager);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update resource job manager");
        }
    }

    @Override
    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainResourceJobManager =
                    resourceJobManagerMapper.toDomain(resourceJobManager);
            return registryService.registerResourceJobManager(domainResourceJobManager);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register resource job manager");
        }
    }

    /**
     * Delete a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @param dmType
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, domainDMType);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete data movement interface");
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
    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws RegistryServiceException {
        throw new RegistryServiceException("updateGridFTPDataMovementDetails is not yet implemented");
    }

    /**
     * Add a GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri          The identifier of the compute resource to which dataMovement protocol to be added
     *
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param gridFTPDataMovement The GridFTPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTPDataMovement =
                    gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            return registryService.addGridFTPDataMovementDetails(
                    computeResourceId, domainDMType, priorityOrder, domainGridFTPDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add GridFTP data movement details");
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
    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws RegistryServiceException {
        throw new RegistryServiceException("updateUnicoreDataMovementDetails is not yet implemented");
    }

    /**
     * Add a UNICORE data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri          The identifier of the compute resource to which data movement protocol to be added
     *
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param unicoreDataMovement
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicoreDataMovement =
                    unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            return registryService.addUnicoreDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainUnicoreDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add Unicore data movement details");
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
    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SCPDataMovement domainSCPDataMovement =
                    scpDataMovementMapper.toDomain(scpDataMovement);
            return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, domainSCPDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update SCP data movement details");
        }
    }

    /**
     * Add a SCP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri      The identifier of the compute resource to which JobSubmission protocol to be added
     *
     * @param dmType
     * @param priorityOrder   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param scpDataMovement The SCPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            org.apache.airavata.common.model.SCPDataMovement domainSCPDataMovement =
                    scpDataMovementMapper.toDomain(scpDataMovement);
            return registryService.addSCPDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainSCPDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add SCP data movement details");
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
    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.LOCALDataMovement domainLocalDataMovement =
                    localDataMovementMapper.toDomain(localDataMovement);
            return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, domainLocalDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update local data movement details");
        }
    }

    /**
     * Add a Local data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri        The identifier of the compute resource to which JobSubmission protocol to be added
     *
     * @param dataMoveType
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localDataMovement The LOCALDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.LOCALDataMovement domainLocalDataMovement =
                    localDataMovementMapper.toDomain(localDataMovement);
            return registryService.addLocalDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainLocalDataMovement);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add local data movement details");
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
    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException {
        throw new RegistryServiceException("updateUnicoreJobSubmissionDetails is not yet implemented");
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
    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.CloudJobSubmission domainCloudJobSubmission =
                    cloudJobSubmissionMapper.toDomain(sshJobSubmission);
            return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, domainCloudJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update cloud job submission details");
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
    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SSHJobSubmission domainSSHJobSubmission =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, domainSSHJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update SSH job submission details");
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
    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.CloudJobSubmission domainCloudJobSubmission =
                    cloudJobSubmissionMapper.toDomain(cloudSubmission);
            return registryService.addCloudJobSubmissionDetails(
                    computeResourceId, priorityOrder, domainCloudJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add cloud job submission details");
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
    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicoreJobSubmission =
                    unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return registryService.addUNICOREJobSubmissionDetails(
                    computeResourceId, priorityOrder, domainUnicoreJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add UNICORE job submission details");
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
    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SSHJobSubmission domainSSHJobSubmission =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return registryService.addSSHForkJobSubmissionDetails(
                    computeResourceId, priorityOrder, domainSSHJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add SSH fork job submission details");
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
    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.SSHJobSubmission domainSSHJobSubmission =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, domainSSHJobSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add SSH job submission details");
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
    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.LOCALSubmission domainLocalSubmission =
                    localSubmissionMapper.toDomain(localSubmission);
            return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, domainLocalSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update local submission details");
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
    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.LOCALSubmission domainLocalSubmission =
                    localSubmissionMapper.toDomain(localSubmission);
            return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, domainLocalSubmission);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add local submission details");
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
    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StorageResourceDescription domainStorageResourceDescription =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return registryService.updateStorageResource(storageResourceId, domainStorageResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update storage resource");
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
    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.StorageResourceDescription domainStorageResourceDescription =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return registryService.registerStorageResource(domainStorageResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register storage resource");
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
    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResourceDescription =
                    computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            return registryService.updateComputeResource(computeResourceId, domainComputeResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update compute resource");
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
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResourceDescription =
                    computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            return registryService.registerComputeResource(domainComputeResourceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register compute resource");
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
    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainApplicationInterfaceDescription =
                    applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            return registryService.updateApplicationInterface(appInterfaceId, domainApplicationInterfaceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update application interface");
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
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainApplicationInterfaceDescription =
                    applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            return registryService.registerApplicationInterface(gatewayId, domainApplicationInterfaceDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register application interface");
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
    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainApplicationDeploymentDescription =
                    applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return registryService.updateApplicationDeployment(appDeploymentId, domainApplicationDeploymentDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update application deployment");
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
    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainApplicationDeploymentDescription =
                    applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return registryService.registerApplicationDeployment(gatewayId, domainApplicationDeploymentDescription);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register application deployment");
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
    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationModule domainApplicationModule =
                    applicationModuleMapper.toDomain(applicationModule);
            return registryService.updateApplicationModule(appModuleId, domainApplicationModule);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update application module");
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
    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ApplicationModule domainApplicationModule =
                    applicationModuleMapper.toDomain(applicationModule);
            return registryService.registerApplicationModule(gatewayId, domainApplicationModule);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register application module");
        }
    }

    @Override
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ComputationalResourceSchedulingModel domainResourceScheduling =
                    computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            registryService.updateResourceScheduleing(airavataExperimentId, domainResourceScheduling);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update resource scheduling");
        }
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserConfigurationDataModel domainUserConfiguration =
                    userConfigurationDataModelMapper.toDomain(userConfiguration);
            registryService.updateExperimentConfiguration(airavataExperimentId, domainUserConfiguration);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update experiment configuration");
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
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    experimentModelMapper.toDomain(experiment);
            registryService.updateExperiment(airavataExperimentId, domainExperiment);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update experiment");
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
     * * @throws org.apache.airavata.common.exception.CoreExceptions.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.common.exception.CoreExceptions.AiravataClientException
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
     * * @throws org.apache.airavata.common.exception.CoreExceptions.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param gatewayId
     * @param experiment
     */
    @Override
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    experimentModelMapper.toDomain(experiment);
            return registryService.createExperiment(gatewayId, domainExperiment);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to create experiment");
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
    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException {
        try {
            var domainFilters = new HashMap<org.apache.airavata.common.model.ExperimentSearchFields, String>();
            for (var entry : filters.entrySet()) {
                domainFilters.put(
                        org.apache.airavata.common.model.ExperimentSearchFields.valueOf(
                                entry.getKey().name()),
                        entry.getValue());
            }
            List<org.apache.airavata.common.model.ExperimentSummaryModel> domainSummaries =
                    registryService.searchExperiments(
                            gatewayId, userName, accessibleExpIds, domainFilters, limit, offset);
            return domainSummaries.stream()
                    .map(experimentSummaryModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to search experiments");
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
    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException {
        try {
            var domainFilters = new HashMap<org.apache.airavata.common.model.ProjectSearchFields, String>();
            for (var entry : filters.entrySet()) {
                domainFilters.put(
                        org.apache.airavata.common.model.ProjectSearchFields.valueOf(
                                entry.getKey().name()),
                        entry.getValue());
            }
            List<org.apache.airavata.common.model.Project> domainProjects = registryService.searchProjects(
                    gatewayId, userName, accessibleProjIds, domainFilters, limit, offset);
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to search projects");
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
    public void updateProject(String projectId, Project updatedProject) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Project domainProject = projectMapper.toDomain(updatedProject);
            registryService.updateProject(projectId, domainProject);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update project");
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
    public String createProject(String gatewayId, Project project) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Project domainProject = projectMapper.toDomain(project);
            return registryService.createProject(gatewayId, domainProject);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to create project");
        }
    }

    @Override
    public boolean updateNotification(Notification notification) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
            return registryService.updateNotification(domainNotification);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update notification");
        }
    }

    /**
     * * API methods to retrieve notifications
     * *
     *
     * @param notification
     */
    @Override
    public String createNotification(Notification notification) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
            return registryService.createNotification(domainNotification);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to create notification");
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
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(updatedGateway);
            return registryService.updateGateway(gatewayId, domainGateway);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update gateway");
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
    public String addGateway(Gateway gateway) throws RegistryServiceException, DuplicateEntryException {
        try {
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(gateway);
            return registryService.addGateway(domainGateway);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add gateway");
        }
    }

    /*This private method wraps the logic of getExperiment method as this method is called internally in the API.*/
    private org.apache.airavata.common.model.ExperimentModel getExperimentInternal(String airavataExperimentId)
            throws RegistryServiceException, ExperimentNotFoundException {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (Throwable e) {
            // Check if this is a "not found" error based on the message
            if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                logger.error("Experiment not found: " + airavataExperimentId, e);
                ExperimentNotFoundException exception = new ExperimentNotFoundException();
                exception.setMessage(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system.");
                throw exception;
            }
            logger.error("Error while retrieving the experiment", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a User Resource Profile.
     *
     * @param userResourceProfile User Resource Profile Object.
     *                            The GatewayID should be obtained from Airavata user profile data model and passed to register a corresponding
     *                            resource profile.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserResourceProfile domainUserResourceProfile =
                    userResourceProfileMapper.toDomain(userResourceProfile);
            return registryService.registerUserResourceProfile(domainUserResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register user resource profile");
        }
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws RegistryServiceException {
        try {
            return registryService.isUserResourceProfileExists(userId, gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if user resource profile exists");
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param userId The identifier for the requested user resource.
     * @return UserResourceProfile object
     */
    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserResourceProfile domainUserResourceProfile =
                    registryService.getUserResourceProfile(userId, gatewayId);
            return userResourceProfileMapper.toThrift(domainUserResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user resource profile");
        }
    }

    /**
     * Update a User Resource Profile.
     *
     * @param gatewayID           The identifier for the requested gateway resource to be updated.
     * @param userResourceProfile Gateway Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserResourceProfile domainUserResourceProfile =
                    userResourceProfileMapper.toDomain(userResourceProfile);
            return registryService.updateUserResourceProfile(userId, gatewayID, domainUserResourceProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update user resource profile");
        }
    }

    /**
     * Delete the given User Resource Profile.
     *
     * @param userId    identifier for user profile
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws RegistryServiceException {
        try {
            return registryService.deleteUserResourceProfile(userId, gatewayID);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete user resource profile");
        }
    }

    @Override
    public String addUser(UserProfile userProfile) throws RegistryServiceException, DuplicateEntryException {
        try {
            org.apache.airavata.common.model.UserProfile domainUserProfile = userProfileMapper.toDomain(userProfile);
            return registryService.addUser(domainUserProfile);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add user");
        }
    }

    /**
     * Add a User Compute Resource Preference to a registered gateway profile.
     *
     * @param userId
     * @param gatewayID                     The identifier for the gateway profile to be added.
     * @param computeResourceId             Preferences related to a particular compute resource
     * @param userComputeResourcePreference The UserComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserComputeResourcePreference domainUserComputeResourcePreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            return registryService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, domainUserComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add user compute resource preference");
        }
    }

    /**
     * Is a User Compute Resource Preference exists.
     *
     * @param userId
     * @param gatewayID         The identifier for the gateway profile to be added.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the addition. If a resource already exists, this operation will fail.
     */
    @Override
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            return registryService.isUserComputeResourcePreferenceExists(userId, gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if user compute resource preference exists");
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID             The identifier of the gateway profile to be added.
     * @param storageResourceId     Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserStoragePreference domainUserStoragePreference =
                    userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return registryService.addUserStoragePreference(
                    userId, gatewayID, storageResourceId, domainUserStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add user storage preference");
        }
    }

    /**
     * Fetch a Compute Resource Preference of a registered gateway profile.
     *
     * @param userId
     * @param gatewayID             The identifier for the gateway profile to be requested
     * @param userComputeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserComputeResourcePreference domainUserComputeResourcePreference =
                    registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return userComputeResourcePreferenceMapper.toThrift(domainUserComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user compute resource preference");
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     *
     * @param userId    identifier for user data model
     * @param gatewayID The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Storage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserStoragePreference domainUserStoragePreference =
                    registryService.getUserStoragePreference(userId, gatewayID, storageId);
            return userStoragePreferenceMapper.toThrift(domainUserStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get user storage preference");
        }
    }

    /**
     * Fetch all User Resource Profiles registered
     *
     * @return UserResourceProfile
     * Returns all the UserResourceProfile list object.
     */
    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.UserResourceProfile> domainUserResourceProfiles =
                    registryService.getAllUserResourceProfiles();
            return domainUserResourceProfiles.stream()
                    .map(userResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all user resource profiles");
        }
    }

    /**
     * Update a Compute Resource Preference to a registered user resource profile.
     *
     * @param userId                        identifier for user data model
     * @param gatewayID                     The identifier for the gateway profile to be updated.
     * @param computeResourceId             Preferences related to a particular compute resource
     * @param userComputeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserComputeResourcePreference domainUserComputeResourcePreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            return registryService.updateUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, domainUserComputeResourcePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update user compute resource preference");
        }
    }

    /**
     * Update a Storage Resource Preference of a registered user resource profile.
     *
     * @param userId                identifier for user data model
     * @param gatewayID             The identifier of the gateway profile to be updated.
     * @param storageId             The Storage resource identifier of the one that you want to update
     * @param userStoragePreference The storagePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.UserStoragePreference domainUserStoragePreference =
                    userStoragePreferenceMapper.toDomain(userStoragePreference);
            return registryService.updateUserStoragePreference(
                    userId, gatewayID, storageId, domainUserStoragePreference);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update user storage preference");
        }
    }

    /**
     * Delete the Compute Resource Preference of a registered gateway profile.
     *
     * @param userId            The identifier for user data model
     * @param gatewayID         The identifier for the gateway profile to be deleted.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            return registryService.deleteUserComputeResourcePreference(userId, gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete user compute resource preference");
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     *
     * @param userId    The identifier for user data model
     * @param gatewayID The identifier of the gateway profile to be deleted.
     * @param storageId ID of the storage preference you want to delete.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            return registryService.deleteUserStoragePreference(userId, gatewayID, storageId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to delete user storage preference");
        }
    }

    /**
     * * Get queue statuses of all compute resources
     * *
     */
    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.QueueStatusModel> domainQueueStatuses =
                    registryService.getLatestQueueStatuses();
            return domainQueueStatuses.stream()
                    .map(queueStatusModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get latest queue statuses");
        }
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.QueueStatusModel> domainQueueStatuses =
                    queueStatuses.stream().map(queueStatusModelMapper::toDomain).collect(Collectors.toList());
            registryService.registerQueueStatuses(domainQueueStatuses);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to register queue statuses");
        }
    }

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.QueueStatusModel domainQueueStatus =
                    registryService.getQueueStatus(hostName, queueName);
            return queueStatusModelMapper.toThrift(domainQueueStatus);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get queue status");
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
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.UserComputeResourcePreference> domainUserComputeResourcePreferences =
                    registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
            return domainUserComputeResourcePreferences.stream()
                    .map(userComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all user compute resource preferences");
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
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.UserStoragePreference> domainUserStoragePreferences =
                    registryService.getAllUserStoragePreferences(userId, gatewayID);
            return domainUserStoragePreferences.stream()
                    .map(userStoragePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get all user storage preferences");
        }
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups)
            throws RegistryServiceException, DuplicateEntryException {
        try {
            org.apache.airavata.common.model.GatewayGroups domainGatewayGroups =
                    gatewayGroupsMapper.toDomain(gatewayGroups);
            registryService.createGatewayGroups(domainGatewayGroups);
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                throw new DuplicateEntryException(e.getMessage());
            }
            throw convertToRegistryServiceException(e, "Error while creating GatewayGroups");
        }
    }

    @Override
    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayGroups domainGatewayGroups =
                    gatewayGroupsMapper.toDomain(gatewayGroups);
            registryService.updateGatewayGroups(domainGatewayGroups);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to update gateway groups");
        }
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryServiceException {
        try {
            return registryService.isGatewayGroupsExists(gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if gateway groups exists");
        }
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayGroups domainGatewayGroups =
                    registryService.getGatewayGroups(gatewayId);
            return gatewayGroupsMapper.toThrift(domainGatewayGroups);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway groups");
        }
    }

    @Override
    public Parser getParser(String parserId, String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Parser domainParser = registryService.getParser(parserId, gatewayId);
            return parserMapper.toThrift(domainParser);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parser");
        }
    }

    @Override
    public String saveParser(Parser parser) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.Parser domainParser = parserMapper.toDomain(parser);
            return registryService.saveParser(domainParser);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to save parser");
        }
    }

    @Override
    public List<Parser> listAllParsers(String gatewayId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.Parser> domainParsers = registryService.listAllParsers(gatewayId);
            return domainParsers.stream().map(parserMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to list all parsers");
        }
    }

    @Override
    public void removeParser(String parserId, String gatewayId) throws RegistryServiceException {
        try {
            registryService.removeParser(parserId, gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove parser");
        }
    }

    @Override
    public ParserInput getParserInput(String parserInputId, String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ParserInput domainParserInput =
                    registryService.getParserInput(parserInputId, gatewayId);
            return parserInputMapper.toThrift(domainParserInput);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parser input");
        }
    }

    @Override
    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ParserOutput domainParserOutput =
                    registryService.getParserOutput(parserOutputId, gatewayId);
            return parserOutputMapper.toThrift(domainParserOutput);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parser output");
        }
    }

    @Override
    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ParsingTemplate domainParsingTemplate =
                    registryService.getParsingTemplate(templateId, gatewayId);
            return parsingTemplateMapper.toThrift(domainParsingTemplate);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parsing template");
        }
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ParsingTemplate> domainParsingTemplates =
                    registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
            return domainParsingTemplates.stream()
                    .map(parsingTemplateMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get parsing templates for experiment");
        }
    }

    @Override
    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.ParsingTemplate domainParsingTemplate =
                    parsingTemplateMapper.toDomain(parsingTemplate);
            return registryService.saveParsingTemplate(domainParsingTemplate);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to save parsing template");
        }
    }

    @Override
    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryServiceException {
        try {
            List<org.apache.airavata.common.model.ParsingTemplate> domainParsingTemplates =
                    registryService.listAllParsingTemplates(gatewayId);
            return domainParsingTemplates.stream()
                    .map(parsingTemplateMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to list all parsing templates");
        }
    }

    @Override
    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        try {
            registryService.removeParsingTemplate(templateId, gatewayId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove parsing template");
        }
    }

    @Override
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            return registryService.isGatewayUsageReportingAvailable(gatewayId, computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to check if gateway usage reporting is available");
        }
    }

    @Override
    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayUsageReportingCommand domainCommand =
                    registryService.getGatewayReportingCommand(gatewayId, computeResourceId);
            return gatewayUsageReportingCommandMapper.toThrift(domainCommand);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to get gateway reporting command");
        }
    }

    @Override
    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryServiceException {
        try {
            org.apache.airavata.common.model.GatewayUsageReportingCommand domainCommand =
                    gatewayUsageReportingCommandMapper.toDomain(command);
            registryService.addGatewayUsageReportingCommand(domainCommand);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to add gateway usage reporting command");
        }
    }

    @Override
    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            registryService.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(e, "Failed to remove gateway usage reporting command");
        }
    }
}
