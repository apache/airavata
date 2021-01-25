/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

 package org.apache.airavata.patform.monitoring;

import io.prometheus.client.exporter.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MonitoringServer {

    private final static Logger logger = LoggerFactory.getLogger(MonitoringServer.class);

    private String host;
    private int port;
    private HTTPServer httpServer;

    public MonitoringServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        try {
            logger.info("Starting the monitoring server");
            httpServer = new HTTPServer(host, port, true);
        } catch (IOException e) {
            logger.error("Failed to start the monitoring server on host {} na port {}", host, port, e);
        }
    }

    public void stop() {
        if (httpServer != null) {
            logger.info("Stopping the monitor server");
            httpServer.stop();
        }
    }
}
