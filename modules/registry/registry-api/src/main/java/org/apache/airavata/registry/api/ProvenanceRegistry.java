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
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.GFacJob.GFacJobStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;

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
    
    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) throws RegistryException;
    
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
	public abstract void updateWorkflowInstanceStatus(String instanceId,State status)throws RegistryException;

    /**
     * Save a status for this workflow execution
     * @param status - contains the status
     * @return
     * @throws RegistryException
     */
	public abstract void updateWorkflowInstanceStatus(WorkflowExecutionStatus status)throws RegistryException;

	
    /**
     * Return the status of the execution
     * @param instanceId
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowExecutionStatus getWorkflowInstanceStatus(String instanceId)throws RegistryException;

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
    public abstract void updateWorkflowNodeStatus(NodeExecutionStatus workflowStatusNode)throws RegistryException;

    public abstract void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, State status)throws RegistryException;
    
    public abstract void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode, State status)throws RegistryException;

    public NodeExecutionStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode)throws RegistryException;
    
    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode)throws RegistryException;
    
    public Date getWorkflowStartTime(WorkflowExecution workflowInstance)throws RegistryException;
    
    /**
     * @deprecated - Will be removed from 0.9 release onwards. Use {@see #addGFacJob #updateGFacJob(GFacJob) etc.} functions instead.
     * This will store the gram specific data in to repository, this can be called before submitting the workflow in to Grid
     * @param workflowNodeGramData
     * @return
     */
    public abstract void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData)throws RegistryException;
    
    public WorkflowExecutionData getWorkflowInstanceData(String workflowInstanceId)throws RegistryException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId)throws RegistryException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent)throws RegistryException;
    
    public NodeExecutionData getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId)throws RegistryException;

    public void addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName) throws RegistryException;
    
    public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws RegistryException;
    
    public void addWorkflowInstanceNode(String workflowInstance, String nodeId) throws RegistryException;
    
    
	/*---------------------------------------  Errors in experiment executions ------------------------------------------*/

    /**
     * Return errors defined at the experiment level 
     * @param experimentId
     * @return
     * @throws RegistryException
     */
    public List<ExperimentExecutionError> getExperimentExecutionErrors(String experimentId) throws RegistryException;
    
    /**
     * Return errors defined at the workflow level 
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws RegistryException
     */
    public List<WorkflowExecutionError> getWorkflowExecutionErrors(String experimentId, String workflowInstanceId) throws RegistryException;

    /**
     * Return errors defined at the node level 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws RegistryException
     */
    public List<NodeExecutionError> getNodeExecutionErrors(String experimentId, String workflowInstanceId, String nodeId) throws RegistryException;
    
    /**
     * Return errors defined for a GFac job 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param gfacJobId
     * @return
     * @throws RegistryException
     */
    public List<GFacJobExecutionError> getGFacJobErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId) throws RegistryException;

    /**
     * Return errors defined for a GFac job 
     * @param gfacJobId
     * @return
     * @throws RegistryException
     */
    public List<GFacJobExecutionError> getGFacJobErrors(String gfacJobId) throws RegistryException;

    /**
     * Return errors filtered by the parameters
     * @param experimentId 
     * @param workflowInstanceId
     * @param nodeId
     * @param gfacJobId
     * @param filterBy - what type of source types the results should contain
     * @return
     * @throws RegistryException
     */
    public List<ExecutionError> getExecutionErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId, ExecutionErrors.Source...filterBy) throws RegistryException;

    /**
     * Adds an experiment execution error 
     * @param error
     * @return
     * @throws RegistryException
     */
    public int addExperimentError(ExperimentExecutionError error) throws RegistryException;
    
    /**
     * Adds an workflow execution error 
     * @param error
     * @return
     * @throws RegistryException
     */
    public int addWorkflowExecutionError(WorkflowExecutionError error) throws RegistryException;
    
    /**
     * Adds an node execution error 
     * @param error
     * @return
     * @throws RegistryException
     */
    public int addNodeExecutionError(NodeExecutionError error) throws RegistryException;

    /**
     * Adds an GFac job execution error 
     * @param error
     * @return
     * @throws RegistryException
     */
    public int addGFacJobExecutionError(GFacJobExecutionError error) throws RegistryException;
    
    
	/*---------------------------------------  Managing Data for GFac Jobs ------------------------------------------*/

    /**
     * Returns <code>true</code> if a gfac job data is existing in the registry
     * @param gfacJobId
     * @return
     * @throws RegistryException
     */
    public boolean isGFacJobExists(String gfacJobId) throws RegistryException;
    
    /**
     * Adding data related to a new GFac job submission
     * @param job - the <code>jobId</code> cannot be <code>null</code>.
     * @throws RegistryException
     */
    public void addGFacJob(GFacJob job) throws RegistryException;
    
    /**
     * update data related to a existing GFac job record in the registry
     * @param job - the <code>jobId</code> cannot be <code>null</code> and should already exist in registry
     * @throws RegistryException
     */
    public void updateGFacJob(GFacJob job) throws RegistryException;
    
    /**
     * Update the status of the job
     * @param gfacJobId
     * @param status
     * @throws RegistryException
     */
    public void updateGFacJobStatus(String gfacJobId, GFacJobStatus status) throws RegistryException;
    
    /**
     * Update the job data. GFacProvider implementation should decide the job data. Typically it'll 
     * be a serialization of the submitted job query (eg: rsl for a GRAM job) 
     * @param gfacJobId
     * @param jobdata
     * @throws RegistryException
     */
    public void updateGFacJobData(String gfacJobId, String jobdata) throws RegistryException;
    
    /**
     * Update the time of job submission or job started executing
     * @param gfacJobId
     * @param submitted
     * @throws RegistryException
     */
    public void updateGFacJobSubmittedTime(String gfacJobId, Date submitted) throws RegistryException;
    
    /**
     * Update the time of job finished executing.
     * @param gfacJobId
     * @param completed
     * @throws RegistryException
     */
    public void updateGFacJobCompletedTime(String gfacJobId, Date completed) throws RegistryException;
    
    /**
     * Custom data field for users
     * @param gfacJobId
     * @param metadata
     * @throws RegistryException
     */
    public void updateGFacJobMetadata(String gfacJobId, String metadata) throws RegistryException;
    
    /**
     * Retrieve the GFac Job for the given job id
     * @param gfacJobId
     * @return
     * @throws RegistryException
     */
    public GFacJob getGFacJob(String gfacJobId) throws RegistryException;
    
    /**
     * Retrieve a list of GFac jobs executed for the given descriptors
     * @param serviceDescriptionId - should be <code>null</code> if user does not care what service description the job corresponds to
     * @param hostDescriptionId - should be <code>null</code> if user does not care what host description the job corresponds to
     * @param applicationDescriptionId - should be <code>null</code> if user does not care what application description the job corresponds to
     * @return
     * @throws RegistryException
     */
    public List<GFacJob> getGFacJobsForDescriptors(String serviceDescriptionId, String hostDescriptionId, String applicationDescriptionId) throws RegistryException;
    
    /**
     * Retrieve a list of GFac jobs executed for the given experiment credentials
     * @param experimentId - should be <code>null</code> if user does not care what experiment the job corresponds to
     * @param workflowExecutionId -  - should be <code>null</code> if user does not care what workflow execution the job corresponds to
     * @param nodeId  - should be <code>null</code> if user does not care what node id the job corresponds to
     * @return
     * @throws RegistryException
     */
    public List<GFacJob> getGFacJobs(String experimentId, String workflowExecutionId, String nodeId) throws RegistryException;
}