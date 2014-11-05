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

package org.apache.airavata.api.server.handler;

import java.util.List;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.WorkflowCatalog;
import org.apache.airavata.api.Workflow.Iface;
import org.apache.airavata.model.Workflow;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.workflow.catalog.WorkflowCatalogFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowServerHandler implements Iface {
    private static final Logger log = LoggerFactory.getLogger(WorkflowServerHandler.class);

	private WorkflowCatalog workflowCatalog;

	@Override
	public List<String> getAllWorkflows() throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().getAllWorkflows();
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving all workflow template Ids.";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public Workflow getWorkflow(String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().getWorkflow(workflowTemplateId);
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving the workflow "+workflowTemplateId+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public void deleteWorkflow(String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			getWorkflowCatalog().deleteWorkflow(workflowTemplateId);
		} catch (AppCatalogException e) {
			String msg = "Error in deleting the workflow "+workflowTemplateId+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public String registerWorkflow(Workflow workflow)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().registerWorkflow(workflow);
		} catch (AppCatalogException e) {
			String msg = "Error in registering the workflow "+workflow.getName()+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public void updateWorkflow(String workflowTemplateId, Workflow workflow)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			getWorkflowCatalog().updateWorkflow(workflowTemplateId, workflow);
		} catch (AppCatalogException e) {
			String msg = "Error in updating the workflow "+workflow.getName()+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public String getWorkflowTemplateId(String workflowName)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().getWorkflowTemplateId(workflowName);
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving the workflow template id for "+workflowName+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
	public boolean isWorkflowExistWithName(String workflowName)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().isWorkflowExistWithName(workflowName);
		} catch (AppCatalogException e) {
			String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	private WorkflowCatalog getWorkflowCatalog() {
		if (workflowCatalog == null) {
			try {
				workflowCatalog = WorkflowCatalogFactory.getWorkflowCatalog();
			} catch (Exception e) {
				log.error("Unable to create Workflow Catalog", e);
			}
		}
		return workflowCatalog;
	}
}
