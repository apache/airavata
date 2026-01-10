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
package org.apache.airavata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AMQP configuration for RabbitMQ messaging.
 * Replaces manual RabbitMQ client configuration with Spring-managed beans.
 * 
 * Configure via airavata.properties:
 *   rabbitmq.enabled=true
 *   rabbitmq.broker-url=amqp://guest:guest@localhost:5672/develop
 *   rabbitmq.prefetch-count=200
 *   rabbitmq.status-exchange-name=status_exchange
 *   rabbitmq.process-exchange-name=process_exchange
 *   rabbitmq.experiment-exchange-name=experiment_exchange
 *   rabbitmq.db-event-exchange-name=dbevent_exchange
 */
@Configuration
@ConditionalOnProperty(prefix = "rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    private final AiravataServerProperties properties;

    public RabbitMQConfig(AiravataServerProperties properties) {
        this.properties = properties;
    }

    /**
     * Connection factory using Spring AMQP's caching connection factory.
     */
    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        
        // Parse broker URL from properties
        String brokerUrl = properties.rabbitmq.brokerUrl;
        if (brokerUrl != null && !brokerUrl.isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(brokerUrl);
                factory.setHost(uri.getHost());
                factory.setPort(uri.getPort() > 0 ? uri.getPort() : 5672);
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    factory.setUsername(userInfo[0]);
                    if (userInfo.length > 1) {
                        factory.setPassword(userInfo[1]);
                    }
                }
            } catch (Exception e) {
                // Fallback to localhost
                factory.setHost("localhost");
                factory.setPort(5672);
            }
        }
        
        factory.setChannelCacheSize(25);
        factory.setConnectionCacheSize(5);
        
        return factory;
    }

    /**
     * JSON message converter using Jackson.
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate for publishing messages.
     * Uses JSON message converter for serialization.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Listener container factory for @RabbitListener methods.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(properties.rabbitmq.prefetchCount > 0 ? properties.rabbitmq.prefetchCount : 10);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }

    // Exchange declarations - create based on configured exchange names
    
    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(properties.rabbitmq.statusExchangeName, true, false);
    }

    @Bean
    public TopicExchange processExchange() {
        return new TopicExchange(properties.rabbitmq.processExchangeName, true, false);
    }

    @Bean
    public TopicExchange experimentExchange() {
        return new TopicExchange(properties.rabbitmq.experimentExchangeName, true, false);
    }

    @Bean
    public TopicExchange dbEventExchange() {
        return new TopicExchange(properties.rabbitmq.dbEventExchangeName, true, false);
    }
}
