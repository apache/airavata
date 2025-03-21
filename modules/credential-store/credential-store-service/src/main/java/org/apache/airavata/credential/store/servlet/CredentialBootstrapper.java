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
package org.apache.airavata.credential.store.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Bootstrapper class for credential-store using Spring Security OAuth2.
 */
public class CredentialBootstrapper {

    protected static Logger log = LoggerFactory.getLogger(CredentialBootstrapper.class);

    public ClientRegistrationRepository getClientRegistrationRepository(ServletContext servletContext) throws Exception {
        File currentDirectory = new File(".");
        log.info("Current directory is - " + currentDirectory.getAbsolutePath());

        // Create OAuth2 client registration
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("myproxy")
                .clientId(System.getProperty("oauth.client.id"))
                .clientSecret(System.getProperty("oauth.client.secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri(System.getProperty("oauth.authorization.uri"))
                .tokenUri(System.getProperty("oauth.token.uri"))
                .userInfoUri(System.getProperty("oauth.userinfo.uri"))
                .userNameAttributeName("sub")
                .clientName("MyProxy OAuth2 Client")
                .build();

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }
}
