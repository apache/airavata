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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Builder pattern for creating test configurations programmatically.
 *
 * <p>This class helps create service configurations for testing by allowing
 * services to be enabled/disabled programmatically and generating properties
 * that can be used in Spring Boot tests.
 *
 * <p>Example usage:
 * <pre>
 * ServiceConfigurationBuilder builder = new ServiceConfigurationBuilder()
 *     .enableController()
 *     .enableParticipant()
 *     .disableAllMonitors();
 *
 * Map&lt;String, String&gt; properties = builder.build();
 * </pre>
 */
public class ServiceConfigurationBuilder {

    private boolean controller = true;
    private boolean participant = true;
    private boolean realtimeMonitor = true;
    private boolean emailMonitor = true;
    private boolean researchService = true;
    private boolean agentService = true;
    private boolean fileService = true;
    private boolean dbeventService = true;
    private boolean telemetryService = true;
    private boolean restApi = true;

    // Port configuration for HTTP services
    private int httpPort = 8080; // Unified HTTP server port

    /**
     * Enable Controller.
     */
    public ServiceConfigurationBuilder enableController() {
        this.controller = true;
        return this;
    }

    /**
     * Disable Controller.
     */
    public ServiceConfigurationBuilder disableController() {
        this.controller = false;
        return this;
    }

    /**
     * Enable Participant.
     */
    public ServiceConfigurationBuilder enableParticipant() {
        this.participant = true;
        return this;
    }

    /**
     * Disable Participant.
     */
    public ServiceConfigurationBuilder disableParticipant() {
        this.participant = false;
        return this;
    }

    /**
     * Enable Realtime Monitor.
     */
    public ServiceConfigurationBuilder enableRealtimeMonitor() {
        this.realtimeMonitor = true;
        return this;
    }

    /**
     * Disable Realtime Monitor.
     */
    public ServiceConfigurationBuilder disableRealtimeMonitor() {
        this.realtimeMonitor = false;
        return this;
    }

    /**
     * Enable Email Monitor.
     */
    public ServiceConfigurationBuilder enableEmailMonitor() {
        this.emailMonitor = true;
        return this;
    }

    /**
     * Disable Email Monitor.
     */
    public ServiceConfigurationBuilder disableEmailMonitor() {
        this.emailMonitor = false;
        return this;
    }

    /**
     * Disable all monitors (Realtime and Email).
     */
    public ServiceConfigurationBuilder disableAllMonitors() {
        this.realtimeMonitor = false;
        this.emailMonitor = false;
        return this;
    }

    /**
     * Enable all background services.
     */
    public ServiceConfigurationBuilder enableAllBackgroundServices() {
        this.controller = true;
        this.participant = true;
        this.realtimeMonitor = true;
        this.emailMonitor = true;
        return this;
    }

    /**
     * Disable all background services.
     */
    public ServiceConfigurationBuilder disableAllBackgroundServices() {
        this.controller = false;
        this.participant = false;
        this.realtimeMonitor = false;
        this.emailMonitor = false;
        return this;
    }

    /**
     * Set minimal configuration (only core services, no background services).
     */
    public ServiceConfigurationBuilder minimalConfiguration() {
        this.restApi = false;
        this.controller = false;
        this.participant = false;
        this.realtimeMonitor = false;
        this.emailMonitor = false;
        this.researchService = false;
        this.agentService = false;
        this.fileService = false;
        this.dbeventService = false;
        this.telemetryService = false;
        return this;
    }

    /**
     * Set all services enabled configuration.
     */
    public ServiceConfigurationBuilder allServicesEnabled() {
        this.restApi = true;
        this.controller = true;
        this.participant = true;
        this.realtimeMonitor = true;
        this.emailMonitor = true;
        this.researchService = true;
        this.agentService = true;
        this.fileService = true;
        this.dbeventService = true;
        this.telemetryService = true;
        return this;
    }

    /**
     * Set Airavata HTTP server port.
     */
    public ServiceConfigurationBuilder withRestPort(int port) {
        this.httpPort = port;
        return this;
    }

    /**
     * Enable Research Service.
     */
    public ServiceConfigurationBuilder enableResearchService() {
        this.researchService = true;
        return this;
    }

    /**
     * Disable Research Service.
     */
    public ServiceConfigurationBuilder disableResearchService() {
        this.researchService = false;
        return this;
    }

    /**
     * Enable Agent Service.
     */
    public ServiceConfigurationBuilder enableAgentService() {
        this.agentService = true;
        return this;
    }

    /**
     * Disable Agent Service.
     */
    public ServiceConfigurationBuilder disableAgentService() {
        this.agentService = false;
        return this;
    }

    /**
     * Enable File Service.
     */
    public ServiceConfigurationBuilder enableFileService() {
        this.fileService = true;
        return this;
    }

    /**
     * Disable File Service.
     */
    public ServiceConfigurationBuilder disableFileService() {
        this.fileService = false;
        return this;
    }

    /**
     * Enable DB Event Service.
     */
    public ServiceConfigurationBuilder enableDbEventService() {
        this.dbeventService = true;
        return this;
    }

    /**
     * Disable DB Event Service.
     */
    public ServiceConfigurationBuilder disableDbEventService() {
        this.dbeventService = false;
        return this;
    }

    /**
     * Enable Telemetry Service (Prometheus monitoring).
     */
    public ServiceConfigurationBuilder enableTelemetryService() {
        this.telemetryService = true;
        return this;
    }

    /**
     * Disable Telemetry Service.
     */
    public ServiceConfigurationBuilder disableTelemetryService() {
        this.telemetryService = false;
        return this;
    }
    /**
     * Enable REST API.
     */
    public ServiceConfigurationBuilder enableRestApi() {
        this.restApi = true;
        return this;
    }

    /**
     * Disable REST API.
     */
    public ServiceConfigurationBuilder disableRestApi() {
        this.restApi = false;
        return this;
    }

    /**
     * Build a map of Spring Boot test properties from the current configuration.
     *
     * @return Map of property keys to values suitable for @SpringBootTest properties
     */
    public Map<String, String> build() {
        Map<String, String> props = new HashMap<>();
        props.put("airavata.services.controller.enabled", String.valueOf(controller));
        props.put("airavata.services.participant.enabled", String.valueOf(participant));
        props.put("airavata.services.monitor.realtime.enabled", String.valueOf(realtimeMonitor));
        props.put("airavata.services.monitor.email.enabled", String.valueOf(emailMonitor));
        props.put("airavata.services.research.enabled", String.valueOf(researchService));
        props.put("airavata.services.agent.enabled", String.valueOf(agentService));
        props.put("airavata.services.fileserver.enabled", String.valueOf(fileService));
        props.put("airavata.services.dbus.enabled", String.valueOf(dbeventService));
        props.put("airavata.services.telemetry.enabled", String.valueOf(telemetryService));
        props.put("airavata.services.restapi.enabled", String.valueOf(restApi));

        // HTTP and gRPC ports
        props.put("airavata.services.http.server.port", String.valueOf(httpPort));
        props.put("airavata.services.grpc.server.port", "9090");

        return props;
    }

    /**
     * Build a Properties object from the current configuration.
     * Useful for writing to property files.
     *
     * @return Properties object with all service configurations
     */
    public Properties buildProperties() {
        Properties props = new Properties();
        props.setProperty("airavata.services.controller.enabled", String.valueOf(controller));
        props.setProperty("airavata.services.participant.enabled", String.valueOf(participant));
        props.setProperty("airavata.services.monitor.realtime.enabled", String.valueOf(realtimeMonitor));
        props.setProperty("airavata.services.monitor.email.enabled", String.valueOf(emailMonitor));
        props.setProperty("airavata.services.research.enabled", String.valueOf(researchService));
        props.setProperty("airavata.services.agent.enabled", String.valueOf(agentService));
        props.setProperty("airavata.services.fileserver.enabled", String.valueOf(fileService));
        props.setProperty("airavata.services.dbus.enabled", String.valueOf(dbeventService));
        props.setProperty("airavata.services.telemetry.enabled", String.valueOf(telemetryService));

        props.setProperty("airavata.services.http.server.port", String.valueOf(httpPort));
        props.setProperty("airavata.services.grpc.server.port", "9090");

        return props;
    }

    /**
     * Create a builder with default configuration (matching airavata.properties defaults).
     */
    public static ServiceConfigurationBuilder defaults() {
        return new ServiceConfigurationBuilder();
    }

    /**
     * Create a builder with minimal configuration (only core services).
     */
    public static ServiceConfigurationBuilder minimal() {
        return new ServiceConfigurationBuilder().minimalConfiguration();
    }

    /**
     * Create a builder with all services enabled.
     */
    public static ServiceConfigurationBuilder allEnabled() {
        return new ServiceConfigurationBuilder().allServicesEnabled();
    }
}
