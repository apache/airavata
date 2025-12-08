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
package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.TaskDriver;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.TaskState;
import org.apache.helix.task.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Workflow Cancellation Task")
public class WorkflowCancellationTask extends AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowCancellationTask.class);

    private TaskDriver taskDriver;
    private HelixManager helixManager;

    @TaskParam(name = "Cancelling Workflow")
    private String cancellingWorkflowName;

    @TaskParam(name = "Waiting time to monitor status (s)")
    private int waitTime = 20;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);

        try {
            String clusterName = "airavata"; // default
            String zkConnection = "localhost:2181"; // default
            try {
                var ctx = org.apache.airavata.helix.impl.task.AiravataTask.getApplicationContext();
                if (ctx != null) {
                    var props = ctx.getBean(org.apache.airavata.config.AiravataServerProperties.class);
                    clusterName = props.helix.clusterName;
                    zkConnection = props.zookeeper.serverConnection;
                }
            } catch (Exception e) {
                logger.warn("Could not get properties from ApplicationContext, using defaults", e);
            }
            helixManager =
                    HelixManagerFactory.getZKHelixManager(clusterName, taskName, InstanceType.SPECTATOR, zkConnection);
            helixManager.connect();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (helixManager.isConnected()) {
                    helixManager.disconnect();
                }
            }));
            taskDriver = new TaskDriver(helixManager);
        } catch (Exception e) {
            try {
                if (helixManager != null) {
                    if (helixManager.isConnected()) {
                        helixManager.disconnect();
                    }
                }
            } catch (Exception ex) {
                logger.warn("Failed to disconnect helix manager", ex);
            }

            logger.error("Failed to build Helix Task driver in " + taskName, e);
            throw new RuntimeException("Failed to build Helix Task driver in " + taskName, e);
        }
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Cancelling workflow " + cancellingWorkflowName);

        try {
            if (taskDriver.getWorkflowConfig(cancellingWorkflowName) == null) {
                // Workflow could be already deleted by cleanup agents
                logger.warn("Can not find a workflow with name " + cancellingWorkflowName + " but continuing");
                return onSuccess("Can not find a workflow with name " + cancellingWorkflowName + " but continuing");
            }

            try {
                WorkflowContext workflowContext = taskDriver.getWorkflowContext(cancellingWorkflowName);

                // if the workflow can not be found, ignore it
                if (workflowContext == null) {
                    logger.warn("Can not find a workflow with id " + cancellingWorkflowName + ". So ignoring");
                    return onSuccess("Can not find a workflow with id " + cancellingWorkflowName + ". So ignoring");
                }

                TaskState workflowState = workflowContext.getWorkflowState();
                logger.info("Current state of workflow " + cancellingWorkflowName + " : " + workflowState.name());

                taskDriver.stop(cancellingWorkflowName);

            } catch (Exception e) {
                logger.error("Failed to stop workflow " + cancellingWorkflowName, e);
                // in case of an error, retry
                return onFail("Failed to stop workflow " + cancellingWorkflowName + ": " + e.getMessage(), false);
            }

            try {
                logger.info("Waiting maximum " + waitTime + "s for workflow " + cancellingWorkflowName
                        + " state to change");
                TaskState newWorkflowState = taskDriver.pollForWorkflowState(
                        cancellingWorkflowName,
                        waitTime * 1000,
                        TaskState.COMPLETED,
                        TaskState.FAILED,
                        TaskState.STOPPED,
                        TaskState.ABORTED,
                        TaskState.NOT_STARTED);

                logger.info("Workflow " + cancellingWorkflowName + " state changed to " + newWorkflowState.name());
                return onSuccess("Successfully cancelled workflow " + cancellingWorkflowName);

            } catch (Exception e) {
                logger.warn("Failed while watching workflow to stop " + cancellingWorkflowName, e);
                return onSuccess(
                        "Failed while watching workflow to stop " + cancellingWorkflowName + ". But continuing");
            }

        } finally {

            try {
                if (helixManager != null) {
                    if (helixManager.isConnected()) {
                        helixManager.disconnect();
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to disconnect helix manager", e);
            }
        }
    }

    @Override
    public void onCancel() {}

    public String getCancellingWorkflowName() {
        return cancellingWorkflowName;
    }

    public void setCancellingWorkflowName(String cancellingWorkflowName) {
        this.cancellingWorkflowName = cancellingWorkflowName;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
