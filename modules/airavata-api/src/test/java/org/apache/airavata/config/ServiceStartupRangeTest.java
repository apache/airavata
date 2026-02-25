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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Comprehensive tests covering the full range from no services enabled to all services enabled.
 *
 * <p>This test class systematically tests:
 * <ul>
 *   <li>No services enabled (minimal/core only)</li>
 *   <li>Single service enabled (each service individually)</li>
 *   <li>Progressive combinations (1, 2, 3, ... services)</li>
 *   <li>All services enabled</li>
 *   <li>Port configuration for TCP server services</li>
 * </ul>
 */
public class ServiceStartupRangeTest {

    /**
     * Test: No services enabled (only core services).
     */
    @Test
    public void testNoServicesEnabled() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.minimal();
        Map<String, String> props = builder.build();

        assertEquals("false", props.get("airavata.services.controller.enabled"));
        assertEquals("false", props.get("airavata.services.participant.enabled"));
        assertEquals("false", props.get("airavata.services.monitor.realtime.enabled"));
        assertEquals("false", props.get("airavata.services.monitor.email.enabled"));

        assertNotNull(props.get("airavata.services.http.server.port"));
        assertNotNull(props.get("airavata.services.grpc.server.port"));
    }

    /**
     * Test: All services enabled.
     */
    @Test
    public void testAllServicesEnabled() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.allEnabled();
        Map<String, String> props = builder.build();

        assertEquals("true", props.get("airavata.services.controller.enabled"));
        assertEquals("true", props.get("airavata.services.participant.enabled"));
        assertEquals("true", props.get("airavata.services.monitor.realtime.enabled"));
        assertEquals("true", props.get("airavata.services.monitor.email.enabled"));

        assertNotNull(props.get("airavata.services.http.server.port"));
        assertNotNull(props.get("airavata.services.grpc.server.port"));
    }

    /**
     * Parameterized test: Each service enabled individually.
     */
    @ParameterizedTest
    @MethodSource("singleServiceConfigurations")
    public void testSingleServiceEnabled(String serviceName, ServiceConfigurationBuilder builder) {
        Map<String, String> props = builder.build();

        boolean foundEnabled = false;
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (entry.getKey().contains("enabled") && "true".equals(entry.getValue())) {
                foundEnabled = true;
                break;
            }
        }

        assertTrue(foundEnabled, "At least one service should be enabled for: " + serviceName);
    }

    static Stream<Arguments> singleServiceConfigurations() {
        return Stream.of(
                Arguments.of(
                        "Airavata REST API",
                        ServiceConfigurationBuilder.defaults()
                                .disableAllBackgroundServices()
                                .enableRestApi()),
                Arguments.of(
                        "Controller",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableParticipant()
                                .disableAllMonitors()
                                .enableController()),
                Arguments.of(
                        "Participant",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableController()
                                .disableAllMonitors()
                                .enableParticipant()),
                Arguments.of(
                        "Realtime Monitor",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableController()
                                .disableParticipant()
                                .disableEmailMonitor()
                                .enableRealtimeMonitor()),
                Arguments.of(
                        "Email Monitor",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableController()
                                .disableParticipant()
                                .disableRealtimeMonitor()
                                .enableEmailMonitor()));
    }

    /**
     * Parameterized test: Progressive service combinations (1, 2, 3, ... services).
     */
    @ParameterizedTest
    @MethodSource("progressiveServiceCombinations")
    public void testProgressiveServiceCombinations(
            String description, int expectedEnabledCount, ServiceConfigurationBuilder builder) {
        Map<String, String> props = builder.build();

        long enabledCount = props.entrySet().stream()
                .filter(e -> {
                    String key = e.getKey();
                    String value = e.getValue();

                    return "true".equals(value)
                            && (key.equals("airavata.services.controller.enabled")
                                    || key.equals("airavata.services.participant.enabled")
                                    || key.equals("airavata.services.monitor.realtime.enabled")
                                    || key.equals("airavata.services.monitor.email.enabled"));
                })
                .count();

        assertEquals(
                expectedEnabledCount,
                enabledCount,
                "Expected " + expectedEnabledCount + " services enabled for: " + description + ". Actual properties: "
                        + props);
    }

    static Stream<Arguments> progressiveServiceCombinations() {
        return Stream.of(
                Arguments.of(
                        "1 service: Controller only",
                        1,
                        ServiceConfigurationBuilder.minimal().enableController()),
                Arguments.of(
                        "2 services: Controller + Participant",
                        2,
                        ServiceConfigurationBuilder.minimal()
                                .enableController()
                                .enableParticipant()),
                Arguments.of(
                        "3 services: Controller + Participant + Realtime Monitor",
                        3,
                        ServiceConfigurationBuilder.minimal()
                                .enableController()
                                .enableParticipant()
                                .enableRealtimeMonitor()),
                Arguments.of(
                        "4 services: All background services",
                        4,
                        ServiceConfigurationBuilder.minimal()
                                .enableController()
                                .enableParticipant()
                                .enableRealtimeMonitor()
                                .enableEmailMonitor()),
                Arguments.of(
                        "4 services: All enabled (REST not counted)",
                        4,
                        ServiceConfigurationBuilder.allEnabled()));
    }

    /**
     * Test: Port configuration for TCP server services.
     */
    @Test
    public void testPortConfiguration() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder().withRestPort(8081);

        Map<String, String> props = builder.build();

        assertEquals("8081", props.get("airavata.services.http.server.port"));
    }

    /**
     * Test: Default port values are set correctly.
     */
    @Test
    public void testDefaultPortValues() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.defaults();
        Map<String, String> props = builder.build();

        assertEquals("8080", props.get("airavata.services.http.server.port"));
        assertEquals("9090", props.get("airavata.services.grpc.server.port"));
    }

    /**
     * Test: Port configuration is independent of service enablement.
     */
    @Test
    public void testPortConfigurationIndependentOfEnablement() {

        ServiceConfigurationBuilder builder =
                ServiceConfigurationBuilder.minimal().withRestPort(9999);

        Map<String, String> props = builder.build();

        assertEquals("9999", props.get("airavata.services.http.server.port"));
    }

    /**
     * Test: Key service combinations produce valid configurations.
     */
    @ParameterizedTest
    @MethodSource("keyServiceCombinations")
    public void testKeyServiceCombinations(String description, ServiceConfigurationBuilder builder) {
        Map<String, String> props = builder.build();

        assertNotNull(props);
        assertFalse(props.isEmpty());

        props.entrySet().stream().filter(e -> e.getKey().contains("enabled")).forEach(e -> {
            String value = e.getValue();
            assertTrue(
                    "true".equals(value) || "false".equals(value),
                    "Invalid boolean value for " + e.getKey() + ": " + value);
        });

        props.entrySet().stream()
                .filter(e -> e.getKey().contains("port") || e.getKey().contains("Port"))
                .forEach(e -> {
                    String value = e.getValue();
                    assertDoesNotThrow(
                            () -> Integer.parseInt(value), "Invalid port value for " + e.getKey() + ": " + value);
                    int port = Integer.parseInt(value);
                    assertTrue(port > 0 && port <= 65535, "Port out of range for " + e.getKey() + ": " + port);
                });
    }

    static Stream<Arguments> keyServiceCombinations() {
        return Stream.of(
                Arguments.of("No services", ServiceConfigurationBuilder.minimal()),
                Arguments.of(
                        "Only Airavata REST API",
                        ServiceConfigurationBuilder.minimal().enableRestApi()),
                Arguments.of(
                        "Only Controller + Participant",
                        ServiceConfigurationBuilder.minimal()
                                .enableAllBackgroundServices()
                                .disableAllMonitors()),
                Arguments.of(
                        "All except Monitors",
                        ServiceConfigurationBuilder.allEnabled().disableAllMonitors()),
                Arguments.of("All services", ServiceConfigurationBuilder.allEnabled()));
    }
}
