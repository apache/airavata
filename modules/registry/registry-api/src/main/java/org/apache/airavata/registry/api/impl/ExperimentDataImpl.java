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
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.InputData;
import org.apache.airavata.registry.api.workflow.OutputData;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExperimentDataImpl implements ExperimentData{
	private WorkflowExecutionStatus executionStatus;
	private String user;
	private List<WorkflowIOData> output;
	private String experimentId;
	private String metadata;
	private String workflowInstanceName;
	private List<WorkflowExecutionDataImpl> workflowInstanceDataList=new ArrayList<WorkflowExecutionDataImpl>();
	private boolean lazyLoaded=false;

    public ExperimentDataImpl() {
        this(false);
    }

    public ExperimentDataImpl(boolean lazyLoaded) {
        this.lazyLoaded = lazyLoaded;
    }

    public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public String getExperimentId() {
		return experimentId;
	}

    @Override
    public String getTemplateName() throws ExperimentLazyLoadedException {
        return getWorkflowExecutionDataList().get(0).getTemplateName();
    }

    @Override
    public WorkflowExecutionStatus.State getState() throws ExperimentLazyLoadedException {
        return getWorkflowExecutionDataList().get(0).getState();
    }

    @Override
    public Date getStatusUpdateTime() throws ExperimentLazyLoadedException {
        return getWorkflowExecutionDataList().get(0).getStatusUpdateTime();
    }

    @Override
    public ExperimentDataImpl getExperimentData() throws ExperimentLazyLoadedException {
        return getWorkflowExecutionDataList().get(0).getExperimentData();
    }

    @Override
    public void setExperimentData(ExperimentDataImpl experimentData) throws ExperimentLazyLoadedException {
        getWorkflowExecutionDataList().get(0).setExperimentData(experimentData);
    }

    public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
	public String getTopic() {
		return experimentId;
	}
	public void setTopic(String topic) {
		this.experimentId = topic;
	}
	public WorkflowExecutionStatus getExecutionStatus() {
		return executionStatus;
	}
	public void setExecutionStatus(WorkflowExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public List<WorkflowIOData> getOutput() {
		if (output==null){
			output=new ArrayList<WorkflowIOData>();
		}
		return output;
	}
	public void setOutput(List<WorkflowIOData> output) {
		this.output = output;
	}
	public void addOutput(WorkflowIOData output) {
		getOutput().add(output);
	}
	public WorkflowIOData getOutput(String nodeId) {
		return (WorkflowNodeIOData)getIOData(nodeId, getOutput());
	}

	private WorkflowIOData getIOData(String nodeId, List<?> list) {
		for (Object data : list) {
			WorkflowIOData iodata=(WorkflowIOData)data;
			if (iodata.getNodeId().equals(nodeId)){
				return iodata;
			}
		}
		return null;
	}
	
	@Override
	public String getExperimentName() {
		return workflowInstanceName;
	}
	
	@Override
	public void setExperimentName(String workflowInstanceName) {
		this.workflowInstanceName=workflowInstanceName;
		
	}

    @Override
    public WorkflowExecutionData getWorkflowExecutionData(String workflowInstanceID) throws ExperimentLazyLoadedException {
        for(WorkflowExecutionData workflowInstanceData : getWorkflowExecutionDataList()){
            if(workflowInstanceData.getId().equals(workflowInstanceID)){
                return workflowInstanceData;
            }
        }
        return null;
    }

    @Override
	public List<WorkflowExecutionDataImpl> getWorkflowExecutionDataList() throws ExperimentLazyLoadedException{
		if (isLazyLoaded()){
			throw new ExperimentLazyLoadedException(getExperimentId());
		}
		return workflowInstanceDataList;
	}

	public boolean isLazyLoaded() {
		return lazyLoaded;
	}

	public void setLazyLoaded(boolean lazyLoaded) {
		this.lazyLoaded = lazyLoaded;
	}

    @Override
    public WorkflowExecution getWorkflowExecution() throws ExperimentLazyLoadedException{
        return getWorkflowExecutionDataList().get(0).getWorkflowExecution();
    }

    @Override
    public List<NodeExecutionData> getNodeDataList() throws ExperimentLazyLoadedException{
        return getWorkflowExecutionDataList().get(0).getNodeDataList();
    }

    @Override
    public void addNodeData(NodeExecutionData... nodeData) throws ExperimentLazyLoadedException {
    	getWorkflowExecutionDataList().get(0).addNodeData(nodeData);
    }

    @Override
    public NodeExecutionData getNodeData(String nodeId) throws ExperimentLazyLoadedException {
        return getWorkflowExecutionDataList().get(0).getNodeData(nodeId);
    }

	@Override
	public String getId() {
		return getExperimentId();
	}

	@Override
	public List<NodeExecutionData> getNodeDataList(WorkflowNode type)
			throws ExperimentLazyLoadedException {
		return getWorkflowExecutionDataList().get(0).getNodeDataList(type);
	}

	@Override
	public List<InputData> getWorkflowInputs() throws ExperimentLazyLoadedException{
		return getWorkflowExecutionDataList().get(0).getWorkflowInputs();
	}

	@Override
	public List<OutputData> getWorkflowOutputs()throws ExperimentLazyLoadedException {
		return getWorkflowExecutionDataList().get(0).getWorkflowOutputs();
	}
}
