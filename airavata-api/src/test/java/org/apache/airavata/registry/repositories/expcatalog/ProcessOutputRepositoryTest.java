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
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessOutputService;
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
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            ProcessOutputRepositoryTest.TestConfiguration.class
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
public class ProcessOutputRepositoryTest extends TestBase {

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
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessOutputService processOutputService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;

    public ProcessOutputRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessOutputService processOutputService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processOutputService = processOutputService;
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
    public void testProcessOutputRepository_CreateAndUpdate() throws RegistryException {
        // Test creating and updating process outputs
        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
        outputDataObjectProType.setName("outputP");
        outputDataObjectProType.setType(DataType.STDERR);

        List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
        outputDataObjectTypeProList.add(outputDataObjectProType);

        processOutputService.addProcessOutputs(outputDataObjectTypeProList, processId);
        assertEquals(
                1, processService.getProcess(processId).getProcessOutputs().size(), "Process should have one output");

        // Update output value
        outputDataObjectProType.setValue("oValueP");
        processOutputService.updateProcessOutputs(outputDataObjectTypeProList, processId);

        List<OutputDataObjectType> retrievedProOutputList = processOutputService.getProcessOutputs(processId);
        assertEquals(1, retrievedProOutputList.size(), "Should have one output");
        assertEquals("oValueP", retrievedProOutputList.get(0).getValue(), "Output value should be updated");
        assertEquals(DataType.STDERR, retrievedProOutputList.get(0).getType(), "Output type should match");
        assertEquals("outputP", retrievedProOutputList.get(0).getName(), "Output name should match");
    }

    @Test
    public void testProcessOutputRepository_MultipleOutputs() throws RegistryException {
        // Test that a process can have multiple outputs (important for complex workflows)
        OutputDataObjectType output1 = new OutputDataObjectType();
        output1.setName("output1");
        output1.setType(DataType.STDERR);
        output1.setValue("value1");

        OutputDataObjectType output2 = new OutputDataObjectType();
        output2.setName("output2");
        output2.setType(DataType.STRING);
        output2.setValue("value2");

        OutputDataObjectType output3 = new OutputDataObjectType();
        output3.setName("output3");
        output3.setType(DataType.URI);
        output3.setValue("value3");

        List<OutputDataObjectType> outputs = new ArrayList<>();
        outputs.add(output1);
        outputs.add(output2);
        outputs.add(output3);

        processOutputService.addProcessOutputs(outputs, processId);

        List<OutputDataObjectType> retrievedOutputs = processOutputService.getProcessOutputs(processId);
        assertEquals(3, retrievedOutputs.size(), "Process should have 3 outputs");

        // Verify all outputs are present
        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output1")), "Output 1 should be present");
        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output2")), "Output 2 should be present");
        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output3")), "Output 3 should be present");
    }
}
