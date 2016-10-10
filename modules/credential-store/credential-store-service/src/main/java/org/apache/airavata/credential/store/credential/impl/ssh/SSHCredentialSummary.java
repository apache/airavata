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

package org.apache.airavata.credential.store.credential.impl.ssh;

import org.apache.airavata.credential.store.credential.Credential;

import java.io.Serializable;

/**
 * An SSH Credential Summary class which is an extension of Airavata Credential
 */
public class SSHCredentialSummary extends Credential implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 1235236447420198981L;

    private byte[] publicKey;
    private String gateway;
    private String username;
    private String token;
    private String description;

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public SSHCredentialSummary() {
    }

    public SSHCredentialSummary(byte[] publicKey, String token, String gateway, String username) {
        this.publicKey = publicKey;
        this.gateway = gateway;
        this.username = username;
        this.token = token;
        this.setPortalUserName(username);
    }


    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] pubKey) {
        this.publicKey = pubKey;
    }

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

}
