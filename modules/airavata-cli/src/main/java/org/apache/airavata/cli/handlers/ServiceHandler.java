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
import org.apache.airavata.cli.util.PropertiesManager;
import org.apache.airavata.config.AiravataServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServiceHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final AiravataServerProperties properties;

    // Service name to thread name and property key mapping
    private static final Map<String, ServiceInfo> SERVICE_MAP = new HashMap<>();

    static {
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
        final String threadName;
        final String propertyKey;

        ServiceInfo(String threadName, String propertyKey) {
            this.threadName = threadName;
            this.propertyKey = propertyKey;
        }
    }

    public ServiceHandler(AiravataServerProperties properties) {
        this.properties = properties;
    }

    /**
     * Check if Airavata process is running.
     */
    public boolean isAiravataRunning() {
        Path pidFile = getPidFilePath();
        if (!Files.exists(pidFile)) {
            return false;
        }

        try {
            String pidStr = Files.readString(pidFile).trim();
            long pid = Long.parseLong(pidStr);

            // Check if process is alive (Unix/Linux/Mac)
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows: use tasklist
                ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid);
                Process process = pb.start();
                int exitCode = process.waitFor();
                // tasklist returns 0 if process found
                return exitCode == 0;
            } else {
                // Unix/Linux/Mac: use kill -0
                ProcessBuilder pb = new ProcessBuilder("kill", "-0", String.valueOf(pid));
                Process process = pb.start();
                int exitCode = process.waitFor();
                return exitCode == 0;
            }
        } catch (Exception e) {
            logger.debug("Error checking process status", e);
            return false;
        }
    }

    /**
     * Get Airavata process PID.
     */
    public Long getAiravataPid() {
        Path pidFile = getPidFilePath();
        if (!Files.exists(pidFile)) {
            return null;
        }

        try {
            String pidStr = Files.readString(pidFile).trim();
            return Long.parseLong(pidStr);
        } catch (Exception e) {
            logger.error("Error reading PID file", e);
            return null;
        }
    }

    /**
     * Get PID file path.
     */
    private Path getPidFilePath() {
        String airavataHome = System.getenv("AIRAVATA_HOME");
        if (airavataHome != null && !airavataHome.isEmpty()) {
            return Paths.get(airavataHome, "bin", "pid-airavata");
        }
        return Paths.get("bin", "pid-airavata");
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

        return new ServiceStatus(serviceName, info.threadName, enabled, running);
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
     * Check if service thread is running.
     */
    private boolean isServiceRunning(String serviceName) {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            return false;
        }

        // Get all threads and check if service thread exists and is alive
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (info.threadName.equals(thread.getName()) && thread.isAlive()) {
                return true;
            }
        }
        return false;
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
     * Start a service (enable it in properties).
     */
    public void startService(String serviceName) throws IOException {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        PropertiesManager.updateBooleanProperty(info.propertyKey, true);
        System.out.println("✓ Service " + serviceName + " enabled in configuration");
        System.out.println("Note: Airavata process restart may be required for changes to take effect.");
    }

    /**
     * Stop a service (disable it in properties).
     */
    public void stopService(String serviceName) throws IOException {
        ServiceInfo info = SERVICE_MAP.get(serviceName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        PropertiesManager.updateBooleanProperty(info.propertyKey, false);
        System.out.println("✓ Service " + serviceName + " disabled in configuration");
        System.out.println("Note: Airavata process restart may be required for changes to take effect.");
    }

    /**
     * Restart a service (stop then start).
     */
    public void restartService(String serviceName) throws IOException {
        stopService(serviceName);
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
        private final String threadName;
        private final boolean enabled;
        private final boolean running;

        public ServiceStatus(String serviceName, String threadName, boolean enabled, boolean running) {
            this.serviceName = serviceName;
            this.threadName = threadName;
            this.enabled = enabled;
            this.running = running;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getThreadName() {
            return threadName;
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
                    "%s (thread: %s, enabled: %s, running: %s)", serviceName, threadName, enabled, running);
        }
    }
}
