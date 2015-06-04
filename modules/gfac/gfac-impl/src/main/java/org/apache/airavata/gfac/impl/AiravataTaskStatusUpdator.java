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
package org.apache.airavata.gfac.impl;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class AiravataTaskStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataTaskStatusUpdator.class);
    private ExperimentCatalog airavataExperimentCatalog;
    private MonitorPublisher monitorPublisher;
    private Publisher publisher;
    
    public ExperimentCatalog getAiravataExperimentCatalog() {
        return airavataExperimentCatalog;
    }

    public void setAiravataExperimentCatalog(ExperimentCatalog airavataExperimentCatalog) {
        this.airavataExperimentCatalog = airavataExperimentCatalog;
    }

    @Subscribe
    public void setupTaskStatus(TaskStatusChangeRequestEvent taskStatus) throws Exception{
    	try {
			updateTaskStatus(taskStatus.getTaskIdentity().getTaskId(), taskStatus.getState());
            logger.debug("expId - {}: Publishing task status for " + taskStatus.getTaskIdentity().getTaskId() + ":"
                    + taskStatus.getState().toString(), taskStatus.getTaskIdentity().getExperimentId());
            TaskStatusChangeEvent event = new TaskStatusChangeEvent(taskStatus.getState(), taskStatus.getTaskIdentity());
            monitorPublisher.publish(event);
            String messageId = AiravataUtils.getId("TASK");
            MessageContext msgCntxt = new MessageContext(event, MessageType.TASK, messageId, taskStatus.getTaskIdentity().getGatewayId());
            msgCntxt.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(msgCntxt);
		} catch (Exception e) {
            String msg = "Error persisting data task status to database...";
            logger.error(msg + e.getLocalizedMessage(), e);
            throw new Exception(msg, e);
		}
    }

    @Subscribe
    public void setupTaskStatus(JobStatusChangeEvent jobStatus) throws Exception{
    	TaskState state=TaskState.UNKNOWN;
    	switch(jobStatus.getState()){
    	case ACTIVE:
    		state=TaskState.EXECUTING; break;
    	case CANCELED:
    		state=TaskState.CANCELED; break;
    	case COMPLETE: case FAILED:
    		state=TaskState.POST_PROCESSING; break;
    	case HELD: case SUSPENDED: case QUEUED:
    		state=TaskState.WAITING; break;
    	case SETUP:
    		state=TaskState.PRE_PROCESSING; break;
    	case SUBMITTED:
    		state=TaskState.STARTED; break;
    	case UN_SUBMITTED:
    		state=TaskState.CANCELED; break;
    	case CANCELING:
    		state=TaskState.CANCELING; break;
		default:
			return;
    	}
    	try {
			updateTaskStatus(jobStatus.getJobIdentity().getTaskId(), state);
            logger.debug("expId - {}: Publishing task status for " + jobStatus.getJobIdentity().getTaskId() + ":"
                    + state.toString(), jobStatus.getJobIdentity().getExperimentId());
            TaskIdentifier taskIdentity = new TaskIdentifier(jobStatus.getJobIdentity().getTaskId(),
                                                         jobStatus.getJobIdentity().getWorkflowNodeId(),
                                                         jobStatus.getJobIdentity().getExperimentId(),
                                                         jobStatus.getJobIdentity().getGatewayId());
            TaskStatusChangeEvent event = new TaskStatusChangeEvent(state, taskIdentity);
            monitorPublisher.publish(event);
            String messageId = AiravataUtils.getId("TASK");
            MessageContext msgCntxt = new MessageContext(event, MessageType.TASK, messageId,jobStatus.getJobIdentity().getGatewayId());
            msgCntxt.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(msgCntxt);

        }  catch (Exception e) {
            logger.error("expId - " + jobStatus.getJobIdentity().getExperimentId() + ": Error persisting data" + e.getLocalizedMessage(), e);
            throw new Exception("Error persisting task status..", e);
		}
    }
    
    public  TaskState updateTaskStatus(String taskId, TaskState state) throws Exception {
    	TaskDetails details = (TaskDetails) airavataExperimentCatalog.get(ExperimentCatalogModelType.TASK_DETAIL, taskId);
        if(details == null) {
            logger.error("Task details cannot be null at this point");
            throw new Exception("Task details cannot be null at this point");
        }
        org.apache.airavata.model.workspace.experiment.TaskStatus status = new org.apache.airavata.model.workspace.experiment.TaskStatus();
        if(!TaskState.CANCELED.equals(details.getTaskStatus().getExecutionState())
                && !TaskState.CANCELING.equals(details.getTaskStatus().getExecutionState())){
            status.setExecutionState(state);
        }else{
            status.setExecutionState(details.getTaskStatus().getExecutionState());
        }
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setTaskStatus(status);
        logger.debug("Updating task status for "+taskId+":"+details.getTaskStatus().toString());

        airavataExperimentCatalog.update(ExperimentCatalogModelType.TASK_STATUS, status, taskId);
        return status.getExecutionState();
    }

	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof ExperimentCatalog){
				this.airavataExperimentCatalog =(ExperimentCatalog)configuration;
			} else if (configuration instanceof MonitorPublisher){
				this.monitorPublisher=(MonitorPublisher) configuration;
			} else if (configuration instanceof Publisher){
                this.publisher=(Publisher) configuration;
            }
        }
	}


    @Subscribe
    public void taskOutputChanged(TaskOutputChangeEvent taskOutputEvent) throws AiravataException {
        String taskId = taskOutputEvent.getTaskIdentity().getTaskId();
        logger.debug("Task Output changed event received for workflow node : " +
                taskOutputEvent.getTaskIdentity().getWorkflowNodeId() + ", task : " + taskId);
        // TODO - do we need to update the output to the registry? , we do it in the workflowInterpreter too.
        MessageContext messageContext = new MessageContext(taskOutputEvent, MessageType.TASKOUTPUT, taskOutputEvent.getTaskIdentity().getTaskId(), taskOutputEvent.getTaskIdentity().getGatewayId());
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        publisher.publish(messageContext);
    }
}
