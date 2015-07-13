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
import org.apache.airavata.api.server.util.AppCatalogInitUtil;
import org.apache.airavata.api.server.util.RegistryInitUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Test methods for Airavata Service Handler
 */
public class AiravataServerHandlerTest {
    private final static Logger logger = LoggerFactory.getLogger(AiravataServerHandlerTest.class);

    private static AiravataServerHandler airavataServerHandler;
    private static String gatewayId = "php_reference_gateway";
    private static  String computeResouceId = null;

    @BeforeClass
    public static void setupBeforeClass() throws Exception{
        RegistryInitUtil.initializeDB();
        AppCatalogInitUtil.initializeDB();
        airavataServerHandler = new AiravataServerHandler();

        Gateway gateway = new Gateway();
        gateway.setGatewayId(gatewayId);
        airavataServerHandler.addGateway(gateway);

        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setHostName("test.compute.resource");
        computeResourceDescription.setResourceDescription("test compute resource");
        computeResourceDescription.setEnabled(true);
        computeResouceId = airavataServerHandler.registerComputeResource(computeResourceDescription);
    }

    @AfterClass
    public static void tearDown(){
        RegistryInitUtil.stopDerbyInServerMode();
    }

    /**
     * Testing for project related API methods
     */
    @Test
    public void testProject(){
        try {
            String TAG = System.currentTimeMillis() + "";

            //testing the creation of a project
            Project project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("TestProject"+TAG);
            project.setDescription("This is a test project"+TAG);
            String projectId1 = airavataServerHandler.createProject(gatewayId, project);
            Assert.assertNotNull(projectId1);

            //testing the update of a project
            Project updatedProject = new Project();
            updatedProject.setProjectID(projectId1);
            updatedProject.setOwner("TestUser"+TAG);
            updatedProject.setName("UpdatedTestProject"+TAG);
            updatedProject.setDescription("This is an updated test project"+TAG);
            airavataServerHandler.updateProject(projectId1, updatedProject);

            //testing project retrieval
            Project retrievedProject = airavataServerHandler.getProject(projectId1);
            Assert.assertEquals(updatedProject.getProjectID(), retrievedProject.getProjectID());
            Assert.assertEquals(updatedProject.getOwner(), retrievedProject.getOwner());
            Assert.assertEquals(updatedProject.getName(), retrievedProject.getName());
            Assert.assertEquals(updatedProject.getDescription(), retrievedProject.getDescription());
            Assert.assertNotNull(retrievedProject.getCreationTime());
            //created user should be in the shared users list
            Assert.assertTrue(retrievedProject.getSharedUsers().size()==1);

            //creating more projects for the same user
            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Terrible"+TAG);
            project.setDescription("This is a test project_2"+TAG);
            String projectId2 = airavataServerHandler.createProject(gatewayId, project);
            Assert.assertNotNull(projectId2);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Funny"+TAG);
            project.setDescription("This is a test project_3"+TAG);
            String projectId3 = airavataServerHandler.createProject(gatewayId, project);
            Assert.assertNotNull(projectId3);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Stupid"+TAG);
            project.setDescription("This is a test project_4"+TAG);
            String projectId4 = airavataServerHandler.createProject(gatewayId, project);
            Assert.assertNotNull(projectId4);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Boring"+TAG);
            project.setDescription("This is a test project_5"+TAG);
            String projectId5 = airavataServerHandler.createProject(gatewayId, project);
            Assert.assertNotNull(projectId5);

            //search project by project name
            List<Project> list = airavataServerHandler.searchProjectsByProjectName(gatewayId,
                    "TestUser"+TAG, "Terrible"+TAG);
            Assert.assertTrue(list.size()==1);
            //with pagination
            list = airavataServerHandler.searchProjectsByProjectNameWithPagination(gatewayId,
                    "TestUser" + TAG, "Project", 2, 1);
            Assert.assertTrue(list.size()==2);

            //search project by project description
            list = airavataServerHandler.searchProjectsByProjectDesc(gatewayId, "TestUser"+TAG,
                    "test project_2"+TAG);
            Assert.assertTrue(list.size()==1);
            //with pagination
            list = airavataServerHandler.searchProjectsByProjectDescWithPagination(gatewayId,
                    "TestUser" + TAG, "test", 2, 1);
            Assert.assertTrue(list.size()==2);

            //get all projects of user
            list = airavataServerHandler.getAllUserProjects(gatewayId, "TestUser"+TAG);
            Assert.assertTrue(list.size()==5);
            //with pagination
            list = airavataServerHandler.getAllUserProjectsWithPagination(gatewayId, "TestUser" + TAG, 2, 2);
            Assert.assertTrue(list.size()==2);
            Project project1 = list.get(0);
            Project project2 = list.get(1);
            Assert.assertTrue(project1.getCreationTime()-project2.getCreationTime() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Testing for project related API methods
     */
    @Test
    public void testExperiment(){
        try {
            String TAG = System.currentTimeMillis() + "";

            String applicationId = "Echo_" + UUID.randomUUID().toString();

            //creating project
            Project project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("TestProject"+TAG);
            project.setDescription("This is a test project"+TAG);
            String projectId1 = airavataServerHandler.createProject(gatewayId,project);
            Assert.assertNotNull(projectId1);

            //creating sample echo experiment. assumes echo application is already defined
            InputDataObjectType inputDataObjectType = new InputDataObjectType();
            inputDataObjectType.setName("Input_to_Echo");
            inputDataObjectType.setValue("Hello World");

            ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
            scheduling.setResourceHostId(computeResouceId);
            scheduling.setComputationalProjectAccount("TG-STA110014S");
            scheduling.setTotalCPUCount(1);
            scheduling.setNodeCount(1);
            scheduling.setWallTimeLimit(15);
            scheduling.setQueueName("normal");

            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);

            Experiment experiment = new Experiment();
            experiment.setProjectID(projectId1);
            experiment.setUserName("TestUser" + TAG);
            experiment.setName("TestExperiment"+TAG);
            experiment.setDescription("experiment");
            experiment.setApplicationId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId1 = airavataServerHandler.createExperiment(gatewayId, experiment);
            Assert.assertNotNull(experimentId1);

            //retrieving the stored experiment
            Experiment retrievedExperiment = airavataServerHandler.getExperiment(experimentId1);
            Assert.assertNotNull(retrievedExperiment);
            Assert.assertEquals(retrievedExperiment.getProjectID(), experiment.getProjectID());
            Assert.assertEquals(retrievedExperiment.getDescription(), experiment.getDescription());
            Assert.assertEquals(retrievedExperiment.getName(), experiment.getName());
            Assert.assertEquals(retrievedExperiment.getApplicationId(), experiment.getApplicationId());
            Assert.assertNotNull(retrievedExperiment.getUserConfigurationData());
            Assert.assertNotNull(retrievedExperiment.getExperimentInputs());

            //updating an existing experiment
            experiment.setName("NewExperimentName"+TAG);
            OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
            outputDataObjectType.setName("Output_to_Echo");
            outputDataObjectType.setValue("Hello World");
            experiment.addToExperimentOutputs(outputDataObjectType);
            airavataServerHandler.updateExperiment(experimentId1, experiment);

            //creating more experiments
            experiment = new Experiment();
            experiment.setProjectID(projectId1);
            experiment.setUserName("TestUser" + TAG);
            experiment.setName("TestExperiment2" + TAG);
            experiment.setDescription("experiment");
            experiment.setApplicationId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId2 = airavataServerHandler.createExperiment(gatewayId, experiment);
            Assert.assertNotNull(experimentId2);

            experiment = new Experiment();
            experiment.setProjectID(projectId1);
            experiment.setUserName("TestUser" + TAG);
            experiment.setName("TestExperiment3"+TAG);
            experiment.setDescription("experiment");
            experiment.setApplicationId(applicationId);
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId3 = airavataServerHandler.createExperiment(gatewayId, experiment);
            Assert.assertNotNull(experimentId3);

            //searching experiments by name
            List<ExperimentSummary> results = airavataServerHandler.searchExperimentsByName(gatewayId,
                    "TestUser" + TAG, "Experiment2");
            Assert.assertTrue(results.size()==1);
            //with pagination
            results = airavataServerHandler.searchExperimentsByNameWithPagination(gatewayId,
                    "TestUser" + TAG, "Experi", 2, 0);
            Assert.assertTrue(results.size()==2);

            //searching experiments by creation time
            long time = System.currentTimeMillis();
            results = airavataServerHandler.searchExperimentsByCreationTime(
                    gatewayId, "TestUser" + TAG, time-10000, time+1000);
            Assert.assertTrue(results.size()==3);
            //with pagination
            results = airavataServerHandler.searchExperimentsByCreationTimeWithPagination(
                    gatewayId, "TestUser" + TAG, time-10000, time+1000, 2, 1);
            Assert.assertTrue(results.size()==2);

            //searching based on experiment state
            ExperimentState experimentState = ExperimentState.findByValue(0);
            results = airavataServerHandler.searchExperimentsByStatus(
                    gatewayId, "TestUser" + TAG, experimentState);
            Assert.assertTrue(results.size() == 3);
            //with pagination
            results = airavataServerHandler.searchExperimentsByStatusWithPagination(
                    gatewayId, "TestUser" + TAG, experimentState, 2, 0);
            Assert.assertTrue(results.size()==2);

            //searching based on application
            results = airavataServerHandler.searchExperimentsByApplication(
                    gatewayId, "TestUser" + TAG, "Ech");
            Assert.assertTrue(results.size() == 3);
            //with pagination
            results = airavataServerHandler.searchExperimentsByApplicationWithPagination(
                    gatewayId, "TestUser" + TAG, "Ech", 2, 0);
            Assert.assertTrue(results.size()==2);

            //searching experiments by description
            results = airavataServerHandler.searchExperimentsByDesc(
                    gatewayId, "TestUser" + TAG, "exp");
            Assert.assertTrue(results.size() == 3);
            //with pagination
            results = airavataServerHandler.searchExperimentsByDescWithPagination(
                    gatewayId, "TestUser" + TAG, "exp", 2, 0);
            Assert.assertTrue(results.size()==2);


            //retrieving all experiments in project
            List<Experiment> list = airavataServerHandler.getAllExperimentsInProject(projectId1);
            Assert.assertTrue(list.size()==3);
            //with pagination
            list = airavataServerHandler.getAllExperimentsInProjectWithPagination(projectId1, 2, 1);
            Assert.assertTrue(list.size()==2);

            //getting all user experiments
            list = airavataServerHandler.getAllUserExperiments(gatewayId, "TestUser" + TAG);
            Assert.assertTrue(list.size() == 3);
            //with pagination
            list = airavataServerHandler.getAllUserExperimentsWithPagination(
                    gatewayId, "TestUser" + TAG, 2, 0);
            //testing time ordering
            Assert.assertTrue(list.size()==2);
            Experiment exp1 = list.get(0);
            Experiment exp2 = list.get(1);
            Assert.assertTrue(exp1.getCreationTime()-exp2.getCreationTime() > 0);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}