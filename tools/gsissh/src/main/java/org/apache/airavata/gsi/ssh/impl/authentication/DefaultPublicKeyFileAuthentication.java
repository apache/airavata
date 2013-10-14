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

import org.apache.airavata.gsi.ssh.api.authentication.SSHPublicKeyFileAuthentication;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 11:40 AM
 */

/**
 * Default public key authentication using files.
 * Note : This is only a sample implementation.
 */
public class DefaultPublicKeyFileAuthentication implements SSHPublicKeyFileAuthentication {

    private String publicKeyFile;
    private String privateKeyFile;
    private String passPhrase = null;

    public DefaultPublicKeyFileAuthentication(String pubFile, String privFile) {
        this.publicKeyFile = pubFile;
        this.privateKeyFile = privFile;

    }

    public DefaultPublicKeyFileAuthentication(String pubFile, String privFile, String pass) {
        this.publicKeyFile = pubFile;
        this.privateKeyFile = privFile;
        this.passPhrase = pass;

    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void bannerMessage(String message) {
        System.out.println(message);
    }

    public String getPublicKeyFile(String userName, String hostName) {
        return publicKeyFile;
    }

    public String getPrivateKeyFile(String userName, String hostName) {
        return privateKeyFile;
    }
}
