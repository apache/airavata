package org.apache.airavata.monitor;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.monitor.kafka.MessageProducer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractMonitor {

    private static final Logger log = LoggerFactory.getLogger(AbstractMonitor.class);

    private MessageProducer messageProducer;
    private CuratorFramework curatorClient;

    public AbstractMonitor() throws ApplicationSettingsException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
        messageProducer = new MessageProducer();
    }

    private boolean validateJobStatus(JobStatusResult jobStatusResult) {
        try {
            String experimentId = MonitoringUtil.getExperimentIdByJobId(curatorClient, jobStatusResult.getJobId());
            String processId = MonitoringUtil.getProcessIdByJobId(curatorClient, jobStatusResult.getJobId());
            if (experimentId != null && processId != null) {
                log.info("Job id " + jobStatusResult.getJobId() + " is owned by process " + processId + " of experiment " + experimentId);
                return true;
            } else {
                log.error("Experiment or process is null for job " + jobStatusResult.getJobId());
                return false;
            }
        } catch (Exception e) {
            log.error("Error at validating job status " + jobStatusResult.getJobId(), e);
            return false;
        }

    }

    public void submitJobStatus(JobStatusResult jobStatusResult) throws MonitoringException {
        try {
            if (validateJobStatus(jobStatusResult)) {
                messageProducer.submitMessageToQueue(jobStatusResult);
            } else {
                throw new MonitoringException("Failed to validate job status for job id " + jobStatusResult.getJobId());
            }
        } catch (Exception e) {
            throw new MonitoringException("Failed to submit job status for job id " + jobStatusResult.getJobId() + " to status queue", e);
        }
    }
}
