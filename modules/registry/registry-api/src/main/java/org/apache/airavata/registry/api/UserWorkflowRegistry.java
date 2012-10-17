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

import java.util.Map;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;


public interface UserWorkflowRegistry extends AiravataSubRegistry {
	
	public boolean isWorkflowExists(String workflowName) throws RegistryException;
	public void addWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowAlreadyExistsException, RegistryException;
	public void updateWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowDoesNotExistsException, RegistryException;
	
	public String getWorkflowGraphXML(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException;
	public Map<String,String> getWorkflows() throws RegistryException;
	
	public ResourceMetadata getWorkflowMetadata(String workflowName) throws RegistryException;	
	
	public void removeWorkflow(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException;
}
