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
import org.apache.airavata.monitor.compute.job.MonitoringJob;
import org.apache.airavata.monitor.compute.utils.Constants;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Service;

/**
 * Computational Resource Monitoring Service
 */
@Service
@Profile("!test")
public class ComputationalResourceMonitoringService extends ServerLifecycle {

    private static final String SERVER_NAME = "Airavata Compute Resource Monitoring Service";
    private static final String SERVER_VERSION = "1.0";

    private Scheduler scheduler;
    private Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();
    private final ApplicationContext applicationContext;

    public ComputationalResourceMonitoringService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        // Use SpringBeanJobFactory to enable Spring DI for Quartz jobs
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.afterPropertiesSet();
        scheduler = schedulerFactoryBean.getScheduler();

        // Note: These properties are not in AiravataServerProperties yet, using defaults
        // TODO: Add these to AiravataServerProperties if needed
        final String metaUsername = ""; // properties.getMetascheduler().getUsername() when added
        final String metaGatewayId = ""; // properties.getMetascheduler().getGateway() when added
        final String metaGroupResourceProfileId = ""; // properties.getMetascheduler().getGrpId() when added
        final int parallelJobs = 1; // default
        final double scanningInterval = 1800000; // default in milliseconds

        for (int i = 0; i < parallelJobs; i++) {
            String name = Constants.COMPUTE_RESOURCE_SCANNER_TRIGGER + "_" + i;
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, Constants.COMPUTE_RESOURCE_SCANNER_GROUP)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds((int) scanningInterval)
                            .repeatForever())
                    .build();

            String jobName = Constants.COMPUTE_RESOURCE_SCANNER_JOB + "_" + i;

            JobDetail jobC = JobBuilder.newJob(MonitoringJob.class)
                    .withIdentity(jobName, Constants.COMPUTE_RESOURCE_SCANNER_JOB)
                    .usingJobData(Constants.METASCHEDULER_SCANNING_JOBS, parallelJobs)
                    .usingJobData(Constants.METASCHEDULER_SCANNING_JOB_ID, i)
                    .usingJobData(Constants.METASCHEDULER_USERNAME, metaUsername)
                    .usingJobData(Constants.METASCHEDULER_GATEWAY, metaGatewayId)
                    .usingJobData(Constants.METASCHEDULER_GRP_ID, metaGroupResourceProfileId)
                    .build();
            jobTriggerMap.put(jobC, trigger);
        }
        scheduler.start();

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
            scheduler.shutdown();
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
