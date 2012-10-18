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
import java.util.Map;

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

/**
 * This interface provide and API to manage all the provenance related methods, get Workflow inputs outputs
 */
public interface ProvenanceManager {

    /**
     * Add input port data for a node in a running instance of a Workflow 
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceNodeInput(WorkflowInstanceNode node, String data) throws AiravataAPIInvocationException;

    /**
     * Add input port data for a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceNodeInput(String experimentId, String workflowInstanceId, String nodeId, String data) throws AiravataAPIInvocationException;

    /**
     * Add output port data for a node in a running instance of a Workflow
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceNodeOutput(WorkflowInstanceNode node, String data) throws AiravataAPIInvocationException;

    /**
     * Add output port data for a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceNodeOutput(String experimentId, String workflowInstanceId, String nodeId, String data) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in a running instance of a Workflow
     * @param node
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String getWorkflowInstanceNodeInput(WorkflowInstanceNode node) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String getWorkflowInstanceNodeInput(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in all the running instance of a particular Workflow template
     * @param workflowTemplateId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeInput(String workflowTemplateId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in a running instance of a Workflow
     * @param node
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String getWorkflowInstanceNodeOutput(WorkflowInstanceNode node) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String getWorkflowInstanceNodeOutput(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in all the running instance of a particular Workflow template
     * @param workflowName
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Map<WorkflowInstanceNode,String> getWorkflowInstanceNodeOutput(String workflowName, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Update the status of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @param status
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceStatus(String experimentId, String workflowInstanceId, ExecutionStatus status) throws AiravataAPIInvocationException;

    /**
     * Update the status of the Workflow instance
     * @param status
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceStatus(WorkflowInstanceStatus status) throws AiravataAPIInvocationException;

    /**
     * Retrieve the status of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceStatus getWorkflowInstanceStatus(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the status of the Workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceStatus getWorkflowInstanceStatus(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;

    /**
     * Update the User of the Workflow instance
     * @param experimentId
     * @param user
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentUser(String experimentId, String user) throws AiravataAPIInvocationException;

    /**
     * Update the User of the Workflow instance
     * @param user
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentUser(ExperimentUser user) throws AiravataAPIInvocationException;

    /**
     * Retrieve the User of the Workflow instance
     * @param experimentId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ExperimentUser getExperimentUser(String experimentId) throws AiravataAPIInvocationException;

    /**
     * Update the metadata of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @param metadata
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentMetadata(String experimentId, String metadata) throws AiravataAPIInvocationException;

    /**
     * Update the metadata of the Workflow instance
     * @param experimentMetadata
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentMetadata(ExperimentMetadata experimentMetadata) throws AiravataAPIInvocationException;
	
    /**
     * Retrieve the metadata of the Workflow instance
     * @param experimentId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ExperimentMetadata getExperimentMetadata(String experimentId) throws AiravataAPIInvocationException;

    /**
     * check whether the experiment exists
     * @param experimentName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean isExperimentNameExist(String experimentName) throws AiravataAPIInvocationException;

	   /**
     * Update the instance name of the Workflow
     * @param experimentId
     * @param instanceName
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentName(String experimentId, String instanceName) throws AiravataAPIInvocationException;

    /**
     * Update the instance name of the Workflow
     * @param experimentName
     * @throws AiravataAPIInvocationException
     */
	public void setExperimentName(ExperimentName experimentName) throws AiravataAPIInvocationException;

    /**
     * Retrieve the metadata of the Workflow instance
     * @param experimentId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ExperimentName getExperimentName(String experimentId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the id's of all the experiments run by the given owner  
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<String> getExperimentIdList(String owner) throws AiravataAPIInvocationException;

	public ExperimentData getExperimentMetaInformation(String experimentId)throws AiravataAPIInvocationException;
	
	public List<ExperimentData> getAllExperimentMetaInformation(String user)throws AiravataAPIInvocationException;
	
    /**
     * Retrieve the id's of all the experiments run by the current user
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<String> getExperimentIdList() throws AiravataAPIInvocationException;

    /**
     * Retrieve all the experiments run by the current user
     * @deprecated
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<ExperimentData> getWorkflowExperimentDataList() throws AiravataAPIInvocationException;

	public List<ExperimentData> getExperimentDataList() throws AiravataAPIInvocationException;
	
    /**
     * Retrieve all the experiments run by the given owner
     * @deprecated
     * @param user
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<ExperimentData> getWorkflowExperimentDataList(String user) throws AiravataAPIInvocationException;
	
	public List<ExperimentData> getExperimentDataList(String user) throws AiravataAPIInvocationException;

    /**
     * Retrieve all the experiment data run by the given owner with paging
     * @deprecated
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<ExperimentData> getWorkflowExperimentData(String user, int pageSize, int pageNo) throws AiravataAPIInvocationException;
	
	public List<ExperimentData> getExperimentData(String user, int pageSize, int pageNo) throws AiravataAPIInvocationException;

	/**
     * Retrieve all the experiment data run by the given owner with paging
     * @deprecated
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ExperimentData getWorkflowExperimentData(String experimentId) throws AiravataAPIInvocationException;
	
	public ExperimentData getExperimentData(String experimentId) throws AiravataAPIInvocationException;
	
    /**
     * Retrieve experiment data for a given workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceData getWorkflowInstanceData(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve experiment data for a given workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceData getWorkflowInstanceData(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;

    /**
     * Retrieve output node names of a experiment
     * @param experimentId
     * @deprecated
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String[] getWorkflowExecutionOutputNames(String experimentId) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceNodeStatus(String experimentId, String workflowInstaceId, String nodeId, ExecutionStatus status) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceNodeStatus(WorkflowInstanceNodeStatus status) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceNodeStatus getWorkflowInstanceNodeStatus(String experimentId, String workflowInstaceId, String nodeId) throws AiravataAPIInvocationException;
	
	public WorkflowInstanceNodeStatus getWorkflowInstanceNodeStatus(WorkflowInstanceNode node) throws AiravataAPIInvocationException;
	
	public void addExperiment(String projectName, String experimentId, String experimentName) throws AiravataAPIInvocationException;
	
	public void addWorkflowInstance(String experimentId, WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;
	
	//TODO setup node type for the node & gram data for the node
}
