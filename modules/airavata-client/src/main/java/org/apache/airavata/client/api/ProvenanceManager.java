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

package org.apache.airavata.client.api;

import java.util.List;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceMetadata;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodePortData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceUser;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;

public interface ProvenanceManager {
	
	//Instance Node data
	public void addWorkflowInstanceNodeInputData(WorkflowInstanceNodePortData data) throws AiravataAPIInvocationException;
	
	public void addWorkflowInstanceNodeInputData(String experimentId, String topicId, String nodeId, String data) throws AiravataAPIInvocationException;
	
	public void addWorkflowInstanceNodeOutputData(WorkflowInstanceNodePortData data) throws AiravataAPIInvocationException;
	
	public void addWorkflowInstanceNodeOutputData(String experimentId, String topicId, String nodeId, String data) throws AiravataAPIInvocationException;
	
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(WorkflowInstanceNode node) throws AiravataAPIInvocationException;
	
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(String experimentId, String topicId, String nodeId) throws AiravataAPIInvocationException;
	
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(WorkflowInstanceNode node) throws AiravataAPIInvocationException;
	
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(String experimentId, String topicId, String nodeId) throws AiravataAPIInvocationException;
	
	//Instance Status
	public void setWorkflowInstanceStatus(String experimentId, String topicId, ExecutionStatus status) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceStatus(WorkflowInstanceStatus status) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceStatus getWorkflowInstanceStatus(String experimentId, String topicId) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceStatus getWorkflowInstanceStatus(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;
	
	//Instance User
	public void setWorkflowInstanceUser(String experimentId, String topicId, String user) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceUser(WorkflowInstanceUser user) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceUser getWorkflowInstanceUser(String experimentId, String topicId) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceUser getWorkflowInstanceUser(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;
	
	//Instance Metadata
	public void setWorkflowInstanceMetadata(String experimentId, String topicId, String metadata) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceMetadata(WorkflowInstanceMetadata instanceMetadata) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(String experimentId, String topicId) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;
	
	

	//General
	public List<String> getExperiments() throws AiravataAPIInvocationException;
	
	public List<WorkflowInstance> getWorkflowInstances() throws AiravataAPIInvocationException;

	public List<WorkflowInstance> getWorkflowInstances(String user) throws AiravataAPIInvocationException;

	public List<WorkflowInstance> getWorkflowInstances(String user, int pageSize, int pageNo) throws AiravataAPIInvocationException;

	public WorkflowInstanceData getWorkflowInstanceData(String experimentId, String topicId) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceData getWorkflowInstanceData(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;


	
	public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException;

    

}
