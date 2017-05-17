/**
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
 */
package org.apache.airavata.gfac.bes.utils;

import java.io.File;
import java.util.List;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public class UASDataStagingProcessor {
	
	public static void generateDataStagingElements(JobDefinitionType value, JobExecutionContext context, String smsUrl) throws Exception{
		smsUrl = "BFT:"+smsUrl;
		
		if (context.getTaskData().getApplicationInputs().size() > 0) {
			buildDataStagingFromInputContext(context, value, smsUrl);
		}
		
		if (context.getTaskData().getApplicationOutputs().size() > 0) {
			buildFromOutputContext(context, value, smsUrl);
		}
	}
	
	private static void createInURISMSElement(JobDefinitionType value, String smsUrl, String uri)
			throws Exception {
		String fileName = new File(uri).getName();
		if (uri.startsWith("file")) {
			uri = smsUrl+"#/"+fileName;
			
		} 
		// no need to stage-in those files to the input
		// directory because unicore site will fetch them for the user
		// supported third party transfers include 
		// gsiftp, http, rns, ftp
		JSDLUtils.addDataStagingSourceElement(value, uri, null, fileName);

	}
	
	//TODO: will be deprecated
	private static void createStdOutURIs(JobDefinitionType value, JobExecutionContext context, String smsUrl, boolean isUnicore) throws Exception {

		// no need to use smsUrl for output location, because output location is activity's working directory 
		
		if(isUnicore) {
			String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
			String scriptExitCode = smsUrl+"#/output/"+scriptExitCodeFName;
			JSDLUtils.addDataStagingTargetElement(value, null,
					scriptExitCodeFName, null);
		}
		
		if(!isUnicore) {
		String stdout = ApplicationProcessor.getApplicationStdOut(value, context);
		
		String stderr = ApplicationProcessor.getApplicationStdErr(value, context);
		
		String stdoutFileName = (stdout == null || stdout.equals("")) ? "stdout"
				: stdout;
		String stdoutURI = smsUrl+"#/output/"+stdoutFileName;
		
		JSDLUtils.addDataStagingTargetElement(value, null, stdoutFileName,
				null);

		String stderrFileName = (stdout == null || stderr.equals("")) ? "stderr"
				: stderr;
		String stderrURI = smsUrl+"#/output/"+stderrFileName;
		
		JSDLUtils.addDataStagingTargetElement(value, null, stderrFileName,
				null);
		}

	}

	// TODO: this should be deprecated, because the outputs are fetched using activity working dir from data transferrer
	private static void createOutStringElements(JobDefinitionType value, String smsUrl, String prmValue) throws Exception {
		if(prmValue == null || "".equals(prmValue)) return;
		String finalSMSPath = smsUrl + "#/output/"+prmValue;
		JSDLUtils.addDataStagingTargetElement(value, null, prmValue, null);
	}

	
	private static void createOutURIElement(JobDefinitionType value,
			String prmValue) throws Exception {
		String fileName = new File(prmValue.toString()).getName();
		JSDLUtils.addDataStagingTargetElement(value, null, fileName, prmValue);
	}

	
	private static JobDefinitionType buildFromOutputContext(JobExecutionContext context,
			JobDefinitionType value, String smsUrl) throws Exception {
		List<OutputDataObjectType> applicationOutputs = context.getTaskData().getApplicationOutputs();
		 if (applicationOutputs != null && !applicationOutputs.isEmpty()){
             for (OutputDataObjectType output : applicationOutputs){
            	 if(output.getType().equals(DataType.URI) && !output.getValue().startsWith("file:")) {
            		 createOutURIElement(value, output.getValue());
            	 }
             }
		 }
		return value;
	}

	
	private static void buildDataStagingFromInputContext(JobExecutionContext context, JobDefinitionType value, String smsUrl) 
			throws Exception {
		List<InputDataObjectType> applicationInputs = context.getTaskData().getApplicationInputs();
		
		if (applicationInputs != null && !applicationInputs.isEmpty()){
			for (InputDataObjectType input : applicationInputs){
				if(input.getType().equals(DataType.URI)){
					//TODO: set the in sms url
					createInURISMSElement(value, smsUrl, input.getValue());
				}
				else if(input.getType().equals(DataType.STRING)){
					ApplicationProcessor.addApplicationArgument(value, context, input.getValue());
				}
				else if (input.getType().equals(DataType.FLOAT) || input.getType().equals(DataType.INTEGER)){
					if(! (input.getName().equals(BESConstants.NUMBER_OF_PROCESSES) || input.getName().equals(BESConstants.PROCESSES_PER_HOST))) {
						// temp avoid environ going to app args
						ApplicationProcessor.addApplicationArgument(value, context, String.valueOf(input.getValue()));
					}
				}
			}
		}
	}
	
	public static boolean isUnicoreEndpoint(JobExecutionContext context) {
		return context.getPreferredJobSubmissionProtocol().equals(JobSubmissionProtocol.UNICORE);
	}

}
