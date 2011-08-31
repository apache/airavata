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
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.exception.JobSubmissionFault;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.provider.utils.GramRSLGenerator;
import org.apache.airavata.core.gfac.provider.utils.JobSubmissionListener;
import org.apache.airavata.core.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.type.app.GramApplicationDeployment;
import org.apache.airavata.core.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.type.host.GlobusHost;
import org.apache.airavata.core.gfac.utils.ErrorCodes;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class GramProvider extends AbstractProvider {

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";

    public void initialize(InvocationContext invocationContext) throws GfacException {
        GlobusHost host = (GlobusHost)invocationContext.getGfacContext().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment)invocationContext.getGfacContext().getApp();
    	
        GridFtp ftp = new GridFtp();

        try {
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();

            String hostgridFTP = host.getGridFTPEndPoint();
            if (host.getGridFTPEndPoint() == null){
                hostgridFTP = host.getName();
            }

            URI tmpdirURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getTmpDir());
            URI workingDirURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getWorkingDir());
            URI inputURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getInputDir());
            URI outputURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getOutputDir());

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
    	GlobusHost host = (GlobusHost)invocationContext.getGfacContext().getHost();
    	GramApplicationDeployment app = (GramApplicationDeployment)invocationContext.getGfacContext().getApp();
        ServiceDescription service = invocationContext.getGfacContext().getService();

        log.info("Searching for Gate Keeper");
        String gatekeeper = host.getGlobusGateKeeperEndPoint();
        if (gatekeeper == null) {
        	gatekeeper = host.getName();
        }
        log.info("Using Globus GateKeeper " + gatekeeper);
        GramJob job = null;
        boolean jobSucsseful = false;

        String rsl = "";
        int errCode = 0;

        try {
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();

            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(invocationContext);
            rsl = jobAttr.toRSL();
            job = new GramJob(rsl);
            job.setCredentials(gssCred);

            log.info("RSL = " + rsl);

            NotificationService notifier = invocationContext.getExecutionContext().getNotificationService();
            notifier.startExecution(this, invocationContext);
            StringBuffer buf = new StringBuffer();

            JobSubmissionListener listener = new JobSubmissionListener(job, invocationContext);
            job.addListener(listener);
            log.info("Request to contact:" + gatekeeper);
            /*
             * The first boolean is to specify the job is a batch job - use true for interactive and false for batch.
             * The second boolean is to specify to use the full proxy and not delegate a limited proxy.
             */
            job.request(gatekeeper, false, false);

            log.info("JobID = " + job.getIDAsString());

            // Gram.request(contact, job, false, false);

            buf.append("Finished launching job, Host = ")
            		.append(host.getName())
                    .append(" RSL = ")
                    .append(job.getRSL())
                    .append(" working directory = ")
                    .append(app.getWorkingDir())
                    .append(" tempDirectory = ")
                    .append(app.getTmpDir())
                    .append(" Globus GateKeeper cantact = ")
                    .append(gatekeeper);
            notifier.info(this, invocationContext, buf.toString());
            String gramJobid = job.getIDAsString();
            notifier.info(this, invocationContext, "JobID=" + gramJobid);
            log.info(buf.toString());
            
            notifier.applicationInfo(this, invocationContext, gramJobid, gatekeeper, null, null,
                    gssCred.getName().toString(), null, job.getRSL());

            listener.waitFor();
            job.removeListener(listener);

            int jobStatus = listener.getStatus();
            if (jobStatus == GramJob.STATUS_FAILED) {
                errCode = listener.getError();                
                String errorMsg = "Job " + job.getID() + " on host " + host.getName() + " Error Code = " + errCode;                
                GfacException error = new JobSubmissionFault(new Exception(errorMsg), "GFAC HOST", gatekeeper, rsl, this);
                if (errCode == 8) {
                	error.setFaultCode(ErrorCodes.JOB_CANCELED);
                } else {
                	error.setFaultCode(ErrorCodes.JOB_FAILED);
                }
                throw error;
            }
            notifier.finishExecution(this, invocationContext);

            /*
             * Stdout and Stderror
             */
            GridFtp ftp = new GridFtp();

            String hostgridFTP = host.getGridFTPEndPoint();
            if (host.getGridFTPEndPoint() == null){
                hostgridFTP = host.getName();
            }

            URI stdoutURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getStdOut());
            URI stderrURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getStdErr());

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
            GfacException error = new JobSubmissionFault(e, localHost, gatekeeper, rsl, this);
            if (errCode == 8) {
                error.setFaultCode(ErrorCodes.JOB_CANCELED);
            } else {
                error.setFaultCode(ErrorCodes.JOB_FAILED);
            }
            throw error;
        } catch (GSSException e) {
            throw new JobSubmissionFault(e, "GFAC HOST", gatekeeper, rsl, this);        
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
    }

}
