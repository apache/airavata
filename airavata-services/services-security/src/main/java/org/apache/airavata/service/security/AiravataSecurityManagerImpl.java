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
package org.apache.airavata.service.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.tenant.Tenant;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.airavata.service.security.authzcache.AuthzCacheEntry;
import org.apache.airavata.service.security.authzcache.AuthzCacheIndex;
import org.apache.airavata.service.security.authzcache.AuthzCacheManager;
import org.apache.airavata.service.security.authzcache.AuthzCacheManagerFactory;
import org.apache.airavata.service.security.authzcache.AuthzCachedStatus;
import org.apache.airavata.service.security.utils.ThriftCustosDataModelConversion;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.custos.authentication.cpi.CustosAuthenticationService;
import org.apache.custos.client.authentication.service.AuthenticationServiceClient;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AiravataSecurityManagerImpl implements AiravataSecurityManager {
    private final static Logger logger = LoggerFactory.getLogger(AiravataSecurityManagerImpl.class);

    private HashMap<String, String> rolePermissionConfig = new HashMap<>();

    // Methods that users user to manage their user resource profile
    private final static String USER_RESOURCE_PROFILE_USER_METHODS = "/airavata/registerUserResourceProfile|/airavata/getUserResourceProfile" +
            "|/airavata/updateUserResourceProfile|/airavata/deleteUserResourceProfile|/airavata/addUserComputeResourcePreference" +
            "|/airavata/addUserStoragePreference|/airavata/getUserComputeResourcePreference|/airavata/getUserStoragePreference" +
            "|/airavata/getAllUserComputeResourcePreferences|/airavata/getAllUserStoragePreferences" +
            "|/airavata/updateUserComputeResourcePreference|/airavata/updateUserStoragePreference" +
            "|/airavata/deleteUserComputeResourcePreference|/airavata/deleteUserStoragePreference" +
            "|/airavata/generateAndRegisterSSHKeys|/airavata/getAllCredentialSummaryForUsersInGateway" +
            "|/airavata/deleteSSHPubKey|/airavata/isUserResourceProfileExists";
    private final static String SHARING_RESOURCE_METHODS = "/airavata/shareResourceWithUsers|/airavata/revokeSharingOfResourceFromUsers" +
            "|/airavata/shareResourceWithGroups|/airavata/revokeSharingOfResourceFromGroups|/airavata/getAllAccessibleUsers" +
            "|/airavata/getAllAccessibleGroups|/airavata/userHasAccess|/airavata/getAllDirectlyAccessibleUsers" +
            "|/airavata/getAllDirectlyAccessibleGroups";
    private final static String SSH_ACCOUNT_PROVISIONER_METHODS =
            "/airavata/getSSHAccountProvisioners|/airavata/doesUserHaveSSHAccount|/airavata" +
                    "/setupUserComputeResourcePreferencesForSSH|" +
                    // getGatewayResourceProfile is needed to look up whether ssh account provisioning is
                    // configured for a gateway's compute resource preference
                    "/airavata/getGatewayResourceProfile";
    // These methods are protected by sharing registry authorization
    private final static String GROUP_RESOURCE_PROFILE_METHODS =
            "/airavata/createGroupResourceProfile|/airavata/updateGroupResourceProfile|/airavata/getGroupResourceProfile" +
                    "|/airavata/removeGroupResourceProfile|/airavata/getGroupResourceList|/airavata/removeGroupComputePrefs" +
                    "|/airavata/removeGroupComputeResourcePolicy|/airavata/removeGroupBatchQueueResourcePolicy" +
                    "|/airavata/getGroupComputeResourcePreference|/airavata/getGroupComputeResourcePolicy" +
                    "|/airavata/getBatchQueueResourcePolicy|/airavata/getGroupComputeResourcePrefList" +
                    "|/airavata/getGroupBatchQueueResourcePolicyList|/airavata/getGroupComputeResourcePolicyList";
    // These methods are protected by sharing registry authorization
    private final static String APPLICATION_DEPLOYMENT_METHODS =
            "/airavata/registerApplicationDeployment|/airavata/getApplicationDeployment|/airavata/updateApplicationDeployment" +
                    "|/airavata/deleteApplicationDeployment|/airavata/getAllApplicationDeployments|/airavata/getAccessibleApplicationDeployments" +
                    "|/airavata/getApplicationDeploymentsForAppModuleAndGroupResourceProfile";
    private final static String APPLICATION_MODULE_METHODS = "/airavata/getAccessibleAppModules";
    private final static String CREDENTIAL_TOKEN_METHODS = "/airavata/getCredentialSummary|/airavata/getAllCredentialSummaries|/airavata/generateAndRegisterSSHKeys|/airavata/registerPwdCredential|/airavata/deleteSSHPubKey|/airavata/deletePWDCredential";
    // Misc. other methods needed for group based authorization
    private final static String GROUP_BASED_AUTH_METHODS = "/airavata/getGatewayGroups";

    private RegistryService.Client registryServiceClient = null;
    private SharingRegistryService.Client sharingRegistryServiceClient = null;
    private CustosAuthenticationService.Client custosAuthenticationServiceClient = null;

    private static class GatewayGroupMembership {
        private boolean inAdminsGroup = false;
        private boolean inReadOnlyAdminsGroup = false;

        public boolean isInAdminsGroup() {
            return inAdminsGroup;
        }

        public void setInAdminsGroup(boolean inAdminsGroup) {
            this.inAdminsGroup = inAdminsGroup;
        }

        public boolean isInReadOnlyAdminsGroup() {
            return inReadOnlyAdminsGroup;
        }

        public void setInReadOnlyAdminsGroup(boolean inReadOnlyAdminsGroup) {
            this.inReadOnlyAdminsGroup = inReadOnlyAdminsGroup;
        }
    }

    public AiravataSecurityManagerImpl() throws AiravataSecurityException {
        rolePermissionConfig.put("admin", "/airavata/.*");
        rolePermissionConfig.put("gateway-provider", "/airavata/.*");
        rolePermissionConfig.put("admin-read-only", "/airavata/getSSHPubKey|/airavata/getAllGatewaySSHPubKeys" +
                "|/airavata/getAllGatewayPWDCredentials|/airavata/getApplicationModule|/airavata/getAllAppModules" +
                "|/airavata/getApplicationDeployment|/airavata/getAllApplicationDeployments|/airavata/getAppModuleDeployedResources" +
                "|/airavata/getStorageResource|/airavata/getAllStorageResourceNames|/airavata/getSCPDataMovement" +
                "|/airavata/getUnicoreDataMovement|/airavata/getGridFTPDataMovement|/airavata/getResourceJobManager" +
                "|/airavata/deleteResourceJobManager|/airavata/getGatewayResourceProfile|/airavata/getGatewayComputeResourcePreference" +
                "|/airavata/getGatewayStoragePreference|/airavata/getAllGatewayComputeResourcePreferences" +
                "|/airavata/getAllGatewayStoragePreferences|/airavata/getAllGatewayResourceProfiles|/airavata/getAPIVersion" +
                "|/airavata/getNotification|/airavata/getAllNotifications|/airavata/createProject|/airavata/updateProject" +
                "|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|/airavata/searchProjectsByProjectName" +
                "|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|/airavata/searchExperimentsByDesc" +
                "|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|/airavata/searchExperimentsByCreationTime" +
                "|/airavata/searchExperiments|/airavata/getExperimentStatistics|/airavata/getExperimentsInProject" +
                "|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|/airavata/getExperiment" +
                "|/airavata/getDetailedExperimentTree|/airavata/updateExperiment|/airavata/updateExperimentConfiguration" +
                "|/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment" +
                "|/airavata/getExperimentStatus|/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs" +
                "|/airavata/getJobStatuses|/airavata/getJobDetails|/airavata/cloneExperiment|/airavata/terminateExperiment" +
                "|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|/airavata/getAllApplicationInterfaces" +
                "|/airavata/getApplicationInputs|/airavata/getApplicationOutputs|/airavata/getAvailableAppInterfaceComputeResources" +
                "|/airavata/getComputeResource|/airavata/getAllComputeResourceNames|/airavata/getWorkflow|/airavata/getWorkflowTemplateId" +
                "|/airavata/isWorkflowExistWithName|/airavata/registerDataProduct|/airavata/getDataProduct|/airavata/registerReplicaLocation" +
                "|/airavata/getParentDataProduct|/airavata/getChildDataProducts|/airavata/getAllAccessibleUsers" +
                "|/airavata/getExperimentByAdmin|/airavata/cloneExperimentByAdmin|/airavata/getAllCredentialSummaryForGateway" +
                "|" + USER_RESOURCE_PROFILE_USER_METHODS + "|/airavata/getAllUserResourceProfiles" +
                "|" + SHARING_RESOURCE_METHODS + "|/airavata/getGateway|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|" + GROUP_RESOURCE_PROFILE_METHODS +
                "|" + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|" + APPLICATION_MODULE_METHODS +
                "|" + CREDENTIAL_TOKEN_METHODS);
        rolePermissionConfig.put("gateway-user", "/airavata/getAPIVersion|/airavata/getNotification|/airavata/getAllNotifications|" +
                "/airavata/createProject|/airavata/updateProject|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|" +
                "/airavata/searchProjectsByProjectName|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|" +
                "/airavata/searchExperimentsByDesc|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|" +
                "/airavata/searchExperimentsByCreationTime|/airavata/searchExperiments|/airavata/getExperimentStatistics|" +
                "/airavata/getExperimentsInProject|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|" +
                "/airavata/getExperiment|/airavata/getDetailedExperimentTree|/airavata/updateExperiment|/airavata/updateExperimentConfiguration|" +
                "/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment|/airavata/getExperimentStatus|" +
                "/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs|/airavata/getJobStatuses|/airavata/getJobDetails|" +
                "/airavata/cloneExperiment|/airavata/terminateExperiment|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|" +
                "/airavata/getAllApplicationInterfaces|/airavata/getApplicationInputs|/airavata/getApplicationOutputs|" +
                "/airavata/getAvailableAppInterfaceComputeResources|/airavata/getComputeResource|/airavata/getAllComputeResourceNames|" +
                "/airavata/getWorkflow|/airavata/getWorkflowTemplateId|/airavata/isWorkflowExistWithName|/airavata/registerDataProduct|" +
                "/airavata/getDataProduct|/airavata/registerReplicaLocation|/airavata/getParentDataProduct|/airavata/getChildDataProducts|" +
                "/airavata/getAllAccessibleUsers|/airavata/getAllApplicationDeployments|/airavata/getAllAppModules|/airavata/getApplicationModule|" + USER_RESOURCE_PROFILE_USER_METHODS + "|" +
                SHARING_RESOURCE_METHODS + "|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|" + GROUP_RESOURCE_PROFILE_METHODS +
                "|" + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|" + APPLICATION_MODULE_METHODS +
                "|" + CREDENTIAL_TOKEN_METHODS);

        initializeSecurityInfra();
    }

    /**
     * Implement this method in your SecurityManager to perform necessary initializations at the server startup.
     *
     * @throws AiravataSecurityException
     */
    @Override
    public void initializeSecurityInfra() throws AiravataSecurityException {
        try {
            //initialize SSL context with the trust store that contains the public cert of WSO2 Identity Server.
            TrustStoreManager trustStoreManager = new TrustStoreManager();
            trustStoreManager.initializeTrustStoreManager(ServerSettings.getTrustStorePath(),
                    ServerSettings.getTrustStorePassword());
        } catch (Exception e) {
            throw new AiravataSecurityException(e.getMessage(), e);
        }

    }

    /**
     * Implement this method with the user authentication/authorization logic in your SecurityManager.
     *
     * @param authzToken : this includes OAuth token and user's claims
     * @param metaData   : this includes other meta data needed for security enforcements.
     * @return
     * @throws AiravataSecurityException
     */
    @Override
    public boolean isUserAuthorized(AuthzToken authzToken, Map<String, String> metaData) throws AiravataSecurityException {
        String subject = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String accessToken = authzToken.getAccessToken();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String action = "/airavata/" + metaData.get(Constants.API_METHOD_NAME);
        try {
            if (!ServerSettings.isAPISecured()) {
                return true;
            }
            initServiceClients();

            if (ServerSettings.isAuthzCacheEnabled()) {
                //obtain an instance of AuthzCacheManager implementation.
                AuthzCacheManager authzCacheManager = AuthzCacheManagerFactory.getAuthzCacheManager();

                //check in the cache
                AuthzCachedStatus authzCachedStatus = authzCacheManager.getAuthzCachedStatus(
                        new AuthzCacheIndex(subject, gatewayId, accessToken, action));

                if (AuthzCachedStatus.AUTHORIZED.equals(authzCachedStatus)) {
                    logger.debug("Authz decision for: (" + subject + ", " + accessToken + ", " + action + ") is retrieved from cache.");
                    return true;
                } else if (AuthzCachedStatus.NOT_AUTHORIZED.equals(authzCachedStatus)) {
                    logger.debug("Authz decision for: (" + subject + ", " + accessToken + ", " + action + ") is retrieved from cache.");
                    return false;
                } else if (AuthzCachedStatus.NOT_CACHED.equals(authzCachedStatus)) {
                    logger.debug("Authz decision for: (" + subject + ", " + accessToken + ", " + action + ") is not in the cache. " +
                            "Generating decision based on group membership.");
                    GatewayGroupMembership gatewayGroupMembership = getGatewayGroupMembership(subject, accessToken, gatewayId);
                    boolean isAuthenticated = authenticateUser(authzToken);
                    boolean authorizationDecision = hasPermission(gatewayGroupMembership, action);
                    //cache the authorization decision
                    long currentTime = System.currentTimeMillis();
                    //TODO get the actual token expiration time
                    authzCacheManager.addToAuthzCache(new AuthzCacheIndex(subject, gatewayId, accessToken, action),
                            new AuthzCacheEntry(authorizationDecision, currentTime + 1000 * 60 * 60, currentTime));
                    return authorizationDecision && isAuthenticated;
                } else {
                    //undefined status returned from the authz cache manager
                    throw new AiravataSecurityException("Error in reading from the authorization cache.");
                }
            } else {
                boolean isAuthenticated = authenticateUser(authzToken);
                GatewayGroupMembership gatewayGroupMembership = getGatewayGroupMembership(subject, accessToken, gatewayId);
                return isAuthenticated && hasPermission(gatewayGroupMembership, action);
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Missing or invalid application setting.", e);
            throw new AiravataSecurityException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error occurred while checking if user: " + subject + " is authorized for action: " + action + " in gateway: " + gatewayId, e);
            throw new AiravataSecurityException(e.getMessage(), e);
        } finally {
            closeServiceClients();
        }
    }

    private GatewayGroupMembership getGatewayGroupMembership(String username, String token, String gatewayId) throws Exception {
        GatewayGroups gatewayGroups = getGatewayGroups(gatewayId);
        List<UserGroup> userGroups = sharingRegistryServiceClient.getAllMemberGroupsForUser(gatewayId, username + "@" + gatewayId);
        List<String> userGroupIds = userGroups.stream().map(g -> g.getGroupId()).collect(Collectors.toList());
        GatewayGroupMembership gatewayGroupMembership = new GatewayGroupMembership();
        gatewayGroupMembership.setInAdminsGroup(userGroupIds.contains(gatewayGroups.getAdminsGroupId()));
        gatewayGroupMembership.setInReadOnlyAdminsGroup(userGroupIds.contains(gatewayGroups.getReadOnlyAdminsGroupId()));
        return gatewayGroupMembership;
    }

    private GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        if (registryServiceClient.isGatewayGroupsExists(gatewayId)) {
            return registryServiceClient.getGatewayGroups(gatewayId);
        } else {
            return GatewayGroupsInitializer.initializeGatewayGroups(gatewayId);
        }
    }

    private boolean hasPermission(GatewayGroupMembership gatewayGroupMembership, String apiMethod) {

        // Note: as a stopgap solution, until all resources are secured with group-based authorization, map the Admins
        // and Read Only Admins groups to the corresponding roles
        final String role;
        if (gatewayGroupMembership.isInAdminsGroup()) {
            return true;
        } else if (gatewayGroupMembership.isInReadOnlyAdminsGroup()) {
            role = "admin-read-only";
        } else {
            // If not in Admins or Read Only Admins groups, treat as a gateway-user
            role = "gateway-user";
        }
        Pattern pattern = Pattern.compile(this.rolePermissionConfig.get(role));
        Matcher matcher = pattern.matcher(apiMethod);
        return matcher.matches();
    }

    private void initServiceClients() throws TException, ApplicationSettingsException {
        registryServiceClient = getRegistryServiceClient();
        sharingRegistryServiceClient = getSharingRegistryServiceClient();
        custosAuthenticationServiceClient = getCustosAuthenticationClient();
    }

    private void closeServiceClients() {
        if (registryServiceClient != null) {
            ThriftUtils.close(registryServiceClient);
        }
        if (sharingRegistryServiceClient != null) {
            ThriftUtils.close(sharingRegistryServiceClient);
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

    private SharingRegistryService.Client getSharingRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getSharingRegistryPort());
        final String serverHost = ServerSettings.getSharingRegistryHost();
        try {
            return SharingRegistryServiceClientFactory.createSharingRegistryClient(serverHost, serverPort);
        } catch (SharingRegistryException e) {
            throw new TException("Unable to create sharing registry client...", e);
        }
    }

    private CustosAuthenticationService.Client getCustosAuthenticationClient() throws TException{
        try {
            String serverHost = ServerSettings.getCustosAuthenticationServerHost();
            String serverPort = ServerSettings.getCustosAuthenticationServerPort();
            CustosAuthenticationService.Client custosAuthenticationClient = AuthenticationServiceClient.createAuthenticationServiceClient(serverHost, Integer.parseInt(serverPort));
            return custosAuthenticationClient;
        }catch (TException | ApplicationSettingsException e) {
            throw new TException("unable to create custos authentication client");
        }
    }

    private boolean authenticateUser(AuthzToken authzToken) throws TException{
        try {
            return custosAuthenticationServiceClient.isUserAuthenticated(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken));
        }catch (TException e) {
            throw new TException("Could not authenticate user", e);
        }
    }
    @Override
    public AuthzToken getUserManagementServiceAccountAuthzToken(String gatewayId) throws AiravataSecurityException{
        try{
            return ThriftCustosDataModelConversion.getAuthzToken(custosAuthenticationServiceClient.getUserManagementServiceAccountAuthzToken(null,gatewayId));
        }catch(TException e) {
            throw new AiravataSecurityException("Could not get user management service account authz token", e);
        }
    }
//    public static void main(String[] args) throws AiravataSecurityException, ApplicationSettingsException {
//        ServerSettings.setSetting("trust.store", "./modules/configuration/server/src/main/resources/client_truststore.jks");
//        ServerSettings.setSetting("trust.store.password", "airavata");
//        AiravataSecurityManagerImpl airavataSecurityManagerImpl = new AiravataSecurityManagerImpl();
//        final String tokenURL = "...";
//        final String clientId = "...";
//        final String clientSecret = "...";
//        JSONObject jsonObject = airavataSecurityManagerImpl.getClientCredentials(tokenURL, clientId, clientSecret);
//        System.out.println("access_token=" + jsonObject.getString("access_token"));
//    }
}
