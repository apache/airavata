/*
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

package org.apache.airavata.services.registry.rest.security;

import org.apache.airavata.security.AuthenticationException;
import org.apache.airavata.security.Authenticator;
import org.apache.airavata.security.configurations.AuthenticatorConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

/**
 * A servlet filter class which intercepts the request and do authentication.
 */
public class HttpAuthenticatorFilter implements Filter {

    private List<Authenticator> authenticatorList;

    private static Logger log = LoggerFactory.getLogger(HttpAuthenticatorFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String authenticatorConfiguration = filterConfig.getInitParameter("authenticatorConfigurations");

        //TODO make this able to read from a file as well


        InputStream configurationFileStream = HttpAuthenticatorFilter.class.getClassLoader().
                getResourceAsStream(authenticatorConfiguration);

        if (configurationFileStream == null) {
            String msg = "Invalid authenticator configuration. Cannot read file - ".concat(authenticatorConfiguration);
            log.error(msg);
            throw new ServletException(msg);
        }

        AuthenticatorConfigurationReader authenticatorConfigurationReader
                = new AuthenticatorConfigurationReader();
        try {
            authenticatorConfigurationReader.init(configurationFileStream);
        } catch (IOException e) {
            String msg = "Error reading authenticator configurations.";

            log.error(msg, e);
            throw new ServletException(msg, e);
        } catch (ParserConfigurationException e) {
            String msg = "Error parsing authenticator configurations.";

            log.error(msg, e);
            throw new ServletException(msg, e);
        } catch (SAXException e) {
            String msg = "Error parsing authenticator configurations.";

            log.error(msg, e);
            throw new ServletException(msg, e);
        } finally {
            try {
                configurationFileStream.close();
            } catch (IOException e) {
                log.error("Error closing authenticator file stream.", e);
            }
        }

        this.authenticatorList = authenticatorConfigurationReader.getAuthenticatorList();

        if (this.authenticatorList.isEmpty()) {
            String msg = "No authenticators registered in the system. System cannot function without authenticators";
            log.error(msg);
            throw new ServletException(msg);
        }

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // Firs check whether authenticators are disabled
        if (! AuthenticatorConfigurationReader.isAuthenticationEnabled()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        Authenticator authenticator = getAuthenticator(httpServletRequest);

        if (authenticator == null) {
            //sendUnauthorisedError(servletResponse, "Invalid request. Request does not contain sufficient credentials to authenticate");
            populateUnauthorisedData(servletResponse, "Invalid request. Request does not contain sufficient credentials to authenticate");
        } else {
            if (authenticator.isAuthenticated(httpServletRequest)) {
                // Allow request to flow
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                try {
                    if (!authenticator.authenticate(httpServletRequest)) {
                        //sendUnauthorisedError(servletResponse, "Unauthorised : Provided credentials are not valid.");
                        populateUnauthorisedData(servletResponse, "Invalid request. Request does not contain sufficient credentials to authenticate");
                    } else {
                        // Allow request to flow
                        filterChain.doFilter(servletRequest, servletResponse);
                    }
                } catch (AuthenticationException e) {
                    String msg = "An error occurred while authenticating request.";
                    log.error(msg, e);
                    //sendUnauthorisedError(servletResponse, e.getMessage());
                    populateUnauthorisedData(servletResponse, "Invalid request. Request does not contain sufficient credentials to authenticate");
                }
            }
        }
    }

    public static void sendUnauthorisedError(ServletResponse servletResponse, String message) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    @Override
    public void destroy() {
        if (this.authenticatorList != null) {
            this.authenticatorList.clear();
        }

        this.authenticatorList = null;
    }

    private Authenticator getAuthenticator(HttpServletRequest httpServletRequest) {

        for (Authenticator authenticator : authenticatorList) {
            if (authenticator.canProcess(httpServletRequest)) {
                return authenticator;
            }
        }

        return null;
    }

    /**
     * This method will create a 401 unauthorized response to be sent.
     *
     * @param servletResponse The HTTP response.
     */
    public static void populateUnauthorisedData(ServletResponse servletResponse, String message) {

        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;

        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.addHeader("Server", "Airavata Server");
        httpServletResponse.addHeader("Description", message);
        httpServletResponse.addDateHeader("Date", Calendar.getInstance().getTimeInMillis());
        httpServletResponse.addHeader("WWW-Authenticate", "Basic realm=Airavata");
        httpServletResponse.setContentType("text/html");

    }
}
