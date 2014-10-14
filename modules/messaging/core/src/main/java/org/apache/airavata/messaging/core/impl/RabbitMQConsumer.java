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
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.messaging.event.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RabbitMQConsumer implements Consumer {
    private String exchangeName;
    private String url;
    private Connection connection;
    private Channel channel;
    private String consumerTag = "hhh";
    private static Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private String routingKey;

    public RabbitMQConsumer(String brokerUrl, String exchangeName, String routingKey) {
        this.exchangeName = exchangeName;
        this.url = brokerUrl;
        this.routingKey = routingKey;
    }

    public void listen(final MessageHandler handler) throws AiravataException {
        try {
            connection = createConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, "fanout", false);
            final String queueName = channel.queueDeclare().getQueue();
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
                        ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
                        WorkflowNodeStatusChangeEvent wfnStatusChangeEvent = new WorkflowNodeStatusChangeEvent();
                        TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
                        JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
                        if (message.getMessageType().equals(MessageType.EXPERIMENT)) {
                            ThriftUtils.createThriftFromBytes(message.getEvent(), experimentStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + experimentStatusChangeEvent.getState());
                        } else if (message.getMessageType().equals(MessageType.WORKFLOWNODE)) {
                            ThriftUtils.createThriftFromBytes(message.getEvent(), wfnStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + wfnStatusChangeEvent.getState());
                        } else if (message.getMessageType().equals(MessageType.TASK)) {
                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + taskStatusChangeEvent.getState());
                        } else if (message.getMessageType().equals(MessageType.JOB)) {
                            ThriftUtils.createThriftFromBytes(message.getEvent(), jobStatusChangeEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with status " + jobStatusChangeEvent.getState());
                        }
                        handler.onMessage(message);
                    } catch (TException e) {
                        String msg = "Failed to de-serialize the thrift message, exchange: " + exchangeName + " routingKey: " + routingKey + " queue: " + queueName;
                        log.warn(msg, e);
                    }
                }
            });
        } catch (Exception e) {
            reset();
            String msg = "could not open channel for exchange " + exchangeName;
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    private void reset() {
        consumerTag = null;
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
}
