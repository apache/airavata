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
 * mutated at runtime. Use {@code @DynamicPropertySource} for Testcontainer-provided services
 * or inline properties via {@code @SpringBootTest(properties = {...})} for static overrides.
 */
public class TestPropertiesHelper {

    private static final Logger logger = LoggerFactory.getLogger(TestPropertiesHelper.class);

    /**
     * Gets test property overrides for messaging/state backends.
     * Kafka, RabbitMQ, and Zookeeper have been replaced by Redis for Dapr.
     * Use {@link TestcontainersConfig#getRedisHost()} when a test needs Redis.
     *
     * @return Map of property overrides; currently empty. Dapr component metadata
     *         (redisHost) is configured via component YAML or --resources-path.
     */
    public static Map<String, String> getMessagingTestProperties() {
        return new HashMap<>();
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

        // Log IAM properties (debug level)
        if (properties.security() != null && properties.security().iam() != null) {
            logger.debug(
                    "security.iam: enabled={}, serverUrl={}",
                    properties.security().iam().enabled(),
                    properties.security().iam().serverUrl());
            if (properties.security().iam().superAdmin() != null) {
                logger.debug(
                        "security.iam.superAdmin: username={}",
                        properties.security().iam().superAdmin().username());
            }
        }
    }
}
