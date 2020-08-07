/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.security.authzcache.*;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.custos.group.management.client.GroupManagementClient;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.apache.custos.identity.service.User;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.user.management.client.UserManagementClient;
import org.apache.custos.user.profile.service.GetAllGroupsResponse;
import org.apache.custos.user.profile.service.Group;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.thrift.TException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeyCloakSecurityManager implements AiravataSecurityManager {
    private final static Logger logger = LoggerFactory.getLogger(KeyCloakSecurityManager.class);

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

    private SharingManagementClient sharingManagementClient;
    private GroupManagementClient groupManagementClient;
    private UserManagementClient userManagementClient;
    private IdentityManagementClient identityManagementClient;

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

    public KeyCloakSecurityManager() throws AiravataSecurityException {
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
                "/airavata/searchExperimentsByCreationTime|/airavata/searchExperiments|" +
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
            this.sharingManagementClient = CustosUtils.getCustosClientProvider().getSharingManagementClient();
            this.userManagementClient = CustosUtils.getCustosClientProvider().getUserManagementClient();
            this.groupManagementClient = CustosUtils.getCustosClientProvider().getGroupManagementClient();
            this.identityManagementClient = CustosUtils.getCustosClientProvider().getIdentityManagementClient();

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
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
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
                    GatewayGroupMembership gatewayGroupMembership = getGatewayGroupMembership(authzToken, subject, accessToken, gatewayId);
                    boolean authorizationDecision = hasPermission(gatewayGroupMembership, action);
                    //cache the authorization decision
                    long currentTime = System.currentTimeMillis();
                    //TODO get the actual token expiration time
                    authzCacheManager.addToAuthzCache(new AuthzCacheIndex(subject, gatewayId, accessToken, action),
                            new AuthzCacheEntry(authorizationDecision, currentTime + 1000 * 60 * 60, currentTime));
                    return authorizationDecision;
                } else {
                    //undefined status returned from the authz cache manager
                    throw new AiravataSecurityException("Error in reading from the authorization cache.");
                }
            } else {
                GatewayGroupMembership gatewayGroupMembership = getGatewayGroupMembership(authzToken, subject, accessToken, gatewayId);
                return hasPermission(gatewayGroupMembership, action);
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

    @Override
    public AuthzToken getUserManagementServiceAccountAuthzToken(String gatewayId) throws AiravataSecurityException {
        try {
            initServiceClients();
            Gateway gateway = registryServiceClient.getGateway(gatewayId);
            String tokenURL = getTokenEndpoint(gatewayId);
            JSONObject clientCredentials = getClientCredentials(tokenURL, gateway.getOauthClientId(), gateway.getOauthClientSecret());
            String accessToken = clientCredentials.getString("access_token");
            AuthzToken authzToken = new AuthzToken(accessToken);
            authzToken.putToClaimsMap(Constants.GATEWAY_ID, gatewayId);
            authzToken.putToClaimsMap(Constants.USER_NAME, gateway.getOauthClientId());
            return authzToken;
        } catch (Exception e) {
            throw new AiravataSecurityException(e);
        } finally {
            closeServiceClients();
        }
    }

    @Override
    public UserInfo getUserInfoFromAuthzToken(AuthzToken authzToken) throws AiravataSecurityException {
        try {
            initServiceClients();
            final String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            final String token = authzToken.getAccessToken();
            return getUserInfo(authzToken);
        } catch (Exception e) {
            throw new AiravataSecurityException(e);
        } finally {
            closeServiceClients();
        }
    }

    private UserInfo getUserInfo(AuthzToken authToken) throws Exception {
        String userId = authToken.getClaimsMap().get(Constants.USER_NAME);
        String custosId = authToken.getClaimsMap().get(Constants.CUSTOS_ID);
        User user = identityManagementClient.getUser(custosId, userId, authToken.getAccessToken());
        return new UserInfo()
                .setSub(user.getSub())
                .setFullName(user.getFullName())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmailAddress(user.getEmailAddress())
                .setUsername(user.getUsername());
    }

    private GatewayGroupMembership getGatewayGroupMembership(AuthzToken authzToken, String username, String token, String gatewayId) throws Exception {
        validateToken(authzToken);
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        GatewayGroups gatewayGroups = getGatewayGroups(gatewayId, custosId);

        GetAllGroupsResponse getAllGroupsResponse =  groupManagementClient.getAllGroupsOfUser(custosId, username);

        List<String> userGroupIds = new ArrayList<>();

        for (Group gr: getAllGroupsResponse.getGroupsList()) {
            userGroupIds.add(gr.getId());
        }

        GatewayGroupMembership gatewayGroupMembership = new GatewayGroupMembership();
        gatewayGroupMembership.setInAdminsGroup(userGroupIds.contains(gatewayGroups.getAdminsGroupId()));
        gatewayGroupMembership.setInReadOnlyAdminsGroup(userGroupIds.contains(gatewayGroups.getReadOnlyAdminsGroupId()));
        return gatewayGroupMembership;
    }

    private GatewayGroups getGatewayGroups(String gatewayId, String custosId) throws Exception {
        if (registryServiceClient.isGatewayGroupsExists(gatewayId)) {
            return registryServiceClient.getGatewayGroups(gatewayId);
        } else {
            return GatewayGroupsInitializer.initializeGatewayGroups(gatewayId, custosId);
        }
    }

    private void validateToken(AuthzToken authzToken) throws Exception {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        UserInfo userInfo = getUserInfo(authzToken);
        if (!username.equals(userInfo.getUsername())) {
            throw new AiravataSecurityException("Subject name and username for the token doesn't match");
        }
    }



    private String getOpenIDConfigurationUrl(String realm) throws ApplicationSettingsException {
        return ServerSettings.getRemoteIDPServiceUrl() + "/realms/" + realm + "/.well-known/openid-configuration";
    }

    public String getFromUrl(String urlToRead, String token) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        if (token != null) {
            String bearerAuth = "Bearer " + token;
            conn.setRequestProperty("Authorization", bearerAuth);
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }



    private String getTokenEndpoint(String gatewayId) throws Exception {
        String openIdConnectUrl = getOpenIDConfigurationUrl(gatewayId);
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        return openIdConnectConfig.getString("token_endpoint");
    }

    private JSONObject getClientCredentials(String tokenURL, String clientId, String clientSecret) throws ApplicationSettingsException, AiravataSecurityException {

        CloseableHttpClient httpClient = HttpClients.createSystem();

        HttpPost httpPost = new HttpPost(tokenURL);
        String encoded = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject tokenInfo = new JSONObject(responseBody);
                return tokenInfo;
            } finally {
                response.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    }

    private void closeServiceClients() {
        if (registryServiceClient != null) {
            ThriftUtils.close(registryServiceClient);
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




    public static void main(String[] args) throws AiravataSecurityException, ApplicationSettingsException {
        ServerSettings.setSetting("trust.store", "./modules/configuration/server/src/main/resources/client_truststore.jks");
        ServerSettings.setSetting("trust.store.password", "airavata");
        KeyCloakSecurityManager keyCloakSecurityManager = new KeyCloakSecurityManager();
        final String tokenURL = "...";
        final String clientId = "...";
        final String clientSecret = "...";
        JSONObject jsonObject = keyCloakSecurityManager.getClientCredentials(tokenURL, clientId, clientSecret);
        System.out.println("access_token=" + jsonObject.getString("access_token"));
    }
}
