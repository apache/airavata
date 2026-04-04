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

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.util.function.Function;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.messaging.util.RabbitMQProperties;
import org.apache.airavata.model.messaging.event.proto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMQPublisher implements Publisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);

    private final RabbitMQProperties properties;
    private final Function<MessageContext, String> routingKeySupplier;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitMQProperties properties, Function<MessageContext, String> routingKeySupplier)
            throws AiravataException {
        this.properties = properties;
        this.routingKeySupplier = routingKeySupplier;
        this.rabbitTemplate = createRabbitTemplate();
    }

    public RabbitMQPublisher(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        this.routingKeySupplier = null;
        this.rabbitTemplate = createRabbitTemplate();
    }

    private RabbitTemplate createRabbitTemplate() throws AiravataException {
        try {
            CachingConnectionFactory connectionFactory =
                    new CachingConnectionFactory(java.net.URI.create(properties.getBrokerUrl()));
            RabbitAdmin admin = new RabbitAdmin(connectionFactory);
            admin.afterPropertiesSet();

            if (properties.getExchangeName() != null) {
                TopicExchange exchange = new TopicExchange(properties.getExchangeName(), true, false);
                admin.declareExchange(exchange);
            }

            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setExchange(properties.getExchangeName());
            log.info("Connected to RabbitMQ for exchange: {}", properties.getExchangeName());
            return template;
        } catch (Exception e) {
            String msg = "RabbitMQ connection issue for exchange: " + properties.getExchangeName() + " with broker url "
                    + properties.getBrokerUrl();
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void publish(MessageContext messageContext) throws AiravataException {
        try {
            byte[] body = ((MessageLite) messageContext.getEvent()).toByteArray();
            Message message = Message.newBuilder()
                    .setEvent(ByteString.copyFrom(body))
                    .setMessageId(messageContext.getMessageId())
                    .setMessageType(messageContext.getType())
                    .setUpdatedTime(messageContext.getUpdatedTime().getTime())
                    .build();
            String routingKey = routingKeySupplier.apply(messageContext);
            send(message.toByteArray(), routingKey);
        } catch (Exception e) {
            String msg = "Error while sending to rabbitmq";
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void publish(MessageContext messageContext, String routingKey) throws AiravataException {
        try {
            byte[] body = ((MessageLite) messageContext.getEvent()).toByteArray();
            Message.Builder messageBuilder = Message.newBuilder()
                    .setEvent(ByteString.copyFrom(body))
                    .setMessageId(messageContext.getMessageId())
                    .setMessageType(messageContext.getType());
            if (messageContext.getUpdatedTime() != null) {
                messageBuilder.setUpdatedTime(messageContext.getUpdatedTime().getTime());
            }
            send(messageBuilder.build().toByteArray(), routingKey);
        } catch (Exception e) {
            String msg = "Error while sending to rabbitmq";
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    public void send(byte[] message, String routingKey) throws Exception {
        try {
            MessageProperties props = new MessageProperties();
            props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            props.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
            org.springframework.amqp.core.Message amqpMessage =
                    new org.springframework.amqp.core.Message(message, props);
            rabbitTemplate.send(properties.getExchangeName(), routingKey, amqpMessage);
        } catch (Exception e) {
            String msg = "Failed to publish message to exchange: " + properties.getExchangeName();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}
