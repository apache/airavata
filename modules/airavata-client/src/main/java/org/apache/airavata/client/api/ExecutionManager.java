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
import org.apache.airavata.registry.api.ExecutionErrors;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.ExecutionError;
import org.apache.airavata.registry.api.workflow.ExperimentExecutionError;
import org.apache.airavata.registry.api.workflow.GFacJobExecutionError;
import org.apache.airavata.registry.api.workflow.NodeExecutionError;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionError;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.EventDataListener;

public interface ExecutionManager {
    /**
     * Run an experiment containing single workflow
     * @param workflow - Workflow template Id or Workflow Graph XML
     * @param inputs
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(String workflow,List<WorkflowInput> inputs) throws AiravataAPIInvocationException;

    /**
     * Run an experiment containing single workflow with custom settings for the experiment
     * @param workflow - Workflow template Id or Workflow Graph XML
     * @param inputs
     * @param options
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(String workflow,List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException;

    /**
     * Run an experiment containing single workflow with custom settings for the experiment & listen 
     * for notification events 
     * @param workflow - Workflow template Id or Workflow Graph XML
     * @param inputs
     * @param options
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(String workflow,List<WorkflowInput> inputs, ExperimentAdvanceOptions options, EventDataListener listener) throws AiravataAPIInvocationException;
	
    /**
     * Run an experiment containing single workflow
     * @param workflow
     * @param inputs
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(Workflow workflow,List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException;

    /**
     * @deprecated Use the function <code>runExperiment(String,List&ltWorkflowInput&gt,ExperimentAdvanceOptions)</code> instead. <br />
     * Run an experiment containing single workflow
     * @param workflowTemplateId
     * @param inputs
     * @param user
     * @param metadata
     * @param workflowInstanceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)throws AiravataAPIInvocationException;

	/**
	 * @deprecated Use the function <code>runExperiment(String,List&ltWorkflowInput&gt,ExperimentAdvanceOptions)</code> instead. <br />
     * Run an experiment containing single workflow
     * @param workflowTemplateId
     * @param inputs
     * @param user
     * @param metadata
     * @param workflowInstanceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, String experimentName)throws AiravataAPIInvocationException;

	/**
	 * @deprecated Use the function <code>runExperiment(String,List&ltWorkflowInput&gt,ExperimentAdvanceOptions)</code> instead. <br />
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
	public String runExperiment(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder)throws AiravataAPIInvocationException;

    /**
     * @deprecated Use the function <code>runExperiment(String,List&ltWorkflowInput&gt,ExperimentAdvanceOptions)</code> instead. <br />
     * Run an experiment containing single workflow
     * @param workflow
     * @param inputs
     * @param user
     * @param metadata
     * @return
     * @throws AiravataAPIInvocationException
     */
	public String runExperiment(Workflow workflow,List<WorkflowInput> inputs, String user, String metadata)throws AiravataAPIInvocationException;

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
	public Monitor getExperimentMonitor(String experimentId, EventDataListener listener) throws AiravataAPIInvocationException;
	
	/**
	 * @deprecated
	 * Creates a WorkflowContextHeaderBuilder object that can be used to customize the scheduling of a workflow execution.
	 * Once configured this object run the workflow using
	 *   <code>runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder)</code>
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
	public WorkflowContextHeaderBuilder createWorkflowContextHeader() throws AiravataAPIInvocationException;

	/**
	 * Create a new experiment advance options
	 * @return
	 * @throws AiravataAPIInvocationException
	 */
    public ExperimentAdvanceOptions createExperimentAdvanceOptions() throws AiravataAPIInvocationException;
    
    /**
     * Create a new experiment advance options
     * @param experimentName - Name of the running experiment
     * @param experimentUser - Experiment submission user
     * @param experimentMetadata - Experiment metadata 
     * @return
     * @throws AiravataAPIInvocationException
     */
    public ExperimentAdvanceOptions createExperimentAdvanceOptions(String experimentName, String experimentUser, String experimentMetadata) throws AiravataAPIInvocationException;
    
    /**
     * Returns when the given experiment has completed
     * @param experimentId
     * @throws AiravataAPIInvocationException
     */
    public void waitForExperimentTermination(String experimentId) throws AiravataAPIInvocationException;

    /*
     * Errors in experiment executions
     */
    
    /**
     * Return errors defined at the experiment level 
     * @param experimentId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ExperimentExecutionError> getExperimentExecutionErrors(String experimentId) throws AiravataAPIInvocationException;
    
    /**
     * Return errors defined at the workflow level 
     * @param experimentId
     * @param workflowInstanceId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<WorkflowExecutionError> getWorkflowExecutionErrors(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException;

    /**
     * Return errors defined at the node level 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<NodeExecutionError> getNodeExecutionErrors(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException;
    
    /**
     * Return errors defined for a GFac job 
     * @param experimentId
     * @param workflowInstanceId
     * @param nodeId
     * @param gfacJobId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<GFacJobExecutionError> getGFacJobErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId) throws AiravataAPIInvocationException;

    /**
     * Return errors defined for a GFac job 
     * @param gfacJobId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<GFacJobExecutionError> getGFacJobErrors(String gfacJobId) throws AiravataAPIInvocationException;

    /**
     * Return errors filtered by the parameters
     * @param experimentId 
     * @param workflowInstanceId
     * @param nodeId
     * @param gfacJobId
     * @param filterBy - what type of source types the results should contain
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ExecutionError> getExecutionErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId, ExecutionErrors.Source...filterBy) throws AiravataAPIInvocationException;

    /**
     * Adds an experiment execution error 
     * @param error
     * @return
     * @throws AiravataAPIInvocationException
     */
    public int addExperimentError(ExperimentExecutionError error) throws AiravataAPIInvocationException;
    
    /**
     * Adds an workflow execution error 
     * @param error
     * @return
     * @throws AiravataAPIInvocationException
     */
    public int addWorkflowExecutionError(WorkflowExecutionError error) throws AiravataAPIInvocationException;
    
    /**
     * Adds an node execution error 
     * @param error
     * @return
     * @throws AiravataAPIInvocationException
     */
    public int addNodeExecutionError(NodeExecutionError error) throws AiravataAPIInvocationException;

    /**
     * Adds an GFac job execution error 
     * @param error
     * @return
     * @throws AiravataAPIInvocationException
     */
    public int addGFacJobExecutionError(GFacJobExecutionError error) throws AiravataAPIInvocationException;

}
