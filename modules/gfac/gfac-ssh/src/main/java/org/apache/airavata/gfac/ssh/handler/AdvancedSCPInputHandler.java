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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.AbstractRecoverableHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.workspace.experiment.DataTransferDetails;
import org.apache.airavata.model.workspace.experiment.TransferState;
import org.apache.airavata.model.workspace.experiment.TransferStatus;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * This handler will copy input data from gateway machine to airavata
 * installed machine, later running handlers can copy the input files to computing resource
 * <Handler class="AdvancedSCPOutputHandler">
 * <property name="privateKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa"/>
 * <property name="publicKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa.pub"/>
 * <property name="userName" value="airavata"/>
 * <property name="hostName" value="gw98.iu.xsede.org"/>
 * <property name="inputPath" value="/home/airavata/outputData"/>
 */
public class AdvancedSCPInputHandler extends AbstractRecoverableHandler {
    private static final Logger log = LoggerFactory.getLogger(AdvancedSCPInputHandler.class);

    private String password = null;

    private String publicKeyPath;

    private String passPhrase;

    private String privateKeyPath;

    private String userName;

    private String hostName;

    private String inputPath;

    public void initProperties(Properties properties) throws GFacHandlerException {
        password = (String) properties.get("password");
        passPhrase = (String) properties.get("passPhrase");
        privateKeyPath = (String) properties.get("privateKeyPath");
        publicKeyPath = (String) properties.get("publicKeyPath");
        userName = (String) properties.get("userName");
        hostName = (String) properties.get("hostName");
        inputPath = (String) properties.get("inputPath");
    }

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        super.invoke(jobExecutionContext);
        int index = 0;
        int oldIndex = 0;
        List<String> oldFiles = new ArrayList<String>();
        MessageContext inputNew = new MessageContext();
        StringBuffer data = new StringBuffer("|");
        try {
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
            if (jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT) == null) {
                try {
                    GFACSSHUtils.addSecurityContext(jobExecutionContext);
                } catch (ApplicationSettingsException e) {
                    log.error(e.getMessage());
                    throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
                }
            }
            ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                    .getApplicationDeploymentDescription().getType();

            AuthenticationInfo authenticationInfo = null;
            if (password != null) {
                authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
            } else {
                authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                        this.passPhrase);
            }
            // Server info
            ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
            Cluster pbsCluster = null;
            // here doesn't matter what the job manager is because we are only doing some file handling
            // not really dealing with monitoring or job submission, so we pa
            pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));
            String parentPath = inputPath + File.separator + jobExecutionContext.getExperimentID() + File.separator + jobExecutionContext.getTaskData().getTaskID();
            if (index < oldIndex) {
                parentPath = oldFiles.get(index);
                data.append(oldFiles.get(index++)).append(","); // we get already transfered file and increment the index
            } else {
                (new File(parentPath)).mkdirs();
                StringBuffer temp = new StringBuffer(data.append(parentPath).append(",").toString());
                GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
            }
            DataTransferDetails detail = new DataTransferDetails();
            TransferStatus status = new TransferStatus();
          
            MessageContext input = jobExecutionContext.getInMessageContext();
            Set<String> parameters = input.getParameters().keySet();
            for (String paramName : parameters) {
                ActualParameter actualParameter = (ActualParameter) input.getParameters().get(paramName);
                String paramValue = MappingFactory.toString(actualParameter);
                //TODO: Review this with type
                if ("URI".equals(actualParameter.getType().getType().toString())) {
                    if (index < oldIndex) {
                        log.info("Input File: " + paramValue + " is already transfered, so we skip this operation !!!");
                        ((URIParameterType) actualParameter.getType()).setValue(oldFiles.get(index));
                        data.append(oldFiles.get(index++)).append(","); // we get already transfered file and increment the index
                    } else {
                        String stageInputFile = stageInputFiles(pbsCluster, paramValue, parentPath);
                        ((URIParameterType) actualParameter.getType()).setValue(stageInputFile);
                        StringBuffer temp = new StringBuffer(data.append(stageInputFile).append(",").toString());
                        status.setTransferState(TransferState.UPLOAD);
                        detail.setTransferStatus(status);
                        detail.setTransferDescription("Input Data Staged: " + stageInputFile);
                        registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
                
                        GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                    }
                } else if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                    List<String> split = Arrays.asList(StringUtil.getElementsFromString(paramValue));
                    List<String> newFiles = new ArrayList<String>();
                    for (String paramValueEach : split) {
                        if (index < oldIndex) {
                            log.info("Input File: " + paramValue + " is already transfered, so we skip this operation !!!");
                            newFiles.add(oldFiles.get(index));
                            data.append(oldFiles.get(index++)).append(",");
                        } else {
                            String stageInputFiles = stageInputFiles(pbsCluster, paramValueEach, parentPath);
                            StringBuffer temp = new StringBuffer(data.append(stageInputFiles).append(",").toString());
                            GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                            newFiles.add(stageInputFiles);
                        }
                    }
                    ((URIArrayType) actualParameter.getType()).setValueArray(newFiles.toArray(new String[newFiles.size()]));
                }
                inputNew.getParameters().put(paramName, actualParameter);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while input File Staging", e, e.getLocalizedMessage());
        }
        jobExecutionContext.setInMessageContext(inputNew);
    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        this.invoke(jobExecutionContext);
    }

    private String stageInputFiles(Cluster cluster, String paramValue, String parentPath) throws GFacException {
        try {
            cluster.scpFrom(paramValue, parentPath);
            return "file://" + parentPath + File.separator + (new File(paramValue)).getName();
        } catch (SSHApiException e) {
            log.error("Error tranfering remote file to local file, remote path: " + paramValue);
            throw new GFacException(e);
        }
    }
}
