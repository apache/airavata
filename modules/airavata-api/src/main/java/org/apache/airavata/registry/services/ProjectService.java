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
package org.apache.airavata.registry.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ProjectMapper;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.repositories.expcatalog.ProjectRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(
            @Qualifier("expCatalogProjectRepository") ProjectRepository projectRepository,
            ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public boolean isProjectExist(String projectId) throws RegistryException {
        return projectRepository.existsById(projectId);
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        // Generate project ID if not already set
        if (project.getProjectID() == null || project.getProjectID().isEmpty()) {
            project.setProjectID(org.apache.airavata.common.utils.AiravataUtils.getId(project.getName()));
        }
        ProjectEntity entity = projectMapper.toEntity(project);
        entity.setGatewayId(gatewayId);
        ProjectEntity saved = projectRepository.save(entity);
        return saved.getProjectID();
    }

    public Project getProject(String projectId) throws RegistryException {
        ProjectEntity entity = projectRepository.findById(projectId).orElse(null);
        if (entity == null) return null;
        return projectMapper.toModel(entity);
    }

    public void removeProject(String projectId) throws RegistryException {
        projectRepository.deleteById(projectId);
    }

    public void updateProject(Project project, String projectId) throws RegistryException {
        // Load existing entity to preserve required fields like gatewayId and creationTime
        ProjectEntity existingEntity = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new RegistryException("Project not found: " + projectId));

        ProjectEntity entity = projectMapper.toEntity(project);
        entity.setProjectID(projectId);
        // Preserve gatewayId if not set in the update
        if (entity.getGatewayId() == null || entity.getGatewayId().isEmpty()) {
            entity.setGatewayId(existingEntity.getGatewayId());
        }
        // Preserve creationTime if not set in the update
        if (entity.getCreationTime() == null) {
            entity.setCreationTime(existingEntity.getCreationTime());
        }
        projectRepository.save(entity);
    }

    public List<Project> searchProjects(
            Map<String, String> filters,
            int limit,
            int offset,
            String orderByIdentifier,
            ResultOrderType resultOrderType)
            throws RegistryException {
        // TODO: Implement complex search using Criteria API
        // For now, return all projects filtered by gatewayId if present
        List<ProjectEntity> entities;
        if (filters != null && filters.containsKey("GATEWAY_ID")) {
            entities = projectRepository.findByGatewayId(filters.get("GATEWAY_ID"));
        } else {
            entities = projectRepository.findAll();
        }
        return projectMapper.toModelList(entities);
    }

    public List<Project> searchAllAccessibleProjects(
            List<String> accessibleProjectIds,
            Map<String, String> filters,
            int limit,
            int offset,
            String orderByIdentifier,
            ResultOrderType resultOrderType)
            throws RegistryException {
        // TODO: Implement complex search using Criteria API with accessibleProjectIds filter
        // For now, return projects from accessibleProjectIds
        List<Project> result = new ArrayList<>();
        for (String projectId : accessibleProjectIds) {
            ProjectEntity entity = projectRepository.findById(projectId).orElse(null);
            if (entity != null) {
                result.add(projectMapper.toModel(entity));
            }
        }
        return result;
    }
}
