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

package org.apache.airavata.gfac.bes.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fzj.unicore.uas.client.StorageClient;


public class DataTransferrer {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private JobExecutionContext jobContext;
	
	private StorageClient storageClient;
	
	public DataTransferrer(JobExecutionContext jobContext, StorageClient storageClient) {
		this.jobContext = jobContext;
		this.storageClient = storageClient;
	}
	
	
	public void uploadLocalFiles() throws GFacProviderException {
		Map<String, Object> inputParams = jobContext.getInMessageContext()
				.getParameters();
		for (String paramKey : inputParams.keySet()) {
			ActualParameter inParam = (ActualParameter) inputParams
					.get(paramKey);
			String paramDataType = inParam.getType().getType().toString();
			if("URI".equals(paramDataType)) {
				String uri = ((URIParameterType) inParam.getType()).getValue();
				String fileName = new File(uri).getName();
				if (uri.startsWith("file")) {
					try {
						String uriWithoutProtocol = uri.substring(uri.lastIndexOf("://") + 1, uri.length());
						FileUploader fileUploader = new FileUploader(uriWithoutProtocol,"input/" + fileName,Mode.overwrite);
						fileUploader.perform(storageClient);
					} catch (FileNotFoundException e3) {
						throw new GFacProviderException(
								"Error while staging-in, local file "+fileName+" not found", e3);
					} catch (Exception e) {
						throw new GFacProviderException("Cannot upload files", e);

					}

				}
			}
		}
		
	}
	
	/**
	 * This method will download all the remote files specified in the output 
	 * context of a job.  
	 * */
	public void downloadRemoteFiles() throws GFacProviderException {
		
		String downloadLocation = getDownloadLocation();
		
		File file = new File(downloadLocation);
		if(!file.exists()){
			file.mkdirs();	
		}
		
		Map<String, ActualParameter> stringMap = new HashMap<String, ActualParameter>();
		     
		Map<String, Object> outputParams = jobContext.getOutMessageContext()
				.getParameters();

		for (String paramKey : outputParams.keySet()) {

			ActualParameter outParam = (ActualParameter) outputParams
					.get(paramKey);

			String paramDataType = outParam.getType().getType().toString();

			if ("String".equals(paramDataType)) {
				String stringPrm = ((StringParameterType) outParam
						.getType()).getValue();
				String localFileName = null;
				//TODO: why analysis.tar? it wont scale to other gateways..
				if(stringPrm == null || stringPrm.isEmpty()){
					continue; 
//					localFileName = "analysis-results.tar";
				}else{
					localFileName = stringPrm.substring(stringPrm.lastIndexOf("/")+1);
				}
				String outputLocation = downloadLocation+File.separator+localFileName;
				FileDownloader fileDownloader = new FileDownloader(stringPrm,outputLocation, Mode.overwrite);
				try {
					fileDownloader.perform(storageClient);
					 ((StringParameterType) outParam.getType()).setValue(outputLocation);
						stringMap.put(paramKey, outParam);
				} catch (Exception e) {
					throw new GFacProviderException(e.getLocalizedMessage(),e);
				}
			}

			else if ("StringArray".equals(paramDataType)) {
				String[] valueArray = ((StringArrayType) outParam.getType())
						.getValueArray();
				for (String v : valueArray) {
					String localFileName = v.substring(v.lastIndexOf("/")+1);;
					String outputLocation = downloadLocation+File.separator+localFileName;
					FileDownloader fileDownloader = new FileDownloader(v,outputLocation, Mode.overwrite);
					try {
						fileDownloader.perform(storageClient);
						 ((StringParameterType) outParam.getType()).setValue(outputLocation);
						stringMap.put(paramKey, outParam);
					} catch (Exception e) {
						throw new GFacProviderException(e.getLocalizedMessage(),e);
					}
				}
			}
		}
		 if (stringMap == null || stringMap.isEmpty()) {
             throw new GFacProviderException("Empty Output returned from the Application, Double check the application" +
                     "and ApplicationDescriptor output Parameter Names");
         }
		
		downloadStdOuts();
	}
	
	
	public void downloadStdOuts()  throws GFacProviderException{
		String downloadLocation = getDownloadLocation();
		File file = new File(downloadLocation);
		if(!file.exists()){
			file.mkdirs();	
		}
		
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) jobContext
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();
		
		String stdout = appDepType.getStandardOutput();
		String stderr = appDepType.getStandardError();
		if(stdout != null) {
			stdout = stdout.substring(stdout.lastIndexOf('/')+1);
		}
		
		if(stderr != null) {
			stderr = stderr.substring(stderr.lastIndexOf('/')+1);
		}
		
		String stdoutFileName = (stdout == null || stdout.equals("")) ? "stdout"
				: stdout;
		String stderrFileName = (stdout == null || stderr.equals("")) ? "stderr"
				: stderr;
		
		ApplicationDescription application = jobContext.getApplicationContext().getApplicationDeploymentDescription();
		ApplicationDeploymentDescriptionType appDesc = application.getType();
	
		String stdoutLocation = downloadLocation+File.separator+stdoutFileName;
		FileDownloader f1 = new FileDownloader(stdoutFileName,stdoutLocation, Mode.overwrite);
		try {
			f1.perform(storageClient);
			String stdoutput = readFile(stdoutLocation);
			appDesc.setStandardOutput(stdoutput);
		} catch (Exception e) {
			throw new GFacProviderException(e.getLocalizedMessage(),e);
		}
		String stderrLocation = downloadLocation+File.separator+stderrFileName;
//		FileDownloader f2 = new FileDownloader(stderrFileName,stderrLocation, Mode.overwrite);
		try {
			f1.setFrom(stderrFileName);
			f1.setTo(stderrLocation);
			f1.perform(storageClient);
			String stderror = readFile(stderrLocation);
			appDesc.setStandardError(stderror);
		} catch (Exception e) {
			throw new GFacProviderException(e.getLocalizedMessage(),e);
		}
		
		if(UASDataStagingProcessor.isUnicoreEndpoint(jobContext)) {
			String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
			f1.setFrom(scriptExitCodeFName);
			f1.setTo(downloadLocation+File.separator+scriptExitCodeFName);
			
		}
	}
	
	public List<String> extractOutStringParams(JobExecutionContext context) {
		
		Map<String, Object> outputParams = context.getOutMessageContext()
				.getParameters();
		
		List<String> outPrmsList = new ArrayList<String>();
		
		for (String paramKey : outputParams.keySet()) {

			ActualParameter outParam = (ActualParameter) outputParams
					.get(paramKey);

			String paramDataType = outParam.getType().getType().toString();

			if ("String".equals(paramDataType)) {
				String strPrm = ((StringParameterType) outParam.getType())
						.getValue();
				outPrmsList.add(strPrm);
			}

			else if (("StringArray").equals(paramDataType)) {
				String[] uriArray = ((URIArrayType) outParam.getType())
						.getValueArray();
				for (String u : uriArray) {
					outPrmsList.add(u);					
				}

			}
		}
		
		return outPrmsList;
	}

	
	private String readFile(String localFile) throws IOException {
		BufferedReader instream = new BufferedReader(new FileReader(localFile));
		StringBuffer buff = new StringBuffer();
		String temp = null;
		while ((temp = instream.readLine()) != null) {
			buff.append(temp);
			buff.append(Constants.NEWLINE);
		}

		log.info("finish read file:" + localFile);

		return buff.toString();
	}
	
	public void setStorageClient(StorageClient sc){
		storageClient = sc;
	}
	
	private String getDownloadLocation() {
		TaskDetails taskData = jobContext.getTaskData();
		if (taskData != null && taskData.getAdvancedOutputDataHandling() != null) {
			String outputDataDirectory = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
			return outputDataDirectory;
		}
		return null;
	}
}