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
package org.apache.airavata.restapi.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Configuration class that maps scoped rest.* properties to Spring Boot standard properties.
 * This allows rest-api specific properties to be clearly scoped while still working
 * with Spring Boot auto-configuration.
 *
 * Uses ApplicationEnvironmentPreparedEvent to ensure mapping happens before auto-configuration runs.
 */
@Configuration
public class RestApiConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RestApiConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Check if rest service is enabled
        String enabled = environment.getProperty("services.rest.enabled", "false");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }
        mapScopedProperties(environment);
    }

    private void mapScopedProperties(ConfigurableEnvironment environment) {
        Map<String, Object> mappedProperties = new HashMap<>();

        // Map services.rest.server.* to server.*
        mapProperty("services.rest.server.port", "server.port", mappedProperties, environment);

        if (!mappedProperties.isEmpty()) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("restApiMappedProperties", mappedProperties));
            logger.debug("Mapped {} rest.* properties to Spring Boot properties", mappedProperties.size());
        }
    }

    private void mapProperty(
            String scopedKey,
            String standardKey,
            Map<String, Object> mappedProperties,
            ConfigurableEnvironment environment) {
        String value = environment.getProperty(scopedKey);
        if (value != null) {
            mappedProperties.put(standardKey, value);
            logger.trace("Mapped {}={} to {}", scopedKey, value, standardKey);
        }
    }
}
