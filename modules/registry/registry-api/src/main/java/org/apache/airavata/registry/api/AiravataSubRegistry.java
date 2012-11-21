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

import java.net.URI;

import org.apache.airavata.common.utils.Version;

public interface AiravataSubRegistry {

	public abstract void setAiravataRegistry(AiravataRegistry2 registry);

	public abstract void setAiravataUser(AiravataUser user);

	public abstract void setGateway(Gateway gateway);

    public abstract Gateway getGateway();

    public abstract AiravataUser getAiravataUser();

	/**
	 * Determines whether this registry is active. If true the registry is ready to do the 
	 * transaction
	 * @return
	 */
	public abstract boolean isActive();
	
	/**
	 * Return the version of the Registry API
	 * @return
	 */
	public Version getVersion();
	
	public void setConnectionURI(URI connectionURI);
	
	public URI getConnectionURI();
	
	public void setCallback(PasswordCallback callback);
	
	public PasswordCallback getCallback();
	
}