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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the Prometheus metrics endpoint served by {@code MonitoringServer}
 * is reachable and returns valid Prometheus text format output.
 */
@Tag("runtime")
class PrometheusHealthTest {

    private static final String HOST = System.getProperty("airavata.monitoring.host", "localhost");
    private static final int PORT = Integer.parseInt(System.getProperty("airavata.monitoring.port", "9097"));

    private static final String METRICS_URL = "http://" + HOST + ":" + PORT + "/metrics";

    private static HttpClient httpClient;

    @BeforeAll
    static void setUp() {
        httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Test
    void metricsEndpointShouldReturn200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(METRICS_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(
                200, response.statusCode(), "Expected HTTP 200 from /metrics endpoint, got " + response.statusCode());
    }

    @Test
    void metricsEndpointShouldReturnPrometheusTextFormat() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(METRICS_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        assertTrue(
                body.contains("# HELP") || body.contains("# TYPE"),
                "Expected Prometheus text format with '# HELP' or '# TYPE' markers, but body was: "
                        + body.substring(0, Math.min(body.length(), 500)));
    }
}
