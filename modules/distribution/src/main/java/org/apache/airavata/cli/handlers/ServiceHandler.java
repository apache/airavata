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
package org.apache.airavata.cli.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.apache.airavata.cli.util.PropertiesManager;
import org.apache.airavata.cli.util.ProcessManager;
import org.apache.airavata.config.AiravataServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

@Service
public class ServiceHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final AiravataServerProperties properties;
    private final ServiceRegistry serviceRegistry;
    private final ApplicationContext applicationContext;

    // Service name to service info mapping
    private static final Map<String, ServiceInfo> SERVICE_MAP = new HashMap<>();

    static {
        // TCP Server Services
        SERVICE_MAP.put("thrift-api", new ServiceInfo("ThriftAPI", "services.thrift.enabled"));
        SERVICE_MAP.put("rest-api", new ServiceInfo("RESTAPI", "services.rest.enabled"));
        
        // Background Services
        SERVICE_MAP.put("helix-controller", new ServiceInfo("HelixController", "helix.controller.enabled"));
        SERVICE_MAP.put("helix-participant", new ServiceInfo("GlobalParticipant", "helix.participant.enabled"));
        SERVICE_MAP.put("pre-workflow-manager", new ServiceInfo("PreWorkflowManager", "services.prewm.enabled"));
        SERVICE_MAP.put("parser-workflow-manager", new ServiceInfo("ParserWorkflowManager", "services.parser.enabled"));
        SERVICE_MAP.put("post-workflow-manager", new ServiceInfo("PostWorkflowManager", "services.postwm.enabled"));
        SERVICE_MAP.put(
                "realtime-monitor", new ServiceInfo("RealtimeMonitor", "services.monitor.realtime.monitorEnabled"));
        SERVICE_MAP.put("email-monitor", new ServiceInfo("EmailMonitor", "services.monitor.email.monitorEnabled"));
    }

    private static class ServiceInfo {
        final String displayName;
        final String propertyKey;

        ServiceInfo(String displayName, String propertyKey) {
            this.displayName = displayName;
            this.propertyKey = propertyKey;
        }
    }

    @Autowired
    public ServiceHandler(AiravataServerProperties properties, ServiceRegistry serviceRegistry, ApplicationContext applicationContext) {
        this.properties = properties;
        this.serviceRegistry = serviceRegistry;
        this.applicationContext = applicationContext;
    }

    /**
     * Check if Airavata process is running using socket-based health check.
     */
    public boolean isAiravataRunning() {
        String configDir = getConfigDir();
        return ServiceSocketClient.isServiceRunning(configDir);
    }

    /**
     * Get Airavata process PID.
     */
    public Long getAiravataPid() {
        String configDir = getConfigDir();
        return ProcessManager.readPid(configDir);
    }

    /**
     * Get config directory from system property or environment.
     */
    private String getConfigDir() {
        String configDir = System.getProperty("airavata.config.dir");
        if (configDir == null || configDir.isEmpty()) {
            configDir = System.getenv("AIRAVATA_CONFIG_DIR");
        }
        if (configDir == null || configDir.isEmpty()) {
            String airavataHome = System.getenv("AIRAVATA_HOME");
            if (airavataHome != null && !airavataHome.isEmpty()) {
                configDir = airavataHome;
            }
        }
        return configDir;
    }

    /**
     * Get status of a specific service.
     */
    public ServiceStatus getServiceStatus(String serviceName) {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        boolean enabled = isServiceEnabled(serviceName);
        boolean running = isServiceRunning(serviceName);

        return new ServiceStatus(serviceName, info.displayName, enabled, running);
    }

    /**
     * Check if service is enabled in configuration.
     */
    private boolean isServiceEnabled(String serviceName) {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            return false;
        }

        try {
            java.util.Properties props = PropertiesManager.readProperties();
            String value = props.getProperty(info.propertyKey, "true");
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            logger.warn("Error reading service property", e);
            // Fallback to checking properties object
            return getServiceEnabledFromProperties(serviceName);
        }
    }

    /**
     * Get service enabled status from AiravataServerProperties.
     */
    private boolean getServiceEnabledFromProperties(String serviceName) {
        try {
            switch (serviceName) {
                case "thrift-api":
                    return properties.services.thrift;
                case "rest-api":
                    return properties.services.rest;
                case "helix-controller":
                    return properties.helix.controller.enabled;
                case "helix-participant":
                    return properties.helix.participant.enabled;
                case "pre-workflow-manager":
                    return properties.services.prewm.enabled;
                case "parser-workflow-manager":
                    return properties.services.parser.enabled;
                case "post-workflow-manager":
                    return properties.services.postwm.enabled;
                case "realtime-monitor":
                    return properties.services.monitor.realtime.monitorEnabled;
                case "email-monitor":
                    return properties.services.monitor.email.monitorEnabled;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.warn("Error getting service enabled status from properties", e);
            return false;
        }
    }

    /**
     * Check if service is running using Spring lifecycle state.
     */
    private boolean isServiceRunning(String serviceName) {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            return false;
        }

        // First check if main Airavata process is running
        if (!isAiravataRunning()) {
            return false;
        }

        // Get lifecycle bean and check its running state
        SmartLifecycle lifecycleBean = serviceRegistry.getLifecycleBean(serviceName);
        if (lifecycleBean != null) {
            return lifecycleBean.isRunning();
        }

        // If no lifecycle bean found, check if service is enabled
        // (Some services may not have lifecycle beans yet)
        return isServiceEnabled(serviceName);
    }

    /**
     * List all services with their status.
     */
    public List<ServiceStatus> listServices() {
        List<ServiceStatus> services = new ArrayList<>();
        for (String serviceName : SERVICE_MAP.keySet()) {
            services.add(getServiceStatus(serviceName));
        }
        return services;
    }

    /**
     * Start a service using Spring lifecycle.
     */
    public void startService(String serviceName) throws IOException {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        // Enable in properties first
        PropertiesManager.updateBooleanProperty(info.propertyKey, true);

        // Start via Spring lifecycle if bean exists
        SmartLifecycle lifecycleBean = serviceRegistry.getLifecycleBean(serviceName);
        if (lifecycleBean != null) {
            try {
                if (!lifecycleBean.isRunning()) {
                    lifecycleBean.start();
                    logger.info("Service {} started via Spring lifecycle", serviceName);
                } else {
                    logger.debug("Service {} is already running", serviceName);
                }
            } catch (Exception e) {
                logger.error("Failed to start service " + serviceName, e);
                throw new IOException("Failed to start service: " + serviceName, e);
            }
        } else {
            logger.warn("No lifecycle bean found for service: {}. Only enabled in configuration.", serviceName);
        }
    }

    /**
     * Stop a service using Spring lifecycle.
     */
    public void stopService(String serviceName) throws IOException {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        // Stop via Spring lifecycle if bean exists
        SmartLifecycle lifecycleBean = serviceRegistry.getLifecycleBean(serviceName);
        if (lifecycleBean != null) {
            try {
                if (lifecycleBean.isRunning()) {
                    lifecycleBean.stop();
                    logger.info("Service {} stopped via Spring lifecycle", serviceName);
                } else {
                    logger.debug("Service {} is not running", serviceName);
                }
            } catch (Exception e) {
                logger.error("Failed to stop service " + serviceName, e);
                throw new IOException("Failed to stop service: " + serviceName, e);
            }
        }

        // Disable in properties
        PropertiesManager.updateBooleanProperty(info.propertyKey, false);
    }

    /**
     * Restart a service (stop then start).
     */
    public void restartService(String serviceName) throws IOException {
        stopService(serviceName);
        // Small delay to ensure clean stop
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startService(serviceName);
    }

    /**
     * Get list of available service names.
     */
    public static List<String> getAvailableServices() {
        return new ArrayList<>(SERVICE_MAP.keySet());
    }

    /**
     * Service status information.
     */
    public static class ServiceStatus {
        private final String serviceName;
        private final String displayName;
        private final boolean enabled;
        private final boolean running;

        public ServiceStatus(String serviceName, String displayName, boolean enabled, boolean running) {
            this.serviceName = serviceName;
            this.displayName = displayName;
            this.enabled = enabled;
            this.running = running;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Deprecated
        public String getThreadName() {
            return displayName; // For backward compatibility
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s (display: %s, enabled: %s, running: %s)", serviceName, displayName, enabled, running);
        }
    }
}
