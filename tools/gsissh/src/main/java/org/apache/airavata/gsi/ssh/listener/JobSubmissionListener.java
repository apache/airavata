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
package org.apache.airavata.gsi.ssh.listener;

import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.JobStatus;

/**
 * This interface can be implemented by the end user of the API
 * to do desired operations based on the job status change. API has a
 * default joblistener which can be used by the end users, but its
 * configurable and can be parse to jobsubmission methods.
 */
public abstract class JobSubmissionListener {

    private JobStatus jobStatus = JobStatus.U;

    /**
     * This can be usd to perform some operation during status change
     *
     * @param jobDescriptor
     * @throws SSHApiException
     */
    public abstract void statusChanged(JobDescriptor jobDescriptor) throws SSHApiException;

    /**
     * This can be usd to perform some operation during status change
     * @param jobStatus
     * @throws SSHApiException
     */
    public abstract void statusChanged(JobStatus jobStatus) throws SSHApiException;


    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    /**
     * This method is used to block the process until the currentStatus of the job is DONE or FAILED
     */
    public void waitFor()  throws SSHApiException{
        while (!isJobDone()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
    }

    /**
     * BAsed on the implementation user can define how to decide the job done
     * scenario
     * @return
     * @throws SSHApiException
     */
    public abstract boolean isJobDone() throws SSHApiException;
}
