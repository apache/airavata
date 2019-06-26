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
package org.apache.airavata.credential.store.store;

import org.apache.airavata.credential.store.credential.AuditInfo;
import org.apache.airavata.credential.store.credential.Credential;

import java.util.List;

/**
 * This interface provides an API for Credential Store. Provides methods to manipulate credential store data.
 */
public interface CredentialReader {

    /**
     * Retrieves the credential from the credential store.
     * 
     * @param gatewayId
     *            The gateway id
     * @param tokenId
     *            The token id associated with the credential
     * @return The Credential object associated with the token.
     * @throws CredentialStoreException
     *             If an error occurred while retrieving a credential.
     */
    Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException;

    /**
     * Gets the admin portal user name who retrieved given community user for given portal user name.
     * 
     * @param gatewayName
     *            The gateway name
     * @param tokenId
     *            The issued token id.
     * @return The portal user name who requested given community user credentials.
     */
    String getPortalUser(String gatewayName, String tokenId) throws CredentialStoreException;

    /**
     * Gets audit information related to given gateway name and community user name.
     * 
     * @param gatewayName
     *            The gateway name.
     * @param tokenId
     *            The community user name.
     * @return CertificateAuditInfo object.
     */
    AuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException;

    /**
     * Gets all the credential records.
     * @return All credential records as a list
     * @throws CredentialStoreException If an error occurred while retrieving credentials.
     */
    public List<Credential> getAllCredentials() throws CredentialStoreException;

    public List<Credential> getAllCredentialsPerGateway(String gatewayId) throws CredentialStoreException;

    public List<Credential> getAllAccessibleCredentialsPerGateway(String gatewayId, List<String> accessibleTokenIds) throws CredentialStoreException;

    public List<Credential> getAllCredentialsPerUser(String userName) throws CredentialStoreException;
    /**
     * Updates the community user contact email address.
     *
     * @param gatewayName
     *            The gateway name.
     * @param communityUser
     *            The community user name.
     * @param email
     *            The new email address.
     */
    void updateCommunityUserEmail(String gatewayName, String communityUser, String email)
            throws CredentialStoreException;

    /**
     * Will remove credentials for the given gateway id and community user.
     * 
     * @param gatewayName
     *            The gateway Id
     * @param tokenId
     *            The issued token id.
     * @throws CredentialStoreException
     *             If an error occurred while retrieving data.
     */
    void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException;
    
    /**
     * Retrieves gatewayID from the credential store.
     * 
     * @param tokenId
     *            The token id associated with the credential
     * @return The Credential object associated with the token.
     * @throws CredentialStoreException
     *             If an error occurred while retrieving a credential.
     */
    String getGatewayID(String tokenId) throws CredentialStoreException;

}
