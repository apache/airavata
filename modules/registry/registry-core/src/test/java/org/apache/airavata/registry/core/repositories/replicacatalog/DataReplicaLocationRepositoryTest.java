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
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataReplicaLocationRepositoryTest extends TestBase {

    private DataProductRepository dataProductRepository;
    private DataReplicaLocationRepository dataReplicaLocationRepository;
    private String gatewayId = "testGateway";
    
    public DataReplicaLocationRepositoryTest() {
        super(Database.REPLICA_CATALOG);
        dataProductRepository = new DataProductRepository();
        dataReplicaLocationRepository = new DataReplicaLocationRepository();
    }

    @Test
    public void dataReplicaLocationRepositoryTest() throws ReplicaCatalogException {
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
        String replicaId1 = dataReplicaLocationRepository.registerReplicaLocation(testDataReplicaLocationModel1);

        DataReplicaLocationModel testDataReplicaLocationModel2 = new DataReplicaLocationModel();
        testDataReplicaLocationModel2.setReplicaName("replicaName2");
        testDataReplicaLocationModel2.setProductUri(productUri);
        String replicaId2 = dataReplicaLocationRepository.registerReplicaLocation(testDataReplicaLocationModel2);

        DataReplicaMetadataEntity dataReplicaMetadataEntity1 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity1.setReplicaId(replicaId1);
        dataReplicaMetadataEntity1.setMetadataKey("dataKey1");
        dataReplicaMetadataEntity1.setMetadataValue("dataValue1");

        DataReplicaMetadataEntity dataReplicaMetadataEntity2 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity2.setReplicaId(replicaId1);
        dataReplicaMetadataEntity2.setMetadataKey("dataKey2");
        dataReplicaMetadataEntity2.setMetadataValue("dataValue2");

        Map<String, String> dataReplicaMetadataEntityMap = new HashMap<>();
        dataReplicaMetadataEntityMap.put(dataReplicaMetadataEntity1.getMetadataKey(), dataReplicaMetadataEntity1.getMetadataValue());
        dataReplicaMetadataEntityMap.put(dataReplicaMetadataEntity2.getMetadataKey(), dataReplicaMetadataEntity2.getMetadataValue());
        testDataReplicaLocationModel1.setReplicaMetadata(dataReplicaMetadataEntityMap);
        testDataReplicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        assertTrue(dataReplicaLocationRepository.updateReplicaLocation(testDataReplicaLocationModel1));

        DataReplicaLocationModel retrievedDataReplicaLocationModel = dataReplicaLocationRepository.getReplicaLocation(replicaId1);
        assertTrue(retrievedDataReplicaLocationModel.getReplicaMetadata().size() == 2);
        assertEquals(retrievedDataReplicaLocationModel.getReplicaPersistentType(), testDataReplicaLocationModel1.getReplicaPersistentType());
        // validUntilTime has a default value
        assertEquals(0, retrievedDataReplicaLocationModel.getValidUntilTime());

        testDataProductModel.setReplicaLocations(Arrays.asList(testDataReplicaLocationModel1, testDataReplicaLocationModel2));
        dataProductRepository.updateDataProduct(testDataProductModel);
        assertTrue(dataProductRepository.getDataProduct(productUri).getReplicaLocations().size() == 2);

        List<DataReplicaLocationModel> dataReplicaLocationModelList = dataReplicaLocationRepository.getAllReplicaLocations(productUri);
        assertTrue(dataReplicaLocationModelList.size() == 2);

        dataReplicaLocationRepository.removeReplicaLocation(replicaId1);
        dataReplicaLocationRepository.removeReplicaLocation(replicaId2);
        dataProductRepository.removeDataProduct(productUri);
    }

}
