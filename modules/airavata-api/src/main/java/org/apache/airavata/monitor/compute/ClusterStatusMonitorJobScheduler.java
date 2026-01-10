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

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.airavata.config.AiravataServerProperties;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cluster Status Monitor Job Scheduler using Spring-managed Quartz scheduler.
 */
public class ClusterStatusMonitorJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJobScheduler.class);

    private final Scheduler scheduler;
    private final AiravataServerProperties properties;

    /**
     * Constructor accepting Spring-managed Scheduler.
     * 
     * @param scheduler Spring-managed Quartz scheduler
     * @param properties Server properties
     */
    public ClusterStatusMonitorJobScheduler(Scheduler scheduler, AiravataServerProperties properties) {
        this.scheduler = scheduler;
        this.properties = properties;
    }

    public void scheduleClusterStatusMonitoring() throws SchedulerException {
        // define the job and tie it to our ClusterStatusMonitorJob class
        JobDetail job = newJob(ClusterStatusMonitorJob.class)
                .withIdentity("cluster-status-monitoring", "airavata")
                .build();

        // Trigger the job to run now, and then repeat based on configured interval
        int repeatTime = properties.services().monitor().compute().clusterCheckRepeatTime();
        Trigger trigger = newTrigger()
                .withIdentity("cluster-status-monitoring-trigger", "airavata")
                .startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(repeatTime).repeatForever())
                .build();

        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(job, trigger);
    }
}
