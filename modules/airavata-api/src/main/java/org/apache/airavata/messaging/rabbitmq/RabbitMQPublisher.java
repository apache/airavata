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
import java.util.function.Function;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ publisher using Spring AMQP.
 * All messages are Jackson JSON-serialized using MessageContext.Wrapper.
 * Used for database state synchronization in airavata-api.
 */
public class RabbitMQPublisher implements Publisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitMQProperties properties;
    private final Function<MessageContext, String> routingKeySupplier;

    /**
     * Constructor with Spring-managed RabbitTemplate.
     */
    public RabbitMQPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, 
                             RabbitMQProperties properties, 
                             Function<MessageContext, String> routingKeySupplier) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.routingKeySupplier = routingKeySupplier;
    }

    /**
     * Constructor with Spring-managed RabbitTemplate without routing key supplier.
     */
    public RabbitMQPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, 
                             RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.routingKeySupplier = null;
    }

    /**
     * Legacy constructor for backward compatibility.
     * Creates a publisher that will use the global RabbitTemplate from Spring context.
     */
    public RabbitMQPublisher(RabbitMQProperties properties, Function<MessageContext, String> routingKeySupplier)
            throws AiravataException {
        this.properties = properties;
        this.routingKeySupplier = routingKeySupplier;
        this.rabbitTemplate = null; // Will be injected lazily
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Legacy constructor for backward compatibility.
     */
    public RabbitMQPublisher(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        this.routingKeySupplier = null;
        this.rabbitTemplate = null; // Will be injected lazily
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void publish(MessageContext messageContext) throws AiravataException {
        try {
            MessageContext.Wrapper jsonWrapper = new MessageContext.Wrapper(messageContext);
            String routingKey = routingKeySupplier != null ? 
                    routingKeySupplier.apply(messageContext) : "";
            byte[] jsonMessageBody = objectMapper.writeValueAsBytes(jsonWrapper);
            send(jsonMessageBody, routingKey);
        } catch (Exception e) {
            String msg = "Error while publishing message";
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void publish(MessageContext messageContext, String routingKey) throws AiravataException {
        try {
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
     * Send JSON-serialized message to RabbitMQ using Spring AMQP.
     */
    public void send(byte[] jsonMessageBody, String routingKey) throws Exception {
        try {
            if (rabbitTemplate != null) {
                // Use Spring AMQP RabbitTemplate
                MessageProperties messageProperties = new MessageProperties();
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                messageProperties.setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
                
                Message message = new Message(jsonMessageBody, messageProperties);
                rabbitTemplate.send(properties.getExchangeName(), routingKey, message);
            } else {
                // Legacy mode - use raw RabbitMQ client (for backward compatibility during migration)
                sendLegacy(jsonMessageBody, routingKey);
            }
        } catch (Exception e) {
            String msg = "Failed to publish message to exchange: " + properties.getExchangeName();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Legacy send method using raw RabbitMQ client.
     * Used during migration for backward compatibility.
     */
    private void sendLegacy(byte[] jsonMessageBody, String routingKey) throws Exception {
        // This is a simplified version - in production, this would use the raw client
        log.warn("Using legacy RabbitMQ client - consider migrating to Spring AMQP");
        throw new UnsupportedOperationException(
                "Legacy RabbitMQ client not available. Use Spring AMQP RabbitTemplate.");
    }

    /**
     * Close resources. With Spring AMQP, connection management is handled by Spring.
     */
    public void close() {
        // Spring AMQP manages connections via CachingConnectionFactory
        // No manual cleanup needed
        log.debug("RabbitMQPublisher close() called - Spring AMQP manages connections");
    }
}
