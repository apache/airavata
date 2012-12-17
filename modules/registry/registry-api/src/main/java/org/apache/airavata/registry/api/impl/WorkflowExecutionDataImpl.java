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

package org.apache.airavata.registry.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class WorkflowExecutionDataImpl implements WorkflowExecutionData {
	private WorkflowExecution workflowInstance;

	@XmlTransient
	private ExperimentDataImpl experimentData;

	private WorkflowExecutionStatus workflowInstanceStatus;

	private List<NodeExecutionData> nodeDataList;

	public WorkflowExecutionDataImpl() {
	}

	public WorkflowExecutionDataImpl(ExperimentData experimentData,
			WorkflowExecution workflowInstance,
			WorkflowExecutionStatus workflowInstanceStatus,
			List<NodeExecutionData> nodeDataList) {
		this.experimentData = (ExperimentDataImpl) experimentData;
		this.workflowInstance = workflowInstance;
		this.workflowInstanceStatus = workflowInstanceStatus;
		this.nodeDataList = nodeDataList;
	}

	public WorkflowExecution getWorkflowExecution() {
		return workflowInstance;
	}

	public List<NodeExecutionData> getNodeDataList() {
		if (nodeDataList == null) {
			nodeDataList = new ArrayList<NodeExecutionData>();
		}
		return nodeDataList;
	}

	public void addNodeData(NodeExecutionData... nodeData) {
		getNodeDataList().addAll(Arrays.asList(nodeData));
	}

	public String getExperimentId() {
		return workflowInstance.getExperimentId();
	}

	/**
	 * @deprecated Use "getWorkflowInstanceID() instead
	 * @return
	 */
	public String getTopicId() {
		return workflowInstance.getWorkflowExecutionId();
	}

	/**
	 * @deprecated Use getId() instead
	 */
	public String getWorkflowInstanceId() {
		return workflowInstance.getWorkflowExecutionId();
	}

	/**
	 * @deprecated Use "WorkflowInstanceData.getTemplateName()" instead
	 * @return
	 */
	public String getWorkflowName() {
		return getTemplateName();
	}

	public String getTemplateName() {
		return workflowInstance.getTemplateName();
	}

	public State getState() {
		return workflowInstanceStatus.getExecutionStatus();
	}

	public Date getStatusUpdateTime() {
		return workflowInstanceStatus.getStatusUpdateTime();
	}

	public NodeExecutionData getNodeData(String nodeId) {
		for (NodeExecutionData nodeData : getNodeDataList()) {
			if (nodeData.getWorkflowInstanceNode().getNodeId().equals(nodeId)) {
				return nodeData;
			}
		}
		return null;
	}

	public ExperimentData getExperimentData() {
		return experimentData;
	}

	public void setExperimentData(ExperimentDataImpl experimentData) {
		this.experimentData = experimentData;
	}

	public String getId() {
		return workflowInstance.getWorkflowExecutionId();
	}

	@Override
	public List<InputData> getWorkflowInputs() {
		List<InputData> workflowInstanceNodePortDatas = new ArrayList<InputData>();
		for (NodeExecutionData workflowInstanceNodeData : getNodeDataList(WorkflowNodeType.WorkflowNode.INPUTNODE)) {
			workflowInstanceNodePortDatas.addAll(convertToInputDataList(workflowInstanceNodeData.getOutputData()));
		}
		return workflowInstanceNodePortDatas;
	}

	private List<InputData> convertToInputDataList(List<OutputData> outputData) {
		List<InputData> i = new ArrayList<InputData>();
		for (OutputData o : outputData) {
			i.add(new InputData(o.getWorkflowInstanceNode(),o.getName(),o.getValue()));
		}
		return i;
	}
	private List<OutputData> convertToOutputDataList(List<InputData> outputData) {
		List<OutputData> i = new ArrayList<OutputData>();
		for (InputData o : outputData) {
			i.add(new OutputData(o.getWorkflowInstanceNode(),o.getName(),o.getValue()));
		}
		return i;
	}

	@Override
	public List<NodeExecutionData> getNodeDataList(WorkflowNode type) {
		List<NodeExecutionData> workflowInstanceNodePortDatas = new ArrayList<NodeExecutionData>();
		for (NodeExecutionData workflowInstanceNodeData : getNodeDataList()) {
			if (workflowInstanceNodeData.getType().equals(type)) {
				workflowInstanceNodePortDatas.add(workflowInstanceNodeData);
			}
		}
		return workflowInstanceNodePortDatas;
	}

	@Override
	public List<OutputData> getWorkflowOutputs() {
		List<OutputData> workflowInstanceNodePortDatas = new ArrayList<OutputData>();
		for (NodeExecutionData workflowInstanceNodeData : getNodeDataList(WorkflowNodeType.WorkflowNode.OUTPUTNODE)) {
			workflowInstanceNodePortDatas.addAll(convertToOutputDataList(workflowInstanceNodeData
					.getInputData()));
		}
		return workflowInstanceNodePortDatas;

	}

}
