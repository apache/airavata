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

import com.github.dozermapper.core.Mapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ProjectRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public boolean isProjectExist(String projectId) throws RegistryException {
        return projectRepository.existsById(projectId);
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProjectEntity entity = mapper.map(project, ProjectEntity.class);
        entity.setGatewayId(gatewayId);
        ProjectEntity saved = projectRepository.save(entity);
        return saved.getProjectID();
    }

    public Project getProject(String projectId) throws RegistryException {
        ProjectEntity entity = projectRepository.findById(projectId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, Project.class);
    }

    public void removeProject(String projectId) throws RegistryException {
        projectRepository.deleteById(projectId);
    }

    public void updateProject(Project project, String projectId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProjectEntity entity = mapper.map(project, ProjectEntity.class);
        entity.setProjectID(projectId);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<Project> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, Project.class)));
        return result;
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        for (String projectId : accessibleProjectIds) {
            ProjectEntity entity = projectRepository.findById(projectId).orElse(null);
            if (entity != null) {
                result.add(mapper.map(entity, Project.class));
            }
        }
        return result;
    }
}
