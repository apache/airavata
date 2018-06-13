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
package org.apache.airavata.helix.workflow;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link QueueOperator} is responsible for handling Airavata Task Queues. Unlike in workflow, queue has the
 * following properties.
 * <p>
 * Queue will be there until user delete it.
 * Queue can keep accepting tasks.
 * No parallel run allowed except intentionally configured.
 */
public class QueueOperator {

    private final static Logger logger = LoggerFactory.getLogger(QueueOperator.class);

    private static final String QUEUE_PREFIX = "Job_queue_";

    private HelixManager helixManager;
    private TaskDriver taskDriver;

    public QueueOperator(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> helixManager.disconnect())
        );

        taskDriver = new TaskDriver(helixManager);
    }

    public synchronized String createQueue(String queueId, boolean monitor) throws Exception {

        String queueName = QUEUE_PREFIX + queueId;
        logger.info("Launching queue " + queueName + " for job queue " + queueId);

        WorkflowConfig.Builder workflowCfgBuilder = new WorkflowConfig.Builder(queueName);
        workflowCfgBuilder.setFailureThreshold(0);
        workflowCfgBuilder.setExpiry(0);

        JobQueue.Builder jobQueueBuilder = new JobQueue.Builder(queueName).setWorkflowConfig(workflowCfgBuilder.build());

        taskDriver.start(jobQueueBuilder.build());

        if (monitor) {
            TaskState taskState = taskDriver.pollForWorkflowState(queueName,
                    TaskState.COMPLETED, TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED);
            logger.info("Queue " + queueName + " finished with state " + taskState.name());
        }

        return queueName;
    }

    public synchronized void stopQueue(String queueName) throws InterruptedException {
        logger.info("Stopping queue: " + queueName);
        taskDriver.stop(queueName);
    }

    public synchronized void resumeQueue(String queueName) {
        logger.info("Resuming queue: " + queueName);
        taskDriver.resume(queueName);
    }

    public synchronized void deleteQueue(String queueName) {
        logger.info("Deleting queue: " + queueName);
        taskDriver.delete(queueName);
    }

    public synchronized void cleanupQueue(String queueName) throws InterruptedException {
        logger.info("Cleaning up queue: " + queueName);
        taskDriver.cleanupQueue(queueName);
    }

    public synchronized String addTaskToQueue(String queueName, AbstractTask task, boolean globalParticipant) throws IllegalAccessException {
        logger.info("Adding task: " + task.getTaskId() + " to queue: " + queueName);

        String taskType = task.getClass().getAnnotation(TaskDef.class).name();

        TaskConfig.Builder taskBuilder = new TaskConfig.Builder().setTaskId("Task_" + task.getTaskId())
                .setCommand(taskType);

        Map<String, String> paramMap = org.apache.airavata.helix.core.util.TaskUtil.serializeTaskData(task);
        paramMap.forEach(taskBuilder::addConfig);

        List<TaskConfig> taskBuilds = new ArrayList<>();
        taskBuilds.add(taskBuilder.build());

        JobConfig.Builder job = new JobConfig.Builder()
                .addTaskConfigs(taskBuilds)
                .setFailureThreshold(0)
                .setMaxAttemptsPerTask(task.getRetryCount());

        if (!globalParticipant) {
            job.setInstanceGroupTag(taskType);
        }

        taskDriver.enqueueJob(queueName, task.getTaskId(), job);

        return task.getTaskId();
    }

    public synchronized String removeTaskFromQueue(String queueName, String taskId) throws InterruptedException {
        logger.info("Removing task: " + taskId + " from queue: " + queueName);
        taskDriver.stop(queueName);
        taskDriver.deleteJob(queueName, taskId);
        taskDriver.resume(queueName);
        return taskId;
    }
}
