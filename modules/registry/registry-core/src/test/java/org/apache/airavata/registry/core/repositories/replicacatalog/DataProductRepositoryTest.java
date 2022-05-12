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
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.data.replica.ReplicaPersistentType;
import org.apache.airavata.registry.core.entities.replicacatalog.DataProductMetadataEntity;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataProductRepositoryTest extends TestBase {

    private DataProductRepository dataProductRepository;
    private String gatewayId = "testGateway";
    private String userId = "testUser";
    private String productName = "testProduct";
    
    public DataProductRepositoryTest() {
        super(Database.REPLICA_CATALOG);
    }

    public void setUp() throws Exception {
        super.setUp();
        dataProductRepository = new DataProductRepository();
    }

    @Test
    public void dataProductRepositoryTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel1 = new DataProductModel();
        testDataProductModel1.setGatewayId(gatewayId);
        testDataProductModel1.setOwnerName(userId);
        testDataProductModel1.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel1.setProductName(productName);

        String productUri1 = dataProductRepository.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductRepository.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel1 = dataProductRepository.getDataProduct(productUri1);
        assertEquals(retrievedDataProductModel1.getProductUri(), productUri1);

        DataProductModel testDataProductModel2 = new DataProductModel();
        testDataProductModel2.setGatewayId(gatewayId);
        testDataProductModel2.setOwnerName(userId);
        testDataProductModel2.setDataProductType(DataProductType.FILE);
        testDataProductModel2.setProductName(productName);

        String productUri2 = dataProductRepository.registerDataProduct(testDataProductModel2);
        assertTrue(dataProductRepository.isDataProductExists(productUri2));

        DataProductMetadataEntity dataProductMetadataEntity = new DataProductMetadataEntity();
        dataProductMetadataEntity.setProductUri(productUri2);
        dataProductMetadataEntity.setMetadataKey("dataKey");
        dataProductMetadataEntity.setMetadataValue("dataValue");

        Map<String, String> dataProductMetadataEntityMap = new HashMap<>();
        dataProductMetadataEntityMap.put(dataProductMetadataEntity.getMetadataKey(), dataProductMetadataEntity.getMetadataValue());
        testDataProductModel2.setProductMetadata(dataProductMetadataEntityMap);
        testDataProductModel2.setParentProductUri(productUri1);
        assertTrue(dataProductRepository.updateDataProduct(testDataProductModel2));

        DataProductModel retrievedDataProductModel2 = dataProductRepository.getDataProduct(productUri2);
        assertTrue(retrievedDataProductModel2.getProductMetadata().size() == 1);

        DataProductModel retrievedParentDataProductModel = dataProductRepository.getParentDataProduct(productUri2);
        assertEquals(retrievedParentDataProductModel.getProductUri(), productUri1);

        List<DataProductModel> childDataProductList = dataProductRepository.getChildDataProducts(productUri1);
        assertTrue(childDataProductList.size() == 1);

        List<DataProductModel> dataProductModelList = dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, -1, 0);
        assertTrue(dataProductModelList.size() == 2);

        dataProductRepository.removeDataProduct(productUri1);
        assertFalse(dataProductRepository.isDataProductExists(productUri1));

        dataProductRepository.removeDataProduct(productUri2);
    }

    @Test
    public void testDataProductWithReplicaLocation() throws ReplicaCatalogException {
        DataProductModel testDataProductModel1 = new DataProductModel();
        testDataProductModel1.setGatewayId(gatewayId);
        testDataProductModel1.setOwnerName(userId);
        testDataProductModel1.setDataProductType(DataProductType.FILE);
        testDataProductModel1.setProductName(productName);

        DataReplicaLocationModel replicaLocationModel1 = new DataReplicaLocationModel();
        replicaLocationModel1.setFilePath("/path/to/file.dat");
        replicaLocationModel1.setReplicaDescription("Description of replica");
        replicaLocationModel1.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
        replicaLocationModel1.setReplicaName("file.dat");
        replicaLocationModel1.setStorageResourceId("storage_resource_id");
        replicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.PERSISTENT);

        testDataProductModel1.addToReplicaLocations(replicaLocationModel1);

        String productUri1 = dataProductRepository.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductRepository.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel1 = dataProductRepository.getDataProduct(productUri1);
        assertEquals(productUri1, retrievedDataProductModel1.getProductUri());

        assertEquals(1, retrievedDataProductModel1.getReplicaLocationsSize());
        DataReplicaLocationModel retrievedReplicaLocationModel1 = retrievedDataProductModel1.getReplicaLocations().get(0);
        assertEquals(productUri1, retrievedReplicaLocationModel1.getProductUri());
        // validUntilTime has a default value
        assertEquals(0, retrievedReplicaLocationModel1.getValidUntilTime());

        dataProductRepository.removeDataProduct(productUri1);
        assertFalse(dataProductRepository.isDataProductExists(productUri1));
    }

}
