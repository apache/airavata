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

import java.util.List;

import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceMetadata;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodePortData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceUser;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;

public class ProvenanceManagerImpl implements ProvenanceManager {

	@Override
	public void addWorkflowInstanceNodeInputData(
			WorkflowInstanceNodePortData data)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWorkflowInstanceNodeInputData(String experimentId,
			String topicId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWorkflowInstanceNodeOutputData(
			WorkflowInstanceNodePortData data)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWorkflowInstanceNodeOutputData(String experimentId,
			String topicId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(
			String experimentId, String topicId, String nodeId)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(
			String experimentId, String topicId, String nodeId)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkflowInstanceStatus(String experimentId, String topicId,
			ExecutionStatus status) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWorkflowInstanceStatus(WorkflowInstanceStatus status)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(
			String experimentId, String topicId)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkflowInstanceUser(String experimentId, String topicId,
			String user) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWorkflowInstanceUser(WorkflowInstanceUser user)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WorkflowInstanceUser getWorkflowInstanceUser(String experimentId,
			String topicId) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceUser getWorkflowInstanceUser(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkflowInstanceMetadata(String experimentId,
			String topicId, String metadata)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWorkflowInstanceMetadata(
			WorkflowInstanceMetadata instanceMetadata)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(
			String experimentId, String topicId)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getExperiments() throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances()
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(String user)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(String user,
			int pageSize, int pageNo) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceData getWorkflowInstanceData(String experimentId,
			String topicId) throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceData getWorkflowInstanceData(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
