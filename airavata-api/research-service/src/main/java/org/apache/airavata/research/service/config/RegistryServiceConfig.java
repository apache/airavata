/**
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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Airavata Registry Service integration
 * Provides RegistryService client bean for accessing existing airavata-api infrastructure
 * Uses lazy initialization to allow application startup even when registry service is unavailable
 */
@Configuration
public class RegistryServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryServiceConfig.class);

    @Value("${airavata.registry.host:localhost}")
    private String registryHost;

    @Value("${airavata.registry.port:9930}")
    private int registryPort;

    @Value("${airavata.registry.enabled:true}")
    private boolean registryEnabled;

    /**
     * Creates RegistryService.Iface client bean using Airavata's RegistryServiceClientFactory
     * This integrates with existing airavata-api infrastructure
     * Uses lazy initialization to allow application startup without registry service
     */
    @Bean
    public RegistryServiceProvider registryServiceProvider() {
        return new RegistryServiceProvider();
    }

    /**
     * Provider class that handles lazy initialization and graceful failure of RegistryService
     */
    public class RegistryServiceProvider {
        private volatile RegistryService.Iface registryService;
        private volatile boolean connectionAttempted = false;
        private volatile Exception lastException;

        public RegistryService.Iface getRegistryService() throws RegistryServiceException {
            if (!registryEnabled) {
                throw new RegistryServiceException("Registry service is disabled. Enable with airavata.registry.enabled=true");
            }

            if (registryService == null && !connectionAttempted) {
                synchronized (this) {
                    if (registryService == null && !connectionAttempted) {
                        connectionAttempted = true;
                        try {
                            registryService = createRegistryService();
                            LOGGER.info("Successfully connected to Airavata Registry Service");
                        } catch (Exception e) {
                            lastException = e;
                            LOGGER.error("Failed to connect to Airavata Registry Service at {}:{} - {}", 
                                registryHost, registryPort, e.getMessage());
                        }
                    }
                }
            }

            if (registryService == null) {
                String errorMsg = String.format("Registry service unavailable at %s:%d", registryHost, registryPort);
                if (lastException != null) {
                    errorMsg += " - " + lastException.getMessage();
                }
                throw new RegistryServiceException(errorMsg);
            }

            return registryService;
        }

        private RegistryService.Iface createRegistryService() throws RegistryServiceException {
            String serverHost = getRegistryServerHost();
            int serverPort = getRegistryServerPort();
            
            LOGGER.info("Attempting to connect to Airavata Registry Service at {}:{}", serverHost, serverPort);
            
            RegistryService.Client registryClient = RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
            return registryClient; // RegistryService.Client implements RegistryService.Iface
        }

        public boolean isAvailable() {
            try {
                return getRegistryService() != null;
            } catch (RegistryServiceException e) {
                return false;
            }
        }
    }

    /**
     * Get registry server host from Airavata ServerSettings or fallback to application properties
     */
    private String getRegistryServerHost() {
        try {
            return ServerSettings.getRegistryServerHost();
        } catch (ApplicationSettingsException e) {
            LOGGER.warn("Unable to get registry host from ServerSettings, using configured value: {}", registryHost);
            return registryHost;
        }
    }

    /**
     * Get registry server port from Airavata ServerSettings or fallback to application properties
     */
    private int getRegistryServerPort() {
        try {
            return Integer.parseInt(ServerSettings.getRegistryServerPort());
        } catch (ApplicationSettingsException | NumberFormatException e) {
            LOGGER.warn("Unable to get registry port from ServerSettings, using configured value: {}", registryPort);
            return registryPort;
        }
    }
}