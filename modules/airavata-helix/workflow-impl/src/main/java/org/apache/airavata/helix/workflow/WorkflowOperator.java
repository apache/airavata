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
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.core.util.TaskUtil;
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
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class WorkflowOperator {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowOperator.class);

    private static final String WORKFLOW_PREFIX = "Workflow_of_process_";
    private static final long WORKFLOW_EXPIRY_TIME = 1 * 1000;
    private static final long TASK_EXPIRY_TIME = 24 * 60 * 60 * 1000;
    private TaskDriver taskDriver;
    private HelixManager helixManager;

    public WorkflowOperator(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        if (helixManager != null && helixManager.isConnected()) {
                            helixManager.disconnect();
                        }
                    }
                }
        );

        taskDriver = new TaskDriver(helixManager);
    }

    public void disconnect() {
        if (helixManager != null && helixManager.isConnected()) {
            helixManager.disconnect();
        }
    }

    public synchronized String launchWorkflow(String processId, List<AbstractTask> tasks, boolean globalParticipant, boolean monitor) throws Exception {

        String workflowName = WORKFLOW_PREFIX + processId;
        logger.info("Launching workflow " + workflowName + " for process " + processId);

        Workflow.Builder workflowBuilder = new Workflow.Builder(workflowName).setExpiry(0);

        for (int i = 0; i < tasks.size(); i++) {
            AbstractTask data = tasks.get(i);
            String taskType = data.getClass().getAnnotation(TaskDef.class).name();
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder().setTaskId("Task_" + data.getTaskId())
                    .setCommand(taskType);
            Map<String, String> paramMap = org.apache.airavata.helix.core.util.TaskUtil.serializeTaskData(data);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setExpiry(WORKFLOW_EXPIRY_TIME)
                    .setTimeoutPerTask(TASK_EXPIRY_TIME)
                    .setNumConcurrentTasksPerInstance(20)
                    .setMaxAttemptsPerTask(data.getRetryCount());


            if (!globalParticipant) {
                job.setInstanceGroupTag(taskType);
            }

            workflowBuilder.addJob((data.getTaskId()), job);

            List<OutPort> outPorts = TaskUtil.getOutPortsOfTask(data);
            outPorts.forEach(outPort -> {
                if (outPort != null) {
                    workflowBuilder.addParentChildDependency(data.getTaskId(), outPort.getNextJobId());
                }
            });
        }

        WorkflowConfig.Builder config = new WorkflowConfig.Builder().setFailureThreshold(0);
        workflowBuilder.setWorkflowConfig(config.build());
        workflowBuilder.setExpiry(WORKFLOW_EXPIRY_TIME);
        Workflow workflow = workflowBuilder.build();

        taskDriver.start(workflow);

        //TODO : Do we need to monitor workflow status? If so how do we do it in a scalable manner? For example,
        // if the hfac that monitors a particular workflow, got killed due to some reason, who is taking the responsibility

        if (monitor) {
            TaskState taskState = pollForWorkflowCompletion(workflow.getName(), 3600000);
            logger.info("Workflow " + workflowName + " for process " + processId + " finished with state " + taskState.name());

        }
        return workflowName;

    }

    public synchronized TaskState pollForWorkflowCompletion(String workflowName, long timeout) throws InterruptedException {
        return taskDriver.pollForWorkflowState(workflowName, timeout, TaskState.COMPLETED,
                TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED);
    }

    public TaskState getWorkflowState(String workflow) {
        return taskDriver.getWorkflowContext(workflow).getWorkflowState();
    }
}