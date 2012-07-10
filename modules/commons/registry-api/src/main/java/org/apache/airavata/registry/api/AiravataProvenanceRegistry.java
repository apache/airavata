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

import java.util.List;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;

public abstract class AiravataProvenanceRegistry implements DataRegistry{
	private String user;
	
	public AiravataProvenanceRegistry(String user) {
		setUser(user);
	}

	/*------------------------------------------- Experiment data ---------------------------------------------*/
    /**
     * Save the username of the user who runs this experiment 
     * @param experimentId
     * @param user
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionUser(String experimentId, String user) throws RegistryException;
    
    /**
     * Retrieve the user who is runing the experiment
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract String getWorkflowExecutionUser(String experimentId) throws RegistryException;
    
    /**
     * Get the name of the workflow intance
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract String getWorkflowExecutionName(String experimentId) throws RegistryException;
    
    /**
     * Save a name for this workflow execution
     * @param experimentId
     * @param workflowIntanceName
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionName(String experimentId,String workflowIntanceName)throws RegistryException;
    
    /**
     * Save a status for this workflow execution
     * @param experimentId
     * @param status - contains the status and the time of the status was defined
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionStatus(String experimentId,WorkflowInstanceStatus status)throws RegistryException;
    
    /**
     * Save a status for this workflow execution with the current time at the moment
     * @param experimentId
     * @param status - contains the status
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionStatus(String experimentId,ExecutionStatus status)throws RegistryException;

    /**
     * Return the status of the execution
     * @param experimentId
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)throws RegistryException;

	/**
     * Return the metadata information saved for the experiment
     * @param experimentId
     * @return
     * @throws RegistryException
     */
    public abstract String getWorkflowExecutionMetadata(String experimentId) throws RegistryException;
    
    /**
     * Save the metadata for the experiment
     * @param experimentId
     * @param metadata
     * @return
     * @throws RegistryException
     */
    public abstract boolean saveWorkflowExecutionMetadata(String experimentId, String metadata) throws RegistryException;
    
	
    /*-------------------------------------- Experiment Workflow instance node data ----------------------------------------*/
    /**
	 * Save the input data of a node in the workflow instance of an experiment
	 * @param workflowInputData
	 * @return true if successfully saved
	 * @throws RegistryException
	 */
	public abstract boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData workflowInputData) throws RegistryException;

    /**
     * Save the output data of a node in the workflow instance of an experiment
     * @param workflowOutputData
     * @return true if successfully saved
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData workflowOutputData)throws RegistryException;
    
    /**
     * Return a list of data passed as input for service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;

	/**
     * Return a list of data returned as output from service node which regex matched nodeId, workflow template id & experiment id 
     * @param experimentIdRegEx
     * @param workflowNameRegEx - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx - nodeId
	 * @return
	 * @throws RegistryException
	 */
	public abstract List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;

    /**
     * Saves the results of output nodes in a workflow
     * @deprecated 
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @param output
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException;
    
    /**
     * Saves the results of output nodes in a workflow
     * @deprecated
     * @param experimentId - also the workflow id
     * @param data
     * @return
     * @throws RegistryException
     */
	public abstract boolean saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException;

    /**
     * Get the output results of a output node of an experiment
     * @param experimentId - also the workflow id
     * @param outputNodeName
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException;
    
    /**
     * Get the list of output node results of an experiment 
     * @param experimentId - also the workflow id
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException;

    /**
     * Get the names of the output nodes of a workflow instance run
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
	public abstract WorkflowExecution getWorkflowExecution(String experimentId) throws RegistryException;
    
    /**
     * Return experiment ids of experiments launched by the given user
     * @param user - a regex user id
     * @return - experiment id list
     * @throws RegistryException
     */
	public abstract List<String> getWorkflowExecutionIdByUser(String user) throws RegistryException;

	/**
	 * Return experiments launched by the given user
	 * @param user
	 * @return experiment object list each populated by current data of that experiment
	 * @throws RegistryException
	 */
    public abstract List<WorkflowExecution> getWorkflowExecutionByUser(String user) throws RegistryException;
    
    /**
     * Return the pageNo set of experiments launched by the given user if grouped in to pages of size pageSize
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws RegistryException
     */
    public abstract List<WorkflowExecution> getWorkflowExecutionByUser(String user, int pageSize, int pageNo) throws RegistryException;
    
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}