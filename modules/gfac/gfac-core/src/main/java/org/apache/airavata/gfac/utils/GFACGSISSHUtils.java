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
package org.apache.airavata.gfac.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobManagerConfiguration;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GFACGSISSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACGSISSHUtils.class);

    public static final String PBS_JOB_MANAGER = "pbs";
    public static final String SLURM_JOB_MANAGER = "slurm";
    public static final String SUN_GRID_ENGINE_JOB_MANAGER = "sge";

    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        RequestData requestData = new RequestData("default");
        GSISecurityContext context = null;
        try {
            //todo fix this
            context = new GSISecurityContext(null, requestData);
        } catch (Exception e) {
            throw new GFacException("An error occurred while creating GSI security context", e);
        }
        HostDescription registeredHost = jobExecutionContext.getApplicationContext().getHostDescription();
        if (registeredHost.getType() instanceof GlobusHostType || registeredHost.getType() instanceof UnicoreHostType
                || registeredHost.getType() instanceof SSHHostType) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if (registeredHost.getType() instanceof GsisshHostType) {
            GSIAuthenticationInfo authenticationInfo
                    = new MyProxyAuthenticationInfo(requestData.getMyProxyUserName(), requestData.getMyProxyPassword(), requestData.getMyProxyServerUrl(),
                    requestData.getMyProxyPort(), requestData.getMyProxyLifeTime(), System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY));
            GsisshHostType gsisshHostType = (GsisshHostType) registeredHost.getType();
            ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress(),
                    gsisshHostType.getPort());

            Cluster pbsCluster = null;
            try {
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
                pbsCluster = new PBSCluster(serverInfo, authenticationInfo, jConfig);
            } catch (SSHApiException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            context.setPbsCluster(pbsCluster);
        }

    }
}
