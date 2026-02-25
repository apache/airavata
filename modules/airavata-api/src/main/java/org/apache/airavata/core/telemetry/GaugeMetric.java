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
package org.apache.airavata.core.telemetry;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gauge metric wrapper using Micrometer.
 * Spring Boot Actuator auto-configures the MeterRegistry.
 */
public class GaugeMetric {

    private final AtomicLong value = new AtomicLong(0);

    /**
     * Legacy constructor for static field initialization.
     * Uses the global MeterRegistry from MetricsFactory.
     */
    public GaugeMetric(String monitorName) {
        this(monitorName, MetricsFactory.getRegistry());
    }

    /**
     * Constructor with explicit MeterRegistry.
     */
    public GaugeMetric(String monitorName, MeterRegistry meterRegistry) {
        Gauge.builder(monitorName, value, AtomicLong::get)
                .description(monitorName)
                .register(meterRegistry);
    }

    public void inc() {
        value.addAndGet(1);
    }

    public void inc(long amount) {
        value.addAndGet(amount);
    }

    public void dec() {
        value.addAndGet(-1);
    }

    public void dec(long amount) {
        value.addAndGet(-amount);
    }

    public void set(long newValue) {
        value.set(newValue);
    }

    public double get() {
        return value.get();
    }
}
