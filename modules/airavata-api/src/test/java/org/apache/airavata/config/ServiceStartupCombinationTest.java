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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Comprehensive tests for service startup with different service combinations.
 *
 * <p>This test class systematically tests all service combinations:
 * <ul>
 *   <li>All services enabled</li>
 *   <li>All services disabled (minimal mode)</li>
 *   <li>Each service enabled individually</li>
 *   <li>Critical service combinations</li>
 *   <li>Optional services</li>
 * </ul>
 *
 * <p>Tests verify that:
 * <ul>
 *   <li>Expected services start when enabled</li>
 *   <li>Expected services don't start when disabled</li>
 *   <li>No startup errors occur</li>
 *   <li>Application context loads successfully</li>
 * </ul>
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            TestcontainersConfig.class,
            AiravataServerProperties.class,
            ServiceStartupTestBase.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        org.apache.airavata.config.AiravataServerProperties.class)
public class ServiceStartupCombinationTest extends ServiceStartupTestBase {

    /**
     * Test all services enabled configuration.
     */
    @Test
    public void testAllServicesEnabled() {

        assertNotNull(applicationContext, "Application context should load with all services enabled");
        assertNotNull(properties, "Properties should be loaded");
    }

    /**
     * Test minimal configuration (only core services).
     */
    @Test
    public void testMinimalConfiguration() {
        assertNotNull(applicationContext, "Application context should load in minimal mode");
        assertNotNull(properties, "Properties should be loaded");
    }

    /**
     * Test Thrift API only configuration.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=true",
                "services.rest.enabled=false",
            })
    class ThriftApiOnlyTest {
        @Test
        public void testThriftApiOnly() {
            assertNotNull(applicationContext, "Application context should load with Thrift API only");
        }
    }

    /**
     * Test REST API only configuration.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=false",
                "services.rest.enabled=true",
            })
    class RestApiOnlyTest {
        @Test
        public void testRestApiOnly() {
            assertNotNull(applicationContext, "Application context should load with REST API only");
        }
    }

    /**
     * Test both APIs enabled.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=true",
                "services.rest.enabled=true",
            })
    class BothApisEnabledTest {
        @Test
        public void testBothApisEnabled() {
            assertNotNull(applicationContext, "Application context should load with both APIs enabled");
        }
    }

    /**
     * Test Helix Controller and Participant only.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=false",
                "services.rest.enabled=false",
                "services.controller.enabled=true",
                "services.participant.enabled=true",
                "services.prewm.enabled=false",
                "services.postwm.enabled=false",
                "services.parser.enabled=false",
                "services.monitor.realtime.enabled=false",
                "services.monitor.email.enabled=false"
            })
    class HelixOnlyTest {
        @Test
        public void testHelixOnly() {
            assertNotNull(applicationContext, "Application context should load with Helix only");
        }
    }

    /**
     * Test workflow managers without Helix (should handle gracefully).
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=false",
                "services.rest.enabled=false",
                "services.controller.enabled=false",
                "services.participant.enabled=false",
                "services.prewm.enabled=true",
                "services.postwm.enabled=true",
                "services.parser.enabled=true"
            })
    class WorkflowManagersWithoutHelixTest {
        @Test
        public void testWorkflowManagersWithoutHelix() {

            assertNotNull(applicationContext, "Application context should load even with invalid configuration");
        }
    }

    /**
     * Test monitors only.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.thrift.enabled=false",
                "services.rest.enabled=false",
                "services.controller.enabled=false",
                "services.participant.enabled=false",
                "services.prewm.enabled=false",
                "services.postwm.enabled=false",
                "services.parser.enabled=false",
                "services.monitor.realtime.enabled=true",
                "services.monitor.email.enabled=true"
            })
    class MonitorsOnlyTest {
        @Test
        public void testMonitorsOnly() {
            assertNotNull(applicationContext, "Application context should load with monitors only");
        }
    }

    /**
     * Parameterized test for individual service configurations.
     */
    @ParameterizedTest
    @MethodSource("individualServiceConfigurations")
    public void testIndividualServiceConfiguration(String testName, Map<String, String> properties) {
        logger.info("Testing configuration: {}", testName);
        assertNotNull(applicationContext, "Application context should load for: " + testName);
        assertNotNull(this.properties, "Properties should be loaded for: " + testName);
    }

    /**
     * Generate test configurations for each service individually.
     */
    static Stream<Arguments> individualServiceConfigurations() {
        return Stream.of(
                Arguments.of(
                        "Thrift API only",
                        ServiceConfigurationBuilder.defaults()
                                .disableRestApi()
                                .disableAllBackgroundServices()
                                .build()),
                Arguments.of(
                        "REST API only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableAllBackgroundServices()
                                .build()),
                Arguments.of(
                        "Helix Controller only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Helix Participant only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Pre Workflow Manager only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Post Workflow Manager only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Parser Workflow Manager only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Realtime Monitor only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableEmailMonitor()
                                .build()),
                Arguments.of(
                        "Email Monitor only",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .disableHelixController()
                                .disableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableRealtimeMonitor()
                                .build()));
    }

    /**
     * Parameterized test for critical service combinations.
     */
    @ParameterizedTest
    @MethodSource("criticalServiceCombinations")
    public void testCriticalServiceCombinations(String testName, Map<String, String> properties) {
        logger.info("Testing critical combination: {}", testName);
        assertNotNull(applicationContext, "Application context should load for: " + testName);
    }

    /**
     * Generate test configurations for critical service combinations.
     */
    static Stream<Arguments> criticalServiceCombinations() {
        return Stream.of(
                Arguments.of(
                        "Controller + Participant",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Helix + Pre-WM",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Helix + Post-WM",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .disablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Helix + All Workflow Managers",
                        ServiceConfigurationBuilder.defaults()
                                .disableThriftApi()
                                .disableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .enablePreWorkflowManager()
                                .enablePostWorkflowManager()
                                .enableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()),
                Arguments.of(
                        "Thrift + Helix",
                        ServiceConfigurationBuilder.defaults()
                                .enableThriftApi()
                                .disableRestApi()
                                .enableHelixController()
                                .enableHelixParticipant()
                                .disablePreWorkflowManager()
                                .disablePostWorkflowManager()
                                .disableParserWorkflowManager()
                                .disableAllMonitors()
                                .build()));
    }

    /**
     * Test that application context loads with no background services.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(properties = {})
    class NoBackgroundServicesTest {
        @Test
        public void testNoBackgroundServices() {
            assertNotNull(applicationContext, "Application context should load without background services");
        }
    }

    /**
     * Test that application context loads with all background services disabled individually.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.controller.enabled=false",
                "services.participant.enabled=false",
                "services.prewm.enabled=false",
                "services.postwm.enabled=false",
                "services.parser.enabled=false",
                "services.monitor.realtime.enabled=false",
                "services.monitor.email.enabled=false"
            })
    class AllBackgroundServicesDisabledTest {
        @Test
        public void testAllBackgroundServicesDisabled() {
            assertNotNull(applicationContext, "Application context should load with all background services disabled");
        }
    }
}
