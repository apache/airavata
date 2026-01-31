/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.repositories.replicacatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataProductType;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.model.ReplicaPersistentType;
import org.apache.airavata.registry.exception.RegistryExceptions.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.DataProductService;
import org.apache.airavata.registry.services.DataReplicaLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for DataReplicaLocationRepository functionality.
 *
 * <p>Note: Metadata for data replicas is stored in the unified MetadataEntity table
 * with parentType = MetadataParentType.DATA_REPLICA. The DataReplicaLocationService handles
 * metadata persistence and retrieval through MetadataRepository.
 */

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class DataReplicaLocationRepositoryTest extends TestBase {

    private final DataProductService dataProductService;
    private final DataReplicaLocationService dataReplicaLocationService;

    private String gatewayId = "testGateway";

    public DataReplicaLocationRepositoryTest(
            DataProductService dataProductService, DataReplicaLocationService dataReplicaLocationService) {
        this.dataProductService = dataProductService;
        this.dataReplicaLocationService = dataReplicaLocationService;
    }

    @Test
    public void dataReplicaLocationServiceTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel = new DataProductModel();
        testDataProductModel.setGatewayId(gatewayId);
        testDataProductModel.setOwnerName("testUser");
        testDataProductModel.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel.setProductName("productName");
        String productUri = dataProductService.registerDataProduct(testDataProductModel);
        assertTrue(dataProductService.isDataProductExists(productUri));

        DataReplicaLocationModel testDataReplicaLocationModel1 = new DataReplicaLocationModel();
        testDataReplicaLocationModel1.setReplicaName("replicaName1");
        testDataReplicaLocationModel1.setProductUri(productUri);
        String replicaId1 = dataReplicaLocationService.registerReplicaLocation(testDataReplicaLocationModel1);

        DataReplicaLocationModel testDataReplicaLocationModel2 = new DataReplicaLocationModel();
        testDataReplicaLocationModel2.setReplicaName("replicaName2");
        testDataReplicaLocationModel2.setProductUri(productUri);
        String replicaId2 = dataReplicaLocationService.registerReplicaLocation(testDataReplicaLocationModel2);

        Map<String, String> replicaMetadata = new HashMap<>();
        replicaMetadata.put("dataKey1", "dataValue1");
        replicaMetadata.put("dataKey2", "dataValue2");
        testDataReplicaLocationModel1.setReplicaMetadata(replicaMetadata);
        testDataReplicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        assertTrue(dataReplicaLocationService.updateReplicaLocation(testDataReplicaLocationModel1));

        DataReplicaLocationModel retrievedDataReplicaLocationModel =
                dataReplicaLocationService.getReplicaLocation(replicaId1);
        assertTrue(retrievedDataReplicaLocationModel.getReplicaMetadata().size() == 2);
        assertEquals(
                retrievedDataReplicaLocationModel.getReplicaPersistentType(),
                testDataReplicaLocationModel1.getReplicaPersistentType());

        assertEquals(0, retrievedDataReplicaLocationModel.getValidUntilTime());

        // Replica locations were already registered via dataReplicaLocationService
        // Verify they're accessible via the replica location service
        List<DataReplicaLocationModel> dataReplicaLocationModelList =
                dataReplicaLocationService.getAllReplicaLocations(productUri);
        assertTrue(dataReplicaLocationModelList.size() == 2);

        dataReplicaLocationService.removeReplicaLocation(replicaId1);
        dataReplicaLocationService.removeReplicaLocation(replicaId2);
        dataProductService.removeDataProduct(productUri);
    }
}
