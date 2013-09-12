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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeExecutionData {
	private WorkflowInstanceNode workflowInstanceNode;
	private List<InputData> inputData;
	private List<OutputData> outputData;
	private String input;
	private String output;
    private NodeExecutionStatus status;
    private WorkflowNodeType.WorkflowNode type;
    private String experimentId;
    private String workflowExecutionId;
    private String nodeId;

    public NodeExecutionData() {
    }

    /**
     * deprecated Use <code>NodeExecutionData(String experimentId, String workflowExecutionId, String nodeId)</code> instead
     * @param workflowInstanceNode
     */
    public NodeExecutionData(WorkflowInstanceNode workflowInstanceNode) {
		this(workflowInstanceNode.getWorkflowInstance().getExperimentId(),workflowInstanceNode.getWorkflowInstance().getWorkflowExecutionId(),workflowInstanceNode.getNodeId());
		setWorkflowInstanceNode(workflowInstanceNode);
	}
    
    public NodeExecutionData(String experimentId, String workflowExecutionId, String nodeId) {
		this.experimentId=experimentId;
		this.workflowExecutionId=workflowExecutionId;
		this.nodeId=nodeId;
	}

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getId()
	 */
	public String getId(){
    	return nodeId;
    }

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getExperimentId()
	 */
	public String getExperimentId(){
    	return experimentId;
    }
    
    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getWorkflowExecutionId()
	 */
	public String getWorkflowExecutionId(){
    	return workflowExecutionId;
    }
    
    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getWorkflowInstanceNode()
	 */
	public WorkflowInstanceNode getWorkflowInstanceNode() {
		return workflowInstanceNode;
	}

	/* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setWorkflowInstanceNode(org.apache.airavata.registry.api.workflow.WorkflowInstanceNode)
	 */
	public void setWorkflowInstanceNode(WorkflowInstanceNode workflowInstanceNode) {
		this.workflowInstanceNode = workflowInstanceNode;
	}

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getStatus()
	 */
	public NodeExecutionStatus getStatus() {
        return status;
    }

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setStatus(org.apache.airavata.registry.api.workflow.NodeExecutionStatus)
	 */
	public void setStatus(NodeExecutionStatus status) {
        this.status = status;
    }

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setStatus(org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State, java.util.Date)
	 */
	public void setStatus(WorkflowExecutionStatus.State status, Date date) {
        setStatus(new NodeExecutionStatus(this.workflowInstanceNode, status, date));

    }

	/* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getInputData()
	 */
	public List<InputData> getInputData() {
		if (inputData==null){
			inputData=new ArrayList<InputData>();
			List<NameValue> data = getIOParameterData(getInput());
			for (NameValue nameValue : data) {
				inputData.add(new InputData(getWorkflowInstanceNode(), nameValue.name, nameValue.value));
			}
		}
		return inputData;
	}

	/* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getOutputData()
	 */
	public List<OutputData> getOutputData() {
		if (outputData==null){
			outputData=new ArrayList<OutputData>();
			List<NameValue> data = getIOParameterData(getOutput());
			for (NameValue nameValue : data) {
				outputData.add(new OutputData(getWorkflowInstanceNode(), nameValue.name, nameValue.value));
			}
		}
		return outputData;
	}

	/* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setInputData(java.util.List)
	 */
	public void setInputData(List<InputData> inputData) {
		this.inputData = inputData;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setOutputData(java.util.List)
	 */
	public void setOutputData(List<OutputData> outputData) {
		this.outputData = outputData;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#getType()
	 */
	public WorkflowNodeType.WorkflowNode getType() {
        return type;
    }

    /* (non-Javadoc)
	 * @see org.apache.airavata.registry.api.workflow.INodeExecutionData#setType(org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode)
	 */
	public void setType(WorkflowNodeType.WorkflowNode type) {
        this.type = type;
    }

	public State getState() {
		return status.getExecutionStatus();
	}

	public Date getStatusUpdateTime() {
		return status.getStatusUpdateTime();
	}
	

    private static class NameValue{
		String name;
		String value;
		public NameValue(String name, String value) {
			this.name=name;
			this.value=value;
		}
	}
	
	private static List<NameValue> getIOParameterData(String data){
		List<NameValue> parameters=new ArrayList<NameValue>();
		if (data!=null && !data.trim().equals("")) {
			String[] pairs = StringUtil.getElementsFromString(data);
			for (String paras : pairs) {
				String name=paras.trim();
				String value="";
				int i = name.indexOf("=");
				//if the paras has a value as well
				if (i!=-1){
					value=name.substring(i+1);
					name=name.substring(0,i);
					parameters.add(new NameValue(name,StringUtil.quoteString(value)));
				}else{
					parameters.get(parameters.size()-1).value=parameters.get(parameters.size()-1).value+","+StringUtil.quoteString(name);
				}
				
			}
		}
		return parameters;
	}

}
