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

import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPService;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.security.servlet.JSPUtil;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.util.ConfigurationReader;
import org.apache.airavata.credential.store.util.CredentialStoreConstants;
import org.apache.airavata.credential.store.util.PrivateKeyStore;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment.CALLBACK_URI_KEY;

/**
 * When portal initiate a request to get credentials it will hit this servlet.
 */
public class CredentialStoreStartServlet extends ClientServlet {

    private static ConfigurationReader configurationReader = null;

    private static Logger log = LoggerFactory.getLogger(CredentialStoreStartServlet.class);
    private OA4MPService oa4mpService;

    protected String decorateURI(URI inputURI, Map<String, String> parameters) {

        if (parameters.isEmpty()) {
            return inputURI.toString();
        }

        String stringUri = inputURI.toString();
        StringBuilder stringBuilder = new StringBuilder(stringUri);

        boolean isFirst = true;

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (isFirst) {
                stringBuilder.append("?");
                isFirst = false;
            } else {
                stringBuilder.append("&");
            }

            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return stringBuilder.toString();

    }

    public void init() throws ServletException {

        super.init();

        try {
            if (configurationReader == null) {
                configurationReader = new ConfigurationReader();
            }
        } catch (CredentialStoreException e) {
            throw new ServletException(e);
        }

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

        String gatewayName
                = request.getParameter(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER);
        String portalUserName
                = request.getParameter(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER);
        String contactEmail
                = request.getParameter(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER);
        String associatedToken = TokenGenerator.generateToken(gatewayName, portalUserName);

        if (gatewayName == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a gateway name."), request, response,
                    configurationReader.getErrorUrl());
            return;
        }

        if (portalUserName == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a portal user name."), request, response,
                    configurationReader.getErrorUrl());
            return;
        }

        if (contactEmail == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a contact email address for community"
                    + " user account."), request, response, configurationReader.getErrorUrl());
            return;
        }

        log.info("1.a. Starting transaction");
        OA4MPResponse gtwResp;

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put(CredentialStoreConstants.GATEWAY_NAME_QUERY_PARAMETER, gatewayName);
        queryParameters.put(CredentialStoreConstants.PORTAL_USER_QUERY_PARAMETER, portalUserName);
        queryParameters.put(CredentialStoreConstants.PORTAL_USER_EMAIL_QUERY_PARAMETER, contactEmail);
        queryParameters.put(CredentialStoreConstants.PORTAL_TOKEN_ID_ASSIGNED, associatedToken);

        Map<String, String> additionalParameters = new HashMap<String, String>();

        if (getOA4MPService() == null) {
            loadEnvironment();
        }

        String modifiedCallbackUri = decorateURI(getOA4MPService().getEnvironment().getCallback(), queryParameters);

        info("The modified callback URI - " + modifiedCallbackUri);

        additionalParameters.put(getEnvironment().getConstants().get(CALLBACK_URI_KEY), modifiedCallbackUri);

        try {
            gtwResp = getOA4MPService().requestCert(additionalParameters);

            // Private key in store
            PrivateKeyStore privateKeyStore = PrivateKeyStore.getPrivateKeyStore();
            privateKeyStore.addKey(associatedToken, gtwResp.getPrivateKey());

        } catch (Throwable t) {
            JSPUtil.handleException(t, request, response, configurationReader.getErrorUrl());
            return;
        }
        log.info("1.b. Got response. Creating page with redirect for " + gtwResp.getRedirect().getHost());
        // Normally, we'd just do a redirect, but we will put up a page and show the redirect to the user.
        // The client response contains the generated private key as well
        // In a real application, the private key would be stored. This, however, exceeds the scope of this
        // sample application -- all we need to do to complete the process is send along the redirect url.

        request.setAttribute(REDIR, REDIR);
        request.setAttribute("redirectUrl", gtwResp.getRedirect().toString());
        request.setAttribute(ACTION_KEY, ACTION_KEY);
        request.setAttribute("action", ACTION_REDIRECT_VALUE);
        log.info("1.b. Showing redirect page.");
        JSPUtil.fwd(request, response, configurationReader.getPortalRedirectUrl());

    }
}
