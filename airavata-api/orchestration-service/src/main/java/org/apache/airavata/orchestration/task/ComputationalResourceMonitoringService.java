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
package org.apache.airavata.orchestration.task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Computational Resource Monitoring Service
 */
public class ComputationalResourceMonitoringService implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(ComputationalResourceMonitoringService.class);
    private static final String SERVER_NAME = "Computational Resource Monitoring";

    private static ServerStatus status;
    private ThreadPoolTaskScheduler taskScheduler;
    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void run() {
        status = ServerStatus.STARTED;
        try {
            final String metaUsername = ServerSettings.getMetaschedulerUsername();
            final String metaGatewayId = ServerSettings.getMetaschedulerGateway();
            final String metaGroupResourceProfileId = ServerSettings.getMetaschedulerGrpId();
            final int parallelJobs = ServerSettings.getMetaschedulerNoOfScanningParallelJobs();
            final double scanningInterval = ServerSettings.getMetaschedulerClusterScanningInterval();

            taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(parallelJobs);
            taskScheduler.setThreadNamePrefix("cr-monitor-");
            taskScheduler.initialize();

            Duration interval = Duration.ofSeconds((long) scanningInterval);

            for (int i = 0; i < parallelJobs; i++) {
                MonitoringJob job =
                        new MonitoringJob(metaGatewayId, metaGroupResourceProfileId, metaUsername, parallelJobs, i);
                ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(job, interval);
                scheduledFutures.add(future);
            }

            // Park here until interrupted (matching original IServer.run() contract)
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            logger.error("ComputationalResourceMonitoringService failed", e);
            status = ServerStatus.FAILED;
        }
    }

    @Override
    public void stop() throws Exception {
        status = ServerStatus.STOPPING;
        scheduledFutures.forEach(f -> f.cancel(false));
        scheduledFutures.clear();
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
        status = ServerStatus.STOPPED;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }
}
