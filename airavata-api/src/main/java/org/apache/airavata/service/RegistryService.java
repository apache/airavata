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

import java.util.*;
import org.apache.airavata.config.AiravataServerProperties;
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
import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.core.entities.expcatalog.JobPK;
import org.apache.airavata.registry.core.repositories.appcatalog.*;
import org.apache.airavata.registry.core.repositories.appcatalog.GroupResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.GwyResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.expcatalog.*;
import org.apache.airavata.registry.core.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.core.repositories.replicacatalog.DataReplicaLocationRepository;
import org.apache.airavata.registry.core.repositories.workflowcatalog.WorkflowRepository;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistryService {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    @Autowired
    private AiravataServerProperties properties;

    @Autowired
    private ApplicationDeploymentRepository applicationDeploymentRepository;

    @Autowired
    private ApplicationInterfaceRepository applicationInterfaceRepository;

    @Autowired
    private StorageResourceRepository storageResourceRepository;

    @Autowired
    private UserResourceProfileRepository userResourceProfileRepository;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ExperimentSummaryRepository experimentSummaryRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ExperimentOutputRepository experimentOutputRepository;

    @Autowired
    private ExperimentStatusRepository experimentStatusRepository;

    @Autowired
    private ExperimentErrorRepository experimentErrorRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessOutputRepository processOutputRepository;

    @Autowired
    private ProcessWorkflowRepository processWorkflowRepository;

    @Autowired
    private ProcessStatusRepository processStatusRepository;

    @Autowired
    private ProcessErrorRepository processErrorRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskErrorRepository taskErrorRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private QueueStatusRepository queueStatusRepository;

    @Autowired
    private DataProductRepository dataProductRepository;

    @Autowired
    private DataReplicaLocationRepository dataReplicaLocationRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private GatewayGroupsRepository gatewayGroupsRepository;

    @Autowired
    private ParserRepository parserRepository;

    @Autowired
    private ParserInputRepository parserInputRepository;

    @Autowired
    private ParserOutputRepository parserOutputRepository;

    @Autowired
    private ParsingTemplateRepository parsingTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Autowired
    private GatewayUsageReportingCommandRepository usageReportingCommandRepository;

    public String getAPIVersion() {
        return org.apache.airavata.registry.api.registry_apiConstants.REGISTRY_API_VERSION;
    }

    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException {
        try {
            return userRepository.isUserExists(gatewayId, userName);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while verifying user: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException {
        try {
            return userRepository.getAllUsernamesInGateway(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving users: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Gateway getGateway(String gatewayId) throws RegistryServiceException {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                String message = String.format(
                        "Gateway '%s' does not exist in the system. Please provide a valid gateway ID.", gatewayId);
                logger.error(message);
                throw new RegistryServiceException(message);
            }
            var gateway = gatewayRepository.getGateway(gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : " + gateway.getGatewayId());
            return gateway;
        } catch (RegistryException e) {
            String message = String.format("Error while getting the gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteGateway(String gatewayId) throws RegistryServiceException {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                String message = String.format(
                        "Gateway '%s' does not exist in the system. Please provide a valid gateway ID.", gatewayId);
                logger.error(message);
                throw new RegistryServiceException(message);
            }
            gatewayRepository.removeGateway(gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while deleting the gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<Gateway> getAllGateways() throws RegistryServiceException {
        try {
            var gateways = gatewayRepository.getAllGateways();
            logger.debug("Airavata retrieved all available gateways...");
            return gateways;
        } catch (RegistryException e) {
            String message = "Error while getting all the gateways";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryServiceException {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while checking if gateway exists: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        try {
            notificationRepository.deleteNotification(notificationId);
            return true;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while deleting notification: gatewayId=%s, notificationId=%s", gatewayId, notificationId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Notification getNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        try {
            return notificationRepository.getNotification(notificationId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving notification: gatewayId=%s, notificationId=%s", gatewayId, notificationId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<Notification> getAllNotifications(String gatewayId) throws RegistryServiceException {
        try {
            List<Notification> notifications = notificationRepository.getAllGatewayNotifications(gatewayId);
            return notifications;
        } catch (RegistryException e) {
            String message = String.format("Error while getting all notifications: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Project getProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                String message = String.format(
                        "Project '%s' does not exist in the system. Please provide a valid project ID.", projectId);
                logger.error(message);
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage(message);
                throw exception;
            }
            logger.debug("Airavata retrieved project with project Id : " + projectId);
            var project = projectRepository.getProject(projectId);
            return project;
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving the project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                String message = String.format(
                        "Project '%s' does not exist in the system. Please provide a valid project ID.", projectId);
                logger.error(message);
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage(message);
                throw exception;
            }
            projectRepository.removeProject(projectId);
            logger.debug("Airavata deleted project with project Id : " + projectId);
            return true;
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryException e) {
            String message = String.format("Error while removing the project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException {
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
            if (!userRepository.isUserExists(gatewayId, userName)) {
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return projects;
            }
            Map<String, String> filters = new HashMap<>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            projects = projectRepository.searchProjects(
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
            throw new RegistryServiceException(message);
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
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            Map<String, String> filters = new HashMap<>();
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
            ExperimentStatistics result = experimentSummaryRepository.getAccessibleExperimentStatistics(
                    accessibleExpIds, filters, limit, offset);
            logger.debug("Airavata retrieved experiments for gateway id : " + gatewayId + " between : "
                    + org.apache.airavata.common.utils.AiravataUtils.getTime(fromTime) + " and "
                    + org.apache.airavata.common.utils.AiravataUtils.getTime(toTime));
            return result;
        } catch (RegistryException e) {
            String message = String.format("Error while getting experiment statistics: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!validateString(projectId)) {
                logger.error("Project id cannot be empty. Please provide a valid project ID...");
                throw new RegistryException("Project id cannot be empty. Please provide a valid project ID...");
            }
            if (!projectRepository.isProjectExist(projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            List<ExperimentModel> experiments = experimentRepository.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID,
                    projectId,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved experiments for project : " + projectId);
            return experiments;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiments: gatewayId=%s, projectId=%s", gatewayId, projectId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
            if (!userRepository.isUserExists(gatewayId, userName)) {
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return experiments;
            }
            experiments = experimentRepository.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.USER_NAME,
                    userName,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved experiments for user : " + userName);
            return experiments;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiments: gatewayId=%s, userName=%s", gatewayId, userName);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteExperiment(String experimentId) throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(experimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
            if (!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)) {
                logger.error("Error while deleting the experiment");
                throw new RegistryException(
                        "Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
            }
            experimentRepository.removeExperiment(experimentId);
            logger.debug("Airavata removed experiment with experiment id : " + experimentId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while deleting the experiment: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentRepository.getExperiment(airavataExperimentId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving the experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryServiceException {
        return getExperimentInternal(airavataExperimentId);
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryServiceException {
        try {
            var experimentModel = getExperimentInternal(airavataExperimentId);
            var processList = processRepository.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
            if (processList != null) {
                processList.stream().forEach(p -> {
                    (p).getTasks().stream().forEach(t -> {
                        try {
                            var jobList = jobRepository.getJobList(
                                    Constants.FieldConstants.JobConstants.TASK_ID, ((TaskModel) t).getTaskId());
                            if (jobList != null) {
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
                });
                experimentModel.setProcesses(processList);
            }
            logger.debug("Airavata retrieved detailed experiment with experiment id : " + airavataExperimentId);
            return experimentModel;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving the experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving experiment status, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentStatusRepository.getExperimentStatus(airavataExperimentId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving experiment status: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryServiceException {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : " + airavataExperimentId);
        return experimentStatus;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId)
            throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Get experiment outputs failed, experiment {} doesn't exit.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            logger.debug("Airavata retrieved experiment outputs for experiment id : " + airavataExperimentId);
            return experimentOutputRepository.getExperimentOutputs(airavataExperimentId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving the experiment outputs: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryServiceException {
        try {
            var jobPK = new org.apache.airavata.registry.core.entities.expcatalog.JobPK();
            jobPK.setTaskId(taskId);
            jobPK.setJobId(jobId);
            jobStatusRepository.updateJobStatus(jobStatus, jobPK);
        } catch (RegistryException e) {
            String message = String.format("Error while updating job status: taskId=%s, jobId=%s", taskId, jobId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addJob(JobModel jobModel, String processId) throws RegistryServiceException {
        try {
            jobRepository.addJob(jobModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding job: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Note: deleteJobs method removed - JobRepository doesn't have this method
    // Jobs should be deleted individually using removeJob(jobPK) or removeJob(jobModel)

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryServiceException {
        try {
            return processRepository.addProcess(processModel, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding process: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException {
        try {
            processRepository.updateProcess(processModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating process: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addTask(TaskModel taskModel, String processId) throws RegistryServiceException {
        try {
            return taskRepository.addTask(taskModel, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding task: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void deleteTasks(String processId) throws RegistryServiceException {
        try {
            taskRepository.deleteTasks(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while deleting tasks: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryServiceException {
        try {
            return experimentRepository.getUserConfigurationData(experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting user configuration: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ProcessModel getProcess(String processId) throws RegistryServiceException {
        try {
            return processRepository.getProcess(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException {
        try {
            var processModels = processRepository.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);
            return processModels;
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process list: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException {
        try {
            return processStatusRepository.getProcessStatus(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while retrieving process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ProcessModel> getProcessListInState(ProcessState processState) throws RegistryServiceException {
        try {
            var finalProcessList = new ArrayList<ProcessModel>();
            int offset = 0;
            int limit = 100;
            int count = 0;
            do {
                var processStatusList = processStatusRepository.getProcessStatusList(processState, offset, limit);
                offset += processStatusList.size();
                count = processStatusList.size();
                for (ProcessStatus processStatus : processStatusList) {
                    var latestStatus = processStatusRepository.getProcessStatus(processStatus.getProcessId());
                    if (latestStatus.getState().name().equals(processState.name())) {
                        finalProcessList.add(processRepository.getProcess(latestStatus.getProcessId()));
                    }
                }
            } while (count == limit);
            return finalProcessList;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving process list with given status: processState=%s", processState);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryServiceException {
        try {
            return processStatusRepository.getProcessStatusList(processId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving process status list for given process Id: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private JobModel fetchJobModel(String queryType, String id) throws RegistryServiceException {
        try {
            if (queryType.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
                var jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
                if (jobs != null) {
                    for (JobModel jobModel : jobs) {
                        if (jobModel.getJobId() != null || !jobModel.equals("")) {
                            return jobModel;
                        }
                    }
                }
            } else if (queryType.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
                var jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
                if (jobs != null) {
                    for (JobModel jobModel : jobs) {
                        if (jobModel.getJobId() != null || !jobModel.equals("")) {
                            return jobModel;
                        }
                    }
                }
            }
            return null;
        } catch (RegistryException e) {
            String message = String.format("Error while fetching job model: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private List<JobModel> fetchJobModels(String queryType, String id) throws RegistryServiceException {
        try {
            List<JobModel> jobs;
            switch (queryType) {
                case Constants.FieldConstants.JobConstants.TASK_ID:
                    jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
                    break;
                case Constants.FieldConstants.JobConstants.PROCESS_ID:
                    jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
                    break;
                case Constants.FieldConstants.JobConstants.JOB_ID:
                    jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.JOB_ID, id);
                    break;
                default:
                    jobs = new ArrayList<>();
                    break;
            }
            return jobs;
        } catch (RegistryException e) {
            String message = String.format("Error while fetching job models: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isJobExist(String queryType, String id) throws RegistryServiceException {
        JobModel jobModel = fetchJobModel(queryType, id);
        return jobModel != null;
    }

    public JobModel getJob(String queryType, String id) throws RegistryServiceException {
        try {
            var jobModel = fetchJobModel(queryType, id);
            if (jobModel != null) return jobModel;
            throw new RegistryException("Job not found for queryType: " + queryType + ", id: " + id);
        } catch (RegistryException e) {
            String message = String.format("Error while getting job: queryType=%s, id=%s", queryType, id);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<JobModel> getJobs(String queryType, String id) throws RegistryServiceException {
        return fetchJobModels(queryType, id);
    }

    public int getJobCount(
            org.apache.airavata.model.status.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        var jobStatusList = jobStatusRepository.getDistinctListofJobStatus(
                gatewayId, jobStatus.getJobState().name(), searchBackTimeInMinutes);
        return jobStatusList.size();
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        return processRepository.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryServiceException {
        try {
            return processOutputRepository.getProcessOutputs(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process outputs: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryServiceException {
        try {
            return processWorkflowRepository.getProcessWorkflows(processId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process workflows: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryServiceException {
        try {
            processWorkflowRepository.addProcessWorkflow(processWorkflow, processWorkflow.getProcessId());
        } catch (RegistryException e) {
            String message =
                    String.format("Error while adding process workflow: processId=%s", processWorkflow.getProcessId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<String> getProcessIds(String experimentId) throws RegistryServiceException {
        try {
            return processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting process ids: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving job details, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            var processModels = processRepository.getProcessList(
                    Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            var jobList = new ArrayList<JobModel>();
            if (processModels != null && !processModels.isEmpty()) {
                for (ProcessModel processModel : processModels) {
                    var tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (TaskModel taskModel : tasks) {
                            var taskId = taskModel.getTaskId();
                            var taskJobs =
                                    jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            jobList.addAll(taskJobs);
                        }
                    }
                }
            }
            logger.debug("Airavata retrieved job models for experiment with experiment id : " + airavataExperimentId);
            return jobList;
        } catch (RegistryException e) {
            String message = String.format("Error while getting job details: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private boolean validateString(String name) {
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0) {
            valid = false;
        }
        return valid;
    }

    public boolean isGatewayExistInternal(String gatewayId) throws RegistryServiceException {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while checking if gateway exists: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryServiceException {
        try {
            var module = applicationInterfaceRepository.getApplicationModule(appModuleId);
            logger.debug("Airavata retrieved application module with module id : " + appModuleId);
            return module;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var moduleList = applicationInterfaceRepository.getAllApplicationModules(gatewayId);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting all app modules: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var moduleList = applicationInterfaceRepository.getAccessibleApplicationModules(
                    gatewayId, accessibleAppIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
            return moduleList;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting accessible app modules: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteApplicationModule(String appModuleId) throws RegistryServiceException {
        try {
            logger.debug("Airavata deleted application module with module id : " + appModuleId);
            return applicationInterfaceRepository.removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            String message = String.format("Error while deleting application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws RegistryServiceException {
        try {
            var deployement = applicationDeploymentRepository.getApplicationDeployement(appDeploymentId);
            logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
            return deployement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryServiceException {
        try {
            applicationDeploymentRepository.removeAppDeployment(appDeploymentId);
            logger.debug("Airavata removed application deployment with deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployements = applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting all application deployments: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployements = applicationDeploymentRepository.getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
            return deployements;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting accessible application deployments: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            var deployments = applicationDeploymentRepository.getAccessibleApplicationDeployments(
                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
            return deployments;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting accessible application deployments for app module: gatewayId=%s, appModuleId=%s",
                    gatewayId, appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws RegistryServiceException {
        try {
            var appDeployments = new ArrayList<String>();
            var filters = new HashMap<String, String>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            var applicationDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
            for (ApplicationDeploymentDescription description : applicationDeployments) {
                appDeployments.add(description.getAppDeploymentId());
            }
            logger.debug("Airavata retrieved application deployments for module id : " + appModuleId);
            return appDeployments;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting app module deployed resources: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryServiceException {
        try {
            var filters = new HashMap<String, String>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            var applicationDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
            return applicationDeployments;
        } catch (AppCatalogException e) {
            String message = String.format("Error while getting application deployments: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws RegistryServiceException {
        try {
            var interfaceDescription = applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
            logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
            return interfaceDescription;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while getting application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryServiceException {
        try {
            boolean removeApplicationInterface =
                    applicationInterfaceRepository.removeApplicationInterface(appInterfaceId);
            logger.debug("Airavata removed application interface with interface id : " + appInterfaceId);
            return removeApplicationInterface;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ApplicationInterfaceDescription> allApplicationInterfaces =
                    applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
            Map<String, String> allApplicationInterfacesMap = new HashMap<>();
            if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()) {
                for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces) {
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
            throw new RegistryServiceException(message);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            List<ApplicationInterfaceDescription> interfaces =
                    applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
            logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
            return interfaces;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving all application interfaces: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws RegistryServiceException {
        try {
            List<InputDataObjectType> applicationInputs =
                    applicationInterfaceRepository.getApplicationInputs(appInterfaceId);
            logger.debug("Airavata retrieved application inputs for application interface id : " + appInterfaceId);
            return applicationInputs;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving application inputs: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId)
            throws RegistryServiceException {
        try {
            List<OutputDataObjectType> applicationOutputs =
                    applicationInterfaceRepository.getApplicationOutputs(appInterfaceId);
            logger.debug("Airavata retrieved application outputs for application interface id : " + appInterfaceId);
            return applicationOutputs;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving application outputs: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryServiceException {
        try {
            List<OutputDataObjectType> list = getApplicationOutputsInternal(appInterfaceId);
            logger.debug("Airavata retrieved application outputs for app interface id : " + appInterfaceId);
            return list;
        } catch (RegistryServiceException e) {
            throw e;
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws RegistryServiceException {
        try {
            Map<String, String> allComputeResources =
                    new ComputeResourceRepository().getAvailableComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface =
                    applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()) {
                for (String moduleId : applicationModules) {
                    filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, moduleId);
                    List<ApplicationDeploymentDescription> applicationDeployments =
                            applicationDeploymentRepository.getApplicationDeployments(filters);
                    for (ApplicationDeploymentDescription deploymentDescription : applicationDeployments) {
                        if (allComputeResources.get(deploymentDescription.getComputeHostId()) != null) {
                            availableComputeResources.put(
                                    deploymentDescription.getComputeHostId(),
                                    allComputeResources.get(deploymentDescription.getComputeHostId()));
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
            throw new RegistryServiceException(message);
        }
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryServiceException {
        try {
            ComputeResourceDescription computeResource =
                    new ComputeResourceRepository().getComputeResource(computeResourceId);
            logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
            return computeResource;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException {
        try {
            Map<String, String> computeResourceIdList = new ComputeResourceRepository().getAllComputeResourceIdList();
            logger.debug("Airavata retrieved all the available compute resources...");
            return computeResourceIdList;
        } catch (AppCatalogException e) {
            String message = "Error while retrieving all compute resource names";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException {
        try {
            new ComputeResourceRepository().removeComputeResource(computeResourceId);
            logger.debug("Airavata deleted compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws RegistryServiceException {
        try {
            StorageResourceDescription storageResource =
                    storageResourceRepository.getStorageResource(storageResourceId);
            logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
            return storageResource;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException {
        try {
            Map<String, String> resourceIdList = storageResourceRepository.getAllStorageResourceIdList();
            logger.debug("Airavata retrieved storage resources list...");
            return resourceIdList;
        } catch (AppCatalogException e) {
            String message = "Error while retrieving all storage resource names";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException {
        try {
            storageResourceRepository.removeStorageResource(storageResourceId);
            logger.debug("Airavata deleted storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while deleting storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            LOCALSubmission localJobSubmission = new ComputeResourceRepository().getLocalJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
            return localJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving local job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            SSHJobSubmission sshJobSubmission = new ComputeResourceRepository().getSSHJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
            return sshJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving SSH job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            UnicoreJobSubmission unicoreJobSubmission =
                    new ComputeResourceRepository().getUNICOREJobSubmission(jobSubmissionId);
            logger.debug(
                    "Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
            return unicoreJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving UNICORE job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        try {
            CloudJobSubmission cloudJobSubmission =
                    new ComputeResourceRepository().getCloudJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
            return cloudJobSubmission;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving cloud job submission: jobSubmissionId=%s", jobSubmissionId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder)
            throws RegistryServiceException {
        return false;
    }

    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder)
            throws RegistryServiceException {
        return false;
    }

    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap)
            throws RegistryServiceException {
        return false;
    }

    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap)
            throws RegistryServiceException {
        return false;
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            logger.debug("Airavata deleted job submission interface with interface id : " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting job submission interface: computeResourceId=%s, jobSubmissionInterfaceId=%s",
                    computeResourceId, jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        try {
            return new ComputeResourceRepository().getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        try {
            new ComputeResourceRepository().deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws RegistryServiceException {
        try {
            new ComputeResourceRepository().removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting batch queue: computeResourceId=%s, queueName=%s",
                    computeResourceId, queueName);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
            return gatewayResourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format("Error while retrieving gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            gwyResourceProfileRepository.delete(gatewayID);
            logger.debug("Airavata deleted gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while deleting gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)) {
                logger.error(
                        gatewayID,
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            }
            if (!computeResourceRepository.isComputeResourceExists(computeResourceId)) {
                logger.error(
                        computeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new AppCatalogException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            ComputeResourcePreference computeResourcePreference =
                    gwyResourceProfileRepository.getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Airavata retrieved gateway compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + computeResourceId);
            return computeResourcePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)) {
                logger.error(
                        gatewayID,
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            }
            StoragePreference storagePreference =
                    gwyResourceProfileRepository.getStoragePreference(gatewayID, storageId);
            logger.debug("Airavata retrieved storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving all gateway compute resource preferences: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getStoragePreferences();
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving all gateway storage preferences: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryServiceException {
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.getAllGatewayProfiles();
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(
                    gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.removeDataStoragePreferenceFromGateway(gatewayID, storageId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException {
        try {
            DataProductModel dataProductModel = dataProductRepository.getDataProduct(productUri);
            return dataProductModel;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving data product: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public DataProductModel getParentDataProduct(String productUri) throws RegistryServiceException {
        try {
            DataProductModel dataProductModel = dataProductRepository.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving parent data product: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryServiceException {
        try {
            List<DataProductModel> dataProductModels = dataProductRepository.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (ReplicaCatalogException e) {
            String message = String.format("Error while retrieving child data products: productUri=%s", productUri);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset)
            throws RegistryServiceException {
        try {
            List<DataProductModel> dataProductModels =
                    dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return dataProductModels;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error while searching data products by name: gatewayId=%s, userId=%s, productName=%s",
                    gatewayId, userId, productName);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(groupResourceProfile.getGatewayId())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            String groupResourceProfileId =
                    groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
            logger.debug("New Group Resource Profile Created: " + groupResourceProfileId);
            return groupResourceProfileId;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while creating group resource profile: gatewayId=%s", groupResourceProfile.getGatewayId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(
                    groupResourceProfile.getGroupResourceProfileId())) {
                logger.error(
                        "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            String groupResourceProfileId =
                    groupResourceProfileRepository.updateGroupResourceProfile(groupResourceProfile);
            logger.debug(" Group Resource Profile updated: " + groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating group resource profile: groupResourceProfileId=%s",
                    groupResourceProfile.getGroupResourceProfileId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            return groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving group resource profile: groupResourceProfileId=%s", groupResourceProfileId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId);
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error(
                        "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new AppCatalogException(
                        "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            return groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while removing group resource profile: groupResourceProfileId=%s", groupResourceProfileId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        logger.debug("Removed compute resource preferences with compute resource ID: " + computeResourceId);
        return true;
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeComputeResourcePolicy(resourcePolicyId);
        logger.debug("Removed compute resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeBatchQueueResourcePolicy(resourcePolicyId);
        logger.debug("Removed batch resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            GroupComputeResourcePreference groupComputeResourcePreference =
                    groupResourceProfileRepository.getGroupComputeResourcePreference(
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
            throw new RegistryServiceException(message);
        }
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.isGroupComputeResourcePreferenceExists(
                computeResourceId, groupResourceProfileId);
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            ComputeResourcePolicy computeResourcePolicy =
                    groupResourceProfileRepository.getComputeResourcePolicy(resourcePolicyId);
            if (!(computeResourcePolicy != null)) {
                logger.error("Group Compute Resource policy not found");
                throw new AppCatalogException("Group Compute Resource policy not found ");
            }
            return computeResourcePolicy;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving group compute resource policy: resourcePolicyId=%s", resourcePolicyId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            BatchQueueResourcePolicy batchQueueResourcePolicy =
                    groupResourceProfileRepository.getBatchQueueResourcePolicy(resourcePolicyId);
            if (!(batchQueueResourcePolicy != null)) {
                logger.error("Group Batch Queue Resource policy not found");
                throw new AppCatalogException("Group Batch Queue Resource policy not found ");
            }
            return batchQueueResourcePolicy;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while retrieving batch queue resource policy: resourcePolicyId=%s", resourcePolicyId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupComputeResourcePreferences(groupResourceProfileId);
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId);
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws RegistryServiceException {
        try {
            String replicaId = dataReplicaLocationRepository.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error in retrieving the replica: replicaName=%s", replicaLocationModel.getReplicaName());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryServiceException {
        try {
            String productUrl = dataProductRepository.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (ReplicaCatalogException e) {
            String message = String.format(
                    "Error in registering the data resource: productName=%s", dataProductModel.getProductName());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            LOCALDataMovement localDataMovement = new ComputeResourceRepository().getLocalDataMovement(dataMovementId);
            logger.debug("Airavata retrieved local data movement with data movement id: " + dataMovementId);
            return localDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving local data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            SCPDataMovement scpDataMovement = new ComputeResourceRepository().getSCPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved SCP data movement with data movement id: " + dataMovementId);
            return scpDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving SCP data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            UnicoreDataMovement unicoreDataMovement =
                    new ComputeResourceRepository().getUNICOREDataMovement(dataMovementId);
            logger.debug("Airavata retrieved UNICORE data movement with data movement id: " + dataMovementId);
            return unicoreDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving UNICORE data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws RegistryServiceException {
        try {
            GridFTPDataMovement gridFTPDataMovement =
                    new ComputeResourceRepository().getGridFTPDataMovement(dataMovementId);
            logger.debug("Airavata retrieved GRIDFTP data movement with data movement id: " + dataMovementId);
            return gridFTPDataMovement;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while retrieving GRIDFTP data movement: dataMovementId=%s", dataMovementId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Experiment operations
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryServiceException {
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
                            new ComputeResourceRepository().getComputeResource(compResourceId);
                    if (!computeResourceDescription.isEnabled()) {
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
                        ComputeResourceDescription computeResourceDescription = new ComputeResourceRepository()
                                .getComputeResource(computationalResourceScheduling.getResourceHostId());
                        if (!computeResourceDescription.isEnabled()) {
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
            String experimentId = experimentRepository.addExperiment(experiment);
            if (experiment.getExperimentType() == ExperimentType.WORKFLOW) {
                try {
                    workflowRepository.registerWorkflow(experiment.getWorkflow(), experimentId);
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
            throw new RegistryServiceException(message);
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
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!userRepository.isUserExists(gatewayId, userName)) {
                logger.error("User does not exist in the system. Please provide a valid user..");
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            Map<String, String> regFilters = new HashMap<String, String>();
            regFilters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            for (Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet()) {
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

            try {
                if (accessibleExpIds.size() == 0 && !properties.getSharing().isEnabled()) {
                    if (!regFilters.containsKey(DBConstants.Experiment.USER_NAME)) {
                        regFilters.put(DBConstants.Experiment.USER_NAME, userName);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error checking sharing settings, continuing without filter", e);
            }
            summaries = experimentSummaryRepository.searchAllAccessibleExperiments(
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
            throw new RegistryServiceException(message);
        }
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Update request failed, Experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }

            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED:
                    case SCHEDULED:
                    case VALIDATED:
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
                                        new ComputeResourceRepository().getComputeResource(compResourceId);
                                if (!computeResourceDescription.isEnabled()) {
                                    logger.error("Compute Resource is not enabled by the Admin!");
                                    throw new RegistryException("Compute Resource is not enabled by the Admin!");
                                }
                            } catch (AppCatalogException e) {
                                throw new RegistryException("Error checking compute resource: " + e.getMessage(), e);
                            }
                        }
                        experimentRepository.updateExperiment(experiment, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated experiment {} ",
                                experiment.getExperimentName());
                        break;
                    default:
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
        } catch (RegistryException e) {
            String message = String.format("Error while updating experiment: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryServiceException e) {
            throw e;
        }
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Update experiment configuration failed, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED:
                    case VALIDATED:
                    case CANCELED:
                    case FAILED:
                        experimentRepository.addUserConfigurationData(userConfiguration, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated experiment configuration for experiment {}.",
                                airavataExperimentId);
                        break;
                    default:
                        logger.error(
                                airavataExperimentId,
                                "Error while updating experiment {}. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ",
                                airavataExperimentId);
                        throw new RegistryException(
                                "Error while updating experiment. Update experiment is only valid for experiments "
                                        + "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given "
                                        + "experiment is in one of above statuses... ");
                }
            }
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating experiment configuration: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryServiceException e) {
            throw e;
        }
    }

    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId)
            throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving intermediate outputs, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<ProcessModel> processModels = processRepository.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, airavataExperimentId);
            List<OutputDataObjectType> intermediateOutputs = new ArrayList<>();
            if (processModels != null && !processModels.isEmpty()) {
                for (ProcessModel processModel : processModels) {
                    List<OutputDataObjectType> processOutputs =
                            processOutputRepository.getProcessOutputs(processModel.getProcessId());
                    if (processOutputs != null && !processOutputs.isEmpty()) {
                        intermediateOutputs.addAll(processOutputs);
                    }
                }
            }
            logger.debug("Airavata retrieved intermediate outputs for experiment with experiment id : "
                    + airavataExperimentId);
            return intermediateOutputs;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving intermediate outputs: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.error(
                        airavataExperimentId,
                        "Error while retrieving job statuses, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            Map<String, JobStatus> jobStatus = new HashMap<>();
            List<ProcessModel> processModels = processRepository.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, airavataExperimentId);
            if (processModels != null && !processModels.isEmpty()) {
                for (ProcessModel processModel : processModels) {
                    List<TaskModel> tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (TaskModel taskModel : tasks) {
                            String taskId = taskModel.getTaskId();
                            List<JobModel> taskJobs =
                                    jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            if (taskJobs != null && !taskJobs.isEmpty()) {
                                for (JobModel jobModel : taskJobs) {
                                    JobPK jobPK = new JobPK();
                                    jobPK.setJobId(jobModel.getJobId());
                                    jobPK.setTaskId(taskId);
                                    JobStatus status = jobStatusRepository.getJobStatus(jobPK);
                                    if (status != null) {
                                        jobStatus.put(jobModel.getJobId(), status);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            logger.debug("Airavata retrieved job statuses for experiment with experiment id : " + airavataExperimentId);
            return jobStatus;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving the job statuses: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryServiceException {
        try {
            if (ExpCatChildDataType.PROCESS_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                processOutputRepository.addProcessOutputs(outputs, id);
            } else if (ExpCatChildDataType.EXPERIMENT_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                experimentOutputRepository.addExperimentOutputs(outputs, id);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while adding outputs: outputType=%s, id=%s", outputType, id);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryServiceException {
        try {
            if (ExpCatChildDataType.EXPERIMENT_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentErrorRepository.addExperimentError(errorModel, id);
            } else if (ExpCatChildDataType.TASK_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                taskErrorRepository.addTaskError(errorModel, id);
            } else if (ExpCatChildDataType.PROCESS_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                processErrorRepository.addProcessError(errorModel, id);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while adding errors: errorType=%s, id=%s", errorType, id);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryServiceException {
        try {
            taskStatusRepository.addTaskStatus(taskStatus, taskId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding task status: taskId=%s", taskId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        try {
            processStatusRepository.addProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while adding process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        try {
            processStatusRepository.updateProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating process status: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryServiceException {
        try {
            experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating experiment status: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryServiceException {
        try {
            JobPK jobPK = new JobPK();
            jobPK.setJobId(jobId);
            jobPK.setTaskId(taskId);
            jobStatusRepository.addJobStatus(jobStatus, jobPK);
        } catch (RegistryException e) {
            String message = String.format("Error while adding job status: taskId=%s, jobId=%s", taskId, jobId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void deleteJobs(String processId) throws RegistryServiceException {
        try {
            List<JobModel> jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, processId);
            for (JobModel jobModel : jobs) {
                jobRepository.removeJob(jobModel);
            }
        } catch (RegistryException e) {
            String message = String.format("Error while deleting jobs: processId=%s", processId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Project operations
    public String createProject(String gatewayId, Project project) throws RegistryServiceException {
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
            String projectId = projectRepository.addProject(project, gatewayId);
            return projectId;
        } catch (RegistryException e) {
            String message = String.format("Error while creating project: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateProject(String projectId, Project updatedProject) throws RegistryServiceException {
        try {
            if (!validateString(projectId)) {
                logger.error("Project id cannot be empty...");
                throw new RegistryException("Project id cannot be empty...");
            }
            if (!projectRepository.isProjectExist(projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            projectRepository.updateProject(updatedProject, projectId);
            logger.debug("Airavata updated project with project Id : " + projectId);
        } catch (RegistryException e) {
            String message = String.format("Error while updating project: projectId=%s", projectId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException {
        try {
            if (!validateString(userName)) {
                logger.error("Username cannot be empty. Please provide a valid user..");
                throw new RegistryException("Username cannot be empty. Please provide a valid user..");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            if (!userRepository.isUserExists(gatewayId, userName)) {
                logger.error("User does not exist in the system. Please provide a valid user..");
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            List<Project> projects = new ArrayList<>();
            Map<String, String> regFilters = new HashMap<>();
            regFilters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            for (Map.Entry<ProjectSearchFields, String> entry : filters.entrySet()) {
                if (entry.getKey().equals(ProjectSearchFields.PROJECT_NAME)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, entry.getValue());
                } else if (entry.getKey().equals(ProjectSearchFields.PROJECT_DESCRIPTION)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, entry.getValue());
                }
            }

            try {
                if (accessibleProjIds.size() == 0 && !properties.getSharing().isEnabled()) {
                    if (!regFilters.containsKey(DBConstants.Project.OWNER)) {
                        regFilters.put(DBConstants.Project.OWNER, userName);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error checking sharing settings, continuing without filter", e);
            }

            projects = projectRepository.searchAllAccessibleProjects(
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
            throw new RegistryServiceException(message);
        }
    }

    // Gateway Resource Profile operations
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException {
        try {
            if (!validateString(gatewayResourceProfile.getGatewayID())) {
                logger.error("Cannot create gateway profile with empty gateway id");
                throw new AppCatalogException("Cannot create gateway profile with empty gateway id");
            }
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            String resourceProfile = gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug(
                    "Airavata registered gateway profile with gateway id : " + gatewayResourceProfile.getGatewayID());
            return resourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while registering gateway resource profile: gatewayID=%s",
                    gatewayResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            gwyResourceProfileRepository.updateGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + gatewayID);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while updating gateway resource profile: gatewayID=%s", gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!(gwyResourceProfileRepository.isExists(gatewayID))) {
                throw new AppCatalogException("Gateway resource profile '" + gatewayID + "' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            profile.addToComputeResourcePreferences(computeResourcePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added gateway compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            List<ComputeResourcePreference> computeResourcePreferences = profile.getComputeResourcePreferences();
            ComputeResourcePreference preferenceToRemove = null;
            for (ComputeResourcePreference preference : computeResourcePreferences) {
                if (preference.getComputeResourceId().equals(computeResourceId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getComputeResourcePreferences().remove(preferenceToRemove);
            }
            profile.getComputeResourcePreferences().add(computeResourcePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating gateway compute resource preference: gatewayID=%s, computeResourceId=%s",
                    gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            if (!(gwyResourceProfileRepository.isExists(gatewayID))) {
                throw new AppCatalogException("Gateway resource profile '" + gatewayID + "' does not exist!!!");
            }
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            profile.addToStoragePreferences(dataStoragePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding gateway storage preference: gatewayID=%s, storageResourceId=%s",
                    gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            List<StoragePreference> dataStoragePreferences = profile.getStoragePreferences();
            StoragePreference preferenceToRemove = null;
            for (StoragePreference preference : dataStoragePreferences) {
                if (preference.getStorageResourceId().equals(storageId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getStoragePreferences().remove(preferenceToRemove);
            }
            profile.getStoragePreferences().add(storagePreference);
            gwyResourceProfileRepository.updateGatewayResourceProfile(profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating gateway storage preference: gatewayID=%s, storageId=%s",
                    gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Compute Resource operations
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException {
        try {
            String computeResource = new ComputeResourceRepository().addComputeResource(computeResourceDescription);
            logger.debug("Airavata registered compute resource with compute resource Id : " + computeResource);
            return computeResource;
        } catch (AppCatalogException e) {
            String message = "Error while registering compute resource";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().updateComputeResource(computeResourceId, computeResourceDescription);
            logger.debug("Airavata updated compute resource with compute resource Id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating compute resource: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws RegistryServiceException {
        try {
            return new ComputeResourceRepository().addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            String message = "Error while registering resource job manager";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating resource job manager: resourceJobManagerId=%s", resourceJobManagerId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws RegistryServiceException {
        try {
            switch (dmType) {
                case COMPUTE_RESOURCE:
                    new ComputeResourceRepository().removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug(
                            "Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                case STORAGE_RESOURCE:
                    storageResourceRepository.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    logger.debug(
                            "Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                    return true;
                default:
                    logger.error(
                            "Unsupported data movement type specifies.. Please provide the correct data movement type... ");
                    return false;
            }
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting data movement interface: resourceId=%s, dataMovementInterfaceId=%s",
                    resourceId, dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws RegistryServiceException {
        try {
            throw new AppCatalogException("updateGridFTPDataMovementDetails is not yet implemented");
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating GridFTP data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String addDataMovementInterface = addDataMovementInterface(
                    computeResourceRepository,
                    computeResourceId,
                    dmType,
                    computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement),
                    DataMovementProtocol.GridFTP,
                    priorityOrder);
            logger.debug("Airavata registered GridFTP data movement for resource Id: " + computeResourceId);
            return addDataMovementInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding GridFTP data movement details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws RegistryServiceException {
        try {
            throw new AppCatalogException("updateUnicoreDataMovementDetails is not yet implemented");
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating Unicore data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(
                    computeResourceRepository,
                    resourceId,
                    dmType,
                    computeResourceRepository.addUnicoreDataMovement(unicoreDataMovement),
                    DataMovementProtocol.UNICORE_STORAGE_SERVICE,
                    priorityOrder);
            logger.debug("Airavata registered UNICORE data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while adding Unicore data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().updateScpDataMovement(scpDataMovement);
            logger.debug("Airavata updated SCP data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating SCP data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(
                    computeResourceRepository,
                    resourceId,
                    dmType,
                    computeResourceRepository.addScpDataMovement(scpDataMovement),
                    DataMovementProtocol.SCP,
                    priorityOrder);
            logger.debug("Airavata registered SCP data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message = String.format("Error while adding SCP data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().updateLocalDataMovement(localDataMovement);
            logger.debug("Airavata updated local data movement with data movement id: " + dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating local data movement details: dataMovementInterfaceId=%s",
                    dataMovementInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String movementInterface = addDataMovementInterface(
                    computeResourceRepository,
                    resourceId,
                    dataMoveType,
                    computeResourceRepository.addLocalDataMovement(localDataMovement),
                    DataMovementProtocol.LOCAL,
                    priorityOrder);
            logger.debug("Airavata registered local data movement for resource Id: " + resourceId);
            return movementInterface;
        } catch (AppCatalogException e) {
            String message = String.format("Error while adding local data movement details: resourceId=%s", resourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Storage Resource operations
    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException {
        try {
            String storageResource = storageResourceRepository.addStorageResource(storageResourceDescription);
            logger.debug("Airavata registered storage resource with storage resource Id : " + storageResource);
            return storageResource;
        } catch (AppCatalogException e) {
            String message = "Error while registering storage resource";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException {
        try {
            storageResourceRepository.updateStorageResource(storageResourceId, storageResourceDescription);
            logger.debug("Airavata updated storage resource with storage resource Id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating storage resource: storageResourceId=%s", storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Helper methods for job submission and data movement interfaces
    private String addJobSubmissionInterface(
            ComputeResourceRepository computeResourceRepository,
            String computeResourceId,
            String jobSubmissionInterfaceId,
            JobSubmissionProtocol protocolType,
            int priorityOrder)
            throws RegistryServiceException {
        try {
            JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
            jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            jobSubmissionInterface.setPriorityOrder(priorityOrder);
            jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
            return computeResourceRepository.addJobSubmissionProtocol(computeResourceId, jobSubmissionInterface);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding job submission interface: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    private String addDataMovementInterface(
            ComputeResourceRepository computeResourceRepository,
            String computeResourceId,
            DMType dmType,
            String dataMovementInterfaceId,
            DataMovementProtocol protocolType,
            int priorityOrder)
            throws RegistryServiceException {
        try {
            DataMovementInterface dataMovementInterface = new DataMovementInterface();
            dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
            dataMovementInterface.setPriorityOrder(priorityOrder);
            dataMovementInterface.setDataMovementProtocol(protocolType);
            if (dmType.equals(DMType.COMPUTE_RESOURCE)) {
                return computeResourceRepository.addDataMovementProtocol(
                        computeResourceId, dmType, dataMovementInterface);
            } else if (dmType.equals(DMType.STORAGE_RESOURCE)) {
                dataMovementInterface.setStorageResourceId(computeResourceId);
                return storageResourceRepository.addDataMovementInterface(dataMovementInterface);
            }
            return null;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding data movement interface: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Job Submission Interface operations
    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceRepository,
                    computeResourceId,
                    computeResourceRepository.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH,
                    priorityOrder);
            logger.debug("Airavata registered SSH job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding SSH job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionDetails = addJobSubmissionInterface(
                    computeResourceRepository,
                    computeResourceId,
                    computeResourceRepository.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH_FORK,
                    priorityOrder);
            logger.debug("Airavata registered Fork job submission for compute resource id: " + computeResourceId);
            return submissionDetails;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding SSH Fork job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceRepository,
                    computeResourceId,
                    computeResourceRepository.addLocalJobSubmission(localSubmission),
                    JobSubmissionProtocol.LOCAL,
                    priorityOrder);
            logger.debug("Airavata added local job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding local submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws RegistryServiceException {
        try {
            new ComputeResourceRepository().updateLocalJobSubmission(localSubmission);
            logger.debug("Airavata updated local job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating local submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceRepository,
                    computeResourceId,
                    computeResourceRepository.addCloudJobSubmission(cloudSubmission),
                    JobSubmissionProtocol.CLOUD,
                    priorityOrder);
            logger.debug("Airavata registered Cloud job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding cloud job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException {
        try {
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            String submissionInterface = addJobSubmissionInterface(
                    computeResourceRepository,
                    computeResourceId,
                    computeResourceRepository.addUNICOREJobSubmission(unicoreJobSubmission),
                    JobSubmissionProtocol.UNICORE,
                    priorityOrder);
            logger.debug("Airavata registered UNICORE job submission for compute resource id: " + computeResourceId);
            return submissionInterface;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding UNICORE job submission details: computeResourceId=%s", computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Application Interface/Module/Deployment operations
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String interfaceId =
                    applicationInterfaceRepository.addApplicationInterface(applicationInterface, gatewayId);
            logger.debug("Airavata registered application interface for gateway id : " + gatewayId);
            return interfaceId;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application interface: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException {
        try {
            applicationInterfaceRepository.updateApplicationInterface(appInterfaceId, applicationInterface);
            logger.debug("Airavata updated application interface with interface id : " + appInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating application interface: appInterfaceId=%s", appInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String module = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
            logger.debug("Airavata registered application module for gateway id : " + gatewayId);
            return module;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application module: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws RegistryServiceException {
        try {
            applicationInterfaceRepository.updateApplicationModule(appModuleId, applicationModule);
            logger.debug("Airavata updated application module with module id: " + appModuleId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format("Error while updating application module: appModuleId=%s", appModuleId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws RegistryServiceException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            String deployment =
                    applicationDeploymentRepository.addApplicationDeployment(applicationDeployment, gatewayId);
            logger.debug("Airavata registered application deployment for gateway id : " + gatewayId);
            return deployment;
        } catch (AppCatalogException e) {
            String message = String.format("Error while registering application deployment: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws RegistryServiceException {
        try {
            applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            logger.debug("Airavata updated application deployment for deployment id : " + appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            String message =
                    String.format("Error while updating application deployment: appDeploymentId=%s", appDeploymentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // User Resource Profile operations
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws RegistryServiceException {
        try {
            if (!validateString(userResourceProfile.getUserId())) {
                logger.error("Cannot create user resource profile with empty user id");
                throw new AppCatalogException("Cannot create user resource profile with empty user id");
            }
            if (!validateString(userResourceProfile.getGatewayID())) {
                logger.error("Cannot create user resource profile with empty gateway id");
                throw new AppCatalogException("Cannot create user resource profile with empty gateway id");
            }
            if (!userRepository.isUserExists(userResourceProfile.getGatewayID(), userResourceProfile.getUserId())) {
                logger.error("User does not exist.Please provide a valid user ID...");
                throw new AppCatalogException("User does not exist.Please provide a valid user ID...");
            }
            String resourceProfile = userResourceProfileRepository.addUserResourceProfile(userResourceProfile);
            logger.debug("Airavata registered user resource profile with gateway id : "
                    + userResourceProfile.getGatewayID() + "and user id : " + userResourceProfile.getUserId());
            return resourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while registering user resource profile: userId=%s, gatewayID=%s",
                    userResourceProfile.getUserId(), userResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while registering user resource profile: userId=%s, gatewayID=%s",
                    userResourceProfile.getUserId(), userResourceProfile.getGatewayID());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws RegistryServiceException {
        boolean userExists;
        try {
            userExists = userRepository.isUserExists(gatewayId, userId);
        } catch (RegistryException e) {
            String message = String.format("User '%s' does not exist in gateway '%s'.", userId, gatewayId);
            logger.error(message);
            throw new RegistryServiceException(message);
        }
        if (!userExists) {
            String message = String.format("User '%s' does not exist in gateway '%s'.", userId, gatewayId);
            logger.error(message);
            throw new RegistryServiceException(message);
        }
        try {
            return userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayId);
        } catch (AppCatalogException e) {
            String message = String.format(
                    "User resource profile with user id '%s' and gateway id '%s' does not exist.", userId, gatewayId);
            logger.error(message);
            throw new RegistryServiceException(message);
        }
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayId, userId)) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("user does not exist.Please provide a valid gateway id...");
            }
            UserResourceProfile userResourceProfile =
                    userResourceProfileRepository.getUserResourceProfile(userId, gatewayId);
            logger.debug("Airavata retrieved User resource profile with user id : " + userId);
            return userResourceProfile;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user resource profile: userId=%s, gatewayId=%s", userId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user resource profile: userId=%s, gatewayId=%s", userId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
            logger.debug("Airavata updated gateway profile with gateway id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.removeUserResourceProfile(userId, gatewayID);
            logger.debug("Airavata deleted User profile with gateway id : " + gatewayID + " and user id : " + userId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while deleting user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while deleting user resource profile: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Resource Scheduling operations
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryServiceException {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                logger.debug(
                        airavataExperimentId,
                        "Update resource scheduling failed, experiment {} doesn't exist.",
                        airavataExperimentId);
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null) {
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState) {
                    case CREATED:
                    case VALIDATED:
                    case CANCELED:
                    case FAILED:
                        processRepository.addProcessResourceSchedule(resourceScheduling, airavataExperimentId);
                        logger.debug(
                                airavataExperimentId,
                                "Successfully updated resource scheduling for the experiment {}.",
                                airavataExperimentId);
                        break;
                    default:
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
        } catch (RegistryException e) {
            String message =
                    String.format("Error while updating resource scheduling: experimentId=%s", airavataExperimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // User operations
    public String addUser(UserProfile userProfile) throws RegistryServiceException {
        try {
            logger.info("Adding User in Registry: " + userProfile);
            if (userRepository.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                throw new RegistryException("User already exists, with userId: " + userProfile.getUserId()
                        + ", and gatewayId: " + userProfile.getGatewayId());
            }
            UserProfile savedUser = userRepository.addUser(userProfile);
            return savedUser.getUserId();
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user: userId=%s, gatewayId=%s",
                    userProfile.getUserId(), userProfile.getGatewayId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // User Compute/Storage Preference operations
    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            profile.addToUserComputeResourcePreferences(userComputeResourcePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added User compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            if (userRepository.isUserExists(gatewayID, userId)
                    && userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                return userResourceProfileRepository.isUserComputeResourcePreferenceExists(
                        userId, gatewayID, computeResourceId);
            }
            return false;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while checking if user compute resource preference exists: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while checking if user compute resource preference exists: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!computeResourceRepository.isComputeResourceExists(userComputeResourceId)) {
                logger.error(
                        userComputeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new AppCatalogException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            UserComputeResourcePreference userComputeResourcePreference =
                    userResourceProfileRepository.getUserComputeResourcePreference(
                            userId, gatewayID, userComputeResourceId);
            logger.debug("Airavata retrieved user compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + userComputeResourceId);
            return userComputeResourcePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user compute resource preference: userId=%s, gatewayID=%s, userComputeResourceId=%s",
                    userId, gatewayID, userComputeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user compute resource preference: userId=%s, gatewayID=%s, userComputeResourceId=%s",
                    userId, gatewayID, userComputeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            List<UserComputeResourcePreference> userComputeResourcePreferences =
                    profile.getUserComputeResourcePreferences();
            UserComputeResourcePreference preferenceToRemove = null;
            for (UserComputeResourcePreference preference : userComputeResourcePreferences) {
                if (preference.getComputeResourceId().equals(computeResourceId)) {
                    preferenceToRemove = preference;
                    break;
                }
            }
            if (preferenceToRemove != null) {
                profile.getUserComputeResourcePreferences().remove(preferenceToRemove);
            }
            profile.getUserComputeResourcePreferences().add(userComputeResourcePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated compute resource preference with gateway id : " + gatewayID
                    + " and for compute resource id : " + computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            dataStoragePreference.setStorageResourceId(storageResourceId);
            profile.addToUserStoragePreferences(dataStoragePreference);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while adding user storage preference: userId=%s, gatewayID=%s, storageResourceId=%s",
                    userId, gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while adding user storage preference: userId=%s, gatewayID=%s, storageResourceId=%s",
                    userId, gatewayID, storageResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }

            UserStoragePreference storagePreference =
                    userResourceProfileRepository.getUserStoragePreference(userId, gatewayID, storageId);
            logger.debug("Airavata retrieved user storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return storagePreference;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while getting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryServiceException {
        try {
            return userResourceProfileRepository.getAllUserResourceProfiles();
        } catch (AppCatalogException e) {
            String message = "Error while getting all user resource profiles";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Gateway Groups operations
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryServiceException {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayId)) {
                final String message = "No GatewayGroups entry exists for " + gatewayId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return gatewayGroupsRepository.get(gatewayId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting gateway groups: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Parser operations
    public Parser getParser(String parserId, String gatewayId) throws RegistryServiceException {
        try {
            if (!parserRepository.isExists(parserId)) {
                final String message = "No Parser Info entry exists for " + parserId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parserRepository.get(parserId);
        } catch (RegistryException e) {
            String message = String.format("Error while getting parser: parserId=%s", parserId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String saveParser(Parser parser) throws RegistryServiceException {
        try {
            Parser saved = parserRepository.saveParser(parser);
            return saved.getId();
        } catch (AppCatalogException e) {
            String message = "Error while saving parser";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void removeParser(String parserId, String gatewayId) throws RegistryServiceException {
        try {
            boolean exists = parserRepository.isExists(parserId);
            if (!exists || gatewayId.equals(parserRepository.get(parserId).getGatewayId())) {
                throw new RegistryException("Parser " + parserId + " does not exist");
            }
            parserRepository.delete(parserId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while removing parser: parserId=%s, gatewayId=%s", parserId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ParserInput getParserInput(String parserInputId, String gatewayId) throws RegistryServiceException {
        if (!parserInputRepository.isExists(parserInputId)) {
            final String message = "No ParserInput entry exists for " + parserInputId;
            logger.error(message);
            throw new RegistryServiceException(message);
        }
        return parserInputRepository.get(parserInputId);
    }

    public String saveParserInput(ParserInput parserInput) throws RegistryServiceException {
        ParserInput saved = parserInputRepository.create(parserInput);
        return saved.getId();
    }

    public void removeParserInput(String parserInputId, String gatewayId) throws RegistryServiceException {
        try {
            boolean exists = parserInputRepository.isExists(parserInputId);
            if (!exists) {
                throw new RegistryException("ParserInput " + parserInputId + " does not exist");
            }
            ParserInput parserInput = parserInputRepository.get(parserInputId);
            Parser parser = parserRepository.get(parserInput.getParserId());
            if (!gatewayId.equals(parser.getGatewayId())) {
                throw new RegistryException(
                        "ParserInput " + parserInputId + " does not belong to gateway " + gatewayId);
            }
            parserInputRepository.delete(parserInputId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error in removeParserInput: parserInputId=%s, gatewayId=%s", parserInputId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws RegistryServiceException {
        try {
            if (!parserOutputRepository.isExists(parserOutputId)) {
                final String message = "No ParserOutput entry exists for " + parserOutputId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parserOutputRepository.get(parserOutputId);
        } catch (RegistryException e) {
            String message = String.format("Error in getParserOutput: parserOutputId=%s", parserOutputId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String saveParserOutput(ParserOutput parserOutput) throws RegistryServiceException {
        ParserOutput saved = parserOutputRepository.create(parserOutput);
        return saved.getId();
    }

    public void removeParserOutput(String parserOutputId, String gatewayId) throws RegistryServiceException {
        try {
            boolean exists = parserOutputRepository.isExists(parserOutputId);
            if (!exists) {
                throw new RegistryException("ParserOutput " + parserOutputId + " does not exist");
            }
            ParserOutput parserOutput = parserOutputRepository.get(parserOutputId);
            Parser parser = parserRepository.get(parserOutput.getParserId());
            if (!gatewayId.equals(parser.getGatewayId())) {
                throw new RegistryException(
                        "ParserOutput " + parserOutputId + " does not belong to gateway " + gatewayId);
            }
            parserOutputRepository.delete(parserOutputId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error in removeParserOutput: parserOutputId=%s, gatewayId=%s", parserOutputId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        try {
            if (!parsingTemplateRepository.isExists(templateId)) {
                final String message = "No ParsingTemplate entry exists for " + templateId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return parsingTemplateRepository.get(templateId);
        } catch (RegistryException e) {
            String message = String.format("Error in getParsingTemplate: templateId=%s", templateId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryServiceException {
        ParsingTemplate saved = parsingTemplateRepository.create(parsingTemplate);
        return saved.getId();
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        try {
            boolean exists = parsingTemplateRepository.isExists(templateId);
            if (!exists
                    || gatewayId.equals(
                            parsingTemplateRepository.get(templateId).getGatewayId())) {
                throw new RegistryException("Parsing template " + templateId + " does not exist");
            }
            parsingTemplateRepository.delete(templateId);
        } catch (RegistryException e) {
            String message =
                    String.format("Error in removeParsingTemplate: templateId=%s, gatewayId=%s", templateId, gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    // Gateway Usage Reporting operations
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            return usageReportingCommandRepository.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while checking if gateway usage reporting is available: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            if (!usageReportingCommandRepository.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId)) {
                String message = "No usage reporting information for the gateway " + gatewayId
                        + " and compute resource " + computeResourceId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return usageReportingCommandRepository.getGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while getting gateway reporting command: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryServiceException {
        try {
            usageReportingCommandRepository.addGatewayUsageReportingCommand(command);
        } catch (RegistryException e) {
            String message = "Error while adding gateway usage reporting command";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        try {
            usageReportingCommandRepository.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while removing gateway usage reporting command: gatewayId=%s, computeResourceId=%s",
                    gatewayId, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws RegistryServiceException {
        try {
            cloudJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceRepository.updateCloudJobSubmission(cloudJobSubmission);
            logger.debug("Airavata updated Cloud job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating cloud job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException {
        try {
            sshJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceRepository.updateSSHJobSubmission(sshJobSubmission);
            logger.debug(
                    "Airavata updated SSH job submission for job submission interface id: " + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating SSH job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException {
        try {
            unicoreJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            computeResourceRepository.updateUNICOREJobSubmission(unicoreJobSubmission);
            logger.debug("Airavata updated UNICORE job submission for job submission interface id: "
                    + jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            String message = String.format(
                    "Error while updating Unicore job submission details: jobSubmissionInterfaceId=%s",
                    jobSubmissionInterfaceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateNotification(Notification notification) throws RegistryServiceException {
        try {
            notificationRepository.updateNotification(notification);
            logger.debug("Airavata updated notification with notification id: " + notification.getNotificationId());
            return true;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while updating notification: notificationId=%s", notification.getNotificationId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String createNotification(Notification notification) throws RegistryServiceException {
        try {
            String notificationId = notificationRepository.createNotification(notification);
            logger.debug("Airavata created notification with notification id: " + notificationId);
            return notificationId;
        } catch (RegistryException e) {
            String message = "Error while creating notification";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryServiceException {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            updatedGateway.setGatewayId(gatewayId);
            gatewayRepository.updateGateway(gatewayId, updatedGateway);
            logger.debug("Airavata updated gateway with gateway id: " + gatewayId);
            return true;
        } catch (RegistryException e) {
            String message = String.format("Error while updating gateway: gatewayId=%s", gatewayId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public String addGateway(Gateway gateway) throws RegistryServiceException {
        try {
            if (gatewayRepository.isGatewayExist(gateway.getGatewayId())) {
                logger.error("Gateway already exists in the system. Please provide a different gateway ID...");
                throw new AppCatalogException(
                        "Gateway already exists in the system. Please provide a different gateway ID...");
            }
            String gatewayId = gatewayRepository.addGateway(gateway);
            logger.debug("Airavata registered gateway with gateway id: " + gatewayId);
            return gatewayId;
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format("Error while adding gateway: gatewayId=%s", gateway.getGatewayId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            List<UserStoragePreference> userStoragePreferences = profile.getUserStoragePreferences();
            UserStoragePreference preferenceToRemove = null;
            for (UserStoragePreference preference : userStoragePreferences) {
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
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            logger.debug("Airavata updated storage resource preference with gateway id : " + gatewayID
                    + " and for storage resource id : " + storageId);
            return true;
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while updating user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(
                    userId, gatewayID, computeResourceId);
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while deleting user compute resource preference: userId=%s, gatewayID=%s, computeResourceId=%s",
                    userId, gatewayID, computeResourceId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileRepository.removeUserDataStoragePreferenceFromGateway(
                    userId, gatewayID, storageId);
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while deleting user storage preference: userId=%s, gatewayID=%s, storageId=%s",
                    userId, gatewayID, storageId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryServiceException {
        try {
            return queueStatusRepository.getLatestQueueStatuses();
        } catch (RegistryException e) {
            String message = "Error while retrieving latest queue statuses";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryServiceException {
        try {
            queueStatusRepository.createQueueStatuses(queueStatuses);
        } catch (RegistryException e) {
            String message = "Error while registering queue statuses";
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryServiceException {
        try {
            Optional<QueueStatusModel> optionalQueueStatusModel =
                    queueStatusRepository.getQueueStatus(hostName, queueName);
            if (optionalQueueStatusModel.isPresent()) {
                return optionalQueueStatusModel.get();
            } else {
                QueueStatusModel queueStatusModel = new QueueStatusModel();
                queueStatusModel.setHostName(hostName);
                queueStatusModel.setQueueName(queueName);
                queueStatusModel.setQueueUp(false);
                queueStatusModel.setRunningJobs(0);
                queueStatusModel.setQueuedJobs(0);
                queueStatusModel.setTime(0);
                return queueStatusModel;
            }
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving queue status: hostName=%s, queueName=%s", hostName, queueName);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void createGatewayGroups(GatewayGroups gatewayGroups) throws RegistryServiceException {
        try {
            if (gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
                logger.error("GatewayGroups already exists for " + gatewayGroups.getGatewayId());
                throw new RegistryException(
                        "GatewayGroups for gatewayId: " + gatewayGroups.getGatewayId() + " already exists.");
            }
            gatewayGroupsRepository.create(gatewayGroups);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while creating gateway groups: gatewayId=%s", gatewayGroups.getGatewayId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryServiceException {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
                final String message = "No GatewayGroups entry exists for " + gatewayGroups.getGatewayId();
                logger.error(message);
                throw new RegistryException(message);
            }
            gatewayGroupsRepository.update(gatewayGroups);
        } catch (RegistryException e) {
            String message =
                    String.format("Error while updating gateway groups: gatewayId=%s", gatewayGroups.getGatewayId());
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryServiceException {
        return gatewayGroupsRepository.isExists(gatewayId);
    }

    public List<Parser> listAllParsers(String gatewayId) throws RegistryServiceException {
        return parserRepository.getAllParsers(gatewayId);
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId)
            throws RegistryServiceException {
        return parsingTemplateRepository.getParsingTemplatesForApplication(applicationInterfaceId);
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryServiceException {
        try {
            ExperimentModel experiment = experimentRepository.getExperiment(experimentId);
            List<ProcessModel> processes = experiment.getProcesses();
            if (processes != null && processes.size() > 0) {
                return parsingTemplateRepository.getParsingTemplatesForApplication(
                        processes.get(processes.size() - 1).getApplicationInterfaceId());
            }
            return Collections.emptyList();
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while retrieving parsing templates for experiment: experimentId=%s", experimentId);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryServiceException {
        return parsingTemplateRepository.getAllParsingTemplates(gatewayId);
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User Resource Profile does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "User Resource Profile does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileRepository
                    .getUserResourceProfile(userId, gatewayID)
                    .getUserComputeResourcePreferences();
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while retrieving all user compute resource preferences: userId=%s, gatewayID=%s",
                    userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws RegistryServiceException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileRepository
                    .getUserResourceProfile(userId, gatewayID)
                    .getUserStoragePreferences();
        } catch (AppCatalogException | RegistryException e) {
            String message = String.format(
                    "Error while retrieving all user storage preferences: userId=%s, gatewayID=%s", userId, gatewayID);
            logger.error(message, e);
            throw new RegistryServiceException(message);
        }
    }
}
