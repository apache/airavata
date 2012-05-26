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

package org.apache.airavata.xbaya.interpretor;

import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.util.XBayaUtil;

public class SSWorkflowInterpreterInteractorImpl implements
		WorkflowInterpreterInteractor {
	private Workflow workflow;

	public SSWorkflowInterpreterInteractorImpl(Workflow workflow) {
		this.workflow = workflow;
	}

	@Override
	public boolean notify(WorkflowExecutionMessage messageType, Object data) {
		switch (messageType) {
		case NODE_STATE_CHANGED:
			break;
		case EXECUTION_STATE_CHANGED:
			WorkflowExecutionState state = (WorkflowExecutionState) data;
			if (state == WorkflowExecutionState.PAUSED
					|| state == WorkflowExecutionState.STOPPED) {
				workflow.setExecutionState(WorkflowExecutionState.STOPPED);
			}
			break;
		case EXECUTION_TASK_START:
			break;
		case EXECUTION_TASK_END:
			break;
		}
		return false;
	}

	@Override
	public Object retrieveData(WorkflowExecutionMessage messageType, Object data)
			throws Exception {
		Object result = null;
		switch (messageType) {
		case INPUT_WORKFLOWINTERPRETER_FOR_WORKFLOW:
			WorkflowExecutionData widata = (WorkflowExecutionData) data;
            WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(widata.workflow,widata.topic,widata.currentInterpreter.getConfig().getMessageBoxURL(), widata.currentInterpreter.getConfig().getMessageBrokerURL(), widata.currentInterpreter.getConfig().getRegistry(), widata.currentInterpreter.getConfig().getConfiguration(), widata.currentInterpreter.getConfig().getGUI(), widata.currentInterpreter.getConfig().getMyProxyChecker(), widata.currentInterpreter.getConfig().getMonitor());
			result = new WorkflowInterpreter(workflowInterpreterConfiguration
					, 
					new SSWorkflowInterpreterInteractorImpl(widata.workflow));
			break;
//		case INPUT_GSS_CREDENTIAL:
//			WorkflowInterpreter w = (WorkflowInterpreter) data;
//			result = SecurityUtil.getGSSCredential(w.getUsername(),
//					w.getPassword(), w.getConfig().getConfiguration().getMyProxyServer());
//			break;
		case INPUT_LEAD_CONTEXT_HEADER:
			WSNodeData d = (WSNodeData) data;
			result = XBayaUtil.buildLeadContextHeader(d.currentInterpreter
					.getWorkflow(), d.currentInterpreter.getConfig().getConfiguration(),
					new MonitorConfiguration(d.currentInterpreter
							.getConfig().getMessageBrokerURL(),
							d.currentInterpreter.getConfig().getTopic(), true,
							d.currentInterpreter.getConfig().getMessageBoxURL()), d.wsNode.getID(),
					null);
			break;
		}
		return result;
	}

}
