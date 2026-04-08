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
package org.apache.airavata.orchestration.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ExecStatusEntity;
import org.apache.airavata.orchestration.model.JobEntity;
import org.apache.airavata.orchestration.model.JobPK;
import org.apache.airavata.orchestration.model.ProcessEntity;
import org.apache.airavata.orchestration.model.TaskEntity;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecStatusRepository extends AbstractRepository<ExecStatusEntity, ExecStatusEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ExecStatusRepository.class);

    public ExecStatusRepository() {
        super(ExecStatusEntity.class, ExecStatusEntity.class);
    }

    @Override
    protected ExecStatusEntity toModel(ExecStatusEntity entity) {
        return entity;
    }

    @Override
    protected ExecStatusEntity toEntity(ExecStatusEntity model) {
        return model;
    }

    // ==========================================================================
    // Process Status
    // ==========================================================================

    public String addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId().isEmpty()) {
            processStatus = processStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"))
                    .build();
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.processStatusToEntity(processStatus);
        entity.setEntityType("PROCESS");
        entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        addToProcess(entity, processId);
        return entity.getStatusId();
    }

    public String updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId().isEmpty()) {
            ProcessStatus current = getProcessStatus(processId);
            if (current == null || current.getState() != processStatus.getState()) {
                processStatus = processStatus.toBuilder()
                        .setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"))
                        .build();
            } else {
                processStatus = processStatus.toBuilder()
                        .setStatusId(current.getStatusId())
                        .build();
            }
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.processStatusToEntity(processStatus);
        entity.setEntityType("PROCESS");
        if (entity.getTimeOfStateChange() == null) {
            entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        }
        mergeInProcess(entity, processId);
        return entity.getStatusId();
    }

    private void addToProcess(ExecStatusEntity entity, String processId) throws RegistryException {
        execute(em -> {
            ProcessEntity processEntity = em.find(ProcessEntity.class, processId);
            if (processEntity != null) {
                if (processEntity.getProcessStatuses() == null) {
                    processEntity.setProcessStatuses(new ArrayList<>());
                }
                processEntity.getProcessStatuses().add(entity);
                em.merge(processEntity);
            }
            return null;
        });
    }

    private void mergeInProcess(ExecStatusEntity entity, String processId) throws RegistryException {
        execute(em -> {
            ProcessEntity processEntity = em.find(ProcessEntity.class, processId);
            if (processEntity != null) {
                if (processEntity.getProcessStatuses() == null) {
                    processEntity.setProcessStatuses(new ArrayList<>());
                }
                boolean found = false;
                for (ExecStatusEntity se : processEntity.getProcessStatuses()) {
                    if (entity.getStatusId() != null && entity.getStatusId().equals(se.getStatusId())) {
                        se.setState(entity.getState());
                        se.setTimeOfStateChange(entity.getTimeOfStateChange());
                        se.setReason(entity.getReason());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    processEntity.getProcessStatuses().add(entity);
                }
                em.merge(processEntity);
            }
            return null;
        });
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        List<ProcessStatus> list = processModel.getProcessStatusesList();
        if (list.isEmpty()) return null;
        ProcessStatus latest = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            Timestamp t = new Timestamp(list.get(i).getTimeOfStateChange());
            Timestamp tLatest = new Timestamp(latest.getTimeOfStateChange());
            if (t.after(tLatest)
                    || (t.equals(tLatest) && list.get(i).getState() == ProcessState.PROCESS_STATE_COMPLETED)
                    || (t.equals(tLatest) && list.get(i).getState() == ProcessState.PROCESS_STATE_FAILED)
                    || (t.equals(tLatest) && list.get(i).getState() == ProcessState.PROCESS_STATE_CANCELED)) {
                latest = list.get(i);
            }
        }
        return latest;
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.getProcess(processId).getProcessStatusesList();
    }

    public List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit)
            throws RegistryException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(DBConstants.ProcessStatus.STATE, processState.name());
        return select(QueryConstants.FIND_PROCESS_WITH_STATUS, limit, offset, queryMap).stream()
                .map(e -> ExecutionMapper.INSTANCE.processStatusToModel(e))
                .collect(Collectors.toList());
    }

    // ==========================================================================
    // Task Status
    // ==========================================================================

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId().isEmpty()) {
            taskStatus = taskStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("TASK_STATE"))
                    .build();
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.taskStatusToEntity(taskStatus);
        entity.setEntityType("TASK");
        entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        final String statusId = taskStatus.getStatusId();
        execute(em -> {
            TaskEntity taskEntity = em.find(TaskEntity.class, taskId);
            if (taskEntity != null) {
                if (taskEntity.getTaskStatuses() == null) {
                    taskEntity.setTaskStatuses(new ArrayList<>());
                }
                taskEntity.getTaskStatuses().add(entity);
                em.merge(taskEntity);
            }
            return null;
        });
        return statusId;
    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId().isEmpty()) {
            taskStatus = taskStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("TASK_STATE"))
                    .build();
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.taskStatusToEntity(taskStatus);
        entity.setEntityType("TASK");
        if (entity.getTimeOfStateChange() == null) {
            entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        }
        final String statusId = taskStatus.getStatusId();
        execute(em -> {
            TaskEntity taskEntity = em.find(TaskEntity.class, taskId);
            if (taskEntity != null) {
                if (taskEntity.getTaskStatuses() == null) {
                    taskEntity.setTaskStatuses(new ArrayList<>());
                }
                boolean found = false;
                for (ExecStatusEntity se : taskEntity.getTaskStatuses()) {
                    if (statusId.equals(se.getStatusId())) {
                        se.setState(entity.getState());
                        se.setTimeOfStateChange(entity.getTimeOfStateChange());
                        se.setReason(entity.getReason());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    taskEntity.getTaskStatuses().add(entity);
                }
                em.merge(taskEntity);
            }
            return null;
        });
        return statusId;
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        List<TaskStatus> list = taskModel.getTaskStatusesList();
        if (list.isEmpty()) return null;
        TaskStatus latest = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            Timestamp t = new Timestamp(list.get(i).getTimeOfStateChange());
            Timestamp tLatest = new Timestamp(latest.getTimeOfStateChange());
            if (t.after(tLatest)
                    || (t.equals(tLatest) && list.get(i).getState() == TaskState.TASK_STATE_COMPLETED)
                    || (t.equals(tLatest) && list.get(i).getState() == TaskState.TASK_STATE_FAILED)
                    || (t.equals(tLatest) && list.get(i).getState() == TaskState.TASK_STATE_CANCELED)) {
                latest = list.get(i);
            }
        }
        return latest;
    }

    // ==========================================================================
    // Job Status
    // ==========================================================================

    public String addJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        if (jobStatus.getStatusId().isEmpty()) {
            jobStatus = jobStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("JOB_STATE"))
                    .build();
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.jobStatusToEntity(jobStatus);
        entity.setEntityType("JOB");
        entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        final String statusId = jobStatus.getStatusId();
        execute(em -> {
            JobEntity jobEntity = em.find(JobEntity.class, jobPK);
            if (jobEntity != null) {
                if (jobEntity.getJobStatuses() == null) {
                    jobEntity.setJobStatuses(new ArrayList<>());
                }
                jobEntity.getJobStatuses().add(entity);
                em.merge(jobEntity);
            }
            return null;
        });
        return statusId;
    }

    public String updateJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        if (jobStatus.getStatusId().isEmpty()) {
            jobStatus = jobStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("JOB_STATE"))
                    .build();
        }
        ExecStatusEntity entity = ExecutionMapper.INSTANCE.jobStatusToEntity(jobStatus);
        entity.setEntityType("JOB");
        if (entity.getTimeOfStateChange() == null) {
            entity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
        }
        final String statusId = jobStatus.getStatusId();
        execute(em -> {
            JobEntity jobEntity = em.find(JobEntity.class, jobPK);
            if (jobEntity != null) {
                if (jobEntity.getJobStatuses() == null) {
                    jobEntity.setJobStatuses(new ArrayList<>());
                }
                boolean found = false;
                for (ExecStatusEntity se : jobEntity.getJobStatuses()) {
                    if (statusId.equals(se.getStatusId())) {
                        se.setState(entity.getState());
                        se.setTimeOfStateChange(entity.getTimeOfStateChange());
                        se.setReason(entity.getReason());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    jobEntity.getJobStatuses().add(entity);
                }
                em.merge(jobEntity);
            }
            return null;
        });
        return statusId;
    }

    public JobStatus getJobStatus(JobPK jobPK) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        JobModel jobModel = jobRepository.getJob(jobPK);
        List<JobStatus> list = jobModel.getJobStatusesList();
        if (list.isEmpty()) return null;
        JobStatus latest = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            Timestamp t = new Timestamp(list.get(i).getTimeOfStateChange());
            Timestamp tLatest = new Timestamp(latest.getTimeOfStateChange());
            if (t.after(tLatest)
                    || (t.equals(tLatest) && list.get(i).getJobState() == JobState.COMPLETE)
                    || (t.equals(tLatest) && list.get(i).getJobState() == JobState.FAILED)
                    || (t.equals(tLatest) && list.get(i).getJobState() == JobState.CANCELED)) {
                latest = list.get(i);
            }
        }
        return latest;
    }

    public List<JobStatus> getDistinctListofJobStatus(String gatewayId, String status, double time) {
        List<ExecStatusEntity> entities = selectWithNativeQuery(
                QueryConstants.FIND_JOB_COUNT_NATIVE_QUERY, gatewayId, status, String.valueOf(time));
        return entities.stream()
                .map(e -> ExecutionMapper.INSTANCE.jobStatusToModel(e))
                .collect(Collectors.toList());
    }
}
