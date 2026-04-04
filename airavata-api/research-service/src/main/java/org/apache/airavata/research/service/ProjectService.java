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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.exception.ServiceNotFoundException;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.experiment.proto.ProjectSearchFields;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.sharing.registry.models.proto.EntitySearchField;
import org.apache.airavata.sharing.registry.models.proto.SearchCondition;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRegistry projectRegistry;
    private final SharingFacade sharingHandler;

    public ProjectService(ProjectRegistry projectRegistry, SharingFacade sharingHandler) {
        this.projectRegistry = projectRegistry;
        this.sharingHandler = sharingHandler;
    }

    public String createProject(RequestContext ctx, String gatewayId, Project project) throws ServiceException {
        try {
            String projectId = projectRegistry.createProject(gatewayId, project);

            if (SharingHelper.isSharingEnabled()) {
                try {
                    final String domainId = project.getGatewayId();
                    sharingHandler.createEntity(
                            projectId,
                            domainId,
                            domainId + ":" + "PROJECT",
                            project.getOwner() + "@" + domainId,
                            project.getName(),
                            project.getDescription(),
                            null);
                } catch (Exception ex) {
                    logger.error("Rolling back project creation Proj ID : {}", projectId, ex);
                    projectRegistry.deleteProject(projectId);
                    throw new ServiceException("Failed to create entry for project in Sharing Registry", ex);
                }
            }

            logger.debug("Created project with id {} for gateway {}", projectId, gatewayId);
            return projectId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating the project: " + e.getMessage(), e);
        }
    }

    public void updateProject(RequestContext ctx, String projectId, Project updatedProject) throws ServiceException {
        try {
            Project existingProject = projectRegistry.getProject(projectId);
            if (existingProject == null) {
                throw new ServiceNotFoundException("Project " + projectId + " does not exist");
            }

            if (!ctx.getUserId().equals(existingProject.getOwner())
                    || !ctx.getGatewayId().equals(existingProject.getGatewayId())) {
                if (SharingHelper.isSharingEnabled()) {
                    String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                    if (!sharingHandler.userHasAccess(
                            ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to update this resource");
                    }
                } else {
                    throw new ServiceAuthorizationException("User does not have permission to update this resource");
                }
            }

            if (!updatedProject.getOwner().equals(existingProject.getOwner())) {
                throw new ServiceException("Owner of a project cannot be changed");
            }
            if (!updatedProject.getGatewayId().equals(existingProject.getGatewayId())) {
                throw new ServiceException("Gateway ID of a project cannot be changed");
            }

            projectRegistry.updateProject(projectId, updatedProject);
            logger.debug("Updated project with id {}", projectId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while updating the project: " + e.getMessage(), e);
        }
    }

    public boolean deleteProject(RequestContext ctx, String projectId) throws ServiceException {
        try {
            Project existingProject = projectRegistry.getProject(projectId);
            if (existingProject == null) {
                throw new ServiceNotFoundException("Project " + projectId + " does not exist");
            }

            if (!ctx.getUserId().equals(existingProject.getOwner())
                    || !ctx.getGatewayId().equals(existingProject.getGatewayId())) {
                if (SharingHelper.isSharingEnabled()) {
                    String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                    if (!sharingHandler.userHasAccess(
                            ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to delete this resource");
                    }
                } else {
                    throw new ServiceAuthorizationException("User does not have permission to delete this resource");
                }
            }

            boolean ret = projectRegistry.deleteProject(projectId);
            logger.debug("Deleted project with id {}", projectId);
            return ret;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while deleting the project: " + e.getMessage(), e);
        }
    }

    public Project getProject(RequestContext ctx, String projectId) throws ServiceException {
        try {
            Project project = projectRegistry.getProject(projectId);
            if (project == null) {
                throw new ServiceNotFoundException("Project " + projectId + " does not exist");
            }

            if (ctx.getUserId().equals(project.getOwner()) && ctx.getGatewayId().equals(project.getGatewayId())) {
                return project;
            }

            if (SharingHelper.isSharingEnabled()) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException("User does not have permission to access this resource");
                }
                return project;
            }

            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving the project: " + e.getMessage(), e);
        }
    }

    public List<Project> getUserProjects(RequestContext ctx, String gatewayId, String userName, int limit, int offset)
            throws ServiceException {
        try {
            if (SharingHelper.isSharingEnabled()) {
                List<String> accessibleProjectIds = new ArrayList<>();
                List<SearchCriteria> filters = new ArrayList<>();
                filters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":PROJECT")
                        .build());
                accessibleProjectIds.addAll(
                        sharingHandler.searchEntityIds(gatewayId, userName + "@" + gatewayId, filters, 0, -1));

                if (accessibleProjectIds.isEmpty()) {
                    return Collections.emptyList();
                }
                return projectRegistry.searchProjects(
                        gatewayId, userName, accessibleProjectIds, new HashMap<>(), limit, offset);
            } else {
                return projectRegistry.getUserProjects(gatewayId, userName, limit, offset);
            }
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving projects: " + e.getMessage(), e);
        }
    }

    public List<Project> searchProjects(
            RequestContext ctx,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws ServiceException {
        try {
            List<String> accessibleProjIds = new ArrayList<>();

            if (SharingHelper.isSharingEnabled()) {
                List<SearchCriteria> sharingFilters = new ArrayList<>();
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":PROJECT")
                        .build());
                accessibleProjIds.addAll(sharingHandler.searchEntityIds(
                        gatewayId, userName + "@" + gatewayId, sharingFilters, 0, Integer.MAX_VALUE));

                if (accessibleProjIds.isEmpty()) {
                    return Collections.emptyList();
                }
            }

            return projectRegistry.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
        } catch (Exception e) {
            throw new ServiceException("Error while searching projects: " + e.getMessage(), e);
        }
    }
}
