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
package org.apache.airavata.file.server;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.agents.api.AdaptorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class FileServerConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FileServerConfiguration.class);

    private final AdaptorSupport adaptorSupport;

    public FileServerConfiguration(@Lazy @Qualifier("adaptorSupportImpl") AdaptorSupport adaptorSupport) {
        this.adaptorSupport = adaptorSupport;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Check if file service is enabled
        String enabled = environment.getProperty("services.fileserver.enabled", "true");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }
        mapScopedProperties(environment);
    }

    private void mapScopedProperties(ConfigurableEnvironment environment) {
        Map<String, Object> mappedProperties = new HashMap<>();

        // Map services.fileserver.server.* to server.*
        mapProperty("services.fileserver.server.port", "server.port", mappedProperties, environment);

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

        if (!mappedProperties.isEmpty()) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("fileServerMappedProperties", mappedProperties));
            logger.debug(
                    "Mapped {} services.fileserver.* properties to Spring Boot properties", mappedProperties.size());
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

    // AdaptorSupport is already a @Component bean (AdaptorSupportImpl), no need to create it here

    // RegistryService is already a @Service bean, no need to create it here
}
