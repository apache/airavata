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

import java.net.URI;
import java.util.Observable;


public abstract class AiravataRegistry2 extends Observable implements DescriptorRegistry, ProjectsRegistry, PublishedWorkflowRegistry, UserWorkflowRegistry, ConfigurationRegistry, ProvenanceRegistry,OrchestratorRegistry, UserRegistry, CredentialRegistry{
	private Gateway gateway;
	private AiravataUser user;
	
	protected static final int SERVICE_TTL=180;
	
	protected void preInitialize(URI connectionURI, Gateway gateway, AiravataUser user, PasswordCallback callback) {
		setConnectionURI(connectionURI);
		setGateway(gateway);
		setUser(user);
		setCallback(callback);
	}
	
	/**
	 * Initialize the Airavata Registry
	 * @throws RegistryException
	 */
	protected abstract void initialize() throws RegistryException;
	
	public Gateway getGateway() {
		return gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	public AiravataUser getUser() {
		return user;
	}

	public void setUser(AiravataUser user) {
		this.user = user;
	}
	
}
