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
package org.apache.airavata.gfac.core.authentication;

/**
 * Public key authentication for vanilla SSH.
 * The public key and private key are returned as byte arrays. Useful when we store private key/public key
 * in a secure storage such as credential store. API user should implement this.
 */
public interface SSHPublicKeyAuthentication extends AuthenticationInfo {

    /**
     * Gets the public key as byte array.
     * @param userName The user who is trying to SSH
     * @param hostName The host which user wants to connect to.
     * @return The public key as a byte array.
     */
    byte[] getPrivateKey(String userName, String hostName);

    /**
     * Gets the private key as byte array.
     * @param userName The user who is trying to SSH
     * @param hostName The host which user wants to connect to.
     * @return The private key as a byte array.
     */
    byte[] getPublicKey(String userName, String hostName);

}
