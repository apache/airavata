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
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentOutputService;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class ExperimentOutputRepositoryTest extends TestBase {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    ExperimentOutputService experimentOutputService;

    public ExperimentOutputRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testExperimentInputRepository() throws RegistryException {
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
        assertTrue(experimentId != null);

        OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
        outputDataObjectTypeExp.setName("outputE");
        outputDataObjectTypeExp.setType(DataType.STRING);

        List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
        outputDataObjectTypeExpList.add(outputDataObjectTypeExp);

        experimentOutputService.addExperimentOutputs(outputDataObjectTypeExpList, experimentId);
        assertTrue(experimentService
                        .getExperiment(experimentId)
                        .getExperimentOutputs()
                        .size()
                == 1);

        outputDataObjectTypeExp.setValue("oValueE");
        experimentOutputService.updateExperimentOutputs(outputDataObjectTypeExpList, experimentId);

        List<OutputDataObjectType> retrievedExpOutputList = experimentOutputService.getExperimentOutputs(experimentId);
        assertTrue(retrievedExpOutputList.size() == 1);
        assertEquals("oValueE", retrievedExpOutputList.get(0).getValue());
        assertEquals(DataType.STRING, retrievedExpOutputList.get(0).getType());

        experimentService.removeExperiment(experimentId);
        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
