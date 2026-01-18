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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for ProjectRepository.
 * Inherits test infrastructure from TestBase.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProjectRepositoryTest extends TestBase {

    private final ProjectService projectService;
    private final GatewayService gatewayService;

    public ProjectRepositoryTest(ProjectService projectService, GatewayService gatewayService) {
        this.projectService = projectService;
        this.gatewayService = gatewayService;
    }

    @Test
    void testProjectCrudOperations() throws Exception {
        // First ensure a gateway exists
        String gatewayId = "test-gateway";
        if (!gatewayService.isGatewayExist(gatewayId)) {
            Gateway gateway = new Gateway();
            gateway.setGatewayId(gatewayId);
            gateway.setGatewayName("Test Gateway");
            gatewayService.addGateway(gateway);
        }

        // Create a project
        String projectId = "test-project-" + System.currentTimeMillis();
        String owner = "testUser";

        Project project = new Project();
        project.setProjectID(projectId);
        project.setName("Test Project");
        project.setOwner(owner);
        project.setGatewayId(gatewayId);
        project.setDescription("Test project description");

        String createdProjectId = projectService.addProject(project, gatewayId);
        assertEquals(projectId, createdProjectId);

        // Read the project
        Project retrieved = projectService.getProject(projectId);
        assertEquals(project.getName(), retrieved.getName());
        assertEquals(owner, retrieved.getOwner());

        // Update the project
        project.setDescription("Updated description");
        projectService.updateProject(project, projectId);

        Project updated = projectService.getProject(projectId);
        assertEquals("Updated description", updated.getDescription());

        // Verify project exists
        assertTrue(projectService.isProjectExist(projectId));

        // Delete the project
        projectService.removeProject(projectId);
        assertFalse(projectService.isProjectExist(projectId));
    }
}
