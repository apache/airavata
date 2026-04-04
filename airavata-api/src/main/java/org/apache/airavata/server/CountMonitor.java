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

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;

public class CountMonitor {

    private final String name;
    private final String[] labelNames;

    public CountMonitor(String monitorName) {
        this.name = monitorName;
        this.labelNames = new String[0];
        // pre-register a no-label counter
        Metrics.counter(monitorName);
    }

    public CountMonitor(String monitorName, String... labelNames) {
        this.name = monitorName;
        this.labelNames = labelNames;
    }

    public void inc() {
        Metrics.counter(name).increment();
    }

    public void inc(String... labelValues) {
        Metrics.counter(name, buildTags(labelValues)).increment();
    }

    public void inc(double amount) {
        Metrics.counter(name).increment(amount);
    }

    public void inc(double amount, String... labelValues) {
        Metrics.counter(name, buildTags(labelValues)).increment(amount);
    }

    private Iterable<Tag> buildTags(String... labelValues) {
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < labelNames.length && i < labelValues.length; i++) {
            tags.add(Tag.of(labelNames[i], labelValues[i]));
        }
        return tags;
    }
}
