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

package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IamAdminServicesHandler implements IamAdminServices.Iface {

    private final static Logger logger = LoggerFactory.getLogger(IamAdminServicesHandler.class);


    @Override
    public String getAPIVersion(AuthzToken authzToken) throws IamAdminServicesException, AuthorizationException {
        try {
            return iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
        } catch (Exception ex) {
            logger.error("Error getting API version, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting API version, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway, PasswordCredential isSuperAdminCredentials) throws IamAdminServicesException, AuthorizationException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try{
            keycloakclient.addTenant(isSuperAdminCredentials,gateway);
            if(!keycloakclient.createTenantAdminAccount(isSuperAdminCredentials,gateway)){
                logger.error("Admin account creation failed !!, please refer error logs for reason");
            }
            Gateway gatewayWithIdAndSecret = keycloakclient.configureClient(isSuperAdminCredentials,gateway);
            return gatewayWithIdAndSecret;
        } catch (IamAdminServicesException ex){
            logger.error("Gateway Setup Failed, reason: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    //ToDo: Will only be secure when using SSL between PGA and Airavata
    @Override
    @SecurityCheck
    public boolean registerUser(AuthzToken authzToken, UserProfile userDetails, PasswordCredential isRealmAdminCredentials, String newPassword) throws IamAdminServicesException, AuthorizationException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try{
            if(keycloakclient.createUser(isRealmAdminCredentials,userDetails,newPassword))
                return true;
            else
                return false;
        } catch (IamAdminServicesException ex){
            logger.error("Error while registering user into Identity Server, reason: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(AuthzToken authzToken, UserProfile userDetails, PasswordCredential isRealmAdminCredentials) throws IamAdminServicesException, AuthorizationException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try{
            if(keycloakclient.enableUserAccount(isRealmAdminCredentials,userDetails))
                return true;
            else
                return false;
        } catch (IamAdminServicesException ex){
            logger.error("Error while enabling user account, reason: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(AuthzToken authzToken, String tenantId, String username, String newPassword) throws IamAdminServicesException, AuthorizationException, TException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try{
            if(keycloakclient.resetUserPassword(authzToken.getAccessToken(), tenantId, username, newPassword))
                return true;
            else
                return false;
        } catch (IamAdminServicesException ex){
            logger.error("Error while resetting user password in Identity Server, reason: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> findUsers(AuthzToken authzToken, String gatewayID, String email, String userId, PasswordCredential isRealmAdminCredentials) throws IamAdminServicesException, AuthorizationException, TException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try{
            return keycloakclient.findUser(isRealmAdminCredentials,gatewayID,email,userId);
        } catch (IamAdminServicesException ex){
            logger.error("Error while retrieving users from Identity Server, reason: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException, AuthorizationException, TException {

        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        try {
            String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (!gatewayId.equals(userDetails.getGatewayId())) {
                throw new IamAdminServicesException("gatewayId in user profile doesn't match authorization token!");
            }
            if (!username.equals(userDetails.getUserId())) {
                throw new IamAdminServicesException("userId in user profile doesn't match authorization token!");
            }
            GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(gatewayId);

            CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
            PasswordCredential credential = csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
            keycloakclient.updateUserProfile(credential, gatewayId, username, userDetails);
        } catch (ApplicationSettingsException e) {
            throw new IamAdminServicesException("Unable to create service clients. Reason: " + e.getMessage());
        }
    }

    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }

    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }
}
