package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.EnvSetupTask;
import org.apache.airavata.helix.impl.task.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.OutputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.task.DefaultJobSubmissionTask;
import org.apache.airavata.helix.impl.task.submission.task.JobSubmissionTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.job.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.job.monitor.parser.JobStatusResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.stream.Collectors;

public class PostWorkflowManager {

    private static final Logger logger = LogManager.getLogger(PostWorkflowManager.class);

    private final String BOOTSTRAP_SERVERS = "localhost:9092";
    private final String TOPIC = "parsed-data";

    private CuratorFramework curatorClient = null;

    private void init() throws ApplicationSettingsException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private Consumer<String, JobStatusResult> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "MonitoringConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JobStatusResultDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, JobStatusResult> consumer = new KafkaConsumer<String, JobStatusResult>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    private String getProcessIdByJobId(String jobId) throws Exception {
        byte[] processBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/process");
        String process = new String(processBytes);
        return process;
    }

    private String getGatewayByJobId(String jobId) throws Exception {
        byte[] gatewayBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/gateway");
        String gateway = new String(gatewayBytes);
        return gateway;
    }

    private String getStatusByJobId(String jobId) throws Exception {
        byte[] statusBytes = this.curatorClient.getData().forPath("/monitoring/" + jobId + "/status");
        String status = new String(statusBytes);
        return status;
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
                String status = getStatusByJobId(jobStatusResult.getJobId());

                // TODO get cluster lock before that
                if ("cancelled".equals(status)) {

                } else {

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
                                        airavataTask = new OutputDataStagingTask();
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
                        WorkflowManager workflowManager = new WorkflowManager("AiravataDemoCluster",
                                "wm-23", ServerSettings.getZookeeperConnection());

                        workflowManager.launchWorkflow(UUID.randomUUID().toString(),
                                allTasks.stream().map(t -> (AiravataTask) t).collect(Collectors.toList()), true);

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

    private void runConsumer() throws InterruptedException {
        final Consumer<String, JobStatusResult> consumer = createConsumer();

        final int giveUp = 100;   int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(1000);

            /*if (consumerRecords.count() == 0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }*/

            consumerRecords.forEach(record -> {
                process(record.value());
            });

            consumer.commitAsync();
        }
        //consumer.close();
        //System.out.println("DONE");
    }

    public static void main(String[] args) throws Exception {

        PostWorkflowManager postManager = new PostWorkflowManager();
        postManager.init();
        postManager.runConsumer();
        /*
        String processId = "PROCESS_5b252ad9-d630-4cf9-80e3-0c30c55d1001";
        ExperimentCatalog experimentCatalog = RegistryFactory.getDefaultExpCatalog();

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
                if (taskModel.getTaskType() == TaskTypes.ENV_SETUP) {
                    //airavataTask = new EnvSetupTask();
                } else if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    //airavataTask = new DefaultJobSubmissionTask();
                    //airavataTask.setRetryCount(1);
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (jobSubmissionFound) {
                        airavataTask = new OutputDataStagingTask();
                    } else {
                        //airavataTask = new InputDataStagingTask();
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() -1).setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

        WorkflowManager workflowManager = new WorkflowManager("AiravataDemoCluster", "wm-22", "localhost:2199");
        workflowManager.launchWorkflow(UUID.randomUUID().toString(), allTasks.stream().map(t -> (AiravataTask)t).collect(Collectors.toList()), true);
        */
    }
}
