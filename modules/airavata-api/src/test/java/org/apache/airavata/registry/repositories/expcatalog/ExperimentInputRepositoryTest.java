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

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentInputService;
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
            ExperimentInputRepositoryTest.TestConfiguration.class
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
public class ExperimentInputRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
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
    private final ExperimentInputService experimentInputService;

    private String gatewayId;
    private String projectId;
    private String experimentId;

    public ExperimentInputRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ExperimentInputService experimentInputService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.experimentInputService = experimentInputService;
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
    public void testExperimentInputRepository_CreateAndUpdate() throws RegistryException {

        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");
        inputDataObjectTypeExp.setType(DataType.STRING);

        List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
        inputDataObjectTypeExpList.add(inputDataObjectTypeExp);

        String returnedExperimentId =
                experimentInputService.addExperimentInputs(inputDataObjectTypeExpList, experimentId);
        assertEquals(experimentId, returnedExperimentId, "Returned experiment ID should match");
        assertEquals(
                1,
                experimentService
                        .getExperiment(experimentId)
                        .getExperimentInputs()
                        .size(),
                "Experiment should have one input");


        inputDataObjectTypeExp.setValue("iValueE");
        experimentInputService.updateExperimentInputs(inputDataObjectTypeExpList, experimentId);

        List<InputDataObjectType> retrievedExpInputsList = experimentInputService.getExperimentInputs(experimentId);
        assertEquals(1, retrievedExpInputsList.size(), "Should have one input");
        assertEquals("iValueE", retrievedExpInputsList.get(0).getValue(), "Input value should be updated");
        assertEquals(DataType.STRING, retrievedExpInputsList.get(0).getType(), "Input type should match");
        assertEquals("inputE", retrievedExpInputsList.get(0).getName(), "Input name should match");
    }

    @Test
    public void testExperimentInputRepository_MultipleInputs() throws RegistryException {

        InputDataObjectType input1 = new InputDataObjectType();
        input1.setName("input1");
        input1.setType(DataType.STRING);
        input1.setValue("value1");

        InputDataObjectType input2 = new InputDataObjectType();
        input2.setName("input2");
        input2.setType(DataType.URI);
        input2.setValue("value2");

        List<InputDataObjectType> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);

        experimentInputService.addExperimentInputs(inputs, experimentId);

        List<InputDataObjectType> retrievedInputs = experimentInputService.getExperimentInputs(experimentId);
        assertEquals(2, retrievedInputs.size(), "Experiment should have 2 inputs");


        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input1")), "Input 1 should be present");
        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input2")), "Input 2 should be present");
    }
}
