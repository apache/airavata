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
 *     .enableThriftApi()
 *     .disableRestApi()
 *     .enableHelixController()
 *     .enableHelixParticipant()
 *     .disableAllMonitors();
 *
 * Map&lt;String, String&gt; properties = builder.build();
 * </pre>
 */
public class ServiceConfigurationBuilder {

    private boolean thriftApi = true; // Default enabled
    private boolean restApi = false; // Default disabled
    private boolean helixController = true;
    private boolean helixParticipant = true;
    private boolean preWorkflowManager = true;
    private boolean postWorkflowManager = true;
    private boolean parserWorkflowManager = false;
    private boolean realtimeMonitor = true;
    private boolean emailMonitor = true;
    private boolean researchService = true;
    private boolean agentService = true;
    private boolean fileService = true;
    private boolean dbeventService = true;
    private boolean telemetryService = true;

    // Port configuration for Thrift and REST API services
    private int thriftPort = 8930;
    private int restPort = 8082;
    // Note: All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on
    // services.thrift.server.port

    /**
     * Enable Thrift API service.
     */
    public ServiceConfigurationBuilder enableThriftApi() {
        this.thriftApi = true;
        return this;
    }

    /**
     * Disable Thrift API service.
     */
    public ServiceConfigurationBuilder disableThriftApi() {
        this.thriftApi = false;
        return this;
    }

    /**
     * Enable REST API service.
     */
    public ServiceConfigurationBuilder enableRestApi() {
        this.restApi = true;
        return this;
    }

    /**
     * Disable REST API service.
     */
    public ServiceConfigurationBuilder disableRestApi() {
        this.restApi = false;
        return this;
    }

    /**
     * Enable Helix Controller.
     */
    public ServiceConfigurationBuilder enableHelixController() {
        this.helixController = true;
        return this;
    }

    /**
     * Disable Helix Controller.
     */
    public ServiceConfigurationBuilder disableHelixController() {
        this.helixController = false;
        return this;
    }

    /**
     * Enable Helix Participant.
     */
    public ServiceConfigurationBuilder enableHelixParticipant() {
        this.helixParticipant = true;
        return this;
    }

    /**
     * Disable Helix Participant.
     */
    public ServiceConfigurationBuilder disableHelixParticipant() {
        this.helixParticipant = false;
        return this;
    }

    /**
     * Enable Pre Workflow Manager.
     */
    public ServiceConfigurationBuilder enablePreWorkflowManager() {
        this.preWorkflowManager = true;
        return this;
    }

    /**
     * Disable Pre Workflow Manager.
     */
    public ServiceConfigurationBuilder disablePreWorkflowManager() {
        this.preWorkflowManager = false;
        return this;
    }

    /**
     * Enable Post Workflow Manager.
     */
    public ServiceConfigurationBuilder enablePostWorkflowManager() {
        this.postWorkflowManager = true;
        return this;
    }

    /**
     * Disable Post Workflow Manager.
     */
    public ServiceConfigurationBuilder disablePostWorkflowManager() {
        this.postWorkflowManager = false;
        return this;
    }

    /**
     * Enable Parser Workflow Manager.
     */
    public ServiceConfigurationBuilder enableParserWorkflowManager() {
        this.parserWorkflowManager = true;
        return this;
    }

    /**
     * Disable Parser Workflow Manager.
     */
    public ServiceConfigurationBuilder disableParserWorkflowManager() {
        this.parserWorkflowManager = false;
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
        this.helixController = true;
        this.helixParticipant = true;
        this.preWorkflowManager = true;
        this.postWorkflowManager = true;
        this.parserWorkflowManager = true;
        this.realtimeMonitor = true;
        this.emailMonitor = true;
        return this;
    }

    /**
     * Disable all background services.
     */
    public ServiceConfigurationBuilder disableAllBackgroundServices() {
        this.helixController = false;
        this.helixParticipant = false;
        this.preWorkflowManager = false;
        this.postWorkflowManager = false;
        this.parserWorkflowManager = false;
        this.realtimeMonitor = false;
        this.emailMonitor = false;
        return this;
    }

    /**
     * Set minimal configuration (only core services, no background services).
     */
    public ServiceConfigurationBuilder minimalConfiguration() {
        this.thriftApi = false;
        this.restApi = false;
        this.helixController = false;
        this.helixParticipant = false;
        this.preWorkflowManager = false;
        this.postWorkflowManager = false;
        this.parserWorkflowManager = false;
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
        this.thriftApi = true;
        this.restApi = true;
        this.helixController = true;
        this.helixParticipant = true;
        this.preWorkflowManager = true;
        this.postWorkflowManager = true;
        this.parserWorkflowManager = true;
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
     * Set Thrift API service port.
     */
    public ServiceConfigurationBuilder withThriftPort(int port) {
        this.thriftPort = port;
        return this;
    }

    /**
     * Set REST API service port.
     */
    public ServiceConfigurationBuilder withRestPort(int port) {
        this.restPort = port;
        return this;
    }

    // Note: All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on
    // services.thrift.server.port
    // Individual port setters removed - use withThriftPort() instead

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
     * Build a map of Spring Boot test properties from the current configuration.
     *
     * @return Map of property keys to values suitable for @SpringBootTest properties
     */
    public Map<String, String> build() {
        Map<String, String> props = new HashMap<>();
        props.put("airavata.services.thrift.enabled", String.valueOf(thriftApi));
        props.put("airavata.services.rest.enabled", String.valueOf(restApi));
        props.put("airavata.services.controller.enabled", String.valueOf(helixController));
        props.put("airavata.services.participant.enabled", String.valueOf(helixParticipant));
        props.put("airavata.services.prewm.enabled", String.valueOf(preWorkflowManager));
        props.put("airavata.services.postwm.enabled", String.valueOf(postWorkflowManager));
        props.put("airavata.services.parser.enabled", String.valueOf(parserWorkflowManager));
        props.put("airavata.services.monitor.realtime.enabled", String.valueOf(realtimeMonitor));
        props.put("airavata.services.monitor.email.enabled", String.valueOf(emailMonitor));
        props.put("airavata.services.research.enabled", String.valueOf(researchService));
        props.put("airavata.services.agent.enabled", String.valueOf(agentService));
        props.put("airavata.services.fileserver.enabled", String.valueOf(fileService));
        props.put("airavata.services.dbus.enabled", String.valueOf(dbeventService));
        props.put("airavata.services.telemetry.enabled", String.valueOf(telemetryService));

        // Thrift and REST API ports
        // Note: All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on
        // airavata.services.thrift.server.port
        props.put("airavata.services.thrift.server.port", String.valueOf(thriftPort));
        props.put("airavata.services.rest.server.port", String.valueOf(restPort));

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
        props.setProperty("airavata.services.thrift.enabled", String.valueOf(thriftApi));
        props.setProperty("airavata.services.rest.enabled", String.valueOf(restApi));
        props.setProperty("airavata.services.controller.enabled", String.valueOf(helixController));
        props.setProperty("airavata.services.participant.enabled", String.valueOf(helixParticipant));
        props.setProperty("airavata.services.prewm.enabled", String.valueOf(preWorkflowManager));
        props.setProperty("airavata.services.postwm.enabled", String.valueOf(postWorkflowManager));
        props.setProperty("airavata.services.parser.enabled", String.valueOf(parserWorkflowManager));
        props.setProperty("airavata.services.monitor.realtime.enabled", String.valueOf(realtimeMonitor));
        props.setProperty("airavata.services.monitor.email.enabled", String.valueOf(emailMonitor));
        props.setProperty("airavata.services.research.enabled", String.valueOf(researchService));
        props.setProperty("airavata.services.agent.enabled", String.valueOf(agentService));
        props.setProperty("airavata.services.fileserver.enabled", String.valueOf(fileService));
        props.setProperty("airavata.services.dbus.enabled", String.valueOf(dbeventService));
        props.setProperty("airavata.services.telemetry.enabled", String.valueOf(telemetryService));

        // Thrift and REST API ports
        // Note: All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing) are multiplexed on
        // airavata.services.thrift.server.port
        props.setProperty("airavata.services.thrift.server.port", String.valueOf(thriftPort));
        props.setProperty("airavata.services.rest.server.port", String.valueOf(restPort));

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
