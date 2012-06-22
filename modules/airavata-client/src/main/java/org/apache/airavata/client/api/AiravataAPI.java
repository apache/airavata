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

public interface AiravataAPI {

	public abstract void loadWorkflowFromaFile(String workflowFile)
			throws URISyntaxException, IOException;

	public abstract void loadWorkflowasaString(String workflowAsaString);

	public abstract NameValue[] setInputs(String fileName) throws IOException;

	public abstract void setInputs(Properties inputList);

	public abstract String runWorkflow(String topic);

	public abstract String runWorkflow(String topic, String user);

	public abstract String runWorkflow(String topic, String user,
			String metadata);

	public abstract Monitor getWorkflowExecutionMonitor(String topic);

	public abstract Monitor getWorkflowExecutionMonitor(String topic,
			MonitorEventListener listener);

	public abstract String runWorkflow(String topic, NameValue[] inputs)
			throws Exception;

	public abstract String runWorkflow(String topic, NameValue[] inputs,
			String user) throws Exception;

	public abstract String runWorkflow(final String topic,
			final NameValue[] inputs, final String user, final String metadata)
			throws Exception;

	public abstract List<WorkflowExecution> getWorkflowExecutionDataByUser(
			String user) throws RegistryException;

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

	public abstract AiravataRegistry getRegistry();

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

	public abstract Property getWorkflowAsString(String workflowTemplateId)
			throws RegistryException, PathNotFoundException,
			RepositoryException;
	
	public AiravataManager getAiravataManager();
	
	public ApplicationManager getApplicationManager();
	
	public WorkflowManager getWorkflowManager();
	
	public ProvenanceManager getProvenanceManager();
	
	public UserManager getUserManager();
	
	public ExecutionManager getExecutionManager();
	
	public String getCurrentUser();

}