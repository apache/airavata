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

import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for job scripts to report status. Replaces Kafka REST / status-publish-endpoint.
 * Configure airavata.services.monitor.compute.job-status-callback-url to the base URL of the API
 * plus /api/v1/monitoring/job-status (e.g. http://airavata-api:8080/api/v1/monitoring/job-status).
 *
 * <p>Accepts JSON: {"jobName":"...", "status":"RUNNING"|"COMPLETED"|"FAILED"|..., "task":"taskId"}.
 * Publishes canonical JobStatusUpdateEvent to status-change-topic (same path as email and realtime).
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringJobStatusController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringJobStatusController.class);

    private final JobStatusEventPublisher jobStatusEventPublisher;

    @Autowired
    public MonitoringJobStatusController(@Autowired(required = false) JobStatusEventPublisher jobStatusEventPublisher) {
        this.jobStatusEventPublisher = jobStatusEventPublisher;
    }

    @PostMapping("/job-status")
    public ResponseEntity<Void> jobStatus(@RequestBody JobStatusRequest req) {
        if (jobStatusEventPublisher == null) {
            log.warn("job-status callback received but JobStatusEventPublisher not available");
            return ResponseEntity.ok().build();
        }
        if (req == null || req.jobName == null || req.status == null || req.task == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var event = new MessagingContracts.JobStatusUpdateEvent(
                    req.jobName(),
                    req.status(),
                    req.task(),
                    "job-callback",
                    null);
            jobStatusEventPublisher.publish(event);
        } catch (Exception e) {
            log.error("Error publishing job-status callback jobName={} status={}", req.jobName(), req.status(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    public record JobStatusRequest(String jobName, String status, String task) {}
}
