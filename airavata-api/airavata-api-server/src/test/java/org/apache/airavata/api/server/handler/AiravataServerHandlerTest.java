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
package org.apache.airavata.api.server.handler;

import junit.framework.Assert;
import org.apache.airavata.api.server.handler.utils.AppCatInit;
import org.apache.airavata.api.server.handler.utils.ExpCatInit;
import org.apache.airavata.api.server.util.ExperimentCatalogInitUtil;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.NotificationPriority;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * Test methods for Airavata Service Handler
 */
public class AiravataServerHandlerTest {
//    private final static Logger logger = LoggerFactory.getLogger(AiravataServerHandlerTest.class);

    private static AiravataServerHandler airavataServerHandler;
    private static String gatewayId = "php_reference_gateway";
    private static String computeResouceId = null;
    private AuthzToken token = new AuthzToken("empty_token");

    @BeforeClass
    public static void setupBeforeClass() throws Exception {
        AppCatInit appCatInit = new AppCatInit("appcatalog-derby.sql");
        appCatInit.initializeDB();
        ExpCatInit expCatInit = new ExpCatInit("expcatalog-derby.sql");
        expCatInit.initializeDB();
        airavataServerHandler = new AiravataServerHandler();

        Gateway gateway = new Gateway();
        gateway.setGatewayId(gatewayId);
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.REQUESTED);
        airavataServerHandler.addGateway(new AuthzToken(""), gateway);

        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setHostName("test.compute.resource");
        computeResourceDescription.setResourceDescription("test compute resource");
        computeResourceDescription.setEnabled(true);
        computeResouceId = airavataServerHandler.registerComputeResource(new AuthzToken(""),
                computeResourceDescription);
    }

    @AfterClass
    public static void tearDown() {
        ExperimentCatalogInitUtil.stopDerbyInServerMode();
    }

    /**
     * Testing for project related API methods
     */
    @Test
    public void testProject() {
        try {
            String TAG = System.currentTimeMillis() + "";


            //testing the creation of a project
            Project project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("TestProject" + TAG);
            project.setDescription("This is a test project" + TAG);
            String projectId1 = airavataServerHandler.createProject(new AuthzToken(""), gatewayId, project);
            Assert.assertNotNull(projectId1);

            //testing the update of a project
            Project updatedProject = new Project();
            updatedProject.setProjectID(projectId1);
            updatedProject.setOwner("TestUser" + TAG);
            updatedProject.setName("UpdatedTestProject" + TAG);
            updatedProject.setDescription("This is an updated test project" + TAG);
            airavataServerHandler.updateProject(new AuthzToken(""), projectId1, updatedProject);

            //testing project retrieval
            Project retrievedProject = airavataServerHandler.getProject(new AuthzToken(""), projectId1);
            Assert.assertEquals(updatedProject.getProjectID(), retrievedProject.getProjectID());
            Assert.assertEquals(updatedProject.getOwner(), retrievedProject.getOwner());
            Assert.assertEquals(updatedProject.getName(), retrievedProject.getName());
            Assert.assertEquals(updatedProject.getDescription(), retrievedProject.getDescription());
            Assert.assertNotNull(retrievedProject.getCreationTime());
            //created user should be in the shared users list
            Assert.assertTrue(retrievedProject.getSharedUsers().size() == 1);

            //creating more projects for the same user
            project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("Project Terrible" + TAG);
            project.setDescription("This is a test project_2" + TAG);
            String projectId2 = airavataServerHandler.createProject(new AuthzToken(""), gatewayId, project);
            Assert.assertNotNull(projectId2);

            project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("Project Funny" + TAG);
            project.setDescription("This is a test project_3" + TAG);
            String projectId3 = airavataServerHandler.createProject(new AuthzToken(""), gatewayId, project);
            Assert.assertNotNull(projectId3);

            project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("Project Stupid" + TAG);
            project.setDescription("This is a test project_4" + TAG);
            String projectId4 = airavataServerHandler.createProject(new AuthzToken(""), gatewayId, project);
            Assert.assertNotNull(projectId4);

            project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("Project Boring" + TAG);
            project.setDescription("This is a test project_5" + TAG);
            String projectId5 = airavataServerHandler.createProject(token, gatewayId, project);
            Assert.assertNotNull(projectId5);


            //get all projects of user
            List<Project> list = airavataServerHandler.getUserProjects(token, gatewayId, "TestUser" + TAG, 2, 2);
            Assert.assertTrue(list.size() == 2);
            Project project1 = list.get(0);
            Project project2 = list.get(1);
            Assert.assertTrue(project1.getCreationTime() - project2.getCreationTime() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Testing for project related API methods
     */
    @Test
    public void testExperiment() {
        try {
            String TAG = System.currentTimeMillis() + "";

            String applicationId = "Echo_" + UUID.randomUUID().toString();

            //creating project
            Project project = new Project();
            project.setOwner("TestUser" + TAG);
            project.setName("TestProject" + TAG);
            project.setDescription("This is a test project" + TAG);
            String projectId1 = airavataServerHandler.createProject(new AuthzToken(""), gatewayId, project);
            Assert.assertNotNull(projectId1);

            //creating sample echo experiment. assumes echo application is already defined
            InputDataObjectType inputDataObjectType = new InputDataObjectType();
            inputDataObjectType.setName("Input_to_Echo");
            inputDataObjectType.setValue("Hello World");
            inputDataObjectType.setType(DataType.STRING);

            ComputationalResourceSchedulingModel scheduling = new ComputationalResourceSchedulingModel();
            scheduling.setResourceHostId(computeResouceId);
            scheduling.setTotalCPUCount(1);
            scheduling.setNodeCount(1);
            scheduling.setWallTimeLimit(15);
            scheduling.setQueueName("normal");

            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);

            ExperimentModel experiment = new ExperimentModel();
            experiment.setProjectId(projectId1);
            experiment.setGatewayId(gatewayId);
            experiment.setUserName("TestUser" + TAG);
            experiment.setExperimentName("TestExperiment" + TAG);
            experiment.setDescription("experiment");
            experiment.setExecutionId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId1 = airavataServerHandler.createExperiment(new AuthzToken(""), gatewayId, experiment);
            Assert.assertNotNull(experimentId1);

            //retrieving the stored experiment
            ExperimentModel retrievedExperiment = airavataServerHandler.getExperiment(new AuthzToken(""), experimentId1);
            Assert.assertNotNull(retrievedExperiment);
            Assert.assertEquals(retrievedExperiment.getProjectId(), experiment.getProjectId());
            Assert.assertEquals(retrievedExperiment.getDescription(), experiment.getDescription());
            Assert.assertEquals(retrievedExperiment.getExperimentName(), experiment.getExperimentName());
            Assert.assertEquals(retrievedExperiment.getExecutionId(), experiment.getExecutionId());
            Assert.assertNotNull(retrievedExperiment.getUserConfigurationData());
            Assert.assertNotNull(retrievedExperiment.getExperimentInputs());

            //updating an existing experiment
            experiment.setExperimentName("NewExperimentName" + TAG);
            OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
            outputDataObjectType.setName("Output_to_Echo");
            outputDataObjectType.setValue("Hello World");
            outputDataObjectType.setType(DataType.STRING);
            experiment.addToExperimentOutputs(outputDataObjectType);
            airavataServerHandler.updateExperiment(new AuthzToken(""), experimentId1, experiment);

            //creating more experiments
            experiment = new ExperimentModel();
            experiment.setProjectId(projectId1);
            experiment.setGatewayId(gatewayId);
            experiment.setUserName("TestUser" + TAG);
            experiment.setExperimentName("TestExperiment2" + TAG);
            experiment.setDescription("experiment");
            experiment.setExecutionId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId2 = airavataServerHandler.createExperiment(new AuthzToken(""), gatewayId, experiment);
            Assert.assertNotNull(experimentId2);

            experiment = new ExperimentModel();
            experiment.setProjectId(projectId1);
            experiment.setGatewayId(gatewayId);
            experiment.setUserName("TestUser" + TAG);
            experiment.setExperimentName("TestExperiment3" + TAG);
            experiment.setDescription("experiment");
            experiment.setExecutionId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId3 = airavataServerHandler.createExperiment(token, gatewayId, experiment);
            Assert.assertNotNull(experimentId3);

            //retrieving all experiments in project
            List<ExperimentModel> list = airavataServerHandler.getExperimentsInProject(token, projectId1, 2, 1);
            Assert.assertTrue(list.size() == 2);

            //getting all user experiments
            list = airavataServerHandler.getUserExperiments(token,
                    gatewayId, "TestUser" + TAG, 2, 0);
            //testing time ordering
            Assert.assertTrue(list.size() == 2);
            ExperimentModel exp1 = list.get(0);
            ExperimentModel exp2 = list.get(1);
            Assert.assertTrue(exp1.getCreationTime() - exp2.getCreationTime() > 0);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
    @Test
    public void testNotifications(){
        try {
            AuthzToken authzToken = new AuthzToken();
            authzToken.setAccessToken("");
            Notification notification = new Notification();
            notification.setTitle("3424234");
            notification.setGatewayId("test");
            notification.setNotificationMessage("sdkjfbjks kjbsdf kjsdbfkjsdbf");
            notification.setPriority(NotificationPriority.NORMAL);
            String notificationId = airavataServerHandler.createNotification(authzToken, notification);
            Assert.assertNotNull(notificationId);
            List<Notification> notifications = airavataServerHandler.getAllNotifications(authzToken, "test");
            Assert.assertTrue(notifications.size() > 0);
            Assert.assertNotNull(airavataServerHandler.getNotification(authzToken,"test",notificationId));
        } catch (TException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}