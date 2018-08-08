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
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProcessInputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInputRepositoryTest.class);
    
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    ProcessInputRepository processInputRepository;

    public ProcessInputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        processInputRepository = new ProcessInputRepository();
    }

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    private Project createSampleProject(String tag) {
        Project project = new Project();
        project.setName("projectName" + tag);
        project.setOwner("user" + tag);
        return project;
    }

    private ExperimentModel createSampleExperiment(String projectId, String gatewayId, String tag) {
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user" + tag);
        experimentModel.setExperimentName("name" + tag);
        return experimentModel;
    }

    private void addProcessInputs(String processId,
                                     HashMap<String, List<InputDataObjectType>> actualProcessInputMap,
                                     int count) throws RegistryException {

        List<InputDataObjectType> tempProcessInputModelList = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            InputDataObjectType inputDataObjectProType = new InputDataObjectType();
            inputDataObjectProType.setName("inputP" + k);
            inputDataObjectProType.setType(DataType.STDOUT);
            tempProcessInputModelList.add(inputDataObjectProType);
            processInputRepository.addProcessInputs(tempProcessInputModelList, processId);
        }
        actualProcessInputMap.put(processId, tempProcessInputModelList);
    }

    @Test
    public void addProcessInputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertNotNull(processId);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType();
        inputDataObjectProType.setName("inputP");
        inputDataObjectProType.setType(DataType.STDOUT);

        List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
        inputDataObjectTypeProList.add(inputDataObjectProType);

        assertEquals(processId, processInputRepository.addProcessInputs(inputDataObjectTypeProList, processId));
        assertEquals(1, processRepository.getProcess(processId).getProcessInputs().size());

        List<InputDataObjectType> savedInputDataObjectTypeProList = processInputRepository.getProcessInputs(processId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(inputDataObjectTypeProList, savedInputDataObjectTypeProList, "__isset_bitfield"));
    }

    @Test
    public void updateProcessInputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertNotNull(processId);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType();
        inputDataObjectProType.setName("inputP");
        inputDataObjectProType.setType(DataType.STDOUT);

        List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
        inputDataObjectTypeProList.add(inputDataObjectProType);

        assertEquals(processId, processInputRepository.addProcessInputs(inputDataObjectTypeProList, processId));
        assertEquals(1, processRepository.getProcess(processId).getProcessInputs().size());

        inputDataObjectProType.setValue("iValueP");
        processInputRepository.updateProcessInputs(inputDataObjectTypeProList, processId);

        List<InputDataObjectType> savedInputDataObjectTypeProList = processInputRepository.getProcessInputs(processId);
        assertEquals(1, savedInputDataObjectTypeProList.size());
        assertEquals("iValueP", savedInputDataObjectTypeProList.get(0).getValue());
        assertEquals(DataType.STDOUT, savedInputDataObjectTypeProList.get(0).getType());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(inputDataObjectTypeProList, savedInputDataObjectTypeProList, "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleProcessInputRepositoryTest() throws RegistryException {
        List<InputDataObjectType> actualProcessInputList = new ArrayList<>();
        List<String> actualProcessIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);
            assertNotNull(processId);

            InputDataObjectType inputDataObjectProType = new InputDataObjectType();
            inputDataObjectProType.setName("inputP" + i);
            inputDataObjectProType.setType(DataType.STDOUT);

            List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
            inputDataObjectTypeProList.add(inputDataObjectProType);
            assertEquals(processId, processInputRepository.addProcessInputs(inputDataObjectTypeProList, processId));
            assertEquals(1, processRepository.getProcess(processId).getProcessInputs().size());

            actualProcessInputList.add(inputDataObjectProType);
            actualProcessIdList.add(processId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<InputDataObjectType> savedProcessInputList = processInputRepository.getProcessInputs(actualProcessIdList.get(j));
            Assert.assertEquals(1, savedProcessInputList.size());

            InputDataObjectType savedInputDataObject = savedProcessInputList.get(0);
            InputDataObjectType actualInputDataObject = actualProcessInputList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualInputDataObject, savedInputDataObject, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleProcessInputRepositoryTest() throws RegistryException {
        List<String> actualProcessIdList = new ArrayList<>();
        HashMap<String, List<InputDataObjectType>> actualProcessInputMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("1" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);
            assertNotNull(processId);

            InputDataObjectType inputDataObjectProType = new InputDataObjectType();
            inputDataObjectProType.setName("inputP" + i);
            inputDataObjectProType.setType(DataType.STDOUT);

            actualProcessIdList.add(processId);
            addProcessInputs(processId, actualProcessInputMap, i + 1);
        }

        for (int j = 0 ; j < 5; j++) {
            int actualsize = j + 1;
            List<InputDataObjectType> savedProcessInputList = processInputRepository.getProcessInputs(actualProcessIdList.get(j));
            Assert.assertEquals(actualsize, savedProcessInputList.size());

            List<InputDataObjectType> actualProcessInputList = actualProcessInputMap.get(actualProcessIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessInputList, savedProcessInputList, "__isset_bitfield"));
        }
    }

}
