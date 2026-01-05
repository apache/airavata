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
package org.apache.airavata.helix.participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.helix.task.TaskDef;
import org.apache.airavata.helix.task.base.AbstractTask;
import org.apache.helix.InstanceType;
import org.apache.helix.examples.OnlineOfflineStateModelFactory;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZKHelixManager;
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.participant.StateMachineEngine;
import org.apache.helix.task.TaskFactory;
import org.apache.helix.task.TaskStateModelFactory;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient.RealmMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixParticipant<T extends AbstractTask> extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(HelixParticipant.class);

    private int shutdownGracePeriod = 30000;
    private int shutdownGraceRetries = 2;

    private String zkAddress;
    private String clusterName;
    private String participantName;
    private ZKHelixManager zkHelixManager;
    private Thread participantThread;

    private String taskTypeName;

    private List<Class<? extends T>> taskClasses;
    private final List<String> runningTasks = Collections.synchronizedList(new ArrayList<String>());
    private AiravataServerProperties properties;
    private ApplicationContext applicationContext;
    private final TaskHelperImpl taskHelper;

    public HelixParticipant(
            List<Class<? extends T>> taskClasses,
            String taskTypeName,
            AiravataServerProperties properties,
            TaskHelperImpl taskHelper) {
        logger.info("Initializing Participant Node");

        this.properties = properties;
        this.taskTypeName = taskTypeName;
        this.taskClasses = taskClasses; // Can be null for deferred initialization
        this.taskHelper = taskHelper;

        // Property-dependent initialization moved to initialize() method
        // This allows subclasses to set taskClasses before initializing properties
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initialize property-dependent fields. This method should be called after
     * taskClasses are set (if deferred initialization is used).
     * Can be called from @PostConstruct in subclasses.
     */
    protected void initialize() {
        if (properties != null) {
            this.zkAddress = properties.zookeeper.serverConnection;
            this.clusterName = properties.services.helix.clusterName;
            this.participantName = getParticipantName();

            logger.info("Zookeeper connection URL " + zkAddress);
            logger.info("Cluster name " + clusterName);
            logger.info("Participant name " + participantName);
            logger.info("Task type " + taskTypeName);
        }

        if (taskClasses != null) {
            for (Class<? extends T> taskClass : taskClasses) {
                logger.info("Task classes include: " + taskClass.getCanonicalName());
            }
        }
    }

    /**
     * Set task classes after construction. Used for deferred initialization.
     *
     * @param taskClasses the task classes to set
     * @throws IllegalStateException if task classes are already set and not empty
     */
    public void setTaskClasses(List<Class<? extends T>> taskClasses) {
        if (this.taskClasses != null && !this.taskClasses.isEmpty()) {
            throw new IllegalStateException("Task classes already set");
        }
        this.taskClasses = taskClasses;

        // Log task classes if they were set
        if (taskClasses != null && !taskClasses.isEmpty()) {
            for (Class<? extends T> taskClass : taskClasses) {
                logger.info("Task classes include: " + taskClass.getCanonicalName());
            }
        }
    }

    public HelixParticipant(
            Class<T> taskClass, String taskTypeName, AiravataServerProperties properties, TaskHelperImpl taskHelper) {
        this(taskClass != null ? Collections.singletonList(taskClass) : null, taskTypeName, properties, taskHelper);
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
        if (taskClasses == null || taskClasses.isEmpty()) {
            throw new IllegalStateException("Task classes must be set before creating task factory");
        }

        Map<String, TaskFactory> taskRegistry = new HashMap<>();

        for (Class<? extends T> taskClass : taskClasses) {
            TaskFactory taskFac = context -> {
                try {
                    AbstractTask task = null;
                    if (applicationContext != null) {
                        // Get task as Spring bean - all tasks must be Spring beans now
                        task = applicationContext.getBean(taskClass);
                    } else {
                        throw new IllegalStateException(
                                "ApplicationContext must be set on HelixParticipant to create tasks");
                    }
                    return task.setParticipant((HelixParticipant<T>) this)
                            .setCallbackContext(context)
                            .setTaskHelper(taskHelper);
                } catch (Exception e) {
                    logger.error(
                            "Failed to initialize the task: "
                                    + context.getTaskConfig().getId(),
                            e);
                    return null;
                }
            };
            TaskDef taskDef = taskClass.getAnnotation(TaskDef.class);
            taskRegistry.put(taskDef.name(), taskFac);
        }
        return taskRegistry;
    }

    private void startParticipant() {
        final ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin.Builder()
                .setRealmMode(RealmMode.SINGLE_REALM)
                .setZkAddress(zkAddress)
                .build();
        try {
            List<String> nodesInCluster = zkHelixAdmin.getInstancesInCluster(clusterName);

            if (!nodesInCluster.contains(participantName)) {
                InstanceConfig instanceConfig = new InstanceConfig(participantName);
                instanceConfig.setHostName("localhost");
                if (taskTypeName != null) {
                    instanceConfig.addTag(taskTypeName);
                }
                zkHelixAdmin.addInstance(clusterName, instanceConfig);
                logger.info("Participant: " + participantName + " has been added to cluster: " + clusterName);
            } else {
                if (taskTypeName != null) {
                    zkHelixAdmin.addInstanceTag(clusterName, participantName, taskTypeName);
                }
                // Instance is enabled by default when added, no need to explicitly enable
                logger.debug("Participant: " + participantName + " has been re-enabled at the cluster: " + clusterName);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.debug("Participant: " + participantName + " shutdown hook called");
                // Instance will be automatically disabled when HelixParticipant disconnects
                // No need to explicitly disable it here
                disconnect();
            }));

            // connect the participant manager
            connect();
        } catch (Exception ex) {
            logger.error("Error in startParticipant() for Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            zkHelixAdmin.close();
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
            machineEngine.registerStateModelFactory(
                    "Task", new TaskStateModelFactory(zkHelixManager, getTaskFactory()));

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

    public String getParticipantName() {
        return properties.services.helix.participantName;
    }

    @Override
    public String getServerName() {
        return "Helix Participant";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 15; // Start after controller
    }

    @Override
    protected void doStart() throws Exception {
        participantThread = new Thread(this::startParticipant);
        participantThread.setName(getServerName());
        participantThread.setDaemon(true);
        participantThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        if (participantThread != null) {
            participantThread.interrupt();
            try {
                participantThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for participant thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        // HelixParticipant's startParticipant() method will call disconnect() in its finally block
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && participantThread != null && participantThread.isAlive();
    }
}
