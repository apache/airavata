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
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
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

import static org.junit.Assert.*;

public class ProcessErrorRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessErrorRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    ProcessErrorRepository processErrorRepository;

    public ProcessErrorRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        processErrorRepository = new ProcessErrorRepository();
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

    @Test
    public void addProcessRepositoryTest() throws RegistryException {
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

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");
        processModel.addToProcessErrors(errorModel);

        String processErrorId = processErrorRepository.addProcessError(errorModel, processId);
        assertNotNull(processErrorId);
        assertEquals(1, processRepository.getProcess(processId).getProcessErrors().size());

        List<ErrorModel> processListError = processErrorRepository.getProcessError(processId);
        ErrorModel savedErrorModel = processListError.get(0);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, savedErrorModel, "__isset_bitfield"));
    }

    @Test
    public void updateProcessRepositoryTest() throws RegistryException {
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

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        processModel.addToProcessErrors(errorModel);

        String processErrorId = processErrorRepository.addProcessError(errorModel, processId);
        assertNotNull(processErrorId);
        assertEquals(1, processRepository.getProcess(processId).getProcessErrors().size());

        errorModel.setActualErrorMessage("message");
        processErrorRepository.updateProcessError(errorModel, processId);

        List<ErrorModel> processListError = processErrorRepository.getProcessError(processId);
        ErrorModel savedErrorModel = processListError.get(0);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, savedErrorModel, "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleProcessRepositoryTest() throws RegistryException {
        List<ErrorModel> actualProcessErrorList = new ArrayList<>();
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

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error" + i);
            processModel.addToProcessErrors(errorModel);

            String processErrorId = processErrorRepository.addProcessError(errorModel, processId);
            assertNotNull(processErrorId);
            assertEquals(1, processRepository.getProcess(processId).getProcessErrors().size());

            errorModel.setActualErrorMessage("message" + i);
            processErrorRepository.updateProcessError(errorModel, processId);
            actualProcessErrorList.add(errorModel);
            actualProcessIdList.add(processId);
        }

        for (int j = 0 ; j < 5; j++) {
            List<ErrorModel> savedErrorModelList = processErrorRepository.getProcessError(actualProcessIdList.get(j));
            ErrorModel savedErrorModel = savedErrorModelList.get(0);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessErrorList.get(j), savedErrorModel, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleRepositoryTest() throws RegistryException {
        List<String> actualProcessIdList  = new ArrayList<>();
        HashMap<String, ErrorModel> actualProcessErrorModelMap = new HashMap<>();

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
            assertTrue(processId != null);

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error" + i);
            processModel.addToProcessErrors(errorModel);

            String processErrorId = processErrorRepository.addProcessError(errorModel, processId);
            assertNotNull(processErrorId);
            assertEquals(1, processRepository.getProcess(processId).getProcessErrors().size());

            errorModel.setActualErrorMessage("message" + i);
            processErrorRepository.updateProcessError(errorModel, processId);

            actualProcessIdList.add(processId);
            actualProcessErrorModelMap.put(processId, errorModel);
        }

        for (int j = 0 ; j < 5; j++) {
            List<ErrorModel> savedErrorModelList = processErrorRepository.getProcessError(actualProcessIdList.get(j));
            ErrorModel savedErrorModel = savedErrorModelList.get(0);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessErrorModelMap.get(actualProcessIdList.get(j)), savedErrorModel, "__isset_bitfield"));
        }
    }

}
