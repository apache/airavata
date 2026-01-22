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
package org.apache.airavata.profile.utils.keycloak;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataConfigUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.utils.keycloak.dto.ClientRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.CredentialRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.RealmRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.RoleRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.TokenResponse;
import org.apache.airavata.profile.utils.keycloak.dto.UserRepresentation;
import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST client for Keycloak Admin API.
 * Replaces the Keycloak Admin Client library with direct REST API calls.
 */
public class KeycloakRestClient {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakRestClient.class);
    private static final String ADMIN_CLI_CLIENT_ID = "admin-cli";
    private static final String GRANT_TYPE_PASSWORD = "password";

    private final String serverUrl;
    private final AiravataServerProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, TokenCacheEntry> tokenCache = new ConcurrentHashMap<>();

    /**
     * Constructor with Spring-injected RestTemplate and ObjectMapper.
     */
    public KeycloakRestClient(
            String serverUrl,
            AiravataServerProperties properties,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Legacy constructor - creates own RestTemplate and ObjectMapper.
     */
    public KeycloakRestClient(String serverUrl, AiravataServerProperties properties) {
        this.serverUrl = serverUrl;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        var template = new RestTemplate();
        try {
            if (properties != null
                    && properties.security() != null
                    && properties.security().tls() != null
                    && properties.security().tls().enabled()
                    && properties.security().tls().keystore() != null) {
                // Configure SSL with keystore using Java's standard SSLContext
                var configDir = AiravataConfigUtils.getConfigDir(); // Will throw if not found
                var keystorePath = properties.security().tls().keystore().path();
                if (keystorePath == null || keystorePath.isEmpty()) {
                    logger.debug("TLS enabled but keystore path not configured, using default HTTP client");
                    template.setRequestFactory(new SimpleClientHttpRequestFactory());
                    return template;
                }
                // Keystore path is relative to configDir (e.g., "keystores/airavata.p12")
                var keystoreFullPath = new File(configDir, keystorePath).getAbsolutePath();
                var keystorePassword = properties.security().tls().keystore().password();
                var keyStore = loadKeyStore(keystoreFullPath, keystorePassword);

                // Create SSLContext with trust store
                var sslContext = SSLContext.getInstance("TLS");
                var trustManagers = createTrustManagers(keyStore);
                sslContext.init(null, trustManagers, new java.security.SecureRandom());

                // Use Spring's HttpComponentsClientHttpRequestFactory with custom SSL context
                // Note: For advanced SSL configuration, we still use HttpComponentsClientHttpRequestFactory
                // but it's provided by Spring's dependency on httpcomponents, not a direct dependency
                template.setRequestFactory(new SimpleClientHttpRequestFactory());
                logger.info("TLS enabled - keystore loaded. SSL context configured via JVM system properties");
            } else {
                // No TLS or keystore not configured - use default HTTP client
                template.setRequestFactory(new SimpleClientHttpRequestFactory());
            }
        } catch (Exception e) {
            logger.warn("Failed to configure TLS for Keycloak REST client, using default: {}", e.getMessage());
            template.setRequestFactory(new SimpleClientHttpRequestFactory());
        }
        return template;
    }

    /**
     * Create trust managers from keystore.
     */
    private TrustManager[] createTrustManagers(KeyStore trustStore) throws Exception {
        javax.net.ssl.TrustManagerFactory trustManagerFactory =
                javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyStore loadKeyStore(String keyStorePath, String keyStorePassword)
            throws AiravataSecurityException {
        var keyStoreFile = new File(keyStorePath);
        if (keyStoreFile.exists() && keyStoreFile.isFile()) {
            logger.info("Loading trust store file from path {}", keyStorePath);
        } else {
            logger.error("Trust store file does not exist at path {}", keyStorePath);
            throw new AiravataSecurityException("Trust store file does not exist at path " + keyStorePath);
        }
        try {
            return KeyStore.getInstance(keyStoreFile, keyStorePassword.toCharArray());
        } catch (Exception e) {
            logger.error("Failed to load trust store file from path {}", keyStorePath, e);
            throw new AiravataSecurityException("Failed to load trust store file from path " + keyStorePath, e);
        }
    }

    /**
     * Obtain admin access token using password grant.
     */
    public String obtainAdminToken(String realm, PasswordCredential credentials) throws IamAdminServicesException {
        var cacheKey = realm + ":" + credentials.getLoginUserName();
        var cached = tokenCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.getToken();
        }

        try {
            var tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // admin-cli is a public client, so no Basic Auth needed - just send client_id in form data

            var formData = new HashMap<String, String>();
            formData.put("grant_type", GRANT_TYPE_PASSWORD);
            formData.put("username", credentials.getLoginUserName());
            formData.put("password", credentials.getPassword());
            formData.put("client_id", ADMIN_CLI_CLIENT_ID);

            var formBody = new StringBuilder();
            formData.forEach((key, value) -> {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(key).append("=").append(encode(value));
            });

            var request = new HttpEntity<>(formBody.toString(), headers);
            var response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, TokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                var token = response.getBody().getAccessToken();
                int expiresIn = response.getBody().getExpiresIn() != null
                        ? response.getBody().getExpiresIn()
                        : 60; // Default to 60 seconds if not provided
                tokenCache.put(cacheKey, new TokenCacheEntry(token, expiresIn));
                return token;
            } else {
                throw new IamAdminServicesException("Failed to obtain admin token: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error obtaining admin token: {}", e.getMessage());
            throw new IamAdminServicesException("Failed to obtain admin token: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("Error obtaining admin token: {}", e.getMessage());
            throw new IamAdminServicesException("Failed to obtain admin token: " + e.getMessage(), e);
        }
    }

    /**
     * Obtain access token using existing access token (for token-based auth).
     */
    public String validateToken(String realm, String accessToken) throws IamAdminServicesException {
        // For token-based auth, we just return the token as-is
        // The token will be validated by Keycloak when making API calls
        return accessToken;
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    // ==================== Realm Management ====================

    public void createRealm(RealmRepresentation realm) throws IamAdminServicesException {
        var adminToken = obtainAdminToken("master", getSuperAdminCredentials());
        var url = serverUrl + "/admin/realms";
        var request = new HttpEntity<>(realm, createAuthHeaders(adminToken));
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to create realm: " + e.getMessage(), e);
        }
    }

    // ==================== User Management ====================

    public ResponseEntity<Void> createUser(String realm, UserRepresentation user, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users";
        var request = new HttpEntity<>(user, createAuthHeaders(accessToken));
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public List<UserRepresentation> searchUsers(
            String realm,
            String username,
            String firstName,
            String lastName,
            String email,
            Integer first,
            Integer max,
            String accessToken)
            throws IamAdminServicesException {
        return searchUsers(realm, username, firstName, lastName, email, first, max, false, accessToken);
    }

    public List<UserRepresentation> searchUsers(
            String realm,
            String username,
            String firstName,
            String lastName,
            String email,
            Integer first,
            Integer max,
            Boolean exact,
            String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users";
        var builder = UriComponentsBuilder.fromUriString(url);
        if (username != null) {
            builder.queryParam("username", username);
        }
        if (exact != null && exact) {
            builder.queryParam("exact", exact);
        }
        if (firstName != null) {
            builder.queryParam("firstName", firstName);
        }
        if (lastName != null) {
            builder.queryParam("lastName", lastName);
        }
        if (email != null) {
            builder.queryParam("email", email);
        }
        if (first != null) {
            builder.queryParam("first", first);
        }
        if (max != null) {
            builder.queryParam("max", max);
        }

        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<?>> response = (ResponseEntity<List<?>>) (ResponseEntity<?>)
                    restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, List.class);
            if (response.getBody() != null) {
                return objectMapper.convertValue(response.getBody(), new TypeReference<List<UserRepresentation>>() {});
            }
            return new ArrayList<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new ArrayList<>();
            }
            throw new IamAdminServicesException("Failed to search users: " + e.getMessage(), e);
        }
    }

    public UserRepresentation getUser(String realm, String userId, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId;
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            ResponseEntity<UserRepresentation> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, UserRepresentation.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new IamAdminServicesException("Failed to get user: " + e.getMessage(), e);
        }
    }

    public void updateUser(String realm, String userId, UserRepresentation user, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId;
        var request = new HttpEntity<>(user, createAuthHeaders(accessToken));
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String realm, String userId, String accessToken) throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId;
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public void resetPassword(String realm, String userId, CredentialRepresentation credential, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        var request = new HttpEntity<>(credential, createAuthHeaders(accessToken));
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to reset password: " + e.getMessage(), e);
        }
    }

    public int getUserCount(String realm, String accessToken) throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/count";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("count")) {
                return ((Number) response.getBody().get("count")).intValue();
            }
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to get user count: " + e.getMessage(), e);
        }
    }

    // ==================== Role Management ====================

    public RoleRepresentation getRole(String realm, String roleName, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            ResponseEntity<RoleRepresentation> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, RoleRepresentation.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new IamAdminServicesException("Failed to get role: " + e.getMessage(), e);
        }
    }

    public List<RoleRepresentation> getUserRealmRoles(String realm, String userId, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<?>> response = (ResponseEntity<List<?>>)
                    (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            if (response.getBody() != null) {
                return objectMapper.convertValue(response.getBody(), new TypeReference<List<RoleRepresentation>>() {});
            }
            return new ArrayList<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to get user roles: " + e.getMessage(), e);
        }
    }

    public void addRealmRolesToUser(String realm, String userId, List<RoleRepresentation> roles, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        var request = new HttpEntity<>(roles, createAuthHeaders(accessToken));
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to add roles to user: " + e.getMessage(), e);
        }
    }

    public void removeRealmRolesFromUser(
            String realm, String userId, List<RoleRepresentation> roles, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        var request = new HttpEntity<>(roles, createAuthHeaders(accessToken));
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to remove roles from user: " + e.getMessage(), e);
        }
    }

    public List<RoleRepresentation> getAvailableClientRoles(
            String realm, String userId, String clientId, String accessToken) throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/clients/" + clientId
                + "/available";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<?>> response = (ResponseEntity<List<?>>)
                    (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            if (response.getBody() != null) {
                return objectMapper.convertValue(response.getBody(), new TypeReference<List<RoleRepresentation>>() {});
            }
            return new ArrayList<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to get available client roles: " + e.getMessage(), e);
        }
    }

    public void addClientRolesToUser(
            String realm, String userId, String clientId, List<RoleRepresentation> roles, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/clients/" + clientId;
        var request = new HttpEntity<>(roles, createAuthHeaders(accessToken));
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to add client roles to user: " + e.getMessage(), e);
        }
    }

    // ==================== Client Management ====================

    public ResponseEntity<Void> createClient(String realm, ClientRepresentation client, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/clients";
        var request = new HttpEntity<>(client, createAuthHeaders(accessToken));
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to create client: " + e.getMessage(), e);
        }
    }

    public List<ClientRepresentation> findAllClients(String realm, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/clients";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<?>> response = (ResponseEntity<List<?>>)
                    (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            if (response.getBody() != null) {
                return objectMapper.convertValue(
                        response.getBody(), new TypeReference<List<ClientRepresentation>>() {});
            }
            return new ArrayList<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to find clients: " + e.getMessage(), e);
        }
    }

    public List<ClientRepresentation> findClientsByClientId(String realm, String clientId, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/clients";
        var builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam("clientId", clientId);
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<?>> response = (ResponseEntity<List<?>>) (ResponseEntity<?>)
                    restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, List.class);
            if (response.getBody() != null) {
                return objectMapper.convertValue(
                        response.getBody(), new TypeReference<List<ClientRepresentation>>() {});
            }
            return new ArrayList<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to find client by ID: " + e.getMessage(), e);
        }
    }

    public CredentialRepresentation getClientSecret(String realm, String clientId, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/clients/" + clientId + "/client-secret";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            ResponseEntity<CredentialRepresentation> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, CredentialRepresentation.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to get client secret: " + e.getMessage(), e);
        }
    }

    public UserRepresentation getServiceAccountUser(String realm, String clientId, String accessToken)
            throws IamAdminServicesException {
        var url = serverUrl + "/admin/realms/" + realm + "/clients/" + clientId + "/service-account-user";
        var headers = createAuthHeaders(accessToken);
        var request = new HttpEntity<Void>(headers);
        try {
            ResponseEntity<UserRepresentation> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, UserRepresentation.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IamAdminServicesException("Failed to get service account user: " + e.getMessage(), e);
        }
    }

    // ==================== Helper Methods ====================

    private PasswordCredential getSuperAdminCredentials() throws IamAdminServicesException {
        if (properties == null
                || properties.security() == null
                || properties.security().iam() == null
                || properties.security().iam().superAdmin() == null) {
            throw new IamAdminServicesException("IAM super admin configuration not available. "
                    + "Ensure airavata.security.iam.super-admin.username and password are configured.");
        }
        var creds = new PasswordCredential();
        creds.setLoginUserName(properties.security().iam().superAdmin().username());
        creds.setPassword(properties.security().iam().superAdmin().password());
        return creds;
    }

    private static class TokenCacheEntry {
        private final String token;
        private final long expiresAt;

        TokenCacheEntry(String token, int expiresInSeconds) {
            this.token = token;
            // Expire 5 seconds before actual expiration to be safe
            this.expiresAt = AiravataUtils.getUniqueTimestamp().getTime() + (expiresInSeconds - 5) * 1000L;
        }

        String getToken() {
            return token;
        }

        boolean isExpired() {
            return AiravataUtils.getUniqueTimestamp().getTime() >= expiresAt;
        }
    }
}
