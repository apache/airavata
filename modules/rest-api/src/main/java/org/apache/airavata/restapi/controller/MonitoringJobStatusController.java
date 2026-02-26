package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.execution.monitoring.JobStatusMonitor;
import org.apache.airavata.execution.monitoring.MessagingContracts;
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
 * Configure airavata.services.monitor.compute.resource-status-callback-url to the base URL of the API
 * plus /api/v1/monitoring/job-status (e.g. http://airavata-api:8080/api/v1/monitoring/job-status).
 *
 * <p>Accepts JSON: {"jobName":"...", "status":"RUNNING"|"COMPLETED"|"FAILED"|..., "task":"taskId"}.
 * Publishes canonical JobStatusUpdateEvent to status-change-topic (same path as email and realtime).
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring")
public class MonitoringJobStatusController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringJobStatusController.class);

    private final JobStatusMonitor jobStatusMonitor;

    @Autowired
    public MonitoringJobStatusController(@Autowired(required = false) JobStatusMonitor jobStatusMonitor) {
        this.jobStatusMonitor = jobStatusMonitor;
    }

    @PostMapping("/job-status")
    public ResponseEntity<Void> jobStatus(@RequestBody JobStatusRequest req) {
        if (jobStatusMonitor == null) {
            log.warn("job-status callback received but JobStatusMonitor not available");
            return ResponseEntity.ok().build();
        }
        if (req == null || req.jobName == null || req.status == null || req.task == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var event = new MessagingContracts.JobStatusUpdateEvent(
                    req.jobName(), req.status(), req.task(), "job-callback", null);
            jobStatusMonitor.publish(event);
        } catch (Exception e) {
            log.error("Error publishing job-status callback jobName={} status={}", req.jobName(), req.status(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    public record JobStatusRequest(String jobName, String status, String task) {}
}
