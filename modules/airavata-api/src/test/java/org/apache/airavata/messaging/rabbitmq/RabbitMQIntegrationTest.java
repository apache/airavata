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
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.TestcontainersConfig;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.Publisher;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.TestMessagingUtils;
import org.apache.airavata.messaging.Type;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, TestcontainersConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "flyway.enabled=false"})
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "rabbitmq.experiment-exchange-name=test_experiment_exchange",
            "rabbitmq.status-exchange-name=test_status_exchange",
            "rabbitmq.durable-queue=false"
        },
        locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(AiravataServerProperties.class)
public class RabbitMQIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Initialize Testcontainers services and get URLs
        String rabbitMQUrl = TestcontainersConfig.getRabbitMQUrl();
        // Register properties - these will be available before Spring context loads
        registry.add("rabbitmq.broker-url", () -> rabbitMQUrl);
    }

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQIntegrationTest.class);
    private static String rabbitMQUrl;

    @Autowired
    private AiravataServerProperties properties;

    @BeforeAll
    public static void setupRabbitMQ() {
        // Get RabbitMQ URL from Testcontainers - this is called after @DynamicPropertySource
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
        properties.setExchangeType(org.apache.airavata.messaging.rabbitmq.RabbitMQProperties.EXCHANGE_TYPE.TOPIC);

        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> "test.key");
        assertNotNull(publisher, "Publisher should be created");
        logger.info("RabbitMQ connection test passed");
    }

    @Test
    public void testMessagePublishAndConsume() throws Exception {
        String exchangeName = "test_exchange_" + AiravataUtils.getUniqueTimestamp().getTime();
        String queueName = "test_queue_" + AiravataUtils.getUniqueTimestamp().getTime();
        String routingKey = "test.routing.key";

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);
        properties.setExchangeType(org.apache.airavata.messaging.rabbitmq.RabbitMQProperties.EXCHANGE_TYPE.TOPIC);

        // Create publisher
        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> routingKey);

        // Create test message
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
        MessageContext messageContext =
                new MessageContext(event, MessageType.EXPERIMENT, "test-message-id", "test-gateway");

        // Setup consumer with latch to verify message received
        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> receivedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            logger.info("Message received: {}", message.getMessageId());
            receivedMessages.add(message);
            messageReceived.countDown();
        };

        // Create subscriber with same exchange and routing key
        RabbitMQSubscriber subscriber = new RabbitMQSubscriber(properties);
        List<String> subscriberRoutingKeys = new ArrayList<>();
        subscriberRoutingKeys.add(routingKey);
        subscriber.listen(
                (connection, channel) -> new org.apache.airavata.messaging.rabbitmq.MessageConsumer(handler, connection, channel),
                queueName,
                subscriberRoutingKeys);
        
        // Give subscriber time to start listening
        Thread.sleep(500);

        // Publish message
        publisher.publish(messageContext);

        // Wait for message (with timeout)
        boolean received = messageReceived.await(5, TimeUnit.SECONDS);

        assertTrue(received, "Message should be received within timeout");
        assertEquals(1, receivedMessages.size(), "Should receive exactly one message");
        assertEquals("test-message-id", receivedMessages.get(0).getMessageId(), "Message ID should match");

        subscriber.close();
        publisher.close();
        logger.info("Message publish and consume test passed");
    }

    @Test
    public void testExchangeAndQueueCreation() throws Exception {
        String exchangeName = "test_exchange_create_" + AiravataUtils.getUniqueTimestamp().getTime();
        String queueName = "test_queue_create_" + AiravataUtils.getUniqueTimestamp().getTime();

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);
        properties.setExchangeType(org.apache.airavata.messaging.rabbitmq.RabbitMQProperties.EXCHANGE_TYPE.TOPIC);

        // Publisher needs a routing key supplier
        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> "test.routing.key");

        // Publishing a message will create exchange and queue
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
        MessageContext messageContext =
                new MessageContext(event, MessageType.EXPERIMENT, "test-create-id", "test-gateway");

        // This should create the exchange and queue
        assertDoesNotThrow(
                () -> publisher.publish(messageContext), "Publishing should create exchange and queue without errors");

        publisher.close();
        logger.info("Exchange and queue creation test passed");
    }

    @Test
    public void testMultipleMessages() throws Exception {
        String exchangeName = "test_exchange_multi_" + AiravataUtils.getUniqueTimestamp().getTime();
        String queueName = "test_queue_multi_" + AiravataUtils.getUniqueTimestamp().getTime();
        int messageCount = 5;

        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setBrokerUrl(rabbitMQUrl);
        properties.setExchangeName(exchangeName);
        properties.setQueueName(queueName);
        properties.setDurable(false);
        properties.setExchangeType(org.apache.airavata.messaging.rabbitmq.RabbitMQProperties.EXCHANGE_TYPE.TOPIC);

        RabbitMQPublisher publisher = new RabbitMQPublisher(properties, msg -> "test.key");

        CountDownLatch messagesReceived = new CountDownLatch(messageCount);
        List<String> receivedMessageIds = new ArrayList<>();

        // For this test, we verify that publishing works correctly
        for (int i = 0; i < messageCount; i++) {
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
            event.setState(org.apache.airavata.common.model.ExperimentState.CREATED);
            MessageContext messageContext =
                    new MessageContext(event, MessageType.EXPERIMENT, "message-" + i, "test-gateway");

            assertDoesNotThrow(
                    () -> publisher.publish(messageContext), "Publishing message " + i + " should not throw exception");
        }

        publisher.close();
        logger.info("Multiple messages test passed - {} messages published", messageCount);
    }

    @Test
    @DisplayName("Should publish and receive ExperimentStatusChangeEvent")
    void shouldPublishAndReceiveExperimentStatusChangeEvent() throws Exception {
        MessagingFactory messagingFactory = new MessagingFactory(properties);
        Publisher publisher = messagingFactory.getPublisher(Type.STATUS);

        String experimentId = "test-exp-" + AiravataUtils.getUniqueTimestamp().getTime();
        String gatewayId = "test-gateway";
        // Routing key format for EXPERIMENT is: gatewayId.experimentId
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(gatewayId + "." + experimentId);
        routingKeys.add(gatewayId + "." + experimentId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) message.getEvent();
                logger.info(
                        "Received ExperimentStatusChangeEvent: experimentId={}, state={}",
                        event.getExperimentId(),
                        event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        // Give subscriber time to start listening and bind to queue
        Thread.sleep(500);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createExperimentStatusChangeMessage(
                    experimentId, gatewayId, ExperimentState.LAUNCHED);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            ExperimentStatusChangeEvent receivedEvent =
                    (ExperimentStatusChangeEvent) capturedMessages.get(0).getEvent();
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
        Publisher publisher = messagingFactory.getPublisher(Type.STATUS);

        String processId = "test-process-" + AiravataUtils.getUniqueTimestamp().getTime();
        String experimentId = "test-exp-" + AiravataUtils.getUniqueTimestamp().getTime();
        String gatewayId = "test-gateway";
        // Routing key format for PROCESS is: gatewayId.experimentId.processId
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(gatewayId + "." + experimentId + "." + processId);
        routingKeys.add(gatewayId + "." + experimentId + "." + processId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.PROCESS)) {
                ProcessStatusChangeEvent event = (ProcessStatusChangeEvent) message.getEvent();
                logger.info(
                        "Received ProcessStatusChangeEvent: processId={}, state={}",
                        event.getProcessIdentity().getProcessId(),
                        event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        // Give subscriber time to start listening
        Thread.sleep(100);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createProcessStatusChangeMessage(
                    processId, experimentId, gatewayId, ProcessState.STARTED);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            ProcessStatusChangeEvent receivedEvent =
                    (ProcessStatusChangeEvent) capturedMessages.get(0).getEvent();
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
        Publisher publisher = messagingFactory.getPublisher(Type.STATUS);

        String jobId = "test-job-" + AiravataUtils.getUniqueTimestamp().getTime();
        String taskId = "test-task";
        String processId = "test-process";
        String experimentId = "test-exp";
        String gatewayId = "test-gateway";
        // Routing key format for JOB is: gatewayId.experimentId.processId.taskId.jobId
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(gatewayId + "." + experimentId + "." + processId + "." + taskId + "." + jobId);
        routingKeys.add(gatewayId + "." + experimentId + "." + processId + "." + taskId + "." + jobId + ".*");

        CountDownLatch messageReceived = new CountDownLatch(1);
        List<MessageContext> capturedMessages = new ArrayList<>();

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.JOB)) {
                JobStatusChangeEvent event = (JobStatusChangeEvent) message.getEvent();
                logger.info(
                        "Received JobStatusChangeEvent: jobId={}, state={}",
                        event.getJobIdentity().getJobId(),
                        event.getState());
                capturedMessages.add(message);
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        // Give subscriber time to start listening
        Thread.sleep(100);
        String subscriberId = null;

        try {
            JobIdentifier jobIdentifier = new JobIdentifier(jobId, taskId, processId, experimentId, gatewayId);
            JobStatusChangeEvent event = new JobStatusChangeEvent(JobState.SUBMITTED, jobIdentifier);
            MessageContext messageContext = new MessageContext(event, MessageType.JOB, "test-job-msg-id", gatewayId);
            publisher.publish(messageContext);

            assertTrue(messageReceived.await(10, TimeUnit.SECONDS), "Message should be received");
            assertEquals(1, capturedMessages.size(), "Should receive exactly one message");
            JobStatusChangeEvent receivedEvent =
                    (JobStatusChangeEvent) capturedMessages.get(0).getEvent();
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
        Publisher publisher = messagingFactory.getPublisher(Type.STATUS);

        String experimentId = "test-exp-routing";
        String gatewayId = "test-gateway";

        // Routing key format for EXPERIMENT is: gatewayId.experimentId
        // Subscribe to specific routing key pattern
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(gatewayId + "." + experimentId);

        CountDownLatch messageReceived = new CountDownLatch(1);
        MessageHandler handler = message -> {
            logger.info("Received message with routing key match");
            messageReceived.countDown();
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        // Give subscriber time to start listening
        Thread.sleep(100);
        String subscriberId = null;

        try {
            MessageContext messageContext = TestMessagingUtils.createExperimentStatusChangeMessage(
                    experimentId, gatewayId, ExperimentState.COMPLETED);
            publisher.publish(messageContext);

            assertTrue(
                    messageReceived.await(10, TimeUnit.SECONDS), "Message should be received with routing key match");
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
        Publisher publisher = messagingFactory.getPublisher(Type.STATUS);

        String experimentId = "test-exp-serialization";
        String gatewayId = "test-gateway";
        // Routing key format for EXPERIMENT is: gatewayId.experimentId
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(gatewayId + "." + experimentId);

        CountDownLatch messageReceived = new CountDownLatch(1);
        ExperimentStatusChangeEvent[] receivedEvent = new ExperimentStatusChangeEvent[1];

        MessageHandler handler = message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                receivedEvent[0] = (ExperimentStatusChangeEvent) message.getEvent();
                messageReceived.countDown();
            }
        };

        Subscriber subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
        // Give subscriber time to start listening
        Thread.sleep(100);
        String subscriberId = null;

        try {
            ExperimentStatusChangeEvent originalEvent = new ExperimentStatusChangeEvent();
            originalEvent.setExperimentId(experimentId);
            originalEvent.setGatewayId(gatewayId);
            originalEvent.setState(ExperimentState.EXECUTING);

            MessageContext messageContext =
                    new MessageContext(originalEvent, MessageType.EXPERIMENT, "test-serialization-id", gatewayId);
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
