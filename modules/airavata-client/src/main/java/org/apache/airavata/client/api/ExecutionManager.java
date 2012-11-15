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

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorEventListener;

public interface ExecutionManager {
    /**
     * Run an experiment containing single workflow
     * @param workflowTemplateId
     * @param inputs
     * @return
     * @throws AiravataAPIInvocationException
     */
	public abstract String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs) throws AiravataAPIInvocationException;

    /**
     * Run an experiment containing single workflow
     * @param workflow
     * @param inputs
     * @return
     * @throws AiravataAPIInvocationException
     */
	public abstract String runExperiment(Workflow workflow,List<WorkflowInput> inputs) throws AiravataAPIInvocationException;

    /**
     * Run an experiment containing single workflow
     * @param workflowTemplateId
     * @param inputs
     * @param user
     * @param metadata
     * @param workflowInstanceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public abstract String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)throws AiravataAPIInvocationException;

	/**
     * Run an experiment containing single workflow
     * @param workflowTemplateId
     * @param inputs
     * @param user
     * @param metadata
     * @param workflowInstanceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public abstract String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, String experimentName)throws AiravataAPIInvocationException;

	/**
	 * Run an experiment containing single workflow
	 * @param workflowTemplateId
	 * @param inputs
	 * @param user
	 * @param metadata
	 * @param workflowInstanceName
	 * @param builder
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public abstract String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder)throws AiravataAPIInvocationException;

    /**
     * Run an experiment containing single workflow
     * @param workflow
     * @param inputs
     * @param user
     * @param metadata
     * @return
     * @throws AiravataAPIInvocationException
     */
	public abstract String runExperiment(Workflow workflow,List<WorkflowInput> inputs, String user, String metadata)throws AiravataAPIInvocationException;

    /**
     * Get a monitor for a running experiment
     * @param experimentId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Monitor getExperimentMonitor(String experimentId)throws AiravataAPIInvocationException;

    /**
     * Get a monitor for a running experiment
     * @param experimentId
     * @param listener
     * @return
     * @throws AiravataAPIInvocationException
     */
	public Monitor getExperimentMonitor(String experimentId, MonitorEventListener listener) throws AiravataAPIInvocationException;

	/**
	 * Creates a WorkflowContextHeaderBuilder object that can be used to customize the scheduling of a workflow execution.
	 * Once configured this object run the workflow using
	 *   <code>runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder)</code>
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public WorkflowContextHeaderBuilder createWorkflowContextHeader() throws AiravataAPIInvocationException;


    /**
     * Creates a DefaultExecutionContext.
     * @return DefaultExecutionContext
     * @throws AiravataAPIInvocationException AiravataAPIInvocationException
     */
//    public DefaultExecutionContext createDefaultExecutionContext() throws AiravataAPIInvocationException;

}
