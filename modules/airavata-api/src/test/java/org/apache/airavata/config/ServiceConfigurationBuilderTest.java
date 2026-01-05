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
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ServiceConfigurationBuilder.
 * These tests verify the builder logic without requiring Spring context.
 */
public class ServiceConfigurationBuilderTest {

    @Test
    public void testDefaultConfiguration() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.defaults();
        Map<String, String> props = builder.build();

        assertEquals("true", props.get("services.thrift.enabled"));
        assertEquals("false", props.get("services.rest.enabled"));
        assertEquals("true", props.get("services.controller.enabled"));
        assertEquals("true", props.get("services.participant.enabled"));
    }

    @Test
    public void testMinimalConfiguration() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.minimal();
        Map<String, String> props = builder.build();

        assertEquals("false", props.get("services.thrift.enabled"));
        assertEquals("false", props.get("services.rest.enabled"));
        assertEquals("false", props.get("services.controller.enabled"));
        assertEquals("false", props.get("services.participant.enabled"));
    }

    @Test
    public void testAllEnabledConfiguration() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.allEnabled();
        Map<String, String> props = builder.build();

        assertEquals("true", props.get("services.thrift.enabled"));
        assertEquals("true", props.get("services.rest.enabled"));
        assertEquals("true", props.get("services.controller.enabled"));
        assertEquals("true", props.get("services.participant.enabled"));
        assertEquals("true", props.get("services.prewm.enabled"));
        assertEquals("true", props.get("services.postwm.enabled"));
        assertEquals("true", props.get("services.parser.enabled"));
        assertEquals("true", props.get("services.monitor.realtime.enabled"));
        assertEquals("true", props.get("services.monitor.email.enabled"));
    }

    @Test
    public void testEnableThriftApi() {
        ServiceConfigurationBuilder builder =
                new ServiceConfigurationBuilder().disableThriftApi().enableThriftApi();

        Map<String, String> props = builder.build();
        assertEquals("true", props.get("services.thrift.enabled"));
    }

    @Test
    public void testDisableThriftApi() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder().disableThriftApi();

        Map<String, String> props = builder.build();
        assertEquals("false", props.get("services.thrift.enabled"));
    }

    @Test
    public void testEnableRestApi() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder().enableRestApi();

        Map<String, String> props = builder.build();
        assertEquals("true", props.get("services.rest.enabled"));
    }

    @Test
    public void testDisableAllMonitors() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder().disableAllMonitors();

        Map<String, String> props = builder.build();
        assertEquals("false", props.get("services.monitor.realtime.enabled"));
        assertEquals("false", props.get("services.monitor.email.enabled"));
    }

    @Test
    public void testDisableAllBackgroundServices() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder().disableAllBackgroundServices();

        Map<String, String> props = builder.build();
        assertEquals("false", props.get("services.controller.enabled"));
        assertEquals("false", props.get("services.participant.enabled"));
        assertEquals("false", props.get("services.prewm.enabled"));
        assertEquals("false", props.get("services.postwm.enabled"));
        assertEquals("false", props.get("services.parser.enabled"));
    }

    @Test
    public void testBuildProperties() {
        ServiceConfigurationBuilder builder = ServiceConfigurationBuilder.defaults();
        Properties props = builder.buildProperties();

        assertNotNull(props);
        assertEquals("true", props.getProperty("services.thrift.enabled"));
        assertEquals("false", props.getProperty("services.rest.enabled"));
    }

    @Test
    public void testChaining() {
        ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder()
                .enableThriftApi()
                .disableRestApi()
                .enableHelixController()
                .disableHelixParticipant()
                .enablePreWorkflowManager()
                .disablePostWorkflowManager();

        Map<String, String> props = builder.build();
        assertEquals("true", props.get("services.thrift.enabled"));
        assertEquals("false", props.get("services.rest.enabled"));
        assertEquals("true", props.get("services.controller.enabled"));
        assertEquals("false", props.get("services.participant.enabled"));
        assertEquals("true", props.get("services.prewm.enabled"));
        assertEquals("false", props.get("services.postwm.enabled"));
    }
}
