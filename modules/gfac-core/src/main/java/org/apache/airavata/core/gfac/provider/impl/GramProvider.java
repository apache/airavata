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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
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
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.airavata.schemas.wec.WorkflowOutputDataHandlingDocument;
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
                    ftp.makeDir(outputURI, gssCred);

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
                    .append(" temp directory = ").append(app.getScratchWorkingDirectory())
                    .append(" Globus GateKeeper Endpoint = ").append(gateKeeper);
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

            if(job.getExitCode() != 0 || jobStatus == GramJob.STATUS_FAILED){
                int errCode = listener.getError();
                String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress() + " Job Exit Code = "
                        + listener.getError();
                JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST",
                        gateKeeper, job.getRSL());
                errorReason(errCode, error);
                invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,error,errorMsg);
                throw error;
            }
         } catch (GramException e) {
            JobSubmissionFault error = new JobSubmissionFault(this, e, host.getHostAddress(), gateKeeper, job.getRSL());
            int errCode = listener.getError();
		    throw errorReason(errCode, error);
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

	private JobSubmissionFault errorReason(int errCode, JobSubmissionFault error) {
		if (errCode == 8) {
		    error.setReason(JobSubmissionFault.JOB_CANCEL);
		} else {
		    error.setReason(JobSubmissionFault.JOB_FAILED + " With Exit Code:" + job.getExitCode());
		}
		return error;
	}

    public Map<String, ?> processOutput(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();
        GridFtp ftp = new GridFtp();
        File localStdErrFile = null;
        try {
            GSSCredential gssCred = gssContext.getGssCredentails();

            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[] { host.getHostAddress() };
            }
            ProviderException pe = new ProviderException("");
            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {
                    /*
                     *  Read Stdout and Stderror
                     */
                    URI stdoutURI = GfacUtils.createGsiftpURI(endpoint, app.getStandardOutput());
                    URI stderrURI = GfacUtils.createGsiftpURI(endpoint, app.getStandardError());

                    log.info("STDOUT:" + stdoutURI.toString());
                    log.info("STDERR:" + stderrURI.toString());

                    File logDir = new File("./service_logs");
                    if (!logDir.exists()) {
                        logDir.mkdir();
                    }

                    String timeStampedServiceName = GfacUtils.createUniqueNameForService(invocationContext
                            .getServiceName());
                    File localStdOutFile = File.createTempFile(timeStampedServiceName, "stdout");
                    localStdErrFile = File.createTempFile(timeStampedServiceName, "stderr");

                    String stdout = ftp.readRemoteFile(stdoutURI, gssCred, localStdOutFile);
                    String stderr = ftp.readRemoteFile(stderrURI, gssCred, localStdErrFile);
                    Map<String,ActualParameter> stringMap = null;
                    MessageContext<Object> output = invocationContext.getOutput();
                    for (Iterator<String> iterator = output.getNames(); iterator.hasNext(); ) {
                        String paramName = iterator.next();
                        ActualParameter actualParameter = (ActualParameter) output.getValue(paramName);
						if ("URIArray".equals(actualParameter.getType().getType().toString())) {
							URI outputURI = GfacUtils.createGsiftpURI(endpoint,app.getOutputDataDirectory());
							List<String> outputList = ftp.listDir(outputURI,gssCred);
							String[] valueList = outputList.toArray(new String[outputList.size()]);
							((URIArrayType) actualParameter.getType()).setValueArray(valueList);
							stringMap = new HashMap<String, ActualParameter>();
							stringMap.put(paramName, actualParameter);
							invocationContext.getExecutionContext().getNotifier().output(invocationContext, actualParameter.toString());
						}
                    	else{
                    	// This is to handle exception during the output parsing.
                        stringMap = OutputUtils.fillOutputFromStdout(invocationContext.<ActualParameter>getOutput(), stdout);
                        String paramValue = output.getStringValue(paramName);
                        if(paramValue == null || paramValue.isEmpty()){
                            int errCode = listener.getError();
                            String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress();
                            JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST",
                                    gateKeeper, job.getRSL());
                            errorReason(errCode, error);
                            invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,error,
                                    readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                            throw error;
                        }
                        }
                    }
                    if(stringMap == null || stringMap.isEmpty()){
                    	ProviderException exception = new ProviderException("Error creating job output");
                    	 invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,exception,exception.getLocalizedMessage());
                         throw exception;
                    }
                    // If users has given an output DAta poth we download the output files in to that directory, this will be apath in the machine where GFac is installed
                    if(WorkflowContextHeaderBuilder.getCurrentContextHeader() != null &&
                            WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowOutputDataHandling() != null){
                        WorkflowOutputDataHandlingDocument.WorkflowOutputDataHandling workflowOutputDataHandling =
                                WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowOutputDataHandling();
                        if(workflowOutputDataHandling.getApplicationOutputDataHandlingArray().length != 0){
                            String outputDataDirectory = workflowOutputDataHandling.getApplicationOutputDataHandlingArray()[0].getOutputDataDirectory();
                            if(outputDataDirectory != null && !"".equals(outputDataDirectory)){
                                stageOutputFiles(invocationContext,outputDataDirectory);
                            }
                        }
                    }
                    return stringMap;
                }catch (XmlException e) {
                    invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                    throw new ProviderException(e.getMessage(), e);
                }
                catch (ToolsException e) {
                    invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                    throw new ProviderException(e.getMessage(), e);
                } catch (URISyntaxException e) {
                    invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                    throw new ProviderException("URI is malformatted:" + e.getMessage(), e);
                }catch (NullPointerException e) {
                    invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,e.getMessage());
                    throw new ProviderException("Outupt is not produced in stdout:" + e.getMessage(), e);
                }
            }

            /*
             * If the execution reach here, all GridFTP Endpoint is failed.
             */
            throw pe;

        } catch (Exception e) {
            invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
            throw new ProviderException(e.getMessage(), e);
        }

    }

	@Override
	protected Map<String, ?> processInput(InvocationContext invocationContext)
            throws ProviderException {
        MessageContext<ActualParameter> inputNew = new ParameterContextImpl();
        try {
		MessageContext<Object> input = invocationContext.getInput();
        for (Iterator<String> iterator = input.getNames(); iterator.hasNext();) {
			String paramName = iterator.next();
			String paramValue = input.getStringValue(paramName);
			ActualParameter actualParameter = (ActualParameter) input
					.getValue(paramName);
			//TODO: Review this with type
			if ("URI".equals(actualParameter.getType().getType().toString())) {
                        ((URIParameterType) actualParameter.getType()).setValue(stageInputFiles(invocationContext, paramValue, actualParameter));
            }else if("URIArray".equals(actualParameter.getType().getType().toString())){
                List<String> split = Arrays.asList(paramValue.split(","));
                List<String> newFiles = new ArrayList<String>();
                    for (String paramValueEach : split) {
                        newFiles.add(stageInputFiles(invocationContext, paramValueEach, actualParameter));
                    }
                ((URIArrayType) actualParameter.getType()).setValueArray(newFiles.toArray(new String[newFiles.size()]));
            }
			inputNew.add(paramName, actualParameter);
		}
        }catch (Exception e){
           invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,"Error during Input File staging");
            throw new ProviderException("Error while input File Staging", e.getCause());
        }
        invocationContext.setInput(inputNew);
		return null;
	}

    private String stageInputFiles(InvocationContext invocationContext, String paramValue, ActualParameter actualParameter) throws URISyntaxException, SecurityException, ToolsException, IOException {
        URI gridftpURL;
        gridftpURL = new URI(paramValue);
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();
        GridFtp ftp = new GridFtp();
        URI destURI = null;
        gssContext = (GSISecurityContext) invocationContext.getSecurityContext(MYPROXY_SECURITY_CONTEXT);
        GSSCredential gssCred = gssContext.getGssCredentails();
        for (String endpoint : host.getGridFTPEndPointArray()) {
            URI inputURI = GfacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
            String fileName = new File(gridftpURL.getPath()).getName();
            String s = inputURI.getPath() + File.separator + fileName;
            //if user give a url just to refer an endpoint, not a web resource we are not doing any transfer
            if (fileName != null && !"".equals(fileName)) {
                destURI = GfacUtils.createGsiftpURI(endpoint, s);
                if (paramValue.startsWith("gsiftp")) {
                    ftp.uploadFile(gridftpURL, destURI, gssCred);
                } else if (paramValue.startsWith("file")) {
                    String localFile = paramValue.substring(paramValue.indexOf(":")+1, paramValue.length());
                    ftp.uploadFile(destURI, gssCred, new FileInputStream(localFile));
                }else if (paramValue.startsWith("http")) {
                    ftp.uploadFile(destURI,
                            gssCred, (gridftpURL.toURL().openStream()));
                }else {
                    //todo throw exception telling unsupported protocol
                    return paramValue;
                }
            }else{
                // When the given input is not a web resource but a URI type input, then we don't do any transfer just keep the same value as it isin the input
                return paramValue;
            }
        }
        return destURI.getPath();
    }

    private void stageOutputFiles(InvocationContext invocationContext,String outputFileStagingPath) throws ProviderException {
        MessageContext<ActualParameter> outputNew = new ParameterContextImpl();
        MessageContext<Object> input = invocationContext.getOutput();
        for (Iterator<String> iterator = input.getNames(); iterator.hasNext(); ) {
            String paramName = iterator.next();
            String paramValue = input.getStringValue(paramName);
            ActualParameter actualParameter = (ActualParameter) input
                    .getValue(paramName);
            //TODO: Review this with type
            GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
            GridFtp ftp = new GridFtp();
            gssContext = (GSISecurityContext) invocationContext.getSecurityContext(MYPROXY_SECURITY_CONTEXT);
            GSSCredential gssCred = null;
            try {
                gssCred = gssContext.getGssCredentails();
            } catch (SecurityException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                if ("URI".equals(actualParameter.getType().getType().toString())) {
                    for (String endpoint : host.getGridFTPEndPointArray()) {
                        ((URIParameterType) actualParameter.getType()).setValue(doStaging(outputFileStagingPath,
                                paramValue, actualParameter, ftp, gssCred, endpoint));
                    }
                } else if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                    List<String> split = Arrays.asList(paramValue.split(","));
                    List<String> newFiles = new ArrayList<String>();
                    for (String endpoint : host.getGridFTPEndPointArray()) {
                        for (String paramValueEach : split) {
                            newFiles.add(doStaging(outputFileStagingPath, paramValueEach, actualParameter, ftp, gssCred, endpoint));
                        }
                        ((URIArrayType) actualParameter.getType()).setValueArray(newFiles.toArray(new String[newFiles.size()]));
                    }

                }
            } catch (URISyntaxException e) {
                throw new ProviderException(e.getMessage(), e);
            } catch (ToolsException e) {
                throw new ProviderException(e.getMessage(), e);
            }
            outputNew.add(paramName, actualParameter);
        }
        invocationContext.setOutput(outputNew);
    }

    private String doStaging(String outputFileStagingPath, String paramValue, ActualParameter actualParameter, GridFtp ftp, GSSCredential gssCred, String endpoint) throws URISyntaxException, ToolsException {
        URI srcURI = GfacUtils.createGsiftpURI(endpoint, paramValue);
        String fileName = new File(srcURI.getPath()).getName();
        File outputFile = new File(outputFileStagingPath + File.separator + fileName);
        ftp.readRemoteFile(srcURI,
                gssCred, outputFile);
        return outputFileStagingPath + File.separator + fileName;
    }

    private String readLastLinesofStdOut(String path, int count) {
        StringBuffer buffer = new StringBuffer();
        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        List<String> strLine = new ArrayList<String>();
        String tmp = null;
        int numberofLines = 0;
        try {
            while ((tmp = br.readLine()) != null) {
                strLine.add(tmp);
                numberofLines++;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (int i = numberofLines - count; i < numberofLines; i++) {
            buffer.append(strLine.get(i));
            buffer.append("\n");
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return buffer.toString();
    }
}
