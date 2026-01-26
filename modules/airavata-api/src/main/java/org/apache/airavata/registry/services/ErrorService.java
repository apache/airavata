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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ErrorParentType;
import org.apache.airavata.registry.entities.ErrorEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.ErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified error service that consolidates error management for all entity types.
 *
 * <p>This service replaces the functionality of:
 * <ul>
 *   <li>{@code ExperimentErrorService}</li>
 *   <li>{@code ProcessErrorService}</li>
 *   <li>{@code TaskErrorService}</li>
 * </ul>
 *
 * <p>It uses the unified {@link ErrorEntity} to store errors with a discriminator
 * for the parent type (experiment, process, task, workflow, application, handler).
 */
@Service
@Transactional
public class ErrorService {

    private final ErrorRepository errorRepository;

    public ErrorService(ErrorRepository errorRepository) {
        this.errorRepository = errorRepository;
    }

    // ========== Generic Error Operations ==========

    /**
     * Add an error for a specific parent entity.
     *
     * @param error The error model to persist
     * @param parentId The ID of the parent entity
     * @param parentType The type of parent entity
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addError(ErrorModel error, String parentId, ErrorParentType parentType) throws RegistryException {
        ErrorEntity entity = toEntity(error, parentId, parentType);
        ErrorEntity saved = errorRepository.save(entity);
        return saved.getErrorId();
    }

    /**
     * Update an existing error.
     *
     * @param error The error model with updated information
     * @param parentId The ID of the parent entity
     * @param parentType The type of parent entity
     * @throws RegistryException if the operation fails
     */
    public void updateError(ErrorModel error, String parentId, ErrorParentType parentType) throws RegistryException {
        ErrorEntity entity = toEntity(error, parentId, parentType);
        errorRepository.save(entity);
    }

    /**
     * Retrieve all errors for a specific parent entity.
     *
     * @param parentId The ID of the parent entity
     * @param parentType The type of parent entity
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getErrors(String parentId, ErrorParentType parentType) throws RegistryException {
        List<ErrorEntity> entities = errorRepository.findByParentIdAndParentType(parentId, parentType);
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    /**
     * Delete all errors for a specific parent entity.
     *
     * @param parentId The ID of the parent entity
     * @param parentType The type of parent entity
     */
    public void deleteErrors(String parentId, ErrorParentType parentType) {
        errorRepository.deleteByParentIdAndParentType(parentId, parentType);
    }

    /**
     * Check if any errors exist for a specific parent entity.
     *
     * @param parentId The ID of the parent entity
     * @param parentType The type of parent entity
     * @return true if errors exist
     */
    @Transactional(readOnly = true)
    public boolean hasErrors(String parentId, ErrorParentType parentType) {
        return errorRepository.existsByParentIdAndParentType(parentId, parentType);
    }

    // ========== Experiment Error Operations ==========

    /**
     * Add an experiment error.
     *
     * @param error The error model to persist
     * @param experimentId The ID of the experiment
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        return addError(error, experimentId, ErrorParentType.EXPERIMENT);
    }

    /**
     * Update an experiment error.
     *
     * @param error The error model with updated information
     * @param experimentId The ID of the experiment
     * @throws RegistryException if the operation fails
     */
    public void updateExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        updateError(error, experimentId, ErrorParentType.EXPERIMENT);
    }

    /**
     * Retrieve all errors for an experiment.
     *
     * @param experimentId The ID of the experiment
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        return getErrors(experimentId, ErrorParentType.EXPERIMENT);
    }

    // ========== Process Error Operations ==========

    /**
     * Add a process error.
     *
     * @param error The error model to persist
     * @param processId The ID of the process
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addProcessError(ErrorModel error, String processId) throws RegistryException {
        return addError(error, processId, ErrorParentType.PROCESS);
    }

    /**
     * Update a process error.
     *
     * @param error The error model with updated information
     * @param processId The ID of the process
     * @throws RegistryException if the operation fails
     */
    public void updateProcessError(ErrorModel error, String processId) throws RegistryException {
        updateError(error, processId, ErrorParentType.PROCESS);
    }

    /**
     * Retrieve all errors for a process.
     *
     * @param processId The ID of the process
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getProcessErrors(String processId) throws RegistryException {
        return getErrors(processId, ErrorParentType.PROCESS);
    }

    // ========== Task Error Operations ==========

    /**
     * Add a task error.
     *
     * @param error The error model to persist
     * @param taskId The ID of the task
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addTaskError(ErrorModel error, String taskId) throws RegistryException {
        return addError(error, taskId, ErrorParentType.TASK);
    }

    /**
     * Update a task error.
     *
     * @param error The error model with updated information
     * @param taskId The ID of the task
     * @throws RegistryException if the operation fails
     */
    public void updateTaskError(ErrorModel error, String taskId) throws RegistryException {
        updateError(error, taskId, ErrorParentType.TASK);
    }

    /**
     * Retrieve all errors for a task.
     *
     * @param taskId The ID of the task
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getTaskErrors(String taskId) throws RegistryException {
        return getErrors(taskId, ErrorParentType.TASK);
    }

    // ========== Workflow Error Operations ==========

    /**
     * Add a workflow error.
     *
     * @param error The error model to persist
     * @param workflowId The ID of the workflow
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addWorkflowError(ErrorModel error, String workflowId) throws RegistryException {
        return addError(error, workflowId, ErrorParentType.WORKFLOW);
    }

    /**
     * Retrieve all errors for a workflow.
     *
     * @param workflowId The ID of the workflow
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getWorkflowErrors(String workflowId) throws RegistryException {
        return getErrors(workflowId, ErrorParentType.WORKFLOW);
    }

    // ========== Application Error Operations ==========

    /**
     * Add an application error.
     *
     * @param error The error model to persist
     * @param applicationId The ID of the application
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addApplicationError(ErrorModel error, String applicationId) throws RegistryException {
        return addError(error, applicationId, ErrorParentType.APPLICATION);
    }

    /**
     * Retrieve all errors for an application.
     *
     * @param applicationId The ID of the application
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getApplicationErrors(String applicationId) throws RegistryException {
        return getErrors(applicationId, ErrorParentType.APPLICATION);
    }

    // ========== Handler Error Operations ==========

    /**
     * Add a handler error.
     *
     * @param error The error model to persist
     * @param handlerId The ID of the handler
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addHandlerError(ErrorModel error, String handlerId) throws RegistryException {
        return addError(error, handlerId, ErrorParentType.HANDLER);
    }

    /**
     * Retrieve all errors for a handler.
     *
     * @param handlerId The ID of the handler
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getHandlerErrors(String handlerId) throws RegistryException {
        return getErrors(handlerId, ErrorParentType.HANDLER);
    }

    // ========== Conversion Methods ==========

    /**
     * Convert ErrorModel to ErrorEntity.
     */
    private ErrorEntity toEntity(ErrorModel model, String parentId, ErrorParentType parentType) {
        ErrorEntity entity = new ErrorEntity();
        entity.setErrorId(model.getErrorId());
        entity.setParentId(parentId);
        entity.setParentType(parentType);
        
        if (model.getCreationTime() > 0) {
            entity.setCreationTime(new java.sql.Timestamp(model.getCreationTime()));
        }
        
        entity.setActualErrorMessage(model.getActualErrorMessage());
        entity.setUserFriendlyMessage(model.getUserFriendlyMessage());
        entity.setTransientOrPersistent(model.getTransientOrPersistent());
        
        if (model.getRootCauseErrorIdList() != null && !model.getRootCauseErrorIdList().isEmpty()) {
            entity.setRootCauseErrorIdList(String.join(",", model.getRootCauseErrorIdList()));
        }
        
        return entity;
    }

    /**
     * Convert ErrorEntity to ErrorModel.
     */
    private ErrorModel toModel(ErrorEntity entity) {
        ErrorModel model = new ErrorModel();
        model.setErrorId(entity.getErrorId());
        
        if (entity.getCreationTime() != null) {
            model.setCreationTime(entity.getCreationTime().getTime());
        }
        
        model.setActualErrorMessage(entity.getActualErrorMessage());
        model.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        model.setTransientOrPersistent(entity.isTransientOrPersistent());
        
        if (entity.getRootCauseErrorIdList() != null && !entity.getRootCauseErrorIdList().isEmpty()) {
            model.setRootCauseErrorIdList(Arrays.asList(entity.getRootCauseErrorIdList().split(",")));
        } else {
            model.setRootCauseErrorIdList(Collections.emptyList());
        }
        
        return model;
    }
}
