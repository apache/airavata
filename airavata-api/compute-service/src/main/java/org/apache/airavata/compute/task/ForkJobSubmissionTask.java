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
package org.apache.airavata.compute.task;

import org.apache.airavata.compute.util.JobSubmissionOutput;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.task.TaskContext;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.util.AiravataUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Fork Job Submission")
@SuppressWarnings("unused")
public class ForkJobSubmissionTask extends JobSubmissionTask {

    private static final Logger logger = LoggerFactory.getLogger(ForkJobSubmissionTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        try {
            GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();

            AgentAdaptor adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

            JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, mapData.getWorkingDirectory());

            JobModel jobModel = JobModel.newBuilder()
                    .setProcessId(getProcessId())
                    .setWorkingDir(mapData.getWorkingDirectory())
                    .setCreationTime(AiravataUtils.getCurrentTimestamp().getTime())
                    .setTaskId(getTaskId())
                    .setJobName(mapData.getJobName())
                    .setJobDescription(submissionOutput.getDescription())
                    .setExitCode(submissionOutput.getExitCode())
                    .setStdErr(submissionOutput.getStdErr())
                    .setStdOut(submissionOutput.getStdOut())
                    .build();

            String jobId = submissionOutput.getJobId();

            if (jobId != null && !jobId.isEmpty()) {
                JobStatus jobStatus = JobStatus.newBuilder()
                        .setJobState(JobState.SUBMITTED)
                        .setReason("Successfully Submitted to "
                                + getComputeResourceDescription().getHostName())
                        .setTimeOfStateChange(
                                AiravataUtils.getCurrentTimestamp().getTime())
                        .build();
                jobModel = jobModel.toBuilder()
                        .setJobId(jobId)
                        .clearJobStatuses()
                        .addJobStatuses(jobStatus)
                        .build();
                saveJobModel(jobModel);
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
