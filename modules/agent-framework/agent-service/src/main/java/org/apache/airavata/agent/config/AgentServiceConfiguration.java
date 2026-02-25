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
package org.apache.airavata.agent.config;

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
        var environment = event.getEnvironment();
        // Check if agent service is enabled
        var enabled = environment.getProperty("services.agent.enabled", "true");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }
        mapScopedProperties(environment);
    }

    private void mapScopedProperties(ConfigurableEnvironment environment) {
        var mappedProperties = new HashMap<String, Object>();

        // Enable gRPC server
        mappedProperties.put("spring.grpc.server.enabled", "true");
        // Disable property-based configuration to avoid NullPointerException
        // We use programmatic ServerBuilderCustomizer instead
        mappedProperties.put("spring.boot.grpc.server.property-mapper.enabled", "false");

        // Keep agent config keys scoped (services.agent.*), but map selected keys into framework defaults
        // that expect grpc.* (agent service runs as a separate process).

        // Map services.agent.spring.servlet.multipart.* to spring.servlet.multipart.*
        mapProperty(
                "services.agent.spring.servlet.multipart.max-file-size",
                "spring.servlet.multipart.max-file-size",
                mappedProperties,
                environment);
        mapProperty(
                "services.agent.spring.servlet.multipart.max-request-size",
                "spring.servlet.multipart.max-request-size",
                mappedProperties,
                environment);

        // Map database.catalog.* to spring.datasource.* (agent uses app_catalog)
        mapProperty("database.catalog.url", "spring.datasource.url", mappedProperties, environment);
        mapProperty("database.catalog.user", "spring.datasource.username", mappedProperties, environment);
        mapProperty("database.catalog.password", "spring.datasource.password", mappedProperties, environment);
        mapProperty(
                "database.catalog.hikari.pool-name",
                "spring.datasource.hikari.pool-name",
                mappedProperties,
                environment);
        mapProperty(
                "database.catalog.hikari.leak-detection-threshold",
                "spring.datasource.hikari.leak-detection-threshold",
                mappedProperties,
                environment);

        // Map services.agent.spring.jpa.* to spring.jpa.*
        mapProperty(
                "services.agent.spring.jpa.hibernate.ddl-auto",
                "spring.jpa.hibernate.ddl-auto",
                mappedProperties,
                environment);
        mapProperty("services.agent.spring.jpa.open-in-view", "spring.jpa.open-in-view", mappedProperties, environment);

        // Enable gRPC server for agent service
        mappedProperties.put("spring.grpc.server.enabled", "true");

        // Map services.agent.grpc.* to spring.grpc.server.*
        mapProperty("services.agent.grpc.port", "spring.grpc.server.port", mappedProperties, environment);
        mapProperty(
                "services.agent.grpc.max-inbound-message-size",
                "spring.grpc.server.max-inbound-message-size",
                mappedProperties,
                environment);

        // Add keepalive defaults to prevent NullPointerException
        // Note: Spring Boot gRPC requires all Duration properties to be non-null
        mapPropertyWithDefault(
                "services.agent.grpc.enable-keep-alive",
                "spring.grpc.server.enable-keep-alive",
                "true",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.keepalive-time",
                "spring.grpc.server.keepalive-time",
                "30s",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.keepalive-timeout",
                "spring.grpc.server.keepalive-timeout",
                "5s",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.permit-keepalive-without-calls",
                "spring.grpc.server.permit-keepalive-without-calls",
                "true",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.permit-keepalive-time",
                "spring.grpc.server.permit-keepalive-time",
                "5m",
                mappedProperties,
                environment);
        // Add additional duration properties that might be required
        mapPropertyWithDefault(
                "services.agent.grpc.max-connection-idle",
                "spring.grpc.server.max-connection-idle",
                "0s",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.max-connection-age",
                "spring.grpc.server.max-connection-age",
                "0s",
                mappedProperties,
                environment);
        mapPropertyWithDefault(
                "services.agent.grpc.max-connection-age-grace",
                "spring.grpc.server.max-connection-age-grace",
                "0s",
                mappedProperties,
                environment);

        if (!mappedProperties.isEmpty()) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("agentServiceMappedProperties", mappedProperties));
            logger.debug("Mapped {} agent.* properties to Spring Boot properties", mappedProperties.size());
        }
    }

    private void mapProperty(
            String scopedKey,
            String standardKey,
            Map<String, Object> mappedProperties,
            ConfigurableEnvironment environment) {
        var value = environment.getProperty(scopedKey);
        if (value != null) {
            mappedProperties.put(standardKey, value);
            logger.trace("Mapped {}={} to {}", scopedKey, value, standardKey);
        }
    }

    private void mapPropertyWithDefault(
            String scopedKey,
            String standardKey,
            String defaultValue,
            Map<String, Object> mappedProperties,
            ConfigurableEnvironment environment) {
        var value = environment.getProperty(scopedKey, defaultValue);
        mappedProperties.put(standardKey, value);
        logger.trace("Mapped {}={} to {} (default: {})", scopedKey, value, standardKey, defaultValue);
    }
}
