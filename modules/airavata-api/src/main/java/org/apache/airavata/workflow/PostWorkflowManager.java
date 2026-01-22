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
package org.apache.airavata.workflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.DataStagingTaskModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.dapr.messaging.DaprJobStatusHandler;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.statemachine.JobStateValidator;
import org.apache.airavata.statemachine.StateTransitionService;
import org.apache.airavata.task.OutPort;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.factory.DaprTaskFactory;
import org.apache.airavata.telemetry.CounterMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "airavata.services.postwm", name = "enabled", havingValue = "true")
public class PostWorkflowManager extends WorkflowManager implements DaprJobStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);
    private static final CounterMetric postwfCounter = new CounterMetric("post_wf_counter");

    private final AiravataServerProperties properties;
    private final org.apache.airavata.task.factory.TaskFactory taskFactory;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.springframework.context.ApplicationContext applicationContext;

    private final org.apache.airavata.service.registry.RegistryService registryService;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.apache.airavata.service.profile.UserProfileService userProfileService;

    @SuppressWarnings("unused") // Reserved for future use or parent class compatibility
    private final org.apache.airavata.service.security.CredentialStoreService credentialStoreService;

    private final ThreadPoolTaskExecutor processingPool;

    public PostWorkflowManager(
            AiravataServerProperties properties,
            org.apache.airavata.task.factory.TaskFactory taskFactory,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
            org.apache.airavata.dapr.messaging.DaprMessagingFactory messagingFactory,
            @Qualifier("postWorkflowManagerExecutor") ThreadPoolTaskExecutor processingPool) {
        // Default values, will be updated in @PostConstruct
        super("post-workflow-manager", false, registryService, messagingFactory);
        this.properties = properties;
        this.taskFactory = taskFactory;
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
                "PostWorkflowManager started; Dapr delivers {} to /api/v1/dapr/pubsub/{}",
                org.apache.airavata.dapr.messaging.DaprTopics.MONITORING_JOB_STATUS,
                org.apache.airavata.dapr.messaging.DaprTopics.MONITORING_JOB_STATUS);
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
            List<JobModel> jobs = registryService.getJobs("jobId", jobId);
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
            JobModel jobModel = jobs.get(0);
            ProcessModel processModel = registryService.getProcess(jobModel.getProcessId());
            ExperimentModel experimentModel = registryService.getExperiment(processModel.getExperimentId());
            ProcessStatus processStatus = registryService.getProcessStatus(processModel.getProcessId());

            var processState = processStatus.getState();

            if (experimentModel != null) {
                jobModel.getJobStatuses()
                        .sort(Comparator.comparingLong(JobStatus::getTimeOfStateChange)
                                .reversed());
                JobState currentJobStatus = jobModel.getJobStatuses().get(0).getJobState();
                logger.info("Last known state of job {} is {}", jobId, jobName);

                if (!StateTransitionService.validateAndLog(
                        JobStateValidator.INSTANCE, currentJobStatus, jobState, jobId, "job")) {
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
                saveAndPublishJobStatus(jobId, task, processId, experimentId, gateway, jobState);

                // TODO get cluster lock before that
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
                            publishProcessStatus(processId, experimentId, gateway, ProcessState.CANCELED);
                            break;
                        default:
                            logger.warn("skipping job={}: eid={}, state={}", jobId, experimentId, jobState);
                    }
                } else {
                    logger.info("Job {} is in state={}", jobId, jobState);
                    if (jobState == JobState.COMPLETE || jobState == JobState.FAILED) {
                        // If Job has FAILED, still run output staging tasks to debug the reason for failure. And
                        // update the experiment status as COMPLETED as job failures are unrelated to Airavata scope.
                        logger.info("running PostWorkflow for process {} of experiment {}", processId, experimentId);
                        executePostWorkflow(processId, gateway, false);

                    } else if (jobStatusResult.getState() == JobState.CANCELED) {
                        logger.info("Setting process {} of experiment {} to state=CANCELED", processId, experimentId);
                        publishProcessStatus(processId, experimentId, gateway, ProcessState.CANCELED);
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
        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        ExperimentModel experimentModel;
        DaprTaskFactory taskFactory;
        try {
            processModel = registryService.getProcess(processId);
            var experimentId = processModel.getExperimentId();
            var crId = processModel.getComputeResourceId();
            var grpId = processModel.getGroupResourceProfileId();

            experimentModel = registryService.getExperiment(experimentId);
            ComputeResourceType resourceType = registryService
                    .getGroupComputeResourcePreference(crId, grpId)
                    .getResourceType();

            taskFactory = this.taskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (RegistryException e) {
            logger.error("Failed to fetch experiment/process from registry for pid={}", processId, e);
            throw new Exception("Failed to fetch experiment/process from registry for pid=" + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasks();

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        AiravataTask jobVerificationTask = taskFactory.createJobVerificationTask(processId);
        jobVerificationTask.setGatewayId(experimentModel.getGatewayId());
        jobVerificationTask.setExperimentId(experimentModel.getExperimentId());
        jobVerificationTask.setProcessId(processModel.getProcessId());
        jobVerificationTask.setTaskId("Job-Verification-Task-" + UUID.randomUUID() + "-");
        jobVerificationTask.setForceRunTask(forceRun);
        jobVerificationTask.setSkipAllStatusPublish(true);

        allTasks.add(jobVerificationTask);

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream()
                    .filter(taskModel -> taskModel.getTaskId().equals(taskId))
                    .findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;
                if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (jobSubmissionFound) {
                        var subTaskModel = (DataStagingTaskModel) taskModel.getSubTaskModel();
                        assert subTaskModel != null;
                        switch (subTaskModel.getType()) {
                            case INPUT:
                                // INPUT staging is handled in pre-workflow, skip here
                                break;
                            case OUPUT:
                                airavataTask = taskFactory.createOutputDataStagingTask(processId);
                                airavataTask.setForceRunTask(true);
                                break;
                            case ARCHIVE_OUTPUT:
                                airavataTask = taskFactory.createArchiveTask(processId);
                                airavataTask.setForceRunTask(true);
                                break;
                        }
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    airavataTask.setRetryCount(taskModel.getMaxRetry());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() - 1)
                                .setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

        AiravataTask completingTask = taskFactory.createCompletingTask(processId);
        completingTask.setGatewayId(experimentModel.getGatewayId());
        completingTask.setExperimentId(experimentModel.getExperimentId());
        completingTask.setProcessId(processModel.getProcessId());
        completingTask.setTaskId("Completing-Task-" + UUID.randomUUID() + "-");
        completingTask.setForceRunTask(forceRun);
        completingTask.setSkipAllStatusPublish(true);
        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(completingTask.getTaskId(), completingTask));
        }
        allTasks.add(completingTask);

        AiravataTask parsingTriggeringTask = taskFactory.createParsingTriggeringTask(processId);
        parsingTriggeringTask.setGatewayId(experimentModel.getGatewayId());
        parsingTriggeringTask.setExperimentId(experimentModel.getExperimentId());
        parsingTriggeringTask.setProcessId(processModel.getProcessId());
        parsingTriggeringTask.setTaskId("Parsing-Triggering-Task");
        parsingTriggeringTask.setSkipAllStatusPublish(true);
        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() - 1)
                    .setNextTask(new OutPort(parsingTriggeringTask.getTaskId(), parsingTriggeringTask));
        }
        allTasks.add(parsingTriggeringTask);

        // Note: Dapr Workflow integration is in progress
        // Currently using workflow naming for registration; full Dapr Workflow support pending
        String workflowName = org.apache.airavata.dapr.workflow.WorkflowNaming.postWorkflow(processId);
        registerWorkflowForProcess(processId, workflowName, "POST");
    }

    private void saveAndPublishJobStatus(
            String jobId, String taskId, String processId, String experimentId, String gateway, JobState jobState)
            throws Exception {
        try {

            JobStatus jobStatus = new JobStatus();
            jobStatus.setReason(jobState.name());
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            jobStatus.setJobState(jobState);

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0) {
                jobStatus.setTimeOfStateChange(
                        AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            RegistryService registryService = this.registryService;

            try {
                registryService.addJobStatus(jobStatus, taskId, jobId);

            } catch (RegistryException e) {
                logger.error("Failed to add job status " + jobId, e);
            }

            JobIdentifier identifier = new JobIdentifier(jobId, taskId, processId, experimentId, gateway);

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(
                    jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);

        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }
}
