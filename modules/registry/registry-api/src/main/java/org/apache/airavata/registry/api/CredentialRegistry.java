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

package org.apache.airavata.registry.api;

import org.apache.airavata.registry.api.exception.RegistryException;

public interface CredentialRegistry extends AiravataSubRegistry{

		
	/**
	 * Checks whether a credential exists in the credential store for given gateway and token
	 * @param String gatewayId
	 * @param String tokenId
	 * @return a boolean (true is credential exists, false if not)
	 * @throws RegistryException
	 */
	
	public boolean isCredentialExist(String gatewayId, String tokenId) throws RegistryException;
	
	/**
	 * Get the public key for a credential in the credential store for given gateway and token
	 * @param String gatewayId
	 * @param String tokenId
	 * @return String The public key of the credential
	 * @throws RegistryException
	 */
	public String getCredentialPublicKey(String gatewayId, String tokenId) throws RegistryException;
	
	/**
	 * Creates a new SSH credential for given gateway and token, encrypts it with a randomly 
	 * generated password and stores it in the credential store 
	 * @param String gatewayId
	 * @param String tokenId
	 * @return String The public key of the credential
	 * @throws RegistryException
	 */
	public String createCredential(String gatewayId, String tokenId) throws RegistryException;
	
	/**
	 * Creates a new SSH credential for given gateway and token, encrypts it with the given password 
	 * and stores it in the credential store
	 * @param String gatewayId
	 * @param String tokenId
	 * @param String username
	 * @return String The public key of the credential
	 * @throws RegistryException
	 */
	public String createCredential(String gatewayId, String tokenId, String username) throws RegistryException;
    
}