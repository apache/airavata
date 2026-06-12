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
package org.apache.airavata.orchestration.util;

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.util.AiravataUtils;

/**
 * Lightweight orchestration-test helper that creates/removes PROJECT rows via native SQL,
 * without depending on research-service's JPA entities or repositories. Provides just enough
 * to satisfy the EXPERIMENT → PROJECT foreign key for process/task/job repository tests.
 */
public class ProjectTestHelper extends AbstractRepository<Project, Project, String> {

    public ProjectTestHelper() {
        super(Project.class, Project.class);
    }

    @Override
    protected Project toModel(Project entity) {
        return entity;
    }

    @Override
    protected Project toEntity(Project model) {
        return model;
    }

    public String addProject(Project project, String gatewayId) throws RegistryException {
        String projectId = project.getProjectId();
        if (projectId.isEmpty()) {
            projectId = AiravataUtils.getId(project.getName());
        }
        executeWithNativeQuery(
                "INSERT INTO PROJECT (PROJECT_ID, USER_NAME, GATEWAY_ID, PROJECT_NAME, DESCRIPTION) "
                        + "VALUES (?1, ?2, ?3, ?4, ?5)",
                projectId,
                project.getOwner(),
                gatewayId,
                project.getName(),
                project.getDescription());
        return projectId;
    }

    public void removeProject(String projectId) throws RegistryException {
        executeWithNativeQuery("DELETE FROM PROJECT WHERE PROJECT_ID = ?1", projectId);
    }
}
