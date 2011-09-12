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

package org.apache.airavata.core.gfac.notification.impl;

import java.net.URI;
import java.util.Properties;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.notification.Notifiable;
import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.common.DurationObj;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

/**
 * Workflow Tracking notification 
 * TODO:: implement properly
 */
public class WorkflowTrackingNotification implements Notifiable {

    private Notifier notifier;

    private String topic;

    private URI workflowID;

    private WorkflowTrackingContext context;

    private InvocationEntity initiator;

    private InvocationEntity receiver;

    private DurationObj duration;

    private org.apache.airavata.workflow.tracking.common.InvocationContext invocationContext;

    public WorkflowTrackingNotification(String brokerURL, String topic) {
        this.topic = topic;
        this.workflowID = URI.create(this.topic);
        Properties props = new Properties();

        this.notifier = NotifierFactory.createNotifier();
        URI initiatorWorkflowID = null;
        URI initiatorServiceID = URI.create(topic);
        String initiatorWorkflowNodeID = null;
        Integer initiatorWorkflowTimeStep = null;
        this.context = this.notifier.createTrackingContext(props, brokerURL, initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID, initiatorWorkflowTimeStep);
        this.context.setTopic(topic);
        this.initiator = this.notifier.createEntity(initiatorWorkflowID, initiatorServiceID, initiatorWorkflowNodeID,
                initiatorWorkflowTimeStep);

        URI receiverWorkflowID = this.workflowID;
        URI receiverServiceID = this.workflowID;
        String receiverWorkflowNodeID = null;
        Integer receiverWorkflowTimeStep = null;
        setReceiver(this.notifier.createEntity(receiverWorkflowID, receiverServiceID, receiverWorkflowNodeID,
                receiverWorkflowTimeStep));

        // send start workflow
        this.invocationContext = this.notifier.workflowInvoked(this.context, this.initiator);
    }

    public void startSchedule(Object notifier, InvocationContext context) {
    }

    public void finishSchedule(Object notifier, InvocationContext context) {
    }

    public void input(Object notifier, InvocationContext context, String... data) {
    }

    public void output(Object notifier, InvocationContext context, String... data) {
    }

    public void startExecution(Object notifier, InvocationContext context) {
        this.duration = this.notifier.computationStarted();
    }

    public void applicationInfo(Object notifier, InvocationContext context, String... data) {
    }

    public void finishExecution(Object notifier, InvocationContext context) {
        this.duration = this.notifier.computationFinished(this.context, this.duration);
    }

    public void statusChanged(Object notifier, InvocationContext context, String... data) {
        this.notifier.info(this.context, data);
    }

    public void executionFail(Object notifier, InvocationContext context, Exception e, String... data) {
        this.notifier.sendingFault(this.context, this.invocationContext, data);
    }

    public void debug(Object notifier, InvocationContext context, String... data) {
    }

    public void info(Object notifier, InvocationContext context, String... data) {
        this.notifier.info(this.context, data);
    }

    public void warning(Object notifier, InvocationContext context, String... data) {
    }

    public void exception(Object notifier, InvocationContext context, String... data) {
    }

	public InvocationEntity getReceiver() {
		return receiver;
	}

	public void setReceiver(InvocationEntity receiver) {
		this.receiver = receiver;
	}

}
