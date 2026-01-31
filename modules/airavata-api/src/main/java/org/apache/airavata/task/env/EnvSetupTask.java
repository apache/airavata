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
package org.apache.airavata.task.env;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.base.TaskContext;
import org.apache.airavata.telemetry.CounterMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@TaskDef(name = "Environment Setup Task")
@Component
@ConditionalOnParticipant
public class EnvSetupTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(EnvSetupTask.class);
    private static final CounterMetric envSetupTaskCounter = new CounterMetric("env_setup_task_counter");

    public EnvSetupTask(
            org.apache.airavata.task.TaskUtil taskUtil,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.orchestrator.internal.messaging.DaprMessagingImpl.DaprMessagingFactory messagingFactory) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory);
    }

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        try {
            envSetupTaskCounter.inc();
            saveAndPublishProcessStatus(ProcessState.CONFIGURING_WORKSPACE);
            AgentAdaptor adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

            logger.info("Creating directory " + getTaskContext().getWorkingDir() + " on compute resource "
                    + getTaskContext().getComputeResourceId()
                    + " by user " + getTaskContext().getComputeResourceLoginUserName()
                    + " using token " + getTaskContext().getComputeResourceCredentialToken());
            adaptor.createDirectory(getTaskContext().getWorkingDir(), true);
            return onSuccess("Envi setup task successfully completed " + getTaskId());

        } catch (Exception e) {
            return onFail("Failed to setup environment of task " + getTaskId(), false, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
