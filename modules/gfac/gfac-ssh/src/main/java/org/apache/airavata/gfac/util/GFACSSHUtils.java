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
package org.apache.airavata.gfac.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class GFACSSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACSSHUtils.class);

    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        HostDescription registeredHost = jobExecutionContext.getApplicationContext().getHostDescription();
        if (registeredHost.getType() instanceof GlobusHostType || registeredHost.getType() instanceof UnicoreHostType
                || registeredHost.getType() instanceof GsisshHostType) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if (registeredHost.getType() instanceof SSHHostType) {
            Properties configurationProperties = ServerSettings.getProperties();
            String sshUserName = configurationProperties.getProperty(Constants.SSH_USER_NAME);
            String sshPrivateKey = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY);
            String sshPrivateKeyPass = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY_PASS);
            String sshPassword = configurationProperties.getProperty(Constants.SSH_PASSWORD);
            String sshPublicKey = configurationProperties.getProperty(Constants.SSH_PUBLIC_KEY);
            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
            AuthenticationInfo authenticationInfo = null;
            // we give higher preference to the password over keypair ssh authentication
            if (sshPassword != null) {
                authenticationInfo = new DefaultPasswordAuthenticationInfo(sshPassword);
            } else {
                authenticationInfo = new DefaultPublicKeyFileAuthentication(sshPublicKey, sshPrivateKey, sshPrivateKeyPass);
            }
            ServerInfo serverInfo = new ServerInfo(sshUserName, registeredHost.getType().getHostAddress());

            Cluster pbsCluster = null;
            try {
                String installedParentPath = "/";
                if (((SSHHostType) registeredHost.getType()).getHpcResource()) {
                    installedParentPath = ((HpcApplicationDeploymentType)
                            jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath();
                }
                pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                        CommonUtils.getPBSJobManager(installedParentPath));
            } catch (SSHApiException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            sshSecurityContext.setPbsCluster(pbsCluster);
            sshSecurityContext.setUsername(sshUserName);
            jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, sshSecurityContext);
        }
    }

}
