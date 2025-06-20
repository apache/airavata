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

import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Cancel Completing Task")
public class CancelCompletingTask extends AiravataTask {
    private static final Logger logger = LoggerFactory.getLogger(CancelCompletingTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting cancel completing task for task " + getTaskId() + ", experiment id " + getExperimentId());
        logger.info("Process " + getProcessId() + " successfully cancelled");
        String cancelled = getContextVariable(RemoteJobCancellationTask.JOB_ALREADY_CANCELLED_OR_NOT_AVAILABLE);

        if ("true".equals(cancelled)) {
            // make  the experiment state as cancelled if it is already being cancelled or similar state.
            // Otherwise wait for the post workflow to cancel the experiment
            logger.info("Making process as cancelled as the job is already being cancelled or not available");
            saveAndPublishProcessStatus(ProcessState.CANCELED);
        } else {
            // TODO: Some schedulers do not send notifications once the job is cancelled. It will cause experiment to
            // stay in
            // cancelling state forever. So we are making the experiment is CANCELLED irrespective of the state of the
            // job
            logger.info("Job is not in the saturated state but updating experiment as cancelled");
            saveAndPublishProcessStatus(ProcessState.CANCELED);
        }

        logger.info("Deleting process level monitoring nodes");
        try {
            // TODO temporary stop cleaning up because this will cause later cancellation events to be gone un notified
            // MonitoringUtil.deleteProcessSpecificNodes(getCuratorClient(), getProcessId());
        } catch (Exception e) {
            logger.error("Failed to delete process specific nodes but continuing", e);
        }

        return onSuccess("Process " + getProcessId() + " successfully cancelled");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
