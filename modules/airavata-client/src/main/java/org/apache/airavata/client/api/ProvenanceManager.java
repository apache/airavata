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

import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceMetadata;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceName;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodePortData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceUser;

/**
 * This interface provide and API to manage all the provenance related methods, get Workflow inputs outputs
 */
public interface ProvenanceManager {

    /**
     * Add input port data for a node in a running instance of a Workflow 
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void addWorkflowInstanceNodeInputData(WorkflowInstanceNodePortData data) throws AiravataAPIInvocationException;

    /**
     * Add input port data for a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void addWorkflowInstanceNodeInputData(String experimentId, String workflowInstanceId, String nodeId, String data) throws AiravataAPIInvocationException;

    /**
     * Add output port data for a node in a running instance of a Workflow
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void addWorkflowInstanceNodeOutputData(WorkflowInstanceNodePortData data) throws AiravataAPIInvocationException;

    /**
     * Add output port data for a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param data
     * @throws AiravataAPIInvocationException
     */
	public void addWorkflowInstanceNodeOutputData(String experimentId, String workflowInstanceId, String nodeId, String data) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in a running instance of a Workflow
     * @param node
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(WorkflowInstanceNode node) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeInputData(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of input ports of a node in all the running instance of a particular Workflow template
     * @param workflowTemplateId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> getWorkflowInstanceNodeInputData(String workflowTemplateId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in a running instance of a Workflow
     * @param node
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(WorkflowInstanceNode node) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in a running instance of a Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstanceNodePortData> getWorkflowInstanceNodeOutputData(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException;

    /**
     * Get data of output ports of a node in all the running instance of a particular Workflow template
     * @param workflowName
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Map<WorkflowInstanceNode,List<WorkflowInstanceNodePortData>> getWorkflowInstanceNodeOutputData(String workflowName, String nodeId) throws AiravataAPIInvocationException;

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
     * @param workflowInstanceId
     * @param user
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceUser(String experimentId, String workflowInstanceId, String user) throws AiravataAPIInvocationException;

    /**
     * Update the User of the Workflow instance
     * @param user
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceUser(WorkflowInstanceUser user) throws AiravataAPIInvocationException;

    /**
     * Retrieve the User of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceUser getWorkflowInstanceUser(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the User of the Workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceUser getWorkflowInstanceUser(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;

    /**
     * Update the metadata of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @param metadata
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceMetadata(String experimentId, String workflowInstanceId, String metadata) throws AiravataAPIInvocationException;

    /**
     * Update the metadata of the Workflow instance
     * @param instanceMetadata
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceMetadata(WorkflowInstanceMetadata instanceMetadata) throws AiravataAPIInvocationException;
	
    /**
     * Retrieve the metadata of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the metadata of the Workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceMetadata getWorkflowInstanceMetadata(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;

	   /**
     * Update the instance name of the Workflow
     * @param experimentId
     * @param workflowInstanceId
     * @param instanceName
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceName(String experimentId, String workflowInstanceId, String instanceName) throws AiravataAPIInvocationException;

    /**
     * Update the instance name of the Workflow
     * @param instanceName
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceName(WorkflowInstanceName instanceName) throws AiravataAPIInvocationException;

    /**
     * Retrieve the metadata of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceName getWorkflowInstanceName(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the metadata of the Workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowInstanceName getWorkflowInstanceName(WorkflowInstance workflowInstance) throws AiravataAPIInvocationException;

    /**
     * Retrieve the id's of all the experiments run by the given owner  
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<String> getExperiments(String owner) throws AiravataAPIInvocationException;

    /**
     * Retrieve the id's of all the experiments run by the current user
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<String> getExperiments() throws AiravataAPIInvocationException;

    /**
     * Retrieve all the experiments run by the current user
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstance> getWorkflowInstances() throws AiravataAPIInvocationException;

    /**
     * Retrieve all the experiments run by the given owner
     * @param user
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstance> getWorkflowInstances(String user) throws AiravataAPIInvocationException;

    /**
     * Retrieve all the experiment data run by the given owner with paging
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws AiravataAPIInvocationException
     */
	public List<WorkflowInstanceData> getWorkflowInstances(String user, int pageSize, int pageNo) throws AiravataAPIInvocationException;

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
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String[] getWorkflowExecutionOutputNames(String experimentId) throws AiravataAPIInvocationException;
}
