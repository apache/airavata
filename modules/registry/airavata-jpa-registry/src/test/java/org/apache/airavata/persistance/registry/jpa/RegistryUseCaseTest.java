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
package org.apache.airavata.persistance.registry.jpa;

import junit.framework.Assert;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.persistance.registry.jpa.util.Initialize;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains test cases for the RegistryImpl class which is the default registry
 * implementation. These test cases are written from the perspective of the Airavata API
 * such as creating/updating/deleting/searching projects and experiments etc.
 */
public class RegistryUseCaseTest {

    private static Registry registry;
    private static Initialize initialize;

    @BeforeClass
    public static void setupBeforeClass() throws RegistryException, SQLException {
        initialize = new Initialize("registry-derby.sql");
        initialize.initializeDB();
        registry = RegistryFactory.getDefaultRegistry();
    }

    @Test
    public void testProject(){
        try {
            String TAG = System.currentTimeMillis() + "";

            String gatewayId = ServerSettings.getDefaultUserGateway();

            //testing the creation of a project
            Project project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("TestProject"+TAG);
            project.setDescription("This is a test project"+TAG);
            String projectId1 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId1);

            //testing the update of a project
            Project updatedProject = new Project();
            updatedProject.setProjectID(projectId1);
            updatedProject.setOwner("TestUser"+TAG);
            updatedProject.setName("UpdatedTestProject"+TAG);
            updatedProject.setDescription("This is an updated test project"+TAG);
            registry.update(RegistryModelType.PROJECT, updatedProject, projectId1);

            //testing project retrieval
            Project retrievedProject = (Project)registry.get(RegistryModelType.PROJECT, projectId1);
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
            String projectId2 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId2);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Funny"+TAG);
            project.setDescription("This is a test project_3"+TAG);
            String projectId3 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId3);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Stupid"+TAG);
            project.setDescription("This is a test project_4"+TAG);
            String projectId4 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId4);

            project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("Project Boring"+TAG);
            project.setDescription("This is a test project_5"+TAG);
            String projectId5 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId5);

            //test get all projects created by the user
            List<Object> list = registry.get(RegistryModelType.PROJECT,
                    Constants.FieldConstants.ProjectConstants.OWNER, "TestUser"+TAG);
            Assert.assertTrue(list.size()==5);

            //search project by project name
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, "TestUser"+TAG);
            filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, "Terrible"+TAG);
            list = registry.search(RegistryModelType.PROJECT, filters);
            Assert.assertTrue(list.size()==1);

            //search project by project description
            filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, "TestUser"+TAG);
            filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, "test project_2"+TAG);
            list = registry.search(RegistryModelType.PROJECT, filters);
            Assert.assertTrue(list.size()==1);

            //search project with only ownername
            filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, "TestUser"+TAG);
            list = registry.search(RegistryModelType.PROJECT, filters);
            Assert.assertTrue(list.size()==5);

            //search projects with pagination
            filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, "TestUser"+TAG);
            list = registry.search(RegistryModelType.PROJECT, filters, 2, 2,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            Assert.assertTrue(list.size()==2);
            Project project1 = (Project)list.get(0);
            Project project2 = (Project)list.get(1);
            Assert.assertTrue(project1.getCreationTime()-project2.getCreationTime() > 0);
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExperiment(){
        try {
            long time  = System.currentTimeMillis();
            String TAG = time + "";

            String gatewayId = ServerSettings.getDefaultUserGateway();

            //creating project
            Project project = new Project();
            project.setOwner("TestUser"+TAG);
            project.setName("TestProject"+TAG);
            project.setDescription("This is a test project"+TAG);
            String projectId1 = (String)registry.add(ParentDataType.PROJECT, project, gatewayId);
            Assert.assertNotNull(projectId1);

            //creating sample echo experiment. assumes echo application is already defined
            InputDataObjectType inputDataObjectType = new InputDataObjectType();
            inputDataObjectType.setName("Input_to_Echo");
            inputDataObjectType.setValue("Hello World");

            ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
            scheduling.setResourceHostId(UUID.randomUUID().toString());
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
            experiment.setDescription("Test 1 experiment");
            experiment.setApplicationId(UUID.randomUUID().toString());
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId1 = (String)registry.add(ParentDataType.EXPERIMENT, experiment, gatewayId);
            Assert.assertNotNull(experimentId1);

            //retrieving the stored experiment
            Experiment retrievedExperiment = (Experiment)registry.get(RegistryModelType.EXPERIMENT,
                    experimentId1);
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
            registry.update(RegistryModelType.EXPERIMENT, experiment, experimentId1);

            //creating more experiments
            experiment = new Experiment();
            experiment.setProjectID(projectId1);
            experiment.setUserName("TestUser" + TAG);
            experiment.setName("TestExperiment2" + TAG);
            experiment.setDescription("Test 2 experiment");
            experiment.setApplicationId(UUID.randomUUID().toString());
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId2 = (String)registry.add(ParentDataType.EXPERIMENT, experiment, gatewayId);
            Assert.assertNotNull(experimentId2);

            experiment = new Experiment();
            experiment.setProjectID(projectId1);
            experiment.setUserName("TestUser" + TAG);
            experiment.setName("TestExperiment3"+TAG);
            experiment.setDescription("Test 3 experiment");
            experiment.setApplicationId(UUID.randomUUID().toString());
            experiment.setUserConfigurationData(userConfigurationData);
            experiment.addToExperimentInputs(inputDataObjectType);

            String experimentId3 = (String)registry.add(ParentDataType.EXPERIMENT, experiment, gatewayId);
            Assert.assertNotNull(experimentId3);

            //searching experiments by
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, "TestUser" + TAG);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, "Experiment2");
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, ExperimentState.CREATED.toString());
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, time - 999999999 + "");
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, time + 999999999 + "");
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            Assert.assertTrue(results.size()==1);

            //retrieving all experiments in project
            List<Object> list = registry.get(RegistryModelType.EXPERIMENT,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID, projectId1);
            Assert.assertTrue(list.size()==3);

            //searching all user experiments
            filters = new HashMap();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, "TestUser" + TAG);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY, gatewayId);
            list = registry.search(RegistryModelType.EXPERIMENT, filters);
            Assert.assertTrue(list.size()==3);

            //searching user experiemets with pagination
            filters = new HashMap();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, "TestUser" + TAG);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY, gatewayId);
            list = registry.search(RegistryModelType.EXPERIMENT, filters, 2, 1,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            Assert.assertTrue(list.size()==2);
            ExperimentSummary exp1 = (ExperimentSummary)list.get(0);
            ExperimentSummary exp2 = (ExperimentSummary)list.get(1);
            Assert.assertTrue(exp1.getCreationTime()-exp2.getCreationTime() > 0);

        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }

}