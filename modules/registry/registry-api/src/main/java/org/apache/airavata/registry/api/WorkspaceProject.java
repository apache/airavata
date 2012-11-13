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

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class WorkspaceProject{
	private String projectName;
    private Gateway gateway;
    private AiravataUser airavataUser;

    @XmlTransient
    private ProjectsRegistry projectsRegistry;

    public WorkspaceProject() {
    }

    public WorkspaceProject(String projectName, ProjectsRegistry registry) {
		setProjectName(projectName);
		setProjectsRegistry(registry);
        setGateway(registry.getGateway());
        setAiravataUser(registry.getAiravataUser());
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
	
	public void createExperiment(AiravataExperiment experiment) throws RegistryException{
		getProjectsRegistry().addExperiment(getProjectName(), experiment);
	}
	
	public List<AiravataExperiment> getExperiments() throws RegistryException{
		return getProjectsRegistry().getExperiments(getProjectName());
	}
	
	public List<AiravataExperiment> getExperiments(Date from, Date to) throws RegistryException{
		return getProjectsRegistry().getExperiments(getProjectName(),from, to);
	}

    public Gateway getGateway() {
        return gateway;
    }

    public AiravataUser getAiravataUser() {
        return airavataUser;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public void setAiravataUser(AiravataUser airavataUser) {
        this.airavataUser = airavataUser;
    }
}
