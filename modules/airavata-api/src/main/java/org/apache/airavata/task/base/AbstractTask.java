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

import org.apache.airavata.dapr.participant.DaprParticipant;
import org.apache.airavata.task.OutPort;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskOutPort;
import org.apache.airavata.task.TaskParam;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.airavata.telemetry.GaugeMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Class level comments please
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

    private static final String NEXT_JOB = "next-job";
    private static final String WORKFLOW_STARTED = "workflow-started";

    @TaskParam(name = "taskId")
    private String taskId;

    @TaskOutPort(name = "Next Task")
    private OutPort nextTask;

    private TaskCallbackContext callbackContext;
    private TaskHelper taskHelper;
    private DaprParticipant<? extends AbstractTask> participant;
    private final TaskUtil taskUtil;

    @TaskParam(name = "Retry Count")
    private int retryCount = 3;

    public AbstractTask(TaskUtil taskUtil) {
        this.taskUtil = taskUtil;
    }

    @Override
    public void init(String workflowName, String jobName, String taskName) {
        this.taskId = taskName;
        try {
            taskInitCounter.inc();
            if (callbackContext != null && callbackContext.getTaskConfig() != null) {
                taskUtil.deserializeTaskData(
                        this, this.callbackContext.getTaskConfig().getConfigMap());
            }
        } catch (Exception e) {
            taskFailCounter.inc();
            logger.error("Deserialization of task parameters failed", e);
        }
        if (participant != null) {
            participant.registerRunningTask(this);
        } else {
            logger.warn("Task with id: " + taskId + " is not registered since the participant is not set");
        }
    }

    @Override
    public final TaskResult run() {
        try {
            taskRunGauge.inc();
            boolean isThisNextJob = getUserContent(WORKFLOW_STARTED, Scope.WORKFLOW) == null
                    || this.callbackContext
                            .getJobConfig()
                            .getJobId()
                            .equals(this.callbackContext.getJobConfig().getWorkflow() + "_"
                                    + getUserContent(NEXT_JOB, Scope.WORKFLOW));

            return isThisNextJob
                    ? onRun(this.taskHelper)
                    : new TaskResult(TaskResult.Status.COMPLETED, "Not a target job");
        } finally {
            if (participant != null) {
                participant.unregisterRunningTask(this);
            } else {
                logger.warn("Task with id: " + taskId + " is not unregistered since the participant is not set");
            }
        }
    }

    @Override
    public final void cancel() {
        try {
            taskRunGauge.dec();
            taskCancelCounter.inc();
            logger.info("Cancelling task {}", taskId);
            onCancel();
        } finally {
            if (participant != null) {
                participant.unregisterRunningTask(this);
            } else {
                logger.warn("Task with id: {} is not unregistered since the participant is not set", taskId);
            }
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
        return nextTask.invoke(new TaskResult(TaskResult.Status.COMPLETED, message));
    }

    protected TaskResult onFail(String reason, boolean fatal) {
        taskRunGauge.dec();
        taskFailCounter.inc();
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, reason);
    }

    protected void publishErrors(Throwable e) {
        // TODO Publish through kafka channel with task and workflow id
        logger.error("Task error", e);
    }

    public void sendNextJob(String jobId) {
        putUserContent(WORKFLOW_STARTED, "TRUE", Scope.WORKFLOW);
        if (jobId != null) {
            putUserContent(NEXT_JOB, jobId, Scope.WORKFLOW);
        }
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

    public org.apache.airavata.task.base.TaskCallbackContext getCallbackContext() {
        return callbackContext;
    }

    public AbstractTask setCallbackContext(org.apache.airavata.task.base.TaskCallbackContext callbackContext) {
        this.callbackContext = callbackContext;
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

    public OutPort getNextTask() {
        return nextTask;
    }

    public void setNextTask(OutPort nextTask) {
        this.nextTask = nextTask;
    }

    public AbstractTask setParticipant(DaprParticipant<? extends AbstractTask> participant) {
        this.participant = participant;
        return this;
    }

    @Override
    protected String getContextKey() {
        // Use workflow name from callback context if available
        if (callbackContext != null && callbackContext.getJobConfig() != null) {
            return callbackContext.getJobConfig().getWorkflow();
        }
        return taskId != null ? taskId : "default";
    }
}
