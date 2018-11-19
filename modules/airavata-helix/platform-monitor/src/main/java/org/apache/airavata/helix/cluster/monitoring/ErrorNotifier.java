package org.apache.airavata.helix.cluster.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorNotifier {

    private final static Logger logger = LoggerFactory.getLogger(ErrorNotifier.class);

    public void sendNotification(PlatformMonitorError monitorError) {
        if (monitorError.getError() == null) {
            logger.error("Monitor error " + monitorError.getReason());
        } else {
            logger.error("Monitor error " + monitorError.getReason(), monitorError.getError());
        }
    }
}
