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

package org.apache.airavata.workflow.catalog;

import java.util.Arrays;
import java.util.List;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.Workflow;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowCatalogImpl implements WorkflowCatalog {
    private static final Logger log = LoggerFactory.getLogger(WorkflowCatalogImpl.class);

	private AiravataRegistry2 registry;
	
	public List<String> getAllWorkflows() throws WorkflowCatalogException{
		try {
			return Arrays.asList(getRegistry().getWorkflows().keySet().toArray(new String[]{}));
		} catch (RegException e) {
			String msg = "Error in retrieving all workflow template Ids.";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public Workflow getWorkflow(String workflowTemplateId)
			throws WorkflowCatalogException{
		try {
			Workflow workflow = new Workflow();
			workflow.setTemplateId(workflowTemplateId);
			workflow.setGraph(getRegistry().getWorkflowGraphXML(workflowTemplateId));
			workflow.setName(workflowTemplateId);
			return workflow;
		} catch (RegException e) {
			String msg = "Error in retrieving the workflow "
					+ workflowTemplateId + ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public void deleteWorkflow(String workflowTemplateId)
			throws WorkflowCatalogException{
		try {
			getRegistry().removeWorkflow(workflowTemplateId);
		} catch (RegException e) {
			String msg = "Error in deleting the workflow " + workflowTemplateId
					+ ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public String registerWorkflow(Workflow workflow)
			throws WorkflowCatalogException {
		try {
			getRegistry().addWorkflow(workflow.getName(),workflow.getGraph());
			return workflow.getName();
		} catch (RegException e) {
			String msg = "Error in registering the workflow "
					+ workflow.getName() + ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public void updateWorkflow(String workflowTemplateId, Workflow workflow)
			throws WorkflowCatalogException {
		try {
			getRegistry().updateWorkflow(workflowTemplateId, workflow.getGraph());
		} catch (RegException e) {
			String msg = "Error in updating the workflow " + workflow.getName()
					+ ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public String getWorkflowTemplateId(String workflowName)
			throws WorkflowCatalogException {
		try {
			if (getRegistry().isWorkflowExists(workflowName)) {
				return workflowName;
			}
			WorkflowCatalogException airavataClientException = new WorkflowCatalogException("No worklfow exists with the name "
							+ workflowName);
			throw airavataClientException;
		} catch (RegException e) {
			String msg = "Error in retrieving the workflow template id for "
					+ workflowName + ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	@Override
	public boolean isWorkflowExistWithName(String workflowName)
			throws WorkflowCatalogException {
		try {
			return getRegistry().isWorkflowExists(workflowName);
		} catch (RegException e) {
			String msg = "Error in veriying the workflow for workflow name "
					+ workflowName + ".";
			log.error(msg, e);
			WorkflowCatalogException exception = new WorkflowCatalogException(msg + " More info : " + e.getMessage());
			throw exception;
		}
	}

	private String getAiravataUserName() throws ApplicationSettingsException {
		return ServerSettings.getDefaultUser();
	}

	private String getGatewayName() throws ApplicationSettingsException {
		return ServerSettings.getDefaultUserGateway();
	}

	private AiravataRegistry2 getRegistry() {
		if (registry == null) {
			try {
				registry = AiravataRegistryFactory.getRegistry(new Gateway(getGatewayName()),
						new AiravataUser(getAiravataUserName()));
			} catch (Exception e) {
				log.error("Unable to create Airavata API", e);
			}
		}
		return registry;
	}
}
