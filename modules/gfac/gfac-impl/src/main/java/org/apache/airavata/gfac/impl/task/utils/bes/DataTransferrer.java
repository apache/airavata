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

package org.apache.airavata.gfac.impl.task.utils.bes;

import de.fzj.unicore.uas.client.StorageClient;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
	
	public DataTransferrer(ProcessContext jobContext, StorageClient storageClient) {
		this.processContext = jobContext;
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
	
}