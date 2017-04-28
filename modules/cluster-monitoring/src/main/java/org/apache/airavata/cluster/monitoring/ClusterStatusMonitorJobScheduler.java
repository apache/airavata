/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.cluster.monitoring;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class ClusterStatusMonitorJobScheduler {
    private final static Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJobScheduler.class);

    Scheduler scheduler;

    public ClusterStatusMonitorJobScheduler() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    public void scheduleClusterStatusMonitoring() throws SchedulerException, ApplicationSettingsException {
        // define the job and tie it to our MyJob class
        JobDetail job = newJob(ClusterStatusMonitorJob.class)
                .withIdentity("cluster-status-monitoring", "airavata")
                .build();

        // Trigger the job to run now, and then repeat every 40 seconds
        Trigger trigger = newTrigger()
                .withIdentity("cluster-status-monitoring-trigger", "airavata")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(ServerSettings.getClusterStatusMonitoringRepatTime()))
                        .repeatForever())
                .build();

        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(job, trigger);
    }

    public static void main(String[] args) throws SchedulerException, ApplicationSettingsException {
        ClusterStatusMonitorJobScheduler jobScheduler = new ClusterStatusMonitorJobScheduler();
        jobScheduler.scheduleClusterStatusMonitoring();
    }
}