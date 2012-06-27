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
     *
     * @param workflowFile
     * @throws URISyntaxException
     * @throws IOException
     */
	public abstract void loadWorkflowFromaFile(String workflowFile)
			throws URISyntaxException, IOException;

    /**
     *
     * @param workflowAsaString
     */
	public abstract void loadWorkflowasaString(String workflowAsaString);

    /**
     *
     * @param fileName
     * @return
     * @throws IOException
     */
	public abstract NameValue[] setInputs(String fileName) throws IOException;

    /**
     *
     * @param inputList
     */
	public abstract void setInputs(Properties inputList);

    /**
     *
     * @param topic
     * @return
     */
	public abstract String runWorkflow(String topic);

    /**
     *
     * @param topic
     * @param user
     * @return
     */
	public abstract String runWorkflow(String topic, String user);

    /**
     *
     * @param topic
     * @param user
     * @param metadata
     * @return
     */
	public abstract String runWorkflow(String topic, String user,
			String metadata);

    /**
     *
     * @param topic
     * @return
     */
	public abstract Monitor getWorkflowExecutionMonitor(String topic);

    /**
     *
     * @param topic
     * @param listener
     * @return
     */
	public abstract Monitor getWorkflowExecutionMonitor(String topic,
			MonitorEventListener listener);

    /**
     *
     * @param topic
     * @param inputs
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(String topic, NameValue[] inputs)
			throws Exception;

    /**
     *
     * @param topic
     * @param inputs
     * @param user
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(String topic, NameValue[] inputs,
			String user) throws Exception;

    /**
     *
     * @param topic
     * @param inputs
     * @param user
     * @param metadata
     * @return
     * @throws Exception
     */
	public abstract String runWorkflow(final String topic,
			final NameValue[] inputs, final String user, final String metadata)
			throws Exception;

    /**
     *
     * @param user
     * @return
     * @throws RegistryException
     */
	public abstract List<WorkflowExecution> getWorkflowExecutionDataByUser(
			String user) throws RegistryException;

    /**
     *
     * @param topic
     * @return
     * @throws RegistryException
     */
	public abstract WorkflowExecution getWorkflowExecutionData(String topic)
			throws RegistryException;

	/**
	 * 
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
     *
     * @return
     */
	public abstract AiravataRegistry getRegistry();

    /**
     *
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
	public abstract String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs) throws Exception;

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
			List<WorkflowInput> inputs, String user, String metadata)
			throws Exception;

	/**
	 * Retrieve the inputs for the given workflow template
	 * @param workflowTemplateId
	 * @return
	 */
	public abstract List<WorkflowInput> getWorkflowInputs(
			String workflowTemplateId) throws Exception;

    /**
     *
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
     *
     * @return
     */
	public AiravataManager getAiravataManager();

    /**
     *
     * @return
     */
	public ApplicationManager getApplicationManager();

    /**
     *
     * @return
     */
	public WorkflowManager getWorkflowManager();

    /**
     *
     * @return
     */
	public ProvenanceManager getProvenanceManager();

    /**
     *
     * @return
     */
	public UserManager getUserManager();

    /**
     *
     * @return
     */
	public ExecutionManager getExecutionManager();

    /**
     *
     * @return
     */
	public String getCurrentUser();

    /**
     *
     * @param templateID
     * @return
     */
    public List<String> getWorkflowServiceNodeIDs(String templateID);
}