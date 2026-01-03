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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.AiravataWorkflow;
import org.apache.airavata.common.model.ApplicationStatus;
import org.apache.airavata.common.model.DataBlock;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.HandlerStatus;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.WorkflowApplication;
import org.apache.airavata.common.model.WorkflowConnection;
import org.apache.airavata.common.model.WorkflowHandler;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.ApplicationErrorEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.ApplicationStatusEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.HandlerErrorEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.HandlerStatusEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowApplicationEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowConnectionEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowDataBlockEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowHandlerEntity;
import org.apache.airavata.registry.exception.WorkflowCatalogException;
import org.apache.airavata.registry.mappers.AiravataWorkflowMapper;
import org.apache.airavata.registry.mappers.ErrorModelMapper;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.mappers.OutputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.workflowcatalog.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final AiravataWorkflowMapper airavataWorkflowMapper;
    private final ErrorModelMapper errorModelMapper;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            AiravataWorkflowMapper airavataWorkflowMapper,
            ErrorModelMapper errorModelMapper,
            InputDataObjectTypeMapper inputDataObjectTypeMapper,
            OutputDataObjectTypeMapper outputDataObjectTypeMapper) {
        this.workflowRepository = workflowRepository;
        this.airavataWorkflowMapper = airavataWorkflowMapper;
        this.errorModelMapper = errorModelMapper;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
        this.outputDataObjectTypeMapper = outputDataObjectTypeMapper;
    }

    public void registerWorkflow(AiravataWorkflow workflow, String experimentId) throws WorkflowCatalogException {
        AiravataWorkflowEntity entity = airavataWorkflowMapper.toEntity(workflow);
        entity.setExperimentId(experimentId);
        // Generate workflow ID if not already set
        if (entity.getId() == null || entity.getId().isEmpty()) {
            String workflowId = org.apache.airavata.common.utils.AiravataUtils.getId(
                    workflow.getDescription() != null ? workflow.getDescription() : "workflow");
            entity.setId(workflowId);
        }
        final String workflowId = entity.getId();
        // Manually convert applications from model to entity (mapper ignores them)
        if (workflow.getApplications() != null) {
            List<WorkflowApplicationEntity> applicationEntities = new ArrayList<>();
            for (WorkflowApplication app : workflow.getApplications()) {
                WorkflowApplicationEntity applicationEntity = convertApplicationToEntity(app);
                // Generate application ID if not set
                if (applicationEntity.getId() == null
                        || applicationEntity.getId().isEmpty()) {
                    String applicationId = org.apache.airavata.common.utils.AiravataUtils.getId("application");
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
            List<WorkflowHandlerEntity> handlerEntities = new ArrayList<>();
            for (WorkflowHandler handler : workflow.getHandlers()) {
                WorkflowHandlerEntity handlerEntity = convertHandlerToEntity(handler);
                // Generate handler ID if not set
                if (handlerEntity.getId() == null || handlerEntity.getId().isEmpty()) {
                    String handlerId = org.apache.airavata.common.utils.AiravataUtils.getId("handler");
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
            List<WorkflowConnectionEntity> connectionEntities = new ArrayList<>();
            for (WorkflowConnection connection : workflow.getConnections()) {
                WorkflowConnectionEntity connectionEntity = convertConnectionToEntity(connection);
                // Generate connection ID if not set
                if (connectionEntity.getId() == null || connectionEntity.getId().isEmpty()) {
                    String connectionId = org.apache.airavata.common.utils.AiravataUtils.getId("connection");
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
        List<AiravataWorkflowEntity> entities = workflowRepository.findByExperimentId(experimentId);
        if (entities.isEmpty()) return null;
        return entities.get(0).getId();
    }

    public AiravataWorkflow getWorkflow(String workflowId) throws WorkflowCatalogException {
        AiravataWorkflowEntity entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) return null;
        AiravataWorkflow workflow = airavataWorkflowMapper.toModel(entity);
        // Manually map nested lists (applications, handlers, connections, statuses, errors)
        if (entity.getApplications() != null) {
            workflow.setApplications(entity.getApplications().stream()
                    .map(this::convertApplication)
                    .collect(Collectors.toList()));
        }
        if (entity.getHandlers() != null) {
            workflow.setHandlers(
                    entity.getHandlers().stream().map(this::convertHandler).collect(Collectors.toList()));
        }
        if (entity.getConnections() != null) {
            workflow.setConnections(entity.getConnections().stream()
                    .map(this::convertConnection)
                    .collect(Collectors.toList()));
        }
        return workflow;
    }

    private WorkflowApplication convertApplication(WorkflowApplicationEntity entity) {
        WorkflowApplication model = new WorkflowApplication();
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
                    .collect(Collectors.toList()));
        }
        if (entity.getErrors() != null) {
            model.setErrors(entity.getErrors().stream()
                    .map(this::convertApplicationError)
                    .collect(Collectors.toList()));
        }
        return model;
    }

    private ApplicationStatus convertApplicationStatus(ApplicationStatusEntity entity) {
        ApplicationStatus model = new ApplicationStatus();
        model.setId(entity.getId());
        if (entity.getState() != null) {
            model.setState(org.apache.airavata.common.model.ApplicationState.valueOf(
                    entity.getState().name()));
        }
        model.setDescription(entity.getDescription());
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        return model;
    }

    private ErrorModel convertApplicationError(ApplicationErrorEntity entity) {
        ErrorModel model = new ErrorModel();
        model.setErrorId(entity.getErrorId());
        model.setCreationTime(
                entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L);
        model.setActualErrorMessage(entity.getActualErrorMessage());
        model.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        model.setTransientOrPersistent(entity.isTransientOrPersistent());
        model.setRootCauseErrorIdList(
                entity.getRootCauseErrorIdList() != null
                        ? java.util.Arrays.asList(
                                entity.getRootCauseErrorIdList().split(","))
                        : null);
        return model;
    }

    private WorkflowHandler convertHandler(WorkflowHandlerEntity entity) {
        WorkflowHandler model = new WorkflowHandler();
        model.setId(entity.getId());
        if (entity.getType() != null) {
            model.setType(org.apache.airavata.common.model.HandlerType.valueOf(
                    entity.getType().name()));
        }
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        if (entity.getInputs() != null) {
            model.setInputs(
                    entity.getInputs().stream().map(this::convertHandlerInput).collect(Collectors.toList()));
        }
        if (entity.getOutputs() != null) {
            model.setOutputs(
                    entity.getOutputs().stream().map(this::convertHandlerOutput).collect(Collectors.toList()));
        }
        if (entity.getStatuses() != null) {
            model.setStatuses(entity.getStatuses().stream()
                    .map(this::convertHandlerStatus)
                    .collect(Collectors.toList()));
        }
        if (entity.getErrors() != null) {
            model.setErrors(
                    entity.getErrors().stream().map(this::convertHandlerError).collect(Collectors.toList()));
        }
        return model;
    }

    private HandlerStatus convertHandlerStatus(HandlerStatusEntity entity) {
        HandlerStatus model = new HandlerStatus();
        model.setId(entity.getId());
        if (entity.getState() != null) {
            try {
                model.setState(org.apache.airavata.common.model.HandlerState.valueOf(entity.getState()));
            } catch (IllegalArgumentException e) {
                // If the state string doesn't match enum, leave it null
            }
        }
        model.setDescription(entity.getDescription());
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        return model;
    }

    private ErrorModel convertHandlerError(HandlerErrorEntity entity) {
        ErrorModel model = new ErrorModel();
        model.setErrorId(entity.getErrorId());
        model.setCreationTime(
                entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L);
        model.setActualErrorMessage(entity.getActualErrorMessage());
        model.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        model.setTransientOrPersistent(entity.isTransientOrPersistent());
        model.setRootCauseErrorIdList(
                entity.getRootCauseErrorIdList() != null
                        ? java.util.Arrays.asList(
                                entity.getRootCauseErrorIdList().split(","))
                        : null);
        return model;
    }

    private WorkflowConnection convertConnection(WorkflowConnectionEntity entity) {
        WorkflowConnection model = new WorkflowConnection();
        model.setId(entity.getId());
        if (entity.getFromType() != null) {
            model.setFromType(org.apache.airavata.common.model.ComponentType.valueOf(
                    entity.getFromType().name()));
        }
        model.setFromId(entity.getFromId());
        model.setFromOutputName(entity.getFromOutputName());
        if (entity.getToType() != null) {
            model.setToType(org.apache.airavata.common.model.ComponentType.valueOf(
                    entity.getToType().name()));
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
        DataBlock model = new DataBlock();
        model.setId(entity.getId());
        model.setValue(entity.getValue());
        if (entity.getDataType() != null) {
            try {
                model.setType(org.apache.airavata.common.model.DataType.valueOf(entity.getDataType()));
            } catch (IllegalArgumentException e) {
                // If the data type string doesn't match enum, leave it null
            }
        }
        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L);
        return model;
    }

    private InputDataObjectType convertHandlerInput(
            org.apache.airavata.registry.entities.airavataworkflowcatalog.HandlerInputEntity entity) {
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

    private OutputDataObjectType convertHandlerOutput(
            org.apache.airavata.registry.entities.airavataworkflowcatalog.HandlerOutputEntity entity) {
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
        WorkflowApplicationEntity entity = new WorkflowApplicationEntity();
        entity.setId(model.getId());
        entity.setProcessId(model.getProcessId());
        entity.setApplicationInterfaceId(model.getApplicationInterfaceId());
        entity.setComputeResourceId(model.getComputeResourceId());
        entity.setQueueName(model.getQueueName());
        entity.setNodeCount(model.getNodeCount());
        entity.setCoreCount(model.getCoreCount());
        entity.setWallTimeLimit(model.getWallTimeLimit());
        entity.setPhysicalMemory(model.getPhysicalMemory());
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new java.sql.Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new java.sql.Timestamp(model.getUpdatedAt()) : null);
        // Note: statuses and errors are handled separately if needed
        return entity;
    }

    private WorkflowHandlerEntity convertHandlerToEntity(WorkflowHandler model) {
        WorkflowHandlerEntity entity = new WorkflowHandlerEntity();
        entity.setId(model.getId());
        if (model.getType() != null) {
            entity.setType(model.getType());
        }
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new java.sql.Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new java.sql.Timestamp(model.getUpdatedAt()) : null);
        // Note: inputs, outputs, statuses, and errors are handled separately if needed
        return entity;
    }

    private WorkflowConnectionEntity convertConnectionToEntity(WorkflowConnection model) {
        WorkflowConnectionEntity entity = new WorkflowConnectionEntity();
        entity.setId(model.getId());
        if (model.getFromType() != null) {
            entity.setFromType(org.apache.airavata.common.model.ComponentType.valueOf(
                    model.getFromType().name()));
        }
        entity.setFromId(model.getFromId());
        entity.setFromOutputName(model.getFromOutputName());
        if (model.getToType() != null) {
            entity.setToType(org.apache.airavata.common.model.ComponentType.valueOf(
                    model.getToType().name()));
        }
        entity.setToId(model.getToId());
        entity.setToInputName(model.getToInputName());
        entity.setCreatedAt(model.getCreatedAt() > 0 ? new java.sql.Timestamp(model.getCreatedAt()) : null);
        entity.setUpdatedAt(model.getUpdatedAt() > 0 ? new java.sql.Timestamp(model.getUpdatedAt()) : null);
        // Note: dataBlock is handled separately if needed
        return entity;
    }
}
