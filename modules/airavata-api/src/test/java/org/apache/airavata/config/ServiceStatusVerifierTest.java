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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for ServiceStatusVerifier using mocked properties.
 * Since AiravataServerProperties is an immutable record, we use Mockito to mock it.
 */
public class ServiceStatusVerifierTest {

    private ApplicationContext applicationContext;
    private AiravataServerProperties properties;
    private ServiceStatusVerifier verifier;

    @BeforeEach
    public void setUp() {
        applicationContext = mock(ApplicationContext.class);
        properties = createMockProperties(true, true, true, true, true, true, true, true, true, true, true, true, true);
        verifier = new ServiceStatusVerifier(applicationContext, properties);
    }

    private AiravataServerProperties createMockProperties(
            boolean thriftEnabled,
            boolean restEnabled,
            boolean controllerEnabled,
            boolean participantEnabled,
            boolean prewmEnabled,
            boolean postwmEnabled,
            boolean parserEnabled,
            boolean realtimeEnabled,
            boolean emailEnabled,
            boolean computeEnabled,
            boolean researchEnabled,
            boolean agentEnabled,
            boolean fileserverEnabled) {

        // Mock the nested records
        var thrift = mock(AiravataServerProperties.Services.Thrift.class);
        when(thrift.enabled()).thenReturn(thriftEnabled);

        var rest = mock(AiravataServerProperties.Services.Rest.class);
        when(rest.enabled()).thenReturn(restEnabled);

        var controller = mock(AiravataServerProperties.Services.Controller.class);
        when(controller.enabled()).thenReturn(controllerEnabled);

        var participant = mock(AiravataServerProperties.Services.Participant.class);
        when(participant.enabled()).thenReturn(participantEnabled);

        var prewm = mock(AiravataServerProperties.Services.PreWm.class);
        when(prewm.enabled()).thenReturn(prewmEnabled);

        var postwm = mock(AiravataServerProperties.Services.PostWm.class);
        when(postwm.enabled()).thenReturn(postwmEnabled);

        var parser = mock(AiravataServerProperties.Services.Parser.class);
        when(parser.enabled()).thenReturn(parserEnabled);

        var realtime = mock(AiravataServerProperties.Services.Monitor.Realtime.class);
        when(realtime.enabled()).thenReturn(realtimeEnabled);

        var email = mock(AiravataServerProperties.Services.Monitor.Email.class);
        when(email.enabled()).thenReturn(emailEnabled);

        var compute = mock(AiravataServerProperties.Services.Monitor.Compute.class);
        when(compute.enabled()).thenReturn(computeEnabled);

        var monitor = mock(AiravataServerProperties.Services.Monitor.class);
        when(monitor.realtime()).thenReturn(realtime);
        when(monitor.email()).thenReturn(email);
        when(monitor.compute()).thenReturn(compute);

        var research = mock(AiravataServerProperties.Services.Research.class);
        when(research.enabled()).thenReturn(researchEnabled);

        var agent = mock(AiravataServerProperties.Services.Agent.class);
        when(agent.enabled()).thenReturn(agentEnabled);

        var fileserver = mock(AiravataServerProperties.Services.Fileserver.class);
        when(fileserver.enabled()).thenReturn(fileserverEnabled);

        var dbus = mock(AiravataServerProperties.Services.Dbus.class);
        when(dbus.enabled()).thenReturn(false);

        var services = mock(AiravataServerProperties.Services.class);
        when(services.thrift()).thenReturn(thrift);
        when(services.rest()).thenReturn(rest);
        when(services.controller()).thenReturn(controller);
        when(services.participant()).thenReturn(participant);
        when(services.prewm()).thenReturn(prewm);
        when(services.postwm()).thenReturn(postwm);
        when(services.parser()).thenReturn(parser);
        when(services.monitor()).thenReturn(monitor);
        when(services.research()).thenReturn(research);
        when(services.agent()).thenReturn(agent);
        when(services.fileserver()).thenReturn(fileserver);
        when(services.dbus()).thenReturn(dbus);

        var props = mock(AiravataServerProperties.class);
        when(props.services()).thenReturn(services);

        return props;
    }

    @Test
    public void testIsServiceEnabled_ThriftApi() {
        var enabledProps =
                createMockProperties(true, true, true, true, true, true, true, true, true, true, true, true, true);
        var enabledVerifier = new ServiceStatusVerifier(applicationContext, enabledProps);
        assertTrue(enabledVerifier.isServiceEnabled("thrift-api"));

        var disabledProps =
                createMockProperties(false, true, true, true, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);
        assertFalse(disabledVerifier.isServiceEnabled("thrift-api"));
    }

    @Test
    public void testIsServiceEnabled_RestApi() {
        var disabledProps =
                createMockProperties(true, false, true, true, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);
        assertFalse(disabledVerifier.isServiceEnabled("rest-api"));

        var enabledProps =
                createMockProperties(true, true, true, true, true, true, true, true, true, true, true, true, true);
        var enabledVerifier = new ServiceStatusVerifier(applicationContext, enabledProps);
        assertTrue(enabledVerifier.isServiceEnabled("rest-api"));
    }

    @Test
    public void testIsServiceEnabled_Controller() {
        assertTrue(verifier.isServiceEnabled("controller"));

        var disabledProps =
                createMockProperties(true, true, false, true, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);
        assertFalse(disabledVerifier.isServiceEnabled("controller"));
    }

    @Test
    public void testIsServiceEnabled_Participant() {
        assertTrue(verifier.isServiceEnabled("participant"));

        var disabledProps =
                createMockProperties(true, true, true, false, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);
        assertFalse(disabledVerifier.isServiceEnabled("participant"));
    }

    @Test
    public void testIsServiceEnabled_WorkflowManagers() {
        assertTrue(verifier.isServiceEnabled("pre-workflow-manager"));
        assertTrue(verifier.isServiceEnabled("post-workflow-manager"));
        assertTrue(verifier.isServiceEnabled("parser-workflow-manager"));
    }

    @Test
    public void testIsServiceEnabled_Monitors() {
        assertTrue(verifier.isServiceEnabled("realtime-monitor"));
        assertTrue(verifier.isServiceEnabled("email-monitor"));
    }

    @Test
    public void testIsServiceEnabled_UnknownService() {
        assertFalse(verifier.isServiceEnabled("unknown-service"));
    }

    @Test
    public void testIsServiceRunning() {
        assertTrue(verifier.isServiceRunning("thrift-api"));

        var disabledProps =
                createMockProperties(false, true, true, true, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);
        assertFalse(disabledVerifier.isServiceRunning("thrift-api"));
    }

    @Test
    public void testIsPortListening_UnusedPort() {
        assertFalse(verifier.isPortListening(65534));
    }

    @Test
    public void testGetAllServiceNames() {
        var serviceNames = verifier.getAllServiceNames();
        assertNotNull(serviceNames);
        assertTrue(serviceNames.size() > 0);
        assertTrue(serviceNames.contains("thrift-api"));
        assertTrue(serviceNames.contains("rest-api"));
        assertTrue(serviceNames.contains("controller"));
    }

    @Test
    public void testVerifyServicesRunning() {
        var result = verifier.verifyServicesRunning("thrift-api", "rest-api");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessfulServices().size());
    }

    @Test
    public void testVerifyServicesNotRunning() {
        var disabledProps =
                createMockProperties(false, false, true, true, true, true, true, true, true, true, true, true, true);
        var disabledVerifier = new ServiceStatusVerifier(applicationContext, disabledProps);

        var result = disabledVerifier.verifyServicesNotRunning("thrift-api", "rest-api");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessfulServices().size());
    }

    @Test
    public void testVerifyServicesRunning_WithFailures() {
        var mixedProps =
                createMockProperties(true, false, true, true, true, true, true, true, true, true, true, true, true);
        var mixedVerifier = new ServiceStatusVerifier(applicationContext, mixedProps);

        var result = mixedVerifier.verifyServicesRunning("thrift-api", "rest-api");
        assertFalse(result.isSuccess());
        assertEquals(1, result.getSuccessfulServices().size());
        assertEquals(1, result.getFailedServices().size());
        assertTrue(result.getFailedServices().containsKey("rest-api"));
    }
}
