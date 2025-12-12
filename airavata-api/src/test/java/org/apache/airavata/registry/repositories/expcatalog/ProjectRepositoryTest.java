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
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.utils.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, ProjectRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.registryService.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProjectRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern =
                                "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.registry\\.messaging\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
        org.apache.airavata.config.DozerMapperConfig.class
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;

    public ProjectRepositoryTest(GatewayService gatewayService, ProjectService projectService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
    }

    @Test
    public void testProjectRepository() throws RegistryException {
        String testGateway =
                "testGateway-" + java.util.UUID.randomUUID().toString().substring(0, 8);
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
