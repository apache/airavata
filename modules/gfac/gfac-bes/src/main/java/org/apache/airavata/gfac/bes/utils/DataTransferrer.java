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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fzj.unicore.uas.client.StorageClient;


public class DataTransferrer {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private JobExecutionContext jobContext;
	
	private StorageClient storageClient;
	
	private List<OutputDataObjectType> resultantOutputsLst;
	
	private String downloadLocation, stdoutLocation, stderrLocation;
	
	public DataTransferrer(JobExecutionContext jobContext, StorageClient storageClient) {
		this.jobContext = jobContext;
		this.storageClient = storageClient;
		resultantOutputsLst = new ArrayList<OutputDataObjectType>();
		initStdoutsLocation();
	}
	
	private void initStdoutsLocation() {

		downloadLocation = getDownloadLocation();
		
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
		
		stdoutLocation = downloadLocation+File.separator+stdoutFileName;
		
		stderrLocation = downloadLocation+File.separator+stderrFileName;

		
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
		
		if(log.isDebugEnabled()) {
			log.debug("Download location is:"+downloadLocation);
		}
		
		List<OutputDataObjectType> applicationOutputs = jobContext.getTaskData().getApplicationOutputs();
		 if (applicationOutputs != null && !applicationOutputs.isEmpty()){
            for (OutputDataObjectType output : applicationOutputs){
				if("".equals(output.getValue()) || output.getValue() == null) {
					continue;
				}

	           	if(output.getType().equals(DataType.STRING)) {
					String value = output.getValue();
					String outputPath = downloadLocation + File.separator + value;
					FileDownloader fileDownloader = new FileDownloader(value,outputPath, Mode.overwrite);
					try {
						fileDownloader.perform(storageClient);
					} catch (Exception e) {
						log.error("Error downloading remote files..");
						throw new GFacProviderException(e.getLocalizedMessage(),e);
					}
					resultantOutputsLst.add(output);
					jobContext.addOutputFile(outputPath);
	           	}
	           	
	           	if(output.getType().equals(DataType.STDOUT)) {
	           		resultantOutputsLst.add(output);
	           	}
	           	
	           	if(output.getType().equals(DataType.STDERR)) {
	           		resultantOutputsLst.add(output);
	           	}
            }
		 }
		downloadStdOuts();
	}
	
	public void downloadStdOuts()  throws GFacProviderException{
		
		String stdoutFileName = new File(stdoutLocation).getName();
		
		String stderrFileName = new File(stderrLocation).getName();
		
		FileDownloader f1 = new FileDownloader(stdoutFileName,stdoutLocation, Mode.overwrite);
		try {
			f1.perform(storageClient);
			log.info("Downloading stdout and stderr..");
			String stdoutput = readFile(stdoutLocation);
			jobContext.addOutputFile(stdoutLocation);
			jobContext.setStandardOutput(stdoutLocation);
			log.info(stdoutFileName + " -> "+stdoutLocation);
			if(UASDataStagingProcessor.isUnicoreEndpoint(jobContext)) {
				String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
				String scriptCodeLocation = downloadLocation+File.separator+scriptExitCodeFName;
				f1.setFrom(scriptExitCodeFName);
				f1.setTo(scriptCodeLocation);
				f1.perform(storageClient);
				jobContext.addOutputFile(scriptCodeLocation);
				log.info("UNICORE_SCRIPT_EXIT_CODE -> "+scriptCodeLocation);
			}
			
			f1.setFrom(stderrFileName);
			f1.setTo(stderrLocation);
			f1.perform(storageClient);
			String stderror = readFile(stderrLocation);
			jobContext.addOutputFile(stderrLocation);
			jobContext.setStandardError(stderrLocation);
			log.info(stderrFileName + " -> " + stderrLocation);
		} catch (Exception e) {
			throw new GFacProviderException(e.getLocalizedMessage(),e);
		}
		
		publishFinalOutputs();
	}
	
	protected void publishFinalOutputs() throws GFacProviderException {
        try {
        	if(!resultantOutputsLst.isEmpty()) { 
	        	Registry registry = jobContext.getRegistry();
				registry.add(ChildDataType.EXPERIMENT_OUTPUT, resultantOutputsLst, jobContext.getExperimentID());
        	}
		} catch (RegistryException e) {
			throw new GFacProviderException("Cannot publish outputs to the registry.");
		}

		
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
		String outputDataDir = "";

		if (taskData != null
				&& taskData.getAdvancedOutputDataHandling() != null ) {

			outputDataDir = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
			
			
			if (outputDataDir == null || "".equals(outputDataDir)) {
				outputDataDir = getTempPath(jobContext.getExperimentID());
			}

			else {
				
				// in case of remote locations use the tmp location
				if (outputDataDir.startsWith("scp:") || 
						outputDataDir.startsWith("ftp:") ||
						outputDataDir.startsWith("gsiftp:")) {
						outputDataDir = getTempPath(jobContext.getExperimentID());
				} else if ( outputDataDir.startsWith("file:")  && 
						     outputDataDir.contains("@")){
							outputDataDir = getTempPath(jobContext.getExperimentID());
					
				} else {
					try {
						URI u = new URI(outputDataDir);
						outputDataDir = u.getPath();
					} catch (URISyntaxException e) {
						outputDataDir = getTempPath(jobContext.getExperimentID());
					}

					
				}
			}
		}
		
		File file = new File(outputDataDir);
		if(!file.exists()){
			file.mkdirs();	
		}

		
		return outputDataDir;
	}

	private String getTempPath(String experimentID) {
		String tmpOutputDir = File.separator + "tmp" + File.separator
				+ jobContext.getExperimentID();
		(new File(tmpOutputDir)).mkdirs();
		return tmpOutputDir;
	}	
	
}