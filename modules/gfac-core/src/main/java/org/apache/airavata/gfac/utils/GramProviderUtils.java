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
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.schemas.gfac.*;
import org.apache.xmlbeans.XmlException;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
        Map<String, ActualParameter> stringMap = null;
        try {
            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
            GSSCredential gssCred = gssContext.getGssCredentails();

            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[]{host.getHostAddress()};
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
                    Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
                    Set<String> keys = output.keySet();
                    for (String paramName : keys) {
                        ActualParameter actualParameter = (ActualParameter) output.get(paramName);
                        if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                            URI outputURI = GFacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());
                            List<String> outputList = ftp.listDir(outputURI, gssCred);
                            String[] valueList = outputList.toArray(new String[outputList.size()]);
                            ((URIArrayType) actualParameter.getType()).setValueArray(valueList);
                            stringMap = new HashMap<String, ActualParameter>();
                            stringMap.put(paramName, actualParameter);
                        }
                        if ("StringArray".equals(actualParameter.getType().getType().toString())) {
                            String[] valueList = OutputUtils.parseStdoutArray(stdout, paramName);
                            ((StringArrayType) actualParameter.getType()).setValueArray(valueList);
                            stringMap = new HashMap<String, ActualParameter>();
                            stringMap.put(paramName, actualParameter);
                        } else {
                            // This is to handle exception during the output parsing.
                            stringMap = OutputUtils.fillOutputFromStdout(jobExecutionContext, stdout, stderr);
                        }
                    }
                    if (stringMap == null || stringMap.isEmpty()) {
                        jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(new Throwable("Empty Output returned from the Application, Double check the application" +
                                "and ApplicationDescriptor output Parameter Names")));
//                    	GFacProviderException exception = new GFacProviderException("Gram provider: Error creating job output", jobExecutionContext);
//                    	 jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,exception,exception.getLocalizedMessage());
//                         throw exception;
                    }
                    //todo check the workflow context header and run the stateOutputFiles method to stage the output files in to a user defined location
                } catch (ToolsException e) {
                    throw new GFacProviderException(e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                } catch (URISyntaxException e) {
                    throw new GFacProviderException("URI is malformatted:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                } catch (NullPointerException e) {
                    throw new GFacProviderException("Output is not produced in stdout:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                }
            }
            //todo this return has to be removed
            return stringMap;
        } catch (Exception e) {
//            jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,e,readLastLinesofStdOut(localStdErrFile.getPath(), 20));
            throw new GFacProviderException(e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
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
        } else {
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

    private static void stageOutputFiles(JobExecutionContext jobExecutionContext, String outputFileStagingPath) throws GFacProviderException {
        MessageContext outputNew = new MessageContext();
        MessageContext output = jobExecutionContext.getOutMessageContext();
        Map<String, Object> parameters = output.getParameters();
        for (String paramName : parameters.keySet()) {
            ActualParameter actualParameter = (ActualParameter) parameters
                    .get(paramName);
            //TODO: Review this with type
            GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
            GridFtp ftp = new GridFtp();

            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
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
                                MappingFactory.toString(actualParameter), ftp, gssCred, endpoint));
                    }
                } else if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                    List<String> split = Arrays.asList(MappingFactory.toString(actualParameter).split(","));
                    List<String> newFiles = new ArrayList<String>();
                    for (String endpoint : host.getGridFTPEndPointArray()) {
                        for (String paramValueEach : split) {
                            newFiles.add(doStaging(outputFileStagingPath, paramValueEach, ftp, gssCred, endpoint));
                        }
                        ((URIArrayType) actualParameter.getType()).setValueArray(newFiles.toArray(new String[newFiles.size()]));
                    }

                }
            } catch (URISyntaxException e) {
                throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
            } catch (ToolsException e) {
                throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
            }
            outputNew.getParameters().put(paramName, actualParameter);
        }
        jobExecutionContext.setOutMessageContext(outputNew);
    }

    private static String doStaging(String outputFileStagingPath, String paramValue, GridFtp ftp, GSSCredential gssCred, String endpoint) throws URISyntaxException, ToolsException {
        URI srcURI = GFacUtils.createGsiftpURI(endpoint, paramValue);
        String fileName = new File(srcURI.getPath()).getName();
        File outputFile = new File(outputFileStagingPath + File.separator + fileName);
        ftp.readRemoteFile(srcURI,
                gssCred, outputFile);
        return outputFileStagingPath + File.separator + fileName;
    }

    private static String stageInputFiles(JobExecutionContext jobExecutionContext, String paramValue) throws URISyntaxException, SecurityException, ToolsException, IOException {
        URI gridftpURL;
        gridftpURL = new URI(paramValue);
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();
        URI destURI = null;
        GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
        GSSCredential gssCred = gssContext.getGssCredentails();

        for (String endpoint : host.getGridFTPEndPointArray()) {
            URI inputURI = GFacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
            String fileName = new File(gridftpURL.getPath()).getName();
            String s = inputURI.getPath() + File.separator + fileName;
            //if user give a url just to refer an endpoint, not a web resource we are not doing any transfer
            if (fileName != null && !"".equals(fileName)) {
                destURI = GFacUtils.createGsiftpURI(endpoint, s);
                if (paramValue.startsWith("gsiftp")) {
                    ftp.uploadFile(gridftpURL, destURI, gssCred);
                } else if (paramValue.startsWith("file")) {
                    String localFile = paramValue.substring(paramValue.indexOf(":") + 1, paramValue.length());
                    ftp.uploadFile(destURI, gssCred, new FileInputStream(localFile));
                } else if (paramValue.startsWith("http")) {
                    ftp.uploadFile(destURI,
                            gssCred, (gridftpURL.toURL().openStream()));
                } else {
                    //todo throw exception telling unsupported protocol
                    return paramValue;
                }
            } else {
                // When the given input is not a web resource but a URI type input, then we don't do any transfer just keep the same value as it isin the input
                return paramValue;
            }
        }
        System.out.println(destURI.getPath());
        return destURI.getPath();
    }

    public static Map<String, ?> processInput(JobExecutionContext jobExecutionContext)
            throws GFacProviderException {
        MessageContext inputNew = new MessageContext();
        try {
            MessageContext input = jobExecutionContext.getInMessageContext();
            Set<String> parameters = input.getParameters().keySet();
            for (String paramName:parameters) {
                ActualParameter actualParameter = (ActualParameter) input.getParameters().get(paramName);
                String paramValue = MappingFactory.toString(actualParameter);
                //TODO: Review this with type
                if ("URI".equals(actualParameter.getType().getType().toString())) {
                    ((URIParameterType) actualParameter.getType()).setValue(stageInputFiles(jobExecutionContext, paramValue));
                } else if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                    List<String> split = Arrays.asList(paramValue.split(","));
                    List<String> newFiles = new ArrayList<String>();
                    for (String paramValueEach : split) {
                        newFiles.add(stageInputFiles(jobExecutionContext, paramValueEach));
                    }
                    ((URIArrayType) actualParameter.getType()).setValueArray(newFiles.toArray(new String[newFiles.size()]));
                }
                inputNew.getParameters().put(paramName, actualParameter);
            }
        } catch (Exception e) {
//           jobExecutionContext.getExecutionContext().getNotifier().executionFail(jobExecutionContext,e,"Error during Input File staging");
            throw new GFacProviderException("Error while input File Staging", jobExecutionContext, e, e.getLocalizedMessage());
        }
        jobExecutionContext.setInMessageContext(inputNew);
        return null;
    }
}
