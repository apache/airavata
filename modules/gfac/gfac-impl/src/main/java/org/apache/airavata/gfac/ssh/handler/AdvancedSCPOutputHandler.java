///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.gfac.ssh.handler;
//
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.gfac.core.GFacException;
//import org.apache.airavata.gfac.core.SSHApiException;
//import org.apache.airavata.gfac.core.cluster.RemoteCluster;
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.handler.AbstractHandler;
//import org.apache.airavata.gfac.core.handler.GFacHandlerException;
//import org.apache.airavata.gfac.core.GFacUtils;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
//import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
//import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
//import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
//import org.apache.airavata.model.appcatalog.appinterface.DataType;
//import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
//import org.apache.airavata.model.experiment.CorrectiveAction;
//import org.apache.airavata.model.experiment.ErrorCategory;
//import org.apache.airavata.registry.cpi.ExpCatChildDataType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.*;
//
///**
// * This handler will copy outputs from airavata installed local directory
// * to a remote location, prior to this handler SCPOutputHandler should be invoked
// * Should add following configuration to gfac-config.xml and configure the keys properly
// * <Handler class="AdvancedSCPOutputHandler">
//                            <property name="privateKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa"/>
//                            <property name="publicKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa.pub"/>
//                        <property name="userName" value="airavata"/>
//                        <property name="hostName" value="gw98.iu.xsede.org"/>
//                        <property name="outputPath" value="/home/airavata/outputData"/>
//                        <property name="passPhrase" value="/home/airavata/outputData"/>
//                        <property name="password" value="/home/airavata/outputData"/>
//
// */
//public class AdvancedSCPOutputHandler extends AbstractHandler {
//    private static final Logger log = LoggerFactory.getLogger(AdvancedSCPOutputHandler.class);
//
//    public static final int DEFAULT_SSH_PORT = 22;
//
//    private String password = null;
//
//    private String publicKeyPath;
//
//    private String passPhrase;
//
//    private String privateKeyPath;
//
//    private String userName;
//
//    private String hostName;
//
//    private String outputPath;
//
//
//    public void initProperties(Properties properties) throws GFacHandlerException {
//        password = (String)properties.get("password");
//        passPhrase = (String)properties.get("passPhrase");
//        privateKeyPath = (String)properties.get("privateKeyPath");
//        publicKeyPath = (String)properties.get("publicKeyPath");
//        userName = (String)properties.get("userName");
//        hostName = (String)properties.get("hostName");
//        outputPath = (String)properties.get("outputPath");
//    }
//
//    @Override
//    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//    	RemoteCluster remoteCluster = null;
//        AuthenticationInfo authenticationInfo = null;
//        if (password != null) {
//            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
//        } else {
//            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
//                    this.passPhrase);
//        }
//        try {
//            String hostName = jobExecutionContext.getHostName();
//            if (jobExecutionContext.getSecurityContext(hostName) == null) {
//                try {
//                    GFACSSHUtils.addSecurityContext(jobExecutionContext);
//                } catch (ApplicationSettingsException e) {
//                    log.error(e.getMessage());
//                    try {
//                        StringWriter errors = new StringWriter();
//                        e.printStackTrace(new PrintWriter(errors));
//         				GFacUtils.saveErrorDetails(jobExecutionContext,  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//         			} catch (GFacException e1) {
//         				 log.error(e1.getLocalizedMessage());
//         			}
//                    throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
//                }
//            }
//            String standardError = jobExecutionContext.getStandardError();
//            String standardOutput = jobExecutionContext.getStandardOutput();
//            super.invoke(jobExecutionContext);
//            // Server info
//            if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() != null && jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().getOutputDataDir() != null){
//                try{
//                    URL outputPathURL = new URL(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().getOutputDataDir());
//                    this.userName = outputPathURL.getUserInfo();
//                    this.hostName = outputPathURL.getHost();
//                    outputPath = outputPathURL.getPath();
//                } catch (MalformedURLException e) {
//                    log.error(e.getLocalizedMessage(),e);
//                }
//            }
//            String key = GFACSSHUtils.prepareSecurityContext(jobExecutionContext, authenticationInfo, this.userName, this.hostName, DEFAULT_SSH_PORT);
//            remoteCluster = ((SSHSecurityContext)jobExecutionContext.getSecurityContext(key)).getRemoteCluster();
//            if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() != null && !jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().isPersistOutputData()){
//            outputPath = outputPath + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID()
//                    + File.separator;
//                remoteCluster.makeDirectory(outputPath);
//            }
//            remoteCluster.scpTo(outputPath, standardError);
//            remoteCluster.scpTo(outputPath, standardOutput);
//            List<OutputDataObjectType> outputArray = new ArrayList<OutputDataObjectType>();
//            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
//            Set<String> keys = output.keySet();
//            for (String paramName : keys) {
//                OutputDataObjectType outputDataObjectType = (OutputDataObjectType) output.get(paramName);
//                if (outputDataObjectType.getType() == DataType.URI) {
//                    // for failed jobs outputs are not generated. So we should not download outputs
//                    if (GFacUtils.isFailedJob(jobExecutionContext)){
//                        continue;
//                    }
//                	String downloadFile = outputDataObjectType.getValue();
//                    if(downloadFile == null || !(new File(downloadFile).isFile())){
//                        GFacUtils.saveErrorDetails(jobExecutionContext, "Empty Output returned from the application", CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//                		throw new GFacHandlerException("Empty Output returned from the application.." );
//                	}
//                	remoteCluster.scpTo(outputPath, downloadFile);
//                    String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar)+1, downloadFile.length());
//                    OutputDataObjectType dataObjectType = new OutputDataObjectType();
//                    dataObjectType.setValue(outputPath + File.separatorChar + fileName);
//                    dataObjectType.setName(paramName);
//                    dataObjectType.setType(DataType.URI);
//                    dataObjectType.setIsRequired(outputDataObjectType.isIsRequired());
//                    dataObjectType.setRequiredToAddedToCommandLine(outputDataObjectType.isRequiredToAddedToCommandLine());
//                    dataObjectType.setApplicationArgument(outputDataObjectType.getApplicationArgument());
//                    dataObjectType.setSearchQuery(outputDataObjectType.getSearchQuery());
//                    outputArray.add(dataObjectType);
//                }else if (outputDataObjectType.getType() == DataType.STDOUT) {
//                    remoteCluster.scpTo(outputPath, standardOutput);
//                    String fileName = standardOutput.substring(standardOutput.lastIndexOf(File.separatorChar)+1, standardOutput.length());
//                    OutputDataObjectType dataObjectType = new OutputDataObjectType();
//                    dataObjectType.setValue(outputPath + File.separatorChar + fileName);
//                    dataObjectType.setName(paramName);
//                    dataObjectType.setType(DataType.STDOUT);
//                    dataObjectType.setIsRequired(outputDataObjectType.isIsRequired());
//                    dataObjectType.setRequiredToAddedToCommandLine(outputDataObjectType.isRequiredToAddedToCommandLine());
//                    dataObjectType.setApplicationArgument(outputDataObjectType.getApplicationArgument());
//                    dataObjectType.setSearchQuery(outputDataObjectType.getSearchQuery());
//                    outputArray.add(dataObjectType);
//                }else if (outputDataObjectType.getType() == DataType.STDERR) {
//                    remoteCluster.scpTo(outputPath, standardError);
//                    String fileName = standardError.substring(standardError.lastIndexOf(File.separatorChar)+1, standardError.length());
//                    OutputDataObjectType dataObjectType = new OutputDataObjectType();
//                    dataObjectType.setValue(outputPath + File.separatorChar + fileName);
//                    dataObjectType.setName(paramName);
//                    dataObjectType.setType(DataType.STDERR);
//                    dataObjectType.setIsRequired(outputDataObjectType.isIsRequired());
//                    dataObjectType.setRequiredToAddedToCommandLine(outputDataObjectType.isRequiredToAddedToCommandLine());
//                    dataObjectType.setApplicationArgument(outputDataObjectType.getApplicationArgument());
//                    dataObjectType.setSearchQuery(outputDataObjectType.getSearchQuery());
//                    outputArray.add(dataObjectType);
//                }
//             }
//           experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
//        } catch (SSHApiException e) {
//            try {
//                StringWriter errors = new StringWriter();
//                e.printStackTrace(new PrintWriter(errors));
//				GFacUtils.saveErrorDetails(jobExecutionContext,  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//			} catch (GFacException e1) {
//				 log.error(e1.getLocalizedMessage());
//			}
//            log.error("Error transfering files to remote host : " + hostName + " with the user: " + userName);
//            log.error(e.getMessage());
//            throw new GFacHandlerException(e);
//        } catch (Exception e) {
//        	 try {
// 				GFacUtils.saveErrorDetails(jobExecutionContext,  e.getCause().toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
// 			} catch (GFacException e1) {
// 				 log.error(e1.getLocalizedMessage());
// 			}
//        	throw new GFacHandlerException(e);
//        }
//    }
//
//    @Override
//    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        // TODO: Auto generated method body.
//    }
//
//
//}
