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
package org.apache.airavata.gfac.gsissh.provider.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.handler.ThreadedHandler;
import org.apache.airavata.gfac.core.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.core.provider.AbstractRecoverableProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.util.GFACGSISSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GSISSHProvider extends AbstractRecoverableProvider {
    private static final Logger log = LoggerFactory.getLogger(GSISSHProvider.class);

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        super.initialize(jobExecutionContext);
        try {
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) == null) {
                GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
            }
        } catch (ApplicationSettingsException e) {
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        } catch (GFacException e) {
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        log.info("Invoking GSISSH Provider Invoke ...");
        StringBuffer data = new StringBuffer();
        jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
        HostDescriptionType host = jobExecutionContext.getApplicationContext().
                getHostDescription().getType();
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription().getType();
        JobDetails jobDetails = new JobDetails();
        try {
            Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }
            // This installed path is a mandetory field, because this could change based on the computing resource
            JobDescriptor jobDescriptor = GFACGSISSHUtils.createJobDescriptor(jobExecutionContext, app, cluster);
            jobDetails.setJobName(jobDescriptor.getJobName());

            log.info(jobDescriptor.toXML());
            data.append("jobDesc=").append(jobDescriptor.toXML());
            jobDetails.setJobDescription(jobDescriptor.toXML());

            String jobID = cluster.submitBatchJob(jobDescriptor);
            jobExecutionContext.setJobDetails(jobDetails);
            if (jobID == null) {
                jobDetails.setJobID("none");
                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
            } else {
                jobDetails.setJobID(jobID);
                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.SUBMITTED);
            }
            data.append(",jobId=").append(jobDetails.getJobID());

            // Now job has submitted to the resource, its up to the Provider to parse the information to daemon handler
            // to perform monitoring, daemon handlers can be accessed from anywhere
            delegateToMonitorHandlers(jobExecutionContext, (GsisshHostType) host, jobID);
            // we know this host is type GsiSSHHostType
        } catch (Exception e) {
            String error = "Error submitting the job to host " + host.getHostAddress() + " message: " + e.getMessage();
            log.error(error);
            jobDetails.setJobID("none");
            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
            GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            throw new GFacProviderException(error, e);
        } finally {
            log.info("Saving data for future recovery: ");
            log.info(data.toString());
            GFacUtils.savePluginData(jobExecutionContext, data, this.getClass().getName());
        }
    }

    public void delegateToMonitorHandlers(JobExecutionContext jobExecutionContext, GsisshHostType host, String jobID) throws GFacHandlerException {
        List<ThreadedHandler> daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        if (daemonHandlers == null) {
            daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        }
        ThreadedHandler pullMonitorHandler = null;
        ThreadedHandler pushMonitorHandler = null;
        String monitorMode = host.getMonitorMode();
        for (ThreadedHandler threadedHandler : daemonHandlers) {
            if ("org.apache.airavata.gfac.monitor.handlers.GridPullMonitorHandler".equals(threadedHandler.getClass().getName())) {
                pullMonitorHandler = threadedHandler;
                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PULL.equals(monitorMode)) {
                    log.info("Job is launched successfully now parsing it to monitoring in pull mode, JobID Returned:  " + jobID);
                    pullMonitorHandler.invoke(jobExecutionContext);
                } else {
                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PULL" +
                            " to handle by the GridPullMonitorHandler");
                }
            } else if ("org.apache.airavata.gfac.monitor.handlers.GridPushMonitorHandler".equals(threadedHandler.getClass().getName())) {
                pushMonitorHandler = threadedHandler;
                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PUSH.equals(monitorMode)) {
                    log.info("Job is launched successfully now parsing it to monitoring in push mode, JobID Returned:  " + jobID);
                    pushMonitorHandler.invoke(jobExecutionContext);
                } else {
                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PUSH" +
                            " to handle by the GridPushMonitorHandler");
                }
            }
            // have to handle the GridPushMonitorHandler logic
        }
        if (pullMonitorHandler == null && pushMonitorHandler == null && ExecutionMode.ASYNCHRONOUS.equals(jobExecutionContext.getGFacConfiguration().getExecutionMode())) {
            log.error("No Daemon handler is configured in gfac-config.xml, either pull or push, so monitoring will not invoked" +
                    ", execution is configured as asynchronous, so Outhandler will not be invoked");
        }
    }

    public void removeFromMonitorHandlers(JobExecutionContext jobExecutionContext, GsisshHostType host, String jobID) throws GFacHandlerException {
        List<ThreadedHandler> daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        if (daemonHandlers == null) {
            daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        }
        ThreadedHandler pullMonitorHandler = null;
        ThreadedHandler pushMonitorHandler = null;
        String monitorMode = host.getMonitorMode();
        for (ThreadedHandler threadedHandler : daemonHandlers) {
            if ("org.apache.airavata.gfac.monitor.handlers.GridPullMonitorHandler".equals(threadedHandler.getClass().getName())) {
                pullMonitorHandler = threadedHandler;
                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PULL.equals(monitorMode)) {
                    jobExecutionContext.setProperty("cancel","true");
                    pullMonitorHandler.invoke(jobExecutionContext);
                } else {
                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PULL" +
                            " to handle by the GridPullMonitorHandler");
                }
            } else if ("org.apache.airavata.gfac.monitor.handlers.GridPushMonitorHandler".equals(threadedHandler.getClass().getName())) {
                pushMonitorHandler = threadedHandler;
                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PUSH.equals(monitorMode)) {
                    pushMonitorHandler.invoke(jobExecutionContext);
                } else {
                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PUSH" +
                            " to handle by the GridPushMonitorHandler");
                }
            }
            // have to handle the GridPushMonitorHandler logic
        }
        if (pullMonitorHandler == null && pushMonitorHandler == null && ExecutionMode.ASYNCHRONOUS.equals(jobExecutionContext.getGFacConfiguration().getExecutionMode())) {
            log.error("No Daemon handler is configured in gfac-config.xml, either pull or push, so monitoring will not invoked" +
                    ", execution is configured as asynchronous, so Outhandler will not be invoked");
        }
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelJob(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException {
        //To change body of implemented methods use File | Settings | File Templates.
        log.info("canceling the job status in GSISSHProvider!!!!!");
        HostDescriptionType host = jobExecutionContext.getApplicationContext().
                getHostDescription().getType();
        JobDetails jobDetails = jobExecutionContext.getJobDetails();
        try {
            Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) == null) {
                GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
            }
            cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }
            // This installed path is a mandetory field, because this could change based on the computing resource
            if(jobDetails == null) {
                log.error("There is not JobDetails so cancelations cannot perform !!!");
                return;
            }
            if (jobDetails.getJobID() != null) {
                cluster.cancelJob(jobDetails.getJobID());
            } else {
                log.error("No Job Id is set, so cannot perform the cancel operation !!!");
                return;
            }
            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.CANCELED);
            // we know this host is type GsiSSHHostType
        } catch (SSHApiException e) {
            String error = "Error submitting the job to host " + host.getHostAddress() + " message: " + e.getMessage();
            log.error(error);
            jobDetails.setJobID("none");
            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
            GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            throw new GFacProviderException(error, e);
        } catch (Exception e) {
            String error = "Error submitting the job to host " + host.getHostAddress() + " message: " + e.getMessage();
            log.error(error);
            jobDetails.setJobID("none");
            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
            GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            throw new GFacProviderException(error, e);
        }
    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException {
        // have to implement the logic to recover a gfac failure
        log.info("Invoking Recovering for the Experiment: " + jobExecutionContext.getExperimentID());
        HostDescriptionType host = jobExecutionContext.getApplicationContext().
                getHostDescription().getType();
        String jobId = "";
        String jobDesc = "";
        try {
            String pluginData = GFacUtils.getPluginData(jobExecutionContext, this.getClass().getName());
            String[] split = pluginData.split(",");
            if (split.length < 2) {
                try {
                    this.execute(jobExecutionContext);
                } catch (GFacException e) {
                    throw new GFacProviderException("Error recovering provider", e);
                }
                return;
            }
            jobDesc = split[0].substring(7);
            jobId = split[1].substring(6);

            log.info("Following data have recovered: ");
            log.info("Job Description: " + jobDesc);
            log.info("Job Id: " + jobId);
            if (jobId == null || "none".equals(jobId) ||
                    "".equals(jobId)) {
                try {
                    this.execute(jobExecutionContext);
                } catch (GFacException e) {
                    throw new GFacProviderException("Error recovering provider", e);
                }
                return;
            }
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            // Now we are we have enough data to recover
            JobDetails jobDetails = new JobDetails();
            jobDetails.setJobDescription(jobDesc);
            jobDetails.setJobID(jobId);
            jobExecutionContext.setJobDetails(jobDetails);
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) == null) {
                try {
                    GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
                } catch (ApplicationSettingsException e) {
                    log.error(e.getMessage());
                    throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
                }
            }
            delegateToMonitorHandlers(jobExecutionContext, (GsisshHostType) host, jobId);
        } catch (GFacHandlerException e) {
            throw new GFacProviderException("Error delegating already ran job to Monitoring", e);
        }
    }
}
