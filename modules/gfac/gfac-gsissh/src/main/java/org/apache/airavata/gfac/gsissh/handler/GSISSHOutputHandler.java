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
package org.apache.airavata.gfac.gsissh.handler;

//import org.apache.airavata.commons.gfac.type.ActualParameter;
//import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractRecoverableHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutputUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.util.GFACGSISSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.DataTransferDetails;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TransferState;
import org.apache.airavata.model.workspace.experiment.TransferStatus;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GSISSHOutputHandler extends AbstractRecoverableHandler {
    private static final Logger log = LoggerFactory.getLogger(GSISSHOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        super.invoke(jobExecutionContext);
        int index = 0;
        int oldIndex = 0;
        List<String> oldFiles = new ArrayList<String>();
        StringBuffer data = new StringBuffer("|");
        String hostAddress = jobExecutionContext.getHostName();
        try {
            if (jobExecutionContext.getSecurityContext(hostAddress) == null) {
                GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
            }
        }  catch (Exception e) {
        	 try {
  				GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
  			} catch (GFacException e1) {
  				 log.error(e1.getLocalizedMessage());
  			}  
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }
        DataTransferDetails detail = new DataTransferDetails();
        TransferStatus status = new TransferStatus();

        Cluster cluster = null;
        
        try {
            cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();
            if (cluster == null) {
                GFacUtils.saveErrorDetails(jobExecutionContext, "Security context is not set properly", CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
                
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
            String pluginData = GFacUtils.getPluginData(jobExecutionContext, this.getClass().getName());
            if (pluginData != null) {
                try {
                    oldIndex = Integer.parseInt(pluginData.split("\\|")[0].trim());
                    oldFiles = Arrays.asList(pluginData.split("\\|")[1].split(","));
                    if (oldIndex == oldFiles.size()) {
                        log.info("Old data looks good !!!!");
                    } else {
                        oldIndex = 0;
                        oldFiles.clear();
                    }
                } catch (NumberFormatException e) {
                    log.error("Previously stored data " + pluginData + " is wrong so we continue the operations");
                }
            }

            String timeStampedExperimentID = GFacUtils.createUniqueNameWithDate(jobExecutionContext.getExperimentID());

            TaskDetails taskData = jobExecutionContext.getTaskData();
            String outputDataDir = null;
            File localStdOutFile;
            File localStdErrFile;
            //FIXME: AdvancedOutput is remote location and third party transfer should work to make this work 
//            if (taskData.getAdvancedOutputDataHandling() != null) {
//                outputDataDir = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
//            }
            if (outputDataDir == null) {
                outputDataDir = File.separator + "tmp";
            }
            outputDataDir = outputDataDir + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID();
            (new File(outputDataDir)).mkdirs();
         	
            String stdOutStr = "";
            if (index < oldIndex) {
                localStdOutFile = new File(oldFiles.get(index));
                data.append(oldFiles.get(index++)).append(",");
            } else {
            	int i = 0;
                localStdOutFile = new File(outputDataDir + File.separator + jobExecutionContext.getApplicationName() + ".stdout");
                while(stdOutStr.isEmpty()){
                try {
                	cluster.scpFrom(jobExecutionContext.getStandardOutput(), localStdOutFile.getAbsolutePath());
                	stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					  Thread.sleep(2000);
		        }
                i++;
                if(i==3)break;
                }
                
                StringBuffer temp = new StringBuffer(data.append(localStdOutFile.getAbsolutePath()).append(",").toString());
                GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
            }
            if (index < oldIndex) {
                localStdErrFile = new File(oldFiles.get(index));
                data.append(oldFiles.get(index++)).append(",");
            } else {
                localStdErrFile = new File(outputDataDir + File.separator + jobExecutionContext.getApplicationName() + ".stderr");
                cluster.scpFrom(jobExecutionContext.getStandardError(), localStdErrFile.getAbsolutePath());
                StringBuffer temp = new StringBuffer(data.append(localStdErrFile.getAbsolutePath()).append(",").toString());
                GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
            }

            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());
            status.setTransferState(TransferState.STDOUT_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDOUT:" + localStdOutFile.getAbsolutePath());
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            status.setTransferState(TransferState.STDERROR_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDERR:" + localStdErrFile.getAbsolutePath());
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            //todo this is a mess we have to fix this
            List<OutputDataObjectType> outputArray = new ArrayList<OutputDataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                OutputDataObjectType outputDataObjectType = (OutputDataObjectType) output.get(paramName);
                if (DataType.URI == outputDataObjectType.getType()) {

                    List<String> outputList = null;
                    int retry=3;
                    while(retry>0){
                    	 outputList = cluster.listDirectory(jobExecutionContext.getOutputDir());
                        if (outputList.size() == 1 && outputList.get(0).isEmpty()) {
                            Thread.sleep(10000);
                        } else if (outputList.size() > 0) {
                            break;
                        }else{
                            Thread.sleep(10000);
                        }
                        retry--;
                        if(retry==0){
                        }
                    	 Thread.sleep(10000);
                    }
                    if (outputList.size() == 0 || outputList.get(0).isEmpty() || outputList.size() > 1) {
                        OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
                        Set<String> strings = output.keySet();
                        outputArray.clear();
                        for (String key : strings) {
                            OutputDataObjectType outputDataObjectType1 = (OutputDataObjectType) output.get(key);
                            if (DataType.URI == outputDataObjectType1.getType()) {
                                String downloadFile = outputDataObjectType1.getValue();
                                String localFile;
                                if (index < oldIndex) {
                                    localFile = oldFiles.get(index);
                                    data.append(oldFiles.get(index++)).append(",");
                                } else {
                                    cluster.scpFrom(downloadFile, outputDataDir);
                                    String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar) + 1, downloadFile.length());
                                    localFile = outputDataDir + File.separator + fileName;
                                    StringBuffer temp = new StringBuffer(data.append(localFile).append(",").toString());
                                    GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                                }
                                jobExecutionContext.addOutputFile(localFile);
                                outputDataObjectType1.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.URI);
                                outputArray.add(dataObjectType);
                            }else if (DataType.STDOUT == outputDataObjectType1.getType()) {
                                String localFile;
                                if (index < oldIndex) {
                                    localFile = oldFiles.get(index);
                                    data.append(oldFiles.get(index++)).append(",");
                                } else {
                                    String fileName = localStdOutFile.getName();
                                    localFile = outputDataDir + File.separator + fileName;
                                    StringBuffer temp = new StringBuffer(data.append(localFile).append(",").toString());
                                    GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                                }
                                jobExecutionContext.addOutputFile(localFile);
                                outputDataObjectType1.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.STDOUT);
                                outputArray.add(dataObjectType);
                            }else if (DataType.STDERR == outputDataObjectType1.getType()) {
                                String localFile;
                                if (index < oldIndex) {
                                    localFile = oldFiles.get(index);
                                    data.append(oldFiles.get(index++)).append(",");
                                } else {
                                    String fileName = localStdErrFile.getName();
                                    localFile = outputDataDir + File.separator + fileName;
                                    StringBuffer temp = new StringBuffer(data.append(localFile).append(",").toString());
                                    GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                                }
                                jobExecutionContext.addOutputFile(localFile);
                                outputDataObjectType1.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.STDERR);
                                outputArray.add(dataObjectType);
                            }
                        }
                        break;
                    } else if(outputList.size() == 1) { //FIXME: this is ultrascan specific
                        String valueList = outputList.get(0);
                        String outputFile;
                        if (index < oldIndex) {
                            outputFile = oldFiles.get(index);
                            data.append(oldFiles.get(index++)).append(",");
                        } else {
                            cluster.scpFrom(jobExecutionContext.getOutputDir() + File.separator + valueList, outputDataDir);
                            outputFile = outputDataDir + File.separator + valueList;
                            jobExecutionContext.addOutputFile(outputFile);
                            StringBuffer temp = new StringBuffer(data.append(outputFile).append(",").toString());
                            GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                        }
                        jobExecutionContext.addOutputFile(outputFile);
                        outputDataObjectType.setValue(outputFile);
                        OutputDataObjectType dataObjectType  = new OutputDataObjectType();
                        dataObjectType.setValue(valueList);
                        dataObjectType.setName(paramName);
                        dataObjectType.setType(DataType.URI);
                        outputArray.add(dataObjectType);
                    }
                } else {
                    OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
//                    break;
                }
            }
            if (outputArray == null || outputArray.isEmpty()) {
                if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() == null){
                throw new GFacHandlerException(
                        "Empty Output returned from the Application, Double check the application"
                                + "and ApplicationDescriptor output Parameter Names"
                );
                }
            }
            // Why we set following?
            jobExecutionContext.setStandardError(localStdErrFile.getAbsolutePath());
            jobExecutionContext.setStandardOutput(localStdOutFile.getAbsolutePath());
            jobExecutionContext.setOutputDir(outputDataDir);
            status.setTransferState(TransferState.DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription(outputDataDir);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
            registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
            fireTaskOutputChangeEvent(jobExecutionContext, outputArray);
        } catch (Exception e) {
            try {
                status.setTransferState(TransferState.FAILED);
                detail.setTransferStatus(status);
                detail.setTransferDescription(e.getLocalizedMessage());
                registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
                GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
            } catch (Exception e1) {
                throw new GFacHandlerException("Error persisting status", e1, e1.getLocalizedMessage());
            }
            throw new GFacHandlerException("Error in retrieving results", e);
        }
     }

    public void initProperties(Properties properties) throws GFacHandlerException {

    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        this.invoke(jobExecutionContext);
    }
}
