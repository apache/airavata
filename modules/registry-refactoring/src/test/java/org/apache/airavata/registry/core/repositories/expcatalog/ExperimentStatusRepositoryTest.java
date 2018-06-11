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
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExperimentStatusRepositoryTest {

    private static Initialize initialize;
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ExperimentStatusRepository experimentStatusRepository;
    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
            experimentRepository = new ExperimentRepository();
            experimentStatusRepository = new ExperimentStatusRepository();
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
    public void ExperimentStatusRepositoryTest() throws RegistryException {
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
        assertTrue(experimentId != null);
        // addExperiment adds the CREATED experiment status
        assertEquals(1, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());

        ExperimentStatus experimentStatus = new ExperimentStatus(ExperimentState.VALIDATED);
        String experimentStatusId = experimentStatusRepository.addExperimentStatus(experimentStatus, experimentId);
        assertTrue(experimentStatusId != null);
        assertEquals(2, experimentRepository.getExperiment(experimentId).getExperimentStatus().size());

        experimentStatus.setState(ExperimentState.EXECUTING);
        experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);

        ExperimentStatus updatedExecutingStatus = new ExperimentStatus(ExperimentState.EXECUTING);
        updatedExecutingStatus.setReason("updated reason");
        String updatedExperimentStatusId = experimentStatusRepository.updateExperimentStatus(updatedExecutingStatus, experimentId);

        ExperimentStatus retrievedExpStatus = experimentStatusRepository.getExperimentStatus(experimentId);
        assertEquals(ExperimentState.EXECUTING, retrievedExpStatus.getState());
        assertEquals("updated reason", updatedExecutingStatus.getReason());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
