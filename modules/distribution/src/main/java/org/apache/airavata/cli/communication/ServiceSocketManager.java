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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.airavata.cli.handlers.ServiceHandler;
import org.apache.airavata.cli.handlers.ServiceHandler.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Unix domain socket server for CLI-service communication.
 *
 * <p>Creates a Unix domain socket at a fixed path (airavata.sock) and listens
 * for health check requests from the CLI. Only one instance can run at a time
 * (enforced by socket file existence check).
 */
public class ServiceSocketManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceSocketManager.class);
    private static final String SOCKET_NAME = "airavata.sock";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Path socketPath;
    private final ServiceHandler serviceHandler;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ServiceSocketManager(String configDir, ServiceHandler serviceHandler) {
        this.socketPath = resolveSocketPath(configDir);
        this.serviceHandler = serviceHandler;
    }

    /**
     * Resolve socket path. Uses config-dir/airavata.sock if writable, otherwise /tmp/airavata.sock.
     */
    private Path resolveSocketPath(String configDir) {
        String socketPathProp = System.getProperty("airavata.socket.path");
        if (socketPathProp != null && !socketPathProp.isEmpty()) {
            return Paths.get(socketPathProp);
        }

        if (configDir != null && !configDir.isEmpty()) {
            Path configPath = Paths.get(configDir);
            if (Files.exists(configPath) && Files.isWritable(configPath)) {
                return configPath.resolve(SOCKET_NAME);
            }
        }

        // Fallback to /tmp
        return Paths.get("/tmp", SOCKET_NAME);
    }

    /**
     * Get the socket path.
     */
    public Path getSocketPath() {
        return socketPath;
    }

    /**
     * Check if socket already exists (another process is running).
     */
    public boolean isSocketLocked() {
        return Files.exists(socketPath);
    }

    /**
     * Start the socket server. Fails if socket already exists.
     */
    public void start() throws IOException {
        if (running.get()) {
            logger.warn("Socket server already running");
            return;
        }

        // Check if socket already exists (lock check)
        if (isSocketLocked()) {
            throw new IOException("Socket already exists at " + socketPath + ". Another Airavata process may be running.");
        }

        try {
            // Create parent directory if needed
            Path parent = socketPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Remove any stale socket file
            if (Files.exists(socketPath)) {
                Files.delete(socketPath);
            }

            // Create Unix domain socket server
            serverChannel = ServerSocketChannel.open(java.net.StandardProtocolFamily.UNIX);
            serverChannel.bind(java.net.UnixDomainSocketAddress.of(socketPath));

            running.set(true);
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ServiceSocketManager");
                t.setDaemon(true);
                return t;
            });

            executor.submit(this::acceptConnections);
            logger.info("Service socket server started at: {}", socketPath);
        } catch (IOException e) {
            running.set(false);
            throw new IOException("Failed to start socket server at " + socketPath, e);
        }
    }

    /**
     * Accept connections and handle health check requests.
     */
    private void acceptConnections() {
        try {
            while (running.get() && serverChannel.isOpen()) {
                try {
                    SocketChannel client = serverChannel.accept();
                    if (client != null) {
                        handleClient(client);
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        logger.debug("Error accepting connection", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Socket server error", e);
        }
    }

    /**
     * Handle a client connection and process commands.
     */
    private void handleClient(SocketChannel client) {
        try {
            // Read request
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int bytesRead = client.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                String request = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                logger.debug("Received request: {}", request);

                // Process command and get response
                String response = processCommand(request);
                
                // Send response
                ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                client.write(responseBuffer);
            }
        } catch (IOException e) {
            logger.debug("Error handling client", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.debug("Error closing client", e);
            }
        }
    }

    /**
     * Process command and return JSON response.
     */
    private String processCommand(String command) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (command == null || command.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Empty command");
                return toJson(response) + "\n";
            }

            String[] parts = command.split(":", 3);
            String cmd = parts[0].toUpperCase();
            String serviceName = parts.length > 1 ? parts[1] : null;

            switch (cmd) {
                case "HEALTH":
                    response.put("status", "success");
                    response.put("data", Map.of("health", "OK"));
                    break;

                case "STATUS":
                    if (serviceName != null && !serviceName.isEmpty()) {
                        // Get specific service status
                        try {
                            ServiceStatus status = serviceHandler.getServiceStatus(serviceName);
                            Map<String, Object> statusData = new HashMap<>();
                            statusData.put("service", status.getServiceName());
                            statusData.put("displayName", status.getDisplayName());
                            statusData.put("enabled", status.isEnabled());
                            statusData.put("running", status.isRunning());
                            response.put("status", "success");
                            response.put("data", statusData);
                        } catch (IllegalArgumentException e) {
                            response.put("status", "error");
                            response.put("message", "Service not found: " + serviceName);
                        }
                    } else {
                        // Get overall process status
                        boolean running = serviceHandler.isAiravataRunning();
                        Long pid = serviceHandler.getAiravataPid();
                        Map<String, Object> statusData = new HashMap<>();
                        statusData.put("running", running);
                        if (pid != null) {
                            statusData.put("pid", pid);
                        }
                        response.put("status", "success");
                        response.put("data", statusData);
                    }
                    break;

                case "LIST":
                    List<ServiceStatus> services = serviceHandler.listServices();
                    List<Map<String, Object>> servicesData = new java.util.ArrayList<>();
                    for (ServiceStatus status : services) {
                        Map<String, Object> serviceData = new HashMap<>();
                        serviceData.put("service", status.getServiceName());
                        serviceData.put("displayName", status.getDisplayName());
                        serviceData.put("enabled", status.isEnabled());
                        serviceData.put("running", status.isRunning());
                        servicesData.add(serviceData);
                    }
                    response.put("status", "success");
                    response.put("data", Map.of("services", servicesData));
                    break;

                case "START":
                    if (serviceName == null || serviceName.isEmpty()) {
                        response.put("status", "error");
                        response.put("message", "Service name required for START command");
                    } else {
                        try {
                            serviceHandler.startService(serviceName);
                            response.put("status", "success");
                            response.put("message", "Service " + serviceName + " started");
                        } catch (Exception e) {
                            response.put("status", "error");
                            response.put("message", "Failed to start service: " + e.getMessage());
                        }
                    }
                    break;

                case "STOP":
                    if (serviceName == null || serviceName.isEmpty()) {
                        response.put("status", "error");
                        response.put("message", "Service name required for STOP command");
                    } else {
                        try {
                            serviceHandler.stopService(serviceName);
                            response.put("status", "success");
                            response.put("message", "Service " + serviceName + " stopped");
                        } catch (Exception e) {
                            response.put("status", "error");
                            response.put("message", "Failed to stop service: " + e.getMessage());
                        }
                    }
                    break;

                case "RESTART":
                    if (serviceName == null || serviceName.isEmpty()) {
                        response.put("status", "error");
                        response.put("message", "Service name required for RESTART command");
                    } else {
                        try {
                            serviceHandler.restartService(serviceName);
                            response.put("status", "success");
                            response.put("message", "Service " + serviceName + " restarted");
                        } catch (Exception e) {
                            response.put("status", "error");
                            response.put("message", "Failed to restart service: " + e.getMessage());
                        }
                    }
                    break;

                default:
                    response.put("status", "error");
                    response.put("message", "Unknown command: " + cmd);
            }

            return toJson(response) + "\n";
        } catch (Exception e) {
            logger.error("Error processing command: " + command, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Internal error: " + e.getMessage());
            return toJson(errorResponse) + "\n";
        }
    }

    /**
     * Convert object to JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Error serializing to JSON", e);
            return "{\"status\":\"error\",\"message\":\"JSON serialization error\"}";
        }
    }

    /**
     * Stop the socket server and clean up.
     */
    public void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);

        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing server channel", e);
        }

        if (executor != null) {
            executor.shutdown();
        }

        // Remove socket file
        try {
            if (Files.exists(socketPath)) {
                Files.delete(socketPath);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete socket file: {}", socketPath, e);
        }

        logger.info("Service socket server stopped");
    }

    /**
     * Check if server is running.
     */
    public boolean isRunning() {
        return running.get() && serverChannel != null && serverChannel.isOpen();
    }
}

