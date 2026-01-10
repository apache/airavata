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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.messaging.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/**
 * RabbitMQ subscriber using Spring AMQP.
 * All messages are Jackson JSON-deserialized using MessageContext.Wrapper.
 * Used for database state synchronization in airavata-api.
 */
public class RabbitMQSubscriber implements Subscriber {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQSubscriber.class);

    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitMQProperties properties;
    private final Map<String, ListenerDetail> listenerMap = new HashMap<>();

    /**
     * Constructor with Spring-managed ConnectionFactory.
     */
    public RabbitMQSubscriber(ConnectionFactory connectionFactory, RabbitMQProperties properties) {
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.properties = properties;
    }

    /**
     * Legacy constructor for backward compatibility.
     */
    public RabbitMQSubscriber(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        this.connectionFactory = null;
        this.rabbitAdmin = null;
        log.warn("Using legacy RabbitMQSubscriber constructor - Spring AMQP features not available");
    }

    @Override
    public String listen(BiFunction<Connection, Channel, Consumer> supplier, String queueName, List<String> routingKeys)
            throws AiravataException {
        
        if (connectionFactory == null) {
            throw new AiravataException("Spring AMQP ConnectionFactory not available. Use Spring-managed subscriber.");
        }

        try {
            // Generate unique ID for this listener
            String id = UUID.randomUUID().toString();
            
            // Create or declare queue
            String actualQueueName = queueName != null ? queueName : "airavata.queue." + id;
            Queue queue = new Queue(actualQueueName, true, false, false);
            rabbitAdmin.declareQueue(queue);
            
            // Declare exchange
            TopicExchange exchange = new TopicExchange(properties.getExchangeName(), true, false);
            rabbitAdmin.declareExchange(exchange);
            
            // Bind queue to exchange with routing keys
            for (String routingKey : routingKeys) {
                Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
                rabbitAdmin.declareBinding(binding);
            }
            
            // Create message listener container
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(actualQueueName);
            container.setPrefetchCount(properties.getPrefetchCount() > 0 ? properties.getPrefetchCount() : 10);
            container.setAcknowledgeMode(properties.isAutoAck() ? AcknowledgeMode.AUTO : AcknowledgeMode.MANUAL);
            
            // Wrap the legacy Consumer in a ChannelAwareMessageListener
            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
                // Get the underlying RabbitMQ connection and channel
                Consumer consumer = supplier.apply(null, channel);
                if (consumer != null) {
                    // Create envelope-like object for compatibility
                    com.rabbitmq.client.Envelope envelope = new com.rabbitmq.client.Envelope(
                            message.getMessageProperties().getDeliveryTag(),
                            message.getMessageProperties().getRedelivered() != null && 
                                message.getMessageProperties().getRedelivered(),
                            properties.getExchangeName(),
                            message.getMessageProperties().getReceivedRoutingKey()
                    );
                    
                    // Create basic properties
                    com.rabbitmq.client.AMQP.BasicProperties basicProps = 
                            new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                                .contentType(message.getMessageProperties().getContentType())
                                .deliveryMode(2) // persistent
                                .build();
                    
                    try {
                        consumer.handleDelivery(
                                properties.getConsumerTag(),
                                envelope,
                                basicProps,
                                message.getBody()
                        );
                    } catch (Exception e) {
                        log.error("Error handling message delivery", e);
                    }
                }
            });
            
            container.start();
            
            // Store listener details for later cleanup
            listenerMap.put(id, new ListenerDetail(container, actualQueueName, routingKeys));
            
            log.info("Started listener {} for queue {} with routing keys {}", id, actualQueueName, routingKeys);
            return id;
            
        } catch (Exception e) {
            String msg = "Could not create listener for exchange " + properties.getExchangeName();
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void stopListen(String id) throws AiravataException {
        ListenerDetail detail = listenerMap.remove(id);
        if (detail != null) {
            try {
                detail.container.stop();
                detail.container.destroy();
                
                // Optionally delete queue
                if (rabbitAdmin != null) {
                    rabbitAdmin.deleteQueue(detail.queueName);
                }
                
                log.info("Stopped listener {} for queue {}", id, detail.queueName);
            } catch (Exception e) {
                log.warn("Error stopping listener {}: {}", id, e.getMessage());
            }
        }
    }

    @Override
    public void sendAck(long deliveryTag) {
        // With Spring AMQP, acknowledgment is handled by the container
        // based on AcknowledgeMode setting
        log.debug("sendAck called for deliveryTag {} - handled by Spring AMQP container", deliveryTag);
    }

    /**
     * Close all listeners and clean up resources.
     */
    public void close() {
        for (Map.Entry<String, ListenerDetail> entry : listenerMap.entrySet()) {
            try {
                entry.getValue().container.stop();
                entry.getValue().container.destroy();
            } catch (Exception e) {
                log.warn("Error closing listener {}: {}", entry.getKey(), e.getMessage());
            }
        }
        listenerMap.clear();
        log.info("RabbitMQSubscriber closed - all listeners stopped");
    }

    /**
     * Internal class to track listener details.
     */
    private static class ListenerDetail {
        final SimpleMessageListenerContainer container;
        final String queueName;
        final List<String> routingKeys;

        ListenerDetail(SimpleMessageListenerContainer container, String queueName, List<String> routingKeys) {
            this.container = container;
            this.queueName = queueName;
            this.routingKeys = routingKeys;
        }
    }
}
