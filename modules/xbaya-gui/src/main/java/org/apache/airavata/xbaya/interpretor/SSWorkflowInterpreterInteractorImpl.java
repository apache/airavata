/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.interpretor;
//
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
//
//public class SSWorkflowInterpreterInteractorImpl implements
//		WorkflowInterpreterInteractor {
//	
//	@Override
//	public boolean notify(WorkflowExecutionMessage messageType, WorkflowInterpreterConfiguration config, Object data) {
//		switch (messageType) {
//		case NODE_STATE_CHANGED:
//			break;
//		case EXECUTION_STATE_CHANGED:
//			WorkflowExecutionState state = (WorkflowExecutionState) data;
//			config.getWorkflow().setExecutionState(state);
////			if (state == WorkflowExecutionState.PAUSED
////					|| state == WorkflowExecutionState.STOPPED) {
////				config.getWorkflow().setExecutionState(WorkflowExecutionState.STOPPED);
////			}else if (state == WorkflowExecutionState.RUNNING) {
////				config.getWorkflow().setExecutionState(WorkflowExecutionState.RUNNING);
////			}
//			break;
//		case EXECUTION_TASK_START:
//			break;
//		case EXECUTION_TASK_END:
//			break;
//		case OPEN_SUBWORKFLOW:
//			break;
//		case HANDLE_DEPENDENT_NODES_DIFFERED_INPUTS:
//				break;
//		default:
//			return false;	
//		}
//		return true;
//	}
//
//	@Override
//	public Object retrieveData(WorkflowExecutionMessage messageType, WorkflowInterpreterConfiguration config, Object data)
//			throws Exception {
//		Object result = null;
//		switch (messageType) {
//		case INPUT_WORKFLOWINTERPRETER_FOR_WORKFLOW:
//			Workflow subWorkflow= (Workflow) data;
//            WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(subWorkflow,config.getTopic(),config.getMessageBoxURL(), config.getMessageBrokerURL(), config.getExperimentCatalog(), config.getConfiguration(), config.getGUI(), config.getMonitor());
//            if (config.isTestMode()){
//        		workflowInterpreterConfiguration.setNotifier(new StandaloneNotificationSender(workflowInterpreterConfiguration.getTopic(),workflowInterpreterConfiguration.getWorkflow()));
//            }
//			result = new WorkflowInterpreter(workflowInterpreterConfiguration
//					, 
//					new SSWorkflowInterpreterInteractorImpl());
//			break;
////		case INPUT_GSS_CREDENTIAL:
////			WorkflowInterpreter w = (WorkflowInterpreter) data;
////			result = SecurityUtil.getGSSCredential(w.getUsername(),
////					w.getPassword(), w.getConfig().getConfiguration().getMyProxyServer());
////			break;
////		case INPUT_LEAD_CONTEXT_HEADER:
////			Node node = (Node) data;
////			result = XBayaUtil.buildLeadContextHeader(config.getWorkflow(), config.getConfiguration(),
////					new MonitorConfiguration(config.getMessageBrokerURL(),
////							config.getTopic(), true,
////							config.getMessageBoxURL()), node.getID(),
////					null);
////			break;
//		default:
//			break;
//		}
//		return result;
//	}
//
//	@Override
//	public void pauseExecution(WorkflowInterpreterConfiguration config) {
//		notify(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED,config, WorkflowExecutionState.PAUSED);
//	}
//
//	@Override
//	public void resumeExecution(WorkflowInterpreterConfiguration config) {
//		notify(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED,config, WorkflowExecutionState.RUNNING);
//	}
//
//	@Override
//	public void terminateExecution(WorkflowInterpreterConfiguration config) {
//		notify(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED,config, WorkflowExecutionState.STOPPED);
//	}
//
//	@Override
//	public boolean isExecutionPaused(WorkflowInterpreterConfiguration config) {
//		return config.getWorkflow().getExecutionState()==WorkflowExecutionState.PAUSED;
//	}
//
//	@Override
//	public boolean isExecutionTerminated(WorkflowInterpreterConfiguration config) {
//		return config.getWorkflow().getExecutionState()==WorkflowExecutionState.STOPPED;
//	}
//
//}
