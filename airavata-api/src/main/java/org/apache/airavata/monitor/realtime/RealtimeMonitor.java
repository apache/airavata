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
package org.apache.airavata.monitor.realtime;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.airavata.monitor.kafka.MessageProducer;
import org.apache.airavata.monitor.realtime.parser.RealtimeJobStatusParser;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealtimeMonitor implements AbstractMonitor, IServer {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    private final RealtimeJobStatusParser parser;
    private final String publisherId;
    private final String brokerTopic;
    private final ThriftClientPool<RegistryService.Client> registryClientPool;
    private final MessageProducer messageProducer;
    private IServer.ServerStatus status = IServer.ServerStatus.STOPPED;

    public RealtimeMonitor() throws ApplicationSettingsException {
        parser = new RealtimeJobStatusParser();
        publisherId = ServerSettings.getSetting("job.monitor.realtime.publisher.id");
        brokerTopic = ServerSettings.getSetting("realtime.monitor.broker.topic");
        registryClientPool = createRegistryClientPool();
        messageProducer = new MessageProducer();
    }

    private static ThriftClientPool<RegistryService.Client> createRegistryClientPool()
            throws ApplicationSettingsException {
        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMinutes(5));
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWait(Duration.ofSeconds(3));
        return new ThriftClientPool<>(
                RegistryService.Client::new,
                poolConfig,
                ServerSettings.getRegistryServerHost(),
                Integer.parseInt(ServerSettings.getRegistryServerPort()),
                "RegistryService");
    }

    @Override
    public void submitJobStatus(JobStatusResult jobStatusResult) throws MonitoringException {
        try {
            if (validateJobStatus(jobStatusResult)) {
                messageProducer.submitMessageToQueue(jobStatusResult);
            } else {
                throw new MonitoringException("Failed to validate job status for job id " + jobStatusResult.getJobId());
            }
        } catch (MonitoringException e) {
            throw e;
        } catch (Exception e) {
            throw new MonitoringException(
                    "Failed to submit job status for job id " + jobStatusResult.getJobId() + " to status queue", e);
        }
    }

    private boolean validateJobStatus(JobStatusResult jobStatusResult) {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            List<JobModel> jobs = registryClient.getJobs("jobId", jobStatusResult.getJobId());
            if (!jobs.isEmpty()) {
                jobs = jobs.stream()
                        .filter(jm -> jm.getJobName().equals(jobStatusResult.getJobName()))
                        .toList();
            }
            if (jobs.size() != 1) {
                logger.error(
                        "Couldn't find exactly one job with id {} and name {} in registry. Count {}",
                        jobStatusResult.getJobId(),
                        jobStatusResult.getJobName(),
                        jobs.size());
                registryClientPool.returnResource(registryClient);
                return false;
            }
            JobModel jobModel = jobs.get(0);
            String processId = jobModel.getProcessId();
            String experimentId = registryClient.getProcess(processId).getExperimentId();
            registryClientPool.returnResource(registryClient);
            if (experimentId != null && processId != null) {
                return true;
            }
            logger.error("Experiment or process null for job {}", jobStatusResult.getJobId());
            return false;
        } catch (Exception e) {
            logger.error("Error validating job status {}", jobStatusResult.getJobId(), e);
            registryClientPool.returnBrokenResource(registryClient);
            return false;
        }
    }

    private Consumer<String, String> createConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("realtime.monitor.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(brokerTopic));
        return consumer;
    }

    private void runConsumer() throws ApplicationSettingsException {
        final Consumer<String, String> consumer = createConsumer();

        while (!Thread.currentThread().isInterrupted()) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            RegistryService.Client registryClient = registryClientPool.getResource();
            consumerRecords.forEach(record -> {
                try {
                    process(record.key(), record.value(), registryClient);
                } catch (Exception e) {
                    logger.error("Error while processing message {}", record.value(), e);
                }
            });
            registryClientPool.returnResource(registryClient);
            consumer.commitAsync();
        }
    }

    private void process(String key, String value, RegistryService.Client registryClient) throws MonitoringException {
        logger.info("received post from {} on {}: {}->{}", publisherId, brokerTopic, key, value);
        JobStatusResult statusResult = parser.parse(value, publisherId, registryClient);
        if (statusResult != null) {
            logger.info("Submitting message to job monitor queue");
            submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }
    }

    @Override
    public void run() {
        status = IServer.ServerStatus.STARTED;
        try {
            runConsumer();
        } catch (ApplicationSettingsException e) {
            logger.error("RealtimeMonitor failed to start consumer", e);
            status = IServer.ServerStatus.FAILED;
        }
    }

    @Override
    public String getName() {
        return "realtime_monitor";
    }

    @Override
    public void stop() throws Exception {
        status = IServer.ServerStatus.STOPPING;
        status = IServer.ServerStatus.STOPPED;
    }

    @Override
    public IServer.ServerStatus getStatus() {
        return status;
    }

    public static void main(String args[]) throws ApplicationSettingsException {
        new RealtimeMonitor().run();
    }
}
