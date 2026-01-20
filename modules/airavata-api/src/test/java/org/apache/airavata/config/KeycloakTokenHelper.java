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
package org.apache.airavata.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.security.model.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class to obtain real OAuth tokens from Keycloak testcontainer.
 * This enables genuine integration testing against Keycloak.
 */
public class KeycloakTokenHelper {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakTokenHelper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    // Cache tokens to avoid repeated auth calls
    private static volatile String cachedAccessToken = null;
    private static volatile long tokenExpiry = 0;

    /**
     * Get a real OAuth access token from Keycloak using Resource Owner Password Grant.
     * Uses default-admin credentials from realm-default.json.
     *
     * @param keycloakUrl The Keycloak base URL (e.g., http://localhost:32768)
     * @param realm The realm name (e.g., "default")
     * @param clientId The OAuth client ID (e.g., "pga")
     * @param clientSecret The OAuth client secret
     * @param username The username to authenticate
     * @param password The user's password
     * @return Access token string
     * @throws RuntimeException if authentication fails
     */
    public static String getAccessToken(
            String keycloakUrl, String realm, String clientId, String clientSecret, String username, String password) {

        // Check cache
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return cachedAccessToken;
        }

        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        logger.info("Obtaining OAuth token from: {}", tokenUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", username);
            body.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String accessToken = json.get("access_token").asText();
                int expiresIn = json.get("expires_in").asInt();

                // Cache with 60 second buffer
                cachedAccessToken = accessToken;
                tokenExpiry = System.currentTimeMillis() + (expiresIn - 60) * 1000L;

                logger.info("Successfully obtained OAuth token, expires in {} seconds", expiresIn);
                return accessToken;
            } else {
                throw new RuntimeException("Failed to obtain token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to obtain OAuth token from Keycloak: {}", e.getMessage());
            throw new RuntimeException("Keycloak authentication failed: " + e.getMessage(), e);
        }
    }

    // Default credentials from realm-default.json for testing
    private static final String DEFAULT_REALM = "default";
    private static final String DEFAULT_CLIENT_ID = "pga";
    private static final String DEFAULT_CLIENT_SECRET = "m36BXQIxX3j3VILadeHMK5IvbOeRlCCc";
    private static final String DEFAULT_ADMIN_USERNAME = "default-admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    /**
     * Create an AuthzToken with real credentials from Keycloak.
     *
     * @param keycloakUrl The Keycloak base URL
     * @param properties Server properties containing OAuth config (optional, uses defaults if null)
     * @param gatewayId The gateway ID to include in claims
     * @param username The username to authenticate and include in claims
     * @return AuthzToken with real access token and proper claims
     */
    public static AuthzToken createRealAuthzToken(
            String keycloakUrl, AiravataServerProperties properties, String gatewayId, String username) {

        // Get credentials from properties or use defaults from realm-default.json
        String clientId = DEFAULT_CLIENT_ID;
        String clientSecret = DEFAULT_CLIENT_SECRET;
        String adminUsername = DEFAULT_ADMIN_USERNAME;
        String adminPassword = DEFAULT_ADMIN_PASSWORD;

        // Use OAuth client credentials from properties if available
        // Note: superAdmin credentials are for Keycloak server admin, NOT realm users
        // We use the realm's default-admin user credentials for token acquisition
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null) {
            var iam = properties.security().iam();
            if (iam.oauthClientId() != null && !iam.oauthClientId().isEmpty()) {
                clientId = iam.oauthClientId();
            }
            if (iam.oauthClientSecret() != null && !iam.oauthClientSecret().isEmpty()) {
                clientSecret = iam.oauthClientSecret();
            }
            // Don't use superAdmin for realm user auth - those are Keycloak server admin creds
            // Realm user credentials are fixed from realm-default.json: default-admin/admin123
        }

        String accessToken =
                getAccessToken(keycloakUrl, DEFAULT_REALM, clientId, clientSecret, adminUsername, adminPassword);

        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(accessToken);

        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.GATEWAY_ID, gatewayId);
        claimsMap.put(Constants.USER_NAME, username);
        authzToken.setClaimsMap(claimsMap);

        return authzToken;
    }

    /**
     * Invalidate cached token (useful for test cleanup).
     */
    public static void invalidateCache() {
        cachedAccessToken = null;
        tokenExpiry = 0;
    }
}
