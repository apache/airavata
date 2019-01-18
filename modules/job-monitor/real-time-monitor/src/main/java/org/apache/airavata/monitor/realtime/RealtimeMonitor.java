/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.monitor.realtime;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.airavata.monitor.realtime.parser.RealtimeJobStatusParser;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class RealtimeMonitor extends AbstractMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    private RealtimeJobStatusParser parser;


    public RealtimeMonitor() throws ApplicationSettingsException {
        parser = new RealtimeJobStatusParser();
    }

    private Consumer<String, String> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("realtime.monitor.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("realtime.monitor.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("realtime.monitor.broker.topic")));
        return consumer;
    }

    private void runConsumer() throws ApplicationSettingsException {
        final Consumer<String, String> consumer = createConsumer();

        while (true) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
            consumerRecords.forEach(record -> {
                RegistryService.Client registryClient = getRegistryClientPool().getResource();
                try {
                    process(record.value(), registryClient);
                    getRegistryClientPool().returnResource(registryClient);
                } catch (Exception e) {
                    logger.error("Error while processing message " + record.value(), e);
                    getRegistryClientPool().returnBrokenResource(registryClient);
                    // ignore this error
                }
            });

            consumer.commitAsync();
        }
    }

    private void process(String value, RegistryService.Client registryClient) throws MonitoringException {
        logger.info("Received data " + value);
        JobStatusResult statusResult = parser.parse(value, registryClient);
        if (statusResult != null) {
            logger.info("Submitting message to job monitor queue");
            submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }

    }

    public static void main(String args[]) throws ApplicationSettingsException {
        new RealtimeMonitor().runConsumer();
    }

}
