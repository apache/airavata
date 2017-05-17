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

import edu.uiuc.ncsa.myproxy.oa4mp.client.AssetResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPService;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.servlet.JSPUtil;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.notifier.NotifierBootstrap;
import org.apache.airavata.credential.store.notifier.impl.EmailNotifierConfiguration;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.util.ConfigurationReader;
import org.apache.airavata.credential.store.util.CredentialStoreConstants;
import org.apache.airavata.credential.store.util.PrivateKeyStore;
import org.apache.airavata.credential.store.util.Utility;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment.CALLBACK_URI_KEY;

/**
 * Callback from the portal will come here. In this class we will store incomming certificate to the database. Partly
 * taken from OA4MP code base.
 */
public class CredentialStoreCallbackServlet extends ClientServlet {

    private OA4MPService oa4mpService;

    private CertificateCredentialWriter certificateCredentialWriter;

    private static ConfigurationReader configurationReader;

    private NotifierBootstrap notifierBootstrap;

    public void init() throws ServletException {

        DBUtil dbUtil;

        try {
            dbUtil = DBUtil.getCredentialStoreDBUtil();
        } catch (Exception e) {
            throw new ServletException("Error initializing database operations.", e);
        }

        try {
            configurationReader = new ConfigurationReader();
            super.init();
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


        info("Credential store callback initialized successfully.");
    }

    @Override
    public OA4MPService getOA4MPService() {
        return oa4mpService;
    }

    @Override
    public void loadEnvironment() throws IOException {
        environment = getConfigurationLoader().load();
        oa4mpService = new OA4MPService((ClientEnvironment) environment);
    }

    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String gatewayName = request.getParameter(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER);
        String portalUserName = request.getParameter(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER);
        String durationParameter = request.getParameter(CredentialStoreConstants.DURATION_QUERY_PARAMETER);
        String contactEmail = request.getParameter(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER);
        String portalTokenId = request.getParameter(CredentialStoreConstants.PORTAL_TOKEN_ID_ASSIGNED);

        // TODO remove hard coded values, once passing query parameters is
        // fixed in OA4MP client api
        long duration = 864000;

        if (durationParameter != null) {
            duration = Long.parseLong(durationParameter);
        }

        if (portalTokenId == null) {
            error("Token given by portal is invalid.");
            GeneralException ge = new GeneralException("Error: The token presented by portal is null.");
            request.setAttribute("exception", ge);
            JSPUtil.fwd(request, response, configurationReader.getErrorUrl());
            return;
        }

        info("Gateway name " + gatewayName);
        info("Portal user name " + portalUserName);
        info("Community user contact email " + contactEmail);
        info("Token id presented " + portalTokenId);

        info("2.a. Getting token and verifier.");
        String token = request.getParameter(CONST(ClientEnvironment.TOKEN));
        String verifier = request.getParameter(CONST(ClientEnvironment.VERIFIER));
        if (token == null || verifier == null) {
            warn("2.a. The token is " + (token == null ? "null" : token) + " and the verifier is "
                    + (verifier == null ? "null" : verifier));
            GeneralException ge = new GeneralException(
                    "Error: This servlet requires parameters for the token and verifier. It cannot be called directly.");
            request.setAttribute("exception", ge);
            JSPUtil.fwd(request, response, configurationReader.getErrorUrl());
            return;
        }
        info("2.a Token and verifier found.");
        X509Certificate[] certificates;
        AssetResponse assetResponse = null;

        PrivateKey privateKey;

        try {

            PrivateKeyStore privateKeyStore = PrivateKeyStore.getPrivateKeyStore();
            privateKey = privateKeyStore.getKey(portalTokenId);

            if (privateKey != null) {
                info("Found private key for token " + portalTokenId);
            } else {
                info("Could not find private key for token " + portalTokenId);
            }

            info("2.a. Getting the cert(s) from the service");
            assetResponse = getOA4MPService().getCert(token, verifier);

            certificates = assetResponse.getX509Certificates();

        } catch (Throwable t) {
            warn("2.a. Exception from the server: " + t.getCause().getMessage());
            error("Exception while trying to get cert. message:" + t.getMessage());
            request.setAttribute("exception", t);
            JSPUtil.fwd(request, response, configurationReader.getErrorUrl());
            return;
        }

        info("2.b. Done! Displaying success page.");

        CertificateCredential certificateCredential = new CertificateCredential();

        certificateCredential.setNotBefore(Utility.convertDateToString(certificates[0].getNotBefore())); //TODO check this is correct
        certificateCredential.setNotAfter(Utility.convertDateToString(certificates[0].getNotAfter()));
        certificateCredential.setCertificates(certificates);
        certificateCredential.setPrivateKey(privateKey);
        certificateCredential
                .setCommunityUser(new CommunityUser(gatewayName, assetResponse.getUsername(), contactEmail));
        certificateCredential.setPortalUserName(portalUserName);
        certificateCredential.setLifeTime(duration);
        certificateCredential.setToken(portalTokenId);


        certificateCredentialWriter.writeCredentials(certificateCredential);

        StringBuilder stringBuilder = new StringBuilder("Certificate for community user ");
        stringBuilder.append(assetResponse.getUsername()).append(" successfully persisted.");
        stringBuilder.append(" Certificate DN - ").append(certificates[0].getSubjectDN());

        info(stringBuilder.toString());

        if (isUrlInSameServer(configurationReader.getSuccessUrl())) {

            String contextPath = request.getContextPath();
            if (!contextPath.endsWith("/")) {
                contextPath = contextPath + "/";
            }
            request.setAttribute("action", contextPath);
            request.setAttribute("tokenId", portalTokenId);
            JSPUtil.fwd(request, response, configurationReader.getSuccessUrl());
        } else {

            String urlToRedirect = decorateUrlWithToken(configurationReader.getSuccessUrl(), portalTokenId);

            info("Redirecting to url - " + urlToRedirect);

            response.sendRedirect(urlToRedirect);
        }

        info("2.a. Completely finished with delegation.");

    }

    private boolean isUrlInSameServer(String url) {

        return !(url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("https"));

    }

    private String decorateUrlWithToken(String url, String tokenId) {

        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("?tokenId=").append(tokenId);
        return stringBuilder.toString();
    }

    private Map<String, String> createQueryParameters(String gatewayName, String portalUserName, String portalEmail,
            String tokenId) {

        String callbackUriKey = getEnvironment().getConstants().get(CALLBACK_URI_KEY);
        ClientEnvironment clientEnvironment = (ClientEnvironment) getEnvironment();

        String callbackUri = clientEnvironment.getCallback().toString();

        StringBuilder stringBuilder = new StringBuilder(callbackUri);

        stringBuilder.append("?").append(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER).append("=").append(gatewayName).append("&")
                .append(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER).append("=").append(portalUserName).append("&")
                .append(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER).append("=").append(portalEmail).append("&")
                .append(CredentialStoreConstants.PORTAL_TOKEN_ID_ASSIGNED).append("=").append(tokenId);

        info("Callback URI is set to - " + stringBuilder.toString());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(callbackUriKey, stringBuilder.toString());

        return parameters;

    }
}
