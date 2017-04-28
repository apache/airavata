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
package org.apache.airavata.registry.core.workflow.catalog.utils;

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.registry.core.workflow.catalog.resources.WorkflowCatAbstractResource;
import org.apache.airavata.registry.core.workflow.catalog.resources.WorkflowCatalogResource;
import org.apache.airavata.registry.core.workflow.catalog.resources.WorkflowInputResource;
import org.apache.airavata.registry.core.workflow.catalog.resources.WorkflowResource;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;

import java.util.ArrayList;
import java.util.List;

public class WorkflowCatalogThriftConversion {

    public static InputDataObjectType getWorkflowInput (WorkflowInputResource resource){
        InputDataObjectType input = new InputDataObjectType();
        input.setName(resource.getInputKey());
        input.setApplicationArgument(resource.getAppArgument());
        input.setInputOrder(resource.getInputOrder());
        input.setType(DataType.valueOf(resource.getDataType()));
        input.setMetaData(resource.getMetadata());
        input.setUserFriendlyDescription(resource.getUserFriendlyDesc());
        input.setIsRequired(resource.getRequired());
        input.setRequiredToAddedToCommandLine(resource.getRequiredToCMD());
        input.setDataStaged(resource.isDataStaged());
        return input;
    }

    public static List<InputDataObjectType> getWFInputs(List<WorkflowCatalogResource> resources){
        List<InputDataObjectType> inputResources = new ArrayList<InputDataObjectType>();
        if (resources != null && !resources.isEmpty()){
            for (WorkflowCatalogResource resource : resources){
                inputResources.add(getWorkflowInput((WorkflowInputResource) resource));
            }
        }
        return inputResources;
    }

    public static WorkflowModel getWorkflow (WorkflowResource resource) throws WorkflowCatalogException {
        WorkflowModel workflow = new WorkflowModel();
        workflow.setTemplateId(resource.getWfTemplateId());
        workflow.setGraph(resource.getGraph());
        workflow.setName(resource.getWfName());
        if (resource.getImage() != null){
            workflow.setImage(resource.getImage().getBytes());
        }
        WorkflowInputResource inputResource = new WorkflowInputResource();
        List<WorkflowCatalogResource> resources = inputResource.get(WorkflowCatAbstractResource.WorkflowInputConstants.WF_TEMPLATE_ID, resource.getWfTemplateId());
        workflow.setWorkflowInputs(getWFInputs(resources));

        return workflow;
    }
}
