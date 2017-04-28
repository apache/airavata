/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProjectRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static Logger logger = LoggerFactory.getLogger(ProjectRegistry.class);

    public ProjectRegistry(GatewayResource gatewayResource, UserResource user) throws RegistryException {
        if (!ExpCatResourceUtils.isGatewayExist(gatewayResource.getGatewayId())){
            this.gatewayResource = gatewayResource;
        }else {
            this.gatewayResource = (GatewayResource) ExpCatResourceUtils.getGateway(gatewayResource.getGatewayId());
        }
        if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())){
            workerResource = ExpCatResourceUtils.addGatewayWorker(gatewayResource, user);
        }else {
            workerResource = (WorkerResource) ExpCatResourceUtils.getWorker(gatewayResource.getGatewayId(),
                    user.getUserName());
        }
    }

    public String addProject (Project project, String gatewayId) throws RegistryException{
        String projectId;
        try {
            if (!ExpCatResourceUtils.isUserExist(project.getOwner(), gatewayId)){
                ExpCatResourceUtils.addUser(project.getOwner(), null, gatewayId);
            }
            ProjectResource projectResource = new ProjectResource();
            projectId = getProjectId(project.getName());
            projectResource.setId(projectId);
            project.setProjectID(projectId);
            projectResource.setName(project.getName());
            projectResource.setDescription(project.getDescription());
            projectResource.setCreationTime(AiravataUtils.getTime(project.getCreationTime()));
            projectResource.setGatewayId(gatewayId);
            WorkerResource worker = new WorkerResource(project.getOwner(), gatewayId);
            projectResource.setWorker(worker);
            projectResource.save();
            ProjectUserResource resource = (ProjectUserResource)projectResource.create(
                    ResourceType.PROJECT_USER);
            resource.setProjectId(project.getProjectID());
            resource.setUserName(project.getOwner());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
           throw new RegistryException(e);
        }
        return projectId;
    }

    private String getProjectId (String projectName){
        String pro = projectName.replaceAll("\\s", "");
        return pro + "_" + UUID.randomUUID();
    }

    public void updateProject (Project project, String projectId) throws RegistryException{
        try {
            ProjectResource existingProject = workerResource.getProject(projectId);
            existingProject.setDescription(project.getDescription());
            existingProject.setName(project.getName());
            WorkerResource worker = new WorkerResource(project.getOwner(), project.getGatewayId());
            existingProject.setWorker(worker);
            existingProject.save();
            ProjectUserResource resource = (ProjectUserResource)existingProject.create(
                    ResourceType.PROJECT_USER);
            resource.setProjectId(projectId);
            resource.setUserName(project.getOwner());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
           throw new RegistryException(e);
        }
    }

    public Project getProject (String projectId) throws RegistryException{
        try {
            ProjectResource project = workerResource.getProject(projectId);
            if (project != null){
                return ThriftDataModelConversion.getProject(project);
            }
        }catch (Exception e){
            logger.error("Error while retrieving project from registry", e);
           throw new RegistryException(e);
        }
        return null;
    }

    /**
     * Get list of projects of the user
     * @param fieldName
     * @param value
     * @return
     * @throws RegistryException
     */
    public List<Project> getProjectList (String fieldName, Object value) throws RegistryException{
        return getProjectList(fieldName, value, -1, -1, null, null);
    }

    /**
     * Get projects list with pagination and result ordering
     * @param fieldName
     * @param value
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<Project> getProjectList (String fieldName, Object value, int limit, int offset,
                                         Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException{
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
            throw new RegistryException(e);
        }
        return projects;
    }

    /**
     * To search projects of user with the given filter criteria. All the matching results will be sent.
     * Results are not ordered in any order
     * @param filters
     * @return
     * @throws RegistryException
     */
    public List<Project> searchProjects (Map<String, String> filters) throws RegistryException{
        return searchProjects(filters, -1, -1, null, null);
    }

    /**
     * To search the projects of user with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC.
     *
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<Project> searchProjects(Map<String, String> filters, int limit,
            int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        Map<String, String> fil = new HashMap<String, String>();
        if (filters != null && filters.size() != 0){
            List<Project> projects = new ArrayList<Project>();
            try {
                for (String field : filters.keySet()){
                    if (field.equals(Constants.FieldConstants.ProjectConstants.PROJECT_NAME)){
                        fil.put(AbstractExpCatResource.ProjectConstants.PROJECT_NAME, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
                        fil.put(AbstractExpCatResource.ProjectConstants.USERNAME, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.DESCRIPTION)){
                        fil.put(AbstractExpCatResource.ProjectConstants.DESCRIPTION, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)){
                        fil.put(AbstractExpCatResource.ProjectConstants.GATEWAY_ID, filters.get(field));
                    }
                }
                List<ProjectResource> projectResources = workerResource
                        .searchProjects(fil, null, limit, offset, orderByIdentifier, resultOrderType);
                if (projectResources != null && !projectResources.isEmpty()){
                    for (ProjectResource pr : projectResources){
                        projects.add(ThriftDataModelConversion.getProject(pr));
                    }
                }
                return projects;
            }catch (Exception e){
                logger.error("Error while retrieving project from registry", e);
                throw new RegistryException(e);
            }
        }
        return null;
    }

    /**
     * To search the projects where the user have access(owner or shared with) with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC.
     *
     * @param accessibleIds
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<Project> searchAllAccessibleProjects(List<String> accessibleIds, Map<String, String> filters, int limit,
                                        int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        Map<String, String> fil = new HashMap<String, String>();
        if (filters != null && filters.size() != 0){
            List<Project> projects = new ArrayList<Project>();
            try {
                for (String field : filters.keySet()){
                    if (field.equals(Constants.FieldConstants.ProjectConstants.PROJECT_NAME)){
                        fil.put(AbstractExpCatResource.ProjectConstants.PROJECT_NAME, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
                        fil.put(AbstractExpCatResource.ProjectConstants.USERNAME, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.DESCRIPTION)){
                        fil.put(AbstractExpCatResource.ProjectConstants.DESCRIPTION, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)){
                        fil.put(AbstractExpCatResource.ProjectConstants.GATEWAY_ID, filters.get(field));
                    }
                }
                List<ProjectResource> projectResources = workerResource
                        .searchProjects(fil, accessibleIds, limit, offset, orderByIdentifier, resultOrderType);
                if (projectResources != null && !projectResources.isEmpty()){
                    for (ProjectResource pr : projectResources){
                        projects.add(ThriftDataModelConversion.getProject(pr));
                    }
                }
                return projects;
            }catch (Exception e){
                logger.error("Error while retrieving project from registry", e);
                throw new RegistryException(e);
            }
        }
        return null;
    }

    public List<String> getProjectIDs (String fieldName, Object value) throws RegistryException{
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
           throw new RegistryException(e);
        }
        return projectIds;
    }

    public void removeProject (String projectId) throws RegistryException {
        try {
            workerResource.removeProject(projectId);
        } catch (Exception e) {
            logger.error("Error while removing the project..", e);
           throw new RegistryException(e);
        }
    }

    public boolean isProjectExist(String projectId) throws RegistryException {
        try {
            return workerResource.isProjectExists(projectId);
        } catch (Exception e) {
            logger.error("Error while retrieving project...", e);
           throw new RegistryException(e);
        }
    }

}
