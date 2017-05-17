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

/**
 * An interface which can be used to authorise accessing resources.
 */
@SuppressWarnings("UnusedDeclaration")
public interface Authoriser {

    /**
     * Checks whether user has sufficient privileges to perform action on the given resource.
     * 
     * @param userName
     *            The user who is performing the action.
     * @param resource
     *            The resource which user is trying to access.
     * @param action
     *            The action (GET, PUT etc ...)
     * @return Returns <code>true</code> if user is authorised to perform the action, else false.
     */
    boolean isAuthorised(String userName, String resource, String action);

}
