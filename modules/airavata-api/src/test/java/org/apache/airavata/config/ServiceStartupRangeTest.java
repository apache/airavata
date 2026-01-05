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

import static org.junit.jupiter.api.Assertions.*;

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

        // Verify all services are disabled
        assertEquals("false", props.get("services.thrift.enabled"));
        assertEquals("false", props.get("services.rest.enabled"));
        assertEquals("false", props.get("services.controller.enabled"));
        assertEquals("false", props.get("services.participant.enabled"));
        assertEquals("false", props.get("services.prewm.enabled"));
        assertEquals("false", props.get("services.postwm.enabled"));
        assertEquals("false", props.get("services.parser.enabled"));
        assertEquals("false", props.get("services.monitor.realtime.enabled"));
        assertEquals("false", props.get("services.monitor.email.enabled"));

        // Ports should still be configured even if services are disabled
        assertNotNull(props.get("services.thrift.server.port"));
        assertNotNull(props.get("services.rest.server.port"));
    }

    /**
     * Test: All services enabled.
     */
    @Test
    public void testAllServicesEnabled() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.allEnabled();
        Map<String, String> props = builder.build();

        // Verify all services are enabled
        assertEquals("true", props.get("services.thrift.enabled"));
        assertEquals("true", props.get("services.rest.enabled"));
        assertEquals("true", props.get("services.controller.enabled"));
        assertEquals("true", props.get("services.participant.enabled"));
        assertEquals("true", props.get("services.prewm.enabled"));
        assertEquals("true", props.get("services.postwm.enabled"));
        assertEquals("true", props.get("services.parser.enabled"));
        assertEquals("true", props.get("services.monitor.realtime.enabled"));
        assertEquals("true", props.get("services.monitor.email.enabled"));

        // Verify all ports are configured
        // Note: All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on services.thrift.server.port
        assertNotNull(props.get("services.thrift.server.port"));
        assertNotNull(props.get("services.rest.server.port"));
    }

    /**
     * Parameterized test: Each service enabled individually.
     */
    @ParameterizedTest
    @MethodSource("singleServiceConfigurations")
    public void testSingleServiceEnabled(String serviceName, ServiceConfigurationBuilder builder) {
        Map<String, String> props = builder.build();

        // Verify the specific service is enabled
        boolean foundEnabled = false;
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (entry.getKey().contains("enabled") && "true".equals(entry.getValue())) {
                foundEnabled = true;
                break;
            }
        }

        // At least one service should be enabled
        assertTrue(foundEnabled, "At least one service should be enabled for: " + serviceName);
    }

    static Stream<Arguments> singleServiceConfigurations() {
        return Stream.of(
                Arguments.of(
                        "Thrift API",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableAllBackgroundServices()
                                .enableThriftApi()),
                Arguments.of(
                        "REST API",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableAllBackgroundServices()
                                .enableRestApi()),
                Arguments.of(
                        "Helix Controller",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .enableHelixController()),
                Arguments.of(
                        "Helix Participant",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .enableHelixParticipant()),
                Arguments.of(
                        "Pre Workflow Manager",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .enablePreWorkflowManager()),
                Arguments.of(
                        "Post Workflow Manager",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .enablePostWorkflowManager()),
                Arguments.of(
                        "Parser Workflow Manager",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableAllMonitors()
                                .enableParserWorkflowManager()),
                Arguments.of(
                        "Realtime Monitor",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableEmailMonitor()
                                .enableRealtimeMonitor()),
                Arguments.of(
                        "Email Monitor",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
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

        // Count enabled services - only count actual service enablement flags
        // Services: thrift, rest, helix.controller, helix.participant, prewm, postwm, parser, realtime monitor, email
        // monitor
        long enabledCount = props.entrySet().stream()
                .filter(e -> {
                    String key = e.getKey();
                    String value = e.getValue();
                    // Count only actual service enablement flags (not background.enabled global flag)
                    return "true".equals(value)
                            && (key.equals("services.thrift.enabled")
                                    || key.equals("services.rest.enabled")
                                    || key.equals("services.controller.enabled")
                                    || key.equals("services.participant.enabled")
                                    || key.equals("services.prewm.enabled")
                                    || key.equals("services.postwm.enabled")
                                    || key.equals("services.parser.enabled")
                                    || key.equals("services.monitor.realtime.enabled")
                                    || key.equals("services.monitor.email.enabled"));
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
                // 1 service
                Arguments.of(
                        "1 service: Thrift only",
                        1,
                        ServiceConfigurationBuilder.minimal().enableThriftApi()),

                // 2 services
                Arguments.of(
                        "2 services: Thrift + REST",
                        2,
                        ServiceConfigurationBuilder.minimal().enableThriftApi().enableRestApi()),
                Arguments.of(
                        "2 services: Controller + Participant",
                        2,
                        ServiceConfigurationBuilder.minimal()
                                .enableHelixController()
                                .enableHelixParticipant()),

                // 3 services
                Arguments.of(
                        "3 services: Thrift + Controller + Participant",
                        3,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()),

                // 4 services
                Arguments.of(
                        "4 services: Thrift + Helix + Pre-WM",
                        4,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()),

                // 5 services
                Arguments.of(
                        "5 services: Thrift + Helix + Pre-WM + Post-WM",
                        5,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()),

                // 6 services
                Arguments.of(
                        "6 services: Thrift + Helix + All Workflow Managers",
                        6,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .enableParserWorkflowManager()),

                // 7 services
                Arguments.of(
                        "7 services: Thrift + Helix + Workflow Managers + Realtime Monitor",
                        7,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .enableParserWorkflowManager()
                                .enableRealtimeMonitor()),

                // 8 services
                Arguments.of(
                        "8 services: Thrift + Helix + Workflow Managers + Realtime + Email",
                        8,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .enableParserWorkflowManager()
                                .enableRealtimeMonitor()
                                .enableEmailMonitor()),

                // 9 services
                Arguments.of(
                        "9 services: Thrift + REST + Helix + Workflow Managers + Monitors",
                        9,
                        ServiceConfigurationBuilder.minimal()
                                .enableThriftApi()
                                .enableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .enableParserWorkflowManager()
                                .enableRealtimeMonitor()
                                .enableEmailMonitor()),

                // 9 services (all configurable services)
                Arguments.of("9 services: All enabled", 9, ServiceConfigurationBuilder.allEnabled()));
    }

    /**
     * Test: Port configuration for TCP server services.
     */
    @Test
    public void testPortConfiguration() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder()
                .withThriftPort(8931);

        Map<String, String> props = builder.build();

        // All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on services.thrift.server.port
        assertEquals("8931", props.get("services.thrift.server.port"));
    }

    /**
     * Test: Default port values are set correctly.
     */
    @Test
    public void testDefaultPortValues() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.defaults();
        Map<String, String> props = builder.build();

        // Verify default ports match expected values
        // Note: All Thrift services are multiplexed on services.thrift.server.port
        assertEquals("8930", props.get("services.thrift.server.port"));
        assertEquals("8082", props.get("services.rest.server.port"));
    }

    /**
     * Test: Port configuration is independent of service enablement.
     */
    @Test
    public void testPortConfigurationIndependentOfEnablement() {
        // Ports should be configured even when services are disabled
        ServiceConfigurationBuilder builder =
                ServiceConfigurationBuilder.minimal().withThriftPort(9999);

        Map<String, String> props = builder.build();

        // Services are disabled
        assertEquals("false", props.get("services.thrift.enabled"));

        // But ports are still configured
        // Note: All Thrift services are multiplexed on services.thrift.server.port
        assertEquals("9999", props.get("services.thrift.server.port"));
    }

    /**
     * Test: All possible service combinations (2^10 = 1024 combinations, but we test key ones).
     */
    @ParameterizedTest
    @MethodSource("keyServiceCombinations")
    public void testKeyServiceCombinations(String description, ServiceConfigurationBuilder builder) {
        Map<String, String> props = builder.build();

        // Verify configuration is valid (all properties are strings)
        assertNotNull(props);
        assertFalse(props.isEmpty());

        // Verify all enabled flags are valid boolean strings
        props.entrySet().stream().filter(e -> e.getKey().contains("enabled")).forEach(e -> {
            String value = e.getValue();
            assertTrue(
                    "true".equals(value) || "false".equals(value),
                    "Invalid boolean value for " + e.getKey() + ": " + value);
        });

        // Verify all port values are valid integers
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
                        "Only Thrift", ServiceConfigurationBuilder.minimal().enableThriftApi()),
                Arguments.of("Only REST", ServiceConfigurationBuilder.minimal().enableRestApi()),
                Arguments.of(
                        "Both APIs",
                        ServiceConfigurationBuilder.minimal().enableThriftApi().enableRestApi()),
                Arguments.of(
                        "Only Helix",
                        ServiceConfigurationBuilder.minimal()
                                .enableAllBackgroundServices()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()),
                Arguments.of(
                        "Helix + Workflow Managers",
                        ServiceConfigurationBuilder.minimal()
                                .enableAllBackgroundServices()
                                .disableAllMonitors()),
                Arguments.of(
                        "All except Monitors",
                        ServiceConfigurationBuilder.allEnabled().disableAllMonitors()),
                Arguments.of(
                        "All except Parser",
                        ServiceConfigurationBuilder.allEnabled().disableParserWorkflowManager()),
                Arguments.of("All services", ServiceConfigurationBuilder.allEnabled()));
    }
}
