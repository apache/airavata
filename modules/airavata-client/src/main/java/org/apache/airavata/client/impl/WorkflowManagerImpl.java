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

package org.apache.airavata.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.namespace.QName;

import org.apache.airavata.client.airavata.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.wf.Workflow;

public class WorkflowManagerImpl implements WorkflowManager {
	private AiravataClient client;
    private static final String OGCE_WORKFLOW_NS = "http://workflow.ogce.org/";

	public WorkflowManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

	private String getWorkflowStringFromNode(Node node) throws ValueFormatException, IllegalStateException, PathNotFoundException, RepositoryException{
		return node.getProperty("workflow").getValue().getString();
	}
	
	@Override
	public boolean saveWorkflow(String workflowAsString, String owner)
			throws AiravataAPIInvocationException {
		return saveWorkflow(getWorkflowFromString(workflowAsString), workflowAsString, owner);
	}

	@Override
	public boolean saveWorkflow(Workflow workflow, String owner)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
	}
	
	private boolean saveWorkflow(Workflow workflow, String workflowAsString,String owner)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().saveWorkflow(new QName(OGCE_WORKFLOW_NS,StringUtil.convertToJavaIdentifier(workflow.getName())), workflow.getName(), workflow.getDescription(), workflowAsString, owner, owner==null? true:false);
			return true;
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<Workflow> getWorkflows(String owner)
			throws AiravataAPIInvocationException {
		try {
			List<Workflow> workflows=new ArrayList<Workflow>();
			Map<QName, Node> workflowMap = getClient().getRegistry().getWorkflows(owner);
			for(Node node:workflowMap.values()){
				workflows.add(getWorkflowFromString(getWorkflowStringFromNode(node)));
			}
			return workflows;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<String> getWorkflowTemplateIds(String owner)
			throws AiravataAPIInvocationException {
		try {
			List<String> workflowList = new ArrayList<String>();
			Map<QName, Node> workflows;
			workflows = getClient().getRegistry().getWorkflows(owner);
			for (QName qname : workflows.keySet()) {
				workflowList.add(qname.getLocalPart());
			}
			return workflowList;
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Workflow getWorkflow(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		return getWorkflowFromString(getWorkflowAsString(workflowName, owner));
	}

	@Override
	public String getWorkflowAsString(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		try {
			return getWorkflowStringFromNode(getClient().getRegistry().getWorkflow(new QName(workflowName), owner));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean deleteWorkflow(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().deleteWorkflow(new QName(workflowName), owner);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean saveWorkflow(String workflowAsString)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflowAsString, getCurrentUser());
	}

	@Override
	public boolean saveWorkflowAsPublic(String workflowAsString)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflowAsString, null);
	}

	@Override
	public boolean saveWorkflow(Workflow workflow)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, getCurrentUser());
	}

	private String getCurrentUser() {
		return getClient().getUserManager().getAiravataUser();
	}

	@Override
	public boolean saveWorkflowAsPublic(Workflow workflow)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, null);
	}

	@Override
	public List<Workflow> getWorkflows() throws AiravataAPIInvocationException {
		return getWorkflows(getCurrentUser());
	}

	@Override
	public List<String> getWorkflowTemplateIds()
			throws AiravataAPIInvocationException {
		return getWorkflowTemplateIds(getCurrentUser());
	}

	@Override
	public Workflow getWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflow(workflowName, getCurrentUser());
	}

	@Override
	public String getWorkflowAsString(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflowAsString(workflowName, getCurrentUser());
	}

	@Override
	public boolean deleteWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return deleteWorkflow(workflowName, getCurrentUser());
	}

	@Override
	public Workflow getWorkflowFromString(String workflowAsString)
			throws AiravataAPIInvocationException {
		try {
			return new Workflow(workflowAsString);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getWorkflowAsString(Workflow workflow)
			throws AiravataAPIInvocationException {
		return XMLUtil.xmlElementToString(workflow.toXML());
	}

}
