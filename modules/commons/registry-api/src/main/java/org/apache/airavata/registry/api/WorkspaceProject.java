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

public class WorkspaceProject{
	private String projectName;
	private ProjectsRegistry projectsRegistry;
	
	public WorkspaceProject(String projectName, ProjectsRegistry registry) {
		setProjectName(projectName);
		setProjectsRegistry(registry);
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public ProjectsRegistry getProjectsRegistry() {
		return projectsRegistry;
	}

	public void setProjectsRegistry(ProjectsRegistry projectsRegistry) {
		this.projectsRegistry = projectsRegistry;
	}
	
	public void createExperiment(AiravataExperiment experiment){
		getProjectsRegistry().addExperiment(getProjectName(), experiment);
	}
	
	public List<AiravataExperiment> getExperiments(){
		return getProjectsRegistry().getExperiments(getProjectName());
	}
	
	public List<AiravataExperiment> getExperiments(Date from, Date to){
		return getProjectsRegistry().getExperiments(getProjectName(),from, to);
	}
}
