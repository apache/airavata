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

import org.apache.airavata.model.commons.ErrorModel;
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

import java.util.*;

public class ExperimentErrorRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentErrorRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ExperimentErrorRepository experimentErrorRepository;

    public ExperimentErrorRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentErrorRepository = new ExperimentErrorRepository();
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

    private void addExperimentErrors(String experimentId,
                                     HashMap<String, List<ErrorModel>> actualErrorModelMap,
                                     int count) throws RegistryException {

        List<ErrorModel> tempErrorModelList = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error" + k);
            tempErrorModelList.add(errorModel);
            String experimentErrorId = experimentErrorRepository.addExperimentError(errorModel, experimentId);
            Assert.assertNotNull(experimentErrorId);
        }
        actualErrorModelMap.put(experimentId, tempErrorModelList);
    }

    @Test
    public void createExperimentErrorTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        String experimentErrorId = experimentErrorRepository.addExperimentError(errorModel, experimentId);
        Assert.assertNotNull(experimentErrorId);

        List<ErrorModel> savedErrors = experimentRepository.getExperiment(experimentId).getErrors();
        Assert.assertEquals(1, savedErrors.size());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, savedErrors.get(0), "__isset_bitfield"));
    }

    @Test
    public void updateExperimentErrorTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);


        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        String experimentErrorId = experimentErrorRepository.addExperimentError(errorModel, experimentId);

        Assert.assertNotNull(experimentErrorId);

        errorModel.setActualErrorMessage("message");
        experimentErrorRepository.updateExperimentError(errorModel, experimentId);

        List<ErrorModel> updatedErrors = experimentRepository.getExperiment(experimentId).getErrors();
        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, updatedErrors.get(0), "__isset_bitfield"));
        Assert.assertEquals(errorModel.getActualErrorMessage(), updatedErrors.get(0).getActualErrorMessage());
    }

    @Test
    public void retrieveSingleExperimentErrorTest() throws RegistryException {
        List<ErrorModel> actualErrorModelList = new ArrayList<>();
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

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error");
            actualErrorModelList.add(errorModel);

            String experimentErrorId = experimentErrorRepository.addExperimentError(errorModel, experimentId);
            Assert.assertNotNull(experimentErrorId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<ErrorModel> retrievedErrorList = experimentErrorRepository.getExperimentErrors(experimentIdList.get(j));
            Assert.assertEquals(1, retrievedErrorList.size());

            ErrorModel actualErrorModel = actualErrorModelList.get(j);
            List<ErrorModel> savedErrors = experimentRepository.getExperiment(experimentIdList.get(j)).getErrors();
            ErrorModel expectedErrorModel = savedErrors.get(0);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualErrorModel, expectedErrorModel, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleExperimentErrorTest() throws RegistryException {
        List<String> experimentIdList = new ArrayList<>();
        HashMap<String, List<ErrorModel>> actualErrorModelMap = new HashMap<>();

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

            addExperimentErrors(experimentId, actualErrorModelMap, i + 1);
        }

        for (int j = 0 ; j < 5; j++) {
            List<ErrorModel> retrievedErrorList = experimentErrorRepository.getExperimentErrors(experimentIdList.get(j));
            Assert.assertEquals(j + 1, retrievedErrorList.size());

            List<ErrorModel> actualErrorModelList = actualErrorModelMap.get(experimentIdList.get(j));
            List<ErrorModel> savedErrorsList = experimentRepository.getExperiment(experimentIdList.get(j)).getErrors();
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualErrorModelList, savedErrorsList, "__isset_bitfield"));
            }
    }

}
