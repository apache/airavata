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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Verifies that Keycloak is reachable, the OIDC discovery document is valid,
 * and that a client-credentials token can be obtained for the test client.
 */
@Tag("integration")
class KeycloakHealthTest {

    private static final String BASE_URL =
            System.getProperty("airavata.keycloak.url", "http://localhost:18080");
    private static final String REALM =
            System.getProperty("airavata.keycloak.realm", "default");
    private static final String CLIENT_ID =
            System.getProperty("airavata.keycloak.client.id", "cs-jupyterlab");
    private static final String CLIENT_SECRET =
            System.getProperty("airavata.keycloak.client.secret", "DxeMtfiWU1qkDEmaGHf13RDahCujzhy1");

    private static final String OIDC_URL =
            BASE_URL + "/realms/" + REALM + "/.well-known/openid-configuration";

    private static HttpClient httpClient;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void oidcDiscoveryShouldReturn200WithTokenEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OIDC_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Expected HTTP 200 from OIDC discovery endpoint, got " + response.statusCode());

        JsonNode doc = objectMapper.readTree(response.body());
        JsonNode tokenEndpoint = doc.get("token_endpoint");
        assertNotNull(tokenEndpoint, "OIDC discovery document is missing 'token_endpoint'");
        assertTrue(tokenEndpoint.asText().startsWith("http"),
                "token_endpoint is not a valid URL: " + tokenEndpoint.asText());
    }

    @Test
    void clientCredentialsGrantShouldReturnAccessToken() throws Exception {
        // First retrieve the token endpoint from the discovery document
        HttpRequest discoveryRequest = HttpRequest.newBuilder()
                .uri(URI.create(OIDC_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> discoveryResponse =
                httpClient.send(discoveryRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, discoveryResponse.statusCode(), "Could not reach Keycloak OIDC discovery");

        String tokenEndpoint = objectMapper.readTree(discoveryResponse.body())
                .get("token_endpoint")
                .asText();

        String form = "grant_type=" + encode("client_credentials")
                + "&client_id=" + encode(CLIENT_ID)
                + "&client_secret=" + encode(CLIENT_SECRET);

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> tokenResponse =
                httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, tokenResponse.statusCode(),
                "client_credentials grant failed with status " + tokenResponse.statusCode()
                        + ": " + tokenResponse.body());

        JsonNode tokenDoc = objectMapper.readTree(tokenResponse.body());
        JsonNode accessToken = tokenDoc.get("access_token");
        assertNotNull(accessToken, "Token response is missing 'access_token'");
        assertTrue(accessToken.asText().length() > 0, "access_token is empty");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
