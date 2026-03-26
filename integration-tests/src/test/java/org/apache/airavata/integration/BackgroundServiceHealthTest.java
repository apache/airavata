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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that each expected background service is registered and reported
 * by the {@code /health/services} endpoint.
 *
 * <p>The parameterized test asserts that every listed service name appears in
 * the health response. Services are expected to match the labels used in
 * {@code AiravataServer#startBackgroundServices()}.
 */
@Tag("integration")
class BackgroundServiceHealthTest {

    private static final String HOST =
            System.getProperty("airavata.monitoring.host", "localhost");
    private static final int PORT =
            Integer.parseInt(System.getProperty("airavata.monitoring.port", "9097"));

    private static final String HEALTH_URL = "http://" + HOST + ":" + PORT + "/health/services";

    private static HttpClient httpClient;
    private static ObjectMapper objectMapper;
    private static JsonNode cachedRoot;

    @BeforeAll
    static void fetchHealthResponse() throws Exception {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HEALTH_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(),
                "Could not reach /health/services — got HTTP " + response.statusCode());
        JsonNode body = objectMapper.readTree(response.body());
        cachedRoot = body.get("services");
        assertNotNull(cachedRoot, "Expected 'services' key in /health/services response");
    }

    static Stream<String> expectedServices() {
        return Stream.of(
                "db_event_manager",
                "monitoring_server",
                "helix_controller",
                "helix_participant",
                "pre_workflow_manager",
                "post_workflow_manager",
                "parser_workflow_manager",
                "realtime_monitor");
    }

    @ParameterizedTest(name = "{0} is registered")
    @MethodSource("expectedServices")
    void serviceShouldBeRegistered(String serviceName) {
        assertNotNull(cachedRoot, "/health/services response was not fetched");
        assertTrue(cachedRoot.has(serviceName),
                "Expected service '" + serviceName + "' to be registered, "
                        + "but it was not found in the health response. "
                        + "Registered services: " + cachedRoot.fieldNames());
    }

    @ParameterizedTest(name = "{0} is UP with positive uptime")
    @MethodSource("expectedServices")
    void registeredServiceShouldBeUp(String serviceName) {
        assertNotNull(cachedRoot, "/health/services response was not fetched");

        JsonNode serviceNode = cachedRoot.get(serviceName);
        assertNotNull(serviceNode, "Service '" + serviceName + "' is not in the health response");

        JsonNode statusNode = serviceNode.get("status");
        assertNotNull(statusNode, "Service '" + serviceName + "' has no 'status' field");
        assertEquals("UP", statusNode.asText(),
                "Service '" + serviceName + "' is not UP — status: " + statusNode.asText());

        JsonNode uptimeNode = serviceNode.get("uptimeMs");
        assertNotNull(uptimeNode, "Service '" + serviceName + "' has no 'uptimeMs' field");
        assertTrue(uptimeNode.asLong() > 0,
                "Service '" + serviceName + "' is UP but uptimeMs is not positive: "
                        + uptimeNode.asLong());
    }
}
