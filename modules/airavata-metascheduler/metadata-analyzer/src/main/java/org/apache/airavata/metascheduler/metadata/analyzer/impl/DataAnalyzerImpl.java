package org.apache.airavata.metascheduler.metadata.analyzer.impl;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.DataAnalyzer;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAnalyzerImpl implements DataAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalyzerImpl.class);

    protected static ThriftClientPool<RegistryService.Client> registryClientPool = Utils.getRegistryServiceClientPool();


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RegistryService.Client client = null;

        try {
            LOGGER.debug("Executing Data Analyzer ....... ");
            client = this.registryClientPool.getResource();

            //TODO: handle multiple gateways
            String gateway = ServerSettings.getDataAnalyzingEnabledGateways();

            JobState state = JobState.SUBMITTED;
            JobStatus jobStatus = new JobStatus();
            jobStatus.setJobState(state);
            double time = ServerSettings.getDataAnalyzerTimeStep();

            int fiveMinuteCount = client.getJobCount(jobStatus, gateway, 5);

            int tenMinuteCount = client.getJobCount(jobStatus, gateway, 10);

            int fifteenMinuteCount = client.getJobCount(jobStatus, gateway, 15);


            double fiveMinuteAverage = fiveMinuteCount * time / (5 * 60);

            double tenMinuteAverage = tenMinuteCount * time / (10 * 60);

            double fifteenMinuteAverage = fifteenMinuteCount * time / (10 * 60);

            LOGGER.info("service rate: 5 min avg" + fiveMinuteAverage + " 10 min avg "
                    + tenMinuteAverage + " 15 min avg " + fifteenMinuteAverage);

        } catch (Exception ex) {
            String msg = "Error occurred while executing data analyzer" + ex.getMessage();
            LOGGER.error(msg, ex);
            if (client != null) {
                this.registryClientPool.returnBrokenResource(client);
            }
            client = null;
        } finally {
            if (client != null) {
                this.registryClientPool.returnResource(client);
            }
        }


    }


}
