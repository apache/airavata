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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Counter metric wrapper using Micrometer.
 * Spring Boot Actuator auto-configures the MeterRegistry.
 */
public class CounterMetric {

    private final Counter counter;
    private final MeterRegistry meterRegistry;
    private final String monitorName;
    private final String[] labelNames;

    /**
     * Legacy constructor for static field initialization.
     * Uses the global MeterRegistry from MetricsFactory.
     */
    public CounterMetric(String monitorName) {
        this(monitorName, MetricsFactory.getRegistry());
    }

    /**
     * Legacy constructor with labels for static field initialization.
     * Uses the global MeterRegistry from MetricsFactory.
     */
    public CounterMetric(String monitorName, String... labelNames) {
        this(monitorName, MetricsFactory.getRegistry(), labelNames);
    }

    /**
     * Constructor with explicit MeterRegistry.
     */
    public CounterMetric(String monitorName, MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.monitorName = monitorName;
        this.labelNames = new String[0];
        this.counter = Counter.builder(monitorName).description(monitorName).register(meterRegistry);
    }

    /**
     * Constructor with explicit MeterRegistry and labels.
     */
    public CounterMetric(String monitorName, MeterRegistry meterRegistry, String... labelNames) {
        this.meterRegistry = meterRegistry;
        this.monitorName = monitorName;
        this.labelNames = labelNames;
        // For labeled counters, we don't pre-register - we create on demand
        this.counter = null;
    }

    public void inc() {
        if (counter != null) {
            counter.increment();
        }
    }

    public void inc(String... labelValues) {
        if (labelNames.length > 0 && labelValues.length == labelNames.length) {
            Tags tags = Tags.empty();
            for (int i = 0; i < labelNames.length; i++) {
                tags = tags.and(labelNames[i], labelValues[i]);
            }
            Counter.builder(monitorName)
                    .description(monitorName)
                    .tags(tags)
                    .register(meterRegistry)
                    .increment();
        } else if (counter != null) {
            counter.increment();
        }
    }

    public void inc(double amount) {
        if (counter != null) {
            counter.increment(amount);
        }
    }

    public void inc(double amount, String... labelValues) {
        if (labelNames.length > 0 && labelValues.length == labelNames.length) {
            Tags tags = Tags.empty();
            for (int i = 0; i < labelNames.length; i++) {
                tags = tags.and(labelNames[i], labelValues[i]);
            }
            Counter.builder(monitorName)
                    .description(monitorName)
                    .tags(tags)
                    .register(meterRegistry)
                    .increment(amount);
        } else if (counter != null) {
            counter.increment(amount);
        }
    }
}
