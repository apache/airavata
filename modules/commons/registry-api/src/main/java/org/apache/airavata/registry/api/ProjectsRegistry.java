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

public interface ProjectsRegistry extends AiravataSubRegistry {
	
	//------------Project management
	public void addWorkspaceProject(WorkspaceProject project);
	public void updateWorkspaceProject(WorkspaceProject project);
	public void deleteWorkspaceProject(String projectName);
	public WorkspaceProject getWorkspaceProject(String projectName);
	
	//------------Experiment management
	public void createExperiment(String projectName, AiravataExperiment experiment);
	public void removeExperiment(String experimentId);
	public List<AiravataExperiment> getExperiments();
	public List<AiravataExperiment> getExperiments(String projectName);
	public List<AiravataExperiment> getExperiments(Date from, Date to);
	public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to);
}
