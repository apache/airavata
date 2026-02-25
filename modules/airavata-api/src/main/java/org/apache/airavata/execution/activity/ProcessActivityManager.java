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
package org.apache.airavata.execution.activity;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.Comparator;
import java.util.UUID;
import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.execution.monitoring.JobStatusResult;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.status.service.StatusService;
import org.apache.airavata.execution.state.StateValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Unified manager for the full process workflow lifecycle.
 *
 * <p>Handles launching pre-execution, post-execution, and cancellation workflows
 * via Temporal, and receives job status callbacks from the monitoring system.
 *
 * <p>Replaces the former PreWorkflowManager and PostWorkflowManager.
 */
@Component
@Profile({"!test", "orchestrator-integration"})
@ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
public class ProcessActivityManager implements JobStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessActivityManager.class);
    private static final CounterMetric prewfCounter = new CounterMetric("pre_wf_counter");
    private static final CounterMetric postwfCounter = new CounterMetric("post_wf_counter");

    private final ProcessService processService;
    private final ExperimentService experimentService;
    private final StatusService statusService;
    private final JobService jobService;
    private final WorkflowClient workflowClient;

    public ProcessActivityManager(
            ProcessService processService,
            ExperimentService experimentService,
            StatusService statusService,
            JobService jobService,
            WorkflowClient workflowClient) {
        this.processService = processService;
        this.experimentService = experimentService;
        this.statusService = statusService;
        this.jobService = jobService;
        this.workflowClient = workflowClient;
    }

    // -------------------------------------------------------------------------
    // Pre-workflow launch
    // -------------------------------------------------------------------------

    public String launchPreWorkflow(String processId, boolean forceRun) throws Exception {
        prewfCounter.inc();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = processService.getProcess(processId);
            experimentModel = experimentService.getExperiment(processModel.getExperimentId());
        } catch (Exception e) {
            logger.error("Failed to fetch experiment or process from registry for process id {}", processId, e);
            throw new Exception("Failed to fetch experiment or process from registry for process id " + processId, e);
        }

        statusService.addProcessStatus(StatusModel.of(ProcessState.LAUNCHED), processId);

        String workflowId = String.format("%s-PRE-%s", processId, UUID.randomUUID());
        var options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(ProcessActivity.TASK_QUEUE)
                .build();
        var workflow = workflowClient.newWorkflowStub(ProcessActivity.PreWf.class, options);

        logger.info("Launching PreWorkflow {} for process {}", workflowId, processId);
        WorkflowClient.start(
                workflow::execute,
                new ProcessActivity.PreInput(
                        processId, experimentModel.getExperimentId(), experimentModel.getGatewayId(), null));
        return workflowId;
    }

    // -------------------------------------------------------------------------
    // Cancel workflow launch
    // -------------------------------------------------------------------------

    public String launchCancelWorkflow(String processId, String gateway) throws Exception {
        ProcessModel processModel;
        try {
            processModel = processService.getProcess(processId);
        } catch (Exception e) {
            logger.error("Failed to fetch process from registry for process id {}", processId, e);
            throw new Exception("Failed to fetch process from registry for process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();
        String workflowId = String.format("%s-CANCEL-%s", processId, UUID.randomUUID());
        var options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(ProcessActivity.TASK_QUEUE)
                .build();
        var workflow = workflowClient.newWorkflowStub(ProcessActivity.CancelWf.class, options);

        logger.info("Launching CancelWorkflow {} for process {}", workflowId, processId);
        WorkflowClient.start(workflow::execute, new ProcessActivity.CancelInput(processId, experimentId, gateway));
        return workflowId;
    }

    // -------------------------------------------------------------------------
    // Job status callback (from monitoring system)
    // -------------------------------------------------------------------------

    @Override
    public void onJobStatusMessage(JobStatusResult message) {
        try {
            boolean success = process(message);
            logger.info("Status of processing {} : {}", message.getJobId(), success);
        } catch (Exception e) {
            logger.error("Error processing job status for job {}", message.getJobId(), e);
        }
    }

    private boolean process(JobStatusResult jobStatusResult) {
        if (jobStatusResult == null) {
            logger.error("Job result is null");
            return false;
        }

        var jobId = jobStatusResult.getJobId();
        var jobName = jobStatusResult.getJobName();
        var jobState = jobStatusResult.getState();
        logger.info(
                "processing JobStatusUpdate<{}> from {}: {}",
                jobId,
                jobStatusResult.getPublisherName(),
                jobStatusResult);

        try {
            var jobModel = resolveUniqueJob(jobId, jobName);
            if (jobModel == null) return false;

            var processModel = processService.getProcess(jobModel.getProcessId());
            if (processModel == null) {
                logger.error("Process not found for job {}", jobId);
                return false;
            }
            var experimentModel = experimentService.getExperiment(processModel.getExperimentId());
            if (experimentModel == null) {
                logger.warn("Could not find experiment for job id {}", jobId);
                return false;
            }

            var processStatus = statusService.getLatestProcessStatus(processModel.getProcessId());
            var processState = processStatus != null ? processStatus.getState() : null;

            // Validate job state transition
            var jobStatuses = jobModel.getJobStatuses();
            JobState currentJobState = null;
            if (jobStatuses != null && !jobStatuses.isEmpty()) {
                jobStatuses.sort(Comparator.comparingLong(StatusModel<JobState>::getTimeOfStateChange)
                        .reversed());
                currentJobState = jobStatuses.get(0).getState();
            }

            if (!StateValidators.StateTransitionService.validateAndLog(
                    StateValidators.JobStateValidator.INSTANCE, currentJobState, jobState, jobId, "job")) {
                return true;
            }

            String processId = processModel.getProcessId();
            String gateway = experimentModel.getGatewayId();
            String experimentId = experimentModel.getExperimentId();

            logger.info(
                    "saving JobStatusUpdate<{}>: pid={}, eid={}, gw={}, state={}",
                    jobId,
                    processId,
                    experimentId,
                    gateway,
                    jobState);

            saveJobStatus(jobId, jobState);

            if (isCancellingOrCanceled(processState) && isTerminalJobState(jobState)) {
                logger.info("canceled job={}: eid={}, state={}", jobId, experimentId, jobState);
                statusService.addProcessStatus(StatusModel.of(ProcessState.CANCELED), processId);
            } else if (jobState == JobState.COMPLETED || jobState == JobState.FAILED) {
                advanceToExecutingIfNeeded(processId, experimentId, gateway, processState);
                logger.info("running PostWorkflow for process {} of experiment {}", processId, experimentId);
                executePostWorkflow(processId, gateway);
            } else if (jobState == JobState.CANCELED) {
                logger.info("Setting process {} of experiment {} to state=CANCELED", processId, experimentId);
                statusService.addProcessStatus(StatusModel.of(ProcessState.CANCELED), processId);
            }

            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to process job: {}, with status : {}",
                    jobStatusResult.getJobId(),
                    jobStatusResult.getState().name(),
                    e);
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private JobModel resolveUniqueJob(String jobId, String jobName) throws RegistryException {
        var jobs = jobService.getJobs("jobId", jobId);
        if (!jobs.isEmpty()) {
            jobs = jobs.stream().filter(jm -> jm.getJobName().equals(jobName)).toList();
        }
        if (jobs.size() != 1) {
            logger.error("Found {} job(s) in registry with id={} and name={}", jobs.size(), jobId, jobName);
            return null;
        }
        return jobs.get(0);
    }

    private static boolean isCancellingOrCanceled(ProcessState state) {
        return state == ProcessState.CANCELING || state == ProcessState.CANCELED;
    }

    private static boolean isTerminalJobState(JobState state) {
        return state == JobState.FAILED
                || state == JobState.SUSPENDED
                || state == JobState.CANCELED
                || state == JobState.COMPLETED;
    }

    private void advanceToExecutingIfNeeded(
            String processId, String experimentId, String gateway, ProcessState processState) throws Exception {
        if (processState != null
                && processState != ProcessState.EXECUTING
                && processState != ProcessState.COMPLETED
                && processState != ProcessState.FAILED
                && processState != ProcessState.CANCELED) {
            logger.info("Advancing process {} from {} to EXECUTING before PostWorkflow", processId, processState);
            statusService.addProcessStatus(StatusModel.of(ProcessState.EXECUTING), processId);
        }
    }

    private void executePostWorkflow(String processId, String gateway) throws Exception {
        postwfCounter.inc();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = processService.getProcess(processId);
            experimentModel = experimentService.getExperiment(processModel.getExperimentId());
        } catch (Exception e) {
            logger.error("Failed to fetch experiment/process from registry for pid={}", processId, e);
            throw new Exception("Failed to fetch experiment/process from registry for pid=" + processId, e);
        }

        String workflowId = String.format("%s-POST-%s", processId, UUID.randomUUID());
        var options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(ProcessActivity.TASK_QUEUE)
                .build();
        var workflow = workflowClient.newWorkflowStub(ProcessActivity.PostWf.class, options);

        logger.info("Launching PostWorkflow {} for process {}", workflowId, processId);
        WorkflowClient.start(
                workflow::execute,
                new ProcessActivity.PostInput(processId, experimentModel.getExperimentId(), gateway, false));
    }

    private void saveJobStatus(String jobId, JobState jobState) throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(jobState, jobState.name());
        try {
            statusService.addJobStatus(jobStatus, jobId);
        } catch (RegistryException e) {
            throw new Exception("Failed to save job status for " + jobId, e);
        }
    }
}
