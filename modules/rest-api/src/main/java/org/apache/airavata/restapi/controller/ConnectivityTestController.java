/**
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
package org.apache.airavata.restapi.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/connectivity-test")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ConnectivityTestController {

    @PostMapping("/ssh")
    public ResponseEntity<?> testSSHConnection(@RequestBody Map<String, String> request) {
        String host = request.get("host");
        int port = Integer.parseInt(request.getOrDefault("port", "22"));
        String username = request.get("username");
        String privateKey = request.get("privateKey");
        String password = request.get("password");

        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test basic connectivity (port open)
            boolean portOpen = testPort(host, port, 5000);
            
            if (!portOpen) {
                result.put("success", false);
                result.put("message", "Cannot connect to " + host + ":" + port);
                result.put("details", "Port is not accessible");
                return ResponseEntity.ok(result);
            }

            // For SSH key authentication, we would use JSch or similar
            // For now, we'll just verify the port is open
            // In production, implement full SSH connection test
            result.put("success", true);
            result.put("message", "SSH port is accessible");
            result.put("details", "Port " + port + " is open on " + host);
            result.put("authentication", privateKey != null ? "SSH key" : password != null ? "Password" : "Not tested");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Connection test failed: " + e.getMessage());
            result.put("details", e.getClass().getSimpleName());
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/sftp")
    public ResponseEntity<?> testSFTPConnection(@RequestBody Map<String, String> request) {
        // SFTP uses SSH, so same test
        return testSSHConnection(request);
    }

    @PostMapping("/slurm")
    public ResponseEntity<?> testSLURMConnection(@RequestBody Map<String, String> request) {
        String host = request.get("host");
        int sshPort = Integer.parseInt(request.getOrDefault("sshPort", "22"));
        int slurmPort = Integer.parseInt(request.getOrDefault("slurmPort", "6817"));

        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test SSH port (for job submission)
            boolean sshOpen = testPort(host, sshPort, 5000);
            
            // Test SLURM controller port
            boolean slurmOpen = testPort(host, slurmPort, 5000);
            
            result.put("success", sshOpen && slurmOpen);
            result.put("sshPort", sshPort);
            result.put("sshAccessible", sshOpen);
            result.put("slurmPort", slurmPort);
            result.put("slurmAccessible", slurmOpen);
            
            if (sshOpen && slurmOpen) {
                result.put("message", "SLURM cluster is accessible");
            } else {
                result.put("message", "Some ports are not accessible");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Connection test failed: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    private boolean testPort(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
