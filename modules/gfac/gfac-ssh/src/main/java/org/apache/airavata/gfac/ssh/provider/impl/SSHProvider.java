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

package org.apache.airavata.gfac.ssh.provider.impl;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.handler.ThreadedHandler;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.monitor.state.GfacExperimentStateChangeRequest;
import org.apache.airavata.gfac.core.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.core.provider.AbstractProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.monitor.email.EmailBasedMonitor;
import org.apache.airavata.gfac.monitor.email.EmailMonitorFactory;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.CommandExecutor;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.JobStatus;
import org.apache.airavata.gsi.ssh.impl.RawCommandInfo;
import org.apache.airavata.gsi.ssh.impl.StandardOutReader;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.xmlbeans.XmlException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.*;

/**
 * Execute application using remote SSH
 */
public class SSHProvider extends AbstractProvider {
    private static final Logger log = LoggerFactory.getLogger(SSHProvider.class);
    private Cluster cluster;
    private String jobID = null;
    private String taskID = null;
    // we keep gsisshprovider to support qsub submission incase of hpc scenario with ssh
    private boolean hpcType = false;

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        try {
            super.initialize(jobExecutionContext);
            String hostAddress = jobExecutionContext.getHostName();
            ResourceJobManager resourceJobManager = jobExecutionContext.getResourceJobManager();
            ResourceJobManagerType resourceJobManagerType = resourceJobManager.getResourceJobManagerType();
            if (jobExecutionContext.getSecurityContext(hostAddress) == null) {
                GFACSSHUtils.addSecurityContext(jobExecutionContext);
            }
            taskID = jobExecutionContext.getTaskData().getTaskID();

            JobSubmissionProtocol preferredJobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
            if (preferredJobSubmissionProtocol == JobSubmissionProtocol.SSH && resourceJobManagerType == ResourceJobManagerType.FORK) {
                jobID = "SSH_" + jobExecutionContext.getHostName() + "_" + Calendar.getInstance().getTimeInMillis();
                cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();

                String remoteFile = jobExecutionContext.getWorkingDir() + File.separatorChar + Constants.EXECUTABLE_NAME;
                details.setJobID(taskID);
                details.setJobDescription(remoteFile);
                jobExecutionContext.setJobDetails(details);
                // FIXME : Why cluster is passed as null
                JobDescriptor jobDescriptor = GFACSSHUtils.createJobDescriptor(jobExecutionContext, cluster);
                details.setJobDescription(jobDescriptor.toXML());

                GFacUtils.saveJobStatus(jobExecutionContext, details, JobState.SETUP, monitorPublisher);
                log.info(remoteFile);
                File runscript = createShellScript(jobExecutionContext);
                cluster.scpTo(remoteFile, runscript.getAbsolutePath());
            } else {
                hpcType = true;
            }
        } catch (ApplicationSettingsException e) {
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        } catch (Exception e) {
            throw new GFacProviderException(e.getLocalizedMessage(), e);
        }
    }


    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        if (!hpcType) {
            try {
                /*
                 * Execute
                 */
                String executable = jobExecutionContext.getWorkingDir() + File.separatorChar + Constants.EXECUTABLE_NAME;
                details.setJobDescription(executable);
                RawCommandInfo rawCommandInfo = new RawCommandInfo("/bin/chmod 755 " + executable + "; " + executable);
                StandardOutReader jobIDReaderCommandOutput = new StandardOutReader();
                log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
                CommandExecutor.executeCommand(rawCommandInfo, cluster.getSession(), jobIDReaderCommandOutput);
                String stdOutputString = getOutputifAvailable(jobIDReaderCommandOutput, "Error submitting job to resource");
                log.info("stdout=" + stdOutputString);
            } catch (Exception e) {
                throw new GFacProviderException(e.getMessage(), e);
            }
        } else {
            try {
                StringBuffer data = new StringBuffer();
                jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
                JobDetails jobDetails = new JobDetails();
                String hostAddress = jobExecutionContext.getHostName();
                try {
                    Cluster cluster = null;
                    if (jobExecutionContext.getSecurityContext(hostAddress) == null) {
                        GFACSSHUtils.addSecurityContext(jobExecutionContext);
                    }
                    cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();
                    if (cluster == null) {
                        throw new GFacProviderException("Security context is not set properly");
                    } else {
                        log.info("Successfully retrieved the Security Context");
                    }
                    // This installed path is a mandetory field, because this could change based on the computing resource
                    JobDescriptor jobDescriptor = GFACSSHUtils.createJobDescriptor(jobExecutionContext, cluster);
                    jobDetails.setJobName(jobDescriptor.getJobName());
                    log.info(jobDescriptor.toXML());
                    String jobID = cluster.submitBatchJob(jobDescriptor);
                    ResourceJobManager resourceJobManager = jobExecutionContext.getResourceJobManager();
                    String jobFileContent = CommonUtils.getJobFileContent(jobDescriptor, resourceJobManager.getResourceJobManagerType().toString(), resourceJobManager.getJobManagerBinPath());
                    jobDetails.setJobDescription(jobFileContent);
                    jobDetails.setWorkingDir(jobDescriptor.getWorkingDirectory());
                    if (jobID != null && !jobID.isEmpty()) {
                        jobDetails.setJobID(jobID);
                        GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.SUBMITTED, monitorPublisher);
                        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                                , GfacExperimentState.JOBSUBMITTED));
                        jobExecutionContext.setJobDetails(jobDetails);
                        if (verifyJobSubmissionByJobId(cluster, jobID)) {
                            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                                    , GfacExperimentState.JOBSUBMITTED));
                            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.QUEUED, monitorPublisher);
                        }
                    } else {
                        jobExecutionContext.setJobDetails(jobDetails);
                        int verificationTryCount = 0;
                        while (verificationTryCount++ < 3) {
                            String verifyJobId = verifyJobSubmission(cluster, jobDetails);
                            if (verifyJobId != null && !verifyJobId.isEmpty()) {
                                // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
                                jobID = verifyJobId;
                                jobDetails.setJobID(jobID);
                                monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                                        , GfacExperimentState.JOBSUBMITTED));
                                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.QUEUED, monitorPublisher);
                                break;
                            }
                            Thread.sleep(verificationTryCount*1000);
                        }
                    }

                    if (jobID == null || jobID.isEmpty()) {
                        String msg = "expId:" + jobExecutionContext.getExperimentID() + " Couldn't find remote jobId for JobName:"
                                + jobDetails.getJobName() + ", both submit and verify steps doesn't return a valid JobId. Hence changing experiment state to Failed";
                        log.error(msg);
                        GFacUtils.saveErrorDetails(jobExecutionContext, msg, CorrectiveAction.CONTACT_SUPPORT , ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                        GFacUtils.publishTaskStatus(jobExecutionContext, monitorPublisher, TaskState.FAILED);
                        return;
                    }
                    data.append("jobDesc=").append(jobDescriptor.toXML());
                    data.append(",jobId=").append(jobDetails.getJobID());
                    monitor(jobExecutionContext);
                } catch (SSHApiException e) {
                    String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                    log.error(error);
                    jobDetails.setJobID("none");
                    GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED, monitorPublisher);
                    GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                    throw new GFacProviderException(error, e);
                } catch (Exception e) {
                    String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                    log.error(error);
                    jobDetails.setJobID("none");
                    GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED, monitorPublisher);
                    GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                    throw new GFacProviderException(error, e);
                } finally {
                    log.info("Saving data for future recovery: ");
                    log.info(data.toString());
                    GFacUtils.saveHandlerData(jobExecutionContext, data, this.getClass().getName());
                }
            } catch (GFacException e) {
                throw new GFacProviderException(e.getMessage(), e);
            }
        }
    }

    private boolean verifyJobSubmissionByJobId(Cluster cluster, String jobID) throws SSHApiException {
        JobStatus status = cluster.getJobStatus(jobID);
        return status != null &&  status != JobStatus.U;
    }

    private String verifyJobSubmission(Cluster cluster, JobDetails jobDetails) {
        String jobName = jobDetails.getJobName();
        String jobId = null;
        try {
          jobId  = cluster.getJobIdByJobName(jobName, cluster.getServerInfo().getUserName());
        } catch (SSHApiException e) {
            log.error("Error while verifying JobId from JobName");
        }
        return jobId;
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {

    }

    public boolean cancelJob(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        JobDetails jobDetails = jobExecutionContext.getJobDetails();
        StringBuffer data = new StringBuffer();
        String hostAddress = jobExecutionContext.getHostName();
        if (!hpcType) {
            throw new NotImplementedException();
        } else {
            Cluster cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }
            // This installed path is a mandetory field, because this could change based on the computing resource
            if (jobDetails == null) {
                log.error("There is not JobDetails, Cancel request can't be performed !!!");
                return false;
            }
            try {
                if (jobDetails.getJobID() != null) {
                    if (cluster.cancelJob(jobDetails.getJobID()) != null) {
                        // if this operation success without any exceptions, we can assume cancel operation succeeded.
                        GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.CANCELED, monitorPublisher);
                        return true;
                    } else {
                        log.info("Job Cancel operation failed");
                    }
                } else {
                    log.error("No Job Id is set, so cannot perform the cancel operation !!!");
                    throw new GFacProviderException("Cancel request failed to cancel job as JobId is null in Job Execution Context");
                }
            } catch (SSHApiException e) {
                String error = "Cancel request failed " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                log.error(error);
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                GFacUtils.saveErrorDetails(jobExecutionContext, errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//                throw new GFacProviderException(error, e);
            } catch (Exception e) {
                String error = "Cancel request failed " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                log.error(error);
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                GFacUtils.saveErrorDetails(jobExecutionContext, errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//                throw new GFacProviderException(error, e);
            }
            return false;
        }
    }

    private File createShellScript(JobExecutionContext context) throws IOException {
        String uniqueDir = jobExecutionContext.getApplicationName() + System.currentTimeMillis()
                + new Random().nextLong();

        File shellScript = File.createTempFile(uniqueDir, "sh");
        OutputStream out = new FileOutputStream(shellScript);

        out.write("#!/bin/bash\n".getBytes());
        out.write(("cd " + jobExecutionContext.getWorkingDir() + "\n").getBytes());
        out.write(("export " + Constants.INPUT_DATA_DIR_VAR_NAME + "=" + jobExecutionContext.getInputDir() + "\n").getBytes());
        out.write(("export " + Constants.OUTPUT_DATA_DIR_VAR_NAME + "=" + jobExecutionContext.getOutputDir() + "\n")
                .getBytes());
        // get the env of the host and the application
        List<SetEnvPaths> envPathList = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getSetEnvironment();
        for (SetEnvPaths setEnvPaths : envPathList) {
            log.debug("Env[" + setEnvPaths.getName() + "] = " + setEnvPaths.getValue());
            out.write(("export " + setEnvPaths.getName() + "=" + setEnvPaths.getValue() + "\n").getBytes());
        }

        // prepare the command
        final String SPACE = " ";
        StringBuffer cmd = new StringBuffer();
        cmd.append(jobExecutionContext.getExecutablePath());
        cmd.append(SPACE);

        MessageContext input = context.getInMessageContext();
        Map<String, Object> inputs = input.getParameters();
        Set<String> keys = inputs.keySet();
        for (String paramName : keys) {
            InputDataObjectType inputParamType = (InputDataObjectType) input.getParameters().get(paramName);
            //if ("URIArray".equals(actualParameter.getType().getType().toString())) {
            if (inputParamType.getType() == DataType.URI) {
                String value = inputParamType.getValue();
                cmd.append(value);
                cmd.append(SPACE);
            } else {
                String paramValue = inputParamType.getValue();
                cmd.append(paramValue);
                cmd.append(SPACE);
            }
        }
        // We redirect the error and stdout to remote files, they will be read
        // in later
        cmd.append(SPACE);
        cmd.append("1>");
        cmd.append(SPACE);
        cmd.append(jobExecutionContext.getStandardOutput());
        cmd.append(SPACE);
        cmd.append("2>");
        cmd.append(SPACE);
        cmd.append(jobExecutionContext.getStandardError());

        String cmdStr = cmd.toString();
        log.info("Command = " + cmdStr);
        out.write((cmdStr + "\n").getBytes());
        String message = "\"execuationSuceeded\"";
        out.write(("echo " + message + "\n").getBytes());
        out.close();

        return shellScript;
    }

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

    /**
     * This method will read standard output and if there's any it will be parsed
     *
     * @param jobIDReaderCommandOutput
     * @param errorMsg
     * @return
     * @throws SSHApiException
     */
    private String getOutputifAvailable(StandardOutReader jobIDReaderCommandOutput, String errorMsg) throws SSHApiException {
        String stdOutputString = jobIDReaderCommandOutput.getStdOutputString();
        String stdErrorString = jobIDReaderCommandOutput.getStdErrorString();

        if (stdOutputString == null || stdOutputString.isEmpty() || (stdErrorString != null && !stdErrorString.isEmpty())) {
            log.error("Standard Error output : " + stdErrorString);
            throw new SSHApiException(errorMsg + stdErrorString);
        }
        return stdOutputString;
    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        // have to implement the logic to recover a gfac failure
        initialize(jobExecutionContext);
        if(hpcType) {
            log.info("Invoking Recovering for the Experiment: " + jobExecutionContext.getExperimentID());
            String hostName = jobExecutionContext.getHostName();
            String jobId = "";
            String jobDesc = "";
            String jobName = "";
            try {
                String pluginData = GFacUtils.getHandlerData(jobExecutionContext, this.getClass().getName());
                String[] split = pluginData.split(",");
                if (split.length < 2) {
                    this.execute(jobExecutionContext);
                    return;
                }
                jobDesc = split[0].substring(8);
                jobId = split[1].substring(6);
                try {
                    JobDescriptor jobDescriptor = JobDescriptor.fromXML(jobDesc);
                    jobName = jobDescriptor.getJobName();
                } catch (XmlException e) {
                    log.error(e.getMessage(), e);
                    log.error("Cannot parse plugin data stored, but trying to recover");

                }
                log.info("Following data have recovered: ");
                log.info("Job Description: " + jobDesc);
                log.info("Job Id: " + jobId);
                if (jobName.isEmpty() || jobId.isEmpty() || "none".equals(jobId) ||
                        "".equals(jobId)) {
                    log.info("Cannot recover data so submitting the job again !!!");
                    this.execute(jobExecutionContext);
                    return;
                }
            } catch (Exception e) {
                log.error("Error while  recovering provider", e);
            }
            try {
                // Now we are we have enough data to recover
                JobDetails jobDetails = new JobDetails();
                jobDetails.setJobDescription(jobDesc);
                jobDetails.setJobID(jobId);
                jobDetails.setJobName(jobName);
                jobExecutionContext.setJobDetails(jobDetails);
                if (jobExecutionContext.getSecurityContext(hostName) == null) {
                    try {
                        GFACSSHUtils.addSecurityContext(jobExecutionContext);
                    } catch (ApplicationSettingsException e) {
                        log.error(e.getMessage());
                        throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
                    }
                }
                monitor(jobExecutionContext);
            } catch (Exception e) {
                log.error("Error while recover the job", e);
                throw new GFacProviderException("Error delegating already ran job to Monitoring", e);
            }
        }else{
            log.info("We do not handle non hpc recovery so we simply run the Job directly");
            this.execute(jobExecutionContext);
        }
    }

    @Override
    public void monitor(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        if (jobExecutionContext.getPreferredJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
            String jobSubmissionInterfaceId = jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId();
            SSHJobSubmission sshJobSubmission = null;
            try {
                sshJobSubmission = jobExecutionContext.getAppCatalog().getComputeResource().getSSHJobSubmission(jobSubmissionInterfaceId);
            } catch (AppCatalogException e) {
                throw new GFacException("Error while reading compute resource", e);
            }
            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
            if (monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR) {
                try {
                    EmailBasedMonitor emailBasedMonitor = EmailMonitorFactory.getEmailBasedMonitor(
                            sshJobSubmission.getResourceJobManager().getResourceJobManagerType());
                    emailBasedMonitor.addToJobMonitorMap(jobExecutionContext);
                } catch (AiravataException e) {
                    throw new GFacHandlerException("Error while activating email job monitoring ", e);
                }
                return;
            }
        }

        // if email monitor is not activeated or not configure we use pull or push monitor
        List<ThreadedHandler> daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        if (daemonHandlers == null) {
            daemonHandlers = BetterGfacImpl.getDaemonHandlers();
        }
        ThreadedHandler pullMonitorHandler = null;
        ThreadedHandler pushMonitorHandler = null;
        for (ThreadedHandler threadedHandler : daemonHandlers) {
            if ("org.apache.airavata.gfac.monitor.handlers.GridPullMonitorHandler".equals(threadedHandler.getClass().getName())) {
                pullMonitorHandler = threadedHandler;
                pullMonitorHandler.invoke(jobExecutionContext);
            }
            // have to handle the GridPushMonitorHandler logic
        }
        if (pullMonitorHandler == null && pushMonitorHandler == null && ExecutionMode.ASYNCHRONOUS.equals(jobExecutionContext.getGFacConfiguration().getExecutionMode())) {
            log.error("No Daemon handler is configured in gfac-config.xml, either pull or push, so monitoring will not invoked" +
                    ", execution is configured as asynchronous, so Outhandler will not be invoked");
        }
    }
}
