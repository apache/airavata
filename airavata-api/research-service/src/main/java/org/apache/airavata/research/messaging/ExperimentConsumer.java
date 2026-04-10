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
package org.apache.airavata.research.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessageHandler;
import org.apache.airavata.model.messaging.event.proto.*;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListener;

public class ExperimentConsumer implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(ExperimentConsumer.class);

    private final MessageHandler handler;

    public ExperimentConsumer(MessageHandler messageHandler) {
        this.handler = messageHandler;
    }

    @Override
    public void onMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            byte[] body = amqpMessage.getBody();
            boolean isRedeliver =
                    Boolean.TRUE.equals(amqpMessage.getMessageProperties().getRedelivered());
            long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

            Message message = Message.parseFrom(body);
            Object event = null;
            String gatewayId = null;

            if (message.getMessageType() == MessageType.EXPERIMENT
                    || message.getMessageType() == MessageType.EXPERIMENT_CANCEL) {

                ExperimentSubmitEvent experimentEvent = ExperimentSubmitEvent.parseFrom(message.getEvent());
                log.info(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId:" + " "
                        + experimentEvent.getExperimentId());
                event = experimentEvent;
                gatewayId = experimentEvent.getGatewayId();
                MessageContext messageContext = new MessageContext(
                        event, message.getMessageType(), message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(isRedeliver);
                handler.onMessage(messageContext);

            } else if (message.getMessageType() == MessageType.INTERMEDIATE_OUTPUTS) {

                ExperimentIntermediateOutputsEvent intermediateOutEvt =
                        ExperimentIntermediateOutputsEvent.parseFrom(message.getEvent());
                log.info(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId:" + " "
                        + intermediateOutEvt.getExperimentId());
                event = intermediateOutEvt;
                gatewayId = intermediateOutEvt.getGatewayId();
                MessageContext messageContext = new MessageContext(
                        event, message.getMessageType(), message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(isRedeliver);
                handler.onMessage(messageContext);

            } else {
                log.error(
                        "{} message type is not handled in Experiment Subscriber. Delivery tag: {}",
                        message.getMessageType().name(),
                        deliveryTag);
            }
        } catch (InvalidProtocolBufferException e) {
            String msg = "Failed to de-serialize the proto message";
            log.warn(msg, e);
        }
    }
}
