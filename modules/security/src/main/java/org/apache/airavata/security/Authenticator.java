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
package org.apache.airavata.security;

import org.w3c.dom.Node;

/**
 * A generic interface to do request authentication. Specific authenticator will implement authenticate method.
 */
@SuppressWarnings("UnusedDeclaration")
public interface Authenticator {

    /**
     * Authenticates the request with given credentials.
     * 
     * @param credentials
     *            Credentials can be a session ticket, password or session id.
     * @return <code>true</code> if request is successfully authenticated else <code>false</code>.
     * @throws AuthenticationException
     *             If a system error occurred during authentication process.
     */
    boolean authenticate(Object credentials) throws AuthenticationException;

    /**
     * Checks whether given user is already authenticated.
     * 
     * @param credentials
     *            The token to be authenticated.
     * @return <code>true</code> if token is already authenticated else <code>false</code>.
     */
    boolean isAuthenticated(Object credentials);

    /**
     * Says whether current authenticator can handle given credentials.
     * 
     * @param credentials
     *            Credentials used during authentication.
     * @return <code>true</code> is can authenticate else <code>false</code>.
     */
    boolean canProcess(Object credentials);

    /**
     * Gets the priority of this authenticator.
     * 
     * @return Higher the priority higher the precedence of selecting the authenticator.
     */
    int getPriority();

    /**
     * Returns the authenticator name. Each authenticator is associated with an identifiable name.
     * 
     * @return The authenticator name.
     */
    String getAuthenticatorName();

    /**
     * Authenticator specific configurations goes into this method.
     * 
     * @param node
     *            An XML configuration node.
     * @throws RuntimeException
     *             If an error occurred while configuring the authenticator.
     */
    void configure(Node node) throws RuntimeException;

    /**
     * Return <code>true</code> if current authenticator is enabled. Else <code>false</code>.
     * 
     * @return <code>true</code> if enabled.
     */
    boolean isEnabled();

    /**
     * User store that should be used by this authenticator. When authenticating a request authenticator should use the
     * user store set by this method.
     * 
     * @param userStore
     *            The user store to be used.
     */
    void setUserStore(UserStore userStore);

    /**
     * Gets the user store used by this authenticator.
     * 
     * @return The user store used by this authenticator.
     */
    UserStore getUserStore();

}
