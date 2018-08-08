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
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

public class ProcessStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private ProcessStatusRepository processStatusRepository;

    public ProcessStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        processStatusRepository = new ProcessStatusRepository();
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
    public void addProcessStatusRepositoryTest() throws RegistryException {
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

        ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
        String processStatusId = processStatusRepository.addProcessStatus(processStatus, processId);
        assertNotNull(processStatusId);
        assertEquals(1, processRepository.getProcess(processId).getProcessStatuses().size());

        ProcessStatus savedProcessStatus = processStatusRepository.getProcessStatus(processId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processStatus, savedProcessStatus, "__isset_bitfield"));
    }

    @Test
    public void updateProcessStatusRepositoryTest() throws RegistryException {
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

        ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
        String processStatusId = processStatusRepository.addProcessStatus(processStatus, processId);
        assertNotNull(processStatusId);
        assertEquals(1, processRepository.getProcess(processId).getProcessStatuses().size());

        processStatus.setState(ProcessState.EXECUTING);
        processStatusRepository.updateProcessStatus(processStatus, processId);

        ProcessStatus savedProcessStatus = processStatusRepository.getProcessStatus(processId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processStatus, savedProcessStatus, "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleProcessStatusRepositoryTest() throws RegistryException {
        List<ProcessStatus> actualProcessStatusList = new ArrayList<>();
        List<String> processIdList = new ArrayList<>();

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

            ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
            String processStatusId = processStatusRepository.addProcessStatus(processStatus, processId);
            assertNotNull(processStatusId);
            assertEquals(1, processRepository.getProcess(processId).getProcessStatuses().size());

            actualProcessStatusList.add(processStatus);
            processIdList.add(processId);
        }
        for (int j = 0 ; j < 5; j++) {
            ProcessStatus savedProcessStatus = processStatusRepository.getProcessStatus(processIdList.get(j));
            ProcessStatus actualProcessStatus = actualProcessStatusList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessStatus, savedProcessStatus, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleProcessStatusRepositoryTest() throws RegistryException {
        List<String> actualProcessIdList = new ArrayList<>();
        HashMap<String, ProcessStatus> actualProcessStatusMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("1" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);
            assertNotNull(processId);

            ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
            String processStatusId = processStatusRepository.addProcessStatus(processStatus, processId);
            assertNotNull(processStatusId);
            assertEquals(1, processRepository.getProcess(processId).getProcessStatuses().size());

            actualProcessIdList.add(processId);
            actualProcessStatusMap.put(processId, processStatus);
        }

        for (int j = 0 ; j < 5; j++) {
            ProcessStatus savedProcess = processStatusRepository.getProcessStatus(actualProcessIdList.get(j));
            ProcessStatus actualProcess = actualProcessStatusMap.get(actualProcessIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcess, savedProcess, "__isset_bitfield"));
        }
    }

}
