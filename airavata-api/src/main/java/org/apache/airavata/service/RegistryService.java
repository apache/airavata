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
import org.apache.airavata.common.utils.ServerSettings;
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
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryService {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    private RegistryServiceException convertException(Throwable e, String msg) {
        logger.error(msg, e);
        RegistryServiceException exception = new RegistryServiceException();
        exception.setMessage(msg + " More info : " + e.getMessage());
        return exception;
    }

    private ApplicationDeploymentRepository applicationDeploymentRepository = new ApplicationDeploymentRepository();
    private ApplicationInterfaceRepository applicationInterfaceRepository = new ApplicationInterfaceRepository();
    private StorageResourceRepository storageResourceRepository = new StorageResourceRepository();
    private UserResourceProfileRepository userResourceProfileRepository = new UserResourceProfileRepository();
    private GatewayRepository gatewayRepository = new GatewayRepository();
    private ProjectRepository projectRepository = new ProjectRepository();
    private NotificationRepository notificationRepository = new NotificationRepository();
    private ExperimentSummaryRepository experimentSummaryRepository = new ExperimentSummaryRepository();
    private ExperimentRepository experimentRepository = new ExperimentRepository();
    private ExperimentOutputRepository experimentOutputRepository = new ExperimentOutputRepository();
    private ExperimentStatusRepository experimentStatusRepository = new ExperimentStatusRepository();
    private ExperimentErrorRepository experimentErrorRepository = new ExperimentErrorRepository();
    private ProcessRepository processRepository = new ProcessRepository();
    private ProcessOutputRepository processOutputRepository = new ProcessOutputRepository();
    private ProcessWorkflowRepository processWorkflowRepository = new ProcessWorkflowRepository();
    private ProcessStatusRepository processStatusRepository = new ProcessStatusRepository();
    private ProcessErrorRepository processErrorRepository = new ProcessErrorRepository();
    private TaskRepository taskRepository = new TaskRepository();
    private TaskStatusRepository taskStatusRepository = new TaskStatusRepository();
    private TaskErrorRepository taskErrorRepository = new TaskErrorRepository();
    private JobRepository jobRepository = new JobRepository();
    private JobStatusRepository jobStatusRepository = new JobStatusRepository();
    private QueueStatusRepository queueStatusRepository = new QueueStatusRepository();
    private DataProductRepository dataProductRepository = new DataProductRepository();
    private DataReplicaLocationRepository dataReplicaLocationRepository = new DataReplicaLocationRepository();
    private WorkflowRepository workflowRepository = new WorkflowRepository();
    private GatewayGroupsRepository gatewayGroupsRepository = new GatewayGroupsRepository();
    private ParserRepository parserRepository = new ParserRepository();
    private ParserInputRepository parserInputRepository = new ParserInputRepository();
    private ParserOutputRepository parserOutputRepository = new ParserOutputRepository();
    private ParsingTemplateRepository parsingTemplateRepository = new ParsingTemplateRepository();
    private UserRepository userRepository = new UserRepository();
    private ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
    private GatewayUsageReportingCommandRepository usageReportingCommandRepository =
            new GatewayUsageReportingCommandRepository();

    public String getAPIVersion() {
        return org.apache.airavata.registry.api.registry_apiConstants.REGISTRY_API_VERSION;
    }

    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException {
        try {
            return userRepository.isUserExists(gatewayId, userName);
        } catch (Throwable e) {
            throw convertException(e, "Error while verifying user");
        }
    }

    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException {
        try {
            return userRepository.getAllUsernamesInGateway(gatewayId);
        } catch (Throwable e) {
            throw convertException(e, "Error while retrieving users");
        }
    }

    public Gateway getGateway(String gatewayId) throws RegistryServiceException {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException("Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            Gateway gateway = gatewayRepository.getGateway(gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : " + gateway.getGatewayId());
            return gateway;
        } catch (Throwable e) {
            throw convertException(e, "Error while getting the gateway");
        }
    }

    public boolean deleteGateway(String gatewayId) throws RegistryServiceException {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException("Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            gatewayRepository.removeGateway(gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : " + gatewayId);
            return true;
        } catch (Throwable e) {
            throw convertException(e, "Error while deleting the gateway");
        }
    }

    public List<Gateway> getAllGateways() throws RegistryException {
        List<Gateway> gateways = gatewayRepository.getAllGateways();
        logger.debug("Airavata retrieved all available gateways...");
        return gateways;
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        return gatewayRepository.isGatewayExist(gatewayId);
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryException {
        notificationRepository.deleteNotification(notificationId);
        return true;
    }

    public Notification getNotification(String gatewayId, String notificationId) throws RegistryException {
        return notificationRepository.getNotification(notificationId);
    }

    public List<Notification> getAllNotifications(String gatewayId) throws RegistryException {
        List<Notification> notifications = notificationRepository.getAllGatewayNotifications(gatewayId);
        return notifications;
    }

    public Project getProject(String projectId) throws RegistryException, ProjectNotFoundException {
        if (!projectRepository.isProjectExist(projectId)) {
            logger.error("Project does not exist in the system. Please provide a valid project ID...");
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
            throw exception;
        }
        logger.debug("Airavata retrieved project with project Id : " + projectId);
        Project project = projectRepository.getProject(projectId);
        return project;
    }

    public boolean deleteProject(String projectId) throws RegistryException, ProjectNotFoundException {
        if (!projectRepository.isProjectExist(projectId)) {
            logger.error("Project does not exist in the system. Please provide a valid project ID...");
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
            throw exception;
        }
        projectRepository.removeProject(projectId);
        logger.debug("Airavata deleted project with project Id : " + projectId);
        return true;
    }

    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
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
                filters, limit, offset, Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
        logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
        return projects;
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
        ExperimentStatistics result =
                experimentSummaryRepository.getAccessibleExperimentStatistics(accessibleExpIds, filters, limit, offset);
        logger.debug("Airavata retrieved experiments for gateway id : " + gatewayId + " between : "
                + org.apache.airavata.common.utils.AiravataUtils.getTime(fromTime) + " and "
                + org.apache.airavata.common.utils.AiravataUtils.getTime(toTime));
        return result;
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryException {
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
            throw new RegistryException("Project does not exist in the system. Please provide a valid project ID...");
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
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryException {
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
    }

    public boolean deleteExperiment(String experimentId) throws RegistryException {
        if (!experimentRepository.isExperimentExist(experimentId)) {
            throw new RegistryException("Requested experiment id " + experimentId + " does not exist in the system..");
        }
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        if (!(experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED)) {
            logger.error("Error while deleting the experiment");
            throw new RegistryException("Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
        }
        experimentRepository.removeExperiment(experimentId);
        logger.debug("Airavata removed experiment with experiment id : " + experimentId);
        return true;
    }

    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws RegistryException {
        if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
            throw new RegistryException(
                    "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
        }
        return experimentRepository.getExperiment(airavataExperimentId);
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryException {
        return getExperimentInternal(airavataExperimentId);
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperimentInternal(airavataExperimentId);
        List<ProcessModel> processList = processRepository.getProcessList(
                Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
        if (processList != null) {
            processList.stream().forEach(p -> {
                (p).getTasks().stream().forEach(t -> {
                    try {
                        List<JobModel> jobList = jobRepository.getJobList(
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
    }

    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws RegistryException {
        if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
            logger.error(
                    airavataExperimentId,
                    "Error while retrieving experiment status, experiment {} doesn't exist.",
                    airavataExperimentId);
            throw new RegistryException(
                    "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
        }
        return experimentStatusRepository.getExperimentStatus(airavataExperimentId);
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryException {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : " + airavataExperimentId);
        return experimentStatus;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws RegistryException {
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
    }

    public void updateJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryException {
        org.apache.airavata.registry.core.entities.expcatalog.JobPK jobPK =
                new org.apache.airavata.registry.core.entities.expcatalog.JobPK();
        jobPK.setTaskId(taskId);
        jobPK.setJobId(jobId);
        jobStatusRepository.updateJobStatus(jobStatus, jobPK);
    }

    public void addJob(JobModel jobModel, String processId) throws RegistryException {
        jobRepository.addJob(jobModel, processId);
    }

    // Note: deleteJobs method removed - JobRepository doesn't have this method
    // Jobs should be deleted individually using removeJob(jobPK) or removeJob(jobModel)

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        return processRepository.addProcess(processModel, experimentId);
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        processRepository.updateProcess(processModel, processId);
    }

    public String addTask(TaskModel taskModel, String processId) throws RegistryException {
        return taskRepository.addTask(taskModel, processId);
    }

    public void deleteTasks(String processId) throws RegistryException {
        taskRepository.deleteTasks(processId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        return experimentRepository.getUserConfigurationData(experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        return processRepository.getProcess(processId);
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        List<ProcessModel> processModels = processRepository.getProcessList(
                Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);
        return processModels;
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        return processStatusRepository.getProcessStatus(processId);
    }

    public List<ProcessModel> getProcessListInState(ProcessState processState) throws RegistryException {
        List<ProcessModel> finalProcessList = new ArrayList<>();
        int offset = 0;
        int limit = 100;
        int count = 0;
        do {
            List<ProcessStatus> processStatusList =
                    processStatusRepository.getProcessStatusList(processState, offset, limit);
            offset += processStatusList.size();
            count = processStatusList.size();
            for (ProcessStatus processStatus : processStatusList) {
                ProcessStatus latestStatus = processStatusRepository.getProcessStatus(processStatus.getProcessId());
                if (latestStatus.getState().name().equals(processState.name())) {
                    finalProcessList.add(processRepository.getProcess(latestStatus.getProcessId()));
                }
            }
        } while (count == limit);
        return finalProcessList;
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        return processStatusRepository.getProcessStatusList(processId);
    }

    private JobModel fetchJobModel(String queryType, String id) throws RegistryException {
        if (queryType.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
            List<JobModel> jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
            if (jobs != null) {
                for (JobModel jobModel : jobs) {
                    if (jobModel.getJobId() != null || !jobModel.equals("")) {
                        return jobModel;
                    }
                }
            }
        } else if (queryType.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
            List<JobModel> jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
            if (jobs != null) {
                for (JobModel jobModel : jobs) {
                    if (jobModel.getJobId() != null || !jobModel.equals("")) {
                        return jobModel;
                    }
                }
            }
        }
        return null;
    }

    private List<JobModel> fetchJobModels(String queryType, String id) throws RegistryException {
        List<JobModel> jobs = new ArrayList<>();
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
        }
        return jobs;
    }

    public boolean isJobExist(String queryType, String id) throws RegistryException {
        JobModel jobModel = fetchJobModel(queryType, id);
        return jobModel != null;
    }

    public JobModel getJob(String queryType, String id) throws RegistryException {
        JobModel jobModel = fetchJobModel(queryType, id);
        if (jobModel != null) return jobModel;
        throw new RegistryException("Job not found for queryType: " + queryType + ", id: " + id);
    }

    public List<JobModel> getJobs(String queryType, String id) throws RegistryException {
        return fetchJobModels(queryType, id);
    }

    public int getJobCount(
            org.apache.airavata.model.status.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryException {
        List<JobStatus> jobStatusList = jobStatusRepository.getDistinctListofJobStatus(
                gatewayId, jobStatus.getJobState().name(), searchBackTimeInMinutes);
        return jobStatusList.size();
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryException {
        return processRepository.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        return processOutputRepository.getProcessOutputs(processId);
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException {
        return processWorkflowRepository.getProcessWorkflows(processId);
    }

    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryException {
        processWorkflowRepository.addProcessWorkflow(processWorkflow, processWorkflow.getProcessId());
    }

    public List<String> getProcessIds(String experimentId) throws RegistryException {
        return processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryException {
        if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
            logger.error(
                    airavataExperimentId,
                    "Error while retrieving job details, experiment {} doesn't exist.",
                    airavataExperimentId);
            throw new RegistryException(
                    "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
        }
        List<ProcessModel> processModels = processRepository.getProcessList(
                Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
        List<JobModel> jobList = new ArrayList<>();
        if (processModels != null && !processModels.isEmpty()) {
            for (ProcessModel processModel : processModels) {
                List<TaskModel> tasks = processModel.getTasks();
                if (tasks != null && !tasks.isEmpty()) {
                    for (TaskModel taskModel : tasks) {
                        String taskId = taskModel.getTaskId();
                        List<JobModel> taskJobs =
                                jobRepository.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                        jobList.addAll(taskJobs);
                    }
                }
            }
        }
        logger.debug("Airavata retrieved job models for experiment with experiment id : " + airavataExperimentId);
        return jobList;
    }

    private boolean validateString(String name) {
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0) {
            valid = false;
        }
        return valid;
    }

    public boolean isGatewayExistInternal(String gatewayId) throws RegistryException {
        return gatewayRepository.isGatewayExist(gatewayId);
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws AppCatalogException {
        ApplicationModule module = applicationInterfaceRepository.getApplicationModule(appModuleId);
        logger.debug("Airavata retrieved application module with module id : " + appModuleId);
        return module;
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationModule> moduleList = applicationInterfaceRepository.getAllApplicationModules(gatewayId);
        logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
        return moduleList;
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationModule> moduleList = applicationInterfaceRepository.getAccessibleApplicationModules(
                gatewayId, accessibleAppIds, accessibleComputeResourceIds);
        logger.debug("Airavata retrieved modules for gateway id : " + gatewayId);
        return moduleList;
    }

    public boolean deleteApplicationModule(String appModuleId) throws AppCatalogException {
        logger.debug("Airavata deleted application module with module id : " + appModuleId);
        return applicationInterfaceRepository.removeApplicationModule(appModuleId);
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws AppCatalogException {
        ApplicationDeploymentDescription deployement =
                applicationDeploymentRepository.getApplicationDeployement(appDeploymentId);
        logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
        return deployement;
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws AppCatalogException {
        applicationDeploymentRepository.removeAppDeployment(appDeploymentId);
        logger.debug("Airavata removed application deployment with deployment id : " + appDeploymentId);
        return true;
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationDeploymentDescription> deployements =
                applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
        logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
        return deployements;
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationDeploymentDescription> deployements =
                applicationDeploymentRepository.getAccessibleApplicationDeployments(
                        gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        logger.debug("Airavata retrieved application deployments for gateway id : " + gatewayId);
        return deployements;
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationDeploymentDescription> deployments =
                applicationDeploymentRepository.getAccessibleApplicationDeployments(
                        gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        return deployments;
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws AppCatalogException {
        List<String> appDeployments = new ArrayList<>();
        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
        List<ApplicationDeploymentDescription> applicationDeployments =
                applicationDeploymentRepository.getApplicationDeployments(filters);
        for (ApplicationDeploymentDescription description : applicationDeployments) {
            appDeployments.add(description.getAppDeploymentId());
        }
        logger.debug("Airavata retrieved application deployments for module id : " + appModuleId);
        return appDeployments;
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws AppCatalogException {
        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
        List<ApplicationDeploymentDescription> applicationDeployments =
                applicationDeploymentRepository.getApplicationDeployments(filters);
        return applicationDeployments;
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AppCatalogException {
        ApplicationInterfaceDescription interfaceDescription =
                applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
        logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
        return interfaceDescription;
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws AppCatalogException {
        boolean removeApplicationInterface = applicationInterfaceRepository.removeApplicationInterface(appInterfaceId);
        logger.debug("Airavata removed application interface with interface id : " + appInterfaceId);
        return removeApplicationInterface;
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationInterfaceDescription> allApplicationInterfaces =
                applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
        Map<String, String> allApplicationInterfacesMap = new HashMap<>();
        if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()) {
            for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces) {
                allApplicationInterfacesMap.put(
                        interfaceDescription.getApplicationInterfaceId(), interfaceDescription.getApplicationName());
            }
        }
        logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
        return allApplicationInterfacesMap;
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        List<ApplicationInterfaceDescription> interfaces =
                applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
        logger.debug("Airavata retrieved application interfaces for gateway id : " + gatewayId);
        return interfaces;
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws AppCatalogException {
        List<InputDataObjectType> applicationInputs =
                applicationInterfaceRepository.getApplicationInputs(appInterfaceId);
        logger.debug("Airavata retrieved application inputs for application interface id : " + appInterfaceId);
        return applicationInputs;
    }

    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId) throws AppCatalogException {
        List<OutputDataObjectType> applicationOutputs =
                applicationInterfaceRepository.getApplicationOutputs(appInterfaceId);
        logger.debug("Airavata retrieved application outputs for application interface id : " + appInterfaceId);
        return applicationOutputs;
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AppCatalogException {
        List<OutputDataObjectType> list = getApplicationOutputsInternal(appInterfaceId);
        logger.debug("Airavata retrieved application outputs for app interface id : " + appInterfaceId);
        return list;
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws AppCatalogException {
        Map<String, String> allComputeResources = new ComputeResourceRepository().getAvailableComputeResourceIdList();
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
        logger.debug("Airavata retrieved available compute resources for application interface id : " + appInterfaceId);
        return availableComputeResources;
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws AppCatalogException {
        ComputeResourceDescription computeResource =
                new ComputeResourceRepository().getComputeResource(computeResourceId);
        logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
        return computeResource;
    }

    public Map<String, String> getAllComputeResourceNames() throws AppCatalogException {
        Map<String, String> computeResourceIdList = new ComputeResourceRepository().getAllComputeResourceIdList();
        logger.debug("Airavata retrieved all the available compute resources...");
        return computeResourceIdList;
    }

    public boolean deleteComputeResource(String computeResourceId) throws AppCatalogException {
        new ComputeResourceRepository().removeComputeResource(computeResourceId);
        logger.debug("Airavata deleted compute resource with compute resource Id : " + computeResourceId);
        return true;
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws AppCatalogException {
        StorageResourceDescription storageResource = storageResourceRepository.getStorageResource(storageResourceId);
        logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
        return storageResource;
    }

    public Map<String, String> getAllStorageResourceNames() throws AppCatalogException {
        Map<String, String> resourceIdList = storageResourceRepository.getAllStorageResourceIdList();
        logger.debug("Airavata retrieved storage resources list...");
        return resourceIdList;
    }

    public boolean deleteStorageResource(String storageResourceId) throws AppCatalogException {
        storageResourceRepository.removeStorageResource(storageResourceId);
        logger.debug("Airavata deleted storage resource with storage resource Id : " + storageResourceId);
        return true;
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws AppCatalogException {
        LOCALSubmission localJobSubmission = new ComputeResourceRepository().getLocalJobSubmission(jobSubmissionId);
        logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
        return localJobSubmission;
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws AppCatalogException {
        SSHJobSubmission sshJobSubmission = new ComputeResourceRepository().getSSHJobSubmission(jobSubmissionId);
        logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
        return sshJobSubmission;
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws AppCatalogException {
        UnicoreJobSubmission unicoreJobSubmission =
                new ComputeResourceRepository().getUNICOREJobSubmission(jobSubmissionId);
        logger.debug("Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
        return unicoreJobSubmission;
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws AppCatalogException {
        CloudJobSubmission cloudJobSubmission = new ComputeResourceRepository().getCloudJobSubmission(jobSubmissionId);
        logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
        return cloudJobSubmission;
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
            throws AppCatalogException {
        new ComputeResourceRepository().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        logger.debug("Airavata deleted job submission interface with interface id : " + jobSubmissionInterfaceId);
        return true;
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        return new ComputeResourceRepository().getResourceJobManager(resourceJobManagerId);
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        new ComputeResourceRepository().deleteResourceJobManager(resourceJobManagerId);
        return true;
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        new ComputeResourceRepository().removeBatchQueue(computeResourceId, queueName);
        return true;
    }

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
        logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
        return gatewayResourceProfile;
    }

    public boolean deleteGatewayResourceProfile(String gatewayID) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        gwyResourceProfileRepository.delete(gatewayID);
        logger.debug("Airavata deleted gateway profile with gateway id : " + gatewayID);
        return true;
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)) {
            logger.error(
                    gatewayID,
                    "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            throw new AppCatalogException(
                    "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
        }
        StoragePreference storagePreference = gwyResourceProfileRepository.getStoragePreference(gatewayID, storageId);
        logger.debug("Airavata retrieved storage resource preference with gateway id : " + gatewayID
                + " and for storage resource id : " + storageId);
        return storagePreference;
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getComputeResourcePreferences();
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getStoragePreferences();
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws AppCatalogException {
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.getAllGatewayProfiles();
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        return gwyResourceProfileRepository.removeDataStoragePreferenceFromGateway(gatewayID, storageId);
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryException {
        DataProductModel dataProductModel = dataProductRepository.getDataProduct(productUri);
        return dataProductModel;
    }

    public DataProductModel getParentDataProduct(String productUri) throws RegistryException {
        DataProductModel dataProductModel = dataProductRepository.getParentDataProduct(productUri);
        return dataProductModel;
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryException {
        List<DataProductModel> dataProductModels = dataProductRepository.getChildDataProducts(productUri);
        return dataProductModels;
    }

    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws RegistryException {
        List<DataProductModel> dataProductModels =
                dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
        return dataProductModels;
    }

    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(groupResourceProfile.getGatewayId())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
        logger.debug("New Group Resource Profile Created: " + groupResourceProfileId);
        return groupResourceProfileId;
    }

    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        if (!groupResourceProfileRepository.isGroupResourceProfileExists(
                groupResourceProfile.getGroupResourceProfileId())) {
            logger.error(
                    "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
            throw new AppCatalogException(
                    "Cannot update. No group resource profile found with matching gatewayId and groupResourceProfileId");
        }
        String groupResourceProfileId = groupResourceProfileRepository.updateGroupResourceProfile(groupResourceProfile);
        logger.debug(" Group Resource Profile updated: " + groupResourceProfileId);
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
            logger.error("No group resource profile found with matching gatewayId and groupResourceProfileId");
            throw new AppCatalogException(
                    "No group resource profile found with matching gatewayId and groupResourceProfileId");
        }
        return groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId);
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
            logger.error(
                    "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
            throw new AppCatalogException(
                    "Cannot Remove. No group resource profile found with matching gatewayId and groupResourceProfileId");
        }
        return groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        logger.debug("Removed compute resource preferences with compute resource ID: " + computeResourceId);
        return true;
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeComputeResourcePolicy(resourcePolicyId);
        logger.debug("Removed compute resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        groupResourceProfileRepository.removeBatchQueueResourcePolicy(resourcePolicyId);
        logger.debug("Removed batch resource policy with resource policy ID: " + resourcePolicyId);
        return true;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        GroupComputeResourcePreference groupComputeResourcePreference =
                groupResourceProfileRepository.getGroupComputeResourcePreference(
                        computeResourceId, groupResourceProfileId);
        if (!(groupComputeResourcePreference != null)) {
            logger.error("GroupComputeResourcePreference not found");
            throw new AppCatalogException("GroupComputeResourcePreference not found ");
        }
        return groupComputeResourcePreference;
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.isGroupComputeResourcePreferenceExists(
                computeResourceId, groupResourceProfileId);
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        ComputeResourcePolicy computeResourcePolicy =
                groupResourceProfileRepository.getComputeResourcePolicy(resourcePolicyId);
        if (!(computeResourcePolicy != null)) {
            logger.error("Group Compute Resource policy not found");
            throw new AppCatalogException("Group Compute Resource policy not found ");
        }
        return computeResourcePolicy;
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        BatchQueueResourcePolicy batchQueueResourcePolicy =
                groupResourceProfileRepository.getBatchQueueResourcePolicy(resourcePolicyId);
        if (!(batchQueueResourcePolicy != null)) {
            logger.error("Group Batch Queue Resource policy not found");
            throw new AppCatalogException("Group Batch Queue Resource policy not found ");
        }
        return batchQueueResourcePolicy;
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupComputeResourcePreferences(groupResourceProfileId);
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws AppCatalogException {
        GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
        return groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId);
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws RegistryException {
        String replicaId = dataReplicaLocationRepository.registerReplicaLocation(replicaLocationModel);
        return replicaId;
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryException {
        String productUrl = dataProductRepository.registerDataProduct(dataProductModel);
        return productUrl;
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws AppCatalogException {
        LOCALDataMovement localDataMovement = new ComputeResourceRepository().getLocalDataMovement(dataMovementId);
        logger.debug("Airavata retrieved local data movement with data movement id: " + dataMovementId);
        return localDataMovement;
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws AppCatalogException {
        SCPDataMovement scpDataMovement = new ComputeResourceRepository().getSCPDataMovement(dataMovementId);
        logger.debug("Airavata retrieved SCP data movement with data movement id: " + dataMovementId);
        return scpDataMovement;
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws AppCatalogException {
        UnicoreDataMovement unicoreDataMovement =
                new ComputeResourceRepository().getUNICOREDataMovement(dataMovementId);
        logger.debug("Airavata retrieved UNICORE data movement with data movement id: " + dataMovementId);
        return unicoreDataMovement;
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws AppCatalogException {
        GridFTPDataMovement gridFTPDataMovement =
                new ComputeResourceRepository().getGridFTPDataMovement(dataMovementId);
        logger.debug("Airavata retrieved GRIDFTP data movement with data movement id: " + dataMovementId);
        return gridFTPDataMovement;
    }

    // Experiment operations
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryException {
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
                && experiment.getUserConfigurationData()
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
                        logger.error("Compute Resource  with id" + computationalResourceScheduling.getResourceHostId()
                                + "" + " is not enabled by the Admin!");
                        throw new RegistryException(
                                "Compute Resource  with id" + computationalResourceScheduling.getResourceHostId() + ""
                                        + " is not enabled by the Admin!");
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
        logger.debug(experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
        return experimentId;
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
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
            if (accessibleExpIds.size() == 0 && !ServerSettings.isEnableSharing()) {
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
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws RegistryException {
        if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
            logger.error(
                    airavataExperimentId, "Update request failed, Experiment {} doesn't exist.", airavataExperimentId);
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
                            && experiment.getUserConfigurationData()
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
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryException {
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
    }

    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws RegistryException {
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
        logger.debug(
                "Airavata retrieved intermediate outputs for experiment with experiment id : " + airavataExperimentId);
        return intermediateOutputs;
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryException {
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
    }

    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryException {
        if (ExpCatChildDataType.PROCESS_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
            processOutputRepository.addProcessOutputs(outputs, id);
        } else if (ExpCatChildDataType.EXPERIMENT_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
            experimentOutputRepository.addExperimentOutputs(outputs, id);
        }
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryException {
        if (ExpCatChildDataType.EXPERIMENT_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
            experimentErrorRepository.addExperimentError(errorModel, id);
        } else if (ExpCatChildDataType.TASK_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
            taskErrorRepository.addTaskError(errorModel, id);
        } else if (ExpCatChildDataType.PROCESS_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
            processErrorRepository.addProcessError(errorModel, id);
        }
    }

    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        taskStatusRepository.addTaskStatus(taskStatus, taskId);
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        processStatusRepository.addProcessStatus(processStatus, processId);
    }

    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        processStatusRepository.updateProcessStatus(processStatus, processId);
    }

    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryException {
        experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);
    }

    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryException {
        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);
        jobStatusRepository.addJobStatus(jobStatus, jobPK);
    }

    public void deleteJobs(String processId) throws RegistryException {
        List<JobModel> jobs = jobRepository.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, processId);
        for (JobModel jobModel : jobs) {
            jobRepository.removeJob(jobModel);
        }
    }

    // Project operations
    public String createProject(String gatewayId, Project project) throws RegistryException {
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
    }

    public void updateProject(String projectId, Project updatedProject) throws RegistryException {
        if (!validateString(projectId)) {
            logger.error("Project id cannot be empty...");
            throw new RegistryException("Project id cannot be empty...");
        }
        if (!projectRepository.isProjectExist(projectId)) {
            logger.error("Project does not exist in the system. Please provide a valid project ID...");
            throw new RegistryException("Project does not exist in the system. Please provide a valid project ID...");
        }
        projectRepository.updateProject(updatedProject, projectId);
        logger.debug("Airavata updated project with project Id : " + projectId);
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryException {
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
            if (accessibleProjIds.size() == 0 && !ServerSettings.isEnableSharing()) {
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
    }

    // Gateway Resource Profile operations
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws AppCatalogException {
        if (!validateString(gatewayResourceProfile.getGatewayID())) {
            logger.error("Cannot create gateway profile with empty gateway id");
            throw new AppCatalogException("Cannot create gateway profile with empty gateway id");
        }
        try {
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        String resourceProfile = gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);
        logger.debug("Airavata registered gateway profile with gateway id : " + gatewayResourceProfile.getGatewayID());
        return resourceProfile;
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        gwyResourceProfileRepository.updateGatewayResourceProfile(gatewayResourceProfile);
        logger.debug("Airavata updated gateway profile with gateway id : " + gatewayID);
        return true;
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    // Compute Resource operations
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws AppCatalogException {
        String computeResource = new ComputeResourceRepository().addComputeResource(computeResourceDescription);
        logger.debug("Airavata registered compute resource with compute resource Id : " + computeResource);
        return computeResource;
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws AppCatalogException {
        new ComputeResourceRepository().updateComputeResource(computeResourceId, computeResourceDescription);
        logger.debug("Airavata updated compute resource with compute resource Id : " + computeResourceId);
        return true;
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        return new ComputeResourceRepository().addResourceJobManager(resourceJobManager);
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AppCatalogException {
        new ComputeResourceRepository().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        return true;
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws AppCatalogException {
        switch (dmType) {
            case COMPUTE_RESOURCE:
                new ComputeResourceRepository().removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                return true;
            case STORAGE_RESOURCE:
                storageResourceRepository.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                logger.debug("Airavata deleted data movement interface with interface id : " + dataMovementInterfaceId);
                return true;
            default:
                logger.error(
                        "Unsupported data movement type specifies.. Please provide the correct data movement type... ");
                return false;
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        throw new AppCatalogException("updateGridFTPDataMovementDetails is not yet implemented");
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws AppCatalogException {
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
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        throw new AppCatalogException("updateUnicoreDataMovementDetails is not yet implemented");
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws AppCatalogException {
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
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws AppCatalogException {
        new ComputeResourceRepository().updateScpDataMovement(scpDataMovement);
        logger.debug("Airavata updated SCP data movement with data movement id: " + dataMovementInterfaceId);
        return true;
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws AppCatalogException {
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
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws AppCatalogException {
        new ComputeResourceRepository().updateLocalDataMovement(localDataMovement);
        logger.debug("Airavata updated local data movement with data movement id: " + dataMovementInterfaceId);
        return true;
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws AppCatalogException {
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
    }

    // Storage Resource operations
    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws AppCatalogException {
        String storageResource = storageResourceRepository.addStorageResource(storageResourceDescription);
        logger.debug("Airavata registered storage resource with storage resource Id : " + storageResource);
        return storageResource;
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws AppCatalogException {
        storageResourceRepository.updateStorageResource(storageResourceId, storageResourceDescription);
        logger.debug("Airavata updated storage resource with storage resource Id : " + storageResourceId);
        return true;
    }

    // Helper methods for job submission and data movement interfaces
    private String addJobSubmissionInterface(
            ComputeResourceRepository computeResourceRepository,
            String computeResourceId,
            String jobSubmissionInterfaceId,
            JobSubmissionProtocol protocolType,
            int priorityOrder)
            throws AppCatalogException {
        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        jobSubmissionInterface.setPriorityOrder(priorityOrder);
        jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
        return computeResourceRepository.addJobSubmissionProtocol(computeResourceId, jobSubmissionInterface);
    }

    private String addDataMovementInterface(
            ComputeResourceRepository computeResourceRepository,
            String computeResourceId,
            DMType dmType,
            String dataMovementInterfaceId,
            DataMovementProtocol protocolType,
            int priorityOrder)
            throws AppCatalogException {
        DataMovementInterface dataMovementInterface = new DataMovementInterface();
        dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
        dataMovementInterface.setPriorityOrder(priorityOrder);
        dataMovementInterface.setDataMovementProtocol(protocolType);
        if (dmType.equals(DMType.COMPUTE_RESOURCE)) {
            return computeResourceRepository.addDataMovementProtocol(computeResourceId, dmType, dataMovementInterface);
        } else if (dmType.equals(DMType.STORAGE_RESOURCE)) {
            dataMovementInterface.setStorageResourceId(computeResourceId);
            return storageResourceRepository.addDataMovementInterface(dataMovementInterface);
        }
        return null;
    }

    // Job Submission Interface operations
    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        String submissionInterface = addJobSubmissionInterface(
                computeResourceRepository,
                computeResourceId,
                computeResourceRepository.addSSHJobSubmission(sshJobSubmission),
                JobSubmissionProtocol.SSH,
                priorityOrder);
        logger.debug("Airavata registered SSH job submission for compute resource id: " + computeResourceId);
        return submissionInterface;
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        String submissionDetails = addJobSubmissionInterface(
                computeResourceRepository,
                computeResourceId,
                computeResourceRepository.addSSHJobSubmission(sshJobSubmission),
                JobSubmissionProtocol.SSH_FORK,
                priorityOrder);
        logger.debug("Airavata registered Fork job submission for compute resource id: " + computeResourceId);
        return submissionDetails;
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws AppCatalogException {
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        String submissionInterface = addJobSubmissionInterface(
                computeResourceRepository,
                computeResourceId,
                computeResourceRepository.addLocalJobSubmission(localSubmission),
                JobSubmissionProtocol.LOCAL,
                priorityOrder);
        logger.debug("Airavata added local job submission for compute resource id: " + computeResourceId);
        return submissionInterface;
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws AppCatalogException {
        new ComputeResourceRepository().updateLocalJobSubmission(localSubmission);
        logger.debug(
                "Airavata updated local job submission for job submission interface id: " + jobSubmissionInterfaceId);
        return true;
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission)
            throws AppCatalogException {
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        String submissionInterface = addJobSubmissionInterface(
                computeResourceRepository,
                computeResourceId,
                computeResourceRepository.addCloudJobSubmission(cloudSubmission),
                JobSubmissionProtocol.CLOUD,
                priorityOrder);
        logger.debug("Airavata registered Cloud job submission for compute resource id: " + computeResourceId);
        return submissionInterface;
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws AppCatalogException {
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        String submissionInterface = addJobSubmissionInterface(
                computeResourceRepository,
                computeResourceId,
                computeResourceRepository.addUNICOREJobSubmission(unicoreJobSubmission),
                JobSubmissionProtocol.UNICORE,
                priorityOrder);
        logger.debug("Airavata registered UNICORE job submission for compute resource id: " + computeResourceId);
        return submissionInterface;
    }

    // Application Interface/Module/Deployment operations
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterface, gatewayId);
        logger.debug("Airavata registered application interface for gateway id : " + gatewayId);
        return interfaceId;
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws AppCatalogException {
        applicationInterfaceRepository.updateApplicationInterface(appInterfaceId, applicationInterface);
        logger.debug("Airavata updated application interface with interface id : " + appInterfaceId);
        return true;
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        String module = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
        logger.debug("Airavata registered application module for gateway id : " + gatewayId);
        return module;
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws AppCatalogException {
        applicationInterfaceRepository.updateApplicationModule(appModuleId, applicationModule);
        logger.debug("Airavata updated application module with module id: " + appModuleId);
        return true;
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AppCatalogException {
        try {
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        String deployment = applicationDeploymentRepository.addApplicationDeployment(applicationDeployment, gatewayId);
        logger.debug("Airavata registered application deployment for gateway id : " + gatewayId);
        return deployment;
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws AppCatalogException {
        applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        logger.debug("Airavata updated application deployment for deployment id : " + appDeploymentId);
        return true;
    }

    // User Resource Profile operations
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        if (!validateString(userResourceProfile.getUserId())) {
            logger.error("Cannot create user resource profile with empty user id");
            throw new AppCatalogException("Cannot create user resource profile with empty user id");
        }
        if (!validateString(userResourceProfile.getGatewayID())) {
            logger.error("Cannot create user resource profile with empty gateway id");
            throw new AppCatalogException("Cannot create user resource profile with empty gateway id");
        }
        try {
            if (!userRepository.isUserExists(userResourceProfile.getGatewayID(), userResourceProfile.getUserId())) {
                logger.error("User does not exist.Please provide a valid user ID...");
                throw new AppCatalogException("User does not exist.Please provide a valid user ID...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        String resourceProfile = userResourceProfileRepository.addUserResourceProfile(userResourceProfile);
        logger.debug("Airavata registered user resource profile with gateway id : " + userResourceProfile.getGatewayID()
                + "and user id : " + userResourceProfile.getUserId());
        return resourceProfile;
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayId, userId)) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("user does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayId);
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayId, userId)) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("user does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        UserResourceProfile userResourceProfile =
                userResourceProfileRepository.getUserResourceProfile(userId, gatewayId);
        logger.debug("Airavata retrieved User resource profile with user id : " + userId);
        return userResourceProfile;
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
        logger.debug("Airavata updated gateway profile with gateway id : " + userId);
        return true;
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        userResourceProfileRepository.removeUserResourceProfile(userId, gatewayID);
        logger.debug("Airavata deleted User profile with gateway id : " + gatewayID + " and user id : " + userId);
        return true;
    }

    // Resource Scheduling operations
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryException {
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
    }

    // User operations
    public String addUser(UserProfile userProfile) throws RegistryException {
        logger.info("Adding User in Registry: " + userProfile);
        if (userRepository.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
            throw new RegistryException("User already exists, with userId: " + userProfile.getUserId()
                    + ", and gatewayId: " + userProfile.getGatewayId());
        }
        UserProfile savedUser = userRepository.addUser(userProfile);
        return savedUser.getUserId();
    }

    // User Compute/Storage Preference operations
    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
            throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id" + gatewayID
                    + "' does not exist!!!");
        }
        UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
        profile.addToUserComputeResourcePreferences(userComputeResourcePreference);
        userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
        logger.debug("Airavata added User compute resource preference with gateway id : " + gatewayID
                + " and for compute resource id : " + computeResourceId);
        return true;
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws AppCatalogException {
        try {
            if (userRepository.isUserExists(gatewayID, userId)
                    && userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                return userResourceProfileRepository.isUserComputeResourcePreferenceExists(
                        userId, gatewayID, computeResourceId);
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return false;
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
            throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id" + gatewayID
                    + "' does not exist!!!");
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
    }

    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
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
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
            throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id" + gatewayID
                    + "' does not exist!!!");
        }
        UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
        dataStoragePreference.setStorageResourceId(storageResourceId);
        profile.addToUserStoragePreferences(dataStoragePreference);
        userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
        logger.debug("Airavata added storage resource preference with gateway id : " + gatewayID
                + " and for storage resource id : " + storageResourceId);
        return true;
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
            throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id" + gatewayID
                    + "' does not exist!!!");
        }

        UserStoragePreference storagePreference =
                userResourceProfileRepository.getUserStoragePreference(userId, gatewayID, storageId);
        logger.debug("Airavata retrieved user storage resource preference with gateway id : " + gatewayID
                + " and for storage resource id : " + storageId);
        return storagePreference;
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        return userResourceProfileRepository.getAllUserResourceProfiles();
    }

    // Gateway Groups operations
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryException {
        if (!gatewayGroupsRepository.isExists(gatewayId)) {
            final String message = "No GatewayGroups entry exists for " + gatewayId;
            logger.error(message);
            throw new RegistryException(message);
        }
        return gatewayGroupsRepository.get(gatewayId);
    }

    // Parser operations
    public Parser getParser(String parserId, String gatewayId) throws RegistryException {
        if (!parserRepository.isExists(parserId)) {
            final String message = "No Parser Info entry exists for " + parserId;
            logger.error(message);
            throw new RegistryException(message);
        }
        return parserRepository.get(parserId);
    }

    public String saveParser(Parser parser) throws RegistryException {
        try {
            Parser saved = parserRepository.saveParser(parser);
            return saved.getId();
        } catch (AppCatalogException e) {
            throw new RegistryException("Error saving parser: " + e.getMessage(), e);
        }
    }

    public void removeParser(String parserId, String gatewayId) throws RegistryException {
        boolean exists = parserRepository.isExists(parserId);
        if (exists && !gatewayId.equals(parserRepository.get(parserId).getGatewayId())) {
            parserRepository.delete(parserId);
        } else {
            throw new RegistryException("Parser " + parserId + " does not exist");
        }
    }

    public ParserInput getParserInput(String parserInputId, String gatewayId) throws RegistryException {
        if (!parserInputRepository.isExists(parserInputId)) {
            final String message = "No ParserInput entry exists for " + parserInputId;
            logger.error(message);
            throw new RegistryException(message);
        }
        return parserInputRepository.get(parserInputId);
    }

    public String saveParserInput(ParserInput parserInput) throws RegistryException {
        ParserInput saved = parserInputRepository.create(parserInput);
        return saved.getId();
    }

    public void removeParserInput(String parserInputId, String gatewayId) throws RegistryException {
        boolean exists = parserInputRepository.isExists(parserInputId);
        if (exists) {
            ParserInput parserInput = parserInputRepository.get(parserInputId);
            Parser parser = parserRepository.get(parserInput.getParserId());
            if (gatewayId.equals(parser.getGatewayId())) {
                parserInputRepository.delete(parserInputId);
            } else {
                throw new RegistryException(
                        "ParserInput " + parserInputId + " does not belong to gateway " + gatewayId);
            }
        } else {
            throw new RegistryException("ParserInput " + parserInputId + " does not exist");
        }
    }

    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws RegistryException {
        if (!parserOutputRepository.isExists(parserOutputId)) {
            final String message = "No ParserOutput entry exists for " + parserOutputId;
            logger.error(message);
            throw new RegistryException(message);
        }
        return parserOutputRepository.get(parserOutputId);
    }

    public String saveParserOutput(ParserOutput parserOutput) throws RegistryException {
        ParserOutput saved = parserOutputRepository.create(parserOutput);
        return saved.getId();
    }

    public void removeParserOutput(String parserOutputId, String gatewayId) throws RegistryException {
        boolean exists = parserOutputRepository.isExists(parserOutputId);
        if (exists) {
            ParserOutput parserOutput = parserOutputRepository.get(parserOutputId);
            Parser parser = parserRepository.get(parserOutput.getParserId());
            if (gatewayId.equals(parser.getGatewayId())) {
                parserOutputRepository.delete(parserOutputId);
            } else {
                throw new RegistryException(
                        "ParserOutput " + parserOutputId + " does not belong to gateway " + gatewayId);
            }
        } else {
            throw new RegistryException("ParserOutput " + parserOutputId + " does not exist");
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        if (!parsingTemplateRepository.isExists(templateId)) {
            final String message = "No ParsingTemplate entry exists for " + templateId;
            logger.error(message);
            throw new RegistryException(message);
        }
        return parsingTemplateRepository.get(templateId);
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryException {
        ParsingTemplate saved = parsingTemplateRepository.create(parsingTemplate);
        return saved.getId();
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryException {
        boolean exists = parsingTemplateRepository.isExists(templateId);
        if (exists
                && !gatewayId.equals(parsingTemplateRepository.get(templateId).getGatewayId())) {
            parsingTemplateRepository.delete(templateId);
        } else {
            throw new RegistryException("Parsing template " + templateId + " does not exist");
        }
    }

    // Gateway Usage Reporting operations
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryException {
        return usageReportingCommandRepository.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId);
    }

    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        if (usageReportingCommandRepository.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId)) {
            return usageReportingCommandRepository.getGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } else {
            String message = "No usage reporting information for the gateway " + gatewayId + " and compute resource "
                    + computeResourceId;
            logger.error(message);
            throw new RegistryException(message);
        }
    }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryException {
        usageReportingCommandRepository.addGatewayUsageReportingCommand(command);
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryException {
        usageReportingCommandRepository.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        cloudJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        computeResourceRepository.updateCloudJobSubmission(cloudJobSubmission);
        logger.debug(
                "Airavata updated Cloud job submission for job submission interface id: " + jobSubmissionInterfaceId);
        return true;
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws AppCatalogException {
        sshJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        computeResourceRepository.updateSSHJobSubmission(sshJobSubmission);
        logger.debug(
                "Airavata updated SSH job submission for job submission interface id: " + jobSubmissionInterfaceId);
        return true;
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        unicoreJobSubmission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        computeResourceRepository.updateUNICOREJobSubmission(unicoreJobSubmission);
        logger.debug(
                "Airavata updated UNICORE job submission for job submission interface id: " + jobSubmissionInterfaceId);
        return true;
    }

    public boolean updateNotification(Notification notification) throws RegistryException {
        notificationRepository.updateNotification(notification);
        logger.debug("Airavata updated notification with notification id: " + notification.getNotificationId());
        return true;
    }

    public String createNotification(Notification notification) throws RegistryException {
        String notificationId = notificationRepository.createNotification(notification);
        logger.debug("Airavata created notification with notification id: " + notificationId);
        return notificationId;
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway)
            throws RegistryException, AppCatalogException {
        if (!gatewayRepository.isGatewayExist(gatewayId)) {
            logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
            throw new RegistryException("Gateway does not exist in the system. Please provide a valid gateway ID...");
        }
        updatedGateway.setGatewayId(gatewayId);
        gatewayRepository.updateGateway(gatewayId, updatedGateway);
        logger.debug("Airavata updated gateway with gateway id: " + gatewayId);
        return true;
    }

    public String addGateway(Gateway gateway) throws RegistryException, AppCatalogException {
        if (gatewayRepository.isGatewayExist(gateway.getGatewayId())) {
            logger.error("Gateway already exists in the system. Please provide a different gateway ID...");
            throw new AppCatalogException(
                    "Gateway already exists in the system. Please provide a different gateway ID...");
        }
        String gatewayId = gatewayRepository.addGateway(gateway);
        logger.debug("Airavata registered gateway with gateway id: " + gatewayId);
        return gatewayId;
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
            throw new AppCatalogException("User resource profile with user id'" + userId + " &  gateway Id" + gatewayID
                    + "' does not exist!!!");
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
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(
                userId, gatewayID, computeResourceId);
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new AppCatalogException("user does not exist.Please provide a valid user id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return userResourceProfileRepository.removeUserDataStoragePreferenceFromGateway(userId, gatewayID, storageId);
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        return queueStatusRepository.getLatestQueueStatuses();
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryException {
        queueStatusRepository.createQueueStatuses(queueStatuses);
    }

    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryException {
        Optional<QueueStatusModel> optionalQueueStatusModel = queueStatusRepository.getQueueStatus(hostName, queueName);
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
    }

    public void createGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        if (gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
            logger.error("GatewayGroups already exists for " + gatewayGroups.getGatewayId());
            throw new RegistryException(
                    "GatewayGroups for gatewayId: " + gatewayGroups.getGatewayId() + " already exists.");
        }
        gatewayGroupsRepository.create(gatewayGroups);
    }

    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        if (!gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
            final String message = "No GatewayGroups entry exists for " + gatewayGroups.getGatewayId();
            logger.error(message);
            throw new RegistryException(message);
        }
        gatewayGroupsRepository.update(gatewayGroups);
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryException {
        return gatewayGroupsRepository.isExists(gatewayId);
    }

    public List<Parser> listAllParsers(String gatewayId) throws RegistryException {
        return parserRepository.getAllParsers(gatewayId);
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId)
            throws RegistryException {
        return parsingTemplateRepository.getParsingTemplatesForApplication(applicationInterfaceId);
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryException {
        ExperimentModel experiment = experimentRepository.getExperiment(experimentId);
        List<ProcessModel> processes = experiment.getProcesses();
        if (processes != null && processes.size() > 0) {
            return parsingTemplateRepository.getParsingTemplatesForApplication(
                    processes.get(processes.size() - 1).getApplicationInterfaceId());
        }
        return Collections.emptyList();
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryException {
        return parsingTemplateRepository.getAllParsingTemplates(gatewayId);
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User Resource Profile does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException(
                        "User Resource Profile does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return userResourceProfileRepository
                .getUserResourceProfile(userId, gatewayID)
                .getUserComputeResourcePreferences();
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws AppCatalogException {
        try {
            if (!userRepository.isUserExists(gatewayID, userId)) {
                logger.error("User does not exist.Please provide a valid gateway id...");
                throw new AppCatalogException("Gateway does not exist.Please provide a valid gateway id...");
            }
        } catch (RegistryException e) {
            throw new AppCatalogException(e.getMessage(), e);
        }
        return userResourceProfileRepository
                .getUserResourceProfile(userId, gatewayID)
                .getUserStoragePreferences();
    }
}
