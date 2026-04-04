/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.service;

import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SSHAccountService {

    private static final Logger logger = LoggerFactory.getLogger(SSHAccountService.class);

    private final CredentialStoreService credentialHandler;
    private final SSHAccountManager sshAccountManager;

    public SSHAccountService(CredentialStoreService credentialHandler, SSHAccountManager sshAccountManager) {
        this.credentialHandler = credentialHandler;
        this.sshAccountManager = sshAccountManager;
    }

    public boolean doesUserHaveSSHAccount(RequestContext ctx, String computeResourceId, String userId)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return sshAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (Exception e) {
            String msg = "Error occurred while checking if [" + userId + "] has an SSH Account on [" + computeResourceId
                    + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }

    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            RequestContext ctx, String computeResourceId, String airavataCredStoreToken) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();

        SSHCredential sshCredential;
        try {
            sshCredential = credentialHandler.getSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Exception e) {
            String msg = "Error occurred while retrieving SSH Credential.";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }

        try {
            return sshAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Exception e) {
            String msg = "Error occurred while checking if setup of SSH account is complete for user [" + userId + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }

    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            RequestContext ctx, String computeResourceId, String userId, String airavataCredStoreToken)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();

        SSHCredential sshCredential;
        try {
            sshCredential = credentialHandler.getSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Exception e) {
            String msg = "Error occurred while retrieving SSH Credential.";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }

        try {
            return sshAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Exception e) {
            String msg = "Error occurred while automatically setting up SSH account for user [" + userId + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }
}
