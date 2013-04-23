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
import java.io.IOException;
import java.util.*;

import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCPInputHandler implements GFacHandler{

	private static final Logger log = LoggerFactory.getLogger(SCPInputHandler.class);


	public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException,GFacException {

		  log.info("Invoking SCPInputHandler");


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
	 private static String stageInputFiles(JobExecutionContext context,String paramValue) throws IOException,GFacException{
		 ApplicationDeploymentDescriptionType app = context.getApplicationContext().getApplicationDeploymentDescription().getType();

		 SSHSecurityContext securityContext = (SSHSecurityContext)context.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT);
		 SCPFileTransfer fileTransfer = securityContext.getSSHClient().newSCPFileTransfer();
		 String remoteFile = app.getInputDataDirectory() + File.separatorChar + paramValue;

		 File inputFile = new File(paramValue);
		 fileTransfer.upload(inputFile.getAbsolutePath(), remoteFile);
		 return remoteFile;
	 }

    public void init(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
