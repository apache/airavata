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
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ProjectRepositoryTest {

    private static Initialize initialize;
    private String testGateway = "testGateway";
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProjectRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
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

        project.setDescription("projectDescription");
        projectRepository.updateProject(project, null);

        Project retrievedProject = projectRepository.getProject(projectId);
        assertEquals(gatewayId, retrievedProject.getGatewayId());

        assertTrue(projectRepository.getProjectIDs(DBConstants.Project.OWNER, "user").contains(projectId));

        List<String> accessibleProjectIds = new ArrayList<>();
        accessibleProjectIds.add(projectId);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.Project.GATEWAY_ID, retrievedProject.getGatewayId());
        filters.put(DBConstants.Project.OWNER, retrievedProject.getOwner());
        filters.put(DBConstants.Project.PROJECT_NAME, retrievedProject.getName());
        filters.put(DBConstants.Project.DESCRIPTION, retrievedProject.getDescription());

        assertTrue(projectRepository.searchAllAccessibleProjects(accessibleProjectIds, filters,
                -1, 0, null, null).size() == 1);

        projectRepository.removeProject(projectId);
        assertFalse(projectRepository.isProjectExist(projectId));

        gatewayRepository.removeGateway(gatewayId);
    }

}
