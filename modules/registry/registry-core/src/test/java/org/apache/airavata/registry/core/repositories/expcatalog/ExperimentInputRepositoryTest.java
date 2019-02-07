/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExperimentInputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentInputRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ExperimentInputRepository experimentInputRepository;

    public ExperimentInputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentInputRepository = new ExperimentInputRepository();
    }

    @Test
    public void ExperimentInputRepositoryTest() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectRepository.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");
        inputDataObjectTypeExp.setType(DataType.STRING);

        List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
        inputDataObjectTypeExpList.add(inputDataObjectTypeExp);

        assertEquals(experimentId, experimentInputRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId));
        assertTrue(experimentRepository.getExperiment(experimentId).getExperimentInputs().size() == 1);

        inputDataObjectTypeExp.setValue("iValueE");
        experimentInputRepository.updateExperimentInputs(inputDataObjectTypeExpList, experimentId);

        List<InputDataObjectType> retrievedExpInputsList = experimentInputRepository.getExperimentInputs(experimentId);
        assertTrue(retrievedExpInputsList.size() == 1);
        assertEquals("iValueE", retrievedExpInputsList.get(0).getValue());
        assertEquals(DataType.STRING, retrievedExpInputsList.get(0).getType());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
