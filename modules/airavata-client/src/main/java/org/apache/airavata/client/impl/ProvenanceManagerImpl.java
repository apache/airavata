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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.ExperimentMetadata;
import org.apache.airavata.registry.api.workflow.ExperimentName;
import org.apache.airavata.registry.api.workflow.ExperimentUser;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;

public class ProvenanceManagerImpl implements ProvenanceManager {
	private AiravataClient client;
	
	public ProvenanceManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	@Override
	public void setWorkflowInstanceNodeInput(WorkflowInstanceNode node, String data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeInput(node, data);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeInput(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceNodeInput(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId), data);
	}

	@Override
	public void setWorkflowInstanceNodeOutput(WorkflowInstanceNode node, String data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeOutput(node, data);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeOutput(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceNodeOutput(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId), data);
		
	}

	@Override
	public String getWorkflowInstanceNodeInput(WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getWorkflowInstanceNodeData(node.getWorkflowInstance().getWorkflowInstanceId(), node.getNodeId()).getInput();
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getWorkflowInstanceNodeInput(String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeInput(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId));
	}
	
	@Override
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeInput(String workflowName, String nodeId) throws AiravataAPIInvocationException{
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistry().searchWorkflowInstanceNodeInput(".*", workflowName, nodeId);
			return groupNodePortData(list);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private Map<WorkflowInstanceNode, String> groupNodePortData(List<WorkflowNodeIOData> list) {
		Map<WorkflowInstanceNode,String> portData=new HashMap<WorkflowInstanceNode, String>();
		for (WorkflowNodeIOData data : list) {
			portData.put(new WorkflowInstanceNode(new WorkflowInstance(data.getExperimentId(), data.getWorkflowInstanceId()), data.getNodeId()), data.getValue());
		}
		return portData;
	}

	@Override
	public String getWorkflowInstanceNodeOutput(WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getWorkflowInstanceNodeData(node.getWorkflowInstance().getWorkflowInstanceId(), node.getNodeId()).getOutput();
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getWorkflowInstanceNodeOutput(String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeOutput(new WorkflowInstanceNode(new WorkflowInstance(experimentId, workflowInstanceId), nodeId));

	}

	@Override
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeOutput(String workflowName, String nodeId) throws AiravataAPIInvocationException{
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
			getClient().getRegistry().updateWorkflowInstanceStatus(status);
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
	public void setExperimentUser(String experimentId, String user) throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentExecutionUser(experimentId, user);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setExperimentUser(ExperimentUser user)
			throws AiravataAPIInvocationException {
		setExperimentUser(user.getExperimentId(), user.getUser());
	}

	@Override
	public ExperimentUser getExperimentUser(String experimentId)throws AiravataAPIInvocationException {
		try {
			return new ExperimentUser(experimentId,getClient().getRegistry().getExperimentExecutionUser(experimentId));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setExperimentMetadata(String experimentId, String metadata)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentMetadata(experimentId, metadata);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public void setExperimentMetadata(ExperimentMetadata instanceMetadata)
			throws AiravataAPIInvocationException {
		setExperimentMetadata(instanceMetadata.getExperimentId(), instanceMetadata.getMetadata());
	}

	@Override
	public ExperimentMetadata getExperimentMetadata(String experimentId)throws AiravataAPIInvocationException {
		try {
			return new ExperimentMetadata(experimentId, getClient().getRegistry().getExperimentMetadata(experimentId));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

    @Override
    public boolean isExperimentNameExist(String experimentName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistry().isExperimentNameExist(experimentName);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
	public List<String> getExperimentIdList(String owner) throws AiravataAPIInvocationException{
		try {
			return getClient().getRegistry().getExperimentIdByUser(owner);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public List<String> getExperimentIdList() throws AiravataAPIInvocationException {
		return getExperimentIdList(getClient().getCurrentUser());
	}

	@Override
	public List<ExperimentData> getWorkflowExperimentDataList()
			throws AiravataAPIInvocationException {
		return getWorkflowExperimentDataList(getClient().getCurrentUser());
	}

	@Override
	public List<ExperimentData> getWorkflowExperimentDataList(String user)
			throws AiravataAPIInvocationException {
		try {
			return  getClient().getRegistry().getExperimentByUser(user);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ExperimentData> getWorkflowExperimentData(String user,
			int pageSize, int pageNo) throws AiravataAPIInvocationException {
		try {
			return  getClient().getRegistry().getExperimentByUser(user, pageSize, pageNo);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowInstanceData getWorkflowInstanceData(String experimentId,
			String workflowInstanceId) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getWorkflowInstanceData(workflowInstanceId);
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
	public void setExperimentName(String experimentId, String experimentName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateExperimentName(experimentId, experimentName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setExperimentName(ExperimentName experimentName)
			throws AiravataAPIInvocationException {
		setExperimentName(experimentName.getExperimentId(),experimentName.getInstanceName());
	}

	@Override
	public ExperimentName getExperimentName(String experimentId)
			throws AiravataAPIInvocationException {
		try {
			return new ExperimentName(experimentId, getClient().getRegistry().getExperimentName(experimentId));
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public ExperimentData getWorkflowExperimentData(String experimentId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getExperiment(experimentId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeStatus(String experimentId,
			String workflowInstaceId, String nodeId, ExecutionStatus status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeStatus(workflowInstaceId, nodeId, status);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public void setWorkflowInstanceNodeStatus(WorkflowInstanceNodeStatus status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().updateWorkflowNodeStatus(status);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowInstanceNodeStatus getWorkflowInstanceNodeStatus(
			String experimentId, String workflowInstaceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeStatus(new WorkflowInstanceNode(new WorkflowInstance(experimentId,workflowInstaceId),nodeId));
	}

	@Override
	public WorkflowInstanceNodeStatus getWorkflowInstanceNodeStatus(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getWorkflowNodeStatus(node);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}	
	}

	@Override
	public void addExperiment(String projectName, String experimentId, String experimentName)
			throws AiravataAPIInvocationException {
		try {
			AiravataExperiment experiment = new AiravataExperiment();
			experiment.setExperimentId(experimentId);
			getClient().getRegistry().addExperiment(projectName, experiment);
			getClient().getRegistry().updateExperimentName(experimentId, experimentName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}	
	}

	@Override
	public void addWorkflowInstance(String experimentId,
			WorkflowInstance workflowInstance)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().addWorkflowInstance(experimentId, workflowInstance.getWorkflowInstanceId(),workflowInstance.getTemplateName());
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public ExperimentData getExperimentMetaInformation(String experimentId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getExperimentMetaInformation(experimentId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ExperimentData> getAllExperimentMetaInformation(String user)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getAllExperimentMetaInformation(user);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ExperimentData> getExperimentDataList()
			throws AiravataAPIInvocationException {
		return getWorkflowExperimentDataList();
	}

	@Override
	public List<ExperimentData> getExperimentDataList(String user)
			throws AiravataAPIInvocationException {
		return getWorkflowExperimentDataList(user);
	}

	@Override
	public List<ExperimentData> getExperimentData(String user, int pageSize,
			int pageNo) throws AiravataAPIInvocationException {
		return getWorkflowExperimentData(user, pageSize, pageNo);
	}

	@Override
	public ExperimentData getExperimentData(String experimentId)
			throws AiravataAPIInvocationException {
		return getWorkflowExperimentData(experimentId);
	}

	
}
