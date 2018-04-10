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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.*;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ExperimentRepository extends ExpCatAbstractRepository<ExperimentModel, ExperimentEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    public ExperimentRepository() {
        super(ExperimentModel.class, ExperimentEntity.class);
    }

    protected String saveExperimentModelData(ExperimentModel experimentModel) throws RegistryException {
        ExperimentEntity experimentEntity = saveExperiment(experimentModel);
        return experimentEntity.getExperimentId();
    }

    protected ExperimentEntity saveExperiment(ExperimentModel experimentModel) throws RegistryException {
        String experimentId = ExpCatalogUtils.getID(experimentModel.getExperimentName());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ExperimentEntity experimentEntity = mapper.map(experimentModel, ExperimentEntity.class);
        experimentEntity.setExperimentId(experimentId);

        if (experimentEntity.getUserConfigurationData() != null) {
            logger.debug("Populating the Primary Key of UserConfigurationData object for the Experiment");
            experimentEntity.getUserConfigurationData().setExperimentId(experimentId);
        }

        if (experimentEntity.getExperimentInputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentInput objects for the Experiment");
            experimentEntity.getExperimentInputs().forEach(experimentInputEntity -> experimentInputEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getExperimentOutputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentOutput objects for the Experiment");
            experimentEntity.getExperimentOutputs().forEach(experimentOutputEntity -> experimentOutputEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getExperimentStatus() != null) {
            logger.debug("Populating the Primary Key of ExperimentStatus objects for the Experiment");
            experimentEntity.getExperimentStatus().forEach(experimentStatusEntity -> experimentStatusEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getErrors() != null) {
            logger.debug("Populating the Primary Key of ExperimentError objects for the Experiment");
            experimentEntity.getErrors().forEach(experimentErrorEntity -> experimentErrorEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getProcesses() != null) {
            logger.debug("Populating the Process objects' Experiment ID for the Experiment");
            experimentEntity.getProcesses().forEach(processEntity -> processEntity.setExperimentId(experimentId));
        }

        if (!isExperimentExist(experimentId)) {
            logger.debug("Checking if the Experiment already exists");
            experimentEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(experimentEntity));
    }

    protected String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    protected ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        String processId = ExpCatalogUtils.getID("PROCESS");
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProcessEntity processEntity = mapper.map(processModel, ProcessEntity.class);
        processEntity.setProcessId(processId);

        if (processEntity.getProcessResourceSchedule() != null) {
            logger.debug("Populating the Primary Key of ProcessResourceSchedule object for the Process");
            processEntity.getProcessResourceSchedule().setProcessId(processId);
        }

        if (processEntity.getProcessInputs() != null) {
            logger.debug("Populating the Primary Key of ProcessInput objects for the Process");
            processEntity.getProcessInputs().forEach(processInputEntity -> processInputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessOutputs() != null) {
            logger.debug("Populating the Primary Key of ProcessOutput objects for the Process");
            processEntity.getProcessOutputs().forEach(processOutputEntity -> processOutputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessStatuses() != null) {
            logger.debug("Populating the Primary Key of ProcessStatus objects for the Process");
            processEntity.getProcessStatuses().forEach(processStatusEntity -> processStatusEntity.setProcessId(processId));
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating the Primary Key of ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(processErrorEntity -> processErrorEntity.setProcessId(processId));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Process objects' Process ID for the Process");
            processEntity.getTasks().forEach(taskEntity -> taskEntity.setParentProcessId(processId));
        }

        if (!isProcessExist(processId)) {
            logger.debug("Checking if the Process already exists");
            processEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        processEntity.setLastUpdateTime(new Timestamp((System.currentTimeMillis())));
        return execute(entityManager -> entityManager.merge(processEntity));
    }

    protected String saveTaskModelData(TaskModel taskModel) throws RegistryException {
        TaskEntity taskEntity = saveTask(taskModel);
        return taskEntity.getTaskId();
    }

    protected TaskEntity saveTask(TaskModel taskModel) throws RegistryException {
        String taskId = ExpCatalogUtils.getID("TASK");
        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskEntity taskEntity = mapper.map(taskModel, TaskEntity.class);
        taskEntity.setTaskId(taskId);

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating the Primary Key of TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(taskStatusEntity -> taskStatusEntity.setTaskId(taskId));
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating the Primary Key of TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(taskErrorEntity -> taskErrorEntity.setTaskId(taskId));
        }

        if (taskEntity.getJobs() != null) {
            logger.debug("Populating the Job objects' Task ID for the Task");
            taskEntity.getJobs().forEach(jobEntity -> jobEntity.setTaskId(taskId));
        }

        if (!isTaskExist(taskId)) {
            logger.debug("Checking if the Task already exists");
            taskEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        taskEntity.setLastUpdateTime(new Timestamp((System.currentTimeMillis())));
        return execute(entityManager -> entityManager.merge(taskEntity));
    }

    protected String saveJobModelData(JobModel jobModel, CompositeIdentifier cis) throws RegistryException {
        JobEntity jobEntity = saveJob(jobModel, cis);
        return jobEntity.getJobId();
    }

    protected JobEntity saveJob(JobModel jobModel, CompositeIdentifier cis) throws RegistryException {
        String jobId = (String) cis.getSecondLevelIdentifier();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobEntity jobEntity = mapper.map(jobModel, JobEntity.class);

        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> jobEntity.setJobId(jobId));
        }

        if (!isJobExist(cis)) {
            logger.debug("Checking if the Job already exists");
            jobEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(jobEntity));
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        return saveExperimentModelData(experimentModel);
    }

    public void updateExperiment(ExperimentModel updatedExperimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(updatedExperimentModel);
    }

    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        return get(experimentId);
    }

    public String addUserConfigurationData(UserConfigurationDataModel userConfigurationDataModel, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel.setUserConfigurationData(userConfigurationDataModel);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateUserConfigurationData(UserConfigurationDataModel updatedUserConfigurationDataModel, String experimentId) throws RegistryException {
        return addUserConfigurationData(updatedUserConfigurationDataModel, experimentId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId, String fieldName) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getUserConfigurationData();
    }

    public String addExperimentInputs(List<InputDataObjectType> experimentInputs, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel.setExperimentInputs(experimentInputs);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> updatedExperimentInputs, String experimentId) throws RegistryException {
        addExperimentInputs(updatedExperimentInputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentInputs();
    }

    public String addExperimentOutputs(List<OutputDataObjectType> experimentOutputs, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel.setExperimentOutputs(experimentOutputs);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> updatedExperimentOutputs, String experimentId) throws RegistryException {
        addExperimentOutputs(updatedExperimentOutputs, experimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentOutputs();
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<ExperimentStatus> experimentStatusList = experimentModel.getExperimentStatus();
        experimentStatusList.add(experimentStatus);
        experimentModel.setExperimentStatus(experimentStatusList);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateExperimentStatus(ExperimentStatus updatedExperimentStatus, String experimentId) throws RegistryException {
        return addExperimentStatus(updatedExperimentStatus, experimentId);
    }

    public List<ExperimentStatus> getExperimentStatus(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentStatus();
    }

    public String addExperimentError(ErrorModel experimentError, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<ErrorModel> errorModelList = experimentModel.getErrors();
        errorModelList.add(experimentError);
        experimentModel.setErrors(errorModelList);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateExperimentError(ErrorModel updatedExperimentError, String experimentId) throws RegistryException {
        return addExperimentError(updatedExperimentError, experimentId);
    }

    public List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getErrors();
    }

    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<ProcessModel> processModelList = experimentModel.getProcesses();
        processModelList.add(process);
        experimentModel.setProcesses(processModelList);
        updateExperiment(experimentModel, experimentId);
        process.setExperimentId(experimentId);
        return saveProcessModelData(process);
    }

    public void updateProcess(ProcessModel updatedProcess, String processId) throws RegistryException {
        saveProcessModelData(updatedProcess);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.get(processId);
    }

    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    public String addProcessInputs(List<InputDataObjectType> processInputs, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessInputs(processInputs);
        updateProcess(processModel, processId);
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> processInputs, String processId) throws RegistryException {
        addProcessInputs(processInputs, processId);
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessInputs();
    }

    public String addProcessOutputs(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessOutputs(processOutputs);
        updateProcess(processModel, processId);
        return processId;
    }

    public void updateProcessOutputs(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {
        addProcessOutputs(processOutputs, processId);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessOutputs();
    }

    public String addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatuses();
        processStatusList.add(processStatus);
        processModel.setProcessStatuses(processStatusList);
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        return addProcessStatus(processStatus, processId);
    }

    public List<ProcessStatus> getProcessStatus(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessStatuses();
    }

    public String addProcessError(ErrorModel processError, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ErrorModel> errorModelList = processModel.getProcessErrors();
        errorModelList.add(processError);
        processModel.setProcessErrors(errorModelList);
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessError(ErrorModel processError, String processId) throws RegistryException {
        return addProcessError(processError, processId);
    }

    public List<ErrorModel> getProcessError(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessErrors();
    }

    public String addTask(TaskModel task, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<TaskModel> taskModelList = processModel.getTasks();
        taskModelList.add(task);
        processModel.setTasks(taskModelList);
        updateProcess(processModel, processId);
        task.setParentProcessId(processId);
        return saveTaskModelData(task);
    }

    public String updateTask(TaskModel task, String taskId) throws RegistryException {
        return saveTaskModelData(task);
    }

    public TaskModel getTask(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        return taskRepository.get(taskId);
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();
        taskStatusList.add(taskStatus);
        taskModel.setTaskStatuses(taskStatusList);
        return updateTask(taskModel, taskId);
    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        return addTaskStatus(taskStatus, taskId);
    }

    public List<TaskStatus> getTaskStatus(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        return taskModel.getTaskStatuses();
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<ErrorModel> errorModelList = taskModel.getTaskErrors();
        errorModelList.add(taskError);
        taskModel.setTaskErrors(errorModelList);
        return updateTask(taskModel, taskId);
    }

    public String updateTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        return addTaskError(taskError, taskId);
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        return taskModel.getTaskErrors();
    }

    public String addJob(JobModel job, String processId) throws RegistryException {
        CompositeIdentifier cis = new CompositeIdentifier(job.getTaskId(), job.getJobId());
        String taskId = (String) cis.getTopLevelIdentifier();
        job.setProcessId(processId);
        TaskModel taskModel = getTask(taskId);
        List<JobModel> jobModelList = taskModel.getJobs();
        jobModelList.add(job);
        taskModel.setJobs(jobModelList);
        taskModel.setParentProcessId(processId);
        updateTask(taskModel, taskId);
        return saveJobModelData(job, cis);
    }

    public String updateJob(JobModel job, CompositeIdentifier cis) throws RegistryException {
        return saveJobModelData(job, cis);
    }

    public JobModel getJob(CompositeIdentifier cis) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        return jobRepository.get((String) cis.getSecondLevelIdentifier());
    }

    public String addJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        JobModel jobModel = getJob(cis);
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();
        jobStatusList.add(jobStatus);
        jobModel.setJobStatuses(jobStatusList);
        return updateJob(jobModel, cis);
    }

    public String updateJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        return addJobStatus(jobStatus, cis);
    }

    public Object getJobStatus(CompositeIdentifier cis) throws RegistryException {
        JobModel jobModel = getJob(cis);
        return jobModel.getJobStatuses();
    }

    /*public void updateExperimentField(String experimentId, String fieldName, Object value) throws RegistryException {

    }*/

    /*public void updateUserConfigurationDataField(String experimentId, String fieldName, Object value) throws RegistryException {

    }*/

    /*public void updateComputeResourceScheduling(ComputationalResourceSchedulingModel value, String experimentId) throws RegistryException {

    }*/

    public List<ExperimentModel> getExperimentList(String fieldName, Object value) throws RegistryException {
        return getExperimentList(fieldName, value, -1, 0, null, null);
    }

    public List<ExperimentModel> getExperimentList(String fieldName, Object value, int limit, int offset,
                                                   Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentModel> experimentModelList;

        if (fieldName.equals(DBConstants.Experiment.USER_NAME)) {
            logger.debug("Search criteria is Username");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.USER_NAME, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_USER, limit, offset, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.PROJECT_ID, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_PROJECT_ID, limit, offset, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is GatewayId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_GATEWAY_ID, limit, offset, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Experiment module.");
            throw new IllegalArgumentException("Unsupported field name for Experiment module.");
        }

        return experimentModelList;
    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Process.EXPERIMENT_ID, value);
            processModelList = processRepository.select(QueryConstants.GET_PROCESS_FOR_EXPERIMENT_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        List<TaskModel> taskModelList;

        if (fieldName.equals(DBConstants.Task.PARENT_PROCESS_ID)) {
            logger.debug("Search criteria is ParentProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Task.PARENT_PROCESS_ID, value);
            taskModelList = taskRepository.select(QueryConstants.GET_TASK_FOR_PARENT_PROCESS_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Task module.");
            throw new IllegalArgumentException("Unsupported field name for Task module.");
        }

        return taskModelList;
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        List<JobModel> jobModelList;

        if (fieldName.equals(DBConstants.Job.PROCESS_ID)) {
            logger.debug("Search criteria is ProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.PROCESS_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_PROCESS_ID, -1, 0, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Job.TASK_ID)) {
            logger.debug("Search criteria is TaskId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.TASK_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_TASK_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Job module.");
            throw new IllegalArgumentException("Unsupported field name for Job module.");
        }

        return jobModelList;
    }

    public List<String> getExperimentIDs(String fieldName, Object value) throws RegistryException {
        List<String> experimentIds = new ArrayList<>();
        List<ExperimentModel> experimentModelList = getExperimentList(fieldName, value);
        for (ExperimentModel experimentModel : experimentModelList) {
            experimentIds.add(experimentModel.getExperimentId());
        }
        return experimentIds;
    }

    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        List<String> processIds = new ArrayList<>();
        List<ProcessModel> processModelList = getProcessList(fieldName, value);
        for (ProcessModel processModel : processModelList) {
            processIds.add(processModel.getProcessId());
        }
        return processIds;
    }

    public List<String> getTaskIds(String fieldName, Object value) throws RegistryException {
        List<String> taskIds = new ArrayList<>();
        List<TaskModel> taskModelList = getTaskList(fieldName, value);
        for (TaskModel taskModel : taskModelList) {
            taskIds.add(taskModel.getTaskId());
        }
        return taskIds;
    }

    public List<String> getJobIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<>();
        List<JobModel> jobModelList = getJobList(fieldName, value);
        for (JobModel jobModel : jobModelList) {
            jobIds.add(jobModel.getJobId());
        }
        return jobIds;
    }

    public boolean isExperimentExist(String experimentId) throws RegistryException {
        return isExists(experimentId);
    }

    public boolean isUserConfigurationDataExist(String experimentId) throws RegistryException {
        UserConfigurationDataRepository userConfigurationDataRepository = new UserConfigurationDataRepository();
        return userConfigurationDataRepository.isExists(experimentId);
    }

    public boolean isProcessExist(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.isExists(processId);
    }

    public boolean isProcessResourceScheduleExist(String processId) throws RegistryException {
        ProcessResourceScheduleRepository processResourceScheduleRepository = new ProcessResourceScheduleRepository();
        return processResourceScheduleRepository.isExists(processId);
    }

    public boolean isTaskExist(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        return taskRepository.isExists(taskId);
    }

    public boolean isJobExist(CompositeIdentifier cis) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        String jobId = (String) cis.getSecondLevelIdentifier();
        return jobRepository.isExists(jobId);
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        delete(experimentId);
    }

    public void removeUserConfigurationData(String experimentId) throws RegistryException {
        UserConfigurationDataRepository userConfigurationDataRepository = new UserConfigurationDataRepository();
        userConfigurationDataRepository.delete(experimentId);
    }

    public void removeProcess(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        processRepository.delete(processId);
    }

    public void removeProcessResourceSchedule(String processId) throws RegistryException {
        ProcessResourceScheduleRepository processResourceScheduleRepository = new ProcessResourceScheduleRepository();
        processResourceScheduleRepository.delete(processId);
    }

    public void removeTask(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        taskRepository.delete(taskId);
    }

    public void removeJob(CompositeIdentifier cis) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        String jobId = (String) cis.getSecondLevelIdentifier();
        jobRepository.delete(jobId);
    }

    public boolean createQueueStatuses(List<QueueStatusModel> queueStatusModels) throws RegistryException {
        QueueStatusRepository queueStatusRepository = new QueueStatusRepository();

        for (QueueStatusModel queueStatusModel : queueStatusModels) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            QueueStatusEntity queueStatusEntity = mapper.map(queueStatusModel, QueueStatusEntity.class);
            queueStatusRepository.execute(entityManager -> entityManager.merge(queueStatusEntity));
        }

        return true;
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        QueueStatusRepository queueStatusRepository = new QueueStatusRepository();
        List<QueueStatusModel> queueStatusModelList = queueStatusRepository.select(QueryConstants.GET_ALL_QUEUE_STATUS_MODELS, 0);
        return queueStatusModelList;
    }

    public boolean isValidStatusTransition(Object object1, Object object2) {

        if (object1 instanceof ExperimentState && object2 instanceof ExperimentState) {
            ExperimentState oldState = (ExperimentState) object1;
            ExperimentState nextState = (ExperimentState) object2;

            if (nextState == null) {
                return false;
            }

            switch (oldState) {
                case CREATED:
                    return true;
                case VALIDATED:
                    return nextState != ExperimentState.CREATED;
                case SCHEDULED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED;
                case LAUNCHED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED;
                case EXECUTING:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED
                            || nextState != ExperimentState.LAUNCHED;

                case CANCELING:
                    return nextState == ExperimentState.CANCELING
                            || nextState == ExperimentState.CANCELED
                            || nextState == ExperimentState.COMPLETED
                            || nextState == ExperimentState.FAILED;
                case CANCELED:
                    return nextState == ExperimentState.CANCELED;
                case COMPLETED:
                    return nextState == ExperimentState.COMPLETED;
                case FAILED:
                    return nextState == ExperimentState.FAILED;
                default:
                    return false;
            }

        }

        else if (object1 instanceof ProcessState && object2 instanceof ProcessState) {
            ProcessState oldState = (ProcessState) object1;
            ProcessState nextState = (ProcessState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        }

        else if (object1 instanceof TaskState && object2 instanceof TaskState) {
            TaskState oldState = (TaskState) object1;
            TaskState nextState = (TaskState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        }

        else if (object1 instanceof JobState && object2 instanceof JobState) {
            JobState oldState = (JobState) object1;
            JobState nextState = (JobState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                default:
//                    return false;
//            }
        }

        return false;
    }

    public String getStatusID(String parentId) {
        String status = parentId.replaceAll("\\s", "");
        return status + "_" + UUID.randomUUID();
    }

    public String getErrorID(String parentId) {
        String error = parentId.replaceAll("\\s", "");
        return error + "_" + UUID.randomUUID();
    }

}