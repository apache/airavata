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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.IServer.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Launches Thrift API servers using Spring context.
 *
 * <p>This runs after background services are started (Order 10).</p>
 * <ol>
 *   <li>DB Event Manager</li>
 *   <li>Registry Server</li>
 *   <li>Credential Store</li>
 *   <li>Sharing Server</li>
 *   <li>API Server</li>
 *   <li>Orchestrator</li>
 *   <li>Profile Service</li>
 * </ol>
 *
 * <p>Servers are loaded from Spring context when available, otherwise instantiated via reflection.
 * The main thread is kept alive to prevent the application from exiting.
 */
@Component
@Order(10)
public class ThriftServerLauncher implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ThriftServerLauncher.class);
    private static final int SERVER_STATUS_CHANGE_WAIT_INTERVAL = 500;

    // Server name constants for api-orch mode
    private static final String API_SERVER = "apiserver.class";
    private static final String CREDENTIAL_STORE = "credential.store.class";
    private static final String REGISTRY_SERVER = "regserver";
    private static final String SHARING_SERVER = "sharing_server";
    private static final String ORCHESTRATOR = "orchestrator";
    private static final String PROFILE_SERVICE = "profile_service.class";
    private static final String DB_EVENT_MANAGER = "db_event_manager.class";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Thrift API servers via Spring integration (api-orch mode)...");

        // Load and start all Thrift servers in api-orch mode
        List<IServer> servers = loadServers();
        startAllServers(servers);

        // Keep the application running
        logger.info("All Airavata services started successfully. Application will continue running...");
        keepAlive();
    }

    private List<IServer> loadServers() {
        List<IServer> servers = new ArrayList<>();

        // api-orch mode server list in dependency order with their class names from properties
        List<ServerConfig> serverConfigs = Arrays.asList(
                new ServerConfig(DB_EVENT_MANAGER, properties.services.dbevent.classpath),
                new ServerConfig(REGISTRY_SERVER, properties.services.registry.classpath),
                new ServerConfig(CREDENTIAL_STORE, properties.services.vault.classpath),
                new ServerConfig(SHARING_SERVER, properties.services.sharing.classpath),
                new ServerConfig(API_SERVER, properties.services.api.classpath),
                new ServerConfig(ORCHESTRATOR, properties.services.orchestrator.classpath),
                new ServerConfig(PROFILE_SERVICE, properties.services.api.profile.classpath));

        for (ServerConfig config : serverConfigs) {
            String serverClassName = config.className;
            if (serverClassName == null || serverClassName.isEmpty()) {
                logger.warn("Server class name not configured for key: {}", config.key);
                continue;
            }

            try {
                Class<?> classInstance =
                        ThriftServerLauncher.class.getClassLoader().loadClass(serverClassName);
                // Try to get from Spring context first, otherwise use reflection
                IServer server = null;
                try {
                    server = applicationContext.getBean(classInstance.asSubclass(IServer.class));
                    logger.debug("Loaded server {} from Spring context", config.key);
                } catch (Exception e) {
                    // Not a Spring bean, use reflection
                    logger.debug("Server {} not found in Spring context, using reflection", serverClassName);
                    server = (IServer) classInstance.getDeclaredConstructor().newInstance();
                }
                servers.add(server);
            } catch (ClassNotFoundException e) {
                logger.error(
                        "Error while locating server implementation \"" + config.key + "\" with class \""
                                + serverClassName + "\"!!!",
                        e);
            } catch (InstantiationException
                    | IllegalAccessException
                    | java.lang.reflect.InvocationTargetException
                    | NoSuchMethodException e) {
                logger.error("Error while initiating server instance \"" + config.key + "\"!!!", e);
            } catch (ClassCastException e) {
                logger.error("Invalid server \"" + config.key + "\"!!!", e);
            }
        }

        return servers;
    }

    private static class ServerConfig {
        final String key;
        final String className;

        ServerConfig(String key, String className) {
            this.key = key;
            this.className = className;
        }
    }

    private void startAllServers(List<IServer> servers) {
        for (IServer server : servers) {
            try {
                server.configure();
                server.start();
                waitForServerToStart(server);
            } catch (Exception e) {
                logger.error("Server Start Error:", e);
            }
        }
    }

    private void waitForServerToStart(IServer server) throws Exception {
        int count = 0;
        while (server.getStatus() == ServerStatus.STARTING && count < 60000) { // Max 60 seconds
            Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
            count += SERVER_STATUS_CHANGE_WAIT_INTERVAL;
        }
        if (server.getStatus() != ServerStatus.STARTED) {
            logger.error("The " + server.getName() + " did not start!!!");
        }
    }

    private void keepAlive() {
        // Keep the main thread alive so Spring application doesn't exit
        // Background services run in daemon threads, so we need to keep main thread alive
        try {
            synchronized (this) {
                while (!Thread.currentThread().isInterrupted()) {
                    this.wait();
                }
            }
        } catch (InterruptedException e) {
            logger.info("ThriftServerLauncher interrupted, shutting down...");
            Thread.currentThread().interrupt();
        }
    }
}
