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

import java.io.*;
import java.util.*;
import org.apache.airavata.compute.util.JobSubmissionOutput;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.status.proto.*;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.apache.airavata.server.CountMonitor;
import org.apache.airavata.task.TaskContext;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.util.AiravataUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Default Job Submission")
public class DefaultJobSubmissionTask extends JobSubmissionTask {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJobSubmissionTask.class);
    private static final CountMonitor defaultJSTaskCounter = new CountMonitor("default_js_task_counter");

    private static final String DEFAULT_JOB_ID = "DEFAULT_JOB_ID";

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        defaultJSTaskCounter.inc();
        String jobId = null;
        AgentAdaptor adaptor;
        String computeId = null;

        try {
            computeId = getTaskContext().getComputeResourceId();
            adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            computeId,
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());
        } catch (Exception e) {
            return onFail("Failed to fetch adaptor to connect to " + computeId, true, e);
        }

        try {
            List<JobModel> jobsOfTask = getTaskContext().getRegistryClient().getJobs("taskId", getTaskId());

            if (jobsOfTask.size() > 0) {
                logger.warn("A job is already available for task " + getTaskId());
                return onSuccess("A job is already available for task " + getTaskId());
            }

            saveAndPublishProcessStatus(ProcessState.PROCESS_STATE_EXECUTING);
            GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();

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

            jobId = submissionOutput.getJobId();

            if (submissionOutput.getExitCode() != 0 || submissionOutput.isJobSubmissionFailed()) {

                jobModel = jobModel.toBuilder().setJobId(DEFAULT_JOB_ID).build();
                if (submissionOutput.isJobSubmissionFailed()) {
                    JobStatus failedStatus = JobStatus.newBuilder()
                            .setJobState(JobState.FAILED)
                            .setReason(submissionOutput.getFailureReason())
                            .build();
                    jobModel = jobModel.toBuilder()
                            .clearJobStatuses()
                            .addJobStatuses(failedStatus)
                            .build();
                    saveJobModel(jobModel);
                    logger.error("Job submission failed for job name " + jobModel.getJobName()
                            + ". Exit code : " + submissionOutput.getExitCode() + ", Submission failed : "
                            + submissionOutput.isJobSubmissionFailed());

                    logger.error("Standard error message : " + submissionOutput.getStdErr());
                    logger.error("Standard out message : " + submissionOutput.getStdOut());
                    return onFail(
                            "Job submission command didn't return a jobId. Reason "
                                    + submissionOutput.getFailureReason(),
                            false,
                            null);

                } else {
                    String msg;
                    saveJobModel(jobModel);
                    ErrorModel.Builder errorModelBuilder = ErrorModel.newBuilder();
                    if (submissionOutput.getExitCode() != Integer.MIN_VALUE) {
                        msg = "Returned non zero exit code:" + submissionOutput.getExitCode() + "  for JobName:"
                                + jobModel.getJobName() + ", with failure reason : "
                                + submissionOutput.getFailureReason()
                                + " Hence changing job state to Failed.";
                        errorModelBuilder.setActualErrorMessage(submissionOutput.getFailureReason());
                    } else {
                        msg = "Didn't return valid job submission exit code for JobName:" + jobModel.getJobName()
                                + ", with failure reason : stdout ->"
                                + submissionOutput.getStdOut() + " stderr -> "
                                + submissionOutput.getStdErr() + " Hence changing job state to Failed.";
                        errorModelBuilder.setActualErrorMessage(msg);
                    }
                    logger.error(msg);
                    return onFail(msg, false, null);
                }

            } else if (jobId != null && !jobId.isEmpty()) {

                logger.info("Received job id " + jobId + " from compute resource");
                jobModel = jobModel.toBuilder().setJobId(jobId).build();
                saveJobModel(jobModel);

                JobStatus jobStatus = JobStatus.newBuilder()
                        .setJobState(JobState.SUBMITTED)
                        .setReason("Successfully Submitted to "
                                + getComputeResourceDescription().getHostName())
                        .setTimeOfStateChange(
                                AiravataUtils.getCurrentTimestamp().getTime())
                        .build();
                jobModel = jobModel.toBuilder()
                        .clearJobStatuses()
                        .addJobStatuses(jobStatus)
                        .build();
                saveAndPublishJobStatus(jobModel);

                if (verifyJobSubmissionByJobId(adaptor, jobId)) {
                    JobStatus queuedStatus = JobStatus.newBuilder()
                            .setJobState(JobState.QUEUED)
                            .setReason("Verification step succeeded")
                            .setTimeOfStateChange(
                                    AiravataUtils.getCurrentTimestamp().getTime())
                            .build();
                    jobModel = jobModel.toBuilder()
                            .clearJobStatuses()
                            .addJobStatuses(queuedStatus)
                            .build();
                    saveAndPublishJobStatus(jobModel);
                }

            } else {

                int verificationTryCount = 0;
                while (verificationTryCount++ < 3) {
                    String verifyJobId = verifyJobSubmission(
                            adaptor, jobModel.getJobName(), getTaskContext().getComputeResourceLoginUserName());
                    if (verifyJobId != null && !verifyJobId.isEmpty()) {
                        // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
                        jobId = verifyJobId;
                        jobModel = jobModel.toBuilder().setJobId(jobId).build();
                        saveJobModel(jobModel);
                        JobStatus jobStatus = JobStatus.newBuilder()
                                .setJobState(JobState.QUEUED)
                                .setReason("Verification step succeeded")
                                .setTimeOfStateChange(
                                        AiravataUtils.getCurrentTimestamp().getTime())
                                .build();
                        jobModel = jobModel.toBuilder()
                                .clearJobStatuses()
                                .addJobStatuses(jobStatus)
                                .build();
                        saveAndPublishJobStatus(jobModel);
                        logger.info("Job id " + verifyJobId + " verification succeeded");
                        break;
                    }
                    logger.info("Verify step return invalid jobId, retry verification step in "
                            + (verificationTryCount * 10) + " secs");
                    Thread.sleep(verificationTryCount * 10000);
                }
            }

            if (jobId == null || jobId.isEmpty()) {
                jobModel = jobModel.toBuilder().setJobId(DEFAULT_JOB_ID).build();
                saveJobModel(jobModel);
                String msg = "expId:" + getExperimentId() + " Couldn't find " + "remote jobId for JobName:"
                        + jobModel.getJobName() + ", both submit and verify steps " + "doesn't return a valid JobId. "
                        + "Hence changing experiment state to Failed";
                logger.error(msg);
                return onFail("Couldn't find job id in both submitted and verified steps. " + msg, false, null);

            } else {
                // usage reporting as the last step of job submission task
                try {
                    mapData.setJobId(jobId);
                    boolean reportingAvailable = getRegistryServiceClient()
                            .isGatewayUsageReportingAvailable(getGatewayId(), taskContext.getComputeResourceId());

                    if (reportingAvailable) {
                        GatewayUsageReportingCommand reportingCommand = getRegistryServiceClient()
                                .getGatewayReportingCommand(getGatewayId(), taskContext.getComputeResourceId());

                        String parsedCommand = mapData.loadFromString(reportingCommand.getCommand());
                        logger.debug("Parsed usage reporting command {}", parsedCommand);

                        Process commandSubmit = Runtime.getRuntime().exec(parsedCommand);

                        BufferedReader reader =
                                new BufferedReader(new InputStreamReader(commandSubmit.getInputStream()));
                        StringBuffer output = new StringBuffer();

                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line);
                            output.append("\n");
                        }

                        logger.info("Usage reporting output " + output.toString());
                        commandSubmit.waitFor();
                        logger.info("Usage reporting completed");

                    } else {
                        logger.info(
                                "No usage reporting found for gateway {} and compute resource id {}",
                                getGatewayId(),
                                taskContext.getComputeResourceId());
                    }
                } catch (Exception e) {
                    logger.error("Usage reporting failed but continuing. ", e);
                }
                return onSuccess("Submitted job to compute resource");
            }

        } catch (Exception e) {

            logger.error("Task failed due to unexpected issue. Trying to control damage", e);

            if (jobId != null && !jobId.isEmpty()) {
                logger.warn("Job " + jobId + " has already being submitted. Trying to cancel the job");
                try {
                    boolean cancelled = cancelJob(adaptor, jobId);
                    if (cancelled) {
                        logger.info("Job " + jobId + " cancellation triggered");
                    } else {
                        logger.error("Failed to cancel job " + jobId + ". Please contact system admins");
                    }
                } catch (Exception e1) {
                    logger.error("Error while cancelling the job " + jobId + ". Please contact system admins");
                    // ignore as we have nothing to do at this point
                }
            }

            return onFail("Task failed due to unexpected issue", false, e);
        }
    }

    private boolean verifyJobSubmissionByJobId(AgentAdaptor agentAdaptor, String jobID) {
        JobStatus status = null;

        try {
            status = getJobStatus(agentAdaptor, jobID);
        } catch (Exception e) {
            logger.warn("Error while fetching the job status for id " + jobID);
        }
        return status != null && status.getJobState() != JobState.JOB_STATE_UNKNOWN;
    }

    private String verifyJobSubmission(AgentAdaptor agentAdaptor, String jobName, String userName) {
        String jobId = null;
        try {
            jobId = getJobIdByJobName(agentAdaptor, jobName, userName);
        } catch (Exception e) {
            logger.warn("Error while verifying JobId from JobName " + jobName);
        }
        return jobId;
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
