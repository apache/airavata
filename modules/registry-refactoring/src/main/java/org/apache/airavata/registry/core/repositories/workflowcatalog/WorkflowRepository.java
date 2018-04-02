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

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.core.utils.WorkflowCatalogUtils;
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

public class WorkflowRepository extends WorkflowCatAbstractRepository<WorkflowModel, WorkflowEntity, String> implements WorkflowCatalog {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);

    public WorkflowRepository() { super(WorkflowModel.class, WorkflowEntity.class); }

    protected String saveWorkflowModelData(WorkflowModel workflowModel, String gatewayId) throws WorkflowCatalogException {
        WorkflowEntity workflowEntity = saveWorkflow(workflowModel, gatewayId);
        return workflowEntity.getTemplateId();
    }

    protected WorkflowEntity saveWorkflow(WorkflowModel workflowModel, String gatewayId) throws WorkflowCatalogException {
        if (workflowModel.getTemplateId() == null) {
            logger.debug("Setting the Template ID for the new Workflow");
            workflowModel.setTemplateId(WorkflowCatalogUtils.getID(workflowModel.getName()));
        }

        String templateId = workflowModel.getTemplateId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        WorkflowEntity workflowEntity = mapper.map(workflowModel, WorkflowEntity.class);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Workflow");
            workflowEntity.setGatewayId(gatewayId);
        }

        if (workflowEntity.getWorkflowInputs() != null) {
            logger.debug("Populating the template ID for WorkflowInput objects for the Workflow");
            workflowEntity.getWorkflowInputs().forEach(workflowInputEntity -> workflowInputEntity.setTemplateId(templateId));
        }

        if (workflowEntity.getWorkflowOutputs() != null) {
            logger.debug("Populating the template ID for WorkflowOutput objects for the Workflow");
            workflowEntity.getWorkflowOutputs().forEach(workflowOutputEntity -> workflowOutputEntity.setTemplateId(templateId));
        }

        if (get(templateId) == null) {
            logger.debug("Checking if the Workflow already exists");
            workflowEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        workflowEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        return execute(entityManager -> entityManager.merge(workflowEntity));
    }

    @Override
    public String registerWorkflow(WorkflowModel workflowModel, String gatewayId) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        //return saveWorkflowModelData(workflowModel, gatewayId);
    }

    @Override
    public void updateWorkflow(String templateId, WorkflowModel updatedWorkflowModel) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        //saveWorkflowModelData(updatedWorkflowModel, null);
    }

    @Override
    public WorkflowModel getWorkflow(String templateId) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        //return get(templateId);
    }

    @Override
    public List<String> getAllWorkflows(String gatewayId) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        /*Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.GATEWAY_ID, gatewayId);
        List<WorkflowModel> workflowModelList = select(QueryConstants.GET_ALL_WORKFLOWS, -1, 0, queryParameters);
        List<String> workflows = new ArrayList<>();
        for (WorkflowModel workflowModel : workflowModelList) {
            workflows.add(workflowModel.getTemplateId());
        }
        return workflows;*/
    }

    @Override
    public String getWorkflowTemplateId(String workflowName) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        /*Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.WORKFLOW_NAME, workflowName);
        List<WorkflowModel> workflowModelList = select(QueryConstants.GET_WORKFLOW_GIVEN_NAME, -1, 0, queryParameters);

        if (!workflowModelList.isEmpty() && workflowModelList != null) {
            logger.debug("Return the record (there is only one record)");
            return workflowModelList.get(0).getTemplateId();
        }
        return null;*/
    }

    @Override
    public boolean isWorkflowExistWithName(String workflowName) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        /*Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Workflow.WORKFLOW_NAME, workflowName);
        List<WorkflowModel> workflowModelList = select(QueryConstants.GET_WORKFLOW_GIVEN_NAME, -1, 0, queryParameters);
        return (!workflowModelList.isEmpty() && workflowModelList != null);*/
    }

    @Override
    public void updateWorkflowOutputs(String templateId, List<OutputDataObjectType> workflowOutputs) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        /*WorkflowModel workflowModel = getWorkflow(templateId);
        workflowModel.setWorkflowOutputs(workflowOutputs);
        updateWorkflow(workflowModel.getTemplateId(), workflowModel);*/
    }

    @Override
    public void deleteWorkflow(String templateId) throws WorkflowCatalogException {
        throw new WorkflowCatalogException("This method is not implemented");
        //delete(templateId);
    }

}
