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

import java.util.List;


public interface WorkflowExecution {
	public String getExperimentId();
	public String getTopic();
	public WorkflowInstanceStatus getExecutionStatus();
	public String getUser();
	public List<WorkflowNodeIOData> getServiceInput();
	public List<WorkflowNodeIOData> getServiceOutput();
	public List<WorkflowIOData> getOutput();
	public WorkflowNodeIOData getServiceInput(String nodeId);
	public WorkflowNodeIOData getServiceOutput(String nodeId);
	public WorkflowIOData getOutput(String nodeId);
	public String getMetadata();
	public String getWorkflowInstanceName();
	
	public void setExperimentId(String experimentId);
	public void setTopic(String topic);
	public void setExecutionStatus(WorkflowInstanceStatus executionStatus);
	public void setUser(String user);
	public void setServiceInput(List<WorkflowNodeIOData> serviceInputs);
	public void setServiceOutput(List<WorkflowNodeIOData> serviceOutputs);
	public void setOutput(List<WorkflowIOData> outputs);
	public void addServiceInput(WorkflowNodeIOData serviceInput);
	public void addServiceOutput(WorkflowNodeIOData serviceOutput);
	public void addOutput(WorkflowIOData output);
	public void setMetadata(String metadata);
	public void setWorkflowInstanceName(String workflowInstanceName);
}
