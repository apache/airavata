/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.workflow;

import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessSubmitEvent;
import org.apache.airavata.common.model.ProcessTerminateEvent;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.MessageHandler;
import org.apache.airavata.dapr.messaging.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message handler for process launch and termination events.
 *
 * <p>Handles LAUNCHPROCESS and TERMINATEPROCESS message types from the process-topic.
 * Extracted from PreWorkflowManager to follow consistent message handler patterns.
 */
public class ProcessLaunchMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessLaunchMessageHandler.class);

    private final PreWorkflowManager preWorkflowManager;
    private Subscriber subscriber;

    public ProcessLaunchMessageHandler(PreWorkflowManager preWorkflowManager, Subscriber subscriber) {
        this.preWorkflowManager = preWorkflowManager;
        this.subscriber = subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        logger.info(
                "Message Received with message id {} and with message type: {}",
                messageContext.getMessageId(),
                messageContext.getType());

        if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
            handleLaunchProcess(messageContext);
        } else if (messageContext.getType().equals(MessageType.TERMINATEPROCESS)) {
            handleTerminateProcess(messageContext);
        } else {
            logger.warn("Unknown message type: {}", messageContext.getType());
            subscriber.sendAck(messageContext.getDeliveryTag());
        }
    }

    private void handleLaunchProcess(MessageContext messageContext) {
        ProcessSubmitEvent event;
        try {
            event = (ProcessSubmitEvent) messageContext.getEvent();
        } catch (Exception e) {
            logger.error("Failed to fetch process submit event", e);
            subscriber.sendAck(messageContext.getDeliveryTag());
            return;
        }

        String processId = event.getProcessId();
        String experimentId = event.getExperimentId();
        String gateway = event.getGatewayId();

        logger.info(
                "Received process launch message for process {} of experiment {} in gateway {}",
                processId,
                experimentId,
                gateway);

        try {
            logger.info(
                    "Launching the pre workflow for process {} of experiment {} in gateway {}",
                    processId,
                    experimentId,
                    gateway);
            String workflowName = preWorkflowManager.createAndLaunchPreWorkflow(processId, false);
            logger.info(
                    "Completed launching the pre workflow {} for process {} of experiment {} in gateway {}",
                    workflowName,
                    processId,
                    experimentId,
                    gateway);

            // updating the process status
            ProcessStatus status = new ProcessStatus();
            status.setState(ProcessState.STARTED);
            status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
            preWorkflowManager.publishProcessStatus(processId, experimentId, gateway, ProcessState.STARTED);
            subscriber.sendAck(messageContext.getDeliveryTag());
        } catch (Exception e) {
            logger.error("Failed to launch the pre workflow for process {} in gateway {}", processId, gateway, e);
            // Don't ack on error - let Dapr retry
        }
    }

    private void handleTerminateProcess(MessageContext messageContext) {
        ProcessTerminateEvent event;
        try {
            event = (ProcessTerminateEvent) messageContext.getEvent();
        } catch (Exception e) {
            logger.error("Failed to fetch process cancellation event", e);
            subscriber.sendAck(messageContext.getDeliveryTag());
            return;
        }

        String processId = event.getProcessId();
        String gateway = event.getGatewayId();

        logger.info("Received process cancel message for process {} in gateway {}", processId, gateway);

        try {
            logger.info("Launching the process cancel workflow for process {} in gateway {}", processId, gateway);
            String workflowName = preWorkflowManager.createAndLaunchCancelWorkflow(processId, gateway);
            logger.info(
                    "Completed process cancel workflow {} for process {} in gateway {}",
                    workflowName,
                    processId,
                    gateway);
            subscriber.sendAck(messageContext.getDeliveryTag());
        } catch (Exception e) {
            logger.error(
                    "Failed to launch process cancel workflow for process {} in gateway {}", processId, gateway, e);
            // Don't ack on error - let Dapr retry
        }
    }
}
