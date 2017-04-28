/**
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
 */
package org.apache.airavata.replica.catalog;

import org.apache.airavata.replica.catalog.util.Initialize;
import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ReplicaCatalog;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ReplicaCatalogTest {
    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogTest.class);
    private static Initialize initialize;
    private static ReplicaCatalog replicacatalog;
    private static DataProductModel dataProductModel;
    private static DataReplicaLocationModel replicaLocationModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            initialize = new Initialize("replicacatalog-derby.sql");
            initialize.initializeDB();
            replicacatalog = RegistryFactory.getReplicaCatalog();
            dataProductModel = new DataProductModel();
            dataProductModel.setProductName("test-file.txt");
            dataProductModel.setOwnerName("scnakandala");
            dataProductModel.setGatewayId("default");
            dataProductModel.setDataProductType(DataProductType.FILE);
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
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void testReplicaCatalog(){
        try {
            String productUri = replicacatalog.registerDataProduct(dataProductModel);
            org.junit.Assert.assertNotNull(productUri);
            dataProductModel = replicacatalog.getDataProduct(productUri);
            Assert.assertTrue(replicacatalog.searchDataProductsByName(dataProductModel.getGatewayId(), dataProductModel.getOwnerName(),
                    dataProductModel.getProductName().substring(1, 5), -1, 0).size() == 1);
            Assert.assertNotNull(dataProductModel);
            boolean result = replicacatalog.removeDataProduct(productUri);
            Assert.assertTrue(result);
            productUri = replicacatalog.registerDataProduct(dataProductModel);
            Assert.assertNotNull(productUri);
            result = replicacatalog.removeDataProduct(productUri);
            Assert.assertTrue(result);
            result = replicacatalog.removeDataProduct(productUri);
            Assert.assertFalse(result);
        } catch (ReplicaCatalogException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}