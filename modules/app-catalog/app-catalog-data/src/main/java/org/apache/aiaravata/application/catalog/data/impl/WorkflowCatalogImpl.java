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

package org.apache.aiaravata.application.catalog.data.impl;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.WorkflowCatalog;
import org.apache.aiaravata.application.catalog.data.resources.AbstractResource;
import org.apache.aiaravata.application.catalog.data.resources.Resource;
import org.apache.aiaravata.application.catalog.data.resources.WorkflowResource;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WorkflowCatalogImpl implements WorkflowCatalog {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowCatalogImpl.class);

    @Override
    public List<String> getAllWorkflows() throws AppCatalogException {
        List<String> workflowIds = new ArrayList<String>();
        try {
            WorkflowResource resource = new WorkflowResource();
            workflowIds = resource.getAllIds();
        } catch (Exception e) {
            logger.error("Error while retrieving all the workflow template ids...", e);
            throw new AppCatalogException(e);
        }
        return workflowIds;
    }

    @Override
    public Workflow getWorkflow(String workflowTemplateId) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            WorkflowResource wfResource = (WorkflowResource)resource.get(workflowTemplateId);
            return AppCatalogThriftConversion.getWorkflow(wfResource);
        } catch (Exception e) {
            logger.error("Error while retrieving the workflow...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void deleteWorkflow(String workflowTemplateId) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            resource.remove(workflowTemplateId);
        } catch (Exception e) {
            logger.error("Error while deleting the workflow...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String registerWorkflow(Workflow workflow) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            resource.setWfTemplateId(AppCatalogUtils.getID(workflow.getName()));
            resource.setWfName(workflow.getName());
            resource.setGraph(workflow.getGraph());
            resource.save();
            workflow.setTemplateId(resource.getWfTemplateId());
            return resource.getWfTemplateId();
        } catch (Exception e) {
            logger.error("Error while saving the workflow...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateWorkflow(String workflowTemplateId, Workflow workflow) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            WorkflowResource existingWF = (WorkflowResource)resource.get(workflowTemplateId);
            existingWF.setWfName(workflow.getName());
            existingWF.setGraph(workflow.getGraph());
            existingWF.save();
        } catch (Exception e) {
            logger.error("Error while updating the workflow...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String getWorkflowTemplateId(String workflowName) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            List<Resource> resourceList = resource.get(AbstractResource.WorkflowConstants.WF_NAME, workflowName);
            if (resourceList != null && !resourceList.isEmpty()){
                WorkflowResource wfResource = (WorkflowResource)resourceList.get(0);
                return wfResource.getWfTemplateId();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the workflow with the workflow name...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public boolean isWorkflowExistWithName(String workflowName) throws AppCatalogException {
        try {
            WorkflowResource resource = new WorkflowResource();
            List<Resource> resourceList = resource.get(AbstractResource.WorkflowConstants.WF_NAME, workflowName);
            if (resourceList != null && !resourceList.isEmpty()){
                return true;
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the workflow with the workflow name...", e);
            throw new AppCatalogException(e);
        }
        return false;
    }
}
