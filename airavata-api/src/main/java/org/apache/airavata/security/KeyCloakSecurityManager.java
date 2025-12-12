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
package org.apache.airavata.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.security.authzcache.*;
import org.apache.airavata.security.authzcache.AuthzCacheManagerFactory;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.models.UserGroup;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeyCloakSecurityManager implements AiravataSecurityManager {
    private static final Logger logger = LoggerFactory.getLogger(KeyCloakSecurityManager.class);
    // Methods that users user to manage their user resource profile
    private static final String USER_RESOURCE_PROFILE_USER_METHODS =
            "/airavata/registerUserResourceProfile|/airavata/getUserResourceProfile"
                    + "|/airavata/updateUserResourceProfile|/airavata/deleteUserResourceProfile|/airavata/addUserComputeResourcePreference"
                    + "|/airavata/addUserStoragePreference|/airavata/getUserComputeResourcePreference|/airavata/getUserStoragePreference"
                    + "|/airavata/getAllUserComputeResourcePreferences|/airavata/getAllUserStoragePreferences"
                    + "|/airavata/updateUserComputeResourcePreference|/airavata/updateUserStoragePreference"
                    + "|/airavata/deleteUserComputeResourcePreference|/airavata/deleteUserStoragePreference"
                    + "|/airavata/generateAndRegisterSSHKeys|/airavata/getAllCredentialSummaryForUsersInGateway"
                    + "|/airavata/deleteSSHPubKey|/airavata/isUserResourceProfileExists";
    private static final String SHARING_RESOURCE_METHODS =
            "/airavata/shareResourceWithUsers|/airavata/revokeSharingOfResourceFromUsers"
                    + "|/airavata/shareResourceWithGroups|/airavata/revokeSharingOfResourceFromGroups|/airavata/getAllAccessibleUsers"
                    + "|/airavata/getAllAccessibleGroups|/airavata/userHasAccess|/airavata/getAllDirectlyAccessibleUsers"
                    + "|/airavata/getAllDirectlyAccessibleGroups";
    private static final String SSH_ACCOUNT_PROVISIONER_METHODS =
            "/airavata/getSSHAccountProvisioners|/airavata/doesUserHaveSSHAccount|/airavata"
                    + "/setupUserComputeResourcePreferencesForSSH|"
                    +
                    // getGatewayResourceProfile is needed to look up whether ssh account provisioning is
                    // configured for a gateway's compute resource preference
                    "/airavata/getGatewayResourceProfile";
    // These methods are protected by sharing registry authorization
    private static final String GROUP_RESOURCE_PROFILE_METHODS =
            "/airavata/createGroupResourceProfile|/airavata/updateGroupResourceProfile|/airavata/getGroupResourceProfile"
                    + "|/airavata/removeGroupResourceProfile|/airavata/getGroupResourceList|/airavata/removeGroupComputePrefs"
                    + "|/airavata/removeGroupComputeResourcePolicy|/airavata/removeGroupBatchQueueResourcePolicy"
                    + "|/airavata/getGroupComputeResourcePreference|/airavata/getGroupComputeResourcePolicy"
                    + "|/airavata/getBatchQueueResourcePolicy|/airavata/getGroupComputeResourcePrefList"
                    + "|/airavata/getGroupBatchQueueResourcePolicyList|/airavata/getGroupComputeResourcePolicyList";
    // These methods are protected by sharing registry authorization
    private static final String APPLICATION_DEPLOYMENT_METHODS =
            "/airavata/registerApplicationDeployment|/airavata/getApplicationDeployment|/airavata/updateApplicationDeployment"
                    + "|/airavata/deleteApplicationDeployment|/airavata/getAllApplicationDeployments|/airavata/getAccessibleApplicationDeployments"
                    + "|/airavata/getApplicationDeploymentsForAppModuleAndGroupResourceProfile";
    private static final String APPLICATION_MODULE_METHODS = "/airavata/getAccessibleAppModules";
    private static final String CREDENTIAL_TOKEN_METHODS =
            "/airavata/getCredentialSummary|/airavata/getAllCredentialSummaries|/airavata/generateAndRegisterSSHKeys|/airavata/registerPwdCredential|/airavata/deleteSSHPubKey|/airavata/deletePWDCredential";
    // Misc. other methods needed for group based authorization
    private static final String GROUP_BASED_AUTH_METHODS = "/airavata/getGatewayGroups";
    private static final String INTERMEDIATE_OUTPUTS_METHODS =
            "/airavata/fetchIntermediateOutputs|/airavata/getIntermediateOutputProcessStatus";
    private final HashMap<String, String> rolePermissionConfig = new HashMap<>();

    private final RegistryService registryService;
    private final SharingRegistryService sharingRegistryService;
    private final AiravataServerProperties properties;
    private final AuthzCacheManagerFactory authzCacheManagerFactory;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;

    public KeyCloakSecurityManager(
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            AiravataServerProperties properties,
            AuthzCacheManagerFactory authzCacheManagerFactory,
            GatewayGroupsInitializer gatewayGroupsInitializer)
            throws AiravataSecurityException {
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.properties = properties;
        this.authzCacheManagerFactory = authzCacheManagerFactory;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
        rolePermissionConfig.put("admin", "/airavata/.*");
        rolePermissionConfig.put("gateway-provider", "/airavata/.*");
        rolePermissionConfig.put(
                "admin-read-only",
                "/airavata/getSSHPubKey|/airavata/getAllGatewaySSHPubKeys"
                        + "|/airavata/getAllGatewayPWDCredentials|/airavata/getApplicationModule|/airavata/getAllAppModules"
                        + "|/airavata/getApplicationDeployment|/airavata/getAllApplicationDeployments|/airavata/getAppModuleDeployedResources"
                        + "|/airavata/getStorageResource|/airavata/getAllStorageResourceNames|/airavata/getSCPDataMovement"
                        + "|/airavata/getUnicoreDataMovement|/airavata/getGridFTPDataMovement|/airavata/getResourceJobManager"
                        + "|/airavata/deleteResourceJobManager|/airavata/getGatewayResourceProfile|/airavata/getGatewayComputeResourcePreference"
                        + "|/airavata/getGatewayStoragePreference|/airavata/getAllGatewayComputeResourcePreferences"
                        + "|/airavata/getAllGatewayStoragePreferences|/airavata/getAllGatewayResourceProfiles|/airavata/getAPIVersion"
                        + "|/airavata/getNotification|/airavata/getAllNotifications|/airavata/createProject|/airavata/updateProject"
                        + "|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|/airavata/searchProjectsByProjectName"
                        + "|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|/airavata/searchExperimentsByDesc"
                        + "|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|/airavata/searchExperimentsByCreationTime"
                        + "|/airavata/searchExperiments|/airavata/getExperimentStatistics|/airavata/getExperimentsInProject"
                        + "|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|/airavata/getExperiment"
                        + "|/airavata/getDetailedExperimentTree|/airavata/updateExperiment|/airavata/updateExperimentConfiguration"
                        + "|/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment"
                        + "|/airavata/getExperimentStatus|/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs"
                        + "|/airavata/getJobStatuses|/airavata/getJobDetails|/airavata/cloneExperiment|/airavata/terminateExperiment"
                        + "|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|/airavata/getAllApplicationInterfaces"
                        + "|/airavata/getApplicationInputs|/airavata/getApplicationOutputs|/airavata/getAvailableAppInterfaceComputeResources"
                        + "|/airavata/getComputeResource|/airavata/getAllComputeResourceNames|/airavata/getWorkflow|/airavata/getWorkflowTemplateId"
                        + "|/airavata/isWorkflowExistWithName|/airavata/registerDataProduct|/airavata/getDataProduct|/airavata/registerReplicaLocation"
                        + "|/airavata/getParentDataProduct|/airavata/getChildDataProducts|/airavata/getAllAccessibleUsers"
                        + "|/airavata/getExperimentByAdmin|/airavata/cloneExperimentByAdmin"
                        + "|"
                        + USER_RESOURCE_PROFILE_USER_METHODS + "|/airavata/getAllUserResourceProfiles" + "|"
                        + SHARING_RESOURCE_METHODS + "|/airavata/getGateway|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|"
                        + GROUP_RESOURCE_PROFILE_METHODS + "|"
                        + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|"
                        + APPLICATION_MODULE_METHODS + "|"
                        + CREDENTIAL_TOKEN_METHODS + "|" + INTERMEDIATE_OUTPUTS_METHODS);
        rolePermissionConfig.put(
                "gateway-user",
                "/airavata/getAPIVersion|/airavata/getNotification|/airavata/getAllNotifications|"
                        + "/airavata/createProject|/airavata/updateProject|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|"
                        + "/airavata/searchProjectsByProjectName|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|"
                        + "/airavata/searchExperimentsByDesc|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|"
                        + "/airavata/searchExperimentsByCreationTime|/airavata/searchExperiments|"
                        + "/airavata/getExperimentsInProject|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|"
                        + "/airavata/getExperiment|/airavata/getDetailedExperimentTree|/airavata/updateExperiment|/airavata/updateExperimentConfiguration|"
                        + "/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment|/airavata/getExperimentStatus|"
                        + "/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs|/airavata/getJobStatuses|/airavata/getJobDetails|"
                        + "/airavata/cloneExperiment|/airavata/terminateExperiment|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|"
                        + "/airavata/getAllApplicationInterfaces|/airavata/getApplicationInputs|/airavata/getApplicationOutputs|"
                        + "/airavata/getAvailableAppInterfaceComputeResources|/airavata/getComputeResource|/airavata/getAllComputeResourceNames|"
                        + "/airavata/getWorkflow|/airavata/getWorkflowTemplateId|/airavata/isWorkflowExistWithName|/airavata/registerDataProduct|"
                        + "/airavata/getDataProduct|/airavata/registerReplicaLocation|/airavata/getParentDataProduct|/airavata/getChildDataProducts|"
                        + "/airavata/getAllAccessibleUsers|/airavata/getAllApplicationDeployments|/airavata/getAllAppModules|/airavata/getApplicationModule|"
                        + USER_RESOURCE_PROFILE_USER_METHODS + "|" + SHARING_RESOURCE_METHODS
                        + "|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|" + GROUP_RESOURCE_PROFILE_METHODS + "|"
                        + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|"
                        + APPLICATION_MODULE_METHODS + "|"
                        + CREDENTIAL_TOKEN_METHODS + "|" + INTERMEDIATE_OUTPUTS_METHODS);
    }

    /**
     * Implement this method with the user authentication/authorization logic in your SecurityManager.
     *
     * @param authzToken : this includes OAuth token and user's claims
     * @param metaData   : this includes other metadata needed for security enforcements.
     */
    @Override
    public boolean isUserAuthorized(AuthzToken authzToken, Map<String, String> metaData)
            throws AiravataSecurityException {
        String subject = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String accessToken = authzToken.getAccessToken();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String action = "/airavata/" + metaData.get(Constants.API_METHOD_NAME);
        try {
            if (!properties.security.tls.enabled) {
                return true;
            }
            initServiceClients();

            boolean decision;
            if (properties.security.authzCache.enabled) {
                var authzCacheManager = authzCacheManagerFactory.getAuthzCacheManager();
                var cacheIndex = new AuthzCacheIndex(subject, gatewayId, accessToken, action);
                var authzCachedStatus = authzCacheManager.getAuthzCachedStatus(cacheIndex);
                switch (authzCachedStatus) {
                    case AUTHORIZED -> decision = true;
                    case NOT_AUTHORIZED -> decision = false;
                    case NOT_CACHED -> {
                        var gatewayGroupMembership = getGatewayGroupMembership(subject, accessToken, gatewayId);
                        decision = hasPermission(gatewayGroupMembership, action);
                        // TODO get the actual token expiration time
                        var currentTime = System.currentTimeMillis();
                        authzCacheManager.addToAuthzCache(
                                new AuthzCacheIndex(subject, gatewayId, accessToken, action),
                                new AuthzCacheEntry(decision, currentTime + 1000 * 60 * 60, currentTime));
                    }
                    default -> throw new AiravataSecurityException("Error in reading from the authorization cache.");
                }
            } else {
                var gatewayGroupMembership = getGatewayGroupMembership(subject, accessToken, gatewayId);
                decision = hasPermission(gatewayGroupMembership, action);
            }
            logger.debug("Authz decision for: ({},{},{}) = {}", subject, accessToken, action, decision);
            return decision;
        } catch (ApplicationSettingsException e) {
            logger.error("Missing or invalid application setting.", e);
            throw new AiravataSecurityException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error making Authz decision for: ({},{},{})", subject, action, gatewayId, e);
            throw new AiravataSecurityException(e.getMessage(), e);
        } finally {
            closeServiceClients();
        }
    }

    @Override
    public AuthzToken getUserManagementServiceAccountAuthzToken(String gatewayId) throws AiravataSecurityException {
        try {
            initServiceClients();
            Gateway gateway = registryService.getGateway(gatewayId);
            String tokenURL = getTokenEndpoint(gatewayId);
            JSONObject clientCredentials =
                    getClientCredentials(tokenURL, gateway.getOauthClientId(), gateway.getOauthClientSecret());
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
            return getUserInfo(gatewayId, token);
        } catch (Exception e) {
            throw new AiravataSecurityException(e);
        } finally {
            closeServiceClients();
        }
    }

    private UserInfo getUserInfo(String gatewayId, String token) throws Exception {
        GatewayResourceProfile gwrp = registryService.getGatewayResourceProfile(gatewayId);
        String identityServerRealm = gwrp.getIdentityServerTenant();
        String openIdConnectUrl = getOpenIDConfigurationUrl(identityServerRealm);
        JSONObject openIdConnectConfig = new JSONObject(getFromUrl(openIdConnectUrl, null));
        String userInfoEndPoint = openIdConnectConfig.getString("userinfo_endpoint");
        JSONObject userInfo = new JSONObject(getFromUrl(userInfoEndPoint, token));
        return new UserInfo()
                .setSub(userInfo.getString("sub"))
                .setFullName(userInfo.getString("name"))
                .setFirstName(userInfo.getString("given_name"))
                .setLastName(userInfo.getString("family_name"))
                .setEmailAddress(userInfo.getString("email"))
                .setUsername(userInfo.getString("preferred_username"));
    }

    private GatewayGroupMembership getGatewayGroupMembership(String username, String token, String gatewayId)
            throws Exception {
        validateToken(username, token, gatewayId);
        GatewayGroups gatewayGroups = getGatewayGroups(gatewayId);
        List<UserGroup> userGroups =
                sharingRegistryService.getAllMemberGroupsForUser(gatewayId, username + "@" + gatewayId);
        List<String> userGroupIds =
                userGroups.stream().map(UserGroup::getGroupId).toList();
        GatewayGroupMembership gatewayGroupMembership = new GatewayGroupMembership();
        gatewayGroupMembership.setInAdminsGroup(userGroupIds.contains(gatewayGroups.getAdminsGroupId()));
        gatewayGroupMembership.setInReadOnlyAdminsGroup(
                userGroupIds.contains(gatewayGroups.getReadOnlyAdminsGroupId()));
        return gatewayGroupMembership;
    }

    private GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        if (registryService.isGatewayGroupsExists(gatewayId)) {
            return registryService.getGatewayGroups(gatewayId);
        } else {
            return gatewayGroupsInitializer.initialize(gatewayId);
        }
    }

    private void validateToken(String username, String token, String gatewayId) throws Exception {
        UserInfo userInfo = getUserInfo(gatewayId, token);
        if (!username.equals(userInfo.getUsername())) {
            throw new AiravataSecurityException("Subject name and username for the token doesn't match");
        }
    }

    private String getOpenIDConfigurationUrl(String realm) {
        return properties.security.iam.serverUrl + "/realms/" + realm + "/.well-known/openid-configuration";
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

    public JSONObject getClientCredentials(String tokenURL, String clientId, String clientSecret) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createSystem();

        HttpPost httpPost = new HttpPost(tokenURL);
        String encoded =
                Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpClient.close();
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

    private void initServiceClients() {
        // Services are now injected via Spring, no initialization needed
    }

    private void closeServiceClients() {
        // Services are managed by Spring, no cleanup needed
    }

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
}
