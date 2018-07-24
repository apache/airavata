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
package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.workflow.AiravataWorkflow;
import org.apache.airavata.registry.core.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.apache.airavata.registry.core.utils.*;
import org.apache.airavata.registry.cpi.WorkflowCatalog;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowRepository extends WorkflowCatAbstractRepository<AiravataWorkflow, AiravataWorkflowEntity, String> implements WorkflowCatalog {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);

    public WorkflowRepository() {
        super(AiravataWorkflow.class, AiravataWorkflowEntity.class);
    }

    protected String saveWorkflowModelData(AiravataWorkflow workflowModel, String gatewayId) throws WorkflowCatalogException {
        AiravataWorkflowEntity workflowEntity = saveWorkflow(workflowModel, gatewayId);
        return workflowEntity.getId();
    }

    protected AiravataWorkflowEntity saveWorkflow(AiravataWorkflow workflowModel, String gatewayId) throws WorkflowCatalogException {

        if (workflowModel.getId() == null || workflowModel.getId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the ID for the new Workflow");
            workflowModel.setId(WorkflowCatalogUtils.getID(workflowModel.getName()));
        }

        if (workflowModel.getStatuses() != null) {
            logger.debug("Populating the status ID of WorkflowStatus objects for the Workflow");
            workflowModel.getStatuses().forEach(workflowStatus -> {
                if (workflowStatus.getId() == null) {
                    workflowStatus.setId(WorkflowCatalogUtils.getID("WORKFLOW_STATUS"));
                }
            });
        }

        if (workflowModel.getGatewayId() == null) {
            logger.debug("Setting the GatewayID for the new Workflow");
            workflowModel.setGatewayId(gatewayId);
        }

        String workflowId = workflowModel.getId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        AiravataWorkflowEntity workflowEntity = mapper.map(workflowModel, AiravataWorkflowEntity.class);

        if (workflowEntity.getNotificationEmails() != null) {
            logger.debug("Populating the Workflow ID of NotificationEmail objects for the Workflow");
            workflowEntity.getNotificationEmails().forEach(notificationEmailEntity -> notificationEmailEntity.setWorkflowId(workflowId));
        }

        if (workflowEntity.getStatuses() != null) {
            logger.debug("Populating the Workflow ID of WorkflowStatus objects for the Workflow");
            workflowEntity.getStatuses().forEach(workflowStatusEntity -> workflowStatusEntity.setWorkflowId(workflowId));
        }

        if (workflowEntity.getApplications() != null) {
            logger.debug("Populating the Workflow ID for WorkflowApplication objects for the Workflow with ID: " + workflowId);
            workflowEntity.getApplications().forEach(applicationEntity -> {
                applicationEntity.setWorkflowId(workflowId);

                if (applicationEntity.getStatuses() != null) {
                    logger.debug("Populating the Workflow ID of ApplicationStatus objects for the Application");
                    applicationEntity.getStatuses().forEach(applicationStatusEntity -> applicationStatusEntity.setApplicationId(applicationEntity.getId()));
                }
            });
        }

        if (workflowEntity.getHandlers() != null) {
            logger.debug("Populating the Workflow ID for WorkflowHandler objects for the Workflow with ID: " + workflowId);
            workflowEntity.getHandlers().forEach(handlerEntity -> {
                handlerEntity.setWorkflowId(workflowId);

                if (handlerEntity.getStatuses() != null) {
                    logger.debug("Populating the Workflow ID of HandlerStatus objects for the Handler");
                    handlerEntity.getStatuses().forEach(handlerStatusEntity -> handlerStatusEntity.setHandlerId(handlerEntity.getId()));
                }

                if (handlerEntity.getInputs() != null && !handlerEntity.getInputs().isEmpty()) {
                    logger.debug("Populating the Handler ID for HandlerInput objects for the Handler with ID: " + handlerEntity.getId());
                    handlerEntity.getInputs().forEach(inputEntity -> inputEntity.setHandlerId(handlerEntity.getId()));
                }

                if (handlerEntity.getOutputs() != null && !handlerEntity.getOutputs().isEmpty()) {
                    logger.debug("Populating the Handler ID for HandlerOutput objects for the Handler with ID: " + handlerEntity.getId());
                    handlerEntity.getOutputs().forEach(outputEntity -> outputEntity.setHandlerId(handlerEntity.getId()));
                }
            });
        }

        if (workflowEntity.getConnections() != null) {
            logger.debug("Populating the ID and Workflow ID for WorkflowConnection objects for the Workflow with ID: " + workflowId);
            workflowEntity.getConnections().forEach(connectionEntity -> {

                if (connectionEntity.getId() == null || connectionEntity.getId().equals(airavata_commonsConstants.DEFAULT_ID)) {
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

    @Override
    public String registerWorkflow(AiravataWorkflow workflowModel, String gatewayId) throws WorkflowCatalogException {
        return saveWorkflowModelData(workflowModel, gatewayId);
    }

    @Override
    public void updateWorkflow(String templateId, AiravataWorkflow updatedWorkflowModel) throws WorkflowCatalogException {
        saveWorkflowModelData(updatedWorkflowModel, null);
    }

    @Override
    public AiravataWorkflow getWorkflow(String workflowId) throws WorkflowCatalogException {
        return get(workflowId);
    }

    @Override
    public List<String> getAllWorkflows(String gatewayId) throws WorkflowCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.GATEWAY_ID, gatewayId);
        List<AiravataWorkflow> workflowModelList = select(QueryConstants.GET_ALL_WORKFLOWS, -1, 0, queryParameters);
        List<String> workflows = new ArrayList<>();
        for (AiravataWorkflow workflowModel : workflowModelList) {
            workflows.add(workflowModel.getId());
        }
        return workflows;
    }

    @Override
    public String getWorkflowId(String workflowName) throws WorkflowCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.NAME, workflowName);
        List<AiravataWorkflow> workflowModelList = select(QueryConstants.GET_WORKFLOW_GIVEN_NAME, -1, 0, queryParameters);

        if (workflowModelList != null && !workflowModelList.isEmpty()) {
            logger.debug("Return the record (there is only one record)");
            return workflowModelList.get(0).getId();
        }
        return null;
    }

    @Override
    public boolean isWorkflowExistWithName(String workflowName) throws WorkflowCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.NAME, workflowName);
        List<AiravataWorkflow> workflowModelList = select(QueryConstants.GET_WORKFLOW_GIVEN_NAME, -1, 0, queryParameters);
        return (workflowModelList != null && !workflowModelList.isEmpty());
    }

    @Override
    public void deleteWorkflow(String workflowId) throws WorkflowCatalogException {
        delete(workflowId);
    }
}
