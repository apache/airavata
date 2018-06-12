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
import java.util.HashMap;
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

    private static final String WORKFLOW_PREFIX = "Job_queue_";
    private HelixManager helixManager;
    private HashMap<String, TaskQueue> taskQueues = new HashMap<>();

    public QueueOperator(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> helixManager.disconnect())
        );
    }

    public synchronized String createJobQueue(String queueName, boolean monitor) throws Exception {

        String workflowName = WORKFLOW_PREFIX + queueName;
        logger.info("Launching workflow " + workflowName + " for job queue " + queueName);

        WorkflowConfig.Builder workflowCfgBuilder = new WorkflowConfig.Builder(workflowName);
        workflowCfgBuilder.setFailureThreshold(0);
        workflowCfgBuilder.setExpiry(0);

        JobQueue.Builder jobQueueBuilder = new JobQueue.Builder(queueName).setWorkflowConfig(workflowCfgBuilder.build());
        taskQueues.put(queueName, new TaskQueue(jobQueueBuilder, workflowName, queueName, monitor));

        return workflowName;
    }

    public synchronized String addTaskToQueue(String queueName, AbstractTask task, boolean globalParticipant) throws IllegalAccessException {
        return taskQueues.get(queueName).addTask(task, globalParticipant);
    }

    public synchronized String removeTaskFromQueue(String queueName, String taskId) throws InterruptedException {
        return taskQueues.get(queueName).removeTask(queueName, taskId);
    }

    private class TaskQueue {

        private TaskDriver taskDriver;
        private String queueName;

        public TaskQueue(JobQueue.Builder jobQueueBuilder, String workflowName, String queueName, boolean monitor) throws InterruptedException {
            this.queueName = queueName;
            taskDriver = new TaskDriver(helixManager);
            taskDriver.start(jobQueueBuilder.build());
            if (monitor) {
                TaskState taskState = taskDriver.pollForWorkflowState(workflowName,
                        TaskState.COMPLETED, TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED);
                logger.info("Workflow " + workflowName + " for queue " + queueName + " finished with state " + taskState.name());
            }
        }

        public String addTask(AbstractTask task, boolean globalParticipant) throws IllegalAccessException {

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

        public String removeTask(String taskId, String queueName) throws InterruptedException {
            taskDriver.stop(queueName);
            taskDriver.deleteJob(queueName, taskId);
            taskDriver.resume(queueName);
            return taskId;
        }
    }
}
