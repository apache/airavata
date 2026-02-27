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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.apache.airavata.cli.util.ProcessManager;
import org.apache.airavata.cli.util.PropertiesManager;
import org.apache.airavata.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

@Service
public class ServiceHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final ServerProperties properties;
    private final ServiceRegistry serviceRegistry;

    // Service name to service info mapping
    private static final Map<String, ServiceInfo> SERVICE_MAP = new HashMap<>();

    static {
        // Background Services
        SERVICE_MAP.put("workflow-manager", new ServiceInfo("ProcessActivityManager", "services.controller.enabled"));
        SERVICE_MAP.put("realtime-monitor", new ServiceInfo("RealtimeMonitor", "services.monitor.realtime.enabled"));
        SERVICE_MAP.put("email-monitor", new ServiceInfo("EmailMonitor", "services.monitor.email.enabled"));
        SERVICE_MAP.put("compute-monitor", new ServiceInfo("ComputeMonitor", "services.monitor.compute.enabled"));

        // Additional Services
        SERVICE_MAP.put("research-service", new ServiceInfo("ResearchService", "services.research.enabled"));
        SERVICE_MAP.put("agent-service", new ServiceInfo("AgentService", "services.agent.enabled"));
        SERVICE_MAP.put("file-service", new ServiceInfo("FileService", "services.fileserver.enabled"));
    }

    private static class ServiceInfo {
        final String displayName;
        final String propertyKey;

        ServiceInfo(String displayName, String propertyKey) {
            this.displayName = displayName;
            this.propertyKey = propertyKey;
        }
    }

    public ServiceHandler(ServerProperties properties, ServiceRegistry serviceRegistry) {
        this.properties = properties;
        this.serviceRegistry = serviceRegistry;
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
     * Get config directory from AIRAVATA_HOME environment variable.
     * Returns AIRAVATA_HOME/conf.
     */
    private String getConfigDir() {
        String airavataHome = System.getenv("AIRAVATA_HOME");
        if (airavataHome == null || airavataHome.isEmpty()) {
            throw new IllegalStateException(
                    "AIRAVATA_HOME environment variable is not set. Please set AIRAVATA_HOME to the Airavata installation directory.");
        }
        return new File(airavataHome, "conf").getAbsolutePath();
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
            var props = PropertiesManager.readProperties();
            String value = props.getProperty(info.propertyKey, "true");
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            logger.warn("Error reading service property", e);
            // Fallback to checking properties object
            return getServiceEnabledFromProperties(serviceName);
        }
    }

    /**
     * Get service enabled status from ServerProperties.
     */
    private boolean getServiceEnabledFromProperties(String serviceName) {
        try {
            return switch (serviceName) {
                case "workflow-manager" -> properties.services().controller().enabled();
                case "realtime-monitor" ->
                    properties.services().monitor().realtime().enabled();
                case "email-monitor" -> properties.services().monitor().email().enabled();
                case "compute-monitor" ->
                    properties.services().monitor().compute().enabled();
                case "research-service" -> properties.services().research().enabled();
                case "agent-service" -> properties.services().agent().enabled();
                case "file-service" -> properties.services().fileserver().enabled();
                default -> false;
            };
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
        var services = new ArrayList<ServiceStatus>();
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
