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
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.InputDataRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessInputRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final InputDataRepository inputDataRepository;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;

    public ProcessInputRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            InputDataRepository inputDataRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.inputDataRepository = inputDataRepository;
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

        InputDataEntity inputEntity = new InputDataEntity();
        inputEntity.setParentId(processId);
        inputEntity.setParentType(DataObjectParentType.PROCESS);
        inputEntity.setName("inputP");
        inputEntity.setType(DataType.STDOUT);

        inputDataRepository.save(inputEntity);
        flushAndClear();

        List<InputDataEntity> retrievedInputs =
                inputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(1, retrievedInputs.size(), "Process should have one input");
        assertEquals("inputP", retrievedInputs.get(0).getName(), "Input name should match");
        assertEquals(DataType.STDOUT, retrievedInputs.get(0).getType(), "Input type should match");

        inputEntity.setValue("iValueP");
        inputDataRepository.save(inputEntity);
        flushAndClear();

        List<InputDataEntity> retrievedProInputsList =
                inputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(1, retrievedProInputsList.size(), "Should have one input");
        assertEquals("iValueP", retrievedProInputsList.get(0).getValue(), "Input value should be updated");
        assertEquals(DataType.STDOUT, retrievedProInputsList.get(0).getType(), "Input type should match");
        assertEquals("inputP", retrievedProInputsList.get(0).getName(), "Input name should match");
    }

    @Test
    public void testProcessInputRepository_MultipleInputs() throws RegistryException {

        InputDataEntity input1 = new InputDataEntity();
        input1.setParentId(processId);
        input1.setParentType(DataObjectParentType.PROCESS);
        input1.setName("input1");
        input1.setType(DataType.STDOUT);
        input1.setValue("value1");
        input1.setInputOrder(0);

        InputDataEntity input2 = new InputDataEntity();
        input2.setParentId(processId);
        input2.setParentType(DataObjectParentType.PROCESS);
        input2.setName("input2");
        input2.setType(DataType.STRING);
        input2.setValue("value2");
        input2.setInputOrder(1);

        InputDataEntity input3 = new InputDataEntity();
        input3.setParentId(processId);
        input3.setParentType(DataObjectParentType.PROCESS);
        input3.setName("input3");
        input3.setType(DataType.URI);
        input3.setValue("value3");
        input3.setInputOrder(2);

        inputDataRepository.save(input1);
        inputDataRepository.save(input2);
        inputDataRepository.save(input3);
        flushAndClear();

        List<InputDataEntity> retrievedInputs =
                inputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(3, retrievedInputs.size(), "Process should have 3 inputs");

        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input1")), "Input 1 should be present");
        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input2")), "Input 2 should be present");
        assertTrue(retrievedInputs.stream().anyMatch(i -> i.getName().equals("input3")), "Input 3 should be present");
    }
}
