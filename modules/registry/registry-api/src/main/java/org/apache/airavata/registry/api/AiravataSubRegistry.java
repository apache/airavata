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

    /**
     * Determines whether this registry is active. If true the registry is ready to do the
     * transaction
     * @return
     */
    public abstract boolean isActive();

    /**
     * Set airavata registry instrance
     * @param registry instance of <code>AiravataRegistry2</code>
     */
    public abstract void setAiravataRegistry(AiravataRegistry2 registry);

    /**
     * Set airavata user
     * @param user current airavata registry user
     */
    public abstract void setAiravataUser(AiravataUser user);

    /**
     * Set gateway of the airavata system
     * @param gateway airavata gateway
     */
    public abstract void setGateway(Gateway gateway);

    /**
     * Set connection url for the registry
     * @param connectionURI connection url for the database
     */
    public void setConnectionURI(URI connectionURI);

    /**
     * Set custom <code>PasswordCallback</code> implementation class
     * @param callback instance of PasswordCallback implementation
     */
    public void setCallback(PasswordCallback callback);

    /**
     * Retrieve the gateway of the airavata system
     * @return gateway
     */
    public abstract Gateway getGateway();

    /**
     * Retrieve the current registry user of the system
     * @return current registry user
     */
    public abstract AiravataUser getAiravataUser();

	/**
	 * Return the version of the Registry API
	 * @return version
	 */
	public Version getVersion();

    /**
     * Retrieve connection URI for the database
     * @return database connection URI
     */
    public URI getConnectionURI();

    /**
     * Retrieve PasswordCallback implementation class
     * @return PasswordCallback impl
     */
    public PasswordCallback getCallback();
	
}