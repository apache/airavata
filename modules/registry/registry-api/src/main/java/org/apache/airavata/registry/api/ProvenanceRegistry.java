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
import java.util.HashMap;
import java.util.List;

import org.apache.airavata.registry.api.exception.RegException;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;

public interface ProvenanceRegistry extends AiravataSubRegistry{

	/*------------------------------------------- Experiment data ---------------------------------------------*/
	
	/**
     * Returns true if the experiment exists 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public boolean isExperimentExists(String experimentId) throws RegException;
	
	public boolean isExperimentExists(String experimentId, boolean createIfNotPresent) throws RegException;
	
    /**
     * Save the username of the user who runs this experiment 
     * @param experimentId
     * @param user
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void updateExperimentExecutionUser(String experimentId, String user) throws RegException;
    
    /**
     * Retrieve the user who is runing the experiment
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public String getExperimentExecutionUser(String experimentId) throws RegException;

    /**
     * check whether the experiment name exists
     * @param experimentName
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public boolean isExperimentNameExist(String experimentName) throws RegException;
    /**
     * Get the name of the workflow intance
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public String getExperimentName(String experimentId) throws RegException;
    
    /**
     * Save a name for this workflow execution
     * @param experimentId
     * @param experimentName
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void updateExperimentName(String experimentId,String experimentName)throws RegException;
    
	/**
     * Return the metadata information saved for the experiment
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public String getExperimentMetadata(String experimentId) throws RegException;
    
    /**
     * Save the metadata for the experiment
     * @param experimentId
     * @param metadata
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateExperimentMetadata(String experimentId, String metadata) throws RegException;
    
    /**
     * Return the template name of the workflow that this intance was created from
     * @param workflowInstanceId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegException;
    
    /**
     * Save the template name of the workflow that this intance was created from
     * @param workflowInstanceId
     * @param templateName
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName) throws RegException;
    
    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) throws RegException;
    
    /*-------------------------------------- Experiment Workflow instance node data ----------------------------------------*/

    public boolean isWorkflowInstanceExists(String instanceId) throws RegException;
    
    public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) throws RegException;
    
    /**
     * Save a status for this workflow execution with the current time at the moment
     * @param instanceId
     * @param status - contains the status
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void updateWorkflowInstanceStatus(String instanceId,State status)throws RegException;

    /**
     * Save a status for this workflow execution
     * @param status - contains the status
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void updateWorkflowInstanceStatus(WorkflowExecutionStatus status)throws RegException;

	
    /**
     * Return the status of the execution
     * @param instanceId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public WorkflowExecutionStatus getWorkflowInstanceStatus(String instanceId)throws RegException;

    /**
	 * Save the input data of a node in the workflow instance of an experiment
	 * @param node
     * @param data
	 * @return true if successfully saved
	 * @throws org.apache.airavata.registry.api.exception.RegException
	 */
	public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data) throws RegException;

    /**
     * Save the output data of a node in the workflow instance of an experiment
     * @param node
     * @return true if successfully saved
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data)throws RegException;
    
    /**
     * Return a list of data passed as input for service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegException;

	/**
     * Return a list of data returned as output from service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
	 * @return
	 * @throws org.apache.airavata.registry.api.exception.RegException
	 */
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegException;
	
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(String workflowInstanceId, String nodeType)throws RegException;
	
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(String workflowInstanceId, String nodeType)throws RegException;

    /**
     * Saves the results of output nodes in a workflow
     * @deprecated 
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @param output
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegException;
    
    /**
     * Saves the results of output nodes in a workflow
     * @deprecated
     * @param experimentId - also the workflow id
     * @param data
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public void saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegException;

    /**
     * Get the output results of a output node of an experiment
     * @deprecated
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegException;
    
    /**
     * Get the list of output node results of an experiment 
     * @deprecated
     * @param experimentId - also the workflow id
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegException;

    /**
     * Get the names of the output nodes of a workflow instance run
     * @deprecated
     * @param exeperimentId - also the workflow id
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegException;

	/*---------------------------------------  Retrieving Experiment ------------------------------------------*/
    /**
     * Return workflow execution object fully populated with data currently avaialble for that experiment
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public ExperimentData getExperiment(String experimentId) throws RegException;
	
	public ExperimentData getExperimentMetaInformation(String experimentId)throws RegException;
	
	public List<ExperimentData> getAllExperimentMetaInformation(String user)throws RegException;
	
	/**
	 * Retrieve experiments which their names match the regular expression experimentNameRegex
	 * @param user
	 * @param experimentNameRegex
	 * @return
	 * @throws org.apache.airavata.registry.api.exception.RegException
	 */
	public List<ExperimentData> searchExperiments(String user, String experimentNameRegex)throws RegException;
    
    /**
     * Return experiment ids of experiments launched by the given user
     * @param user - a regex user id
     * @return - experiment id list
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
	public List<String> getExperimentIdByUser(String user) throws RegException;

	/**
	 * Return experiments launched by the given user
	 * @param user
	 * @return experiment object list each populated by current data of that experiment
	 * @throws org.apache.airavata.registry.api.exception.RegException
	 */
    public List<ExperimentData> getExperimentByUser(String user) throws RegException;
    
	public List<ExperimentData> getExperiments(HashMap<String, String> params) throws RegException;
    
    /**
     * Return the pageNo set of experiments launched by the given user if grouped in to pages of size pageSize
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ExperimentData> getExperimentByUser(String user, int pageSize, int pageNo) throws RegException;

    /**
     * This will update the workflowStatus for given experimentID,workflowInstanceID combination.
     * @param workflowStatusNode
     * @return
     */
    public void updateWorkflowNodeStatus(NodeExecutionStatus workflowStatusNode)throws RegException;

    public void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, State status)throws RegException;
    
    public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode, State status)throws RegException;

    public NodeExecutionStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode)throws RegException;
    
    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode)throws RegException;
    
    public Date getWorkflowStartTime(WorkflowExecution workflowInstance)throws RegException;
    
    /**
     * @deprecated - Will be removed from 0.9 release onwards. Use {@see #addApplicationJob #updateApplicationJob(ApplicationJob) etc.} functions instead.
     * This will store the gram specific data in to repository, this can be called before submitting the workflow in to Grid
     * @param workflowNodeGramData
     * @return
     */
    public void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData)throws RegException;
    
    public WorkflowExecutionData getWorkflowInstanceData(String workflowInstanceId)throws RegException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId)throws RegException;
    
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent)throws RegException;
    
    public NodeExecutionData getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId)throws RegException;

    public void addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName) throws RegException;
    
    public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws RegException;
    
    public void addWorkflowInstanceNode(String workflowInstance, String nodeId) throws RegException;
    
    
	/*---------------------------------------  Errors in experiment executions ------------------------------------------*/

    /**
     * Return errors defined at the experiment level 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ExperimentExecutionError> getExperimentExecutionErrors(String experimentId) throws RegException;
    
    /**
     * Return errors defined at the workflow level 
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<WorkflowExecutionError> getWorkflowExecutionErrors(String experimentId, String workflowInstanceId) throws RegException;

    /**
     * Return errors defined at the node level 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<NodeExecutionError> getNodeExecutionErrors(String experimentId, String workflowInstanceId, String nodeId) throws RegException;
    
    /**
     * Return errors defined for a Application job 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param jobId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ApplicationJobExecutionError> getApplicationJobErrors(String experimentId, String workflowInstanceId, String nodeId, String jobId) throws RegException;

    /**
     * Return errors defined for a Application job 
     * @param jobId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ApplicationJobExecutionError> getApplicationJobErrors(String jobId) throws RegException;

    /**
     * Return errors filtered by the parameters
     * @param experimentId 
     * @param workflowInstanceId
     * @param nodeId
     * @param jobId
     * @param filterBy - what type of source types the results should contain
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ExecutionError> getExecutionErrors(String experimentId, String workflowInstanceId, String nodeId, String jobId, ExecutionErrors.Source...filterBy) throws RegException;
    /**
     * Adds an experiment execution error 
     * @param error
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public int addExperimentError(ExperimentExecutionError error) throws RegException;
    
    /**
     * Adds an workflow execution error 
     * @param error
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public int addWorkflowExecutionError(WorkflowExecutionError error) throws RegException;
    
    /**
     * Adds an node execution error 
     * @param error
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public int addNodeExecutionError(NodeExecutionError error) throws RegException;

    /**
     * Adds an Application job execution error 
     * @param error
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public int addApplicationJobExecutionError(ApplicationJobExecutionError error) throws RegException;
    
    
	/*---------------------------------------  Managing Data for Application Jobs ------------------------------------------*/

    /**
     * Returns <code>true</code> if a Application job data is existing in the registry
     * @param jobId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public boolean isApplicationJobExists(String jobId) throws RegException;
    
    /**
     * Adding data related to a new Application job submission
     * @param job - the <code>jobId</code> cannot be <code>null</code>.
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void addApplicationJob(ApplicationJob job) throws RegException;
    
    /**
     * update data related to a existing Application job record in the registry
     * @param job - the <code>jobId</code> cannot be <code>null</code> and should already exist in registry
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJob(ApplicationJob job) throws RegException;
    
    /**
     * Update the status of the job
     * @param jobId
     * @param status
     * @param statusUpdateTime
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJobStatus(String jobId, ApplicationJobStatus status, Date statusUpdateTime) throws RegException;
   
    /**
     * Update the job data. GFacProvider implementation should decide the job data. Typically it'll 
     * be a serialization of the submitted job query (eg: rsl for a GRAM job) 
     * @param jobId
     * @param jobdata
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJobData(String jobId, String jobdata) throws RegException;
    
    /**
     * Update the time of job submission or job started executing
     * @param jobId
     * @param submitted
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJobSubmittedTime(String jobId, Date submitted) throws RegException;
    
    /**
     * Update the time of current job status is valid.
     * @param jobId
     * @param statusUpdateTime
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJobStatusUpdateTime(String jobId, Date statusUpdateTime) throws RegException;
    
    /**
     * Custom data field for users
     * @param jobId
     * @param metadata
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public void updateApplicationJobMetadata(String jobId, String metadata) throws RegException;
    
    /**
     * Retrieve the Application Job for the given job id
     * @param jobId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public ApplicationJob getApplicationJob(String jobId) throws RegException;
    
    /**
     * Retrieve a list of Application jobs executed for the given descriptors
     * @param serviceDescriptionId - should be <code>null</code> if user does not care what service description the job corresponds to
     * @param hostDescriptionId - should be <code>null</code> if user does not care what host description the job corresponds to
     * @param applicationDescriptionId - should be <code>null</code> if user does not care what application description the job corresponds to
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ApplicationJob> getApplicationJobsForDescriptors(String serviceDescriptionId, String hostDescriptionId, String applicationDescriptionId) throws RegException;
    
    /**
     * Retrieve a list of Application jobs executed for the given experiment credentials
     * @param experimentId - should be <code>null</code> if user does not care what experiment the job corresponds to
     * @param workflowExecutionId -  - should be <code>null</code> if user does not care what workflow execution the job corresponds to
     * @param nodeId  - should be <code>null</code> if user does not care what node id the job corresponds to
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ApplicationJob> getApplicationJobs(String experimentId, String workflowExecutionId, String nodeId) throws RegException;
    
    /**
     * Retrieve the list all the status updates for an application job.
     * @param jobId - Application job id
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegException
     */
    public List<ApplicationJobStatusData> getApplicationJobStatusHistory(String jobId) throws RegException;
}