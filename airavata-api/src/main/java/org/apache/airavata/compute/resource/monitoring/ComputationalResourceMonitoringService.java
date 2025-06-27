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
package org.apache.airavata.compute.resource.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.compute.resource.monitoring.job.MonitoringJob;
import org.apache.airavata.compute.resource.monitoring.utils.Constants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computational Resource Monitoring Service
 */
public class ComputationalResourceMonitoringService implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(ComputationalResourceMonitoringService.class);
    private static final String SERVER_NAME = "Airavata Compute Resource Monitoring Service";
    private static final String SERVER_VERSION = "1.0";

    private static ServerStatus status;
    private static Scheduler scheduler;
    private static Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void start() throws Exception {

        jobTriggerMap.clear();
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();

        final String metaUsername = ServerSettings.getMetaschedulerUsername();
        final String metaGatewayId = ServerSettings.getMetaschedulerGateway();
        final String metaGroupResourceProfileId = ServerSettings.getMetaschedulerGrpId();
        final int parallelJobs = ServerSettings.getMetaschedulerNoOfScanningParallelJobs();
        final double scanningInterval = ServerSettings.getMetaschedulerClusterScanningInterval();

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
    public void stop() throws Exception {
        scheduler.unscheduleJobs(jobTriggerMap.values().stream()
                .map(trigger -> {
                    return trigger.getKey();
                })
                .collect(Collectors.toList()));
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void configure() throws Exception {}

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    public void setServerStatus(ServerStatus status) {
        this.status = status;
    }
}
