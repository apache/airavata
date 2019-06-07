/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.monitor.realtime.parser;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RealtimeJobStatusParser {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeJobStatusParser.class);

    private String getJobIdIdByJobNameWithRetry(String jobName, String taskId, RegistryService.Client registryClient) throws Exception {
        for (int  i = 0; i < 3; i++) {

            List<JobModel> jobsOfTask = registryClient.getJobs("taskId", taskId);
            if (jobsOfTask == null || jobsOfTask.size() == 0) {
                // Retry after 2s
                logger.warn("No jobs for task " + taskId + ". Retrying in 2 seconds");
                Thread.sleep(2000);
            } else {
                Optional<JobModel> filtered = jobsOfTask.stream().filter(job -> jobName.equals(job.getJobName())).findFirst();
                if (filtered.isPresent()) {
                    return filtered.get().getJobId();
                } else {
                    logger.warn("No job for job name " + jobName + " and task " + taskId + ". Retrying in 2 seconds");
                    Thread.sleep(2000);
                }
            }
        }
        return null;
    }

    public JobStatusResult parse(String rawMessage, RegistryService.Client registryClient) {

        try {
            Map asMap = new Gson().fromJson(rawMessage, Map.class);
            if (asMap.containsKey("jobName") && asMap.containsKey("status")) {
                String jobName = (String) asMap.get("jobName");
                String status = (String) asMap.get("status");
                String taskId = (String) asMap.get("task");

                if (jobName != null && status != null && taskId != null) {

                    try {
                        String jobId = getJobIdIdByJobNameWithRetry(jobName, taskId, registryClient);
                        if (jobId == null) {
                            logger.error("No job id for job name " + jobName);
                            return null;
                        }

                        JobState jobState = null;

                        switch (status) {
                            case "RUNNING":
                                jobState = JobState.ACTIVE;
                                break;
                            case "COMPLETED":
                                jobState = JobState.COMPLETE;
                                break;
                            case "FAILED":
                                jobState = JobState.FAILED;
                                break;
                            case "SUBMITTED":
                                jobState = JobState.SUBMITTED;
                                break;
                            case "QUEUED":
                                jobState = JobState.QUEUED;
                                break;
                            case "CANCELED":
                                jobState = JobState.CANCELED;
                                break;
                            case "SUSPENDED":
                                jobState = JobState.SUSPENDED;
                                break;
                            case "UNKNOWN":
                                jobState = JobState.UNKNOWN;
                                break;
                            case "NON_CRITICAL_FAIL":
                                jobState = JobState.NON_CRITICAL_FAIL;
                                break;
                        }

                        if (jobState == null) {
                            logger.error("Invalid job state " + status);
                            return null;
                        }

                        JobStatusResult jobStatusResult = new JobStatusResult();
                        jobStatusResult.setJobId(jobId);
                        jobStatusResult.setJobName(jobName);
                        jobStatusResult.setState(jobState);
                        jobStatusResult.setPublisherName(ServerSettings.getSetting("job.monitor.broker.publisher.id"));
                        return jobStatusResult;
                    } catch (Exception e) {
                        logger.error("Failed to fetch job id for job name " + jobName);
                        return null;
                    }
                } else {
                    logger.error("Job name, taskId or status is null in message " + rawMessage);
                    return null;
                }
            } else {
                logger.error("Data structure of message " + rawMessage + " is not correct");
                return null;
            }
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse raw data " + rawMessage + " to type Map", e);
            return null;
        }
    }
}
