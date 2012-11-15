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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowData;
import org.apache.airavata.workflow.model.wf.WorkflowInput;

public class WorkflowManagerImpl implements WorkflowManager {
	private AiravataClient client;

	public WorkflowManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
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
			
			if (getClient().getUserWFResourceClient().isWorkflowExists(workflow.getName())) {
				getClient().getUserWFResourceClient().updateWorkflow(workflow.getName(),workflowAsString);
			}else{
				getClient().getUserWFResourceClient().addWorkflow(workflow.getName(),workflowAsString);
			}
			if (owner==null){
				getClient().getPublishedWFResourceClient().publishWorkflow(workflow.getName());
			}
			return true;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<Workflow> getWorkflows(String owner)
			throws AiravataAPIInvocationException {
		try {
			List<Workflow> workflows=new ArrayList<Workflow>();
			Map<String, String> workflowMap = getClient().getUserWFResourceClient().getWorkflows();
			for(String workflowStr:workflowMap.values()){
				workflows.add(getWorkflowFromString(workflowStr));
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
			Map<String, String> workflows;
			workflows = getClient().getUserWFResourceClient().getWorkflows();
			for (String name : workflows.keySet()) {
				workflowList.add(name);
			}
			return workflowList;
		} catch (Exception e) {
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
			return getClient().getUserWFResourceClient().getWorkflowGraphXML(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean deleteWorkflow(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		try {
			getClient().getUserWFResourceClient().removeWorkflow(workflowName);
			return true;
		} catch (Exception e) {
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
		return getClient().getCurrentUser();
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

	@Override
	public List<String> getWorkflowServiceNodeIDs(String templateID) throws AiravataAPIInvocationException{
        return getWorkflow(templateID).getWorkflowServiceNodeIDs();
	}

	@Override
	public boolean isPublishedWorkflowExists(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getPublishedWFResourceClient().isPublishedWorkflowExists(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void publishWorkflow(String workflowName, String publishWorkflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getPublishedWFResourceClient().publishWorkflow(workflowName, publishWorkflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void publishWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getPublishedWFResourceClient().publishWorkflow(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getPublishedWorkflowGraphXML(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getPublishedWFResourceClient().getPublishedWorkflowGraphXML(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public Workflow getPublishedWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflowFromString(getPublishedWorkflowGraphXML(workflowName));
	}

	@Override
	public List<String> getPublishedWorkflowNames()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getPublishedWFResourceClient().getPublishedWorkflowNames();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Map<String, Workflow> getPublishedWorkflows()
			throws AiravataAPIInvocationException {
		try {
			Map<String, Workflow> workflows=new HashMap<String, Workflow>();
			Map<String, String> publishedWorkflows = getClient().getPublishedWFResourceClient().getPublishedWorkflows();
			for (String name : publishedWorkflows.keySet()) {
				workflows.put(name, getWorkflowFromString(publishedWorkflows.get(name)));
			}
			return workflows;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void removePublishedWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getPublishedWFResourceClient().removePublishedWorkflow(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<WorkflowInput> getWorkflowInputs(String workflowName) throws AiravataAPIInvocationException, Exception {
		return getWorkflow(workflowName).getWorkflowInputs();
	}

	@Override
	public List<WorkflowInput> getWorkflowInputs(WorkflowData workflowData) throws AiravataAPIInvocationException, Exception {
		if (workflowData.isPublished()){
			return getWorkflowFromString(getClient().getPublishedWFResourceClient().getPublishedWorkflowGraphXML(workflowData.getName())).getWorkflowInputs();
		}else{
			return getWorkflowInputs(workflowData.getName());
		}
	}

	@Override
	public List<WorkflowData> getAllWorkflows() throws AiravataAPIInvocationException {
		List<WorkflowData> list = new ArrayList<WorkflowData>();
		List<String> workflowTemplateIds = getWorkflowTemplateIds();
		try {
			for (String id : workflowTemplateIds) {
				list.add(new WorkflowData(id,null,false));
			}
			List<String> publishedWorkflowNames = getClient().getPublishedWFResourceClient().getPublishedWorkflowNames();
			for (String id : publishedWorkflowNames) {
				list.add(new WorkflowData(id,null,false));
			}
			return list;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

}
