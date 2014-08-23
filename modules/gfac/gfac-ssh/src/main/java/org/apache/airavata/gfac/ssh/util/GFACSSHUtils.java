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
package org.apache.airavata.gfac.ssh.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.security.TokenizedSSHAuthInfo;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GFACSSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACSSHUtils.class);

    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        HostDescription registeredHost = jobExecutionContext.getApplicationContext().getHostDescription();
        if (registeredHost.getType() instanceof GlobusHostType || registeredHost.getType() instanceof UnicoreHostType) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if (registeredHost.getType() instanceof SSHHostType
                || registeredHost.getType() instanceof GsisshHostType) {
            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
            String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
            RequestData requestData = new RequestData(ServerSettings.getDefaultUserGateway());
            requestData.setTokenId(credentialStoreToken);

            ServerInfo serverInfo = new ServerInfo(null, registeredHost.getType().getHostAddress());

            Cluster pbsCluster = null;
            try {
                TokenizedSSHAuthInfo tokenizedSSHAuthInfo = new TokenizedSSHAuthInfo(requestData);
                String installedParentPath = ((HpcApplicationDeploymentType)
                            jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath();
                if(installedParentPath== null) {
                    installedParentPath = "/";
                }
                SSHCredential credentials = tokenizedSSHAuthInfo.getCredentials();// this is just a call to get and set credentials in to this object,data will be used
                serverInfo.setUserName(credentials.getPortalUserName());

                // inside the pbsCluser object
                pbsCluster = new PBSCluster(serverInfo, tokenizedSSHAuthInfo,
                        CommonUtils.getPBSJobManager(installedParentPath));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            sshSecurityContext.setPbsCluster(pbsCluster);
            jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, sshSecurityContext);
        }
    }

    public static JobDescriptor createJobDescriptor(JobExecutionContext jobExecutionContext,
                                                    ApplicationDeploymentDescriptionType app, Cluster cluster) {
        JobDescriptor jobDescriptor = new JobDescriptor();
        // this is common for any application descriptor
        jobDescriptor.setInputDirectory(app.getInputDataDirectory());
        jobDescriptor.setOutputDirectory(app.getOutputDataDirectory());
        jobDescriptor.setExecutablePath(app.getExecutableLocation());
        jobDescriptor.setStandardOutFile(app.getStandardOutput());
        jobDescriptor.setStandardErrorFile(app.getStandardError());
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE);
        jobDescriptor.setJobName(String.valueOf(i+99999999));
        jobDescriptor.setWorkingDirectory(app.getStaticWorkingDirectory());


        List<String> inputValues = new ArrayList<String>();
        MessageContext input = jobExecutionContext.getInMessageContext();
        Map<String, Object> inputs = input.getParameters();
        Set<String> keys = inputs.keySet();
        for (String paramName : keys) {
            ActualParameter actualParameter = (ActualParameter) inputs.get(paramName);
            if ("URIArray".equals(actualParameter.getType().getType().toString()) || "StringArray".equals(actualParameter.getType().getType().toString())
                    || "FileArray".equals(actualParameter.getType().getType().toString())) {
                String[] values = null;
                if (actualParameter.getType() instanceof URIArrayType) {
                    values = ((URIArrayType) actualParameter.getType()).getValueArray();
                } else if (actualParameter.getType() instanceof StringArrayType) {
                    values = ((StringArrayType) actualParameter.getType()).getValueArray();
                } else if (actualParameter.getType() instanceof FileArrayType) {
                    values = ((FileArrayType) actualParameter.getType()).getValueArray();
                }
                String value = StringUtil.createDelimiteredString(values, " ");
                inputValues.add(value);
            } else {
                String paramValue = MappingFactory.toString(actualParameter);
                inputValues.add(paramValue);
            }
        }
        jobDescriptor.setInputValues(inputValues);

        // this part will fill out the hpcApplicationDescriptor
        if (app instanceof HpcApplicationDeploymentType) {
            HpcApplicationDeploymentType applicationDeploymentType
                    = (HpcApplicationDeploymentType) app;
            jobDescriptor.setShellName("/bin/bash");
            jobDescriptor.setAllEnvExport(true);
            jobDescriptor.setMailOptions("n");
            jobDescriptor.setNodes(applicationDeploymentType.getNodeCount());
            jobDescriptor.setProcessesPerNode(applicationDeploymentType.getProcessorsPerNode());
            jobDescriptor.setMaxWallTime(String.valueOf(applicationDeploymentType.getMaxWallTime()));
            jobDescriptor.setJobSubmitter(applicationDeploymentType.getJobSubmitterCommand());
            jobDescriptor.setCPUCount(applicationDeploymentType.getCpuCount());
            if (applicationDeploymentType.getProjectAccount() != null) {
                if (applicationDeploymentType.getProjectAccount().getProjectAccountNumber() != null) {
                    jobDescriptor.setAcountString(applicationDeploymentType.getProjectAccount().getProjectAccountNumber());
                }
            }
            if (applicationDeploymentType.getQueue() != null) {
                if (applicationDeploymentType.getQueue().getQueueName() != null) {
                    jobDescriptor.setQueueName(applicationDeploymentType.getQueue().getQueueName());
                }
            }
            jobDescriptor.setOwner(((PBSCluster) cluster).getServerInfo().getUserName());
            TaskDetails taskData = jobExecutionContext.getTaskData();
            if (taskData != null && taskData.isSetTaskScheduling()) {
                ComputationalResourceScheduling computionnalResource = taskData.getTaskScheduling();
                if (computionnalResource.getNodeCount() > 0) {
                    jobDescriptor.setNodes(computionnalResource.getNodeCount());
                }
                if (computionnalResource.getComputationalProjectAccount() != null) {
                    jobDescriptor.setAcountString(computionnalResource.getComputationalProjectAccount());
                }
                if (computionnalResource.getQueueName() != null) {
                    jobDescriptor.setQueueName(computionnalResource.getQueueName());
                }
                if (computionnalResource.getTotalCPUCount() > 0) {
                    jobDescriptor.setProcessesPerNode(computionnalResource.getTotalCPUCount());
                }
                if (computionnalResource.getWallTimeLimit() > 0) {
                    jobDescriptor.setMaxWallTime(String.valueOf(computionnalResource.getWallTimeLimit()));
                }
            }

        }
        return jobDescriptor;
    }

}
