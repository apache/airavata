package org.apache.airavata.helix.cluster.monitoring.agents;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.cluster.monitoring.ErrorNotifier;
import org.apache.airavata.helix.cluster.monitoring.PlatformMonitor;
import org.apache.airavata.helix.cluster.monitoring.PlatformMonitorError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ZookeeperMonitor implements PlatformMonitor {

    private final static Logger logger = LoggerFactory.getLogger(ZookeeperMonitor.class);

    private String zkConnection = ServerSettings.getZookeeperConnection();

    public ZookeeperMonitor() throws ApplicationSettingsException {
    }

    public void monitor(ErrorNotifier notifier) {

        logger.info("Monitoring Zookeeper started");

        Socket s = null;

        try {
            s = new Socket(zkConnection.split(":")[0], Integer.parseInt(zkConnection.split(":")[1]));
        } catch (IOException e) {
            PlatformMonitorError monitorError = new PlatformMonitorError();
            monitorError.setError(e);
            monitorError.setReason("Could not establish a connection with Zookeeper " + zkConnection);
            monitorError.setCategory("Zookeeper");
            monitorError.setCategory("ZK001");
            notifier.sendNotification(monitorError);
        } finally {
            if(s != null)
                try {s.close();}
                catch(Exception ignored){}
        }

        logger.info("Monitoring Zookeeper finished");

    }
}