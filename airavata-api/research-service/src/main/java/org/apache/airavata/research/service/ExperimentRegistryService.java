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
package org.apache.airavata.research.service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.Constants;
import org.apache.airavata.interfaces.ExecutionDataAccess;
import org.apache.airavata.interfaces.ExpCatChildDataType;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.interfaces.ResultOrderType;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.*;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.*;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.research.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ExperimentRegistryService implements ExperimentRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentRegistryService.class);

    @Autowired
    private ExecutionDataAccess executionDataAccess;

    @Autowired
    private UserProfileProvider userProfileProvider;

    private final ExperimentRepository experimentRepository = new ExperimentRepository();
    private final ExperimentSummaryRepository experimentSummaryRepository = new ExperimentSummaryRepository();
    private final ExperimentStatusRepository experimentStatusRepository = new ExperimentStatusRepository();
    private final ExperimentOutputRepository experimentOutputRepository = new ExperimentOutputRepository();
    private final ExperimentErrorRepository experimentErrorRepository = new ExperimentErrorRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final GatewayRepository gatewayRepository = new GatewayRepository();

    @PostConstruct
    void init() {
        experimentRepository.setExecutionDataAccess(executionDataAccess);
    }

    // =========================================================================
    // ExperimentRegistry interface methods
    // =========================================================================

    @Override
    public ExperimentModel getExperiment(String airavataExperimentId) throws Exception {
        return getExperimentInternal(airavataExperimentId);
    }

    @Override
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws Exception {
        ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
        logger.debug("Airavata retrieved experiment status for experiment id : " + airavataExperimentId);
        return experimentStatus;
    }

    @Override
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws Exception {
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
                    case EXPERIMENT_STATE_CREATED:
                    case EXPERIMENT_STATE_SCHEDULED:
                    case EXPERIMENT_STATE_VALIDATED:
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
                            org.apache.airavata.compute.repository.ComputeResourceRepository computeResourceRepository =
                                    new org.apache.airavata.compute.repository.ComputeResourceRepository();
                            org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription
                                    computeResourceDescription =
                                            computeResourceRepository.getComputeResource(compResourceId);
                            if (!computeResourceDescription.getEnabled()) {
                                logger.error("Compute Resource is not enabled by the Admin!");
                                throw new RegistryException("Compute Resource is not enabled by the Admin!");
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
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            throw new RegistryException("Error while updating experiment. More info : " + e.getMessage());
        } catch (AppCatalogException e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            throw new RegistryException("Error while updating experiment. More info : " + e.getMessage());
        }
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws Exception {
        try {
            experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while updating experiment status", e);
            throw new RegistryException("Error while updating experiment status. More info : " + e.getMessage());
        }
    }

    @Override
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws Exception {
        try {
            if (ExpCatChildDataType.PROCESS_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                executionDataAccess.addProcessOutputs(outputs, id);
            } else if (ExpCatChildDataType.EXPERIMENT_OUTPUT.equals(ExpCatChildDataType.valueOf(outputType))) {
                experimentOutputRepository.addExperimentOutputs(outputs, id);
            }
        } catch (Exception e) {
            logger.error(id, "Error while adding outputs", e);
            throw new RegistryException("Error while adding outputs. More info : " + e.getMessage());
        }
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId) throws Exception {
        try {
            return executionDataAccess.addProcess(processModel, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while adding process ", e);
            throw new RegistryException("Error while adding process. More info : " + e.getMessage());
        }
    }

    @Override
    public ProcessModel getProcess(String processId) throws Exception {
        try {
            return executionDataAccess.getProcess(processId);
        } catch (Exception e) {
            logger.error(processId, "Error while retrieving user configuration ", e);
            throw new RegistryException("Error while retrieving user configuration. More info : " + e.getMessage());
        }
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws Exception {
        try {
            return executionDataAccess.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while retrieving process list ", e);
            throw new RegistryException("Error while retrieving process list. More info : " + e.getMessage());
        }
    }

    @Override
    public List<ProcessModel> getProcessListInState(ProcessState processState) throws Exception {
        try {
            List<ProcessModel> finalProcessList = new ArrayList<>();
            int offset = 0;
            int limit = 100;
            int count = 0;
            do {
                List<ProcessStatus> processStatusList =
                        executionDataAccess.getProcessStatusList(processState, offset, limit);
                offset += processStatusList.size();
                count = processStatusList.size();
                for (ProcessStatus processStatus : processStatusList) {
                    ProcessStatus latestStatus = executionDataAccess.getProcessStatus(processStatus.getProcessId());
                    if (latestStatus.getState().name().equals(processState.name())) {
                        finalProcessList.add(executionDataAccess.getProcess(latestStatus.getProcessId()));
                    }
                }
            } while (count == limit);
            return finalProcessList;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving process list with given status. More info : " + e.getMessage());
        }
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws Exception {
        try {
            return executionDataAccess.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while retrieving process ids", e);
            throw new RegistryException("Error while retrieving process ids. More info : " + e.getMessage());
        }
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws Exception {
        try {
            return executionDataAccess.getProcessStatus(processId);
        } catch (Exception e) {
            logger.error(processId, "Error while retrieving process status", e);
            throw new RegistryException("Error while retrieving process status. More info : " + e.getMessage());
        }
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws Exception {
        try {
            executionDataAccess.updateProcess(processModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while updating process ", e);
            throw new RegistryException("Error while updating process. More info : " + e.getMessage());
        }
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws Exception {
        try {
            executionDataAccess.addProcessStatus(processStatus, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding process status", e);
            throw new RegistryException("Error while adding process status. More info : " + e.getMessage());
        }
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws Exception {
        try {
            executionDataAccess.updateProcessStatus(processStatus, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while updating process status", e);
            throw new RegistryException("Error while updating process status. More info : " + e.getMessage());
        }
    }

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws Exception {
        try {
            executionDataAccess.addProcessWorkflow(processWorkflow, processWorkflow.getProcessId());
        } catch (Exception e) {
            logger.error("Error while adding process workflows for process id " + processWorkflow.getProcessId(), e);
            throw new RegistryException("Error while adding process workflows for process id "
                    + processWorkflow.getProcessId() + ". More info : " + e.getMessage());
        }
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws Exception {
        try {
            return executionDataAccess.addTask(taskModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding task ", e);
            throw new RegistryException("Error while adding task. More info : " + e.getMessage());
        }
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws Exception {
        try {
            executionDataAccess.addTaskStatus(taskStatus, taskId);
        } catch (Exception e) {
            logger.error(taskId, "Error while adding task status", e);
            throw new RegistryException("Error while adding task status. More info : " + e.getMessage());
        }
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws Exception {
        try {
            executionDataAccess.addJob(jobModel, processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding job ", e);
            throw new RegistryException("Error while adding job. More info : " + e.getMessage());
        }
    }

    @Override
    public List<JobModel> getJobs(String queryType, String id) throws Exception {
        try {
            return fetchJobModels(queryType, id);
        } catch (Exception e) {
            logger.error(id, "Error while retrieving jobs for query " + queryType + " and id " + id, e);
            throw new RegistryException("Error while retrieving jobs for query " + queryType + " and id " + id
                    + ". More info : " + e.getMessage());
        }
    }

    @Override
    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws Exception {
        try {
            executionDataAccess.addJobStatus(jobStatus, jobId, taskId);
        } catch (Exception e) {
            logger.error(jobId, "Error while adding job status", e);
            throw new RegistryException("Error while adding job status. More info : " + e.getMessage());
        }
    }

    @Override
    public void deleteJobs(String processId) throws Exception {
        try {
            List<JobModel> jobs =
                    executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, processId);
            for (JobModel jobModel : jobs) {
                executionDataAccess.removeJob(jobModel);
            }
        } catch (Exception e) {
            logger.error(processId, "Error while deleting job ", e);
            throw new RegistryException("Error while deleting job. More info : " + e.getMessage());
        }
    }

    @Override
    public int getJobCount(JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes) throws Exception {
        List<JobStatus> jobStatusList = executionDataAccess.getDistinctListofJobStatus(
                gatewayId, jobStatus.getJobState().name(), searchBackTimeInMinutes);
        return jobStatusList.size();
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id) throws Exception {
        try {
            if (ExpCatChildDataType.EXPERIMENT_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                experimentErrorRepository.addExperimentError(errorModel, id);
            } else if (ExpCatChildDataType.TASK_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                executionDataAccess.addTaskError(errorModel, id);
            } else if (ExpCatChildDataType.PROCESS_ERROR.equals(ExpCatChildDataType.valueOf(errorType))) {
                executionDataAccess.addProcessError(errorModel, id);
            }
        } catch (Exception e) {
            logger.error(id, "Error while adding error", e);
            throw new RegistryException("Error while adding error. More info : " + e.getMessage());
        }
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws Exception {
        return executionDataAccess.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    // =========================================================================
    // Additional experiment methods (not yet on the interface)
    // =========================================================================

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
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Internal error");
        }
        try {
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
            return experimentSummaryRepository.getAccessibleExperimentStatistics(
                    accessibleExpIds, filters, limit, offset);
        } catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            throw new RegistryException("Error while retrieving experiments. More info : " + e.getMessage());
        }
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Internal error");
        }
        if (!validateString(projectId)) {
            throw new RegistryException("Project id cannot be empty. Please provide a valid project ID...");
        }
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            return experimentRepository.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID,
                    projectId,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            throw new RegistryException("Error while retrieving the experiments. More info : " + e.getMessage());
        }
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws Exception {
        if (!validateString(userName)) {
            throw new RegistryException("Username cannot be empty. Please provide a valid user..");
        }
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Internal error");
        }
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) == null) {
                return new ArrayList<>();
            }
            return experimentRepository.getExperimentList(
                    gatewayId,
                    Constants.FieldConstants.ExperimentConstants.USER_NAME,
                    userName,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            throw new RegistryException("Error while retrieving the experiments. More info : " + e.getMessage());
        }
    }

    public boolean deleteExperiment(String experimentId) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(experimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
            if (!(experimentModel.getExperimentStatusList().get(0).getState()
                    == ExperimentState.EXPERIMENT_STATE_CREATED)) {
                throw new org.apache.airavata.interfaces.ExperimentCatalogException(
                        "Experiment is not in CREATED state. Hence cannot deleted. ID:" + experimentId);
            }
            experimentRepository.removeExperiment(experimentId);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            throw new RegistryException("Error while deleting the experiment. More info : " + e.getMessage());
        }
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws Exception {
        try {
            ExperimentModel experimentModel = getExperiment(airavataExperimentId);
            List<ProcessModel> processList = executionDataAccess.getProcessList(
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
            if (processList != null) {
                List<ProcessModel> updatedProcessList = new ArrayList<>();
                for (ProcessModel p : processList) {
                    List<TaskModel> updatedTasks = new ArrayList<>();
                    for (TaskModel t : p.getTasksList()) {
                        try {
                            List<JobModel> jobList = executionDataAccess.getJobList(
                                    Constants.FieldConstants.JobConstants.TASK_ID, t.getTaskId());
                            if (jobList != null) {
                                Collections.sort(
                                        jobList, (o1, o2) -> (int) (o1.getCreationTime() - o2.getCreationTime()));
                                t = t.toBuilder()
                                        .clearJobs()
                                        .addAllJobs(jobList)
                                        .build();
                            }
                        } catch (RegistryException e) {
                            logger.error(e.getMessage(), e);
                        }
                        updatedTasks.add(t);
                    }
                    updatedProcessList.add(
                            p.toBuilder().clearTasks().addAllTasks(updatedTasks).build());
                }
                experimentModel = experimentModel.toBuilder()
                        .clearProcesses()
                        .addAllProcesses(updatedProcessList)
                        .build();
            }
            return experimentModel;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            throw new RegistryException("Error while retrieving the experiment. More info : " + e.getMessage());
        }
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentOutputRepository.getExperimentOutputs(airavataExperimentId);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            throw new RegistryException("Error while retrieving the experiment outputs. More info : " + e.getMessage());
        }
    }

    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws Exception {
        return null;
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<ProcessModel> processModels = executionDataAccess.getProcessList(
                    Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            Map<String, JobStatus> jobStatus = new HashMap<>();
            if (processModels != null && !processModels.isEmpty()) {
                for (ProcessModel processModel : processModels) {
                    for (TaskModel task : processModel.getTasksList()) {
                        List<JobModel> jobs = executionDataAccess.getJobList(
                                Constants.FieldConstants.JobConstants.TASK_ID, task.getTaskId());
                        if (jobs != null && !jobs.isEmpty()) {
                            for (JobModel jobModel : jobs) {
                                List<JobStatus> status = jobModel.getJobStatusesList();
                                if (status != null && status.size() > 0) {
                                    jobStatus.put(jobModel.getJobId(), status.get(status.size() - 1));
                                }
                            }
                        }
                    }
                }
            }
            return jobStatus;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job statuses", e);
            throw new RegistryException("Error while retrieving the job statuses. More info : " + e.getMessage());
        }
    }

    public void deleteTasks(String processId) throws Exception {
        try {
            executionDataAccess.deleteTasks(processId);
        } catch (Exception e) {
            logger.error(processId, "Error while adding task ", e);
            throw new RegistryException("Error while adding task. More info : " + e.getMessage());
        }
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws Exception {
        try {
            return experimentRepository.getUserConfigurationData(experimentId);
        } catch (Exception e) {
            logger.error(experimentId, "Error while getting user configuration ", e);
            throw new RegistryException("Error while adding task. More info : " + e.getMessage());
        }
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws Exception {
        try {
            return executionDataAccess.getProcessStatusList(processId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving process status list for given process Id. More info : " + e.getMessage());
        }
    }

    public boolean isJobExist(String queryType, String id) throws Exception {
        try {
            return fetchJobModel(queryType, id) != null;
        } catch (Exception e) {
            logger.error(id, "Error while retrieving job", e);
            throw new RegistryException("Error while retrieving job. More info : " + e.getMessage());
        }
    }

    public JobModel getJob(String queryType, String id) throws Exception {
        try {
            JobModel jobModel = fetchJobModel(queryType, id);
            if (jobModel != null) return jobModel;
            throw new Exception("Job not found for queryType: " + queryType + ", id: " + id);
        } catch (Exception e) {
            logger.error(id, "Error while retrieving job", e);
            throw new RegistryException("Error while retrieving job. More info : " + e.getMessage());
        }
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws Exception {
        try {
            return executionDataAccess.getProcessOutputs(processId);
        } catch (Exception e) {
            logger.error("Error while retrieving process outputs for process id " + processId, e);
            throw new RegistryException("Error while retrieving process outputs. More info : " + e.getMessage());
        }
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws Exception {
        try {
            return executionDataAccess.getProcessWorkflows(processId);
        } catch (Exception e) {
            logger.error("Error while retrieving process workflows for process id " + processId, e);
            throw new RegistryException("Error while retrieving process workflows for process id " + processId
                    + ". More info : " + e.getMessage());
        }
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<ProcessModel> processModels = executionDataAccess.getProcessList(
                    Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            List<JobModel> jobList = new ArrayList<>();
            if (processModels != null && !processModels.isEmpty()) {
                for (ProcessModel processModel : processModels) {
                    for (TaskModel taskModel : processModel.getTasksList()) {
                        jobList.addAll(executionDataAccess.getJobList(
                                Constants.FieldConstants.JobConstants.TASK_ID, taskModel.getTaskId()));
                    }
                }
            }
            return jobList;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job details", e);
            throw new RegistryException("Error while retrieving the job details. More info : " + e.getMessage());
        }
    }

    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatus(airavataExperimentId);
            if (experimentStatus != null) {
                switch (experimentStatus.getState()) {
                    case EXPERIMENT_STATE_CREATED:
                    case EXPERIMENT_STATE_VALIDATED:
                    case EXPERIMENT_STATE_CANCELED:
                    case EXPERIMENT_STATE_FAILED:
                        executionDataAccess.addProcessResourceSchedule(resourceScheduling, airavataExperimentId);
                        break;
                    default:
                        throw new RegistryException(
                                "Error while updating experiment. Update experiment is only valid for experiments with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN.");
                }
            }
        } catch (Exception e) {
            throw new RegistryException("Error while updating scheduling info. More info : " + e.getMessage());
        }
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatus(airavataExperimentId);
            if (experimentStatus != null) {
                switch (experimentStatus.getState()) {
                    case EXPERIMENT_STATE_CREATED:
                    case EXPERIMENT_STATE_VALIDATED:
                    case EXPERIMENT_STATE_CANCELED:
                    case EXPERIMENT_STATE_FAILED:
                        experimentRepository.addUserConfigurationData(userConfiguration, airavataExperimentId);
                        break;
                    default:
                        throw new RegistryException(
                                "Error while updating experiment. Update experiment is only valid for experiments with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN.");
                }
            }
        } catch (Exception e) {
            throw new RegistryException("Error while updating user configuration. More info : " + e.getMessage());
        }
    }

    public String createExperiment(String gatewayId, ExperimentModel experiment) throws Exception {
        try {
            if (!validateString(experiment.getExperimentName())) {
                throw new RegistryException("Cannot create experiments with empty experiment name");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                throw new RegistryException("Internal error");
            }
            if (experiment.getUserConfigurationData() != null
                    && experiment.getUserConfigurationData().getComputationalResourceScheduling() != null
                    && experiment
                                    .getUserConfigurationData()
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId()
                            != null) {
                org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription crd =
                        new org.apache.airavata.compute.repository.ComputeResourceRepository()
                                .getComputeResource(experiment
                                        .getUserConfigurationData()
                                        .getComputationalResourceScheduling()
                                        .getResourceHostId());
                if (!crd.getEnabled()) {
                    throw new RegistryException("Compute Resource is not enabled by the Admin!");
                }
            } else if (!experiment
                    .getUserConfigurationData()
                    .getAutoScheduledCompResourceSchedulingListList()
                    .isEmpty()) {
                for (ComputationalResourceSchedulingModel crs :
                        experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingListList()) {
                    org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription crd =
                            new org.apache.airavata.compute.repository.ComputeResourceRepository()
                                    .getComputeResource(crs.getResourceHostId());
                    if (!crd.getEnabled()) {
                        throw new RegistryException(
                                "Compute Resource with id" + crs.getResourceHostId() + " is not enabled by the Admin!");
                    }
                }
            }
            experiment = experiment.toBuilder().setGatewayId(gatewayId).build();
            String experimentId = experimentRepository.addExperiment(experiment);
            if (experiment.getExperimentType() == ExperimentType.WORKFLOW) {
                executionDataAccess.registerWorkflow(experiment.getWorkflow(), experimentId);
            }
            return experimentId;
        } catch (Exception e) {
            throw new RegistryException("Error while creating the experiment. More info : " + e.getMessage());
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception {
        if (!validateString(userName)) {
            throw new RegistryException("Username cannot be empty. Please provide a valid user..");
        }
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Internal error");
        }
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) == null) {
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            Map<String, String> regFilters = new HashMap<>();
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
            if (accessibleExpIds.size() == 0 && !ServerSettings.isEnableSharing()) {
                if (!regFilters.containsKey(DBConstants.Experiment.USER_NAME)) {
                    regFilters.put(DBConstants.Experiment.USER_NAME, userName);
                }
            }
            return experimentSummaryRepository.searchAllAccessibleExperiments(
                    accessibleExpIds,
                    regFilters,
                    limit,
                    offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME,
                    ResultOrderType.DESC);
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving experiments. More info : " + e.getMessage());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws Exception {
        try {
            if (!experimentRepository.isExperimentExist(airavataExperimentId)) {
                throw new RegistryException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return experimentRepository.getExperiment(airavataExperimentId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the experiment", e);
            throw new RegistryException("Error while retrieving the experiment. More info : " + e.getMessage());
        }
    }

    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws Exception {
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
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment status", e);
            throw new RegistryException("Error while retrieving the experiment status. More info : " + e.getMessage());
        }
    }

    private List<JobModel> fetchJobModels(String queryType, String id) throws RegistryException {
        List<JobModel> jobs = new ArrayList<>();
        switch (queryType) {
            case Constants.FieldConstants.JobConstants.TASK_ID:
                jobs = executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
                break;
            case Constants.FieldConstants.JobConstants.PROCESS_ID:
                jobs = executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
                break;
            case Constants.FieldConstants.JobConstants.JOB_ID:
                jobs = executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.JOB_ID, id);
                break;
        }
        return jobs;
    }

    private JobModel fetchJobModel(String queryType, String id) throws RegistryException {
        if (queryType.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
            List<JobModel> jobs = executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.TASK_ID, id);
            if (jobs != null) {
                for (JobModel j : jobs) {
                    if (j.getJobId() != null || !j.equals("")) {
                        return j;
                    }
                }
            }
        } else if (queryType.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
            List<JobModel> jobs = executionDataAccess.getJobList(Constants.FieldConstants.JobConstants.PROCESS_ID, id);
            if (jobs != null) {
                for (JobModel j : jobs) {
                    if (j.getJobId() != null || !j.equals("")) {
                        return j;
                    }
                }
            }
        }
        return null;
    }

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }

    private boolean isGatewayExistInternal(String gatewayId) throws Exception {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }
}
