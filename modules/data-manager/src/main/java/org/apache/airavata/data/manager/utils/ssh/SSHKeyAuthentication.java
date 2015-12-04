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
package org.apache.airavata.data.manager.utils.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHKeyAuthentication {
    private final static Logger logger = LoggerFactory.getLogger(SSHKeyAuthentication.class);

    private String userName;
    private String privateKeyFilePath;
    private String publicKeyFilePath;
    private String passphrase;
    private String knownHostsFilePath;
    private String strictHostKeyChecking; // yes or no

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPrivateKeyFilePath() {
        return privateKeyFilePath;
    }

    public void setPrivateKeyFilePath(String privateKeyFilePath) {
        this.privateKeyFilePath = privateKeyFilePath;
    }

    public String getPublicKeyFilePath() {
        return publicKeyFilePath;
    }

    public void setPublicKeyFilePath(String publicKeyFilePath) {
        this.publicKeyFilePath = publicKeyFilePath;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getKnownHostsFilePath() {
        return knownHostsFilePath;
    }

    public void setKnownHostsFilePath(String knownHostsFilePath) {
        this.knownHostsFilePath = knownHostsFilePath;
    }

    public String getStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public void setStrictHostKeyChecking(String strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }
}