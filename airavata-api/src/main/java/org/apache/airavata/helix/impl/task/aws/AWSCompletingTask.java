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
package org.apache.airavata.helix.impl.task.aws;

import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.aws.utils.AWSTaskUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "AWS_COMPLETING_TASK")
public class AWSCompletingTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(AWSCompletingTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting completing task for task {}, experiment id {}", getTaskId(), getExperimentId());
        logger.info("Process {} successfully completed", getProcessId());
        saveAndPublishProcessStatus(ProcessState.COMPLETED);
        cleanup();
        AWSTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());
        return onSuccess("Process " + getProcessId() + " successfully completed");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
