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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.airavata.config.AiravataServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AiravataServerProperties properties;

    @Autowired
    private RestTemplate restTemplate;

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
        logger.info("Logout request received - idToken present: {}, refreshToken present: {}, redirectUri: {}",
                request.getIdToken() != null && !request.getIdToken().isEmpty(),
                request.getRefreshToken() != null && !request.getRefreshToken().isEmpty(),
                request.getPostLogoutRedirectUri());
        
        String keycloakUrl = getKeycloakServerUrl();
        String realm = getKeycloakRealm();
        String clientId = getKeycloakClientId();
        String clientSecret = getKeycloakClientSecret();

        if (keycloakUrl == null || realm == null) {
            logger.error("Keycloak configuration not available");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LogoutResponse(null, false, "Authentication server configuration not available"));
        }

        boolean serverLogoutSuccess = false;

        // Step 1: Revoke the refresh token (server-side logout)
        if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            try {
                serverLogoutSuccess = revokeRefreshToken(keycloakUrl, realm, clientId, clientSecret,
                        request.getRefreshToken());
            } catch (Exception e) {
                logger.warn("Failed to revoke refresh token: {}", e.getMessage());
                // Continue - we still want to return the logout URL
            }
        }

        // Step 2: Build the Keycloak logout URL for federated logout
        String logoutUrl = buildLogoutUrl(keycloakUrl, realm, request.getIdToken(),
                request.getPostLogoutRedirectUri());

        String message = serverLogoutSuccess
                ? "Server session invalidated. Redirect to logoutUrl for complete logout."
                : "Redirect to logoutUrl for logout.";

        return ResponseEntity.ok(new LogoutResponse(logoutUrl, serverLogoutSuccess, message));
    }

    /**
     * Simple GET endpoint that returns the logout URL for a given realm.
     * Useful for clients that just need the URL without token revocation.
     */
    @PostMapping("/logout-url")
    public ResponseEntity<Map<String, String>> getLogoutUrl(
            @RequestParam(required = false) String idTokenHint,
            @RequestParam(required = false) String postLogoutRedirectUri) {

        String keycloakUrl = getKeycloakServerUrl();
        String realm = getKeycloakRealm();

        if (keycloakUrl == null || realm == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication server configuration not available"));
        }

        String logoutUrl = buildLogoutUrl(keycloakUrl, realm, idTokenHint, postLogoutRedirectUri);

        Map<String, String> response = new HashMap<>();
        response.put("logoutUrl", logoutUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Revokes the refresh token at Keycloak's token endpoint.
     */
    private boolean revokeRefreshToken(String keycloakUrl, String realm, String clientId,
            String clientSecret, String refreshToken) {
        String revokeUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/revoke";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        StringBuilder formData = new StringBuilder();
        formData.append("client_id=").append(encode(clientId));
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.append("&client_secret=").append(encode(clientSecret));
        }
        formData.append("&token=").append(encode(refreshToken));
        formData.append("&token_type_hint=refresh_token");

        HttpEntity<String> request = new HttpEntity<>(formData.toString(), headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(revokeUrl, HttpMethod.POST, request, Void.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.debug("Successfully revoked refresh token");
            }
            return success;
        } catch (Exception e) {
            logger.warn("Failed to revoke refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds the Keycloak logout URL for federated logout.
     * 
     * This URL, when visited by the user's browser, will:
     * 1. Log out from Keycloak
     * 2. If Keycloak used an external IdP, trigger logout from that IdP
     * 3. Redirect to the postLogoutRedirectUri (only if id_token_hint is provided)
     * 
     * Note: According to OIDC RP-Initiated Logout spec, post_logout_redirect_uri 
     * requires id_token_hint for validation. Without id_token_hint, Keycloak will
     * show a confirmation page.
     */
    private String buildLogoutUrl(String keycloakUrl, String realm, String idToken, String postLogoutRedirectUri) {
        StringBuilder url = new StringBuilder();
        url.append(keycloakUrl);
        url.append("/realms/").append(realm);
        url.append("/protocol/openid-connect/logout");

        boolean hasIdToken = idToken != null && !idToken.isEmpty();

        // Add id_token_hint if available (required to identify the session)
        if (hasIdToken) {
            url.append("?id_token_hint=").append(encode(idToken));
            
            // Only add post_logout_redirect_uri if id_token_hint is present
            // (OIDC spec requires id_token_hint to validate the redirect URI)
            if (postLogoutRedirectUri != null && !postLogoutRedirectUri.isEmpty()) {
                url.append("&post_logout_redirect_uri=").append(encode(postLogoutRedirectUri));
            }
        }
        // If no id_token_hint, don't add post_logout_redirect_uri - Keycloak will show
        // a confirmation page to the user instead

        return url.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String getKeycloakServerUrl() {
        if (properties != null && properties.security() != null && properties.security().iam() != null) {
            return properties.security().iam().serverUrl();
        }
        return null;
    }

    private String getKeycloakRealm() {
        if (properties != null && properties.security() != null && properties.security().iam() != null) {
            return properties.security().iam().realm();
        }
        return "default"; // Default realm
    }

    private String getKeycloakClientId() {
        if (properties != null && properties.security() != null && properties.security().iam() != null) {
            return properties.security().iam().oauthClientId();
        }
        return "pga"; // Default client ID
    }

    private String getKeycloakClientSecret() {
        if (properties != null && properties.security() != null && properties.security().iam() != null) {
            return properties.security().iam().oauthClientSecret();
        }
        return null;
    }
}
