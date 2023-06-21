package org.apache.airavata.apis.workflow;

import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.helix.InstanceType;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.examples.OnlineOfflineStateModelFactory;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZKHelixManager;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.participant.StateMachineEngine;
import org.apache.helix.task.TaskFactory;
import org.apache.helix.task.TaskStateModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication()
@PropertySource(value = "classpath:workflow.properties")
public class WorkflowExecutor implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);

    @org.springframework.beans.factory.annotation.Value("${workflow.cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${workflow.controller.name}")
    private String controllerName;

    @org.springframework.beans.factory.annotation.Value("${workflow.participant.name}")
    private String participantName;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.address}")
    private String zkAddress;

    private org.apache.helix.HelixManager zkControllerHelixManager;
    private org.apache.helix.HelixManager zkParticipantHelixManager;

    private CountDownLatch controllerStartLatch = new CountDownLatch(1);
    private CountDownLatch serverStopLatch = new CountDownLatch(1);

    private final List<String> runningTasks = Collections.synchronizedList(new ArrayList<>());
    private int shutdownGracePeriod = 30000;
    private int shutdownGraceRetries = 2;
    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    @Override
    public void run(String... args) throws Exception {
        pool.submit(this::startController);
        pool.submit(this::startParticipant);
    }

    private void startController() {
        logger.info("Starting Workflow Controller ......");

        try {
            ZkClient zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            // Creates the zk cluster if not available
            if (!zkHelixAdmin.getClusters().contains(clusterName)) {
                logger.info("Creating the cluster for first time: {}", clusterName);
                zkHelixAdmin.addCluster(clusterName, true);
            }

            zkHelixAdmin.close();
            zkClient.close();

            logger.info("Connection to helix cluster : {}  with name : {}", clusterName, controllerName);
            logger.info("Zookeeper connection string {}", zkAddress);

            zkControllerHelixManager = HelixControllerMain.startHelixController(zkAddress, clusterName,
                    controllerName, HelixControllerMain.STANDALONE);
            controllerStartLatch.countDown();
            logger.info("Workflow Controller Started...");
            serverStopLatch.await();
            logger.info("Workflow Controller Stopping");

        } catch (Exception ex) {
            logger.error("Error in running the Controller: {}", controllerName, ex);

        } finally {
            disconnectController();
        }
    }
    private void startParticipant() {
        try {
            controllerStartLatch.await();
            logger.info("Controller started. Starting the participant...");

            ZkClient zkClient = null;
            try {
                zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                        ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
                ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

                List<String> nodesInCluster = zkHelixAdmin.getInstancesInCluster(clusterName);

                if (!nodesInCluster.contains(participantName)) {
                    InstanceConfig instanceConfig = new InstanceConfig(participantName);
                    instanceConfig.setHostName("localhost");
                    instanceConfig.setInstanceEnabled(true);
                    instanceConfig.setMaxConcurrentTask(30);
                    zkHelixAdmin.addInstance(clusterName, instanceConfig);
                    logger.info("Participant: " + participantName + " has been added to cluster: " + clusterName);

                } else {
                    zkHelixAdmin.enableInstance(clusterName, participantName, true);
                    logger.debug("Participant: " + participantName + " has been re-enabled at the cluster: " + clusterName);
                }

                Runtime.getRuntime().addShutdownHook(
                        new Thread(() -> {
                            logger.debug("Participant: " + participantName + " shutdown hook called");
                            try {
                                zkHelixAdmin.enableInstance(clusterName, participantName, false);
                            } catch (Exception e) {
                                logger.warn("Participant: " + participantName + " was not disabled normally", e);
                            }
                            disconnectParticipant();
                        })
                );

                zkParticipantHelixManager = new ZKHelixManager(clusterName, participantName, InstanceType.PARTICIPANT, zkAddress);
                // register online-offline model
                StateMachineEngine machineEngine = zkParticipantHelixManager.getStateMachineEngine();
                OnlineOfflineStateModelFactory factory = new OnlineOfflineStateModelFactory(participantName);
                machineEngine.registerStateModelFactory(BuiltInStateModelDefinitions.OnlineOffline.name(), factory);

                // register task model
                machineEngine.registerStateModelFactory("Task", new TaskStateModelFactory(zkParticipantHelixManager, getTaskFactory()));

                logger.debug("Participant: " + participantName + ", registered state model factories.");

                zkParticipantHelixManager.connect();
                logger.info("Participant: " + participantName + ", has connected to cluster: " + clusterName);
                serverStopLatch.await();
                logger.info("Workflow participant is stopping");
            } catch (Exception e) {
                logger.error("Failed to start the participant", e);
            }

        } catch (InterruptedException e) {
            logger.error("Failed waiting for controller to start.", e);
        }
    }

    private Map<String, TaskFactory> getTaskFactory() throws Exception {

        String[] taskClasses = {
                "org.apache.airavata.apis.workflow.task.data.DataMovementTask",
                "org.apache.airavata.apis.workflow.task.ec2.CreateEC2InstanceTask"};

        Map<String, TaskFactory> taskMap = new HashMap<>();

        for (String className : taskClasses) {
            try {
                logger.info("Loading task {}", className);
                Class<?> taskClz = Class.forName(className);
                Object taskObj = taskClz.getConstructor().newInstance();
                BaseTask baseTask = (BaseTask) taskObj;
                TaskFactory taskFactory = context -> {
                    baseTask.setCallbackContext(context);
                    return baseTask;
                };
                TaskDef btDef = baseTask.getClass().getAnnotation(TaskDef.class);
                taskMap.put(btDef.name(), taskFactory);

            } catch (ClassNotFoundException e) {
                logger.error("Couldn't find a class with name {}", className);
                throw e;
            }
        }

        return taskMap;
    }

    private void disconnectController() {
        if (zkControllerHelixManager != null) {
            logger.info("Controller: {}, has disconnected from cluster: {}", controllerName, clusterName);
            zkControllerHelixManager.disconnect();
        }
    }

    private void disconnectParticipant() {
        logger.info("Shutting down participant. Currently available tasks " + runningTasks.size());
        if (zkParticipantHelixManager != null) {
            if (runningTasks.size() > 0) {
                for (int i = 0; i <= shutdownGraceRetries; i++) {
                    logger.info("Shutting down gracefully [RETRY " + i + "]");
                    try {
                        Thread.sleep(shutdownGracePeriod);
                    } catch (InterruptedException e) {
                        logger.warn("Waiting for running tasks failed [RETRY " + i + "]", e);
                    }
                    if (runningTasks.size() == 0) {
                        break;
                    }
                }
            }
            logger.info("Participant: " + participantName + ", has disconnected from cluster: " + clusterName);
            zkParticipantHelixManager.disconnect();
        }
    }

    public static void main(String args[]) throws Exception {
        SpringApplication app = new SpringApplication(WorkflowExecutor.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        System.out.printf("Starting app");
        app.run(args);
    }
}
