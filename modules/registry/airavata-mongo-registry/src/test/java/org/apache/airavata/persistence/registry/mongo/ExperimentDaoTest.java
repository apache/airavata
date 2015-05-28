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
package org.apache.airavata.persistence.registry.mongo;

import junit.framework.Assert;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.mongo.dao.ExperimentDao;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ExperimentDaoTest extends AbstractDaoTest{
    private final static Logger logger = LoggerFactory.getLogger(ExperimentDaoTest.class);

    private static ExperimentDao experimentDao;

    @BeforeClass
    public static void setupBeforeClass() throws Exception{
        experimentDao = new ExperimentDao();
    }


    @Test
    public void testExperimentOperations() throws RegistryException {
        Experiment experiment = createExperiment();
        experimentDao.createExperiment(experiment);
        Experiment persistedExperiement
                = experimentDao.getExperiment(experiment.getExperimentId());
        Assert.assertNotNull(persistedExperiement);
        Assert.assertEquals(experiment, persistedExperiement);

        experiment.setName("New Name");
        experimentDao.updateExperiment(experiment);
        persistedExperiement = experimentDao.getExperiment(experiment.getExperimentId());
        Assert.assertEquals(experiment, persistedExperiement);

        List<Experiment> experimentList = experimentDao.getAllExperiments();
        Assert.assertTrue(experimentList.size()==1);

        Map<String, String> filters = new HashMap();
        filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME,
                experiment.getName().substring(1, 4));
        experimentList = experimentDao.searchExperiments(filters, 1, 0, null, null);
        Assert.assertTrue(experimentList.size()==1);

        experimentDao.deleteExperiment(experiment);
        experimentList = experimentDao.getAllExperiments();
        Assert.assertTrue(experimentList.size()==0);
    }


    @Test
    public void testWFOperations() throws RegistryException, IOException {
        Experiment experiment = createExperiment();
        experimentDao.createExperiment(experiment);
        WorkflowNodeDetails wfnd = createWorkFlowNodeDetails();
        experimentDao.createWFNode(experiment.getExperimentId(), wfnd);
        Assert.assertEquals(wfnd, experimentDao.getWFNode(wfnd.getNodeInstanceId()));

        wfnd.setNodeName("NewName");
        experimentDao.updateWFNode(wfnd);
        Assert.assertEquals(wfnd.getNodeName(),
                experimentDao.getWFNode(wfnd.getNodeInstanceId()).getNodeName());

        experimentDao.deleteWFNode(wfnd);
        Assert.assertNull(experimentDao.getWFNode(wfnd.getNodeInstanceId()));
    }

    @Test
    public void testTaskOperations() throws RegistryException, IOException {
    }

    //Todo set all the fields in the experiment object
    private Experiment createExperiment(){
        String TAG = System.currentTimeMillis() + "";
        //creating sample echo experiment
        InputDataObjectType inputDataObjectType = new InputDataObjectType();
        inputDataObjectType.setName("Input_to_Echo");
        inputDataObjectType.setValue("Hello World");

        ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
        scheduling.setResourceHostId(UUID.randomUUID().toString());
        scheduling.setComputationalProjectAccount("TG-STA110014S");
        scheduling.setTotalCpuCount(1);
        scheduling.setNodeCount(1);
        scheduling.setWallTimeLimit(15);
        scheduling.setQueueName("normal");

        UserConfigurationData userConfigurationData = new UserConfigurationData();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);

        Experiment experiment = new Experiment();
        experiment.setExperimentId("28395669237854235"+TAG);
        experiment.setProjectId("2392519y92312341" + TAG);
        experiment.setUserName("TestUser" + TAG);
        experiment.setName("TestExperiment" + TAG);
        experiment.setDescription("experiment");
        experiment.setApplicationId("2358382458362846287" + TAG);
        experiment.setUserConfigurationData(userConfigurationData);
        experiment.addToExperimentInputs(inputDataObjectType);
        experiment.setGatewayExecutionId("default");
        experiment.setEnableEmailNotification(true);
        ArrayList<String> emailList = new ArrayList();
        emailList.add("qwerty123@gmail.com");
        experiment.setEmailAddresses(emailList);
        ExperimentStatus experimentStatus = new ExperimentStatus();
        experimentStatus.setExperimentState(ExperimentState.CREATED);
        experiment.setExperimentStatus(experimentStatus);

        experiment.addToWorkflowNodeDetailsList(createWorkFlowNodeDetails());
        return experiment;
    }

    private WorkflowNodeDetails createWorkFlowNodeDetails(){
        String TAG = System.currentTimeMillis() + "";
        WorkflowNodeDetails wfnd = new WorkflowNodeDetails();
        wfnd.setNodeInstanceId("tempNode_4e1582bd-f9dd-4563-8808-472470c93dbc"+TAG);
        wfnd.setNodeName("Temp Node" + TAG);
        wfnd.setExecutionUnit(ExecutionUnit.APPLICATION);
        WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
        workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.UNKNOWN);
        wfnd.setWorkflowNodeStatus(workflowNodeStatus);

        TaskDetails taskDetails = new TaskDetails();
        taskDetails.setTaskId("Temp_Task"+TAG);
        taskDetails.setApplicationId("Ultrascan_856df1d5-944a-49d3-a476-d969e57a8f37");

        wfnd.addToTaskDetailsList(taskDetails);
        return wfnd;
    }
}