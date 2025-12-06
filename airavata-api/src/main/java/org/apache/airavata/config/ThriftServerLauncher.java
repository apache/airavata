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

import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.main.ThriftAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Launches Thrift API servers using Spring context.
 * This runs after background services are started (Order 10).
 */
@Component
@Order(10)
public class ThriftServerLauncher implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ThriftServerLauncher.class);
    private static final String SERVERS_KEY = "servers";

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Thrift API servers via Spring integration...");
        
        // Set Spring context in ThriftAPI for server loading
        ThriftAPI.setSpringContext((org.springframework.context.ConfigurableApplicationContext) applicationContext);
        
        // Parse server names from args
        String serverNames = "all";
        for (String string : args) {
            logger.info("Server Arguments: " + string);
            if (string.startsWith("--servers=")) {
                serverNames = string.substring("--servers=".length());
            }
        }
        serverNames = ApplicationSettings.getSetting(SERVERS_KEY, serverNames);
        
        // Start all Thrift servers using ThriftAPI's logic
        // This will use Spring context for server instances
        ThriftAPI.startAllServers(serverNames);
        
        // Keep the application running
        logger.info("All Airavata services started successfully. Application will continue running...");
        keepAlive();
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

