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

import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorEventListener;

public interface ExecutionManager {

	public abstract String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs) throws AiravataAPIInvocationException;

	public abstract String runWorkflow(Workflow workflow,List<WorkflowInput> inputs) throws AiravataAPIInvocationException;
	
	public abstract String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata)throws AiravataAPIInvocationException;
	
	public abstract String runWorkflow(Workflow workflow,List<WorkflowInput> inputs, String user, String metadata)throws AiravataAPIInvocationException;
	
	public Monitor getWorkflowExecutionMonitor(String topic)throws AiravataAPIInvocationException;

	public Monitor getWorkflowExecutionMonitor(String topic, MonitorEventListener listener) throws AiravataAPIInvocationException;

}
