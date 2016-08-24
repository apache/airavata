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
package org.airavata.xbaya.connectors.wso2is;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.codehaus.jackson.map.ObjectMapper;
import org.airavata.xbaya.util.XbayaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class AuthenticationManager {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    String hostName = XbayaContext.getInstance().getIdpUrl();
    String[] allowedUserRoles = XbayaContext.getInstance().getAuthorisedUserRoles();
    String clientId = XbayaContext.getInstance().getOAuthClientId();
    String clientSecret = XbayaContext.getInstance().getOAuthClientSecret();

    public AuthResponse authenticate(String username,String password) throws AuthenticationException {
        try {
            username = username + "@" + XbayaContext.getInstance().getIdpTenantId();

            OAuthClientRequest request = OAuthClientRequest.tokenLocation(hostName+"/oauth2/token").
                    setClientId(clientId).setClientSecret(clientSecret).
                    setGrantType(GrantType.PASSWORD).
                    setRedirectURI("").
                    setUsername(username).
                    setPassword(password).
                    setScope("openid").
                    buildBodyMessage();


            URLConnectionClient ucc = new URLConnectionClient();

            org.apache.oltu.oauth2.client.OAuthClient oAuthClient = new org.apache.oltu.oauth2.client.OAuthClient(ucc);
            OAuthResourceResponse resp = oAuthClient.resource(request, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

            //converting JSON to object
            ObjectMapper mapper = new ObjectMapper();
            AuthResponse authResponse;
            try{
                authResponse = mapper.readValue(resp.getBody(), AuthResponse.class);
            }catch (Exception e){
                return null;
            }

            String accessToken = authResponse.getAccess_token();
            if(accessToken != null && !accessToken.isEmpty()){
                request = new OAuthBearerClientRequest(hostName + "/oauth2/userinfo?schema=openid").
                        buildQueryMessage();
                ucc = new URLConnectionClient();
                request.setHeader("Authorization","Bearer "+accessToken);
                oAuthClient = new org.apache.oltu.oauth2.client.OAuthClient(ucc);
                resp = oAuthClient.resource(request, OAuth.HttpMethod.GET,
                        OAuthResourceResponse.class);
                Map<String,String> profile = mapper.readValue(resp.getBody(), Map.class);
                String[] userRoles = profile.get("roles").split(",");
                for(String userRole : userRoles){
                    for(String allowedRole : allowedUserRoles){
                        if(allowedRole.equals(userRole)){
                            logger.info("User Authenticated Successfully");
                            return authResponse;
                        }
                    }
                }
            }
        }catch (Exception ex){
            throw new AuthenticationException(ex);
        }
        return null;
    }

    public AuthResponse getRefreshedOAuthToken(String refreshToken) throws OAuthSystemException, OAuthProblemException,
            IOException {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(hostName + "/oauth2/token").
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.REFRESH_TOKEN).
                setRefreshToken(refreshToken).
                buildBodyMessage();

        URLConnectionClient ucc = new URLConnectionClient();

        org.apache.oltu.oauth2.client.OAuthClient oAuthClient = new org.apache.oltu.oauth2.client.OAuthClient(ucc);
        OAuthResourceResponse resp = oAuthClient.resource(request, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

        //converting JSON to object
        ObjectMapper mapper = new ObjectMapper();
        AuthResponse authResponse = mapper.readValue(resp.getBody(), AuthResponse.class);
        logger.info("Fetched new refreshed OAuth token");
        return authResponse;
    }
}


