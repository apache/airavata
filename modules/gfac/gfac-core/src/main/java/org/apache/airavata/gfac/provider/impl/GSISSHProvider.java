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

import java.util.Map;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSISSHProvider extends AbstractProvider{
    private static final Logger log = LoggerFactory.getLogger(GSISSHProvider.class);

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
    	super.initialize(jobExecutionContext);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        log.info("Invoking GSISSH Provider Invoke ...");
        jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
        HostDescriptionType host = jobExecutionContext.getApplicationContext().
                getHostDescription().getType();
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription().getType();
        JobDetails jobDetails = new JobDetails();
     	String taskID = jobExecutionContext.getTaskData().getTaskID();
        try {
            Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            } else {
                cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }
            // This installed path is a mandetory field, because this could change based on the computing resource
            JobDescriptor jobDescriptor = GFacUtils.createJobDescriptor(jobExecutionContext, app, cluster);

            log.info(jobDescriptor.toXML());
            
            jobDetails.setJobDescription(jobDescriptor.toXML());
            
            String jobID = cluster.submitBatchJob(jobDescriptor);
            jobExecutionContext.setJobDetails(jobDetails);
            if(jobID == null){
                jobDetails.setJobID("none");
                GFacUtils.saveJobStatus(jobDetails, JobState.FAILED, taskID);
            }else{
                jobDetails.setJobID(jobID);
                GFacUtils.saveJobStatus(jobDetails, JobState.SUBMITTED, taskID);
            }

        } catch (SSHApiException e) {
            String error = "Error submitting the job to host " + host.getHostAddress() + " message: " + e.getMessage();
            log.error(error);
            jobDetails.setJobID("none");
        	GFacUtils.saveJobStatus(jobDetails,JobState.FAILED,taskID);
         	GFacUtils.saveErrorDetails(error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR, taskID);
            throw new GFacProviderException(error, e);
        } catch (Exception e) {
        	String error = "Error submitting the job to host " + host.getHostAddress() + " message: " + e.getMessage();
         	log.error(error);
            jobDetails.setJobID("none");
        	GFacUtils.saveJobStatus(jobDetails,JobState.FAILED,taskID);
         	GFacUtils.saveErrorDetails(error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR, taskID);
            throw new GFacProviderException(error, e);
        }
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelJob(String jobId, JobExecutionContext jobExecutionContext) throws GFacException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
