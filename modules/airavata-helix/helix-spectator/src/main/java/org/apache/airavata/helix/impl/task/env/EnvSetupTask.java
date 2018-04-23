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
package org.apache.airavata.helix.impl.task.env;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Environment Setup Task")
public class EnvSetupTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(EnvSetupTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        try {
            saveAndPublishProcessStatus(ProcessState.CONFIGURING_WORKSPACE);
            AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    getTaskContext().getComputeResourceId(),
                    getTaskContext().getJobSubmissionProtocol(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());

            logger.info("Creating directory " + getTaskContext().getWorkingDir() + " on compute resource " + getTaskContext().getComputeResourceId());
            adaptor.createDirectory(getTaskContext().getWorkingDir());
            return onSuccess("Envi setup task successfully completed " + getTaskId());

        } catch (Exception e) {
            return onFail("Failed to setup environment of task " + getTaskId(), true, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }

}
