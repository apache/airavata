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
import org.apache.airavata.task.DbTaskResult;
import org.apache.airavata.task.TaskContext;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.util.AiravataUtils;
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
    // A node/boot failure is only treated as terminal once the job has stayed out of the scheduler
    // for this many consecutive polls: slurm requeues such failures, and the requeued run re-appears
    // in the queue, so we ride out a short grace window rather than failing the experiment on a
    // transient infrastructure hiccup.
    private static final int INFRA_FAIL_GRACE_POLLS = 6; // ~30s at POLL_INTERVAL_MS

    // The job's JobPK is (jobId, owningTaskId) where owningTaskId is the JOB_SUBMISSION task that
    // created the job — NOT this monitoring task. addJobStatus() does em.find(JobEntity, JobPK) and
    // silently no-ops on a miss, so persisting under getTaskId() (the monitoring task) drops the
    // update and the job stays frozen at its last submission-time status. Captured in resolveJobId().
    private String jobOwningTaskId;
    private boolean activeStatusPersisted = false;

    @Override
    public DbTaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        monitoringTaskCounter.inc();
        try {
            String jobId = resolveJobId();
            if (jobId == null || jobId.isEmpty() || "DEFAULT_JOB_ID".equals(jobId)) {
                return onFail(
                        "No valid remote job id found for process " + getProcessId() + "; cannot monitor", true, null);
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
            // Consecutive observations of an infra failure (NODE_FAIL/BOOT_FAIL) with the job no longer
            // in the scheduler. Slurm requeues such jobs, so the requeued run re-appears in the queue
            // (resetting this) and we keep tracking it; only a sustained absence is treated as failure.
            int infraFailStreak = 0;
            while (System.currentTimeMillis() < deadline) {
                JobLifecycle obs = observeJob(adaptor, jobId);
                switch (obs) {
                    case RUNNING:
                        // Job is running on the resource: record the transition once so the Job
                        // Submission stage advances past QUEUED to ACTIVE while it executes.
                        if (!activeStatusPersisted) {
                            persistJobState(jobId, JobState.ACTIVE);
                            activeStatusPersisted = true;
                        }
                        infraFailStreak = 0;
                        break;
                    case IN_QUEUE:
                        // Pending / requeued / in transition: still within the job's lifecycle.
                        infraFailStreak = 0;
                        break;
                    case COMPLETED:
                        terminal = JobState.COMPLETE;
                        break;
                    case CANCELED:
                        terminal = JobState.CANCELED;
                        break;
                    case FAILED:
                        terminal = JobState.FAILED;
                        break;
                    case INFRA_FAILURE:
                        if (++infraFailStreak >= INFRA_FAIL_GRACE_POLLS) {
                            terminal = JobState.FAILED;
                        }
                        break;
                }
                if (terminal != null) {
                    break;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }

            if (terminal == null) {
                return onFail(
                        "Timed out monitoring remote job " + jobId + " for process " + getProcessId(), false, null);
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

    /**
     * Read the remote job id persisted by the job-submission task for this process, and capture the
     * job's owning task id (the JOB_SUBMISSION task) — the second half of the job's {@code JobPK},
     * needed so {@link #persistJobState} updates the right job row rather than silently no-op-ing.
     */
    private String resolveJobId() throws Exception {
        List<JobModel> jobs = getTaskContext().getRegistryClient().getJobs("processId", getProcessId());
        for (JobModel job : jobs) {
            if (job.getJobId() != null && !job.getJobId().isEmpty()) {
                this.jobOwningTaskId = job.getTaskId();
                return job.getJobId();
            }
        }
        return null;
    }

    /**
     * State of the most-recently-submitted record among the {@code State|Submit} lines sacct emits for
     * a (possibly reused) job id, or {@code null} if there are none. Submit is ISO-8601, so a lexical
     * comparison of the Submit column is chronological.
     */
    private static String latestStateBySubmit(String stdout) {
        if (stdout == null) {
            return null;
        }
        String bestState = null;
        String bestSubmit = "";
        for (String line : stdout.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\|");
            if (parts.length < 2) {
                continue;
            }
            String state = parts[0].trim();
            String submit = parts[1].trim();
            if (bestState == null || submit.compareTo(bestSubmit) > 0) {
                bestState = state;
                bestSubmit = submit;
            }
        }
        return bestState;
    }

    /**
     * Return the observed {@link JobState}: a terminal state (COMPLETE/FAILED/CANCELED), {@link
     * JobState#ACTIVE} while running, or {@code null} if still pending/in-transition. Prefers {@code
     * sacct} (retains completed jobs); if accounting has no record yet, falls back to the scheduler
     * monitor command ({@code squeue}) and treats its disappearance as completion.
     */
    /** Where a job currently sits in its lifecycle, as observed from the scheduler + accounting. */
    private enum JobLifecycle {
        RUNNING, // executing on the resource
        IN_QUEUE, // pending / requeued / in transition — still within the lifecycle
        COMPLETED, // finished successfully
        CANCELED, // cancelled
        FAILED, // genuine terminal failure (FAILED / TIMEOUT / OUT_OF_MEMORY / DEADLINE)
        INFRA_FAILURE // NODE_FAIL / BOOT_FAIL — slurm requeues these, so not necessarily terminal
    }

    /**
     * Observe where the job is in its lifecycle. Prefers the live scheduler view: a job that is still
     * queued or running — including one slurm has requeued after a node failure — is still in its
     * lifecycle and must keep being tracked. Only once the job has left the scheduler do we read the
     * terminal verdict from {@code sacct} (which retains finished jobs), so a transient NODE_FAIL that
     * slurm requeues no longer aborts the experiment.
     */
    private JobLifecycle observeJob(AgentAdaptor adaptor, String jobId) {
        try {
            JobStatus live = getJobStatus(adaptor, jobId);
            if (live != null && live.getJobState() != JobState.JOB_STATE_UNKNOWN) {
                JobState s = live.getJobState();
                if (s == JobState.ACTIVE) {
                    return JobLifecycle.RUNNING;
                }
                if (s == JobState.COMPLETE) {
                    return JobLifecycle.COMPLETED;
                }
                if (s == JobState.CANCELED) {
                    return JobLifecycle.CANCELED;
                }
                if (s == JobState.FAILED) {
                    return JobLifecycle.FAILED;
                }
                return JobLifecycle.IN_QUEUE; // SUBMITTED / QUEUED / in transition
            }
        } catch (Exception e) {
            logger.warn("Scheduler queue poll failed for job {}: {}", jobId, e.getMessage());
        }

        // Job is no longer in the scheduler queue: read the terminal verdict from accounting.
        try {
            // --duplicates returns every accounting record for this id. slurm reuses job ids across
            // controller restarts (e.g. a devstack reset), so an id can carry stale records from prior
            // sessions; take the State of the most-recently-SUBMITTED record (Submit is ISO-8601, so it
            // sorts chronologically) — otherwise an old CANCELLED/COMPLETED record can masquerade as this
            // job's state.
            CommandOutput out = adaptor.executeCommand("sacct -X -n -P -o State,Submit --duplicates -j " + jobId, null);
            String state = latestStateBySubmit(out.getStdOut());
            if (state == null || state.isEmpty()) {
                // Gone from the queue with no accounting record => it left the queue cleanly.
                logger.info("Job {} no longer visible in scheduler queue; treating as completed", jobId);
                return JobLifecycle.COMPLETED;
            }
            // sacct may suffix cancellations as "CANCELLED by <uid>"
            if (state.startsWith("CANCELLED")) {
                return JobLifecycle.CANCELED;
            }
            switch (state) {
                case "COMPLETED":
                    return JobLifecycle.COMPLETED;
                case "RUNNING":
                case "COMPLETING":
                    return JobLifecycle.RUNNING; // accounting lag behind the scheduler
                case "PENDING":
                case "REQUEUED":
                case "RESIZING":
                case "SUSPENDED":
                case "CONFIGURING":
                    return JobLifecycle.IN_QUEUE;
                case "NODE_FAIL":
                case "BOOT_FAIL":
                    return JobLifecycle.INFRA_FAILURE; // slurm requeues these — not necessarily terminal
                default:
                    // FAILED, TIMEOUT, OUT_OF_MEMORY, DEADLINE, or anything else genuinely terminal.
                    return JobLifecycle.FAILED;
            }
        } catch (Exception e) {
            logger.warn("sacct poll failed for job {}: {}", jobId, e.getMessage());
        }
        return JobLifecycle.IN_QUEUE; // transient error: keep tracking rather than abort
    }

    private void persistJobState(String jobId, JobState state) {
        try {
            JobStatus jobStatus = JobStatus.newBuilder()
                    .setJobState(state)
                    .setReason("Monitoring task observed state " + state)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            // Key by the job's OWNING task id (job submission), not this monitoring task — JobPK is
            // (jobId, taskId) and addJobStatus silently no-ops if the (jobId, taskId) row is not found.
            getRegistryServiceClient().addJobStatus(jobStatus, jobOwningTaskId, jobId);
        } catch (Exception e) {
            logger.warn("Failed to persist job status {} for job {}: {}", state, jobId, e.getMessage());
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
