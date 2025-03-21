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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.notifier.NotifierBootstrap;
import org.apache.airavata.credential.store.notifier.impl.EmailNotifierConfiguration;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.util.ConfigurationReader;
import org.apache.airavata.credential.store.util.CredentialStoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Callback from the portal will come here. In this class we will store incoming certificate to the database.
 */
public class CredentialStoreCallbackServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(CredentialStoreCallbackServlet.class);
    private CertificateCredentialWriter certificateCredentialWriter;
    private static ConfigurationReader configurationReader;
    private NotifierBootstrap notifierBootstrap;
    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2AuthorizedClientService authorizedClientService;
    private DBUtil dbUtil;

    public void init() throws ServletException {
        try {
            if (configurationReader == null) {
                configurationReader = new ConfigurationReader();
            }
            CredentialBootstrapper bootstrapper = new CredentialBootstrapper();
            clientRegistrationRepository = bootstrapper.getClientRegistrationRepository(getServletContext());
            dbUtil = DBUtil.getCredentialStoreDBUtil();
            certificateCredentialWriter = new CertificateCredentialWriter(dbUtil);
        } catch (Exception e) {
            throw new ServletException("Error initializing configuration reader.", e);
        }

        // initialize notifier
        try {
            boolean enabled = Boolean.parseBoolean(ApplicationSettings.getCredentialStoreNotifierEnabled());

            if (enabled) {
                EmailNotifierConfiguration notifierConfiguration
                        = EmailNotifierConfiguration.getEmailNotifierConfigurations();
                long duration = Long.parseLong(ApplicationSettings.getCredentialStoreNotifierDuration());

                notifierBootstrap = new NotifierBootstrap(duration, dbUtil, notifierConfiguration);
            }

        } catch (ApplicationSettingsException e) {
            throw new ServletException("Error initializing notifier.", e);
        }

        log.info("Credential store callback initialized successfully.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gatewayName = request.getParameter(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER);
        String portalUserName = request.getParameter(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER);
        String durationParameter = request.getParameter(CredentialStoreConstants.DURATION_QUERY_PARAMETER);
        String contactEmail = request.getParameter(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER);
        String portalTokenId = request.getParameter(CredentialStoreConstants.PORTAL_TOKEN_ID_ASSIGNED);
        String state = request.getParameter(OAuth2ParameterNames.STATE);
        String code = request.getParameter(OAuth2ParameterNames.CODE);
        String error = request.getParameter(OAuth2ParameterNames.ERROR);

        long duration = 864000;
        if (durationParameter != null) {
            duration = Long.parseLong(durationParameter);
        }

        if (portalTokenId == null) {
            handleError(request, response, "Error: The token presented by portal is null.");
            return;
        }

        log.info("Gateway name {}", gatewayName);
        log.info("Portal user name {}", portalUserName);
        log.info("Community user contact email {}", contactEmail);
        log.info("Token id presented {}", portalTokenId);

        try {
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("myproxy");
            OAuth2AuthorizationResponse authorizationResponse;
            if (error != null) {
                authorizationResponse = OAuth2AuthorizationResponse.error(error)
                        .state(state)
                        .redirectUri(request.getRequestURL().toString())
                        .build();
            } else {
                authorizationResponse = OAuth2AuthorizationResponse.success(code)
                        .state(state)
                        .redirectUri(request.getRequestURL().toString())
                        .build();
            }
            
            if (authorizationResponse.getError() != null) {
                handleError(request, response, "Authorization error: " + authorizationResponse.getError().getErrorCode());
                return;
            }

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    clientRegistration.getRegistrationId(),
                    request.getSession().getId());

            if (authorizedClient == null) {
                handleError(request, response, "No authorized client found");
                return;
            }

            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            X509Certificate[] certificates = getCertificatesFromToken(accessToken);
            PrivateKey privateKey = getPrivateKeyFromToken(accessToken);

            CertificateCredential certificateCredential = new CertificateCredential();
            certificateCredential.setPortalUserName(portalUserName);
            certificateCredential.setCommunityUser(new CommunityUser(gatewayName, portalUserName, contactEmail));
            certificateCredential.setToken(portalTokenId);

            certificateCredentialWriter.writeCredentials(certificateCredential);

            log.info("Successfully stored certificate for user {}", portalUserName);
            response.sendRedirect(configurationReader.getSuccessUrl());

        } catch (Exception e) {
            log.error("Error processing OAuth2 callback", e);
            handleError(request, response, "Error processing OAuth2 callback: " + e.getMessage());
        }
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher(configurationReader.getErrorUrl()).forward(request, response);
    }

    private X509Certificate[] getCertificatesFromToken(OAuth2AccessToken token) throws Exception {
        // In a real implementation, you would extract the certificate from the token claims
        // For example, if the certificate is stored in a "x509_cert" claim as a Base64 encoded string
        String certClaim = token.getTokenValue(); // This is a placeholder - actual implementation would use token.getClaims()
        
        if (certClaim == null || certClaim.isEmpty()) {
            throw new Exception("Certificate not found in token");
        }
        
        try {
            // Decode the Base64 encoded certificate
            byte[] certBytes = Base64.getDecoder().decode(certClaim);
            
            // Create a certificate factory and parse the certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(certBytes));
            
            // Return an array with the certificate
            return new X509Certificate[] { cert };
        } catch (Exception e) {
            log.error("Error parsing certificate from token", e);
            throw new Exception("Failed to parse certificate from token", e);
        }
    }

    private PrivateKey getPrivateKeyFromToken(OAuth2AccessToken token) throws Exception {
        // In a real implementation, you would extract the private key from the token claims
        // For example, if the private key is stored in a "private_key" claim as a Base64 encoded string
        String keyClaim = token.getTokenValue(); // This is a placeholder - actual implementation would use token.getClaims()
        
        if (keyClaim == null || keyClaim.isEmpty()) {
            throw new Exception("Private key not found in token");
        }
        
        try {
            // Decode the Base64 encoded private key
            byte[] keyBytes = Base64.getDecoder().decode(keyClaim);
            
            // Create a PKCS8 encoded private key
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Error parsing private key from token", e);
            throw new Exception("Failed to parse private key from token", e);
        }
    }
}
