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
package org.apache.airavata.service.security.oauth;

import org.apache.airavata.security.AiravataSecurityException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * This is the default OAuth Client that talks to WSO2 IS's OAuth Authentication Server
 * to get the OAuth token validated.
 */
public class DefaultOAuthClient {

    private OAuth2TokenValidationServiceStub stub;
    private final static Logger logger = LoggerFactory.getLogger(DefaultOAuthClient.class);
    public static final String BEARER_TOKEN_TYPE = "bearer";

    /**
     * OAuth2TokenValidationService Admin Service Client
     *
     * @param auhorizationServerURL
     * @param username
     * @param password
     * @param configCtx
     * @throws Exception
     */
    public DefaultOAuthClient(String auhorizationServerURL, String username, String password,
                              ConfigurationContext configCtx) throws AiravataSecurityException {
        try {
            String serviceURL = auhorizationServerURL + "OAuth2TokenValidationService";
            stub = new OAuth2TokenValidationServiceStub(configCtx, serviceURL);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true, stub._getServiceClient());
        } catch (AxisFault e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error initializing OAuth client.");
        }
    }

    /**
     * Validates the OAuth 2.0 access token
     *
     * @param accessToken
     * @return
     * @throws Exception
     */
    public OAuth2TokenValidationResponseDTO validateAccessToken(String accessToken)
            throws AiravataSecurityException {

        try {
            OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
            OAuth2TokenValidationRequestDTO_OAuth2AccessToken token =
                    new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
            token.setIdentifier(accessToken);
            token.setTokenType(BEARER_TOKEN_TYPE);
            oauthReq.setAccessToken(token);
            return stub.validate(oauthReq);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in validating the OAuth access token.");
        }
    }

}
