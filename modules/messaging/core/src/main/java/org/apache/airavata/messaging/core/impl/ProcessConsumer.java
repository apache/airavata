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
import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.ProcessTerminateEvent;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcessConsumer extends QueueingConsumer{
    private static final Logger log = LoggerFactory.getLogger(ProcessConsumer.class);

    private MessageHandler handler;
    private Channel channel;
    private Connection connection;

    public ProcessConsumer(MessageHandler messageHandler, Connection connection, Channel channel){
        super(channel);
        this.handler = messageHandler;
        this.connection = connection;
        this.channel = channel;
    }


    @Override public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties basicProperties,
                               byte[] body) throws IOException {

        Message message = new Message();

        try {
            ThriftUtils.createThriftFromBytes(body, message);
            TBase event = null;
            String gatewayId = null;
            long deliveryTag = envelope.getDeliveryTag();
            if (message.getMessageType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent processSubmitEvent = new ProcessSubmitEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), processSubmitEvent);
                log.info(" Message Received with message id '" + message.getMessageId()
                        + " and with message type:" + message.getMessageType() + ", for processId:" +
                        processSubmitEvent.getProcessId() + ", expId:" + processSubmitEvent.getExperimentId());
                event = processSubmitEvent;
                gatewayId = processSubmitEvent.getGatewayId();
                MessageContext messageContext = new MessageContext(event, message.getMessageType(),
                        message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(envelope.isRedeliver());
                handler.onMessage(messageContext);
            } else if (message.getMessageType().equals(MessageType.TERMINATEPROCESS)) {
                ProcessTerminateEvent processTerminateEvent = new ProcessTerminateEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), processTerminateEvent);
                log.info(" Message Received with message id '" + message.getMessageId()
                        + " and with message type:" + message.getMessageType() + ", for processId:" +
                        processTerminateEvent.getProcessId());
                event = processTerminateEvent;
                gatewayId = processTerminateEvent.getGatewayId();
                MessageContext messageContext = new MessageContext(event, message.getMessageType(),
                        message.getMessageId(), gatewayId, deliveryTag);
                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                messageContext.setIsRedeliver(envelope.isRedeliver());
                handler.onMessage(messageContext);
            } else {
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
