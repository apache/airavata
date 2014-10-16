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


import com.rabbitmq.client.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.Consumer;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.model.messaging.event.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RabbitMQConsumer implements Consumer {
    private static Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private String exchangeName;
    private String url;
    private Connection connection;
    private Channel channel;
    private Map<String, QueueDetails> queueDetailsMap = new HashMap<String, QueueDetails>();

    public RabbitMQConsumer(String brokerUrl, String exchangeName) throws AiravataException {
        this.exchangeName = exchangeName;
        this.url = brokerUrl;

        try {
            connection = createConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, "direct", false);
        } catch (Exception e) {
            String msg = "could not open channel for exchange " + exchangeName;
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    public String listen(final MessageHandler handler) throws AiravataException {
        try {
            Map<String, String> props = handler.getProperties();
            final String routingKey = props.get(MessagingConstants.RABBIT_ROUTING_KEY);
            if (routingKey == null) {
                throw new IllegalArgumentException("The routing key must be present");
            }

            String queueName = props.get(MessagingConstants.RABBIT_QUEUE);
            String consumerTag = props.get(MessagingConstants.RABBIT_CONSUMER_TAG);
            if (queueName == null) {
                queueName = channel.queueDeclare().getQueue();
            } else {
                channel.queueDeclare(queueName, true, false, false, null);
            }
            if (consumerTag == null) {
                consumerTag = "default";
            }
            String id = routingKey + "." + queueName;
            channel.queueBind(queueName, exchangeName, routingKey);
            channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    Message message = new Message();

                    try {
                        ThriftUtils.createThriftFromBytes(body, message);
                        TBase event = null;
                        if (message.getMessageType().equals(MessageType.EXPERIMENT)) {
                            ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), experimentStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + experimentStatusChangeEvent.getState());
                            event = experimentStatusChangeEvent;
                        } else if (message.getMessageType().equals(MessageType.WORKFLOWNODE)) {
                            WorkflowNodeStatusChangeEvent wfnStatusChangeEvent = new WorkflowNodeStatusChangeEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), wfnStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + wfnStatusChangeEvent.getState());
                            event = wfnStatusChangeEvent;
                        } else if (message.getMessageType().equals(MessageType.TASK)) {
                            TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + taskStatusChangeEvent.getState());
                            event = taskStatusChangeEvent;
                        } else if (message.getMessageType().equals(MessageType.JOB)) {
                            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), jobStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + jobStatusChangeEvent.getState());
                            event = jobStatusChangeEvent;
                        }

                        MessageContext messageContext = new MessageContext(event, message.getMessageType(), message.getMessageId());
                        handler.onMessage(messageContext);
                    } catch (TException e) {
                        String msg = "Failed to de-serialize the thrift message, exchange: " + exchangeName + " routingKey: " + routingKey;
                        log.warn(msg, e);
                    }
                }
            });
            // save the name for deleting the queue
            queueDetailsMap.put(id, new QueueDetails(queueName, routingKey));
            return id;
        } catch (Exception e) {
            String msg = "could not open channel for exchange " + exchangeName;
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    public void stopListen(final String id) throws AiravataException {
        QueueDetails details = queueDetailsMap.get(id);
        if (details != null) {
            try {
                channel.queueUnbind(details.getQueueName(), exchangeName, details.getRoutingKey());
                channel.queueDelete(details.getQueueName(), true, true);
            } catch (IOException e) {
                String msg = "could not un-bind queue: " + details.getQueueName() + " for exchange " + exchangeName;
                log.error(msg);
                throw new AiravataException(msg, e);
            }
        }
    }

    private Connection createConnection() throws IOException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(url);
            Connection connection = connectionFactory.newConnection();
            connection.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                }
            });
            log.info("connected to rabbitmq: " + connection + " for " + exchangeName);
            return connection;
        } catch (Exception e) {
            log.info("connection failed to rabbitmq: " + connection + " for " + exchangeName);
            return null;
        }
    }

    /**
     * Private class for holding some information about the consumers registered
     */
    private class QueueDetails {
        String queueName;

        String routingKey;

        private QueueDetails(String queueName, String routingKey) {
            this.queueName = queueName;
            this.routingKey = routingKey;
        }

        public String getQueueName() {
            return queueName;
        }

        public String getRoutingKey() {
            return routingKey;
        }
    }
}
