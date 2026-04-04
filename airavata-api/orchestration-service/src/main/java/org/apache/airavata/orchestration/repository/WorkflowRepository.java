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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workflow.proto.AiravataWorkflow;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.workflow.AiravataWorkflowEntity;
import org.apache.airavata.util.WorkflowCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRepository extends AbstractRepository<AiravataWorkflow, AiravataWorkflowEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);

    public WorkflowRepository() {
        super(AiravataWorkflow.class, AiravataWorkflowEntity.class);
    }

    @Override
    protected AiravataWorkflow toModel(AiravataWorkflowEntity entity) {
        return ExecutionMapper.INSTANCE.workflowToModel(entity);
    }

    @Override
    protected AiravataWorkflowEntity toEntity(AiravataWorkflow model) {
        return ExecutionMapper.INSTANCE.workflowToEntity(model);
    }

    protected String saveWorkflowModelData(AiravataWorkflow workflowModel, String experimentId)
            throws RegistryException {
        AiravataWorkflowEntity workflowEntity = saveWorkflow(workflowModel, experimentId);
        return workflowEntity.getId();
    }

    protected AiravataWorkflowEntity saveWorkflow(AiravataWorkflow workflowModel, String experimentId)
            throws RegistryException {

        if (workflowModel.getId().isEmpty() || workflowModel.getId().equals("DO_NOT_SET_AT_CLIENTS")) {
            String newId = WorkflowCatalogUtils.getID(experimentId);
            logger.debug("Setting the ID: " + newId + " for the new Workflow");
            workflowModel = workflowModel.toBuilder().setId(newId).build();
        }

        if (!workflowModel.getStatusesList().isEmpty()) {
            logger.debug("Populating the status IDs of WorkflowStatus objects for the Workflow with ID: "
                    + workflowModel.getId());
            AiravataWorkflow.Builder wfBuilder = workflowModel.toBuilder().clearStatuses();
            for (org.apache.airavata.model.workflow.proto.WorkflowStatus ws : workflowModel.getStatusesList()) {
                if (ws.getId().isEmpty()) {
                    ws = ws.toBuilder()
                            .setId(WorkflowCatalogUtils.getID("WORKFLOW_STATUS"))
                            .build();
                }
                wfBuilder.addStatuses(ws);
            }
            workflowModel = wfBuilder.build();
        }

        if (workflowModel.getExperimentId().isEmpty()) {
            logger.debug("Setting the ExperimentID: " + experimentId + " for the new Workflow with ID: "
                    + workflowModel.getId());
            workflowModel =
                    workflowModel.toBuilder().setExperimentId(experimentId).build();
        }

        String workflowId = workflowModel.getId();
        AiravataWorkflowEntity workflowEntity = ExecutionMapper.INSTANCE.workflowToEntity(workflowModel);

        if (workflowEntity.getStatuses() != null) {
            logger.debug(
                    "Populating the Workflow ID of WorkflowStatus objects for the Workflow with ID: " + workflowId);
            workflowEntity
                    .getStatuses()
                    .forEach(workflowStatusEntity -> workflowStatusEntity.setWorkflowId(workflowId));
        }

        if (workflowEntity.getApplications() != null) {
            logger.debug("Populating the Workflow ID for WorkflowApplication objects for the Workflow with ID: "
                    + workflowId);
            workflowEntity.getApplications().forEach(applicationEntity -> {
                applicationEntity.setWorkflowId(workflowId);

                if (applicationEntity.getStatuses() != null) {
                    logger.debug("Populating the Workflow ID of ApplicationStatus objects for the Application");
                    applicationEntity
                            .getStatuses()
                            .forEach(applicationStatusEntity ->
                                    applicationStatusEntity.setApplicationId(applicationEntity.getId()));
                }
            });
        }

        if (workflowEntity.getHandlers() != null) {
            logger.debug(
                    "Populating the Workflow ID for WorkflowHandler objects for the Workflow with ID: " + workflowId);
            workflowEntity.getHandlers().forEach(handlerEntity -> {
                handlerEntity.setWorkflowId(workflowId);

                if (handlerEntity.getStatuses() != null) {
                    logger.debug("Populating the Workflow ID of HandlerStatus objects for the Handler");
                    handlerEntity
                            .getStatuses()
                            .forEach(handlerStatusEntity -> handlerStatusEntity.setHandlerId(handlerEntity.getId()));
                }

                if (handlerEntity.getInputs() != null
                        && !handlerEntity.getInputs().isEmpty()) {
                    logger.debug("Populating the Handler ID for HandlerInput objects for the Handler with ID: "
                            + handlerEntity.getId());
                    handlerEntity.getInputs().forEach(inputEntity -> inputEntity.setHandlerId(handlerEntity.getId()));
                }

                if (handlerEntity.getOutputs() != null
                        && !handlerEntity.getOutputs().isEmpty()) {
                    logger.debug("Populating the Handler ID for HandlerOutput objects for the Handler with ID: "
                            + handlerEntity.getId());
                    handlerEntity
                            .getOutputs()
                            .forEach(outputEntity -> outputEntity.setHandlerId(handlerEntity.getId()));
                }
            });
        }

        if (workflowEntity.getConnections() != null) {
            logger.debug("Populating the ID and Workflow ID for WorkflowConnection objects for the Workflow with ID: "
                    + workflowId);
            workflowEntity.getConnections().forEach(connectionEntity -> {
                if (connectionEntity.getId() == null || connectionEntity.getId().equals("DO_NOT_SET_AT_CLIENTS")) {
                    connectionEntity.setId(WorkflowCatalogUtils.getID("WORKFLOW_CONNECTION"));
                }

                connectionEntity.setWorkflowId(workflowId);
            });
        }

        if (get(workflowId) == null) {
            logger.debug("Checking if the Workflow already exists");
            workflowEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }

        workflowEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return execute(entityManager -> entityManager.merge(workflowEntity));
    }

    public String registerWorkflow(AiravataWorkflow workflowModel, String experimentId) throws RegistryException {
        return saveWorkflowModelData(workflowModel, experimentId);
    }

    public void updateWorkflow(String workflowId, AiravataWorkflow updatedWorkflowModel) throws RegistryException {
        saveWorkflowModelData(updatedWorkflowModel, null);
    }

    public AiravataWorkflow getWorkflow(String workflowId) throws RegistryException {
        return get(workflowId);
    }

    public String getWorkflowId(String experimentId) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.EXPERIMENT_ID, experimentId);
        List<AiravataWorkflow> workflowModelList =
                select(QueryConstants.GET_WORKFLOW_FOR_EXPERIMENT_ID, -1, 0, queryParameters);

        if (workflowModelList != null && !workflowModelList.isEmpty()) {
            logger.debug("Return the record (there is only one record)");
            return workflowModelList.get(0).getId();
        }
        return null;
    }

    public void deleteWorkflow(String workflowId) throws RegistryException {
        delete(workflowId);
    }
}
