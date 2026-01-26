/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatistics;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.ExperimentSummaryService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ExperimentSummaryRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final StatusService statusService;
    private final ExperimentSummaryService experimentSummaryService;
    private final ExperimentSummaryRepository experimentSummaryRepository;

    public ExperimentSummaryRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            StatusService statusService,
            ExperimentSummaryService experimentSummaryService,
            ExperimentSummaryRepository experimentSummaryRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.statusService = statusService;
        this.experimentSummaryService = experimentSummaryService;
        this.experimentSummaryRepository = experimentSummaryRepository;
    }

    private void saveExperimentSummary(ExperimentModel experimentModel, String status) {}

    @Test
    public void testExperimentSummaryRepository() throws RegistryException, RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);
        String projectId = projectService.addProject(project, gatewayId);

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

        ExperimentModel experimentModelThree = new ExperimentModel();
        experimentModelThree.setProjectId(projectId);
        experimentModelThree.setGatewayId(gatewayId);
        experimentModelThree.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModelThree.setUserName("userThree");
        experimentModelThree.setExperimentName("nameThree");
        experimentModelThree.setDescription("descriptionThree");
        experimentModelThree.setExecutionId("executionIdThree");

        String experimentIdOne = experimentService.addExperiment(experimentModelOne);
        assertTrue(experimentIdOne != null);

        experimentModelOne = experimentService.getExperiment(experimentIdOne);
        saveExperimentSummary(experimentModelOne, null);

        String experimentIdTwo = experimentService.addExperiment(experimentModelTwo);
        assertTrue(experimentIdTwo != null);

        experimentModelTwo = experimentService.getExperiment(experimentIdTwo);
        saveExperimentSummary(experimentModelTwo, null);

        String experimentIdThree = experimentService.addExperiment(experimentModelThree);
        assertTrue(experimentIdThree != null);

        experimentModelThree = experimentService.getExperiment(experimentIdThree);
        saveExperimentSummary(experimentModelThree, null);

        Timestamp timeOne = Timestamp.valueOf("2010-01-01 09:00:00");
        experimentModelOne.setCreationTime(timeOne.getTime());
        experimentService.updateExperiment(experimentModelOne, experimentIdOne);
        saveExperimentSummary(experimentModelOne, null);

        Timestamp timeTwo = Timestamp.valueOf("2018-01-01 09:00:00");
        experimentModelTwo.setCreationTime(timeTwo.getTime());
        experimentService.updateExperiment(experimentModelTwo, experimentIdTwo);
        saveExperimentSummary(experimentModelTwo, null);

        Timestamp timeThree = Timestamp.valueOf("2020-01-01 09:00:00");
        experimentModelThree.setCreationTime(timeThree.getTime());
        experimentService.updateExperiment(experimentModelThree, experimentIdThree);
        saveExperimentSummary(experimentModelThree, null);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.PROJECT_ID, projectId);

        List<String> allExperimentIds = Arrays.asList(experimentIdOne, experimentIdTwo, experimentIdThree);
        List<ExperimentSummaryModel> experimentSummaryModelList =
                experimentSummaryService.searchAllAccessibleExperiments(allExperimentIds, filters, -1, 0, null, null);
        assertEquals(3, experimentSummaryModelList.size());

        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdTwo");

        experimentSummaryModelList =
                experimentSummaryService.searchAllAccessibleExperiments(allExperimentIds, filters, -1, 0, null, null);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        String fromDate =
                String.valueOf(Timestamp.valueOf("2010-10-10 09:00:00").getTime());
        String toDate = String.valueOf(AiravataUtils.getUniqueTimestamp().getTime());

        filters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
        filters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);
        experimentSummaryModelList =
                experimentSummaryService.searchAllAccessibleExperiments(allExperimentIds, filters, -1, 0, null, null);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        filters.remove(DBConstants.ExperimentSummary.FROM_DATE);
        filters.remove(DBConstants.ExperimentSummary.TO_DATE);

        List<String> accessibleExperimentIds = new ArrayList<>();
        accessibleExperimentIds.add(experimentIdOne);
        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdOne");

        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                accessibleExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertTrue(experimentSummaryModelList.size() == 1);
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());

        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                Collections.emptyList(),
                Collections.singletonMap(DBConstants.Experiment.GATEWAY_ID, gatewayId),
                -1,
                0,
                DBConstants.Experiment.CREATION_TIME,
                ResultOrderType.ASC);
        assertEquals(0, experimentSummaryModelList.size(), "should return no experiments since none are accessible");

        filters.clear();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.USER_NAME, "userOne");
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only userOne's exp");
        assertEquals("userOne", experimentSummaryModelList.get(0).getUserName());

        filters.clear();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 2, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(2, experimentSummaryModelList.size(), "should only return 2 experiments since limit=2");
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(1).getExperimentId());

        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 2, 2, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(
                1,
                experimentSummaryModelList.size(),
                "should only return 1 experiment since limit=2 but partial last page");
        assertEquals(experimentIdThree, experimentSummaryModelList.get(0).getExperimentId());

        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 3, 3, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(
                0,
                experimentSummaryModelList.size(),
                "should return 0 since we're just past the last page (page size of 3)");

        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 3, 10, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(
                0,
                experimentSummaryModelList.size(),
                "should return 0 since we're well past the last page (page size of 3)");

        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.USER_NAME, "userTwo");
        filters.put(DBConstants.Experiment.RESOURCE_HOST_ID, "resourceHost");
        filters.put(DBConstants.Experiment.EXECUTION_ID, "executionIdTwo");
        filters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
        filters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);

        ExperimentStatistics experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 10, 0);
        assertTrue(experimentStatistics.getAllExperimentCount() == 0);

        filters.remove(DBConstants.Experiment.RESOURCE_HOST_ID);

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 10, 0);
        assertTrue(experimentStatistics.getAllExperimentCount() == 1);
        assertEquals(experimentStatistics.getAllExperiments().get(0).getExperimentId(), experimentIdTwo);

        filters.remove(DBConstants.Experiment.USER_NAME);
        filters.remove(DBConstants.Experiment.EXECUTION_ID);

        ExperimentStatus experimentStatusOne = new ExperimentStatus();
        experimentStatusOne.setState(ExperimentState.CREATED);
        String statusIdOne = statusService.addExperimentStatus(experimentStatusOne, experimentIdOne);
        assertTrue(statusIdOne != null);
        saveExperimentSummary(experimentModelOne, ExperimentState.CREATED.name());

        ExperimentStatus experimentStatusTwo = new ExperimentStatus();
        experimentStatusTwo.setState(ExperimentState.EXECUTING);
        String statusIdTwo = statusService.addExperimentStatus(experimentStatusTwo, experimentIdTwo);
        assertTrue(statusIdTwo != null);
        saveExperimentSummary(experimentModelTwo, ExperimentState.EXECUTING.name());

        ExperimentStatus experimentStatusThree = new ExperimentStatus();
        experimentStatusThree.setState(ExperimentState.CANCELED);
        String statusIdThree = statusService.addExperimentStatus(experimentStatusThree, experimentIdThree);
        assertTrue(statusIdThree != null);
        saveExperimentSummary(experimentModelThree, ExperimentState.CANCELED.name());

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 10, 0);
        assertEquals(2, experimentStatistics.getAllExperimentCount());
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);

        assertEquals(
                experimentIdThree,
                experimentStatistics.getAllExperiments().get(0).getExperimentId());

        filters.remove(DBConstants.ExperimentSummary.FROM_DATE);
        filters.remove(DBConstants.ExperimentSummary.TO_DATE);

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 10, 0);
        assertTrue(experimentStatistics.getAllExperimentCount() == 3);
        assertTrue(experimentStatistics.getCreatedExperimentCount() == 1);
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);

        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, ExperimentState.CREATED.name());
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only one CREATED exp");
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());

        filters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, ExperimentState.EXECUTING.name());
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only one EXECUTING exp");
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        experimentStatistics = experimentSummaryService.getAccessibleExperimentStatistics(
                Collections.singletonList(experimentIdTwo), filters, 10, 0);
        assertTrue(experimentStatistics.getAllExperimentCount() == 1);
        assertTrue(experimentStatistics.getCreatedExperimentCount() == 0);
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);

        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 1, 0);

        assertEquals(3, experimentStatistics.getAllExperimentCount());

        assertEquals(1, experimentStatistics.getAllExperiments().size());
        assertEquals(
                experimentIdThree,
                experimentStatistics.getAllExperiments().get(0).getExperimentId());

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 1, 1);

        assertEquals(3, experimentStatistics.getAllExperimentCount());

        assertEquals(1, experimentStatistics.getAllExperiments().size());
        assertEquals(
                experimentIdTwo, experimentStatistics.getAllExperiments().get(0).getExperimentId());

        experimentService.removeExperiment(experimentIdOne);
        experimentService.removeExperiment(experimentIdTwo);
        experimentService.removeExperiment(experimentIdThree);

        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
