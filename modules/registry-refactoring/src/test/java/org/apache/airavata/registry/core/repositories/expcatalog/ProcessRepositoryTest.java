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
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ProcessRepositoryTest {

    private static Initialize initialize;
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProcessRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
            experimentRepository = new ExperimentRepository();
            processRepository = new ProcessRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void ProcessRepositoryTest() throws RegistryException {
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
        assertTrue(experimentRepository.getExperiment(experimentId).getProcesses().size() == 1);

        processModel.setProcessDetail("detail");
        processModel.setUseUserCRPref(true);
        processRepository.updateProcess(processModel, processId);

        ProcessModel retrievedProcessModel = processRepository.getProcess(processId);
        assertEquals(experimentId, retrievedProcessModel.getExperimentId());
        assertEquals("detail", retrievedProcessModel.getProcessDetail());
        assertTrue(retrievedProcessModel.isUseUserCRPref());

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel.setQueueName("queue");
        processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        assertEquals("queue", processRepository.getProcessResourceSchedule(processId).getQueueName());

        List<String> processIdList = processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        assertTrue(processIdList.size() == 1);
        assertTrue(processIdList.get(0).equals(processId));

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        assertFalse(processRepository.isProcessExist(processId));

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
