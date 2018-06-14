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
 * <ul>
 * <li>Queue will be there until user delete it.</li>
 * <li>Queue can keep accepting tasks.</li>
 * <li>No parallel run allowed except intentionally configured.</li>
 * </ul>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class QueueOperator {

    private final static Logger logger = LoggerFactory.getLogger(QueueOperator.class);

    private static final String QUEUE_PREFIX = "Job_queue_";

    private HelixManager helixManager;
    private TaskDriver taskDriver;

    /**
     * This is the constructor for {@link QueueOperator}
     *
     * @param helixClusterName   is the name of the Helix cluster
     * @param instanceName       is the name of the Helix instance
     * @param zkConnectionString is the connection details for Zookeeper connection in {@code <host>:<port>} format
     * @throws Exception can be thrown when connecting
     */
    public QueueOperator(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> helixManager.disconnect())
        );

        taskDriver = new TaskDriver(helixManager);
    }

    /**
     * Creates a new Helix job queue and returns its name
     *
     * @param queueId is the identifier given for the queue
     * @param monitor indicates whether monitoring is required for the queue
     * @return the name of the queue that needs to be used for queue operations after creating it
     * @throws InterruptedException can be thrown (if monitor enabled) when polling for workflow state
     */
    public synchronized String createQueue(String queueId, boolean monitor) throws InterruptedException {

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

    /**
     * Stops the queue. The queue is guaranteed to be stopped if this method completes without any exception.
     *
     * @param queueName is the name returned at {@link #createQueue(String, boolean)}
     * @param timeout   is the timeout to stop the queue in milliseconds
     * @throws InterruptedException can be thrown when stopping the queue
     */
    public synchronized void stopQueue(String queueName, int timeout) throws InterruptedException {
        logger.info("Stopping queue: " + queueName + " with timeout: " + timeout);
        taskDriver.waitToStop(queueName, timeout);
    }

    /**
     * Resumes the queue if it was stopped
     *
     * @param queueName is the name returned at {@link #createQueue(String, boolean)}
     */
    public synchronized void resumeQueue(String queueName) {
        logger.info("Resuming queue: " + queueName);
        taskDriver.resume(queueName);
    }

    /**
     * Deletes the queue
     *
     * @param queueName is the name returned at {@link #createQueue(String, boolean)}
     */
    public synchronized void deleteQueue(String queueName) {
        if (taskDriver.getWorkflows().containsKey(queueName)) {
            logger.info("Deleting queue: " + queueName);
            taskDriver.delete(queueName);
        }
        logger.warn("Provided queue name: " + queueName + " is not available");
    }

    /**
     * Removes all jobs that are in final states (ABORTED, FAILED, COMPLETED) from the job queue. The
     * job config, job context will be removed from Zookeeper.
     *
     * @param queueName is the name returned at {@link #createQueue(String, boolean)}
     */
    public synchronized void cleanupQueue(String queueName) {
        logger.info("Cleaning up queue: " + queueName);
        taskDriver.cleanupQueue(queueName);
    }

    /**
     * Adds a new task to the queue
     *
     * @param queueName         is the name returned at {@link #createQueue(String, boolean)}
     * @param task              {@link AbstractTask} instance which needs to added to the queue
     * @param globalParticipant enable if needs to handled by the global participant
     * @return the identifier of the added task
     * @throws IllegalAccessException can be thrown when serializing the task
     */
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

    /**
     * Removes a task from the queue
     *
     * @param queueName is the name returned at {@link #createQueue(String, boolean)}
     * @param taskId    is the identifier of the task to be removed
     * @param timeout   is the timeout to stop the queue before removing task in milliseconds
     * @return the identifier of the removed task
     * @throws InterruptedException can be thrown when stooping the queue before removing the task
     */
    public synchronized String removeTaskFromQueue(String queueName, String taskId, int timeout) throws InterruptedException {
        logger.info("Removing task: " + taskId + " from queue: " + queueName);
        taskDriver.waitToStop(queueName, timeout);
        taskDriver.deleteJob(queueName, taskId);
        taskDriver.resume(queueName);
        return taskId;
    }
}
