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
package org.apache.airavata.execution.monitoring;

import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.execution.activity.JobStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JobStatusMonitor {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusMonitor.class);

    private final JobStatusHandler jobStatusHandler;
    private final JobService jobService;

    public JobStatusMonitor(JobService jobService, @Nullable JobStatusHandler jobStatusHandler) {
        this.jobService = jobService;
        this.jobStatusHandler = jobStatusHandler;
    }

    /**
     * Publish a pre-built JobStatusResult directly (used by email monitor which already parses results).
     * Delivers directly to jobStatusHandler without validation.
     */
    public void publish(JobStatusResult result) {
        if (jobStatusHandler == null) {
            logger.warn("JobStatusHandler not available; job status result dropped");
            return;
        }
        if (result != null) {
            logger.debug("Delivering job status result: jobId={} state={}", result.jobId(), result.state());
            jobStatusHandler.onJobStatusMessage(result);
        }
    }

    /**
     * Publish a canonical job status event by converting and delivering directly to JobStatusHandler.
     */
    public void publish(JobStatusUpdateEvent event) {
        logger.debug("Delivering job status event: jobName={} status={}", event.jobName(), event.status());
        if (jobStatusHandler == null) {
            logger.warn("JobStatusHandler not available; job status event dropped");
            return;
        }
        JobStatusResult result = convertEvent(event);
        if (result != null) {
            jobStatusHandler.onJobStatusMessage(result);
        }
    }

    // -------------------------------------------------------------------------
    // Event conversion (inlined from former JobStatusEventToResultConverter)
    // -------------------------------------------------------------------------

    private JobStatusResult convertEvent(JobStatusUpdateEvent event) {
        String jobName = event.jobName();
        String status = event.status();
        String taskId = event.taskId();
        if (jobName == null || status == null || taskId == null) {
            logger.error("Job name, status or taskId is null in event {}", event);
            return null;
        }
        try {
            var jobsOfTask = jobService.getJobs("taskId", taskId);
            if (jobsOfTask == null || jobsOfTask.isEmpty()) {
                logger.warn("No jobs found for task {}. Job record should have been saved before submission.", taskId);
                return null;
            }
            String jobId = jobsOfTask.stream()
                    .filter(job -> jobName.equals(job.getJobName()))
                    .findFirst()
                    .map(job -> job.getJobId())
                    .orElse(null);
            if (jobId == null) {
                logger.error("No job id for job name {} task {}", jobName, taskId);
                return null;
            }
            JobState jobState = mapStatus(status);
            if (jobState == null) {
                logger.error("Invalid job state {}", status);
                return null;
            }
            return new JobStatusResult(jobState, jobId, jobName, true, event.publisherName());
        } catch (Exception e) {
            logger.error("Failed to convert job status event for job name {}", jobName, e);
            return null;
        }
    }

    private static JobState mapStatus(String status) {
        return switch (status.toUpperCase()) {
            case "RUNNING" -> JobState.ACTIVE;
            case "COMPLETED" -> JobState.COMPLETED;
            case "FAILED" -> JobState.FAILED;
            case "SUBMITTED" -> JobState.SUBMITTED;
            case "QUEUED" -> JobState.QUEUED;
            case "CANCELED" -> JobState.CANCELED;
            case "SUSPENDED" -> JobState.SUSPENDED;
            case "UNKNOWN" -> JobState.UNKNOWN;
            case "NON_CRITICAL_FAIL" -> JobState.NON_CRITICAL_FAIL;
            default -> null;
        };
    }
}
