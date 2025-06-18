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
package org.apache.airavata.tools.load;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;

public class Authenticator {

    public static AuthzToken getAuthzToken(
            String userName,
            String password,
            String gateway,
            String keycloakUrl,
            String keycloakClientId,
            String keycloakClientSecret)
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", keycloakClientSecret);
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient =
                HttpClients.custom().setSSLSocketFactory(sslsf).build();

        Configuration configuration =
                new Configuration(keycloakUrl, gateway, keycloakClientId, clientCredentials, httpclient);
        AuthzClient keycloakClient = AuthzClient.create(configuration);
        AccessTokenResponse accessToken = keycloakClient.obtainAccessToken(userName, password);

        AuthzToken authzToken = new AuthzToken();
        Map<String, String> claims = new HashMap<>();
        claims.put("gatewayID", gateway);
        claims.put("userName", userName);
        authzToken.setAccessToken(accessToken.getToken());
        authzToken.setClaimsMap(claims);
        return authzToken;
    }
}
