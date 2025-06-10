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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.metascheduler.process.scheduling.utils.Constants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process rescheduling service to scann the Queue or Requeued services and relaunch them.
 */
public class ProcessReschedulingService implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(ProcessReschedulingService.class);
    private static final String SERVER_NAME = "Airavata Process Rescheduling Service";
    private static final String SERVER_VERSION = "1.0";

    private static ServerStatus status;
    private static Scheduler scheduler;
    private static Map<JobDetail, Trigger> jobTriggerMap = new HashMap<>();

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    @Override
    public void start() throws Exception {

        jobTriggerMap.clear();
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();

        final int parallelJobs = ServerSettings.getMetaschedulerNoOfScanningParallelJobs();
        final double scanningInterval = ServerSettings.getMetaschedulerJobScanningInterval();

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
    public void stop() throws Exception {
        scheduler.unscheduleJobs(new ArrayList(jobTriggerMap.values()));
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
