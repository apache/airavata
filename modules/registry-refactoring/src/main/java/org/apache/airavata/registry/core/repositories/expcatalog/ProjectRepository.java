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

import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
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
        String projectID = getProjectId(project.getName());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProjectEntity projectEntity = mapper.map(project, ProjectEntity.class);
        projectEntity.setProjectID(projectID);
        projectEntity.setGatewayId(gatewayId);

        if (!isProjectExist(projectID)) {
            logger.debug("Checking if the Project already exists");
            projectEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(projectEntity));
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        return saveProjectData(project, gatewayId);
    }

    public void updateProject(Project project, String projectId) throws RegistryException {
        saveProjectData(project, null);
    }

    private String getProjectId(String projectName){
        String pro = projectName.replaceAll("\\s", "");
        return pro + "_" + UUID.randomUUID();
    }

    public Project getProject(String projectId) throws RegistryException {
        return get(projectId);
    }

    public List<Project> getProjectList(String fieldName, Object value) throws RegistryException {
        return getProjectList(fieldName, value, -1, -1, null, null);
    }

    public List<Project> getProjectList(String fieldName, Object value, int limit, int offset,
                                         Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException{
        if (fieldName.equals(DBConstants.Project.OWNER)) {
            logger.debug("Checking if the field name is owner");
            List<Project> projectList = select(QueryConstants.GET_ALL_PROJECTS, 0);

            if (!projectList.isEmpty() && projectList != null) {
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

        if (!projectList.isEmpty() && projectList != null) {
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
        String query = "SELECT DISTINCT P FROM " + ProjectEntity.class.getSimpleName() + " P WHERE ";

        if (!filters.isEmpty() && filters != null) {

            for (String field : filters.keySet()) {

                if (field.equals(DBConstants.Project.GATEWAY_ID)) {
                    logger.debug("Filter Projects by Gateway ID");
                    query += "P.gatewayId LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Project.OWNER)) {
                    logger.debug("Filter Projects by Owner");
                    query += "P.owner LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Project.PROJECT_NAME)) {
                    logger.debug("Filter Projects by Project Name");
                    query += "P.name LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Project.DESCRIPTION)) {
                    logger.debug("Filter Projects by Description");
                    query += "P.description LIKE :" + filters.get(field) + " AND ";
                }

                else {
                    logger.error("Unsupported field name for Project module.");
                    throw new IllegalArgumentException("Unsupported field name for Project module.");
                }

            }

        }

        if (accessibleProjectIds != null && !accessibleProjectIds.isEmpty()) {
            logger.debug("Filter Projects by Accessible Project IDs");
            query += "P.projectId IN (";
            for(String projectId : accessibleProjectIds) {
                query += (":" + projectId + ",");
            }
            query = query.substring(0, query.length() - 1) + ")";
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 5);
        }

        List<Project> projectList = select(query, offset);
        return projectList;
    }

    public boolean isProjectExist(String projectId) throws RegistryException {
        return isExists(projectId);
    }

    public void removeProject(String projectId) throws RegistryException {
        delete(projectId);
    }

}