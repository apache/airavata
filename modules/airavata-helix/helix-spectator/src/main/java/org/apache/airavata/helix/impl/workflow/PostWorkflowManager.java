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

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.*;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.impl.task.staging.ArchiveTask;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.monitor.JobStateValidator;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.messaging.core.impl.RabbitMQPublisher;
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
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.*;

public class PostWorkflowManager {

    private static final Logger logger = LogManager.getLogger(PostWorkflowManager.class);

    private CuratorFramework curatorClient = null;
    private Publisher statusPublisher;

    private void init() throws ApplicationSettingsException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private Consumer<String, JobStatusResult> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("kafka.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JobStatusResultDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, JobStatusResult> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("kafka.broker.topic")));
        return consumer;
    }

    private void registerWorkflow(String processId, String workflowId) throws Exception {
        this.curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/registry/" + processId + "/workflows/" + workflowId , new byte[0]);
    }

    private String getExperimentIdByJobId(String jobId) throws Exception {
        String path = "/monitoring/" + jobId + "/experiment";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = this.curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    private String getTaskIdByJobId(String jobId) throws Exception {
        String path = "/monitoring/" + jobId + "/task";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = this.curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    private String getProcessIdByJobId(String jobId) throws Exception {
        String path = "/monitoring/" + jobId + "/process";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = this.curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    private String getGatewayByJobId(String jobId) throws Exception {
        String path = "/monitoring/" + jobId + "/gateway";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] gatewayBytes = this.curatorClient.getData().forPath(path);
            return new String(gatewayBytes);
        } else {
            return null;
        }
    }

    private void updateStatusOfJob(String jobId, JobState jobState) throws Exception {
        String path = "/monitoring/" + jobId + "/status";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            this.curatorClient.delete().forPath(path);
        }
        this.curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, jobState.name().getBytes());
    }

    private JobState getCurrentStatusOfJob(String jobId) throws Exception {
        String path = "/monitoring/" + jobId + "/status";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] gatewayBytes = this.curatorClient.getData().forPath(path);
            return JobState.valueOf(new String(gatewayBytes));
        } else {
            return null;
        }
    }

    private String getStatusOfProcess(String processId) throws Exception {
        String path = "/registry/" + processId + "/status";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] statusBytes = this.curatorClient.getData().forPath(path);
            return new String(statusBytes);
        } else {
            return null;
        }
    }

    private boolean hasMonitoringRegistered(String jobId) throws Exception {
        Stat stat = this.curatorClient.checkExists().forPath("/monitoring/" + jobId);
        return stat != null;
    }

    private void process(JobStatusResult jobStatusResult) {

        if (jobStatusResult == null) {
            return;
        }

        try {
            logger.info("Processing job result of job id " + jobStatusResult.getJobId() + " sent by " + jobStatusResult.getPublisherName());

            if (hasMonitoringRegistered(jobStatusResult.getJobId())) {

                JobState currentJobStatus = getCurrentStatusOfJob(jobStatusResult.getJobId());
                if (!JobStateValidator.isValid(currentJobStatus, jobStatusResult.getState())) {
                    logger.warn("Job state of " + jobStatusResult.getJobId() + " is not valid. Previous state " +
                            currentJobStatus + ", new state " + jobStatusResult.getState());
                    return;
                }

                String gateway = Optional.ofNullable(getGatewayByJobId(jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the gateway for job id " + jobStatusResult.getJobId()));

                String processId = Optional.ofNullable(getProcessIdByJobId(jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the process for job id " + jobStatusResult.getJobId()));

                String experimentId = Optional.ofNullable(getExperimentIdByJobId(jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the experiment for job id " + jobStatusResult.getJobId()));

                String task = Optional.ofNullable(getTaskIdByJobId(jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the task for job id " + jobStatusResult.getJobId()));

                String processStatus = getStatusOfProcess(processId);

                logger.info("Updating the job status for job id : " + jobStatusResult.getJobId() + " with process id "
                        + processId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());
                saveAndPublishJobStatus(jobStatusResult.getJobId(), task, processId, experimentId, gateway, jobStatusResult.getState());

                // TODO get cluster lock before that
                if ("cancel".equals(processStatus)) {
                    logger.info("Cancelled post workflow for process " + processId);
                    // TODO to be implemented
                } else {

                    if (jobStatusResult.getState() == JobState.COMPLETE) {

                        logger.info("Starting the post workflow for job id : " + jobStatusResult.getJobId() + " with process id "
                                + processId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());

                        logger.info("Job " + jobStatusResult.getJobId() + " was completed");

                        ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog(gateway);
                        ProcessModel processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
                        ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, processModel.getExperimentId());
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

                        WorkflowManager workflowManager = new WorkflowManager(
                                ServerSettings.getSetting("helix.cluster.name"),
                                ServerSettings.getSetting("post.workflow.manager.name"),
                                ServerSettings.getZookeeperConnection());

                        String workflowName = workflowManager.launchWorkflow(processId + "-POST-" + UUID.randomUUID().toString(),
                                new ArrayList<>(allTasks), true, false);
                        try {
                            registerWorkflow(processId, workflowName);
                        } catch (Exception e) {
                            logger.error("Failed to save workflow " + workflowName + " of process " + processId + " in zookeeper registry. " +
                                    "This will affect cancellation tasks", e);
                        }
                    } else if (jobStatusResult.getState() == JobState.CANCELED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was externally cancelled");

                    } else if (jobStatusResult.getState() == JobState.FAILED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was failed");

                    } else if (jobStatusResult.getState() == JobState.SUBMITTED) {
                        logger.info("Job " + jobStatusResult.getJobId() + " was submitted");

                    }
                }
            } else {
                logger.warn("Could not find a monitoring register for job id " + jobStatusResult.getJobId());
            }
        } catch (Exception e) {
            logger.error("Failed to process job : " + jobStatusResult.getJobId() + ", with status : " + jobStatusResult.getState().name(), e);
        }
    }

    private void runConsumer() throws ApplicationSettingsException {
        final Consumer<String, JobStatusResult> consumer = createConsumer();

        while (true) {
            final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(1000);
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

            CompositeIdentifier ids = new CompositeIdentifier(taskId, jobId);
            ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog(gateway);
            experimentCatalog.add(ExpCatChildDataType.JOB_STATUS, jobStatus, ids);
            JobIdentifier identifier = new JobIdentifier(jobId, taskId,
                    processId, experimentId, gateway);

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
                    (MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);

            updateStatusOfJob(jobId, jobState);
        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    private Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            synchronized (RabbitMQPublisher.class) {
                if (statusPublisher == null) {
                    statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
                }
            }
        }
        return statusPublisher;
    }

    public static void main(String[] args) throws Exception {

        PostWorkflowManager postManager = new PostWorkflowManager();
        postManager.init();
        postManager.runConsumer();
    }
}
