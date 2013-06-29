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

package org.apache.airavata.credential.store.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.AssetResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPService;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.servlet.JSPUtil;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.util.Utility;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment.CALLBACK_URI_KEY;

/**
 * Callback from the portal will come here. In this class we will store incomming certificate to the database. Partly
 * taken from OA4MP code base.
 */
public class CredentialStoreCallbackServlet extends ClientServlet {

    private static final String ERROR_PAGE = "/credential-store/error.jsp";
    private static final String SUCCESS_PAGE = "/credential-store/success.jsp";

    private static final String GATEWAY_NAME_QUERY_PARAMETER = "gatewayName";
    private static final String PORTAL_USER_QUERY_PARAMETER = "portalUserName";
    private static final String PORTAL_USER_EMAIL_QUERY_PARAMETER = "email";
    private static final String PORTAL_TOKEN_ID_ASSIGNED = "associatedToken";
    private static final String DURATION_QUERY_PARAMETER = "duration";

    private OA4MPService oa4mpService;

    private CertificateCredentialWriter certificateCredentialWriter;

    public void init() throws ServletException {

        DBUtil dbUtil;

        try {
            dbUtil = DBUtil.getDBUtil();
        } catch (Exception e) {
            throw new ServletException("Error initializing database operations.", e);
        }

        super.init();
        certificateCredentialWriter = new CertificateCredentialWriter(dbUtil);

        info("Credential store callback initialized successfully.");
    }

    @Override
    public OA4MPService getOA4MPService() {
        return oa4mpService;
    }

    @Override
    public void loadEnvironment() throws IOException {
        environment = getConfigurationLoader().load();
        oa4mpService = new CredentialStoreOA4MPServer((ClientEnvironment) environment);
    }

    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String gatewayName = request.getParameter(GATEWAY_NAME_QUERY_PARAMETER);
        String portalUserName = request.getParameter(PORTAL_USER_QUERY_PARAMETER);
        String durationParameter = request.getParameter(DURATION_QUERY_PARAMETER);
        String contactEmail = request.getParameter(PORTAL_USER_EMAIL_QUERY_PARAMETER);
        String portalTokenId = request.getParameter(PORTAL_TOKEN_ID_ASSIGNED);

        // TODO remove hard coded values, once passing query parameters is
        // fixed in OA4MP client api
        long duration = 800;

        if (durationParameter != null) {
            duration = Long.parseLong(durationParameter);
        }

        if (portalTokenId == null) {
            error("Token given by portal is invalid.");
            GeneralException ge = new GeneralException("Error: The token presented by portal is null.");
            request.setAttribute("exception", ge);
            JSPUtil.fwd(request, response, ERROR_PAGE);
            return;
        }

        info("Gateway name " + gatewayName);
        info("Portal user name " + portalUserName);
        info("Community user contact email " + contactEmail);
        info("Token id presented " + portalTokenId);

        info("2.a. Getting token and verifier.");
        String token = request.getParameter(TOKEN_KEY);
        String verifier = request.getParameter(VERIFIER_KEY);
        if (token == null || verifier == null) {
            warn("2.a. The token is " + (token == null ? "null" : token) + " and the verifier is "
                    + (verifier == null ? "null" : verifier));
            GeneralException ge = new GeneralException(
                    "Error: This servlet requires parameters for the token and verifier. It cannot be called directly.");
            request.setAttribute("exception", ge);
            JSPUtil.fwd(request, response, ERROR_PAGE);
            return;
        }
        info("2.a Token and verifier found.");
        X509Certificate cert = null;
        AssetResponse assetResponse = null;
        OA4MPResponse oa4MPResponse = null;

        Map<String, String> parameters = createQueryParameters(gatewayName, portalUserName, contactEmail, portalTokenId);

        try {
            info("Requesting private key ...");
            oa4MPResponse = getOA4MPService().requestCert(parameters);
            // oa4MPResponse = getOA4MPService().requestCert();

            info("2.a. Getting the cert(s) from the service");
            assetResponse = getOA4MPService().getCert(token, verifier);
            cert = assetResponse.getX509Certificates()[0];

            // The work in this call
        } catch (Throwable t) {
            warn("2.a. Exception from the server: " + t.getCause().getMessage());
            error("Exception while trying to get cert. message:" + t.getMessage());
            request.setAttribute("exception", t);
            JSPUtil.fwd(request, response, ERROR_PAGE);
            return;
        }
        info("2.b. Done! Displaying success page.");

        CertificateCredential certificateCredential = new CertificateCredential();

        certificateCredential.setNotBefore(Utility.convertDateToString(cert.getNotBefore()));
        certificateCredential.setNotAfter(Utility.convertDateToString(cert.getNotAfter()));
        certificateCredential.setCertificate(cert);
        certificateCredential.setPrivateKey(oa4MPResponse.getPrivateKey());
        certificateCredential
                .setCommunityUser(new CommunityUser(gatewayName, assetResponse.getUsername(), contactEmail));
        certificateCredential.setPortalUserName(portalUserName);
        certificateCredential.setLifeTime(duration);
        certificateCredential.setToken(portalTokenId);

        certificateCredentialWriter.writeCredentials(certificateCredential);

        StringBuilder stringBuilder = new StringBuilder("Certificate for community user ");
        stringBuilder.append(assetResponse.getUsername()).append(" successfully persisted.");
        stringBuilder.append(" Certificate DN - ").append(cert.getSubjectDN());

        info(stringBuilder.toString());

        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }
        request.setAttribute("action", contextPath);
        JSPUtil.fwd(request, response, SUCCESS_PAGE);
        info("2.a. Completely finished with delegation.");

    }

    private Map<String, String> createQueryParameters(String gatewayName, String portalUserName, String portalEmail,
            String tokenId) {

        String callbackUriKey = getEnvironment().getConstants().get(CALLBACK_URI_KEY);
        ClientEnvironment clientEnvironment = (ClientEnvironment) getEnvironment();

        String callbackUri = clientEnvironment.getCallback().toString();

        StringBuilder stringBuilder = new StringBuilder(callbackUri);

        stringBuilder.append("?").append(GATEWAY_NAME_QUERY_PARAMETER).append("=").append(gatewayName).append("&")
                .append(PORTAL_USER_QUERY_PARAMETER).append("=").append(portalUserName).append("&")
                .append(PORTAL_USER_EMAIL_QUERY_PARAMETER).append("=").append(portalEmail).append("&")
                .append(PORTAL_TOKEN_ID_ASSIGNED).append("=").append(tokenId);

        info("Callback URI is set to - " + stringBuilder.toString());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(callbackUriKey, stringBuilder.toString());

        return parameters;

    }
}
