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
package org.apache.airavata.gfac.provider;

import org.apache.airavata.gfac.JobSubmissionFault;
import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.utils.GramJobSubmissionListener;
import org.apache.airavata.gfac.utils.GramProviderUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramProvider implements GFacProvider {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    private GramJob job;
    private GramJobSubmissionListener listener;

    // This method precpare the environment before the application invocation.
    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        GramProviderUtils.makeDirectory(jobExecutionContext);
        job = GramProviderUtils.setupEnvironment(jobExecutionContext);
        listener = new GramJobSubmissionListener(job, jobExecutionContext);
        job.addListener(listener);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        System.out.println("Executing the job");
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();

        StringBuffer buf = new StringBuffer();
        try {

            /*
            * Set Security
            */
            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
            GSSCredential gssCred = gssContext.getGssCredentails();
            job.setCredentials(gssCred);
            // We do not support multiple gatekeepers in XBaya GUI, so we simply pick the 0th element in the array
            String gateKeeper = host.getGlobusGateKeeperEndPointArray(0);
            log.debug("Request to contact:" + gateKeeper);

            buf.append("Finished launching job, Host = ").append(host.getHostAddress()).append(" RSL = ")
                    .append(job.getRSL()).append(" working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" temp directory = ").append(app.getScratchWorkingDirectory())
                    .append(" Globus GateKeeper Endpoint = ").append(gateKeeper);

            /*
            * The first boolean is to specify the job is a batch job - use true for interactive and false for batch.
            * The second boolean is to specify to use the full proxy and not delegate a limited proxy.
            */
            job.request(gateKeeper, false, false);
            String gramJobid = job.getIDAsString();
            log.info("JobID = " + gramJobid);

            log.info(buf.toString());
            /*
            * Block untill job is done
            */
            listener.waitFor();

            /*
            * Remove listener
            */
            job.removeListener(listener);

            /*
            * Fail job
            */
            int jobStatus = listener.getStatus();

            if (job.getExitCode() != 0 || jobStatus == GramJob.STATUS_FAILED) {
                int errCode = listener.getError();
                String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress() + " Job Exit Code = "
                        + listener.getError();
                JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST",
                        gateKeeper, job.getRSL(), jobExecutionContext);
                jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(error.getCause()));
                throw error;
            }
        } catch (GramException e) {
            JobSubmissionFault error = new JobSubmissionFault(this, e, host.getHostAddress(),
                    host.getGlobusGateKeeperEndPointArray(0), job.getRSL(), jobExecutionContext);
            jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(error.getCause()));
        } catch (GSSException e) {
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        } catch (InterruptedException e) {
            throw new GFacProviderException("Thread", e, jobExecutionContext);
        } catch (SecurityException e) {
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        } finally {
            if (job != null) {
                try {
                    job.cancel();
                } catch (Exception e) {
                }
            }
        }

    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
    }
}
