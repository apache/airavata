/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.service;

import java.util.*;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.Constants;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.interfaces.ResultOrderType;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.model.experiment.proto.ProjectSearchFields;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.repository.NotificationRepository;
import org.apache.airavata.research.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ProjectRegistryService implements ProjectRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ProjectRegistryService.class);

    private final UserProfileProvider userProfileProvider;
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final GatewayRepository gatewayRepository = new GatewayRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    public ProjectRegistryService(UserProfileProvider userProfileProvider) {
        this.userProfileProvider = userProfileProvider;
    }

    @Override
    public String createProject(String gatewayId, Project project) throws Exception {
        try {
            if (!validateString(project.getName()) || !validateString(project.getOwner())) {
                logger.error("Project name and owner cannot be empty...");
                throw new RegistryException("Internal error");
            }
            if (!validateString(gatewayId)) {
                logger.error("Gateway ID cannot be empty...");
                throw new RegistryException("Internal error");
            }
            if (!isGatewayExistInternal(gatewayId)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Internal error");
            }
            String projectId = projectRepository.addProject(project, gatewayId);
            return projectId;
        } catch (Exception e) {
            logger.error("Error while creating the project", e);
            throw new RegistryException("Error while creating the project. More info : " + e.getMessage());
        }
    }

    @Override
    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset) throws Exception {
        if (!validateString(userName)) {
            logger.error("Username cannot be empty. Please provide a valid user..");
            throw new RegistryException("Username cannot be empty. Please provide a valid user..");
        }
        if (!isGatewayExistInternal(gatewayId)) {
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        List<Project> projects = new ArrayList<>();
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) == null) {
                logger.warn("User does not exist in the system. Please provide a valid user..");
                return projects;
            }
            Map<String, String> filters = new HashMap<>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            projects = projectRepository.searchProjects(
                    filters,
                    limit,
                    offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME,
                    ResultOrderType.DESC);
            logger.debug("Airavata retrieved projects for user : " + userName + " and gateway id : " + gatewayId);
            return projects;
        } catch (RegistryException e) {
            logger.error("Error while retrieving projects", e);
            throw new RegistryException("Error while retrieving projects. More info : " + e.getMessage());
        }
    }

    // --- Notification methods ---

    public boolean deleteNotification(String gatewayId, String notificationId) throws Exception {
        try {
            notificationRepository.deleteNotification(notificationId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting notification", e);
            throw new RegistryException("Error while deleting notification. More info : " + e.getMessage());
        }
    }

    public Notification getNotification(String gatewayId, String notificationId) throws Exception {
        try {
            return notificationRepository.getNotification(notificationId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving notification", e);
            throw new RegistryException("Error while retreiving notification. More info : " + e.getMessage());
        }
    }

    public List<Notification> getAllNotifications(String gatewayId) throws Exception {
        try {
            return notificationRepository.getAllGatewayNotifications(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting all notifications", e);
            throw new RegistryException("Error while getting all notifications. More info : " + e.getMessage());
        }
    }

    public boolean updateNotification(Notification notification) throws Exception {
        try {
            notificationRepository.updateNotification(notification);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating notification", e);
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }

    public String createNotification(Notification notification) throws Exception {
        try {
            return notificationRepository.createNotification(notification);
        } catch (RegistryException e) {
            logger.error("Error while creating notification", e);
            throw new RegistryException("Error while creating notification. More info : " + e.getMessage());
        }
    }

    // --- Project CRUD methods ---

    public Project getProject(String projectId) throws Exception {
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            return projectRepository.getProject(projectId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the project", e);
            throw new RegistryException("Error while retrieving the project. More info : " + e.getMessage());
        }
    }

    public boolean deleteProject(String projectId) throws Exception {
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            projectRepository.removeProject(projectId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while removing the project", e);
            throw new RegistryException("Error while removing the project. More info : " + e.getMessage());
        }
    }

    public void updateProject(String projectId, Project updatedProject) throws Exception {
        if (!validateString(projectId)) {
            throw new RegistryException("Project id cannot be empty...");
        }
        try {
            if (!projectRepository.isProjectExist(projectId)) {
                throw new RegistryException(
                        "Project does not exist in the system. Please provide a valid project ID...");
            }
            projectRepository.updateProject(updatedProject, projectId);
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            throw new RegistryException("Error while updating the project. More info : " + e.getMessage());
        }
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception {
        if (!validateString(userName)) {
            throw new RegistryException("Username cannot be empty. Please provide a valid user..");
        }
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Internal error");
        }
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) == null) {
                throw new RegistryException("User does not exist in the system. Please provide a valid user..");
            }
            Map<String, String> regFilters = new HashMap<>();
            regFilters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            for (Map.Entry<ProjectSearchFields, String> entry : filters.entrySet()) {
                if (entry.getKey().equals(ProjectSearchFields.PROJECT_NAME)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, entry.getValue());
                } else if (entry.getKey().equals(ProjectSearchFields.PROJECT_DESCRIPTION)) {
                    regFilters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, entry.getValue());
                }
            }
            if (accessibleProjIds.size() == 0 && !ServerSettings.isEnableSharing()) {
                if (!regFilters.containsKey(DBConstants.Project.OWNER)) {
                    regFilters.put(DBConstants.Project.OWNER, userName);
                }
            }
            return projectRepository.searchAllAccessibleProjects(
                    accessibleProjIds,
                    regFilters,
                    limit,
                    offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME,
                    ResultOrderType.DESC);
        } catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            throw new RegistryException("Error while retrieving projects. More info : " + e.getMessage());
        }
    }

    // --- Private helpers ---

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }

    private boolean isGatewayExistInternal(String gatewayId) throws Exception {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }
}
