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
package org.apache.airavata.cli.communication;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import org.apache.airavata.cli.handlers.ServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Starts Unix domain socket server for CLI-service communication.
 */
@Component
@ConditionalOnProperty(name = "airavata.server.enabled", havingValue = "true", matchIfMissing = false)
public class SocketServerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SocketServerConfiguration.class);
    private ServiceSocketManager socketManager;

    @Autowired
    private ServiceHandler serviceHandler;

    @PostConstruct
    public void startSocketServer() {
        // Get configDir from AIRAVATA_HOME/conf
        String airavataHome = System.getenv("AIRAVATA_HOME");
        if (airavataHome == null || airavataHome.isEmpty()) {
            airavataHome = System.getProperty("airavata.home");
        }
        if (airavataHome == null || airavataHome.isEmpty()) {
            throw new IllegalStateException(
                    "AIRAVATA_HOME environment variable or airavata.home system property must be set.");
        }
        var configDir = new File(airavataHome, "conf").getAbsolutePath();
        socketManager = new ServiceSocketManager(configDir, serviceHandler);

        if (socketManager.isSocketLocked()) {
            logger.error(
                    "Socket already exists at {}. Another Airavata process may be running.",
                    socketManager.getSocketPath());
            throw new IllegalStateException("Socket already exists. Only one Airavata process can run at a time.");
        }

        try {
            socketManager.start();
            logger.info("Service socket server started at: {}", socketManager.getSocketPath());
        } catch (IOException e) {
            logger.error("Failed to start socket server", e);
            throw new IllegalStateException("Failed to start socket server", e);
        }
    }

    @PreDestroy
    public void stopSocketServer() {
        if (socketManager != null) {
            socketManager.stop();
        }
    }
}
