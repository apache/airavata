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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.ssh.SSHUtil;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/connectivity-test")
@Tag(name = "Connectivity Test")
public class ConnectivityTestController {

    private final CredentialStoreService credentialStoreService;
    private final AuthorizationService authorizationService;

    public ConnectivityTestController(
            CredentialStoreService credentialStoreService, AuthorizationService authorizationService) {
        this.credentialStoreService = credentialStoreService;
        this.authorizationService = authorizationService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return request != null ? (AuthzToken) request.getAttribute("authzToken") : null;
    }

    /**
     * Validates SSH authentication using a stored credential (actual SSH login, not just port check).
     * Request body: credentialToken, hostname, loginUsername (required), gatewayId (optional, from auth), port (optional, default 22).
     * Login username is not stored on the credential; it is set per resource in the access grant and must be supplied here.
     */
    @PostMapping("/ssh/validate")
    public ResponseEntity<?> validateSSHCredential(
            @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        AuthzToken authzToken = getAuthzToken(httpRequest);
        String gatewayId = request.get("gatewayId") != null
                ? request.get("gatewayId").toString().trim()
                : null;
        if (gatewayId == null || gatewayId.isBlank()) {
            if (authzToken != null
                    && authzToken.getClaimsMap() != null
                    && authzToken.getClaimsMap().get(Constants.GATEWAY_ID) != null) {
                gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID).toString();
            }
        }
        if (gatewayId == null || gatewayId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "gatewayId is required"));
        }
        if (authzToken != null) {
            authorizationService.requireGatewayAccess(authzToken, gatewayId);
        }

        String credentialToken = request.get("credentialToken") != null
                ? request.get("credentialToken").toString()
                : null;
        String hostname =
                request.get("hostname") != null ? request.get("hostname").toString() : null;
        String loginUsername = request.get("loginUsername") != null
                ? request.get("loginUsername").toString().trim()
                : null;
        if (loginUsername != null && loginUsername.isEmpty()) {
            loginUsername = null;
        }
        int port = 22;
        if (request.get("port") != null) {
            try {
                port = request.get("port") instanceof Number n
                        ? n.intValue()
                        : Integer.parseInt(request.get("port").toString());
            } catch (NumberFormatException e) {
                port = 22;
            }
        }

        if (credentialToken == null || credentialToken.isBlank() || hostname == null || hostname.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "credentialToken and hostname are required"));
        }

        SSHCredential sshCred;
        try {
            sshCred = credentialStoreService.getSSHCredential(credentialToken, gatewayId);
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
        if (sshCred == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "SSH credential not found"));
        }

        if (loginUsername == null || loginUsername.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Login username is required. Pass loginUsername in the request (it is set per resource in the access grant)."));
        }
        String username = loginUsername;

        Map<String, Object> result = new HashMap<>();
        try {
            // Optional: test port first
            boolean portOpen = testPort(hostname, port, 5000);
            result.put("portAccessible", portOpen);
            if (!portOpen) {
                result.put("success", false);
                result.put("message", "Port " + port + " is not accessible on " + hostname);
                return ResponseEntity.ok(result);
            }

            SSHUtil.validate(hostname, port, username, sshCred);
            result.put("success", true);
            result.put("message", "SSH authentication successful");
            result.put("username", username);
            result.put("auth_validated", true);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", "SSH authentication failed: " + e.getMessage());
            result.put("auth_validated", false);
            return ResponseEntity.ok(result);
        }
    }

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
