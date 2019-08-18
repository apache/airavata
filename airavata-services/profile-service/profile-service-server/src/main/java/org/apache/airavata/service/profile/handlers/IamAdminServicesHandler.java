/**
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
 */
package org.apache.airavata.service.profile.handlers;

import java.util.List;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.utils.ThriftCustosDataModelConversion;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.airavata.service.profile.utils.CustosClientFactory.getCustosIamAdminServicesClient;

public class IamAdminServicesHandler implements IamAdminServices.Iface {

    private final static Logger logger = LoggerFactory.getLogger(IamAdminServicesHandler.class);
    private static org.apache.custos.profile.iam.admin.services.cpi.IamAdminServices.Client custosIamAdminServicesClient;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.IAM_ADMIN);

    public IamAdminServicesHandler() throws TException{
        custosIamAdminServicesClient = getCustosIamAdminServicesClient();
    }

    @Override
    public String getAPIVersion() throws TException {
        return iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway) throws IamAdminServicesException, AuthorizationException {
        try {
            // Load the tenant admin password stored in gateway request
            CredentialStoreService.Client credentialStoreClient = getCredentialStoreServiceClient();
            // Admin password token should already be stored under requested gateway's gatewayId
            PasswordCredential tenantAdminPasswordCredential = credentialStoreClient.getPasswordCredential(gateway.getIdentityServerPasswordToken(), gateway.getGatewayId());
            gateway  = ThriftCustosDataModelConversion.getGateway(custosIamAdminServicesClient.setUpGateway(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), ThriftCustosDataModelConversion.getCustosGateway(gateway), ThriftCustosDataModelConversion.getCustosPasswordCredentials(tenantAdminPasswordCredential)));
            if (gateway != null) {
                return gateway;
            }
            else {
                throw new TException("Gateway object is null");
            }
        } catch (TException|ApplicationSettingsException ex) {
            logger.error("Gateway Setup Failed, reason: " + ex.getMessage(), ex);
            IamAdminServicesException iamAdminServicesException = new IamAdminServicesException(ex.getMessage());
            throw iamAdminServicesException;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        return custosIamAdminServicesClient.isUsernameAvailable(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username);
    }

    //ToDo: Will only be secure when using SSL between PGA and Airavata
    @Override
    @SecurityCheck
    public boolean registerUser(AuthzToken authzToken, String username, String emailAddress, String firstName, String lastName, String newPassword) throws IamAdminServicesException, AuthorizationException {
        try {
            return custosIamAdminServicesClient.registerUser(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username, emailAddress, firstName, lastName, newPassword);
        } catch (TException ex) {
            String msg = "Error while registering user into Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException {
        try {
            UserProfile userProfile = ThriftCustosDataModelConversion.getUserProfile(custosIamAdminServicesClient.enableUser(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username));
            if (userProfile != null) {
                // Dispatch IAM_ADMIN service event for a new USER_PROFILE
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, userProfile);
                return true;
            } else {
                return false;
            }
        } catch (TException | AiravataException ex) {
            String msg = "Error while enabling user account, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return custosIamAdminServicesClient.isUserEnabled(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username);
        } catch (Exception ex) {
            String msg = "Error while checking if user account is enabled, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return custosIamAdminServicesClient.isUserEnabled(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username);
        } catch (Exception ex) {
            String msg = "Error while checking if user account exists, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public UserProfile getUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return ThriftCustosDataModelConversion.getUserProfile(custosIamAdminServicesClient.getUser(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username));
        } catch (Exception ex) {
            String msg = "Error while retrieving user profile from IAM backend, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return ThriftCustosDataModelConversion.getUserProfiles(custosIamAdminServicesClient.getUsers(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), offset,limit,search));
        } catch (Exception ex) {
            String msg = "Error while retrieving user profile from IAM backend, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return custosIamAdminServicesClient.resetUserPassword(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username, newPassword);
        } catch (TException ex) {
            String msg = "Error while resetting user password in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return ThriftCustosDataModelConversion.getUserProfiles(custosIamAdminServicesClient.findUsers(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), email, userId));
        } catch (TException ex) {
            String msg = "Error while retrieving users from Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException, AuthorizationException, TException {
        custosIamAdminServicesClient.updateUserProfile(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), ThriftCustosDataModelConversion.getCustosUserProfile(userDetails));
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        //TODO: not in custos
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        return keycloakclient.deleteUser(authzToken.getAccessToken(), gatewayId, username);
    }

    @Override
    @SecurityCheck
    @Deprecated
    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            return custosIamAdminServicesClient.addRoleToUser(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username, roleName, ThriftCustosDataModelConversion.getCustosPasswordCredentials(isRealmAdminCredentials));
        } catch (TException | ApplicationSettingsException ex) {
            String msg = "Error while adding role to user, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            return custosIamAdminServicesClient.removeRoleFromUser(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), username, roleName, ThriftCustosDataModelConversion.getCustosPasswordCredentials(isRealmAdminCredentials));
        } catch (TException | ApplicationSettingsException ex) {
            String msg = "Error while removing role from user, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName) throws IamAdminServicesException, AuthorizationException, TException {

        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            return ThriftCustosDataModelConversion.getUserProfiles(custosIamAdminServicesClient.getUsersWithRole(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), roleName, ThriftCustosDataModelConversion.getCustosPasswordCredentials(isRealmAdminCredentials)));
        } catch (Exception ex) {
            String msg = "Error while retrieving users with role, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    private PasswordCredential getTenantAdminPasswordCredential(String tenantId) throws TException, ApplicationSettingsException {

        GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(tenantId);

        CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
        return csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
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
