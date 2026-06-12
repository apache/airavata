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

import java.util.List;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.interfaces.CommandOutput;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.server.CountMonitor;
import org.apache.airavata.task.TaskContext;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.task.DbTaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous job monitor. After {@link DefaultJobSubmissionTask} submits the batch job and returns,
 * this task gates the DAG: it polls the remote scheduler over SSH until the job reaches a terminal
 * state, so output staging only runs once the job has actually finished. Terminal state is read from
 * {@code sacct} (which retains finished jobs), with the job disappearing from {@code squeue} treated
 * as a completion signal when the accounting record is not yet available.
 */
@TaskDef(name = "Monitoring Task")
public class MonitoringTask extends JobSubmissionTask {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringTask.class);
    private static final CountMonitor monitoringTaskCounter = new CountMonitor("monitoring_task_counter");

    private static final long POLL_INTERVAL_MS = 5000;
    private static final long MAX_WAIT_MS = 30 * 60 * 1000; // 30 minutes

    @Override
    public DbTaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        monitoringTaskCounter.inc();
        try {
            String jobId = resolveJobId();
            if (jobId == null || jobId.isEmpty() || "DEFAULT_JOB_ID".equals(jobId)) {
                return onFail("No valid remote job id found for process " + getProcessId() + "; cannot monitor", true, null);
            }

            AgentAdaptor adaptor = taskHelper
                    .getAdaptorSupport()
                    .fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

            logger.info("Monitoring remote job {} for process {} until terminal", jobId, getProcessId());

            long deadline = System.currentTimeMillis() + MAX_WAIT_MS;
            JobState terminal = null;
            while (System.currentTimeMillis() < deadline) {
                JobState state = pollTerminalState(adaptor, jobId);
                if (state != null) {
                    terminal = state;
                    break;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }

            if (terminal == null) {
                return onFail("Timed out monitoring remote job " + jobId + " for process " + getProcessId(), false, null);
            }

            persistJobState(jobId, terminal);
            logger.info("Remote job {} for process {} reached terminal state {}", jobId, getProcessId(), terminal);

            if (terminal == JobState.COMPLETE) {
                return onSuccess("Job " + jobId + " completed on compute resource");
            }
            return onFail("Job " + jobId + " ended in state " + terminal, true, null);

        } catch (Exception e) {
            return onFail("Unknown error while monitoring job for task " + getTaskId(), false, e);
        }
    }

    /** Read the remote job id persisted by the job-submission task for this process. */
    private String resolveJobId() throws Exception {
        List<JobModel> jobs = getTaskContext().getRegistryClient().getJobs("processId", getProcessId());
        for (JobModel job : jobs) {
            if (job.getJobId() != null && !job.getJobId().isEmpty()) {
                return job.getJobId();
            }
        }
        return null;
    }

    /**
     * Return the terminal {@link JobState} for the job, or {@code null} if it is still pending/running.
     * Prefers {@code sacct} (retains completed jobs); if accounting has no record yet, falls back to
     * the scheduler monitor command ({@code squeue}) and treats its disappearance as completion.
     */
    private JobState pollTerminalState(AgentAdaptor adaptor, String jobId) {
        try {
            CommandOutput out = adaptor.executeCommand("sacct -X -n -P -o State -j " + jobId, null);
            String stdout = out.getStdOut() == null ? "" : out.getStdOut().trim();
            if (!stdout.isEmpty()) {
                String state = stdout.split("\\r?\\n")[0].trim();
                // sacct may suffix cancellations as "CANCELLED by <uid>"
                if (state.startsWith("CANCELLED")) {
                    return JobState.CANCELED;
                }
                switch (state) {
                    case "COMPLETED":
                        return JobState.COMPLETE;
                    case "FAILED":
                    case "NODE_FAIL":
                    case "TIMEOUT":
                    case "OUT_OF_MEMORY":
                    case "BOOT_FAIL":
                    case "DEADLINE":
                        return JobState.FAILED;
                    case "PENDING":
                    case "RUNNING":
                    case "REQUEUED":
                    case "RESIZING":
                    case "SUSPENDED":
                    case "CONFIGURING":
                    case "COMPLETING":
                        return null; // still in flight
                    default:
                        logger.debug("Unrecognized sacct state '{}' for job {}; continuing to poll", state, jobId);
                        return null;
                }
            }
        } catch (Exception e) {
            logger.warn("sacct poll failed for job {}: {}", jobId, e.getMessage());
        }

        // No accounting record: fall back to the scheduler monitor command. If the job is no longer in
        // the queue, parseJobStatus returns null, which we read as "left the queue" => completed.
        try {
            JobStatus status = getJobStatus(adaptor, jobId);
            if (status == null || status.getJobState() == JobState.JOB_STATE_UNKNOWN) {
                logger.info("Job {} no longer visible in scheduler queue; treating as completed", jobId);
                return JobState.COMPLETE;
            }
            JobState s = status.getJobState();
            if (s == JobState.COMPLETE || s == JobState.FAILED || s == JobState.CANCELED) {
                return s;
            }
        } catch (Exception e) {
            logger.warn("Scheduler queue poll failed for job {}: {}", jobId, e.getMessage());
        }
        return null;
    }

    private void persistJobState(String jobId, JobState state) {
        try {
            JobStatus jobStatus = JobStatus.newBuilder()
                    .setJobState(state)
                    .setReason("Monitoring task observed terminal state " + state)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            getRegistryServiceClient().addJobStatus(jobStatus, getTaskId(), jobId);
        } catch (Exception e) {
            logger.warn("Failed to persist terminal job status for job {}: {}", jobId, e.getMessage());
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
