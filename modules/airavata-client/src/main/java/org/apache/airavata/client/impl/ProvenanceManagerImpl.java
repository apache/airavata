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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;

public class ProvenanceManagerImpl implements ProvenanceManager {
	private AiravataClient client;
	
	public ProvenanceManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	@Override
	public void setWorkflowInstanceNodeInput(WorkflowInstanceNode node, String data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateWorkflowNodeInput(node, data);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeInput(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceNodeInput(new WorkflowInstanceNode(new WorkflowExecution(experimentId, workflowInstanceId), nodeId), data);
	}

	@Override
	public void setWorkflowInstanceNodeOutput(WorkflowInstanceNode node, String data)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateWorkflowNodeOutput(node, data);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeOutput(String experimentId,
			String workflowInstanceId, String nodeId, String data)
			throws AiravataAPIInvocationException {
		setWorkflowInstanceNodeOutput(new WorkflowInstanceNode(new WorkflowExecution(experimentId, workflowInstanceId), nodeId), data);
		
	}

	@Override
	public String getWorkflowInstanceNodeInput(WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowInstanceNodeData(node.getWorkflowInstance().getWorkflowExecutionId(), node.getNodeId()).getInput();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getWorkflowInstanceNodeInput(String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeInput(new WorkflowInstanceNode(new WorkflowExecution(experimentId, workflowInstanceId), nodeId));
	}
	
	@Override
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeInput(String workflowName, String nodeId) throws AiravataAPIInvocationException{
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistryClient().searchWorkflowInstanceNodeInput(".*", workflowName, nodeId);
			return groupNodePortData(list);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private Map<WorkflowInstanceNode, String> groupNodePortData(List<WorkflowNodeIOData> list) {
		Map<WorkflowInstanceNode,String> portData=new HashMap<WorkflowInstanceNode, String>();
		for (WorkflowNodeIOData data : list) {
			portData.put(new WorkflowInstanceNode(new WorkflowExecution(data.getExperimentId(), data.getWorkflowInstanceId()), data.getNodeId()), data.getValue());
		}
		return portData;
	}

	@Override
	public String getWorkflowInstanceNodeOutput(WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowInstanceNodeData(node.getWorkflowInstance().getWorkflowExecutionId(), node.getNodeId()).getOutput();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getWorkflowInstanceNodeOutput(String experimentId, String workflowInstanceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeOutput(new WorkflowInstanceNode(new WorkflowExecution(experimentId, workflowInstanceId), nodeId));

	}

	@Override
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeOutput(String workflowName, String nodeId) throws AiravataAPIInvocationException{
		try {
			List<WorkflowNodeIOData> list = getClient().getRegistryClient().searchWorkflowInstanceNodeOutput(".*", workflowName, nodeId);
			return groupNodePortData(list);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public void setWorkflowInstanceStatus(String experimentId, String workflowInstanceId,
			State status) throws AiravataAPIInvocationException {
		setWorkflowInstanceStatus(new WorkflowExecutionStatus(new WorkflowExecution(experimentId, workflowInstanceId),status));
	}

	@Override
	public void setWorkflowInstanceStatus(WorkflowExecutionStatus status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateWorkflowInstanceStatus(status);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public WorkflowExecutionStatus getWorkflowInstanceStatus(
			String experimentId, String workflowInstanceId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowInstanceStatus(experimentId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowExecutionStatus getWorkflowInstanceStatus(
			WorkflowExecution workflowInstance)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceStatus(workflowInstance.getExperimentId(), workflowInstance.getWorkflowExecutionId());
	}

	@Override
	public void setExperimentUser(String experimentId, String user) throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateExperimentExecutionUser(experimentId, user);
		} catch (Exception e) {
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
			return new ExperimentUser(experimentId,getClient().getRegistryClient().getExperimentExecutionUser(experimentId));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setExperimentMetadata(String experimentId, String metadata)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateExperimentMetadata(experimentId, metadata);
		} catch (Exception e) {
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
			return new ExperimentMetadata(experimentId, getClient().getRegistryClient().getExperimentMetadata(experimentId));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

    @Override
    public boolean isExperimentNameExist(String experimentName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().isExperimentNameExist(experimentName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
	public List<String> getExperimentIdList(String owner) throws AiravataAPIInvocationException{
		try {
			return getClient().getRegistryClient().getExperimentIdByUser(owner);
		} catch (Exception e) {
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
			return  getClient().getRegistryClient().getExperimentByUser(user);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ExperimentData> getWorkflowExperimentData(String user,
			int pageSize, int pageNo) throws AiravataAPIInvocationException {
		try {
			return  getClient().getRegistryClient().getExperimentByUser(user, pageSize, pageNo);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowExecutionData getWorkflowInstanceData(String experimentId,
			String workflowInstanceId) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowInstanceData(workflowInstanceId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowExecutionData getWorkflowInstanceData(WorkflowExecution workflowInstance)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceData(workflowInstance.getExperimentId(), workflowInstance.getWorkflowExecutionId());
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
			getClient().getRegistryClient().updateExperimentName(experimentId, experimentName);
		} catch (Exception e) {
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
			return new ExperimentName(experimentId, getClient().getRegistryClient().getExperimentName(experimentId));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public ExperimentData getWorkflowExperimentData(String experimentId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getExperiment(experimentId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void setWorkflowInstanceNodeStatus(String experimentId,
			String workflowInstaceId, String nodeId, State status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateWorkflowNodeStatus(workflowInstaceId, nodeId, status);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

	@Override
	public void setWorkflowInstanceNodeStatus(NodeExecutionStatus status)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().updateWorkflowNodeStatus(status);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public NodeExecutionStatus getWorkflowInstanceNodeStatus(
			String experimentId, String workflowInstaceId, String nodeId)
			throws AiravataAPIInvocationException {
		return getWorkflowInstanceNodeStatus(new WorkflowInstanceNode(new WorkflowExecution(experimentId,workflowInstaceId),nodeId));
	}

	@Override
	public NodeExecutionStatus getWorkflowInstanceNodeStatus(
			WorkflowInstanceNode node) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowNodeStatus(node);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}	
	}

	@Override
	public void addExperiment(String projectName, String experimentId, String experimentName)
			throws AiravataAPIInvocationException {
		try {
			AiravataExperiment experiment = new AiravataExperiment();
			experiment.setExperimentId(experimentId);
			getClient().getRegistryClient().addExperiment(projectName, experiment);
			getClient().getRegistryClient().updateExperimentName(experimentId, experimentName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}	
	}

	@Override
	public void addWorkflowInstance(String experimentId,
			WorkflowExecution workflowInstance)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().addWorkflowInstance(experimentId, workflowInstance.getWorkflowExecutionId(),workflowInstance.getTemplateName());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
		
	}

    @Override
    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) throws AiravataAPIInvocationException {
        try{
            return getClient().getRegistryClient().getExperimentWorkflowInstances(experimentId);
        }catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().updateWorkflowNodeType(node, type);
        }catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
        }catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void saveWorkflowExecutionOutput(String experimentId, String outputNodeName, String output) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
	public ExperimentData getExperimentMetaInformation(String experimentId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getExperimentMetaInformation(experimentId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ExperimentData> getAllExperimentMetaInformation(String user)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getAllExperimentMetaInformation(user);
		} catch (Exception e) {
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

    public void updateWorkflowNodeGramData(WorkflowNodeGramData data) throws AiravataAPIInvocationException {
        try {
            client.getRegistryClient().updateWorkflowNodeGramData(data);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException(e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

	@Override
	public boolean isApplicationJobExists(String gfacJobId)
			throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().isApplicationJobExists(gfacJobId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public void addApplicationJob(ApplicationJob job) throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().addApplicationJob(job);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJob(ApplicationJob job)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJob(job);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobStatus(String jobId, ApplicationJobStatus status, Date statusUpdateTime)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJobStatus(jobId, status, statusUpdateTime);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobData(String jobId, String jobdata)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJobData(jobId, jobdata);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobSubmittedTime(String jobId, Date submitted)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJobSubmittedTime(jobId, submitted);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobStatusUpdateTime(String jobId, Date completed)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJobStatusUpdateTime(jobId, completed);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobMetadata(String jobId, String metadata)
			throws AiravataAPIInvocationException {
		try {
			client.getRegistryClient().updateApplicationJobMetadata(jobId, metadata);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public ApplicationJob getApplicationJob(String jobId)
			throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().getApplicationJob(jobId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationJob> getApplicationJobsForDescriptors(String serviceDescriptionId,
			String hostDescriptionId, String applicationDescriptionId)
			throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().getApplicationJobsForDescriptors(serviceDescriptionId, hostDescriptionId, applicationDescriptionId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationJob> getApplicationJobs(String experimentId,
			String workflowExecutionId, String nodeId)
			throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().getApplicationJobs(experimentId, workflowExecutionId, nodeId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void updateApplicationJobStatus(String jobId,
			ApplicationJobStatus status) throws AiravataAPIInvocationException {
		updateApplicationJobStatus(jobId, status, Calendar.getInstance().getTime());
	}

}
