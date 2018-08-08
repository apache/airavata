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

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
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

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    private Project createSampleProject(String tag) {
        Project project = new Project();
        project.setName("projectName" + tag);
        project.setOwner("user" + tag);
        return project;
    }

    private ExperimentModel createSampleExperiment(String projectId, String gatewayId, String tag) {
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user" + tag);
        experimentModel.setExperimentName("name" + tag);
        return experimentModel;
    }

    @Test
    public void addProjectRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project savedProject = createSampleProject("1");
        savedProject.setGatewayId(gatewayId);
        String projectId = projectRepository.addProject(savedProject, gatewayId);
        Assert.assertNotNull(projectId);

        Project retrievedProject = projectRepository.getProject(projectId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedProject, retrievedProject, "__isset_bitfield", "creationTime"));
    }

    @Test
    public void updateProjectRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project savedProject = createSampleProject("1");
        savedProject.setGatewayId(gatewayId);
        String projectId = projectRepository.addProject(savedProject, gatewayId);
        Assert.assertNotNull(projectId);

        savedProject.setDescription("projectDescription");
        projectRepository.updateProject(savedProject, null);

        Project retrievedProject = projectRepository.getProject(projectId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedProject, retrievedProject, "__isset_bitfield", "creationTime"));
    }

    @Test
    public void retrieveSingleProjectRepositoryTest() throws RegistryException {
        List<Project> actualProjectRepositoryList = new ArrayList<>();
        List<String> projectIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project savedProject = createSampleProject("" + i);
            savedProject.setGatewayId(gatewayId);
            String projectId = projectRepository.addProject(savedProject, gatewayId);
            Assert.assertNotNull(projectId);

            actualProjectRepositoryList.add(savedProject);
            projectIdList.add(projectId);
        }
        for (int j = 0 ; j < 5; j++) {
            Project savedProject = projectRepository.getProject(projectIdList.get(j));
            Project actualProject = actualProjectRepositoryList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProject , savedProject, "__isset_bitfield", "creationTime"));
        }
    }

    @Test
    public void retrieveMultipleProjectRepositoryTest() throws RegistryException {
        List<String> actualProjectIdList = new ArrayList<>();
        HashMap<String, Project> actualProjectStatusMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project savedProject = createSampleProject("" + i);
            savedProject.setGatewayId(gatewayId);
            String projectId = projectRepository.addProject(savedProject, gatewayId);
            Assert.assertNotNull(projectId);

            actualProjectIdList.add(projectId);
            actualProjectStatusMap.put(projectId, savedProject);
        }

        for (int j = 0 ; j < 5; j++) {
            Project savedProject = projectRepository.getProject(actualProjectIdList.get(j));
            Project actualProject = actualProjectStatusMap.get(actualProjectIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProject, savedProject, "__isset_bitfield", "creationTime"));
        }
    }

    @Test
    public void removeProjectRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project savedProject = createSampleProject("1");
        savedProject.setGatewayId(gatewayId);
        String projectId = projectRepository.addProject(savedProject, gatewayId);
        Assert.assertNotNull(projectId);

        projectRepository.removeProject(projectId);
        assertFalse(projectRepository.isProjectExist(projectId));
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
        assertNotNull(projectId);

        project.setDescription("projectDescription");
        projectRepository.updateProject(project, null);

        Project retrievedProject = projectRepository.getProject(projectId);
        assertEquals(gatewayId, retrievedProject.getGatewayId());

        assertTrue(projectRepository.getProjectIDs(Constants.FieldConstants.ProjectConstants.OWNER, "user").contains(projectId));

        List<String> accessibleProjectIds = new ArrayList<>();
        accessibleProjectIds.add(projectId);

        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, retrievedProject.getGatewayId());
        filters.put(Constants.FieldConstants.ProjectConstants.OWNER, retrievedProject.getOwner());
        filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, retrievedProject.getName());
        filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, retrievedProject.getDescription());

        assertEquals(1, projectRepository.searchAllAccessibleProjects(accessibleProjectIds, filters,
                -1, 0, null, null).size());

        projectRepository.removeProject(projectId);
        assertFalse(projectRepository.isProjectExist(projectId));

        gatewayRepository.removeGateway(gatewayId);
    }

}
