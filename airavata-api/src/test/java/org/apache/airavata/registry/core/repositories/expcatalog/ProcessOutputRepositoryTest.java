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
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessOutputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessOutputRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    ProcessOutputRepository processOutputRepository;

    public ProcessOutputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        JobRepository jobRepository = new JobRepository();
        TaskRepository taskRepository = new TaskRepository(jobRepository);
        processRepository = new ProcessRepository(taskRepository);
        processOutputRepository = new ProcessOutputRepository(processRepository);
    }

    @Test
    public void testProcessOutputRepository() throws RegistryException {
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

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
        outputDataObjectProType.setName("outputP");
        outputDataObjectProType.setType(DataType.STDERR);

        List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
        outputDataObjectTypeProList.add(outputDataObjectProType);

        assertEquals(processId, processOutputRepository.addProcessOutputs(outputDataObjectTypeProList, processId));
        assertTrue(processRepository.getProcess(processId).getProcessOutputs().size() == 1);

        outputDataObjectProType.setValue("oValueP");
        processOutputRepository.updateProcessOutputs(outputDataObjectTypeProList, processId);

        List<OutputDataObjectType> retrievedProOutputList = processOutputRepository.getProcessOutputs(processId);
        assertTrue(retrievedProOutputList.size() == 1);
        assertEquals("oValueP", retrievedProOutputList.get(0).getValue());
        assertEquals(DataType.STDERR, retrievedProOutputList.get(0).getType());

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
