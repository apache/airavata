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

package org.apache.airavata.core.gfac.provider.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.context.security.SecurityContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.JobSubmissionFault;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.exception.SecurityException;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.provider.AbstractProvider;
import org.apache.airavata.core.gfac.provider.utils.GramRSLGenerator;
import org.apache.airavata.core.gfac.provider.utils.JobSubmissionListener;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.xmlbeans.XmlException;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Provider uses Gram for job submission
 */
public class GramProvider extends AbstractProvider {

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
    private GSISecurityContext gssContext;
    private GramJob job;
    private String gateKeeper;
    private JobSubmissionListener listener;

    public void makeDirectory(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();

        GridFtp ftp = new GridFtp();

        try {
            gssContext = (GSISecurityContext)invocationContext.getSecurityContext(MYPROXY_SECURITY_CONTEXT);
            GSSCredential gssCred = gssContext.getGssCredentails();
            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[] { host.getHostAddress() };
            }

            boolean success = false;
            ProviderException pe = new ProviderException("");

            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {

                    URI tmpdirURI = GfacUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GfacUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GfacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GfacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

                    log.info("Host FTP = " + hostgridFTP);
                    log.info("temp directory = " + tmpdirURI);
                    log.info("Working directory = " + workingDirURI);
                    log.info("Input directory = " + inputURI);
                    log.info("Output directory = " + outputURI);

                    ftp.makeDir(tmpdirURI, gssCred);
                    ftp.makeDir(workingDirURI, gssCred);
                    ftp.makeDir(inputURI, gssCred);
                    success = true;
                    break;
                } catch (URISyntaxException e) {
                    pe = new ProviderException("URI is malformatted:" + e.getMessage(), e);

                } catch (ToolsException e) {
                    pe = new ProviderException(e.getMessage(), e);
                }
            }
            if (success == false) {
                throw pe;
            }
        } catch (SecurityException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

    public void setupEnvironment(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();

        log.info("Searching for Gate Keeper");


        String tmp[] = host.getGlobusGateKeeperEndPointArray();
        if (tmp == null || tmp.length == 0) {
            gateKeeper = host.getHostAddress();
        }else{
            /*
             * TODO: algorithm for correct gatekeeper selection
             */
            gateKeeper = tmp[0];
        }
        log.info("Using Globus GateKeeper " + gateKeeper);

        try {
            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(invocationContext);
            String rsl = jobAttr.toRSL();

            log.info("RSL = " + rsl);

            job = new GramJob(rsl);
            listener = new JobSubmissionListener(job, invocationContext);
            job.addListener(listener);

        } catch (ToolsException te) {
            throw new ProviderException(te.getMessage(), te);
        }

    }

    public void executeApplication(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();

        StringBuffer buf = new StringBuffer();
        try {

            /*
             * Set Security
             */
            GSSCredential gssCred = gssContext.getGssCredentails();
            job.setCredentials(gssCred);

            log.info("Request to contact:" + gateKeeper);

            buf.append("Finished launching job, Host = ").append(host.getHostAddress()).append(" RSL = ")
                    .append(job.getRSL()).append(" working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" tempDirectory = ").append(app.getScratchWorkingDirectory())
                    .append(" Globus GateKeeper cantact = ").append(gateKeeper);
            invocationContext.getExecutionContext().getNotifier().info(invocationContext, buf.toString());

            /*
             * The first boolean is to specify the job is a batch job - use true for interactive and false for batch.
             * The second boolean is to specify to use the full proxy and not delegate a limited proxy.
             */
            job.request(gateKeeper, false, false);
            String gramJobid = job.getIDAsString();
            log.info("JobID = " + gramJobid);
            invocationContext.getExecutionContext().getNotifier().info(invocationContext, "JobID=" + gramJobid);

            log.info(buf.toString());

            invocationContext
                    .getExecutionContext()
                    .getNotifier()
                    .applicationInfo(invocationContext, gramJobid, gateKeeper, null, null,
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
                String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress() + " Error Code = "
                        + errCode;
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
            JobSubmissionFault error = new JobSubmissionFault(this, e, host.getHostAddress(), gateKeeper, job.getRSL());
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

    public Map<String, ?> processOutput(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();
        GridFtp ftp = new GridFtp();
        try {
            GSSCredential gssCred = gssContext.getGssCredentails();

            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[] { host.getHostAddress() };
            }

            boolean success = false;
            ProviderException pe = new ProviderException("");

            for (String endpoint : host.getGridFTPEndPointArray()) {

                try {

                    /*
                     * Stdout and Stderror
                     */

                    URI stdoutURI = GfacUtils.createGsiftpURI(endpoint, app.getStandardOutput());
                    URI stderrURI = GfacUtils.createGsiftpURI(endpoint, app.getStandardError());

                    log.info("STDOUT:" + stdoutURI.toString());
                    log.info("STDERR:" + stderrURI.toString());

                    File logDir = new File("./service_logs");
                    if (!logDir.exists()) {
                        logDir.mkdir();
                    }

                    // Get the Stdouts and StdErrs
                    String timeStampedServiceName = GfacUtils.createUniqueNameForService(invocationContext
                            .getServiceName());
                    File localStdOutFile = File.createTempFile(timeStampedServiceName, "stdout");
                    File localStdErrFile = File.createTempFile(timeStampedServiceName, "stderr");

                    String stdout = ftp.readRemoteFile(stdoutURI, gssCred, localStdOutFile);
                    String stderr = ftp.readRemoteFile(stderrURI, gssCred, localStdErrFile);

                    return OutputUtils.fillOutputFromStdout(invocationContext.<ActualParameter> getOutput(), stdout);
                } catch (ToolsException e) {
                    throw new ProviderException(e.getMessage(), e);
                } catch (URISyntaxException e) {
                    throw new ProviderException("URI is malformatted:" + e.getMessage(), e);
                }
            }

            /*
             * If the execution reach here, all GridFTP Endpoint is failed.
             */
            throw pe;

        } catch (XmlException e) {
            throw new ProviderException("Cannot read output:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ProviderException(e.getMessage(), e);
        }

    }

	@Override
	protected Map<String, ?> processInput(InvocationContext invocationContext)
            throws ProviderException {
        MessageContext inputNew = new ParameterContextImpl();
		MessageContext<Object> input = invocationContext.getInput();
        for (Iterator<String> iterator = input.getNames(); iterator.hasNext();) {
			String paramName = iterator.next();
			String paramValue = input.getStringValue(paramName);
			ActualParameter actualParameter = (ActualParameter) input
					.getValue(paramName);
			//TODO: Review this with type
			if ("URI".equals(actualParameter.getType().getType().toString())) {
				if(paramValue.startsWith("gsiftp") || paramValue.startsWith("http")){
					URI gridftpURL;
					try {
                        gridftpURL = new URI(paramValue);
                        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
                        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();
                        GridFtp ftp = new GridFtp();
                        gssContext = (GSISecurityContext) invocationContext.getSecurityContext(MYPROXY_SECURITY_CONTEXT);
                        GSSCredential gssCred = gssContext.getGssCredentails();
                        for (String endpoint : host.getGridFTPEndPointArray()) {
                            URI inputURI = GfacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                            ftp.uploadFile(gridftpURL, GfacUtils.createGsiftpURI(endpoint, inputURI.getPath() + gridftpURL.getPath()), gssCred);
                        }
                        ((URIParameterType) actualParameter.getType()).setValue(gridftpURL.getPath());
                        log.info("File: " + paramValue + " moved to " + gridftpURL.getPath());
                    } catch (URISyntaxException e) {
                        throw new ProviderException(e.getMessage(), e);
                    } catch (ToolsException e) {
                        throw new ProviderException(e.getMessage(), e);
                    } catch (SecurityException e) {
                        throw new ProviderException(e.getMessage(), e);
                    }

                }
                inputNew.add(paramName, actualParameter);
            }
		}
        invocationContext.setInput(inputNew);
		return null;
	}
}
