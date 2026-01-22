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
package org.apache.airavata.task.base;

import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskParam;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.airavata.telemetry.GaugeMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Airavata tasks.
 *
 * <p>Provides common functionality for task execution including:
 * - Task lifecycle management (init, run, cancel)
 * - State management via UserContentStore (backed by Dapr State Store)
 * - Retry logic
 *
 * <p>Tasks extend this class and implement onRun() and onCancel() methods.
 * The task execution is managed by Dapr workflows and activities.
 * Task orchestration is handled by Dapr workflows, not by task chaining.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public abstract class AbstractTask extends org.apache.airavata.task.base.UserContentStore
        implements org.apache.airavata.task.base.Task {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    private static final CounterMetric taskInitCounter = new CounterMetric("task_init_count");
    private static final GaugeMetric taskRunGauge = new GaugeMetric("task_run_gauge");
    private static final CounterMetric taskCancelCounter = new CounterMetric("task_cancel_count");
    private static final CounterMetric taskFailCounter = new CounterMetric("task_fail_count");
    private static final CounterMetric taskCompleteCounter = new CounterMetric("task_complete_count");

    @TaskParam(name = "taskId")
    private String taskId;

    private TaskHelper taskHelper;
    // TaskUtil kept for compatibility with subclasses, but no longer used for deserialization
    // in Dapr-based architecture
    @SuppressWarnings("unused")
    private final TaskUtil taskUtil;

    @TaskParam(name = "Retry Count")
    private int retryCount = 3;

    public AbstractTask(TaskUtil taskUtil) {
        this.taskUtil = taskUtil;
    }

    @Override
    public void init(String workflowName, String jobName, String taskName) {
        this.taskId = taskName;
        taskInitCounter.inc();
        // Task initialization - in Dapr workflows, tasks are executed via activities
        // and don't use callbackContext for parameter deserialization
    }

    @Override
    public final TaskResult run() {
        try {
            taskRunGauge.inc();
            // In Dapr workflows, tasks are executed directly via activities
            // No need for Helix-specific job sequencing logic
            return onRun(this.taskHelper);
        } catch (Exception e) {
            logger.error("Task execution failed for task {}", taskId, e);
            return onFail("Task execution failed: " + e.getMessage(), false);
        }
    }

    @Override
    public final void cancel() {
        try {
            taskRunGauge.dec();
            taskCancelCounter.inc();
            logger.info("Cancelling task {}", taskId);
            onCancel();
        } catch (Exception e) {
            logger.error("Error cancelling task {}", taskId, e);
        }
    }

    public abstract TaskResult onRun(TaskHelper helper);

    public abstract void onCancel();

    protected TaskResult onSuccess(String message) {
        taskRunGauge.dec();
        taskCompleteCounter.inc();
        String successMessage =
                "Task " + getTaskId() + " completed." + (message != null ? " Message : " + message : "");
        logger.info(successMessage);
        // In Dapr workflows, task orchestration is handled by workflows, not task chaining
        return new TaskResult(TaskResult.Status.COMPLETED, message);
    }

    protected TaskResult onFail(String reason, boolean fatal) {
        taskRunGauge.dec();
        taskFailCounter.inc();
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, reason);
    }

    protected void publishErrors(Throwable e) {
        // Task errors are logged; status updates are published via Dapr messaging
        logger.error("Task error", e);
    }

    protected void setContextVariable(String key, String value) {
        putUserContent(key, value, Scope.WORKFLOW);
    }

    protected String getContextVariable(String key) {
        return getUserContent(key, Scope.WORKFLOW);
    }

    // Getters and setters

    public String getTaskId() {
        return taskId;
    }

    public AbstractTask setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public TaskHelper getTaskHelper() {
        return taskHelper;
    }

    public AbstractTask setTaskHelper(TaskHelper taskHelper) {
        this.taskHelper = taskHelper;
        return this;
    }

    /** Retry count: was in ZK via MonitoringUtil; now fixed. Use Dapr Activity retry when on Workflows. */
    protected int getCurrentRetryCount() {
        return 1;
    }

    /** No-op: was in ZK via MonitoringUtil. Use Dapr Activity retry when on Workflows. */
    protected void markNewRetry(int currentRetryCount) {}

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        // set the default retry count to 1
        this.retryCount = retryCount <= 0 ? 1 : retryCount;
    }

    @Override
    protected String getContextKey() {
        // Use taskId as context key for Dapr State Store
        // In Dapr workflows, tasks are executed independently via activities
        return taskId != null ? taskId : "default";
    }
}
