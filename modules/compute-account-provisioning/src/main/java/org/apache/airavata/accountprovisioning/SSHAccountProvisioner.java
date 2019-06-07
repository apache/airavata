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
package org.apache.airavata.accountprovisioning;

import java.util.Map;

/**
 * An SSHAccountProvisioner is capable of installing an Airavata-managed SSH public key onto a compute host for a user.
 * SSHAccountProvisioners may also optionally provide the capability to create accounts directly on the compute host
 * for the user. An SSHAccountProvisioner's {@link SSHAccountProvisionerProvider} provides some methods to define the
 * configuration params that this SSHAccountProvisioner requires as well as some metadata method to describe the
 * capabilities of this SSHAccountProvisioner.
 */
public interface SSHAccountProvisioner {

    /**
     * Initialize this SSHAccountProvisioner.
     * @param config
     */
    void init(Map<ConfigParam, String> config);

    /**
     * Return true if this user has an account on the compute host
     * @param userId the Airavata user id
     * @return
     * @throws InvalidUsernameException
     */
    boolean hasAccount(String userId) throws InvalidUsernameException;

    /**
     * Create an account for the user if no account exists.  May throw {@link UnsupportedOperationException} if
     * unimplemented for this SSHAccountProvisioner.
     * @param userId the Airavata user id
     * @param sshPublicKey the public key part of an Airavata managed SSH credential
     * @return username
     * @throws InvalidUsernameException
     */
    String createAccount(String userId, String sshPublicKey) throws InvalidUsernameException;

    /**
     * Return true if this sshPublicKey has been installed for this user account and all other related setup tasks are complete.
     * @param userId
     * @param sshPublicKey
     * @return
     * @throws InvalidUsernameException
     */
    boolean isSSHAccountProvisioningComplete(String userId, String sshPublicKey) throws InvalidUsernameException;

    /**
     * Install an SSH key for the user on the compute host.
     * @param userId the Airavata user id
     * @param sshPublicKey the public key part of an Airavata managed SSH credential
     * @return username
     * @throws InvalidUsernameException
     */
    String installSSHKey(String userId, String sshPublicKey) throws InvalidUsernameException;

    /**
     * Get the scratch location that should be created for the user. Note: this method doesn't create the scratch
     * location on the compute host, it merely determines a path to a good scratch location to be used by a gateway
     * on behalf of the user.
     *
     * @param userId
     * @return a filesystem path (e.g. "/N/scratch/username/some-gateway")
     * @throws InvalidUsernameException
     */
    String getScratchLocation(String userId) throws InvalidUsernameException;
}
