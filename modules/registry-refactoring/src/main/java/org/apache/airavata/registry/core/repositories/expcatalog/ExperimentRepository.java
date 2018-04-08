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
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.*;
import org.apache.regexp.RE;
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

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        return saveExperimentModelData(experimentModel);
    }

    public void updateExperiment(ExperimentModel experimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(experimentModel);
    }

    public ExperimentModel getExperiment(String experimentId, String fieldName) throws RegistryException {
        return get(experimentId);
    }

    public String addUserConfigurationData(UserConfigurationDataModel configurationData, String experimentId) throws RegistryException {

    }

    public String addExperimentInputs(List<InputDataObjectType> exInputs, String experimentId) throws RegistryException {

    }

    public String addExperimentOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {

    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String expId) throws RegistryException {

    }

    public String addExperimentError(ErrorModel experimentError, String expId) throws RegistryException {

    }

    public String addProcess(ProcessModel process, String expId) throws RegistryException {

    }

    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel resourceSchedule, String processID) throws RegistryException {

    }

    public String addProcessInputs(List<InputDataObjectType> processInputs, String processID) throws RegistryException {

    }

    public String addProcessOutputs(List<OutputDataObjectType> processOutput, String processID) throws RegistryException {

    }

    public String addProcessStatus(ProcessStatus processStatus, String processID) throws RegistryException {

    }

    public String addProcessError(ErrorModel processError, String processID) throws RegistryException {

    }

    public String addTask(TaskModel task, String processID) throws RegistryException {

    }

    public String addTaskStatus(TaskStatus taskStatus, String taskID) throws RegistryException {

    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {

    }

    public String addJob(JobModel job, String processId) throws RegistryException {

    }

    public String addJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {

    }

    public void updateExpInputs(List<InputDataObjectType> exInputs, String expID) throws RegistryException {

    }

    public void updateExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {

    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String expId) throws RegistryException {

    }

    public String updateExperimentError(ErrorModel experimentError, String expId) throws RegistryException {

    }

    public String updateUserConfigData(UserConfigurationDataModel configurationData, String experimentId) throws RegistryException {

    }

    public void updateProcess(ProcessModel process, String processId) throws RegistryException {

    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel resourceSchedule, String processID) throws RegistryException {

    }

    public void updateProcessInputs(List<InputDataObjectType> processInputs, String processID) throws RegistryException {

    }

    public void updateProcessOutputs(List<OutputDataObjectType> processOutput, String processID) throws RegistryException {

    }

    public String updateProcessStatus(ProcessStatus processStatus, String processID) throws RegistryException {

    }

    public String updateProcessError(ErrorModel processErrors, String processID) throws RegistryException {

    }

    public String updateTask(TaskModel task, String taskID) throws RegistryException {

    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskID) throws RegistryException {

    }

    public String updateTaskError(ErrorModel taskError, String taskID) throws RegistryException {

    }

    public String updateJob(JobModel job, CompositeIdentifier cis) throws RegistryException {

    }

    public String updateJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {

    }

    public void updateExperimentField(String expID, String fieldName, Object value) throws RegistryException {

    }

    public void updateUserConfigDataField(String expID, String fieldName, Object value) throws RegistryException {

    }

    public void updateComputeResourceScheduling(ComputationalResourceSchedulingModel value, String expID) throws RegistryException {

    }

    public Object getExperimentInputs(String expID) throws RegistryException {

    }

    public Object getExperimentOutputs(String expID) throws RegistryException {

    }

    public Object getExperimentErrors(String expID) throws RegistryException {

    }

    public Object getExperimentStatus(String expID) throws RegistryException {

    }

    public Object getUserConfigData(String expId, String fieldName) throws RegistryException {

    }

    public Object getProcess(String processId, String fieldName) throws RegistryException {

    }

    public Object getProcessError(String processId) throws RegistryException {

    }

    public Object getProcessStatus(String processId) throws RegistryException {

    }

    public Object getProcessInputs(String processId) throws RegistryException {

    }

    public Object getProcessOutputs(String processId) throws RegistryException {

    }

    public Object getProcessResourceSchedule(String processId) throws RegistryException {

    }

    public Object getTask(String taskId, String fieldName) throws RegistryException {

    }

    public Object getTaskError(String taskId) throws RegistryException {

    }

    public Object getTaskStatus(String taskId) throws RegistryException {

    }

    public Object getJob(CompositeIdentifier cis, String fieldName) throws RegistryException {

    }

    public Object getJobStatus(CompositeIdentifier cis) throws RegistryException {

    }

    public List<ExperimentModel> getExperimentList(String fieldName, Object value) throws RegistryException {

    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {

    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {

    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {

    }

    public List<ExperimentModel> getExperimentList(String fieldName, Object value, int limit, int offset,
                                                   Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {

    }

    public List<ExperimentSummaryModel> searchExperiments(Map<String, String> filters, int limit,
                                                          int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {

    }

    public List<ExperimentSummaryModel> searchAllAccessibleExperiments(List<String> accessibleIds, Map<String, String> filters, int limit,
                                                                       int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {

    }

    public ExperimentStatistics getExperimentStatistics(Map<String,String> filters) throws RegistryException {

    }

    public List<String> getExperimentIDs(String fieldName, Object value) throws RegistryException {

    }

    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {

    }

    public List<String> getTaskIds(String fieldName, Object value) throws RegistryException {

    }

    public List<String> getJobIds(String fieldName, Object value) throws RegistryException {

    }

    public boolean isExperimentExist(String experimentId) throws RegistryException {
        isExists(experimentId);
    }

    public boolean isUserConfigurationDataExist(String experimentId) throws RegistryException {
        UserConfigurationDataRepository userConfigurationDataRepository = new UserConfigurationDataRepository();
        userConfigurationDataRepository.isExists(experimentId);
    }

    public boolean isProcessExist(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        processRepository.isExists(processId);
    }

    public boolean isProcessResourceScheduleExist(String processId) throws RegistryException {
        ProcessResourceScheduleRepository processResourceScheduleRepository = new ProcessResourceScheduleRepository();
        processResourceScheduleRepository.isExists(processId);
    }

    public boolean isTaskExist(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        taskRepository.isExists(taskId);
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

    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {

    }

    public String getStatusID(String parentId) {
        String status = parentId.replaceAll("\\s", "");
        return status + "_" + UUID.randomUUID();
    }

    public String getErrorID(String parentId) {
        String error = parentId.replaceAll("\\s", "");
        return error + "_" + UUID.randomUUID();
    }

    public boolean isValidStatusTransition(Object object1, Object object2) {

    }

}