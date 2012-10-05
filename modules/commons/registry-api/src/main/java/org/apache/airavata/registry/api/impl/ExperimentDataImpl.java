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
import java.util.List;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;

public class ExperimentDataImpl implements ExperimentData{
	private WorkflowInstanceStatus executionStatus;
	private String user;
	private List<WorkflowIOData> output;
	private String experimentId;
	private String metadata;
	private String workflowInstanceName;
	private List<WorkflowInstanceData> workflowInstanceDataList=new ArrayList<WorkflowInstanceData>();
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
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
	public String getTopic() {
		return experimentId;
	}
	public void setTopic(String topic) {
		this.experimentId = topic;
	}
	public WorkflowInstanceStatus getExecutionStatus() {
		return executionStatus;
	}
	public void setExecutionStatus(WorkflowInstanceStatus executionStatus) {
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
	public List<WorkflowInstanceData> getWorkflowInstanceData() throws ExperimentLazyLoadedException{
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
}
