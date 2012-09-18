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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceMetadata;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceName;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodePortData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceUser;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;

public class ProvenanceManagerImpl implements ProvenanceManager {
	private AiravataClient client;
	
	public ProvenanceManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	@Override
	public void addWorkflowInstanceNodeInputData(
			WorkflowInstanceNodePortData data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeInput(data);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void addWorkflowInstanceNodeInputData(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		addWorkflowInstanceNodeInputData(new WorkflowInstanceNodePortData(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId), data));
	}

	@Override
	public void addWorkflowInstanceNodeOutputData(
			WorkflowInstanceNodePortData data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeOutput(data);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void addWorkflowInstanceNodeOutputData(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		addWorkflowInstanceNodeOutputData(new WorkflowInstanceNodePortData(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId), data));
		
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistry().searchWorkflowInstanceNodeInput(node.getWorkflowInstance().getExperimentId(), ".*", node.getNodeId());
			return generateWorkflowInstanceNodePortData(list);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private List<WorkflowInstanceNodePortData> generateWorkflowInstanceNodePortData(
			List<WorkflowNodeIOData> list) {
		List<WorkflowInstanceNodePortData> portData=new ArrayList<WorkflowInstanceNodePortData>();
		for (WorkflowNodeIOData data : list) {
			portData.add(new WorkflowInstanceNodePortData(data));
		}
		return portData;
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(
			String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeInputData(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId));
	}
	
	private List<WorkflowInstanceNodePortData> getWorkflowInstanceNodePortDataListForWorkflowInstanceNode(Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> portData, WorkflowInstanceNode instanceNode, boolean createIfNotPresent){
		for (WorkflowInstanceNode node : portData.keySet()) {
			if (node.getWorkflowInstance().getExperimentId().equals(instanceNode.getWorkflowInstance().getExperimentId()) && node.getWorkflowInstance().getWorkflowInstanceId().equals(instanceNode.getWorkflowInstance().getWorkflowInstanceId()) &&
					node.getNodeId().equals(instanceNode.getNodeId())){
				return portData.get(node);
			}
		}
		if (createIfNotPresent){
			portData.put(instanceNode, new ArrayList<WorkflowInstanceNodePortData>());
			return portData.get(instanceNode);
		}
		return null;
	}
	
	@Override
	public Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> getWorkflowInstanceNodeInputData(String workflowName, String nodeId) throws AiravataAPIInvocationException{
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistry().searchWorkflowInstanceNodeInput(".*", workflowName, nodeId);
			return groupNodePortData(list);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private Map<WorkflowInstanceNode, List<WorkflowInstanceNodePortData>> groupNodePortData(
			List<WorkflowNodeIOData> list) {
		Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> portData=new HashMap<WorkflowInstanceNode, List<WorkflowInstanceNodePortData>>();
		for (WorkflowNodeIOData data : list) {
			WorkflowInstanceNodePortData workflowInstanceNodePortData = new WorkflowInstanceNodePortData(data);
			List<WorkflowInstanceNodePortData> portDataList = getWorkflowInstanceNodePortDataListForWorkflowInstanceNode(portData, workflowInstanceNodePortData.getWorkflowInstanceNode(), true);
			portDataList.add(workflowInstanceNodePortData);
		}
		return portData;
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistry().searchWorkflowInstanceNodeOutput(node.getWorkflowInstance().getExperimentId(), ".*", node.getNodeId());
			return generateWorkflowInstanceNodePortData(list);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(
			String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeOutputData(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId));

	}

	@Override
	public Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> getWorkflowInstanceNodeOutputData(String workflowName, String nodeId) throws AiravataAPIInvocationException{
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistry().searchWorkflowInstanceNodeOutput(".*", workflowName, nodeId);
			return groupNodePortData(list);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public void setWorkflowInstanceStatus(String experimentId, String workflowInstanceId,
			ExecutionStatus status) throws AiravataAPIInvocationException {
		setWorkflowInstanceStatus(new WorkflowInstanceStatus(new WorkflowInstance(experimentId, workflowInstanceId),status));
	}

	@Override
	public void setWorkflowInstanceStatus(WorkflowInstanceStatus status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowInstanceStatus(status.getWorkflowInstance().getExperimentId(), status);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(
			String experimentId, String workflowInstanceId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getWorkflowInstanceStatus(experimentId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceStatus(workflowInstance.getExperimentId(), workflowInstance.getWorkflowInstanceId());
	}

	@Override
	public void setWorkflowInstanceUser(String experimentId, String workflowInstanceId,
			String user) throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentExecutionUser(experimentId, user);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceUser(WorkflowInstanceUser user)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceUser(user.getWorkflowInstance().getExperimentId(), user.getWorkflowInstance().getWorkflowInstanceId(), user.getUser());
	}

	@Override
	public WorkflowInstanceUser getWorkflowInstanceUser(String experimentId,
			String workflowInstanceId) throws AiravataAPIInvocationException {
		return getWorkflowInstanceUser(new WorkflowInstance(experimentId, workflowInstanceId));
	}

	@Override
	public WorkflowInstanceUser getWorkflowInstanceUser(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		try {
			return new WorkflowInstanceUser(workflowInstance,getClient().getRegistry().getExperimentExecutionUser(workflowInstance.getExperimentId()));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceMetadata(String experimentId,
			String workflowInstanceId, String metadata)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentMetadata(experimentId, metadata);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public void setWorkflowInstanceMetadata(
			WorkflowInstanceMetadata instanceMetadata)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceMetadata(instanceMetadata.getWorkflowInstance().getExperimentId(), instanceMetadata.getWorkflowInstance().getWorkflowInstanceId(), instanceMetadata.getMetadata());
	}

	@Override
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(
			String experimentId, String workflowInstanceId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceMetadata(new WorkflowInstance(experimentId, workflowInstanceId));
	}

	@Override
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		try {
			return new WorkflowInstanceMetadata(workflowInstance, getClient().getRegistry().getExperimentMetadata(workflowInstance.getExperimentId()));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<String> getExperiments(String owner) throws AiravataAPIInvocationException{
		try {
			return getClient().getRegistry().getExperimentIdByUser(owner);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public List<String> getExperiments() throws AiravataAPIInvocationException {
		return getExperiments(getClient().getCurrentUser());
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances()
			throws AiravataAPIInvocationException {
		return getWorkflowInstances(getClient().getCurrentUser());
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(String user)
			throws AiravataAPIInvocationException {
		List<WorkflowInstance> list=new ArrayList<WorkflowInstance>();
		List<String> experiments = getExperiments(user);
		for (String id : experiments) {
			list.add(new WorkflowInstance(id, id));
		}
		return list;
	}

	@Override
	public List<WorkflowInstanceData> getWorkflowInstances(String user,
			int pageSize, int pageNo) throws AiravataAPIInvocationException {
		try {
			List<WorkflowExecution> experimentIds = getClient().getRegistry().getExperimentByUser(user, pageSize, pageNo);
			List<WorkflowInstanceData> list=new ArrayList<WorkflowInstanceData>();
			for (WorkflowExecution execution : experimentIds) {
				WorkflowInstanceData workflowInstanceData = createWorkflowInstanceData(execution);
				list.add(workflowInstanceData);
			}
			return list;
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private WorkflowInstanceData createWorkflowInstanceData(
			WorkflowExecution execution) {
		WorkflowInstance workflowInstance = new WorkflowInstance(execution.getExperimentId(),execution.getTopic());
		WorkflowInstanceData workflowInstanceData = new WorkflowInstanceData(workflowInstance, new WorkflowInstanceName(workflowInstance, execution.getWorkflowInstanceName()),new WorkflowInstanceUser(workflowInstance,execution.getUser()), new WorkflowInstanceStatus(workflowInstance,execution.getExecutionStatus().getExecutionStatus(),execution.getExecutionStatus().getStatusUpdateTime()), new WorkflowInstanceMetadata(workflowInstance,execution.getMetadata()), null);
		Map<WorkflowInstanceNode, List<WorkflowInstanceNodePortData>> groupNodePortInputData = groupNodePortData(execution.getServiceInput());
		Map<WorkflowInstanceNode, List<WorkflowInstanceNodePortData>> groupNodePortOutputData = groupNodePortData(execution.getServiceOutput());
		for (WorkflowInstanceNode instanceNode : groupNodePortInputData.keySet()) {
			workflowInstanceData.addNodeData(new WorkflowInstanceNodeData(instanceNode, groupNodePortInputData.get(instanceNode), null));
		}
		for (WorkflowInstanceNode instanceNode : groupNodePortOutputData.keySet()) {
			WorkflowInstanceNodeData nodeData = workflowInstanceData.getNodeData(instanceNode.getNodeId());
			if (nodeData==null){
				workflowInstanceData.addNodeData(new WorkflowInstanceNodeData(instanceNode, null,groupNodePortOutputData.get(instanceNode)));
			}else{
				nodeData.setOutputData(groupNodePortOutputData.get(instanceNode));
			}
		}
		return workflowInstanceData;
	}

	@Override
	public WorkflowInstanceData getWorkflowInstanceData(String experimentId,
			String workflowInstanceId) throws AiravataAPIInvocationException {
		try {
			return createWorkflowInstanceData(getClient().getRegistry().getExperiment(experimentId));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowInstanceData getWorkflowInstanceData(WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceData(workflowInstance.getExperimentId(), workflowInstance.getWorkflowInstanceId());
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new Exception("Not implemented"));
	}

	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

	@Override
	public void setWorkflowInstanceName(String experimentId,
			String workflowInstanceId, String instanceName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentName(experimentId, instanceName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceName(WorkflowInstanceName instanceName)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceName(instanceName.getWorkflowInstance().getExperimentId(),instanceName.getWorkflowInstance().getWorkflowInstanceId(),instanceName.getInstanceName());
	}

	@Override
	public WorkflowInstanceName getWorkflowInstanceName(String experimentId,
			String workflowInstanceId) throws AiravataAPIInvocationException {
		return getWorkflowInstanceName(new WorkflowInstance(experimentId, workflowInstanceId));
	}

	@Override
	public WorkflowInstanceName getWorkflowInstanceName(
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		try {
			return new WorkflowInstanceName(workflowInstance, getClient().getRegistry().getExperimentName(workflowInstance.getExperimentId()));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	
}
