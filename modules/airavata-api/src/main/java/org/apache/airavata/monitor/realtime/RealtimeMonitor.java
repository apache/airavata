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

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

/**
 * Realtime monitor using Spring Kafka's @KafkaListener.
 * Consumes job status messages from Kafka and processes them.
 * 
 * Configure via application.properties:
 *   services.monitor.realtime.enabled=true
 *   services.monitor.realtime.broker-topic=realtime-monitor-topic
 *   services.monitor.realtime.broker-consumer-group=airavata-consumer
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.monitor.realtime", name = "enabled", havingValue = "true")
public class RealtimeMonitor extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);
    private static final String LISTENER_ID = "realtimeMonitorListener";

    private final AiravataServerProperties properties;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final RealtimeComputeStatusParser parser;
    private final AbstractMonitor abstractMonitor;
    private final KafkaListenerEndpointRegistry kafkaListenerRegistry;
    private String publisherId;
    private String brokerTopic;

    public RealtimeMonitor(
            org.apache.airavata.service.registry.RegistryService registryService, 
            AiravataServerProperties properties,
            KafkaListenerEndpointRegistry kafkaListenerRegistry) {
        this.registryService = registryService;
        this.properties = properties;
        this.kafkaListenerRegistry = kafkaListenerRegistry;
        this.abstractMonitor = new AbstractMonitor(registryService, properties);
        this.parser = new RealtimeComputeStatusParser();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        publisherId = properties.services().monitor().compute().realtimePublisherId();
        brokerTopic = properties.services().monitor().realtime().brokerTopic();
    }

    /**
     * Kafka listener for realtime job status messages.
     * Uses SpEL to get topic name from properties.
     */
    @KafkaListener(
            id = LISTENER_ID,
            topics = "#{@airavataServerProperties.services.monitor.realtime.brokerTopic}",
            groupId = "#{@airavataServerProperties.services.monitor.realtime.brokerConsumerGroup}",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "false"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            process(record.key(), record.value());
        } catch (Exception e) {
            logger.error("Error while processing message {}", record.value(), e);
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
        logger.info("Starting RealtimeMonitor Kafka listener for topic: {}", brokerTopic);
        // Start the Kafka listener
        if (kafkaListenerRegistry.getListenerContainer(LISTENER_ID) != null) {
            kafkaListenerRegistry.getListenerContainer(LISTENER_ID).start();
            logger.info("RealtimeMonitor Kafka listener started");
        } else {
            logger.warn("Kafka listener container not found - Kafka may not be configured");
        }
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("Stopping RealtimeMonitor Kafka listener");
        if (kafkaListenerRegistry.getListenerContainer(LISTENER_ID) != null) {
            kafkaListenerRegistry.getListenerContainer(LISTENER_ID).stop();
            logger.info("RealtimeMonitor Kafka listener stopped");
        }
    }

    @Override
    public boolean isRunning() {
        if (kafkaListenerRegistry.getListenerContainer(LISTENER_ID) != null) {
            return kafkaListenerRegistry.getListenerContainer(LISTENER_ID).isRunning();
        }
        return false;
    }

    protected org.apache.airavata.service.registry.RegistryService getRegistryService() {
        return registryService;
    }
}
