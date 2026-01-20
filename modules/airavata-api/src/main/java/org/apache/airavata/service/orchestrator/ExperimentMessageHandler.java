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
package org.apache.airavata.service.orchestrator;

import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.model.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.common.model.ExperimentSubmitEvent;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.MessageHandler;
import org.apache.airavata.dapr.messaging.Subscriber;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.registry.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Message handler for experiment-related Dapr Pub/Sub messages.
 *
 * <p>Handles EXPERIMENT, EXPERIMENT_CANCEL, and INTERMEDIATE_OUTPUTS message types.
 * Extracted from OrchestratorService to follow consistent message handler patterns.
 */
public class ExperimentMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentMessageHandler.class);

    private final OrchestratorService orchestratorService;
    private Subscriber subscriber;

    public ExperimentMessageHandler(OrchestratorService orchestratorService, Subscriber subscriber) {
        this.orchestratorService = orchestratorService;
        this.subscriber = subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        MDC.put(MDCConstants.GATEWAY_ID, messageContext.getGatewayId());
        try {
            switch (messageContext.getType()) {
                case EXPERIMENT -> launchExperiment(messageContext);
                case EXPERIMENT_CANCEL -> cancelExperiment(messageContext);
                case INTERMEDIATE_OUTPUTS -> handleIntermediateOutputsEvent(messageContext);
                default -> {
                    subscriber.sendAck(messageContext.getDeliveryTag());
                    logger.error("Orchestrator got unsupported message type: {}", messageContext.getType());
                }
            }
        } finally {
            MDC.clear();
        }
    }

    private void launchExperiment(MessageContext messageContext) {
        try {
            orchestratorService.handleLaunchExperimentFromMessage(messageContext);
        } catch (Exception e) {
            logger.error("Error while launching experiment", e);
        } finally {
            subscriber.sendAck(messageContext.getDeliveryTag());
            MDC.clear();
        }
    }

    private void cancelExperiment(MessageContext messageContext) {
        try {
            ExperimentSubmitEvent expEvent = (ExperimentSubmitEvent) messageContext.getEvent();
            logger.info(
                    "Cancelling experiment with experimentId: {} gateway Id: {}",
                    expEvent.getExperimentId(),
                    expEvent.getGatewayId());
            orchestratorService.handleCancelExperiment(expEvent);
        } catch (RegistryException | OrchestratorException e) {
            String msg = String.format(
                    "Error while cancelling experiment: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                    messageContext.getGatewayId(),
                    messageContext.getDeliveryTag(),
                    messageContext.isRedeliver(),
                    e.getMessage());
            logger.error(msg, e);
            // Log but don't throw - we need to ack the message
        } catch (Exception e) {
            String msg = String.format(
                    "Error while cancelling experiment: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                    messageContext.getGatewayId(),
                    messageContext.getDeliveryTag(),
                    messageContext.isRedeliver(),
                    e.getMessage());
            logger.error(msg, e);
            // Log but don't throw - we need to ack the message
        } finally {
            subscriber.sendAck(messageContext.getDeliveryTag());
        }
    }

    private void handleIntermediateOutputsEvent(MessageContext messageContext) {
        try {
            ExperimentIntermediateOutputsEvent event = (ExperimentIntermediateOutputsEvent) messageContext.getEvent();
            logger.info(
                    "INTERMEDIATE_OUTPUTS event for experimentId: {} gateway Id: {} outputs: {}",
                    event.getExperimentId(),
                    event.getGatewayId(),
                    event.getOutputNames());
            orchestratorService.handleIntermediateOutputsEvent(event);
        } catch (OrchestratorException e) {
            String msg = String.format(
                    "Error while fetching intermediate outputs: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                    messageContext.getGatewayId(),
                    messageContext.getDeliveryTag(),
                    messageContext.isRedeliver(),
                    e.getMessage());
            logger.error(msg, e);
            // Log but don't throw - we need to ack the message
        } catch (Exception e) {
            String msg = String.format(
                    "Error while fetching intermediate outputs: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                    messageContext.getGatewayId(),
                    messageContext.getDeliveryTag(),
                    messageContext.isRedeliver(),
                    e.getMessage());
            logger.error(msg, e);
            // Log but don't throw - we need to ack the message
        } finally {
            subscriber.sendAck(messageContext.getDeliveryTag());
        }
    }
}
