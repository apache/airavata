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

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.manager.dbevent.DBEventManagerRunner;
import org.apache.airavata.metascheduler.metadata.analyzer.DataInterpreterService;
import org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ProcessReschedulingService;
import org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService;
import org.apache.airavata.thriftapi.server.ThriftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Registry that maps service names to their Spring lifecycle beans.
 *
 * <p>Discovers all SmartLifecycle beans from ApplicationContext and provides
 * methods to get lifecycle beans by service name. Handles both TCP server
 * services (Thrift, REST) and background services.
 */
@Component
public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private final ApplicationContext applicationContext;
    private final Map<String, String> serviceNameToBeanName = new HashMap<>();
    private final Map<String, Class<? extends SmartLifecycle>> serviceNameToBeanClass = new HashMap<>();

    public ServiceRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initializeServiceMappings();
    }

    /**
     * Initialize service name to bean mappings.
     */
    private void initializeServiceMappings() {
        // TCP Server Services
        serviceNameToBeanClass.put("thrift-api", ThriftServer.class);
        // REST API is handled specially via WebServerApplicationContext
        // Note: All Thrift services (Airavata, Profile, Orchestrator, Registry, Credential, Sharing)
        // are now multiplexed in the unified ThriftServer

        // Background Services (if they implement SmartLifecycle)
        serviceNameToBeanClass.put("db-event-manager", DBEventManagerRunner.class);
        serviceNameToBeanClass.put("data-interpreter", DataInterpreterService.class);
        serviceNameToBeanClass.put("process-rescheduler", ProcessReschedulingService.class);
        serviceNameToBeanClass.put("compute-monitor", ComputationalResourceMonitoringService.class);

        // Discover additional SmartLifecycle beans dynamically
        discoverLifecycleBeans();
    }

    /**
     * Discover all SmartLifecycle beans and map them by their class name or bean name.
     */
    private void discoverLifecycleBeans() {
        try {
            Map<String, SmartLifecycle> lifecycleBeans = applicationContext.getBeansOfType(SmartLifecycle.class);
            for (Map.Entry<String, SmartLifecycle> entry : lifecycleBeans.entrySet()) {
                String beanName = entry.getKey();
                SmartLifecycle bean = entry.getValue();
                Class<?> beanClass = bean.getClass();

                // Map by bean name (lowercase, with dashes)
                String serviceName = beanName.toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2");
                if (!serviceNameToBeanName.containsKey(serviceName)) {
                    serviceNameToBeanName.put(serviceName, beanName);
                }

                logger.debug("Discovered lifecycle bean: {} -> {}", serviceName, beanClass.getSimpleName());
            }
        } catch (Exception e) {
            logger.warn("Error discovering lifecycle beans", e);
        }
    }

    /**
     * Get SmartLifecycle bean by service name.
     */
    public SmartLifecycle getLifecycleBean(String serviceName) {
        // Check if it's REST API (special case)
        if ("rest-api".equals(serviceName)) {
            return getRestApiLifecycle();
        }

        // Check class-based mapping first
        Class<? extends SmartLifecycle> beanClass = serviceNameToBeanClass.get(serviceName);
        if (beanClass != null) {
            try {
                return applicationContext.getBean(beanClass);
            } catch (Exception e) {
                logger.debug("Bean not found for class: {}", beanClass.getName(), e);
            }
        }

        // Check bean name mapping
        String beanName = serviceNameToBeanName.get(serviceName);
        if (beanName != null) {
            try {
                return applicationContext.getBean(beanName, SmartLifecycle.class);
            } catch (Exception e) {
                logger.debug("Bean not found for name: {}", beanName, e);
            }
        }

        // Try direct lookup by service name
        try {
            return applicationContext.getBean(serviceName, SmartLifecycle.class);
        } catch (Exception e) {
            logger.debug("Bean not found for service name: {}", serviceName, e);
        }

        return null;
    }

    /**
     * Get REST API lifecycle wrapper.
     * REST API is managed by Spring Boot's embedded web server.
     */
    private SmartLifecycle getRestApiLifecycle() {
        try {
            if (applicationContext instanceof ServletWebServerApplicationContext) {
                ServletWebServerApplicationContext webContext = (ServletWebServerApplicationContext) applicationContext;
                return new RestApiLifecycleWrapper(webContext);
            }
        } catch (Exception e) {
            logger.debug("REST API not available", e);
        }
        return null;
    }

    /**
     * Check if a service exists in the registry.
     */
    public boolean hasService(String serviceName) {
        if ("rest-api".equals(serviceName)) {
            return getRestApiLifecycle() != null;
        }
        return getLifecycleBean(serviceName) != null;
    }

    /**
     * Get all registered service names.
     */
    public java.util.Set<String> getRegisteredServiceNames() {
        java.util.Set<String> names = new java.util.HashSet<>(serviceNameToBeanClass.keySet());
        names.addAll(serviceNameToBeanName.keySet());
        if (getRestApiLifecycle() != null) {
            names.add("rest-api");
        }
        return names;
    }

    /**
     * Wrapper for REST API web server to implement SmartLifecycle.
     */
    private static class RestApiLifecycleWrapper implements SmartLifecycle {
        private final ServletWebServerApplicationContext webContext;

        public RestApiLifecycleWrapper(ServletWebServerApplicationContext webContext) {
            this.webContext = webContext;
        }

        @Override
        public void start() {
            // Web server is started automatically by Spring Boot
            // This is a no-op as the server is already managed
        }

        @Override
        public void stop() {
            // Web server is stopped automatically by Spring Boot
            // This is a no-op as the server is already managed
        }

        @Override
        public boolean isRunning() {
            try {
                org.springframework.boot.web.server.WebServer webServer = webContext.getWebServer();
                return webServer != null;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public int getPhase() {
            return Integer.MAX_VALUE; // Start last
        }

        @Override
        public boolean isAutoStartup() {
            return true;
        }

        @Override
        public void stop(Runnable callback) {
            stop();
            if (callback != null) {
                callback.run();
            }
        }
    }
}
