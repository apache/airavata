package org.apache.airavata.apis.scheduling;

import org.apache.airavata.api.execution.ExperimentLaunchRequest;
import org.apache.airavata.api.execution.stubs.EC2Backend;
import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.api.execution.stubs.RunConfiguration;
import org.apache.airavata.apis.service.ExecutionService;
import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.OutPort;
import org.apache.airavata.apis.workflow.task.common.TaskUtil;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskOutPort;
import org.apache.airavata.apis.workflow.task.data.DataMovementTask;
import org.apache.airavata.apis.workflow.task.ec2.CreateEC2InstanceTask;
import org.apache.airavata.apis.workflow.task.ec2.DestroyEC2InstanceTask;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretCreateRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.*;

public class ExperimentLauncher {

    private static final long WORKFLOW_EXPIRY_TIME = 1 * 1000;
    private static final long TASK_EXPIRY_TIME = 24 * 60 * 60 * 1000;
    private static final int PARALLEL_JOBS_PER_WORKFLOW = 20;

    private final static Logger logger = LoggerFactory.getLogger(ExperimentLauncher.class);

    private TaskDriver taskDriver;
    private HelixManager helixManager;
    @Autowired
    private ExecutionService executionService;


    public void launchExperiment(ExperimentLaunchRequest experimentLaunchRequest) throws Exception {
        Optional<Experiment> experimentOp = executionService.getExperiment(experimentLaunchRequest.getExperimentId());
        if (experimentOp.isEmpty()) {
            throw new Exception("No experiment with id " + experimentLaunchRequest.getExperimentId());
        }

        Experiment experiment = experimentOp.get();
    }

    private void submitEC2Workflow(RunConfiguration runConfiguration) {
        CreateEC2InstanceTask ec2InstanceTask = new CreateEC2InstanceTask();
        ec2InstanceTask.setEc2Backend(runConfiguration.getEc2());
        ec2InstanceTask.setSecretServiceHost("localhost");
        ec2InstanceTask.setSecretServicePort(7003);
        ec2InstanceTask.setUserToken("token");


    }


    public static void main(String args[]) throws Exception {
        ExperimentLauncher launcher = new ExperimentLauncher();
        launcher.init("airavata", "wm", "localhost:2181");

        SecretServiceClient secretServiceClient = SecretServiceClientBuilder.buildClient("localhost", 7002);
        S3Secret s3Secret = secretServiceClient.s3().createS3Secret(S3SecretCreateRequest.newBuilder()
                .setAccessKey("key").setSecretKey("sec").build());

        logger.info("S3 Secret id : " + s3Secret.getSecretId());

        Map<String, BaseTask> taskMap = new HashMap<>();

        EC2Backend ec2Backend = EC2Backend.newBuilder()
                .setAwsCredentialId(s3Secret.getSecretId())
                .setLoginUserName("ubuntu")
                .setRegion("us-east-1")
                .setFlavor("t2.micro")
                .setImageId("ami-053b0d53c279acc90").build();

        CreateEC2InstanceTask ec2InstanceTask = new CreateEC2InstanceTask();
        ec2InstanceTask.setTaskId(UUID.randomUUID().toString());
        ec2InstanceTask.setEc2Backend(ec2Backend);
        ec2InstanceTask.setSecretServiceHost("localhost");
        ec2InstanceTask.setSecretServicePort(7002);
        ec2InstanceTask.setUserToken("token");
        taskMap.put(ec2InstanceTask.getTaskId(), ec2InstanceTask);

        DataMovementTask dataMovementTask = new DataMovementTask();
        dataMovementTask.setTaskId(UUID.randomUUID().toString());
        dataMovementTask.setSecretServiceHost("localhost");
        dataMovementTask.setSecretServicePort(7002);
        dataMovementTask.setTransferServiceHost("localhost");
        dataMovementTask.setTransferServicePort(7002);
        dataMovementTask.setResourceServiceHost("localhost");
        dataMovementTask.setResourceServicePort(7002);
        dataMovementTask.setUserToken("token");
        dataMovementTask.setSourceStorageId("504643b6-f813-4aa1-8e66-2533cb4f837c");
        dataMovementTask.setSourceCredentialId("");
        dataMovementTask.setSourcePath("/Users/dwannipu/Downloads/IMG-9309.jpg");
        dataMovementTask.setDestinationPath("/tmp/IMG-9309.jpg");
        dataMovementTask.setDestinationStorageId("");
        dataMovementTask.setDestinationCredentialId("");
        dataMovementTask.overrideParameterFromWorkflowContext("destinationStorageId", // Loading context parameter from previous Task
                CreateEC2InstanceTask.EC2_INSTANCE_STORAGE_ID);
        dataMovementTask.overrideParameterFromWorkflowContext("destinationCredentialId",
                CreateEC2InstanceTask.EC2_INSTANCE_SECRET_ID);


        taskMap.put(dataMovementTask.getTaskId(), dataMovementTask);

        DestroyEC2InstanceTask destroyEC2InstanceTask = new DestroyEC2InstanceTask();
        destroyEC2InstanceTask.setTaskId(UUID.randomUUID().toString());
        destroyEC2InstanceTask.setEc2Backend(ec2Backend);
        destroyEC2InstanceTask.setSecretServiceHost("localhost");
        destroyEC2InstanceTask.setSecretServicePort(7002);
        destroyEC2InstanceTask.setUserToken("token");
        destroyEC2InstanceTask.setInstanceId(""); // Override by workflow
        destroyEC2InstanceTask.overrideParameterFromWorkflowContext("instanceId", CreateEC2InstanceTask.EC2_INSTANCE_ID);

        //taskMap.put(destroyEC2InstanceTask.getTaskId(), destroyEC2InstanceTask);

        ec2InstanceTask.addOutPort(new OutPort().setNextTaskId(dataMovementTask.getTaskId()));

        String[] startTaskIds = {ec2InstanceTask.getTaskId()};
        logger.info("Submitting workflow");
        launcher.buildAndRunWorkflow(taskMap, startTaskIds);
    }

    public void init(String clusterName, String workflowManagerName, String zkAddress)
            throws Exception {

        helixManager = HelixManagerFactory.getZKHelixManager(clusterName, workflowManagerName,
                InstanceType.SPECTATOR, zkAddress);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        if (helixManager != null && helixManager.isConnected()) {
                            helixManager.disconnect();
                        }
                    }
                }
        );

        taskDriver = new TaskDriver(helixManager);
    }

    public void destroy() {
        if (helixManager != null) {
            helixManager.disconnect();
        }
    }

    public String buildAndRunWorkflow(Map<String, BaseTask> taskMap, String[] startTaskIds) throws Exception {

        if (taskDriver == null) {
            throw new Exception("Workflow operator needs to be initialized");
        }

        String workflowName = UUID.randomUUID().toString();
        Workflow.Builder workflowBuilder = new Workflow.Builder(workflowName).setExpiry(0);

        for (String startTaskId: startTaskIds) {
            buildWorkflowRecursively(workflowBuilder, workflowName, startTaskId, taskMap);
        }

        WorkflowConfig.Builder config = new WorkflowConfig.Builder()
                .setFailureThreshold(0)
                .setAllowOverlapJobAssignment(true);

        workflowBuilder.setWorkflowConfig(config.build());
        workflowBuilder.setExpiry(WORKFLOW_EXPIRY_TIME);
        Workflow workflow = workflowBuilder.build();

        logger.info("Starting workflow {}", workflowName);
        taskDriver.start(workflow);
        return workflowName;
    }

    private void buildWorkflowRecursively(Workflow.Builder workflowBuilder, String workflowName,
                                          String nextTaskId, Map<String, BaseTask> taskMap)
            throws Exception{
        BaseTask currentTask = taskMap.get(nextTaskId);

        if (currentTask == null) {
            logger.error("Couldn't find a task with id {} in the task map", nextTaskId);
            throw new Exception("Couldn't find a task with id " + nextTaskId +" in the task map");
        }

        TaskDef blockingTaskDef = currentTask.getClass().getAnnotation(TaskDef.class);

        if (blockingTaskDef != null) {
            String taskName = blockingTaskDef.name();
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder()
                    .setTaskId(currentTask.getTaskId())
                    .setCommand(taskName);

            Map<String, String> paramMap = TaskUtil.serializeTaskData(currentTask);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setExpiry(WORKFLOW_EXPIRY_TIME)
                    .setTimeoutPerTask(TASK_EXPIRY_TIME)
                    .setNumConcurrentTasksPerInstance(20)
                    .setMaxAttemptsPerTask(currentTask.getRetryCount());

            workflowBuilder.addJob(currentTask.getTaskId(), job);

            List<OutPort> outPorts = getOutPortsOfTask(currentTask);

            for (OutPort outPort : outPorts) {
                if (outPort != null) {
                    workflowBuilder.addParentChildDependency(currentTask.getTaskId(), outPort.getNextTaskId());
                    logger.info("Parent to child dependency {} -> {}", currentTask.getTaskId(), outPort.getNextTaskId());
                    buildWorkflowRecursively(workflowBuilder, workflowName, outPort.getNextTaskId(), taskMap);
                }
            }
        } else {
            logger.error("Couldn't find the task def annotation in class {}", currentTask.getClass().getName());
            throw new Exception("Couldn't find the task def annotation in class " + currentTask.getClass().getName());
        }
    }


    private <T extends BaseTask> List<OutPort> getOutPortsOfTask(T taskObj) throws IllegalAccessException {

        List<OutPort> outPorts = new ArrayList<>();
        for (Class<?> c = taskObj.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                TaskOutPort outPortAnnotation = field.getAnnotation(TaskOutPort.class);
                if (outPortAnnotation != null) {
                    field.setAccessible(true);
                    List<OutPort> outPort = (List<OutPort>) field.get(taskObj);
                    outPorts.addAll(outPort);
                }
            }
        }
        return outPorts;
    }


}
