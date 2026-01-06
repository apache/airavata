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

import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.ExperimentStatusService;
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
            ExperimentStatusRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",



        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ExperimentStatusRepositoryTest extends TestBase {

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
    private final ExperimentStatusService experimentStatusService;

    private String gatewayId;
    private String projectId;
    private String experimentId;

    public ExperimentStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ExperimentStatusService experimentStatusService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.experimentStatusService = experimentStatusService;
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
    public void testExperimentStatusRepository_InitialStatusCreation() throws RegistryException {

        assertEquals(
                1,
                experimentService
                        .getExperiment(experimentId)
                        .getExperimentStatus()
                        .size(),
                "Experiment should have initial CREATED status");

        ExperimentStatus initialStatus = experimentStatusService.getExperimentStatus(experimentId);
        assertNotNull(initialStatus, "Initial status should exist");
        assertEquals(ExperimentState.CREATED, initialStatus.getState(), "Initial status should be CREATED");
    }

    @Test
    public void testExperimentStatusRepository_StateTransitions() throws RegistryException {

        ExperimentStatus validatedStatus = new ExperimentStatus();
        validatedStatus.setState(ExperimentState.VALIDATED);
        String statusId = experimentStatusService.addExperimentStatus(validatedStatus, experimentId);
        assertNotNull(statusId, "Status ID should not be null");
        assertEquals(
                2,
                experimentService
                        .getExperiment(experimentId)
                        .getExperimentStatus()
                        .size(),
                "Experiment should have 2 statuses");


        validatedStatus.setState(ExperimentState.EXECUTING);
        experimentStatusService.updateExperimentStatus(validatedStatus, experimentId);

        ExperimentStatus retrievedStatus = experimentStatusService.getExperimentStatus(experimentId);
        assertEquals(ExperimentState.EXECUTING, retrievedStatus.getState(), "Status should be updated to EXECUTING");
    }

    @Test
    public void testExperimentStatusRepository_StatusUpdateWithReason() throws RegistryException {

        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.EXECUTING);
        experimentStatusService.addExperimentStatus(status, experimentId);

        long originalTime = status.getTimeOfStateChange();


        ExperimentStatus updatedStatus = new ExperimentStatus();
        updatedStatus.setState(ExperimentState.EXECUTING);
        updatedStatus.setReason("Updated execution reason");
        updatedStatus.setTimeOfStateChange(originalTime);
        experimentStatusService.updateExperimentStatus(updatedStatus, experimentId);

        ExperimentStatus retrievedStatus = experimentStatusService.getExperimentStatus(experimentId);
        assertEquals(ExperimentState.EXECUTING, retrievedStatus.getState(), "Status should remain EXECUTING");
        assertEquals("Updated execution reason", retrievedStatus.getReason(), "Reason should be updated");
    }
}
