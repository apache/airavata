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
package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.messaging.event.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExperimentConsumer extends QueueingConsumer {
    private static final Logger log = LoggerFactory.getLogger(ExperimentConsumer.class);

    private MessageHandler handler;
    private Channel channel;
    private Connection connection;

    public ExperimentConsumer(MessageHandler messageHandler, Connection connection, Channel channel) {
        super(channel);
        this.handler = messageHandler;
        this.connection = connection;
        this.channel = channel;
    }


    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        Message message = new Message();

        try {
            ThriftUtils.createThriftFromBytes(body, message);
            long deliveryTag = envelope.getDeliveryTag();

            TBase event = null;
            String gatewayId = null;
            if (message.getMessageType() == MessageType.EXPERIMENT ||
                    message.getMessageType() == MessageType.EXPERIMENT_CANCEL) {

                ExperimentSubmitEvent experimentEvent = new ExperimentSubmitEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), experimentEvent);
                log.info(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId:" +
                        " " +
                        experimentEvent.getExperimentId());
                event = experimentEvent;
                gatewayId = experimentEvent.getGatewayId();
                MessageContext messageContext = new MessageContext(event, message.getMessageType(),
                        message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(envelope.isRedeliver());
                handler.onMessage(messageContext);

            } else if (message.getMessageType() == MessageType.INTERMEDIATE_OUTPUTS) {

                ExperimentIntermediateOutputsEvent intermediateOutEvt = new ExperimentIntermediateOutputsEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), intermediateOutEvt);
                log.info(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId:" +
                        " " +
                        intermediateOutEvt.getExperimentId());
                event = intermediateOutEvt;
                gatewayId = intermediateOutEvt.getGatewayId();
                MessageContext messageContext = new MessageContext(event, message.getMessageType(),
                        message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(envelope.isRedeliver());
                handler.onMessage(messageContext);

            }else {
                log.error("{} message type is not handle in ProcessLaunch Subscriber. Sending ack for " +
                        "delivery tag {} ", message.getMessageType().name(), deliveryTag);
                sendAck(deliveryTag);
            }
        } catch (TException e) {
            String msg = "Failed to de-serialize the thrift message, from routing keys:" + envelope.getRoutingKey();
            log.warn(msg, e);
        }

    }


    private void sendAck(long deliveryTag){
        try {
            if (channel.isOpen()){
                channel.basicAck(deliveryTag,false);
            }else {
                channel = connection.createChannel();
                channel.basicQos(ServerSettings.getRabbitmqPrefetchCount());
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
