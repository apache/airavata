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

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

/**
 * This handler will copy input data from gateway machine to airavata
 * installed machine, later running handlers can copy the input files to computing resource
 * <Handler class="org.apache.airavata.gfac.handler.AdvancedSCPOutputHandler">
                            <property name="privateKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa"/>
                            <property name="publicKeyPath" value="/Users/lahirugunathilake/.ssh/id_dsa.pub"/>
                        <property name="userName" value="airavata"/>
                        <property name="hostName" value="gw98.iu.xsede.org"/>
                        <property name="inputPath" value="/home/airavata/outputData"/>
 */
public class AdvancedSCPInputHandler extends AbstractHandler{
    private static final Logger log = LoggerFactory.getLogger(AdvancedSCPInputHandler.class);

    private String password = null;

    private String publicKeyPath;

    private String passPhrase;

    private String privateKeyPath;

    private String userName;

    private String hostName;

    private String inputPath;

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {
        password = properties.get("password");
        passPhrase = properties.get("passPhrase");
        privateKeyPath = properties.get("privateKeyPath");
        publicKeyPath = properties.get("publicKeyPath");
        userName = properties.get("userName");
        hostName = properties.get("hostName");
        inputPath = properties.get("inputPath");
    }

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException, GFacException {
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
        MessageContext inputNew = new MessageContext();
        try {
            // here doesn't matter what the job manager is because we are only doing some file handling
            // not really dealing with monitoring or job submission, so we pa
            pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));
            String parentPath = inputPath + File.separator + jobExecutionContext.getExperimentID() + File.separator + jobExecutionContext.getTaskData().getTaskID();
            pbsCluster.makeDirectory(parentPath);
            MessageContext input = jobExecutionContext.getInMessageContext();
            Set<String> parameters = input.getParameters().keySet();
            for (String paramName : parameters) {
                ActualParameter actualParameter = (ActualParameter) input.getParameters().get(paramName);
                String paramValue = MappingFactory.toString(actualParameter);
                //TODO: Review this with type
                if ("URI".equals(actualParameter.getType().getType().toString())) {
                    ((URIParameterType) actualParameter.getType()).setValue(stageInputFiles(pbsCluster, paramValue, parentPath));
                } else if ("URIArray".equals(actualParameter.getType().getType().toString())) {
                    List<String> split = Arrays.asList(StringUtil.getElementsFromString(paramValue));
                    List<String> newFiles = new ArrayList<String>();
                    for (String paramValueEach : split) {
                        String stageInputFiles = stageInputFiles(pbsCluster, paramValueEach, parentPath);
                        newFiles.add(stageInputFiles);
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

    private String stageInputFiles(Cluster cluster, String paramValue, String parentPath) throws GFacException {
        try {
            cluster.scpFrom(paramValue, parentPath);
            return "file://" + parentPath + File.separator + URI.create(paramValue).toURL().getFile();
        } catch (SSHApiException e) {
            log.error("Error tranfering remote file to local file, remote path: " + paramValue);
            throw new GFacException(e);
        } catch (MalformedURLException e) {
            log.error("Error processing input URL");
            throw new GFacException(e);
        }
    }
}
