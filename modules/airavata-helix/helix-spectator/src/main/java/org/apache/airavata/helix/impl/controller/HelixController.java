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
package org.apache.airavata.helix.impl.controller;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixController implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(HelixController.class);

    private String clusterName;
    private String controllerName;
    private String zkAddress;
    private org.apache.helix.HelixManager zkHelixManager;

    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch stopLatch = new CountDownLatch(1);

    @SuppressWarnings("WeakerAccess")
    public HelixController() throws ApplicationSettingsException {
        this.clusterName = ServerSettings.getSetting("helix.cluster.name");
        this.controllerName = ServerSettings.getSetting("helix.controller.name");
        this.zkAddress = ServerSettings.getZookeeperConnection();
    }

    public void run() {
        try {
            ZkClient zkClient = new ZkClient(ServerSettings.getZookeeperConnection(), ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            // Creates the zk cluster if not available
            if (! zkHelixAdmin.getClusters().contains(clusterName)) {
                zkHelixAdmin.addCluster(clusterName, true);
            }

            zkHelixAdmin.close();
            zkClient.close();

            logger.info("Connection to helix cluster : " + clusterName + " with name : " + controllerName);
            logger.info("Zookeeper connection string " + zkAddress);

            zkHelixManager = HelixControllerMain.startHelixController(zkAddress, clusterName,
                    controllerName, HelixControllerMain.STANDALONE);
            startLatch.countDown();
            stopLatch.await();
        } catch (Exception ex) {
            logger.error("Error in run() for Controller: " + controllerName + ", reason: " + ex, ex);
        } finally {
            disconnect();
        }
    }

    public void startServer() throws Exception {

        //WorkflowCleanupAgent cleanupAgent = new WorkflowCleanupAgent();
        //cleanupAgent.init();
        //ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        //executor.scheduleWithFixedDelay(cleanupAgent, 10, 120, TimeUnit.SECONDS);

        new Thread(this).start();
        try {
            startLatch.await();
            logger.info("Controller: " + controllerName + ", has connected to cluster: " + clusterName);

            Runtime.getRuntime().addShutdownHook(
                    new Thread(this::disconnect)
            );

        } catch (InterruptedException ex) {
            logger.error("Controller: " + controllerName + ", is interrupted! reason: " + ex, ex);
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void stop() {
        stopLatch.countDown();
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            logger.info("Controller: " + controllerName + ", has disconnected from cluster: " + clusterName);
            zkHelixManager.disconnect();
        }
    }

    public static void main(String args[]) {
        try {

            logger.info("Starting helix controller");

            HelixController helixController = new HelixController();
            helixController.startServer();

        } catch (Exception e) {
            logger.error("Failed to start the helix controller", e);
        }
    }
}
