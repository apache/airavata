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
package org.apache.airavata.status.service;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.model.EventKind;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.status.entity.EventEntity;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.status.repository.EventRepository;
import org.apache.airavata.status.mapper.StatusMapper;
import org.apache.airavata.execution.model.TaskState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Process-level status service. All status events are scoped to a process.
 *
 * <p>Experiment state is a direct column on the experiment table, mutated by
 * the orchestration layer when process state changes cascade upward.
 *
 * <p>Task and job roles are engulfed by process: task status is stored as process
 * status, and job status is converted to process status before persistence.
 */
@Service
@Transactional
public class DefaultStatusService implements StatusService {

    private final EventRepository eventRepository;
    private final EntityManager entityManager;
    private final StatusMapper statusMapper;

    public DefaultStatusService(EventRepository eventRepository, EntityManager entityManager, StatusMapper statusMapper) {
        this.eventRepository = eventRepository;
        this.entityManager = entityManager;
        this.statusMapper = statusMapper;
    }

    // ========== Process Status ==========

    @Override
    public String addProcessStatus(StatusModel<ProcessState> status, String processId) throws RegistryException {
        entityManager.flush();
        EventEntity entity = toEventEntity(
                status.getState() != null ? status.getState().name() : null,
                status.getReason(),
                status.getTimeOfStateChange(),
                processId);
        var existing = eventRepository.findByParentIdAndEventKindOrderBySequenceNumDesc(processId, EventKind.STATUS);
        entity.setSequenceNum(existing.isEmpty() ? 1L : existing.get(0).getSequenceNum() + 1);
        EventEntity saved = eventRepository.save(entity);
        entityManager.flush();
        return saved.getEventId();
    }

    @Override
    public StatusModel<ProcessState> getLatestProcessStatus(String processId) throws RegistryException {
        entityManager.flush();
        return eventRepository
                .findLatestByParentIdAndEventKind(processId, EventKind.STATUS)
                .map(e -> statusMapper.toStatus(e, ProcessState.class))
                .orElse(null);
    }

    // ========== Task Status (engulfed: taskId = processId) ==========

    @Override
    public String addTaskStatus(StatusModel<TaskState> status, String taskId) throws RegistryException {
        StatusModel<ProcessState> processStatus = taskStatusToProcessStatus(status);
        return addProcessStatus(processStatus, taskId);
    }

    // ========== Job Status (engulfed: jobId = processId for job process) ==========

    @Override
    public String addJobStatus(StatusModel<JobState> status, String jobId) throws RegistryException {
        StatusModel<ProcessState> processStatus = jobStatusToProcessStatus(status);
        return addProcessStatus(processStatus, jobId);
    }

    // ========== Error Operations ==========

    @Override
    public String addProcessError(ErrorModel error, String processId) throws RegistryException {
        return addError(error, processId);
    }

    @Override
    public String addTaskError(ErrorModel error, String taskId) throws RegistryException {
        return addError(error, taskId);
    }

    // ========== Internal Helpers ==========

    private String addError(ErrorModel error, String parentId) {
        EventEntity entity = errorToEventEntity(error, parentId);
        EventEntity saved = eventRepository.save(entity);
        return saved.getEventId();
    }

    private EventEntity errorToEventEntity(ErrorModel model, String processId) {
        EventEntity entity = new EventEntity();
        entity.setEventId(
                model.getErrorId() != null
                        ? model.getErrorId()
                        : java.util.UUID.randomUUID().toString());
        entity.setParentId(processId);
        entity.setEventKind(EventKind.ERROR);
        entity.setEventTime(
                model.getCreationTime() > 0
                        ? new java.sql.Timestamp(model.getCreationTime())
                        : IdGenerator.getUniqueTimestamp());
        entity.setSequenceNum(eventRepository.getNextSequenceNum(processId));
        entity.setActualErrorMessage(model.getActualErrorMessage());
        entity.setUserFriendlyMessage(model.getUserFriendlyMessage());
        entity.setTransientOrPersistent(model.getTransientOrPersistent());
        if (model.getRootCauseErrorIdList() != null
                && !model.getRootCauseErrorIdList().isEmpty()) {
            entity.setRootCauseErrorIdList(String.join(",", model.getRootCauseErrorIdList()));
        }
        return entity;
    }

    private EventEntity toEventEntity(String state, String reason, long timeOfStateChange, String processId) {
        EventEntity entity = new EventEntity();
        entity.setEventId(java.util.UUID.randomUUID().toString());
        entity.setParentId(processId);
        entity.setEventKind(EventKind.STATUS);
        entity.setState(state);
        entity.setReason(reason);
        entity.setEventTime(
                timeOfStateChange > 0 ? new java.sql.Timestamp(timeOfStateChange) : IdGenerator.getUniqueTimestamp());
        return entity;
    }

    private StatusModel<ProcessState> taskStatusToProcessStatus(StatusModel<TaskState> status) {
        StatusModel<ProcessState> ps = new StatusModel<>();
        if (status.getState() != null) {
            ProcessState state =
                    switch (status.getState()) {
                        case CREATED -> ProcessState.CREATED;
                        case EXECUTING -> ProcessState.EXECUTING;
                        case COMPLETED -> ProcessState.COMPLETED;
                        case FAILED -> ProcessState.FAILED;
                        case CANCELED -> ProcessState.CANCELED;
                    };
            ps.setState(state);
        }
        ps.setReason(status.getReason());
        ps.setTimeOfStateChange(
                status.getTimeOfStateChange() > 0
                        ? status.getTimeOfStateChange()
                        : IdGenerator.getCurrentTimestamp().getTime());
        ps.setStatusId(status.getStatusId());
        return ps;
    }

    private StatusModel<ProcessState> jobStatusToProcessStatus(StatusModel<JobState> status) {
        StatusModel<ProcessState> ps = new StatusModel<>();
        if (status.getState() != null) {
            ps.setState(jobStateToProcessState(status.getState()));
        }
        ps.setReason(status.getReason());
        ps.setTimeOfStateChange(
                status.getTimeOfStateChange() > 0
                        ? status.getTimeOfStateChange()
                        : IdGenerator.getCurrentTimestamp().getTime());
        ps.setStatusId(status.getStatusId());
        return ps;
    }

    private static ProcessState jobStateToProcessState(JobState js) {
        return switch (js) {
            case SUBMITTED -> ProcessState.LAUNCHED;
            case QUEUED -> ProcessState.QUEUED;
            case ACTIVE -> ProcessState.EXECUTING;
            case COMPLETED -> ProcessState.COMPLETED;
            case CANCELED -> ProcessState.CANCELED;
            case FAILED -> ProcessState.FAILED;
            case SUSPENDED -> ProcessState.MONITORING;
            case UNKNOWN, NON_CRITICAL_FAIL -> ProcessState.EXECUTING;
        };
    }
}
