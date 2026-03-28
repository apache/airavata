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
package org.apache.airavata.execution.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.config.ServerSettings;
import org.apache.airavata.common.server.IServer;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataInterpreterService implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(DataInterpreterService.class);
    private static final String SERVER_NAME = "Data Interpreter Service";

    private static ServerStatus status;
    private static Scheduler scheduler;
    private static Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void run() {
        status = ServerStatus.STARTED;
        try {
            jobTriggerMap.clear();
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();

            final int parallelJobs = ServerSettings.getDataAnalyzerNoOfScanningParallelJobs();
            final double scanningInterval = ServerSettings.getDataAnalyzerScanningInterval();

            for (int i = 0; i < parallelJobs; i++) {
                String name = AnalyzerConstants.METADATA_SCANNER_TRIGGER + "_" + i;
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(name, AnalyzerConstants.METADATA_SCANNER_GROUP)
                        .startNow()
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds((int) scanningInterval)
                                .repeatForever())
                        .build();

                String jobName = AnalyzerConstants.METADATA_SCANNER_JOB + "_" + i;

                JobDetail jobC = JobBuilder.newJob(DataAnalyzerImpl.class)
                        .withIdentity(jobName, AnalyzerConstants.METADATA_SCANNER_JOB)
                        .build();
                jobTriggerMap.put(jobC, trigger);
            }
            scheduler.start();

            jobTriggerMap.forEach((x, v) -> {
                try {
                    scheduler.scheduleJob(x, v);
                } catch (SchedulerException e) {
                    logger.error(
                            "Error occurred while scheduling job " + x.getKey().getName());
                }
            });

            // Quartz scheduler runs on its own threads; park here until interrupted
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            logger.error("DataInterpreterService failed", e);
            status = ServerStatus.FAILED;
        }
    }

    @Override
    public void stop() throws Exception {
        status = ServerStatus.STOPPING;
        scheduler.unscheduleJobs(new ArrayList(jobTriggerMap.values()));
        status = ServerStatus.STOPPED;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }
}
