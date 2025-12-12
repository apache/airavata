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
import java.util.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.ExperimentStatusService;
import org.apache.airavata.registry.services.ExperimentSummaryService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, ExperimentSummaryRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.registryService.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ExperimentSummaryRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern =
                                "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
        org.apache.airavata.config.DozerMapperConfig.class
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ExperimentStatusService experimentStatusService;
    private final ExperimentSummaryService experimentSummaryService;

    public ExperimentSummaryRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ExperimentStatusService experimentStatusService,
            ExperimentSummaryService experimentSummaryService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.experimentStatusService = experimentStatusService;
        this.experimentSummaryService = experimentSummaryService;
    }

    @Test
    public void testExperimentSummaryRepository() throws RegistryException {
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
        // Reload experiment to get its status' identifier
        experimentModelOne = experimentService.getExperiment(experimentIdOne);

        String experimentIdTwo = experimentService.addExperiment(experimentModelTwo);
        assertTrue(experimentIdTwo != null);
        // Reload experiment to get its status' identifier
        experimentModelTwo = experimentService.getExperiment(experimentIdTwo);

        String experimentIdThree = experimentService.addExperiment(experimentModelThree);
        assertTrue(experimentIdThree != null);
        // Reload experiment to get its status' identifier
        experimentModelThree = experimentService.getExperiment(experimentIdThree);

        Timestamp timeOne = Timestamp.valueOf("2010-01-01 09:00:00");
        experimentModelOne.setCreationTime(timeOne.getTime());
        experimentService.updateExperiment(experimentModelOne, experimentIdOne);

        Timestamp timeTwo = Timestamp.valueOf("2018-01-01 09:00:00");
        experimentModelTwo.setCreationTime(timeTwo.getTime());
        experimentService.updateExperiment(experimentModelTwo, experimentIdTwo);

        Timestamp timeThree = Timestamp.valueOf("2020-01-01 09:00:00");
        experimentModelThree.setCreationTime(timeThree.getTime());
        experimentService.updateExperiment(experimentModelThree, experimentIdThree);

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
        String toDate = String.valueOf(System.currentTimeMillis());

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

        // Test with empty accessibleExperimentIds
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                Collections.emptyList(),
                Collections.singletonMap(DBConstants.Experiment.GATEWAY_ID, gatewayId),
                -1,
                0,
                DBConstants.Experiment.CREATION_TIME,
                ResultOrderType.ASC);
        assertEquals(0, experimentSummaryModelList.size(), "should return no experiments since none are accessible");

        // Test with a userName filter
        filters.clear();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.Experiment.USER_NAME, "userOne");
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only userOne's exp");
        assertEquals("userOne", experimentSummaryModelList.get(0).getUserName());

        // Test with pagination
        filters.clear();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 2, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(2, experimentSummaryModelList.size(), "should only return 2 experiments since limit=2");
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(1).getExperimentId());
        // page 2
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 2, 2, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(
                1,
                experimentSummaryModelList.size(),
                "should only return 1 experiment since limit=2 but partial last page");
        assertEquals(experimentIdThree, experimentSummaryModelList.get(0).getExperimentId());
        // Test with offset at the end (should return empty list)
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, 3, 3, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(
                0,
                experimentSummaryModelList.size(),
                "should return 0 since we're just past the last page (page size of 3)");
        // Test with offset past the end (should return empty list)
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

        ExperimentStatus experimentStatusOne = new ExperimentStatus(ExperimentState.CREATED);
        String statusIdOne = experimentStatusService.addExperimentStatus(experimentStatusOne, experimentIdOne);
        assertTrue(statusIdOne != null);

        ExperimentStatus experimentStatusTwo = new ExperimentStatus(ExperimentState.EXECUTING);
        String statusIdTwo = experimentStatusService.addExperimentStatus(experimentStatusTwo, experimentIdTwo);
        assertTrue(statusIdTwo != null);

        ExperimentStatus experimentStatusThree = new ExperimentStatus(ExperimentState.CANCELED);
        String statusIdThree = experimentStatusService.addExperimentStatus(experimentStatusThree, experimentIdThree);
        assertTrue(statusIdThree != null);

        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 10, 0);
        assertEquals(2, experimentStatistics.getAllExperimentCount());
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);
        // Experiment 3 is most recent
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

        // Test searchAllAccessibleExperiments with status filtering
        // Only CREATED status
        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        filters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, ExperimentState.CREATED.name());
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only one CREATED exp");
        assertEquals(experimentIdOne, experimentSummaryModelList.get(0).getExperimentId());
        // Only EXECUTING status
        filters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, ExperimentState.EXECUTING.name());
        experimentSummaryModelList = experimentSummaryService.searchAllAccessibleExperiments(
                allExperimentIds, filters, -1, 0, DBConstants.Experiment.CREATION_TIME, ResultOrderType.ASC);
        assertEquals(1, experimentSummaryModelList.size(), "should return only one EXECUTING exp");
        assertEquals(experimentIdTwo, experimentSummaryModelList.get(0).getExperimentId());

        // Experiment 2 is EXECUTING and should be the only one returned
        experimentStatistics = experimentSummaryService.getAccessibleExperimentStatistics(
                Collections.singletonList(experimentIdTwo), filters, 10, 0);
        assertTrue(experimentStatistics.getAllExperimentCount() == 1);
        assertTrue(experimentStatistics.getCreatedExperimentCount() == 0);
        assertTrue(experimentStatistics.getRunningExperimentCount() == 1);

        // Check pagination
        filters = new HashMap<>();
        filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        // First page
        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 1, 0);
        // Should still return total count even when only returning the first page of experiment summaries
        assertEquals(3, experimentStatistics.getAllExperimentCount());
        // experiment 3 is most recent
        assertEquals(1, experimentStatistics.getAllExperimentsSize());
        assertEquals(
                experimentIdThree,
                experimentStatistics.getAllExperiments().get(0).getExperimentId());
        // Second page
        experimentStatistics =
                experimentSummaryService.getAccessibleExperimentStatistics(allExperimentIds, filters, 1, 1);
        // Should still return total count even when only returning the first page of experiment summaries
        assertEquals(3, experimentStatistics.getAllExperimentCount());
        // experiment 2 is less recent
        assertEquals(1, experimentStatistics.getAllExperimentsSize());
        assertEquals(
                experimentIdTwo, experimentStatistics.getAllExperiments().get(0).getExperimentId());

        experimentService.removeExperiment(experimentIdOne);
        experimentService.removeExperiment(experimentIdTwo);
        experimentService.removeExperiment(experimentIdThree);

        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
