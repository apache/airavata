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
package org.apache.airavata.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Telemetry service using Spring Boot Actuator and Micrometer.
 * 
 * Prometheus metrics are exposed via Spring Boot Actuator at /actuator/prometheus
 * No manual HTTP server is needed - Actuator handles this automatically.
 * 
 * Configure via application.properties:
 *   services.telemetry.enabled=true
 *   management.endpoints.web.exposure.include=health,info,prometheus
 *   management.endpoint.prometheus.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "airavata.services.telemetry", name = "enabled", havingValue = "true")
public class TelemetryServer extends ServerLifecycle {

    private final AiravataServerProperties properties;
    private final MeterRegistry meterRegistry;

    public TelemetryServer(AiravataServerProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public String getServerName() {
        return "Micrometer Metrics Service";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        // Start early so metrics are available when other services start
        return 5;
    }

    @Override
    protected void doStart() throws Exception {
        // Actuator automatically exposes /actuator/prometheus endpoint
        // No manual HTTP server needed
        logger.info("Micrometer metrics service started. Metrics available at /actuator/prometheus");
        logger.info("MeterRegistry type: {}", meterRegistry.getClass().getSimpleName());
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("Micrometer metrics service stopped");
    }

    /**
     * Get the MeterRegistry for creating custom metrics.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
