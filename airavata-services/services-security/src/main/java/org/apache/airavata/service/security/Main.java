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

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.error.AuthenticationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.security.oauth.DefaultOAuthClient;
import org.apache.airavata.service.security.xacml.DefaultXACMLPEP;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    private static String username = "scigap_admin";
    private static String password = "sci9067@min";
    private static String hostName = "https://idp.scigap.org:7443";
//    private static String clientId = "KUu0a74dFbrwvSxD3C_GhwKeNrQa";
    private static String clientId = "O3iUdkkVYyHgzWPiVTQpY_tb96Ma";
//    private static String clientSecret = "UTKb9nDOPsuWB4lEX39TwhkW8qIa";
    private static String clientSecret = "6Ck1jZoa2oRtrzodSqkUZ2iINkUa";

    public static void main(String[] args) throws AuthenticationException, AiravataSecurityException, AxisFault {
        String accessToken = authenticate("master@master.airavata", "master").getAccess_token();
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        DefaultOAuthClient defaultOAuthClient = new DefaultOAuthClient(hostName+"/services/",username,password, configContext);
        OAuth2TokenValidationResponseDTO tokenValidationRequestDTO = defaultOAuthClient.validateAccessToken(accessToken);
        String authorizedUser = tokenValidationRequestDTO.getAuthorizedUser();
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(accessToken);
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.USER_NAME, "scigap_admin");
        claimsMap.put(Constants.API_METHOD_NAME, "/airavata/getAPIVersion");
        authzToken.setClaimsMap(claimsMap);

        DefaultXACMLPEP defaultXACMLPEP = new DefaultXACMLPEP(hostName+"/services/",username,password,configContext);
        HashMap<String, String> metaDataMap = new HashMap();
        boolean result = defaultXACMLPEP.getAuthorizationDecision(authzToken, metaDataMap);
        System.out.println(result);
    }

    public static AuthResponse authenticate(String username,String password) throws AuthenticationException {
        try {
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
                return authResponse;
            }
        }catch (Exception ex){
            throw new AuthenticationException(ex.getMessage());
        }
        return null;
    }
}

class AuthResponse{

    private String token_type;
    private int expires_in;
    private String refresh_token;
    private String access_token;
    public String id_token;
    private String scope;


    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}