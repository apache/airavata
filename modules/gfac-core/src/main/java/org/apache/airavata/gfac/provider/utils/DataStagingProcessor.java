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

package org.apache.airavata.gfac.provider.utils;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public class DataStagingProcessor {
	
	public static void generateDataStagingElements(JobDefinitionType value, JobExecutionContext context) throws Exception{
		
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();

		
		String gridftpEndpoint = ((UnicoreHostType) context.getApplicationContext().getHostDescription().getType())
				.getGridFTPEndPointArray()[0];


		if (context.getInMessageContext().getParameters().size() > 0) {
			buildDataStagingFromInputContext(context, value, gridftpEndpoint, appDepType);
		}

		if (context.getOutMessageContext().getParameters().size() > 0) {
			buildFromOutputContext(context, value, gridftpEndpoint, appDepType);
		}

		createStdOutURIs(value, appDepType, gridftpEndpoint, isUnicoreEndpoint(context));

	}
	
	private static void createInURIElement(JobDefinitionType value,
			String endpoint, String inputDir, ActualParameter inParam)
			throws Exception {

		String uri = ((URIParameterType) inParam.getType()).getValue();
		String fileName = new File(uri).getName();
		if (uri.startsWith("file")) {
			URI gridFTPInputDirURI = GFacUtils.createGsiftpURI(endpoint,
					inputDir);
			String filePath = gridFTPInputDirURI.toString() + File.separator
					+ fileName;
			JSDLUtils
					.addDataStagingSourceElement(value, filePath, null, fileName);
		} else if (uri.startsWith("gsiftp") || uri.startsWith("http")
				|| uri.startsWith("rns")) {
			// no need to stage-in those files to the input
			// directory
			JSDLUtils.addDataStagingSourceElement(value, uri, null, fileName);
		}

	}

	private static void createStdOutURIs(JobDefinitionType value,
			HpcApplicationDeploymentType appDepType, String endpoint,
			boolean isUnicore) throws Exception {

		URI remoteOutputDir = GFacUtils.createGsiftpURI(endpoint,
				appDepType.getOutputDataDirectory());
		
		String stdout = ApplicationProcessor.getApplicationStdOut(value, appDepType);
		
		String stderr = ApplicationProcessor.getApplicationStdErr(value, appDepType);
		
		String stdoutFileName = (stdout == null || stdout.equals("")) ? "stdout"
				: stdout;
		String stdoutURI = GFacUtils.createGsiftpURIAsString(
				remoteOutputDir.toString(), stdoutFileName);
		JSDLUtils.addDataStagingTargetElement(value, null, stdoutFileName,
				stdoutURI);

		String stderrFileName = (stdout == null || stderr.equals("")) ? "stderr"
				: stderr;
		String stderrURI = GFacUtils.createGsiftpURIAsString(
				remoteOutputDir.toString(), stderrFileName);
		JSDLUtils.addDataStagingTargetElement(value, null, stderrFileName,
				stderrURI);
		
		if(isUnicore) {
			String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
			String scriptExitCode = GFacUtils.createGsiftpURIAsString(
					remoteOutputDir.toString(), scriptExitCodeFName);
			JSDLUtils.addDataStagingTargetElement(value, null,
					scriptExitCodeFName, scriptExitCode.toString());
		}

	}

	
	private static void createOutStringElements(JobDefinitionType value,
			HpcApplicationDeploymentType appDeptype, String endpoint, String prmValue) throws Exception {
		
		String outputUri = GFacUtils.createGsiftpURIAsString(endpoint, appDeptype.getOutputDataDirectory());
		
		URI finalOutputUri = GFacUtils.createGsiftpURI(outputUri, prmValue);
		JSDLUtils.addDataStagingTargetElement(value, null, prmValue,	finalOutputUri.toString());
	}

	
	private static void createOutURIElement(JobDefinitionType value,
			String prmValue) throws Exception {
		String fileName = new File(prmValue.toString()).getName();
		JSDLUtils.addDataStagingTargetElement(value, null, fileName, prmValue);
	}

	
	private static JobDefinitionType buildFromOutputContext(JobExecutionContext context,
			JobDefinitionType value, String gridftpEndpoint,
			HpcApplicationDeploymentType appDepType) throws Exception {
		
		Map<String, Object> outputParams = context.getOutMessageContext()
				.getParameters();

		for (String paramKey : outputParams.keySet()) {

			ActualParameter outParam = (ActualParameter) outputParams
					.get(paramKey);

			// if single urls then convert each url into jsdl source
			// elements,
			// that are formed by concat of gridftpurl+inputdir+filename

			String paramDataType = outParam.getType().getType().toString();

			if ("URI".equals(paramDataType)) {
				String uriPrm = ((URIParameterType) outParam.getType())
						.getValue();
				createOutURIElement(value, uriPrm);
			}

			// string params are converted into the job arguments

			else if (("URIArray").equals(paramDataType)) {
				String[] uriArray = ((URIArrayType) outParam.getType())
						.getValueArray();
				for (String u : uriArray) {
					createOutURIElement(value, u);
				}

			}
			else if ("String".equals(paramDataType)) {
				String stringPrm = ((StringParameterType) outParam
						.getType()).getValue();
				createOutStringElements(value, appDepType, gridftpEndpoint, stringPrm);
			}

			else if ("StringArray".equals(paramDataType)) {
				String[] valueArray = ((StringArrayType) outParam.getType())
						.getValueArray();
				for (String v : valueArray) {
					createOutStringElements(value, appDepType, gridftpEndpoint, v);
				}
			}
		}
		
		return value;
	}

	
	private static void buildDataStagingFromInputContext(JobExecutionContext context, JobDefinitionType value, String gridftpEndpoint, HpcApplicationDeploymentType appDepType) 
			throws Exception {
		
		// TODO set data directory
		Map<String, Object> inputParams = context.getInMessageContext()
				.getParameters();

		for (String paramKey : inputParams.keySet()) {

			ActualParameter inParam = (ActualParameter) inputParams
					.get(paramKey);

			// if single urls then convert each url into jsdl source
			// elements,
			// that are formed by concat of gridftpurl+inputdir+filename

			String paramDataType = inParam.getType().getType().toString();

			if ("URI".equals(paramDataType)) {
				createInURIElement(value, gridftpEndpoint,
						appDepType.getInputDataDirectory(), inParam);
			}

			// string params are converted into the job arguments

			else if ("String".equals(paramDataType)) {
				String stringPrm = ((StringParameterType) inParam.getType())
						.getValue();
				ApplicationProcessor.addApplicationArgument(value, appDepType, stringPrm);
			}
		}
		
	}
	
	
	public static boolean isUnicoreEndpoint(JobExecutionContext context) {
		return ( (context.getApplicationContext().getHostDescription().getType() instanceof UnicoreHostType)?true:false );
	}

	
}
