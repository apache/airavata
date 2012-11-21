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

package org.apache.airavata.xbaya.model.registrybrowser;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;

public class XBayaWorkflow {
	private List<XBayaWorkflowNodeElement> workflowServices;
	private WorkflowInstance workflowInstance;
	private AiravataAPI airavataAPI;
	
	public XBayaWorkflow(WorkflowInstance workflowInstance, AiravataAPI airavataAPI) {
		setWorkflowInstance(workflowInstance);
		setAiravataAPI(airavataAPI);
	}

	public List<XBayaWorkflowNodeElement> getWorkflowServices() {
		if (workflowServices==null){
			workflowServices=new ArrayList<XBayaWorkflowNodeElement>();
			try {
				WorkflowInstanceData workflowInstanceData = getAiravataAPI().getProvenanceManager().getWorkflowInstanceData(getWorkflowId(), getWorkflowId());
				List<WorkflowInstanceNodeData> nodeDataList = workflowInstanceData.getNodeDataList();
				for (WorkflowInstanceNodeData nodeData : nodeDataList) {
					workflowServices.add(new XBayaWorkflowNodeElement(nodeData.getWorkflowInstanceNode().getNodeId(), nodeData));
				}
			} catch (AiravataAPIInvocationException e) {
				e.printStackTrace();
			}
		}
		return workflowServices;
	}

	public void setWorkflowNodes(List<XBayaWorkflowNodeElement> workflowServices) {
		this.workflowServices = workflowServices;
	}
	
	public void add(XBayaWorkflowNodeElement workflowService){
		getWorkflowServices().add(workflowService);
	}

	public String getWorkflowName() {
		return getWorkflowInstance().getTemplateName();
	}

	public String getWorkflowId() {
		return getWorkflowInstance().getWorkflowInstanceId();
	}

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public WorkflowInstance getWorkflowInstance() {
		return workflowInstance;
	}

	public void setWorkflowInstance(WorkflowInstance workflowInstance) {
		this.workflowInstance = workflowInstance;
	}
}
