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
package org.apache.airavata.workflow.process.post;

import io.dapr.workflows.client.DaprWorkflowClient;
import java.util.Comparator;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.orchestrator.JobStatusHandler;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.orchestrator.state.StateValidators;
import org.apache.airavata.orchestrator.state.StateModel;
import org.apache.airavata.orchestrator.state.StateModel;
import org.apache.airavata.orchestrator.state.StateModel;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.airavata.workflow.common.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "airavata.services.postwm", name = "enabled", havingValue = "true")
public class PostWorkflowManager extends WorkflowManager implements JobStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);
    private static final CounterMetric postwfCounter = new CounterMetric("post_wf_counter");

    private final AiravataServerProperties properties;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.springframework.context.ApplicationContext applicationContext;

    private final org.apache.airavata.service.registry.RegistryService registryService;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.apache.airavata.service.profile.UserProfileService userProfileService;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.apache.airavata.service.security.CredentialStoreService credentialStoreService;

    private final ThreadPoolTaskExecutor processingPool;

    @Autowired(required = false)
    private DaprWorkflowClient workflowClient;

    @Autowired(required = false)
    private StateModel.StateManager stateManager;

    public PostWorkflowManager(
            AiravataServerProperties properties,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            MessagingFactory messagingFactory,
            @Qualifier("postWorkflowManagerExecutor") ThreadPoolTaskExecutor processingPool,
            ProcessStatusUpdater statusUpdateHelper) {
        // Default values, will be updated in @PostConstruct
        super("post-workflow-manager", false, registryService, messagingFactory, statusUpdateHelper);
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.registryService = registryService;
        this.userProfileService = userProfileService;
        this.credentialStoreService = credentialStoreService;
        this.processingPool = processingPool;
    }

    @jakarta.annotation.PostConstruct
    public void initWorkflowManager() {
        if (properties != null) {
            this.workflowManagerName = this.getClass().getSimpleName();
            this.loadBalanceClusters = properties.services().postwm().loadBalanceClusters();
        }
    }

    @Override
    public String getServerName() {
        return "Post Workflow Manager";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 30; // Start after parser
    }

    @Override
    public void onJobStatusMessage(JobStatusResult message) {
        try {
            boolean success = process(message);
            logger.info("Status of processing {} : {}", message.getJobId(), success);
        } catch (Exception e) {
            logger.error("Error processing job status for job {}", message.getJobId(), e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        init();
        logger.info(
                "PostWorkflowManager started; job-status via JobStatusHandler (direct invoke from AbstractMonitor)");
    }

    @Override
    protected void doStop() throws Exception {
        if (processingPool != null) {
            processingPool.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process(JobStatusResult jobStatusResult) {

        if (jobStatusResult == null) {
            logger.error("Job result is null");
            return false;
        }

        RegistryService registryService = this.registryService;

        var jobId = jobStatusResult.getJobId();
        var jobName = jobStatusResult.getJobName();
        var jobState = jobStatusResult.getState();
        var publisherId = jobStatusResult.getPublisherName();
        logger.info("processing JobStatusUpdate<{}> from {}: {}", jobId, publisherId, jobStatusResult);

        try {
            var jobs = registryService.getJobs("jobId", jobId);
            logger.info("Found {} jobs in registry with id={}", jobs.size(), jobId);
            if (!jobs.isEmpty()) {
                jobs = jobs.stream()
                        .filter(jm -> jm.getJobName().equals(jobName))
                        .toList();
                logger.info("Found {} jobs in registry with id={} and name={}", jobs.size(), jobId, jobName);
            }
            if (jobs.size() != 1) {
                logger.error("Found {} job(s) in registry with id={} and name={}", jobs.size(), jobId, jobName);
                return false;
            }
            var jobModel = jobs.get(0);
            var processModel = registryService.getProcess(jobModel.getProcessId());
            var experimentModel = registryService.getExperiment(processModel.getExperimentId());
            var processStatus = registryService.getProcessStatus(processModel.getProcessId());

            var processState = processStatus.getState();

            if (experimentModel != null) {
                jobModel.getJobStatuses()
                        .sort(Comparator.comparingLong(JobStatus::getTimeOfStateChange)
                                .reversed());
                var currentJobStatus = jobModel.getJobStatuses().get(0).getJobState();
                logger.info("Last known state of job {} is {}", jobId, jobName);

                if (!StateModel.StateTransitionService.validateAndLog(
                        StateValidators.JobStateValidator.INSTANCE, currentJobStatus, jobState, jobId, "job")) {
                    return true;
                }

                String task = jobModel.getTaskId();
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

                // Acquire distributed lock for process to prevent concurrent status updates
                String lockKey = StateModel.StateKeys.processLock(processId);
                boolean lockAcquired = acquireProcessLock(lockKey, processId);
                try {
                    saveAndPublishJobStatus(jobId, task, processId, experimentId, gateway, jobState);

                    if (ProcessState.CANCELLING.equals(processState) || ProcessState.CANCELED.equals(processState)) {
                        logger.info("Cancelled post workflow for process {} in experiment {}", processId, experimentId);
                        // This will mark a canceling Experiment with CANCELED status for a set of valid job statuses
                        // This is a safety check. Cancellation is originally handled in Job Cancellation Workflow
                        switch (jobState) {
                            case FAILED:
                            case SUSPENDED:
                            case CANCELED:
                            case COMPLETE:
                                logger.info("canceled job={}: eid={}, state={}", jobId, experimentId, jobState);
                                updateProcessStatus(processId, experimentId, gateway, ProcessState.CANCELED);
                                break;
                            default:
                                logger.warn("skipping job={}: eid={}, state={}", jobId, experimentId, jobState);
                        }
                    } else {
                        logger.info("Job {} is in state={}", jobId, jobState);
                        if (jobState == JobState.COMPLETE || jobState == JobState.FAILED) {
                            // If Job has FAILED, still run output staging tasks to debug the reason for failure. And
                            // update the experiment status as COMPLETED as job failures are unrelated to Airavata
                            // scope.
                            logger.info(
                                    "running PostWorkflow for process {} of experiment {}", processId, experimentId);
                            executePostWorkflow(processId, gateway, false);

                        } else if (jobStatusResult.getState() == JobState.CANCELED) {
                            logger.info(
                                    "Setting process {} of experiment {} to state=CANCELED", processId, experimentId);
                            updateProcessStatus(processId, experimentId, gateway, ProcessState.CANCELED);
                        }
                    }
                } finally {
                    // Release the distributed lock
                    if (lockAcquired) {
                        releaseProcessLock(lockKey, processId);
                    }
                }
                return true;
            } else {
                logger.warn("Could not find a monitoring register for job id {}", jobId);
                return false;
            }
        } catch (Exception e) {
            logger.error(
                    "Failed to process job: {}, with status : {}",
                    jobStatusResult.getJobId(),
                    jobStatusResult.getState().name(),
                    e);
            return false;
        }
    }

    private void executePostWorkflow(String processId, String gateway, boolean forceRun) throws Exception {

        postwfCounter.inc();
        var regService = this.registryService;

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = regService.getProcess(processId);
            var experimentId = processModel.getExperimentId();
            experimentModel = regService.getExperiment(experimentId);
        } catch (RegistryException e) {
            logger.error("Failed to fetch experiment/process from registry for pid={}", processId, e);
            throw new Exception("Failed to fetch experiment/process from registry for pid=" + processId, e);
        }

        // Use durable workflow instead of building task chains
        if (workflowClient != null) {
            try {
                // Create ProcessPostWorkflowInput
                org.apache.airavata.workflow.process.post.ProcessPostWorkflowInput input =
                        new org.apache.airavata.workflow.process.post.ProcessPostWorkflowInput(
                                processId, experimentModel.getExperimentId(), gateway, forceRun);

                // Schedule the workflow (returns instance ID)
                logger.info("Scheduling ProcessPostWorkflow for process {}", processId);
                String workflowInstanceId = workflowClient.scheduleNewWorkflow(ProcessPostWorkflow.class, input);

                // Register the workflow instance ID with the process
                registerWorkflowForProcess(
                        processId,
                        workflowInstanceId,
                        org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.TYPE_POST);

                logger.info(
                        "Successfully scheduled ProcessPostWorkflow {} for process {}", workflowInstanceId, processId);

            } catch (Exception e) {
                logger.error("Failed to schedule ProcessPostWorkflow for process {}", processId, e);
                throw new Exception("Failed to schedule ProcessPostWorkflow for process " + processId, e);
            }
        } else {
            // Fallback: log warning if workflow client is not available
            logger.warn(
                    "Workflow client not available; cannot launch post workflow for process {}. "
                            + "Enable airavata.services.controller.enabled=true",
                    processId);
            // Still register workflow name for compatibility
            String workflowName =
                    org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.postWorkflow(processId);
            registerWorkflowForProcess(processId, workflowName, "POST");
        }
    }

    private void saveAndPublishJobStatus(
            String jobId, String taskId, String processId, String experimentId, String gateway, JobState jobState)
            throws Exception {
        try {

            var jobStatus = new JobStatus();
            jobStatus.setReason(jobState.name());
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            jobStatus.setJobState(jobState);

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0) {
                jobStatus.setTimeOfStateChange(
                        AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            var regService = this.registryService;

            try {
                regService.addJobStatus(jobStatus, taskId, jobId);
            } catch (RegistryException e) {
                logger.error("Failed to add job status " + jobId, e);
            }

        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Acquire a distributed lock for a process to prevent concurrent status updates.
     *
     * @param lockKey The lock key (generated using StateModel.StateKeys.processLock)
     * @param processId The process ID (for logging)
     * @return true if lock was acquired, false otherwise
     */
    private boolean acquireProcessLock(String lockKey, String processId) {
        if (stateManager == null || !stateManager.isAvailable()) {
            logger.debug("State manager not available; proceeding without lock for process {}", processId);
            return false;
        }

        try {
            // Try to check if lock exists
            boolean lockExists = stateManager.exists(lockKey);
            if (lockExists) {
                logger.warn(
                        "Process lock already exists for process {}; another instance may be updating status",
                        processId);
                // In a production system, you might want to wait or check lock age
                // For now, we'll proceed anyway to avoid blocking (graceful degradation)
                return false;
            }

            // Create the lock with a timestamp
            String lockValue = String.valueOf(System.currentTimeMillis());
            stateManager.saveState(lockKey, lockValue);
            logger.debug("Acquired process lock for process {}", processId);
            return true;
        } catch (org.apache.airavata.common.exception.CoreExceptions.AiravataException e) {
            logger.warn("Failed to acquire process lock for process {}: {}", processId, e.getMessage());
            // Graceful degradation: proceed without lock
            return false;
        }
    }

    /**
     * Release a distributed lock for a process.
     *
     * @param lockKey The lock key
     * @param processId The process ID (for logging)
     */
    private void releaseProcessLock(String lockKey, String processId) {
        if (stateManager == null || !stateManager.isAvailable()) {
            return;
        }

        try {
            stateManager.deleteState(lockKey);
            logger.debug("Released process lock for process {}", processId);
        } catch (org.apache.airavata.common.exception.CoreExceptions.AiravataException e) {
            logger.warn("Failed to release process lock for process {}: {}", processId, e.getMessage());
            // Non-critical error - lock will expire or be cleaned up
        }
    }
}
