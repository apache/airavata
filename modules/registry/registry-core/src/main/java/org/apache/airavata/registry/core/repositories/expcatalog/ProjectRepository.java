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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ProjectRepository extends ExpCatAbstractRepository<Project, ProjectEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProjectRepository.class);

    public ProjectRepository() { super(Project.class, ProjectEntity.class); }

    protected String saveProjectData(Project project, String gatewayId) throws RegistryException {
        ProjectEntity projectEntity = saveProject(project, gatewayId);
        return projectEntity.getProjectID();
    }

    protected ProjectEntity saveProject(Project project, String gatewayId) throws RegistryException {
        if (project.getProjectID() == null || project.getProjectID().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Project's ProjectId");
            project.setProjectID(AiravataUtils.getId(project.getName()));
        }

        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProjectEntity projectEntity = mapper.map(project, ProjectEntity.class);

        if (project.getGatewayId() == null) {
            logger.debug("Setting the Project's GatewayId");
            projectEntity.setGatewayId(gatewayId);
        }

        if (!isProjectExist(projectEntity.getProjectID())) {
            logger.debug("Checking if the Project already exists");
            projectEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(projectEntity));
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        return saveProjectData(project, gatewayId);
    }

    public void updateProject(Project project, String projectId) throws RegistryException {
        project.setProjectID(projectId);
        saveProjectData(project, project.getGatewayId());
    }

    public Project getProject(String projectId) throws RegistryException {
        return get(projectId);
    }

    public List<Project> getProjectList(String fieldName, Object value) throws RegistryException {
        return getProjectList(fieldName, value, -1, 0, null, null);
    }

    public List<Project> getProjectList(String fieldName, Object value, int limit, int offset,
                                         Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException{
        Map<String, Object> queryParameters = new HashMap<>();

        if (fieldName.equals(Constants.FieldConstants.ProjectConstants.OWNER)) {
            logger.debug("Checking if the field name is owner");
            queryParameters.put(DBConstants.Project.OWNER, value);
            List<Project> projectList = select(QueryConstants.GET_ALL_PROJECTS_FOR_OWNER, limit, offset, queryParameters);

            if (projectList != null && !projectList.isEmpty()) {
                logger.debug("The retrieved list is not empty or null");
                return projectList;
            }

        }

        else {
            logger.error("Unsupported field name for Project module.");
            throw new IllegalArgumentException("Unsupported field name for Project module.");
        }

        return null;
    }

    public List<String> getProjectIDs(String fieldName, Object value) throws RegistryException {
        List<Project> projectList = getProjectList(fieldName, value);
        List<String> projectIds = new ArrayList<>();

        if (projectList != null && !projectList.isEmpty()) {
            logger.debug("The retrieved list is not empty or null");
            for (Project project : projectList) {
                projectIds.add(project.getProjectID());
            }
        }

        return projectIds;
    }

    public List<Project> searchProjects(Map<String, String> filters, int limit,
                                        int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        return searchAllAccessibleProjects(null, filters, limit, offset, orderByIdentifier, resultOrderType);
    }

    public List<Project> searchAllAccessibleProjects(List<String> accessibleProjectIds, Map<String, String> filters, int limit,
                                                     int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        String query = "SELECT P FROM " + ProjectEntity.class.getSimpleName() + " P WHERE ";
        Map<String, Object> queryParameters = new HashMap<>();

        if (filters == null || !filters.containsKey(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)) {
            logger.error("GatewayId is required");
            throw new RegistryException("GatewayId is required");
        }

        for (String field : filters.keySet()) {

            if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)) {
                logger.debug("Filter Projects by Gateway ID");
                queryParameters.put(DBConstants.Project.GATEWAY_ID, filters.get(field));
                query += "P.gatewayId LIKE :" + DBConstants.Project.GATEWAY_ID + " AND ";
            }

            else if (field.equals(Constants.FieldConstants.ProjectConstants.OWNER)) {
                logger.debug("Filter Projects by Owner");
                queryParameters.put(DBConstants.Project.OWNER, filters.get(field));
                query += "P.owner LIKE :" + DBConstants.Project.OWNER + " AND ";
            }

            else if (field.equals(Constants.FieldConstants.ProjectConstants.PROJECT_NAME)) {
                logger.debug("Filter Projects by Project Name");
                queryParameters.put(DBConstants.Project.PROJECT_NAME, filters.get(field));
                query += "P.name LIKE :" + DBConstants.Project.PROJECT_NAME + " AND ";
            }

            else if (field.equals(Constants.FieldConstants.ProjectConstants.DESCRIPTION)) {
                logger.debug("Filter Projects by Description");
                queryParameters.put(DBConstants.Project.DESCRIPTION, filters.get(field));
                query += "P.description LIKE :" + DBConstants.Project.DESCRIPTION + " AND ";
            }

            else {
                logger.error("Unsupported field name for Project module.");
                throw new IllegalArgumentException("Unsupported field name for Project module.");
            }

        }

        if (accessibleProjectIds != null && !accessibleProjectIds.isEmpty()) {
            logger.debug("Filter Projects by Accessible Project IDs");
            queryParameters.put(DBConstants.Project.ACCESSIBLE_PROJECT_IDS, accessibleProjectIds);
            query += "P.projectID IN :" + DBConstants.Project.ACCESSIBLE_PROJECT_IDS;
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 5);
        }

        if (orderByIdentifier != null && resultOrderType != null && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
            String order = (resultOrderType == ResultOrderType.ASC) ? "ASC" : "DESC";
            query += " ORDER BY P." + DBConstants.Project.CREATION_TIME + " " + order;
        }

        List<Project> projectList = select(query, limit, offset, queryParameters);
        return projectList;
    }

    public boolean isProjectExist(String projectId) throws RegistryException {
        return isExists(projectId);
    }

    public void removeProject(String projectId) throws RegistryException {
        delete(projectId);
    }

}
