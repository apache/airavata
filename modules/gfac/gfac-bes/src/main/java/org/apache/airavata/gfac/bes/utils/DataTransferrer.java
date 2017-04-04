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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.experiment.TaskDetails;
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
		List<String> inFilePrms = extractInFileParams();
		for (String uri : inFilePrms) {
			String fileName = new File(uri).getName();
				if (uri.startsWith("file")) {
					try {
						String uriWithoutProtocol = uri.substring(uri.lastIndexOf("://") + 1, uri.length());
						FileUploader fileUploader = new FileUploader(uriWithoutProtocol,fileName,Mode.overwrite);
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

		Map<String, Object> output = jobContext.getOutMessageContext().getParameters();
        Set<String> keys = output.keySet();
        
		for (String outPrm : keys) {
			OutputDataObjectType actualParameter = (OutputDataObjectType) output.get(outPrm);
				if (DataType.STDERR == actualParameter.getType()) continue;
				if (DataType.STDOUT == actualParameter.getType()) continue;
				
				String value = actualParameter.getValue();
				FileDownloader fileDownloader = new FileDownloader(value,downloadLocation, Mode.overwrite);
				try {
					fileDownloader.perform(storageClient);
					String outputPath = downloadLocation + File.separator + value.substring(value.lastIndexOf('/')+1);
					actualParameter.setValue(outputPath);
					actualParameter.setType(DataType.URI);
					jobContext.addOutputFile(outputPath);
				} catch (Exception e) {
					throw new GFacProviderException(e.getLocalizedMessage(),e);
				}
		}
		downloadStdOuts();
	}
	
	
	public void downloadStdOuts()  throws GFacProviderException{
		String downloadLocation = getDownloadLocation();
		File file = new File(downloadLocation);
		if(!file.exists()){
			file.mkdirs();	
		}
		
		String stdout = jobContext.getStandardOutput();
		String stderr = jobContext.getStandardError();
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
		
		ApplicationDeploymentDescription application = jobContext.getApplicationContext().getApplicationDeploymentDescription();
		
		String stdoutLocation = downloadLocation+File.separator+stdoutFileName;
		FileDownloader f1 = new FileDownloader(stdoutFileName,stdoutLocation, Mode.overwrite);
		try {
			f1.perform(storageClient);
			log.info("Downloading stdout and stderr..");
			String stdoutput = readFile(stdoutLocation);
			jobContext.addOutputFile(stdoutLocation);
			jobContext.setStandardOutput(stdoutLocation);
			log.info("Stdout downloaded to -> "+stdoutLocation);
			if(UASDataStagingProcessor.isUnicoreEndpoint(jobContext)) {
				String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
				String scriptCodeLocation = downloadLocation+File.separator+scriptExitCodeFName;
				f1.setFrom(scriptExitCodeFName);
				f1.setTo(scriptCodeLocation);
				f1.perform(storageClient);
				log.info("UNICORE_SCRIPT_EXIT_CODE downloaded to "+scriptCodeLocation);
			}
			String stderrLocation = downloadLocation+File.separator+stderrFileName;
			f1.setFrom(stderrFileName);
			f1.setTo(stderrLocation);
			f1.perform(storageClient);
			String stderror = readFile(stderrLocation);
			jobContext.addOutputFile(stderrLocation);
			jobContext.setStandardError(stderrLocation);
			log.info("Stderr downloaded to -> "+stderrLocation);
		} catch (Exception e) {
			throw new GFacProviderException(e.getLocalizedMessage(),e);
		}
		
	}
	
	public List<String> extractOutParams(JobExecutionContext context) {
		List<String> outPrmsList = new ArrayList<String>();
		List<OutputDataObjectType> applicationOutputs = jobContext.getTaskData().getApplicationOutputs();
		 if (applicationOutputs != null && !applicationOutputs.isEmpty()){
           for (OutputDataObjectType output : applicationOutputs){
          	 if(output.getType().equals(DataType.STRING)) {
          		outPrmsList.add(output.getValue());
          	 }
          	 else if(output.getType().equals(DataType.FLOAT) || output.getType().equals(DataType.INTEGER)) {
          		outPrmsList.add(String.valueOf(output.getValue()));
          		 
          	 }
           }
		 }
		return outPrmsList;
	}
	
	public List<String> extractInFileParams() {
		List<String> filePrmsList = new ArrayList<String>();
		List<InputDataObjectType> applicationInputs = jobContext.getTaskData().getApplicationInputs();
		 if (applicationInputs != null && !applicationInputs.isEmpty()){
           for (InputDataObjectType output : applicationInputs){
          	 if(output.getType().equals(DataType.URI)) {
          		filePrmsList.add(output.getValue());
          	 }
           }
		 }
		return filePrmsList;
	}

	
	private String readFile(String localFile) throws IOException {
		BufferedReader instream = new BufferedReader(new FileReader(localFile));
		StringBuffer buff = new StringBuffer();
		String temp = null;
		while ((temp = instream.readLine()) != null) {
			buff.append(temp);
			buff.append(GFacConstants.NEWLINE);
		}

		log.info("finish read file:" + localFile);

		return buff.toString();
	}
	
	public void setStorageClient(StorageClient sc){
		storageClient = sc;
	}
	
	private String getDownloadLocation() {
		TaskDetails taskData = jobContext.getTaskData();
		//In case of third party transfer this will not work.
//		if (taskData != null && taskData.getAdvancedOutputDataHandling() != null) {
//			String outputDataDirectory = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
//			return outputDataDirectory;
//		}
		String outputDataDir = File.separator + "tmp";
        outputDataDir = outputDataDir + File.separator + jobContext.getExperimentID();
        (new File(outputDataDir)).mkdirs();
		return outputDataDir;
	}
}