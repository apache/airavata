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

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Validates all Airavata configuration properties at startup.
 *
 * <p>Provides argparse-like behavior: fails fast with clear error messages
 * if required properties are missing or invalid.
 *
 * <p>This configuration runs at application startup and logs all property
 * values (redacting sensitive ones) for debugging purposes.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class PropertiesValidationConfig {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidationConfig.class);

    private final AiravataServerProperties properties;

    public PropertiesValidationConfig(AiravataServerProperties properties) {
        this.properties = properties;
        // Validate immediately on construction - fail fast
        if (properties == null) {
            throw new IllegalStateException("CRITICAL: AiravataServerProperties bean is null. "
                    + "This usually means @EnableConfigurationProperties is not set or "
                    + "the properties file is not being loaded correctly.");
        }
    }

    @PostConstruct
    public void validateProperties() {
        logger.info("=== Airavata Configuration Validation ===");

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate top-level properties (all under airavata.* prefix)
        validateNotNull(properties.security(), "airavata.security", errors);
        validateNotNull(properties.services(), "airavata.services", errors);

        // Validate security properties
        if (properties.security() != null) {
            if (properties.security().iam() != null
                    && properties.security().iam().enabled()) {
                validateNotEmpty(properties.security().iam().serverUrl(), "airavata.security.iam.server-url", errors);
            }
        }

        // Validate service properties
        if (properties.services() != null) {
            // Validate API services
            if (properties.services().thrift() != null
                    && properties.services().thrift().enabled()) {
                validateNotNull(properties.services().thrift().server(), "airavata.services.thrift.server", errors);
            }
            if (properties.services().rest() != null
                    && properties.services().rest().enabled()) {
                validateNotNull(properties.services().rest().server(), "airavata.services.rest.server", errors);
            }
        }

        // Log all warnings
        for (String warning : warnings) {
            logger.warn("Config warning: {}", warning);
        }

        // If there are validation errors, fail fast
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("\n\n");
            sb.append("=== CONFIGURATION VALIDATION FAILED ===\n");
            sb.append("The following required properties are missing or invalid:\n\n");
            for (String error : errors) {
                sb.append("  - ").append(error).append("\n");
            }
            sb.append("\nPlease ensure all required properties are set in application.properties.\n");
            sb.append("================================================\n");

            throw new IllegalStateException(sb.toString());
        }

        // Log successful validation
        logger.info("Configuration validation passed. Logging active configuration:");
        logConfiguration();
    }

    private void validateNotNull(Object value, String propertyPath, List<String> errors) {
        if (value == null) {
            errors.add(propertyPath + " is required but not set");
        }
    }

    private void validateNotEmpty(String value, String propertyPath, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(propertyPath + " is required but empty or not set");
        }
    }

    private void logConfiguration() {
        // Log service enablement
        if (properties.services() != null) {
            var services = properties.services();
            logger.info("  Services:");
            logger.info(
                    "    thrift.enabled = {}",
                    services.thrift() != null && services.thrift().enabled());
            logger.info(
                    "    rest.enabled = {}",
                    services.rest() != null && services.rest().enabled());
            logger.info(
                    "    controller.enabled = {}",
                    services.controller() != null && services.controller().enabled());
            logger.info(
                    "    participant.enabled = {}",
                    services.participant() != null && services.participant().enabled());
            if (services.scheduler() != null) {
                logger.info("    scheduler.enabled = {}", services.scheduler().enabled());
            }
            if (services.monitor() != null) {
                if (services.monitor().email() != null) {
                    logger.info(
                            "    monitor.email.enabled = {}",
                            services.monitor().email().enabled());
                }
                if (services.monitor().compute() != null) {
                    logger.info(
                            "    monitor.compute.enabled = {}",
                            services.monitor().compute().enabled());
                }
            }
        }

        // Log security settings (redacted)
        if (properties.security() != null) {
            logger.info("  Security:");
            if (properties.security().tls() != null) {
                logger.info("    tls.enabled = {}", properties.security().tls().enabled());
            }
            if (properties.security().authzCache() != null) {
                logger.info(
                        "    authzCache.enabled = {}",
                        properties.security().authzCache().enabled());
            }
            if (properties.security().iam() != null) {
                logger.info("    iam.enabled = {}", properties.security().iam().enabled());
                logger.info(
                        "    iam.server-url = {}", properties.security().iam().serverUrl());
            }
        }

        // Log messaging settings
        if (properties.kafka() != null) {
            logger.info("  Kafka:");
            logger.info("    enabled = {}", properties.kafka().enabled());
        }
        if (properties.rabbitmq() != null) {
            logger.info("  RabbitMQ:");
            logger.info("    enabled = {}", properties.rabbitmq().enabled());
        }

        logger.info("=== Configuration Logging Complete ===");
    }
}
