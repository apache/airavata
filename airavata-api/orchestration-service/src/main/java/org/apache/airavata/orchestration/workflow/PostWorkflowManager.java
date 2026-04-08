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
package org.apache.airavata.orchestration.workflow;

import java.util.*;
import java.util.concurrent.*;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.messaging.event.proto.JobIdentifier;
import org.apache.airavata.model.messaging.event.proto.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.task.proto.DataStagingTaskModel;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.orchestration.infrastructure.HelixTaskFactory;
import org.apache.airavata.orchestration.infrastructure.TaskFactory;
import org.apache.airavata.orchestration.task.JobStateValidator;
import org.apache.airavata.orchestration.task.JobStatusResult;
import org.apache.airavata.orchestration.task.JobStatusResultDeserializer;
import org.apache.airavata.server.CountMonitor;
import org.apache.airavata.server.IServer;
import org.apache.airavata.task.AiravataTask;
import org.apache.airavata.task.OutPort;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ThriftUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostWorkflowManager implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);
    private static final CountMonitor postwfCounter = new CountMonitor("post_wf_counter");

    private final WorkflowManager wfManager;
    private final ExecutorService processingPool = Executors.newFixedThreadPool(10);
    private IServer.ServerStatus status = IServer.ServerStatus.STOPPED;

    public PostWorkflowManager() throws ApplicationSettingsException {
        wfManager = new WorkflowManager(
                ServerSettings.getSetting("post.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("post.workflow.manager.loadbalance.clusters")));
    }

    public static void main(String[] args) throws Exception {

        PostWorkflowManager postManager = new PostWorkflowManager();
        postManager.run();
    }

    private void init() throws Exception {
        wfManager.initComponents();
    }

    private Consumer<String, JobStatusResult> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("job.monitor.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JobStatusResultDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        // Create the consumer using props.
        final Consumer<String, JobStatusResult> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("job.monitor.broker.topic")));
        return consumer;
    }

    private boolean process(JobStatusResult jobStatusResult) {

        if (jobStatusResult == null) {
            logger.error("Job result is null");
            return false;
        }

        RegistryHandler registryHandler = wfManager.getRegistryHandler();

        var jobId = jobStatusResult.getJobId();
        var jobName = jobStatusResult.getJobName();
        var jobState = jobStatusResult.getState();
        var publisherId = jobStatusResult.getPublisherName();
        logger.info("processing JobStatusUpdate<{}> from {}: {}", jobId, publisherId, jobStatusResult);

        try {
            List<JobModel> jobs = registryHandler.getJobs("jobId", jobId);
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
            ProcessModel processModel = registryHandler.getProcess(jobModel.getProcessId());
            ExperimentModel experimentModel = registryHandler.getExperiment(processModel.getExperimentId());
            ProcessStatus processStatus = registryHandler.getProcessStatus(processModel.getProcessId());

            var processState = processStatus.getState();

            if (experimentModel != null) {
                jobModel.getJobStatusesList()
                        .sort(Comparator.comparingLong(JobStatus::getTimeOfStateChange)
                                .reversed());
                JobState currentJobStatus = jobModel.getJobStatusesList().get(0).getJobState();
                logger.info("Last known state of job {} is {}", jobId, jobName);

                if (!JobStateValidator.isValid(currentJobStatus, jobState)) {
                    logger.warn("JobStatusUpdate<{}> invalid. prev={} -> new={}", jobId, currentJobStatus, jobState);
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
                if (ProcessState.PROCESS_STATE_CANCELLING.equals(processState)
                        || ProcessState.PROCESS_STATE_CANCELED.equals(processState)) {
                    logger.info("Cancelled post workflow for process {} in experiment {}", processId, experimentId);
                    // This will mark a canceling Experiment with CANCELED status for a set of valid job statuses
                    // This is a safety check. Cancellation is originally handled in Job Cancellation Workflow
                    switch (jobState) {
                        case FAILED:
                        case SUSPENDED:
                        case CANCELED:
                        case COMPLETE:
                            logger.info("canceled job={}: eid={}, state={}", jobId, experimentId, jobState);
                            wfManager.publishProcessStatus(
                                    processId, experimentId, gateway, ProcessState.PROCESS_STATE_CANCELED);
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
                        wfManager.publishProcessStatus(
                                processId, experimentId, gateway, ProcessState.PROCESS_STATE_CANCELED);
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
        RegistryHandler registryHandler = wfManager.getRegistryHandler();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        HelixTaskFactory taskFactory;
        try {
            processModel = registryHandler.getProcess(processId);
            var experimentId = processModel.getExperimentId();
            var crId = processModel.getComputeResourceId();
            var grpId = processModel.getGroupResourceProfileId();

            experimentModel = registryHandler.getExperiment(experimentId);
            ResourceType resourceType = registryHandler
                    .getGroupComputeResourcePreference(crId, grpId)
                    .getResourceType();

            taskFactory = TaskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (Exception e) {
            logger.error("Failed to fetch experiment/process from registry for pid={}", processId, e);
            throw new Exception("Failed to fetch experiment/process from registry for pid=" + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasksList();

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
                        DataStagingTaskModel subTaskModel =
                                (DataStagingTaskModel) ThriftUtils.getSubTaskModel(taskModel);
                        assert subTaskModel != null;
                        switch (subTaskModel.getType()) {
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

        String workflowName = wfManager
                .getWorkflowOperator()
                .launchWorkflow(processId + "-POST-" + UUID.randomUUID(), new ArrayList<>(allTasks), true, false);

        wfManager.registerWorkflowForProcess(processId, workflowName, "POST");
    }

    @Override
    public void run() {
        status = ServerStatus.STARTED;
        try {
            startServer();
        } catch (Exception e) {
            logger.error("PostWorkflowManager failed", e);
            status = ServerStatus.FAILED;
        }
    }

    private void startServer() throws Exception {

        init();
        final Consumer<String, JobStatusResult> consumer = createConsumer();
        new Thread(() -> {
                    while (true) {
                        final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(Long.MAX_VALUE);
                        var executorCompletionService = new ExecutorCompletionService<>(processingPool);
                        var processingFutures = new ArrayList<>();

                        for (var topicPartition : consumerRecords.partitions()) {
                            var partitionRecords = consumerRecords.records(topicPartition);
                            logger.info("Received job records {}", partitionRecords.size());

                            for (var record : partitionRecords) {
                                var topic = topicPartition.topic();
                                var partition = topicPartition.partition();
                                var key = record.key();
                                var value = record.value();
                                logger.info("received post on {}/{}: {}->{}", topic, partition, key, value);
                                logger.info(
                                        "Submitting {} to process in thread pool",
                                        record.value().getJobId());

                                // This avoids kafka read thread to wait until processing is completed before committing
                                // There is a risk of missing 20 messages in case of a restart, but this improves the
                                // robustness of the kafka read thread by avoiding wait timeouts
                                processingFutures.add(executorCompletionService.submit(() -> {
                                    boolean success = process(record.value());
                                    logger.info(
                                            "Status of processing {} : {}",
                                            record.value().getJobId(),
                                            success);
                                    return success;
                                }));

                                consumer.commitSync(Collections.singletonMap(
                                        topicPartition, new OffsetAndMetadata(record.offset() + 1)));
                            }
                        }

                        for (var f : processingFutures) {
                            try {
                                executorCompletionService.take().get();
                            } catch (Exception e) {
                                logger.error("Failed processing job", e);
                            }
                        }
                        logger.info("All messages processed. Moving to next round");
                    }
                })
                .start();
    }

    private void saveAndPublishJobStatus(
            String jobId, String taskId, String processId, String experimentId, String gateway, JobState jobState)
            throws Exception {
        try {

            long now = AiravataUtils.getCurrentTimestamp().getTime();
            JobStatus jobStatus = JobStatus.newBuilder()
                    .setReason(jobState.name())
                    .setTimeOfStateChange(now)
                    .setJobState(jobState)
                    .build();

            try {
                wfManager.getRegistryHandler().addJobStatus(jobStatus, taskId, jobId);
            } catch (Exception e) {
                logger.error("Failed to add job status " + jobId, e);
            }

            JobIdentifier identifier = JobIdentifier.newBuilder()
                    .setJobId(jobId)
                    .setTaskId(taskId)
                    .setProcessId(processId)
                    .setExperimentId(experimentId)
                    .setGatewayId(gateway)
                    .build();

            JobStatusChangeEvent jobStatusChangeEvent = JobStatusChangeEvent.newBuilder()
                    .setState(jobStatus.getJobState())
                    .setJobIdentity(identifier)
                    .build();
            MessageContext msgCtx = new MessageContext(
                    jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            wfManager.getStatusPublisher().publish(msgCtx);

        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "post_workflow_manager";
    }

    @Override
    public void stop() throws Exception {
        status = ServerStatus.STOPPING;
        status = ServerStatus.STOPPED;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }
}
