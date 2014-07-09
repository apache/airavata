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

import org.apache.airavata.api.workflow.Workflow.Iface;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.model.Workflow;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowServerHandler implements Iface {
    private static final Logger log = LoggerFactory.getLogger(WorkflowServerHandler.class);

	private AiravataAPI airavataAPI;

	@Override
	public List<String> getAllWorkflows() throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			return getAiravataAPI().getWorkflowManager().getWorkflowTemplateIds();
		} catch (AiravataAPIInvocationException e) {
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
			org.apache.airavata.workflow.model.wf.Workflow w = getAiravataAPI().getWorkflowManager().getWorkflow(workflowTemplateId);
			Workflow workflow = new Workflow();
			workflow.setTemplateId(workflowTemplateId);
			workflow.setGraph(XMLUtil.xmlElementToString(w.toXML()));
			workflow.setName(w.getName());
			return workflow;
		} catch (AiravataAPIInvocationException e) {
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
			getAiravataAPI().getWorkflowManager().deleteWorkflow(workflowTemplateId);
		} catch (AiravataAPIInvocationException e) {
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
			getAiravataAPI().getWorkflowManager().addWorkflow(workflow.getGraph());
			return workflow.getName();
		} catch (AiravataAPIInvocationException e) {
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
			getAiravataAPI().getWorkflowManager().updateWorkflow(workflowTemplateId, workflow.getGraph());
		} catch (AiravataAPIInvocationException e) {
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
			if (getAiravataAPI().getWorkflowManager().isWorkflowExists(workflowName)){
				return workflowName;
			}
			AiravataClientException airavataClientException = new AiravataClientException(AiravataErrorType.UNKNOWN);
			airavataClientException.setParameter("No worklfow exists with the name "+workflowName);
			throw airavataClientException;
		} catch (AiravataAPIInvocationException e) {
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
			return getAiravataAPI().getWorkflowManager().isWorkflowExists(workflowName);
		} catch (AiravataAPIInvocationException e) {
			String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
			log.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}
	
	private String getAiravataUserName() throws ApplicationSettingsException {
		return ServerSettings.getDefaultUserGateway();
	}

	private String getGatewayName() throws ApplicationSettingsException {
		return ServerSettings.getDefaultUser();
	}

	private AiravataAPI getAiravataAPI() {
		if (airavataAPI == null) {
			try {
				airavataAPI = AiravataAPIFactory.getAPI(getGatewayName(),
						getAiravataUserName());
			} catch (Exception e) {
				log.error("Unable to create Airavata API", e);
			}
		}
		return airavataAPI;
	}
}
