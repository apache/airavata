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

package org.apache.airavata.persistance.registry.mongo.repository;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.User;
import org.apache.airavata.persistance.registry.mongo.dao.ProjectDao;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProjectRepository {
    private final static Logger logger = LoggerFactory.getLogger(ProjectRepository.class);

    private Gateway gateway;
    private User user;

    private ProjectDao projectDao;
    private GatewayRepository gatewayRepository;
    private UserRepository userRepository;

    public ProjectRepository(Gateway gateway, User user) throws RegistryException {
        this.projectDao = new ProjectDao();
        this.userRepository = new UserRepository();

        if(gatewayRepository.getGateway(gateway.getGatewayId()) == null){
            gatewayRepository.addGateway(gateway);
        }
        //Todo check for gateway workers

        this.gateway = gateway;
        this.user = user;
    }

    public String addProject (Project project, String gatewayId) throws RegistryException{
        try {
            if (!userRepository.isUserExists(project.getOwner())){
                userRepository.addUser(new User(project.getOwner()));
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
            //check project owner is a gateway user else add gateway user
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
