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
package org.apache.airavata.replica.catalog;

import org.apache.airavata.replica.catalog.utils.AppCatInit;
import org.apache.airavata.replica.catalog.utils.DataCatInit;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplicaCatalogImplTest {
    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogImplTest.class);
    private static AppCatInit appCatInit;
    private static DataCatInit dataCatInit;
    private static ReplicaCatalog replicaCatalog;
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
            replicaCatalog = new ReplicaCatalogImpl(dataCatalog);
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("1-st-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g75.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataReplicaLocationModel.setDataLocations(dataLocations);
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
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = replicaCatalog.removeResource("234234234");
            Assert.assertFalse(result);
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = replicaCatalog.removeResource(resourceId);
            Assert.assertTrue(result);
            result = replicaCatalog.removeResource(resourceId);
            Assert.assertFalse(result);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = replicaCatalog.getResource(resourceId);
            Assert.assertNotNull(persistedCopy);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = replicaCatalog.updateResource(dataResourceModel);
            Assert.assertFalse(result);
            replicaCatalog.publishResource(dataResourceModel);
            dataResourceModel.setResourceName("updated-name");
            replicaCatalog.updateResource(dataResourceModel);
            dataResourceModel = replicaCatalog.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getResourceName().equals("updated-name"));
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPublishReplicaLocation(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            org.junit.Assert.assertNotNull(replicaId);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveReplicaLocation(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            boolean result = replicaCatalog.removeReplicaLocation(replicaId);
            Assert.assertTrue(result);
            result = replicaCatalog.removeReplicaLocation(dataReplicaLocationModel.getReplicaId());
            Assert.assertFalse(result);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetReplicaLocation(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = replicaCatalog.getReplicaLocation(replicaId);
            Assert.assertNotNull(persistedCopy);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateReplicaLocation(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = replicaCatalog.getReplicaLocation(replicaId);
            persistedCopy.setReplicaDescription("updated-description");
            replicaCatalog.updateReplicaLocation(persistedCopy);
            persistedCopy = replicaCatalog.getReplicaLocation(replicaId);
            Assert.assertTrue(persistedCopy.getReplicaDescription().equals("updated-description"));
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetAllReplicaLocations(){
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            List<DataReplicaLocationModel> replicaLocationModelList = replicaCatalog.getAllReplicaLocations(resourceId);
            Assert.assertTrue(replicaLocationModelList.get(0).getReplicaId().equals(replicaId));
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}