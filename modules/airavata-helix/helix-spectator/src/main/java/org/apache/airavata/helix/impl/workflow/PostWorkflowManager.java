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
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.impl.task.*;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.impl.task.staging.ArchiveTask;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.model.status.ProcessState;
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
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PostWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(PostWorkflowManager.class);

    public PostWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("post.workflow.manager.name"));
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

        try {
            logger.info("Processing job result of job id " + jobStatusResult.getJobId() + " sent by " + jobStatusResult.getPublisherName());

            if (MonitoringUtil.hasMonitoringRegistered(getCuratorClient(), jobStatusResult.getJobId())) {

                JobState currentJobStatus = MonitoringUtil.getCurrentStatusOfJob(getCuratorClient(), jobStatusResult.getJobId());
                if (!JobStateValidator.isValid(currentJobStatus, jobStatusResult.getState())) {
                    logger.warn("Job state of " + jobStatusResult.getJobId() + " is not valid. Previous state " +
                            currentJobStatus + ", new state " + jobStatusResult.getState());
                    return true;
                }

                String gateway = Optional.ofNullable(MonitoringUtil.getGatewayByJobId(getCuratorClient(), jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the gateway for job id " + jobStatusResult.getJobId()));

                String processId = Optional.ofNullable(MonitoringUtil.getProcessIdByJobId(getCuratorClient(), jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the process for job id " + jobStatusResult.getJobId()));

                String experimentId = Optional.ofNullable(MonitoringUtil.getExperimentIdByJobId(getCuratorClient(), jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the experiment for job id " + jobStatusResult.getJobId()));

                String task = Optional.ofNullable(MonitoringUtil.getTaskIdByJobId(getCuratorClient(), jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the task for job id " + jobStatusResult.getJobId()));

                String processStatus = MonitoringUtil.getStatusOfProcess(getCuratorClient(), processId);

                logger.info("Updating the job status for job id : " + jobStatusResult.getJobId() + " with process id "
                        + processId + ", exp id " + experimentId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());

                saveAndPublishJobStatus(jobStatusResult.getJobId(), task, processId, experimentId, gateway, jobStatusResult.getState());

                // TODO get cluster lock before that
                if (MonitoringUtil.CANCEL.equals(processStatus)) {
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
                                                break;
                                            case ARCHIVE_OUTPUT:
                                                airavataTask = new ArchiveTask();
                                                break;
                                        }
                                    }
                                }

                                if (airavataTask != null) {
                                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                                    airavataTask.setProcessId(processModel.getProcessId());
                                    airavataTask.setTaskId(taskModel.getTaskId());
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
                        completingTask.setTaskId("Completing-Task");
                        completingTask.setSkipTaskStatusPublish(true);
                        if (allTasks.size() > 0) {
                            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(completingTask.getTaskId(), completingTask));
                        }
                        allTasks.add(completingTask);

                        String workflowName = getWorkflowOperator().launchWorkflow(processId + "-POST-" + UUID.randomUUID().toString(),
                                new ArrayList<>(allTasks), true, false);
                        try {
                            MonitoringUtil.registerWorkflow(getCuratorClient(), processId, workflowName);
                        } catch (Exception e) {
                            logger.error("Failed to save workflow " + workflowName + " of process " + processId + " in zookeeper registry. " +
                                    "This will affect cancellation tasks", e);
                        }

                    } else if (jobStatusResult.getState() == JobState.CANCELED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was externally cancelled but process is not marked as cancelled yet");
                        MonitoringUtil.registerCancelProcess(getCuratorClient(), processId);
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
            return false;
        }
    }

    private void runConsumer() throws ApplicationSettingsException {
        final Consumer<String, JobStatusResult> consumer = createConsumer();

        while (true) {
            final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(Long.MAX_VALUE);

            for (TopicPartition partition : consumerRecords.partitions()) {
                List<ConsumerRecord<String, JobStatusResult>> partitionRecords = consumerRecords.records(partition);
                for (ConsumerRecord<String, JobStatusResult> record : partitionRecords) {
                    boolean success = process(record.value());
                    logger.info("Status of processing " + record.value().getJobId() + " : " + success);
                    if (success) {
                        consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                    }
                }
            }

            consumerRecords.forEach(record -> {
                process(record.value());
            });

            consumer.commitAsync();
        }
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

            MonitoringUtil.updateStatusOfJob(getCuratorClient(), jobId, jobState);
        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {

        PostWorkflowManager postManager = new PostWorkflowManager();
        postManager.init();
        postManager.runConsumer();
    }
}
