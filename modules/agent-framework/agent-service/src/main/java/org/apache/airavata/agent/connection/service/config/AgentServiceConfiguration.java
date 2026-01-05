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
package org.apache.airavata.agent.connection.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class that maps scoped agent.* properties to Spring Boot standard properties.
 * This allows agent-service specific properties to be clearly scoped while still working
 * with Spring Boot auto-configuration.
 * 
 * Uses ApplicationEnvironmentPreparedEvent to ensure mapping happens before auto-configuration runs.
 */
@Configuration
public class AgentServiceConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Check if agent service is enabled
        String enabled = environment.getProperty("services.agent.enabled", "true");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }
        mapScopedProperties(environment);
    }

    private void mapScopedProperties(ConfigurableEnvironment environment) {
        Map<String, Object> mappedProperties = new HashMap<>();

        // Map agent.server.* to server.*
        mapProperty("agent.server.port", "server.port", mappedProperties, environment);
        mapProperty("agent.server.address", "server.address", mappedProperties, environment);

        // Map agent.grpc.server.* to grpc.server.*
        mapProperty("agent.grpc.server.host", "grpc.server.host", mappedProperties, environment);
        mapProperty("agent.grpc.server.port", "grpc.server.port", mappedProperties, environment);
        mapProperty("agent.grpc.server.max-inbound-message-size", "grpc.server.max-inbound-message-size", mappedProperties, environment);

        // Map agent.spring.servlet.multipart.* to spring.servlet.multipart.*
        mapProperty("agent.spring.servlet.multipart.max-file-size", "spring.servlet.multipart.max-file-size", mappedProperties, environment);
        mapProperty("agent.spring.servlet.multipart.max-request-size", "spring.servlet.multipart.max-request-size", mappedProperties, environment);

        // Map agent.spring.datasource.* to spring.datasource.*
        mapProperty("agent.spring.datasource.url", "spring.datasource.url", mappedProperties, environment);
        mapProperty("agent.spring.datasource.username", "spring.datasource.username", mappedProperties, environment);
        mapProperty("agent.spring.datasource.password", "spring.datasource.password", mappedProperties, environment);
        mapProperty("agent.spring.datasource.driver-class-name", "spring.datasource.driver-class-name", mappedProperties, environment);
        mapProperty("agent.spring.datasource.hikari.pool-name", "spring.datasource.hikari.pool-name", mappedProperties, environment);
        mapProperty("agent.spring.datasource.hikari.leak-detection-threshold", "spring.datasource.hikari.leak-detection-threshold", mappedProperties, environment);

        // Map agent.spring.jpa.* to spring.jpa.*
        mapProperty("agent.spring.jpa.hibernate.ddl-auto", "spring.jpa.hibernate.ddl-auto", mappedProperties, environment);
        mapProperty("agent.spring.jpa.open-in-view", "spring.jpa.open-in-view", mappedProperties, environment);

        if (!mappedProperties.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("agentServiceMappedProperties", mappedProperties));
            logger.debug("Mapped {} agent.* properties to Spring Boot properties", mappedProperties.size());
        }
    }

    private void mapProperty(String scopedKey, String standardKey, Map<String, Object> mappedProperties, ConfigurableEnvironment environment) {
        String value = environment.getProperty(scopedKey);
        if (value != null) {
            mappedProperties.put(standardKey, value);
            logger.trace("Mapped {}={} to {}", scopedKey, value, standardKey);
        }
    }
}

