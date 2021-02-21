/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ProjectRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRepositoryTest.class);

    private String testGateway = "testGateway";
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;

    public ProjectRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
    }

    @Test
    public void ProjectRepositoryTest() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGateway);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectRepository.addProject(project, gatewayId);
        assertTrue(projectId != null);

        Project updatedProject = project.deepCopy();
        // Simulate clients that may or may not set projectId but will pass
        // projectId as an argument to updateProject
        updatedProject.unsetProjectID();
        updatedProject.setName("updated projectName");
        updatedProject.setDescription("projectDescription");
        projectRepository.updateProject(updatedProject, projectId);

        Project retrievedProject = projectRepository.getProject(projectId);
        assertEquals(gatewayId, retrievedProject.getGatewayId());
        assertEquals("updated projectName", retrievedProject.getName());
        assertEquals("projectDescription", retrievedProject.getDescription());

        assertTrue(projectRepository.getProjectIDs(Constants.FieldConstants.ProjectConstants.OWNER, "user").contains(projectId));

        List<String> accessibleProjectIds = new ArrayList<>();
        accessibleProjectIds.add(projectId);

        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, retrievedProject.getGatewayId());
        filters.put(Constants.FieldConstants.ProjectConstants.OWNER, retrievedProject.getOwner());
        filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, retrievedProject.getName());
        filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, retrievedProject.getDescription());

        assertTrue(projectRepository.searchAllAccessibleProjects(accessibleProjectIds, filters,
                -1, 0, null, null).size() == 1);

        projectRepository.removeProject(projectId);
        assertFalse(projectRepository.isProjectExist(projectId));

        gatewayRepository.removeGateway(gatewayId);
    }

}
