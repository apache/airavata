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
 * An interface to wrap the functionality of a user store. A user store is place where we keep user attribute
 * information. Usually this contains, user id, user name, password etc ...
 * We also authenticate users against the credentials stored in a user store. In addition to user attributes
 * we also store role information and group information.
 * This interface provide methods to manipulated data in a user store.
 * Such operations are as follows,
 * <ol>
 *     <li>authenticate user</li>
 *     <li>add user</li>
 *     <li>delete user</li>
 *     <li>add a role</li>
 *     <li>delete a role</li>
 *     <li>... etc ...</li>
 * </ol>
 */
public interface UserStore {

    /**
     * Checks whether given user exists in the user store and its credentials match with the credentials stored
     * in the user store.
     * @param userName Name of the user to authenticate.
     * @param credentials User credentials as an object. User credentials may not be a string always.
     * @return True if user exists in the user store and its credentials match with the credentials in user store.
     *          <code>false</code> else.
     * @throws UserStoreException if a system wide error occurred while authenticating the user.
     */
    boolean authenticate(String userName, Object credentials) throws UserStoreException;

    /**
     * Authenticates a user using a token.
     * @param credentials The token information.
     * @return <code>true</code> if authentication successful else <code>false</code>.
     * @throws UserStoreException if a system wide error occurred while authenticating the user.
     */
    boolean authenticate(Object credentials) throws UserStoreException;

    /**
     * This method will do necessary configurations of the user store.
     * @param node An XML configuration node.
     * @throws RuntimeException If an error occurred while configuring the authenticator.
     */
    void configure(Node node) throws UserStoreException;

}
