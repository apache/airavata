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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.CredentialStoreManager;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;

public class CredentialStoreManagerImpl implements CredentialStoreManager{

	private AiravataClient client;
	
	public CredentialStoreManagerImpl(AiravataClient client) {
		this.client = client;
	}
	
	@Override
	public boolean isCredentialExist(String gatewayId, String tokenId) throws AiravataAPIInvocationException {
		if(gatewayId==null || gatewayId.isEmpty() || tokenId == null || tokenId.isEmpty()) {
    		return false;
    	}
		try {
			return client.getRegistryClient().isCredentialExist(gatewayId, tokenId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getCredentialPublicKey(String gatewayId, String tokenId) throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().getCredentialPublicKey(gatewayId, tokenId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String createCredential(String gatewayId, String tokenId) throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().createCredential(gatewayId, tokenId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String createCredential(String gatewayId, String tokenId, String username) throws AiravataAPIInvocationException {
		try {
			return client.getRegistryClient().createCredential(gatewayId, tokenId, username);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

}
