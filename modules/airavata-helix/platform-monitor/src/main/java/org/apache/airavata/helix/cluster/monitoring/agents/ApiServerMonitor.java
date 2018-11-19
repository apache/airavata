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

public class ApiServerMonitor implements PlatformMonitor {

    private final static Logger logger = LoggerFactory.getLogger(PlatformMonitor.class);

    private String apiServerHost =ServerSettings.getSetting("api.server.host");
    private String apiServerPort = ServerSettings.getSetting("api.server.port");

    public ApiServerMonitor() throws ApplicationSettingsException {
    }

    public void monitor(ErrorNotifier notifier) {

        logger.info("Monitoring API Server started");
        Socket s = null;

        try {
            s = new Socket(apiServerHost, Integer.parseInt(apiServerPort));
        } catch (IOException e) {
            PlatformMonitorError monitorError = new PlatformMonitorError();
            monitorError.setError(e);
            monitorError.setReason("Could not establish a connection with Api Server " + apiServerHost + ":" + apiServerPort);
            monitorError.setCategory("ApiServer");
            monitorError.setCategory("AS001");
            notifier.sendNotification(monitorError);
        } finally {
            if(s != null)
                try {s.close();}
                catch(Exception ignored){}
        }

        logger.info("Monitoring API Server finished");

    }
}
