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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.airavata.client.airavata.AiravataClientConfiguration;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorEventListener;

/**
 * This is the base interface for AiravataAPI which contains all the base methods for Airavata API
 */
public interface AiravataAPI {

    /**
     * Set the workflow object in to Client from the given file path
     * @param workflowFile
     * @throws URISyntaxException
     * @throws IOException
     */
	public abstract void loadWorkflowFromaFile(String workflowFile)
			throws URISyntaxException, IOException;

    /**
     * Set the workflow object in to Client from the given workflow String (Workflow.xwf context as a string)
     * @param workflowAsaString
     */
	public abstract void loadWorkflowasaString(String workflowAsaString);

    /**
     * This file has to be a properties file with properly setup the inputName values
     * @param fileName Path of the properties file
     * @return
     * @throws IOException
     */
	public abstract NameValue[] setInputs(String fileName) throws IOException;

    /**
     * Provide input list as a Properties object with properly setup input Name values
     * @param inputList
     */
	public abstract void setInputs(Properties inputList);

    /**
     * run the workflow with given topic ID and user will be null
     * @param topic
     * @return
     */
	public abstract String runWorkflow(String topic);

    /**
     * run the workflow with given topic ID and user
     * @param topic
     * @param user
     * @return
     */
	public abstract String runWorkflow(String topic, String user);

    /**
     * run the workflow with given topic,user, metadata and workflowInstanceName
     * @param topic
     * @param user
     * @param metadata
     * @return
     */
	public abstract String runWorkflow(String topic, String user,
			String metadata, String workflowInstanceName);

    /**
     * Returns the Monitor object for given experiment
     * @param topic
     * @return Monitor handler, so that user can use this object to extract notification
     *         for this particular experiment
     */
	public abstract Monitor getWorkflowExecutionMonitor(String topic);

    /**
     * Returns a Monitor object with given listener
     * @param topic
     * @param listener
     * @return
     */
	public abstract Monitor getWorkflowExecutionMonitor(String topic,
			MonitorEventListener listener);

    /**
     * launch the workflow with given topic and given input Values
     * @param topic
     * @param inputs
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(String topic, NameValue[] inputs)
			throws Exception;

    /**
     * launch the workflow with given topic,input values and user
     * @param topic
     * @param inputs
     * @param user
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(String topic, NameValue[] inputs,
			String user) throws Exception;

    /**
     * This could be considered as mostly recommended workflow launch method which required all the parameters.
     * This accept topic,input values in NameValue array,username,metadata, and workflowInstanceName
     * @param topic
     * @param inputs
     * @param user
     * @param metadata
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(final String topic,
			final NameValue[] inputs, final String user, final String metadata, String workflowInstanceName)
			throws Exception;

    /**
     * Extract WorkflowExecution data based on the given user
     * @param user
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowExecution> getWorkflowExecutionDataByUser(
			String user) throws RegistryException;

    /**
     * Extract WorkflowExecution data based on the given topic, we do exact match for the topic
     * @param topic
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowExecution getWorkflowExecutionData(String topic)
			throws RegistryException;

	/**
	 * Extract WorkflowExecution data based on the given user, but return the data in given size
	 * @param user
	 * @param pageSize
	 *            - number of executions to return (page size)
	 * @param PageNo
	 *            - which page to return to (>=0)
	 * @return
	 * @throws RegistryException
	 */
	public abstract List<WorkflowExecution> getWorkflowExecutionData(
			String user, int pageSize, int PageNo) throws RegistryException;

    /**
     * Gives the registry handler to do registry related operations
     * @return
     */
	public abstract AiravataRegistry getRegistry();

    /**
     * Returns configuration object of AiravataClient API
     * @return
     */
	public abstract AiravataClientConfiguration getClientConfiguration();

	/**
	 * Retrieve the names of the workflow templates saved in the registry
	 * @return
	 */
	public abstract List<String> getWorkflowTemplateIds();

	/**
	 * Execute the given workflow template with the given inputs and return the topic id 
	 * @param workflowTemplateId
	 * @param inputs
	 * @return
	 */
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs) throws Exception;
	
	/**
	 * Execute the given workflow template with the given inputs and return the topic id and workflow instance
	 * @param workflowTemplateId
	 * @param inputs
	 * @return
	 */
	public abstract String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs, String workflowInstanceName) throws Exception;

	/**
	 * Execute the given workflow template with the given inputs, user, metadata and return the topic id
	 * @param workflowTemplateId
	 * @param inputs
	 * @param user
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	public abstract String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)
			throws Exception;

	/**
	 * Retrieve the inputs for the given workflow template
	 * @param workflowTemplateId
	 * @return
	 */
	public abstract List<WorkflowInput> getWorkflowInputs(
			String workflowTemplateId) throws Exception;

    /**
     * Returns the workflow file as a string
     * @param workflowTemplateId
     * @return
     * @throws RegistryException
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
	public abstract Property getWorkflowAsString(String workflowTemplateId)
			throws RegistryException, PathNotFoundException,
			RepositoryException;

    /**
     * Returns the AiravataManager
     * @return
     */
	public AiravataManager getAiravataManager();

    /**
     * Returns the ApplicationManager
     * @return
     */
	public ApplicationManager getApplicationManager();

    /**
     * Returns the WorkflowManager
     * @return
     */
	public WorkflowManager getWorkflowManager();

    /**
     * Returns the ProvenanceManager
     * @return
     */
	public ProvenanceManager getProvenanceManager();

    /**
     * Returns the UserManager
     * @return
     */
	public UserManager getUserManager();

    /**
     * Returns the ExecutionManager
     * @return
     */
	public ExecutionManager getExecutionManager();

    /**
     * Returns the Current User
     * @return
     */
	public String getCurrentUser();

    /**
     * Gives the service Node IDs for the given template ID, this will be useful when you want to know the service ID to
     * Configure each node with different WorkflowContextHeaders
     * @param templateID
     * @return
     */
    public List<String> getWorkflowServiceNodeIDs(String templateID);
}