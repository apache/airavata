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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.ExperimentMetadata;
import org.apache.airavata.registry.api.workflow.ExperimentName;
import org.apache.airavata.registry.api.workflow.ExperimentUser;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.registry.api.workflow.NodeExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;

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
	public void setWorkflowInstanceStatus(String experimentId, String workflowInstanceId, State status) throws AiravataAPIInvocationException;

    /**
     * Update the status of the Workflow instance
     * @param status
     * @throws AiravataAPIInvocationException
     */
	public void setWorkflowInstanceStatus(WorkflowExecutionStatus status) throws AiravataAPIInvocationException;

    /**
     * Retrieve the status of the Workflow instance
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowExecutionStatus getWorkflowInstanceStatus(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve the status of the Workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowExecutionStatus getWorkflowInstanceStatus(WorkflowExecution workflowInstance) throws AiravataAPIInvocationException;

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
     * @param experimentId
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
	public WorkflowExecutionData getWorkflowInstanceData(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Retrieve experiment data for a given workflow instance
     * @param workflowInstance
     * @return
     * @throws AiravataAPIInvocationException
     */
	public WorkflowExecutionData getWorkflowInstanceData(WorkflowExecution workflowInstance) throws AiravataAPIInvocationException;

    /**
     * Retrieve output node names of a experiment
     * @param experimentId
     * @deprecated
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String[] getWorkflowExecutionOutputNames(String experimentId) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceNodeStatus(String experimentId, String workflowInstaceId, String nodeId, State status) throws AiravataAPIInvocationException;
	
	public void setWorkflowInstanceNodeStatus(NodeExecutionStatus status) throws AiravataAPIInvocationException;
	
	public NodeExecutionStatus getWorkflowInstanceNodeStatus(String experimentId, String workflowInstaceId, String nodeId) throws AiravataAPIInvocationException;
	
	public NodeExecutionStatus getWorkflowInstanceNodeStatus(WorkflowInstanceNode node) throws AiravataAPIInvocationException;
	
	public void addExperiment(String projectName, String experimentId, String experimentName) throws AiravataAPIInvocationException;
	
	public void addWorkflowInstance(String experimentId, WorkflowExecution workflowInstance) throws AiravataAPIInvocationException;
	
	//TODO setup node type for the node & gram data for the node

    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) throws AiravataAPIInvocationException;

    public void setWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws AiravataAPIInvocationException;

    public void setWorkflowInstanceTemplateName(String workflowInstanceId,String templateName) throws AiravataAPIInvocationException;

    public void saveWorkflowExecutionOutput(String experimentId, String outputNodeName, String output) throws AiravataAPIInvocationException;

    /**
     * @deprecated - Will be removed from 0.9 release onwards. Use {@see #addApplicationJob #updateApplicationJob(ApplucationJob) etc.} functions instead.
     * This will store the gram specific data in to repository, this can be called before submitting the workflow in to Grid
     * @param data
     * @throws AiravataAPIInvocationException
     */
    public void updateWorkflowNodeGramData(WorkflowNodeGramData data) throws AiravataAPIInvocationException;
     
 	/*---------------------------------------  Managing Data for Application Jobs ------------------------------------------*/

     /**
      * Returns <code>true</code> if a Application job data is existing in Airavata
      * @param jobId
      * @return
      * @throws RegistryException
      */
     public boolean isApplicationJobExists(String jobId) throws AiravataAPIInvocationException;
     
     /**
      * Adding data related to a new Application job submission
      * @param job - the <code>jobId</code> cannot be <code>null</code>.
      * @throws AiravataAPIInvocationException
      */
     public void addApplicationJob(ApplicationJob job) throws AiravataAPIInvocationException;
     
     /**
      * Update data related to a existing Application job record in Airavata
      * @param job - the <code>jobId</code> cannot be <code>null</code> and should already exist in Airavata
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJob(ApplicationJob job) throws AiravataAPIInvocationException;
     
     /**
      * Update the status of the job
      * @param jobId
      * @param status
      * @param statusUpdateTime
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJobStatus(String jobId, ApplicationJobStatus status, Date statusUpdateTime) throws AiravataAPIInvocationException;
     
	 /**
	 * Update the status of the job for the current server time
	 * @param jobId
	 * @param status
	 * @throws RegistryException
	 */
     public void updateApplicationJobStatus(String jobId, ApplicationJobStatus status) throws AiravataAPIInvocationException;
     
     /**
      * Update the job data. GFacProvider implementation should decide the job data. Typically it'll 
      * be a serialization of the submitted job query (eg: rsl for a GRAM job) 
      * @param jobId
      * @param jobdata
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJobData(String jobId, String jobdata) throws AiravataAPIInvocationException;
     
     /**
      * Update the time of job submission or job started executing
      * @param jobId
      * @param submitted
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJobSubmittedTime(String jobId, Date submitted) throws AiravataAPIInvocationException;
     
     /**
      * Update the time of current job status is valid.
      * @param jobId
      * @param statusUpdateTime
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJobStatusUpdateTime(String jobId, Date statusUpdateTime) throws AiravataAPIInvocationException;
     
     /**
      * Custom data field for users
      * @param jobId
      * @param metadata
      * @throws AiravataAPIInvocationException
      */
     public void updateApplicationJobMetadata(String jobId, String metadata) throws AiravataAPIInvocationException;
     
     /**
      * Retrieve the Application Job for the given job id
      * @param jobId
      * @return
      * @throws AiravataAPIInvocationException
      */
     public ApplicationJob getApplicationJob(String jobId) throws AiravataAPIInvocationException;
     
     /**
      * Retrieve a list of Application jobs executed for the given descriptors
      * @param serviceDescriptionId - should be <code>null</code> if user does not care what service description the job corresponds to
      * @param hostDescriptionId - should be <code>null</code> if user does not care what host description the job corresponds to
      * @param applicationDescriptionId - should be <code>null</code> if user does not care what application description the job corresponds to
      * @return
      * @throws AiravataAPIInvocationException
      */
     public List<ApplicationJob> getApplicationJobsForDescriptors(String serviceDescriptionId, String hostDescriptionId, String applicationDescriptionId) throws AiravataAPIInvocationException;
     
     /**
      * Retrieve a list of Application jobs executed for the given experiment credentials
      * @param experimentId - should be <code>null</code> if user does not care what experiment the job corresponds to
      * @param workflowExecutionId -  - should be <code>null</code> if user does not care what workflow execution the job corresponds to
      * @param nodeId  - should be <code>null</code> if user does not care what node id the job corresponds to
      * @return
      * @throws AiravataAPIInvocationException
      */
     public List<ApplicationJob> getApplicationJobs(String experimentId, String workflowExecutionId, String nodeId) throws AiravataAPIInvocationException;
}
