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
package org.apache.airavata.helix.task.submission;

import java.util.UUID;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.task.TaskDef;
import org.apache.airavata.helix.task.TaskHelper;
import org.apache.airavata.helix.task.TaskUtil;
import org.apache.airavata.helix.task.base.TaskContext;
import org.apache.helix.task.TaskResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@TaskDef(name = "Local Job Submission")
@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.participant.enabled", havingValue = "true", matchIfMissing = true)
public class LocalJobSubmissionTask extends JobSubmissionTask {

    public LocalJobSubmissionTask(
            TaskUtil taskUtil,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.messaging.core.MessagingFactory messagingFactory,
            org.apache.airavata.helix.task.submission.GroovyMapBuilder groovyMapBuilder,
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

            // TODO fix this
            /*File jobFile = SubmissionUtil.createJobFile(groovyMapData);

            if (jobFile != null && jobFile.exists()) {
                jobModel.setJobDescription(FileUtils.readFileToString(jobFile));
                saveJobModel(jobModel);

                AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                        getTaskContext().getGatewayId(),
                        getTaskContext().getComputeResourceId(),
                        getTaskContext().getJobSubmissionProtocol().name(),
                        getTaskContext().getComputeResourceCredentialToken(),
                        getTaskContext().getComputeResourceLoginUserName());

                GroovyMapData mapData = groovyMapBuilder.build(getTaskContext());
                JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, groovyMapData.getWorkingDirectory());

                JobStatus jobStatus = new JobStatus();
                jobStatus.setJobState(JobState.SUBMITTED);
                jobStatus.setReason("Successfully Submitted to " + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));

                saveAndPublishJobStatus(jobModel);

                jobModel.setExitCode(submissionOutput.getExitCode());
                jobModel.setStdErr(submissionOutput.getStdErr());
                jobModel.setStdOut(submissionOutput.getStdOut());

                jobStatus.setJobState(JobState.COMPLETE);
                jobStatus.setReason("Successfully Completed " + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));

                saveAndPublishJobStatus(jobModel);

                return null;
            }*/

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
