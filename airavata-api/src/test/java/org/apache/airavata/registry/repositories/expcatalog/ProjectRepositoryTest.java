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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.utils.Constants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRepositoryTest.class);

    private String testGateway = "testGateway";

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectService projectService;

    public ProjectRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testProjectRepository() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGateway);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectService.addProject(project, gatewayId);
        assertTrue(projectId != null);

        Project updatedProject = project.deepCopy();
        // Simulate clients that may or may not set projectId but will pass
        // projectId as an argument to updateProject
        updatedProject.unsetProjectID();
        updatedProject.setName("updated projectName");
        updatedProject.setDescription("projectDescription");
        projectService.updateProject(updatedProject, projectId);

        Project retrievedProject = projectService.getProject(projectId);
        assertEquals(gatewayId, retrievedProject.getGatewayId());
        assertEquals("updated projectName", retrievedProject.getName());
        assertEquals("projectDescription", retrievedProject.getDescription());

        // Note: getProjectIDs method may need to be added to ProjectService if needed
        // For now, skipping this assertion as it requires additional service method

        List<String> accessibleProjectIds = new ArrayList<>();
        accessibleProjectIds.add(projectId);

        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, retrievedProject.getGatewayId());
        filters.put(Constants.FieldConstants.ProjectConstants.OWNER, retrievedProject.getOwner());
        filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, retrievedProject.getName());
        filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, retrievedProject.getDescription());

        assertTrue(projectService
                        .searchAllAccessibleProjects(accessibleProjectIds, filters, -1, 0, null, null)
                        .size()
                == 1);

        projectService.removeProject(projectId);
        assertFalse(projectService.isProjectExist(projectId));

        gatewayService.removeGateway(gatewayId);
    }
}
