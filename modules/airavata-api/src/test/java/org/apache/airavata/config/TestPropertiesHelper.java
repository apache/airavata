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
package org.apache.airavata.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for managing test properties.
 * Provides utilities to get test-specific property overrides for Testcontainers services.
 * 
 * <p>Note: Since AiravataServerProperties is an immutable record, properties cannot be
 * mutated at runtime. Use @TestPropertySource annotations in tests to override properties.
 */
public class TestPropertiesHelper {

    private static final Logger logger = LoggerFactory.getLogger(TestPropertiesHelper.class);

    /**
     * Gets test property overrides for messaging services.
     * These properties should be used to override default messaging configuration
     * when using Testcontainers.
     *
     * @return Map of property keys to values for Kafka, RabbitMQ, and Zookeeper
     */
    public static Map<String, String> getMessagingTestProperties() {
        Map<String, String> properties = new HashMap<>();

        // Initialize containers and get connection strings
        String kafkaBootstrapServers = TestcontainersConfig.getKafkaBootstrapServers();
        String rabbitMQUrl = TestcontainersConfig.getRabbitMQUrl();
        String zookeeperConnection = TestcontainersConfig.getZookeeperConnectionString();

        // Set Kafka properties
        properties.put("kafka.broker-url", kafkaBootstrapServers);
        logger.debug("Test property: kafka.broker-url={}", kafkaBootstrapServers);

        // Set RabbitMQ properties
        properties.put("rabbitmq.broker-url", rabbitMQUrl);
        logger.debug("Test property: rabbitmq.broker-url={}", rabbitMQUrl);

        // Set Zookeeper properties
        properties.put("zookeeper.server.connection", zookeeperConnection);
        logger.debug("Test property: zookeeper.server.connection={}", zookeeperConnection);

        return properties;
    }

    /**
     * Gets all test property overrides including database and messaging services.
     *
     * @return Map of all test property overrides
     */
    public static Map<String, String> getAllTestProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.putAll(getMessagingTestProperties());

        // Database properties are handled by TestcontainersConfig DataSource beans
        // No need to override database URLs as they're injected directly

        return properties;
    }

    /**
     * Logs the current property values for debugging.
     * Since AiravataServerProperties is immutable, this only reads values.
     *
     * @param properties AiravataServerProperties to log
     */
    public static void logProperties(AiravataServerProperties properties) {
        if (properties == null) {
            logger.debug("AiravataServerProperties is null");
            return;
        }
        
        if (properties.kafka() != null) {
            logger.debug("kafka.brokerUrl: {}", properties.kafka().brokerUrl());
        }
        if (properties.rabbitmq() != null) {
            logger.debug("rabbitmq.brokerUrl: {}", properties.rabbitmq().brokerUrl());
        }
        if (properties.zookeeper() != null && properties.zookeeper().server() != null) {
            logger.debug("zookeeper.server.connection: {}", properties.zookeeper().server().connection());
        }
    }
}
