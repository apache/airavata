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
package org.apache.airavata.helix.impl.task.staging;

import java.util.List;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Job Verification Task")
public class JobVerificationTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(JobVerificationTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        try {
            List<JobModel> jobs = getRegistryService().getJobs("processId", getProcessId());

            logger.info("Fetching jobs for process " + getProcessId() + " to verify to saturation");

            if (jobs == null || jobs.size() == 0) {
                return onSuccess("Can not find running jobs for process " + getProcessId());
            }

            logger.info("Found " + jobs.size() + " jobs for process");

            logger.info("Fetching job manager configuration for process " + getProcessId());

            JobManagerConfiguration jobManagerConfiguration =
                    JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                            getRegistryService(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getPreferredJobSubmissionInterface()));

            AgentAdaptor adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

            for (JobModel job : jobs) {

                try {
                    RawCommandInfo monitorCommand = jobManagerConfiguration.getMonitorCommand(job.getJobId());
                    int retryDelaySeconds = 30;
                    int nextWaitingTime;
                    for (int i = 1; i <= 4; i++) {
                        CommandOutput jobMonitorOutput = adaptor.executeCommand(monitorCommand.getRawCommand(), null);

                        if (jobMonitorOutput.getExitCode() == 0) {
                            JobStatus jobStatus = jobManagerConfiguration
                                    .getParser()
                                    .parseJobStatus(job.getJobId(), jobMonitorOutput.getStdOut());
                            if (jobStatus != null) {
                                logger.info("Status of job id " + job.getJobId() + " " + jobStatus.getJobState());
                            } else {
                                logger.info("Status for job " + job.getJobId() + " is not available. Ignoring");
                                break;
                            }
                            if (jobStatus.getJobState() == JobState.ACTIVE
                                    || jobStatus.getJobState() == JobState.QUEUED
                                    || jobStatus.getJobState() == JobState.SUBMITTED) {
                                nextWaitingTime = retryDelaySeconds * i;
                                logger.info("Waiting " + nextWaitingTime + " seconds until the job becomes saturated");
                                Thread.sleep(nextWaitingTime);
                            } else {
                                logger.info("Job is in saturated state");
                                break;
                            }

                        } else {
                            logger.warn("Error while fetching the job " + job.getJobId() + " status. Std out "
                                    + jobMonitorOutput.getStdOut() + ". Std err " + jobMonitorOutput.getStdError()
                                    + ". Job monitor command " + monitorCommand.getRawCommand());
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unknown error while fetching the job status but continuing..", e);
                }
            }

            logger.info("Successfully completed job verification task");
            return onSuccess("Successfully completed job verification task");

        } catch (Exception e) {
            logger.error(
                    "Unknown error while verifying jobs of process " + getProcessId()
                            + " but continuing as this is non critical",
                    e);
            return onSuccess("Unknown error while verifying jobs of process " + getProcessId()
                    + " but continuing as this is non critical");
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
