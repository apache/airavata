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
package org.apache.airavata.task.completing;

import org.apache.airavata.common.model.ExperimentCleanupStrategy;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.orchestrator.internal.messaging.DaprMessagingImpl.DaprMessagingFactory;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.base.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@TaskDef(name = "Completing Task")
@Component
@ConditionalOnParticipant
public class CompletingTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(CompletingTask.class);

    public CompletingTask(
            TaskUtil taskUtil,
            ApplicationContext applicationContext,
            RegistryService registryService,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            DaprMessagingFactory messagingFactory) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory);
    }

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting completing task for task " + getTaskId() + ", experiment id " + getExperimentId());
        logger.info("Process " + getProcessId() + " successfully completed");
        saveAndPublishProcessStatus(ProcessState.COMPLETED);
        cleanup();

        try {
            if (getExperimentModel().getCleanUpStrategy() == ExperimentCleanupStrategy.ALWAYS) {
                var adaptor = helper.getAdaptorSupport()
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
