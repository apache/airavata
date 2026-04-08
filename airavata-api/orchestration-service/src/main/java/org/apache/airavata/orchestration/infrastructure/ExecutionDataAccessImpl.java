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
package org.apache.airavata.orchestration.infrastructure;

import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.model.ComputationalResourceSchedulingEntity;
import org.apache.airavata.interfaces.ExecutionDataAccess;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.JobPK;
import org.apache.airavata.orchestration.model.ProcessEntity;
import org.apache.airavata.orchestration.model.UserConfigurationDataEntity;
import org.apache.airavata.orchestration.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecutionDataAccessImpl implements ExecutionDataAccess {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionDataAccessImpl.class);

    private final ProcessRepository processRepository = new ProcessRepository();
    private final ExecIoParamRepository execIoParamRepository = new ExecIoParamRepository();
    private final ExecStatusRepository execStatusRepository = new ExecStatusRepository();
    private final ExecErrorRepository execErrorRepository = new ExecErrorRepository();
    private final ProcessWorkflowRepository processWorkflowRepository = new ProcessWorkflowRepository();
    private final JobRepository jobRepository = new JobRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    // --- Process ---

    @Override
    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        return processRepository.addProcess(process, experimentId);
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryException {
        return processRepository.getProcess(processId);
    }

    @Override
    public void updateProcess(ProcessModel process, String processId) throws RegistryException {
        processRepository.updateProcess(process, processId);
    }

    @Override
    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        return processRepository.getProcessList(fieldName, value);
    }

    @Override
    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        return processRepository.getProcessIds(fieldName, value);
    }

    @Override
    public void addProcessResourceSchedule(ComputationalResourceSchedulingModel scheduling, String processId)
            throws RegistryException {
        processRepository.addProcessResourceSchedule(scheduling, processId);
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes) {
        return processRepository.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    // --- Process Status ---

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        execStatusRepository.addProcessStatus(processStatus, processId);
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        execStatusRepository.updateProcessStatus(processStatus, processId);
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        return execStatusRepository.getProcessStatus(processId);
    }

    @Override
    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        return execStatusRepository.getProcessStatusList(processId);
    }

    @Override
    public List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit)
            throws RegistryException {
        return execStatusRepository.getProcessStatusList(processState, offset, limit);
    }

    // --- Process Error ---

    @Override
    public void addProcessError(ErrorModel processError, String processId) throws RegistryException {
        execErrorRepository.addProcessError(processError, processId);
    }

    // --- Process Output ---

    @Override
    public void addProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        execIoParamRepository.addProcessOutputs(outputs, processId);
    }

    @Override
    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        return execIoParamRepository.getProcessOutputs(processId);
    }

    // --- Process Workflow ---

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow, String processId) throws RegistryException {
        processWorkflowRepository.addProcessWorkflow(processWorkflow, processId);
    }

    @Override
    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException {
        return processWorkflowRepository.getProcessWorkflows(processId);
    }

    // --- Task ---

    @Override
    public String addTask(TaskModel task, String processId) throws RegistryException {
        return taskRepository.addTask(task, processId);
    }

    @Override
    public void deleteTasks(String processId) throws RegistryException {
        taskRepository.deleteTasks(processId);
    }

    // --- Task Status ---

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        execStatusRepository.addTaskStatus(taskStatus, taskId);
    }

    // --- Task Error ---

    @Override
    public void addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        execErrorRepository.addTaskError(taskError, taskId);
    }

    // --- Job ---

    @Override
    public void addJob(JobModel job, String processId) throws RegistryException {
        jobRepository.addJob(job, processId);
    }

    @Override
    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        return jobRepository.getJobList(fieldName, value);
    }

    @Override
    public void removeJob(JobModel job) throws RegistryException {
        jobRepository.removeJob(job);
    }

    // --- Job Status ---

    @Override
    public void addJobStatus(JobStatus jobStatus, String jobId, String taskId) throws RegistryException {
        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);
        execStatusRepository.addJobStatus(jobStatus, jobPK);
    }

    @Override
    public List<JobStatus> getDistinctListofJobStatus(String gatewayId, String status, double time) {
        return execStatusRepository.getDistinctListofJobStatus(gatewayId, status, time);
    }

    // --- UserConfigurationData ---

    @Override
    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        return processRepository.execute(entityManager -> {
            UserConfigurationDataEntity ucdEntity = entityManager.find(UserConfigurationDataEntity.class, experimentId);
            if (ucdEntity != null) {
                return ExecutionMapper.INSTANCE.userConfigDataToModel(ucdEntity);
            }
            return null;
        });
    }

    @Override
    public void saveUserConfigurationData(UserConfigurationDataModel ucdModel, String experimentId)
            throws RegistryException {
        UserConfigurationDataEntity ucdEntity = ExecutionMapper.INSTANCE.userConfigDataToEntity(ucdModel);
        ucdEntity.setExperimentId(experimentId);
        if (ucdEntity.getAutoScheduledCompResourceSchedulingList() != null) {
            logger.debug(
                    "Populating the Primary Key of UserConfigurationData.ComputationalResourceSchedulingEntities for Experiment");
            for (ComputationalResourceSchedulingEntity entity :
                    ucdEntity.getAutoScheduledCompResourceSchedulingList()) {
                entity.setExperimentId(experimentId);
            }
        }
        processRepository.execute(entityManager -> entityManager.merge(ucdEntity));
    }

    // --- Processes for Experiment ---

    @Override
    public List<ProcessModel> getProcessesForExperiment(String experimentId) throws RegistryException {
        return processRepository.execute(entityManager -> {
            List<ProcessEntity> processEntities = entityManager
                    .createQuery("SELECT p FROM ProcessEntity p WHERE p.experimentId = :expId", ProcessEntity.class)
                    .setParameter("expId", experimentId)
                    .getResultList();
            List<ProcessModel> result = new java.util.ArrayList<>();
            for (ProcessEntity pe : processEntities) {
                result.add(ExecutionMapper.INSTANCE.processToModel(pe));
            }
            return result;
        });
    }
}
