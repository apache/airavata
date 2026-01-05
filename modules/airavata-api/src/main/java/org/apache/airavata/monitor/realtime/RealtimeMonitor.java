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
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.monitor.realtime.enabled", havingValue = "true", matchIfMissing = true)
public class RealtimeMonitor extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    private final AiravataServerProperties properties;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final RealtimeComputeStatusParser parser;
    private final AbstractMonitor abstractMonitor;
    private String publisherId;
    private String brokerTopic;
    private Thread consumerThread;
    private volatile Consumer<String, String> consumer;

    public RealtimeMonitor(
            org.apache.airavata.service.registry.RegistryService registryService, AiravataServerProperties properties) {
        this.registryService = registryService;
        this.properties = properties;
        this.abstractMonitor = new AbstractMonitor(registryService, properties);
        parser = new RealtimeComputeStatusParser();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        publisherId = properties.services.monitor.compute.realtimePublisherId;
        brokerTopic = properties.services.monitor.realtime.brokerTopic;
    }

    private Consumer<String, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafka.brokerUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.services.monitor.realtime.brokerConsumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Create the consumer using props.
        consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(brokerTopic));
        return consumer;
    }

    private void runConsumer() {
        consumer = createConsumer();

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
            abstractMonitor.submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }
    }

    @Override
    public String getServerName() {
        return "Realtime Monitor";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 35; // Start after workflow managers
    }

    @Override
    protected void doStart() throws Exception {
        consumerThread = new Thread(() -> {
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

    @Override
    protected void doStop() throws Exception {
        if (consumer != null) {
            try {
                consumer.wakeup();
            } catch (Exception e) {
                logger.warn("Error waking up consumer", e);
            }
        }
        if (consumerThread != null) {
            consumerThread.interrupt();
            try {
                consumerThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for consumer thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                logger.warn("Error closing consumer", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && consumerThread != null && consumerThread.isAlive();
    }

    protected org.apache.airavata.service.registry.RegistryService getRegistryService() {
        return registryService;
    }
}
