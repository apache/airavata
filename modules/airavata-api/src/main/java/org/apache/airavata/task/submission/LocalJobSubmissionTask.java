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
package org.apache.airavata.task.submission;

import java.util.UUID;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@TaskDef(name = "Local Job Submission")
@Component
@ConditionalOnParticipant
public class LocalJobSubmissionTask extends JobSubmissionTask {
    private static final Logger logger = LoggerFactory.getLogger(LocalJobSubmissionTask.class);

    public LocalJobSubmissionTask(
            TaskUtil taskUtil,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.orchestrator.internal.messaging.DaprMessagingFactory messagingFactory,
            org.apache.airavata.task.submission.GroovyMapBuilder groovyMapBuilder,
            org.apache.airavata.monitor.compute.ComputeSubmissionTracker computeSubmissionTracker) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory,
                groovyMapBuilder,
                computeSubmissionTracker);
    }

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        try {
            GroovyMapData groovyMapData = new GroovyMapData();
            String jobId = "JOB_ID_" + UUID.randomUUID().toString();

            JobModel jobModel = new JobModel();
            jobModel.setProcessId(getProcessId());
            jobModel.setWorkingDir(groovyMapData.getWorkingDirectory());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setTaskId(getTaskId());
            jobModel.setJobId(jobId);

            // Local job submission: This task is for jobs that run directly on the Airavata server
            // without requiring remote compute resource submission. The implementation depends on
            // the specific use case - for now, this is a placeholder that creates a job model.
            // If local job execution is needed, it should be implemented to execute commands
            // directly on the local system without going through AgentAdaptor/remote submission.

            // Save the job model
            try {
                getRegistryService().addJob(jobModel, getProcessId());
            } catch (Exception e) {
                logger.error("Failed to save job model for local job {}", jobId, e);
                return new TaskResult(TaskResult.Status.FAILED, "Failed to save job model: " + e.getMessage());
            }

            return new TaskResult(TaskResult.Status.COMPLETED, "Local job model created with ID: " + jobId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
