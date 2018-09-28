/**
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
 */
package org.apache.airavata.registry.core.workflow.catalog.impl;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.workflow.AiravataWorkflow;
import org.apache.airavata.registry.core.workflow.catalog.resources.*;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogThriftConversion;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogUtils;
import org.apache.airavata.registry.cpi.WorkflowCatalog;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowCatalogImpl implements WorkflowCatalog {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowCatalogImpl.class);

//    @Override
//    public List<String> getAllWorkflows(String gatewayId) throws WorkflowCatalogException {
//        List<String> workflowIds = new ArrayList<String>();
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            resource.setGatewayId(gatewayId);
//            workflowIds = resource.getAllIds();
//        } catch (Exception e) {
//            logger.error("Error while retrieving all the workflow template ids...", e);
//            throw new WorkflowCatalogException(e);
//        }
//        return workflowIds;
//    }

    @Override
    public AiravataWorkflow getWorkflow(String workflowId) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            WorkflowResource wfResource = (WorkflowResource)resource.get(workflowId);
//            return WorkflowCatalogThriftConversion.getWorkflow(wfResource);
//        } catch (Exception e) {
//            logger.error("Error while retrieving the workflow...", e);
//            throw new WorkflowCatalogException(e);
//        }
        return null;
    }

    @Override
    public void deleteWorkflow(String workflowId) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            resource.remove(workflowId);
//        } catch (Exception e) {
//            logger.error("Error while deleting the workflow...", e);
//            throw new WorkflowCatalogException(e);
//        }
    }

    @Override
    public String registerWorkflow(AiravataWorkflow workflow, String gatewayId) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            resource.setWfTemplateId(WorkflowCatalogUtils.getID(workflow.getName()));
//            resource.setWfName(workflow.getName());
//            resource.setGraph(workflow.getGraph());
//            resource.setGatewayId(gatewayId);
//            if (workflow.getImage() != null){
//                resource.setImage(new String(workflow.getImage()));
//            }
//            resource.save();
//            workflow.setTemplateId(resource.getWfTemplateId());
//            List<InputDataObjectType> workflowInputs = workflow.getWorkflowInputs();
//            if (workflowInputs != null && workflowInputs.size() != 0){
//                for (InputDataObjectType input : workflowInputs){
//                    WorkflowInputResource wfInputResource = new WorkflowInputResource();
//                    wfInputResource.setWorkflowResource(resource);
//                    wfInputResource.setInputKey(input.getName());
//                    wfInputResource.setInputVal(input.getValue());
//                    wfInputResource.setWfTemplateId(resource.getWfTemplateId());
//                    wfInputResource.setDataType(input.getType().toString());
//                    wfInputResource.setAppArgument(input.getApplicationArgument());
//                    wfInputResource.setStandardInput(input.isStandardInput());
//                    wfInputResource.setUserFriendlyDesc(input.getUserFriendlyDescription());
//                    wfInputResource.setMetadata(input.getMetaData());
//                    wfInputResource.save();
//                }
//            }
//            List<OutputDataObjectType> workflowOutputs = workflow.getWorkflowOutputs();
//            if (workflowOutputs != null && workflowOutputs.size() != 0){
//                for (OutputDataObjectType output : workflowOutputs){
//                    WorkflowOutputResource outputResource = new WorkflowOutputResource();
//                    outputResource.setWorkflowResource(resource);
//                    outputResource.setOutputKey(output.getName());
//                    outputResource.setOutputVal(output.getValue());
//                    outputResource.setWfTemplateId(resource.getWfTemplateId());
//                    outputResource.setDataType(output.getType().toString());
//                    outputResource.setAppArgument(output.getApplicationArgument());
//                    outputResource.setDataNameLocation(output.getLocation());
//                    outputResource.setRequired(output.isIsRequired());
//                    outputResource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                    outputResource.setOutputStreaming(output.isOutputStreaming());
//                    outputResource.setDataMovement(output.isDataMovement());
//                    outputResource.save();
//                }
//            }
//            return resource.getWfTemplateId();
//        } catch (Exception e) {
//            logger.error("Error while saving the workflow...", e);
//            throw new WorkflowCatalogException(e);
//        }
        return null;
    }

    @Override
    public void updateWorkflow(String workflowId, AiravataWorkflow workflow) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            WorkflowResource existingWF = (WorkflowResource)resource.get(workflowId);
//            existingWF.setWfName(workflow.getName());
//            existingWF.setGraph(workflow.getGraph());
//            if (workflow.getImage() != null){
//                existingWF.setImage(new String(workflow.getImage()));
//            }
//            existingWF.save();
//            List<InputDataObjectType> existingwFInputs = workflow.getWorkflowInputs();
//            if (existingwFInputs != null && existingwFInputs.size() != 0){
//                for (InputDataObjectType input : existingwFInputs){
//                    WorkflowInputResource wfInputResource = new WorkflowInputResource();
//                    Map<String, String> ids = new HashMap<String, String>();
//                    ids.put(WorkflowCatAbstractResource.WorkflowInputConstants.WF_TEMPLATE_ID,existingWF.getWfTemplateId());
//                    ids.put(WorkflowCatAbstractResource.WorkflowInputConstants.INPUT_KEY,input.getName());
//                    WorkflowInputResource existingInput = (WorkflowInputResource)wfInputResource.get(ids);
//                    existingInput.setWorkflowResource(existingWF);
//                    existingInput.setInputKey(input.getName());
//                    existingInput.setInputVal(input.getValue());
//                    existingInput.setWfTemplateId(existingWF.getWfTemplateId());
//                    existingInput.setDataType(input.getType().toString());
//                    existingInput.setAppArgument(input.getApplicationArgument());
//                    existingInput.setStandardInput(input.isStandardInput());
//                    existingInput.setUserFriendlyDesc(input.getUserFriendlyDescription());
//                    existingInput.setMetadata(input.getMetaData());
//                    existingInput.save();
//                }
//            }
//            List<OutputDataObjectType> workflowOutputs = workflow.getWorkflowOutputs();
//            if (workflowOutputs != null && workflowOutputs.size() != 0){
//                for (OutputDataObjectType output : workflowOutputs){
//                    WorkflowOutputResource outputResource = new WorkflowOutputResource();
//                    Map<String, String> ids = new HashMap<String, String>();
//                    ids.put(WorkflowCatAbstractResource.WorkflowOutputConstants.WF_TEMPLATE_ID,existingWF.getWfTemplateId());
//                    ids.put(WorkflowCatAbstractResource.WorkflowOutputConstants.OUTPUT_KEY,output.getName());
//                    WorkflowOutputResource existingOutput = (WorkflowOutputResource)outputResource.get(ids);
//                    existingOutput.setWorkflowResource(existingWF);
//                    existingOutput.setOutputKey(output.getName());
//                    existingOutput.setOutputVal(output.getValue());
//                    existingOutput.setWfTemplateId(existingWF.getWfTemplateId());
//                    existingOutput.setDataType(output.getType().toString());
//                    existingOutput.setDataType(output.getType().toString());
//                    existingOutput.setAppArgument(output.getApplicationArgument());
//                    existingOutput.setDataNameLocation(output.getLocation());
//                    existingOutput.setRequired(output.isIsRequired());
//                    existingOutput.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                    existingOutput.setOutputStreaming(output.isOutputStreaming());
//                    existingOutput.setDataMovement(output.isDataMovement());
//                    existingOutput.save();
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating the workflow...", e);
//            throw new WorkflowCatalogException(e);
//        }
    }

    @Override
    public String getWorkflowId(String workflowName) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            List<WorkflowCatalogResource> resourceList = resource.get(WorkflowCatAbstractResource.WorkflowConstants.WORKFLOW_NAME, workflowName);
//            if (resourceList != null && !resourceList.isEmpty()){
//                WorkflowResource wfResource = (WorkflowResource)resourceList.get(0);
//                return wfResource.getWfTemplateId();
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving the workflow with the workflow name...", e);
//            throw new WorkflowCatalogException(e);
//        }
        return null;
    }

//    @Override
//    public boolean isWorkflowExistWithName(String workflowName) throws WorkflowCatalogException {
//        try {
//            WorkflowResource resource = new WorkflowResource();
//            List<WorkflowCatalogResource> resourceList = resource.get(WorkflowCatAbstractResource.WorkflowConstants.WORKFLOW_NAME, workflowName);
//            if (resourceList != null && !resourceList.isEmpty()){
//                return true;
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving the workflow with the workflow name...", e);
//            throw new WorkflowCatalogException(e);
//        }
//        return false;
//    }

//    @Override
//    public void updateWorkflowOutputs(String workflowId, List<OutputDataObjectType> workflowOutputs) throws WorkflowCatalogException {
//        WorkflowResource resource = new WorkflowResource();
//        WorkflowResource existingWF = (WorkflowResource)resource.get(workflowId);
//        if (workflowOutputs != null && workflowOutputs.size() != 0) {
//            for (OutputDataObjectType output : workflowOutputs) {
//                WorkflowOutputResource outputResource = new WorkflowOutputResource();
//                Map<String, String> ids = new HashMap<String, String>();
//                ids.put(WorkflowCatAbstractResource.WorkflowOutputConstants.WF_TEMPLATE_ID, existingWF.getWfTemplateId());
//                ids.put(WorkflowCatAbstractResource.WorkflowOutputConstants.OUTPUT_KEY, output.getName());
//                WorkflowOutputResource existingOutput = (WorkflowOutputResource) outputResource.get(ids);
//                existingOutput.setWorkflowResource(existingWF);
//                existingOutput.setOutputKey(output.getName());
//                existingOutput.setOutputVal(output.getValue());
//                existingOutput.setWfTemplateId(existingWF.getWfTemplateId());
//                existingOutput.setDataType(output.getType().toString());
//                existingOutput.setDataType(output.getType().toString());
//                existingOutput.setAppArgument(output.getApplicationArgument());
//                existingOutput.setDataNameLocation(output.getLocation());
//                existingOutput.setRequired(output.isIsRequired());
//                existingOutput.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                existingOutput.setOutputStreaming(output.isOutputStreaming());
//                existingOutput.setDataMovement(output.isDataMovement());
//                existingOutput.save();
//            }
//        }
//    }
}
