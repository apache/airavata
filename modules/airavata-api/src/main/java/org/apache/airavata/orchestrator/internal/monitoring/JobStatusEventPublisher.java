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
package org.apache.airavata.orchestrator.internal.monitoring;

import io.dapr.client.DaprClient;
import java.util.HashMap;
import java.util.List;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.orchestrator.internal.config.DaprConfigConstants;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts;
import org.apache.airavata.orchestrator.JobStatusHandler;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publishes canonical job status events to status-change-topic so all sources (email, realtime, notify API)
 * are handled by the same listener. When Dapr is disabled, delivers directly to JobStatusHandler.
 */
@Component
public class JobStatusEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobStatusEventPublisher.class);
    private static final String META_CONTENT_TYPE = "content-type";

    private final DaprClient daprClient;
    private final String pubsubName;
    private final boolean daprEnabled;
    private final JobStatusHandler jobStatusHandler;
    private final JobStatusEventToResultConverter converter;
    private final RegistryService registryService;

    @Autowired
    public JobStatusEventPublisher(
            @Autowired(required = false) DaprClient daprClient,
            @Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}") boolean daprEnabled,
            @Value("${" + DaprConfigConstants.DAPR_PUBSUB_NAME + ":" + DaprConfigConstants.DEFAULT_PUBSUB_NAME + "}")
                    String pubsubName,
            @Autowired(required = false) JobStatusHandler jobStatusHandler,
            JobStatusEventToResultConverter converter,
            @Autowired(required = false) RegistryService registryService) {
        this.daprClient = daprClient;
        this.daprEnabled = daprEnabled;
        this.pubsubName = pubsubName;
        this.jobStatusHandler = jobStatusHandler;
        this.converter = converter;
        this.registryService = registryService;
    }

    /**
     * Publish a canonical job status event. When Dapr is enabled, publishes to status-change-topic;
     * when disabled, converts and calls JobStatusHandler directly.
     */
    public void publish(MessagingContracts.JobStatusUpdateEvent event) {
        if (daprEnabled && daprClient != null) {
            try {
                var metadata = new HashMap<String, String>();
                metadata.put(META_CONTENT_TYPE, "application/json");
                daprClient
                        .publishEvent(pubsubName, MessagingContracts.DaprTopics.STATUS_CHANGE, event, metadata)
                        .block();
                log.debug("Published job status event to status-change-topic: jobName={} status={}", event.getJobName(), event.getStatus());
            } catch (Exception e) {
                log.error("Error publishing job status event to Dapr", e);
                deliverDirect(event);
            }
        } else {
            deliverDirect(event);
        }
    }

    /**
     * Publish from an already-built JobStatusResult (e.g. from email). Builds canonical event
     * (looks up taskId from registry when needed) and publishes.
     */
    public void publish(JobStatusResult result) {
        if (result == null) return;
        if (daprEnabled && daprClient != null && registryService != null) {
            String taskId = taskIdFromJobId(result.getJobId());
            if (taskId == null) {
                log.warn("Could not resolve taskId for jobId {}; delivering direct", result.getJobId());
                deliverDirect(result);
                return;
            }
            String statusStr = result.getState() != null ? result.getState().name() : null;
            if (statusStr == null) {
                deliverDirect(result);
                return;
            }
            // Map JobState enum to wire string (e.g. COMPLETE -> COMPLETED)
            statusStr = toWireStatus(statusStr);
            var event = new MessagingContracts.JobStatusUpdateEvent(
                    result.getJobName(),
                    statusStr,
                    taskId,
                    result.getPublisherName() != null ? result.getPublisherName() : "email",
                    null);
            publish(event);
        } else {
            deliverDirect(result);
        }
    }

    private static String toWireStatus(String stateName) {
        if ("COMPLETE".equals(stateName)) return "COMPLETED";
        if ("ACTIVE".equals(stateName)) return "RUNNING";
        return stateName;
    }

    private String taskIdFromJobId(String jobId) {
        try {
            List<org.apache.airavata.common.model.JobModel> jobs = registryService.getJobs("jobId", jobId);
            if (jobs != null && !jobs.isEmpty()) {
                return jobs.get(0).getTaskId();
            }
        } catch (RegistryException e) {
            log.debug("Registry lookup for taskId by jobId failed: {}", e.getMessage());
        }
        return null;
    }

    private void deliverDirect(MessagingContracts.JobStatusUpdateEvent event) {
        if (jobStatusHandler == null || converter == null) {
            log.warn("JobStatusHandler or converter not available; job status event dropped");
            return;
        }
        // Converter needs RegistryService; when Dapr disabled we may not have it in publisher context for convert.
        // The controller has RegistryService. So when Dapr disabled, we must have a way to convert. The publisher
        // is used by Notify API and Email - when Dapr disabled they call publish(event) and we need to convert.
        // So the converter needs RegistryService which we have in the controller but publisher also needs it for
        // deliverDirect(event). So inject RegistryService into the publisher (already done as optional). When
        // delivering direct we need to convert event to result - converter.convert(event, registryService). So
        // we need RegistryService in the publisher for deliverDirect. We have it as optional - when Dapr disabled
        // we need it. So use it in deliverDirect: if registryService != null, converter.convert(event, registryService)
        // and then handler.onJobStatusMessage(result). If registryService is null we can't convert - log and drop.
        if (registryService == null) {
            log.warn("RegistryService not available; cannot deliver job status event directly");
            return;
        }
        JobStatusResult result = converter.convert(event, registryService);
        if (result != null) {
            jobStatusHandler.onJobStatusMessage(result);
        }
    }

    private void deliverDirect(JobStatusResult result) {
        if (jobStatusHandler != null) {
            jobStatusHandler.onJobStatusMessage(result);
        } else {
            log.warn("JobStatusHandler not available; job status dropped");
        }
    }
}
