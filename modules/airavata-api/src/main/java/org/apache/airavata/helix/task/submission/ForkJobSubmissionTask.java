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

import java.util.List;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.helix.task.TaskDef;
import org.apache.airavata.helix.task.TaskHelper;
import org.apache.airavata.helix.task.TaskUtil;
import org.apache.airavata.helix.task.base.TaskContext;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@TaskDef(name = "Fork Job Submission")
@SuppressWarnings("unused")
@Component
@ConditionalOnParticipant
public class ForkJobSubmissionTask extends JobSubmissionTask {

    private static final Logger logger = LoggerFactory.getLogger(ForkJobSubmissionTask.class);

    public ForkJobSubmissionTask(
            TaskUtil taskUtil,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.messaging.rabbitmq.MessagingFactory messagingFactory,
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
            GroovyMapData mapData = groovyMapBuilder.build(getTaskContext());

            JobModel jobModel = new JobModel();
            jobModel.setProcessId(getProcessId());
            jobModel.setWorkingDir(mapData.getWorkingDirectory());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setTaskId(getTaskId());
            jobModel.setJobName(mapData.getJobName());

            AgentAdaptor adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

            JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, mapData.getWorkingDirectory());

            jobModel.setJobDescription(submissionOutput.getDescription());
            jobModel.setExitCode(submissionOutput.getExitCode());
            jobModel.setStdErr(submissionOutput.getStdErr());
            jobModel.setStdOut(submissionOutput.getStdOut());

            String jobId = submissionOutput.getJobId();

            if (jobId != null && !jobId.isEmpty()) {
                jobModel.setJobId(jobId);
                saveJobModel(jobModel);
                JobStatus jobStatus = new JobStatus();
                jobStatus.setJobState(JobState.SUBMITTED);
                jobStatus.setReason("Successfully Submitted to "
                        + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(
                        AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(List.of(jobStatus));
                saveAndPublishJobStatus(jobModel);

                return onSuccess("Job submitted successfully");
            } else {
                String msg = "expId:" + getExperimentId() + " Couldn't find remote jobId for JobName:"
                        + jobModel.getJobName()
                        + ", both submit and verify steps doesn't return a valid JobId. "
                        + "Hence changing experiment state to Failed";

                return onFail(msg, true, null);
            }

        } catch (Exception e) {
            logger.error("Unknown error while submitting job", e);
            return onFail("Unknown error while submitting job", true, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
