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

package org.apache.airavata.credential.store;

/**
 * This interface provides an API for Credential Store.
 * Provides methods to manipulate credential store data.
 */
public interface CredentialStore {

    /**
     * Gets the admin portal user name who  retrieved given community user for
     * given portal user name.
     * @param gatewayName The gateway name
     * @param communityUser The community user name.
     * @return The portal user name who requested given community user credentials.
     */
    String getPortalUser(String gatewayName, String communityUser) throws CredentialStoreException;

    /**
     * Gets audit information related to given gateway name and community
     * user name.
     * @param gatewayName The gateway name.
     * @param communityUser The community user name.
     * @return AuditInfo object.
     */
    AuditInfo getAuditInfo(String gatewayName, String communityUser) throws CredentialStoreException;

    /**
     * Updates the community user contact email address.
     * @param gatewayName The gateway name.
     * @param communityUser The community user name.
     * @param email The new email address.
     */
    void updateCommunityUserEmail(String gatewayName, String communityUser, String email) throws CredentialStoreException;

    /**
     * Will remove credentials for the given gateway id and community user.
     * @param gatewayName The gateway Id
     * @param communityUser The community user name.
     * @throws CredentialStoreException If an error occurred while retrieving data.
     */
    void removeCredentials(String gatewayName, String communityUser) throws CredentialStoreException;


}
