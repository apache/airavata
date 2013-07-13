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
package org.apache.airavata.gfac.provider.impl;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.JobSubmissionFault;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.notification.events.GramJobIDEvent;
import org.apache.airavata.gfac.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.GramJobSubmissionListener;
import org.apache.airavata.gfac.utils.GramProviderUtils;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.WaitingForCommitException;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramProvider implements GFacProvider {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    private GramJob job;
    private GramJobSubmissionListener listener;
    private boolean twoPhase = true;

    /**
     * If normal job submission fail due to an authorisation failure or script failure we
     * will re-attempt to submit the job. In-order to avoid any recursive loop during a continuous
     * failure we track whether failure paths are tried or not. Following booleans keeps track whether
     * we already tried a failure path or not.
     */
    /**
     * To track job submissions during a authorisation failure while requesting job.
     */
    private boolean renewCredentialsAttempt = false;
    /**
     * To track job submission during a script error situation.
     */
    private boolean reSubmissionInProgress = false;
    /**
     * To track authorisation failures during status monitoring.
     */
    private boolean authorisationFailedAttempt = false;

    private static final Map<String, GramJob> currentlyExecutingJobCache
            = new ConcurrentHashMap<String, GramJob>();

    private static ResourceBundle resources;

    static {
        try {
            resources = ResourceBundle.getBundle("org.globus.gram.internal.errors",
                    Locale.getDefault());
        } catch (MissingResourceException mre) {
            log.error("org.globus.gram.internal.gram.errors.properties not found", mre);
        }
    }


    // This method prepare the environment before the application invocation.
    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        job = GramProviderUtils.setupEnvironment(jobExecutionContext, twoPhase);
        listener = new GramJobSubmissionListener(job, jobExecutionContext);
        job.addListener(listener);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException{
        jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().
                getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription().getType();

        StringBuilder stringBuilder = new StringBuilder();
        try {

            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.
                    getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentials();
            job.setCredentials(gssCred);
            // We do not support multiple gatekeepers in XBaya GUI, so we simply pick the 0th element in the array
            String gateKeeper = host.getGlobusGateKeeperEndPointArray(0);
            log.info("Request to contact:" + gateKeeper);

            stringBuilder.append("Finished launching job, Host = ").append(host.getHostAddress()).append(" RSL = ")
                    .append(job.getRSL()).append(" working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" temp directory = ").append(app.getScratchWorkingDirectory())
                    .append(" Globus GateKeeper Endpoint = ").append(gateKeeper);

            log.info(stringBuilder.toString());

            submitJobs(gateKeeper, jobExecutionContext, host);

        } finally {
            if (job != null) {
                try {
                	 /*
                     * Remove listener
                     */
                    job.removeListener(listener);
                } catch (Exception e) {
                	 log.error(e.getMessage());
                }
            }
        }

    }

    private void submitJobs(String gateKeeper,
                            JobExecutionContext jobExecutionContext,
                            GlobusHostType globusHostType) throws GFacException, GFacProviderException {

        if (twoPhase) {
            try {
                /*
                * The first boolean is to force communication through SSLv3
                * The second boolean is to specify the job is a batch job - use true for interactive and false for
                 * batch.
                * The third boolean is to specify to use the full proxy and not delegate a limited proxy.
                */
                job.request(true, gateKeeper, false, false);

                // Single boolean to track all authentication failures, therefore we need to re-initialize
                // this here
                renewCredentialsAttempt = false;

            } catch (WaitingForCommitException e) {

                saveApplicationJob(jobExecutionContext, ApplicationJobStatus.UN_SUBMITTED);

                String jobStatusMessage = "Un-submitted JobID= " + job.getIDAsString();
                log.info(jobStatusMessage);
                jobExecutionContext.getNotifier().publish(new GramJobIDEvent(jobStatusMessage));

                log.info("JobID = " + job.getIDAsString());


                log.info("Two phase commit: sending COMMIT_REQUEST signal; Job id - " + job.getIDAsString());

                try {
                    job.signal(GramJob.SIGNAL_COMMIT_REQUEST);

                } catch (GramException gramException) {
                    throw new GFacException("Error while sending commit request. Job Id - "
                            + job.getIDAsString(), gramException);
                } catch (GSSException gssException) {

                    // User credentials are invalid
                    log.error("Error while submitting commit request - Credentials provided are invalid. Job Id - "
                            + job.getIDAsString(), e);
                    log.info("Attempting to renew credentials and re-submit commit signal...");

                    renewCredentials(jobExecutionContext);

                    try {
                        job.signal(GramJob.SIGNAL_COMMIT_REQUEST);
                    } catch (GramException e1) {
                        throw new GFacException("Error while sending commit request. Job Id - "
                                + job.getIDAsString(), e1);
                    } catch (GSSException e1) {
                        throw new GFacException("Error while sending commit request. Job Id - "
                                + job.getIDAsString() + ". Credentials provided invalid", e1);
                    }
                }

                jobStatusMessage = "Submitted JobID= " + job.getIDAsString();
                log.info(jobStatusMessage);
                jobExecutionContext.getNotifier().publish(new GramJobIDEvent(jobStatusMessage));

            } catch (GSSException e) {
                // Renew credentials and re-submit
                reSubmitJob(gateKeeper, jobExecutionContext, globusHostType, e);

            } catch (GramException e) {
                throw new GFacException("An error occurred while submitting a job, job id = " + job.getIDAsString(), e);
            }
        } else {

            /*
            * The first boolean is to force communication through SSLv3
            * The second boolean is to specify the job is a batch job - use true for interactive and false for
             * batch.
            * The third boolean is to specify to use the full proxy and not delegate a limited proxy.
            */
            try {

                job.request(true, gateKeeper, false, false);
                renewCredentialsAttempt = false;

            } catch (GramException e) {
                throw new GFacException("An error occurred while submitting a job, job id = " + job.getIDAsString(), e);
            } catch (GSSException e) {

                // Renew credentials and re-submit
                reSubmitJob(gateKeeper, jobExecutionContext, globusHostType, e);
            }

            String jobStatusMessage = "Un-submitted JobID= " + job.getIDAsString();
            log.info(jobStatusMessage);
            jobExecutionContext.getNotifier().publish(new GramJobIDEvent(jobStatusMessage));

        }

        currentlyExecutingJobCache.put(job.getIDAsString(), job);

        // for provider test
        saveApplicationJob(jobExecutionContext, ApplicationJobStatus.SUBMITTED);

        /*
        * Wait until job is done
        */
        listener.waitFor();

        checkJobStatus(jobExecutionContext, globusHostType, gateKeeper);

    }

    private void renewCredentials(JobExecutionContext jobExecutionContext) throws GFacException {

        renewCredentials(this.job, jobExecutionContext);
    }

    private void renewCredentials(GramJob gramJob, JobExecutionContext jobExecutionContext) throws GFacException {

        GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.
                getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).renewCredentials();

        try {
            gramJob.renew(gssCred);
        } catch (GramException e1) {
            throw new GFacException("Unable to renew credentials. Job Id - "
                    + gramJob.getIDAsString(), e1);
        } catch (GSSException e1) {
            throw new GFacException("Unable to renew credentials. Job Id - "
                    + gramJob.getIDAsString(), e1);
        }
    }

    private void reSubmitJob(String gateKeeper,
                             JobExecutionContext jobExecutionContext,
                             GlobusHostType globusHostType, Exception e) throws GFacException, GFacProviderException {

        if (!renewCredentialsAttempt) {

            renewCredentialsAttempt = true;

            // User credentials are invalid
            log.error("Error while submitting job - Credentials provided are invalid. Job Id - "
                    + job.getIDAsString(), e);
            log.info("Attempting to renew credentials and re-submit jobs...");

            // Remove existing listener and register a new listener
            job.removeListener(listener);
            listener = new GramJobSubmissionListener(job, jobExecutionContext);

            job.addListener(listener);

            renewCredentials(jobExecutionContext);

            submitJobs(gateKeeper, jobExecutionContext, globusHostType);

        } else {
            throw new GFacException("Error while submitting job - Credentials provided are invalid. Job Id - "
                    + job.getIDAsString(), e);
        }

    }

    private void reSubmitJob(String gateKeeper,
                             JobExecutionContext jobExecutionContext,
                             GlobusHostType globusHostType) throws GFacException, GFacProviderException {

        // User credentials are invalid
        log.info("Attempting to renew credentials and re-submit jobs...");

        // Remove existing listener and register a new listener
        job.removeListener(listener);
        listener = new GramJobSubmissionListener(job, jobExecutionContext);

        job.addListener(listener);

        renewCredentials(jobExecutionContext);

        submitJobs(gateKeeper, jobExecutionContext, globusHostType);

    }

	private void saveApplicationJob(JobExecutionContext jobExecutionContext, ApplicationJobStatus jobStatus) {
		ApplicationJob appJob = GFacUtils.createApplicationJob(jobExecutionContext);
		appJob.setJobId(job.getIDAsString());
		appJob.setJobData(job.getRSL());
		appJob.setSubmittedTime(Calendar.getInstance().getTime());
		appJob.setStatus(jobStatus);
		appJob.setStatusUpdateTime(appJob.getSubmittedTime());
		GFacUtils.recordApplicationJob(jobExecutionContext, appJob);
	}
	
    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
    }

    @Override
    public void cancelJob(String experimentId, JobExecutionContext jobExecutionContext) throws GFacException {

        List<ApplicationJob> jobs = GFacUtils.getJobIds(jobExecutionContext, experimentId);

        for (ApplicationJob job : jobs) {
            cancelSingleJob(job.getJobId(), jobExecutionContext);
        }
    }

    @Override
    public void cancelJob(String experimentId, String workflowId,
                          JobExecutionContext jobExecutionContext) throws GFacException {
        List<ApplicationJob> jobs = GFacUtils.getJobIds(jobExecutionContext, experimentId, workflowId);

        for (ApplicationJob job : jobs) {
            cancelSingleJob(job.getJobId(), jobExecutionContext);
        }
    }

    @Override
    public void cancelJob(String experimentId, String workflowId, String nodeId,
                          JobExecutionContext jobExecutionContext) throws GFacException {
        List<ApplicationJob> jobs = GFacUtils.getJobIds(jobExecutionContext, experimentId, workflowId, nodeId);

        for (ApplicationJob job : jobs) {
            cancelSingleJob(job.getJobId(), jobExecutionContext);
        }
    }


    private void cancelSingleJob(String jobId, JobExecutionContext context) throws GFacException {
        // First check whether job id is in the cache
        if (currentlyExecutingJobCache.containsKey(jobId)) {

            synchronized (this) {
                GramJob gramJob = currentlyExecutingJobCache.get(jobId);

                // Even though we check using containsKey, at this point job could be null
                if (gramJob != null && (gramJob.getStatus() != GRAMConstants.STATUS_DONE ||
                        gramJob.getStatus() != GRAMConstants.STATUS_FAILED)) {
                    cancelJob(gramJob, context);
                }
            }

        } else {

            GSSCredential gssCred = ((GSISecurityContext)context.
                    getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentials();

            GramJob gramJob = new GramJob(null);
            try {
                gramJob.setID(jobId);
            } catch (MalformedURLException e) {
                throw new GFacException("Invalid job id - " + jobId, e);
            }
            gramJob.setCredentials(gssCred);

            synchronized (this) {
                if (gramJob.getStatus() != GRAMConstants.STATUS_DONE ||
                        gramJob.getStatus() != GRAMConstants.STATUS_FAILED) {
                    cancelJob(gramJob, context);
                }
            }
        }
    }

    private void cancelJob(GramJob gramJob, JobExecutionContext context) throws GFacException{

        try {
            gramJob.cancel();
        } catch (GramException e) {
            throw new GFacException("Error cancelling job, id - " + gramJob.getIDAsString(), e);
        } catch (GSSException e) {

            log.warn("Credentials invalid to cancel job. Attempting to renew credentials and re-try. " +
                    "Job id - " + gramJob.getIDAsString());
            renewCredentials(gramJob, context);

            try {
                gramJob.cancel();
                gramJob.signal(GramJob.SIGNAL_COMMIT_END);
            } catch (GramException e1) {
                throw new GFacException("Error cancelling job, id - " + gramJob.getIDAsString(), e1);
            } catch (GSSException e1) {
                throw new GFacException("Error cancelling job, invalid credentials. Job id - "
                        + gramJob.getIDAsString(), e);
            }
        }

    }

    public void initProperties(Map<String, String> properties) throws GFacException {

    }

    private void checkJobStatus(JobExecutionContext jobExecutionContext, GlobusHostType host, String gateKeeper)
            throws GFacProviderException {
        int jobStatus = listener.getCurrentStatus();

        if (jobStatus == GramJob.STATUS_FAILED) {

            String errorMsg = "Job " + job.getIDAsString() + " on host " + host.getHostAddress() + " Job Exit Code = "
                    + listener.getError() + " Error Description = " + getGramErrorString(listener.getError());

            if (listener.getError() == GRAMProtocolErrorConstants.INVALID_SCRIPT_REPLY) {

                // re-submitting without renewing
                // TODO verify why we re-submit jobs when we get a invalid script reply
                if (!reSubmissionInProgress) {
                    reSubmissionInProgress = true;

                    log.info("Invalid script reply received. Re-submitting job, id - " + job.getIDAsString());
                    try {
                        reSubmitJob(gateKeeper, jobExecutionContext, host);
                    } catch (GFacException e) {
                        throw new GFacProviderException
                                ("Error during re-submission. Original job submission data - " + errorMsg,  e);
                    }
                    return;
                }

            } else if (listener.getError() == GRAMProtocolErrorConstants.ERROR_AUTHORIZATION) {

                // re-submit with renewed credentials
                if (!authorisationFailedAttempt) {
                    authorisationFailedAttempt = true;
                    log.info("Authorisation error contacting provider. Re-submitting job with renewed credentials.");

                    try {
                        renewCredentials(jobExecutionContext);
                        reSubmitJob(gateKeeper, jobExecutionContext, host);
                    } catch (GFacException e) {
                        throw new GFacProviderException
                                ("Error during re-submission. Original job submission data - " + errorMsg,  e);
                    }

                    return;
                }

            } else if (listener.getError() == GRAMProtocolErrorConstants.USER_CANCELLED) {

                log.info("User successfully cancelled job id " + job.getIDAsString());
                return;
            }



            log.error(errorMsg);

            synchronized (this) {
                currentlyExecutingJobCache.remove(job.getIDAsString());
            }

            throw new JobSubmissionFault(new Exception(errorMsg), host.getHostAddress(), gateKeeper,
                            job.getRSL(), jobExecutionContext, getGramErrorString(listener.getError()),
                    listener.getError());

        } else if (jobStatus == GramJob.STATUS_DONE) {
            log.info("Job " + job.getIDAsString() + " on host " + host.getHostAddress() + " is successfully executed.");

            synchronized (this) {
                currentlyExecutingJobCache.remove(job.getIDAsString());
            }
        }
    }

    private String getGramErrorString(int errorCode) {

        if (resources != null) {
            try {
                return resources.getString(String.valueOf(errorCode));
            } catch (MissingResourceException mre) {
                log.warn("Error reading globus error descriptions.", mre);
                return "Error code: " + errorCode;
            }
        } else {
            return "Error code: " + errorCode;
        }

    }

}
