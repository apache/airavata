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
package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.*;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.impl.task.parsing.ParsingTriggeringTask;
import org.apache.airavata.helix.impl.task.staging.ArchiveTask;
import org.apache.airavata.helix.impl.task.staging.JobVerificationTask;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.monitor.JobStateValidator;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PostWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);
    private final static CountMonitor postwfCounter = new CountMonitor("post_wf_counter");

    private ExecutorService processingPool = Executors.newFixedThreadPool(10);

    public PostWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("post.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("post.workflow.manager.loadbalance.clusters")));
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private Consumer<String, JobStatusResult> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("kafka.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JobStatusResultDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        // Create the consumer using props.
        final Consumer<String, JobStatusResult> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("kafka.broker.topic")));
        return consumer;
    }

    private boolean process(JobStatusResult jobStatusResult) {

        if (jobStatusResult == null) {
            logger.error("Job result is null");
            return false;
        }

        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        try {
            logger.info("Processing job result of job id " + jobStatusResult.getJobId() + " sent by " + jobStatusResult.getPublisherName());

            List<JobModel> jobs = registryClient.getJobs("jobId", jobStatusResult.getJobId());

            if (jobs.size() > 0) {
                logger.info("Filtering total " + jobs.size() + " with target job name " + jobStatusResult.getJobName());
                jobs = jobs.stream().filter(jm -> jm.getJobName().equals(jobStatusResult.getJobName())).collect(Collectors.toList());
            }

            if (jobs.size() != 1) {
                logger.error("Couldn't find exactly one job with id " + jobStatusResult.getJobId() + " and name " +
                        jobStatusResult.getJobName() + " in the registry. Count " + jobs.size());
                getRegistryClientPool().returnResource(registryClient);
                return false;
            }

            JobModel jobModel = jobs.get(0);
            ProcessModel processModel = registryClient.getProcess(jobModel.getProcessId());
            ExperimentModel experimentModel = registryClient.getExperiment(processModel.getExperimentId());
            ProcessStatus processStatus = registryClient.getProcessStatus(processModel.getProcessId());

            getRegistryClientPool().returnResource(registryClient);

            if (processModel != null && experimentModel != null) {

                jobModel.getJobStatuses().sort(Comparator.comparingLong(JobStatus::getTimeOfStateChange).reversed());
                JobState currentJobStatus = jobModel.getJobStatuses().get(0).getJobState();

                logger.info("Last known state of job " + jobModel.getJobId() + " is " + currentJobStatus.name());

                if (!JobStateValidator.isValid(currentJobStatus, jobStatusResult.getState())) {
                    logger.warn("Job state of " + jobStatusResult.getJobId() + " is not valid. Previous state " +
                            currentJobStatus + ", new state " + jobStatusResult.getState());
                    return true;
                }

                String gateway = experimentModel.getGatewayId();
                String processId = processModel.getProcessId();
                String experimentId = experimentModel.getExperimentId();
                String task = jobModel.getTaskId();

                logger.info("Updating the job status for job id : " + jobStatusResult.getJobId() + " with process id "
                        + processId + ", exp id " + experimentId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());

                saveAndPublishJobStatus(jobStatusResult.getJobId(), task, processId, experimentId, gateway, jobStatusResult.getState());

                // TODO get cluster lock before that
                if (ProcessState.CANCELLING.equals(processStatus.getState()) || ProcessState.CANCELED.equals(processStatus.getState())) {
                    logger.info("Cancelled post workflow for process " + processId + " in experiment " + experimentId);
                    // This will mark an cancelling Experiment into a cancelled status for a set of valid job statuses
                    // This is a safety check. Cancellation is originally handled in Job Cancellation Workflow
                    switch (jobStatusResult.getState()) {
                        case FAILED:
                        case SUSPENDED:
                        case CANCELED:
                        case COMPLETE:
                            logger.info("Job " + jobStatusResult.getJobId() + " status is " + jobStatusResult.getState() +
                                    " so marking experiment " + experimentId + " as cancelled" );
                            publishProcessStatus(processId, experimentId, gateway, ProcessState.CANCELED);
                            break;
                        default:
                            logger.warn("Job " + jobStatusResult.getJobId() + " status " + jobStatusResult.getState() +
                                    " is invalid to mark experiment " + experimentId + " as cancelled");
                    }
                } else {

                    if (jobStatusResult.getState() == JobState.COMPLETE || jobStatusResult.getState() == JobState.FAILED) {
                        // if the job is FAILED, still run output staging tasks to debug the reason for failure. And update
                        // the experiment status as COMPLETED as this job failure is not related to Airavata scope.
                        logger.info("Starting the post workflow for job id : " + jobStatusResult.getJobId() + " with process id "
                                + processId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());

                        logger.info("Job " + jobStatusResult.getJobId() + " was completed");

                        executePostWorkflow(processId, gateway, false);

                    } else if (jobStatusResult.getState() == JobState.CANCELED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was externally cancelled but process is not marked as cancelled yet");
                        publishProcessStatus(processId, experimentId,gateway, ProcessState.CANCELED);
                        logger.info("Marked process " + processId + " of experiment " + experimentId + " as cancelled as job is already being cancelled");

                    } else if (jobStatusResult.getState() == JobState.SUBMITTED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was submitted");

                    }
                }
                return true;
            } else {
                logger.warn("Could not find a monitoring register for job id " + jobStatusResult.getJobId());
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to process job : " + jobStatusResult.getJobId() + ", with status : " + jobStatusResult.getState().name(), e);
            getRegistryClientPool().returnBrokenResource(registryClient);
            return false;
        }
    }

    private void executePostWorkflow(String processId, String gateway, boolean forceRun) throws Exception {

        postwfCounter.inc();
        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = registryClient.getProcess(processId);
            experimentModel = registryClient.getExperiment(processModel.getExperimentId());
            getRegistryClientPool().returnResource(registryClient);

        } catch (Exception e) {
            logger.error("Failed to fetch experiment or process from registry associated with process id " + processId, e);
            getRegistryClientPool().returnResource(registryClient);
            throw new Exception("Failed to fetch experiment or process from registry associated with process id " + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasks();

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        JobVerificationTask jobVerificationTask = new JobVerificationTask();
        jobVerificationTask.setGatewayId(experimentModel.getGatewayId());
        jobVerificationTask.setExperimentId(experimentModel.getExperimentId());
        jobVerificationTask.setProcessId(processModel.getProcessId());
        jobVerificationTask.setTaskId("Job-Verification-Task-" + UUID.randomUUID().toString() +"-");
        jobVerificationTask.setForceRunTask(forceRun);
        jobVerificationTask.setSkipAllStatusPublish(true);

        allTasks.add(jobVerificationTask);

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream().filter(taskModel -> taskModel.getTaskId().equals(taskId)).findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;
                if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (jobSubmissionFound) {
                        DataStagingTaskModel subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel(taskModel);
                        assert subTaskModel != null;
                        switch (subTaskModel.getType()) {
                            case OUPUT:
                                airavataTask = new OutputDataStagingTask();
                                airavataTask.setForceRunTask(true);
                                break;
                            case ARCHIVE_OUTPUT:
                                airavataTask = new ArchiveTask();
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
                        allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

        CompletingTask completingTask = new CompletingTask();
        completingTask.setGatewayId(experimentModel.getGatewayId());
        completingTask.setExperimentId(experimentModel.getExperimentId());
        completingTask.setProcessId(processModel.getProcessId());
        completingTask.setTaskId("Completing-Task-" + UUID.randomUUID().toString() +"-");
        completingTask.setForceRunTask(forceRun);
        completingTask.setSkipAllStatusPublish(true);
        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(completingTask.getTaskId(), completingTask));
        }
        allTasks.add(completingTask);

        ParsingTriggeringTask parsingTriggeringTask = new ParsingTriggeringTask();
        parsingTriggeringTask.setGatewayId(experimentModel.getGatewayId());
        parsingTriggeringTask.setExperimentId(experimentModel.getExperimentId());
        parsingTriggeringTask.setProcessId(processModel.getProcessId());
        parsingTriggeringTask.setTaskId("Parsing-Triggering-Task");
        parsingTriggeringTask.setSkipAllStatusPublish(true);
        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(parsingTriggeringTask.getTaskId(), parsingTriggeringTask));
        }
        allTasks.add(parsingTriggeringTask);

        String workflowName = getWorkflowOperator().launchWorkflow(processId + "-POST-" + UUID.randomUUID().toString(),
                new ArrayList<>(allTasks), true, false);

        registerWorkflowForProcess(processId, workflowName, "POST");
    }

    public void startServer() throws Exception {

        init();
        final Consumer<String, JobStatusResult> consumer = createConsumer();
        new Thread(() -> {

            while (true) {

                final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(Long.MAX_VALUE);
                CompletionService<Boolean> executorCompletionService= new ExecutorCompletionService<>(processingPool);
                List<Future<Boolean>> processingFutures = new ArrayList<>();

                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<String, JobStatusResult>> partitionRecords = consumerRecords.records(partition);
                    logger.info("Received job records {}", partitionRecords.size());

                    for (ConsumerRecord<String, JobStatusResult> record : partitionRecords) {
                        logger.info("Submitting {} to process in thread pool", record.value().getJobId());

                        // This avoids kafka read thread to wait until processing is completed before committing
                        // There is a risk of missing 20 messages in case of a restart but this improves the robustness
                        // of the kafka read thread by avoiding wait timeouts
                        processingFutures.add(executorCompletionService.submit(() -> {
                            boolean success = process(record.value());
                            logger.info("Status of processing " + record.value().getJobId() + " : " + success);
                            return success;
                        }));

                        consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                    }
                }

                for (Future<Boolean> f: processingFutures) {
                    try {
                        executorCompletionService.take().get();
                    } catch (Exception e) {
                        logger.error("Failed processing job", e);
                    }
                }
                logger.info("All messages processed. Moving to next round");
            }
        }).start();
    }

    private void saveAndPublishJobStatus(String jobId, String taskId, String processId, String experimentId, String gateway,
                                        JobState jobState) throws Exception {
        try {

            JobStatus jobStatus = new JobStatus();
            jobStatus.setReason(jobState.name());
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            jobStatus.setJobState(jobState);

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0 ) {
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            RegistryService.Client registryClient = getRegistryClientPool().getResource();

            try {
                registryClient.addJobStatus(jobStatus, taskId, jobId);
                getRegistryClientPool().returnResource(registryClient);

            } catch (Exception e) {
                logger.error("Failed to add job status " + jobId, e);
                getRegistryClientPool().returnBrokenResource(registryClient);
            }

            JobIdentifier identifier = new JobIdentifier(jobId, taskId, processId, experimentId, gateway);

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
                    (MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);

        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    public void stopServer() {

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
}
