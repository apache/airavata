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
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.IdealState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixController implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(HelixController.class);

    private final String clusterName;
    private final String controllerName;
    private final String zkAddress;
    private org.apache.helix.HelixManager zkHelixManager;

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    @SuppressWarnings("WeakerAccess")
    public HelixController() throws ApplicationSettingsException {
        this.clusterName = ServerSettings.getSetting("helix.cluster.name");
        this.controllerName = ServerSettings.getSetting("helix.controller.name");
        this.zkAddress = ServerSettings.getZookeeperConnection();
    }

    public void run() {
        try {
            logger.info("Zookeeper connection string: {}", zkAddress);
            logger.info("Helix cluster: {}", clusterName);
            logger.info("Helix controller: {}", controllerName);
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin.Builder().setZkAddress(this.zkAddress).build();
            logger.info("[zkAdmin] started");
            logger.info("[zkAdmin] Cluster: {} adding if not available...", clusterName);
            zkHelixAdmin.addCluster(this.clusterName, true);
            logger.info("[zkAdmin] Cluster: {} now available!", clusterName);
            zkHelixAdmin.close();
            logger.info("[zkAdmin] closed");

            logger.info("helix controller - starting...");
            zkHelixManager = HelixControllerMain.startHelixController(zkAddress, clusterName, controllerName, HelixControllerMain.STANDALONE);
            logger.info("Helix controller - started");
            startLatch.countDown();
            stopLatch.await();
        } catch (Exception ex) {
            logger.error("Error in run() for Controller: {}, reason: {}", controllerName, ex, ex);
        } finally {
            disconnect();
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void startServer() throws Exception {

        new Thread(this).start();
        try {
            startLatch.await();
            logger.info("Controller: {}, has connected to cluster: {}", controllerName, clusterName);

            Runtime.getRuntime().addShutdownHook(
                    new Thread(this::disconnect)
            );

        } catch (InterruptedException ex) {
            logger.error("Controller: {}, is interrupted! reason: {}", controllerName, ex, ex);
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void stop() {
        stopLatch.countDown();
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            logger.info("Controller: {}, has disconnected from cluster: {}", controllerName, clusterName);
            zkHelixManager.disconnect();
        }
    }

    public static void main(String[] args) {
        try {

            logger.info("Starting helix controller");

            HelixController helixController = new HelixController();
            helixController.startServer();

        } catch (Exception e) {
            logger.error("Failed to start the helix controller", e);
        }
    }
}
