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
package org.apache.airavata.monitor.realtime;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.airavata.monitor.realtime.parser.RealtimeJobStatusParser;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RealtimeMonitor extends AbstractMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    @org.springframework.beans.factory.annotation.Autowired
    private AiravataServerProperties properties;

    private final RealtimeJobStatusParser parser;
    private String publisherId;
    private String brokerTopic;

    public RealtimeMonitor() {
        super();
        parser = new RealtimeJobStatusParser();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        publisherId = properties.services.monitor.job.realtimePublisherId;
        brokerTopic = properties.services.monitor.realtime.brokerTopic;
    }

    private Consumer<String, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafka.brokerUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.services.monitor.realtime.brokerConsumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(brokerTopic));
        return consumer;
    }

    private void runConsumer() {
        final Consumer<String, String> consumer = createConsumer();

        while (true) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            consumerRecords.forEach(record -> {
                try {
                    process(record.key(), record.value());
                } catch (Exception e) {
                    logger.error("Error while processing message {}", record.value(), e);
                }
            });
            consumer.commitAsync();
        }
    }

    private void process(String key, String value) throws MonitoringException {
        logger.info("received post from {} on {}: {}->{}", publisherId, brokerTopic, key, value);
        JobStatusResult statusResult = parser.parse(value, publisherId, getRegistryService());
        if (statusResult != null) {
            logger.info("Submitting message to job monitor queue");
            submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }
    }

    /**
     * Standardized start method for Spring Boot integration.
     * Non-blocking: starts consumer in background thread and returns immediately.
     */
    public void start() {
        Thread consumerThread = new Thread(() -> {
            try {
                runConsumer();
            } catch (Exception e) {
                logger.error("Error in RealtimeMonitor consumer thread", e);
            }
        });
        consumerThread.setName("RealtimeMonitor-Consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    public static void main(String args[]) throws ApplicationSettingsException {
        RealtimeMonitor monitor = new RealtimeMonitor();
        monitor.start();
        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.error("RealtimeMonitor main thread interrupted", e);
        }
    }
}
