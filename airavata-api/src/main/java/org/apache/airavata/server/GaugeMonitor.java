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
package org.apache.airavata.server;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.concurrent.atomic.AtomicLong;

public class GaugeMonitor {

    private final AtomicLong value = new AtomicLong(0);

    public GaugeMonitor(String monitorName) {
        Gauge.builder(monitorName, value, AtomicLong::doubleValue).register(Metrics.globalRegistry);
    }

    public void inc() {
        value.incrementAndGet();
    }

    public void inc(double amount) {
        value.addAndGet((long) amount);
    }

    public void dec() {
        value.decrementAndGet();
    }

    public void dec(double amount) {
        value.addAndGet(-(long) amount);
    }
}
