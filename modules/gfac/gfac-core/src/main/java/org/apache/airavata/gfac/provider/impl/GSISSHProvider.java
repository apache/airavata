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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSISSHProvider extends AbstractProvider implements GFacProvider{
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
            JobDescriptor jobDescriptor = new JobDescriptor();
            jobDescriptor.setWorkingDirectory(app.getStaticWorkingDirectory());
            jobDescriptor.setShellName("/bin/bash");
            Random random = new Random();
            int i = random.nextInt();
            jobDescriptor.setJobName(app.getApplicationName().getStringValue() + String.valueOf(i));
            jobDescriptor.setExecutablePath(app.getExecutableLocation());
            jobDescriptor.setAllEnvExport(true);
            jobDescriptor.setMailOptions("n");
            jobDescriptor.setStandardOutFile(app.getStandardOutput());
            jobDescriptor.setStandardErrorFile(app.getStandardError());
            jobDescriptor.setNodes(app.getNodeCount());
            jobDescriptor.setProcessesPerNode(app.getProcessorsPerNode());
            jobDescriptor.setMaxWallTime(String.valueOf(app.getMaxWallTime()));
            jobDescriptor.setJobSubmitter(app.getJobSubmitterCommand());
            if (app.getProjectAccount().getProjectAccountNumber() != null) {
                jobDescriptor.setAcountString(app.getProjectAccount().getProjectAccountNumber());
            }
            if (app.getQueue().getQueueName() != null) {
                jobDescriptor.setQueueName(app.getQueue().getQueueName());
            }
            jobDescriptor.setOwner(((PBSCluster) cluster).getServerInfo().getUserName());
            
            TaskDetails taskData = jobExecutionContext.getTaskData();
            if(taskData != null && taskData.isSetTaskScheduling()){
            	ComputationalResourceScheduling computionnalResource = taskData.getTaskScheduling();
                if(computionnalResource.getNodeCount() > 0){
                	jobDescriptor.setNodes(computionnalResource.getNodeCount());
                }
                if(computionnalResource.getComputationalProjectAccount() != null){
                	jobDescriptor.setAcountString(computionnalResource.getComputationalProjectAccount());
                }
                if(computionnalResource.getQueueName() != null){
                	jobDescriptor.setQueueName(computionnalResource.getQueueName());
                }
                if(computionnalResource.getTotalCPUCount() > 0){
                	jobDescriptor.setProcessesPerNode(computionnalResource.getTotalCPUCount());
                }
                if(computionnalResource.getWallTimeLimit() > 0){
                	jobDescriptor.setMaxWallTime(String.valueOf(computionnalResource.getWallTimeLimit()));
                }
            }
            List<String> inputValues = new ArrayList<String>();
            MessageContext input = jobExecutionContext.getInMessageContext();
            Map<String, Object> inputs = input.getParameters();
            Set<String> keys = inputs.keySet();
            for (String paramName : keys) {
                ActualParameter actualParameter = (ActualParameter) inputs.get(paramName);
                if ("URIArray".equals(actualParameter.getType().getType().toString()) || "StringArray".equals(actualParameter.getType().getType().toString())
                        || "FileArray".equals(actualParameter.getType().getType().toString())) {
                    String[] values = null;
                    if (actualParameter.getType() instanceof URIArrayType) {
                        values = ((URIArrayType) actualParameter.getType()).getValueArray();
                    } else if (actualParameter.getType() instanceof StringArrayType) {
                        values = ((StringArrayType) actualParameter.getType()).getValueArray();
                    } else if (actualParameter.getType() instanceof FileArrayType) {
                        values = ((FileArrayType) actualParameter.getType()).getValueArray();
                    }
                    String value = StringUtil.createDelimiteredString(values, " ");
                    inputValues.add(value);
                } else {
                    String paramValue = MappingFactory.toString(actualParameter);
                    inputValues.add(paramValue);
                }
            }
            jobDescriptor.setInputValues(inputValues);

            log.info(jobDescriptor.toXML());
            
            jobDetails.setJobDescription(jobDescriptor.toXML());
            
            String jobID = cluster.submitBatchJob(jobDescriptor);
            jobDetails.setJobID(jobID);
            jobExecutionContext.setJobDetails(jobDetails);
            GFacUtils.saveJobStatus(jobDetails,JobState.QUEUED,taskID);
        } catch (SSHApiException e) {
            String error = "Error submitting the job to host " + host.getHostAddress() + e.getMessage();
            log.error(error);
        	GFacUtils.saveJobStatus(jobDetails,JobState.FAILED,taskID);
         	GFacUtils.saveErrorDetails(error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR, taskID);
            throw new GFacProviderException(error, e);
        } catch (Exception e) {
        	String error = "Error submitting the job to host " + host.getHostAddress() + e.getMessage();
         	log.error(error);
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
