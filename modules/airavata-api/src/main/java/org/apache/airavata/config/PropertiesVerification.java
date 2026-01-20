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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Verifies that all properties are loaded correctly from AiravataServerProperties.
 * Runs early in the startup sequence to validate configuration.
 * Disabled in test profile since tests use minimal property sets.
 */
@Component
@Profile("!test")
public class PropertiesVerification implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesVerification.class);

    private final AiravataServerProperties properties;

    public PropertiesVerification(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Verifying AiravataServerProperties ===");

        // Verify API Servers
        logger.info(
                "Thrift Server - Port: {}",
                properties.services().thrift().server().port());
        logger.info(
                "REST Server - Port: {}", properties.services().rest().server().port());
        // Database configuration is now managed by Spring Boot standard properties
        // (spring.datasource.*) and is configured via JpaConfig
        // Verify Default Registry
        logger.info(
                "Default Registry - Gateway: {}, User: {}",
                properties.defaultGateway(),
                properties.security().iam().superAdmin().username());
        // Verify Sharing
        logger.info("Sharing - Enabled: {}", properties.sharing().enabled());
        // Verify Zookeeper
        logger.info(
                "Zookeeper - Embedded: {}, Connection: {}",
                properties.zookeeper().embedded(),
                properties.zookeeper().server().connection());
        // Verify RabbitMQ
        logger.info(
                "RabbitMQ - Broker URL: {}, Experiment Queue: {}",
                properties.rabbitmq().brokerUrl(),
                properties.rabbitmq().experimentLaunchQueueName());
        logger.info("=== Properties verification complete ===");
    }
}
