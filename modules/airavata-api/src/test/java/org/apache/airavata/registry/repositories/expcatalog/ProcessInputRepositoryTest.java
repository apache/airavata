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
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessInputService;
import org.apache.airavata.registry.services.ProcessService;
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
            ProcessInputRepositoryTest.TestConfiguration.class
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
public class ProcessInputRepositoryTest extends TestBase {

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
    @Import({})
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessInputService processInputService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;

    public ProcessInputRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessInputService processInputService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processInputService = processInputService;
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

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processId = processService.addProcess(processModel, experimentId);
        assertNotNull(processId, "Process ID should not be null");
    }

    @Test
    public void testProcessInputRepository_CreateAndUpdate() throws RegistryException {

        InputDataObjectType inputDataObjectProType = new InputDataObjectType();
        inputDataObjectProType.setName("inputP");
        inputDataObjectProType.setType(DataType.STDOUT);

        List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
        inputDataObjectTypeProList.add(inputDataObjectProType);

        String returnedProcessId = processInputService.addProcessInputs(inputDataObjectTypeProList, processId);
        assertEquals(processId, returnedProcessId, "Returned process ID should match");
        assertEquals(
                1, processService.getProcess(processId).getProcessInputs().size(), "Process should have one input");

        inputDataObjectProType.setValue("iValueP");
        processInputService.updateProcessInputs(inputDataObjectTypeProList, processId);

        List<InputDataObjectType> retrievedProInputsList = processInputService.getProcessInputs(processId);
        assertEquals(1, retrievedProInputsList.size(), "Should have one input");
        assertEquals("iValueP", retrievedProInputsList.get(0).getValue(), "Input value should be updated");
        assertEquals(DataType.STDOUT, retrievedProInputsList.get(0).getType(), "Input type should match");
        assertEquals("inputP", retrievedProInputsList.get(0).getName(), "Input name should match");
    }

    @Test
    public void testProcessInputRepository_MultipleInputs() throws RegistryException {

        InputDataObjectType input1 = new InputDataObjectType();
        input1.setName("input1");
        input1.setType(DataType.STDOUT);
        input1.setValue("value1");

        InputDataObjectType input2 = new InputDataObjectType();
        input2.setName("input2");
        input2.setType(DataType.STRING);
        input2.setValue("value2");

        InputDataObjectType input3 = new InputDataObjectType();
        input3.setName("input3");
        input3.setType(DataType.URI);
        input3.setValue("value3");

        List<InputDataObjectType> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);
        inputs.add(input3);

        processInputService.addProcessInputs(inputs, processId);

        List<InputDataObjectType> retrievedInputs = processInputService.getProcessInputs(processId);
        assertEquals(3, retrievedInputs.size(), "Process should have 3 inputs");

        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input1")), "Input 1 should be present");
        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input2")), "Input 2 should be present");
        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input3")), "Input 3 should be present");
    }
}
