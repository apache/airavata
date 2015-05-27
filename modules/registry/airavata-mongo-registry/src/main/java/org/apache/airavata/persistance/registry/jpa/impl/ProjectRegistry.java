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

import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.mongo.dao.ProjectDao;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
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

    private ProjectDao projectDao;

    public ProjectRegistry(GatewayResource gatewayResource, UserResource user) throws RegistryException {
        this.projectDao = new ProjectDao();

        if (!ResourceUtils.isGatewayExist(gatewayResource.getGatewayId())){
            this.gatewayResource = gatewayResource;
        }else {
            this.gatewayResource = (GatewayResource)ResourceUtils.getGateway(gatewayResource.getGatewayId());
        }
        if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())){
            workerResource = ResourceUtils.addGatewayWorker(gatewayResource, user);
        }else {
            workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayId(),
                    user.getUserName());
        }
    }

    public String addProject (Project project, String gatewayId) throws RegistryException{
        try {
            if (!ResourceUtils.isUserExist(project.getOwner())){
                ResourceUtils.addUser(project.getOwner(), null);
            }
            project.setProjectId(getProjectId(project.getName()));
            projectDao.createProject(project);
            return project.getProjectId();
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
           throw new RegistryException(e);
        }
    }

    private String getProjectId (String projectName){
        String pro = projectName.replaceAll("\\s", "");
        return pro + "_" + UUID.randomUUID();
    }

    public void updateProject (Project project, String projectId) throws RegistryException{
        try {
            UserResource user = (UserResource)ResourceUtils.getUser(project.getOwner());
            if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())){
                workerResource = ResourceUtils.addGatewayWorker(gatewayResource, user);
            }else {
                workerResource = (WorkerResource)ResourceUtils.getWorker(
                        gatewayResource.getGatewayName(), user.getUserName());
            }
            projectDao.updateProject(project);
        }catch (Exception e){
            logger.error("Error while saving project to registry", e);
           throw new RegistryException(e);
        }
    }

    public Project getProject (String projectId) throws RegistryException{
        try {
            return projectDao.getProject(projectId);
        }catch (Exception e){
            logger.error("Error while retrieving project from registry", e);
           throw new RegistryException(e);
        }
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
        try {
                Map<String, String> filters = new HashMap();
                filters.put(fieldName, (String)value);
                return  projectDao.searchProjects(filters, limit, offset, orderByIdentifier, resultOrderType);
        }catch (Exception e){
            logger.error("Error while retrieving project from registry", e);
            throw new RegistryException(e);
        }
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
          try {
                return  projectDao.searchProjects(filters, limit, offset, orderByIdentifier, resultOrderType);
            }catch (Exception e){
                logger.error("Error while retrieving project from registry", e);
                throw new RegistryException(e);
            }

    }

    public List<String> getProjectIds (String fieldName, Object value) throws RegistryException{
        List<String> projectIds = new ArrayList<String>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
                Map<String, String> filters = new HashMap();
                filters.put(fieldName, (String)value);
                projectDao.searchProjects(filters, -1, -1, null, null).stream()
                .forEach(pr->projectIds.add(pr.getProjectId()));
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
            Project project = new Project();
            project.setProjectId(projectId);
            projectDao.deleteProject(project);
        } catch (Exception e) {
            logger.error("Error while removing the project..", e);
           throw new RegistryException(e);
        }
    }

    public boolean isProjectExist(String projectId) throws RegistryException {
        try {
            return projectDao.getProject(projectId) != null;
        } catch (Exception e) {
            logger.error("Error while retrieving project...", e);
           throw new RegistryException(e);
        }
    }
}
