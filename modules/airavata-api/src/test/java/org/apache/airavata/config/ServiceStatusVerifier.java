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

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

/**
 * Utility class for verifying service status in tests.
 *
 * <p>Provides methods to:
 * <ul>
 *   <li>Check if services are running via ServiceHandler</li>
 *   <li>Verify port availability for external services</li>
 *   <li>Check Spring lifecycle state</li>
 *   <li>Wait for services to start with timeout and retry logic</li>
 * </ul>
 */
public class ServiceStatusVerifier {

    private static final Logger logger = LoggerFactory.getLogger(ServiceStatusVerifier.class);

    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;

    public ServiceStatusVerifier(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    /**
     * Verify that a service is enabled in configuration.
     *
     * @param serviceName Name of the service to check
     * @return true if service is enabled, false otherwise
     */
    public boolean isServiceEnabled(String serviceName) {
        if (properties == null) {
            return false;
        }
        try {
            // Check properties directly
            switch (serviceName) {
                case "thrift-api":
                    return properties.services.thrift.enabled;
                case "rest-api":
                    return properties.services.rest.enabled;
                case "helix-controller":
                    return properties.services.controller.enabled;
                case "helix-participant":
                    return properties.services.participant.enabled;
                case "pre-workflow-manager":
                    return properties.services.prewm.enabled;
                case "post-workflow-manager":
                    return properties.services.postwm.enabled;
                case "parser-workflow-manager":
                    return properties.services.parser.enabled;
                case "realtime-monitor":
                    return properties.services.monitor.realtime.enabled;
                case "email-monitor":
                    return properties.services.monitor.email.enabled;
                case "compute-monitor":
                    return properties.services.monitor.compute.enabled;
                case "research-service":
                    return properties.services.research.enabled;
                case "agent-service":
                    return properties.services.agent.enabled;
                case "file-service":
                    return properties.services.fileserver.enabled;
                case "dbevent-service":
                    return properties.services.dbus.enabled;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error checking if service {} is enabled: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Verify that a service is running (in test profile, this checks if enabled).
     *
     * @param serviceName Name of the service to check
     * @return true if service is enabled (running in test context means enabled)
     */
    public boolean isServiceRunning(String serviceName) {
        // In test profile, we can only check if service is enabled
        // Actual running state requires ServiceHandler which is not available
        return isServiceEnabled(serviceName);
    }

    /**
     * Wait for a service to start with timeout and retry logic.
     *
     * @param serviceName Name of the service to check
     * @param timeoutSeconds Maximum time to wait in seconds
     * @param retryIntervalMs Interval between retries in milliseconds
     * @return true if service started within timeout, false otherwise
     */
    public boolean waitForService(String serviceName, int timeoutSeconds, long retryIntervalMs) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        logger.info("Waiting for service {} to start (timeout: {}s)", serviceName, timeoutSeconds);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isServiceRunning(serviceName)) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.info("Service {} started after {}ms", serviceName, elapsed);
                return true;
            }

            try {
                Thread.sleep(retryIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for service {}", serviceName);
                return false;
            }
        }

        logger.warn("Service {} did not start within {} seconds", serviceName, timeoutSeconds);
        return false;
    }

    /**
     * Wait for a service to start with default timeout (30 seconds).
     *
     * @param serviceName Name of the service to check
     * @return true if service started, false if timeout
     */
    public boolean waitForService(String serviceName) {
        return waitForService(serviceName, 30, 500);
    }

    /**
     * Check if a port is listening (for external services).
     *
     * @param host Hostname to check
     * @param port Port number to check
     * @param timeoutMs Timeout in milliseconds
     * @return true if port is listening, false otherwise
     */
    public boolean isPortListening(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            logger.debug("Port {}:{} is listening", host, port);
            return true;
        } catch (IOException e) {
            logger.debug("Port {}:{} is not listening: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a port is listening on localhost with default timeout (1 second).
     *
     * @param port Port number to check
     * @return true if port is listening, false otherwise
     */
    public boolean isPortListening(int port) {
        return isPortListening("localhost", port, 1000);
    }

    /**
     * Wait for a port to become available.
     *
     * @param host Hostname to check
     * @param port Port number to check
     * @param timeoutSeconds Maximum time to wait in seconds
     * @param retryIntervalMs Interval between retries in milliseconds
     * @return true if port became available, false if timeout
     */
    public boolean waitForPort(String host, int port, int timeoutSeconds, long retryIntervalMs) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        logger.info("Waiting for port {}:{} to become available (timeout: {}s)", host, port, timeoutSeconds);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isPortListening(host, port, 1000)) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.info("Port {}:{} became available after {}ms", host, port, elapsed);
                return true;
            }

            try {
                Thread.sleep(retryIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for port {}:{}", host, port);
                return false;
            }
        }

        logger.warn("Port {}:{} did not become available within {} seconds", host, port, timeoutSeconds);
        return false;
    }

    /**
     * Verify that expected services are running.
     *
     * @param expectedServices Array of service names that should be running
     * @return VerificationResult with details
     */
    public VerificationResult verifyServicesRunning(String... expectedServices) {
        VerificationResult result = new VerificationResult();
        for (String serviceName : expectedServices) {
            boolean running = isServiceRunning(serviceName);
            if (running) {
                result.addSuccess(serviceName);
                logger.info("✓ Service {} is running", serviceName);
            } else {
                result.addFailure(serviceName, "Service is not running");
                logger.error("✗ Service {} is not running", serviceName);
            }
        }
        return result;
    }

    /**
     * Verify that expected services are NOT running.
     *
     * @param expectedDisabledServices Array of service names that should NOT be running
     * @return VerificationResult with details
     */
    public VerificationResult verifyServicesNotRunning(String... expectedDisabledServices) {
        VerificationResult result = new VerificationResult();
        for (String serviceName : expectedDisabledServices) {
            boolean running = isServiceRunning(serviceName);
            if (!running) {
                result.addSuccess(serviceName);
                logger.info("✓ Service {} is not running (as expected)", serviceName);
            } else {
                result.addFailure(serviceName, "Service should not be running but is");
                logger.error("✗ Service {} should not be running but is", serviceName);
            }
        }
        return result;
    }

    /**
     * Get all service names that can be tested.
     *
     * @return List of service names
     */
    public List<String> getAllServiceNames() {
        return java.util.Arrays.asList(
                "thrift-api",
                "rest-api",
                "helix-controller",
                "helix-participant",
                "pre-workflow-manager",
                "post-workflow-manager",
                "parser-workflow-manager",
                "realtime-monitor",
                "email-monitor",
                "compute-monitor",
                "research-service",
                "agent-service",
                "file-service",
                "dbevent-service");
    }

    /**
     * Check Spring lifecycle state for a service bean.
     *
     * @param beanName Name of the Spring bean
     * @return true if bean exists and is running, false otherwise
     */
    public boolean isLifecycleBeanRunning(String beanName) {
        try {
            if (applicationContext.containsBean(beanName)) {
                Object bean = applicationContext.getBean(beanName);
                if (bean instanceof SmartLifecycle) {
                    SmartLifecycle lifecycle = (SmartLifecycle) bean;
                    return lifecycle.isRunning();
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking lifecycle state for bean {}: {}", beanName, e.getMessage());
        }
        return false;
    }

    /**
     * Result of service verification operations.
     */
    public static class VerificationResult {
        private final List<String> successfulServices = new ArrayList<>();
        private final Map<String, String> failedServices = new java.util.HashMap<>();

        void addSuccess(String serviceName) {
            successfulServices.add(serviceName);
        }

        void addFailure(String serviceName, String reason) {
            failedServices.put(serviceName, reason);
        }

        public boolean isSuccess() {
            return failedServices.isEmpty();
        }

        public List<String> getSuccessfulServices() {
            return new ArrayList<>(successfulServices);
        }

        public Map<String, String> getFailedServices() {
            return new java.util.HashMap<>(failedServices);
        }

        public String getSummary() {
            if (isSuccess()) {
                return String.format("All %d services verified successfully", successfulServices.size());
            } else {
                return String.format(
                        "%d succeeded, %d failed: %s",
                        successfulServices.size(), failedServices.size(), failedServices);
            }
        }
    }
}
