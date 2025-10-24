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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixController implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HelixController.class);

    private String clusterName;
    private String controllerName;
    private String zkAddress;
    private org.apache.helix.HelixManager zkHelixManager;

    @SuppressWarnings("WeakerAccess")
    public HelixController() throws ApplicationSettingsException {
        this.clusterName = ServerSettings.getSetting("helix.cluster.name");
        this.controllerName = ServerSettings.getSetting("helix.controller.name");
        this.zkAddress = ServerSettings.getZookeeperConnection();
    }

    public void run() {
        try {
            var zkHelixAdmin = new ZKHelixAdmin.Builder()
                    .setZkAddress(ServerSettings.getZookeeperConnection())
                    .build();
            zkHelixAdmin.addCluster(clusterName, false);
            zkHelixAdmin.close();

            logger.info(
                    "Starting helix controller '{}' for cluster '{}' at address '{}'",
                    controllerName,
                    clusterName,
                    zkAddress);
            zkHelixManager = HelixControllerMain.startHelixController(
                    zkAddress, clusterName, controllerName, HelixControllerMain.STANDALONE);
            logger.info("Controller '{}' started for cluster '{}'", controllerName, clusterName);
            Thread.currentThread().join();
        } catch (InterruptedException ie) {
            logger.info("Helix controller interrupted, shutting down.");
        } catch (Exception ex) {
            logger.error("Error in run() for controller '{}', reason: {}", controllerName, ex, ex);
        } finally {
            if (zkHelixManager != null) {
                logger.info("Controller '{}' has disconnected from cluster '{}'", controllerName, clusterName);
                zkHelixManager.disconnect();
            }
        }
    }

    public void start() throws Exception {
        Thread controllerThread = new Thread(this, this.getClass().getSimpleName());
        controllerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(controllerThread::interrupt));
    }
}
