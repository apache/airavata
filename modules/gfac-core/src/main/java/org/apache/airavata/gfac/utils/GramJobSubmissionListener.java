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
package org.apache.airavata.gfac.utils;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.notification.events.StatusChangeEvent;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramJobSubmissionListener implements GramJobListener{
    private final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    public static final int NO_ERROR = -42;
    public static final int INITIAL_STATUS = -43;

    private volatile boolean jobDone = false;
    private volatile int error = NO_ERROR;
    private int currentStatus = INITIAL_STATUS;

    private JobExecutionContext context;
    private GramJob job;

    public GramJobSubmissionListener(GramJob job, JobExecutionContext context) {
        this.job = job;
        this.context = context;
    }

    /**
     * This method is used to block the process until the currentStatus of the job is DONE or FAILED
     */
    public void waitFor()  {
        while (!isJobDone()) {

            synchronized (this) {

                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
    }


    
    private synchronized boolean isJobDone() {
        return this.jobDone;
    }

    private void setStatus(int status, int error) {
		GFacUtils.updateApplicationJobStatus(context,job.getIDAsString(),
                GFacUtils.getApplicationJobStatus(status));
        this.currentStatus = status;
        this.error = error;

        switch (this.currentStatus) {
        case GramJob.STATUS_FAILED:
            log.info("Job Error Code: " + error);
            this.jobDone = true;
            notifyAll();
        case GramJob.STATUS_DONE:
            this.jobDone = true;
            notifyAll();
        }

    }

    public synchronized void statusChanged(GramJob job) {

        int jobStatus = job.getStatus();
        String jobStatusMessage = "Status of job " + job.getIDAsString() + "is " + job.getStatusAsString();
        /*
         * Notify currentStatus change
         */
        this.context.getNotifier().publish(new StatusChangeEvent(jobStatusMessage));

        /*
         * Set new currentStatus if it is jobDone, notify all wait object
         */
        if (currentStatus != jobStatus) {
            currentStatus = jobStatus;

            setStatus(job.getStatus(), job.getError());

            // Test to see whether we need to renew credentials
            renewCredentials(job);
        }
    }

    private void renewCredentials(GramJob job) {

        try {

            int proxyExpTime = job.getCredentials().getRemainingLifetime();
            if (proxyExpTime < GSISecurityContext.CREDENTIAL_RENEWING_THRESH_HOLD) {
                log.info("Job proxy expired. Trying to renew proxy");
                GSSCredential gssCred = ((GSISecurityContext)context.
                        getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).renewCredentials();
                job.renew(gssCred);
                log.info("MyProxy credentials are renewed .");
            }

        } catch (Exception e) {
            log.error("An error occurred while trying to renew credentials. Job id " + job.getIDAsString());
        }


    }

    public synchronized int getError() {
        return error;
    }

    public synchronized int getCurrentStatus() {
        return currentStatus;
    }
}
