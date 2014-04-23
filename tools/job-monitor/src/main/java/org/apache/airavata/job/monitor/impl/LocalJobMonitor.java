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
package org.apache.airavata.job.monitor.impl;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.job.monitor.JobIdentity;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.core.AiravataAbstractMonitor;
import org.apache.airavata.job.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.model.workspace.experiment.JobState;

import java.util.concurrent.BlockingQueue;

/**
 * This monitor can be used to monitor a job which runs locally,
 * Since its a local job job doesn't have states, once it get executed
 * then the job starts running
 */
public class LocalJobMonitor extends AiravataAbstractMonitor {
    // Though we have a qeuue here, it not going to be used in local jobs
    BlockingQueue<MonitorID> jobQueue;

    public void run() {
        do {
            try {
                MonitorID take = jobQueue.take();
                getPublisher().publish(new JobStatusChangeRequest(take, new JobIdentity(take.getExperimentID(), take.getWorkflowNodeID(), take.getTaskID(), take.getJobID()), JobState.COMPLETE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!ServerSettings.isStopAllThreads());
    }

    public BlockingQueue<MonitorID> getJobQueue() {
        return jobQueue;
    }

    public void setJobQueue(BlockingQueue<MonitorID> jobQueue) {
        this.jobQueue = jobQueue;
    }
}
