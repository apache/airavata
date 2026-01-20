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
package org.apache.airavata.task.factory;

import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.completing.CompletingTask;
import org.apache.airavata.task.env.EnvSetupTask;
import org.apache.airavata.task.parsing.ParsingTriggeringTask;
import org.apache.airavata.task.staging.ArchiveTask;
import org.apache.airavata.task.staging.InputDataStagingTask;
import org.apache.airavata.task.staging.JobVerificationTask;
import org.apache.airavata.task.staging.OutputDataStagingTask;
import org.apache.airavata.task.submission.DefaultJobSubmissionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnParticipant
public class SlurmTaskFactory implements DaprTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmTaskFactory.class);

    private final ApplicationContext applicationContext;
    private final RegistryService registryService;
    private final UserProfileService userProfileService;
    private final CredentialStoreService credentialStoreService;
    private final org.apache.airavata.task.submission.GroovyMapBuilder groovyMapBuilder;
    private final org.apache.airavata.dapr.messaging.DaprMessagingFactory messagingFactory;

    public SlurmTaskFactory(
            ApplicationContext applicationContext,
            RegistryService registryService,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            org.apache.airavata.task.submission.GroovyMapBuilder groovyMapBuilder,
            org.apache.airavata.dapr.messaging.DaprMessagingFactory messagingFactory) {
        this.applicationContext = applicationContext;
        this.registryService = registryService;
        this.userProfileService = userProfileService;
        this.credentialStoreService = credentialStoreService;
        this.groovyMapBuilder = groovyMapBuilder;
        this.messagingFactory = messagingFactory;
    }

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating Slurm EnvSetupTask for process {}...", processId);
        return applicationContext.getBean(EnvSetupTask.class);
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm InputDataStagingTask for process {}...", processId);
        return applicationContext.getBean(InputDataStagingTask.class);
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        LOGGER.info("Creating Slurm DefaultJobSubmissionTask for process {}...", processId);
        return applicationContext.getBean(DefaultJobSubmissionTask.class);
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm OutputDataStagingTask for process {}...", processId);
        return applicationContext.getBean(OutputDataStagingTask.class);
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        LOGGER.info("Creating Slurm ArchiveTask for process {}...", processId);
        return applicationContext.getBean(ArchiveTask.class);
    }

    @Override
    public AiravataTask createJobVerificationTask(String processId) {
        LOGGER.info("Creating Slurm JobVerificationTask for process {}...", processId);
        return applicationContext.getBean(JobVerificationTask.class);
    }

    @Override
    public AiravataTask createCompletingTask(String processId) {
        LOGGER.info("Creating Slurm CompletingTask for process {}...", processId);
        return applicationContext.getBean(CompletingTask.class);
    }

    @Override
    public AiravataTask createParsingTriggeringTask(String processId) {
        LOGGER.info("Creating Slurm ParsingTriggeringTask for process {}...", processId);
        return applicationContext.getBean(ParsingTriggeringTask.class);
    }
}
