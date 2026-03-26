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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Verifies the {@code /health/services} endpoint on the MonitoringServer.
 *
 * <p>The endpoint returns a JSON object whose keys are service names and values are
 * {@code ServiceStatus} objects with fields: {@code status} (UP/DOWN),
 * {@code uptimeMs} (long), and {@code lastError} (string or null).
 *
 * <p>Every service that is UP must report a positive uptimeMs.
 */
@Tag("integration")
class ServiceHealthEndpointTest {

    private static final String HOST =
            System.getProperty("airavata.monitoring.host", "localhost");
    private static final int PORT =
            Integer.parseInt(System.getProperty("airavata.monitoring.port", "9097"));

    private static final String HEALTH_URL = "http://" + HOST + ":" + PORT + "/health/services";

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
    void healthEndpointShouldReturn200() throws Exception {
        HttpResponse<String> response = getServicesResponse();
        assertEquals(200, response.statusCode(),
                "Expected HTTP 200 from /health/services, got " + response.statusCode());
    }

    @Test
    void healthResponseShouldBeNonEmptyJsonObject() throws Exception {
        HttpResponse<String> response = getServicesResponse();
        assertEquals(200, response.statusCode());

        JsonNode root = objectMapper.readTree(response.body());
        assertNotNull(root, "Response body could not be parsed as JSON");
        assertTrue(root.isObject(), "Expected JSON object at root, got: " + root.getNodeType());
        JsonNode services = root.get("services");
        assertNotNull(services, "Expected 'services' key in response, got: " + root);
        assertFalse(services.isEmpty(), "Expected at least one service entry in the response");
    }

    @Test
    void allUpServicesShouldHavePositiveUptime() throws Exception {
        HttpResponse<String> response = getServicesResponse();
        assertEquals(200, response.statusCode());

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode services = root.get("services");
        assertNotNull(services, "Expected 'services' key in response");
        Iterator<Map.Entry<String, JsonNode>> fields = services.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String serviceName = entry.getKey();
            JsonNode serviceNode = entry.getValue();

            JsonNode statusNode = serviceNode.get("status");
            assertNotNull(statusNode, "Service '" + serviceName + "' is missing 'status' field");

            if ("UP".equals(statusNode.asText())) {
                JsonNode uptimeNode = serviceNode.get("uptimeMs");
                assertNotNull(uptimeNode,
                        "UP service '" + serviceName + "' is missing 'uptimeMs' field");
                assertTrue(uptimeNode.asLong() > 0,
                        "UP service '" + serviceName + "' has non-positive uptimeMs: "
                                + uptimeNode.asLong());
            }
        }
    }

    private HttpResponse<String> getServicesResponse() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HEALTH_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
