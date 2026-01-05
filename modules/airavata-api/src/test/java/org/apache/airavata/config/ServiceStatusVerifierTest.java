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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Unit tests for ServiceStatusVerifier.
 * These tests verify the verifier logic with mock ApplicationContext.
 */
public class ServiceStatusVerifierTest {

    private ApplicationContext applicationContext;
    private AiravataServerProperties properties;
    private ServiceStatusVerifier verifier;

    @BeforeEach
    public void setUp() {
        // Create a minimal Spring context for testing
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AiravataPropertiesConfiguration.class);
        ctx.refresh();

        this.applicationContext = ctx;
        this.properties = ctx.getBean(AiravataServerProperties.class);
        this.verifier = new ServiceStatusVerifier(applicationContext, properties);
    }

    @Test
    public void testIsServiceEnabled_ThriftApi() {
        // Default configuration has thrift enabled
        properties.services.thrift.enabled = true;
        assertTrue(verifier.isServiceEnabled("thrift-api"));

        properties.services.thrift.enabled = false;
        assertFalse(verifier.isServiceEnabled("thrift-api"));
    }

    @Test
    public void testIsServiceEnabled_RestApi() {
        // Default configuration has rest disabled
        properties.services.rest.enabled = false;
        assertFalse(verifier.isServiceEnabled("rest-api"));

        properties.services.rest.enabled = true;
        assertTrue(verifier.isServiceEnabled("rest-api"));
    }

    @Test
    public void testIsServiceEnabled_HelixController() {
        properties.services.controller.enabled = true;
        assertTrue(verifier.isServiceEnabled("helix-controller"));

        properties.services.controller.enabled = false;
        assertFalse(verifier.isServiceEnabled("helix-controller"));
    }

    @Test
    public void testIsServiceEnabled_HelixParticipant() {
        properties.services.participant.enabled = true;
        assertTrue(verifier.isServiceEnabled("helix-participant"));

        properties.services.participant.enabled = false;
        assertFalse(verifier.isServiceEnabled("helix-participant"));
    }

    @Test
    public void testIsServiceEnabled_WorkflowManagers() {
        properties.services.prewm.enabled = true;
        assertTrue(verifier.isServiceEnabled("pre-workflow-manager"));

        properties.services.postwm.enabled = true;
        assertTrue(verifier.isServiceEnabled("post-workflow-manager"));

        properties.services.parser.enabled = true;
        assertTrue(verifier.isServiceEnabled("parser-workflow-manager"));
    }

    @Test
    public void testIsServiceEnabled_Monitors() {
        properties.services.monitor.realtime.enabled = true;
        assertTrue(verifier.isServiceEnabled("realtime-monitor"));

        properties.services.monitor.email.enabled = true;
        assertTrue(verifier.isServiceEnabled("email-monitor"));
    }

    @Test
    public void testIsServiceEnabled_UnknownService() {
        assertFalse(verifier.isServiceEnabled("unknown-service"));
    }

    @Test
    public void testIsServiceRunning() {
        // In test context, isServiceRunning checks if enabled
        properties.services.thrift.enabled = true;
        assertTrue(verifier.isServiceRunning("thrift-api"));

        properties.services.thrift.enabled = false;
        assertFalse(verifier.isServiceRunning("thrift-api"));
    }

    @Test
    public void testIsPortListening_UnusedPort() {
        // Test with a port that's unlikely to be listening (should return false)
        // Using a high port number that's valid but typically unused
        assertFalse(verifier.isPortListening(65534));
    }

    @Test
    public void testGetAllServiceNames() {
        var serviceNames = verifier.getAllServiceNames();
        assertNotNull(serviceNames);
        assertTrue(serviceNames.size() > 0);
        assertTrue(serviceNames.contains("thrift-api"));
        assertTrue(serviceNames.contains("rest-api"));
        assertTrue(serviceNames.contains("helix-controller"));
    }

    @Test
    public void testVerifyServicesRunning() {
        properties.services.thrift.enabled = true;
        properties.services.rest.enabled = true;

        var result = verifier.verifyServicesRunning("thrift-api", "rest-api");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessfulServices().size());
    }

    @Test
    public void testVerifyServicesNotRunning() {
        properties.services.thrift.enabled = false;
        properties.services.rest.enabled = false;

        var result = verifier.verifyServicesNotRunning("thrift-api", "rest-api");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessfulServices().size());
    }

    @Test
    public void testVerifyServicesRunning_WithFailures() {
        properties.services.thrift.enabled = true;
        properties.services.rest.enabled = false; // This one is disabled

        var result = verifier.verifyServicesRunning("thrift-api", "rest-api");
        assertFalse(result.isSuccess());
        assertEquals(1, result.getSuccessfulServices().size());
        assertEquals(1, result.getFailedServices().size());
        assertTrue(result.getFailedServices().containsKey("rest-api"));
    }
}
