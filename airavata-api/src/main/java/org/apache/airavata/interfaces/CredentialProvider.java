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
package org.apache.airavata.interfaces;

import org.apache.airavata.model.credential.store.proto.PasswordCredential;
import org.apache.airavata.model.credential.store.proto.SSHCredential;

/**
 * SPI contract for credential operations required by the execution engine.
 *
 * <p>This interface decouples the execution module from the credential store's handler
 * and repository implementations. Implementations are expected to be provided by the
 * credential module and injected into execution components.
 */
public interface CredentialProvider {

    /**
     * Retrieve an SSH credential by its token and gateway.
     *
     * @param tokenId   the credential token identifier
     * @param gatewayId the gateway identifier
     * @return the SSH credential, or {@code null} if not found
     * @throws Exception if a credential store error occurs
     */
    SSHCredential getSSHCredential(String tokenId, String gatewayId) throws Exception;

    /**
     * Retrieve a password credential by its token and gateway.
     *
     * @param tokenId   the credential token identifier
     * @param gatewayId the gateway identifier
     * @return the password credential, or {@code null} if not found
     * @throws Exception if a credential store error occurs
     */
    PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws Exception;

    /**
     * Store an SSH credential and return its generated token.
     *
     * @param sshCredential the SSH credential to store
     * @return the generated credential token
     * @throws Exception if a credential store error occurs
     */
    String addSSHCredential(SSHCredential sshCredential) throws Exception;

    /**
     * Delete an SSH credential by its token and gateway.
     *
     * @param tokenId   the credential token identifier
     * @param gatewayId the gateway identifier
     * @return {@code true} if the credential was deleted
     * @throws Exception if a credential store error occurs
     */
    boolean deleteSSHCredential(String tokenId, String gatewayId) throws Exception;
}
