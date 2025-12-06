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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Verifies that all properties are loaded correctly from AiravataServerProperties.
 * Runs early in the startup sequence to validate configuration.
 */
@Component
@Order(1)
public class PropertiesVerification implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesVerification.class);

    @Autowired
    private AiravataServerProperties properties;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Verifying AiravataServerProperties ===");

        // Verify API Server
        logger.info(
                "API Server - Host: {}, Port: {}",
                properties.getApiServer().getHost(),
                properties.getApiServer().getPort());
        // Verify Database configurations
        logger.info(
                "AppCatalog DB - URL: {}, Driver: {}",
                properties.getDatabase().getAppCatalog().getJdbcUrl(),
                properties.getDatabase().getAppCatalog().getJdbcDriver());
        logger.info(
                "Registry DB - URL: {}, Driver: {}",
                properties.getDatabase().getRegistry().getJdbcUrl(),
                properties.getDatabase().getRegistry().getJdbcDriver());
        // Verify Default Registry
        logger.info(
                "Default Registry - Gateway: {}, User: {}",
                properties.getDefaultRegistry().getGateway(),
                properties.getDefaultRegistry().getUser());
        // Verify Sharing
        logger.info("Sharing - Enabled: {}", properties.getSharing().isEnabled());
        // Verify Zookeeper
        logger.info(
                "Zookeeper - Embedded: {}, Connection: {}",
                properties.getZookeeper().isEmbedded(),
                properties.getZookeeper().getServerConnection());
        // Verify RabbitMQ
        logger.info(
                "RabbitMQ - Broker URL: {}, Experiment Queue: {}",
                properties.getRabbitMQ().getBrokerUrl(),
                properties.getRabbitMQ().getExperimentLaunchQueueName());
        logger.info("=== Properties verification complete ===");
    }
}
