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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridFTPInputHandler implements GFacHandler {
    private static final Logger log = LoggerFactory.getLogger(AppDescriptorCheckHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        log.info("Invoking GridFTPInputHandler ...");


        MessageContext inputNew = new MessageContext();
        try {
            MessageContext input = jobExecutionContext.getInMessageContext();
            Set<String> parameters = input.getParameters().keySet();
            for (String paramName : parameters) {
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
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while input File Staging", jobExecutionContext, e, e.getLocalizedMessage());
        }
        jobExecutionContext.setInMessageContext(inputNew);
    }

    private static String stageInputFiles(JobExecutionContext jobExecutionContext, String paramValue) throws URISyntaxException, SecurityException, ToolsException, IOException,GFacException {
        URI gridftpURL = new URI(paramValue);

        String[] gridFTPEndpointArray = null;

        // not to download input files to the input dir if its http / gsiftp
        // but if local then yes
        boolean isInputNonLocal = true;

        //TODO: why it is tightly coupled with gridftp
//        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();

        //TODO: make it more reusable
        HostDescriptionType hostType = jobExecutionContext.getApplicationContext().getHostDescription().getType();

        if(jobExecutionContext.getApplicationContext().getHostDescription().getType() instanceof GlobusHostType){
        	gridFTPEndpointArray = ((GlobusHostType) hostType).getGridFTPEndPointArray();
        }
        else if (jobExecutionContext.getApplicationContext().getHostDescription().getType() instanceof UnicoreHostType){
        	gridFTPEndpointArray = ((UnicoreHostType) hostType).getGridFTPEndPointArray();
        	isInputNonLocal = false;
        }
        else {
        	//TODO
        }


        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();
        URI destURI = null;
        GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentials();

        for (String endpoint : gridFTPEndpointArray) {
            URI inputURI = GFacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
            String fileName = new File(gridftpURL.getPath()).getName();
            String destLocalPath = inputURI.getPath() + File.separator + fileName;
            //if user give a url just to refer an endpoint, not a web resource we are not doing any transfer
            if (fileName != null && !"".equals(fileName)) {
                destURI = GFacUtils.createGsiftpURI(endpoint, destLocalPath);
                if (paramValue.startsWith("gsiftp")) {
                	// no need to do if it is unicore, as unicore will download this on user's behalf to the job space dir
                	if(isInputNonLocal) ftp.uploadFile(gridftpURL, destURI, gssCred);
                	else return paramValue;
                } else if (paramValue.startsWith("file")) {
                    String localFile = paramValue.substring(paramValue.indexOf(":") + 1, paramValue.length());
                    FileInputStream fis = null;
                    try {
                    	fis = new FileInputStream(localFile);
                    	ftp.uploadFile(destURI, gssCred, fis);
                    } catch (IOException e) {
                        throw new GFacException("Unable to create file : " + localFile ,e);
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                } else if (paramValue.startsWith("http")) {
                	// no need to do if it is unicore
                	if(isInputNonLocal) {
                		InputStream is = null;
                		try {
                			is = gridftpURL.toURL().openStream();
                			ftp.uploadFile(destURI, gssCred, (is));
                		}finally {
                			is.close();
                		}
                	} else {
                		// don't return destUri
                		return paramValue;
                	}

                } else {
                    //todo throw exception telling unsupported protocol
                    return paramValue;
                }
            } else {
                // When the given input is not a web resource but a URI type input, then we don't do any transfer just keep the same value as it isin the input
                return paramValue;
            }
        }
        return destURI.getPath();
    }

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
