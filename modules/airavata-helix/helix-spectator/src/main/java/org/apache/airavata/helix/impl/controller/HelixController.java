package org.apache.airavata.helix.impl.controller;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class HelixController implements Runnable {

    private static final Logger logger = LogManager.getLogger(HelixController.class);

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

    public void start() {
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
            helixController.start();

        } catch (Exception e) {
            logger.error("Failed to start the helix controller", e);
        }
    }
}
