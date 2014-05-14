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

package org.apache.airavata.persistance.registry.jpa.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectUserResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.persistance.registry.jpa.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static Logger logger = LoggerFactory.getLogger(ProjectRegistry.class);

    public ProjectRegistry(GatewayResource gatewayResource, UserResource user) {
        if (!ResourceUtils.isGatewayExist(gatewayResource.getGatewayName())){
            this.gatewayResource = gatewayResource;
        }else {
            this.gatewayResource = (GatewayResource)ResourceUtils.getGateway(gatewayResource.getGatewayName());
        }
        if (!ResourceUtils.isUserExist(user.getUserName())){
            workerResource = new WorkerResource(user.getUserName(), gatewayResource);
            workerResource.save();
        }else {
            workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayName(), user.getUserName());
        }
    }

    public String addProject (Project project) throws Exception{
        String projectId;
        try {
            if (!ResourceUtils.isUserExist(project.getOwner())){
                ResourceUtils.addUser(project.getOwner(), null);
            }
            ProjectResource projectResource = new ProjectResource();
            projectId = getProjectId(project.getName());
            projectResource.setId(projectId);
            project.setProjectID(projectId);
            projectResource.setName(project.getName());
            projectResource.setDescription(project.getDescription());
            projectResource.setCreationTime(AiravataUtils.getTime(project.getCreationTime()));
            projectResource.setGateway(workerResource.getGateway());
            WorkerResource worker = new WorkerResource(project.getOwner(), workerResource.getGateway());
            projectResource.setWorker(worker);
            projectResource.save();
            ProjectUserResource resource = (ProjectUserResource)projectResource.create(ResourceType.PROJECT_USER);
            resource.setProjectId(project.getProjectID());
            resource.setUserName(project.getOwner());
            resource.save();
            List<String> sharedGroups = project.getSharedGroups();
            if (sharedGroups != null && !sharedGroups.isEmpty()){
                for (String group : sharedGroups){
                    //TODO - add shared groups
                    logger.info("Groups are not supported at the moment...");
                }
            }

            List<String> sharedUsers = project.getSharedUsers();
            if (sharedUsers != null && !sharedUsers.isEmpty()){
                for (String username : sharedUsers){
                    ProjectUserResource pr = (ProjectUserResource)projectResource.create(ResourceType.PROJECT_USER);
                    pr.setUserName(username);
                    pr.save();
                }
            }
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
            throw new Exception(e);
        }
        return projectId;
    }

    private String getProjectId (String projectName){
        String pro = projectName.replaceAll("\\s", "");
        return pro + "_" + UUID.randomUUID();
    }

    public void updateProject (Project project, String projectId) throws Exception{
        try {
            ProjectResource existingProject = workerResource.getProject(projectId);
            existingProject.setDescription(project.getDescription());
            existingProject.setCreationTime(AiravataUtils.getTime(project.getCreationTime()));
            existingProject.setGateway(gatewayResource);
            UserResource user = (UserResource)ResourceUtils.getUser(project.getOwner());
            ResourceUtils.addGatewayWorker(gatewayResource, user);
            WorkerResource worker = new WorkerResource(project.getOwner(), gatewayResource);
            existingProject.setWorker(worker);
            existingProject.save();
            ProjectUserResource resource = (ProjectUserResource)existingProject.create(ResourceType.PROJECT_USER);
            resource.setProjectId(projectId);
            resource.setUserName(project.getOwner());
            resource.save();
            List<String> sharedGroups = project.getSharedGroups();
            if (sharedGroups != null && !sharedGroups.isEmpty()){
                for (String group : sharedGroups){
                    //TODO - add shared groups
                    logger.info("Groups are not supported at the moment...");
                }
            }

            List<String> sharedUsers = project.getSharedUsers();
            if (sharedUsers != null && !sharedUsers.isEmpty()){
                for (String username : sharedUsers){
                    ProjectUserResource pr = (ProjectUserResource)existingProject.create(ResourceType.PROJECT_USER);
                    pr.setUserName(username);
                    pr.save();
                }
            }
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
            throw new Exception(e);
        }
    }

    public Project getProject (String projectId) throws Exception{
        try {
            ProjectResource project = workerResource.getProject(projectId);
            if (project != null){
                return ThriftDataModelConversion.getProject(project);
            }
        }catch (Exception e){
            logger.error("Error while retrieving project from registry", e);
            throw new Exception(e);
        }
        return null;
    }

    public List<Project> getProjectList (String fieldName, Object value) throws Exception{
        List<Project> projects = new ArrayList<Project>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
                workerResource.setUser((String)value);
                List<ProjectResource> projectList = workerResource.getProjects();
                if (projectList != null && !projectList.isEmpty()){
                    for (ProjectResource pr : projectList){
                        projects.add(ThriftDataModelConversion.getProject(pr));
                    }
                }
                return projects;
            }
        }catch (Exception e){
            logger.error("Error while retrieving project from registry", e);
            throw new Exception(e);
        }
        return projects;
    }

    public List<String> getProjectIDs (String fieldName, Object value) throws Exception{
        List<String> projectIds = new ArrayList<String>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
                workerResource.setUser((String)value);
                List<ProjectResource> projectList = workerResource.getProjects();
                if (projectList != null && !projectList.isEmpty()){
                    for (ProjectResource pr : projectList){
                        projectIds.add(pr.getName());
                    }
                }
                return projectIds;
            }
        }catch (Exception e){
            logger.error("Error while retrieving projects from registry", e);
            throw new Exception(e);
        }
        return projectIds;
    }

    public void removeProject (String projectId) throws Exception {
        try {
            workerResource.removeProject(projectId);
        } catch (Exception e) {
            logger.error("Error while removing the project..", e);
            throw new Exception(e);
        }
    }

    public boolean isProjectExist(String projectId) throws Exception {
        try {
            return workerResource.isProjectExists(projectId);
        } catch (Exception e) {
            logger.error("Error while retrieving project...", e);
            throw new Exception(e);
        }
    }

}
