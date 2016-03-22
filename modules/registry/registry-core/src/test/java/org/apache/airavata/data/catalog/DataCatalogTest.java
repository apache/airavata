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
import org.apache.airavata.model.data.product.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataCatalogTest {
    private final static Logger logger = LoggerFactory.getLogger(DataCatalogTest.class);
    private static Initialize initialize;
    private static DataCatalog datacatalog;
    private static DataProductModel dataProductModel;
    private static DataReplicaLocationModel replicaLocationModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            initialize = new Initialize("datacatalog-derby.sql");
            initialize.initializeDB();
            datacatalog = RegistryFactory.getDataCatalog();
            dataProductModel = new DataProductModel();
            dataProductModel.setProductName("test-file.txt");
            HashMap<String, String> resMetadata = new HashMap<>();
            resMetadata.put("name", "name");
            dataProductModel.setProductMetadata(resMetadata);

            replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setReplicaName("1-st-replica");
            replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.COMPUTE_RESOURCE);
            replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.PERSISTENT);
            HashMap<String, String> rMetadata = new HashMap<>();
            rMetadata.put("name", "name");
            replicaLocationModel.setReplicaMetadata(rMetadata);
            dataProductModel.addToReplicaLocations(replicaLocationModel);
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
    public void testPublishDataProduct(){
        try {
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            org.junit.Assert.assertNotNull(productUri);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataProduct(){
        try {
            boolean result = datacatalog.removeDataProduct("234234234");
            Assert.assertFalse(result);
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            Assert.assertNotNull(productUri);
            result = datacatalog.removeDataProduct(productUri);
            Assert.assertTrue(result);
            result = datacatalog.removeDataProduct(productUri);
            Assert.assertFalse(result);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataProduct(){
        try {
            dataProductModel.setDataProductType(DataProductType.COLLECTION);
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            Assert.assertNotNull(productUri);
            DataProductModel persistedCopy = datacatalog.getDataProduct(productUri);
            Assert.assertNotNull(persistedCopy);
            dataProductModel.setParentProductUri(productUri);
            dataProductModel.setDataProductType(DataProductType.FILE);
            datacatalog.registerDataProduct(dataProductModel);
            datacatalog.registerDataProduct(dataProductModel);
            persistedCopy = datacatalog.getDataProduct(productUri);
            Assert.assertTrue(persistedCopy.getChildProducts().size()==2);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataProduct(){
        try {
            dataProductModel.setProductUri(UUID.randomUUID().toString());
            boolean result = datacatalog.updateDataProduct(dataProductModel);
            Assert.assertFalse(result);
            datacatalog.registerDataProduct(dataProductModel);
            dataProductModel.setProductName("updated-name");
            datacatalog.updateDataProduct(dataProductModel);
            dataProductModel = datacatalog.getDataProduct(dataProductModel.getProductUri());
            Assert.assertTrue(dataProductModel.getProductName().equals("updated-name"));
            Assert.assertTrue(dataProductModel.getProductMetadata().size()==1);
            dataProductModel.getProductMetadata().put("name2","name2");
            datacatalog.updateDataProduct(dataProductModel);
            dataProductModel = datacatalog.getDataProduct(dataProductModel.getProductUri());
            Assert.assertTrue(dataProductModel.getProductMetadata().size()==2);
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPublishReplicaLocation(){
        try {
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            replicaLocationModel.setProductUri(productUri);
            String replicaId = datacatalog.registerReplicaLocation(replicaLocationModel);
            Assert.assertNotNull(replicaId);;
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveReplicaLocation(){
        try {
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            replicaLocationModel.setProductUri(productUri);
            String replicaId = datacatalog.registerReplicaLocation(replicaLocationModel);
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
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            replicaLocationModel.setProductUri(productUri);
            String replicaId = datacatalog.registerReplicaLocation(replicaLocationModel);
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
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            replicaLocationModel.setProductUri(productUri);
            String replicaId = datacatalog.registerReplicaLocation(replicaLocationModel);
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
            String productUri = datacatalog.registerDataProduct(dataProductModel);
            replicaLocationModel.setProductUri(productUri);
            datacatalog.registerReplicaLocation(replicaLocationModel);
            List<DataReplicaLocationModel> replicaLocationModelList = datacatalog.getAllReplicaLocations(productUri);
            Assert.assertNotNull(replicaLocationModelList.get(0).getReplicaId());
        } catch (DataCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}