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

package org.apache.airavata.registry.api.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;

public class WorkflowInstanceData {
	private WorkflowInstance workflowInstance;
	private ExperimentData experimentData;
	private WorkflowInstanceStatus workflowInstanceStatus;
	
	private List<WorkflowInstanceNodeData> nodeDataList;
	
	public WorkflowInstanceData(ExperimentData experimentData, WorkflowInstance workflowInstance,WorkflowInstanceStatus workflowInstanceStatus,List<WorkflowInstanceNodeData> nodeDataList) {
		this.experimentData=experimentData;
		this.workflowInstance=workflowInstance;
		this.workflowInstanceStatus=workflowInstanceStatus;
		this.nodeDataList=nodeDataList;
	}

	public List<WorkflowInstanceNodeData> getNodeDataList() {
		if (nodeDataList==null){
			nodeDataList=new ArrayList<WorkflowInstanceNodeData>();
		}
		return nodeDataList;
	}
	
	public void addNodeData(WorkflowInstanceNodeData...nodeData){
		getNodeDataList().addAll(Arrays.asList(nodeData));
	}
	
	public String getExperimentId(){
		return workflowInstance.getExperimentId();
	}
	
	public String getTopicId(){
		return workflowInstance.getWorkflowInstanceId();
	}
	
	public String getWorkflowName(){
		return workflowInstance.getWorkflowName();
	}
	
	public ExecutionStatus getStatus(){
		return workflowInstanceStatus.getExecutionStatus();
	}
	
	public Date getStatusUpdateTime(){
		return workflowInstanceStatus.getStatusUpdateTime();
	}

	public WorkflowInstanceNodeData getNodeData(String nodeId){
		for (WorkflowInstanceNodeData nodeData : getNodeDataList()) {
			if (nodeData.getWorkflowInstanceNode().getNodeId().equals(nodeId)){
				return nodeData;
			}
		}
		return null;
	}
	
	public List<WorkflowInstanceNodeData> getInputNodeData(){
		//TODO
		return null;
	}
	
	public List<WorkflowInstanceNodeData> getOutputNodeData(){
		//TODO
		return null;
	}

	public ExperimentData getExperimentData() {
		return experimentData;
	}

	public void setExperimentData(ExperimentData experimentData) {
		this.experimentData = experimentData;
	}

}
