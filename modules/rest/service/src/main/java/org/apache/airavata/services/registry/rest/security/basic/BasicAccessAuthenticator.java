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

package org.apache.airavata.services.registry.rest.security.basic;

import org.apache.airavata.common.context.RequestContext;
import org.apache.airavata.common.context.WorkflowContext;
import org.apache.airavata.security.AbstractAuthenticator;
import org.apache.airavata.security.AuthenticationException;
import org.apache.airavata.security.UserStoreException;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This authenticator handles basic access authentication requests. In basic access authentication
 * we get user name and password as HTTP headers. The password is encoded with base64.
 * More information @link{http://en.wikipedia.org/wiki/Basic_access_authentication}
 */
public class BasicAccessAuthenticator extends AbstractAuthenticator {


    private static final String AUTHENTICATOR_NAME = "BasicAccessAuthenticator";

    /**
     * Header names
     */
    private static final String AUTHORISATION_HEADER_NAME = "Authorization";
    public static final String USER_IN_SESSION = "userName";

    public BasicAccessAuthenticator() {
        super(AUTHENTICATOR_NAME);
    }

    private String decode(String encoded) {
        return new String(Base64.decodeBase64(encoded.getBytes()));
    }

    /**
     * Returns user name and password as an array. The first element is user name and second is password.
     *
     * @param httpServletRequest The servlet request.
     * @return User name password pair as an array.
     * @throws AuthenticationException If an error occurred while extracting user name and password.
     */
    private String[] getUserNamePassword(HttpServletRequest httpServletRequest) throws AuthenticationException {

        String basicHeader = httpServletRequest.getHeader(AUTHORISATION_HEADER_NAME);

        if (basicHeader == null) {
            throw new AuthenticationException("Authorization Required");
        }

        String[] userNamePasswordArray = basicHeader.split(" ");

        if (userNamePasswordArray == null || userNamePasswordArray.length != 2) {
            throw new AuthenticationException("Authorization Required");
        }

        String decodedString = decode(userNamePasswordArray[1]);

        String[] array = decodedString.split(":");

        if (array == null || array.length != 2) {
            throw new AuthenticationException("Authorization Required");
        }

        return array;

    }

    @Override
    protected boolean doAuthentication(Object credentials) throws AuthenticationException {
        if (this.getUserStore() == null) {
            throw new AuthenticationException("Authenticator is not initialized. Error processing request.");
        }

        if (credentials == null)
            return false;

        HttpServletRequest httpServletRequest = (HttpServletRequest) credentials;

        String[] array = getUserNamePassword(httpServletRequest);

        String userName = array[0];
        String password = array[1];

        try {
            return this.getUserStore().authenticate(userName, password);

        } catch (UserStoreException e) {
            throw new AuthenticationException("Error querying database for session information.", e);
        }
    }

    protected void addUserToSession(String userName, HttpServletRequest servletRequest) {

        if (servletRequest.getSession() != null) {
            servletRequest.getSession().setAttribute(USER_IN_SESSION, userName);
        }

        // Add user to context - TODO We may need to abstract out this
        RequestContext requestContext = new RequestContext();
        requestContext.setUserIdentity(userName);

        WorkflowContext.set(requestContext);
    }

    @Override
    public void onSuccessfulAuthentication(Object authenticationInfo) {

        HttpServletRequest httpServletRequest = (HttpServletRequest) authenticationInfo;

        try {
            String[] array = getUserNamePassword(httpServletRequest);

            StringBuilder stringBuilder = new StringBuilder("User : ");

            if (array != null) {

                addUserToSession(array[0], httpServletRequest);

                stringBuilder.append(array[0]).append(" successfully logged into system at ").append(getCurrentTime());
                log.info(stringBuilder.toString());

            } else {
                log.error("System error occurred while extracting user name after authentication. " +
                        "Couldn't extract user name from the request.");
            }
        } catch (AuthenticationException e) {
            log.error("System error occurred while extracting user name after authentication.", e);
        }

    }

    @Override
    public void onFailedAuthentication(Object authenticationInfo) {

        HttpServletRequest httpServletRequest = (HttpServletRequest) authenticationInfo;

        try {
            String[] array = getUserNamePassword(httpServletRequest);

            StringBuilder stringBuilder = new StringBuilder("User : ");

            if (array != null) {

                stringBuilder.append(array[0]).append(" Failed login attempt to system at ").append(getCurrentTime());
                log.warn(stringBuilder.toString());

            } else {
                stringBuilder.append("Failed login attempt to system at ").append(getCurrentTime()).append( ". User unknown.");
                log.warn(stringBuilder.toString());
            }
        } catch (AuthenticationException e) {
            log.error("System error occurred while extracting user name after authentication.", e);
        }
    }

    @Override
    public boolean isAuthenticated(Object credentials) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) credentials;

        HttpSession httpSession = httpServletRequest.getSession();

        return httpSession != null && httpSession.getAttribute(USER_IN_SESSION) != null;

    }

    @Override
    public boolean canProcess(Object credentials) {

        HttpServletRequest httpServletRequest = (HttpServletRequest) credentials;

        return (httpServletRequest.getHeader(AUTHORISATION_HEADER_NAME) != null);
    }

    @Override
    public void configure(Node node) throws RuntimeException {

        /**
         <specificConfigurations>
         <database>
         <jdbcUrl></jdbcUrl>
         <databaseDriver></databaseDriver>
         <userName></userName>
         <password></password>
         <userTableName></userTableName>
         <userNameColumnName></userNameColumnName>
         <passwordColumnName></passwordColumnName>
         </database>
         </specificConfigurations>
         */

        try {
            this.getUserStore().configure(node);
        } catch (UserStoreException e) {
            throw new RuntimeException("Error while configuring authenticator user store", e);
        }

    }

}
