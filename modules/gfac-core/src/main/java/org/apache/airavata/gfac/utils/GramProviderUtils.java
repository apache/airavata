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
package org.apache.airavata.gfac.utils;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.xmlbeans.XmlException;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GramProviderUtils {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    public static void makeDirectory(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();

        try {
            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
            GSSCredential gssCred = gssContext.getGssCredentails();
            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[]{host.getHostAddress()};
            }
            boolean success = false;
            GFacProviderException pe = null;// = new ProviderException("");
            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {

                    URI tmpdirURI = GFacUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GFacUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GFacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GFacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

                    log.info("Host FTP = " + hostgridFTP[0]);
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
                    pe = new GFacProviderException("URI is malformatted:" + e.getMessage(), e, jobExecutionContext);

                } catch (ToolsException e) {
                    pe = new GFacProviderException(e.getMessage(), e, jobExecutionContext);
                }
            }
            if (success == false) {
                throw pe;
            }
        } catch (SecurityException e) {
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        }
    }

    public static GramJob setupEnvironment(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        log.debug("Searching for Gate Keeper");
        try {
            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(jobExecutionContext);
            String rsl = jobAttr.toRSL();

            log.debug("RSL = " + rsl);
            GramJob job = new GramJob(rsl);
            return job;
        } catch (ToolsException te) {
            throw new GFacProviderException(te.getMessage(), te, jobExecutionContext);
        }
    }

    public static Map<String, ?> processOutput(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();
        File localStdErrFile = null;
        try {
            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
            GSSCredential gssCred = gssContext.getGssCredentails();

            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[] { host.getHostAddress() };
            }
            GFacProviderException pe = null;
            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {
                    /*
                     *  Read Stdout and Stderror
                     */
                    URI stdoutURI = GFacUtils.createGsiftpURI(endpoint, app.getStandardOutput());
                    URI stderrURI = GFacUtils.createGsiftpURI(endpoint, app.getStandardError());

                    log.info("STDOUT:" + stdoutURI.toString());
                    log.info("STDERR:" + stderrURI.toString());

                    File logDir = new File("./service_logs");
                    if (!logDir.exists()) {
                        logDir.mkdir();
                    }

                    String timeStampedServiceName = GFacUtils.createUniqueNameForService(jobExecutionContext
                            .getServiceName());
                    File localStdOutFile = File.createTempFile(timeStampedServiceName, "stdout");
                    localStdErrFile = File.createTempFile(timeStampedServiceName, "stderr");

                    String stdout = ftp.readRemoteFile(stdoutURI, gssCred, localStdOutFile);
                    String stderr = ftp.readRemoteFile(stderrURI, gssCred, localStdErrFile);
                    OutputUtils.fillOutputFromStdout(jobExecutionContext, stdout, stderr);
//                    Map<String,ActualParameter> stringMap = null;
//                    MessageContext<Object> output = jobExecutionContext.getOutput();
//                    for (Iterator<String> iterator = output.getNames(); iterator.hasNext(); ) {
//                        String paramName = iterator.next();
//                        ActualParameter actualParameter = (ActualParameter) output.getValue(paramName);
//						if ("URIArray".equals(actualParameter.getType().getType().toString())) {
//							URI outputURI = GfacUtils.createGsiftpURI(endpoint,app.getOutputDataDirectory());
//							List<String> outputList = ftp.listDir(outputURI,gssCred);
//							String[] valueList = outputList.toArray(new String[outputList.size()]);
//							((URIArrayType) actualParameter.getType()).setValueArray(valueList);
//							stringMap = new HashMap<String, ActualParameter>();
//							stringMap.put(paramName, actualParameter);
//							jobExecutionContext.getExecutionContext().getNotifier().output(jobExecutionContext, actualParameter.toString());
//						}
//                        if ("StringArray".equals(actualParameter.getType().getType().toString())) {
//                            String[] valueList = OutputUtils.parseStdoutArray(stdout, paramName);
//                            ((StringArrayType) actualParameter.getType()).setValueArray(valueList);
//                            stringMap = new HashMap<String, ActualParameter>();
//                            stringMap.put(paramName, actualParameter);
//                            jobExecutionContext.getExecutionContext().getNotifier().output(jobExecutionContext, actualParameter.toString());
//                        }
//                    	else{
//                    	// This is to handle exception during the output parsing.
//                        stringMap = OutputUtils.fillOutputFromStdout(jobExecutionContext.<ActualParameter>getOutput(), stdout,stderr);
//                        String paramValue = output.getStringValue(paramName);
//                        if(paramValue == null || paramValue.isEmpty()){
//                            int errCode = listener.getError();
//                            String errorMsg = "Job " + job.getID() + " on host " + host.getHostAddress();
//                            JobSubmissionFault error = new JobSubmissionFault(this, new Exception(errorMsg), "GFAC HOST",
//                                    gateKeeper, job.getRSL(), jobExecutionContext);
//                            errorReason(errCode, error);
//                            jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,error,
//                                    readLastLinesofStdOut(localStdErrFile.getPath(), 20));
//                            throw error;
//                        }
//                        }
//                    }
//                    if(stringMap == null || stringMap.isEmpty()){
//                    	GFacProviderException exception = new GFacProviderException("Gram provider: Error creating job output", jobExecutionContext);
//                    	 jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,exception,exception.getLocalizedMessage());
//                         throw exception;
//                    }
//                    // If users has given an output DAta poth we download the output files in to that directory, this will be apath in the machine where GFac is installed
//                    if(WorkflowContextHeaderBuilder.getCurrentContextHeader() != null &&
//                            WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowOutputDataHandling() != null){
//                        WorkflowOutputDataHandlingDocument.WorkflowOutputDataHandling workflowOutputDataHandling =
//                                WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowOutputDataHandling();
//                        if(workflowOutputDataHandling.getApplicationOutputDataHandlingArray().length != 0){
//                            String outputDataDirectory = workflowOutputDataHandling.getApplicationOutputDataHandlingArray()[0].getOutputDataDirectory();
//                            if(outputDataDirectory != null && !"".equals(outputDataDirectory)){
//                                stageOutputFiles(jobExecutionContext,outputDataDirectory);
//                            }
//                        }
//                    }
//                    return stringMap;
//                }catch (XmlException e) {
//                    throw new GFacProviderException(e.getMessage(),jobExecutionContext, e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
//                }
                }catch (ToolsException e) {
                    throw new GFacProviderException(e.getMessage(),jobExecutionContext, e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                } catch (URISyntaxException e) {
                    throw new GFacProviderException("URI is malformatted:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                }catch (NullPointerException e) {
                    throw new GFacProviderException("Output is not produced in stdout:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                }
            }

            /*
             * If the execution reach here, all GridFTP Endpoint is failed.
             */
            throw pe;

        } catch (Exception e) {
//            jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
            throw new GFacProviderException(e.getMessage(), jobExecutionContext,e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
        }

    }

     private static String readLastLinesofStdOut(String path, int count) {
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
        if (numberofLines > count) {
             for (int i = numberofLines - count; i < numberofLines; i++) {
                buffer.append(strLine.get(i));
                buffer.append("\n");
            }
        }else{
             for (int i = 0; i < numberofLines; i++) {
                buffer.append(strLine.get(i));
                buffer.append("\n");
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return buffer.toString();
    }
}
