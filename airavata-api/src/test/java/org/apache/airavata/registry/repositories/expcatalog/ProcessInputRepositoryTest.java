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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessInputService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class ProcessInputRepositoryTest extends TestBase {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInputService processInputService;

    public ProcessInputRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testProcessInputRepository() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType();
        inputDataObjectProType.setName("inputP");
        inputDataObjectProType.setType(DataType.STDOUT);

        List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
        inputDataObjectTypeProList.add(inputDataObjectProType);

        assertEquals(processId, processInputService.addProcessInputs(inputDataObjectTypeProList, processId));
        assertTrue(processService.getProcess(processId).getProcessInputs().size() == 1);

        inputDataObjectProType.setValue("iValueP");
        processInputService.updateProcessInputs(inputDataObjectTypeProList, processId);

        List<InputDataObjectType> retrievedProInputsList = processInputService.getProcessInputs(processId);
        assertTrue(retrievedProInputsList.size() == 1);
        assertEquals("iValueP", retrievedProInputsList.get(0).getValue());
        assertEquals(DataType.STDOUT, retrievedProInputsList.get(0).getType());

        experimentService.removeExperiment(experimentId);
        processService.removeProcess(processId);
        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
