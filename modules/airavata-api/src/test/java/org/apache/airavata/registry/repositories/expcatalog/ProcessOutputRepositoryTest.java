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
import org.apache.airavata.registry.entities.OutputDataEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.OutputDataRepository;
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
public class ProcessOutputRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final OutputDataRepository outputDataRepository;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;

    public ProcessOutputRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            OutputDataRepository outputDataRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.outputDataRepository = outputDataRepository;
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

        OutputDataEntity outputEntity = new OutputDataEntity();
        outputEntity.setParentId(processId);
        outputEntity.setParentType(DataObjectParentType.PROCESS);
        outputEntity.setName("outputP");
        outputEntity.setType(DataType.STDERR);

        outputDataRepository.save(outputEntity);
        flushAndClear();

        List<OutputDataEntity> retrievedOutputs =
                outputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(1, retrievedOutputs.size(), "Process should have one output");
        assertEquals("outputP", retrievedOutputs.get(0).getName(), "Output name should match");
        assertEquals(DataType.STDERR, retrievedOutputs.get(0).getType(), "Output type should match");

        outputEntity.setValue("oValueP");
        outputDataRepository.save(outputEntity);
        flushAndClear();

        List<OutputDataEntity> retrievedProOutputList =
                outputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(1, retrievedProOutputList.size(), "Should have one output");
        assertEquals("oValueP", retrievedProOutputList.get(0).getValue(), "Output value should be updated");
        assertEquals(DataType.STDERR, retrievedProOutputList.get(0).getType(), "Output type should match");
        assertEquals("outputP", retrievedProOutputList.get(0).getName(), "Output name should match");
    }

    @Test
    public void testProcessOutputRepository_MultipleOutputs() throws RegistryException {

        OutputDataEntity output1 = new OutputDataEntity();
        output1.setParentId(processId);
        output1.setParentType(DataObjectParentType.PROCESS);
        output1.setName("output1");
        output1.setType(DataType.STDERR);
        output1.setValue("value1");
        output1.setOutputOrder(0);

        OutputDataEntity output2 = new OutputDataEntity();
        output2.setParentId(processId);
        output2.setParentType(DataObjectParentType.PROCESS);
        output2.setName("output2");
        output2.setType(DataType.STRING);
        output2.setValue("value2");
        output2.setOutputOrder(1);

        OutputDataEntity output3 = new OutputDataEntity();
        output3.setParentId(processId);
        output3.setParentType(DataObjectParentType.PROCESS);
        output3.setName("output3");
        output3.setType(DataType.URI);
        output3.setValue("value3");
        output3.setOutputOrder(2);

        outputDataRepository.save(output1);
        outputDataRepository.save(output2);
        outputDataRepository.save(output3);
        flushAndClear();

        List<OutputDataEntity> retrievedOutputs =
                outputDataRepository.findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
        assertEquals(3, retrievedOutputs.size(), "Process should have 3 outputs");

        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output1")), "Output 1 should be present");
        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output2")), "Output 2 should be present");
        assertTrue(
                retrievedOutputs.stream().anyMatch(o -> o.getName().equals("output3")), "Output 3 should be present");
    }
}
