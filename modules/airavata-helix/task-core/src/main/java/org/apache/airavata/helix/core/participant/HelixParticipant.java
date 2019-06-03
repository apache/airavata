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
package org.apache.airavata.helix.core.participant;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
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
import org.apache.helix.task.TaskFactory;
import org.apache.helix.task.TaskStateModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixParticipant<T extends AbstractTask> implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(HelixParticipant.class);

    private int shutdownGracePeriod = 30000;
    private int shutdownGraceRetries = 2;

    private String zkAddress;
    private String clusterName;
    private String participantName;
    private ZKHelixManager zkHelixManager;

    private String taskTypeName;

    private List<Class<? extends T>> taskClasses;
    private final List<String> runningTasks = Collections.synchronizedList(new ArrayList<String>());

    public HelixParticipant(List<Class<? extends T>> taskClasses, String taskTypeName) throws ApplicationSettingsException {

        logger.info("Initializing Participant Node");

        this.zkAddress = ServerSettings.getZookeeperConnection();
        this.clusterName = ServerSettings.getSetting("helix.cluster.name");
        this.participantName = getParticipantName();

        this.taskTypeName = taskTypeName;
        this.taskClasses = taskClasses;

        logger.info("Zookeeper connection URL " + zkAddress);
        logger.info("Cluster name " + clusterName);
        logger.info("Participant name " + participantName);
        logger.info("Task type " + taskTypeName);

        if (taskClasses != null) {
            for (Class<? extends T> taskClass : taskClasses) {
                logger.info("Task classes include: " + taskClass.getCanonicalName());
            }
        }
    }

    public HelixParticipant(Class<T> taskClass, String taskTypeName) throws ApplicationSettingsException {
        this(taskClass != null ? Collections.singletonList(taskClass) : null, taskTypeName);
    }

    public void setShutdownGracePeriod(int shutdownGracePeriod) {
        this.shutdownGracePeriod = shutdownGracePeriod;
    }

    public void setShutdownGraceRetries(int shutdownGraceRetries) {
        this.shutdownGraceRetries = shutdownGraceRetries;
    }

    public void registerRunningTask(AbstractTask task) {
        runningTasks.add(task.getTaskId());
        logger.info("Registered Task " + task.getTaskId() + ". Currently available " + runningTasks.size());
    }

    public void unregisterRunningTask(AbstractTask task) {
        runningTasks.remove(task.getTaskId());
        logger.info("Un registered Task " + task.getTaskId() + ". Currently available " + runningTasks.size());
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, TaskFactory> getTaskFactory() {
        Map<String, TaskFactory> taskRegistry = new HashMap<>();

        for (Class<? extends T> taskClass : taskClasses) {
            TaskFactory taskFac = context -> {
                try {
                    return AbstractTask.class.cast(taskClass.newInstance())
                            .setParticipant(HelixParticipant.this)
                            .setCallbackContext(context)
                            .setTaskHelper(new TaskHelperImpl());
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to initialize the task: " + context.getTaskConfig().getId(), e);
                    return null;
                }
            };
            TaskDef taskDef = taskClass.getAnnotation(TaskDef.class);
            taskRegistry.put(taskDef.name(), taskFac);
        }
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
                logger.info("Participant: " + participantName + " has been added to cluster: " + clusterName);
            } else {
                if (taskTypeName != null) {
                    zkHelixAdmin.addInstanceTag(clusterName, participantName, taskTypeName);
                }
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
                        disconnect();
                    })
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
        } catch (Exception ex) {
            logger.error("Error in connect() for Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        logger.info("Shutting down participant. Currently available tasks " + runningTasks.size());
        if (zkHelixManager != null) {
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
            zkHelixManager.disconnect();
        }
    }

    public String getParticipantName() throws ApplicationSettingsException {
        return ServerSettings.getSetting("helix.participant.name");
    }
}
