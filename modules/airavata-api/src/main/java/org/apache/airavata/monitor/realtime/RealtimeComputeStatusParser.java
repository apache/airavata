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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealtimeComputeStatusParser {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeComputeStatusParser.class);

    private String getJobIdIdByJobNameWithRetry(String jobName, String taskId, RegistryService registryService)
            throws RegistryException, InterruptedException {
        for (int i = 0; i < 3; i++) {

            var jobsOfTask = registryService.getJobs("taskId", taskId);
            if (jobsOfTask == null || jobsOfTask.isEmpty()) {
                // Retry after 2s
                logger.warn("No jobs for task {}. Retrying in 2 seconds", taskId);
                Thread.sleep(2000);
            } else {
                var filtered = jobsOfTask.stream()
                        .filter(job -> jobName.equals(job.getJobName()))
                        .findFirst();
                if (filtered.isPresent()) {
                    return filtered.get().getJobId();
                } else {
                    logger.warn("No job for job name {} and task {}. Retrying in 2 seconds", jobName, taskId);
                    Thread.sleep(2000);
                }
            }
        }
        return null;
    }

    public JobStatusResult parse(String rawMessage, String publisherId, RegistryService registryService) {

        try {
            var objectMapper = new ObjectMapper();
            var asMap = objectMapper.readValue(rawMessage, new TypeReference<Map<String, Object>>() {});
            if (asMap.containsKey("jobName") && asMap.containsKey("status")) {
                var jobName = (String) asMap.get("jobName");
                var status = (String) asMap.get("status");
                var taskId = (String) asMap.get("task");

                if (jobName != null && status != null && taskId != null) {

                    try {
                        var jobId = getJobIdIdByJobNameWithRetry(jobName, taskId, registryService);
                        if (jobId == null) {
                            logger.error("No job id for job name {}", jobName);
                            return null;
                        }

                        var jobState =
                                switch (status) {
                                    case "RUNNING" -> JobState.ACTIVE;
                                    case "COMPLETED" -> JobState.COMPLETE;
                                    case "FAILED" -> JobState.FAILED;
                                    case "SUBMITTED" -> JobState.SUBMITTED;
                                    case "QUEUED" -> JobState.QUEUED;
                                    case "CANCELED" -> JobState.CANCELED;
                                    case "SUSPENDED" -> JobState.SUSPENDED;
                                    case "UNKNOWN" -> JobState.UNKNOWN;
                                    case "NON_CRITICAL_FAIL" -> JobState.NON_CRITICAL_FAIL;
                                    default -> null;
                                };

                        if (jobState == null) {
                            logger.error("Invalid job state {}", status);
                            return null;
                        }

                        var jobStatusResult = new JobStatusResult();
                        jobStatusResult.setJobId(jobId);
                        jobStatusResult.setJobName(jobName);
                        jobStatusResult.setState(jobState);
                        jobStatusResult.setPublisherName(publisherId);
                        return jobStatusResult;
                    } catch (Exception e) {
                        logger.error("Failed to fetch job id for job name {}", jobName);
                        return null;
                    }
                } else {
                    logger.error("Job name, taskId or status is null in message {}", rawMessage);
                    return null;
                }
            } else {
                logger.error("Data structure of message {} is not correct", rawMessage);
                return null;
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse raw data {} to type Map", rawMessage, e);
            return null;
        }
    }
}
