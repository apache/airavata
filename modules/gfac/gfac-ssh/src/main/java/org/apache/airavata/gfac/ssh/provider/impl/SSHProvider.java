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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.handler.ThreadedHandler;
import org.apache.airavata.gfac.core.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.core.provider.AbstractProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.monitor.email.EmailBasedMonitor;
import org.apache.airavata.gfac.monitor.email.EmailMonitorFactory;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.CommandExecutor;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.RawCommandInfo;
import org.apache.airavata.gsi.ssh.impl.StandardOutReader;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.EmailMonitorProperty;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

                GFacUtils.saveJobStatus(jobExecutionContext, details, JobState.SETUP);
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

//                GFacUtils.updateJobStatus(details, JobState.SUBMITTED);
                RawCommandInfo rawCommandInfo = new RawCommandInfo("/bin/chmod 755 " + executable + "; " + executable);

                StandardOutReader jobIDReaderCommandOutput = new StandardOutReader();

                CommandExecutor.executeCommand(rawCommandInfo, cluster.getSession(), jobIDReaderCommandOutput);
                String stdOutputString = getOutputifAvailable(jobIDReaderCommandOutput, "Error submitting job to resource");

                log.info("stdout=" + stdOutputString);

//                GFacUtils.updateJobStatus(details, JobState.COMPLETE);
            } catch (Exception e) {
                throw new GFacProviderException(e.getMessage(), e);
            }
        } else {
            try {
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
                    delegateToMonitorHandlers(jobExecutionContext);
                } catch (SSHApiException e) {
                    String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                    log.error(error);
                    jobDetails.setJobID("none");
                    GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
                    GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                    throw new GFacProviderException(error, e);
                } catch (Exception e) {
                    String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                    log.error(error);
                    jobDetails.setJobID("none");
                    GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
                    GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                    throw new GFacProviderException(error, e);
                }
            } catch (GFacException e) {
                throw new GFacProviderException(e.getMessage(), e);
            }
        }
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {

    }


    public void cancelJob(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
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
                log.error("There is not JobDetails so cancelations cannot perform !!!");
                return;
            }
            try {
                if (jobDetails.getJobID() != null) {
                    cluster.cancelJob(jobDetails.getJobID());
                } else {
                    log.error("No Job Id is set, so cannot perform the cancel operation !!!");
                    return;
                }
                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.CANCELED);
            } catch (SSHApiException e) {
                String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                log.error(error);
                jobDetails.setJobID("none");
                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
                GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                throw new GFacProviderException(error, e);
            } catch (Exception e) {
                String error = "Error submitting the job to host " + jobExecutionContext.getHostName() + " message: " + e.getMessage();
                log.error(error);
                jobDetails.setJobID("none");
                GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.FAILED);
                GFacUtils.saveErrorDetails(jobExecutionContext, error, CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                throw new GFacProviderException(error, e);
            }
            // we know this host is type GsiSSHHostType
        }
    }

//    public void removeFromMonitorHandlers(JobExecutionContext jobExecutionContext, GsisshHostType host, String jobID) throws GFacHandlerException {
//        List<ThreadedHandler> daemonHandlers = BetterGfacImpl.getDaemonHandlers();
//        if (daemonHandlers == null) {
//            daemonHandlers = BetterGfacImpl.getDaemonHandlers();
//        }
//        ThreadedHandler pullMonitorHandler = null;
//        ThreadedHandler pushMonitorHandler = null;
//        String monitorMode = host.getMonitorMode();
//        for (ThreadedHandler threadedHandler : daemonHandlers) {
//            if ("org.apache.airavata.gfac.monitor.handlers.GridPullMonitorHandler".equals(threadedHandler.getClass().getName())) {
//                pullMonitorHandler = threadedHandler;
//                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PULL.equals(monitorMode)) {
//                    jobExecutionContext.setProperty("cancel","true");
//                    pullMonitorHandler.invoke(jobExecutionContext);
//                } else {
//                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PULL" +
//                            " to handle by the GridPullMonitorHandler");
//                }
//            } else if ("org.apache.airavata.gfac.monitor.handlers.GridPushMonitorHandler".equals(threadedHandler.getClass().getName())) {
//                pushMonitorHandler = threadedHandler;
//                if ("".equals(monitorMode) || monitorMode == null || org.apache.airavata.common.utils.Constants.PUSH.equals(monitorMode)) {
//                    log.info("Job is launched successfully now parsing it to monitoring in push mode, JobID Returned:  " + jobID);
//                    pushMonitorHandler.invoke(jobExecutionContext);
//                } else {
//                    log.error("Currently we only support Pull and Push monitoring and monitorMode should be PUSH" +
//                            " to handle by the GridPushMonitorHandler");
//                }
//            }
//            // have to handle the GridPushMonitorHandler logic
//        }
//        if (pullMonitorHandler == null && pushMonitorHandler == null && ExecutionMode.ASYNCHRONOUS.equals(jobExecutionContext.getGFacConfiguration().getExecutionMode())) {
//            log.error("No Daemon handler is configured in gfac-config.xml, either pull or push, so monitoring will not invoked" +
//                    ", execution is configured as asynchronous, so Outhandler will not be invoked");
//        }
//    }

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
//                String[] values = ((URIArrayType) actualParameter.getType()).getValueArray();
//                for (String value : values) {
//                    cmd.append(value);
//                    cmd.append(SPACE);
//                }
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

    public void delegateToMonitorHandlers(JobExecutionContext jobExecutionContext) throws GFacHandlerException, AppCatalogException {
        if (jobExecutionContext.getPreferredJobSubmissionProtocol()== JobSubmissionProtocol.SSH) {
            String jobSubmissionInterfaceId = jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId();
            SSHJobSubmission sshJobSubmission = jobExecutionContext.getAppCatalog().getComputeResource().getSSHJobSubmission(jobSubmissionInterfaceId);
            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
            if (monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR) {
                EmailMonitorProperty emailMonitorProp = sshJobSubmission.getEmailMonitorProperty();
                if (emailMonitorProp != null) {
                    EmailBasedMonitor emailBasedMonitor = EmailMonitorFactory.getEmailBasedMonitor(emailMonitorProp);
                    emailBasedMonitor.addToJobMonitorMap(jobExecutionContext);
                    return;
                }
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
