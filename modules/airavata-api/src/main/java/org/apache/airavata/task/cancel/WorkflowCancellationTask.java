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
package org.apache.airavata.task.cancel;

import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskParam;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@TaskDef(name = "Workflow Cancellation Task")
@Component
@ConditionalOnParticipant
public class WorkflowCancellationTask extends AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowCancellationTask.class);

    public WorkflowCancellationTask(TaskUtil taskUtil) {
        super(taskUtil);
    }

    @TaskParam(name = "Cancelling Workflow")
    private String cancellingWorkflowName;

    @TaskParam(name = "Waiting time to monitor status (s)")
    private int waitTime = 20;

    @Override
    public void init(String workflowName, String jobName, String taskName) {
        super.init(workflowName, jobName, taskName);
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Cancelling workflow " + cancellingWorkflowName);

        // Note: Dapr Workflow cancellation integration is in progress
        // Workflow cancellation will be handled by Dapr Workflows API:
        // daprWorkflowClient.terminateWorkflow(cancellingWorkflowName);
        // For now, log and return success
        logger.warn(
                "Workflow cancellation via Dapr Workflows not yet implemented. Workflow: " + cancellingWorkflowName);
        return onSuccess(
                "Workflow cancellation requested for " + cancellingWorkflowName + " (Dapr implementation pending)");
    }

    /**
     * Called when the task is cancelled.
     * No cleanup needed for workflow cancellation tasks.
     */
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
