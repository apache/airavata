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
package org.apache.airavata.service.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.ProjectNotFoundException;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.common.model.CloudJobSubmission;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourcePolicy;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.DMType;
import org.apache.airavata.common.model.DataMovementInterface;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentSearchFields;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatistics;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayGroups;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.model.GatewayUsageReportingCommand;
import org.apache.airavata.common.model.GridFTPDataMovement;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.LOCALDataMovement;
import org.apache.airavata.common.model.LOCALSubmission;
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.Parser;
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.common.model.ParserOutput;
import org.apache.airavata.common.model.ParsingTemplate;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.ProjectSearchFields;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.StoragePreference;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.UnicoreDataMovement;
import org.apache.airavata.common.model.UnicoreJobSubmission;
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.common.model.UserStoragePreference;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.exception.WorkflowCatalogException;
import org.apache.airavata.registry.model.ExpCatChildDataType;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.services.ApplicationDeploymentService;
import org.apache.airavata.registry.services.ApplicationInterfaceService;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.DataProductService;
import org.apache.airavata.registry.services.DataReplicaLocationService;
import org.apache.airavata.registry.services.ExperimentErrorService;
import org.apache.airavata.registry.services.ExperimentOutputService;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.ExperimentStatusService;
import org.apache.airavata.registry.services.ExperimentSummaryService;
import org.apache.airavata.registry.services.GatewayGroupsService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.GatewayUsageReportingCommandService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.apache.airavata.registry.services.GwyResourceProfileService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.JobStatusService;
import org.apache.airavata.registry.services.NotificationService;
import org.apache.airavata.registry.services.ParserInputService;
import org.apache.airavata.registry.services.ParserOutputService;
import org.apache.airavata.registry.services.ParserService;
import org.apache.airavata.registry.services.ParsingTemplateService;
import org.apache.airavata.registry.services.ProcessErrorService;
import org.apache.airavata.registry.services.ProcessOutputService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProcessWorkflowService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.QueueStatusService;
import org.apache.airavata.registry.services.StorageResourceService;
import org.apache.airavata.registry.services.TaskErrorService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.services.TaskStatusService;
import org.apache.airavata.registry.services.UserResourceProfileService;
import org.apache.airavata.registry.services.UserService;
import org.apache.airavata.registry.services.WorkflowService;
import org.apache.airavata.registry.utils.Constants;
import org.apache.airavata.registry.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnApiService
public class RegistryService {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    private final AiravataServerProperties properties;
    private final ApplicationDeploymentService applicationDeploymentService;
    private final StorageResourceService storageResourceService;
    private final GwyResourceProfileService gwyResourceProfileService;
    private final UserResourceProfileService userResourceProfileService;
    private final GroupResourceProfileService groupResourceProfileService;
    private final ComputeResourceService computeResourceService;
    // Note: All repository access now through service classes
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final JobService jobService;
    private final ExperimentSummaryService experimentSummaryService;
    private final UserService userService;
    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final ProcessStatusService processStatusService;
    private final TaskStatusService taskStatusService;
    private final ExperimentStatusService experimentStatusService;
    private final JobStatusService jobStatusService;
    private final ProcessOutputService processOutputService;
    private final ExperimentOutputService experimentOutputService;
    private final ProcessWorkflowService processWorkflowService;
    private final ExperimentErrorService experimentErrorService;
    private final QueueStatusService queueStatusService;
    private final TaskErrorService taskErrorService;
    private final ProcessErrorService processErrorService;
    private final DataProductService dataProductService;
    private final DataReplicaLocationService dataReplicaLocationService;
    private final WorkflowService workflowService;
    private final ParserService parserService;
    private final ParserInputService parserInputService;
    private final ParserOutputService parserOutputService;
    private final ParsingTemplateService parsingTemplateService;
    private final GatewayGroupsService gatewayGroupsService;
    private final GatewayUsageReportingCommandService gatewayUsageReportingCommandService;
    private final ApplicationInterfaceService applicationInterfaceService;

    public RegistryService(
            AiravataServerProperties properties,
            ApplicationDeploymentService applicationDeploymentService,
            StorageResourceService storageResourceService,
            GwyResourceProfileService gwyResourceProfileService,
            UserResourceProfileService userResourceProfileService,
            GroupResourceProfileService groupResourceProfileService,
            ComputeResourceService computeResourceService,
            org.apache.airavata.registry.services.ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            JobService jobService,
            ExperimentSummaryService experimentSummaryService,
            UserService userService,
            GatewayService gatewayService,
            org.apache.airavata.registry.services.ProjectService projectService,
            org.apache.airavata.registry.services.NotificationService notificationService,
            ProcessStatusService processStatusService,
            TaskStatusService taskStatusService,
            ExperimentStatusService experimentStatusService,
            JobStatusService jobStatusService,
            ProcessOutputService processOutputService,
            ExperimentOutputService experimentOutputService,
            ProcessWorkflowService processWorkflowService,
            ExperimentErrorService experimentErrorService,
            QueueStatusService queueStatusService,
            TaskErrorService taskErrorService,
            ProcessErrorService processErrorService,
            org.apache.airavata.registry.services.DataProductService dataProductService,
            DataReplicaLocationService dataReplicaLocationService,
            WorkflowService workflowService,
            ParserService parserService,
            ParserInputService parserInputService,
            ParserOutputService parserOutputService,
            ParsingTemplateService parsingTemplateService,
            GatewayGroupsService gatewayGroupsService,
            GatewayUsageReportingCommandService gatewayUsageReportingCommandService,
            ApplicationInterfaceService applicationInterfaceService) {
        this.properties = properties;
        this.applicationDeploymentService = applicationDeploymentService;
        this.storageResourceService = storageResourceService;
        this.gwyResourceProfileService = gwyResourceProfileService;
        this.userResourceProfileService = userResourceProfileService;
        this.groupResourceProfileService = groupResourceProfileService;
        this.computeResourceService = computeResourceService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.experimentSummaryService = experimentSummaryService;
        this.userService = userService;
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.processStatusService = processStatusService;
        this.taskStatusService = taskStatusService;
        this.experimentStatusService = experimentStatusService;
        this.jobStatusService = jobStatusService;
        this.processOutputService = processOutputService;
        this.experimentOutputService = experimentOutputService;
        this.processWorkflowService = processWorkflowService;
        this.experimentErrorService = experimentErrorService;
        this.queueStatusService = queueStatusService;
        this.taskErrorService = taskErrorService;
        this.processErrorService = processErrorService;
        this.dataProductService = dataProductService;
        this.dataReplicaLocationService = dataReplicaLocationService;
        this.workflowService = workflowService;
        this.parserService = parserService;
        this.parserInputService = parserInputService;
        this.parserOutputService = parserOutputService;
        this.parsingTemplateService = parsingTemplateService;
        this.gatewayGroupsService = gatewayGroupsService;
        this.gatewayUsageReportingCommandService = gatewayUsageReportingCommandService;
        this.applicationInterfaceService = applicationInterfaceService;
    }

    public String getAPIVersion() {
        return org.apache.airavata.registry.model.RegistryApiConstants.REGISTRY_API_VERSION;
    }

    public boolean isUserExists(String gatewayId, String userName) throws RegistryException {
        try {
            return userService.isUserExists(gatewayId, userName);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while verifying user: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryException {
        try {
            return userService.getAllUsernamesInGateway(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving users: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Gateway getGateway(String gatewayId) throws RegistryException {
        try {
            if (!gatewayService.isGatewayExist(gatewayId)) {
                String message = String.format(
                        "Gateway '%s' does not exist in the system. Please provide a valid gateway ID.", gatewayId);
                logger.error(message);
                throw new RegistryException(message);
            }
            var gateway = gatewayService.getGateway(gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : {}", gateway.getGatewayId());
            return gateway;
        } catch (RegistryException e) {
            String message = String.format("Error while getting the gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteGateway(String gatewayId) throws RegistryException {
        try {
            if (!gatewayService.isGatewayExist(gatewayId)) {
                // Gateway doesn't exist - this is fine for delete operations (idempotent)
                // It may have been deleted already or never existed in this registry
                logger.debug("Gateway '{}' does not exist in the system, nothing to delete.", gatewayId);
                return true;
            }
            gatewayService.removeGateway(gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : {}", gatewayId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while deleting the gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<Gateway> getAllGateways() throws RegistryException {
        try {
            var gateways = gatewayService.getAllGateways();
            logger.debug("Airavata retrieved all available gateways...");
            return gateways;
        } catch (RegistryException e) {
            String message = "Error while getting all the gateways";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        try {
            return gatewayService.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while checking if gateway exists: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryException {
        try {
            notificationService.deleteNotification(notificationId);
            return true;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while deleting notification: gatewayId=%s, notificationId=%s", gatewayId, notificationId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Notification getNotification(String gatewayId, String notificationId) throws RegistryException {
        try {
            return notificationService.getNotification(notificationId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving notification: gatewayId=%s, notificationId=%s", gatewayId, notificationId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<Notification> getAllNotifications(String gatewayId) throws RegistryException {
        try {
            List<Notification> notifications = notificationService.getAllGatewayNotifications(gatewayId);
            return notifications;
        } catch (RegistryException e) {
            String message = String.format("Error while getting all notifications: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Project getProject(String projectId) throws RegistryException, ProjectNotFoundException {
        try {
            if (!projectService.isProjectExist(projectId)) {
                String message = String.format(
                        "Project '%s' does not exist in the system. Please provide a valid project ID.", projectId);
                logger.error(message);
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage(message);
                throw exception;
            }
            logger.debug("Airavata retrieved project with project Id : {}", projectId);
            var project = projectService.getProject(projectId);
            return project;
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving the project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteProject(String projectId) throws RegistryException, ProjectNotFoundException {
        try {
            if (!projectService.isProjectExist(projectId)) {
                String message = String.format(
                        "Project '%s' does not exist in the system. Please provide a valid project ID.", projectId);
                logger.error(message);
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage(message);
                throw exception;
            }
            projectService.removeProject(projectId);
            logger.debug("Airavata deleted project with project Id : {}", projectId);
            return true;
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryException e) {
            String message = String.format("Error while removing the project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<Project> projects = new ArrayList<>();
            if (!userService.isUserExists(gatewayId, userName)) {
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return projects;
            }
            var filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            projects = projectService.searchProjects(
                    filters,
                    limit,
                    offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
            return projects;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving projects: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryException(message);
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
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, fromTime + "");
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, toTime + "");
            if (userName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            }
            if (applicationName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, applicationName);
            }
            if (resourceHostName != null) {
                filters.put(Constants.FieldConstants.ExperimentConstants.RESOURCE_HOST_ID, resourceHostName);
            }
            limit = Math.min(limit, 1000);
            ExperimentStatistics result = experimentSummaryService.getAccessibleExperimentStatistics(
                    accessibleExpIds, filters, limit, offset);
            logger.debug(
                    "Airavata retrieved experiments for gateway id : {} between : {} and {}",
                    gatewayId,
                    org.apache.airavata.common.utils.AiravataUtils.getTime(fromTime),
                    org.apache.airavata.common.utils.AiravataUtils.getTime(toTime));
            return result;
        } catch (RegistryException e) {
            String message = String.format("Error while getting experiment statistics: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!validateString(projectId)) {
                logger.error("Project id cannot be empty. Please provide a valid project ID...");
                throw new RegistryException("Project id cannot be empty. Please provide a valid project ID...");
            }
            if (!projectService.isProjectExist(projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            List<ExperimentModel> experiments = experimentService.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID,
                    projectId,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved experiments for project : {}", projectId);
            return experiments;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiments: gatewayId=%s, projectId=%s", gatewayId, projectId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ExperimentModel> experiments = new ArrayList<>();
            if (!userService.isUserExists(gatewayId, userName)) {
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return experiments;
            }
            experiments = experimentService.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.USER_NAME,
                    userName,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved experiments for user : {}", userName);
            return experiments;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiments: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteExperiment(String experimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(experimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = experimentService.getExperiment(experimentId);
            if (!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)) {
                logger.error("Error while deleting the experiment");
                throw new RegistryException(
                        "Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
            }
            experimentService.removeExperiment(experimentId);
            logger.debug("Airavata removed experiment with experiment id : {}", experimentId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while deleting the experiment: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentService.getExperiment(airavataExperimentId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving the experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryException {
        return getExperimentInternal(airavataExperimentId);
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryException {
        try {
            var experimentModel = getExperimentInternal(airavataExperimentId);
            var processList = processService.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
            if (processList != null) {
                processList.forEach(p -> {
                    p.getTasks().forEach(t -> {
                        try {
                            var jobList = jobService.getJobList(
                                    Constants.FieldConstants.JobConstants.TASK_ID, ((TaskModel) t).getTaskId());
                            if (jobList != null) {
                                jobList.sort(Comparator.comparingLong(JobModel::getCreationTime));
                                t.setJobs(jobList);
                            }
                        } catch (RegistryException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
                });
                experimentModel.setProcesses(processList);
            }
            logger.debug("Airavata retrieved detailed experiment with experiment id : {}", airavataExperimentId);
            return experimentModel;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving the experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving experiment status, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentStatusService.getExperimentStatus(airavataExperimentId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving experiment status: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryException {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : {}", airavataExperimentId);
        return experimentStatus;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Get experiment outputs failed, experiment {} doesn't exit.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            logger.debug("Airavata retrieved experiment outputs for experiment id : {}", airavataExperimentId);
            return experimentOutputService.getExperimentOutputs(airavataExperimentId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiment outputs: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryException {
        try {
            var jobPK = new JobPK();
            jobPK.setTaskId(taskId);
            jobPK.setJobId(jobId);
            jobStatusService.updateJobStatus(jobStatus, jobPK);
        } catch (RegistryException e) {
            String message = String.format("Error while updating job status: taskId=%s, jobId=%s", taskId, jobId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addJob(JobModel jobModel, String processId) throws RegistryException {
        try {
            jobService.addJob(jobModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding job: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        try {
            return processService.addProcess(processModel, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding process: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        try {
            processService.updateProcess(processModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating process: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addTask(TaskModel taskModel, String processId) throws RegistryException {
        try {
            return taskService.addTask(taskModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding task: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void deleteTasks(String processId) throws RegistryException {
        try {
            taskService.deleteTasks(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while deleting tasks: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        return experimentService.getUserConfigurationData(experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        try {
            return processService.getProcess(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        try {
            var processModels = processService.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);
            return processModels;
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process list: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        try {
            return processStatusService.getProcessStatus(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ProcessModel> getProcessListInState(ProcessState processState) throws RegistryException {
        try {
            var finalProcessList = new ArrayList<ProcessModel>();
            int offset = 0;
            int limit = 100;
            int count = 0;
            do {
                var processStatusList = processStatusService.getProcessStatusList(processState, offset, limit);
                offset += processStatusList.size();
                count = processStatusList.size();
                for (var processStatus : processStatusList) {
                    var latestStatus = processStatusService.getProcessStatus(processStatus.getProcessId());
                    if (latestStatus != null && latestStatus.getState().name().equals(processState.name())) {
                        finalProcessList.add(processService.getProcess(latestStatus.getProcessId()));
                    }
                }
            } while (count == limit);
            return finalProcessList;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving process list with given status: processState=%s", processState);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        try {
            return processStatusService.getProcessStatusList(processId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving process status list for given process Id: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private JobModel fetchJobModel(String queryType, String id) throws RegistryException {
        try {
            if (queryType.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
                return jobService.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id).stream()
                        .filter(job -> job.getJobId() != null && !job.getJobId().isEmpty())
                        .findFirst()
                        .orElse(null);
            } else if (queryType.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
                return jobService.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id).stream()
                        .filter(job -> job.getJobId() != null && !job.getJobId().isEmpty())
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (RegistryException e) {
            String message = String.format("Error while fetching job model: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private List<JobModel> fetchJobModels(String queryType, String id) throws RegistryException {
        try {
            var jobs =
                    switch (queryType) {
                        case Constants.FieldConstants.JobConstants.TASK_ID ->
                            jobService.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
                        case Constants.FieldConstants.JobConstants.PROCESS_ID ->
                            jobService.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
                        case Constants.FieldConstants.JobConstants.JOB_ID ->
                            jobService.getJobList(Constants.FieldConstants.JobConstants.JOB_ID, id);
                        default -> new ArrayList<JobModel>();
                    };
            return jobs;
        } catch (RegistryException e) {
            String message = String.format("Error while fetching job models: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isJobExist(String queryType, String id) throws RegistryException {
        JobModel jobModel = fetchJobModel(queryType, id);
        return jobModel != null;
    }

    public JobModel getJob(String queryType, String id) throws RegistryException {
        try {
            var jobModel = fetchJobModel(queryType, id);
            if (jobModel != null) return jobModel;
            throw new RegistryException("Job not found for queryType: " + queryType + ", id: " + id);
        } catch (RegistryException e) {
            String message = String.format("Error while getting job: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<JobModel> getJobs(String queryType, String id) throws RegistryException {
        return fetchJobModels(queryType, id);
    }

    public int getJobCount(
            org.apache.airavata.common.model.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryException {
        try {
            var jobStatusList = jobStatusService.getDistinctListofJobStatus(
                    gatewayId, jobStatus.getJobState().name(), searchBackTimeInMinutes);
            return jobStatusList.size();
        } catch (RegistryException e) {
            String message = String.format("Error while getting job count: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryException {
        return processService.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        try {
            return processOutputService.getProcessOutputs(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process outputs: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException {
        try {
            return processWorkflowService.getProcessWorkflows(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process workflows: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryException {
        try {
            processWorkflowService.addProcessWorkflow(processWorkflow, processWorkflow.getProcessId());
        } catch (RegistryException e) {
            String message =
                    String.format("Error while adding process workflow: processId=%s", processWorkflow.getProcessId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<String> getProcessIds(String experimentId) throws RegistryException {
        try {
            return processService.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process ids: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving job details, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            var processModels = processService.getProcessList(
                    Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            var jobList = new ArrayList<JobModel>();
            if (processModels != null && !processModels.isEmpty()) {
                for (var processModel : processModels) {
                    var tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (var taskModel : tasks) {
                            var taskId = taskModel.getTaskId();
                            var taskJobs = jobService.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            jobList.addAll(taskJobs);
                        }
                    }
                }
            }
            logger.debug("Airavata retrieved job models for experiment with experiment id : {}", airavataExperimentId);
            return jobList;
        } catch (RegistryException e) {
            String message = String.format("Error while getting job details: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private boolean validateString(String name) {
        return name != null && !name.isBlank();
    }

    public boolean isGatewayExistInternal(String gatewayId) throws RegistryException {
        try {
            return gatewayService.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while checking if gateway exists: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryException {
        try {
            var module = applicationInterfaceService.getApplicationModule(appModuleId);
            logger.debug("Airavata retrieved application module with module id : {}", appModuleId);
            return module;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var moduleList = applicationInterfaceService.getAllApplicationModules(gatewayId);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting all app modules: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var moduleList = applicationInterfaceService.getAccessibleApplicationModules(
                    gatewayId, accessibleAppIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting accessible app modules: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteApplicationModule(String appModuleId) throws RegistryException {
        try {
            logger.debug("Airavata deleted application module with module id : " + appModuleId);
            return applicationInterfaceService.removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            String message = String.format("Error while deleting application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws RegistryException {
        try {
            var deployement = applicationDeploymentService.getApplicationDeployement(appDeploymentId);
            logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
            return deployement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryException {
        try {
            applicationDeploymentService.removeAppDeployment(appDeploymentId);
            logger.debug("Airavata removed application deployment with deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployements = applicationDeploymentService.getAllApplicationDeployements(gatewayId);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting all application deployments: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployements = applicationDeploymentService.getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting accessible application deployments: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployments = applicationDeploymentService.getAccessibleApplicationDeployments(
                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            return deployments;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting accessible application deployments for app module: gatewayId=%s, appModuleId=%s",
                    gatewayId, appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws RegistryException {
        try {
            var appDeployments = new ArrayList<String>();
            var filters = new HashMap<String, String>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            var applicationDeployments = applicationDeploymentService.getApplicationDeployments(filters);
            for (var description : applicationDeployments) {
                appDeployments.add(description.getAppDeploymentId());
            }
            logger.debug("Airavata retrieved application deployments for module id : " + appModuleId);
            return appDeployments;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting app module deployed resources: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryException {
        try {
            var filters = new HashMap<String, String>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            var applicationDeployments = applicationDeploymentService.getApplicationDeployments(filters);
            return applicationDeployments;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting application deployments: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws RegistryException {
        try {
            var interfaceDescription = applicationInterfaceService.getApplicationInterface(appInterfaceId);
            logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
            return interfaceDescription;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryException {
        try {
            boolean removeApplicationInterface = applicationInterfaceService.removeApplicationInterface(appInterfaceId);
            logger.debug("Airavata removed application interface with interface id : " + appInterfaceId);
            return removeApplicationInterface;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ApplicationInterfaceDescription> allApplicationInterfaces =
                    applicationInterfaceService.getAllApplicationInterfaces(gatewayId);
            var allApplicationInterfacesMap = new HashMap<String, String>();
            if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()) {
                for (var interfaceDescription : allApplicationInterfaces) {
                    allApplicationInterfacesMap.put(
                            interfaceDescription.getApplicationInterfaceId(),
                            interfaceDescription.getApplicationName());
                }
            }
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return allApplicationInterfacesMap;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving all application interface names: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ApplicationInterfaceDescription> interfaces =
                    applicationInterfaceService.getAllApplicationInterfaces(gatewayId);
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return interfaces;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving all application interfaces: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws RegistryException {
        try {
            List<InputDataObjectType> applicationInputs =
                    applicationInterfaceService.getApplicationInputs(appInterfaceId);
            logger.debug("Airavata retrieved application inputs for application interface id : " + appInterfaceId);
            return applicationInputs;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving application inputs: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId) throws RegistryException {
        try {
            List<OutputDataObjectType> applicationOutputs =
                    applicationInterfaceService.getApplicationOutputs(appInterfaceId);
            logger.debug("Airavata retrieved application outputs for application interface id : " + appInterfaceId);
            return applicationOutputs;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving application outputs: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryException {
        try {
            List<OutputDataObjectType> list = getApplicationOutputsInternal(appInterfaceId);
            logger.debug("Airavata retrieved application outputs for app interface id : " + appInterfaceId);
            return list;
        } catch (RegistryException e) {
            throw e;
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws RegistryException {
        try {
            Map<String, String> allComputeResources = computeResourceService.getAvailableComputeResourceIdList();
            var availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface =
                    applicationInterfaceService.getApplicationInterface(appInterfaceId);
            var filters = new HashMap<String, String>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()) {
                for (var moduleId : applicationModules) {
                    filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, moduleId);
                    var applicationDeployments = applicationDeploymentService.getApplicationDeployments(filters);
                    for (var deploymentDescription : applicationDeployments) {
                        var computeHostId = deploymentDescription.getComputeHostId();
                        if (allComputeResources.get(computeHostId) != null) {
                            availableComputeResources.put(computeHostId, allComputeResources.get(computeHostId));
                        }
                    }
                }
            }
            logger.debug(
                    "Airavata retrieved available compute resources for application interface id : " + appInterfaceId);
            return availableComputeResources;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving available app interface compute resources: appInterfaceId=%s",
                    appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryException {
        try {
            ComputeResourceDescription computeResource = computeResourceService.getComputeResource(computeResourceId);
            logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
            return computeResource;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Map<String, String> getAllComputeResourceNames() throws RegistryException {
        try {
            Map<String, String> computeResourceIdList = computeResourceService.getAllComputeResourceIdList();
            logger.debug("Airavata retrieved all the available compute resources...");
            return computeResourceIdList;
        } catch (AppCatalogException e) {
            String message = "Error while retrieving all compute resource names";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteComputeResource(String computeResourceId) throws RegistryException {
        try {
            computeResourceService.removeComputeResource(computeResourceId);
            logger.debug("Airavata deleted compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws RegistryException {
        try {
            StorageResourceDescription storageResource = storageResourceService.getStorageResource(storageResourceId);
            logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
            return storageResource;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public Map<String, String> getAllStorageResourceNames() throws RegistryException {
        try {
            Map<String, String> resourceIdList = storageResourceService.getAllStorageResourceIdList();
            logger.debug("Airavata retrieved storage resources list...");
            return resourceIdList;
        } catch (AppCatalogException e) {
            String message = "Error while retrieving all storage resource names";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws RegistryException {
        try {
            storageResourceService.removeStorageResource(storageResourceId);
            logger.debug("Airavata deleted storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryException {
        try {
            LOCALSubmission localJobSubmission = computeResourceService.getLocalJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
            return localJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving local job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryException {
        try {
            SSHJobSubmission sshJobSubmission = computeResourceService.getSSHJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
            return sshJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving SSH job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws RegistryException {
        try {
            UnicoreJobSubmission unicoreJobSubmission = computeResourceService.getUNICOREJobSubmission(jobSubmissionId);
            logger.debug(
                    "Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
            return unicoreJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving UNICORE job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws RegistryException {
        try {
            CloudJobSubmission cloudJobSubmission = computeResourceService.getCloudJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
            return cloudJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving cloud job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder)
            throws RegistryException {
        return false;
    }

    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder)
            throws RegistryException {
        return false;
    }

    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap)
            throws RegistryException {
        return false;
    }

    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap) throws RegistryException {
        return false;
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws RegistryException {
        try {
            computeResourceService.removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            logger.debug("Airavata deleted job submission interface with interface id : " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting job submission interface: computeResourceId=%s, jobSubmissionInterfaceId=%s",
                    computeResourceId, jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws RegistryException {
        try {
            return computeResourceService.getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryException {
        try {
            computeResourceService.deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws RegistryException {
        try {
            computeResourceService.removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting batch queue: computeResourceId=%s, queueName=%s",
                    computeResourceId, queueName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileService.getGatewayProfile(gatewayID);
            logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
            return gatewayResourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format("Error while retrieving gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            gwyResourceProfileService.removeGatewayResourceProfile(gatewayID);
            logger.debug("Airavata deleted gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while deleting gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!gwyResourceProfileService.isGatewayResourceProfileExists(gatewayID)) {
                logger.error(
                        gatewayID,
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            }
            if (!computeResourceService.isComputeResourceExists(computeResourceId)) {
                logger.error(
                        computeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new AppCatalogException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            ComputeResourcePreference computeResourcePreference =
                    gwyResourceProfileService.getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Airavata retrieved gateway compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + computeResourceId);
            return computeResourcePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!gwyResourceProfileService.isGatewayResourceProfileExists(gatewayID)) {
                logger.error(
                        gatewayID,
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            }
            StoragePreference storagePreference = gwyResourceProfileService.getStoragePreference(gatewayID, storageId);
            logger.debug("Airavata retrieved storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gwyResourceProfileService.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving all gateway compute resource preferences: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gwyResourceProfileService.getGatewayProfile(gatewayID).getStoragePreferences();
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving all gateway storage preferences: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryException {
        return gwyResourceProfileService.getAllGatewayProfiles();
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gwyResourceProfileService.removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gwyResourceProfileService.removeDataStoragePreferenceFromGateway(gatewayID, storageId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryException {
        try {
            DataProductModel dataProductModel = dataProductService.getDataProduct(productUri);
            return dataProductModel;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving data product: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public DataProductModel getParentDataProduct(String productUri) throws RegistryException {
        try {
            DataProductModel dataProductModel = dataProductService.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving parent data product: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryException {
        try {
            List<DataProductModel> dataProductModels = dataProductService.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving child data products: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws RegistryException {
        try {
            List<DataProductModel> dataProductModels =
                    dataProductService.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return dataProductModels;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error while searching data products by name: gatewayId=%s, userId=%s, productName=%s",
                    gatewayId, userId, productName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryException {
        try {
            if (!isGatewayExistInternal(groupResourceProfile.getGatewayId())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String groupResourceProfileId = groupResourceProfileService.addGroupResourceProfile(groupResourceProfile);
            logger.debug("New Group Resource Profile Created: " + groupResourceProfileId);
            return groupResourceProfileId;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while creating group resource profile: gatewayId=%s", groupResourceProfile.getGatewayId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryException {
        try {
            if (!groupResourceProfileService.isGroupResourceProfileExists(
                    groupResourceProfile.getGroupResourceProfileId())) {
                logger.error(
                        "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            String groupResourceProfileId =
                    groupResourceProfileService.updateGroupResourceProfile(groupResourceProfile);
            logger.debug(" Group Resource Profile updated: " + groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating group resource profile: groupResourceProfileId=%s",
                    groupResourceProfile.getGroupResourceProfileId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryException {
        try {
            if (!groupResourceProfileService.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            return groupResourceProfileService.getGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving group resource profile: groupResourceProfileId=%s", groupResourceProfileId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws RegistryException {
        return groupResourceProfileService.isGroupResourceProfileExists(groupResourceProfileId);
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws RegistryException {
        try {
            if (!groupResourceProfileService.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error(
                        "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            return groupResourceProfileService.removeGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while removing group resource profile: groupResourceProfileId=%s", groupResourceProfileId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws RegistryException {
        return groupResourceProfileService.getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws RegistryException {
        groupResourceProfileService.removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        logger.debug("Removed compute resource preferences with compute resource ID: " + computeResourceId);
        return true;
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryException {
        groupResourceProfileService.removeComputeResourcePolicy(resourcePolicyId);
        logger.debug("Removed compute resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryException {
        groupResourceProfileService.removeBatchQueueResourcePolicy(resourcePolicyId);
        logger.debug("Removed batch resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryException {
        try {
            GroupComputeResourcePreference groupComputeResourcePreference =
                    groupResourceProfileService.getGroupComputeResourcePreference(
                            computeResourceId, groupResourceProfileId);
            if (!(groupComputeResourcePreference != null)) {
                logger.error("GroupComputeResourcePreference not found");
                throw new AppCatalogException("GroupComputeResourcePreference not found ");
            }
            return groupComputeResourcePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving group compute resource preference: computeResourceId=%s, groupResourceProfileId=%s",
                    computeResourceId, groupResourceProfileId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws RegistryException {
        return groupResourceProfileService.isGroupComputeResourcePreferenceExists(
                computeResourceId, groupResourceProfileId);
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryException {
        try {
            ComputeResourcePolicy computeResourcePolicy =
                    groupResourceProfileService.getComputeResourcePolicy(resourcePolicyId);
            if (!(computeResourcePolicy != null)) {
                logger.error("Group Compute Resource policy not found");
                throw new AppCatalogException("Group Compute Resource policy not found ");
            }
            return computeResourcePolicy;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving group compute resource policy: resourcePolicyId=%s", resourcePolicyId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryException {
        try {
            BatchQueueResourcePolicy batchQueueResourcePolicy =
                    groupResourceProfileService.getBatchQueueResourcePolicy(resourcePolicyId);
            if (!(batchQueueResourcePolicy != null)) {
                logger.error("Group Batch Queue Resource policy not found");
                throw new AppCatalogException("Group Batch Queue Resource policy not found ");
            }
            return batchQueueResourcePolicy;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving batch queue resource policy: resourcePolicyId=%s", resourcePolicyId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws RegistryException {
        return groupResourceProfileService.getAllGroupComputeResourcePreferences(groupResourceProfileId);
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws RegistryException {
        return groupResourceProfileService.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws RegistryException {
        return groupResourceProfileService.getAllGroupComputeResourcePolicies(groupResourceProfileId);
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws RegistryException {
        try {
            String replicaId = dataReplicaLocationService.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error in retrieving the replica: replicaName=%s", replicaLocationModel.getReplicaName());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryException {
        try {
            String productUrl = dataProductService.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error in registering the data resource: productName=%s", dataProductModel.getProductName());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryException {
        try {
            LOCALDataMovement localDataMovement = computeResourceService.getLocalDataMovement(dataMovementId);
            logger.debug("Airavata retrieved local data movement with data movement id: " + dataMovementId);
            return localDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving local data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryException {
        try {
            SCPDataMovement scpDataMovement = computeResourceService.getSCPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved SCP data movement with data movement id: " + dataMovementId);
            return scpDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving SCP data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws RegistryException {
        try {
            UnicoreDataMovement unicoreDataMovement = computeResourceService.getUNICOREDataMovement(dataMovementId);
            logger.debug("Airavata retrieved UNICORE data movement with data movement id: " + dataMovementId);
            return unicoreDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving UNICORE data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws RegistryException {
        try {
            GridFTPDataMovement gridFTPDataMovement = computeResourceService.getGridFTPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved GRIDFTP data movement with data movement id: " + dataMovementId);
            return gridFTPDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving GRIDFTP data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Experiment operations
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryException {
        try {
            if (!validateString(experiment.getExperimentName())) {
                logger.error("Cannot create experiments with empty experiment name");
                throw new RegistryException("Cannot create experiments with empty experiment name");
            }
            logger.info("Creating experiment with name " + experiment.getExperimentName());
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }

            if (experiment.getUserConfigurationData() != null
                    && experiment.getUserConfigurationData().getComputationalResourceScheduling() != null
                    && experiment
                                    .getUserConfigurationData()
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId()
                            != null) {

                String compResourceId = experiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();
                try {
                    ComputeResourceDescription computeResourceDescription =
                            computeResourceService.getComputeResource(compResourceId);
                    if (!computeResourceDescription.getEnabled()) {
                        logger.error("Compute Resource is not enabled by the Admin!");
                        throw new RegistryException("Compute Resource is not enabled by the Admin!");
                    }
                } catch (AppCatalogException e) {
                    throw new RegistryException("Error checking compute resource: " + e.getMessage(), e);
                }
            } else if (experiment.getUserConfigurationData() != null
                    && !experiment
                            .getUserConfigurationData()
                            .getAutoScheduledCompResourceSchedulingList()
                            .isEmpty()) {
                for (ComputationalResourceSchedulingModel computationalResourceScheduling :
                        experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList()) {
                    try {
                        ComputeResourceDescription computeResourceDescription =
                                computeResourceService.getComputeResource(
                                        computationalResourceScheduling.getResourceHostId());
                        if (!computeResourceDescription.getEnabled()) {
                            logger.error(
                                    "Compute Resource  with id" + computationalResourceScheduling.getResourceHostId()
                                            + "" + " is not enabled by the Admin!");
                            throw new RegistryException(
                                    "Compute Resource  with id" + computationalResourceScheduling.getResourceHostId()
                                            + "" + " is not enabled by the Admin!");
                        }
                    } catch (AppCatalogException e) {
                        throw new RegistryException("Error checking compute resource: " + e.getMessage(), e);
                    }
                }
            }

            experiment.setGatewayId(gatewayId);
            String experimentId = experimentService.addExperiment(experiment);
            if (experiment.getExperimentType() == ExperimentType.WORKFLOW) {
                try {
                    workflowService.registerWorkflow(experiment.getWorkflow(), experimentId);
                } catch (WorkflowCatalogException e) {
                    throw new RegistryException("Error registering workflow: " + e.getMessage(), e);
                }
            }
            logger.debug(
                    experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
            return experimentId;
        } catch (RegistryException e) {
            String message = String.format("Error while creating experiment: gatewayId=%s", gatewayId);
            logger.error(message, e);
            e.setMessage(message);
            throw e;
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!userService.isUserExists(gatewayId, userName)) {
                logger.error("User does not exist in the system. Please provide a valid user..");
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<>();
            Map<String, String> regFilters = new HashMap<>();
            regFilters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            for (var entry : filters.entrySet()) {
                if (entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_NAME)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_DESC)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.DESCRIPTION, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.APPLICATION_ID)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.STATUS)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.FROM_DATE)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.TO_DATE)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.PROJECT_ID)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.PROJECT_ID, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.USER_NAME)) {
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, entry.getValue());
                } else if (entry.getKey().equals(ExperimentSearchFields.JOB_ID)) {
                    regFilters.put(Constants.FieldConstants.JobConstants.JOB_ID, entry.getValue());
                }
            }

            // Check sharing settings - if sharing is not enabled or not configured,
            // fall back to filtering by username for non-shared experiments
            if (accessibleExpIds.isEmpty() && !properties.isSharingEnabled()) {
                if (!regFilters.containsKey(DBConstants.Experiment.USER_NAME)) {
                    regFilters.put(DBConstants.Experiment.USER_NAME, userName);
                }
            }
            summaries = experimentSummaryService.searchAllAccessibleExperiments(
                    accessibleExpIds,
                    regFilters,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved experiments for user : " + userName + " and gateway id : " + gatewayId);
            return summaries;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while searching experiments: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            e.setMessage(message);
            throw e;
        }
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Update request failed, Experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }

            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                var experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED, SCHEDULED, VALIDATED -> {
                        if (experiment.getUserConfigurationData() != null
                                && experiment.getUserConfigurationData().getComputationalResourceScheduling() != null
                                && experiment
                                                .getUserConfigurationData()
                                                .getComputationalResourceScheduling()
                                                .getResourceHostId()
                                        != null) {
                            var compResourceId = experiment
                                    .getUserConfigurationData()
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId();
                            try {
                                var computeResourceDescription =
                                        computeResourceService.getComputeResource(compResourceId);
                                if (!computeResourceDescription.getEnabled()) {
                                    logger.error("Compute Resource is not enabled by the Admin!");
                                    throw new RegistryException("Compute Resource is not enabled by the Admin!");
                                }
                            } catch (AppCatalogException e) {
                                throw new RegistryException("Error checking compute resource: " + e.getMessage(), e);
                            }
                        }
                        experimentService.updateExperiment(experiment, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated experiment {} ",
                                experiment.getExperimentName());
                    }
                    default -> {
                        logger.error(
                                airavataExperimentId,
                                "Error while updating experiment. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ");
                        throw new RegistryException(
                                "Error while updating experiment. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ");
                    }
                }
            }
        } catch (RegistryException e) {
            String message = String.format("Error while updating experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            e.setMessage(message);
            throw e;
        }
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Update experiment configuration failed, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                var experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED, VALIDATED, CANCELED, FAILED -> {
                        experimentService.addUserConfigurationData(userConfiguration, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated experiment configuration for experiment {}.",
                                airavataExperimentId);
                    }
                    default -> {
                        String msg = String.format(
                                "Error while updating experiment configuration: experimentId=%s. Operation is valid only for experiments in CREATED, VALIDATED, CANCELLED, FAILED or UNKNOWN state.",
                                airavataExperimentId);
                        logger.error(msg);
                        throw new RegistryException(msg);
                    }
                }
            }
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating experiment configuration: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            e.setMessage(message);
            throw e;
        }
    }

    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                String msg = String.format(
                        "Error while retrieving intermediate outputs: experimentId=%s. Experiment does not exist.",
                        airavataExperimentId);
                logger.error(msg);
                throw new RegistryException(msg);
            }
            var processModels = processService.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, airavataExperimentId);
            var intermediateOutputs = new ArrayList<OutputDataObjectType>();
            if (processModels != null && !processModels.isEmpty()) {
                for (var processModel : processModels) {
                    var processOutputs = processOutputService.getProcessOutputs(processModel.getProcessId());
                    if (processOutputs != null && !processOutputs.isEmpty()) {
                        intermediateOutputs.addAll(processOutputs);
                    }
                }
            }
            logger.debug("Intermediate outputs retrieved for experiment=%s", airavataExperimentId);
            return intermediateOutputs;
        } catch (RegistryException e) {
            String message =
                    String.format("Error retrieving intermediate outputs for experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message, e);
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving job statuses, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            var jobStatus = new HashMap<String, JobStatus>();
            var processModels = processService.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, airavataExperimentId);
            if (processModels != null && !processModels.isEmpty()) {
                for (var processModel : processModels) {
                    var tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (var taskModel : tasks) {
                            var taskId = taskModel.getTaskId();
                            var taskJobs = jobService.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            if (taskJobs != null && !taskJobs.isEmpty()) {
                                for (var jobModel : taskJobs) {
                                    var jobPK = new JobPK();
                                    jobPK.setJobId(jobModel.getJobId());
                                    jobPK.setTaskId(taskId);
                                    var status = jobStatusService.getJobStatus(jobPK);
                                    if (status != null) {
                                        jobStatus.put(jobModel.getJobId(), status);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            logger.debug("Job statuses retrieved for experiment=%s", airavataExperimentId);
            return jobStatus;
        } catch (RegistryException e) {
            var msg = String.format("Error retrieving job statuses for experimentId=%s", airavataExperimentId);
            logger.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryException {
        try {
            if (ExpCatChildDataType.PROCESS_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                processOutputService.addProcessOutputs(outputs, id);
            } else if (ExpCatChildDataType.EXPERIMENT_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                experimentOutputService.addExperimentOutputs(outputs, id);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while adding outputs: outputType=%s, id=%s", outputType, id);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryException {
        try {
            if (ExpCatChildDataType.EXPERIMENT_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentErrorService.addExperimentError(errorModel, id);
            } else if (ExpCatChildDataType.TASK_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                taskErrorService.addTaskError(errorModel, id);
            } else if (ExpCatChildDataType.PROCESS_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                processErrorService.addProcessError(errorModel, id);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while adding errors: errorType=%s, id=%s", errorType, id);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        try {
            taskStatusService.addTaskStatus(taskStatus, taskId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding task status: taskId=%s", taskId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        try {
            processStatusService.addProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        try {
            processStatusService.updateProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryException {
        try {
            experimentStatusService.updateExperimentStatus(experimentStatus, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating experiment status: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryException {
        try {
            JobPK jobPK = new JobPK();
            jobPK.setJobId(jobId);
            jobPK.setTaskId(taskId);
            jobStatusService.addJobStatus(jobStatus, jobPK);
        } catch (RegistryException e) {
            String message = String.format("Error while adding job status: taskId=%s, jobId=%s", taskId, jobId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void deleteJobs(String processId) throws RegistryException {
        try {
            List<JobModel> jobs = jobService.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, processId);
            for (var jobModel : jobs) {
                jobService.removeJob(jobModel);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while deleting jobs: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Project operations
    public String createProject(String gatewayId, Project project) throws RegistryException {
        try {
            if (!validateString(project.getName()) || !validateString(project.getOwner())) {
                logger.error("Project name and owner cannot be empty...");
                throw new RegistryException("Project name and owner cannot be empty...");
            }
            if (!validateString(gatewayId)) {
                logger.error("Gateway ID cannot be empty...");
                throw new RegistryException("Gateway ID cannot be empty...");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String projectId = projectService.addProject(project, gatewayId);
            return projectId;
        } catch (RegistryException e) {
            String message = String.format("Error while creating project: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateProject(String projectId, Project updatedProject) throws RegistryException {
        try {
            if (!validateString(projectId)) {
                logger.error("Project id cannot be empty...");
                throw new RegistryException("Project id cannot be empty...");
            }
            if (!projectService.isProjectExist(projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            projectService.updateProject(updatedProject, projectId);
            logger.debug("Airavata updated project with project Id : " + projectId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!userService.isUserExists(gatewayId, userName)) {
                logger.error("User does not exist in the system. Please provide a valid user..");
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            List<Project> projects = new ArrayList<>();
            Map<String, String> regFilters = new HashMap<>();
            regFilters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            for (var entry : filters.entrySet()) {
                if (entry.getKey().equals(ProjectSearchFields.PROJECT_NAME)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, entry.getValue());
                } else if (entry.getKey().equals(ProjectSearchFields.PROJECT_DESCRIPTION)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, entry.getValue());
                }
            }

            // Check sharing settings - if sharing is not enabled or not configured,
            // fall back to filtering by owner for non-shared projects
            if (accessibleProjIds.isEmpty() && !properties.isSharingEnabled()) {
                if (!regFilters.containsKey(DBConstants.Project.OWNER)) {
                    regFilters.put(DBConstants.Project.OWNER, userName);
                }
            }

            projects = projectService.searchAllAccessibleProjects(
                    accessibleProjIds,
                    regFilters,
                    limit,
                    offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
            return projects;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while searching projects: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Gateway Resource Profile operations
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws RegistryException {
        try {
            if (!validateString(gatewayResourceProfile.getGatewayID())) {
                logger.error("Cannot create gateway profile with empty gateway id");
                throw new AppCatalogException("Cannot create gateway profile with empty gateway id");
            }
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String resourceProfile = gwyResourceProfileService.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug(
                    "Airavata registered gateway profile with gateway id : " + gatewayResourceProfile.getGatewayID());
            return resourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while registering gateway resource profile: gatewayID=%s",
                    gatewayResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            gwyResourceProfileService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while updating gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!(gwyResourceProfileService.isGatewayResourceProfileExists(gatewayID))) {
                throw new AppCatalogException("Gateway resource profile '" + gatewayID + "' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileService.getGatewayProfile(gatewayID);
            if (profile.getComputeResourcePreferences() == null) {
                profile.setComputeResourcePreferences(new java.util.ArrayList<>());
            }
            profile.getComputeResourcePreferences().add(computeResourcePreference);
            gwyResourceProfileService.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added gateway compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GatewayResourceProfile profile = gwyResourceProfileService.getGatewayProfile(gatewayID);
            List<ComputeResourcePreference> computeResourcePreferences = profile.getComputeResourcePreferences();
            ComputeResourcePreference preferenceToRemove = null;
            for (var preference : computeResourcePreferences) {
                if (preference.getComputeResourceId().equals(computeResourceId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getComputeResourcePreferences().remove(preferenceToRemove);
            }
            profile.getComputeResourcePreferences().add(computeResourcePreference);
            gwyResourceProfileService.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!(gwyResourceProfileService.isGatewayResourceProfileExists(gatewayID))) {
                throw new AppCatalogException("Gateway resource profile '" + gatewayID + "' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileService.getGatewayProfile(gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            if (profile.getStoragePreferences() == null) {
                profile.setStoragePreferences(new java.util.ArrayList<>());
            }
            profile.getStoragePreferences().add(dataStoragePreference);
            gwyResourceProfileService.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding gateway storage preference: gatewayID=%s, storageResourceId=%s",
                    gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GatewayResourceProfile profile = gwyResourceProfileService.getGatewayProfile(gatewayID);
            List<StoragePreference> dataStoragePreferences = profile.getStoragePreferences();
            StoragePreference preferenceToRemove = null;
            for (var preference : dataStoragePreferences) {
                if (preference.getStorageResourceId().equals(storageId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getStoragePreferences().remove(preferenceToRemove);
            }
            profile.getStoragePreferences().add(storagePreference);
            gwyResourceProfileService.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Compute Resource operations
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws RegistryException {
        try {
            String computeResource = computeResourceService.addComputeResource(computeResourceDescription);
            logger.debug("Airavata registered compute resource with compute resource Id : " + computeResource);
            return computeResource;
        } catch (AppCatalogException e) {
            String message = "Error while registering compute resource";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription) throws RegistryException {
        try {
            computeResourceService.updateComputeResource(computeResourceId, computeResourceDescription);
            logger.debug("Airavata updated compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws RegistryException {
        try {
            return computeResourceService.addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            String message = "Error while registering resource job manager";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryException {
        try {
            computeResourceService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws RegistryException {
        try {
            return switch (dmType) {
                case COMPUTE_RESOURCE -> {
                    computeResourceService.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug(
                            "Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    yield true;
                }
                case STORAGE_RESOURCE -> {
                    storageResourceService.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug(
                            "Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    yield true;
                }
                default -> {
                    logger.error(
                            "Unsupported data movement type specifies.. Please provide the correct data movement type... ");
                    yield false;
                }
            };
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting data movement interface: resourceId=%s, dataMovementInterfaceId=%s",
                    resourceId, dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws RegistryException {
        try {
            throw new AppCatalogException("updateGridFTPDataMovementDetails is not yet implemented");
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating GridFTP data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws RegistryException {
        try {
            String addDataMovementInterface = addDataMovementInterface(
                    computeResourceId,
                    dmType,
                    computeResourceService.addGridFTPDataMovement(gridFTPDataMovement),
                    DataMovementProtocol.GridFTP,
                    priorityOrder);
            logger.debug("Airavata registered GridFTP data movement for resource Id: " + computeResourceId);
            return addDataMovementInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding GridFTP data movement details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws RegistryException {
        try {
            throw new AppCatalogException("updateUnicoreDataMovementDetails is not yet implemented");
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating Unicore data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws RegistryException {
        try {
            String movementInterface = addDataMovementInterface(
                    resourceId,
                    dmType,
                    computeResourceService.addUnicoreDataMovement(unicoreDataMovement),
                    DataMovementProtocol.UNICORE_STORAGE_SERVICE,
                    priorityOrder);
            logger.debug("Airavata registered UNICORE data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while adding Unicore data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws RegistryException {
        try {
            computeResourceService.updateScpDataMovement(scpDataMovement);
            logger.debug("Airavata updated SCP data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating SCP data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws RegistryException {
        try {
            String movementInterface = addDataMovementInterface(
                    resourceId,
                    dmType,
                    computeResourceService.addScpDataMovement(scpDataMovement),
                    DataMovementProtocol.SCP,
                    priorityOrder);
            logger.debug("Airavata registered SCP data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message = String.format("Error while adding SCP data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws RegistryException {
        try {
            computeResourceService.updateLocalDataMovement(localDataMovement);
            logger.debug("Airavata updated local data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating local data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws RegistryException {
        try {
            String movementInterface = addDataMovementInterface(
                    resourceId,
                    dataMoveType,
                    computeResourceService.addLocalDataMovement(localDataMovement),
                    DataMovementProtocol.LOCAL,
                    priorityOrder);
            logger.debug("Airavata registered local data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message = String.format("Error while adding local data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Storage Resource operations
    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws RegistryException {
        try {
            String storageResource = storageResourceService.addStorageResource(storageResourceDescription);
            logger.debug("Airavata registered storage resource with storage resource Id : " + storageResource);
            return storageResource;
        } catch (AppCatalogException e) {
            String message = "Error while registering storage resource";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) throws RegistryException {
        try {
            storageResourceService.updateStorageResource(storageResourceId, storageResourceDescription);
            logger.debug("Airavata updated storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Helper methods for job submission and data movement interfaces
    private String addJobSubmissionInterface(
            String computeResourceId,
            String jobSubmissionInterfaceId,
            JobSubmissionProtocol protocolType,
            int priorityOrder)
            throws RegistryException {
        try {
            JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
            jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            jobSubmissionInterface.setPriorityOrder(priorityOrder);
            jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
            return computeResourceService.addJobSubmissionProtocol(computeResourceId, jobSubmissionInterface);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding job submission interface: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    private String addDataMovementInterface(
            String computeResourceId,
            DMType dmType,
            String dataMovementInterfaceId,
            DataMovementProtocol protocolType,
            int priorityOrder)
            throws RegistryException {
        try {
            DataMovementInterface dataMovementInterface = new DataMovementInterface();
            dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
            dataMovementInterface.setPriorityOrder(priorityOrder);
            dataMovementInterface.setDataMovementProtocol(protocolType);
            if (dmType.equals(DMType.COMPUTE_RESOURCE)) {
                return computeResourceService.addDataMovementProtocol(computeResourceId, dmType, dataMovementInterface);
            } else if (dmType.equals(DMType.STORAGE_RESOURCE)) {
                dataMovementInterface.setStorageResourceId(computeResourceId);
                return storageResourceService.addDataMovementInterface(dataMovementInterface);
            }
            return null;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding data movement interface: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Job Submission Interface operations
    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws RegistryException {
        try {
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceId,
                    computeResourceService.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH,
                    priorityOrder);
            logger.debug("Airavata registered SSH job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding SSH job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws RegistryException {
        try {
            String submissionDetails = addJobSubmissionInterface(
                    computeResourceId,
                    computeResourceService.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH_FORK,
                    priorityOrder);
            logger.debug("Airavata registered Fork job submission for compute resource id: " + computeResourceId);
            return submissionDetails;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding SSH Fork job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws RegistryException {
        try {
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceId,
                    computeResourceService.addLocalJobSubmission(localSubmission),
                    JobSubmissionProtocol.LOCAL,
                    priorityOrder);
            logger.debug("Airavata added local job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding local submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws RegistryException {
        try {
            computeResourceService.updateLocalJobSubmission(localSubmission);
            logger.debug("Airavata updated local job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating local submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission) throws RegistryException {
        try {
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceId,
                    computeResourceService.addCloudJobSubmission(cloudSubmission),
                    JobSubmissionProtocol.CLOUD,
                    priorityOrder);
            logger.debug("Airavata registered Cloud job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding cloud job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryException {
        try {
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceId,
                    computeResourceService.addUNICOREJobSubmission(unicoreJobSubmission),
                    JobSubmissionProtocol.UNICORE,
                    priorityOrder);
            logger.debug("Airavata registered UNICORE job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding UNICORE job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Application Interface/Module/Deployment operations
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String interfaceId = applicationInterfaceService.addApplicationInterface(applicationInterface, gatewayId);
            logger.debug("Airavata registered application interface for gateway id : " + gatewayId);
            return interfaceId;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application interface: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws RegistryException {
        try {
            applicationInterfaceService.updateApplicationInterface(appInterfaceId, applicationInterface);
            logger.debug("Airavata updated application interface with interface id : " + appInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String module = applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);
            logger.debug("Airavata registered application module for gateway id : " + gatewayId);
            return module;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application module: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws RegistryException {
        try {
            applicationInterfaceService.updateApplicationModule(appModuleId, applicationModule);
            logger.debug("Airavata updated application module with module id: " + appModuleId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while updating application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws RegistryException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String deployment = applicationDeploymentService.addApplicationDeployment(applicationDeployment, gatewayId);
            logger.debug("Airavata registered application deployment for gateway id : " + gatewayId);
            return deployment;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application deployment: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws RegistryException {
        try {
            applicationDeploymentService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            logger.debug("Airavata updated application deployment for deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // User Resource Profile operations
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws RegistryException {
        try {
            if (!validateString(userResourceProfile.getUserId())) {
                logger.error("Cannot create user resource profile with empty user id");
                throw new AppCatalogException("Cannot create user resource profile with empty user id");
            }
            if (!validateString(userResourceProfile.getGatewayID())) {
                logger.error("Cannot create user resource profile with empty gateway id");
                throw new AppCatalogException("Cannot create user resource profile with empty gateway id");
            }
            if (!userService.isUserExists(userResourceProfile.getGatewayID(), userResourceProfile.getUserId())) {
                logger.error("User does not exist.Please provide a valid user ID...");
                throw new AppCatalogException("User does not exist.Please provide a valid user ID...");
            }
            String resourceProfile = userResourceProfileService.addUserResourceProfile(userResourceProfile);
            logger.debug("Airavata registered user resource profile with gateway id : "
                    + userResourceProfile.getGatewayID() + "and user id : " + userResourceProfile.getUserId());
            return resourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while registering user resource profile: userId=%s, gatewayID=%s",
                    userResourceProfile.getUserId(), userResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while registering user resource profile: userId=%s, gatewayID=%s",
                    userResourceProfile.getUserId(), userResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws RegistryException {
        boolean userExists;
        try {
            userExists = userService.isUserExists(gatewayId, userId);
        } catch (RegistryException e) {
            String message = String.format("User '%s' does not exist in gateway '%s'.", userId, gatewayId);
            logger.error(message);
            throw new RegistryException(message);
        }
        if (!userExists) {
            String message = String.format("User '%s' does not exist in gateway '%s'.", userId, gatewayId);
            logger.error(message);
            throw new RegistryException(message);
        }
        try {
            return userResourceProfileService.isUserResourceProfileExists(userId, gatewayId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "User resource profile with user id '%s' and gateway id '%s' does not exist.", userId, gatewayId);
            logger.error(message);
            throw new RegistryException(message);
        }
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayId, userId)) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("user does not exist.Please provide a valid gateway id...");
            }
            UserResourceProfile userResourceProfile =
                    userResourceProfileService.getUserResourceProfile(userId, gatewayId);
            logger.debug("Airavata retrieved User resource profile with user id : " + userId);
            return userResourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user resource profile: userId=%s, gatewayId=%s", userId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user resource profile: userId=%s, gatewayId=%s", userId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileService.removeUserResourceProfile(userId, gatewayID);
            logger.debug("Airavata deleted User profile with gateway id : " + gatewayID + " and user id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while deleting user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Resource Scheduling operations
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryException {
        try {
            if (!experimentService.isExperimentExist(airavataExperimentId)) {
                logger.debug(
                        airavataExperimentId,
                        "Update resource scheduling failed, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                var experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED, VALIDATED, CANCELED, FAILED -> {
                        processService.addProcessResourceSchedule(resourceScheduling, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated resource scheduling for the experiment {}.",
                                airavataExperimentId);
                    }
                    default -> {
                        logger.error(
                                airavataExperimentId,
                                "Error while updating scheduling info. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ");
                        throw new RegistryException(
                                "Error while updating experiment. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ");
                    }
                }
            }
        } catch (RegistryException e) {
            String message =
                    String.format("Error while updating resource scheduling: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // User operations
    public String addUser(UserProfile userProfile) throws RegistryException {
        try {
            logger.info("Adding User in Registry: " + userProfile);
            if (userService.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                throw new RegistryException("User already exists, with userId: " + userProfile.getUserId()
                        + ", and gatewayId: " + userProfile.getGatewayId());
            }
            UserProfile savedUser = userService.addUser(userProfile);
            return savedUser.getUserId();
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user: userId=%s, gatewayId=%s",
                    userProfile.getUserId(), userProfile.getGatewayId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // User Compute/Storage Preference operations
    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileService.getUserResourceProfile(userId, gatewayID);
            if (profile.getUserComputeResourcePreferences() == null) {
                profile.setUserComputeResourcePreferences(new java.util.ArrayList<>());
            }
            profile.getUserComputeResourcePreferences().add(userComputeResourcePreference);
            userResourceProfileService.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added User compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws RegistryException {
        try {
            if (userService.isUserExists(gatewayID, userId)
                    && userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                return userResourceProfileService.isUserComputeResourcePreferenceExists(
                        userId, gatewayID, computeResourceId);
            }
            return false;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while checking if user compute resource preference exists: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while checking if user compute resource preference exists: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            if (!computeResourceService.isComputeResourceExists(userComputeResourceId)) {
                logger.error(
                        userComputeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new AppCatalogException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            UserComputeResourcePreference userComputeResourcePreference =
                    userResourceProfileService.getUserComputeResourcePreference(
                            userId, gatewayID, userComputeResourceId);
            logger.debug("Airavata retrieved user compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + userComputeResourceId);
            return userComputeResourcePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user compute resource preference: userId=%s, gatewayID=%s, userComputeResourceId=%s",
                    userId, gatewayID, userComputeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user compute resource preference: userId=%s, gatewayID=%s, userComputeResourceId=%s",
                    userId, gatewayID, userComputeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileService.getUserResourceProfile(userId, gatewayID);
            List<UserComputeResourcePreference> userComputeResourcePreferences =
                    profile.getUserComputeResourcePreferences();
            UserComputeResourcePreference preferenceToRemove = null;
            for (var preference : userComputeResourcePreferences) {
                if (preference.getComputeResourceId().equals(computeResourceId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getUserComputeResourcePreferences().remove(preferenceToRemove);
            }
            profile.getUserComputeResourcePreferences().add(userComputeResourcePreference);
            userResourceProfileService.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileService.getUserResourceProfile(userId, gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            if (profile.getUserStoragePreferences() == null) {
                profile.setUserStoragePreferences(new java.util.ArrayList<>());
            }
            profile.getUserStoragePreferences().add(dataStoragePreference);
            userResourceProfileService.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding user storage preference: userId=%s, gatewayID=%s, storageResourceId=%s",
                    userId, gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user storage preference: userId=%s, gatewayID=%s, storageResourceId=%s",
                    userId, gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }

            UserStoragePreference storagePreference =
                    userResourceProfileService.getUserStoragePreference(userId, gatewayID, storageId);
            logger.debug("Airavata retrieved user storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryException {
        try {
            return userResourceProfileService.getAllUserResourceProfiles();
        } catch (AppCatalogException e) {
            String message = "Error while getting all user resource profiles";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Gateway Groups operations
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryException {
        try {
            if (!gatewayGroupsService.isExists(gatewayId)) {
                final String message = "No GatewayGroups entry exists for " + gatewayId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return gatewayGroupsService.get(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting gateway groups: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Parser operations
    public Parser getParser(String parserId, String gatewayId) throws RegistryException {
        try {
            if (!parserService.isExists(parserId)) {
                final String message = "No Parser Info entry exists for " + parserId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parserService.get(parserId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting parser: parserId=%s", parserId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String saveParser(Parser parser) throws RegistryException {
        try {
            Parser saved = parserService.saveParser(parser);
            return saved.getId();
        } catch (AppCatalogException e) {
            String message = "Error while saving parser";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void removeParser(String parserId, String gatewayId) throws RegistryException {
        try {
            boolean exists = parserService.isExists(parserId);
            if (!exists || !gatewayId.equals(parserService.get(parserId).getGatewayId())) {
                throw new RegistryException("Parser " + parserId + " does not exist");
            }
            parserService.delete(parserId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while removing parser: parserId=%s, gatewayId=%s", parserId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ParserInput getParserInput(String parserInputId, String gatewayId) throws RegistryException {
        try {
            if (!parserInputService.isExists(parserInputId)) {
                final String message = "No ParserInput entry exists for " + parserInputId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parserInputService.get(parserInputId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting parser input: parserInputId=%s", parserInputId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String saveParserInput(ParserInput parserInput) throws RegistryException {
        try {
            ParserInput saved = parserInputService.create(parserInput);
            return saved.getId();
        } catch (RegistryException e) {
            String message = "Error while saving parser input";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void removeParserInput(String parserInputId, String gatewayId) throws RegistryException {
        try {
            boolean exists = parserInputService.isExists(parserInputId);
            if (!exists) {
                throw new RegistryException("ParserInput " + parserInputId + " does not exist");
            }
            ParserInput parserInput = parserInputService.get(parserInputId);
            Parser parser = parserService.get(parserInput.getParserId());
            if (!gatewayId.equals(parser.getGatewayId())) {
                throw new RegistryException(
                        "ParserInput " + parserInputId + " does not belong to gateway " + gatewayId);
            }
            parserInputService.delete(parserInputId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error in removeParserInput: parserInputId=%s, gatewayId=%s", parserInputId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws RegistryException {
        try {
            if (!parserOutputService.isExists(parserOutputId)) {
                final String message = "No ParserOutput entry exists for " + parserOutputId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parserOutputService.get(parserOutputId);
        } catch (RegistryException e) {
            String message = String.format("Error in getParserOutput: parserOutputId=%s", parserOutputId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String saveParserOutput(ParserOutput parserOutput) throws RegistryException {
        try {
            ParserOutput saved = parserOutputService.create(parserOutput);
            return saved.getId();
        } catch (RegistryException e) {
            String message = "Error while saving parser output";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void removeParserOutput(String parserOutputId, String gatewayId) throws RegistryException {
        try {
            boolean exists = parserOutputService.isExists(parserOutputId);
            if (!exists) {
                throw new RegistryException("ParserOutput " + parserOutputId + " does not exist");
            }
            ParserOutput parserOutput = parserOutputService.get(parserOutputId);
            Parser parser = parserService.get(parserOutput.getParserId());
            if (!gatewayId.equals(parser.getGatewayId())) {
                throw new RegistryException(
                        "ParserOutput " + parserOutputId + " does not belong to gateway " + gatewayId);
            }
            parserOutputService.delete(parserOutputId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error in removeParserOutput: parserOutputId=%s, gatewayId=%s", parserOutputId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        try {
            if (!parsingTemplateService.isExists(templateId)) {
                final String message = "No ParsingTemplate entry exists for " + templateId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parsingTemplateService.get(templateId);
        } catch (RegistryException e) {
            String message = String.format("Error in getParsingTemplate: templateId=%s", templateId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryException {
        try {
            ParsingTemplate saved = parsingTemplateService.create(parsingTemplate);
            return saved.getId();
        } catch (RegistryException e) {
            String message = "Error while saving parsing template";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        try {
            boolean exists = parsingTemplateService.isExists(templateId);
            if (!exists
                    || !gatewayId.equals(parsingTemplateService.get(templateId).getGatewayId())) {
                throw new RegistryException("Parsing template " + templateId + " does not exist");
            }
            parsingTemplateService.delete(templateId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error in removeParsingTemplate: templateId=%s, gatewayId=%s", templateId, gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    // Gateway Usage Reporting operations
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryException {
        try {
            return gatewayUsageReportingCommandService.isGatewayUsageReportingCommandExists(
                    gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while checking if gateway usage reporting is available: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        try {
            if (!gatewayUsageReportingCommandService.isGatewayUsageReportingCommandExists(
                    gatewayId, computeResourceId)) {
                String message = "No usage reporting information for the gateway " + gatewayId
                        + " and compute resource " + computeResourceId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return gatewayUsageReportingCommandService.getGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting gateway reporting command: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryException {
        try {
            gatewayUsageReportingCommandService.addGatewayUsageReportingCommand(command);
        } catch (RegistryException e) {
            String message = "Error while adding gateway usage reporting command";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        try {
            gatewayUsageReportingCommandService.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while removing gateway usage reporting command: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws RegistryException {
        try {
            cloudJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceService.updateCloudJobSubmission(cloudJobSubmission);
            logger.debug("Airavata updated Cloud job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating cloud job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws RegistryException {
        try {
            sshJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceService.updateSSHJobSubmission(sshJobSubmission);
            logger.debug(
                    "Airavata updated SSH job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating SSH job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws RegistryException {
        try {
            unicoreJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceService.updateUNICOREJobSubmission(unicoreJobSubmission);
            logger.debug("Airavata updated UNICORE job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating Unicore job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateNotification(Notification notification) throws RegistryException {
        try {
            notificationService.updateNotification(notification);
            logger.debug("Airavata updated notification with notification id: " + notification.getNotificationId());
            return true;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating notification: notificationId=%s", notification.getNotificationId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String createNotification(Notification notification) throws RegistryException {
        try {
            notificationService.createNotification(notification);
            String notificationId = notification.getNotificationId();
            logger.debug("Airavata created notification with notification id: " + notificationId);
            return notificationId;
        } catch (RegistryException e) {
            String message = "Error while creating notification";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryException {
        try {
            if (!gatewayService.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            updatedGateway.setGatewayId(gatewayId);
            gatewayService.updateGateway(gatewayId, updatedGateway);
            logger.debug("Airavata updated gateway with gateway id: " + gatewayId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while updating gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public String addGateway(Gateway gateway) throws RegistryException {
        try {
            if (gatewayService.isGatewayExist(gateway.getGatewayId())) {
                logger.error("Gateway already exists in the system. Please provide a different gateway ID...");
                throw new AppCatalogException(
                        "Gateway already exists in the system. Please provide a different gateway ID...");
            }
            String gatewayId = gatewayService.addGateway(gateway);
            logger.debug("Airavata registered gateway with gateway id: " + gatewayId);
            return gatewayId;
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format("Error while adding gateway: gatewayId=%s", gateway.getGatewayId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileService.getUserResourceProfile(userId, gatewayID);
            List<UserStoragePreference> userStoragePreferences = profile.getUserStoragePreferences();
            UserStoragePreference preferenceToRemove = null;
            for (var preference : userStoragePreferences) {
                if (preference.getStorageResourceId().equals(storageId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getUserStoragePreferences().remove(preferenceToRemove);
            }
            userStoragePreference.setStorageResourceId(storageId);
            profile.getUserStoragePreferences().add(userStoragePreference);
            userResourceProfileService.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return true;
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while updating user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileService.removeUserComputeResourcePreferenceFromGateway(
                    userId, gatewayID, computeResourceId);
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while deleting user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileService.removeUserDataStoragePreferenceFromGateway(userId, gatewayID, storageId);
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while deleting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        try {
            return queueStatusService.getLatestQueueStatuses();
        } catch (RegistryException e) {
            String message = "Error while retrieving latest queue statuses";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryException {
        try {
            queueStatusService.createQueueStatuses(queueStatuses);
        } catch (RegistryException e) {
            String message = "Error while registering queue statuses";
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryException {
        try {
            QueueStatusModel queueStatusModel = queueStatusService.getQueueStatus(hostName, queueName);
            if (queueStatusModel != null) {
                return queueStatusModel;
            } else {
                QueueStatusModel newQueueStatusModel = new QueueStatusModel();
                newQueueStatusModel.setHostName(hostName);
                newQueueStatusModel.setQueueName(queueName);
                newQueueStatusModel.setQueueUp(false);
                newQueueStatusModel.setRunningJobs(0);
                newQueueStatusModel.setQueuedJobs(0);
                newQueueStatusModel.setTime(0);
                return newQueueStatusModel;
            }
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving queue status: hostName=%s, queueName=%s", hostName, queueName);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void createGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        try {
            if (gatewayGroupsService.isExists(gatewayGroups.getGatewayId())) {
                logger.error("GatewayGroups already exists for " + gatewayGroups.getGatewayId());
                throw new RegistryException(
                        "GatewayGroups for gatewayId: " + gatewayGroups.getGatewayId() + " already exists.");
            }
            gatewayGroupsService.create(gatewayGroups);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while creating gateway groups: gatewayId=%s", gatewayGroups.getGatewayId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        try {
            if (!gatewayGroupsService.isExists(gatewayGroups.getGatewayId())) {
                final String message = "No GatewayGroups entry exists for " + gatewayGroups.getGatewayId();
                logger.error(message);
                throw new RegistryException(message);
            }
            gatewayGroupsService.update(gatewayGroups);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while updating gateway groups: gatewayId=%s", gatewayGroups.getGatewayId());
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryException {
        try {
            return gatewayGroupsService.isExists(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while checking gateway groups existence: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<Parser> listAllParsers(String gatewayId) throws RegistryException {
        try {
            return parserService.getAllParsers(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while listing parsers: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId)
            throws RegistryException {
        try {
            return parsingTemplateService.getParsingTemplatesForApplication(applicationInterfaceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting parsing templates: applicationInterfaceId=%s", applicationInterfaceId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryException {
        try {
            ExperimentModel experiment = experimentService.getExperiment(experimentId);
            List<ProcessModel> processes = experiment.getProcesses();
            if (processes != null && !processes.isEmpty()) {
                return parsingTemplateService.getParsingTemplatesForApplication(
                        processes.get(processes.size() - 1).getApplicationInterfaceId());
            }
            return Collections.emptyList();
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving parsing templates for experiment: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryException {
        try {
            return parsingTemplateService.getAllParsingTemplates(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while listing parsing templates: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("User Resource Profile does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "User Resource Profile does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileService
                    .getUserResourceProfile(userId, gatewayID)
                    .getUserComputeResourcePreferences();
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while retrieving all user compute resource preferences: userId=%s, gatewayID=%s",
                    userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws RegistryException {
        try {
            if (!userService.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileService
                    .getUserResourceProfile(userId, gatewayID)
                    .getUserStoragePreferences();
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while retrieving all user storage preferences: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryException(message);
        }
    }
}
