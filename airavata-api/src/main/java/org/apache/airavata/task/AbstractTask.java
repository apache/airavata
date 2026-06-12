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
package org.apache.airavata.task;

import org.apache.airavata.server.CountMonitor;
import org.apache.airavata.server.GaugeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for the DB-transactional task SPI. The {@link org.apache.airavata.orchestration} executor
 * resolves the concrete task, populates its {@code @TaskParam} fields via {@link TaskUtil}, and
 * invokes {@link #onRun(TaskHelper)}; task sequencing and retries are driven off the DB
 * ({@code PROCESS.TASK_DAG}, {@code EXEC_STATUS}), not an external workflow engine.
 */
public abstract class AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    private static final GaugeMonitor taskRunGauge = new GaugeMonitor("task_run_gauge");
    private static final CountMonitor taskFailCounter = new CountMonitor("task_fail_count");
    private static final CountMonitor taskCompleteCounter = new CountMonitor("task_complete_count");

    @TaskParam(name = "taskId")
    private String taskId;

    private TaskHelper taskHelper;

    @TaskParam(name = "Retry Count")
    private int retryCount = 3;

    public abstract DbTaskResult onRun(TaskHelper helper);

    public abstract void onCancel();

    protected DbTaskResult onSuccess(String message) {
        taskRunGauge.dec();
        taskCompleteCounter.inc();
        String successMessage =
                "Task " + getTaskId() + " completed." + (message != null ? " Message : " + message : "");
        logger.info(successMessage);
        return DbTaskResult.completed(message);
    }

    protected DbTaskResult onFail(String reason, boolean fatal) {
        taskRunGauge.dec();
        taskFailCounter.inc();
        return fatal ? DbTaskResult.fatal(reason) : DbTaskResult.failed(reason);
    }

    protected void publishErrors(Throwable e) {
        logger.error("Task {} failed", getTaskId(), e);
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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        // set the default retry count to 1
        this.retryCount = retryCount <= 0 ? 1 : retryCount;
    }
}
