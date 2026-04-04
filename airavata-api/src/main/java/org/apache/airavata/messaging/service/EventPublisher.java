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
package org.apache.airavata.messaging.service;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.model.messaging.event.proto.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.model.messaging.event.proto.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final Publisher statusPublisher;
    private final Publisher experimentPublisher;

    public EventPublisher(Publisher statusPublisher, Publisher experimentPublisher) {
        this.statusPublisher = statusPublisher;
        this.experimentPublisher = experimentPublisher;
    }

    public void publishExperimentStatus(String experimentId, String gatewayId, ExperimentState state) {
        if (statusPublisher == null) return;
        try {
            ExperimentStatusChangeEvent event = ExperimentStatusChangeEvent.newBuilder()
                    .setState(state)
                    .setExperimentId(experimentId)
                    .setGatewayId(gatewayId)
                    .build();
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment status event for {}", experimentId, e);
        }
    }

    public void publishExperimentLaunch(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = ExperimentSubmitEvent.newBuilder()
                    .setExperimentId(experimentId)
                    .setGatewayId(gatewayId)
                    .build();
            MessageContext messageContext =
                    new MessageContext(event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment launch event for {}", experimentId, e);
        }
    }

    public void publishExperimentCancel(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = ExperimentSubmitEvent.newBuilder()
                    .setExperimentId(experimentId)
                    .setGatewayId(gatewayId)
                    .build();
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT_CANCEL, "CANCEL.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment cancel event for {}", experimentId, e);
        }
    }

    public void publishIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames) {
        if (experimentPublisher == null) return;
        try {
            ExperimentIntermediateOutputsEvent event = ExperimentIntermediateOutputsEvent.newBuilder()
                    .setExperimentId(experimentId)
                    .setGatewayId(gatewayId)
                    .addAllOutputNames(outputNames)
                    .build();
            MessageContext messageContext = new MessageContext(
                    event,
                    MessageType.INTERMEDIATE_OUTPUTS,
                    "INTERMEDIATE_OUTPUTS.EXP-" + UUID.randomUUID(),
                    gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish intermediate outputs event for {}", experimentId, e);
        }
    }
}
