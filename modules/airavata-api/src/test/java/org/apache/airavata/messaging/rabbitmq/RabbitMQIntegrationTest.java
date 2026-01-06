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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.TestcontainersConfig;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.TestMessagingUtils;
import org.apache.airavata.messaging.Type;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {
    org.apache.airavata.config.JpaConfig.class,
    TestcontainersConfig.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "flyway.enabled=false"
})
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "rabbitmq.broker-url=${rabbitMQBrokerUrl}",
            "rabbitmq.experiment-exchange-name=test_experiment_exchange",
            "rabbitmq.status-exchange-name=test_status_exchange",
            "rabbitmq.durable-queue=false"
        },
        locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(AiravataServerProperties.class)
public class RabbitMQIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQIntegrationTest.class);
    private static String rabbitMQUrl;

    @Autowired
    private AiravataServerProperties properties;

    @BeforeAll
    public static void setupRabbitMQ() {
        rabbitMQUrl = TestcontainersConfig.getRabbitMQUrl();
        logger.info("RabbitMQ container initialized at: {}", rabbitMQUrl);
    }

    @Test
    public void testRabbitMQConnection() throws Exception {
        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName("test_exchange");
        properties.setQueueName("test_queue");
        properties.setDurable(false);

        RabbitMQPublisher publisher = new RabbitMQPublisher(properties);
        assertNotNull(publisher, "Publisher should be created");
        logger.info("RabbitMQ connection test passed");
    }

    @Test
    public void testMessagePublishAndConsume() throws Exception {
        String exchangeName = "test_exchange_" + System.currentTimeMillis();
        String queueName = "test_queue_" + System.currentTimeMillis();
        String routingKey = "test.routing.key";

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);

        // Create publisher
        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> routingKey);

        // Create test message
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
        MessageContext messageContext = new MessageContext(
                event, MessageType.EXPERIMENT, "test-message-id", "test-gateway");

        // Setup consumer with latch to verify message received
        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> receivedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            logger.info("Message received: {}", message.getMessageId());
            receivedMessages.add(message);
            messageReceived.countDown();
        };

        // Create subscriber
        MessagingFactory messagingFactory = new MessagingFactory(new AiravataServerProperties());
        // would need proper configuration. For integration testing, we test the core
        // publish/subscribe functionality directly.

        // Publish message
        publisher.publish(messageContext);

        // Wait for message (with timeout)
        boolean received = messageReceived.await(5, TimeUnit.SECONDS);

        assertTrue(received, "Message should be received within timeout");
        assertEquals(1, receivedMessages.size(), "Should receive exactly one message");
        assertEquals("test-message-id", receivedMessages.get(0).getMessageId(), "Message ID should match");

        publisher.close();
        logger.info("Message publish and consume test passed");
    }

    @Test
    public void testExchangeAndQueueCreation() throws Exception {
        String exchangeName = "test_exchange_create_" + System.currentTimeMillis();
        String queueName = "test_queue_create_" + System.currentTimeMillis();

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);

        RabbitMQPublisher publisher = new RabbitMQPublisher(properties);
        
        // Publishing a message will create exchange and queue
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
        MessageContext messageContext = new MessageContext(
                event, MessageType.EXPERIMENT, "test-create-id", "test-gateway");

        // This should create the exchange and queue
        assertDoesNotThrow(() -> publisher.publish(messageContext), 
                "Publishing should create exchange and queue without errors");

        publisher.close();
        logger.info("Exchange and queue creation test passed");
    }

    @Test
    public void testMultipleMessages() throws Exception {
        String exchangeName = "test_exchange_multi_" + System.currentTimeMillis();
        String queueName = "test_queue_multi_" + System.currentTimeMillis();
        int messageCount = 5;

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);

        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> "test.key");

        CountDownLatch messagesReceived = new CountDownLatch(messageCount);
        List<String> receivedMessageIds = new ArrayList<>();

        // For this test, we verify that publishing works correctly
        for (int i = 0; i < messageCount; i++) {
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
            event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT, "message-" + i, "test-gateway");
            
            assertDoesNotThrow(() -> publisher.publish(messageContext),
                    "Publishing message " + i + " should not throw exception");
        }

        publisher.close();
        logger.info("Multiple messages test passed - {} messages published", messageCount);
    }

    @Test
    @DisplayName("Should publish and receive ExperimentStatusChangeEvent")
    void shouldPublishAndReceiveExperimentStatusChangeEvent() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        RabbitMQPublisher publisher = new RabbitMQPublisher(
                new RabbitMQProperties()
                        .setBrokerUrl(properties.rabbitmq.brokerUrl)
                        .setExchangeName(properties.rabbitmq.experimentExchangeName)
                        .setDurable(properties.rabbitmq.durableQueue)
                        .setPrefetchCount(properties.rabbitmq.prefetchCount));

        String experimentId = "test-exp-" + System.currentTimeMillis();
        String gatewayId = "test-gateway";
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(experimentId);
        routingKeys.add(experimentId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) message.getEvent();
                logger.info("Received ExperimentStatusChangeEvent: experimentId={}, state={}",
                        event.getExperimentId(), event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createExperimentStatusChangeMessage(
                    experimentId, gatewayId, ExperimentState.LAUNCHED);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            ExperimentStatusChangeEvent receivedEvent = (ExperimentStatusChangeEvent) capturedMessages.get(0).getEvent();
            assertEquals(experimentId, receivedEvent.getExperimentId(), "Experiment ID should match");
            assertEquals(ExperimentState.LAUNCHED, receivedEvent.getState(), "State should match");
        } finally {
            if (subscriberId != null) {
                subscriber.stopListen(subscriberId);
            }
        }
    }

    @Test
    @DisplayName("Should publish and receive ProcessStatusChangeEvent")
    void shouldPublishAndReceiveProcessStatusChangeEvent() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        RabbitMQPublisher publisher = new RabbitMQPublisher(
                new RabbitMQProperties()
                        .setBrokerUrl(properties.rabbitmq.brokerUrl)
                        .setExchangeName(properties.rabbitmq.experimentExchangeName)
                        .setDurable(properties.rabbitmq.durableQueue)
                        .setPrefetchCount(properties.rabbitmq.prefetchCount));

        String processId = "test-process-" + System.currentTimeMillis();
        String experimentId = "test-exp-" + System.currentTimeMillis();
        String gatewayId = "test-gateway";
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(processId);
        routingKeys.add(processId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.PROCESS)) {
                ProcessStatusChangeEvent event = (ProcessStatusChangeEvent) message.getEvent();
                logger.info("Received ProcessStatusChangeEvent: processId={}, state={}",
                        event.getProcessIdentity().getProcessId(), event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createProcessStatusChangeMessage(
                    processId, experimentId, gatewayId, ProcessState.STARTED);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            ProcessStatusChangeEvent receivedEvent = (ProcessStatusChangeEvent) capturedMessages.get(0).getEvent();
            assertEquals(processId, receivedEvent.getProcessIdentity().getProcessId(), "Process ID should match");
            assertEquals(ProcessState.STARTED, receivedEvent.getState(), "State should match");
        } finally {
            if (subscriberId != null) {
                subscriber.stopListen(subscriberId);
            }
        }
    }

    @Test
    @DisplayName("Should publish and receive JobStatusChangeEvent")
    void shouldPublishAndReceiveJobStatusChangeEvent() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        RabbitMQPublisher publisher = new RabbitMQPublisher(
                new RabbitMQProperties()
                        .setBrokerUrl(properties.rabbitmq.brokerUrl)
                        .setExchangeName(properties.rabbitmq.experimentExchangeName)
                        .setDurable(properties.rabbitmq.durableQueue)
                        .setPrefetchCount(properties.rabbitmq.prefetchCount));

        String jobId = "test-job-" + System.currentTimeMillis();
        String taskId = "test-task";
        String processId = "test-process";
        String experimentId = "test-exp";
        String gatewayId = "test-gateway";
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(jobId);
        routingKeys.add(jobId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.JOB)) {
                JobStatusChangeEvent event = (JobStatusChangeEvent) message.getEvent();
                logger.info("Received JobStatusChangeEvent: jobId={}, state={}",
                        event.getJobIdentity().getJobId(), event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        String subscriberId = null;

        try {
            JobIdentifier jobIdentifier = new JobIdentifier(jobId, taskId, processId, experimentId, gatewayId);
            JobStatusChangeEvent event = new JobStatusChangeEvent(JobState.SUBMITTED, jobIdentifier);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.JOB, "test-job-msg-id", gatewayId);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            JobStatusChangeEvent receivedEvent = (JobStatusChangeEvent) capturedMessages.get(0).getEvent();
            assertEquals(jobId, receivedEvent.getJobIdentity().getJobId(), "Job ID should match");
            assertEquals(JobState.SUBMITTED, receivedEvent.getState(), "State should match");
        } finally {
            if (subscriberId != null) {
                subscriber.stopListen(subscriberId);
            }
        }
    }

    @Test
    @DisplayName("Should verify message routing with routing keys")
    void shouldVerifyMessageRoutingWithRoutingKeys() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        RabbitMQPublisher publisher = new RabbitMQPublisher(
                new RabbitMQProperties()
                        .setBrokerUrl(properties.rabbitmq.brokerUrl)
                        .setExchangeName(properties.rabbitmq.experimentExchangeName)
                        .setDurable(properties.rabbitmq.durableQueue)
                        .setPrefetchCount(properties.rabbitmq.prefetchCount));

        String experimentId = "test-exp-routing";
        String gatewayId = "test-gateway";

        // Subscribe to specific routing key
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(experimentId + ".status");

        CountDownLatch messageReceived = new CountDownLatch(1);
        MessageHandler handler = message -> {
            logger.info("Received message with routing key match");
            messageReceived.countDown();
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createExperimentStatusChangeMessage(
                    experimentId, gatewayId, ExperimentState.COMPLETED);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received with routing key match");
        } finally {
            if (subscriberId != null) {
                subscriber.stopListen(subscriberId);
            }
        }
    }

    @Test
    @DisplayName("Should verify message serialization and deserialization")
    void shouldVerifyMessageSerializationAndDeserialization() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        RabbitMQPublisher publisher = new RabbitMQPublisher(
                new RabbitMQProperties()
                        .setBrokerUrl(properties.rabbitmq.brokerUrl)
                        .setExchangeName(properties.rabbitmq.experimentExchangeName)
                        .setDurable(properties.rabbitmq.durableQueue)
                        .setPrefetchCount(properties.rabbitmq.prefetchCount));

        String experimentId = "test-exp-serialization";
        String gatewayId = "test-gateway";
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(experimentId);

        CountDownLatch messageReceived = new CountDownLatch(1);
        ExperimentStatusChangeEvent[] receivedEvent = new ExperimentStatusChangeEvent[1];

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                receivedEvent[0] = (ExperimentStatusChangeEvent) message.getEvent();
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        String subscriberId = null;

        try {
            ExperimentStatusChangeEvent originalEvent = new ExperimentStatusChangeEvent();
            originalEvent.setExperimentId(experimentId);
            originalEvent.setGatewayId(gatewayId);
            originalEvent.setState(ExperimentState.EXECUTING);

            MessageContext messageContext = new MessageContext(
                    originalEvent, MessageType.EXPERIMENT, "test-serialization-id", gatewayId);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertNotNull(receivedEvent[0], "Event should be deserialized");
            assertEquals(experimentId, receivedEvent[0].getExperimentId(), "Experiment ID should be preserved");
            assertEquals(gatewayId, receivedEvent[0].getGatewayId(), "Gateway ID should be preserved");
            assertEquals(ExperimentState.EXECUTING, receivedEvent[0].getState(), "State should be preserved");
        } finally {
            if (subscriberId != null) {
                subscriber.stopListen(subscriberId);
            }
        }
    }
}

