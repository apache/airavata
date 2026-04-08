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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.messaging.util.RabbitMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public class RabbitMQSubscriber implements Subscriber {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQSubscriber.class);

    private final CachingConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitMQProperties properties;
    private final Map<String, SubscriptionDetail> subscriptions = new HashMap<>();

    public RabbitMQSubscriber(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        try {
            this.connectionFactory = new CachingConnectionFactory(java.net.URI.create(properties.getBrokerUrl()));
            this.rabbitAdmin = new RabbitAdmin(connectionFactory);
            rabbitAdmin.afterPropertiesSet();

            // Declare exchange
            TopicExchange exchange = new TopicExchange(properties.getExchangeName(), true, false);
            rabbitAdmin.declareExchange(exchange);

            log.info("Connected to RabbitMQ for exchange: {}", properties.getExchangeName());
        } catch (Exception e) {
            String msg = "Could not connect to RabbitMQ for exchange " + properties.getExchangeName();
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public String listen(MessageListener listener, String queueName, List<String> routingKeys)
            throws AiravataException {
        try {
            String id = buildId(routingKeys, queueName);
            if (subscriptions.containsKey(id)) {
                throw new IllegalStateException("Subscriber already defined for id: " + id);
            }

            // Declare queue
            Queue queue;
            if (queueName == null || queueName.isEmpty()) {
                queueName = "auto." + UUID.randomUUID();
                queue = new Queue(queueName, false, true, true);
            } else {
                queue = new Queue(queueName, true, false, false);
            }
            rabbitAdmin.declareQueue(queue);

            // Bind routing keys
            TopicExchange exchange = new TopicExchange(properties.getExchangeName(), true, false);
            for (String key : routingKeys) {
                rabbitAdmin.declareBinding(
                        BindingBuilder.bind(queue).to(exchange).with(key));
            }

            // Create and start listener container
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(queueName);
            container.setMessageListener(listener);
            container.setPrefetchCount(properties.getPrefetchCount());
            container.setAcknowledgeMode(properties.isAutoAck() ? AcknowledgeMode.AUTO : AcknowledgeMode.MANUAL);
            container.setConsumerTagStrategy(q -> properties.getConsumerTag() + "." + q);
            container.start();

            subscriptions.put(id, new SubscriptionDetail(queueName, routingKeys, container));
            return id;
        } catch (Exception e) {
            String msg = "Could not set up listener for exchange " + properties.getExchangeName();
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void stopListen(String id) throws AiravataException {
        SubscriptionDetail detail = subscriptions.remove(id);
        if (detail != null) {
            try {
                detail.container.stop();
                for (String key : detail.routingKeys) {
                    rabbitAdmin.removeBinding(BindingBuilder.bind(new Queue(detail.queueName))
                            .to(new TopicExchange(properties.getExchangeName()))
                            .with(key));
                }
                rabbitAdmin.deleteQueue(detail.queueName);
            } catch (Exception e) {
                log.debug(
                        "Could not clean up queue: {} for exchange {}", detail.queueName, properties.getExchangeName());
            }
        }
    }

    @Override
    public void sendAck(long deliveryTag) {
        // With Spring AMQP MANUAL ack mode, ack is handled in the listener via Channel parameter.
        // This method is retained for backward compatibility but is a no-op when using
        // ChannelAwareMessageListener.
        log.warn("sendAck() called directly — acks should be handled inside the message listener");
    }

    public void close() {
        for (SubscriptionDetail detail : subscriptions.values()) {
            try {
                detail.container.stop();
            } catch (Exception ignore) {
            }
        }
        subscriptions.clear();
        connectionFactory.destroy();
    }

    private String buildId(List<String> routingKeys, String queueName) {
        StringBuilder sb = new StringBuilder();
        for (String key : routingKeys) {
            sb.append("_").append(key);
        }
        sb.append("_").append(queueName);
        return sb.toString();
    }

    private static class SubscriptionDetail {
        final String queueName;
        final List<String> routingKeys;
        final SimpleMessageListenerContainer container;

        SubscriptionDetail(String queueName, List<String> routingKeys, SimpleMessageListenerContainer container) {
            this.queueName = queueName;
            this.routingKeys = routingKeys;
            this.container = container;
        }
    }
}
