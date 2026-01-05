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
package org.apache.airavata.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.util.function.Function;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RabbitMQ publisher for airavata-api.
 * All messages are Jackson JSON-serialized using MessageContext.Wrapper (never Thrift).
 * Used exclusively for database state synchronization in airavata-api.
 */
public class RabbitMQPublisher implements Publisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final RabbitMQProperties properties;
    private final Function<MessageContext, String> routingKeySupplier;
    private Connection connection;
    private ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

    public RabbitMQPublisher(RabbitMQProperties properties, Function<MessageContext, String> routingKeySupplier)
            throws AiravataException {
        this.properties = properties;
        this.routingKeySupplier = routingKeySupplier;
        // Lazy initialization - connect on first use instead of in constructor
    }

    public RabbitMQPublisher(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        routingKeySupplier = null;
        // Lazy initialization - connect on first use instead of in constructor
    }

    private void connect() throws AiravataException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(properties.getBrokerUrl());
            connectionFactory.setAutomaticRecoveryEnabled(properties.isAutoRecoveryEnable());
            connection = connectionFactory.newConnection();
            connection.addShutdownListener(cause -> {
                log.warn(
                        "RabbitMQ connection {} for exchange={} is now shutdown. cause={}",
                        connection.getId(),
                        properties.getExchangeName(),
                        cause);
            });
            log.info("connected to rabbitmq: " + connection + " for " + properties.getExchangeName());
        } catch (Exception e) {
            String msg = "RabbitMQ connection issue for exchange : " + properties.getExchangeName()
                    + " with broker url " + properties.getBrokerUrl();
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void publish(MessageContext messageContext) throws AiravataException {
        try {
            // Lazy connection initialization
            if (connection == null) {
                connect();
            }
            // Serialize MessageContext to JSON using MessageContext.Wrapper (Jackson serialization, never Thrift)
            // All RabbitMQ messages in airavata-api use Jackson JSON serialization
            MessageContext.Wrapper jsonWrapper = new MessageContext.Wrapper(messageContext);
            String routingKey = routingKeySupplier.apply(messageContext);
            byte[] jsonMessageBody = objectMapper.writeValueAsBytes(jsonWrapper);
            send(jsonMessageBody, routingKey);
        } catch (Exception e) {
            String msg = "Error while publishing message";
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    /**
     * This method is used only for publishing DB Events to RabbitMQ.
     * All messages are Jackson JSON-serialized (never Thrift).
     *
     * @param messageContext object of message context which will include actual db event and other information.
     *                       The MessageContext and its event are domain models that will be JSON-serialized.
     * @param routingKey routing key for the message
     * @throws AiravataException
     */
    @Override
    public void publish(MessageContext messageContext, String routingKey) throws AiravataException {
        try {
            // Lazy connection initialization
            if (connection == null) {
                connect();
            }
            // Serialize MessageContext to JSON using MessageContext.Wrapper (Jackson serialization, never Thrift)
            // All RabbitMQ messages in airavata-api use Jackson JSON serialization
            MessageContext.Wrapper jsonWrapper = new MessageContext.Wrapper(messageContext);
            byte[] jsonMessageBody = objectMapper.writeValueAsBytes(jsonWrapper);
            send(jsonMessageBody, routingKey);
        } catch (Exception e) {
            String msg = "Error while publishing message";
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    /**
     * Send JSON-serialized message to RabbitMQ.
     * @param jsonMessageBody JSON-serialized message bytes (Jackson, never Thrift)
     * @param routingKey routing key for the message
     */
    public void send(byte[] jsonMessageBody, String routingKey) throws Exception {
        try {
            // Lazy connection initialization
            if (connection == null) {
                connect();
            }
            if (channelThreadLocal.get() == null) {
                log.info("Creating the channel for thread "
                        + Thread.currentThread().getName() + " " + toString());
                Channel channel = connection.createChannel();
                if (properties.getPrefetchCount() > 0) {
                    channel.basicQos(properties.getPrefetchCount());
                }

                if (properties.getExchangeName() != null) {
                    channel.exchangeDeclare(
                            properties.getExchangeName(), properties.getExchangeType(), true); // durable
                }
                channelThreadLocal.set(channel);
            }
            channelThreadLocal
                    .get()
                    .basicPublish(
                            properties.getExchangeName(),
                            routingKey,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            jsonMessageBody);
        } catch (IOException e) {
            String msg = "Failed to publish message to exchange: " + properties.getExchangeName();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}
