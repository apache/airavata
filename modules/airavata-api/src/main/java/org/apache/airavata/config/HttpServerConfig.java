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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Unified configuration for HTTP server.
 * Maps airavata.services.http.server.port to server.port for Spring Boot's embedded server.
 *
 * <p>All HTTP endpoints (Airavata API, File API, Agent API, Research API)
 * are served through a single unified HTTP server on the configured port.
 */
@Configuration
public class HttpServerConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final AiravataServerProperties properties;

    public HttpServerConfig(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        mapHttpPortProperty(environment);
    }

    private void mapHttpPortProperty(ConfigurableEnvironment environment) {
        if (properties.services() != null && properties.services().http() != null) {
            var httpPort = properties.services().http().server().port();
            var mappedProperties = new HashMap<String, Object>();
            mappedProperties.put("server.port", httpPort);
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("httpServerMappedProperties", mappedProperties));
        }
    }
}
