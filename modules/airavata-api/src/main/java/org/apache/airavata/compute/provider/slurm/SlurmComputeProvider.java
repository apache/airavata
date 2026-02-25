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
package org.apache.airavata.compute.provider.slurm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.compute.provider.ComputeProvider;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.compute.resource.repository.JobRepository;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.compute.resource.submission.JobFactory;
import org.apache.airavata.compute.resource.submission.JobManagerSpec;
import org.apache.airavata.compute.resource.submission.JobSubmissionDataBuilder;
import org.apache.airavata.compute.resource.submission.JobSubmissionSupport;
import org.apache.airavata.compute.resource.submission.RawCommandInfo;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.execution.dag.DagTaskResult;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.execution.scheduling.ComputeSubmissionTracker;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.CommandOutput;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SLURM/PLAIN compute provider — covers the full lifecycle of SLURM-scheduled
 * job execution.
 *
 * <p>Resource lifecycle:
 * <ul>
 *   <li>{@link #provision} — creates working directory on remote resource via SSH
 *   <li>{@link #deprovision} — state correction + local/remote directory cleanup
 * </ul>
 *
 * <p>Job lifecycle:
 * <ul>
 *   <li>{@link #submit} — submits batch job to SLURM scheduler
 *   <li>{@link #monitor} — polls job status via squeue until terminal state
 *   <li>{@link #cancel} — cancels remote jobs + force-publishes CANCELED status
 * </ul>
 */
@Component
@ConditionalOnParticipant
public class SlurmComputeProvider implements ComputeProvider {

    private static final Logger logger = LoggerFactory.getLogger(SlurmComputeProvider.class);
    private static final CounterMetric defaultJSTaskCounter = new CounterMetric("default_js_task_counter");
    private static final String DEFAULT_JOB_ID = "DEFAULT_JOB_ID";

    private final AdapterSupport adapterSupport;
    private final JobSubmissionSupport jobSubmissionSupport;
    private final JobSubmissionDataBuilder jobSubmissionDataBuilder;
    private final ComputeSubmissionTracker computeSubmissionTracker;
    private final JobService jobService;
    private final StatusService statusService;
    private final ServerProperties serverProperties;
    private final JobFactory jobFactory;
    private final JobRepository jobRepository;

    public SlurmComputeProvider(
            AdapterSupport adapterSupport,
            JobSubmissionSupport jobSubmissionSupport,
            JobSubmissionDataBuilder jobSubmissionDataBuilder,
            ComputeSubmissionTracker computeSubmissionTracker,
            JobService jobService,
            StatusService statusService,
            ServerProperties serverProperties,
            JobFactory jobFactory,
            JobRepository jobRepository) {
        this.adapterSupport = adapterSupport;
        this.jobSubmissionSupport = jobSubmissionSupport;
        this.jobSubmissionDataBuilder = jobSubmissionDataBuilder;
        this.computeSubmissionTracker = computeSubmissionTracker;
        this.jobService = jobService;
        this.statusService = statusService;
        this.serverProperties = serverProperties;
        this.jobFactory = jobFactory;
        this.jobRepository = jobRepository;
    }

    // =========================================================================
    // Provision — create working directory on remote resource
    // =========================================================================

    @Override
    public DagTaskResult provision(TaskContext context) {
        try {
            AgentAdapter adapter = adapterSupport.fetchAdapter(
                    context.getGatewayId(),
                    context.getComputeResourceId(),
                    context.getJobSubmissionProtocol(),
                    context.getComputeResourceCredentialToken(),
                    context.getComputeResourceLoginUserName());

            String workingDir = context.getWorkingDir();
            logger.info("Creating directory {} on compute resource {} by user {} using token {}",
                    workingDir,
                    context.getComputeResourceId(),
                    context.getComputeResourceLoginUserName(),
                    context.getComputeResourceCredentialToken());

            adapter.createDirectory(workingDir, true);
            return new DagTaskResult.Success("Provisioning completed for " + context.getTaskId());

        } catch (Exception e) {
            return new DagTaskResult.Failure(
                    "Failed to provision compute resource for task " + context.getTaskId(), false, e);
        }
    }

    // =========================================================================
    // Submit — submit batch job to SLURM scheduler
    // =========================================================================

    @Override
    public DagTaskResult submit(TaskContext context) {

        defaultJSTaskCounter.inc();
        String jobId = null;
        AgentAdapter adapter;
        String computeId = null;

        try {
            computeId = context.getComputeResourceId();
            adapter = adapterSupport.fetchAdapter(
                    context.getGatewayId(),
                    computeId,
                    context.getJobSubmissionProtocol(),
                    context.getComputeResourceCredentialToken(),
                    context.getComputeResourceLoginUserName());
        } catch (Exception e) {
            return new DagTaskResult.Failure("Failed to fetch adapter to connect to " + computeId, true, e);
        }

        try {
            var jobsOfTask = jobService.getJobs("processId", context.getProcessId());

            if (!jobsOfTask.isEmpty()) {
                logger.warn("A job is already available for process " + context.getProcessId());
                return new DagTaskResult.Success("A job is already available for process " + context.getProcessId());
            }

            var mapData = jobSubmissionDataBuilder.build(context);

            var jobModel = jobSubmissionSupport.createJobModel(context.getProcessId(), context.getTaskId(), mapData);

            var submissionOutput = jobSubmissionSupport.submitBatchJob(
                    adapter, mapData, mapData.getWorkingDirectory(),
                    context.getComputeResource(), context.getProcessId(),
                    computeSubmissionTracker, computeId);

            jobModel.setJobDescription(submissionOutput.getDescription());
            jobModel.setExitCode(submissionOutput.getExitCode());
            jobModel.setStdErr(submissionOutput.getStdErr());
            jobModel.setStdOut(submissionOutput.getStdOut());

            jobId = submissionOutput.getJobId();

            if (submissionOutput.getExitCode() != 0 || submissionOutput.isJobSubmissionFailed()) {

                jobModel.setJobId(DEFAULT_JOB_ID);
                if (submissionOutput.isJobSubmissionFailed()) {
                    jobSubmissionSupport.publishJobStatus(jobModel, JobState.FAILED, submissionOutput.getFailureReason());
                    jobService.saveJob(jobModel);
                    logger.error(
                            "Job submission failed for job name {}. Exit code : {}, Submission failed : {}",
                            jobModel.getJobName(),
                            submissionOutput.getExitCode(),
                            submissionOutput.isJobSubmissionFailed());

                    logger.error("Standard error message : {}", submissionOutput.getStdErr());
                    logger.error("Standard out message : {}", submissionOutput.getStdOut());
                    return new DagTaskResult.Failure(
                            "Job submission command didn't return a jobId. Reason "
                                    + submissionOutput.getFailureReason(),
                            false);

                } else {
                    String msg;
                    jobService.saveJob(jobModel);
                    var errorModel = new ErrorModel();
                    if (submissionOutput.getExitCode() != Integer.MIN_VALUE) {
                        msg = "Returned non zero exit code:" + submissionOutput.getExitCode() + "  for JobName:"
                                + jobModel.getJobName() + ", with failure reason : "
                                + submissionOutput.getFailureReason()
                                + " Hence changing job state to Failed.";
                        errorModel.setActualErrorMessage(submissionOutput.getFailureReason());
                    } else {
                        msg = "Didn't return valid job submission exit code for JobName:" + jobModel.getJobName()
                                + ", with failure reason : stdout ->"
                                + submissionOutput.getStdOut() + " stderr -> "
                                + submissionOutput.getStdErr() + " Hence changing job state to Failed.";
                        errorModel.setActualErrorMessage(msg);
                    }
                    logger.error(msg);
                    return new DagTaskResult.Failure(msg, false);
                }

            } else if (jobId != null && !jobId.isEmpty()) {

                logger.info("Received job id {} from compute resource", jobId);
                jobModel.setJobId(jobId);
                jobService.saveJob(jobModel);

                jobSubmissionSupport.publishJobStatus(jobModel, JobState.SUBMITTED,
                        "Successfully Submitted to " + context.getComputeResource().getHostName());

                if (verifyJobSubmissionByJobId(adapter, jobId, context)) {
                    jobSubmissionSupport.publishJobStatus(jobModel, JobState.QUEUED, "Verification step succeeded");
                }

            } else {

                int verificationTryCount = 0;
                while (verificationTryCount++ < 3) {
                    String verifyJobId = verifyJobSubmission(
                            adapter, jobModel.getJobName(), context.getComputeResourceLoginUserName(), context);
                    if (verifyJobId != null && !verifyJobId.isEmpty()) {
                        jobId = verifyJobId;
                        jobModel.setJobId(jobId);
                        jobService.saveJob(jobModel);
                        jobSubmissionSupport.publishJobStatus(jobModel, JobState.QUEUED, "Verification step succeeded");
                        logger.info("Job id {} verification succeeded", verifyJobId);
                        break;
                    }
                    logger.info(
                            "Verify step return invalid jobId, retry verification step in {} secs",
                            verificationTryCount * 10);
                    Thread.sleep(verificationTryCount * 10000L);
                }
            }

            if (jobId == null || jobId.isEmpty()) {
                jobModel.setJobId(DEFAULT_JOB_ID);
                jobService.saveJob(jobModel);
                String msg = "expId:" + context.getExperimentId() + " Couldn't find " + "remote jobId for JobName:"
                        + jobModel.getJobName() + ", both submit and verify steps " + "doesn't return a valid JobId. "
                        + "Hence changing experiment state to Failed";
                logger.error(msg);
                return new DagTaskResult.Failure(
                        "Couldn't find job id in both submitted and verified steps. " + msg, false);

            } else {
                mapData.setJobId(jobId);
                return new DagTaskResult.Success("Submitted job to compute resource");
            }

        } catch (Exception e) {

            logger.error("Task failed due to unexpected issue. Trying to control damage", e);

            if (jobId != null && !jobId.isEmpty()) {
                logger.warn("Job {} has already being submitted. Trying to cancel the job", jobId);
                try {
                    boolean cancelled = jobSubmissionSupport.cancelJob(
                            adapter, jobId, context.getComputeResource());
                    if (cancelled) {
                        logger.info("Job {} cancellation triggered", jobId);
                    } else {
                        logger.error("Failed to cancel job {}. Please contact system admins", jobId);
                    }
                } catch (Exception e1) {
                    logger.error("Error while cancelling the job {}. Please contact system admins", jobId);
                }
            }

            return new DagTaskResult.Failure("Task failed due to unexpected issue", false, e);
        }
    }

    // =========================================================================
    // Deprovision — state correction + directory cleanup
    // =========================================================================

    @Override
    public DagTaskResult deprovision(TaskContext context) {
        logger.info("Deprovisioning for process {}", context.getProcessId());

        ensureExecutingStateBeforeCompleted(context);
        cleanupLocalData(context);
        cleanupWorkingDirectory(context);

        return new DagTaskResult.Success("Process " + context.getProcessId() + " successfully completed");
    }

    // =========================================================================
    // Monitor — poll job status until terminal state
    // =========================================================================

    @Override
    public DagTaskResult monitor(TaskContext context) {
        try {
            List<JobModel> jobs = jobService.getJobs("processId", context.getProcessId());
            if (jobs == null || jobs.isEmpty()) {
                return new DagTaskResult.Success("No running jobs found for process " + context.getProcessId());
            }

            logger.info("Found {} jobs for process {} — polling until saturated", jobs.size(), context.getProcessId());

            JobManagerSpec jobManagerConfig = jobFactory.getJobManagerConfiguration(context.getComputeResource());
            AgentAdapter adapter = adapterSupport.fetchAdapter(
                    context.getGatewayId(),
                    context.getComputeResourceId(),
                    context.getJobSubmissionProtocol(),
                    context.getComputeResourceCredentialToken(),
                    context.getComputeResourceLoginUserName());

            for (JobModel job : jobs) {
                pollJobUntilSaturated(adapter, jobManagerConfig, job);
            }

            return new DagTaskResult.Success("Job monitoring completed for process " + context.getProcessId());

        } catch (Exception e) {
            logger.error("Error during job monitoring for process {} — continuing (non-critical)",
                    context.getProcessId(), e);
            return new DagTaskResult.Success("Job monitoring encountered errors but continuing (non-critical)");
        }
    }

    // =========================================================================
    // Cancel — cancel remote jobs
    // =========================================================================

    @Override
    public DagTaskResult cancel(TaskContext context) {
        logger.info("Cancelling jobs for process {}", context.getProcessId());

        try {
            cancelRemoteJobs(context);
        } catch (Exception e) {
            logger.error("Error during job cancellation for process {}", context.getProcessId(), e);
            return new DagTaskResult.Failure(
                    "Error during job cancellation for process " + context.getProcessId(), true, e);
        }

        return new DagTaskResult.Success("Process " + context.getProcessId() + " successfully cancelled");
    }

    // -------------------------------------------------------------------------
    // Monitor helpers
    // -------------------------------------------------------------------------

    private void pollJobUntilSaturated(AgentAdapter adapter, JobManagerSpec config, JobModel job) {
        try {
            var monitorCommand = config.getMonitorCommand(job.getJobId());
            if (monitorCommand.isEmpty()) {
                logger.info("No monitor command for job {} — skipping", job.getJobId());
                return;
            }

            int retryDelaySeconds = 30;
            for (int i = 1; i <= 4; i++) {
                CommandOutput output = adapter.executeCommand(monitorCommand.get().getRawCommand(), null);
                if (output.getExitCode() != 0) {
                    logger.warn("Monitor command failed for job {}: stdout={}, stderr={}",
                            job.getJobId(), output.getStdOut(), output.getStdError());
                    break;
                }

                StatusModel<JobState> jobStatus = config.getParser()
                        .parseJobStatus(job.getJobId(), output.getStdOut());
                if (jobStatus == null) {
                    logger.info("Status unavailable for job {} — skipping", job.getJobId());
                    break;
                }

                logger.info("Job {} status: {}", job.getJobId(), jobStatus.getState());

                if (jobStatus.getState() != JobState.ACTIVE
                        && jobStatus.getState() != JobState.QUEUED
                        && jobStatus.getState() != JobState.SUBMITTED) {
                    logger.info("Job {} reached saturated state", job.getJobId());
                    break;
                }

                int waitTime = retryDelaySeconds * i;
                logger.info("Waiting {} seconds before next poll for job {}", waitTime, job.getJobId());
                Thread.sleep(waitTime * 1000L);
            }
        } catch (Exception e) {
            logger.warn("Error polling job {} — continuing", job.getJobId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Submit helpers
    // -------------------------------------------------------------------------

    private boolean verifyJobSubmissionByJobId(AgentAdapter agentAdapter, String jobID, TaskContext context) {
        StatusModel<JobState> status = null;
        try {
            status = jobSubmissionSupport.getJobStatus(agentAdapter, jobID, context.getComputeResource());
        } catch (Exception e) {
            logger.warn("Error while fetching the job status for id " + jobID);
        }
        return status != null && status.getState() != JobState.UNKNOWN;
    }

    private String verifyJobSubmission(AgentAdapter agentAdapter, String jobName, String userName, TaskContext context) {
        String jobId = null;
        try {
            jobId = jobSubmissionSupport.getJobIdByJobName(
                    agentAdapter, jobName, userName, context.getComputeResource());
        } catch (Exception e) {
            logger.warn("Error while verifying JobId from JobName " + jobName);
        }
        return jobId;
    }

    // -------------------------------------------------------------------------
    // Deprovision helpers — state machine correction
    // -------------------------------------------------------------------------

    private void ensureExecutingStateBeforeCompleted(TaskContext context) {
        try {
            var currentStatus = statusService.getLatestProcessStatus(context.getProcessId());
            if (currentStatus != null && currentStatus.getState() != null) {
                var currentState = currentStatus.getState();
                if (currentState != ProcessState.EXECUTING
                        && currentState != ProcessState.MONITORING
                        && currentState != ProcessState.OUTPUT_DATA_STAGING
                        && currentState != ProcessState.POST_PROCESSING
                        && currentState != ProcessState.COMPLETED) {
                    logger.info("Process {} at state {}, transitioning through EXECUTING before COMPLETED",
                            context.getProcessId(), currentState);
                    publishProcessStatus(context, ProcessState.EXECUTING);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not check/update intermediate process state for {}", context.getProcessId(), e);
        }
    }

    private void publishProcessStatus(TaskContext context, ProcessState state) {
        try {
            var status = org.apache.airavata.core.model.StatusModel.of(state);
            statusService.addProcessStatus(status, context.getProcessId());
            logger.info("Saved intermediate process status {} for process {}", state, context.getProcessId());
        } catch (Exception e) {
            logger.warn("Failed to save process status {} for process {}", state, context.getProcessId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Deprovision helpers — local data cleanup
    // -------------------------------------------------------------------------

    private void cleanupLocalData(TaskContext context) {
        try {
            String localDataPath = serverProperties.localDataLocation();
            localDataPath = (localDataPath.endsWith(File.separator)
                    ? localDataPath
                    : localDataPath + File.separator);
            localDataPath = localDataPath + context.getProcessId();

            Path path = Path.of(localDataPath);
            if (Files.exists(path)) {
                Files.walk(path).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        logger.warn("Failed to delete {}", p, e);
                    }
                });
                logger.info("Cleaned up local data directory {}", localDataPath);
            }
        } catch (Exception e) {
            logger.error("Failed to clean up local data directory for process {}", context.getProcessId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Deprovision helpers — remote working directory cleanup
    // -------------------------------------------------------------------------

    private void cleanupWorkingDirectory(TaskContext context) {
        try {
            var adapter = adapterSupport.fetchAdapter(
                    context.getGatewayId(),
                    context.getComputeResourceId(),
                    context.getJobSubmissionProtocol(),
                    context.getComputeResourceCredentialToken(),
                    context.getComputeResourceLoginUserName());
            logger.info("Cleaning up working directory {}", context.getWorkingDir());
            adapter.deleteDirectory(context.getWorkingDir());
        } catch (Exception e) {
            logger.error("Failed to clean up working directory for experiment {}", context.getExperimentId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Cancel helpers — remote job cancellation
    // -------------------------------------------------------------------------

    private void cancelRemoteJobs(TaskContext context) throws Exception {
        List<JobModel> jobs = jobRepository.findByProcessId(context.getProcessId()).stream()
                .map(entity -> {
                    JobModel m = new JobModel();
                    m.setJobId(entity.getJobId());
                    m.setProcessId(entity.getProcessId());
                    m.setJobStatuses(entity.getJobStatuses());
                    return m;
                })
                .collect(Collectors.toList());

        if (jobs.isEmpty()) {
            logger.info("No jobs found for process {} — nothing to cancel", context.getProcessId());
            return;
        }

        logger.info("Found {} jobs for process {} — checking states and cancelling",
                jobs.size(), context.getProcessId());

        JobManagerSpec jobManagerConfig;
        try {
            jobManagerConfig = jobFactory.getJobManagerConfiguration(context.getComputeResource());
        } catch (Exception e) {
            logger.warn("Failed to resolve job manager configuration for process {}. Skipping cancellation.",
                    context.getProcessId(), e);
            return;
        }

        AgentAdapter adapter = adapterSupport.fetchAdapter(
                context.getGatewayId(),
                context.getComputeResourceId(),
                context.getJobSubmissionProtocol(),
                context.getComputeResourceCredentialToken(),
                context.getComputeResourceLoginUserName());

        for (JobModel job : jobs) {
            if (isJobAlreadySaturated(job, adapter, jobManagerConfig)) {
                continue;
            }

            try {
                RawCommandInfo cancelCommand = jobManagerConfig.getCancelCommand(job.getJobId());
                logger.info("Cancelling job {} with command: {}", job.getJobId(), cancelCommand.getRawCommand());

                CommandOutput cancelOutput = adapter.executeCommand(cancelCommand.getRawCommand(), null);
                if (cancelOutput.getExitCode() != 0) {
                    logger.warn("Cancel command failed for job {}: stdout={}, stderr={}",
                            job.getJobId(), cancelOutput.getStdOut(), cancelOutput.getStdError());
                }
            } catch (Exception ex) {
                logger.error("Error cancelling job {} of process {}", job.getJobId(), context.getProcessId());
                throw ex;
            }

            publishJobStatus(job.getJobId(), JobState.CANCELED);
        }
    }

    private boolean isJobAlreadySaturated(JobModel job, AgentAdapter adapter, JobManagerSpec config) {
        if (job.getJobStatuses() != null && !job.getJobStatuses().isEmpty()) {
            StatusModel<JobState> lastStatus = job.getJobStatuses().stream()
                    .max(Comparator.comparing(StatusModel::getTimeOfStateChange))
                    .orElse(null);
            if (lastStatus != null) {
                switch (lastStatus.getState()) {
                    case FAILED, CANCELED, COMPLETED, SUSPENDED -> {
                        logger.info("Job {} already in saturated state {} (monitoring)",
                                job.getJobId(), lastStatus.getState());
                        return true;
                    }
                    default -> {}
                }
            }
        }

        try {
            var monitorCommand = config.getMonitorCommand(job.getJobId());
            if (monitorCommand.isEmpty()) {
                return false;
            }

            CommandOutput output = adapter.executeCommand(monitorCommand.get().getRawCommand(), null);
            if (output.getExitCode() == 0) {
                StatusModel<JobState> clusterStatus = config.getParser()
                        .parseJobStatus(job.getJobId(), output.getStdOut());
                if (clusterStatus != null) {
                    switch (clusterStatus.getState()) {
                        case COMPLETED, CANCELED, SUSPENDED, FAILED -> {
                            logger.info("Job {} already in saturated state {} (cluster)",
                                    job.getJobId(), clusterStatus.getState());
                            return true;
                        }
                        default -> {}
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error checking cluster status for job {} — proceeding with cancellation",
                    job.getJobId(), e);
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Cancel helpers — job status publishing
    // -------------------------------------------------------------------------

    private void publishJobStatus(String jobId, JobState jobState) {
        try {
            StatusModel<JobState> jobStatus = StatusModel.of(jobState, jobState.name());
            statusService.addJobStatus(jobStatus, jobId);
        } catch (Exception e) {
            logger.error("Error persisting job status for job {}", jobId, e);
        }
    }
}
