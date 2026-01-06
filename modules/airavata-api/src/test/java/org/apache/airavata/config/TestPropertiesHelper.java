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
     * Applies test properties to AiravataServerProperties.
     * This method updates the properties object with Testcontainers service URLs.
     *
     * @param properties AiravataServerProperties to update
     */
    public static void applyTestProperties(AiravataServerProperties properties) {
        Map<String, String> testProps = getAllTestProperties();

        // Update Kafka broker URL
        if (testProps.containsKey("kafka.broker-url")) {
            properties.kafka.brokerUrl = testProps.get("kafka.broker-url");
            logger.debug("Updated kafka.brokerUrl to: {}", properties.kafka.brokerUrl);
        }

        // Update RabbitMQ broker URL
        if (testProps.containsKey("rabbitmq.broker-url")) {
            properties.rabbitmq.brokerUrl = testProps.get("rabbitmq.broker-url");
            logger.debug("Updated rabbitmq.brokerUrl to: {}", properties.rabbitmq.brokerUrl);
        }

        // Update Zookeeper connection
        if (testProps.containsKey("zookeeper.server.connection")) {
            properties.zookeeper.server.connection = testProps.get("zookeeper.server.connection");
            logger.debug("Updated zookeeper.server.connection to: {}", properties.zookeeper.server.connection);
        }
    }
}

