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
package org.apache.airavata.xbaya.messaging;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.WorkflowIdentifier;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;

import java.util.Date;

public class EventData {

    private MessageContext  messageEvent;

    private Date updateDate;

    private String status;
    private String experimentId;
    private String workflowNodeId;

    private String message;
    private String messageId;

    /**
     * Constructs a MonitorEvent.
     * 
     * @param event
     */
    public EventData(MessageContext event) {
        this.messageEvent = event;
        process(event);
    }

    private void process(MessageContext event) {
        this.messageId = event.getMessageId();
        if (event.getType() == MessageType.EXPERIMENT) {
            ExperimentStatusChangeEvent experimentStatusChangeEvent = (ExperimentStatusChangeEvent) event.getEvent();
            this.status = experimentStatusChangeEvent.getState().toString();
            this.experimentId = experimentStatusChangeEvent.getExperimentId();
            this.workflowNodeId = "";
            this.message = "Received experiment event , expId : " + experimentStatusChangeEvent.getExperimentId() +
                    ", status : " + experimentStatusChangeEvent.getState().toString();
        } else if (event.getType() == MessageType.WORKFLOWNODE) {
            WorkflowNodeStatusChangeEvent wfnStatusChangeEvent = (WorkflowNodeStatusChangeEvent) event.getEvent();
            WorkflowIdentifier wfIdentifier = wfnStatusChangeEvent.getWorkflowNodeIdentity();
            this.status = wfnStatusChangeEvent.getState().toString();
            this.experimentId = wfIdentifier.getExperimentId();
            this.workflowNodeId = wfIdentifier.getWorkflowNodeId();
            this.message = "Received workflow status change event, expId : " + wfIdentifier.getExperimentId() +
                    ", nodeId : " + wfIdentifier.getWorkflowNodeId() + " , status : " + wfnStatusChangeEvent.getState().toString();

        } else if (event.getType() == MessageType.TASK) {
            TaskStatusChangeEvent taskStatusChangeEvent = (TaskStatusChangeEvent) event.getEvent();
            TaskIdentifier taskIdentifier = taskStatusChangeEvent.getTaskIdentity();
            this.status = taskStatusChangeEvent.getState().toString();
            this.experimentId = taskIdentifier.getExperimentId();
            this.workflowNodeId = taskIdentifier.getWorkflowNodeId();
            this.message = "Received task event , expId : " + taskIdentifier.getExperimentId() + ",taskId : " +
                    taskIdentifier.getTaskId() + ", wfNodeId : " + taskIdentifier.getWorkflowNodeId() + ", status : " +
                    taskStatusChangeEvent.getState().toString();
        } else if (event.getType() == MessageType.JOB) {
            JobStatusChangeEvent jobStatusChangeEvent = (JobStatusChangeEvent) event.getEvent();
            JobIdentifier jobIdentifier = jobStatusChangeEvent.getJobIdentity();
            this.status = jobStatusChangeEvent.getState().toString();
            this.experimentId = jobIdentifier.getExperimentId();
            this.workflowNodeId = jobIdentifier.getWorkflowNodeId();
            this.message = "Received task event , expId : " + jobIdentifier.getExperimentId() + " ,taskId : " +
                    jobIdentifier.getTaskId() + ", wfNodeId : " + jobIdentifier.getWorkflowNodeId() + ", status : " +
                    jobStatusChangeEvent.getState().toString();
        }
    }

    /**
     * Returns the event.
     * 
     * @return The event
     */
    public MessageContext getEvent() {
        return this.messageEvent;
    }

    /**
     * Returns the type.
     * 
     * @return The type
     */
    public MessageType getType() {
        return this.messageEvent.getType();
    }

	public Date getUpdateTime() {
        if (updateDate == null) {
            updateDate = new Date(this.messageEvent.getUpdatedTime().getTime());
        }
        return updateDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getWorkflowNodeId() {
        return workflowNodeId;
    }
}