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

package org.apache.airavata.core.gfac.provider.utils;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.GFACContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSubmissionListener implements GramJobListener {

	public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
	
    private boolean finished;
    private int error;
    private int status;
    private InvocationContext context;
    private GramJob job;
    protected final Logger log = LoggerFactory.getLogger(JobSubmissionListener.class);

    public JobSubmissionListener(GramJob job, InvocationContext context) {
        this.job = job;
        this.context = context;
    }

    // waits for DONE or FAILED status
    public void waitFor() throws InterruptedException, GSSException, GfacException, GramException {
        while (!finished) {
            int proxyExpTime = job.getCredentials().getRemainingLifetime();
            if (proxyExpTime < 900) {
                log.info("Job proxy expired. Trying to renew proxy");
                GSSCredential gssCred = ((GSISecurityContext) context
                        .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();
                job.renew(gssCred);
            }
            // job status is changed but method isn't invoked
            if (status != 0) {
                if (job.getStatus() != status) {
                    log.info("invoke method manually");
                    statusChanged(job);
                } else {
                    log.info("job " + job.getIDAsString() + " have same status: " + GramJob.getStatusAsString(status));
                }
            } else {
                log.info("Status is zero");
            }

            synchronized (this) {
                wait(60 * 1000l);
            }
        }
    }

    public synchronized void statusChanged(GramJob job) {
        int jobStatus = job.getStatus();
        String jobId = job.getIDAsString();
        String statusString = job.getStatusAsString();
        String jobStatusMessage = GfacUtils.formatJobStatus(jobId, statusString);
        log.info(jobStatusMessage);
        status = jobStatus;
        context.getExecutionContext().getNotificationService().statusChanged(this, this.context, jobStatusMessage);
        if (jobStatus == GramJob.STATUS_DONE) {
            finished = true;
        } else if (jobStatus == GramJob.STATUS_FAILED) {
            finished = true;
            error = job.getError();
            log.info("Job Error Code: " + error);
        }

        if (finished) {
            notify();
        }
    }

    public int getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public void wakeup() {
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
