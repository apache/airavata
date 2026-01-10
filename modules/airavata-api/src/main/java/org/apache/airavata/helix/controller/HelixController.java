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
package org.apache.airavata.helix.controller;

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient.RealmMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Helix Controller manages the Helix cluster for workflow orchestration.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "services.controller", name = "enabled", havingValue = "true")
public class HelixController extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(HelixController.class);

    private final AiravataServerProperties properties;
    private String clusterName;
    private String controllerName;
    private String zkAddress;
    private org.apache.helix.HelixManager zkHelixManager;

    private Thread controllerThread;

    @SuppressWarnings("WeakerAccess")
    public HelixController(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        this.clusterName = properties.helix().cluster().name();
        this.controllerName = properties.helix().controller().name();
        this.zkAddress = properties.zookeeper().server().connection();
    }

    @Override
    public String getServerName() {
        return "Helix Controller";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 10; // Start early, after telemetry
    }

    @Override
    protected void doStart() throws Exception {
        controllerThread = new Thread(() -> {
            try {
                ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin.Builder()
                        .setRealmMode(RealmMode.SINGLE_REALM)
                        .setZkAddress(zkAddress)
                        .build();

                // Creates the zk cluster if not available
                if (!zkHelixAdmin.getClusters().contains(clusterName)) {
                    zkHelixAdmin.addCluster(clusterName, true);
                }

                zkHelixAdmin.close();

                logger.info("Connection to helix cluster : " + clusterName + " with name : " + controllerName);
                logger.info("Zookeeper connection string " + zkAddress);

                zkHelixManager = HelixControllerMain.startHelixController(
                        zkAddress, clusterName, controllerName, HelixControllerMain.STANDALONE);
                logger.info("Controller: " + controllerName + ", has connected to cluster: " + clusterName);

                // Keep thread alive until interrupted
                Thread.currentThread().join();
            } catch (InterruptedException ex) {
                logger.debug("Controller thread interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.error("Error in controller thread for Controller: " + controllerName + ", reason: " + ex, ex);
            } finally {
                disconnect();
            }
        });
        controllerThread.setName(getServerName());
        controllerThread.setDaemon(true);
        controllerThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        if (controllerThread != null) {
            controllerThread.interrupt();
            try {
                controllerThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for controller thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        disconnect();
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && controllerThread != null && controllerThread.isAlive();
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            logger.info("Controller: " + controllerName + ", has disconnected from cluster: " + clusterName);
            zkHelixManager.disconnect();
        }
    }
}
