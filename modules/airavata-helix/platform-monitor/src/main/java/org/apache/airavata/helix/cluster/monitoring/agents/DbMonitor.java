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

public class DbMonitor implements PlatformMonitor {

    private final static Logger logger = LoggerFactory.getLogger(DbMonitor.class);

    private String dbServerHost = ServerSettings.getSetting("database.host");
    private String dbPort = ServerSettings.getSetting("database.port");

    public DbMonitor() throws ApplicationSettingsException {
    }

    public void monitor(ErrorNotifier notifier) {

        logger.info("Monitoring Database Server started");

        Socket s = null;

        try {
            s = new Socket(dbServerHost, Integer.parseInt(dbPort));
        } catch (IOException e) {
            PlatformMonitorError monitorError = new PlatformMonitorError();
            monitorError.setError(e);
            monitorError.setReason("Could not establish a connection with Database " + dbServerHost + ":" + dbPort);
            monitorError.setCategory("Database");
            monitorError.setCategory("DB001");
            notifier.sendNotification(monitorError);
        } finally {
            if(s != null)
                try {s.close();}
                catch(Exception ignored){}
        }

        logger.info("Monitoring Database Server finished");

    }
}
