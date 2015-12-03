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
package org.apache.airavata.data.manager;

import org.apache.airavata.data.manager.utils.AppCatInit;
import org.apache.airavata.data.manager.utils.DataCatInit;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManagerImplTest {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImplTest.class);
    private static AppCatInit appCatInit;
    private static DataCatInit dataCatInit;
    private static DataManager dataManager;
    private static DataResourceModel dataResourceModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            appCatInit = new AppCatInit("appcatalog-derby.sql");
            appCatInit.initializeDB();
            dataCatInit = new DataCatInit("datacatalog-derby.sql");
            dataCatInit.initializeDB();
            dataManager = DataManagerFactory.getDataManager();
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            List<DataReplicaLocationModel> replicaLocationModelList = new ArrayList<>();
            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("1-st-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g75.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataReplicaLocationModel.setDataLocations(dataLocations);
            replicaLocationModelList.add(dataReplicaLocationModel);
            dataResourceModel.setReplicaLocations(replicaLocationModelList);
        } catch (DataManagerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        dataCatInit.stopDerbyServer();
    }

    @Test
    public void testPublishDataResource(){
        try {
            String resourceId = dataManager.publishDataResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = dataManager.removeDataResource("234234234");
            Assert.assertFalse(result);
            String resourceId = dataManager.publishDataResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = dataManager.removeDataResource(resourceId);
            Assert.assertTrue(result);
            result = dataManager.removeDataResource(resourceId);
            Assert.assertFalse(result);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            String resourceId = dataManager.publishDataResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = dataManager.getDataResource(resourceId);
            Assert.assertNotNull(persistedCopy);
            Assert.assertTrue(persistedCopy.getReplicaLocations().size()==1);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = dataManager.updateDataResource(dataResourceModel);
            Assert.assertFalse(result);
            dataManager.publishDataResource(dataResourceModel);
            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("2-nd-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g175.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataResourceModel.getReplicaLocations().add(dataReplicaLocationModel);
            dataManager.updateDataResource(dataResourceModel);
            dataResourceModel = dataManager.getDataResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getReplicaLocations().size()==2);
            dataResourceModel.getReplicaLocations().remove(1);
            dataManager.updateDataResource(dataResourceModel);
            dataResourceModel = dataManager.getDataResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getReplicaLocations().size()==1);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}