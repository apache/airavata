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
package org.apache.airavata.execution.dag.interceptor;

import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.core.telemetry.GaugeMetric;
import org.apache.airavata.execution.dag.DagTaskResult;
import org.apache.airavata.execution.dag.TaskInterceptor;
import org.apache.airavata.execution.dag.TaskNode;
import org.apache.airavata.execution.task.TaskContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Tracks task execution metrics: active count, completion count, and failure count.
 */
@Component
@Order(2)
public class MetricsInterceptor implements TaskInterceptor {

    private static final GaugeMetric activeTaskGauge = new GaugeMetric("dag_task_active");
    private static final CounterMetric completedCounter = new CounterMetric("dag_task_completed");
    private static final CounterMetric failedCounter = new CounterMetric("dag_task_failed");

    @Override
    public void before(TaskContext context, TaskNode node) {
        activeTaskGauge.inc();
    }

    @Override
    public void afterSuccess(TaskContext context, TaskNode node, DagTaskResult.Success result) {
        activeTaskGauge.dec();
        completedCounter.inc();
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        activeTaskGauge.dec();
        failedCounter.inc();
    }
}
