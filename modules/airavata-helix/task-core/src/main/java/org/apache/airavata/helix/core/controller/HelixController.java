package org.apache.airavata.helix.core.controller;

import org.apache.airavata.helix.core.util.PropertyResolver;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
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

    public HelixController(String propertyFile) throws IOException {

        PropertyResolver propertyResolver = new PropertyResolver();
        propertyResolver.loadInputStream(this.getClass().getClassLoader().getResourceAsStream(propertyFile));

        this.clusterName = propertyResolver.get("helix.cluster.name");
        this.controllerName = propertyResolver.get("helix.controller.name");
        this.zkAddress = propertyResolver.get("zookeeper.connection.url");
    }

    public void run() {
        try {
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
                    new Thread() {
                        @Override
                        public void run() {
                            disconnect();
                        }
                    }
            );

        } catch (InterruptedException ex) {
            logger.error("Controller: " + controllerName + ", is interrupted! reason: " + ex, ex);
        }
    }

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
            HelixController helixController = new HelixController("application.properties");
            helixController.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
