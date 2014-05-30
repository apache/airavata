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

import java.util.List;
import java.util.Map;

import org.apache.airavata.registry.api.exception.RegException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;


public interface PublishedWorkflowRegistry extends AiravataSubRegistry {
	
	public boolean isPublishedWorkflowExists(String workflowName) throws RegException;
	public void publishWorkflow(String workflowName, String publishWorkflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegException;
	public void publishWorkflow(String workflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegException;
	
	public String getPublishedWorkflowGraphXML(String workflowName) throws PublishedWorkflowDoesNotExistsException, RegException;
	public List<String> getPublishedWorkflowNames() throws RegException;
	public Map<String,String> getPublishedWorkflows() throws RegException;
	public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) throws RegException;
	
	public void removePublishedWorkflow(String workflowName)throws PublishedWorkflowDoesNotExistsException, RegException;
}
