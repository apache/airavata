package org.apache.airavata.monitor;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.monitor.kafka.MessageProducer;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AbstractMonitor {

    private static final Logger log = LoggerFactory.getLogger(AbstractMonitor.class);

    private MessageProducer messageProducer;
    private CuratorFramework curatorClient;
    private ThriftClientPool<RegistryService.Client> registryClientPool;

    public AbstractMonitor() throws ApplicationSettingsException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
        this.initRegistryClientPool();
        messageProducer = new MessageProducer();
    }

    private void initRegistryClientPool() throws ApplicationSettingsException {

        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);

        this.registryClientPool = new ThriftClientPool<>(
                RegistryService.Client::new, poolConfig, ServerSettings.getRegistryServerHost(),
                Integer.parseInt(ServerSettings.getRegistryServerPort()));
    }

    private boolean validateJobStatus(JobStatusResult jobStatusResult) {
        RegistryService.Client registryClient = getRegistryClientPool().getResource();
        boolean validated = true;
        try {
            log.info("Fetching matching jobs for job id {} from registry", jobStatusResult.getJobId());
            List<JobModel> jobs = registryClient.getJobs("jobId", jobStatusResult.getJobId());

            if (jobs.size() > 0) {
                log.info("Filtering total " + jobs.size() + " with target job name " + jobStatusResult.getJobName());
                jobs = jobs.stream().filter(jm -> jm.getJobName().equals(jobStatusResult.getJobName())).collect(Collectors.toList());
            }

            if (jobs.size() != 1) {
                log.error("Couldn't find exactly one job with id " + jobStatusResult.getJobId() + " and name " +
                        jobStatusResult.getJobName() + " in the registry. Count " + jobs.size());
                validated = false;

            } else  {
                JobModel jobModel = jobs.get(0);

                String processId = jobModel.getProcessId();
                String experimentId = registryClient.getProcess(processId).getExperimentId();

                if (experimentId != null && processId != null) {
                    log.info("Job id " + jobStatusResult.getJobId() + " is owned by process " + processId + " of experiment " + experimentId);
                    validated = true;
                } else {
                    log.error("Experiment or process is null for job " + jobStatusResult.getJobId());
                    validated = false;
                }
            }
            getRegistryClientPool().returnResource(registryClient);
            return validated;

        } catch (Exception e) {
            log.error("Error at validating job status " + jobStatusResult.getJobId(), e);
            getRegistryClientPool().returnBrokenResource(registryClient);
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

    public ThriftClientPool<RegistryService.Client> getRegistryClientPool() {
        return registryClientPool;
    }
}
