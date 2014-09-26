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
package org.apache.airavata.gfac.ssh.handler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutputUtils;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHOutputHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(SSHOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        if (jobExecutionContext.getApplicationContext().getHostDescription().getType() instanceof GsisshHostType) { // this is because we don't have the right jobexecution context
            // so attempting to get it from the registry
            if (Constants.PUSH.equals(((GsisshHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType()).getMonitorMode())) { // this is because we don't have the right jobexecution context
                // so attempting to get it from the registry
                log.warn("During the out handler chain jobExecution context came null, so trying to handler");
                ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
                TaskDetails taskData = null;
                try {
                    taskData = (TaskDetails) registry.get(RegistryModelType.TASK_DETAIL, jobExecutionContext.getTaskData().getTaskID());
                } catch (RegistryException e) {
                    log.error("Error retrieving job details from Registry");
                    throw new GFacHandlerException("Error retrieving job details from Registry", e);
                }
                JobDetails jobDetails = taskData.getJobDetailsList().get(0);
                String jobDescription = jobDetails.getJobDescription();
                if (jobDescription != null) {
                    JobDescriptor jobDescriptor = null;
                    try {
                        jobDescriptor = JobDescriptor.fromXML(jobDescription);
                    } catch (XmlException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    applicationDeploymentDescription.getType().setScratchWorkingDirectory(
                            jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getWorkingDirectory());
                    applicationDeploymentDescription.getType().setInputDataDirectory(jobDescriptor.getInputDirectory());
                    applicationDeploymentDescription.getType().setOutputDataDirectory(jobDescriptor.getOutputDirectory());
                    applicationDeploymentDescription.getType().setStandardError(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardErrorFile());
                    applicationDeploymentDescription.getType().setStandardOutput(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardOutFile());
                }
            }
        }

        try {
            if (jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT) == null) {

                GFACSSHUtils.addSecurityContext(jobExecutionContext);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
 				GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
 			} catch (GFacException e1) {
 				 log.error(e1.getLocalizedMessage());
 			}
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }

        super.invoke(jobExecutionContext);
        DataTransferDetails detail = new DataTransferDetails();
        TransferStatus status = new TransferStatus();

        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getType();
        try {
            Cluster cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT)).getPbsCluster();
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
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


            localStdOutFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stdout");
            localStdErrFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stderr");
//            cluster.makeDirectory(outputDataDir);
            int i = 0;
            String stdOutStr = "";
            while(stdOutStr.isEmpty()){ 		
            cluster.scpFrom(app.getStandardOutput(), localStdOutFile.getAbsolutePath());
            stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
            i++;
            if(i == 3) break;
            }
            Thread.sleep(1000);
            cluster.scpFrom(app.getStandardError(), localStdErrFile.getAbsolutePath());
            Thread.sleep(1000);

            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());
            status.setTransferState(TransferState.STDOUT_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDOUT:" + stdOutStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            status.setTransferState(TransferState.STDERROR_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDERR:" + stdErrStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());


            List<DataObjectType> outputArray = new ArrayList<DataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                ActualParameter actualParameter = (ActualParameter) output.get(paramName);
                if ("URI".equals(actualParameter.getType().getType().toString())) {

                    List<String> outputList = cluster.listDirectory(app.getOutputDataDirectory());
                    if (outputList.size() == 0 || outputList.get(0).isEmpty()) {
                        OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr,outputArray);
                        Set<String> strings = output.keySet();
                        outputArray.clear();
                        for (String key : strings) {
                            ActualParameter actualParameter1 = (ActualParameter) output.get(key);
                            if ("URI".equals(actualParameter1.getType().getType().toString())) {
                              	String downloadFile = MappingFactory.toString(actualParameter1);
                            	cluster.scpFrom(downloadFile, outputDataDir);
                            	String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar)+1, downloadFile.length());
                            	String localFile = outputDataDir +  File.separator +fileName;
								jobExecutionContext.addOutputFile(localFile);
								MappingFactory.fromString(actualParameter1, localFile);
								DataObjectType dataObjectType = new DataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setKey(key);
                                dataObjectType.setType(DataType.URI);
                                outputArray.add(dataObjectType);
                            }
                        }
                    
                        break;
                    } else {
                        String valueList = outputList.get(0);
                        cluster.scpFrom(app.getOutputDataDirectory() + File.separator + valueList, outputDataDir);
                        String outputPath = outputDataDir + File.separator + valueList;
						jobExecutionContext.addOutputFile(outputPath);
						MappingFactory.fromString(actualParameter, outputPath);
						DataObjectType dataObjectType = new DataObjectType();
                        dataObjectType.setValue(outputPath);
                        dataObjectType.setKey(paramName);
                        dataObjectType.setType(DataType.URI);
                        outputArray.add(dataObjectType);
                    }
                } else {
                    OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr,outputArray);
                }
            }
            if (outputArray == null || outputArray.isEmpty()) {
            	log.error("Empty Output returned from the Application, Double check the application and ApplicationDescriptor output Parameter Names");
            	if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() == null){
            		throw new GFacHandlerException(
                        "Empty Output returned from the Application, Double check the application"
                                + "and ApplicationDescriptor output Parameter Names");
            	}
            }
            app.setStandardError(localStdErrFile.getAbsolutePath());
            app.setStandardOutput(localStdOutFile.getAbsolutePath());
            app.setOutputDataDirectory(outputDataDir);
            status.setTransferState(TransferState.DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription(outputDataDir);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
            registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
            
        }catch (Exception e) {
            try {
                status.setTransferState(TransferState.FAILED);
                detail.setTransferStatus(status);
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
}
