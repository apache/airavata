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

package org.apache.airavata.gfac.notification.listeners;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.gfac.notification.events.*;
import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.common.DurationObj;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

import java.net.URI;
import java.util.Properties;

public class WorkflowTrackingListener {

    private Notifier notifier;

    private String topic;

    private URI workflowID;

    private WorkflowTrackingContext context;

    private InvocationEntity initiator;

    private InvocationEntity receiver;

    private DurationObj duration;

    private org.apache.airavata.workflow.tracking.common.InvocationContext invocationContext;

    public WorkflowTrackingListener(String workflowID, String workflowNodeID, String brokerURL, String topic){
        this.topic = topic;
        this.workflowID = URI.create(this.topic);
        this.notifier = NotifierFactory.createNotifier();
        URI initiatorWorkflowID = URI.create(workflowID);
        URI initiatorServiceID = URI.create(topic);
        String initiatorWorkflowNodeID = workflowNodeID;
        Integer initiatorWorkflowTimeStep = null;
        this.context.setTopic(topic);

        this.context = this.notifier.createTrackingContext(new Properties(), brokerURL, initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID, initiatorWorkflowTimeStep);
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



    @Subscribe
    public void startExecution(StartExecutionEvent e) {
        this.duration = this.notifier.computationStarted();
    }

    @Subscribe
    public void finishExecution(FinishExecutionEvent e) {
        this.duration = this.notifier.computationFinished(this.context, this.duration);
    }

    @Subscribe
    public void statusChanged(InvocationContext context, String... data) {
        this.notifier.info(this.context, data);
    }

    @Subscribe
    public void startSchedule(StartScheduleEvent e){
        this.notifier.info(this.context,e.getEventType());
    }

    @Subscribe
    public void executionFail(ExecutionFailEvent e) {
        this.notifier.sendingFault(this.context, this.invocationContext, e.getCauseForFailure().getMessage());
    }


    @Subscribe
    public void info(InvocationContext context, String... data) {
        this.notifier.info(this.context, data);
    }

    @Subscribe
    public void warning(InvocationContext context, String... data) {
    }

    @Subscribe
    public void exception(InvocationContext context, String... data) {
    }

    @Subscribe
    public void finishSchedule(FinishScheduleEvent e){
        this.notifier.info(this.context,e.getEventType());
    }


    public InvocationEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(InvocationEntity receiver) {
        this.receiver = receiver;
    }

}
