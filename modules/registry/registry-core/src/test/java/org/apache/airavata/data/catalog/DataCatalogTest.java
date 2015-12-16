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
package org.apache.airavata.data.catalog;

import org.apache.airavata.data.catalog.util.Initialize;
import org.apache.airavata.model.data.resource.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataCatalogTest {
    private final static Logger logger = LoggerFactory.getLogger(DataCatalogTest.class);
    private static Initialize initialize;
    private static DataCatalog datacatalog;
    private static DataResourceModel dataResourceModel;
    private static DataReplicaLocationModel replicaLocationModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            initialize = new Initialize("datacatalog-derby.sql");
            initialize.initializeDB();
            datacatalog = RegistryFactory.getDataCatalog();
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            HashMap<String, String> resMetadata = new HashMap<>();
            resMetadata.put("name", "name");
            dataResourceModel.setResourceMetadata(resMetadata);

            replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setReplicaName("1-st-replica");
            replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.COMPUTE_RESOURCE);
            replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.PERSISTENT);
            HashMap<String, String> rMetadata = new HashMap<>();
            rMetadata.put("name", "name");
            replicaLocationModel.setReplicaMetadata(rMetadata);
            dataResourceModel.addToReplicaLocations(replicaLocationModel);
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void testPublishDataResource(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = datacatalog.removeResource("234234234");
            Assert.assertFalse(result);
            String resourceId = datacatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = datacatalog.removeResource(resourceId);
            Assert.assertTrue(result);
            result = datacatalog.removeResource(resourceId);
            Assert.assertFalse(result);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            dataResourceModel.setDataResourceType(DataResourceType.COLLECTION);
            String resourceId = datacatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = datacatalog.getResource(resourceId);
            Assert.assertNotNull(persistedCopy);
            dataResourceModel.setParentResourceId(resourceId);
            dataResourceModel.setDataResourceType(DataResourceType.FILE);
            datacatalog.publishResource(dataResourceModel);
            datacatalog.publishResource(dataResourceModel);
            persistedCopy = datacatalog.getResource(resourceId);
            Assert.assertTrue(persistedCopy.getChildResources().size()==2);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = datacatalog.updateResource(dataResourceModel);
            Assert.assertFalse(result);
            datacatalog.publishResource(dataResourceModel);
            dataResourceModel.setResourceName("updated-name");
            datacatalog.updateResource(dataResourceModel);
            dataResourceModel = datacatalog.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getResourceName().equals("updated-name"));
            Assert.assertTrue(dataResourceModel.getResourceMetadata().size()==1);
            dataResourceModel.getResourceMetadata().put("name2","name2");
            datacatalog.updateResource(dataResourceModel);
            dataResourceModel = datacatalog.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getResourceMetadata().size()==2);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPublishReplicaLocation(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            replicaLocationModel.setResourceId(resourceId);
            String replicaId = datacatalog.publishReplicaLocation(replicaLocationModel);
            Assert.assertNotNull(replicaId);;
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveReplicaLocation(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            replicaLocationModel.setResourceId(resourceId);
            String replicaId = datacatalog.publishReplicaLocation(replicaLocationModel);
            boolean result = datacatalog.removeReplicaLocation(replicaId);
            Assert.assertTrue(result);
            result = datacatalog.removeReplicaLocation(replicaLocationModel.getReplicaId());
            Assert.assertFalse(result);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetReplicaLocation(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            replicaLocationModel.setResourceId(resourceId);
            String replicaId = datacatalog.publishReplicaLocation(replicaLocationModel);
            DataReplicaLocationModel persistedCopy = datacatalog.getReplicaLocation(replicaId);
            Assert.assertNotNull(persistedCopy);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateReplicaLocation(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            replicaLocationModel.setResourceId(resourceId);
            String replicaId = datacatalog.publishReplicaLocation(replicaLocationModel);
            DataReplicaLocationModel persistedCopy = datacatalog.getReplicaLocation(replicaId);
            persistedCopy.setReplicaDescription("updated-description");
            datacatalog.updateReplicaLocation(persistedCopy);
            persistedCopy = datacatalog.getReplicaLocation(replicaId);
            Assert.assertTrue(persistedCopy.getReplicaDescription().equals("updated-description"));
            Assert.assertEquals(persistedCopy.getReplicaLocationCategory(), replicaLocationModel.getReplicaLocationCategory());
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetAllReplicaLocations(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            replicaLocationModel.setResourceId(resourceId);
            datacatalog.publishReplicaLocation(replicaLocationModel);
            List<DataReplicaLocationModel> replicaLocationModelList = datacatalog.getAllReplicaLocations(resourceId);
            Assert.assertNotNull(replicaLocationModelList.get(0).getReplicaId());
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}