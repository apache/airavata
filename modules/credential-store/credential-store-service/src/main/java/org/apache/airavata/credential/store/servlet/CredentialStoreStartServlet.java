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

import org.apache.airavata.credential.store.util.ConfigurationReader;
import org.apache.airavata.credential.store.util.CredentialStoreConstants;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * When portal initiate a request to get credentials it will hit this servlet.
 */
public class CredentialStoreStartServlet extends HttpServlet {

    private static ConfigurationReader configurationReader = null;
    private static Logger log = LoggerFactory.getLogger(CredentialStoreStartServlet.class);
    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2AuthorizedClientService authorizedClientService;

    public void init() throws ServletException {
        try {
            if (configurationReader == null) {
                configurationReader = new ConfigurationReader();
            }
            CredentialBootstrapper bootstrapper = new CredentialBootstrapper();
            clientRegistrationRepository = bootstrapper.getClientRegistrationRepository(getServletContext());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gatewayName = request.getParameter(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER);
        String portalUserName = request.getParameter(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER);
        String contactEmail = request.getParameter(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER);
        String associatedToken = TokenGenerator.generateToken(gatewayName, portalUserName);

        if (gatewayName == null) {
            handleError(request, response, "Please specify a gateway name.");
            return;
        }

        if (portalUserName == null) {
            handleError(request, response, "Please specify a portal user name.");
            return;
        }

        if (contactEmail == null) {
            handleError(request, response, "Please specify a contact email address for community user account.");
            return;
        }

        log.info("1.a. Starting OAuth2 authorization request");

        try {
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("myproxy");
            String redirectUri = clientRegistration.getRedirectUri()
                    .replace("{baseUrl}", request.getRequestURL().toString().replace(request.getRequestURI(), ""))
                    .replace("{registrationId}", clientRegistration.getRegistrationId());

            Map<String, Object> additionalParameters = new HashMap<>();
            additionalParameters.put(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER, gatewayName);
            additionalParameters.put(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER, portalUserName);
            additionalParameters.put(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER, contactEmail);
            additionalParameters.put(CredentialStoreConstants.PORTAL_TOKEN_ID_ASSIGNED, associatedToken);

            String state = generateState();
            String codeVerifier = new Base64StringKeyGenerator(32).generateKey();
            String codeChallenge = generateCodeChallenge(codeVerifier);

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .clientId(clientRegistration.getClientId())
                    .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                    .redirectUri(redirectUri)
                    .state(state)
                    .scope(clientRegistration.getScopes().toArray(new String[0]))
                    .additionalParameters(additionalParameters);

            builder.attributes(attrs -> {
                attrs.put(PkceParameterNames.CODE_VERIFIER, codeVerifier);
                attrs.put(PkceParameterNames.CODE_CHALLENGE, codeChallenge);
                attrs.put(PkceParameterNames.CODE_CHALLENGE_METHOD, "S256");
            });

            OAuth2AuthorizationRequest authorizationRequest = builder.build();
            String authorizationRequestUri = authorizationRequest.getAuthorizationRequestUri();
            response.sendRedirect(authorizationRequestUri);
        } catch (RuntimeException e) {
            handleError(request, response, "Failed to process authorization request: " + e.getMessage());
        }
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher(configurationReader.getErrorUrl()).forward(request, response);
    }

    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }
}
