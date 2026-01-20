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
package org.apache.airavata.dapr.monitor;

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.MonitoringException;
import org.apache.airavata.monitor.realtime.RealtimeComputeStatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Realtime monitor using Dapr Pub/Sub. Consumes job status messages from
 * monitoring-data-topic (pushed by Dapr to /api/v1/dapr/pubsub/monitoring-data-topic)
 * and processes them via {@link DaprMonitoringHandler#onMonitoringMessage}.
 *
 * <p>Configure via application.properties:
 * <ul>
 *   <li>airavata.dapr.enabled=true
 *   <li>airavata.services.monitor.realtime.enabled=true
 *   <li>Topic/routes in dapr/components/monitoring-data-subscription.yaml
 * </ul>
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.monitor.realtime", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
public class RealtimeMonitor extends ServerLifecycle implements DaprMonitoringHandler {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeMonitor.class);

    private final AiravataServerProperties properties;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final RealtimeComputeStatusParser parser;
    private final AbstractMonitor abstractMonitor;
    private String publisherId;
    private boolean started;

    public RealtimeMonitor(
            org.apache.airavata.service.registry.RegistryService registryService,
            AiravataServerProperties properties,
            AbstractMonitor abstractMonitor) {
        this.registryService = registryService;
        this.properties = properties;
        this.abstractMonitor = abstractMonitor;
        this.parser = new RealtimeComputeStatusParser();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        publisherId = properties.services().monitor().compute().realtimePublisherId();
    }

    @Override
    public void onMonitoringMessage(String key, String payload) {
        try {
            process(key, payload);
        } catch (Exception e) {
            logger.error("Error while processing monitoring message: {}", payload, e);
        }
    }

    private void process(String key, String value) throws MonitoringException {
        logger.info("received post from {} on {}: {}->{}", publisherId, MessageProducer.MONITORING_TOPIC, key, value);
        JobStatusResult statusResult = parser.parse(value, publisherId, getRegistryService());
        if (statusResult != null) {
            logger.info("Submitting message to job monitor queue");
            abstractMonitor.submitJobStatus(statusResult);
        } else {
            logger.warn("Ignoring message as it is invalid");
        }
    }

    @Override
    public String getServerName() {
        return "Realtime Monitor";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 35; // Start after workflow managers
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("Starting RealtimeMonitor (Dapr Pub/Sub) for topic: {}", MessageProducer.MONITORING_TOPIC);
        started = true;
        logger.info("RealtimeMonitor started; Dapr delivers to /api/v1/dapr/pubsub/monitoring-data-topic");
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("Stopping RealtimeMonitor");
        started = false;
        logger.info("RealtimeMonitor stopped");
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    protected org.apache.airavata.service.registry.RegistryService getRegistryService() {
        return registryService;
    }
}
