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
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.execution.monitoring.MessagingContracts.JobStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts canonical JobStatusUpdateEvent to JobStatusResult (resolve jobId by jobName+taskId, map status string).
 * Single place used by the status-change-topic controller for all sources.
 */
@Component
public class JobStatusEventToResultConverter {

    private static final Logger log = LoggerFactory.getLogger(JobStatusEventToResultConverter.class);

    public JobStatusResult convert(JobStatusUpdateEvent event, JobService jobService) {
        String jobName = event.getJobName();
        String status = event.getStatus();
        String taskId = event.getTaskId();
        String publisherName = event.getPublisherName();
        if (jobName == null || status == null || taskId == null) {
            log.error("Job name, status or taskId is null in event {}", event);
            return null;
        }
        try {
            String jobId = getJobIdByJobName(jobName, taskId, jobService);
            if (jobId == null) {
                log.error("No job id for job name {} task {}", jobName, taskId);
                return null;
            }
            JobState jobState = mapStatus(status);
            if (jobState == null) {
                log.error("Invalid job state {}", status);
                return null;
            }
            JobStatusResult r = new JobStatusResult();
            r.setJobId(jobId);
            r.setJobName(jobName);
            r.setState(jobState);
            r.setPublisherName(publisherName);
            return r;
        } catch (Exception e) {
            log.error("Failed to convert job status event for job name {}", jobName, e);
            return null;
        }
    }

    private static String getJobIdByJobName(String jobName, String taskId, JobService jobService)
            throws RegistryException {
        var jobsOfTask = jobService.getJobs("taskId", taskId);
        if (jobsOfTask == null || jobsOfTask.isEmpty()) {
            log.warn("No jobs found for task {}. Job record should have been saved before submission.", taskId);
            return null;
        }
        return jobsOfTask.stream()
                .filter(job -> jobName.equals(job.getJobName()))
                .findFirst()
                .map(job -> job.getJobId())
                .orElse(null);
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
