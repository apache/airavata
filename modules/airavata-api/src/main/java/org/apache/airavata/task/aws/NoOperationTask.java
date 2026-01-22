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
package org.apache.airavata.task.aws;

import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.orchestrator.internal.messaging.DaprMessagingFactory;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.base.TaskContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@TaskDef(name = "No Operation Task")
@Component
@ConditionalOnParticipant
public class NoOperationTask extends AiravataTask {

    public NoOperationTask(
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
        return new TaskResult(TaskResult.Status.COMPLETED, "OK");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
