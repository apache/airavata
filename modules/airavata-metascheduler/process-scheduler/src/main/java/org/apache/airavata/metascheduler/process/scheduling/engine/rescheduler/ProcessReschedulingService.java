package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.metascheduler.process.scheduling.utils.Constants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Process rescheduling service to scann the Queue or Requeued services and relaunch them.
 */
public class ProcessReschedulingService implements IServer {

    private final static Logger logger = LoggerFactory.getLogger(ProcessReschedulingService.class);
    private static final String SERVER_NAME = "Airavata Process Rescheduling Service";
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

        final int parallelJobs = ServerSettings.getMetaschedulerNoOfScanningParallelJobs();
        final double scanningInterval = ServerSettings.getMetaschedulerScanningInterval();


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

            JobDetail jobC = JobBuilder
                    .newJob(ProcessScannerImpl.class)
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
    public void configure() throws Exception {

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }


    public void setServerStatus(ServerStatus status) {
        this.status = status;
    }

}
