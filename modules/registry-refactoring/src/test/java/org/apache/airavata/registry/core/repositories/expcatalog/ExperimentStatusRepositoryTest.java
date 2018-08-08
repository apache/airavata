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

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
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
import java.util.List;

import static org.junit.Assert.*;

public class ExperimentStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ExperimentStatusRepository experimentStatusRepository;

    public ExperimentStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentStatusRepository = new ExperimentStatusRepository();
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

    private void addExperimentStatus(String experimentId, List<ExperimentStatus> actualExperimentList) throws RegistryException {
            ExperimentStatus experimentStatus = new ExperimentStatus(ExperimentState.VALIDATED);
            experimentStatus.setReason("Reason");
            String experimentStatusId = experimentStatusRepository.addExperimentStatus(experimentStatus, experimentId);
            actualExperimentList.add(experimentStatus);
            Assert.assertNotNull(experimentStatusId);
    }

    @Test
    public void addExperimentStatusRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());

        ExperimentStatus savedExperimentStatus = new ExperimentStatus(ExperimentState.VALIDATED);
        String experimentStatusId = experimentStatusRepository.addExperimentStatus(savedExperimentStatus, experimentId);
        Assert.assertNotNull(experimentStatusId);

        ExperimentStatus retrievedExperimentStatus = experimentStatusRepository.getExperimentStatus(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedExperimentStatus, retrievedExperimentStatus, "__isset_bitfield"));
        assertEquals(2, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());
    }

    @Test
    public void updateExperimentStatusRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);
        Assert.assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());

        ExperimentStatus experimentStatus = new ExperimentStatus(ExperimentState.VALIDATED);
        String experimentStatusId = experimentStatusRepository.addExperimentStatus(experimentStatus, experimentId);
        assertNotNull(experimentStatusId);
        assertEquals(2, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());
        experimentStatus.setState(ExperimentState.EXECUTING);
        experimentStatus.setReason("updated Reason");

        experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);

        ExperimentStatus retrievedExpStatus = experimentStatusRepository.getExperimentStatus(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentStatus, retrievedExpStatus, "__isset_bitfield"));
        assertEquals(ExperimentState.EXECUTING, retrievedExpStatus.getState());
        assertEquals("updated Reason", retrievedExpStatus.getReason());
    }

    @Test
    public void retrieveSingleExperimentStatusRepositoryTest() throws RegistryException {
        List<ExperimentStatus> savedExperimentStatusList = new ArrayList<>();
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

            ExperimentStatus savedExperimentStatus = new ExperimentStatus(ExperimentState.VALIDATED);
            String experimentStatusId = experimentStatusRepository.addExperimentStatus(savedExperimentStatus, experimentId);
            Assert.assertNotNull(experimentStatusId);

            savedExperimentStatusList.add(savedExperimentStatus);
        }

        for (int j = 0 ; j < 5; j++) {
            ExperimentStatus actualExperimentStatus = savedExperimentStatusList.get(j);
            ExperimentStatus expectedExperimentStatus = experimentStatusRepository.getExperimentStatus(experimentIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentStatus, expectedExperimentStatus, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleExperimentStatusTest() throws RegistryException {
        List<String> experimentIdList = new ArrayList<>();
        List<ExperimentStatus> actualExperimentList = new ArrayList<>();

        for (int i = 1 ; i < 5; i++) {
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

            addExperimentStatus(experimentId, actualExperimentList);
        }

        for (int j = 0 ; j < 4; j++) {
            ExperimentStatus actualExperimentStatus = actualExperimentList.get(j);
            ExperimentStatus expectedExperimentStatus = experimentStatusRepository.getExperimentStatus(experimentIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentStatus, expectedExperimentStatus, "__isset_bitfield"));
        }
    }

}
