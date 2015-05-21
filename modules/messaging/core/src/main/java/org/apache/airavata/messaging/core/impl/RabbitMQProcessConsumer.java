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

package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class RabbitMQProcessConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQProcessConsumer.class);

    private String url;
    private Connection connection;
    private Channel channel;

    public RabbitMQProcessConsumer() throws AiravataException {
        try {
            url = ServerSettings.getSetting(MessagingConstants.RABBITMQ_BROKER_URL);
            createConnection();
        } catch (ApplicationSettingsException e) {
            String message = "Failed to get read the required properties from airavata to initialize rabbitmq";
            log.error(message, e);
            throw new AiravataException(message, e);
        }
    }

    private void createConnection() throws AiravataException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(url);
            connectionFactory.setAutomaticRecoveryEnabled(true);
            connection = connectionFactory.newConnection();
            connection.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                }
            });
            log.info("connected to rabbitmq: " + connection + " for default");

            channel = connection.createChannel();
            channel.basicQos(MessagingConstants.PREFETCH_COUNT);
//            channel.exchangeDeclare(taskLaunchExchangeName, "fanout");

        } catch (Exception e) {
            String msg = "could not open channel for exchange default";
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }


    public String listen(final MessageHandler handler) throws AiravataException {
        try {
            Map<String, Object> props = handler.getProperties();
            final Object routing = props.get(MessagingConstants.RABBIT_ROUTING_KEY);
            if (routing == null) {
                throw new IllegalArgumentException("The routing key must be present");
            }
            String queueName = (String) props.get(MessagingConstants.RABBIT_QUEUE);
            String consumerTag = (String) props.get(MessagingConstants.RABBIT_CONSUMER_TAG);
            if (queueName == null) {
                if (!channel.isOpen()) {
                    channel = connection.createChannel();
                    channel.basicQos(MessagingConstants.PREFETCH_COUNT);
//                    channel.exchangeDeclare(taskLaunchExchangeName, "fanout");
                }
                queueName = channel.queueDeclare().getQueue();
            } else {
                channel.queueDeclare(queueName, true, false, false, null);
            }

            if (consumerTag == null) {
                consumerTag = "default";
            }
            // autoAck=false, we will ack after task is done
            final String finalQueueName = queueName;
            channel.basicConsume(queueName, true, new QueueingConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    Message message = new Message();

                    try {
                        ThriftUtils.createThriftFromBytes(body, message);
                        TBase event = null;
                        String gatewayId = null;
                        ProcessSubmitEvent processSubmitEvent = new ProcessSubmitEvent();
                        ThriftUtils.createThriftFromBytes(message.getEvent(), processSubmitEvent);
                        log.debug("Message received with message id : " + message.getMessageId()
                                + " with task id : " + processSubmitEvent.getTaskId());
                        event = processSubmitEvent;
                        MessageContext messageContext = new MessageContext(event, message.getMessageType(), message.getMessageId(), null);
                        messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                        handler.onMessage(messageContext);
                    } catch (TException e) {
                        String msg = "Failed to de-serialize the thrift message, from routing keys and queueName " + finalQueueName;
                        log.warn(msg, e);
                    }
                }
            });
            return "";
        } catch (Exception e) {
            String msg = "could not open channel for exchange default";
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    public void stopListen(final String queueName , final String exchangeName) throws AiravataException {
        try {
            channel.queueUnbind(queueName, exchangeName, null);
        } catch (IOException e) {
            String msg = "could not un-bind queue: " + queueName + " for exchange " + exchangeName;
            log.debug(msg);
        }
    }

}
