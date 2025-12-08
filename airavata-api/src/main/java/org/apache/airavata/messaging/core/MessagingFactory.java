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
package org.apache.airavata.messaging.core;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.messaging.core.impl.*;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessagingFactory {

    private static MessagingFactory instance;

    @Autowired
    private AiravataServerProperties properties;

    @jakarta.annotation.PostConstruct
    public void init() {
        instance = this;
    }

    public static Subscriber getSubscriber(final MessageHandler messageHandler, List<String> routingKeys, Type type)
            throws AiravataException {
        if (instance == null) {
            throw new IllegalStateException("MessagingFactory not initialized. Make sure it's a Spring bean.");
        }
        Subscriber subscriber = null;
        RabbitMQProperties rProperties = instance.getProperties();

        switch (type) {
            case EXPERIMENT_LAUNCH:
                subscriber = instance.getExperimentSubscriber(rProperties);
                subscriber.listen(
                        ((connection, channel) -> new ExperimentConsumer(messageHandler, connection, channel)),
                        rProperties.getQueueName(),
                        routingKeys);
                break;
            case PROCESS_LAUNCH:
                subscriber = instance.getProcessSubscriber(rProperties);
                subscriber.listen(
                        (connection, channel) -> new ProcessConsumer(messageHandler, connection, channel),
                        rProperties.getQueueName(),
                        routingKeys);
                break;
            case STATUS:
                subscriber = instance.getStatusSubscriber(rProperties);
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

    public static Subscriber getDBEventSubscriber(final MessageHandler messageHandler, String serviceName)
            throws AiravataException {
        if (instance == null) {
            throw new IllegalStateException("MessagingFactory not initialized. Make sure it's a Spring bean.");
        }
        RabbitMQProperties rProperties = instance.getProperties();

        // FIXME: Set autoAck to false and handle possible situations
        rProperties
                .setExchangeName(DBEventManagerConstants.DB_EVENT_EXCHANGE_NAME)
                .setQueueName(DBEventManagerConstants.getQueueName(serviceName))
                .setAutoAck(false);
        Subscriber subscriber = new RabbitMQSubscriber(rProperties);
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

    public static Publisher getPublisher(Type type) throws AiravataException {
        if (instance == null) {
            throw new IllegalStateException("MessagingFactory not initialized. Make sure it's a Spring bean.");
        }
        RabbitMQProperties rProperties = instance.getProperties();
        Publisher publiser = null;
        switch (type) {
            case EXPERIMENT_LAUNCH:
                publiser = instance.getExperimentPublisher(rProperties);
                break;
            case PROCESS_LAUNCH:
                publiser = instance.gerProcessPublisher(rProperties);
                break;
            case STATUS:
                publiser = instance.getStatusPublisher(rProperties);
                break;
            default:
                throw new IllegalArgumentException("Publisher " + type + " is not handled");
        }

        return publiser;
    }

    public static Publisher getDBEventPublisher() throws AiravataException {
        if (instance == null) {
            throw new IllegalStateException("MessagingFactory not initialized. Make sure it's a Spring bean.");
        }
        RabbitMQProperties rProperties = instance.getProperties();
        rProperties.setExchangeName(DBEventManagerConstants.DB_EVENT_EXCHANGE_NAME);
        return new RabbitMQPublisher(rProperties);
    }

    private Publisher getExperimentPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(properties.rabbitmq.experimentExchangeName);
        return new RabbitMQPublisher(rProperties, messageContext -> rProperties.getExchangeName());
    }

    private Publisher getStatusPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(properties.rabbitmq.statusExchangeName);
        return new RabbitMQPublisher(rProperties, MessagingFactory::statusRoutingkey);
    }

    private Publisher gerProcessPublisher(RabbitMQProperties rProperties) throws AiravataException {
        rProperties.setExchangeName(properties.rabbitmq.processExchangeName);
        return new RabbitMQPublisher(rProperties, messageContext -> rProperties.getExchangeName());
    }

    private RabbitMQProperties getProperties() {
        return new RabbitMQProperties()
                .setBrokerUrl(properties.rabbitmq.brokerUrl)
                .setDurable(properties.rabbitmq.durableQueue)
                .setPrefetchCount(properties.rabbitmq.prefetchCount)
                .setAutoRecoveryEnable(true)
                .setConsumerTag("default")
                .setExchangeType(RabbitMQProperties.EXCHANGE_TYPE.TOPIC);
    }

    private RabbitMQSubscriber getStatusSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(properties.rabbitmq.statusExchangeName).setAutoAck(true);
        return new RabbitMQSubscriber(sp);
    }

    private RabbitMQSubscriber getProcessSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(properties.rabbitmq.processExchangeName)
                .setQueueName("process_launch")
                .setAutoAck(false);
        return new RabbitMQSubscriber(sp);
    }

    private Subscriber getExperimentSubscriber(RabbitMQProperties sp) throws AiravataException {
        sp.setExchangeName(properties.rabbitmq.experimentExchangeName)
                .setQueueName("experiment_launch")
                .setAutoAck(false);
        return new RabbitMQSubscriber(sp);
    }

    private static String statusRoutingkey(MessageContext msgCtx) {
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
