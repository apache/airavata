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
package org.apache.airavata.registry.services;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified status service that consolidates status management for all entity types.
 *
 * <p>This service replaces the functionality of:
 * <ul>
 *   <li>{@code ExperimentStatusService}</li>
 *   <li>{@code ProcessStatusService}</li>
 *   <li>{@code TaskStatusService}</li>
 *   <li>{@code JobStatusService}</li>
 *   <li>{@code QueueStatusService}</li>
 * </ul>
 *
 * <p>It uses the unified {@link StatusEntity} to store status history with a discriminator
 * for the parent type (experiment, process, task, job, queue, workflow, application, handler).
 */
@Service
@Transactional
public class StatusService {

    private final StatusRepository statusRepository;
    private final EntityManager entityManager;

    public StatusService(StatusRepository statusRepository, EntityManager entityManager) {
        this.statusRepository = statusRepository;
        this.entityManager = entityManager;
    }

    // ========== Generic Status Operations ==========

    /**
     * Add a status for a specific parent entity.
     *
     * @param statusId the status ID
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @param state the state value as string
     * @param reason the reason for the state change
     * @return the saved status entity
     */
    public StatusEntity addStatus(String statusId, String parentId, StatusParentType parentType,
                                  String state, String reason) {
        StatusEntity entity = new StatusEntity();
        entity.setStatusId(statusId);
        entity.setParentId(parentId);
        entity.setParentType(parentType);
        entity.setState(state);
        entity.setReason(reason);
        entity.setSequenceNum(statusRepository.getNextSequenceNum(parentId, parentType));
        return statusRepository.save(entity);
    }

    /**
     * Get all statuses for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return list of status entities
     */
    public List<StatusEntity> getStatuses(String parentId, StatusParentType parentType) {
        entityManager.flush();
        return statusRepository.findByParentIdAndParentType(parentId, parentType);
    }

    /**
     * Assigns DB-backed sequence numbers to status entities that have null sequenceNum.
     * Call before persisting (e.g. before cascade save from Experiment/Process/Task).
     *
     * @param statuses list of status entities (may contain existing with sequenceNum already set)
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    public void assignSequenceNumbersForNewStatuses(
            List<StatusEntity> statuses, String parentId, StatusParentType parentType) {
        if (statuses == null) return;
        for (StatusEntity s : statuses) {
            if (s.getSequenceNum() == null) {
                s.setSequenceNum(statusRepository.getNextSequenceNum(parentId, parentType));
            }
        }
    }

    /**
     * Get the latest status for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return the latest status entity, if any
     */
    public Optional<StatusEntity> getLatestStatus(String parentId, StatusParentType parentType) {
        entityManager.flush();
        return statusRepository.findLatestByParentIdAndParentType(parentId, parentType);
    }

    /**
     * Delete all statuses for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    public void deleteStatuses(String parentId, StatusParentType parentType) {
        statusRepository.deleteByParentIdAndParentType(parentId, parentType);
    }

    // ========== Experiment Status Operations ==========

    /**
     * Add an experiment status.
     *
     * @param status the experiment status
     * @param experimentId the experiment ID
     * @return the status ID
     * @throws RegistryException if the operation fails
     */
    public String addExperimentStatus(ExperimentStatus status, String experimentId) throws RegistryException {
        StatusEntity entity = toEntity(status, experimentId, StatusParentType.EXPERIMENT);
        entity.setSequenceNum(statusRepository.getNextSequenceNum(experimentId, StatusParentType.EXPERIMENT));
        StatusEntity saved = statusRepository.save(entity);
        entityManager.flush();
        return saved.getStatusId();
    }

    /**
     * Get the latest experiment status.
     *
     * @param experimentId the experiment ID
     * @return the latest experiment status, or null if none
     * @throws RegistryException if the operation fails
     */
    public ExperimentStatus getLatestExperimentStatus(String experimentId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findLatestByExperimentId(experimentId)
                .map(this::toExperimentStatus)
                .orElse(null);
    }

    /**
     * Get all experiment statuses.
     *
     * @param experimentId the experiment ID
     * @return list of experiment statuses
     * @throws RegistryException if the operation fails
     */
    public List<ExperimentStatus> getExperimentStatuses(String experimentId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findByExperimentId(experimentId).stream()
                .map(this::toExperimentStatus)
                .collect(Collectors.toList());
    }

    // ========== Process Status Operations ==========

    /**
     * Add a process status.
     *
     * @param status the process status
     * @param processId the process ID
     * @return the status ID
     * @throws RegistryException if the operation fails
     */
    public String addProcessStatus(ProcessStatus status, String processId) throws RegistryException {
        StatusEntity entity = toEntity(status, processId, StatusParentType.PROCESS);
        entity.setSequenceNum(statusRepository.getNextSequenceNum(processId, StatusParentType.PROCESS));
        StatusEntity saved = statusRepository.save(entity);
        entityManager.flush();
        return saved.getStatusId();
    }

    /**
     * Get the latest process status.
     *
     * @param processId the process ID
     * @return the latest process status, or null if none
     * @throws RegistryException if the operation fails
     */
    public ProcessStatus getLatestProcessStatus(String processId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findLatestByProcessId(processId)
                .map(this::toProcessStatus)
                .orElse(null);
    }

    /**
     * Get all process statuses.
     *
     * @param processId the process ID
     * @return list of process statuses
     * @throws RegistryException if the operation fails
     */
    public List<ProcessStatus> getProcessStatuses(String processId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findByProcessId(processId).stream()
                .map(this::toProcessStatus)
                .collect(Collectors.toList());
    }

    // ========== Task Status Operations ==========

    /**
     * Add a task status.
     *
     * @param status the task status
     * @param taskId the task ID
     * @return the status ID
     * @throws RegistryException if the operation fails
     */
    public String addTaskStatus(TaskStatus status, String taskId) throws RegistryException {
        StatusEntity entity = toEntity(status, taskId, StatusParentType.TASK);
        StatusEntity saved = statusRepository.save(entity);
        entityManager.flush();
        return saved.getStatusId();
    }

    /**
     * Get the latest task status.
     *
     * @param taskId the task ID
     * @return the latest task status, or null if none
     * @throws RegistryException if the operation fails
     */
    public TaskStatus getLatestTaskStatus(String taskId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findLatestByTaskId(taskId)
                .map(this::toTaskStatus)
                .orElse(null);
    }

    /**
     * Get all task statuses.
     *
     * @param taskId the task ID
     * @return list of task statuses
     * @throws RegistryException if the operation fails
     */
    public List<TaskStatus> getTaskStatuses(String taskId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findByTaskId(taskId).stream()
                .map(this::toTaskStatus)
                .collect(Collectors.toList());
    }

    // ========== Job Status Operations ==========

    /**
     * Add a job status.
     *
     * @param status the job status
     * @param jobId the job ID
     * @return the status ID
     * @throws RegistryException if the operation fails
     */
    public String addJobStatus(JobStatus status, String jobId) throws RegistryException {
        StatusEntity entity = toEntity(status, jobId, StatusParentType.JOB);
        entity.setSequenceNum(statusRepository.getNextSequenceNum(jobId, StatusParentType.JOB));
        StatusEntity saved = statusRepository.save(entity);
        entityManager.flush();
        return saved.getStatusId();
    }

    /**
     * Get the latest job status.
     *
     * @param jobId the job ID
     * @return the latest job status, or null if none
     * @throws RegistryException if the operation fails
     */
    public JobStatus getLatestJobStatus(String jobId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findLatestByJobId(jobId)
                .map(this::toJobStatus)
                .orElse(null);
    }

    /**
     * Get all job statuses.
     *
     * @param jobId the job ID
     * @return list of job statuses
     * @throws RegistryException if the operation fails
     */
    public List<JobStatus> getJobStatuses(String jobId) throws RegistryException {
        entityManager.flush();
        return statusRepository.findByJobId(jobId).stream()
                .map(this::toJobStatus)
                .collect(Collectors.toList());
    }

    // ========== Conversion Methods ==========

    private StatusEntity toEntity(ExperimentStatus status, String experimentId, StatusParentType parentType) {
        StatusEntity entity = new StatusEntity();
        entity.setStatusId(java.util.UUID.randomUUID().toString());
        entity.setParentId(experimentId);
        entity.setParentType(parentType);
        entity.setState(status.getState() != null ? status.getState().name() : null);
        entity.setReason(status.getReason());
        // Always set timestamp immediately to ensure correct ordering
        entity.setTimeOfStateChange(status.getTimeOfStateChange() > 0
                ? new java.sql.Timestamp(status.getTimeOfStateChange())
                : AiravataUtils.getUniqueTimestamp());
        return entity;
    }

    private StatusEntity toEntity(ProcessStatus status, String processId, StatusParentType parentType) {
        StatusEntity entity = new StatusEntity();
        entity.setStatusId(java.util.UUID.randomUUID().toString());
        entity.setParentId(processId);
        entity.setParentType(parentType);
        entity.setState(status.getState() != null ? status.getState().name() : null);
        entity.setReason(status.getReason());
        // Always set timestamp immediately to ensure correct ordering
        entity.setTimeOfStateChange(status.getTimeOfStateChange() > 0
                ? new java.sql.Timestamp(status.getTimeOfStateChange())
                : AiravataUtils.getUniqueTimestamp());
        return entity;
    }

    private StatusEntity toEntity(TaskStatus status, String taskId, StatusParentType parentType) {
        StatusEntity entity = new StatusEntity();
        entity.setStatusId(java.util.UUID.randomUUID().toString());
        entity.setParentId(taskId);
        entity.setParentType(parentType);
        entity.setState(status.getState() != null ? status.getState().name() : null);
        entity.setReason(status.getReason());
        // Always set timestamp immediately to ensure correct ordering
        entity.setTimeOfStateChange(status.getTimeOfStateChange() > 0
                ? new java.sql.Timestamp(status.getTimeOfStateChange())
                : AiravataUtils.getUniqueTimestamp());
        return entity;
    }

    private StatusEntity toEntity(JobStatus status, String jobId, StatusParentType parentType) {
        StatusEntity entity = new StatusEntity();
        entity.setStatusId(java.util.UUID.randomUUID().toString());
        entity.setParentId(jobId);
        entity.setParentType(parentType);
        entity.setState(status.getJobState() != null ? status.getJobState().name() : null);
        entity.setReason(status.getReason());
        // Always set timestamp immediately to ensure correct ordering
        entity.setTimeOfStateChange(status.getTimeOfStateChange() > 0
                ? new java.sql.Timestamp(status.getTimeOfStateChange())
                : AiravataUtils.getUniqueTimestamp());
        return entity;
    }

    private ExperimentStatus toExperimentStatus(StatusEntity entity) {
        ExperimentStatus status = new ExperimentStatus();
        if (entity.getState() != null) {
            try {
                status.setState(ExperimentState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                // Handle unknown state gracefully
                status.setState(null);
            }
        }
        status.setReason(entity.getReason());
        if (entity.getTimeOfStateChange() != null) {
            status.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        }
        return status;
    }

    private ProcessStatus toProcessStatus(StatusEntity entity) {
        ProcessStatus status = new ProcessStatus();
        if (entity.getState() != null) {
            try {
                status.setState(ProcessState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                status.setState(null);
            }
        }
        status.setReason(entity.getReason());
        if (entity.getTimeOfStateChange() != null) {
            status.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        }
        return status;
    }

    private TaskStatus toTaskStatus(StatusEntity entity) {
        TaskStatus status = new TaskStatus();
        if (entity.getState() != null) {
            try {
                status.setState(TaskState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                status.setState(null);
            }
        }
        status.setReason(entity.getReason());
        if (entity.getTimeOfStateChange() != null) {
            status.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        }
        return status;
    }

    private JobStatus toJobStatus(StatusEntity entity) {
        JobStatus status = new JobStatus();
        if (entity.getState() != null) {
            try {
                status.setJobState(JobState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                status.setJobState(null);
            }
        }
        status.setReason(entity.getReason());
        if (entity.getTimeOfStateChange() != null) {
            status.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        }
        return status;
    }

    // ========== Status Update Methods ==========

    /**
     * Update an experiment status (adds a new status entry).
     *
     * @param status the experiment status
     * @param experimentId the experiment ID
     * @throws RegistryException if the operation fails
     */
    public void updateExperimentStatus(ExperimentStatus status, String experimentId) throws RegistryException {
        addExperimentStatus(status, experimentId);
    }

    /**
     * Update a process status (adds a new status entry).
     *
     * @param status the process status
     * @param processId the process ID
     * @throws RegistryException if the operation fails
     */
    public void updateProcessStatus(ProcessStatus status, String processId) throws RegistryException {
        addProcessStatus(status, processId);
    }

    /**
     * Update a task status (adds a new status entry).
     *
     * @param status the task status
     * @param taskId the task ID
     * @throws RegistryException if the operation fails
     */
    public void updateTaskStatus(TaskStatus status, String taskId) throws RegistryException {
        addTaskStatus(status, taskId);
    }

    // ========== Queue Status Operations ==========

    /**
     * Get the latest queue statuses.
     *
     * @return list of queue status models
     * @throws RegistryException if the operation fails
     */
    public List<org.apache.airavata.common.model.QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        entityManager.flush();
        return statusRepository.findAllQueueStatuses().stream()
                .map(this::toQueueStatusModel)
                .collect(Collectors.toList());
    }

    /**
     * Register queue statuses.
     *
     * @param queueStatuses list of queue status models
     * @throws RegistryException if the operation fails
     */
    public void createQueueStatuses(List<org.apache.airavata.common.model.QueueStatusModel> queueStatuses) throws RegistryException {
        for (org.apache.airavata.common.model.QueueStatusModel queueStatus : queueStatuses) {
            StatusEntity entity = toEntity(queueStatus);
            String parentId = queueStatus.getHostName() + ":" + queueStatus.getQueueName();
            entity.setSequenceNum(statusRepository.getNextSequenceNum(parentId, StatusParentType.QUEUE));
            statusRepository.save(entity);
        }
    }

    /**
     * Get a specific queue status.
     *
     * @param hostName the host name
     * @param queueName the queue name
     * @return the queue status model, or null if not found
     * @throws RegistryException if the operation fails
     */
    public org.apache.airavata.common.model.QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryException {
        entityManager.flush();
        String parentId = hostName + ":" + queueName;
        return statusRepository.findLatestByParentIdAndParentType(parentId, StatusParentType.QUEUE)
                .map(this::toQueueStatusModel)
                .orElse(null);
    }

    private StatusEntity toEntity(org.apache.airavata.common.model.QueueStatusModel queueStatus) {
        StatusEntity entity = new StatusEntity();
        String parentId = queueStatus.getHostName() + ":" + queueStatus.getQueueName();
        entity.setStatusId(java.util.UUID.randomUUID().toString());
        entity.setParentId(parentId);
        entity.setParentType(StatusParentType.QUEUE);
        // Store queue state info as JSON-like string
        String state = String.format("UP=%b,RUNNING=%d,QUEUED=%d", 
                queueStatus.getQueueUp(), queueStatus.getRunningJobs(), queueStatus.getQueuedJobs());
        entity.setState(state);
        if (queueStatus.getTime() > 0) {
            entity.setTimeOfStateChange(new java.sql.Timestamp(queueStatus.getTime()));
        }
        return entity;
    }

    private org.apache.airavata.common.model.QueueStatusModel toQueueStatusModel(StatusEntity entity) {
        org.apache.airavata.common.model.QueueStatusModel model = new org.apache.airavata.common.model.QueueStatusModel();
        // Parse parentId which is "hostName:queueName"
        String[] parts = entity.getParentId().split(":", 2);
        if (parts.length == 2) {
            model.setHostName(parts[0]);
            model.setQueueName(parts[1]);
        }
        // Parse state which is "UP=true,RUNNING=5,QUEUED=10"
        if (entity.getState() != null) {
            String state = entity.getState();
            if (state.contains("UP=")) {
                model.setQueueUp(state.contains("UP=true"));
            }
            java.util.regex.Matcher runningMatcher = java.util.regex.Pattern.compile("RUNNING=(\\d+)").matcher(state);
            if (runningMatcher.find()) {
                model.setRunningJobs(Integer.parseInt(runningMatcher.group(1)));
            }
            java.util.regex.Matcher queuedMatcher = java.util.regex.Pattern.compile("QUEUED=(\\d+)").matcher(state);
            if (queuedMatcher.find()) {
                model.setQueuedJobs(Integer.parseInt(queuedMatcher.group(1)));
            }
        }
        if (entity.getTimeOfStateChange() != null) {
            model.setTime(entity.getTimeOfStateChange().getTime());
        }
        return model;
    }
}
