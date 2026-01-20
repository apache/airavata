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

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.TaskOutputChangeEvent;
import org.apache.airavata.common.model.TaskStatusChangeEvent;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.Publisher;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessagingFactory {

    private static final Logger logger = LoggerFactory.getLogger(MessagingFactory.class);

    private final AiravataServerProperties properties;
    private final ConnectionFactory connectionFactory;

    /**
     * Constructor with optional Spring AMQP ConnectionFactory.
     * When ConnectionFactory is available, subscribers will use Spring AMQP features.
     */
    @Autowired
    public MessagingFactory(
            AiravataServerProperties properties, @Autowired(required = false) ConnectionFactory connectionFactory) {
        this.properties = properties;
        this.connectionFactory = connectionFactory;
        if (connectionFactory != null) {
            logger.info("MessagingFactory initialized with Spring AMQP ConnectionFactory");
        } else {
            logger.warn("MessagingFactory initialized without Spring AMQP ConnectionFactory - using legacy mode");
        }
    }

    /**
     * Backward-compatible constructor without ConnectionFactory.
     * Uses legacy mode where Spring AMQP features are not available.
     */
    public MessagingFactory(AiravataServerProperties properties) {
        this(properties, null);
    }

    public Subscriber getSubscriber(final MessageHandler messageHandler, List<String> routingKeys, Type type)
            throws AiravataException {
        Subscriber subscriber = null;
        RabbitMQProperties rProperties = getProperties();

        switch (type) {
            case EXPERIMENT_LAUNCH:
                subscriber = getExperimentSubscriber(rProperties);
                subscriber.listen(
                        ((connection, channel) -> new ExperimentConsumer(messageHandler, connection, channel)),
                        rProperties.getQueueName(),
                        routingKeys);
                break;
            case PROCESS_LAUNCH:
                subscriber = getProcessSubscriber(rProperties);
                subscriber.listen(
                        (connection, channel) -> new ProcessConsumer(messageHandler, connection, channel),
                        rProperties.getQueueName(),
                        routingKeys);
                break;
            case STATUS:
                subscriber = getStatusSubscriber(rProperties);
                subscriber.listen(
                        (connection, channel) -> new StatusConsumer(messageHandler, connection, channel),
                        rProperties.getQueueName(),
                        routingKeys);
                break;
            default:
                break;
        }

        return subscriber;
    }

    public Subscriber getDBEventSubscriber(final MessageHandler messageHandler, String serviceName)
            throws AiravataException {
        RabbitMQProperties rProperties = getProperties();

        // FIXME: Set autoAck to false and handle possible situations
        rProperties
                .setExchangeName(DBEventManagerConstants.DB_EVENT_EXCHANGE_NAME)
                .setQueueName(DBEventManagerConstants.getQueueName(serviceName))
                .setAutoAck(false);
        Subscriber subscriber = createSubscriber(rProperties);
        subscriber.listen(
                ((connection, channel) -> new MessageConsumer(messageHandler, connection, channel)),
                rProperties.getQueueName(),
                new ArrayList<String>() {
                    {
                        add(DBEventManagerConstants.getRoutingKey(serviceName));
                    }
                });

        return subscriber;
    }

    public Publisher getPublisher(Type type) throws AiravataException {
        RabbitMQProperties rProperties = getProperties();
        Publisher publiser = null;
        switch (type) {
            case EXPERIMENT_LAUNCH:
                publiser = getExperimentPublisher(rProperties);
                break;
            case PROCESS_LAUNCH:
                publiser = gerProcessPublisher(rProperties);
                break;
            case STATUS:
                publiser = getStatusPublisher(rProperties);
                break;
            default:
                throw new IllegalArgumentException("Publisher " + type + " is not handled");
        }

        return publiser;
    }

    public Publisher getDBEventPublisher() throws AiravataException {
        RabbitMQProperties rProperties = getProperties();
        rProperties.setExchangeName(DBEventManagerConstants.DB_EVENT_EXCHANGE_NAME);
        return new RabbitMQPublisher(rProperties);
    }

    private Publisher getExperimentPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(getExperimentExchangeName());
        return new RabbitMQPublisher(rProperties, messageContext -> rProperties.getExchangeName());
    }

    private Publisher getStatusPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(getStatusExchangeName());
        return new RabbitMQPublisher(rProperties, this::statusRoutingkey);
    }

    private Publisher gerProcessPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(getProcessExchangeName());
        return new RabbitMQPublisher(rProperties, messageContext -> rProperties.getExchangeName());
    }

    // Helper methods for exchange names with defaults for Testcontainers integration
    private String getExperimentExchangeName() {
        return properties.rabbitmq() != null && properties.rabbitmq().experimentExchangeName() != null
                ? properties.rabbitmq().experimentExchangeName()
                : "experiment_exchange";
    }

    private String getStatusExchangeName() {
        return properties.rabbitmq() != null && properties.rabbitmq().statusExchangeName() != null
                ? properties.rabbitmq().statusExchangeName()
                : "status_exchange";
    }

    private String getProcessExchangeName() {
        return properties.rabbitmq() != null && properties.rabbitmq().processExchangeName() != null
                ? properties.rabbitmq().processExchangeName()
                : "process_exchange";
    }

    private RabbitMQProperties getProperties() {
        RabbitMQProperties rProperties = new RabbitMQProperties()
                .setAutoRecoveryEnable(true)
                .setConsumerTag("default")
                .setExchangeType(RabbitMQProperties.EXCHANGE_TYPE.TOPIC);

        if (properties.rabbitmq() != null) {
            // Use properties if available
            rProperties
                    .setBrokerUrl(properties.rabbitmq().brokerUrl())
                    .setDurable(properties.rabbitmq().durableQueue())
                    .setPrefetchCount(properties.rabbitmq().prefetchCount());
        } else if (connectionFactory != null) {
            // Fall back to ConnectionFactory (for Testcontainers integration)
            // Extract connection info from Spring AMQP ConnectionFactory
            if (connectionFactory
                    instanceof org.springframework.amqp.rabbit.connection.CachingConnectionFactory cachingFactory) {
                String host = cachingFactory.getHost();
                int port = cachingFactory.getPort();
                String brokerUrl = "amqp://" + host + ":" + port;
                rProperties.setBrokerUrl(brokerUrl);
                logger.info("Using broker URL from ConnectionFactory: {}", brokerUrl);
            }
            // Use sensible defaults for other properties
            rProperties.setDurable(false).setPrefetchCount(200);
        } else {
            throw new IllegalStateException(
                    "RabbitMQ is not configured. Ensure airavata.rabbitmq.enabled=true and all required "
                            + "RabbitMQ properties are set (broker-url, exchange names, etc.)");
        }

        return rProperties;
    }

    /**
     * Check if RabbitMQ messaging is available.
     * @return true if RabbitMQ is configured and enabled (via properties or ConnectionFactory)
     */
    public boolean isRabbitMQAvailable() {
        // Check if ConnectionFactory is available (set by Spring AMQP/Testcontainers)
        if (connectionFactory != null) {
            return true;
        }
        // Fall back to checking properties using null-safe helper methods
        return properties.isRabbitMQEnabled() && properties.getRabbitMQBrokerUrl() != null;
    }

    private RabbitMQSubscriber getStatusSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(getStatusExchangeName()).setAutoAck(true);
        return createSubscriber(sp);
    }

    private RabbitMQSubscriber getProcessSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(getProcessExchangeName())
                .setQueueName("process_launch")
                .setAutoAck(false);
        return createSubscriber(sp);
    }

    private Subscriber getExperimentSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(getExperimentExchangeName())
                .setQueueName("experiment_launch")
                .setAutoAck(false);
        return createSubscriber(sp);
    }

    /**
     * Create a RabbitMQSubscriber, using Spring AMQP ConnectionFactory if available.
     */
    private RabbitMQSubscriber createSubscriber(RabbitMQProperties sp) throws AiravataException {
        if (connectionFactory != null) {
            return new RabbitMQSubscriber(connectionFactory, sp);
        } else {
            return new RabbitMQSubscriber(sp);
        }
    }

    private String statusRoutingkey(MessageContext msgCtx) {
        String gatewayId = msgCtx.getGatewayId();
        String routingKey = null;
        if (msgCtx.getType() == MessageType.EXPERIMENT) {
            ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) msgCtx.getEvent();
            routingKey = gatewayId + "." + event.getExperimentId();
        } else if (msgCtx.getType() == MessageType.TASK) {
            TaskStatusChangeEvent event = (TaskStatusChangeEvent) msgCtx.getEvent();
            routingKey = gatewayId + "." + event.getTaskIdentity().getExperimentId() + "."
                    + event.getTaskIdentity().getProcessId() + "."
                    + event.getTaskIdentity().getTaskId();
        } else if (msgCtx.getType() == MessageType.PROCESSOUTPUT) {
            TaskOutputChangeEvent event = (TaskOutputChangeEvent) msgCtx.getEvent();
            routingKey = gatewayId + "." + event.getTaskIdentity().getExperimentId() + "."
                    + event.getTaskIdentity().getProcessId() + "."
                    + event.getTaskIdentity().getTaskId();
        } else if (msgCtx.getType() == MessageType.PROCESS) {
            ProcessStatusChangeEvent event = (ProcessStatusChangeEvent) msgCtx.getEvent();
            ProcessIdentifier processIdentifier = event.getProcessIdentity();
            routingKey = gatewayId + "." + processIdentifier.getExperimentId() + "." + processIdentifier.getProcessId();
        } else if (msgCtx.getType() == MessageType.JOB) {
            JobStatusChangeEvent event = (JobStatusChangeEvent) msgCtx.getEvent();
            JobIdentifier identity = event.getJobIdentity();
            routingKey = gatewayId + "." + identity.getExperimentId() + "." + identity.getProcessId()
                    + "." + identity.getTaskId()
                    + "." + identity.getJobId();
        }
        return routingKey;
    }
}
