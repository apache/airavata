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
package org.apache.airavata.data.catalog.core;

import org.apache.airavata.data.catalog.cpi.DataManagerException;
import org.apache.airavata.data.catalog.core.utils.AppCatInit;
import org.apache.airavata.data.catalog.core.utils.DataCatInit;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.data.catalog.cpi.DataManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class DataManagerImplTest {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImplTest.class);
    private static AppCatInit appCatInit;
    private static DataCatInit dataCatInit;
    private static DataManager dataManager;
    private static DataResourceModel dataResourceModel;
    private static DataReplicaLocationModel dataReplicaLocationModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            appCatInit = new AppCatInit("appcatalog-derby.sql");
            appCatInit.initializeDB();
            dataCatInit = new DataCatInit("datacatalog-derby.sql");
            dataCatInit.initializeDB();
            DataCatalog dataCatalog = RegistryFactory.getDataCatalog();
            DataManagerImplTest.dataManager = new DataManagerImpl(dataCatalog);
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("1-st-replica");
        } catch (Exception e) {
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
            String resourceId = dataManager.registerResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = dataManager.removeResource("234234234");
            Assert.assertFalse(result);
            String resourceId = dataManager.registerResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = dataManager.removeResource(resourceId);
            Assert.assertTrue(result);
            result = dataManager.removeResource(resourceId);
            Assert.assertFalse(result);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = dataManager.getResource(resourceId);
            Assert.assertNotNull(persistedCopy);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = dataManager.updateResource(dataResourceModel);
            Assert.assertFalse(result);
            dataManager.registerResource(dataResourceModel);
            dataResourceModel.setResourceName("updated-name");
            dataManager.updateResource(dataResourceModel);
            dataResourceModel = dataManager.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getResourceName().equals("updated-name"));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPublishReplicaLocation(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.registerReplicaLocation(dataReplicaLocationModel);
            org.junit.Assert.assertNotNull(replicaId);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveReplicaLocation(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.registerReplicaLocation(dataReplicaLocationModel);
            boolean result = dataManager.removeReplicaLocation(replicaId);
            Assert.assertTrue(result);
            result = dataManager.removeReplicaLocation(dataReplicaLocationModel.getReplicaId());
            Assert.assertFalse(result);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetReplicaLocation(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.registerReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = dataManager.getReplicaLocation(replicaId);
            Assert.assertNotNull(persistedCopy);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateReplicaLocation(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.registerReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = dataManager.getReplicaLocation(replicaId);
            persistedCopy.setReplicaDescription("updated-description");
            dataManager.updateReplicaLocation(persistedCopy);
            persistedCopy = dataManager.getReplicaLocation(replicaId);
            Assert.assertTrue(persistedCopy.getReplicaDescription().equals("updated-description"));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetAllReplicaLocations(){
        try {
            String resourceId = dataManager.registerResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.registerReplicaLocation(dataReplicaLocationModel);
            List<DataReplicaLocationModel> replicaLocationModelList = dataManager.getAllReplicaLocations(resourceId);
            Assert.assertTrue(replicaLocationModelList.get(0).getReplicaId().equals(replicaId));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}