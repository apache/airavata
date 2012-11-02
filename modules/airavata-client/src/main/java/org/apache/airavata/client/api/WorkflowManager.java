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

import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowData;
import org.apache.airavata.workflow.model.wf.WorkflowInput;

public interface WorkflowManager {

	//privilledged API

    /**
     * Save the workflow under the given user
     * @param workflowAsString
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
	public boolean saveWorkflow(String workflowAsString, String owner) throws AiravataAPIInvocationException;

    /**
     * Save the workflow under the given user
     * @param workflow
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean saveWorkflow(Workflow workflow, String owner) throws AiravataAPIInvocationException;

    /**
     * Retrieve workflows saved by the given user
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<Workflow> getWorkflows(String owner) throws AiravataAPIInvocationException;

    /**
     * Retrieve workflow templace ids saved by the given user
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<String> getWorkflowTemplateIds(String owner) throws AiravataAPIInvocationException;

    /**
     * Retrieve a given workflow saved by the given user
     * @param workflowName
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Workflow getWorkflow(String workflowName, String owner) throws AiravataAPIInvocationException;

    /**
     * Retrive the given workflow saved by the given user as a string
     * @param workflowName
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String getWorkflowAsString(String workflowName, String owner) throws AiravataAPIInvocationException;

    /**
     * Delete the workflow saved by the given user
     * @param workflowName
     * @param owner
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean deleteWorkflow(String workflowName, String owner) throws AiravataAPIInvocationException;
    
    //user api

    /**
     * Save the workflow
     * @param workflowAsString
     * @return
     * @throws AiravataAPIInvocationException
     */
	public boolean saveWorkflow(String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Save the workflow as public
     * @param workflowAsString
     * @return
     * @throws AiravataAPIInvocationException
     */
	public boolean saveWorkflowAsPublic(String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Save the workflow
     * @param workflow
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean saveWorkflow(Workflow workflow) throws AiravataAPIInvocationException;

    /**
     * Save the workflow as public
     * @param workflow
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean saveWorkflowAsPublic(Workflow workflow) throws AiravataAPIInvocationException;

    /**
     * Get all workflows of the current user
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<Workflow> getWorkflows() throws AiravataAPIInvocationException;

    /**
     * Get template id's of all workflows of the current user
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<String> getWorkflowTemplateIds() throws AiravataAPIInvocationException;

    /**
     * Retrieve the given workflow 
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Workflow getWorkflow(String workflowName) throws AiravataAPIInvocationException;

    /**
     * Retrieve the given workflow as a string
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String getWorkflowAsString(String workflowName) throws AiravataAPIInvocationException;

    /**
     * Delete the given workflow 
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean deleteWorkflow(String workflowName) throws AiravataAPIInvocationException;

    /**
     * Create workflow object from workflow string 
     * @param workflowAsString
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Workflow getWorkflowFromString(String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Convert workflow in to a string
     * @param workflow
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String getWorkflowAsString(Workflow workflow) throws AiravataAPIInvocationException;

    /**
     * Gives the service Node IDs for the given template ID, this will be useful when you want to know the service ID to
     * Configure each node with different WorkflowContextHeaders
     * @param templateID
     * @return
     */
    public List<String> getWorkflowServiceNodeIDs(String templateID) throws AiravataAPIInvocationException;
    
    /**
     * Check if the workflow from the given name is published in the system
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public boolean isPublishedWorkflowExists(String workflowName) throws AiravataAPIInvocationException;
	
	/**
	 * Publish the workflow "workflowName" residing user space to the published space under name  publishWorkflowName
	 * @param workflowName
	 * @param publishWorkflowName
	 * @throws AiravataAPIInvocationException
	 */
	public void publishWorkflow(String workflowName, String publishWorkflowName) throws AiravataAPIInvocationException;
	
	/**
	 * Publish the workflow "workflowName" residing user space
	 * @param workflowName
	 * @throws AiravataAPIInvocationException
	 */
	public void publishWorkflow(String workflowName) throws AiravataAPIInvocationException;
	
	/**
	 * Retrive published workflow
	 * @param workflowName
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public String getPublishedWorkflowGraphXML(String workflowName) throws AiravataAPIInvocationException;
	
	/**
	 * Retrive published workflow
	 * @param workflowName
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public Workflow getPublishedWorkflow(String workflowName) throws AiravataAPIInvocationException;
	
	/**
	 * Retrive published workflow names
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public List<String> getPublishedWorkflowNames() throws AiravataAPIInvocationException;
	
	/**
	 * Retrive published workflows
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public Map<String,Workflow> getPublishedWorkflows() throws AiravataAPIInvocationException;
	
	/**
	 * Remove published workflow from the system
	 * @param workflowName
	 * @throws AiravataAPIInvocationException
	 */
	public void removePublishedWorkflow(String workflowName)throws AiravataAPIInvocationException;

    /**
     * get workflow inputs of the workflow
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     * @throws Exception
     */
	public List<WorkflowInput> getWorkflowInputs(String workflowName) throws AiravataAPIInvocationException, Exception;
    
    /**
     * Retrieve the workflow inputs for a workflow
     * @param workflowData
     * @return
     * @throws AiravataAPIInvocationException
     * @throws Exception
     */
    public List<WorkflowInput> getWorkflowInputs(WorkflowData workflowData) throws AiravataAPIInvocationException, Exception;
    
    /**
     * Retrieve all workflows in published space & user space accessible to the user. 
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<WorkflowData> getAllWorkflows() throws AiravataAPIInvocationException;
    
}
