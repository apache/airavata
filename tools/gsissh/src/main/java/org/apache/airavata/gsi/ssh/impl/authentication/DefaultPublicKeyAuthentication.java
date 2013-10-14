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

package org.apache.airavata.gsi.ssh.impl.authentication;

import org.apache.airavata.gsi.ssh.api.authentication.SSHPublicKeyAuthentication;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 11:44 AM
 */

/**
 * Default public key authentication.
 * Note : This is only a sample implementation.
 */
public class DefaultPublicKeyAuthentication implements SSHPublicKeyAuthentication {

    private byte[] privateKey;
    private byte[] publicKey;
    private String passPhrase = null;

    public DefaultPublicKeyAuthentication(byte[] priv, byte[] pub) {
        this.privateKey = priv;
        this.publicKey = pub;
    }

    public DefaultPublicKeyAuthentication(byte[] priv, byte[] pub, String pass) {
        this.privateKey = priv;
        this.publicKey = pub;
        this.passPhrase = pass;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void bannerMessage(String message) {
        System.out.println(message);
    }

    public byte[] getPrivateKey(String userName, String hostName) {
        return privateKey;
    }

    public byte[] getPublicKey(String userName, String hostName) {
        return publicKey;
    }
}
