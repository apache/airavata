package org.apache.airavata.helix.cluster.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelixControllerMonitor implements PlatformMonitor {

    private final static Logger logger = LoggerFactory.getLogger(HelixControllerMonitor.class);

    @Override
    public void monitor(ErrorNotifier notifier) {
        logger.info("Monitoring Controller started");
        logger.info("Monitoring Controller finished");
    }
}