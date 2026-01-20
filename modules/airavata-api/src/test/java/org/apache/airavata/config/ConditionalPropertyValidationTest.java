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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Validates that conditional property annotations work correctly.
 * Uses a minimal Spring context to avoid complex test configuration issues.
 */
@SpringBootTest(classes = ConditionalPropertyValidationTest.MinimalConfig.class)
@ActiveProfiles("test")
public class ConditionalPropertyValidationTest {

    @Configuration
    // application.properties is auto-loaded by Spring Boot
    @EnableConfigurationProperties(AiravataServerProperties.class)
    static class MinimalConfig {}

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    // ==================== Property Binding Tests ====================

    @Test
    @DisplayName("Verify all required properties are bound correctly")
    void testPropertiesAreBound() {
        assertNotNull(properties, "Properties should be loaded");

        // Security properties
        assertNotNull(properties.security(), "Security properties should be configured");
        assertNotNull(properties.security().iam(), "IAM properties should be configured");
        assertNotNull(properties.security().authzCache(), "AuthzCache properties should be configured");
        assertNotNull(properties.security().authentication(), "Authentication properties should be configured");

        // Service properties
        assertNotNull(properties.services(), "Services properties should be configured");
        assertNotNull(properties.services().participant(), "Participant properties should be configured");
        assertNotNull(properties.services().controller(), "Controller properties should be configured");
        assertNotNull(properties.services().monitor(), "Monitor properties should be configured");
        assertNotNull(properties.services().scheduler(), "Scheduler properties should be configured");
    }

    @Test
    @DisplayName("Verify scheduler policy properties are bound correctly")
    void testSchedulerPolicyPropertiesAreBound() {
        assertNotNull(
                properties.services().scheduler().selectionPolicy(), "Scheduler selection-policy should be configured");
        assertNotNull(
                properties.services().scheduler().reschedulerPolicy(),
                "Scheduler rescheduler-policy should be configured");

        // Verify default values
        assertEquals(
                "DefaultComputeResourceSelectionPolicy",
                properties.services().scheduler().selectionPolicy(),
                "Default selection policy should be DefaultComputeResourceSelectionPolicy");
        assertEquals(
                "ExponentialBackOffReScheduler",
                properties.services().scheduler().reschedulerPolicy(),
                "Default rescheduler policy should be ExponentialBackOffReScheduler");
    }

    @Test
    @DisplayName("Verify security enabled flags are bound correctly")
    void testSecurityEnabledFlagsAreBound() {
        // IAM is enabled for tests via Keycloak testcontainer
        assertTrue(
                properties.security().iam().enabled(),
                "airavata.security.iam.enabled should be true for tests with Keycloak");
        assertTrue(
                properties.security().authentication().enabled(),
                "airavata.security.authentication.enabled should be true");
        assertTrue(properties.security().authzCache().enabled(), "airavata.security.authzCache.enabled should be true");
    }

    @Test
    @DisplayName("Verify flyway is disabled in test")
    void testFlywayDisabled() {
        assertFalse(properties.flyway().enabled(), "flyway.enabled should be false in test");
    }

    @Test
    @DisplayName("Verify property naming conventions are consistent")
    void testPropertyNamingConventions() {
        // All these should be properly bound using kebab-case in properties file
        // but accessible via camelCase in Java

        assertNotNull(properties.services().monitor().compute(), "services.monitor.compute should be accessible");
        assertNotNull(properties.services().monitor().email(), "services.monitor.email should be accessible");
        assertNotNull(properties.services().monitor().realtime(), "services.monitor.realtime should be accessible");

        // Verify the enabled flags are accessible
        assertTrue(
                properties.services().monitor().compute().enabled(),
                "services.monitor.compute.enabled should be true in test");
    }

    @Test
    @DisplayName("Verify all conditional enabled properties exist in airavata.properties")
    void testAllConditionalPropertiesExist() {
        // Core services
        assertNotNull(properties.services().controller(), "services.controller must be set");
        assertNotNull(properties.services().participant(), "services.participant must be set");

        // Workflow managers
        assertNotNull(properties.services().prewm(), "services.prewm must be set");
        assertNotNull(properties.services().postwm(), "services.postwm must be set");
        assertNotNull(properties.services().parser(), "services.parser must be set");

        // Monitor services
        assertNotNull(properties.services().monitor().compute(), "services.monitor.compute must be set");
        assertNotNull(properties.services().monitor().email(), "services.monitor.email must be set");
        assertNotNull(properties.services().monitor().realtime(), "services.monitor.realtime must be set");

        // Scheduler services
        assertNotNull(properties.services().scheduler(), "services.scheduler must be set");
        assertNotNull(properties.services().scheduler().interpreter(), "services.scheduler.interpreter must be set");
        assertNotNull(properties.services().scheduler().rescheduler(), "services.scheduler.rescheduler must be set");

        // Telemetry
        assertNotNull(properties.services().telemetry(), "services.telemetry must be set");

        // API services
        assertNotNull(properties.services().rest(), "services.rest must be set");
        assertNotNull(properties.services().thrift(), "services.thrift must be set");

        // Database migration
        assertNotNull(properties.flyway(), "flyway must be set");

        // Security
        assertNotNull(properties.security().iam(), "security.iam must be set");
        assertNotNull(properties.security().authentication(), "security.authentication must be set");
        assertNotNull(properties.security().authzCache(), "security.authz-cache must be set");

        // Sharing
        assertNotNull(properties.sharing(), "airavata.sharing must be set");
    }

    @Test
    @DisplayName("Verify scheduler policies are configured")
    void testSchedulerPoliciesConfigured() {
        String selectionPolicy = properties.services().scheduler().selectionPolicy();
        String reschedulerPolicy = properties.services().scheduler().reschedulerPolicy();

        assertNotNull(selectionPolicy, "Selection policy must be configured");
        assertNotNull(reschedulerPolicy, "Rescheduler policy must be configured");

        // Verify policies are valid class names
        assertTrue(
                selectionPolicy.equals("DefaultComputeResourceSelectionPolicy")
                        || selectionPolicy.equals("MultipleComputeResourcePolicy"),
                "Selection policy must be a valid policy class name");

        assertTrue(
                reschedulerPolicy.equals("ExponentialBackOffReScheduler"),
                "Rescheduler policy must be a valid rescheduler class name");
    }
}
