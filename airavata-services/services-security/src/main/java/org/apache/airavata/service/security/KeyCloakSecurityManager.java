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
package org.apache.airavata.service.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.airavata.service.security.authzcache.*;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            "|/airavata/deleteSSHPubKey";
    private final static String SHARING_RESOURCE_METHODS = "/airavata/shareResourceWithUsers|/airavata/revokeSharingOfResourceFromUsers|/airavata/getAllAccessibleUsers";
    private final static String SSH_ACCOUNT_PROVISIONER_METHODS =
            "/airavata/getSSHAccountProvisioners|/airavata/doesUserHaveSSHAccount|/airavata" +
                    "/setupUserComputeResourcePreferencesForSSH|" +
                    // getGatewayResourceProfile is needed to look up whether ssh account provisioning is
                    // configured for a gateway's compute resource preference
                    "/airavata/getGatewayResourceProfile";

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
                "|" + SHARING_RESOURCE_METHODS + "|/airavata/getGateway|" + SSH_ACCOUNT_PROVISIONER_METHODS);
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
                "/airavata/getAllAccessibleUsers|/airavata/getAllApplicationDeployments|" + USER_RESOURCE_PROFILE_USER_METHODS + "|" +
                SHARING_RESOURCE_METHODS + "|" + SSH_ACCOUNT_PROVISIONER_METHODS);

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
                            "Obtaining it from the authorization server.");
                    String[] roles = getUserRolesFromOAuthToken(subject, accessToken, gatewayId);
                    boolean authorizationDecision = hasPermission(roles, action);
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
                String[] roles = getUserRolesFromOAuthToken(subject, accessToken, gatewayId);
                return hasPermission(roles, action);
            }

        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
            throw new AiravataSecurityException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AiravataSecurityException(e.getMessage(), e);
        }
    }

    private String[] getUserRolesFromOAuthToken(String username, String token, String gatewayId) throws Exception {
        GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(gatewayId);
        String identityServerRealm = gwrp.getIdentityServerTenant();
        String openIdConnectUrl = getOpenIDConfigurationUrl(identityServerRealm);
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, token));
        String userInfoEndPoint = openIdConnectConfig.getString("userinfo_endpoint");
        JSONObject userInfo = new JSONObject(getFromUrl(userInfoEndPoint, token));
        if (!username.equals(userInfo.get("preferred_username"))) {
            throw new AiravataSecurityException("Subject name and username for the token doesn't match");
        }
        String userId = userInfo.getString("sub");

        String userRoleMappingUrl = ServerSettings.getRemoteIDPServiceUrl() + "/admin/realms/"
                + identityServerRealm + "/users/"
                + userId + "/role-mappings/realm";
        JSONArray roleMappings = new JSONArray(getFromUrl(userRoleMappingUrl, getAdminAccessToken(gatewayId)));
        String[] roles = new String[roleMappings.length()];
        for (int i = 0; i < roleMappings.length(); i++) {
            roles[i] = (new JSONObject(roleMappings.get(i).toString())).get("name").toString();
        }

        return roles;
    }

    private String getOpenIDConfigurationUrl(String realm) throws ApplicationSettingsException {
        return ServerSettings.getRemoteIDPServiceUrl() + "/realms/" + realm + "/.well-known/openid-configuration";
    }

    public String getFromUrl(String urlToRead, String token) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
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

    private String getAdminAccessToken(String gatewayId) throws Exception {
        CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
        GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(gatewayId);
        String identityServerRealm = gwrp.getIdentityServerTenant();
        String openIdConnectUrl = getOpenIDConfigurationUrl(identityServerRealm);
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        PasswordCredential credential = csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
        String username = credential.getLoginUserName();
        String password = credential.getPassword();
        String urlString = openIdConnectConfig.getString("token_endpoint");
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String postFields = "client_id=admin-cli&username=" + username + "&password=" + password + "&grant_type=password";
        conn.getOutputStream().write(postFields.getBytes());
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        JSONObject tokenInfo = new JSONObject(result.toString());
        return tokenInfo.get("access_token").toString();
    }


    private boolean hasPermission(String[] roles, String apiMethod) {
        for (int i = 0; i < roles.length; i++) {
            String role = roles[i];
            if (this.rolePermissionConfig.keySet().contains(role)) {
                Pattern pattern = Pattern.compile(this.rolePermissionConfig.get(role));
                Matcher matcher = pattern.matcher(apiMethod);
                if (matcher.matches())
                    return true;
            }
        }
        return false;
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