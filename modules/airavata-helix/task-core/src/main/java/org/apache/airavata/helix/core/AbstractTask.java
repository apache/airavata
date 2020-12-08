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
package org.apache.airavata.helix.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.core.util.TaskUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.airavata.patform.monitoring.GaugeMonitor;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.helix.HelixManager;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public abstract class AbstractTask extends UserContentStore implements Task {

    private final static Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    private final static CountMonitor taskInitCounter = new CountMonitor("task_init_count");
    private final static GaugeMonitor taskRunGauge = new GaugeMonitor("task_run_gauge");
    private final static CountMonitor taskCancelCounter = new CountMonitor("task_cancel_count");
    private final static CountMonitor taskFailCounter = new CountMonitor("task_fail_count");
    private final static CountMonitor taskCompleteCounter = new CountMonitor("task_complete_count");

    private static final String NEXT_JOB = "next-job";
    private static final String WORKFLOW_STARTED = "workflow-started";

    private static CuratorFramework curatorClient = null;

    @TaskParam(name = "taskId")
    private String taskId;

    @TaskOutPort(name = "Next Task")
    private OutPort nextTask;

    private TaskCallbackContext callbackContext;
    private TaskHelper taskHelper;
    private HelixParticipant participant;

    @TaskParam(name = "Retry Count")
    private int retryCount = 3;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            taskInitCounter.inc();
            TaskUtil.deserializeTaskData(this, this.callbackContext.getTaskConfig().getConfigMap());
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
            boolean isThisNextJob = getUserContent(WORKFLOW_STARTED, Scope.WORKFLOW) == null ||
                    this.callbackContext.getJobConfig().getJobId()
                            .equals(this.callbackContext.getJobConfig().getWorkflow() + "_" + getUserContent(NEXT_JOB, Scope.WORKFLOW));

            return isThisNextJob ? onRun(this.taskHelper) : new TaskResult(TaskResult.Status.COMPLETED, "Not a target job");
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
            logger.info("Cancelling task " + taskId);
            onCancel();
        } finally {
            if (participant != null) {
                participant.unregisterRunningTask(this);
            } else {
                logger.warn("Task with id: " + taskId + " is not unregistered since the participant is not set");
            }
        }
    }

    public abstract TaskResult onRun(TaskHelper helper);

    public abstract void onCancel();

    protected TaskResult onSuccess(String message) {
        taskRunGauge.dec();
        taskCompleteCounter.inc();
        String successMessage = "Task " + getTaskId() + " completed." + (message != null ? " Message : " + message : "");
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
        e.printStackTrace();
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

    public TaskCallbackContext getCallbackContext() {
        return callbackContext;
    }

    public AbstractTask setCallbackContext(TaskCallbackContext callbackContext) {
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

    protected int getCurrentRetryCount() throws Exception {
        return MonitoringUtil.getTaskRetryCount(getCuratorClient(), taskId);
    }

    protected void markNewRetry(int currentRetryCount) throws Exception {
        MonitoringUtil.increaseTaskRetryCount(getCuratorClient(), taskId, currentRetryCount);
    }

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

    protected synchronized CuratorFramework getCuratorClient() {

        if (curatorClient == null) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            try {
                this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
                this.curatorClient.start();
            } catch (ApplicationSettingsException e) {
                logger.error("Failed to create curator client ", e);
                throw new RuntimeException(e);
            }
        }
        return curatorClient;
    }

    public AbstractTask setParticipant(HelixParticipant participant) {
        this.participant = participant;
        return this;
    }
}
