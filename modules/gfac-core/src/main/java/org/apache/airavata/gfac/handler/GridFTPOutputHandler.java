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
package org.apache.airavata.gfac.handler;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.GramJobSubmissionListener;
import org.apache.airavata.gfac.utils.OutputUtils;
import org.apache.airavata.schemas.gfac.*;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GridFTPOutputHandler implements GFacHandler {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        log.info("Invoking GridFTPOutputHandler ...");
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();
        File localStdErrFile = null;
        Map<String, ActualParameter> stringMap = null;
        try {
            if (jobExecutionContext.getSecurityContext() == null ||
                    !(jobExecutionContext.getSecurityContext() instanceof GSISecurityContext))
            {
                GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
                jobExecutionContext.setSecurityContext(gssContext);
            }
            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.getSecurityContext()).getGssCredentails();
            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[]{host.getHostAddress()};
            }
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
                        throw new GFacHandlerException("Empty Output returned from the Application, Double check the application" +
                                "and ApplicationDescriptor output Parameter Names");
                    }
                    //todo check the workflow context header and run the stateOutputFiles method to stage the output files in to a user defined location
//                    stageOutputFiles(jobExecutionContext, app.getOutputDataDirectory());
                } catch (ToolsException e) {
                    log.error(e.getMessage());
                    throw new GFacHandlerException(e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                } catch (URISyntaxException e) {
                    log.error(e.getMessage());
                    throw new GFacHandlerException("URI is malformatted:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                } catch (NullPointerException e) {
                    log.error(e.getMessage());
                    throw new GFacHandlerException("Output is not produced in stdout:" + e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GFacHandlerException(e.getMessage(), jobExecutionContext, e, readLastLinesofStdOut(localStdErrFile.getPath(), 20));
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

            if (jobExecutionContext.getSecurityContext() == null ||
                    !(jobExecutionContext.getSecurityContext() instanceof GSISecurityContext))
            {
                GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
                jobExecutionContext.setSecurityContext(gssContext);
            }
            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.getSecurityContext()).getGssCredentails();
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
                log.error(e.getMessage());
                throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
            } catch (ToolsException e) {
                log.error(e.getMessage());
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
}
