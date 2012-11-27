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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.AiravataClientConfiguration;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.MonitorEventListener;

import java.util.Calendar;
import java.util.List;

public class ExecutionManagerImpl implements ExecutionManager {
	private AiravataClient client;

	public ExecutionManagerImpl(AiravataClient client) {
		setClient(client);
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs) throws AiravataAPIInvocationException {
		return runExperiment(workflowTemplateId, inputs ,getClient().getCurrentUser(),null, workflowTemplateId+"_"+Calendar.getInstance().getTime().toString());
	}

	@Override
	public String runExperiment(Workflow workflow, List<WorkflowInput> inputs)
			throws AiravataAPIInvocationException {
		return runExperiment(workflow,inputs, getClient().getCurrentUser(),null);
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowTemplateId, inputs, user, metadata, workflowInstanceName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	@Override
	public String runExperiment(Workflow workflow, List<WorkflowInput> inputs,
			String user, String metadata) throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflow, inputs, user, metadata,workflow.getName()+"_"+Calendar.getInstance().getTime().toString());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Monitor getExperimentMonitor(String experimentId)
			throws AiravataAPIInvocationException {
		return getClient().getWorkflowExecutionMonitor(experimentId);
	}

	@Override
	public Monitor getExperimentMonitor(String experimentId,
			MonitorEventListener listener)
			throws AiravataAPIInvocationException {
		return getClient().getWorkflowExecutionMonitor(experimentId,listener);
	}

	public AiravataClient getClient() {
		return client;
	}
	public void setClient(AiravataClient client) {
		this.client = client;
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName, WorkflowContextHeaderBuilder builder)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowTemplateId, inputs, user, metadata, workflowInstanceName,builder);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowContextHeaderBuilder createWorkflowContextHeader()
			throws AiravataAPIInvocationException {
		AiravataClientConfiguration config = getClient().getClientConfiguration();
		try {
			return new WorkflowContextHeaderBuilder(config.getMessagebrokerURL().toString(),
					config.getGfacURL().toString(),config.getRegistryURL().toString(),null,null,
					config.getMessageboxURL().toString());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

//    @Override
//    public DefaultExecutionContext createDefaultExecutionContext() throws AiravataAPIInvocationException {
//        DefaultExecutionContext ec = new DefaultExecutionContext();
////        ec.addNotifiable(new LoggingNotification());
////        try {
//            // TODO : Fix me
////            ec.setRegistryService(getClient().getRegistry());
////        } catch (RegistryException e) {
////            throw new AiravataAPIInvocationException(e);
////        }
//        return  ec;
//    }

	@Override
	public String runExperiment(String workflowName,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName, String experimentName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowName, inputs, user, metadata, workflowInstanceName,experimentName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

}
