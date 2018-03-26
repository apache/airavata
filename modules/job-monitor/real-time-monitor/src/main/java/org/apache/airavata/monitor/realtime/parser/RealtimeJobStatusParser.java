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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RealtimeJobStatusParser {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeJobStatusParser.class);

    private CuratorFramework curatorClient = null;

    public RealtimeJobStatusParser() throws ApplicationSettingsException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private String getJobIdIdByJobName(String jobName) throws Exception {
        String path = "/monitoring/" + jobName + "/jobId";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = this.curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    public JobStatusResult parse(String rawMessage) {

        try {
            Map asMap = new Gson().fromJson(rawMessage, Map.class);
            if (asMap.containsKey("jobName") && asMap.containsKey("status")) {
                String jobName = (String) asMap.get("jobName");
                String status = (String) asMap.get("status");

                if (jobName != null && status != null) {

                    try {
                        String jobId = getJobIdIdByJobName(jobName);
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
                    logger.error("Job name or status is null in message " + rawMessage);
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
