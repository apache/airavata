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

import com.mongodb.MongoClient;
import junit.framework.Assert;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.persistance.registry.jpa.mongo.dao.ExperimentDao;
import org.apache.airavata.persistance.registry.jpa.mongo.utils.MongoUtil;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExperimentDaoTest {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentDaoTest.class);

    private static String gatewayId = "php_reference_gateway";

    private static ExperimentDao experimentDao;
    @BeforeClass
    public static void setupBeforeClass() throws Exception{
        experimentDao = new ExperimentDao();
    }

    @AfterClass
    public static void tearDown(){
        MongoClient mongoClient = MongoUtil.getMongoClient();
        mongoClient.dropDatabase("airavata-data");
    }

    @Test
    public void testExperimentDao() throws RegistryException {
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
        experiment.setName("TestExperiment"+TAG);
        experiment.setDescription("experiment");
        experiment.setApplicationId("2358382458362846287"+TAG);
        experiment.setUserConfigurationData(userConfigurationData);
        experiment.addToExperimentInputs(inputDataObjectType);
        experiment.setGatewayExecutionId("329619820461624214"+TAG);

        experimentDao.createExperiment(experiment);
        Experiment persistedExperiement = experimentDao.getExperiment(experiment.getExperimentId());
        Assert.assertNotNull(persistedExperiement);
        Assert.assertEquals(experiment, persistedExperiement);

        experiment.setName("New Name"+TAG);
        experimentDao.updateExperiment(experiment);
        persistedExperiement = experimentDao.getExperiment(experiment.getExperimentId());
        Assert.assertEquals(experiment, persistedExperiement);

        List<Experiment> experimentList = experimentDao.getAllExperiments();
        Assert.assertTrue(experimentList.size()==1);

        experimentDao.deleteExperiment(experiment);
        experimentList = experimentDao.getAllExperiments();
        Assert.assertTrue(experimentList.size()==0);
    }

    @Test
    public void test() throws RegistryException, IOException {
        Registry registry = RegistryFactory.getDefaultRegistry();
        MongoUtil.dropAiravataRegistry();

        ExperimentDao experimentDao = new ExperimentDao();
        BufferedReader reader = new BufferedReader(new FileReader("/home/supun/Downloads/EXPERIMENT.csv"));
        String temp = reader.readLine();
        int i = 1;
        long time1 = System.currentTimeMillis();
        while(temp != null && !temp.isEmpty()){
            try{
                Experiment experiement = (Experiment) registry.get(RegistryModelType.EXPERIMENT, temp.trim());
                experimentDao.createExperiment(experiement);
                Experiment persistedExperiment = experimentDao.getExperiment(temp.trim());
//                List<Experiment> experimentList = experimentDao.getAllExperiments();
                Assert.assertEquals(experiement, persistedExperiment);
                System.out.println(i+" :"+experiement.getExperimentId());
                i++;
            }catch (Exception e){
                System.out.println(temp);
                e.printStackTrace();
            }
            temp = reader.readLine();
        }
        long time2  = System.currentTimeMillis();
        System.out.println(time2-time1);
    }

    @Test
    public void testGetExperimentOfWFNode() throws RegistryException, IOException {
//        String nodeId = "IDontNeedaNode_48c545a1-bedd-46cf-90d4-e4390b129693";
//        ExperimentDao experimentDao = new ExperimentDao();
//        long time1 = System.currentTimeMillis();
//        Experiment experiment = experimentDao.getExperimentOfWFNode(nodeId);
//        long time2 = System.currentTimeMillis();
//        System.out.println(time2-time1);
//        Assert.assertNotNull(experiment);

        ExperimentDao experimentDao = new ExperimentDao();
        BufferedReader reader = new BufferedReader(new FileReader("/home/supun/Downloads/WORKFLOW_NODE_DETAIL.csv"));
        String temp = reader.readLine();
        int i = 1;
        int count  = 0;
        long time1 = System.currentTimeMillis();
        while(temp != null && !temp.isEmpty()){
            try{
                Experiment experiment = experimentDao.getParentExperimentOfWFNode(temp.trim());
                if(experiment != null) {
                    System.out.println(i + " :" + experiment.getExperimentId());
                    count++;
                }else{
                    System.out.println("FAILED: " + temp);
                }
                i++;
            }catch (Exception e){
                System.out.println(temp);
                e.printStackTrace();
            }
            temp = reader.readLine();
        }
        long time2  = System.currentTimeMillis();
        System.out.println(count);
        System.out.println(time2-time1);
    }

    @Test
    public void testGetExperimentOfTask() throws RegistryException, IOException {
//        String taskId = "tempNode_fceda7f7-267c-4197-bf20-a54f4fff395b";
//        ExperimentDao experimentDao = new ExperimentDao();
//        long time1 = System.currentTimeMillis();
//        Experiment experiment = experimentDao.getExperimentOfTask(taskId);
//        long time2 = System.currentTimeMillis();
//        System.out.println(time2-time1);
//        Assert.assertNotNull(experiment);
//        AiravataUtils.setExecutionAsServer();
//        Registry registry = RegistryFactory.getDefaultRegistry();
//        MongoUtil.dropAiravataRegistry();

        ExperimentDao experimentDao = new ExperimentDao();
        BufferedReader reader = new BufferedReader(new FileReader("/home/supun/Downloads/TASK_DETAIL.csv"));
        String temp = reader.readLine();
        int i = 1;
        int count  = 0;
        long time1 = System.currentTimeMillis();
        while(temp != null && !temp.isEmpty()){
            try{
                Experiment experiment = experimentDao.getParentExperimentOfTask(temp.trim());
                if(experiment != null) {
                    //System.out.println(i + " :" + experiment.getExperimentId());
                    count++;
                }else{
                    System.out.println("FAILED: " + temp);
                }
                i++;
            }catch (Exception e){
                System.out.println(temp);
                e.printStackTrace();
            }
            temp = reader.readLine();
        }
        long time2  = System.currentTimeMillis();
        System.out.println(count);
        System.out.println(time2-time1);
    }

    @Test
    public void testWorkFlow() throws RegistryException {
        String nodeId = "tempNode_758b52ba-091b-43a5-a7b7-4c3a239c5d1e";
        String newNodeId = "newNode_758b52ba-091b-43a5-a7b7-4c3a2325d1e";
        String expId = "AlamoTest3_3965f4e2-0213-4434-9c3f-fe898b018666";
        ExperimentDao experimentDao = new ExperimentDao();
        WorkflowNodeDetails wfNode = experimentDao.getWFNode("newNode_758b52ba-091b-43a5-a7b7-4c3a239c5d1e");
        Assert.assertTrue(wfNode.getNodeInstanceId().equals("newNode_758b52ba-091b-43a5-a7b7-4c3a239c5d1e"));

        wfNode.setNodeName("New2 Name"+System.currentTimeMillis());
        experimentDao.updateWFNode(wfNode);
        WorkflowNodeDetails updatedWfNode = experimentDao.getWFNode("newNode_758b52ba-091b-43a5-a7b7-4c3a239c5d1e");
        Assert.assertTrue(updatedWfNode.getNodeName().equals(wfNode.getNodeName()));

        WorkflowNodeDetails newWfNode = wfNode;
        newWfNode.setTaskDetailsList(null);
        newWfNode.setNodeInstanceId(newNodeId);
        experimentDao.createWFNode(expId, newWfNode);

        Experiment experiment = experimentDao.getExperiment(expId);

        experimentDao.deleteWFNode(newWfNode);

        experiment = experimentDao.getExperiment(expId);

        System.out.println();
    }

    @Test
    public void testTask() throws RegistryException {
        String taskId = "tempNode_58e1b2e4-f7d6-4543-9281-43dcb58e2c1a";
        ExperimentDao experimentDao = new ExperimentDao();
        TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
        Assert.assertTrue(taskDetails.getTaskId().equals(taskId));

        taskDetails.setTaskStatus(null);
        experimentDao.updateTaskDetail(taskDetails);
        taskDetails = experimentDao.getTaskDetail(taskId);
        Assert.assertTrue(taskDetails.getTaskId().equals(taskId));

        String expid = "alamotest2_5420547e-877a-4a9c-8752-377c2806906c";
        Experiment experiment = experimentDao.getExperiment(expid);
        System.out.println();
    }

    @Test
    public void testSearch() throws RegistryException{
        Map<String, String> filters = new HashMap();
        filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, "Eroma123");
        filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC, "Test");
        List<Experiment> result = experimentDao.searchExperiments(
                filters, 10, 2, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size()==10);
        Assert.assertTrue(result.get(0).getCreationTime() > result.get(9).getCreationTime());
    }
}