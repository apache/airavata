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

package org.apache.airavata.core.gfac.provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.exception.JobSubmissionFault;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.model.ExecutionModel;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.provider.utils.GramRSLGenerator;
import org.apache.airavata.core.gfac.provider.utils.JobSubmissionListener;
import org.apache.airavata.core.gfac.utils.ErrorCodes;
import org.apache.airavata.core.gfac.utils.GFacOptions.CurrentProviders;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogce.schemas.gfac.documents.GlobusGatekeeperType;

import edu.indiana.extreme.lead.workflow_tracking.common.DurationObj;

public class GramProvider extends AbstractProvider {

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";

    public void initialize(InvocationContext invocationContext) throws GfacException {
        ExecutionContext appExecContext = invocationContext.getExecutionContext();
        ExecutionModel model = appExecContext.getExecutionModel();

        GridFtp ftp = new GridFtp();

        try {
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();

            // get Hostname
            String hostgridFTP = null;

            if (model.getHostDesc().getHostConfiguration().getGridFTPArray() != null
                    && model.getHostDesc().getHostConfiguration().getGridFTPArray().length > 0) {
                hostgridFTP = model.getHostDesc().getHostConfiguration().getGridFTPArray(0).getEndPointReference();
            } else {
                hostgridFTP = model.getHost();
            }

            URI tmpdirURI = GfacUtils.createGsiftpURI(hostgridFTP, model.getTmpDir());
            URI workingDirURI = GfacUtils.createGsiftpURI(hostgridFTP, model.getWorkingDir());
            URI inputURI = GfacUtils.createGsiftpURI(hostgridFTP, model.getInputDataDir());
            URI outputURI = GfacUtils.createGsiftpURI(hostgridFTP, model.getOutputDataDir());

            log.info("Host FTP = " + hostgridFTP);
            log.info("temp directory = " + tmpdirURI);
            log.info("Working directory = " + workingDirURI);
            log.info("Input directory = " + inputURI);
            log.info("Output directory = " + outputURI);

            ftp.makeDir(tmpdirURI, gssCred);
            ftp.makeDir(workingDirURI, gssCred);
            ftp.makeDir(inputURI, gssCred);
            ftp.makeDir(outputURI, gssCred);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(InvocationContext invocationContext) throws GfacException {
        ExecutionContext context = invocationContext.getExecutionContext();

        String contact = null;
        log.info("Searching for Gate Keeper");
        GlobusGatekeeperType gatekeeper = context.getExecutionModel().getGatekeeper();
        if (gatekeeper == null) {
            contact = context.getExecutionModel().getHost();
        } else {
            contact = gatekeeper.getEndPointReference();
        }
        log.info("Using Globus GateKeeper " + contact);
        GramJob job = null;
        boolean jobSucsseful = false;

        String rsl = "";
        int errCode = 0;

        try {
            GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext()).getGssCredentails();

            log.info("Host desc = " + context.getExecutionModel().getHostDesc().xmlText());

            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(context);
            rsl = jobAttr.toRSL();
            job = new GramJob(rsl);
            job.setCredentials(gssCred);

            log.info("RSL = " + rsl);

            NotificationService notifier = context.getNotificationService();
            DurationObj compObj = notifier.computationStarted();
            StringBuffer buf = new StringBuffer();

            JobSubmissionListener listener = new JobSubmissionListener(job, context);
            job.addListener(listener);
            log.info("Request to contact:" + contact);
            // The first boolean is to specify the job is a batch job - use true
            // for interactive and false for batch.
            // the second boolean is to specify to use the full proxy and not
            // delegate a limited proxy.
            job.request(contact, false, false);

            log.info("JobID = " + job.getIDAsString());

            // Gram.request(contact, job, false, false);

            buf.append("Finished launching job, Host = ").append(context.getExecutionModel().getHost())
                    .append(" RSL = ").append(job.getRSL()).append("working directory =")
                    .append(context.getExecutionModel().getWorkingDir()).append("tempDirectory =")
                    .append(context.getExecutionModel().getTmpDir()).append("Globus GateKeeper cantact = ")
                    .append(contact);
            context.getNotificationService().info(buf.toString());
            String gramJobid = job.getIDAsString();
            context.getNotificationService().info("JobID=" + gramJobid);
            log.info(buf.toString());
            // Send Audit Notifications
            notifier.appAudit(invocationContext.getServiceName(), new URI(job.getIDAsString()), contact, null, null,
                    gssCred.getName().toString(), null, job.getRSL());

            listener.waitFor();
            job.removeListener(listener);

            int jobStatus = listener.getStatus();
            if (jobStatus == GramJob.STATUS_FAILED) {
                errCode = listener.getError();
                // Adding retry for error code to properties files as
                // gfac.retryonJobErrorCodes with comma separated
                if (context.getServiceContext().getGlobalConfiguration().getRetryonErrorCodes()
                        .contains(Integer.toString(errCode))) {
                    try {
                        log.info("Job Failed with Error code " + errCode + " and job id: " + gramJobid);
                        log.info("Retry job sumttion one more time for error code" + errCode);
                        job = new GramJob(rsl);
                        job.setCredentials(gssCred);
                        listener = new JobSubmissionListener(job, context);
                        job.addListener(listener);
                        job.request(contact, false, false);
                        String newGramJobid = job.getIDAsString();
                        String jobStatusMessage = GfacUtils.formatJobStatus(newGramJobid, "RETRY");
                        context.getNotificationService().info(jobStatusMessage);
                        context.getNotificationService().info("JobID=" + newGramJobid);
                        notifier.appAudit(context.getServiceContext().getService().getService().getServiceName()
                                .getStringValue(), new URI(job.getIDAsString()), contact, null, null, gssCred.getName()
                                .toString(), null, job.getRSL());
                        listener.waitFor();
                        job.removeListener(listener);
                        int jobStatus1 = listener.getStatus();
                        if (jobStatus1 == GramJob.STATUS_FAILED) {
                            int errCode1 = listener.getError();
                            String errorMsg = "Job " + job.getID() + " on host "
                                    + context.getExecutionModel().getHost() + " Error Code = " + errCode1;
                            String localHost = context.getServiceContext().getGlobalConfiguration().getLocalHost();
                            throw new JobSubmissionFault(new Exception(errorMsg), localHost, "", "",
                                    CurrentProviders.Gram);
                        }
                    } catch (Exception e) {
                        String localHost = context.getServiceContext().getGlobalConfiguration().getLocalHost();
                        throw new JobSubmissionFault(e, localHost, "", "", CurrentProviders.Gram);
                    }
                } else {
                    String errorMsg = "Job " + job.getID() + " on host " + context.getExecutionModel().getHost()
                            + " Error Code = " + errCode;
                    String localHost = context.getServiceContext().getGlobalConfiguration().getLocalHost();
                    GfacException error = new JobSubmissionFault(new Exception(errorMsg), localHost, contact, rsl,
                            CurrentProviders.Gram);
                    if (errCode == 8) {
                        error.setFaultCode(ErrorCodes.JOB_CANCELED);
                    } else {
                        error.setFaultCode(ErrorCodes.JOB_FAILED);
                    }
                    // error.addProperty(ErrorCodes.JOB_TYPE,
                    // ErrorCodes.JobType.Gram.toString());
                    // error.addProperty(ErrorCodes.CONTACT, contact);
                    throw error;
                }
            }
            notifier.computationFinished(compObj);

            /*
             * Stdout and Stderror
             */
            GridFtp ftp = new GridFtp();

            // get Hostname
            String hostgridFTP = null;

            if (invocationContext.getExecutionContext().getExecutionModel().getHostDesc().getHostConfiguration()
                    .getGridFTPArray() != null
                    && invocationContext.getExecutionContext().getExecutionModel().getHostDesc().getHostConfiguration()
                            .getGridFTPArray().length > 0) {
                hostgridFTP = invocationContext.getExecutionContext().getExecutionModel().getHostDesc()
                        .getHostConfiguration().getGridFTPArray(0).getEndPointReference();
            } else {
                hostgridFTP = invocationContext.getExecutionContext().getExecutionModel().getHost();
            }

            URI stdoutURI = GfacUtils.createGsiftpURI(hostgridFTP, invocationContext.getExecutionContext()
                    .getExecutionModel().getStdOut());
            URI stderrURI = GfacUtils.createGsiftpURI(hostgridFTP, invocationContext.getExecutionContext()
                    .getExecutionModel().getStderr());

            System.out.println(stdoutURI);
            System.out.println(stderrURI);

            File logDir = new File("./service_logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            // Get the Stdouts and StdErrs
            QName x = QName.valueOf(invocationContext.getServiceName());
            String timeStampedServiceName = GfacUtils.createServiceDirName(x);
            File localStdOutFile = new File(logDir, timeStampedServiceName + ".stdout");
            File localStdErrFile = new File(logDir, timeStampedServiceName + ".stderr");

            String stdout = ftp.readRemoteFile(stdoutURI, gssCred, localStdOutFile);
            String stderr = ftp.readRemoteFile(stderrURI, gssCred, localStdErrFile);

            // set to context
            OutputUtils.fillOutputFromStdout(invocationContext.getMessageContext("output"), stdout, stderr);

            jobSucsseful = true;
        } catch (GramException e) {
            String localHost = "xxxx";
            GfacException error = new JobSubmissionFault(e, localHost, contact, rsl, CurrentProviders.Gram);
            if (errCode == 8) {
                error.setFaultCode(ErrorCodes.JOB_CANCELED);
            } else {
                error.setFaultCode(ErrorCodes.JOB_FAILED);
            }
            // error.addProperty(ErrorCodes.JOB_TYPE,
            // ErrorCodes.JobType.Gram.toString());
            // error.addProperty(ErrorCodes.CONTACT, contact);
            throw error;
        } catch (GSSException e) {
            String localHost = context.getServiceContext().getGlobalConfiguration().getLocalHost();
            throw new JobSubmissionFault(e, localHost, contact, rsl, CurrentProviders.Gram);
        } catch (URISyntaxException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        } catch (InterruptedException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        } finally {
            if (job != null && !jobSucsseful) {
                try {
                    job.cancel();
                } catch (Exception e) {
                }
            }
        }

    }

    public void dispose(InvocationContext invocationContext) throws GfacException {

    }

    public void abort(InvocationContext invocationContext) throws GfacException {
        try {
            ExecutionContext context = invocationContext.getExecutionContext();
            GramJob job = new GramJob("");
            job.setID(context.getExecutionModel().getJobID());
            job.setCredentials(((GSISecurityContext) context.getSecurityContext()).getGssCredentails());
            job.cancel();
        } catch (MalformedURLException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        } catch (GramException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        } catch (GSSException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }

    }

}
