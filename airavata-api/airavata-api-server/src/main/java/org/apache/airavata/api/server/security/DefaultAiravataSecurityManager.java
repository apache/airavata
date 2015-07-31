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
package org.apache.airavata.api.server.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import java.util.Map;

/**
 * This enforces authentication and authorization on Airavata API calls.
 */
public class DefaultAiravataSecurityManager implements AiravataSecurityManager {
    private final static Logger logger = LoggerFactory.getLogger(DefaultAiravataSecurityManager.class);

    public boolean isUserAuthorized(AuthzToken authzToken, Map<String, String> metaData) throws AiravataSecurityException {
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

            //initialize SSL context with the trust store that contains the public cert of WSO2 Identity Server.
            TrustStoreManager trustStoreManager = new TrustStoreManager();
            trustStoreManager.initializeTrustStoreManager(ServerSettings.getTrustStorePath(),
                    ServerSettings.getTrustStorePassword());

            DefaultOAuthClient oauthClient = new DefaultOAuthClient(ServerSettings.getRemoteOauthServerUrl(),
                    ServerSettings.getAdminUsername(), ServerSettings.getAdminPassword(), configContext);
            OAuth2TokenValidationResponseDTO validationResponse = oauthClient.validateAccessToken(
                    authzToken.getAccessToken());
            boolean isOAuthTokenValid = validationResponse.getValid();
            //if XACML based authorization is enabled, check for role based authorization for the API invocation
            DefaultXACMLPEP entitlementClient = new DefaultXACMLPEP(ServerSettings.getRemoteOauthServerUrl(),
                    ServerSettings.getAdminUsername(), ServerSettings.getAdminPassword(), configContext);
            boolean authorizationDecision = entitlementClient.getAuthorizationDecision(authzToken, metaData);

            return (isOAuthTokenValid && authorizationDecision);

        } catch (AxisFault axisFault) {
            logger.error(axisFault.getMessage(), axisFault);
            throw new AiravataSecurityException("Error in initializing the configuration context for creating the OAuth validation client.");
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in reading OAuth server configuration.");
        }
    }
}
