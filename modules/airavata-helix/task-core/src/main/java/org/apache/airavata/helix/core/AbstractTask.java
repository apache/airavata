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

import org.apache.airavata.helix.core.util.TaskUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
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

    private static final String NEXT_JOB = "next-job";
    private static final String WORKFLOW_STARTED = "workflow-started";

    @TaskParam(name = "taskId")
    private String taskId;

    @TaskOutPort(name = "Next Task")
    private OutPort nextTask;

    private TaskCallbackContext callbackContext;
    private TaskHelper taskHelper;

    private int retryCount = 3;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            TaskUtil.deserializeTaskData(this, this.callbackContext.getTaskConfig().getConfigMap());
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final TaskResult run() {
        boolean isThisNextJob = getUserContent(WORKFLOW_STARTED, Scope.WORKFLOW) == null ||
                this.callbackContext.getJobConfig().getJobId()
                        .equals(this.callbackContext.getJobConfig().getWorkflow() + "_" + getUserContent(NEXT_JOB, Scope.WORKFLOW));
        if (isThisNextJob) {
            return onRun(this.taskHelper);
        } else {
            return new TaskResult(TaskResult.Status.COMPLETED, "Not a target job");
        }
    }

    @Override
    public final void cancel() {
        logger.info("Cancelling task " + taskId);
        onCancel();
    }

    public abstract TaskResult onRun(TaskHelper helper);

    public abstract void onCancel();

    protected TaskResult onSuccess(String message) {
        String successMessage = "Task " + getTaskId() + " completed." + (message != null ? " Message : " + message : "");
        logger.info(successMessage);
        return nextTask.invoke(new TaskResult(TaskResult.Status.COMPLETED, message));
    }

    protected TaskResult onFail(String reason, boolean fatal) {
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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public OutPort getNextTask() {
        return nextTask;
    }

    public void setNextTask(OutPort nextTask) {
        this.nextTask = nextTask;
    }
}
