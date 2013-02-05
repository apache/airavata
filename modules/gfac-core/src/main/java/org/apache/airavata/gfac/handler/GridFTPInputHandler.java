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
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GridFTPInputHandler implements GFacHandler{
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
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
            throw new GFacHandlerException("Error while input File Staging", jobExecutionContext, e, e.getLocalizedMessage());
        }
        jobExecutionContext.setInMessageContext(inputNew);
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
}
