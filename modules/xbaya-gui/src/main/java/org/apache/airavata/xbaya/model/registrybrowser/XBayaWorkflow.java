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
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;

public class XBayaWorkflow {
	private List<XBayaWorkflowNodeElement> workflowServices;
	private WorkflowExecution workflowInstance;
	private AiravataAPI airavataAPI;
	
	public XBayaWorkflow(WorkflowExecution workflowInstance, AiravataAPI airavataAPI) {
		setWorkflowInstance(workflowInstance);
		setAiravataAPI(airavataAPI);
	}

	public List<XBayaWorkflowNodeElement> getWorkflowServices() {
		if (workflowServices==null){
			workflowServices=new ArrayList<XBayaWorkflowNodeElement>();
			try {
				WorkflowExecutionData workflowInstanceData = getAiravataAPI().getProvenanceManager().getWorkflowInstanceData(getWorkflowId(), getWorkflowId());
				List<NodeExecutionData> nodeDataList = workflowInstanceData.getNodeDataList();
				for (NodeExecutionData nodeData : nodeDataList) {
					workflowServices.add(new XBayaWorkflowNodeElement(nodeData.getWorkflowInstanceNode().getNodeId(), nodeData));
				}
			} catch (AiravataAPIInvocationException e) {
				e.printStackTrace();
			} catch (ExperimentLazyLoadedException e) {
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
		return getWorkflowInstance().getWorkflowExecutionId();
	}

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public WorkflowExecution getWorkflowInstance() {
		return workflowInstance;
	}

	public void setWorkflowInstance(WorkflowExecution workflowInstance) {
		this.workflowInstance = workflowInstance;
	}
}
