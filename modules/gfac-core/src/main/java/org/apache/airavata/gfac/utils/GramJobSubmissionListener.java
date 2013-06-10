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

import java.util.Calendar;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.notification.events.StatusChangeEvent;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramJobSubmissionListener implements GramJobListener{
    private final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);
    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
    private static final int JOB_PROXY_REMAINING_TIME_LIMIT = 900;

    private static final long JOB_FINISH_WAIT_TIME = 60 * 1000l;
    private boolean finished;
    private int error;
    private int status;
    private JobExecutionContext context;
    private GramJob job;

    public GramJobSubmissionListener(GramJob job, JobExecutionContext context) {
        this.job = job;
        this.context = context;
    }

    /**
     * This method is used to block the process until the status of the job is DONE or FAILED
     *
     * @throws InterruptedException
     * @throws GSSException
     * @throws GramException
     * @throws SecurityException
     */
    public void waitFor() throws InterruptedException, GSSException, GramException, SecurityException,GFacException {
        while (!isFinished()) {
            int proxyExpTime = job.getCredentials().getRemainingLifetime();
            if (proxyExpTime < JOB_PROXY_REMAINING_TIME_LIMIT) {
                log.info("Job proxy expired. Trying to renew proxy");
                GSSCredential gssCred = ((GSISecurityContext)context.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentials();
                job.renew(gssCred);
                log.info("Myproxy renewed");
            }

            synchronized (this) {

                /*
                 * job status is changed but method isn't invoked
                 */
                if (status != 0) {
                    if (job.getStatus() != status) {
                        log.info("Change job status manually");
                        if (setStatus(job.getStatus(), job.getError())) {
                            break;
                        }
                    } else {
                    	GFacUtils.updateApplicationJobStatusUpdateTime(context, job.getIDAsString(), Calendar.getInstance().getTime());
                        log.info("job " + job.getIDAsString() + " have same status: "
                                + GramJob.getStatusAsString(status));
                    }
                } else {
                    log.info("Status is zero");
                }

                wait(JOB_FINISH_WAIT_TIME);
            }
        }
    }

    private ApplicationJobStatus getApplicationJobStatus(int gramStatus){
    	switch(gramStatus){
    	case GramJob.STATUS_ACTIVE:
    		return ApplicationJobStatus.EXECUTING;
    	case GramJob.STATUS_DONE:
    		return ApplicationJobStatus.FINISHED;
    	case GramJob.STATUS_FAILED:
    		return ApplicationJobStatus.FAILED;
    	case GramJob.STATUS_PENDING:
    		return ApplicationJobStatus.PENDING;
    	case GramJob.STATUS_STAGE_IN:
    		return ApplicationJobStatus.INITIALIZE;
    	case GramJob.STATUS_STAGE_OUT:
    		return ApplicationJobStatus.FINALIZE;
    	case GramJob.STATUS_SUSPENDED:
    		return ApplicationJobStatus.SUSPENDED;
    	default:
    		return ApplicationJobStatus.UNKNOWN;
    	}
    }
    
    private synchronized boolean isFinished() {
        return this.finished;
    }

    private synchronized boolean setStatus(int status, int error) {
		GFacUtils.updateApplicationJobStatus(context,job.getIDAsString(), getApplicationJobStatus(status));
        this.status = status;
        this.error = error;

        switch (this.status) {
        case GramJob.STATUS_FAILED:
            log.info("Job Error Code: " + error);
            this.finished = true;
        case GramJob.STATUS_DONE:
            this.finished = true;
        }

        return this.finished;
    }

    public synchronized void statusChanged(GramJob job) {
        String jobStatusMessage = "Status of job " + job.getIDAsString() + "is " + job.getStatusAsString();
        /*
         * Notify status change
         */
        this.context.getNotifier().publish(new StatusChangeEvent(jobStatusMessage));

        /*
         * Set new status if it is finished, notify all wait object
         */
        if (setStatus(job.getStatus(), job.getError())) {
            notifyAll();
        }
    }

    public synchronized int getError() {
        return error;
    }

    public synchronized int getStatus() {
        return status;
    }
}
