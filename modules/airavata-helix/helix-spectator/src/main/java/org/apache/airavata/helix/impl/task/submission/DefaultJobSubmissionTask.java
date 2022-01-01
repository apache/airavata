/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.submission;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.submission.config.GroovyMapBuilder;
import org.apache.airavata.helix.impl.task.submission.config.GroovyMapData;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.commons.ErrorModel;
 import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@TaskDef(name = "Default Job Submission")
public class DefaultJobSubmissionTask extends JobSubmissionTask {

    private final static Logger logger = LoggerFactory.getLogger(DefaultJobSubmissionTask.class);
    private final static CountMonitor defaultJSTaskCounter = new CountMonitor("default_js_task_counter");

    private static final String DEFAULT_JOB_ID = "DEFAULT_JOB_ID";

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        defaultJSTaskCounter.inc();
        String jobId = null;
        AgentAdaptor adaptor;
        String computeId = null;

        try {
            computeId = getTaskContext().getComputeResourceId();
            adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
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

            saveAndPublishProcessStatus(ProcessState.EXECUTING);
            GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();

            JobModel jobModel = new JobModel();
            jobModel.setProcessId(getProcessId());
            jobModel.setWorkingDir(mapData.getWorkingDirectory());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setTaskId(getTaskId());
            jobModel.setJobName(mapData.getJobName());
            jobModel.setJobDescription("Sample description");

            JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, mapData.getWorkingDirectory());

            jobModel.setJobDescription(submissionOutput.getDescription());
            jobModel.setExitCode(submissionOutput.getExitCode());
            jobModel.setStdErr(submissionOutput.getStdErr());
            jobModel.setStdOut(submissionOutput.getStdOut());

            jobId = submissionOutput.getJobId();

            if (submissionOutput.getExitCode() != 0 || submissionOutput.isJobSubmissionFailed()) {

                jobModel.setJobId(DEFAULT_JOB_ID);
                if (submissionOutput.isJobSubmissionFailed()) {
                    List<JobStatus> statusList = new ArrayList<>();
                    statusList.add(new JobStatus(JobState.FAILED));
                    statusList.get(0).setReason(submissionOutput.getFailureReason());
                    jobModel.setJobStatuses(statusList);
                    saveJobModel(jobModel);
                    logger.error("Job submission failed for job name " + jobModel.getJobName()
                            + ". Exit code : " + submissionOutput.getExitCode() + ", Submission failed : "
                            + submissionOutput.isJobSubmissionFailed());

                    logger.error("Standard error message : " + submissionOutput.getStdErr());
                    logger.error("Standard out message : " + submissionOutput.getStdOut());
                    return onFail("Job submission command didn't return a jobId. Reason " + submissionOutput.getFailureReason(),
                            false, null);

                } else {
                    String msg;
                    saveJobModel(jobModel);
                    ErrorModel errorModel = new ErrorModel();
                    if (submissionOutput.getExitCode() != Integer.MIN_VALUE) {
                        msg = "Returned non zero exit code:" + submissionOutput.getExitCode() + "  for JobName:" + jobModel.getJobName() +
                                ", with failure reason : " + submissionOutput.getFailureReason()
                                + " Hence changing job state to Failed." ;
                        errorModel.setActualErrorMessage(submissionOutput.getFailureReason());
                    } else {
                        msg = "Didn't return valid job submission exit code for JobName:" + jobModel.getJobName() +
                                ", with failure reason : stdout ->" + submissionOutput.getStdOut() +
                                " stderr -> " + submissionOutput.getStdErr() + " Hence changing job state to Failed." ;
                        errorModel.setActualErrorMessage(msg);
                    }
                    logger.error(msg);
                    return onFail(msg, false, null);

                }

            } else if (jobId != null && !jobId.isEmpty()) {

                logger.info("Received job id " + jobId + " from compute resource");
                jobModel.setJobId(jobId);
                saveJobModel(jobModel);

                JobStatus jobStatus = new JobStatus();
                jobStatus.setJobState(JobState.SUBMITTED);
                jobStatus.setReason("Successfully Submitted to " + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Collections.singletonList(jobStatus));
                saveAndPublishJobStatus(jobModel);

                if (verifyJobSubmissionByJobId(adaptor, jobId)) {
                    jobStatus.setJobState(JobState.QUEUED);
                    jobStatus.setReason("Verification step succeeded");
                    jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    jobModel.setJobStatuses(Collections.singletonList(jobStatus));
                    saveAndPublishJobStatus(jobModel);
                }

            } else {

                int verificationTryCount = 0;
                while (verificationTryCount++ < 3) {
                    String verifyJobId = verifyJobSubmission(adaptor, jobModel.getJobName(), getTaskContext().getComputeResourceLoginUserName());
                    if (verifyJobId != null && !verifyJobId.isEmpty()) {
                        // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
                        jobId = verifyJobId;
                        jobModel.setJobId(jobId);
                        saveJobModel(jobModel);
                        JobStatus jobStatus = new JobStatus();
                        jobStatus.setJobState(JobState.QUEUED);
                        jobStatus.setReason("Verification step succeeded");
                        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                        jobModel.setJobStatuses(Collections.singletonList(jobStatus));
                        saveAndPublishJobStatus(jobModel);
                        logger.info("Job id " + verifyJobId + " verification succeeded");
                        break;
                    }
                    logger.info("Verify step return invalid jobId, retry verification step in " + (verificationTryCount * 10) + " secs");
                    Thread.sleep(verificationTryCount * 10000);
                }
            }

            if (jobId == null || jobId.isEmpty()) {
                jobModel.setJobId(DEFAULT_JOB_ID);
                saveJobModel(jobModel);
                String msg = "expId:" + getExperimentId() + " Couldn't find " +
                        "remote jobId for JobName:" + jobModel.getJobName() + ", both submit and verify steps " +
                        "doesn't return a valid JobId. " + "Hence changing experiment state to Failed";
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

                        BufferedReader reader = new BufferedReader(new InputStreamReader(commandSubmit.getInputStream()));
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
                        logger.info("No usage reporting found for gateway {} and compute resource id {}",
                                                                    getGatewayId(), taskContext.getComputeResourceId());
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
        return status != null &&  status.getJobState() != JobState.UNKNOWN;
    }

    private String verifyJobSubmission(AgentAdaptor agentAdaptor, String jobName, String userName) {
        String jobId = null;
        try {
            jobId  = getJobIdByJobName(agentAdaptor, jobName, userName);
        } catch (Exception e) {
            logger.warn("Error while verifying JobId from JobName " + jobName);
        }
        return jobId;
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
