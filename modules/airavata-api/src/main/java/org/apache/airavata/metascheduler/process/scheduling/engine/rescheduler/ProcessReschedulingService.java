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
package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.utils.IServer.ServerStatus;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.metascheduler.process.scheduling.utils.Constants;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 * Process rescheduling service to scann the Queue or Requeued services and relaunch them.
 */
@Component
@ConditionalOnProperty(name = "services.scheduler.rescheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ProcessReschedulingService extends ServerLifecycle {

    private static final String SERVER_NAME = "Airavata Process Rescheduling Service";
    private static final String SERVER_VERSION = "1.0";

    private Scheduler scheduler;
    private Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();

    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContext;

    public ProcessReschedulingService(AiravataServerProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
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
        return 6;
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

        final int parallelJobs = properties.services.scheduler.clusterScanningParallelJobs;
        final double scanningInterval = properties.services.scheduler.jobScanningInterval;

        for (int i = 0; i < parallelJobs; i++) {
            String name = Constants.PROCESS_SCANNER_TRIGGER + "_" + i;
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, Constants.PROCESS_SCANNER_GROUP)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds((int) scanningInterval)
                            .repeatForever())
                    .build();

            String jobName = Constants.PROCESS_SCANNER_JOB + "_" + i;

            JobDetail jobC = JobBuilder.newJob(ProcessScannerImpl.class)
                    .withIdentity(jobName, Constants.PROCESS_SCANNER_JOB)
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
                    .collect(java.util.stream.Collectors.toList()));
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
