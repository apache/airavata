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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentErrorService;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.AiravataServerProperties.class,
            ExperimentErrorRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",

            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ExperimentErrorRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataServerProperties.class,
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ExperimentErrorService experimentErrorService;

    private String gatewayId;
    private String projectId;
    private String experimentId;

    public ExperimentErrorRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ExperimentErrorService experimentErrorService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.experimentErrorService = experimentErrorService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);
        assertNotNull(experimentId, "Experiment ID should not be null");
    }

    @Test
    public void testExperimentErrorRepository_CreateAndUpdate() throws RegistryException {
        // Test creating and updating experiment errors
        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error-1");
        errorModel.setActualErrorMessage("Initial error message");
        errorModel.setUserFriendlyMessage("User-friendly initial message");

        String experimentErrorId = experimentErrorService.addExperimentError(errorModel, experimentId);
        assertNotNull(experimentErrorId, "Experiment error ID should not be null");
        assertEquals("error-1", experimentErrorId, "Error ID should match");

        // Verify error is associated with experiment
        assertEquals(
                1,
                experimentService.getExperiment(experimentId).getErrors().size(),
                "Experiment should have one error");

        // Update error message
        errorModel.setActualErrorMessage("Updated error message");
        errorModel.setUserFriendlyMessage("Updated user-friendly message");
        experimentErrorService.updateExperimentError(errorModel, experimentId);

        // Verify update
        List<ErrorModel> retrievedErrorList = experimentErrorService.getExperimentErrors(experimentId);
        assertEquals(1, retrievedErrorList.size(), "Should have one error");
        assertEquals(
                "Updated error message",
                retrievedErrorList.get(0).getActualErrorMessage(),
                "Error message should be updated");
        assertEquals(
                "Updated user-friendly message",
                retrievedErrorList.get(0).getUserFriendlyMessage(),
                "User-friendly message should be updated");
    }

    @Test
    public void testExperimentErrorRepository_MultipleErrorsPerExperiment() throws RegistryException {
        // Test that an experiment can have multiple errors (important for error history)
        ErrorModel error1 = new ErrorModel();
        error1.setErrorId("error-1");
        error1.setActualErrorMessage("First error");
        String errorId1 = experimentErrorService.addExperimentError(error1, experimentId);

        ErrorModel error2 = new ErrorModel();
        error2.setErrorId("error-2");
        error2.setActualErrorMessage("Second error");
        String errorId2 = experimentErrorService.addExperimentError(error2, experimentId);

        ErrorModel error3 = new ErrorModel();
        error3.setErrorId("error-3");
        error3.setActualErrorMessage("Third error");
        String errorId3 = experimentErrorService.addExperimentError(error3, experimentId);

        // Verify all errors are associated with the experiment
        List<ErrorModel> errors = experimentErrorService.getExperimentErrors(experimentId);
        assertEquals(3, errors.size(), "Experiment should have 3 errors");

        // Verify all error IDs are present
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId1)), "Error 1 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId2)), "Error 2 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId3)), "Error 3 should be present");
    }
}
