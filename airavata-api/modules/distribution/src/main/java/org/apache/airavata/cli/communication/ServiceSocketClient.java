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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.airavata.core.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for communicating with Airavata service via Unix domain socket.
 *
 * <p>Checks if the service is running by attempting to connect to the
 * fixed socket path (airavata.sock) and sending a health check request.
 */
public class ServiceSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceSocketClient.class);
    private static final String SOCKET_NAME = "airavata.sock";
    private static final String HEALTH_REQUEST = "HEALTH\n";
    private static final long CONNECTION_TIMEOUT_MS = 2000;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    /**
     * Resolve socket path using same logic as ServiceSocketManager.
     */
    public static Path resolveSocketPath(String configDir) {
        String socketPathProp = System.getProperty("airavata.socket.path");
        if (socketPathProp != null && !socketPathProp.isEmpty()) {
            return Paths.get(socketPathProp);
        }

        if (configDir != null && !configDir.isEmpty()) {
            var configPath = Paths.get(configDir);
            if (Files.exists(configPath) && Files.isWritable(configPath)) {
                return configPath.resolve(SOCKET_NAME);
            }
        }

        // Fallback to /tmp
        return Paths.get("/tmp", SOCKET_NAME);
    }

    /**
     * Check if socket file exists.
     */
    public static boolean socketExists(String configDir) {
        Path socketPath = resolveSocketPath(configDir);
        return Files.exists(socketPath);
    }

    /**
     * Check if service is running by connecting to socket and sending health check.
     */
    public static boolean isServiceRunning(String configDir) {
        Path socketPath = resolveSocketPath(configDir);

        if (!Files.exists(socketPath)) {
            return false;
        }

        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            // Set non-blocking for timeout
            channel.configureBlocking(false);
            boolean connected = channel.connect(UnixDomainSocketAddress.of(socketPath));

            if (!connected) {
                // Wait for connection with timeout
                long startTime = IdGenerator.getUniqueTimestamp().toEpochMilli();
                while (!connected
                        && (IdGenerator.getUniqueTimestamp().toEpochMilli() - startTime) < CONNECTION_TIMEOUT_MS) {
                    connected = channel.finishConnect();
                    if (!connected) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }
            }

            if (!connected) {
                return false;
            }

            // Send health check request
            var request = ByteBuffer.wrap(HEALTH_REQUEST.getBytes(StandardCharsets.UTF_8));
            while (request.hasRemaining()) {
                channel.write(request);
            }

            // Read response
            var response = ByteBuffer.allocate(4096);
            channel.configureBlocking(true);
            int bytesRead = channel.read(response);
            if (bytesRead > 0) {
                response.flip();
                String responseStr =
                        StandardCharsets.UTF_8.decode(response).toString().trim();
                // Parse JSON response
                try {
                    Map<String, Object> jsonResponse = objectMapper.readValue(responseStr, MAP_TYPE);
                    Object data = jsonResponse.get("data");
                    if (data instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        Object health = dataMap.get("health");
                        return "OK".equals(health);
                    }
                    // Check status directly
                    return "success".equals(jsonResponse.get("status"));
                } catch (Exception e) {
                    // Fallback to simple string check
                    return responseStr.contains("\"health\":\"OK\"") || responseStr.contains("\"status\":\"success\"");
                }
            }

            return false;
        } catch (IOException e) {
            logger.debug("Failed to connect to service socket: {}", socketPath, e);
            return false;
        }
    }

    /**
     * Get socket path for a given config directory.
     */
    public static Path getSocketPath(String configDir) {
        return resolveSocketPath(configDir);
    }

    /**
     * Send a command to the service socket and get JSON response.
     */
    public static Map<String, Object> sendCommand(String configDir, String command) throws IOException {
        Path socketPath = resolveSocketPath(configDir);

        if (!Files.exists(socketPath)) {
            throw new IOException("Service socket not found. Airavata service may not be running.");
        }

        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.configureBlocking(true);
            channel.connect(UnixDomainSocketAddress.of(socketPath));

            // Send command
            String commandLine = command.endsWith("\n") ? command : command + "\n";
            var request = ByteBuffer.wrap(commandLine.getBytes(StandardCharsets.UTF_8));
            while (request.hasRemaining()) {
                channel.write(request);
            }

            // Read response
            var response = ByteBuffer.allocate(8192);
            int totalBytes = 0;
            while (true) {
                int bytesRead = channel.read(response);
                if (bytesRead <= 0) {
                    break;
                }
                totalBytes += bytesRead;
                if (totalBytes >= 8192) {
                    // Response too large, but continue reading
                    break;
                }
            }

            if (totalBytes > 0) {
                response.flip();
                String responseStr =
                        StandardCharsets.UTF_8.decode(response).toString().trim();
                return objectMapper.readValue(responseStr, MAP_TYPE);
            }

            throw new IOException("No response from service");
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw e;
            }
            throw new IOException("Failed to send command: " + e.getMessage(), e);
        }
    }

    /**
     * Get service status via socket.
     */
    public static Map<String, Object> getServiceStatus(String configDir, String serviceName) throws IOException {
        String command = serviceName != null && !serviceName.isEmpty() ? "STATUS:" + serviceName : "STATUS";
        return sendCommand(configDir, command);
    }

    /**
     * List all services via socket.
     */
    public static Map<String, Object> listServices(String configDir) throws IOException {
        return sendCommand(configDir, "LIST");
    }

    /**
     * Start a service via socket.
     */
    public static Map<String, Object> startService(String configDir, String serviceName) throws IOException {
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        return sendCommand(configDir, "START:" + serviceName);
    }

    /**
     * Stop a service via socket.
     */
    public static Map<String, Object> stopService(String configDir, String serviceName) throws IOException {
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        return sendCommand(configDir, "STOP:" + serviceName);
    }

    /**
     * Restart a service via socket.
     */
    public static Map<String, Object> restartService(String configDir, String serviceName) throws IOException {
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        return sendCommand(configDir, "RESTART:" + serviceName);
    }
}
