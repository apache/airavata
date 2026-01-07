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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ExperimentRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@org.springframework.transaction.annotation.Transactional("expCatalogTransactionManager")
public class ExperimentRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({})
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;

    private String gatewayId;

    private String projectId;

    public ExperimentRepositoryTest(
            GatewayService gatewayService, ProjectService projectService, ExperimentService experimentService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        projectId = projectService.addProject(project, gatewayId);
    }

    @Test
    public void testExperimentRepository() throws RegistryException {

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        experimentModel.setGatewayInstanceId("gateway-instance-id");

        String experimentId = experimentService.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        if (experimentModel.getEmailAddresses() == null) {
            experimentModel.setEmailAddresses(new java.util.ArrayList<>());
        }

        ExperimentModel retrievedExperiment = experimentService.getExperiment(experimentId);
        int emailCount = retrievedExperiment.getEmailAddresses() != null
                ? retrievedExperiment.getEmailAddresses().size()
                : 0;
        assertEquals(0, emailCount);

        experimentModel.setDescription("description");
        experimentModel.getEmailAddresses().add("notify@example.com");
        experimentModel.getEmailAddresses().add("notify2@example.com");
        experimentService.updateExperiment(experimentModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentService.getExperiment(experimentId);
        assertEquals("description", retrievedExperimentModel.getDescription());
        assertEquals(ExperimentType.SINGLE_APPLICATION, retrievedExperimentModel.getExperimentType());
        assertEquals("gateway-instance-id", retrievedExperimentModel.getGatewayInstanceId());
        assertNotNull(retrievedExperimentModel.getExperimentStatus());
        assertEquals(1, retrievedExperimentModel.getExperimentStatus().size());
        assertEquals(
                ExperimentState.CREATED,
                retrievedExperimentModel.getExperimentStatus().get(0).getState());
        assertNotNull(retrievedExperimentModel.getEmailAddresses());
        assertEquals(2, retrievedExperimentModel.getEmailAddresses().size());
        assertEquals(
                "notify@example.com",
                retrievedExperimentModel.getEmailAddresses().get(0));
        assertEquals(
                "notify2@example.com",
                retrievedExperimentModel.getEmailAddresses().get(1));

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setAiravataAutoSchedule(true);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setResourceHostId("resource-host-id");
        computationalResourceSchedulingModel.setTotalCPUCount(12);
        computationalResourceSchedulingModel.setNodeCount(13);
        computationalResourceSchedulingModel.setNumberOfThreads(14);
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber("override-project-num");
        computationalResourceSchedulingModel.setOverrideLoginUserName("override-login-username");
        computationalResourceSchedulingModel.setOverrideScratchLocation("override-scratch-location");
        computationalResourceSchedulingModel.setQueueName("queue-name");
        computationalResourceSchedulingModel.setStaticWorkingDir("static-working-dir");
        computationalResourceSchedulingModel.setTotalPhysicalMemory(1333);
        computationalResourceSchedulingModel.setWallTimeLimit(77);
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        assertEquals(
                experimentId, experimentService.addUserConfigurationData(userConfigurationDataModel, experimentId));

        userConfigurationDataModel.setInputStorageResourceId("storage2");
        userConfigurationDataModel.setOutputStorageResourceId("storage2");
        experimentService.updateUserConfigurationData(userConfigurationDataModel, experimentId);

        final UserConfigurationDataModel retrievedUserConfigurationDataModel =
                experimentService.getUserConfigurationData(experimentId);
        assertNotNull(retrievedUserConfigurationDataModel, "UserConfigurationData should not be null");
        assertEquals("storage2", retrievedUserConfigurationDataModel.getInputStorageResourceId());
        assertEquals("storage2", retrievedUserConfigurationDataModel.getOutputStorageResourceId());
        final ComputationalResourceSchedulingModel retrievedComputationalResourceScheduling =
                retrievedUserConfigurationDataModel.getComputationalResourceScheduling();
        assertNotNull(retrievedComputationalResourceScheduling);
        assertEquals("resource-host-id", retrievedComputationalResourceScheduling.getResourceHostId());
        assertEquals(12, retrievedComputationalResourceScheduling.getTotalCPUCount());
        assertEquals(13, retrievedComputationalResourceScheduling.getNodeCount());
        assertEquals(14, retrievedComputationalResourceScheduling.getNumberOfThreads());
        assertEquals(
                "override-project-num", retrievedComputationalResourceScheduling.getOverrideAllocationProjectNumber());
        assertEquals("override-login-username", retrievedComputationalResourceScheduling.getOverrideLoginUserName());
        assertEquals(
                "override-scratch-location", retrievedComputationalResourceScheduling.getOverrideScratchLocation());
        assertEquals("queue-name", retrievedComputationalResourceScheduling.getQueueName());
        assertEquals("static-working-dir", retrievedComputationalResourceScheduling.getStaticWorkingDir());
        assertEquals(1333, retrievedComputationalResourceScheduling.getTotalPhysicalMemory());
        assertEquals(77, retrievedComputationalResourceScheduling.getWallTimeLimit());

        experimentService.removeExperiment(experimentId);
        assertFalse(experimentService.isExperimentExist(experimentId));
    }

    @Test
    public void testExperimentInputs() throws RegistryException {

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        experimentModel.setGatewayInstanceId("gateway-instance-id");

        InputDataObjectType input1 = new InputDataObjectType();
        input1.setName("name1");
        input1.setIsRequired(true);
        input1.setType(DataType.STRING);
        input1.setInputOrder(0);
        input1.setApplicationArgument("-arg1");
        input1.setDataStaged(true);
        input1.setIsReadOnly(true);
        input1.setMetaData("{\"foo\": 123}");
        input1.setRequiredToAddedToCommandLine(true);
        input1.setStandardInput(true);
        input1.setStorageResourceId("storageResourceId");
        input1.setUserFriendlyDescription("First argument");
        input1.setValue("value1");
        input1.setOverrideFilename("gaussian.com");

        if (experimentModel.getExperimentInputs() == null) {
            experimentModel.setExperimentInputs(new java.util.ArrayList<>());
        }
        experimentModel.getExperimentInputs().add(input1);

        String experimentId = experimentService.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        ExperimentModel retrievedExperimentModel = experimentService.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputs().size());
        InputDataObjectType retrievedInput1 =
                retrievedExperimentModel.getExperimentInputs().get(0);
        assertEquals("name1", retrievedInput1.getName());
        assertTrue(retrievedInput1.getIsRequired());
        assertEquals(DataType.STRING, retrievedInput1.getType());
        assertEquals(0, retrievedInput1.getInputOrder());
        assertEquals("-arg1", retrievedInput1.getApplicationArgument());
        assertTrue(retrievedInput1.getDataStaged());
        assertTrue(retrievedInput1.getIsReadOnly());
        assertEquals("{\"foo\": 123}", retrievedInput1.getMetaData());
        assertTrue(retrievedInput1.getRequiredToAddedToCommandLine());
        assertTrue(retrievedInput1.getStandardInput());
        assertEquals("storageResourceId", retrievedInput1.getStorageResourceId());
        assertEquals("First argument", retrievedInput1.getUserFriendlyDescription());
        assertEquals("value1", retrievedInput1.getValue());
        assertEquals("gaussian.com", retrievedInput1.getOverrideFilename());

        retrievedInput1.setIsRequired(false);
        retrievedInput1.setType(DataType.URI);
        retrievedInput1.setInputOrder(1);
        retrievedInput1.setApplicationArgument("-arg1a");
        retrievedInput1.setDataStaged(false);
        retrievedInput1.setIsReadOnly(false);
        retrievedInput1.setMetaData("{\"bar\": 456}");
        retrievedInput1.setRequiredToAddedToCommandLine(false);
        retrievedInput1.setStandardInput(false);
        retrievedInput1.setStorageResourceId("storageResourceId2");
        retrievedInput1.setUserFriendlyDescription("First argument~");
        retrievedInput1.setValue("value1a");
        retrievedInput1.setOverrideFilename("gaussian.com-updated");

        experimentService.updateExperiment(retrievedExperimentModel, experimentId);

        retrievedExperimentModel = experimentService.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputs().size());
        retrievedInput1 = retrievedExperimentModel.getExperimentInputs().get(0);
        assertFalse(retrievedInput1.getIsRequired());
        assertEquals(DataType.URI, retrievedInput1.getType());
        assertEquals(1, retrievedInput1.getInputOrder());
        assertEquals("-arg1a", retrievedInput1.getApplicationArgument());
        assertFalse(retrievedInput1.getDataStaged());
        assertFalse(retrievedInput1.getIsReadOnly());
        assertEquals("{\"bar\": 456}", retrievedInput1.getMetaData());
        assertFalse(retrievedInput1.getRequiredToAddedToCommandLine());
        assertFalse(retrievedInput1.getStandardInput());
        assertEquals("storageResourceId2", retrievedInput1.getStorageResourceId());
        assertEquals("First argument~", retrievedInput1.getUserFriendlyDescription());
        assertEquals("value1a", retrievedInput1.getValue());
        assertEquals("gaussian.com-updated", retrievedInput1.getOverrideFilename());

        experimentService.removeExperiment(experimentId);
        assertFalse(experimentService.isExperimentExist(experimentId));
    }

    /**
     * Verify that slashes (forward and backward) are replaced with underscores.
     */
    @Test
    public void testSlashesInExperimentName() throws RegistryException {

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name/forward-slash//a");

        String experimentId = experimentService.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_forward-slash__a"));

        experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name\\backward-slash\\\\a");

        experimentId = experimentService.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_backward-slash__a"));
    }
}
