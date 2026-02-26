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

import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.execution.activity.JobStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JobStatusMonitor {

    private static final Logger log = LoggerFactory.getLogger(JobStatusMonitor.class);

    private final JobStatusHandler jobStatusHandler;
    private final JobService jobService;
    private final ProcessService processService;
    private final JobStatusEventToResultConverter converter;

    public JobStatusMonitor(
            JobService jobService,
            ProcessService processService,
            @Autowired(required = false) JobStatusHandler jobStatusHandler,
            @Autowired(required = false) JobStatusEventToResultConverter converter) {
        this.jobService = jobService;
        this.processService = processService;
        this.jobStatusHandler = jobStatusHandler;
        this.converter = converter;
    }

    private boolean validateJobStatus(JobStatusResult jobStatusResult) {
        boolean validated = true;
        try {
            log.info("Fetching matching jobs for job id {} from database", jobStatusResult.getJobId());
            var jobs = jobService.getJobs("jobId", jobStatusResult.getJobId());

            if (!jobs.isEmpty()) {
                log.info("Filtering total {} with target job name {}", jobs.size(), jobStatusResult.getJobName());
                jobs = jobs.stream()
                        .filter(jm -> jm.getJobName().equals(jobStatusResult.getJobName()))
                        .toList();
            }

            if (jobs.size() != 1) {
                log.error(
                        "Couldn't find exactly one job with id {} and name {} in the database. Count {}",
                        jobStatusResult.getJobId(),
                        jobStatusResult.getJobName(),
                        jobs.size());
                validated = false;

            } else {
                var jobModel = jobs.get(0);

                var processId = jobModel.getProcessId();
                var experimentId = processService.getProcess(processId).getExperimentId();

                if (experimentId != null && processId != null) {
                    log.info(
                            "Job id {} is owned by process {} of experiment {}",
                            jobStatusResult.getJobId(),
                            processId,
                            experimentId);
                    validated = true;
                } else {
                    log.error("Experiment or process is null for job {}", jobStatusResult.getJobId());
                    validated = false;
                }
            }
            return validated;

        } catch (RegistryException e) {
            log.error("Error at validating job status {}", jobStatusResult.getJobId(), e);
            return false;
        }
    }

    public void submitJobStatus(JobStatusResult jobStatusResult) throws MonitoringException {
        if (jobStatusHandler == null) {
            throw new MonitoringException(
                    "JobStatusHandler (e.g. ProcessActivityManager) is not available. Enable airavata.services.controller for direct job-status handling.");
        }
        try {
            if (validateJobStatus(jobStatusResult)) {
                jobStatusHandler.onJobStatusMessage(jobStatusResult);
            } else {
                throw new MonitoringException("Failed to validate job status for job id " + jobStatusResult.getJobId());
            }
        } catch (Exception e) {
            throw new MonitoringException("Failed to submit job status for job id " + jobStatusResult.getJobId(), e);
        }
    }

    /**
     * Publish a pre-built JobStatusResult directly (used by email monitor which already parses results).
     * Delivers directly to jobStatusHandler without validation.
     */
    public void publish(JobStatusResult result) {
        if (jobStatusHandler == null) {
            log.warn("JobStatusHandler not available; job status result dropped");
            return;
        }
        if (result != null) {
            log.debug("Delivering job status result: jobId={} state={}", result.getJobId(), result.getState());
            jobStatusHandler.onJobStatusMessage(result);
        }
    }

    /**
     * Publish a canonical job status event by converting and delivering directly to JobStatusHandler.
     */
    public void publish(MessagingContracts.JobStatusUpdateEvent event) {
        log.debug("Delivering job status event: jobName={} status={}", event.getJobName(), event.getStatus());
        if (jobStatusHandler == null || converter == null) {
            log.warn("JobStatusHandler or converter not available; job status event dropped");
            return;
        }
        if (jobService == null) {
            log.warn("JobService not available; cannot deliver job status event");
            return;
        }
        JobStatusResult result = converter.convert(event, jobService);
        if (result != null) {
            jobStatusHandler.onJobStatusMessage(result);
        }
    }
}
