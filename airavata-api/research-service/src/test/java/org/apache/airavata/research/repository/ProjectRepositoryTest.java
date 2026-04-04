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
package org.apache.airavata.research.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.Constants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRepositoryTest.class);

    private String testGateway = "testGateway";
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;

    public ProjectRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
    }

    @Test
    public void ProjectRepositoryTest() throws RegistryException {
        Gateway gateway = Gateway.newBuilder().setGatewayId(testGateway).build();
        gateway = gateway.toBuilder().setDomain("SEAGRID").build();
        gateway = gateway.toBuilder().setEmailAddress("abc@d.com").build();
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder().setName("projectName").build();
        project = project.toBuilder().setOwner("user").build();
        project = project.toBuilder().setGatewayId(gatewayId).build();

        String projectId = projectRepository.addProject(project, gatewayId);
        assertTrue(projectId != null);

        Project updatedProject = project.toBuilder().build();
        // Simulate clients that may or may not set projectId but will pass
        // projectId as an argument to updateProject
        updatedProject = updatedProject.toBuilder().clearProjectId().build();
        updatedProject =
                updatedProject.toBuilder().setName("updated projectName").build();
        updatedProject =
                updatedProject.toBuilder().setDescription("projectDescription").build();
        projectRepository.updateProject(updatedProject, projectId);

        Project retrievedProject = projectRepository.getProject(projectId);
        assertEquals(gatewayId, retrievedProject.getGatewayId());
        assertEquals("updated projectName", retrievedProject.getName());
        assertEquals("projectDescription", retrievedProject.getDescription());

        assertTrue(projectRepository
                .getProjectIDs(Constants.FieldConstants.ProjectConstants.OWNER, "user")
                .contains(projectId));

        List<String> accessibleProjectIds = new ArrayList<>();
        accessibleProjectIds.add(projectId);

        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, retrievedProject.getGatewayId());
        filters.put(Constants.FieldConstants.ProjectConstants.OWNER, retrievedProject.getOwner());
        filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, retrievedProject.getName());
        filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, retrievedProject.getDescription());

        assertTrue(projectRepository
                        .searchAllAccessibleProjects(accessibleProjectIds, filters, -1, 0, null, null)
                        .size()
                == 1);

        projectRepository.removeProject(projectId);
        assertFalse(projectRepository.isProjectExist(projectId));

        gatewayRepository.removeGateway(gatewayId);
    }
}
