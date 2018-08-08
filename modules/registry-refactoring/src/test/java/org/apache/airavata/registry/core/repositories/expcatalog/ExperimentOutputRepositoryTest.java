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

public class ExperimentOutputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentOutputRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ExperimentOutputRepository experimentOutputRepository;

    public ExperimentOutputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentOutputRepository = new ExperimentOutputRepository();
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

    private void addExperimentOutputObjects(String experimentId,
                                           HashMap<String, List<OutputDataObjectType>> actualOutputObjectModelMap,
                                           int count) throws RegistryException {

        List<OutputDataObjectType> tempOutputObjectList = new ArrayList<>();

        for (int k = 0; k < count; k++) {
            OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
            outputDataObjectTypeExp.setName("inputE" + k);
            outputDataObjectTypeExp.setType(DataType.STRING);
            tempOutputObjectList.add(outputDataObjectTypeExp);
            String experimentInputId = experimentOutputRepository.addExperimentOutputs(tempOutputObjectList, experimentId);
            Assert.assertNotNull(experimentInputId);
        }
        actualOutputObjectModelMap.put(experimentId, tempOutputObjectList);
    }

    @Test
    public void addExperimentOutputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
        outputDataObjectTypeExp.setName("outputE");
        outputDataObjectTypeExp.setType(DataType.STRING);

        List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
        outputDataObjectTypeExpList.add(outputDataObjectTypeExp);
        experimentOutputRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId);
        List<OutputDataObjectType> expectedoutputDataObjectTypeExpList = experimentOutputRepository.getExperimentOutputs(experimentId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedoutputDataObjectTypeExpList, outputDataObjectTypeExpList, "__isset_bitfield"));
        Assert.assertEquals(experimentId, experimentOutputRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId));
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentOutputs().size());
    }

    @Test
    public void updateExperimentOutputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
        outputDataObjectTypeExp.setName("outputE");
        outputDataObjectTypeExp.setType(DataType.STRING);

        List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
        outputDataObjectTypeExpList.add(outputDataObjectTypeExp);
        experimentOutputRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentOutputs().size());

        outputDataObjectTypeExp.setValue("oValueE");
        experimentOutputRepository.updateExperimentOutputs(outputDataObjectTypeExpList, experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentOutputs().size());

        List<OutputDataObjectType> expectedinputDataObjectTypeExpList = experimentOutputRepository.getExperimentOutputs(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedinputDataObjectTypeExpList, outputDataObjectTypeExpList, "__isset_bitfield"));
        Assert.assertEquals("oValueE",expectedinputDataObjectTypeExpList.get(0).getValue());
    }

    @Test
    public void retrieveSingleExperimentOutputTest() throws RegistryException {
        List<OutputDataObjectType> actualOutputObjectList = new ArrayList<>();
        List<String> experimentIdList = new ArrayList<>();

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
            experimentIdList.add(experimentId);

            OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
            outputDataObjectTypeExp.setName("outputE");
            outputDataObjectTypeExp.setType(DataType.STRING);
            actualOutputObjectList.add(outputDataObjectTypeExp);

            List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
            outputDataObjectTypeExpList.add(outputDataObjectTypeExp);

            String experimentInputId = experimentOutputRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId);
            Assert.assertNotNull(experimentInputId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<OutputDataObjectType> experimentOutputList = experimentOutputRepository.getExperimentOutputs(experimentIdList.get(j));
            Assert.assertEquals(1, experimentOutputList.size());

            OutputDataObjectType actualOutputObject = actualOutputObjectList.get(j);
            List<OutputDataObjectType> savedOutputObjects = experimentOutputRepository.getExperimentOutputs(experimentIdList.get(j));
            OutputDataObjectType expectedOutputObject = savedOutputObjects.get(0);
            Assert.assertEquals(actualOutputObjectList.get(j).getType(), savedOutputObjects.get(0).getType());
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualOutputObject, expectedOutputObject, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleExperimentOutputTest() throws RegistryException {
        List<String> experimentIdList = new ArrayList<>();
        HashMap<String, List<OutputDataObjectType>> actualOutputObjectModelMap = new HashMap<>();

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
            experimentIdList.add(experimentId);

            addExperimentOutputObjects(experimentId,actualOutputObjectModelMap,i+1);
        }

        for (int j = 0 ; j < 5; j++) {
            int actualSize = j+1;
            List<OutputDataObjectType> retrievedOutputObjectTypeList = experimentRepository.getExperiment(experimentIdList.get(j)).getExperimentOutputs();
            Assert.assertEquals(actualSize, retrievedOutputObjectTypeList.size());

            List<OutputDataObjectType> actualExperimentOutputObjectList = actualOutputObjectModelMap.get(experimentIdList.get(j));
            List<OutputDataObjectType> savedExperimentOutputObjectList = experimentRepository.getExperiment(experimentIdList.get(j)).getExperimentOutputs();

            Assert.assertEquals(actualExperimentOutputObjectList.get(j).getType(), savedExperimentOutputObjectList.get(j).getType());
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentOutputObjectList, savedExperimentOutputObjectList, "__isset_bitfield"));
        }
    }

}
