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
package org.apache.airavata.iam.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.airavata.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service responsible for all Keycloak logout-related HTTP interactions.
 *
 * <p>This service encapsulates:
 * <ul>
 *   <li>Refresh-token revocation via the Keycloak token revocation endpoint
 *       ({@code /realms/{realm}/protocol/openid-connect/revoke}).
 *   <li>Construction of the Keycloak RP-Initiated Logout URL
 *       ({@code /realms/{realm}/protocol/openid-connect/logout}) per the OIDC spec.
 * </ul>
 *
 * <p>Keycloak coordinates are sourced exclusively from {@link ServerProperties}.
 * All HTTP concerns are handled here so that callers (e.g. REST controllers) remain a
 * thin delegation layer with no direct knowledge of the Keycloak API.
 */
@Service
public class DefaultKeycloakLogoutService implements KeycloakLogoutService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultKeycloakLogoutService.class);

    private final ServerProperties properties;
    private final RestTemplate restTemplate;

    public DefaultKeycloakLogoutService(ServerProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns the Keycloak server URL from configuration, or {@code null} if unavailable.
     */
    public String getKeycloakServerUrl() {
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null) {
            return properties.security().iam().serverUrl();
        }
        return null;
    }

    /**
     * Returns the Keycloak realm from configuration, defaulting to {@code "default"}.
     */
    public String getKeycloakRealm() {
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null) {
            return properties.security().iam().realm();
        }
        return "default";
    }

    /**
     * Revokes the supplied refresh token at Keycloak's token revocation endpoint.
     *
     * <p>Issues an HTTP POST to
     * {@code {keycloakServerUrl}/realms/{realm}/protocol/openid-connect/revoke}
     * with {@code token_type_hint=refresh_token}. Returns {@code true} when Keycloak
     * responds with a 2xx status; returns {@code false} on any error without propagating
     * the exception, so callers can continue building a logout URL even when revocation fails.
     *
     * @param refreshToken the refresh token to revoke; must not be {@code null} or blank
     * @return {@code true} if Keycloak accepted the revocation, {@code false} otherwise
     */
    public boolean revokeRefreshToken(String refreshToken) {
        String keycloakUrl = getKeycloakServerUrl();
        String realm = getKeycloakRealm();
        String clientId = getClientId();
        String clientSecret = getClientSecret();

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
     * Builds the Keycloak RP-Initiated Logout URL.
     *
     * <p>When {@code idToken} is non-blank the URL includes {@code id_token_hint}, which
     * is required by the OIDC spec to allow {@code post_logout_redirect_uri} to be
     * honoured. Without {@code id_token_hint} Keycloak will show an interactive
     * confirmation page instead of performing the redirect automatically.
     *
     * <p>This method is pure URL construction — it makes no outbound HTTP call.
     *
     * @param idToken               the ID token issued at login (may be {@code null})
     * @param postLogoutRedirectUri the URI to redirect to after logout (may be {@code null})
     * @return a fully-formed Keycloak logout URL
     */
    public String buildLogoutUrl(String idToken, String postLogoutRedirectUri) {
        String keycloakUrl = getKeycloakServerUrl();
        String realm = getKeycloakRealm();

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
        // If no id_token_hint, omit post_logout_redirect_uri — Keycloak will show
        // a confirmation page to the user instead

        return url.toString();
    }

    /**
     * Returns {@code true} when Keycloak server URL and realm are both configured.
     */
    public boolean isConfigured() {
        return getKeycloakServerUrl() != null && getKeycloakRealm() != null;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String getClientId() {
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null) {
            return properties.security().iam().oauthClientId();
        }
        return "pga";
    }

    private String getClientSecret() {
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null) {
            return properties.security().iam().oauthClientSecret();
        }
        return null;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
