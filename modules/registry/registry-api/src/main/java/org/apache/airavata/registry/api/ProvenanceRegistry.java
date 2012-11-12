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

package org.apache.airavata.registry.api;

import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;

public interface ProvenanceRegistry extends AiravataSubRegistry{

	/*------------------------------------------- Experiment data ---------------------------------------------*/
	
	/**
     * Returns true if the experiment exists 
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract boolean isExperimentExists(String experimentId) throws RegistryException;
	
	public abstract boolean isExperimentExists(String experimentId, boolean createIfNotPresent) throws RegistryException;
	
    /**
     * Save the username of the user who runs this experiment 
     * @param experimentId
     * @param user
     * @return
     * @throws RegistryException
     */
	public abstract void updateExperimentExecutionUser(String experimentId, String user) throws RegistryException;
    
    /**
     * Retrieve the user who is runing the experiment
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract String getExperimentExecutionUser(String experimentId) throws RegistryException;

    /**
     * check whether the experiment name exists
     * @param experimentName
     * @return
     * @throws RegistryException
     */
    public boolean isExperimentNameExist(String experimentName) throws  RegistryException;
    /**
     * Get the name of the workflow intance
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract String getExperimentName(String experimentId) throws RegistryException;
    
    /**
     * Save a name for this workflow execution
     * @param experimentId
     * @param experimentName
     * @return
     * @throws RegistryException
     */
	public abstract void updateExperimentName(String experimentId,String experimentName)throws RegistryException;
    
	/**
     * Return the metadata information saved for the experiment
     * @param experimentId
     * @return
     * @throws RegistryException
     */
    public abstract String getExperimentMetadata(String experimentId) throws RegistryException;
    
    /**
     * Save the metadata for the experiment
     * @param experimentId
     * @param metadata
     * @return
     * @throws RegistryException
     */
    public abstract void updateExperimentMetadata(String experimentId, String metadata) throws RegistryException;
    
    /**
     * Return the template name of the workflow that this intance was created from
     * @param workflowInstanceId
     * @return
     * @throws RegistryException
     */
    public abstract String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException;
    
    /**
     * Save the template name of the workflow that this intance was created from
     * @param workflowInstanceId
     * @param templateName
     * @throws RegistryException
     */
    public abstract void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName) throws RegistryException;
    
    public List<WorkflowInstance> getExperimentWorkflowInstances(String experimentId) throws RegistryException;
	
    /*-------------------------------------- Experiment Workflow instance node data ----------------------------------------*/

    public boolean isWorkflowInstanceExists(String instanceId) throws RegistryException;
    
    public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) throws RegistryException;
    
    /**
     * Save a status for this workflow execution with the current time at the moment
     * @param instanceId
     * @param status - contains the status
     * @return
     * @throws RegistryException
     */
	public abstract void updateWorkflowInstanceStatus(String instanceId,ExecutionStatus status)throws RegistryException;

    /**
     * Save a status for this workflow execution
     * @param status - contains the status
     * @return
     * @throws RegistryException
     */
	public abstract void updateWorkflowInstanceStatus(WorkflowInstanceStatus status)throws RegistryException;

	
    /**
     * Return the status of the execution
     * @param instanceId
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowInstanceStatus getWorkflowInstanceStatus(String instanceId)throws RegistryException;

    /**
	 * Save the input data of a node in the workflow instance of an experiment
	 * @param node
     * @param data
	 * @return true if successfully saved
	 * @throws RegistryException
	 */
	public abstract void updateWorkflowNodeInput(WorkflowInstanceNode node, String data) throws RegistryException;

    /**
     * Save the output data of a node in the workflow instance of an experiment
     * @param node
     * @return true if successfully saved
     * @throws RegistryException
     */
	public abstract void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data)throws RegistryException;
    
    /**
     * Return a list of data passed as input for service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;

	/**
     * Return a list of data returned as output from service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
	 * @return
	 * @throws RegistryException
	 */
	public abstract List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;
	
	public abstract List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(String workflowInstanceId, String nodeType)throws RegistryException;
	
	public abstract List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(String workflowInstanceId, String nodeType)throws RegistryException;

    /**
     * Saves the results of output nodes in a workflow
     * @deprecated 
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @param output
     * @return
     * @throws RegistryException
     */
	public abstract void saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException;
    
    /**
     * Saves the results of output nodes in a workflow
     * @deprecated
     * @param experimentId - also the workflow id
     * @param data
     * @return
     * @throws RegistryException
     */
	public abstract void saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException;

    /**
     * Get the output results of a output node of an experiment
     * @deprecated
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException;
    
    /**
     * Get the list of output node results of an experiment 
     * @deprecated
     * @param experimentId - also the workflow id
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException;

    /**
     * Get the names of the output nodes of a workflow instance run
     * @deprecated
     * @param exeperimentId - also the workflow id
     * @return
     * @throws RegistryException
     */
	public abstract String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException;

	/*---------------------------------------  Retrieving Experiment ------------------------------------------*/
    /**
     * Return workflow execution object fully populated with data currently avaialble for that experiment
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract ExperimentData getExperiment(String experimentId) throws RegistryException;
	
	public ExperimentData getExperimentMetaInformation(String experimentId)throws RegistryException;
	
	public List<ExperimentData> getAllExperimentMetaInformation(String user)throws RegistryException;
	
	/**
	 * Retrieve experiments which their names match the regular expression experimentNameRegex
	 * @param user
	 * @param experimentNameRegex
	 * @return
	 * @throws RegistryException
	 */
	public List<ExperimentData> searchExperiments(String user, String experimentNameRegex)throws RegistryException;
    
    /**
     * Return experiment ids of experiments launched by the given user
     * @param user - a regex user id
     * @return - experiment id list
     * @throws RegistryException
     */
	public abstract List<String> getExperimentIdByUser(String user) throws RegistryException;

	/**
	 * Return experiments launched by the given user
	 * @param user
	 * @return experiment object list each populated by current data of that experiment
	 * @throws RegistryException
	 */
    public abstract List<ExperimentData> getExperimentByUser(String user) throws RegistryException;
    
    /**
     * Return the pageNo set of experiments launched by the given user if grouped in to pages of size pageSize
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws RegistryException
     */
    public abstract List<ExperimentData> getExperimentByUser(String user, int pageSize, int pageNo) throws RegistryException;

    /**
     * This will update the workflowStatus for given experimentID,workflowInstanceID combination.
     * @param workflowStatusNode
     * @return
     */
    public abstract void updateWorkflowNodeStatus(WorkflowInstanceNodeStatus workflowStatusNode)throws RegistryException;

    public abstract void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, ExecutionStatus status)throws RegistryException;
    
    public abstract void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode, ExecutionStatus status)throws RegistryException;

    public WorkflowInstanceNodeStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode)throws RegistryException;
    
    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode)throws RegistryException;
    
    public Date getWorkflowStartTime(WorkflowInstance workflowInstance)throws RegistryException;
    
    /**
     * This will store the gram specific data in to repository, this can be called before submitting the workflow in to Grid
     * @param workflowNodeGramData
     * @return
     */
    public abstract void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData)throws RegistryException;
    
    public WorkflowInstanceData getWorkflowInstanceData(String workflowInstanceId)throws RegistryException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId)throws RegistryException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent)throws RegistryException;
    
    public WorkflowInstanceNodeData getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId)throws RegistryException;

    public void addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName) throws RegistryException;
    
    public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws RegistryException;
    
    public void addWorkflowInstanceNode(String workflowInstance, String nodeId) throws RegistryException;
    
}