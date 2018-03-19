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
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.job.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.job.monitor.parser.JobStatusResult;
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

    private String getExperimentIdByJobId(String jobId) throws Exception {
        byte[] processBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/experiment");
        return new String(processBytes);
    }

    private String getTaskIdByJobId(String jobId) throws Exception {
        byte[] processBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/task");
        return new String(processBytes);
    }

    private String getProcessIdByJobId(String jobId) throws Exception {
        byte[] processBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/process");
        return new String(processBytes);
    }

    private String getGatewayByJobId(String jobId) throws Exception {
        byte[] gatewayBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/gateway");
        return new String(gatewayBytes);
    }

    private String getStatusByJobId(String jobId) throws Exception {
        byte[] statusBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/status");
        return new String(statusBytes);
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
            logger.info("Processing job result " + jobStatusResult.getJobId());

            if (hasMonitoringRegistered(jobStatusResult.getJobId())) {
                String gateway = getGatewayByJobId(jobStatusResult.getJobId());
                String processId = getProcessIdByJobId(jobStatusResult.getJobId());
                String experimentId = getExperimentIdByJobId(jobStatusResult.getJobId());
                String task = getTaskIdByJobId(jobStatusResult.getJobId());
                String status = getStatusByJobId(jobStatusResult.getJobId());

                logger.info("Starting the post workflow for job id : " + jobStatusResult.getJobId() + " with process id "
                        + processId + ", gateway " + gateway + " and status " + jobStatusResult.getState().name());

                // TODO get cluster lock before that
                if ("cancelled".equals(status)) {
                    // TODO to be implemented
                } else {

                    saveAndPublishJobStatus(jobStatusResult.getJobId(), task, processId, experimentId, gateway, jobStatusResult.getState());

                    if (jobStatusResult.getState() == JobState.COMPLETE) {
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

                        workflowManager.launchWorkflow(processId + "-POST-" + UUID.randomUUID().toString(),
                                new ArrayList<>(allTasks), true, false);

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
