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
package org.apache.airavata.registry.core.repositories.replicacatalog;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaPersistentType;
import org.apache.airavata.registry.core.entities.replicacatalog.DataReplicaMetadataEntity;
import org.apache.airavata.registry.core.repositories.util.Initialize;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DataReplicaLocationRepositoryTest {

    private static Initialize initialize;
    private DataProductRepository dataProductRepository;
    private DataReplicaLocationRepository dataReplicaLocationRepository;
    private String gatewayId = "testGateway";
    private static final Logger logger = LoggerFactory.getLogger(DataReplicaLocationRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("replicacatalog-derby.sql");
            initialize.initializeDB();
            dataProductRepository = new DataProductRepository();
            dataReplicaLocationRepository = new DataReplicaLocationRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void DataReplicaLocationRepositoryTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel = new DataProductModel();
        testDataProductModel.setGatewayId(gatewayId);
        testDataProductModel.setOwnerName("testUser");
        testDataProductModel.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel.setProductName("productName");
        String productUri = dataProductRepository.registerDataProduct(testDataProductModel);
        assertTrue(dataProductRepository.isDataProductExists(productUri));

        DataReplicaLocationModel testDataReplicaLocationModel1 = new DataReplicaLocationModel();
        testDataReplicaLocationModel1.setReplicaName("replicaName1");
        testDataReplicaLocationModel1.setProductUri(productUri);
        String replicaId1 = dataReplicaLocationRepository.registerDataReplicaLocation(testDataReplicaLocationModel1);
        assertTrue(dataReplicaLocationRepository.isExists(replicaId1));

        DataReplicaLocationModel testDataReplicaLocationModel2 = new DataReplicaLocationModel();
        testDataReplicaLocationModel2.setReplicaName("replicaName2");
        testDataReplicaLocationModel2.setProductUri(productUri);
        String replicaId2 = dataReplicaLocationRepository.registerDataReplicaLocation(testDataReplicaLocationModel2);
        assertTrue(dataReplicaLocationRepository.isExists(replicaId2));

        DataReplicaMetadataEntity dataReplicaMetadataEntity1 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity1.setReplicaId(replicaId1);
        dataReplicaMetadataEntity1.setMetadataKey("dataKey1");
        dataReplicaMetadataEntity1.setMetadataValue("dataValue1");

        DataReplicaMetadataEntity dataReplicaMetadataEntity2 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity2.setReplicaId(replicaId1);
        dataReplicaMetadataEntity1.setMetadataKey("dataKey2");
        dataReplicaMetadataEntity1.setMetadataValue("dataValue2");

        Map<String, String> dataReplicaMetadataEntityMap = new HashMap<>();
        dataReplicaMetadataEntityMap.put(dataReplicaMetadataEntity1.getMetadataKey(), dataReplicaMetadataEntity1.getMetadataValue());
        dataReplicaMetadataEntityMap.put(dataReplicaMetadataEntity2.getMetadataKey(), dataReplicaMetadataEntity2.getMetadataValue());
        testDataReplicaLocationModel1.setReplicaMetadata(dataReplicaMetadataEntityMap);
        testDataReplicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        dataReplicaLocationRepository.updateDataReplicaLocation(testDataReplicaLocationModel1);

        DataReplicaLocationModel retrievedDataReplicaLocationModel = dataReplicaLocationRepository.getDataReplicaLocation(replicaId1);
        assertTrue(retrievedDataReplicaLocationModel.getReplicaMetadata().size() == 2);
        assertEquals(retrievedDataReplicaLocationModel.getReplicaPersistentType(), testDataReplicaLocationModel1.getReplicaPersistentType());

        testDataProductModel.setReplicaLocations(Arrays.asList(testDataReplicaLocationModel1, testDataReplicaLocationModel2));
        dataProductRepository.updateDataProduct(testDataProductModel);
        assertTrue(dataProductRepository.getDataProduct(productUri).getReplicaLocations().size() == 2);


        List<DataReplicaLocationModel> dataReplicaLocationModelList = dataReplicaLocationRepository.getAllDataReplicaLocations(productUri);
        assertTrue(dataReplicaLocationModelList.size() == 2);

        dataReplicaLocationRepository.removeDataReplicaLocation(replicaId1);
        assertFalse(dataReplicaLocationRepository.isDataReplicaLocationExists(replicaId1));

        dataReplicaLocationRepository.removeDataReplicaLocation(replicaId2);
        dataProductRepository.removeDataProduct(productUri);
    }

}
