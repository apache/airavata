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
//*/
//package org.apache.airavata.gfac.impl;
//
//import com.google.common.eventbus.Subscribe;
//import org.apache.airavata.common.utils.AiravataUtils;
//import org.apache.airavata.common.utils.LocalEventPublisher;
//import org.apache.airavata.common.utils.listener.AbstractActivityListener;
//import org.apache.airavata.messaging.core.MessageContext;
//import org.apache.airavata.messaging.core.Publisher;
//import org.apache.airavata.model.messaging.event.MessageType;
//import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
//import org.apache.airavata.model.messaging.event.WorkflowIdentifier;
//import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;
//import org.apache.airavata.model.experiment.WorkflowNodeDetails;
//import org.apache.airavata.model.experiment.WorkflowNodeState;
//import org.apache.airavata.model.experiment.WorkflowNodeStatus;
//import org.apache.airavata.model.status.ProcessState;
//import org.apache.airavata.model.status.ProcessStatus;
//import org.apache.airavata.registry.cpi.ExperimentCatalog;
//import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Calendar;
//
//public class AiravataProcessStatusUpdator implements AbstractActivityListener {
//    private final static Logger logger = LoggerFactory.getLogger(AiravataProcessStatusUpdator.class);
//
//    private ExperimentCatalog airavataExperimentCatalog;
//    private LocalEventPublisher localEventPublisher;
//    private Publisher publisher;
//
//
//
//
//    public ExperimentCatalog getAiravataExperimentCatalog() {
//        return airavataExperimentCatalog;
//    }
//
//    public void setAiravataExperimentCatalog(ExperimentCatalog airavataExperimentCatalog) {
//        this.airavataExperimentCatalog = airavataExperimentCatalog;
//    }
//
//    @Subscribe
//    public void setupProcessStatus(TaskStatusChangeEvent taskStatus) throws Exception{
//        ProcessState state;
//    	switch(taskStatus.getState()){
//    	case CANCELED:
//    		state=ProcessState.CANCELED; break;
//    	case COMPLETED:
//    		state=ProcessState.EXECUTING; break;
//    	case FAILED:
//    		state=ProcessState.FAILED; break;
//    	case EXECUTING:
//    		state=ProcessState.EXECUTING; break;
//		default:
//			return;
//    	}
//    	try {
//            String expId = taskStatus.getTaskIdentity().getExperimentId();
//			updateWorkflowNodeStatus(expId, taskStatus.getTaskIdentity().getWorkflowNodeId(), state);
//            logger.debug("expId - {}: Publishing workflow node status for " + taskStatus.getTaskIdentity().getWorkflowNodeId()
//                    + ":" + state.toString(), taskStatus.getTaskIdentity().getExperimentId());
//            WorkflowIdentifier workflowIdentity = new WorkflowIdentifier(taskStatus.getTaskIdentity().getWorkflowNodeId(),
//                                                                         taskStatus.getTaskIdentity().getExperimentId(),
//                                                                         taskStatus.getTaskIdentity().getGatewayId());
//            WorkflowNodeStatusChangeEvent event = new WorkflowNodeStatusChangeEvent(state, workflowIdentity);
//            localEventPublisher.publish(event);
//            String messageId = AiravataUtils.getId("WFNODE");
//            MessageContext msgCntxt = new MessageContext(event, MessageType.WORKFLOWNODE, messageId, taskStatus.getTaskIdentity().getGatewayId());
//            msgCntxt.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
//
//            publisher.publish(msgCntxt);
//		} catch (Exception e) {
//            logger.error("expId - " + taskStatus.getTaskIdentity().getExperimentId() + ": Error persisting data"
//                    + e.getLocalizedMessage(), e);
//            throw new Exception("Error persisting workflow node status..", e);
//        }
//    }
//
//    public  void updateWorkflowNodeStatus(String experimentId, String workflowNodeId, WorkflowNodeState state) throws Exception {
//		logger.info("expId - {}: Updating workflow node status for "+workflowNodeId+":"+state.toString(), experimentId);
//    	WorkflowNodeDetails details = (WorkflowNodeDetails) airavataExperimentCatalog.get(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNodeId);
//        if(details == null) {
//            details = new WorkflowNodeDetails();
//            details.setNodeInstanceId(workflowNodeId);
//        }
//        WorkflowNodeStatus status = new WorkflowNodeStatus();
//        status.setWorkflowNodeState(state);
//        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
//        details.setWorkflowNodeStatus(status);
//        airavataExperimentCatalog.update(ExperimentCatalogModelType.WORKFLOW_NODE_STATUS, status, workflowNodeId);
//    }
//
//	public void setup(Object... configurations) {
//		for (Object configuration : configurations) {
//			if (configuration instanceof ExperimentCatalog){
//				this.airavataExperimentCatalog =(ExperimentCatalog)configuration;
//			} else if (configuration instanceof LocalEventPublisher){
//				this.localEventPublisher =(LocalEventPublisher) configuration;
//			}  else if (configuration instanceof Publisher){
//                this.publisher=(Publisher) configuration;
//            }
//        }
//	}
//}
