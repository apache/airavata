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
package org.apache.airavata.gfac.gsissh.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.security.TokenizedMyProxyAuthInfo;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.api.job.JobManagerConfiguration;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class GFACGSISSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACGSISSHUtils.class);

    public static final String PBS_JOB_MANAGER = "pbs";
    public static final String SLURM_JOB_MANAGER = "slurm";
    public static final String SUN_GRID_ENGINE_JOB_MANAGER = "sge";

    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        HostDescription registeredHost = jobExecutionContext.getApplicationContext().getHostDescription();
        if (registeredHost.getType() instanceof GlobusHostType || registeredHost.getType() instanceof UnicoreHostType
                || registeredHost.getType() instanceof SSHHostType) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if (registeredHost.getType() instanceof GsisshHostType) {
            String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
            RequestData requestData = new RequestData(ServerSettings.getDefaultUserGateway());
            requestData.setTokenId(credentialStoreToken);
            PBSCluster pbsCluster = null;
            GSISecurityContext context = null;
            try {
                TokenizedMyProxyAuthInfo tokenizedMyProxyAuthInfo = new TokenizedMyProxyAuthInfo(requestData);
                GsisshHostType gsisshHostType = (GsisshHostType) registeredHost.getType();
                ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress(),
                        gsisshHostType.getPort());

                JobManagerConfiguration jConfig = null;
                String installedParentPath = ((HpcApplicationDeploymentType)
                        jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath();
                String jobManager = ((GsisshHostType) registeredHost.getType()).getJobManager();
                if (jobManager == null) {
                    logger.error("No Job Manager is configured, so we are picking pbs as the default job manager");
                    jConfig = CommonUtils.getPBSJobManager(installedParentPath);
                } else {
                    if (PBS_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                        jConfig = CommonUtils.getPBSJobManager(installedParentPath);
                    } else if (SLURM_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                        jConfig = CommonUtils.getSLURMJobManager(installedParentPath);
                    } else if (SUN_GRID_ENGINE_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                        jConfig = CommonUtils.getSGEJobManager(installedParentPath);
                    }
                }
                pbsCluster = new PBSCluster(serverInfo, tokenizedMyProxyAuthInfo, jConfig);
                context = new GSISecurityContext(tokenizedMyProxyAuthInfo.getCredentialReader(), requestData,pbsCluster);
            } catch (Exception e) {
                throw new GFacException("An error occurred while creating GSI security context", e);
            }
            jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, context);
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
        int i = random.nextInt(Integer.MAX_VALUE); // We always set the job name
        jobDescriptor.setJobName(String.valueOf(i));
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
