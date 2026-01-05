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
package org.apache.airavata.research.service.config;

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
 * Configuration class that maps scoped research.* properties to Spring Boot standard properties.
 * This allows research-service specific properties to be clearly scoped while still working
 * with Spring Boot auto-configuration.
 *
 * Uses ApplicationEnvironmentPreparedEvent to ensure mapping happens before auto-configuration runs.
 */
@Configuration
public class ResearchServiceConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ResearchServiceConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Check if research service is enabled
        String enabled = environment.getProperty("services.research.enabled", "true");
        Map<String, Object> mappedProperties = new HashMap<>();
        
        if ("true".equalsIgnoreCase(enabled)) {
            // Enable gRPC server and map properties when research service is enabled
            mappedProperties.put("spring.grpc.server.enabled", "true");
            mapScopedProperties(environment, mappedProperties);
        } else {
            // Disable gRPC server when research service is disabled
            mappedProperties.put("spring.grpc.server.enabled", "false");
        }
        
        if (!mappedProperties.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("researchServiceGrpcConfig", mappedProperties));
            logger.debug("Set spring.grpc.server.enabled={} based on services.research.enabled={}", 
                    mappedProperties.get("spring.grpc.server.enabled"), enabled);
        }
    }

    private void mapScopedProperties(ConfigurableEnvironment environment, Map<String, Object> mappedProperties) {
        // Map services.research.grpc.* to spring.grpc.server.*
        mapProperty("services.research.grpc.port", "spring.grpc.server.port", mappedProperties, environment);
        
        // Map keepalive properties if they exist in scoped form
        mapProperty("services.research.grpc.keepalive-time", "spring.grpc.server.keepalive-time", mappedProperties, environment);
        mapProperty("services.research.grpc.keepalive-timeout", "spring.grpc.server.keepalive-timeout", mappedProperties, environment);
        mapProperty("services.research.grpc.permit-keepalive-without-calls", "spring.grpc.server.permit-keepalive-without-calls", mappedProperties, environment);

        // Map services.research.spring.servlet.multipart.* to spring.servlet.multipart.*
        mapProperty(
                "services.research.spring.servlet.multipart.max-file-size",
                "spring.servlet.multipart.max-file-size",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.spring.servlet.multipart.max-request-size",
                "spring.servlet.multipart.max-request-size",
                mappedProperties,
                environment);

        // Map services.research.springdoc.* to springdoc.*
        mapProperty(
                "services.research.springdoc.api-docs.enabled",
                "springdoc.api-docs.enabled",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.path",
                "springdoc.swagger-ui.path",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.operationsSorter",
                "springdoc.swagger-ui.operationsSorter",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.tagsSorter",
                "springdoc.swagger-ui.tagsSorter",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.doc-expansion",
                "springdoc.swagger-ui.doc-expansion",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.oauth.use-pkce-with-authorization-code-grant",
                "springdoc.swagger-ui.oauth.use-pkce-with-authorization-code-grant",
                mappedProperties,
                environment);
        mapProperty(
                "services.research.springdoc.swagger-ui.oauth.client-id",
                "springdoc.swagger-ui.oauth.client-id",
                mappedProperties,
                environment);

        if (!mappedProperties.isEmpty()) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("researchServiceMappedProperties", mappedProperties));
            logger.debug("Mapped {} research.* properties to Spring Boot properties", mappedProperties.size());
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
