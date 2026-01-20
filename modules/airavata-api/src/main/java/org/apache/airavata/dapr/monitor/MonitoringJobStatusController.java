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

import org.apache.airavata.common.model.JobState;
import org.apache.airavata.dapr.messaging.DaprJobStatusHandler;
import org.apache.airavata.monitor.JobStatusResult;
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
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringJobStatusController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringJobStatusController.class);

    private final DaprJobStatusHandler jobStatusHandler;

    @Autowired
    public MonitoringJobStatusController(@Autowired(required = false) DaprJobStatusHandler jobStatusHandler) {
        this.jobStatusHandler = jobStatusHandler;
    }

    @PostMapping("/job-status")
    public ResponseEntity<Void> jobStatus(@RequestBody JobStatusRequest req) {
        if (jobStatusHandler == null) {
            log.warn("job-status callback received but DaprJobStatusHandler not available");
            return ResponseEntity.ok().build();
        }
        if (req == null || req.jobName == null || req.status == null || req.task == null) {
            return ResponseEntity.badRequest().build();
        }
        JobState state =
                switch (req.status.toUpperCase()) {
                    case "RUNNING" -> JobState.ACTIVE;
                    case "COMPLETED" -> JobState.COMPLETE;
                    case "FAILED" -> JobState.FAILED;
                    case "SUBMITTED" -> JobState.SUBMITTED;
                    case "QUEUED" -> JobState.QUEUED;
                    case "CANCELED" -> JobState.CANCELED;
                    case "SUSPENDED" -> JobState.SUSPENDED;
                    case "NON_CRITICAL_FAIL" -> JobState.NON_CRITICAL_FAIL;
                    default -> JobState.UNKNOWN;
                };
        JobStatusResult r = new JobStatusResult();
        r.setJobId(req.jobName);
        r.setJobName(req.jobName);
        r.setState(state);
        r.setPublisherName("job-callback");
        try {
            jobStatusHandler.onJobStatusMessage(r);
        } catch (Exception e) {
            log.error("Error processing job-status callback jobName={} status={}", req.jobName, req.status, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    public record JobStatusRequest(String jobName, String status, String task) {}
}
