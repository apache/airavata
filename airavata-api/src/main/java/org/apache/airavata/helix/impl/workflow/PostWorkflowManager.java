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
package org.apache.airavata.helix.impl.workflow;

import java.util.*;
import java.util.concurrent.*;
import org.apache.airavata.api.thrift.util.ThriftUtils;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.HelixTaskFactory;
import org.apache.airavata.helix.impl.task.TaskFactory;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.monitor.JobStateValidator;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.monitor.platform.CountMonitor;
import org.apache.airavata.monitor.platform.MonitoringServer;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.ServiceFactory;
import org.apache.airavata.service.ServiceFactoryException;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostWorkflowManager extends WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);
    private static final CountMonitor postwfCounter = new CountMonitor("post_wf_counter");

    private final ExecutorService processingPool = Executors.newFixedThreadPool(10);

    public PostWorkflowManager() throws ApplicationSettingsException {
        super(
                ServerSettings.getSetting("post.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("post.workflow.manager.loadbalance.clusters")));
    }

    public static void main(String[] args) throws Exception {

        if (ServerSettings.getBooleanSetting("post.workflow.manager.monitoring.enabled")) {
            MonitoringServer monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("post.workflow.manager.monitoring.host"),
                    ServerSettings.getIntSetting("post.workflow.manager.monitoring.port"));
            monitoringServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        PostWorkflowManager postManager = new PostWorkflowManager();
        postManager.startServer();
    }

    private void init() throws Exception {
        super.initComponents();
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

        RegistryService registryService;
        try {
            registryService = org.apache.airavata.config.RegistryServiceProvider.getInstance();
        } catch (Exception e) {
            // Fallback to ServiceFactory for backward compatibility
            try {
                registryService = ServiceFactory.getInstance().getRegistryService();
            } catch (ServiceFactoryException ex) {
                logger.error("Failed to get RegistryService from ServiceFactory", ex);
                return false;
            }
        }

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
        RegistryService registryService = getRegistryService();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        HelixTaskFactory taskFactory;
        try {
            processModel = registryService.getProcess(processId);
            var experimentId = processModel.getExperimentId();
            var crId = processModel.getComputeResourceId();
            var grpId = processModel.getGroupResourceProfileId();

            experimentModel = registryService.getExperiment(experimentId);
            ResourceType resourceType = registryService
                    .getGroupComputeResourcePreference(crId, grpId)
                    .getResourceType();

            taskFactory = TaskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (RegistryServiceException e) {
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

        String workflowName = getWorkflowOperator()
                .launchWorkflow(processId + "-POST-" + UUID.randomUUID(), new ArrayList<>(allTasks), true, false);

        registerWorkflowForProcess(processId, workflowName, "POST");
    }

    public void startServer() throws Exception {

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

            RegistryService registryService = getRegistryService();

            try {
                registryService.addJobStatus(jobStatus, taskId, jobId);

            } catch (RegistryServiceException e) {
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

    public void stopServer() {}
}
