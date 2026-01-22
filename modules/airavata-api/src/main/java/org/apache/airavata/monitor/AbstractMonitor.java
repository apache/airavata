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
package org.apache.airavata.monitor;

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.JobStatusHandler;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AbstractMonitor {

    private static final Logger log = LoggerFactory.getLogger(AbstractMonitor.class);

    private final JobStatusHandler jobStatusHandler;
    private final RegistryService registryService;
    private final AiravataServerProperties properties;

    public AbstractMonitor(
            RegistryService registryService,
            AiravataServerProperties properties,
            @Autowired(required = false) JobStatusHandler jobStatusHandler) {
        this.registryService = registryService;
        this.properties = properties;
        this.jobStatusHandler = jobStatusHandler;
    }

    private boolean validateJobStatus(JobStatusResult jobStatusResult) {
        boolean validated = true;
        try {
            log.info("Fetching matching jobs for job id {} from registry", jobStatusResult.getJobId());
            var jobs = registryService.getJobs("jobId", jobStatusResult.getJobId());

            if (!jobs.isEmpty()) {
                log.info("Filtering total {} with target job name {}", jobs.size(), jobStatusResult.getJobName());
                jobs = jobs.stream()
                        .filter(jm -> jm.getJobName().equals(jobStatusResult.getJobName()))
                        .toList();
            }

            if (jobs.size() != 1) {
                log.error(
                        "Couldn't find exactly one job with id {} and name {} in the registry. Count {}",
                        jobStatusResult.getJobId(),
                        jobStatusResult.getJobName(),
                        jobs.size());
                validated = false;

            } else {
                var jobModel = jobs.get(0);

                var processId = jobModel.getProcessId();
                var experimentId = registryService.getProcess(processId).getExperimentId();

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
                    "JobStatusHandler (e.g. PostWorkflowManager) is not available. Enable airavata.services.postwm for direct job-status handling.");
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

    protected RegistryService getRegistryService() {
        return registryService;
    }
}
