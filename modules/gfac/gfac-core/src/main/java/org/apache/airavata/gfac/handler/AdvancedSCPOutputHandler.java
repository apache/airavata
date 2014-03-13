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
package org.apache.airavata.gfac.handler;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This handler will copy outputs from airavata installed local directory
 * to a remote location, prior to this handler SCPOutputHandler should be invoked
 * Should add following configuration to gfac-config.xml and configure the keys properly
 * <Handler class="org.apache.airavata.gfac.handler.AdvancedSCPOutputHandler">
                            <property name="privateKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa"/>
                            <property name="publicKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa.pub"/>
                        <property name="userName" value="airavata"/>
                        <property name="hostName" value="gw98.iu.xsede.org"/>
                        <property name="outputPath" value="/home/airavata/outputData"/>
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

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {
        password = properties.get("password");
        passPhrase = properties.get("passPhrase");
        privateKeyPath = properties.get("privateKeyPath");
        publicKeyPath = properties.get("publicKeyPath");
        userName = properties.get("userName");
        hostName = properties.get("hostName");
        outputPath = properties.get("outputPath");
    }

    @Override
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException, GFacException {
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getType();
        String standardError = app.getStandardError();
        String standardOutput = app.getStandardOutput();
        String outputDataDirectory = app.getOutputDataDirectory();

        AuthenticationInfo authenticationInfo = null;
        if (password != null) {
            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
        } else {
            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                    this.passPhrase);
        }
        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
        try {
            Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));
            outputPath = outputPath + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID()
            + File.separator;
            pbsCluster.scpTo(outputPath, standardError);
            pbsCluster.scpTo(outputPath,standardOutput);
            for(String files:jobExecutionContext.getOutputFiles()){
                pbsCluster.scpTo(outputPath,files);
            }
        } catch (SSHApiException e) {
            log.error("Error transfering files to remote host : " + hostName + " with the user: " + userName);
            log.error(e.getMessage());
            throw new GFacException(e);
        }
    }
}
