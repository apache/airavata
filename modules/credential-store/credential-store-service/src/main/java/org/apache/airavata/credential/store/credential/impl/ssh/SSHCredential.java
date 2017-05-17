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
package org.apache.airavata.credential.store.credential.impl.ssh;

import org.apache.airavata.credential.store.credential.Credential;
import java.io.Serializable;

/**
 * An SSH Credential class which is an extension of Airavata Credential 
 */
public class SSHCredential extends Credential implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1277154647420198981L;
	
	private byte[] privatekey;
    private byte[] publicKey;
    private String passphrase;
    private String gateway;
    private String username;


    public SSHCredential() {
    }

    public SSHCredential(byte[] privatekey, byte[] publicKey, String passphrase, String gateway,String username) {
        this.privatekey = privatekey;
        this.publicKey = publicKey;
        this.passphrase = passphrase;
        this.gateway = gateway;
        this.username = username;
        this.setPortalUserName(username);
    }

    public byte[] getPrivateKey() {
        return privatekey;
    }

    public void setPrivateKey(byte[] privatekey) {
        this.privatekey = privatekey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] pubKey) {
        this.publicKey = pubKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

}
