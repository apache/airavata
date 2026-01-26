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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.airavata.common.model.AiravataWorkflow;
import org.apache.airavata.common.model.ApplicationState;
import org.apache.airavata.common.model.ApplicationStatus;
import org.apache.airavata.common.model.ComponentType;
import org.apache.airavata.common.model.DataBlock;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.HandlerState;
import org.apache.airavata.common.model.HandlerStatus;
import org.apache.airavata.common.model.HandlerType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.WorkflowApplication;
import org.apache.airavata.common.model.WorkflowConnection;
import org.apache.airavata.common.model.WorkflowExecutionState;
import org.apache.airavata.common.model.WorkflowHandler;
import org.apache.airavata.common.model.WorkflowStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.ErrorEntity;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.entities.OutputDataEntity;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowApplicationEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowConnectionEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowDataBlockEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowHandlerEntity;
import org.apache.airavata.registry.exception.WorkflowCatalogException;
import org.apache.airavata.registry.mappers.AiravataWorkflowMapper;
import org.apache.airavata.registry.repositories.workflowcatalog.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final AiravataWorkflowMapper airavataWorkflowMapper;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            AiravataWorkflowMapper airavataWorkflowMapper) {
        this.workflowRepository = workflowRepository;
        this.airavataWorkflowMapper = airavataWorkflowMapper;
    }

    public void registerWorkflow(AiravataWorkflow workflow, String experimentId) throws WorkflowCatalogException {
        var entity = airavataWorkflowMapper.toEntity(workflow);
        entity.setExperimentId(experimentId);
        // Generate workflow ID if not already set
        if (entity.getId() == null || entity.getId().isEmpty()) {
            var generatedWorkflowId =
                    AiravataUtils.getId(workflow.getDescription() != null ? workflow.getDescription() : "workflow");
            entity.setId(generatedWorkflowId);
        }
        final var workflowId = entity.getId();
        // Manually convert applications from model to entity (mapper ignores them)
        if (workflow.getApplications() != null) {
            var applicationEntities = new ArrayList<WorkflowApplicationEntity>();
            for (var app : workflow.getApplications()) {
                var applicationEntity = convertApplicationToEntity(app);
                // Generate application ID if not set
                if (applicationEntity.getId() == null
                        || applicationEntity.getId().isEmpty()) {
                    var applicationId = AiravataUtils.getId("application");
                    applicationEntity.setId(applicationId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                applicationEntity.setWorkflowId(workflowId);
                applicationEntities.add(applicationEntity);
            }
            entity.setApplications(applicationEntities);
        }
        // Manually convert handlers from model to entity (mapper ignores them)
        if (workflow.getHandlers() != null) {
            var handlerEntities = new ArrayList<WorkflowHandlerEntity>();
            for (var handler : workflow.getHandlers()) {
                var handlerEntity = convertHandlerToEntity(handler);
                // Generate handler ID if not set
                if (handlerEntity.getId() == null || handlerEntity.getId().isEmpty()) {
                    var handlerId = AiravataUtils.getId("handler");
                    handlerEntity.setId(handlerId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                handlerEntity.setWorkflowId(workflowId);
                handlerEntities.add(handlerEntity);
            }
            entity.setHandlers(handlerEntities);
        }
        // Manually convert connections from model to entity (mapper ignores them)
        if (workflow.getConnections() != null) {
            var connectionEntities = new ArrayList<WorkflowConnectionEntity>();
            for (var connection : workflow.getConnections()) {
                var connectionEntity = convertConnectionToEntity(connection);
                // Generate connection ID if not set
                if (connectionEntity.getId() == null || connectionEntity.getId().isEmpty()) {
                    var connectionId = AiravataUtils.getId("connection");
                    connectionEntity.setId(connectionId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                connectionEntity.setWorkflowId(workflowId);
                connectionEntities.add(connectionEntity);
            }
            entity.setConnections(connectionEntities);
        }
        workflowRepository.save(entity);
    }

    public String getWorkflowId(String experimentId) throws WorkflowCatalogException {
        var entities = workflowRepository.findByExperimentId(experimentId);
        if (entities.isEmpty()) return null;
        return entities.get(0).getId();
    }

    public AiravataWorkflow getWorkflow(String workflowId) throws WorkflowCatalogException {
        var entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) return null;
        var workflow = airavataWorkflowMapper.toModel(entity);
        // Manually map nested lists (applications, handlers, connections, statuses, errors)
        if (entity.getApplications() != null) {
            workflow.setApplications(entity.getApplications().stream()
                    .map(this::convertApplication)
                    .toList());
        }
        if (entity.getHandlers() != null) {
            workflow.setHandlers(
                    entity.getHandlers().stream().map(this::convertHandler).toList());
        }
        if (entity.getConnections() != null) {
            workflow.setConnections(entity.getConnections().stream()
                    .map(this::convertConnection)
                    .toList());
        }
        if (entity.getStatuses() != null) {
            workflow.setStatuses(entity.getStatuses().stream()
                    .map(this::convertWorkflowStatus)
                    .toList());
        }
        if (entity.getErrors() != null) {
            workflow.setErrors(
                    entity.getErrors().stream().map(this::convertError).toList());
        }
        return workflow;
    }

    public java.util.List<AiravataWorkflow> getAllWorkflows() throws WorkflowCatalogException {
        var entities = workflowRepository.findAll();
        return entities.stream()
                .map(entity -> {
                    try {
                        return getWorkflow(entity.getId());
                    } catch (WorkflowCatalogException e) {
                        return null;
                    }
                })
                .filter(w -> w != null)
                .toList();
    }

    public void updateWorkflow(String workflowId, AiravataWorkflow workflow) throws WorkflowCatalogException {
        var existingEntity = workflowRepository.findById(workflowId).orElse(null);
        if (existingEntity == null) {
            throw new WorkflowCatalogException("Workflow not found: " + workflowId);
        }
        
        // Update the entity with new values
        var entity = airavataWorkflowMapper.toEntity(workflow);
        entity.setId(workflowId);
        entity.setExperimentId(existingEntity.getExperimentId());
        
        // Preserve relationships
        if (workflow.getApplications() != null) {
            var applicationEntities = new ArrayList<WorkflowApplicationEntity>();
            for (var app : workflow.getApplications()) {
                var applicationEntity = convertApplicationToEntity(app);
                if (applicationEntity.getId() == null || applicationEntity.getId().isEmpty()) {
                    applicationEntity.setId(AiravataUtils.getId("application"));
                }
                applicationEntity.setWorkflowId(workflowId);
                applicationEntities.add(applicationEntity);
            }
            entity.setApplications(applicationEntities);
        }
        
        if (workflow.getHandlers() != null) {
            var handlerEntities = new ArrayList<WorkflowHandlerEntity>();
            for (var handler : workflow.getHandlers()) {
                var handlerEntity = convertHandlerToEntity(handler);
                if (handlerEntity.getId() == null || handlerEntity.getId().isEmpty()) {
                    handlerEntity.setId(AiravataUtils.getId("handler"));
                }
                handlerEntity.setWorkflowId(workflowId);
                handlerEntities.add(handlerEntity);
            }
            entity.setHandlers(handlerEntities);
        }
        
        if (workflow.getConnections() != null) {
            var connectionEntities = new ArrayList<WorkflowConnectionEntity>();
            for (var connection : workflow.getConnections()) {
                var connectionEntity = convertConnectionToEntity(connection);
                if (connectionEntity.getId() == null || connectionEntity.getId().isEmpty()) {
                    connectionEntity.setId(AiravataUtils.getId("connection"));
                }
                connectionEntity.setWorkflowId(workflowId);
                connectionEntities.add(connectionEntity);
            }
            entity.setConnections(connectionEntities);
        }
        
        workflowRepository.save(entity);
    }

    public void deleteWorkflow(String workflowId) throws WorkflowCatalogException {
        var entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) {
            throw new WorkflowCatalogException("Workflow not found: " + workflowId);
        }
        workflowRepository.delete(entity);
    }

    private WorkflowApplication convertApplication(WorkflowApplicationEntity entity) {
        var model = new WorkflowApplication();
        model.setId(entity.getId());
        model.setProcessId(entity.getProcessId());
        model.setApplicationInterfaceId(entity.getApplicationInterfaceId());
        model.setComputeResourceId(entity.getComputeResourceId());
        model.setQueueName(entity.getQueueName());
        model.setNodeCount(entity.getNodeCount());
        model.setCoreCount(entity.getCoreCount());
        model.setWallTimeLimit(entity.getWallTimeLimit());
        model.setPhysicalMemory(entity.getPhysicalMemory());
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        if (entity.getStatuses() != null) {
            model.setStatuses(entity.getStatuses().stream()
                    .map(this::convertApplicationStatus)
                    .toList());
        }
        if (entity.getErrors() != null) {
            model.setErrors(entity.getErrors().stream()
                    .map(this::convertError)
                    .toList());
        }
        return model;
    }

    private ApplicationStatus convertApplicationStatus(StatusEntity entity) {
        var model = new ApplicationStatus();
        model.setId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                model.setState(ApplicationState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                // If the state string doesn't match enum, leave it null
            }
        }
        model.setDescription(entity.getReason());
        model.setUpdatedAt(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L);
        return model;
    }

    private ErrorModel convertError(ErrorEntity entity) {
        var model = new ErrorModel();
        model.setErrorId(entity.getErrorId());
        model.setCreationTime(
                entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L);
        model.setActualErrorMessage(entity.getActualErrorMessage());
        model.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        model.setTransientOrPersistent(entity.isTransientOrPersistent());
        model.setRootCauseErrorIdList(
                entity.getRootCauseErrorIdList() != null
                        ? Arrays.asList(entity.getRootCauseErrorIdList().split(","))
                        : null);
        return model;
    }

    private WorkflowHandler convertHandler(WorkflowHandlerEntity entity) {
        var model = new WorkflowHandler();
        model.setId(entity.getId());
        if (entity.getType() != null) {
            model.setType(HandlerType.valueOf(entity.getType().name()));
        }
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        if (entity.getInputs() != null) {
            model.setInputs(
                    entity.getInputs().stream().map(this::convertInputData).toList());
        }
        if (entity.getOutputs() != null) {
            model.setOutputs(
                    entity.getOutputs().stream().map(this::convertOutputData).toList());
        }
        if (entity.getStatuses() != null) {
            model.setStatuses(entity.getStatuses().stream()
                    .map(this::convertHandlerStatus)
                    .toList());
        }
        if (entity.getErrors() != null) {
            model.setErrors(
                    entity.getErrors().stream().map(this::convertError).toList());
        }
        return model;
    }

    private HandlerStatus convertHandlerStatus(StatusEntity entity) {
        var model = new HandlerStatus();
        model.setId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                model.setState(HandlerState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                // If the state string doesn't match enum, leave it null
            }
        }
        model.setDescription(entity.getReason());
        model.setUpdatedAt(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L);
        return model;
    }


    private WorkflowStatus convertWorkflowStatus(StatusEntity entity) {
        var model = new WorkflowStatus();
        model.setId(entity.getStatusId());
        if (entity.getState() != null) {
            // Convert state string to WorkflowExecutionState
            try {
                var stateName = entity.getState();
                // Map state name to WorkflowExecutionState
                WorkflowExecutionState executionState = null;
                switch (stateName) {
                    case "CREATED":
                        executionState = WorkflowExecutionState.CREATED;
                        break;
                    case "STARTED":
                    case "LAUNCHED":
                        executionState = WorkflowExecutionState.LAUNCHED;
                        break;
                    case "EXECUTING":
                        executionState = WorkflowExecutionState.EXECUTING;
                        break;
                    case "COMPLETED":
                        executionState = WorkflowExecutionState.COMPLETED;
                        break;
                    case "FAILED":
                        executionState = WorkflowExecutionState.FAILED;
                        break;
                    case "CANCELLING":
                    case "CANCELING":
                        executionState = WorkflowExecutionState.CANCELING;
                        break;
                    case "CANCELED":
                        executionState = WorkflowExecutionState.CANCELED;
                        break;
                    default:
                        try {
                            executionState = WorkflowExecutionState.valueOf(stateName);
                        } catch (IllegalArgumentException e) {
                            // If no match, leave as null
                        }
                }
                model.setState(executionState);
            } catch (Exception e) {
                // If conversion fails, leave state as null
            }
        }
        model.setDescription(entity.getReason());
        model.setUpdatedAt(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L);
        return model;
    }


    private WorkflowConnection convertConnection(WorkflowConnectionEntity entity) {
        var model = new WorkflowConnection();
        model.setId(entity.getId());
        if (entity.getFromType() != null) {
            model.setFromType(ComponentType.valueOf(entity.getFromType().name()));
        }
        model.setFromId(entity.getFromId());
        model.setFromOutputName(entity.getFromOutputName());
        if (entity.getToType() != null) {
            model.setToType(ComponentType.valueOf(entity.getToType().name()));
        }
        model.setToId(entity.getToId());
        model.setToInputName(entity.getToInputName());
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        if (entity.getDataBlock() != null) {
            model.setDataBlock(convertDataBlock(entity.getDataBlock()));
        }
        return model;
    }

    private DataBlock convertDataBlock(WorkflowDataBlockEntity entity) {
        var model = new DataBlock();
        model.setId(entity.getId());
        model.setValue(entity.getValue());
        if (entity.getDataType() != null) {
            try {
                model.setType(DataType.valueOf(entity.getDataType()));
            } catch (IllegalArgumentException e) {
                // If the data type string doesn't match enum, leave it null
            }
        }
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        return model;
    }

    private InputDataObjectType convertInputData(InputDataEntity entity) {
        var model = new InputDataObjectType();
        model.setName(entity.getName());
        model.setValue(entity.getValue());
        if (entity.getType() != null) {
            model.setType(entity.getType());
        }
        model.setApplicationArgument(entity.getApplicationArgument());
        model.setStandardInput(entity.isStandardInput());
        model.setUserFriendlyDescription(entity.getUserFriendlyDescription());
        model.setMetaData(entity.getMetaData());
        model.setInputOrder(entity.getInputOrder());
        model.setIsRequired(entity.isRequired());
        model.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        model.setDataStaged(entity.isDataStaged());
        model.setStorageResourceId(entity.getStorageResourceId());
        model.setIsReadOnly(entity.isReadOnly());
        return model;
    }

    private OutputDataObjectType convertOutputData(OutputDataEntity entity) {
        var model = new OutputDataObjectType();
        model.setName(entity.getName());
        model.setValue(entity.getValue());
        if (entity.getType() != null) {
            model.setType(entity.getType());
        }
        model.setApplicationArgument(entity.getApplicationArgument());
        model.setIsRequired(entity.isRequired());
        model.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        model.setDataMovement(entity.isDataMovement());
        model.setLocation(entity.getLocation());
        model.setSearchQuery(entity.getSearchQuery());
        model.setOutputStreaming(entity.isOutputStreaming());
        model.setStorageResourceId(entity.getStorageResourceId());
        return model;
    }

    private WorkflowApplicationEntity convertApplicationToEntity(WorkflowApplication model) {
        var entity = new WorkflowApplicationEntity();
        entity.setId(model.getId());
        entity.setProcessId(model.getProcessId());
        entity.setApplicationInterfaceId(model.getApplicationInterfaceId());
        entity.setComputeResourceId(model.getComputeResourceId());
        entity.setQueueName(model.getQueueName());
        entity.setNodeCount(model.getNodeCount());
        entity.setCoreCount(model.getCoreCount());
        entity.setWallTimeLimit(model.getWallTimeLimit());
        entity.setPhysicalMemory(model.getPhysicalMemory());
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new Timestamp(model.getUpdatedAt()) : null);
        // Note: statuses and errors are handled separately if needed
        return entity;
    }

    private WorkflowHandlerEntity convertHandlerToEntity(WorkflowHandler model) {
        var entity = new WorkflowHandlerEntity();
        entity.setId(model.getId());
        if (model.getType() != null) {
            entity.setType(model.getType());
        }
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new Timestamp(model.getUpdatedAt()) : null);
        // Note: inputs, outputs, statuses, and errors are handled separately if needed
        return entity;
    }

    private WorkflowConnectionEntity convertConnectionToEntity(WorkflowConnection model) {
        var entity = new WorkflowConnectionEntity();
        entity.setId(model.getId());
        if (model.getFromType() != null) {
            entity.setFromType(ComponentType.valueOf(model.getFromType().name()));
        }
        entity.setFromId(model.getFromId());
        entity.setFromOutputName(model.getFromOutputName());
        if (model.getToType() != null) {
            entity.setToType(ComponentType.valueOf(model.getToType().name()));
        }
        entity.setToId(model.getToId());
        entity.setToInputName(model.getToInputName());
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new Timestamp(model.getUpdatedAt()) : null);
        // Note: dataBlock is handled separately if needed
        return entity;
    }
}
