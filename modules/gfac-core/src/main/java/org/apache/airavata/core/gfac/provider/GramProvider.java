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
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.host.GlobusHost;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.JobSubmissionFault;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.exception.SecurityException;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.provider.utils.GramRSLGenerator;
import org.apache.airavata.core.gfac.provider.utils.JobSubmissionListener;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class GramProvider extends AbstractProvider {

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
    private GramJob job;
    private String gateKeeper;
    private JobSubmissionListener listener;

    public void initialize(InvocationContext invocationContext) throws ProviderException {
    }

    public void dispose(InvocationContext invocationContext) throws GfacException {
    }

    public void abort(InvocationContext invocationContext) throws GfacException {
    }

    public void makeDirectory(InvocationContext invocationContext) throws ProviderException {
        GlobusHost host = (GlobusHost) invocationContext.getExecutionDescription().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment) invocationContext.getExecutionDescription()
                .getApp();

        GridFtp ftp = new GridFtp();

        try {
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();

            String hostgridFTP = host.getGridFTPEndPoint();
            if (host.getGridFTPEndPoint() == null) {
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
        } catch (URISyntaxException e) {
            throw new ProviderException("URI is malformatted:" + e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (ToolsException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

    public void setupEnvironment(InvocationContext invocationContext) throws ProviderException {
        GlobusHost host = (GlobusHost) invocationContext.getExecutionDescription().getHost();

        log.info("Searching for Gate Keeper");
        gateKeeper = host.getGlobusGateKeeperEndPoint();
        if (gateKeeper == null) {
            gateKeeper = host.getName();
        }
        log.info("Using Globus GateKeeper " + gateKeeper);

        String rsl = "";

        try {
            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(invocationContext);
            rsl = jobAttr.toRSL();

            log.info("RSL = " + rsl);

            job = new GramJob(rsl);
            listener = new JobSubmissionListener(job, invocationContext);
            job.addListener(listener);

        } catch (ToolsException te) {
            throw new ProviderException(te.getMessage(), te);
        }

    }

    public void executeApplication(InvocationContext invocationContext) throws ProviderException {
        GlobusHost host = (GlobusHost) invocationContext.getExecutionDescription().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment) invocationContext.getExecutionDescription()
                .getApp();
        StringBuffer buf = new StringBuffer();
        try {

            /*
             * Set Security
             */
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();
            job.setCredentials(gssCred);

            log.info("Request to contact:" + gateKeeper);

            buf.append("Finished launching job, Host = ").append(host.getName()).append(" RSL = ").append(job.getRSL())
                    .append(" working directory = ").append(app.getWorkingDir()).append(" tempDirectory = ")
                    .append(app.getTmpDir()).append(" Globus GateKeeper cantact = ").append(gateKeeper);
            invocationContext.getExecutionContext().getNotifier().info(this, invocationContext, buf.toString());

            /*
             * The first boolean is to specify the job is a batch job - use true
             * for interactive and false for batch. The second boolean is to
             * specify to use the full proxy and not delegate a limited proxy.
             */
            job.request(gateKeeper, false, false);
            String gramJobid = job.getIDAsString();
            log.info("JobID = " + gramJobid);
            invocationContext.getExecutionContext().getNotifier().info(this, invocationContext, "JobID=" + gramJobid);

            log.info(buf.toString());

            invocationContext
                    .getExecutionContext()
                    .getNotifier()
                    .applicationInfo(this, invocationContext, gramJobid, gateKeeper, null, null,
                            gssCred.getName().toString(), null, job.getRSL());

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
            if (jobStatus == GramJob.STATUS_FAILED) {
                int errCode = listener.getError();
                String errorMsg = "Job " + job.getID() + " on host " + host.getName() + " Error Code = " + errCode;
                JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST",
                        gateKeeper, job.getRSL());
                if (errCode == 8) {
                    error.setReason(JobSubmissionFault.JOB_CANCEL);
                } else {
                    error.setReason(JobSubmissionFault.JOB_FAILED);
                }
                throw error;
            }

        } catch (GramException e) {
            JobSubmissionFault error = new JobSubmissionFault(this, e, host.getName(), gateKeeper, job.getRSL());
            if (listener.getError() == 8) {
                error.setReason(JobSubmissionFault.JOB_CANCEL);
            } else {
                error.setReason(JobSubmissionFault.JOB_FAILED);
            }
            throw error;
        } catch (GSSException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new ProviderException("Thread", e);
        } catch (SecurityException e) {
            throw new ProviderException(e.getMessage(), e);
        } finally {
            if (job != null) {
                try {
                    job.cancel();
                } catch (Exception e) {
                }
            }
        }

    }

    public void retrieveOutput(InvocationContext invocationContext) throws ProviderException {
        GlobusHost host = (GlobusHost) invocationContext.getExecutionDescription().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment) invocationContext.getExecutionDescription()
                .getApp();
        GridFtp ftp = new GridFtp();

        try {
            GSSCredential gssCred = ((GSISecurityContext) invocationContext
                    .getSecurityContext(MYPROXY_SECURITY_CONTEXT)).getGssCredentails();

            /*
             * Stdout and Stderror
             */

            String hostgridFTP = host.getGridFTPEndPoint();
            if (host.getGridFTPEndPoint() == null) {
                hostgridFTP = host.getName();
            }

            URI stdoutURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getStdOut());
            URI stderrURI = GfacUtils.createGsiftpURI(hostgridFTP, app.getStdErr());

            log.info("STDOUT:" + stdoutURI.toString());
            log.info("STDERR:" + stderrURI.toString());

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
            OutputUtils.fillOutputFromStdout(invocationContext.<AbstractParameter> getOutput(), stdout, stderr);
            
        } catch (URISyntaxException e) {
            throw new ProviderException("URI is malformatted:" + e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (ToolsException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

}
