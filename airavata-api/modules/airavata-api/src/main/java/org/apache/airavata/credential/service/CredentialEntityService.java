/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.service;

import java.util.List;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.Credential;

/**
 * Service contract for managing credential entities with encryption/decryption support.
 */
public interface CredentialEntityService {

    /**
     * Add or update credentials.
     *
     * @param gatewayId the gateway ID
     * @param credential the credential to save
     * @throws CredentialStoreException if an error occurs
     */
    void saveCredential(String gatewayId, Credential credential) throws CredentialStoreException;

    /**
     * Check if this credential is referenced by RESOURCE_BINDING or CREDENTIAL_ALLOCATION_PROJECT.
     */
    boolean hasReferences(String credentialId);

    /**
     * Delete credentials.
     * Fails if the credential is still referenced by resource bindings or allocation projects.
     */
    void deleteCredential(String gatewayId, String credentialId) throws CredentialStoreException;

    /**
     * Check if a credential exists.
     */
    boolean credentialExists(String gatewayId, String credentialId);

    /**
     * Get credential by gateway ID and credential ID.
     */
    Credential getCredential(String gatewayId, String credentialId) throws CredentialStoreException;

    /**
     * Get gateway ID by credential ID.
     */
    String getGatewayId(String credentialId) throws CredentialStoreException;

    /**
     * Get all credentials for a gateway.
     */
    List<Credential> getCredentials(String gatewayId) throws CredentialStoreException;

    /**
     * Get credentials for a gateway with specific credential IDs.
     * If accessibleCredentialIds is null, returns all credentials for the gateway.
     * If accessibleCredentialIds is empty, returns an empty list.
     */
    List<Credential> getCredentials(String gatewayId, List<String> accessibleCredentialIds)
            throws CredentialStoreException;

    /**
     * Get credential IDs for a gateway owned by the given user.
     */
    List<String> getCredentialIdsByGatewayIdAndUserId(String gatewayId, String userId);

    /**
     * Get all credentials.
     */
    List<Credential> getAllCredentials() throws CredentialStoreException;
}
