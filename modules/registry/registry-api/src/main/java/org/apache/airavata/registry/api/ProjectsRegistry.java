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

import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;

public interface ProjectsRegistry extends AiravataSubRegistry {
	
	//------------Project management
	public boolean isWorkspaceProjectExists(String projectName) throws RegistryException;
	public boolean isWorkspaceProjectExists(String projectName, boolean createIfNotExists) throws RegistryException;
	public void addWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectAlreadyExistsException, RegistryException;
	public void updateWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectDoesNotExistsException, RegistryException;
	public void deleteWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException;
	public WorkspaceProject getWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException;
	public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException;
	
	//------------Experiment management
	public void addExperiment(String projectName, AiravataExperiment experiment) throws WorkspaceProjectDoesNotExistsException, ExperimentDoesNotExistsException, RegistryException;
	public void removeExperiment(String experimentId) throws ExperimentDoesNotExistsException;
	public List<AiravataExperiment> getExperiments() throws RegistryException;
	public List<AiravataExperiment> getExperiments(String projectName)throws RegistryException;
	public List<AiravataExperiment> getExperiments(Date from, Date to)throws RegistryException;
	public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) throws RegistryException;
}
