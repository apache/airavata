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
import org.apache.airavata.model.application.io.OutputDataObjectType;
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

public class ProcessOutputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessOutputRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private ProcessOutputRepository processOutputRepository;

    public ProcessOutputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        processOutputRepository = new ProcessOutputRepository();
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

    private void addProcessOutputs(String processId,
                                  HashMap<String, List<OutputDataObjectType>> actualProcessOutputMap,
                                  int count) throws RegistryException {

        List<OutputDataObjectType> tempProcessOutputList = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
            outputDataObjectProType.setName("outputP" + k);
            outputDataObjectProType.setType(DataType.STDERR);
            tempProcessOutputList.add(outputDataObjectProType);
            processOutputRepository.addProcessOutputs(tempProcessOutputList, processId);
        }
        actualProcessOutputMap.put(processId, tempProcessOutputList);
    }

    @Test
    public void addProcessOutputRepositoryTest() throws RegistryException {
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

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
        outputDataObjectProType.setName("outputP");
        outputDataObjectProType.setType(DataType.STDERR);

        List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
        outputDataObjectTypeProList.add(outputDataObjectProType);

        assertEquals(processId, processOutputRepository.addProcessOutputs(outputDataObjectTypeProList, processId));
        assertEquals(1, processRepository.getProcess(processId).getProcessOutputs().size());
        List<OutputDataObjectType> savedOutputDataObjectProTypeList = processOutputRepository.getProcessOutputs(processId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(outputDataObjectProType, savedOutputDataObjectProTypeList.get(0), "__isset_bitfield"));
    }

    @Test
    public void updateProcessOutputRepositoryTest() throws RegistryException {
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

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
        outputDataObjectProType.setName("outputP");
        outputDataObjectProType.setType(DataType.STDERR);

        List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
        outputDataObjectTypeProList.add(outputDataObjectProType);

        assertEquals(processId, processOutputRepository.addProcessOutputs(outputDataObjectTypeProList, processId));
        assertEquals(1, processRepository.getProcess(processId).getProcessOutputs().size());

        outputDataObjectProType.setValue("oValueP");
        processOutputRepository.updateProcessOutputs(outputDataObjectTypeProList, processId);

        List<OutputDataObjectType> savedOutputDataObjectProTypeList = processOutputRepository.getProcessOutputs(processId);
        assertEquals(1, savedOutputDataObjectProTypeList.size());
        assertEquals("oValueP", savedOutputDataObjectProTypeList.get(0).getValue());
        assertEquals(DataType.STDERR, savedOutputDataObjectProTypeList.get(0).getType());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(outputDataObjectProType, savedOutputDataObjectProTypeList.get(0), "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleProcessOutputRepositoryTest() throws RegistryException {
        List<OutputDataObjectType> actualProcessOutputList = new ArrayList<>();
        List<String> actualProcessIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
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

            OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
            outputDataObjectProType.setName("outputP");
            outputDataObjectProType.setType(DataType.STDERR);

            List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
            outputDataObjectTypeProList.add(outputDataObjectProType);

            assertEquals(processId, processOutputRepository.addProcessOutputs(outputDataObjectTypeProList, processId));
            assertEquals(1, processRepository.getProcess(processId).getProcessOutputs().size());

            actualProcessOutputList.add(outputDataObjectProType);
            actualProcessIdList.add(processId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<OutputDataObjectType> savedProcessOutputList = processOutputRepository.getProcessOutputs(actualProcessIdList.get(j));
            Assert.assertEquals(1, savedProcessOutputList.size());

            OutputDataObjectType savedOutputDataObject = savedProcessOutputList.get(0);
            OutputDataObjectType actualOutputDataObject = actualProcessOutputList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualOutputDataObject, savedOutputDataObject, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleProcessOutputRepositoryTest() throws RegistryException {
        List<String> actualProcessIdList = new ArrayList<>();
        HashMap<String, List<OutputDataObjectType>> actualProcessOutputMap = new HashMap<>();

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

            actualProcessIdList.add(processId);
            addProcessOutputs(processId, actualProcessOutputMap, i + 1);
        }

        for (int j = 0 ; j < 5; j++) {
            int actualsize = j + 1;
            List<OutputDataObjectType> savedProcessOutputList = processOutputRepository.getProcessOutputs(actualProcessIdList.get(j));
            Assert.assertEquals(actualsize, savedProcessOutputList.size());

            List<OutputDataObjectType> actualProcessOutputList = actualProcessOutputMap.get(actualProcessIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessOutputList, savedProcessOutputList, "__isset_bitfield"));
        }
    }

}
