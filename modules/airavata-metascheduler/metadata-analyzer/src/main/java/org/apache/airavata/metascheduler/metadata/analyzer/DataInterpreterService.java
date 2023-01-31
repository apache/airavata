package org.apache.airavata.metascheduler.metadata.analyzer;

import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.metascheduler.metadata.analyzer.impl.DataAnalyzerImpl;
import org.apache.airavata.metascheduler.metadata.analyzer.utils.Constants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataInterpreterService implements IServer {

    private final static Logger logger = LoggerFactory.getLogger(DataInterpreterService.class);
    private static final String SERVER_NAME = "Data Interpreter Service";
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

        final int parallelJobs = ServerSettings.getDataAnalyzerNoOfScanningParallelJobs();
        final double scanningInterval = ServerSettings.getDataAnalyzerScanningInterval();


        for (int i = 0; i < parallelJobs; i++) {
            String name = Constants.METADATA_SCANNER_TRIGGER + "_" + i;
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, Constants.METADATA_SCANNER_GROUP)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds((int) scanningInterval)
                            .repeatForever())
                    .build();

            String jobName = Constants.METADATA_SCANNER_JOB + "_" + i;

            JobDetail jobC = JobBuilder
                    .newJob(DataAnalyzerImpl.class)
                    .withIdentity(jobName, Constants.METADATA_SCANNER_JOB)
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
