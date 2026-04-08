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
package org.apache.airavata.research.util;

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.model.ProjectEntity;
import org.apache.airavata.util.AiravataUtils;

/**
 * Lightweight test helper that creates/removes projects without depending on research-service's ProjectRepository.
 * Used by orchestration-service tests that need project fixtures for experiment/process/task/job testing.
 */
public class ProjectTestHelper extends AbstractRepository<Project, ProjectEntity, String> {

    public ProjectTestHelper() {
        super(Project.class, ProjectEntity.class);
    }

    @Override
    protected Project toModel(ProjectEntity entity) {
        return Project.newBuilder()
                .setProjectId(entity.getProjectID())
                .setOwner(entity.getOwner() != null ? entity.getOwner() : "")
                .setGatewayId(entity.getGatewayId() != null ? entity.getGatewayId() : "")
                .setName(entity.getName() != null ? entity.getName() : "")
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "")
                .build();
    }

    @Override
    protected ProjectEntity toEntity(Project model) {
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectID(model.getProjectId());
        entity.setOwner(model.getOwner());
        entity.setGatewayId(model.getGatewayId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        return entity;
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        String projectId = project.getProjectId();
        if (projectId.isEmpty()) {
            projectId = AiravataUtils.getId(project.getName());
        }
        project = project.toBuilder()
                .setProjectId(projectId)
                .setGatewayId(gatewayId)
                .build();
        ProjectEntity entity = toEntity(project);
        entity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        ProjectEntity merged = execute(entityManager -> entityManager.merge(entity));
        return merged.getProjectID();
    }

    public void removeProject(String projectId) throws RegistryException {
        delete(projectId);
    }
}
