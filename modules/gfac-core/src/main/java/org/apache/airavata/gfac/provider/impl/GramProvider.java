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

import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.JobSubmissionFault;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.notification.events.GramJobIDEvent;
import org.apache.airavata.gfac.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GramJobSubmissionListener;
import org.apache.airavata.gfac.utils.GramProviderUtils;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GramProvider implements GFacProvider {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    private GramJob job;
    private GramJobSubmissionListener listener;

    // This method precpare the environment before the application invocation.
    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        job = GramProviderUtils.setupEnvironment(jobExecutionContext);
        listener = new GramJobSubmissionListener(job, jobExecutionContext);
        job.addListener(listener);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException{
        jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();

        StringBuffer buf = new StringBuffer();
        try {
            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentails();
            job.setCredentials(gssCred);
            // We do not support multiple gatekeepers in XBaya GUI, so we simply pick the 0th element in the array
            String gateKeeper = host.getGlobusGateKeeperEndPointArray(0);
            log.info("Request to contact:" + gateKeeper);

            buf.append("Finished launching job, Host = ").append(host.getHostAddress()).append(" RSL = ")
                    .append(job.getRSL()).append(" working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" temp directory = ").append(app.getScratchWorkingDirectory())
                    .append(" Globus GateKeeper Endpoint = ").append(gateKeeper);

            /*
            * The first boolean is to specify the job is a batch job - use true for interactive and false for batch.
            * The second boolean is to specify to use the full proxy and not delegate a limited proxy.
            */
            job.request(gateKeeper, false, false);
            log.info(buf.toString());
            checkJobStatus(jobExecutionContext, host, gateKeeper);
            
            String gramJobid = job.getIDAsString();
            String jobID = "JobID= " + gramJobid;
            log.info(jobID);
            jobExecutionContext.getNotifier().publish(new GramJobIDEvent(jobID));
           
            String experimentID = (String) jobExecutionContext.getProperty(Constants.PROP_TOPIC);
            String nodeID = (String)jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_NODE_ID);
            String hostName = jobExecutionContext.getApplicationContext().getHostDescription().getType().getHostName();
            WorkflowNodeGramData workflowNodeGramData = new WorkflowNodeGramData(experimentID, nodeID, job.getRSL(),hostName , job.getIDAsString());
            try {
            	// for provider test
            	if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null)
                jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateWorkflowNodeGramData(workflowNodeGramData);
            } catch (AiravataAPIInvocationException e) {
                throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
            }
            /*
            * Wait until job is done
            */
            listener.waitFor();

            checkJobStatus(jobExecutionContext, host, gateKeeper);
            
        } catch (GramException e) {
            log.error(e.getMessage());
            JobSubmissionFault error = new JobSubmissionFault(this, e, host.getHostAddress(),
                    host.getGlobusGateKeeperEndPointArray(0), job.getRSL(), jobExecutionContext);
            throw error;
        }catch(JobSubmissionFault e){
        	throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        } 
        catch (GSSException e) {
            log.error(e.getMessage());
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new GFacProviderException("Thread", e, jobExecutionContext);
        } catch (SecurityException e) {
            log.error(e.getMessage());
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
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
	
    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
    }

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

    private void checkJobStatus(JobExecutionContext jobExecutionContext, GlobusHostType host, String gateKeeper)
            throws GFacProviderException {
        int jobStatus = listener.getStatus();

        if (jobStatus == GramJob.STATUS_FAILED) {
            String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress() + " Job Exit Code = "
                    + listener.getError();
            if (listener.getError() == 24) {
                try {
                    job.request(gateKeeper, false, false);
                    listener.waitFor();
                } catch (GramException e) {
                    log.error(e.getMessage());
                    JobSubmissionFault error = new JobSubmissionFault(this, e, host.getHostAddress(),
                            host.getGlobusGateKeeperEndPointArray(0), job.getRSL(), jobExecutionContext);
                    throw error;
                } catch (GSSException e) {
                    log.error(e.getMessage());
                    throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    throw new GFacProviderException("Thread", e, jobExecutionContext);
                } catch (GFacException e) {
                    JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST", gateKeeper,
                            job.getRSL(), jobExecutionContext);
                    throw error;
                }
            }

        }
    }

}
