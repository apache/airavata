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
package org.apache.airavata.telemetry;

import io.prometheus.client.exporter.HTTPServer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "services.telemetry.enabled", havingValue = "true", matchIfMissing = true)
public class TelemetryServer extends ServerLifecycle {

    private final AiravataServerProperties properties;
    private HTTPServer httpServer;

    public TelemetryServer(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getServerName() {
        return "Prometheus Monitoring Server";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        // Start early so metrics are available when other services start
        return 5;
    }

    @Override
    protected void doStart() throws Exception {
        // Bind address is no longer configurable; keep it simple and local-only by default.
        String host = "localhost";
        int port = properties.services.telemetry.port;
        try {
            logger.info("Starting Prometheus monitoring server on {}:{}", host, port);
            httpServer = new HTTPServer(host, port, true);
            logger.info("Prometheus monitoring server started successfully on {}:{}", host, port);
        } catch (Exception e) {
            logger.error("Failed to start the monitoring server on host {} and port {}", host, port, e);
            throw e;
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (httpServer != null) {
            logger.info("Stopping Prometheus monitoring server");
            try {
                httpServer.stop();
            } catch (Exception e) {
                logger.warn("Error stopping Prometheus monitoring server", e);
            }
            httpServer = null;
            logger.info("Prometheus monitoring server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        // HTTPServer doesn't have isRunning() method, so we track state via httpServer != null
        // The ServerLifecycle base class also tracks running state
        return httpServer != null && super.isRunning();
    }
}
