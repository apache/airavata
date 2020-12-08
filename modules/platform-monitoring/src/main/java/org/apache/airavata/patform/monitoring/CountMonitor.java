/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 package org.apache.airavata.patform.monitoring;

import io.prometheus.client.Counter;

public class CountMonitor {

    private Counter counter;

    public CountMonitor(String monitorName) {
        counter = Counter.build().name(monitorName).help(monitorName).register();
    }

    public CountMonitor(String monitorName, String... labelNames) {
        counter = Counter.build().name(monitorName).help(monitorName).labelNames(labelNames).register();
    }

    public void inc() {
        counter.inc();
    }

    public void inc(String... labelValues) {
        counter.labels(labelValues).inc();
    }

    public void inc(double amount) {
        counter.inc(amount);
    }

    public void inc(double amount, String... labelValues) {
        counter.labels(labelValues).inc(amount);
    }
}
