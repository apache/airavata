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
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 2:39 PM
 */

/**
 * Abstracts out common methods for SSH key authentication.
 */
public class SSHKeyAuthentication implements AuthenticationInfo {

	private String userName;
	private byte[] privateKey;
	private byte[] publicKey;
	private String passphrase;
	private String knownHostsFilePath;
	private String strictHostKeyChecking; // yes or no

	public SSHKeyAuthentication() {
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userNameIn) {
		this.userName = userNameIn;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKeyIn) {
		this.privateKey = privateKeyIn;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKeyIn) {
		this.publicKey = publicKeyIn;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphraseIn) {
		this.passphrase = passphraseIn;
	}

	public String getKnownHostsFilePath() {
		return knownHostsFilePath;
	}

	public void setKnownHostsFilePath(String knownHostsFilePathIn) {
		this.knownHostsFilePath = knownHostsFilePathIn;
	}

	public String getStrictHostKeyChecking() {
		return strictHostKeyChecking;
	}

	public void setStrictHostKeyChecking(String strictHostKeyCheckingIn) {
		this.strictHostKeyChecking = strictHostKeyCheckingIn;
	}
}
