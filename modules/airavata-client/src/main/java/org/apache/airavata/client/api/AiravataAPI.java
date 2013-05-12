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

package org.apache.airavata.client.api;

import java.net.URI;

import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.registry.api.PasswordCallback;


/**
 * This is the base interface for AiravataAPI which contains all the base methods for Airavata API
 */
public interface AiravataAPI {

    /**
     * Returns the AiravataManager - manage Airavata related configurations
     * @return
     */
	public AiravataManager getAiravataManager();

    /**
     * Returns the ApplicationManager
     * @return
     */
	public ApplicationManager getApplicationManager();

    /**
     * Returns the WorkflowManager
     * @return
     */
	public WorkflowManager getWorkflowManager();

    /**
     * Returns the ProvenanceManager
     * @return
     */
	public ProvenanceManager getProvenanceManager();

    /**
     * Returns the UserManager
     * @return
     */
	public UserManager getUserManager();

    /**
     * Returns the ExecutionManager
     * @return
     */
	public ExecutionManager getExecutionManager();

    /**
     * Returns the Current User
     * @return
     */
	public String getCurrentUser();
	
	/**
	 * Get Airavata API version
	 * @return
	 */
	public Version getVersion();

    /**
     * Gets the DescriptorBuilder. DescriptorBuilder is a helper class to create various descriptors.
     * E.g :- Build HostDescriptors, ServiceDescriptors etc ...
     * @return DescriptorBuilder.
     */
    public DescriptorBuilder getDescriptorBuilder();

	public void setCurrentUser(String alternateUsername);

	public void setCallBack(PasswordCallback callback);

	public void setRegitryURI(URI registryURL);

	public void initialize() throws AiravataAPIInvocationException;

	public void setGateway(String gateway);

    /**
     * Gets the gateway id.
     * @return The gateway id.
     */
    public String getGateway();

}