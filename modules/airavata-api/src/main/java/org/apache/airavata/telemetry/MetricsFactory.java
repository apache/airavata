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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Factory for creating Micrometer-based metrics.
 * Provides a global MeterRegistry that can be accessed from static contexts.
 * 
 * Spring Boot auto-configures a CompositeMeterRegistry which is injected here
 * and made available globally for legacy static metric usage.
 */
@Component
public class MetricsFactory {

    private static MeterRegistry globalRegistry = new SimpleMeterRegistry();

    private final MeterRegistry meterRegistry;

    public MetricsFactory(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Set the Spring-managed registry as the global registry
        globalRegistry = meterRegistry;
    }

    /**
     * Get the global MeterRegistry.
     * For static contexts where dependency injection is not available.
     */
    public static MeterRegistry getRegistry() {
        return globalRegistry;
    }

    /**
     * Create a new CounterMetric with the global registry.
     */
    public static CounterMetric createCounter(String name) {
        return new CounterMetric(name, globalRegistry);
    }

    /**
     * Create a new CounterMetric with labels.
     */
    public static CounterMetric createCounter(String name, String... labelNames) {
        return new CounterMetric(name, globalRegistry, labelNames);
    }

    /**
     * Create a new GaugeMetric with the global registry.
     */
    public static GaugeMetric createGauge(String name) {
        return new GaugeMetric(name, globalRegistry);
    }
}
