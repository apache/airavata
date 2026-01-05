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
package org.apache.airavata.helix.task.aws;

import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.helix.task.TaskDef;
import org.apache.airavata.helix.task.TaskHelper;
import org.apache.airavata.helix.task.aws.utils.AWSTaskUtil;
import org.apache.airavata.helix.task.base.AiravataTask;
import org.apache.airavata.helix.task.base.TaskContext;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@TaskDef(name = "AWS_COMPLETING_TASK")
@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.participant.enabled", havingValue = "true", matchIfMissing = true)
public class AWSCompletingTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(AWSCompletingTask.class);
    private final AWSTaskUtil awsTaskUtil;

    public AWSCompletingTask(
            org.apache.airavata.helix.task.TaskUtil taskUtil,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.messaging.core.MessagingFactory messagingFactory,
            AWSTaskUtil awsTaskUtil) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory);
        this.awsTaskUtil = awsTaskUtil;
    }

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting completing task for task {}, experiment id {}", getTaskId(), getExperimentId());
        logger.info("Process {} successfully completed", getProcessId());
        saveAndPublishProcessStatus(ProcessState.COMPLETED);
        cleanup();
        awsTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());
        return onSuccess("Process " + getProcessId() + " successfully completed");
    }

    /**
     * Called when the task is cancelled.
     * No cleanup needed for AWS completing tasks.
     */
    @Override
    public void onCancel(TaskContext taskContext) {}
}
