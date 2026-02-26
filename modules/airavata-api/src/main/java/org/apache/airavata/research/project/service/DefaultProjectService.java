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
package org.apache.airavata.research.project.service;

import java.util.List;
import org.apache.airavata.core.service.AbstractCrudService;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.PaginationUtil;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.apache.airavata.research.project.mapper.ProjectMapper;
import org.apache.airavata.research.project.model.Project;
import org.apache.airavata.research.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ProjectService}.
 *
 * <p>Standard CRUD operations (get/update/delete/listByGateway) are provided by
 * {@link AbstractCrudService}. Domain-specific methods ({@link #createProject},
 * {@link #deleteProject}, {@link #searchProjects}) are implemented here because they carry
 * extra parameters or return types that differ from the generic contract.
 */
@Service("projectServiceFacade")
public class DefaultProjectService extends AbstractCrudService<ProjectEntity, Project>
        implements ProjectService {

    private final ProjectRepository projectRepository;

    public DefaultProjectService(ProjectRepository repository, ProjectMapper mapper) {
        super(repository, mapper);
        this.projectRepository = repository;
    }

    // -------------------------------------------------------------------------
    // AbstractCrudService hooks
    // -------------------------------------------------------------------------

    @Override
    protected String getId(Project model) {
        return model.getProjectId();
    }

    @Override
    protected void setId(Project model, String id) {
        model.setProjectId(id);
    }

    @Override
    protected List<ProjectEntity> findByGateway(String gatewayId) {
        return projectRepository.findByGatewayId(gatewayId);
    }

    @Override
    protected String entityName() {
        return "Project";
    }

    // -------------------------------------------------------------------------
    // Domain-specific operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public String createProject(String gatewayId, Project project) {
        project.setProjectId(IdGenerator.ensureId(project.getProjectId()));
        project.setGatewayId(gatewayId);
        var saved = projectRepository.save(mapper.toEntity(project));
        logger.debug("Created project with id={}", saved.getProjectId());
        return saved.getProjectId();
    }

    @Override
    @Transactional
    public boolean deleteProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            return false;
        }
        projectRepository.deleteById(projectId);
        logger.debug("Deleted {} id={}", entityName(), projectId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> searchProjects(
            String gatewayId, String userName, Object searchFields, int limit, int offset) {
        var pageable = PaginationUtil.toPageRequest(limit, offset);
        return mapper.toModelList(projectRepository.findByGatewayIdOrderByCreatedAtDesc(gatewayId, pageable));
    }
}
