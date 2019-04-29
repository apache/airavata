package org.apache.airavata.helix.cluster.monitoring;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.cluster.monitoring.agents.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainMonitor {

    private final static Logger logger = LoggerFactory.getLogger(MainMonitor.class);

    public static void main(String args[]) throws Exception {

        logger.info("Starting platform monitor");

        List<PlatformMonitor> platformMonitors = Arrays.asList(new ApiServerMonitor(),
                new DbMonitor(), new HelixControllerMonitor(),
                new HelixParticipantMonitor(), new ZookeeperMonitor());

        ErrorNotifier errorNotifier = new ErrorNotifier();

        for (PlatformMonitor monitor : platformMonitors) {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleWithFixedDelay(() -> monitor.monitor(errorNotifier), 0,
                    Integer.parseInt(ServerSettings.getSetting("platform_monitor_interval_minutes")),
                    TimeUnit.MINUTES);
        }
    }
}