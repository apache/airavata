package org.apache.airavata.helix.impl.controller;

import org.apache.airavata.helix.core.util.PropertyResolver;
import org.apache.helix.controller.HelixControllerMain;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
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

    public HelixController(String propertyFile, boolean readPropertyFromFile) throws IOException {

        PropertyResolver propertyResolver = new PropertyResolver();
        if (readPropertyFromFile) {
            propertyResolver.loadFromFile(new File(propertyFile));
        } else {
            propertyResolver.loadInputStream(this.getClass().getClassLoader().getResourceAsStream(propertyFile));
        }

        this.clusterName = propertyResolver.get("helix.cluster.name");
        this.controllerName = propertyResolver.get("helix.controller.name");
        this.zkAddress = propertyResolver.get("zookeeper.connection.url");
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

            logger.info("Starting helix controller");
            String confDir = null;
            if (args != null) {
                for (String arg : args) {
                    if (arg.startsWith("--confDir=")) {
                        confDir = arg.substring("--confDir=".length());
                    }
                }
            }

            String propertiesFile = "application.properties";
            boolean readPropertyFromFile = false;

            if (confDir != null && !confDir.isEmpty()) {
                propertiesFile = confDir.endsWith(File.separator)? confDir + propertiesFile : confDir + File.separator + propertiesFile;
                readPropertyFromFile = true;
            }

            logger.info("Using configuration file " + propertiesFile);

            HelixController helixController = new HelixController(propertiesFile, readPropertyFromFile);
            helixController.start();

        } catch (IOException e) {
            logger.error("Failed to start the helix controller", e);
        }
    }
}
