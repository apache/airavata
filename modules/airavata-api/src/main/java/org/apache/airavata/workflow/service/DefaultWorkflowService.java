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
package org.apache.airavata.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.workflow.entity.WorkflowEntity;
import org.apache.airavata.workflow.entity.WorkflowRunEntity;
import org.apache.airavata.workflow.model.Workflow;
import org.apache.airavata.workflow.model.WorkflowRun;
import org.apache.airavata.workflow.model.WorkflowRunStepState;
import org.apache.airavata.workflow.model.WorkflowStep;
import org.apache.airavata.workflow.repository.WorkflowRepository;
import org.apache.airavata.workflow.repository.WorkflowRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultWorkflowService implements WorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowService.class);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final WorkflowRepository workflowRepository;
    private final WorkflowRunRepository workflowRunRepository;

    public DefaultWorkflowService(WorkflowRepository workflowRepository, WorkflowRunRepository workflowRunRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowRunRepository = workflowRunRepository;
    }

    // -------------------------------------------------------------------------
    // Workflow CRUD
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        workflow.setWorkflowId(IdGenerator.getId("WORKFLOW"));
        var entity = toEntity(workflow);
        var saved = workflowRepository.save(entity);
        logger.debug("Created workflow {} for project {}", saved.getWorkflowId(), saved.getProjectId());
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Workflow getWorkflow(String workflowId) {
        var entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) {
            logger.debug("Workflow not found: {}", workflowId);
            return null;
        }
        return toModel(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workflow> getWorkflowsByProject(String projectId, String gatewayId) {
        return workflowRepository.findByProjectIdAndGatewayId(projectId, gatewayId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workflow> getWorkflowsByUser(String userName, String gatewayId) {
        return workflowRepository.findByUserNameAndGatewayId(userName, gatewayId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Workflow updateWorkflow(String workflowId, Workflow workflow) {
        var entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) {
            logger.warn("Cannot update workflow {}: not found", workflowId);
            return null;
        }
        entity.setWorkflowName(workflow.getWorkflowName());
        entity.setDescription(workflow.getDescription());
        entity.setSteps(workflow.getSteps());
        entity.setEdges(workflow.getEdges());
        var saved = workflowRepository.save(entity);
        logger.debug("Updated workflow {}", workflowId);
        return toModel(saved);
    }

    @Override
    @Transactional
    public void deleteWorkflow(String workflowId) {
        workflowRepository.deleteById(workflowId);
        logger.debug("Deleted workflow {}", workflowId);
    }

    // -------------------------------------------------------------------------
    // Workflow Run
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public WorkflowRun createRun(String workflowId, String userName) {
        var workflowEntity = workflowRepository.findById(workflowId).orElse(null);
        if (workflowEntity == null) {
            logger.warn("Cannot create run for workflow {}: workflow not found", workflowId);
            return null;
        }

        List<WorkflowStep> steps = workflowEntity.getSteps();

        Map<String, WorkflowRunStepState> stepStates = new HashMap<>();
        if (steps != null) {
            for (var step : steps) {
                var state = new WorkflowRunStepState();
                state.setStatus(STATUS_PENDING);
                stepStates.put(step.getStepId(), state);
            }
        }

        var entity = new WorkflowRunEntity();
        entity.setRunId(IdGenerator.getId("WORKFLOW_RUN"));
        entity.setWorkflowId(workflowId);
        entity.setUserName(userName);
        entity.setStatus(STATUS_CREATED);
        entity.setStepStates(stepStates);

        var saved = workflowRunRepository.save(entity);
        logger.debug("Created run {} for workflow {}", saved.getRunId(), workflowId);
        return toRunModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowRun getRun(String runId) {
        var entity = workflowRunRepository.findById(runId).orElse(null);
        if (entity == null) {
            logger.debug("WorkflowRun not found: {}", runId);
            return null;
        }
        return toRunModel(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowRun> getRunsByWorkflow(String workflowId) {
        return workflowRunRepository.findByWorkflowIdOrderByCreatedAtDesc(workflowId).stream()
                .map(this::toRunModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkflowRun updateRunStepState(String runId, String stepId, String experimentId, String status) {
        var entity = workflowRunRepository.findById(runId).orElse(null);
        if (entity == null) {
            logger.warn("Cannot update step state for run {}: not found", runId);
            return null;
        }

        Map<String, WorkflowRunStepState> stepStates = entity.getStepStates();
        if (stepStates == null) {
            stepStates = new HashMap<>();
        }

        var stepState = stepStates.computeIfAbsent(stepId, k -> new WorkflowRunStepState());
        stepState.setExperimentId(experimentId);
        stepState.setStatus(status);
        entity.setStepStates(stepStates);

        entity.setStatus(deriveRunStatus(stepStates));

        var saved = workflowRunRepository.save(entity);
        logger.debug("Updated step {} of run {} to status {}", stepId, runId, status);
        return toRunModel(saved);
    }

    @Override
    @Transactional
    public WorkflowRun updateRunStatus(String runId, String status) {
        var entity = workflowRunRepository.findById(runId).orElse(null);
        if (entity == null) {
            logger.warn("Cannot update status for run {}: not found", runId);
            return null;
        }
        entity.setStatus(status);
        var saved = workflowRunRepository.save(entity);
        logger.debug("Updated run {} status to {}", runId, status);
        return toRunModel(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers: entity <-> model conversion
    // -------------------------------------------------------------------------

    private Workflow toModel(WorkflowEntity entity) {
        var model = new Workflow();
        model.setWorkflowId(entity.getWorkflowId());
        model.setProjectId(entity.getProjectId());
        model.setGatewayId(entity.getGatewayId());
        model.setUserName(entity.getUserName());
        model.setWorkflowName(entity.getWorkflowName());
        model.setDescription(entity.getDescription());
        model.setSteps(entity.getSteps());
        model.setEdges(entity.getEdges());
        if (entity.getCreatedAt() != null) {
            model.setCreationTime(entity.getCreatedAt().getTime());
        }
        if (entity.getUpdatedAt() != null) {
            model.setUpdateTime(entity.getUpdatedAt().getTime());
        }
        return model;
    }

    private WorkflowEntity toEntity(Workflow model) {
        var entity = new WorkflowEntity();
        entity.setWorkflowId(model.getWorkflowId());
        entity.setProjectId(model.getProjectId());
        entity.setGatewayId(model.getGatewayId());
        entity.setUserName(model.getUserName());
        entity.setWorkflowName(model.getWorkflowName());
        entity.setDescription(model.getDescription());
        entity.setSteps(model.getSteps());
        entity.setEdges(model.getEdges());
        return entity;
    }

    private WorkflowRun toRunModel(WorkflowRunEntity entity) {
        var model = new WorkflowRun();
        model.setRunId(entity.getRunId());
        model.setWorkflowId(entity.getWorkflowId());
        model.setUserName(entity.getUserName());
        model.setStatus(entity.getStatus());
        model.setStepStates(entity.getStepStates());
        if (entity.getCreatedAt() != null) {
            model.setCreationTime(entity.getCreatedAt().getTime());
        }
        if (entity.getUpdatedAt() != null) {
            model.setUpdateTime(entity.getUpdatedAt().getTime());
        }
        return model;
    }

    // -------------------------------------------------------------------------
    // Private helpers: run status derivation
    // -------------------------------------------------------------------------

    /**
     * Derives the aggregate run status from the current step states.
     *
     * <ul>
     *   <li>Any step FAILED → FAILED</li>
     *   <li>Any step RUNNING or PENDING (with at least one non-PENDING step present) → RUNNING</li>
     *   <li>All steps COMPLETED → COMPLETED</li>
     *   <li>All steps PENDING → CREATED</li>
     * </ul>
     */
    private String deriveRunStatus(Map<String, WorkflowRunStepState> stepStates) {
        if (stepStates == null || stepStates.isEmpty()) {
            return STATUS_CREATED;
        }

        var statuses = stepStates.values().stream()
                .map(WorkflowRunStepState::getStatus)
                .toList();

        if (statuses.stream().anyMatch(STATUS_FAILED::equals)) {
            return STATUS_FAILED;
        }
        if (statuses.stream().allMatch(STATUS_COMPLETED::equals)) {
            return STATUS_COMPLETED;
        }
        if (statuses.stream().allMatch(STATUS_PENDING::equals)) {
            return STATUS_CREATED;
        }
        // Mix of pending/completed/running/other = in progress
        return STATUS_RUNNING;
    }
}
