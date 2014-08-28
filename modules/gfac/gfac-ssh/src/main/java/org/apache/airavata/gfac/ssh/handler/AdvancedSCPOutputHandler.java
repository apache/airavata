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
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
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
import org.apache.airavata.model.workspace.experiment.DataObjectType;
import org.apache.airavata.model.workspace.experiment.DataType;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This handler will copy outputs from airavata installed local directory
 * to a remote location, prior to this handler SCPOutputHandler should be invoked
 * Should add following configuration to gfac-config.xml and configure the keys properly
 * <Handler class="AdvancedSCPOutputHandler">
                            <property name="privateKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa"/>
                            <property name="publicKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa.pub"/>
                        <property name="userName" value="airavata"/>
                        <property name="hostName" value="gw98.iu.xsede.org"/>
                        <property name="outputPath" value="/home/airavata/outputData"/>
                        <property name="passPhrase" value="/home/airavata/outputData"/>
                        <property name="password" value="/home/airavata/outputData"/>

 */
public class AdvancedSCPOutputHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(AdvancedSCPOutputHandler.class);

    private String password = null;

    private String publicKeyPath;

    private String passPhrase;

    private String privateKeyPath;

    private String userName;

    private String hostName;

    private String outputPath;

    public void initProperties(Properties properties) throws GFacHandlerException {
        password = (String)properties.get("password");
        passPhrase = (String)properties.get("passPhrase");
        privateKeyPath = (String)properties.get("privateKeyPath");
        publicKeyPath = (String)properties.get("publicKeyPath");
        userName = (String)properties.get("userName");
        hostName = (String)properties.get("hostName");
        outputPath = (String)properties.get("outputPath");
    }

    @Override
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        try {
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
            String standardError = app.getStandardError();
            String standardOutput = app.getStandardOutput();
            String outputDataDirectory = app.getOutputDataDirectory();
            super.invoke(jobExecutionContext);
            AuthenticationInfo authenticationInfo = null;
            if (password != null) {
                authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
            } else {
                authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                        this.passPhrase);
            }
            // Server info
            if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().getOutputDataDir() != null){
            	try{
            	URL outputPathURL = new URL(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().getOutputDataDir());
            	this.userName = outputPathURL.getUserInfo();
            	this.hostName = outputPathURL.getHost();
            	outputPath = outputPathURL.getPath();
            	} catch (MalformedURLException e) {
					log.error(e.getLocalizedMessage(),e);
				}
            }
            ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);

            Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));
            if(!jobExecutionContext.getTaskData().getAdvancedOutputDataHandling().isPersistOutputData()){
            outputPath = outputPath + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID()
                    + File.separator;
            pbsCluster.makeDirectory(outputPath);
            }
            pbsCluster.scpTo(outputPath, standardError);
            pbsCluster.scpTo(outputPath, standardOutput);
            List<DataObjectType> outputArray = new ArrayList<DataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                ActualParameter actualParameter = (ActualParameter) output.get(paramName);
                if ("URI".equals(actualParameter.getType().getType().toString())) {
                	String downloadFile = MappingFactory.toString(actualParameter);
                	pbsCluster.scpTo(outputPath, downloadFile);
                    String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar)+1, downloadFile.length());
                    DataObjectType dataObjectType = new DataObjectType();
                    dataObjectType.setValue(outputPath + File.separatorChar + fileName);
                    dataObjectType.setKey(paramName);
                    dataObjectType.setType(DataType.URI);
                    outputArray.add(dataObjectType);
                }
             }
           registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
        } catch (SSHApiException e) {
            log.error("Error transfering files to remote host : " + hostName + " with the user: " + userName);
            log.error(e.getMessage());
            throw new GFacHandlerException(e);
        } catch (Exception e) {
            throw new GFacHandlerException(e);
        }
    }
}
