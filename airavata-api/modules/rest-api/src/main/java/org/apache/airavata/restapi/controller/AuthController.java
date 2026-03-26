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
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.iam.service.KeycloakLogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication operations including federated logout.
 *
 * This controller handles Single Logout (SLO) by:
 * 1. Revoking the refresh token at Keycloak (server-side logout)
 * 2. Returning the Keycloak end_session_endpoint URL for full federated logout
 *    (including any upstream identity providers like CILogon)
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final KeycloakLogoutService keycloakLogoutService;

    public AuthController(KeycloakLogoutService keycloakLogoutService) {
        this.keycloakLogoutService = keycloakLogoutService;
    }

    /**
     * Request body for logout operation
     */
    public static class LogoutRequest {
        private String refreshToken;
        private String idToken;
        private String postLogoutRedirectUri;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }

        public String getPostLogoutRedirectUri() {
            return postLogoutRedirectUri;
        }

        public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
            this.postLogoutRedirectUri = postLogoutRedirectUri;
        }
    }

    /**
     * Response containing the federated logout URL
     */
    public static class LogoutResponse {
        private String logoutUrl;
        private boolean serverLogoutSuccess;
        private String message;

        public LogoutResponse(String logoutUrl, boolean serverLogoutSuccess, String message) {
            this.logoutUrl = logoutUrl;
            this.serverLogoutSuccess = serverLogoutSuccess;
            this.message = message;
        }

        public String getLogoutUrl() {
            return logoutUrl;
        }

        public boolean isServerLogoutSuccess() {
            return serverLogoutSuccess;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Performs federated logout.
     *
     * This endpoint:
     * 1. Revokes the refresh token at Keycloak (invalidates server-side session)
     * 2. Returns the Keycloak logout URL for the client to redirect to
     *
     * The client should redirect to the returned logoutUrl to complete the
     * federated logout chain, which will:
     * - Log out from Keycloak
     * - If using external IdP (e.g., CILogon), log out from that as well
     * - Redirect back to the postLogoutRedirectUri
     *
     * @param request Contains refreshToken, idToken, and postLogoutRedirectUri
     * @return LogoutResponse with the federated logout URL
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestBody LogoutRequest request) {
        logger.info(
                "Logout request received - idToken present: {}, refreshToken present: {}, redirectUri: {}",
                request.getIdToken() != null && !request.getIdToken().isEmpty(),
                request.getRefreshToken() != null && !request.getRefreshToken().isEmpty(),
                request.getPostLogoutRedirectUri());

        if (!keycloakLogoutService.isConfigured()) {
            logger.error("Keycloak configuration not available");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LogoutResponse(null, false, "Authentication server configuration not available"));
        }

        boolean serverLogoutSuccess = false;

        // Step 1: Revoke the refresh token (server-side logout)
        if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            try {
                serverLogoutSuccess = keycloakLogoutService.revokeRefreshToken(request.getRefreshToken());
            } catch (Exception e) {
                logger.warn("Failed to revoke refresh token: {}", e.getMessage());
                // Continue - we still want to return the logout URL
            }
        }

        // Step 2: Build the Keycloak logout URL for federated logout
        String logoutUrl =
                keycloakLogoutService.buildLogoutUrl(request.getIdToken(), request.getPostLogoutRedirectUri());

        String message = serverLogoutSuccess
                ? "Server session invalidated. Redirect to logoutUrl for complete logout."
                : "Redirect to logoutUrl for logout.";

        return ResponseEntity.ok(new LogoutResponse(logoutUrl, serverLogoutSuccess, message));
    }

    /**
     * Simple POST endpoint that returns the logout URL for a given realm.
     * Useful for clients that just need the URL without token revocation.
     */
    @PostMapping("/logout-url")
    public ResponseEntity<Map<String, String>> getLogoutUrl(
            @RequestParam(required = false) String idTokenHint,
            @RequestParam(required = false) String postLogoutRedirectUri) {

        if (!keycloakLogoutService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication server configuration not available"));
        }

        String logoutUrl = keycloakLogoutService.buildLogoutUrl(idTokenHint, postLogoutRedirectUri);

        Map<String, String> response = new HashMap<>();
        response.put("logoutUrl", logoutUrl);
        return ResponseEntity.ok(response);
    }
}
