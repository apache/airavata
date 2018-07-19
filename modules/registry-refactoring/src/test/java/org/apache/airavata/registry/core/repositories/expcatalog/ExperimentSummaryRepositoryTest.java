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
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExperimentSummaryRepositoryTest extends TestBase{

    private static final Logger logger = LoggerFactory.getLogger(ExperimentSummaryRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ExperimentStatusRepository experimentStatusRepository;
    ExperimentSummaryRepository experimentSummaryRepository;

    public ExperimentSummaryRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentStatusRepository = new ExperimentStatusRepository();
        experimentSummaryRepository = new ExperimentSummaryRepository();
    }

    @Test
    public void ExperimentSummaryRepositoryTest() throws RegistryException {
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

        ExperimentModel experimentModelOne = new ExperimentModel();
        experimentModelOne.setProjectId(projectId);
        experimentModelOne.setGatewayId(gatewayId);
        experimentModelOne.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModelOne.setUserName("userOne");
        experimentModelOne.setExperimentName("nameOne");
        experimentModelOne.setDescription("descriptionOne");
        experimentModelOne.setExecutionId("executionIdOne");

        ExperimentModel experimentModelTwo = new ExperimentModel();
        experimentModelTwo.setProjectId(projectId);
        experimentModelTwo.setGatewayId(gatewayId);
        experimentModelTwo.setExperimentType(ExperimentType.WORKFLOW);
        experimentModelTwo.setUserName("userTwo");
        experimentModelTwo.setExperimentName("nameTwo");
        experimentModelTwo.setDescription("descriptionTwo");
        experimentModelTwo.setExecutionId("executionIdTwo");

        String experimentIdOne = experimentRepository.addExperiment(experimentModelOne);
        assertTrue(experimentIdOne != null);
        // Reload experiment to get its status' identifier
        experimentModelOne = experimentRepository.getExperiment(experimentIdOne);

        String expertimentIdTwo = experimentRepository.addExperiment(experimentModelTwo);
        assertTrue(expertimentIdTwo != null);
        // Reload experiment to get its status' identifier
        experimentModelTwo = experimentRepository.getExperiment(expertimentIdTwo);

        Timestamp timeOne = Timestamp.valueOf("2010-01-01 09:00:00");
        experimentModelOne.setCreationTime(timeOne.getTime());
        experimentRepository.updateExperiment(experimentModelOne, experimentIdOne);

        Timestamp timeTwo = Timestamp.valueOf("2018-01-01 09:00:00");
        experimentModelTwo.setCreationTime(timeTwo.getTime());
        experimentRepository.updateExperiment(experimentModelTwo, expertimentIdTwo);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.PROJECT_ID, projectId);

        List<ExperimentSummaryModel> experimentSummaryModelList = experimentSummaryRepository.
                searchExperiments(filters, -1, 0, null, null);
        assertEquals(2, experimentSummaryModelList.size());

        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdTwo");

        experimentSummaryModelList = experimentSummaryRepository.
                searchExperiments(filters, -1, 0, null, null);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(expertimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        String fromDate = String.valueOf(Timestamp.valueOf("2010-10-10 09:00:00"));
        String toDate = String.valueOf(new Timestamp(System.currentTimeMillis()));

        filters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
        filters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);
        experimentSummaryModelList = experimentSummaryRepository.
                searchExperiments(filters, -1, 0, null, null);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(expertimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        filters.remove(DBConstants.ExperimentSummary.FROM_DATE);
        filters.remove(DBConstants.ExperimentSummary.TO_DATE);

        List<String> accessibleExperimentIds = new ArrayList<>();
        accessibleExperimentIds.add(experimentIdOne);
        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdOne");

        experimentSummaryModelList = experimentSummaryRepository.
                searchAllAccessibleExperiments(accessibleExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());

        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.USER_NAME, "userTwo");
        filters.put(DBConstants.Experiment.RESOURCE_HOST_ID, "resourceHost");
        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdTwo");
        filters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
        filters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);

        ExperimentStatistics experimentStatistics = experimentSummaryRepository.getExperimentStatistics(filters);
        assertTrue(experimentStatistics.getAllExperimentCount() == 0);

        filters.remove(DBConstants.Experiment.RESOURCE_HOST_ID);

        experimentStatistics = experimentSummaryRepository.getExperimentStatistics(filters);
        assertTrue(experimentStatistics.getAllExperimentCount() == 1);
        assertEquals(experimentStatistics.getAllExperiments().get(0).getExperimentId(), expertimentIdTwo);

        filters.remove(DBConstants.Experiment.USER_NAME);
        filters.remove(DBConstants.Experiment.EXECUTION_ID);

        ExperimentStatus experimentStatusOne = new ExperimentStatus(ExperimentState.CREATED);
        String statusIdOne = experimentStatusRepository.addExperimentStatus(experimentStatusOne, experimentIdOne);
        assertTrue(statusIdOne != null);

        ExperimentStatus experimentStatusTwo = new ExperimentStatus(ExperimentState.EXECUTING);
        String statusIdTwo = experimentStatusRepository.addExperimentStatus(experimentStatusTwo, expertimentIdTwo);
        assertTrue(statusIdTwo != null);

        experimentStatistics = experimentSummaryRepository.getExperimentStatistics(filters);
        assertTrue(experimentStatistics.getAllExperimentCount() == 1);
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);
        assertEquals(expertimentIdTwo, experimentStatistics.getAllExperiments().get(0).getExperimentId());

        filters.remove(DBConstants.ExperimentSummary.FROM_DATE);
        filters.remove(DBConstants.ExperimentSummary.TO_DATE);

        experimentStatistics = experimentSummaryRepository.getExperimentStatistics(filters);
        assertTrue(experimentStatistics.getAllExperimentCount() == 2);
        assertTrue(experimentStatistics.getCreatedExperimentCount() == 1);

        experimentRepository.removeExperiment(experimentIdOne);
        experimentRepository.removeExperiment(expertimentIdTwo);

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
