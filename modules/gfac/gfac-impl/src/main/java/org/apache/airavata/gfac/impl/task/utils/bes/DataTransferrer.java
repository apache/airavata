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
package org.apache.airavata.gfac.impl.task.utils.bes;

import de.fzj.unicore.uas.client.StorageClient;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data movement utility class for transferring files before and after the job execution phase.   
 * 
 * */
public class DataTransferrer {
   
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected ProcessContext processContext;
	
	protected StorageClient storageClient;
	
	protected List<OutputDataObjectType> resultantOutputsLst;

	protected String gatewayDownloadLocation, stdoutLocation, stderrLocation;
	
	public DataTransferrer(ProcessContext processContext, StorageClient storageClient) {
		this.processContext = processContext;
		this.storageClient = storageClient;
		resultantOutputsLst = new ArrayList<OutputDataObjectType>();
		initStdoutsLocation();
	}
	
	private void initStdoutsLocation() {

		gatewayDownloadLocation = getDownloadLocation();
		
		String stdout = processContext.getStdoutLocation();
		String stderr = processContext.getStderrLocation();

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
		
		stdoutLocation = gatewayDownloadLocation+File.separator+stdoutFileName;
		
		stderrLocation = gatewayDownloadLocation+File.separator+stderrFileName;

        List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
        if (processOutputs != null && !processOutputs.isEmpty()){
            for (OutputDataObjectType processOutput : processOutputs){
                if (processOutput.getType().equals(DataType.STDOUT)){
                    processOutput.setValue(stdoutLocation);
                }
                if (processOutput.getType().equals(DataType.STDERR)){
                    processOutput.setValue(stderrLocation);
                }

            }
        }
	}

    public void uploadLocalFiles() throws GFacException {
        List<String> inFilePrms = new ArrayList<>();
        // FIXME - remove hard coded file path.
        inFilePrms.addAll(extractInFileParams());
//        inFilePrms.add("file://home/airavata/test/hpcinput-localhost-uslims3_cauma3d-00950.tar");
        for (String uri : inFilePrms) {
            String fileName = new File(uri).getName();
            if (uri.startsWith("file")) {
                try {
                    String uriWithoutProtocol = uri.substring(uri.lastIndexOf("://") + 2, uri.length());
                    FileUploader fileUploader = new FileUploader(uriWithoutProtocol, fileName, Mode.overwrite, false);
                    log.info("Uploading file {}", fileName);
                    fileUploader.perform(storageClient);
                } catch (FileNotFoundException e3) {
                    throw new GFacException(
                            "Error while staging-in, local file "+fileName+" not found", e3);
                } catch (Exception e) {
                    throw new GFacException("Cannot upload files", e);

                }

            }
        }
    }

    public List<String> extractInFileParams() {
        List<String> filePrmsList = new ArrayList<String>();
        List<InputDataObjectType> applicationInputs = processContext.getProcessModel().getProcessInputs();
        if (applicationInputs != null && !applicationInputs.isEmpty()){
            for (InputDataObjectType output : applicationInputs){
                if(output.getType().equals(DataType.URI)) {
                    filePrmsList.add(output.getValue());
                }
            }
        }
        return filePrmsList;
    }

    public void setStorageClient(StorageClient sc){
        storageClient = sc;
    }

    public void downloadStdOuts()  throws GFacException{

        String stdoutFileName = new File(stdoutLocation).getName();

        String stderrFileName = new File(stderrLocation).getName();

        FileDownloader f1 = null;
        log.info("Downloading stdout and stderr..");
        log.info(stdoutFileName + " -> " + stdoutLocation);

        f1 = new FileDownloader(stdoutFileName, stdoutLocation, Mode.overwrite);
        try {
            f1.perform(storageClient);
//            String stdoutput = readFile(stdoutLocation);
        } catch (Exception e) {
            log.error("Error while downloading " + stdoutFileName + " to location " + stdoutLocation, e);
        }

        log.info(stderrFileName + " -> " + stderrLocation);
        f1.setFrom(stderrFileName);
        f1.setTo(stderrLocation);
        try {
            f1.perform(storageClient);
//            String stderror = readFile(stderrLocation);
        } catch (Exception e) {
            log.error("Error while downloading " + stderrFileName + " to location " + stderrLocation);
        }
        String scriptExitCodeFName = "UNICORE_SCRIPT_EXIT_CODE";
        String scriptCodeLocation = gatewayDownloadLocation + File.separator + scriptExitCodeFName;
        if (UASDataStagingProcessor.isUnicoreEndpoint(processContext)) {
            f1.setFrom(scriptExitCodeFName);
            f1.setTo(scriptCodeLocation);
            try {
                f1.perform(storageClient);
                OutputDataObjectType output = new OutputDataObjectType();
                output.setName(scriptExitCodeFName);
                output.setValue(scriptCodeLocation);
                output.setType(DataType.URI);
                output.setIsRequired(true);
                processContext.getProcessModel().getProcessOutputs().add(output);
                log.info("UNICORE_SCRIPT_EXIT_CODE -> " + scriptCodeLocation);
                log.info("EXIT CODE: " + readFile(scriptCodeLocation));
            } catch (Exception e) {
                log.error("Error downloading file " + scriptExitCodeFName + " to location " + scriptCodeLocation, e);
            }
        }
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

	private String getDownloadLocation() {
		ProcessModel processModel = processContext.getProcessModel();
		String outputDataDir = "";

		if (processContext.getOutputDir() != null ) {

			outputDataDir = processContext.getOutputDir();
			
			
			if ("".equals(outputDataDir)) {
				outputDataDir = getTempPath();
			}

			else {
				
				// in case of remote locations use the tmp location
				if (outputDataDir.startsWith("scp:") || 
						outputDataDir.startsWith("ftp:") ||
						outputDataDir.startsWith("gsiftp:")) {
						outputDataDir = getTempPath();
				} else if ( outputDataDir.startsWith("file:")  && 
						     outputDataDir.contains("@")){
							outputDataDir = getTempPath();
					
				} else {
					try {
						URI u = new URI(outputDataDir);
						outputDataDir = u.getPath();
					} catch (URISyntaxException e) {
						outputDataDir = getTempPath();
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

	private String getTempPath() {
		String tmpOutputDir = File.separator + "tmp" + File.separator
				+ processContext.getProcessId();
		(new File(tmpOutputDir)).mkdirs();
		return tmpOutputDir;
	}

    public List<OutputDataObjectType> downloadRemoteFiles() throws GFacException {

        if(log.isDebugEnabled()) {
            log.debug("Download location is:" + gatewayDownloadLocation);
        }

        List<OutputDataObjectType> applicationOutputs = processContext.getProcessModel().getProcessOutputs();
        if (applicationOutputs != null && !applicationOutputs.isEmpty()){
            for (OutputDataObjectType output : applicationOutputs){
                if("".equals(output.getValue()) || output.getValue() == null) {
                    continue;
                }
                if(output.getType().equals(DataType.STDOUT)) {
                    output.setValue(stdoutLocation);
                    resultantOutputsLst.add(output);
                } else if(output.getType().equals(DataType.STDERR)) {
                    output.setValue(stderrLocation);
                    resultantOutputsLst.add(output);
                } else if (output.getType().equals(DataType.URI)) {
                    String value = null;
                    if (!output.getLocation().isEmpty()) {
                        value = output.getLocation() + File.separator + output.getValue();
                    } else {
                        value = output.getValue();
                    }
                    String outputPath = gatewayDownloadLocation + File.separator + output.getValue();
                    File f = new File(gatewayDownloadLocation);
                    if (!f.exists())
                        f.mkdirs();

                    FileDownloader fileDownloader = new FileDownloader(value, outputPath, Mode.overwrite);
                    try {
                        log.info("Downloading file {}", value);
                        fileDownloader.perform(storageClient);
                        output.setType(DataType.URI);
                        output.setValue(outputPath);
                        resultantOutputsLst.add(output);
                    } catch (Exception e) {
                        log.error("Error downloading " + value + " from job working directory. ");
//                        throw new GFacException(e.getLocalizedMessage(),e);
                    }
                } else {
                    log.info("Ignore output file {}, type {}", output.getValue(), output.getType().toString());
                }

            }

        }

        downloadStdOuts();
        return resultantOutputsLst;

    }

    public void publishFinalOutputs() throws GFacException, TException {
        if(!resultantOutputsLst.isEmpty()) {
            log.debug("Publishing the list of outputs to the registry instance..");
            RegistryService.Client registryClient = Factory.getRegistryServiceClient();
            try {
                registryClient.addExperimentProcessOutputs(GFacConstants.EXPERIMENT_OUTPUT, resultantOutputsLst, processContext.getExperimentId());
            } finally {
                if (registryClient != null) {
                    ThriftUtils.close(registryClient);
                }
            }
        }


    }
	
}