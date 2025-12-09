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
package org.apache.airavata.helix.impl.controller;

import java.util.concurrent.CountDownLatch;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Component
public class HelixController implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HelixController.class);

    private final AiravataServerProperties properties;
    private String clusterName;
    private String controllerName;
    private String zkAddress;
    private org.apache.helix.HelixManager zkHelixManager;

    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch stopLatch = new CountDownLatch(1);

    @SuppressWarnings("WeakerAccess")
    public HelixController(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        this.clusterName = properties.helix.clusterName;
        this.controllerName = properties.helix.controllerName;
        this.zkAddress = properties.zookeeper.serverConnection;
    }

    public void run() {
        try {
            ZkClient zkClient = new ZkClient(
                    zkAddress,
                    ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT,
                    new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            // Creates the zk cluster if not available
            if (!zkHelixAdmin.getClusters().contains(clusterName)) {
                zkHelixAdmin.addCluster(clusterName, true);
            }

            zkHelixAdmin.close();
            zkClient.close();

            logger.info("Connection to helix cluster : " + clusterName + " with name : " + controllerName);
            logger.info("Zookeeper connection string " + zkAddress);

            zkHelixManager = HelixControllerMain.startHelixController(
                    zkAddress, clusterName, controllerName, HelixControllerMain.STANDALONE);
            startLatch.countDown();
            stopLatch.await();
        } catch (Exception ex) {
            logger.error("Error in run() for Controller: " + controllerName + ", reason: " + ex, ex);
        } finally {
            disconnect();
        }
    }

    public void startServer() throws Exception {

        // WorkflowCleanupAgent cleanupAgent = new WorkflowCleanupAgent();
        // cleanupAgent.init();
        // ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        // executor.scheduleWithFixedDelay(cleanupAgent, 10, 120, TimeUnit.SECONDS);

        new Thread(this).start();
        try {
            startLatch.await();
            logger.info("Controller: " + controllerName + ", has connected to cluster: " + clusterName);
        } catch (InterruptedException ex) {
            logger.error("Controller: " + controllerName + ", is interrupted! reason: " + ex, ex);
        }
    }

    /**
     * Standardized start method for Spring Boot integration.
     * Non-blocking: starts internal thread and returns immediately.
     */
    public void start() throws Exception {
        startServer();
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
        // Note: HelixController is a Spring component and requires AiravataServerProperties.
        // This main method should be run within a Spring application context.
        // For standalone execution, use Spring Boot application or provide dependencies manually.
        throw new UnsupportedOperationException("HelixController must be used within a Spring application context");
    }
}
