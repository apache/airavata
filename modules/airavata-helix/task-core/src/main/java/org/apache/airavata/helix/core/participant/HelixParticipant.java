package org.apache.airavata.helix.core.participant;

import org.apache.airavata.helix.core.support.TaskHelperImpl;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.util.PropertyResolver;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.InstanceType;
import org.apache.helix.examples.OnlineOfflineStateModelFactory;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZKHelixManager;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.participant.StateMachineEngine;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskFactory;
import org.apache.helix.task.TaskStateModelFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixParticipant <T extends AbstractTask> implements Runnable {

    private static final Logger logger = LogManager.getLogger(HelixParticipant.class);

    private String zkAddress;
    private String clusterName;
    private String participantName;
    private ZKHelixManager zkHelixManager;
    private String taskTypeName;
    private PropertyResolver propertyResolver;
    private Class<T> taskClass;

    public HelixParticipant(String propertyFile, Class<T> taskClass, String taskTypeName, boolean readPropertyFromFile) throws IOException {

        logger.info("Initializing Participant Node");

        this.propertyResolver = new PropertyResolver();
        if (readPropertyFromFile) {
            propertyResolver.loadFromFile(new File(propertyFile));
        } else {
            propertyResolver.loadInputStream(this.getClass().getClassLoader().getResourceAsStream(propertyFile));
        }

        this.zkAddress = propertyResolver.get("zookeeper.connection.url");
        this.clusterName = propertyResolver.get("helix.cluster.name");
        this.participantName = propertyResolver.get("participant.name");
        this.taskTypeName = taskTypeName;
        this.taskClass = taskClass;

        logger.info("Zookeper connection url " + zkAddress);
        logger.info("Cluster name " + clusterName);
        logger.info("Participant name " + participantName);
        logger.info("Task type " + taskTypeName);
        if (taskClass != null) {
            logger.info("Task class " + taskClass.getCanonicalName());
        }
    }

    public Map<String, TaskFactory> getTaskFactory() {
        Map<String, TaskFactory> taskRegistry = new HashMap<String, TaskFactory>();

        TaskFactory taskFac = new TaskFactory() {
            public Task createNewTask(TaskCallbackContext context) {
                try {
                    return taskClass.newInstance()
                            .setCallbackContext(context)
                            .setTaskHelper(new TaskHelperImpl());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        TaskDef taskDef = taskClass.getAnnotation(TaskDef.class);
        taskRegistry.put(taskDef.name(), taskFac);

        return taskRegistry;
    }

    public void run() {
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
                if (taskTypeName != null) {
                    instanceConfig.addTag(taskTypeName);
                }
                zkHelixAdmin.addInstance(clusterName, instanceConfig);
                logger.debug("Instance: " + participantName + ", has been added to cluster: " + clusterName);
            } else {
                if (taskTypeName != null) {
                    zkHelixAdmin.addInstanceTag(clusterName, participantName, taskTypeName);
                }
            }

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        @Override
                        public void run() {
                            logger.debug("Participant: " + participantName + ", shutdown hook called.");
                            disconnect();
                        }
                    }
            );

            // connect the participant manager
            connect();
        } catch (Exception ex) {
            logger.error("Error in run() for Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

    private void connect() {
        try {
            zkHelixManager = new ZKHelixManager(clusterName, participantName, InstanceType.PARTICIPANT, zkAddress);
            // register online-offline model
            StateMachineEngine machineEngine = zkHelixManager.getStateMachineEngine();
            OnlineOfflineStateModelFactory factory = new OnlineOfflineStateModelFactory(participantName);
            machineEngine.registerStateModelFactory(BuiltInStateModelDefinitions.OnlineOffline.name(), factory);

            // register task model
            machineEngine.registerStateModelFactory("Task", new TaskStateModelFactory(zkHelixManager, getTaskFactory()));

            logger.debug("Participant: " + participantName + ", registered state model factories.");

            zkHelixManager.connect();
            logger.info("Participant: " + participantName + ", has connected to cluster: " + clusterName);

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            logger.error("Participant: " + participantName + ", is interrupted! reason: " + ex, ex);
        }
        catch (Exception ex) {
            logger.error("Error in connect() for Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            logger.info("Participant: " + participantName + ", has disconnected from cluster: " + clusterName);
            zkHelixManager.disconnect();
        }
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }
}
