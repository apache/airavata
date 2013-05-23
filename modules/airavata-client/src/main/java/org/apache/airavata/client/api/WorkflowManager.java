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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowData;
import org.apache.airavata.workflow.model.wf.WorkflowInput;

public interface WorkflowManager {

	//privilledged API

    /**
     * Save the workflow under the given user
     * @param workflowAsString The workflow as a string.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @return  <code>true</code> if successful else <code>false</code>.
     * @throws AiravataAPIInvocationException If an error occurred while saving the workflow.
     * @deprecated Use {@see #addOwnerWorkflow(String workflowAsString, String owner)} and
     *                  {@see #updateOwnerWorkflow(String workflowAsString, String owner)} methods.
     */
    @Deprecated
	public boolean saveWorkflow(String workflowAsString, String owner) throws AiravataAPIInvocationException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowAsString The new workflow to add as a string.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     * @throws WorkflowAlreadyExistsException If adding workflow already exists for the given owner.
     */
    public void addOwnerWorkflow (String workflowAsString, String owner) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowPath File path of the workflow.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     * @throws WorkflowAlreadyExistsException If adding workflow already exists for the given owner.
     */
    public void addOwnerWorkflow (URI workflowPath, String owner) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflow The new workflow to add.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     * @throws WorkflowAlreadyExistsException If adding workflow already exists for the given owner.
     */
    public void addOwnerWorkflow (Workflow workflow, String owner) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * @param workflowPath File path of the workflow.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateOwnerWorkflow (URI workflowPath, String owner) throws AiravataAPIInvocationException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * @param workflowAsString The workflow to update as a string. Workflow is uniquely identified by &lt;xgr:id&gt; tag..
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateOwnerWorkflow (String workflowAsString, String owner) throws AiravataAPIInvocationException;

    /**
     * Save the workflow under the given user
     * @param workflow The workflow as a string.
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @return <code>true</code> if successful else <code>false</code>.
     * @throws AiravataAPIInvocationException If an error occurred while saving the workflow.
     * @deprecated Use {@see #addOwnerWorkflow(Workflow workflow, String owner)} and
     *                  {@see #updateOwnerWorkflow(Workflow workflow, String owner)} methods.
     */
    @Deprecated
    public boolean saveWorkflow(Workflow workflow, String owner) throws AiravataAPIInvocationException;



    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * @param workflow The workflow to update. Workflow is uniquely identified by &lt;xgr:id&gt; tag..
     * @param owner Under which user workflow should be added. This is a privileged method and usually
     *              only admin calls this.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateOwnerWorkflow (Workflow workflow, String owner) throws AiravataAPIInvocationException;

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
     * @param workflowAsString Workflow as a string.
     * @deprecated Use {@see #addWorkflow(String workflowAsString)} and
     *          {@see #updateWorkflow(String workflowAsString)} methods.
     * @throws AiravataAPIInvocationException
     */
    @Deprecated
	public boolean saveWorkflow(String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Save the workflow as public
     * @param workflowAsString Workflow as a string.
     * @deprecated Use {@see #addWorkflowAsPublic(String workflowAsString)} and
     *          {@see #updateWorkflowAsPublic(String workflowAsString)} methods.
     * @throws AiravataAPIInvocationException
     */
    @Deprecated
	public boolean saveWorkflowAsPublic(String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Save the workflow
     * @param workflow {@see Workflow} object to save.
     * @deprecated Use {@see #addWorkflow(Workflow workflow)} and
     *          {@see #updateWorkflow(Workflow workflow)} methods.
     * @throws AiravataAPIInvocationException
     */
    @Deprecated
    public boolean saveWorkflow(Workflow workflow) throws AiravataAPIInvocationException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowAsString The new workflow to add and its content as a string.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     */
    public void addWorkflowAsPublic (String workflowAsString) throws WorkflowAlreadyExistsException,
    	AiravataAPIInvocationException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * @param workflowAsString The workflow to update. Workflow is uniquely identified by &lt;xgr:id&gt; tag.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateWorkflowAsPublic (String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowUri Where the workflow file (xml file) exists.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     */
    public void addWorkflowAsPublic (URI workflowUri) throws WorkflowAlreadyExistsException,
    		AiravataAPIInvocationException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * @param workflowUri Where the workflow file resides. File location is given as a URI.
     *                    Workflow is uniquely identified by &lt;xgr:id&gt; tag.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateWorkflowAsPublic (URI workflowUri) throws AiravataAPIInvocationException;


    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowAsString The new workflow to add and its content as a string.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     */
    public void addWorkflow (String workflowAsString) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * TODO : What exception should we throw if a different user tries to update the workflow ?
     * @param workflowAsString The workflow to update. Workflow is uniquely identified by &lt;xgr:id&gt; tag..
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateWorkflow (String workflowAsString) throws AiravataAPIInvocationException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflow The new workflow to add.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     */
    public void addWorkflow (Workflow workflow) throws WorkflowAlreadyExistsException,
    		AiravataAPIInvocationException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * TODO : What exception should we throw if a different user tries to update the workflow ?
     * @param workflow The workflow to update. Workflow is uniquely identified by &lt;xgr:id&gt; tag..
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateWorkflow (Workflow workflow) throws AiravataAPIInvocationException;

    /**
     * Adds a new workflow. Workflow is added to users private space. i.e. only user who added the
     * workflow will be able to retrieve it.
     * @param workflowUri New workflow file as a URI.
     * @throws AiravataAPIInvocationException If an error occurred while adding a new workflow.
     */
    public void addWorkflow (URI workflowUri) throws WorkflowAlreadyExistsException,
			AiravataAPIInvocationException;

    /**
     * Updates a given workflow. Only user who added the workflow will be able to update it.
     * TODO : What exception should we throw if a different user tries to update the workflow ?
     * @param workflowUri The workflow to update as a URI.
     * @throws AiravataAPIInvocationException If an error occurred while updating the workflow.
     */
    public void updateWorkflow (URI workflowUri) throws AiravataAPIInvocationException;

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
     * Creates a workflow from a given URI. When we want to create a workflow from a file path
     * we can use this method.
     * @param workflowPath The workflow file path as a URI.
     * @return A workflow object created using given workflow XML file.
     * @throws AiravataAPIInvocationException If an error occurred while parsing the XML file.
     */
    public Workflow getWorkflowFromURI(URI workflowPath) throws AiravataAPIInvocationException;


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
	public List<WorkflowInput> getWorkflowInputs(String workflowName) throws AiravataAPIInvocationException;
    
    /**
     * Retrieve the workflow inputs for a workflow
     * @param workflowData
     * @return
     * @throws AiravataAPIInvocationException
     * @throws Exception
     */
    public List<WorkflowInput> getWorkflowInputs(WorkflowData workflowData) throws AiravataAPIInvocationException;
    
    /**
     * Retrieve all workflows in published space & user space accessible to the user. 
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<WorkflowData> getAllWorkflows() throws AiravataAPIInvocationException;

    /**
     * Check to see if the workflow exists in user space
     * @param workflowName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean isWorkflowExists(String workflowName) throws AiravataAPIInvocationException;

    /**
     * Update an existing workflow with the given workflow graph string
     * @param workflowName
     * @param workflowGraphXml
     * @throws AiravataAPIInvocationException
     */
    public void updateWorkflow(String workflowName, String workflowGraphXml) throws AiravataAPIInvocationException;

    /**
     * Delete the workflow from the user space
     * @param workflowName
     * @throws AiravataAPIInvocationException
     */
    public void removeWorkflow(String workflowName) throws AiravataAPIInvocationException;

}
