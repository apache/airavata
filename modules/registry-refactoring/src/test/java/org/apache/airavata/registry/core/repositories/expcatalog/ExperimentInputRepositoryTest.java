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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExperimentInputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentInputRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ExperimentInputRepository experimentInputRepository;

    public ExperimentInputRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentInputRepository = new ExperimentInputRepository();
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

    private void addExperimentInputObjects(String experimentId,
                                         HashMap<String, List<InputDataObjectType>> actualInputObjectModelMap,
                                         int count) throws RegistryException {

        List<InputDataObjectType> tempInputObjectList = new ArrayList<>();

        for (int k = 0; k < count; k++) {
            InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
            inputDataObjectTypeExp.setName("inputE" + k);
            inputDataObjectTypeExp.setType(DataType.STRING);
            tempInputObjectList.add(inputDataObjectTypeExp);
            String experimentInputId = experimentInputRepository.addExperimentInputs(tempInputObjectList, experimentId);
            Assert.assertNotNull(experimentInputId);
        }
        actualInputObjectModelMap.put(experimentId, tempInputObjectList);
    }

    @Test
    public void addExperimentInputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");
        inputDataObjectTypeExp.setType(DataType.STRING);

        List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
        inputDataObjectTypeExpList.add(inputDataObjectTypeExp);
        experimentInputRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId);
        List<InputDataObjectType> expectedinputDataObjectTypeExpList = experimentInputRepository.getExperimentInputs(experimentId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedinputDataObjectTypeExpList, inputDataObjectTypeExpList, "__isset_bitfield"));
        Assert.assertEquals(experimentId, experimentInputRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId));
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentInputs().size());
    }

    @Test
    public void updateExperimentInputRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");
        inputDataObjectTypeExp.setType(DataType.STRING);

        List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
        inputDataObjectTypeExpList.add(inputDataObjectTypeExp);
        experimentInputRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentInputs().size());

        inputDataObjectTypeExp.setValue("iValueE");
        experimentInputRepository.updateExperimentInputs(inputDataObjectTypeExpList, experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentInputs().size());

        List<InputDataObjectType> expectedinputDataObjectTypeExpList = experimentInputRepository.getExperimentInputs(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedinputDataObjectTypeExpList, inputDataObjectTypeExpList, "__isset_bitfield"));
        Assert.assertEquals("iValueE",expectedinputDataObjectTypeExpList.get(0).getValue());
    }

    @Test
    public void retrieveSingleExperimentInputTest() throws RegistryException {
        List<InputDataObjectType> actualInputObjectList = new ArrayList<>();
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

            InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
            inputDataObjectTypeExp.setName("inputE");
            inputDataObjectTypeExp.setType(DataType.STRING);
            actualInputObjectList.add(inputDataObjectTypeExp);

            List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
            inputDataObjectTypeExpList.add(inputDataObjectTypeExp);

            String experimentInputId = experimentInputRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId);
            Assert.assertNotNull(experimentInputId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<InputDataObjectType> retrievedErrorList = experimentInputRepository.getExperimentInputs(experimentIdList.get(j));
            Assert.assertEquals(1, retrievedErrorList.size());

            InputDataObjectType actualInputObject = actualInputObjectList.get(j);
            List<InputDataObjectType> savedInputObjects = experimentInputRepository.getExperimentInputs(experimentIdList.get(j));
            InputDataObjectType expectedInputObject = savedInputObjects.get(0);
            Assert.assertEquals(actualInputObjectList.get(j).getType(), savedInputObjects.get(0).getType());
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualInputObject, expectedInputObject, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleExperimentInputTest() throws RegistryException {
        List<String> experimentIdList = new ArrayList<>();
        HashMap<String, List<InputDataObjectType>> actualInputObjectModelMap = new HashMap<>();

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

            addExperimentInputObjects(experimentId,actualInputObjectModelMap,i+1);
        }

        for (int j = 0 ; j < 5; j++) {
            int actualSize = j+1;
            List<InputDataObjectType> retrievedInputObjectTypeList = experimentRepository.getExperiment(experimentIdList.get(j)).getExperimentInputs();
            Assert.assertEquals(actualSize, retrievedInputObjectTypeList.size());

            List<InputDataObjectType> actualExperimentInputObjectList = actualInputObjectModelMap.get(experimentIdList.get(j));
            List<InputDataObjectType> savedExperimentInputObjectList = experimentRepository.getExperiment(experimentIdList.get(j)).getExperimentInputs();

            Assert.assertEquals(actualExperimentInputObjectList.get(j).getType(), savedExperimentInputObjectList.get(j).getType());
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentInputObjectList, savedExperimentInputObjectList, "__isset_bitfield"));
        }
    }
}
