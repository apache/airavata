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
package org.apache.airavata.grpc.config;

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
 * Configuration class that maps scoped agent gRPC properties to Spring Boot standard properties.
 *
 * <p>Uses ApplicationEnvironmentPreparedEvent to ensure mapping happens before auto-configuration runs.
 */
@Configuration
public class GrpcApiConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GrpcApiConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        var environment = event.getEnvironment();
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
        mappedProperties.put("spring.boot.grpc.server.property-mapper.enabled", "false");

        // Map services.agent.grpc.* to spring.grpc.server.*
        mapProperty("services.agent.grpc.port", "spring.grpc.server.port", mappedProperties, environment);
        mapProperty(
                "services.agent.grpc.max-inbound-message-size",
                "spring.grpc.server.max-inbound-message-size",
                mappedProperties,
                environment);

        // Keepalive defaults
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
                    .addFirst(new MapPropertySource("grpcApiMappedProperties", mappedProperties));
            logger.debug("Mapped {} gRPC properties to Spring Boot properties", mappedProperties.size());
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
