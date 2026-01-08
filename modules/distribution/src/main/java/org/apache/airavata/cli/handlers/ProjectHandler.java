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
package org.apache.airavata.cli.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.airavata.common.exception.ProjectNotFoundException;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.common.utils.AiravataUtils;
import org.springframework.stereotype.Service;

@Service
public class ProjectHandler {
    private final RegistryService registryService;

    public ProjectHandler(RegistryService registryService) {
        this.registryService = registryService;
    }

    public String createProject(String name, String owner, String gatewayId, String description) {
        try {
            Project project = new Project();
            project.setName(name);
            project.setOwner(owner);
            project.setGatewayId(gatewayId);
            project.setDescription(description != null ? description : "");
            project.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
            project.setSharedUsers(new ArrayList<>());
            project.setSharedGroups(new ArrayList<>());

            String projectId = registryService.createProject(gatewayId, project);
            System.out.println("✓ Project created: " + projectId + " (" + name + ")");
            return projectId;
        } catch (RegistryServiceException e) {
            throw new RuntimeException("Failed to create project: " + e.getMessage(), e);
        }
    }

    public void updateProject(String projectId, String name, String description, String owner) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            if (name != null) {
                project.setName(name);
            }
            if (description != null) {
                project.setDescription(description);
            }
            if (owner != null) {
                project.setOwner(owner);
            }

            registryService.updateProject(projectId, project);
            System.out.println("✓ Project updated: " + projectId);
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to update project: " + e.getMessage(), e);
        }
    }

    public void deleteProject(String projectId) {
        try {
            boolean deleted = registryService.deleteProject(projectId);
            if (deleted) {
                System.out.println("✓ Project deleted: " + projectId);
            } else {
                System.out.println("⚠ Project not found or could not be deleted: " + projectId);
            }
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to delete project: " + e.getMessage(), e);
        }
    }

    public List<Project> listProjects(String gatewayId) {
        try {
            if (gatewayId == null || gatewayId.isEmpty()) {
                throw new RuntimeException("Gateway ID is required for listing projects");
            }
            List<Project> projects = registryService.searchProjects(
                    gatewayId, "", new ArrayList<>(), new HashMap<>(), Integer.MAX_VALUE, 0);
            if (projects == null || projects.isEmpty()) {
                System.out.println("No projects found.");
            } else {
                System.out.println("Projects:");
                for (Project project : projects) {
                    System.out.println("  " + project.getProjectID() + " -> " + project.getName() + " (owner: "
                            + project.getOwner() + ", gateway: " + project.getGatewayId() + ")");
                }
            }
            return projects;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list projects: " + e.getMessage(), e);
        }
    }

    public Project getProject(String projectId) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            System.out.println("Project Details:");
            System.out.println("  ID: " + project.getProjectID());
            System.out.println("  Name: " + project.getName());
            System.out.println("  Owner: " + project.getOwner());
            System.out.println("  Gateway: " + project.getGatewayId());
            System.out.println("  Description: " + (project.getDescription() != null ? project.getDescription() : ""));
            System.out.println("  Creation Time: " + new java.util.Date(project.getCreationTime()));
            if (project.getSharedUsers() != null && !project.getSharedUsers().isEmpty()) {
                System.out.println("  Shared Users: " + String.join(", ", project.getSharedUsers()));
            }
            if (project.getSharedGroups() != null && !project.getSharedGroups().isEmpty()) {
                System.out.println("  Shared Groups: " + String.join(", ", project.getSharedGroups()));
            }
            return project;
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to get project: " + e.getMessage(), e);
        }
    }

    public void addUserToProject(String projectId, String userId, String gatewayId) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            String sharingUserId = userId + "@" + gatewayId;
            if (project.getSharedUsers() == null) {
                project.setSharedUsers(new ArrayList<>());
            }
            if (!project.getSharedUsers().contains(sharingUserId)) {
                project.getSharedUsers().add(sharingUserId);
                registryService.updateProject(projectId, project);
                System.out.println("✓ User " + userId + " added to project " + projectId);
            } else {
                System.out.println("User " + userId + " is already in project " + projectId);
            }
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to add user to project: " + e.getMessage(), e);
        }
    }

    public void removeUserFromProject(String projectId, String userId, String gatewayId) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            String sharingUserId = userId + "@" + gatewayId;
            if (project.getSharedUsers() != null && project.getSharedUsers().contains(sharingUserId)) {
                project.getSharedUsers().remove(sharingUserId);
                registryService.updateProject(projectId, project);
                System.out.println("✓ User " + userId + " removed from project " + projectId);
            } else {
                System.out.println("User " + userId + " is not in project " + projectId);
            }
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to remove user from project: " + e.getMessage(), e);
        }
    }

    public void addGroupToProject(String projectId, String groupId) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            if (project.getSharedGroups() == null) {
                project.setSharedGroups(new ArrayList<>());
            }
            if (!project.getSharedGroups().contains(groupId)) {
                project.getSharedGroups().add(groupId);
                registryService.updateProject(projectId, project);
                System.out.println("✓ Group " + groupId + " added to project " + projectId);
            } else {
                System.out.println("Group " + groupId + " is already in project " + projectId);
            }
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to add group to project: " + e.getMessage(), e);
        }
    }

    public void removeGroupFromProject(String projectId, String groupId) {
        try {
            Project project = registryService.getProject(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            if (project.getSharedGroups() != null && project.getSharedGroups().contains(groupId)) {
                project.getSharedGroups().remove(groupId);
                registryService.updateProject(projectId, project);
                System.out.println("✓ Group " + groupId + " removed from project " + projectId);
            } else {
                System.out.println("Group " + groupId + " is not in project " + projectId);
            }
        } catch (RegistryServiceException | ProjectNotFoundException e) {
            throw new RuntimeException("Failed to remove group from project: " + e.getMessage(), e);
        }
    }
}
