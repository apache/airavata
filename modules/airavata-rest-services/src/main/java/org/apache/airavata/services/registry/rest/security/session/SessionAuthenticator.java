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

package org.apache.airavata.services.registry.rest.security.session;

import org.apache.airavata.security.AbstractAuthenticator;
import org.apache.airavata.security.AuthenticationException;
import org.apache.airavata.security.UserStoreException;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This authenticator will authenticate requests based on a session (NOT HTTP Session) id stored
 * in the database.
 */
public class SessionAuthenticator extends AbstractAuthenticator {

    private static final String AUTHENTICATOR_NAME = "SessionAuthenticator";

    private static final String SESSION_TICKET = "sessionTicket";

    public SessionAuthenticator() {
        super(AUTHENTICATOR_NAME);
    }

    @Override
    public boolean doAuthentication(Object credentials) throws AuthenticationException {

       if (credentials == null)
            return false;

        HttpServletRequest httpServletRequest = (HttpServletRequest)credentials;
        String sessionTicket = httpServletRequest.getHeader(SESSION_TICKET);
        try {
            return this.getUserStore().authenticate(sessionTicket);
        } catch (UserStoreException e) {
            throw new AuthenticationException("Error querying database for session information.", e);
        }
    }

    @Override
    public boolean canProcess(Object credentials) {

        if (credentials instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) credentials;

            String ticket = request.getHeader(SESSION_TICKET);
            if (ticket != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSuccessfulAuthentication(Object authenticationInfo) {

        HttpServletRequest httpServletRequest = (HttpServletRequest)authenticationInfo;
        String sessionTicket = httpServletRequest.getHeader(SESSION_TICKET);

        // Add sessionTicket to http session
        HttpSession httpSession = httpServletRequest.getSession();

        if (httpSession != null) {
            httpSession.setAttribute(SESSION_TICKET, sessionTicket);
        }

        log.info("A request with a session ticket is successfully logged in.");

    }

    @Override
    public void onFailedAuthentication(Object authenticationInfo) {
        log.warn("Failed attempt to login.");
    }

    @Override
    public void configure(Node node) throws RuntimeException {

        try {
            this.getUserStore().configure(node);
        } catch (UserStoreException e) {
            throw new RuntimeException("Error while configuring authenticator user store", e);
        }
    }


    @Override
    public boolean isAuthenticated(Object credentials) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)credentials;

        if (httpServletRequest.getSession() != null) {
            String sessionTicket = (String)httpServletRequest.getSession().getAttribute(SESSION_TICKET);
            return (sessionTicket != null);
        }

        return false;

    }
}
