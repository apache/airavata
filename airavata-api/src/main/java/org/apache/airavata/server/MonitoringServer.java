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
package org.apache.airavata.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringServer implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringServer.class);
    private static final String SERVER_NAME = "Monitoring Server";

    private final String host;
    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer httpServer;
    private ServiceRegistry serviceRegistry;
    private ServerStatus status = ServerStatus.STOPPED;

    public MonitoringServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void run() {
        setStatus(ServerStatus.STARTING);
        try {
            logger.info("Starting the monitoring server on {}:{}", host, port);
            httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);

            httpServer.createContext("/metrics", this::handleMetrics);
            httpServer.createContext("/health/services", this::handleHealthServices);
            httpServer.createContext("/admin/restart/", this::handleAdminRestart);

            httpServer.start();
            setStatus(ServerStatus.STARTED);
            logger.info("Monitoring server started on {}:{}", host, port);

            // Park thread until interrupted
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start the monitoring server on host {} and port {}", host, port, e);
            setStatus(ServerStatus.FAILED);
        }
    }

    @Override
    public void stop() {
        setStatus(ServerStatus.STOPPING);
        if (httpServer != null) {
            logger.info("Stopping the monitoring server");
            httpServer.stop(0);
        }
        setStatus(ServerStatus.STOPPED);
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    private void setStatus(ServerStatus stat) {
        status = stat;
        status.updateTime();
    }

    private void handleMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            Map<String, Object> metricsMap = new LinkedHashMap<>();
            for (Meter meter : Metrics.globalRegistry.getMeters()) {
                String name = meter.getId().getName();
                meter.measure()
                        .forEach(m -> metricsMap.put(
                                name + "." + m.getStatistic().name().toLowerCase(), m.getValue()));
            }
            sendJson(exchange, 200, metricsMap);
        } catch (Exception e) {
            logger.error("Error serving /metrics", e);
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleHealthServices(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            Map<String, ServiceStatus> statuses = serviceRegistry != null ? serviceRegistry.getStatuses() : Map.of();
            sendJson(exchange, 200, Map.of("services", statuses));
        } catch (Exception e) {
            logger.error("Error serving /health/services", e);
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleAdminRestart(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        // path is /admin/restart/{name}
        String prefix = "/admin/restart/";
        if (!path.startsWith(prefix) || path.length() <= prefix.length()) {
            sendJson(exchange, 400, Map.of("error", "Missing service name in path"));
            return;
        }
        String name = path.substring(prefix.length());
        if (serviceRegistry == null) {
            sendJson(exchange, 500, Map.of("error", "ServiceRegistry not configured"));
            return;
        }
        try {
            serviceRegistry.restart(name);
            sendJson(exchange, 200, Map.of("status", "restarted", "service", name));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 404, Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            sendJson(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error restarting service '{}'", name, e);
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
