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
package org.apache.airavata.helix.task.factory;

import org.apache.airavata.helix.task.aws.AWSCompletingTask;
import org.apache.airavata.helix.task.aws.AWSJobSubmissionTask;
import org.apache.airavata.helix.task.aws.CreateEC2InstanceTask;
import org.apache.airavata.helix.task.aws.NoOperationTask;
import org.apache.airavata.helix.task.base.AiravataTask;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.participant.enabled", havingValue = "true", matchIfMissing = true)
public class AWSTaskFactory implements HelixTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSTaskFactory.class);

    private final ApplicationContext applicationContext;
    private final RegistryService registryService;
    private final UserProfileService userProfileService;
    private final CredentialStoreService credentialStoreService;
    private final org.apache.airavata.helix.task.submission.GroovyMapBuilder groovyMapBuilder;
    private final org.apache.airavata.helix.task.aws.utils.AWSTaskUtil awsTaskUtil;
    private final org.apache.airavata.messaging.core.MessagingFactory messagingFactory;

    public AWSTaskFactory(
            ApplicationContext applicationContext,
            RegistryService registryService,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            org.apache.airavata.helix.task.submission.GroovyMapBuilder groovyMapBuilder,
            org.apache.airavata.helix.task.aws.utils.AWSTaskUtil awsTaskUtil,
            org.apache.airavata.messaging.core.MessagingFactory messagingFactory) {
        this.applicationContext = applicationContext;
        this.registryService = registryService;
        this.userProfileService = userProfileService;
        this.credentialStoreService = credentialStoreService;
        this.groovyMapBuilder = groovyMapBuilder;
        this.awsTaskUtil = awsTaskUtil;
        this.messagingFactory = messagingFactory;
    }

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating AWS CreateEc2InstanceTask for process {}...", processId);
        return applicationContext.getBean(CreateEC2InstanceTask.class);
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        return applicationContext.getBean(NoOperationTask.class);
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        return applicationContext.getBean(AWSJobSubmissionTask.class);
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        return applicationContext.getBean(NoOperationTask.class);
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        return applicationContext.getBean(NoOperationTask.class);
    }

    @Override
    public AiravataTask createJobVerificationTask(String processId) {
        return applicationContext.getBean(NoOperationTask.class);
    }

    @Override
    public AiravataTask createCompletingTask(String processId) {
        return applicationContext.getBean(AWSCompletingTask.class);
    }

    @Override
    public AiravataTask createParsingTriggeringTask(String processId) {
        return applicationContext.getBean(NoOperationTask.class);
    }
}
