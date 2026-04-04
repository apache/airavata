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
package org.apache.airavata.orchestration.task;

import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.model.experiment.proto.ExperimentCleanupStrategy;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.task.AiravataTask;
import org.apache.airavata.task.TaskContext;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Completing Task")
public class CompletingTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(CompletingTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting completing task for task " + getTaskId() + ", experiment id " + getExperimentId());
        logger.info("Process " + getProcessId() + " successfully completed");
        saveAndPublishProcessStatus(ProcessState.PROCESS_STATE_COMPLETED);
        cleanup();

        try {
            if (getExperimentModel().getCleanUpStrategy() == ExperimentCleanupStrategy.ALWAYS) {
                AgentAdaptor adaptor = helper.getAdaptorSupport()
                        .fetchAdaptor(
                                getTaskContext().getGatewayId(),
                                getTaskContext().getComputeResourceId(),
                                getTaskContext().getJobSubmissionProtocol(),
                                getTaskContext().getComputeResourceCredentialToken(),
                                getTaskContext().getComputeResourceLoginUserName());
                logger.info("Cleaning up the working directory {}", taskContext.getWorkingDir());
                adaptor.deleteDirectory(getTaskContext().getWorkingDir());
            }
        } catch (Exception e) {
            logger.error("Failed clean up experiment " + getExperimentId(), e);
        }
        return onSuccess("Process " + getProcessId() + " successfully completed");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
