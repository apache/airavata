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
package org.apache.airavata.monitor.compute;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.IServer.ServerStatus;
import org.apache.airavata.config.ServerLifecycle;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Computational Resource Monitoring Service using Spring-managed Quartz scheduler
 */
@Service
@Profile("!test")
@ConditionalOnProperty(name = "services.monitor.compute.enabled", havingValue = "true", matchIfMissing = true)
public class ComputationalResourceMonitoringService extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ComputationalResourceMonitoringService.class);
    private static final String SERVER_NAME = "Airavata Compute Resource Monitoring Service";
    private static final String SERVER_VERSION = "1.0";

    private final Scheduler scheduler;
    private Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();

    /**
     * Constructor with Spring-managed Scheduler injection.
     * The scheduler is configured via QuartzConfig and supports Spring DI in jobs.
     */
    @Autowired
    public ComputationalResourceMonitoringService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public String getServerName() {
        return SERVER_NAME;
    }

    @Override
    public String getServerVersion() {
        return SERVER_VERSION;
    }

    @Override
    public int getPhase() {
        // Start before Thrift servers
        return 5;
    }

    @Override
    protected void doStart() throws Exception {

        jobTriggerMap.clear();

        // Note: These properties are not in AiravataServerProperties yet, using defaults
        // TODO: Add these to AiravataServerProperties if needed
        final String metaUsername = ""; // properties.getMetascheduler().getUsername() when added
        final String metaGatewayId = ""; // properties.getMetascheduler().getGateway() when added
        final String metaGroupResourceProfileId = ""; // properties.getMetascheduler().getGrpId() when added
        final int parallelJobs = 1; // default
        final double scanningInterval = 1800000; // default in milliseconds

        for (int i = 0; i < parallelJobs; i++) {
            String name = ComputeMonitorConstants.COMPUTE_RESOURCE_SCANNER_TRIGGER + "_" + i;
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, ComputeMonitorConstants.COMPUTE_RESOURCE_SCANNER_GROUP)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds((int) scanningInterval)
                            .repeatForever())
                    .build();

            String jobName = ComputeMonitorConstants.COMPUTE_RESOURCE_SCANNER_JOB + "_" + i;

            JobDetail jobC = JobBuilder.newJob(ComputeMonitoringJob.class)
                    .withIdentity(jobName, ComputeMonitorConstants.COMPUTE_RESOURCE_SCANNER_JOB)
                    .usingJobData(ComputeMonitorConstants.METASCHEDULER_SCANNING_JOBS, parallelJobs)
                    .usingJobData(ComputeMonitorConstants.METASCHEDULER_SCANNING_JOB_ID, i)
                    .usingJobData(ComputeMonitorConstants.METASCHEDULER_USERNAME, metaUsername)
                    .usingJobData(ComputeMonitorConstants.METASCHEDULER_GATEWAY, metaGatewayId)
                    .usingJobData(ComputeMonitorConstants.METASCHEDULER_GRP_ID, metaGroupResourceProfileId)
                    .build();
            jobTriggerMap.put(jobC, trigger);
        }
        
        // Scheduler is already started by Spring Boot, just schedule jobs
        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        jobTriggerMap.forEach((x, v) -> {
            try {
                scheduler.scheduleJob(x, v);
            } catch (SchedulerException e) {
                logger.error("Error occurred while scheduling job " + x.getKey().getName());
            }
        });
    }

    @Override
    protected void doStop() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.unscheduleJobs(jobTriggerMap.values().stream()
                    .map(trigger -> trigger.getKey())
                    .collect(Collectors.toList()));
            // Don't shutdown the shared scheduler, just unschedule our jobs
            // The scheduler will be shut down by Spring Boot
        }
    }

    public ServerStatus getStatus() {
        return isRunning() ? ServerStatus.STARTED : ServerStatus.STOPPED;
    }

    public void setServerStatus(ServerStatus status) {
        // Status is now managed by SmartLifecycle.isRunning()
        // This method is kept for backward compatibility with OrchestratorServiceServer
    }
}
