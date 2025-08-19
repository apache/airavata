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

public class RealtimeMonitor extends AbstractMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    private final RealtimeJobStatusParser parser;
    private final String publisherId;
    private final String brokerTopic;

    public RealtimeMonitor() throws ApplicationSettingsException {
        parser = new RealtimeJobStatusParser();
        publisherId = ServerSettings.getSetting("job.monitor.realtime.publisher.id");
        brokerTopic = ServerSettings.getSetting("realtime.monitor.broker.topic");
    }

    private Consumer<String, String> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("realtime.monitor.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(brokerTopic));
        return consumer;
    }

    private void runConsumer() {
        final Consumer<String, String> consumer;
        try {
            consumer = createConsumer();
        } catch (ApplicationSettingsException e) {
            logger.error("Error while creating consumer", e);
            return;
        }

        try {
          while (true) {
              final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
              RegistryService.Iface registry = getRegistry();
              consumerRecords.forEach(record -> {
                  try {
                      process(record.key(), record.value(), registry);
                  } catch (Exception e) {
                      logger.error("Error while processing message {}", record.value(), e);
                  }
              });
              consumer.commitAsync();
              if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("RealtimeMonitor is interrupted!");
              }
          }
        } catch (InterruptedException ex) {
          logger.error("RealtimeMonitor is interrupted! reason: " + ex, ex);
        } finally {
          consumer.close();
        }
    }

    private void process(String key, String value, RegistryService.Iface registry) throws MonitoringException {
        logger.info("received post from {} on {}: {}->{}", publisherId, brokerTopic, key, value);
        JobStatusResult statusResult = parser.parse(value, publisherId, registry);
        if (statusResult != null) {
            logger.info("Submitting message to job monitor queue");
            submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }
    }

    @Override
    public void run() {
        var thread = new Thread(this::runConsumer, this.getClass().getSimpleName());
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(thread::interrupt));
    }
}
