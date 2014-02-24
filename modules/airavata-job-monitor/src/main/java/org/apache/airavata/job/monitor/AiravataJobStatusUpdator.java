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
 *
*/
package org.apache.airavata.job.monitor;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.model.experiment.JobState;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class AiravataJobStatusUpdator{
    private final static Logger logger = LoggerFactory.getLogger(AiravataJobStatusUpdator.class);

    private Registry airavataRegistry;

    private BlockingQueue<MonitorID> jobsToMonitor;


    public AiravataJobStatusUpdator(Registry airavataRegistry, BlockingQueue<MonitorID> jobsToMonitor) {
        this.airavataRegistry = airavataRegistry;
        this.jobsToMonitor = jobsToMonitor;
    }

    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }

    public BlockingQueue<MonitorID> getJobsToMonitor() {
        return jobsToMonitor;
    }

    public void setJobsToMonitor(BlockingQueue<MonitorID> jobsToMonitor) {
        this.jobsToMonitor = jobsToMonitor;
    }

    @Subscribe
    public void updateRegistry(JobStatus jobStatus) {
        /* Here we need to parse the jobStatus message and update
                the registry accordingly, for now we are just printing to standard Out
                 */
        JobState state = jobStatus.getState();
        System.out.println("Job ID: " + jobStatus.getMonitorID().getJobID());
        System.out.println("Username: " + jobStatus.getMonitorID().getUserName());
        System.out.println("Job Status: " + jobStatus.getState().toString());


        switch (state) {
            case COMPLETE:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is DONE");
                jobsToMonitor.remove(jobStatus.getMonitorID());
                break;
            case UNKNOWN:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is UNKNOWN");
                System.out.println("Unknown job status came, if the old job status is RUNNING or something active, we have to make it complete");
                //todo implement this logic
                break;
            case QUEUED:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is QUEUED");

            case SUBMITTED:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is SUBMITTED");
            case ACTIVE:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is ACTIVE");
                break;
            case CANCELED:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is CANCELED");
                break;
            case FAILED:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is FAILED");
                break;
            case HELD:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is HELD");
                break;
            case SUSPENDED:
                logger.info("Job ID:" + jobStatus.getMonitorID().getJobID() + "is SUSPENDED");
                break;
        }
    }
}
