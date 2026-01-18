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
package org.apache.airavata.messaging.kafka;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.TestcontainersConfig;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.TestMessagingUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, TestcontainersConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "airavata.flyway.enabled=false"})
@ActiveProfiles("test")
@EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)  // Prevent tests from hanging indefinitely
public class KafkaIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Initialize Testcontainers services and get URLs
        String kafkaUrl = TestcontainersConfig.getKafkaBootstrapServers();
        // Register properties - these will be available before Spring context loads
        registry.add("airavata.kafka.broker-url", () -> kafkaUrl);
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaIntegrationTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String bootstrapServers;

    @Autowired
    private AiravataServerProperties properties;

    @BeforeAll
    public static void setupKafka() {
        bootstrapServers = TestcontainersConfig.getKafkaBootstrapServers();
        logger.info("Kafka container initialized at: {}", bootstrapServers);
    }

    @Test
    public void testKafkaConnection() throws Exception {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            assertNotNull(producer, "Producer should be created");
            logger.info("Kafka connection test passed");
        }
    }

    @Test
    public void testMessagePublishAndConsume() throws Exception {
        String topicName = "test-topic-" + AiravataUtils.getUniqueTimestamp().getTime();
        String testMessage = "Test message from integration test";
        String testKey = "test-key";

        // Create producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create consumer
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-consumer-group-" + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        CountDownLatch messageReceived = new CountDownLatch(1);
        String[] receivedMessage = new String[1];

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish message FIRST (since auto.offset.reset=earliest, consumer will see it)
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, testKey, testMessage);
            RecordMetadata metadata = producer.send(record).get();

            assertNotNull(metadata, "Message should be sent successfully");
            logger.info(
                    "Message sent to topic: {}, partition: {}, offset: {}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());

            // Now poll for the message
            long startTime = System.currentTimeMillis();
            while (receivedMessage[0] == null && System.currentTimeMillis() - startTime < 10000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    logger.info(
                            "Received message: key={}, value={}, offset={}",
                            consumerRecord.key(),
                            consumerRecord.value(),
                            consumerRecord.offset());
                    receivedMessage[0] = consumerRecord.value();
                    messageReceived.countDown();
                }
            }

            // Wait for message to be received
            boolean received = messageReceived.await(5, TimeUnit.SECONDS);

            assertTrue(received, "Message should be received within timeout");
            assertEquals(testMessage, receivedMessage[0], "Received message should match sent message");
        }

        logger.info("Message publish and consume test passed");
    }

    @Test
    public void testMultipleMessages() throws Exception {
        String topicName =
                "test-topic-multi-" + AiravataUtils.getUniqueTimestamp().getTime();
        int messageCount = 10;

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-consumer-group-multi-"
                        + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        CountDownLatch messagesReceived = new CountDownLatch(messageCount);
        int[] receivedCount = new int[1];

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish multiple messages FIRST (since auto.offset.reset=earliest, consumer will see them)
            for (int i = 0; i < messageCount; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "key-" + i, "message-" + i);
                producer.send(record);
            }
            producer.flush();
            logger.info("Published {} messages", messageCount);

            // Now poll for the messages
            long startTime = System.currentTimeMillis();
            while (receivedCount[0] < messageCount && System.currentTimeMillis() - startTime < 30000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    receivedCount[0]++;
                    logger.debug("Received message {}/{}: {}", receivedCount[0], messageCount, record.value());
                    messagesReceived.countDown();
                }
            }

            // Wait for all messages
            boolean allReceived = messagesReceived.await(5, TimeUnit.SECONDS);

            assertTrue(allReceived, "All messages should be received within timeout");
            assertEquals(messageCount, receivedCount[0], "Should receive all messages");
        }

        logger.info("Multiple messages test passed - {} messages sent and received", messageCount);
    }

    @Test
    public void testTopicCreation() throws Exception {
        String topicName =
                "test-topic-create-" + AiravataUtils.getUniqueTimestamp().getTime();

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            // Publishing to a non-existent topic will auto-create it
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "key", "value");

            assertDoesNotThrow(
                    () -> {
                        RecordMetadata metadata = producer.send(record).get();
                        assertNotNull(metadata, "Topic should be auto-created and message sent");
                        logger.info("Topic {} auto-created successfully", topicName);
                    },
                    "Topic creation and message publishing should not throw exception");
        }

        logger.info("Topic creation test passed");
    }

    @Test
    @DisplayName("Should publish and consume ExperimentStatusChangeEvent")
    void shouldPublishAndConsumeExperimentStatusChangeEvent() throws Exception {
        String topicName =
                "test-experiment-topic-" + AiravataUtils.getUniqueTimestamp().getTime();
        String experimentId = "test-exp-kafka";
        String gatewayId = "test-gateway";

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-experiment-group-" + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        CountDownLatch messageReceived = new CountDownLatch(1);
        ExperimentStatusChangeEvent[] receivedEvent = new ExperimentStatusChangeEvent[1];

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish message FIRST (since auto.offset.reset=earliest, consumer will see it)
            MessageContext messageContext = TestMessagingUtils.createExperimentStatusChangeMessage(
                    experimentId, gatewayId, ExperimentState.COMPLETED);
            String messageValue = objectMapper.writeValueAsString(new MessageContext.Wrapper(messageContext));
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, experimentId, messageValue);
            producer.send(record).get();
            logger.info("Published ExperimentStatusChangeEvent message");

            // Now poll for the message
            long startTime = System.currentTimeMillis();
            while (receivedEvent[0] == null && System.currentTimeMillis() - startTime < 10000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    MessageContext.Wrapper wrapper =
                            objectMapper.readValue(consumerRecord.value(), MessageContext.Wrapper.class);
                    MessageContext ctx = wrapper.toMessageContext();
                    if (ctx.getType() == MessageType.EXPERIMENT) {
                        receivedEvent[0] = (ExperimentStatusChangeEvent) ctx.getEvent();
                        logger.info(
                                "Received ExperimentStatusChangeEvent: experimentId={}, state={}",
                                receivedEvent[0].getExperimentId(),
                                receivedEvent[0].getState());
                        messageReceived.countDown();
                    }
                }
            }

            assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Message should be received");
            assertNotNull(receivedEvent[0], "Event should be deserialized");
            assertEquals(experimentId, receivedEvent[0].getExperimentId(), "Experiment ID should match");
            assertEquals(ExperimentState.COMPLETED, receivedEvent[0].getState(), "State should match");
        }
    }

    @Test
    @DisplayName("Should publish and consume ProcessStatusChangeEvent")
    void shouldPublishAndConsumeProcessStatusChangeEvent() throws Exception {
        String topicName =
                "test-process-topic-" + AiravataUtils.getUniqueTimestamp().getTime();
        String processId = "test-process-kafka";
        String experimentId = "test-exp";
        String gatewayId = "test-gateway";

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-process-group-" + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        CountDownLatch messageReceived = new CountDownLatch(1);
        ProcessStatusChangeEvent[] receivedEvent = new ProcessStatusChangeEvent[1];

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish message FIRST (since auto.offset.reset=earliest, consumer will see it)
            MessageContext messageContext = TestMessagingUtils.createProcessStatusChangeMessage(
                    processId, experimentId, gatewayId, ProcessState.EXECUTING);
            String messageValue = objectMapper.writeValueAsString(new MessageContext.Wrapper(messageContext));
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, processId, messageValue);
            producer.send(record).get();
            logger.info("Published ProcessStatusChangeEvent message");

            // Now poll for the message
            long startTime = System.currentTimeMillis();
            while (receivedEvent[0] == null && System.currentTimeMillis() - startTime < 10000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    MessageContext.Wrapper wrapper =
                            objectMapper.readValue(consumerRecord.value(), MessageContext.Wrapper.class);
                    MessageContext ctx = wrapper.toMessageContext();
                    if (ctx.getType() == MessageType.PROCESS) {
                        receivedEvent[0] = (ProcessStatusChangeEvent) ctx.getEvent();
                        logger.info(
                                "Received ProcessStatusChangeEvent: processId={}, state={}",
                                receivedEvent[0].getProcessIdentity().getProcessId(),
                                receivedEvent[0].getState());
                        messageReceived.countDown();
                    }
                }
            }

            assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Message should be received");
            assertNotNull(receivedEvent[0], "Event should be deserialized");
            assertEquals(processId, receivedEvent[0].getProcessIdentity().getProcessId(), "Process ID should match");
            assertEquals(ProcessState.EXECUTING, receivedEvent[0].getState(), "State should match");
        }
    }

    @Test
    @DisplayName("Should publish and consume JobStatusChangeEvent")
    void shouldPublishAndConsumeJobStatusChangeEvent() throws Exception {
        String topicName =
                "test-job-topic-" + AiravataUtils.getUniqueTimestamp().getTime();
        String jobId = "test-job-kafka";
        String taskId = "test-task";
        String processId = "test-process";
        String experimentId = "test-exp";
        String gatewayId = "test-gateway";

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-job-group-" + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        CountDownLatch messageReceived = new CountDownLatch(1);
        JobStatusChangeEvent[] receivedEvent = new JobStatusChangeEvent[1];

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish message FIRST (since auto.offset.reset=earliest, consumer will see it)
            JobIdentifier jobIdentifier = new JobIdentifier(jobId, taskId, processId, experimentId, gatewayId);
            JobStatusChangeEvent event = new JobStatusChangeEvent(JobState.ACTIVE, jobIdentifier);
            MessageContext messageContext = new MessageContext(event, MessageType.JOB, "test-job-msg-id", gatewayId);
            String messageValue = objectMapper.writeValueAsString(new MessageContext.Wrapper(messageContext));
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, jobId, messageValue);
            producer.send(record).get();
            logger.info("Published JobStatusChangeEvent message");

            // Now poll for the message
            long startTime = System.currentTimeMillis();
            while (receivedEvent[0] == null && System.currentTimeMillis() - startTime < 10000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    MessageContext.Wrapper wrapper =
                            objectMapper.readValue(consumerRecord.value(), MessageContext.Wrapper.class);
                    MessageContext ctx = wrapper.toMessageContext();
                    if (ctx.getType() == MessageType.JOB) {
                        receivedEvent[0] = (JobStatusChangeEvent) ctx.getEvent();
                        logger.info(
                                "Received JobStatusChangeEvent: jobId={}, state={}",
                                receivedEvent[0].getJobIdentity().getJobId(),
                                receivedEvent[0].getState());
                        messageReceived.countDown();
                    }
                }
            }

            assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Message should be received");
            assertNotNull(receivedEvent[0], "Event should be deserialized");
            assertEquals(jobId, receivedEvent[0].getJobIdentity().getJobId(), "Job ID should match");
            assertEquals(JobState.ACTIVE, receivedEvent[0].getState(), "State should match");
        }
    }

    @Test
    @DisplayName("Should verify message ordering for state transitions")
    void shouldVerifyMessageOrderingForStateTransitions() throws Exception {
        String topicName =
                "test-ordering-topic-" + AiravataUtils.getUniqueTimestamp().getTime();
        String experimentId = "test-exp-ordering";
        String gatewayId = "test-gateway";

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-ordering-group-" + AiravataUtils.getUniqueTimestamp().getTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        List<ExperimentState> expectedStates = new ArrayList<>();
        expectedStates.add(ExperimentState.CREATED);
        expectedStates.add(ExperimentState.VALIDATED);
        expectedStates.add(ExperimentState.LAUNCHED);

        CountDownLatch messagesReceived = new CountDownLatch(expectedStates.size());
        List<ExperimentState> receivedStates = new ArrayList<>();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            consumer.subscribe(Collections.singletonList(topicName));
            
            // Force partition assignment by doing an initial poll
            consumer.poll(Duration.ofMillis(100));
            logger.info("Consumer subscribed and partition assignment triggered");

            // Publish all messages FIRST (since auto.offset.reset=earliest, consumer will see them)
            for (ExperimentState state : expectedStates) {
                MessageContext messageContext =
                        TestMessagingUtils.createExperimentStatusChangeMessage(experimentId, gatewayId, state);
                String messageValue = objectMapper.writeValueAsString(new MessageContext.Wrapper(messageContext));
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, experimentId, messageValue);
                producer.send(record).get();
            }
            producer.flush();
            logger.info("Published {} state transition messages", expectedStates.size());

            // Now poll for the messages
            long startTime = System.currentTimeMillis();
            while (receivedStates.size() < expectedStates.size() && System.currentTimeMillis() - startTime < 30000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    MessageContext.Wrapper wrapper =
                            objectMapper.readValue(consumerRecord.value(), MessageContext.Wrapper.class);
                    MessageContext ctx = wrapper.toMessageContext();
                    if (ctx.getType() == MessageType.EXPERIMENT) {
                        ExperimentStatusChangeEvent event =
                                (ExperimentStatusChangeEvent) ctx.getEvent();
                        receivedStates.add(event.getState());
                        messagesReceived.countDown();
                    }
                }
            }

            assertTrue(messagesReceived.await(5, TimeUnit.SECONDS), "All messages should be received");
            assertEquals(expectedStates.size(), receivedStates.size(), "Should receive all states");
            for (int i = 0; i < expectedStates.size(); i++) {
                assertEquals(expectedStates.get(i), receivedStates.get(i), "State order should be preserved");
            }
        }
    }
}
