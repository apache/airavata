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
 * Airavata API Configuration - Part of the unified HTTP server.
 *
 * <p>This configuration class sets up the Airavata API as part of the unified HTTP server
 * that runs on port 8080 (configurable via {@code airavata.services.http.server.port}).
 * The Airavata API provides HTTP endpoints that serve the same core API functionalities,
 * accessing the same internal services.
 *
 * <p><b>External API:</b> This is part of one of four external API layers in Airavata:
 * <ul>
 *   <li>HTTP Server (port 8080):
 *       <ul>
 *         <li>Airavata API (this module) - HTTP Endpoints for Airavata API functions</li>
 *         <li>File API (at /api/v1/files) - HTTP Endpoints for file upload/download</li>
 *         <li>Agent API - HTTP Endpoints for interactive job contexts</li>
 *         <li>Research API - HTTP Endpoints for use by research hub</li>
 *       </ul>
 *   </li>
 *   <li>gRPC Server (port 9090) - For airavata binaries to open persistent channels with airavata APIs</li>
 *   <li>Dapr gRPC (port 50001) - Sidecar for pub/sub, state, and workflow execution</li>
 * </ul>
 *
 * <p><b>Airavata API Endpoints:</b> The Airavata API provides HTTP endpoints including:
 * <ul>
 *   <li>Experiments, Processes, Jobs</li>
 *   <li>Applications and Deployments</li>
 *   <li>Compute and Storage Resources</li>
 *   <li>Projects, Gateways, Groups</li>
 *   <li>Workflows</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code airavata.services.rest.server.port} - Maps to Spring Boot {@code server.port}</li>
 *   <li>Airavata API runs on the unified HTTP server port (8080 by default)</li>
 * </ul>
 *
 * <p><b>Internal Services:</b> The Airavata API accesses the same internal services
 * (Orchestrator, Registry, Profile Service, Sharing Registry, Credential Store).
 *
 * <p>Uses {@code ApplicationEnvironmentPreparedEvent} to ensure property mapping happens
 * before Spring Boot auto-configuration runs.
 */
@Configuration
public class RestApiConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RestApiConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        mapScopedProperties(environment);
    }

    private void mapScopedProperties(ConfigurableEnvironment environment) {
        var mappedProperties = new HashMap<String, Object>();

        // Map services.rest.server.* to server.*
        mapProperty("services.rest.server.port", "server.port", mappedProperties, environment);

        // Map services.fileserver.spring.servlet.multipart.* to spring.servlet.multipart.*
        mapProperty(
                "services.fileserver.spring.servlet.multipart.max-file-size",
                "spring.servlet.multipart.max-file-size",
                mappedProperties,
                environment);
        mapProperty(
                "services.fileserver.spring.servlet.multipart.max-request-size",
                "spring.servlet.multipart.max-request-size",
                mappedProperties,
                environment);

        // Map services.agent.spring.servlet.multipart.* to spring.servlet.multipart.*
        // (agent multipart settings override fileserver ones if both are present)
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

        // Map database.catalog.* to spring.datasource.*
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

        if (!mappedProperties.isEmpty()) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("restApiMappedProperties", mappedProperties));
            logger.debug("Mapped {} rest/file properties to Spring Boot properties", mappedProperties.size());
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
