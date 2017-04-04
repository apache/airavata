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
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.airavata.service.security.authzcache.*;
import org.apache.airavata.service.security.oauth.DefaultOAuthClient;
import org.apache.airavata.service.security.xacml.DefaultPAPClient;
import org.apache.airavata.service.security.xacml.DefaultXACMLPEP;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * This enforces authentication and authorization on Airavata API calls.
 */
public class DefaultAiravataSecurityManager implements AiravataSecurityManager {
    private final static Logger logger = LoggerFactory.getLogger(DefaultAiravataSecurityManager.class);

    @Override
    public void initializeSecurityInfra() throws AiravataSecurityException {
        /* in the default security manager, this method checks if the xacml authorization policy is published,
         * and if not, publish the policy to the PDP (of WSO2 Identity Server)
         */
        try {
            if (ServerSettings.isAPISecured()) {
                ConfigurationContext configContext =
                        ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
                //initialize SSL context with the trust store that contains the public cert of WSO2 Identity Server.
                TrustStoreManager trustStoreManager = new TrustStoreManager();
                trustStoreManager.initializeTrustStoreManager(ServerSettings.getTrustStorePath(),
                        ServerSettings.getTrustStorePassword());
                List<GatewayResourceProfile> gwProfiles = getRegistryServiceClient().getAllGatewayResourceProfiles();
                //read the policy as a string
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(
                        ServerSettings.getAuthorizationPoliyName() + ".xml")));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String defaultXACMLPolicy = stringBuilder.toString();
                CredentialStoreService.Client csClient = getCredentialStoreServiceClient();

                for(GatewayResourceProfile gwrp : gwProfiles){
                    if(gwrp.getIdentityServerPwdCredToken() != null && gwrp.getIdentityServerTenant() != null){
                        PasswordCredential credential = csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
                        String username = credential.getLoginUserName();
                        if(gwrp.getIdentityServerTenant() != null && !gwrp.getIdentityServerTenant().isEmpty())
                            username = username + "@" + gwrp.getIdentityServerTenant();
                        String password = credential.getPassword();
                        DefaultPAPClient PAPClient = new DefaultPAPClient(ServerSettings.getRemoteAuthzServerUrl(),
                                username, password, configContext);
                        boolean policyAdded = PAPClient.isPolicyAdded(ServerSettings.getAuthorizationPoliyName());
                        if (policyAdded) {
                            logger.debug("Authorization policy is already added in the authorization server.");
                        } else {
                            //publish the policy and enable it in a separate thread
                            PAPClient.addPolicy(defaultXACMLPolicy);
                            logger.debug("Authorization policy is published in the authorization server.");
                        }
                    }else{
                        logger.warn("Identity Server configuration missing for gateway : " + gwrp.getGatewayID());
                    }
                }
            }
        } catch (AxisFault axisFault) {
            logger.error(axisFault.getMessage(), axisFault);
            throw new AiravataSecurityException("Error in initializing the configuration context for creating the " +
                    "PAP client.");
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading configuration when creating the PAP client.");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading authorization policy.");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading the authorization policy.");
        } catch (RegistryServiceException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading the Gateway Profiles from App Catalog.");
        } catch (TException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in connecting to Credential Store Service.");
        }
    }

    public boolean isUserAuthorized(AuthzToken authzToken, Map<String, String> metaData) throws AiravataSecurityException {
        try {
            String subject = authzToken.getClaimsMap().get(Constants.USER_NAME);
            String accessToken = authzToken.getAccessToken();
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String action = metaData.get(Constants.API_METHOD_NAME);

            //if the authz cache is enabled, check in the cache if the authz decision is cached and if so, what the status is
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

                    CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
                    GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(gatewayId);
                    PasswordCredential credential = csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
                    String username = credential.getLoginUserName();
                    if(gwrp.getIdentityServerTenant() != null && !gwrp.getIdentityServerTenant().isEmpty())
                        username = username + "@" + gwrp.getIdentityServerTenant();
                    String password = credential.getPassword();

                    //talk to Authorization Server, obtain the decision, cache it and return the result.
                    ConfigurationContext configContext =
                            ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

                    //initialize SSL context with the trust store that contains the public cert of WSO2 Identity Server.
                    TrustStoreManager trustStoreManager = new TrustStoreManager();
                    trustStoreManager.initializeTrustStoreManager(ServerSettings.getTrustStorePath(),
                            ServerSettings.getTrustStorePassword());

                    DefaultOAuthClient oauthClient = new DefaultOAuthClient(ServerSettings.getRemoteAuthzServerUrl(),
                            username, password, configContext);
                    OAuth2TokenValidationResponseDTO validationResponse = oauthClient.validateAccessToken(
                            authzToken.getAccessToken());
                    if(validationResponse.getValid()){
                        String authorizedUserName = validationResponse.getAuthorizedUser();
                        if(authorizedUserName.contains("@")){
                            authorizedUserName = authorizedUserName.split("@")[0];
                        }
                        if(subject.contains("@")){
                            subject = subject.split("@")[0];
                        }
                        //cannot impersonate users
                        if(!authorizedUserName.toLowerCase().equals(subject.toLowerCase()))
                            return false;

                        long expiryTimestamp = validationResponse.getExpiryTime();

                        //check for fine grained authorization for the API invocation, based on XACML.
                        DefaultXACMLPEP entitlementClient = new DefaultXACMLPEP(ServerSettings.getRemoteAuthzServerUrl(),
                                username, password, configContext);
                        boolean authorizationDecision = entitlementClient.getAuthorizationDecision(authzToken, metaData);

                        //cache the authorization decision
                        authzCacheManager.addToAuthzCache(new AuthzCacheIndex(subject, gatewayId, accessToken, action),
                                new AuthzCacheEntry(authorizationDecision, expiryTimestamp, System.currentTimeMillis()));

                        return authorizationDecision;
                    }else {
                        return false;
                    }


                } else {
                    //undefined status returned from the authz cache manager
                    throw new AiravataSecurityException("Error in reading from the authorization cache.");
                }
            } else {
                CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
                GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(gatewayId);
                PasswordCredential credential = csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
                String username = credential.getLoginUserName();
                if(gwrp.getIdentityServerTenant() != null && !gwrp.getIdentityServerTenant().isEmpty())
                    username = username + "@" + gwrp.getIdentityServerTenant();
                String password = credential.getPassword();

                //talk to Authorization Server, obtain the decision and return the result (authz cache is not enabled).
                ConfigurationContext configContext =
                        ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

                //initialize SSL context with the trust store that contains the public cert of WSO2 Identity Server.
                TrustStoreManager trustStoreManager = new TrustStoreManager();
                trustStoreManager.initializeTrustStoreManager(ServerSettings.getTrustStorePath(),
                        ServerSettings.getTrustStorePassword());

                DefaultOAuthClient oauthClient = new DefaultOAuthClient(ServerSettings.getRemoteAuthzServerUrl(),
                        username, password, configContext);
                OAuth2TokenValidationResponseDTO validationResponse = oauthClient.validateAccessToken(
                        authzToken.getAccessToken());
                boolean isOAuthTokenValid = validationResponse.getValid();
                //if XACML based authorization is enabled, check for role based authorization for the API invocation
                DefaultXACMLPEP entitlementClient = new DefaultXACMLPEP(ServerSettings.getRemoteAuthzServerUrl(),
                        username, password, configContext);
                boolean authorizationDecision = entitlementClient.getAuthorizationDecision(authzToken, metaData);

                return (isOAuthTokenValid && authorizationDecision);
            }

        } catch (AxisFault axisFault) {
            logger.error(axisFault.getMessage(), axisFault);
            throw new AiravataSecurityException("Error in initializing the configuration context for creating the OAuth validation client.");
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading OAuth server configuration.");
        } catch (RegistryServiceException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in accessing AppCatalog.");
        } catch (TException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in connecting to Credential Store Service.");
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

    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }
}